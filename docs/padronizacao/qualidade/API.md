[← Índice](../../PADRONIZACAO_INDEX.md) · [← ESTRUTURA.md](../fundamentos/ESTRUTURA.md)

# API — Java 17 / Spring Boot 2.7

> **Escopo**: a API pública de **Java 17 + Spring Boot 2.7** deste projeto:
> REST puro, versionamento no caminho, erro padronizado, fronteira de tipos,
> serialização determinística e contrato testado. O mecanismo é feito de
> filtros e `@RestControllerAdvice`, e o Spring 5 não traz um tipo de erro
> RFC 7807 pronto. Os exemplos usam o domínio de cartões de benefício. Ver
> [Particularidades do Spring Boot 2.7](#particularidades-do-spring-boot-27).
>
> **Nota sobre exemplos**: os trechos de código e payloads abaixo são
> ilustrativos — a regra normativa é o texto conceitual, não os nomes
> exatos de rota ou tipo. O **legado do desafio** saiu da árvore
> (ver [`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md#este-documento-é-vivo));
> este documento descreve o contrato que o substituiu.
>
> **Documentos relacionados**: [`VALIDACAO.md`](VALIDACAO.md),
> [`ERROS.md`](ERROS.md) e [`BANCO.md`](BANCO.md).

Define a superfície pública única do sistema: REST, roteamento e códigos
de status, versionamento, corpo de erro padronizado, pipeline de
requisição, fronteira de tipos, serialização determinística, contrato e
idempotência.

---

## Particularidades do Spring Boot 2.7

| Tema | Regra neste projeto |
|---|---|
| Erro padronizado | O Spring 5 **não tem** tipo pronto (`ProblemDetail` só no Spring 6/Boot 3). Corpo próprio `MensagemProblema` com o **formato RFC 7807** e `Content-Type: application/problem+json` |
| Pipeline | `javax.servlet.Filter` (borda externa) → `DispatcherServlet` → `HandlerInterceptor` → resolução/validação de argumentos → controller → `@RestControllerAdvice` |
| Ordem do pipeline | `FilterRegistrationBean#setOrder(...)` — menor número = mais externo |
| Controller | `@GetMapping`, `ResponseEntity<T>` / tipo de retorno direto |
| Validação | Bean Validation (`javax.validation`): `@Valid` no corpo, `@Validated` + `@Min`/`@Max` em parâmetros |
| Autenticação/autorização | **Fora de escopo** — Spring Security seria dependência nova, proibida pelo desafio (ver [seção 10](#10-autenticação-autorização-e-fronteira)) |
| Cache de saída | Não se aplica |
| Cancelamento da requisição | Não existe no modelo Servlet (uma thread por requisição); o recurso é **limitar a duração** (timeout de transação e de lock) |
| Bloqueio da thread | É o modelo normal; a regra é **manter a transação curta** |
| Serialização | Jackson configurado em `application.yml` (`spring.jackson.*`) — uma única vez |
| Namespace de validação | `javax.validation` (Boot 2.7 usa `javax.*`, **não** `jakarta.*`) |

---

## 1. REST Puro como API Pública

O backend expõe uma API REST como **única** superfície pública para
qualquer cliente. Não há caminho privilegiado: tudo que o sistema faz
existe como endpoint versionado.

**Por quê:**

- Permite que qualquer cliente (front-end, app nativo, integração de
  terceiro) consuma o mesmo contrato, sem exceção nem atalho.
- **Nenhuma superfície privada "por baixo da API"**: endpoint interno,
  rota fora de `/api/v1/`, flag de debug que abre caminho paralelo. No
  estado atual, o `/h2-console` é exatamente isso: o README do desafio o
  documenta como parte do ambiente local, então ele fica habilitado no
  perfil padrão, mas **precisa ser desabilitado no perfil `prod`**
  (`application-prod.yml`) — ver [`SEGURANCA.md`](SEGURANCA.md#3-mínimo-de-privilégios).

**Rotas** são em kebab-case, plural, em português:

```text
CORRETO
GET    /api/v1/consumidores
GET    /api/v1/consumidores/{id}
POST   /api/v1/consumidores
PUT    /api/v1/consumidores/{id}
POST   /api/v1/creditos                      ← crédito é um recurso que se cria
POST   /api/v1/compras                       ← compra é um recurso que se cria

ERRADO
GET    /consumer/consumerList                ← inglês misturado, verbo na rota, sem versão (legado)
POST   /consumer/createConsumer              ← verbo na rota
POST   /consumer/updateConsumer              ← verbo na rota, atualização via POST
GET    /consumer/setcardbalance              ← GET que altera estado + verbo na rota
GET    /consumer/buy                         ← GET que altera estado
GET    /api/v1/consumidor                    ← singular
GET    /consumidores                         ← sem versão
```

### Mapa do Legado Para o Novo Contrato

O README do desafio autoriza alterar os contratos. A tabela abaixo é o
registro dessa mudança — vai na descrição do PR:

| Legado | Novo | Mudança |
|---|---|---|
| `GET /consumer/consumerList` (50 mil registros) | `GET /api/v1/consumidores?pagina=1&tamanhoPagina=20` | paginado, envelope único |
| `POST /consumer/createConsumer` | `POST /api/v1/consumidores` | 201 + `Location`; cartões entram com saldo zero |
| `POST /consumer/updateConsumer` | `PUT /api/v1/consumidores/{id}` | **não aceita saldo**; 404 se o id não existe |
| — | `GET /api/v1/consumidores/{id}` | necessário para o `Location` do 201 |
| `GET /consumer/setcardbalance` | `POST /api/v1/creditos` | `POST`; cartão e valor no **corpo**, não na URL |
| `GET /consumer/buy` | `POST /api/v1/compras` | `POST`; devolve comprovante |

**Dado sensível não vai na URL.** Número de cartão e valor em query string
(como no legado) acabam em log de acesso, histórico de navegador e
cabeçalho de referência. Operações que carregam número de cartão recebem
o dado no **corpo** — por isso `/creditos` e `/compras` são recursos de
primeiro nível, e não sub-recursos de `/cartoes/{numero}`.

**Consequência:** cada endpoint novo é superfície pública a manter,
testar e versionar — adicionar rota é assumir contrato com quem já
consome.

---

## 2. Versionamento `/api/v1/`

Toda rota da API pública começa com `/api/v1/`. Mudanças quebradoras
incrementam a versão (`/api/v2/`). Versões antigas são mantidas por um
período de depreciação **anunciado**, nunca removidas em silêncio.

```java
// CORRETO — o prefixo versionado vive no @RequestMapping da classe
@RestController
@RequestMapping("/api/v1/consumidores")
public class ConsumidorController { /* ... */ }

// ERRADO
@RestController
@RequestMapping("/consumer")
public class ConsumerController { /* ... */ }
```

A versão vive **só no caminho** — nunca em cabeçalho, query string ou
tipo de mídia negociado: um cliente deve conseguir dizer qual versão está
usando lendo a URL.

### O Que é Mudança Quebradora

| Mudança | Quebra? | O que fazer |
|---|---|---|
| Remover ou renomear campo da resposta | **Sim** | nova versão |
| Mudar tipo de campo (string → número, data com fuso → sem fuso) | **Sim** | nova versão |
| Tornar campo opcional que era obrigatório na entrada | **Sim** | nova versão |
| Mudar código de status de um caso (200 → 409) | **Sim** | nova versão |
| Mudar semântica de enum (renomear, trocar significado) | **Sim** | nova versão |
| Adicionar campo **novo e opcional** na resposta | Não | aditivo, mesma versão |
| Adicionar endpoint novo | Não | mesma versão |
| Corrigir mensagem de `detail` (texto humano) | Não | mesma versão |

> **Atenção ao rejeitar campos desconhecidos na entrada** (ver
> [seção 6](#6-serialização-determinística)): tornar o servidor estrito
> faz um campo novo enviado por um cliente desatualizado virar 400. Por
> isso a lista de campos aceitos de cada `Mensagem*` é parte do contrato.

**Por quê:** um cliente não acompanha a implantação do servidor — o que
muda em silêncio quebra em produção, com o cliente só descobrindo na hora
da falha. Toda mudança de formato é mudança versionada, nunca ajuste
unilateral.

---

## 3. Erro Padronizado (RFC 7807)

Erros da API retornam um corpo no formato **Problem Details (RFC 7807)**,
com `Content-Type: application/problem+json` — nunca stack trace,
exceção crua, página HTML de erro, o JSON padrão do Spring
(`timestamp/status/error/path`) nem `{ "erro": "..." }` inventado a cada
endpoint.

O Spring Boot 2.7 **não traz** esse tipo pronto; ele é a `record`
`MensagemProblema`, em `api.comum.problema` (ver `ESTRUTURA.md`):

```java
/**
 * Corpo de erro da API no formato RFC 7807 ...
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record MensagemProblema(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    String idRequisicao,
    Map<String, List<String>> errors) { }
```

```json
{
  "type": "urn:consumer-pat:erro:CPT-NEN-CRT-001",
  "title": "Cartão não encontrado",
  "status": 404,
  "detail": "Não existe cartão de alimentação com o número informado.",
  "instance": "/api/v1/compras",
  "idRequisicao": "0f1c9c1e-8a45-4d5e-9f77-1a2b3c4d5e6f"
}
```

- **`type`** é uma URI estável, identificadora **de máquina** — é o que o
  cliente compara; nunca varia entre idiomas nem entre implantações.
  Usamos **URN** com o **código do erro**
  (`urn:consumer-pat:erro:CPT-<CATEGORIA>-<ÁREA>-<SEQ>`, ver
  [`ERROS.md`](ERROS.md#8-código-de-erro-legível-e-verificado-por-teste))
  para não inventar um domínio que não existe. **`title`** é humano e pode mudar.
- **`status`** espelha o código HTTP real do caso.
- **`detail`** descreve o caso concreto, **sem vazar internals** — nunca
  SQL, nome de tabela, stack, nem o número do cartão completo.
- **`idRequisicao`** liga o erro ao log da mesma requisição (ver
  [seção 4](#4-pipeline-de-requisição)) — o usuário reporta o ID, o
  operador encontra o log.

### Erros de Validação: Campo a Campo

Falha de validação de borda usa o membro `errors`, com o campo de entrada
como chave. **O mesmo formato** sai quando é o **domínio** que recusa uma
entrada malformada (`ErroValidacao`, via `camposComErro()`) — o cliente não
distingue quem recusou ([`VALIDACAO.md`](VALIDACAO.md#3-borda-domínio-e-estado--três-perguntas)).
O valor rejeitado **nunca** vai ao corpo, e o texto do Jackson é traduzido
([`VALIDACAO.md`](VALIDACAO.md#6-um-só-formato-de-400)):

```json
{
  "type": "urn:consumer-pat:erro:CPT-VAL-SIS-001",
  "title": "Entrada inválida",
  "status": 400,
  "detail": "Um ou mais campos precisam de correção.",
  "instance": "/api/v1/compras",
  "idRequisicao": "0f1c9c1e-8a45-4d5e-9f77-1a2b3c4d5e6f",
  "errors": {
    "numeroCartao": ["não deve estar em branco"],
    "valor": ["deve ser maior que zero"]
  }
}
```

### Mapeamento Erro → Status

O mapeamento é **único e centralizado**, nunca `status` escolhido dentro
de cada controller. Há duas fontes de erro, com dois tradutores:

1. **Erro de domínio** (`Resultado.Falha`): cada `ErroDominio` carrega uma
   `CategoriaErro`, e **o status HTTP é dado do próprio enum** (nível 1 de
   [`CONDICIONAIS.md`](../codigo/CONDICIONAIS.md)). `RespostaDeResultado`
   converte sem `if`.
2. **Exceção de borda ou de infraestrutura**: `@ExceptionHandler` em
   `FiltroDeProblemaDeApi` (`@RestControllerAdvice`), um método por tipo de
   exceção.

| Origem | Status | Observação |
|---|---|---|
| `CategoriaErro.VALIDACAO` / `MethodArgumentNotValidException` / `ConstraintViolationException` / `HttpMessageNotReadableException` / `MethodArgumentTypeMismatchException` | 400 | com `errors` campo a campo quando houver campo |
| `CategoriaErro.NAO_ENCONTRADO` / `NoHandlerFoundException` | 404 | cartão, consumidor ou rota inexistente |
| `HttpRequestMethodNotSupportedException` | 405 | com cabeçalho `Allow` |
| `CategoriaErro.CONFLITO` / `DataIntegrityViolationException` / `ObjectOptimisticLockingFailureException` / `PessimisticLockingFailureException` | 409 | ver [seção 11](#11-concorrência-idempotência-e-conflitos) |
| `CategoriaErro.REGRA_DE_NEGOCIO` (ex.: saldo insuficiente) | 422 | o 400 fica só para entrada malformada; aqui a entrada é válida e a regra de negócio a recusa |
| falha inesperada (qualquer outra `Exception`) | 500 | `detail` genérico — **nunca** SQL, stack ou mensagem da exceção |

```java
// CORRETO — status vem do enum; o tradutor não decide nada
public enum CategoriaErro {
    NAO_ENCONTRADO(404), REGRA_DE_NEGOCIO(422), CONFLITO(409), VALIDACAO(400);
    // ...
}

// ERRADO — status escolhido dentro do controller (foi o legado: tudo 200 ou 500)
@PostMapping("/compras")
ResponseEntity<?> comprar(...) {
    try { /* ... */ return ResponseEntity.ok().build(); }
    catch (Exception e) { return ResponseEntity.status(500).body(e.getMessage()); }
}
```

### Cobrindo o Que o `@RestControllerAdvice` Não Alcança

O `@RestControllerAdvice` só trata exceção lançada **dentro** do
`DispatcherServlet`. Duas lacunas do Spring Boot 2.7 precisam de
configuração para que o corpo padronizado valha sempre (propostas — validar
ao aplicar, ainda não executadas):

```yaml
# application.yml
spring:
  mvc:
    throw-exception-if-no-handler-found: true   # rota inexistente vira NoHandlerFoundException → 404 padronizado
  web:
    resources:
      add-mappings: false                        # sem isso, o 404 cai no handler de recursos estáticos
server:
  error:
    include-message: never                       # nada de texto da exceção no corpo
    include-stacktrace: never
    include-binding-errors: never
```

- **Filtro nunca lança**: filtros do projeto só anotam (MDC, log). Uma
  exceção num filtro escapa do advice e cai no `/error` do Boot, com o
  formato que o projeto proíbe.
- Se o `swagger-ui` deixar de abrir com `add-mappings: false`, registrar o
  caso e tratar à parte — o Swagger não é superfície do contrato.

**Por quê:** enquanto o status for escolhido caso a caso, o cliente é
obrigado a ler o corpo para descobrir o que aconteceu — e dois clientes
descobrem coisas diferentes do mesmo erro. Um contrato de status único é o
que permite ao consumidor tratar falha **sem** interpretar texto.

---

## 4. Pipeline de Requisição

A requisição atravessa uma cadeia de componentes de responsabilidade única
cada, **nesta ordem** — a ordem é parte do contrato, não detalhe de
implementação (ver [`CONDICIONAIS.md`](../codigo/CONDICIONAIS.md) §6).

No Spring, "middleware" se divide em camadas com mecanismos distintos:

| # | Etapa | Mecanismo | Por que nesta posição |
|---|---|---|---|
| 1 | Cabeçalhos de segurança | Servlet `Filter` mais externo (`HIGHEST_PRECEDENCE`), só em `/api/*`; o HTTPS/HSTS é terminado fora da aplicação | protegem toda resposta, inclusive as de erro ([`SEGURANCA.md`](SEGURANCA.md#6-cabeçalhos-de-segurança)) |
| 2 | Correlação de requisição (`idRequisicao`) | Servlet `Filter` logo **dentro** do 1 (`HIGHEST_PRECEDENCE + 1`) | primeiro a gerar dado: todo log abaixo já carrega o ID via `MDC` |
| 3 | Log de conclusão | Servlet `Filter` logo **dentro** do 2 (`HIGHEST_PRECEDENCE + 2`) | o código depois de `chain.doFilter` roda ainda com o `MDC` do passo 2 vivo — registra status e duração com o ID |
| 4 | Autenticação | — | **fora de escopo** (ver [seção 10](#10-autenticação-autorização-e-fronteira)) |
| 5 | Autorização | — | **fora de escopo** |
| 6 | Validação de entrada | `@Valid`/`@Validated` na resolução de argumentos, **antes** do corpo do controller | nada não validado chega ao caso de uso |
| 7 | Cache de saída | — | não se aplica |
| 8 | **Caso de uso** | controller → `*Caso` | orquestra domínio e retorna `Resultado` |
| 9 | Tradução de erro | `@RestControllerAdvice` | envolve o controller: qualquer exceção lá dentro vira `MensagemProblema` |

```java
// api/configuracao/OrdemDosFiltros.java — a ordem do contrato, num lugar só
// (menor número = mais externo; só a composition root registra filtro)
public final class OrdemDosFiltros {
    public static final int CABECALHOS_DE_SEGURANCA = Ordered.HIGHEST_PRECEDENCE;
    public static final int CORRELACAO = Ordered.HIGHEST_PRECEDENCE + 1;
    public static final int LOG_DE_CONCLUSAO = Ordered.HIGHEST_PRECEDENCE + 2;
    public static final int REJEICAO_DE_TRACE = Ordered.HIGHEST_PRECEDENCE + 3;
}

// api/configuracao/ConfiguracaoDoFiltroDeCabecalhosDeSeguranca.java — um filtro por configuração
@Configuration
public class ConfiguracaoDoFiltroDeCabecalhosDeSeguranca {

    @Bean
    FilterRegistrationBean<FiltroDeCabecalhosDeSeguranca> filtroDeCabecalhosDeSeguranca() {
        var registro = new FilterRegistrationBean<>(new FiltroDeCabecalhosDeSeguranca());
        registro.addUrlPatterns("/api/*");          // não vale para /swagger-ui nem /h2-console
        registro.setOrder(OrdemDosFiltros.CABECALHOS_DE_SEGURANCA);
        return registro;
    }
}
// os outros filtros seguem o mesmo molde, cada um na sua própria configuração
```

**Por quê o log de conclusão fica dentro da correlação e não fora:** um
filtro envolve tudo que vem depois dele — a parte antes de
`chain.doFilter(...)` roda na entrada, a parte depois roda na saída, com a
resposta pronta. O filtro de correlação limpa o `MDC` ao final
(`finally`); se o log de conclusão fosse mais externo, o ID já teria sido
apagado quando ele escrevesse a linha.

**Regras:**

- Um filtro faz **uma** coisa; se dois comportamentos são independentes,
  são dois filtros.
- Nenhum filtro chama caso de uso — ele orquestra fronteira (anotar,
  recusar, medir), nunca decide regra de negócio.
- **O `idRequisicao` aceito do cliente** (`X-Request-Id`) só é reaproveitado
  se tiver formato de UUID; caso contrário, é gerado um novo. Aceitar texto
  livre do cliente em log é injeção de log.
- O `idRequisicao` é devolvido no cabeçalho `X-Request-Id` **de toda
  resposta**, não só nas de erro.
- Ordem trocada é mudança revisada: o filtro de log passar a ser mais
  externo que o de correlação perde o ID em toda linha de conclusão. Um
  teste do contexto inteiro (`PipelineDeRequisicaoTeste`) a fixa: trocar os
  números de `setOrder` o derruba.
- **O `idRequisicao` também sai no corpo de erro**, lido do `MDC` num ponto
  só, e não passado de mão em mão: quem monta o corpo (`MensagemProblema`)
  o pega de `IdDaRequisicaoAtual`.
- Os filtros estendem `OncePerRequestFilter` e são registrados só aqui, por
  `FilterRegistrationBean`; eles não são beans soltos, para o Spring não os
  registrar uma segunda vez fora da ordem.

---

## 5. DTOs para Entrada e Saída

A API nunca retorna entidade de domínio nem entidade JPA. Sempre usa tipos
de fronteira para entrada e saída — tipos internos **nunca** cruzam a
fronteira da API.

```java
// CORRETO — retorna mensagem de fronteira
@GetMapping("/{id}")
public MensagemConsumidor obter(@PathVariable long id) { /* ... */ }

// ERRADO — retorna o objeto interno (foi o legado: List<Consumer>, com saldos e todos os campos)
@GetMapping("/consumerList")
public List<Consumer> listAllConsumers() { /* ... */ }
```

### Quem é Fronteira, Quem é Interno

| Tipo | Onde vive | Exemplo |
|---|---|---|
| `Mensagem<Assunto/Operacao>` | fronteira REST — corpo de request/response (`api/{conceito}/`) | `MensagemCriarConsumidor`, `MensagemRegistrarCompra`, `MensagemConsumidor` |
| `Dto<Assunto>` | transferência **interna** entre camadas (`aplicacao/dtos/`) | `DtoCompra`, `DtoComprovanteCompra` |

> **Sobre os prefixos:** a regra **vinculante** é de conteúdo, não de
> prefixo. O que cruza a fronteira é um **tipo de fronteira**, um por
> operação — nunca a entidade de domínio, nunca o `Resultado<T, E>`
> interno, nunca a `@Entity`.

**Mensagens são `record`**: o Jackson 2.13 do Boot 2.7 serializa e
desserializa `record` nativamente, e as anotações do Bean Validation sobre
os componentes valem para o corpo.

```java
public record MensagemRegistrarCompra(
    @NotNull Integer tipoEstabelecimento,
    @NotBlank String nomeEstabelecimento,
    @NotBlank String numeroCartao,
    @NotBlank String descricaoProduto,
    @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valor) { }
```

**Regras da fronteira:**

1. **Um tipo de fronteira por operação**, não um tipo genérico reutilizado
   entre endpoints. Criar consumidor aceita os dados cadastrais **e** os
   números dos cartões; atualizar aceita **só** os dados cadastrais
   (`MensagemDadosConsumidor`, reaproveitada como componente dentro de
   `MensagemCriarConsumidor` e como corpo do `PUT`). O saldo **não existe
   em nenhuma mensagem de entrada** — a impossibilidade de alterar saldo
   passa a ser estrutural, não um comentário ("não deve ser possível
   alterar o saldo", que o legado violava ao fazer `repository.save` do
   objeto recebido).
2. **Só o que a operação precisa.** A resposta não é "o objeto inteiro":
   é a projeção que o consumidor pediu.
3. **A entrada nunca monta domínio nem entidade direto.** O corpo chega
   como `Mensagem*`, é validado na borda e só então vira `Dto*` de entrada
   do caso de uso — a entidade é construída pelo domínio, não
   desserializada.
4. **Nada de vazamento interno**: chaves de tabela de ligação, nomes de
   coluna, `versao` de linha de tabela que o cliente não precisa e
   estratégias de persistência não aparecem na resposta só porque existem
   no banco.
5. **`spring.jpa.open-in-view: false`.** Com o padrão `true`, a sessão do
   Hibernate fica aberta até a serialização — uma entidade com relação
   `LAZY` poderia ser serializada pela API e disparar consulta durante a
   escrita da resposta. Desligado, a fronteira de tipos passa a ser
   também fronteira de transação.

**Por quê:** a entidade de domínio muda com a regra de negócio; o contrato
da API muda com versão. Quando os dois são o mesmo tipo, toda mudança
interna vira mudança pública involuntária — exatamente o acidente que o
versionamento existe para evitar.

---

## 6. Serialização Determinística

JSON é o formato padrão. **Mesma configuração em todos os endpoints** — as
opções do Jackson são definidas **uma vez**, em `application.yml`
(`spring.jackson.*`), nunca sobrescritas por controller nem por
`@JsonFormat` em campo solto (exceção só com justificativa no PR):

```yaml
# application.yml — proposta; validar ao aplicar
spring:
  jackson:
    time-zone: UTC
    default-property-inclusion: non_null
    serialization:
      write-dates-as-timestamps: false          # datas em ISO 8601, não em número
      order-map-entries-by-keys: true           # chaves de Map em ordem estável
    generator:
      write-bigdecimal-as-plain: true           # 100.00, nunca 1E+2
    deserialization:
      fail-on-unknown-properties: true          # campo desconhecido na entrada → 400
```

- **camelCase** nos nomes de propriedade (padrão do Jackson).
- **Datas** em ISO 8601: `LocalDate` como `2026-10-27`; `Instant` em UTC
  com sufixo `Z` — nunca formato local, nunca com fuso implícito.
- **Enums** serializados como **string nomeada**, nunca como índice
  numérico — reordenar um enum mudaria o significado do número enviado.
  (Padrão do Jackson; proibido `WRITE_ENUMS_USING_INDEX`.)
- **Coleções** sempre como array (mesmo vazia, quando o campo faz parte do
  contrato) — a `Mensagem*` inicializa lista vazia, nunca `null`.
- **Dinheiro** é `Dinheiro` (`BigDecimal` por dentro, escala de 6 casas) e
  vai na **saída como string decimal com a escala completa**
  (`"29.997000"`), nunca como número JSON: um cliente JavaScript que lê
  número converte para `double` e perde a fração que o sistema guarda. Na
  **entrada** aceita `BigDecimal` com até **2 casas**
  (`@Digits(integer = 13, fraction = 2)`) — fração de centavo é resultado de
  cálculo, não dado de entrada. Nunca `double`; ver
  [`VALORES.md`](../codigo/VALORES.md#6-dinheiro-e-percentuais).
- **Chaves de objeto em ordem estável**: `record` serializa na ordem dos
  componentes — duas respostas iguais produzem bytes iguais.
- **Campo desconhecido na entrada é recusado (400).** Sem isso, um cliente
  que envia `"foodCardBalance": 9999` num `PUT` teria o campo **ignorado em
  silêncio** e acharia que funcionou — a mesma classe de falha do legado.

```json
{
  "id": 42,
  "nome": "Maria Souza",
  "documento": "12345678901",
  "dataNascimento": "1990-05-17",
  "cartoes": [
    { "tipo": "ALIMENTACAO", "numero": "5555000011112222", "saldo": "150.000000" },
    { "tipo": "COMBUSTIVEL", "numero": "5555000033334444", "saldo": "0.000000" }
  ]
}
```

**Por quê:** "determinística" é o que permite ao consumidor fazer `diff` entre
duas respostas, cachear por hash e testar contrato sem falsos positivos.
Formatação que varia com locale, fuso ou ordem de chaves transforma igual
em diferente — e o cliente não consegue distinguir mudança real de ruído.

---

## 7. Endpoints São Stateless

A API não armazena estado de sessão no servidor. Toda informação
necessária para processar a requisição vem no próprio request.

**O que isso obriga na prática:**

- **Nenhum estado entre requisições na memória da API** — sem
  `HttpSession`, sem campo estático ou de instância mutável num bean
  singleton guardando "o último filtro usado". Todo bean do Spring é
  singleton e compartilhado entre threads: **bean com campo mutável é bug de
  concorrência** (ver [`CONCORRENCIA.md`](../runtime/CONCORRENCIA.md#4-sem-estado-global-mutável)).
  Estado do sistema mora no banco.
- **Qualquer instância processa qualquer requisição** — é a condição para
  escalar horizontalmente: se houvesse sessão em memória, a resposta
  dependeria de *qual* servidor atendeu.
- **A transação é curta e o lock também.** Não há cancelamento da
  requisição no modelo Servlet; o recurso prático é **limitar** o que ela
  pode segurar: `@Transactional(timeout = …)` e tempo máximo de espera de
  lock. Nenhuma chamada externa (HTTP, fila) dentro de uma transação que
  mantém um cartão travado.
- **Trabalho de longa duração não nasce na requisição** — fora do escopo
  deste projeto, mas a regra vale se aparecer (ex.: processamento em lote).

**Por quê:** estado de sessão no servidor é o que faz resposta variar por
instância, por horário e por histórico — a fonte clássica de bugs que
"só aparecem atrás do load balancer".

---

## 8. Verbos, Recursos e Códigos de Status

| Verbo | Significado | Sucesso |
|---|---|---|
| `GET` | leitura, **sem efeito colateral** | 200 |
| `POST` | cria recurso (ou operação que gera recurso) | 201 (+ `Location` quando o recurso tem rota de leitura) |
| `PUT` | substituição total, **idempotente** | 200 |
| `PATCH` | alteração parcial | 200 |
| `DELETE` | remoção, **idempotente** | 204 |

| Situação | Status |
|---|---|
| Leitura OK | 200 |
| Criação OK | 201 |
| Operação OK sem corpo | 204 |
| Validação de entrada falhou | 400 + `errors` |
| Sem token ou token inválido/expirado | 401 *(quando houver autenticação)* |
| Sem permissão para a operação | 403 *(quando houver autorização)* |
| Recurso inexistente **ou não legível por este usuário** | 404 |
| Método não suportado na rota | 405 |
| Conflito (concorrência, duplicidade) | 409 |
| Entrada válida, regra de negócio recusou (saldo insuficiente) | 422 |
| Dependência crítica indisponível (**só** `GET /api/v1/saude`) | 503 — ver [`OBSERVABILIDADE.md`](../runtime/OBSERVABILIDADE.md#7-health-check--apiv1saude) |
| Falha inesperada | 500 + corpo padronizado sem detalhe interno |

**Regras:**

- **Nunca verbo na rota**: `/consumer/createConsumer`, `/setcardbalance` e
  `/buy` do legado são o anti-padrão. A ação vira recurso:
  `POST /api/v1/compras`.
- **`GET` nunca altera estado.** O legado fazia `GET /buy` debitar saldo —
  qualquer pré-busca de navegador, *crawler* ou *link preview* geraria uma
  compra. Operação que altera estado é `POST`/`PUT`/`PATCH`/`DELETE`.
- **`POST` de crédito e de compra devolvem 201 sem `Location`**: o recurso
  criado (a linha de extrato) ainda não tem rota de leitura. É a **única**
  exceção à regra "201 + `Location`" e some quando `GET /api/v1/extratos/{id}`
  existir. O corpo é o comprovante (valor debitado e saldo restante).
- **401 ≠ 403** e **404 ≠ 403 para recurso oculto**: valem integralmente
  quando houver autenticação (ver [seção 10](#10-autenticação-autorização-e-fronteira)).

---

## 9. Paginação, Filtros e Ordenação

Nenhuma listagem retorna coleção ilimitada — o legado devolvia ~50.000
consumidores de uma vez (`select * from Consumer`), com todos os campos.

**Requisição:** `pagina` (**base 1**), `tamanhoPagina` (padrão 20, teto
**100** — acima dele, 400), `ordenacao` e `direcao`.

```text
GET /api/v1/consumidores?pagina=2&tamanhoPagina=20&ordenacao=nome&direcao=asc
```

```java
@GetMapping
public MensagemPagina<MensagemResumoConsumidor> listar(
        @RequestParam(defaultValue = "1")    int pagina,
        @RequestParam(defaultValue = "20")   int tamanhoPagina,
        @RequestParam(defaultValue = "nome") String ordenacao,
        @RequestParam(defaultValue = "asc")  String direcao) { /* ... */ }
// os quatro parâmetros são lidos juntos pelo domínio (PedidoDePaginaDeParametros),
// que devolve todos os erros de uma vez — sem @Min/@Max, e sem aceitar Pageable
```

- O contrato é de **base 1**; o Spring Data é de base 0. A conversão
  (`pagina - 1`) acontece **uma vez**, na borda — nunca vaza para o domínio.
- `ordenacao` é um **enum fechado** (`ID`, `NOME`) — nunca o nome de coluna
  digitado pelo cliente (SQL/JPQL injection e vazamento de modelo). O
  mapeamento enum → propriedade da entidade é do **adaptador de persistência**,
  e não do enum: o domínio não conhece o nome de um atributo da entidade.
- **O tamanho acima do teto é erro**, e não um corte silencioso em 100.
- **A ordenação sempre desempata por `id`**: `ORDER BY nome, id`. Sem
  desempate, dois consumidores com o mesmo nome podem trocar de lugar entre
  duas consultas e a paginação pula ou repete itens.

**Resposta — envelope único para toda listagem** (`MensagemPagina<T>`):

```json
{
  "itens": [
    { "id": 42, "nome": "Maria Souza" }
  ],
  "pagina": 2,
  "tamanhoPagina": 20,
  "total": 50000,
  "totalPaginas": 2500
}
```

**Por quê:**

- **Ordenação é obrigatória junto da paginação.** Paginar sobre resultado
  sem ordem determinística pula e repete itens entre páginas — não é
  detalhe, é página incorreta entregue com sucesso.
- **Envelope único** significa que o cliente genérico de listagem nunca
  precisa de `if` por endpoint para saber onde estão os itens e se há
  próxima página.
- **Filtro e projeção são parte do contrato** — parâmetro novo que muda o
  conjunto retornado é mudança documentada, não atalho de query.

---

## 10. Autenticação, Autorização e Fronteira

**Fora de escopo deste projeto.** Autenticação e autorização, no
ecossistema Spring, são feitas com Spring Security — **dependência nova**,
proibida pelo enunciado do desafio. O contrato atual é, portanto,
**público e sem autenticação**, e isso é um **risco registrado**, não uma
decisão de design: a API altera saldo financeiro e expõe dado pessoal
(documento, telefone, endereço).

No PR, o item é listado como **recomendação**, junto com o que passa a
valer no dia em que a autenticação existir — estas regras são mantidas
aqui como contrato futuro:

- **Leitura pública, escrita autenticada** — o contrato de cada endpoint
  declara se é público ou autenticado, e isso é testado como contrato.
- **Toda escrita exige autenticação + permissão explícita** — nunca
  "autenticado já basta"; a verificação é por recurso, não por rota.
- **Token no cabeçalho `Authorization: Bearer`**, nunca em query string
  (query string vaza em log, histórico e cabeçalho de referência) — a
  mesma razão pela qual o número de cartão não vai na URL.
- **401 traz `WWW-Authenticate`**; **403** é "sei quem você é, e você não
  pode"; **404** para recurso que o usuário nem pode ler (evita
  enumeração de IDs).
- **Erro de autorização nunca descreve a árvore de permissões.**

**Por quê:** a fronteira é onde segurança vira comportamento observável.
Se cada endpoint improvisar sua resposta de auth, o cliente seguro é
obrigado a improvisar junto — e improviso em autenticação é falha.

---

## 11. Concorrência, Idempotência e Conflitos

**Idempotência por verbo:**

| Verbo | Repetir a mesma chamada | Consequência |
|---|---|---|
| `GET`, `PUT`, `DELETE` | idempotente | repetir não altera o resultado final — retentativa é segura |
| `PATCH` | depende da alteração | retentativa só é segura se o patch descreve estado final, não delta |
| `POST` | **não idempotente** | repetir cria outro recurso — retentar sem verificar antes duplica |

> **Em dinheiro isso é crítico:** `POST /api/v1/compras` e
> `POST /api/v1/creditos` **não podem ser retentados às cegas** — um
> *timeout* de rede após o débito, seguido de retentativa, debita duas
> vezes. A evolução recomendada é uma chave de idempotência por requisição
> (cabeçalho `Idempotency-Key`), **fora do escopo do desafio** e registrada
> como recomendação no PR.

**Duas estratégias de concorrência, uma por tipo de dado:**

| Dado | Estratégia | Por quê |
|---|---|---|
| **Saldo do cartão** (débito/crédito) | **lock pessimista** (`@Lock(PESSIMISTIC_WRITE)`) dentro da transação do caso de uso, mais `@Version` como rede de segurança | duas compras simultâneas no mesmo cartão **devem ambas ter sucesso**, em série, desde que o saldo cubra — responder 409 a uma compra legítima só por contenção seria erro |
| **Dados cadastrais do consumidor** (`PUT`) | **otimista**: a mensagem de leitura expõe `versao` e o `PUT` a devolve | duas edições concorrentes do mesmo cadastro não podem se sobrescrever em silêncio |

**Quando a API responde 409:**

- `versao` enviada no `PUT` não é mais a atual (edição concorrente);
- `ObjectOptimisticLockingFailureException` (a rede de segurança do `@Version`);
- `PessimisticLockingFailureException` — a espera pelo lock estourou o
  tempo limite; o `detail` orienta tentar novamente;
- `DataIntegrityViolationException` por duplicidade (documento ou número de
  cartão já cadastrado).

```json
{
  "type": "urn:consumer-pat:erro:CPT-CNF-SIS-001",
  "title": "Conflito de concorrência",
  "status": 409,
  "detail": "O consumidor foi alterado por outra requisição. Recarregue antes de salvar novamente.",
  "instance": "/api/v1/consumidores/42",
  "idRequisicao": "0f1c9c1e-8a45-4d5e-9f77-1a2b3c4d5e6f"
}
```

Nunca sobrescreve em silêncio, nunca responde 200 com a versão do outro
salva por baixo. O legado fazia exatamente isso: `findBy…`, soma em
memória, `save` — duas requisições concorrentes liam o mesmo saldo e o
segundo `save` apagava o primeiro (*lost update*).

**Por quê:** a pergunta não é *se* vai haver escrita concorrente, é o que
a API responde quando houver. Silêncio (200 sobrescrevendo) é a pior
resposta possível: o segundo salvamento apaga o primeiro sem ninguém
saber — e, aqui, o dinheiro de alguém.

---

## 12. Contrato Público e Testes de Contrato

Como a API é a única superfície pública, **o contrato dela é artefato de
primeira classe**, não consequência do código:

1. **Teste de contrato cobre status, corpo e versionamento** de cada
   endpoint — é a prioridade crítica de cobertura da suíte. Em Spring, com
   `@SpringBootTest` + `@AutoConfigureMockMvc` (já presentes em
   `spring-boot-starter-test`), verificando status, `Content-Type`
   (`application/problem+json` nos erros) e `idRequisicao`.
2. **Toda mudança de formato vem com teste de contrato atualizado no mesmo
   PR** — contrato velho que ainda passa significa que ninguém mudou o
   teste junto, e o consumidor quebra primeiro.
3. **Cada versão viva tem seu contrato coberto** — se `/api/v2/` existir,
   os testes de v1 e de v2 rodam; remover v1 é remover os testes dela
   junto, no PR da remoção.
4. **A especificação OpenAPI é derivada do código** — o `springdoc-openapi`
   já é dependência do projeto e gera `/v3/api-docs` a partir dos
   controllers. Nunca editada à mão, nunca fonte de verdade concorrente do
   teste. Descrição de operação e dos status possíveis vai em
   `@Operation`/`@ApiResponse` (resumo curto) e o contrato completo em
   Javadoc (ver [`DOCUMENTACAO.md`](../codigo/DOCUMENTACAO.md)).
5. **Casos obrigatórios de contrato**, derivados dos bugs do legado:
   - `PUT` com campo de saldo no corpo → 400 (campo desconhecido), e o
     saldo no banco permanece intacto;
   - compra com cartão inexistente → 404 (e não NPE/500);
   - compra com saldo insuficiente → 422, saldo inalterado;
   - compra em estabelecimento de tipo diferente do cartão → 404;
   - valor negativo ou zero em crédito e compra → 400 com `errors.valor`;
   - N compras simultâneas no mesmo cartão → saldo final exato.
   - rota inexistente → 404 em `application/problem+json`.

**Por quê:** consumidor não lê implementação. Sem contrato verificado, a
única forma de descobrir o formato é chamar em produção — que é
exatamente o custo que esta padronização existe para eliminar.

---

## Anti-Padrões

| Anti-padrão | Correto |
|---|---|
| Rota fora de `/api/v1/` ou sem versão | Toda rota pública em `/api/v{n}/` |
| Verbo na rota (`/buy`, `/setcardbalance`, `/createConsumer`) | Recurso + verbo HTTP: `POST /api/v1/compras` |
| `GET` que altera estado | `POST`/`PUT`/`PATCH`/`DELETE` |
| Número de cartão, valor ou token em query string / path | No corpo da requisição |
| Retornar entidade JPA ou de domínio | Retornar `Mensagem*` de projeção |
| Receber `@Entity` ou objeto de domínio em `@RequestBody` | Receber `Mensagem*` validada, um tipo por operação |
| `catch` devolvendo 200 com corpo de erro | Status HTTP real + `MensagemProblema` centralizada |
| Stack trace, SQL ou texto de exceção na resposta | `detail` genérico + `idRequisicao` para o operador |
| JSON de erro padrão do Spring (`timestamp/error/path`) | `application/problem+json` (RFC 7807) |
| Status escolhido dentro de cada controller | `CategoriaErro` → status no enum; `@ExceptionHandler` central |
| Filtro que lança exceção | Filtro só anota; exceção de filtro escapa do advice |
| Filtro/interceptor que decide regra de negócio | Filtro orquestra fronteira; regra fica no caso de uso |
| Lista sem paginação ou sem ordenação determinística | `pagina`/`tamanhoPagina` + `ordenacao` + desempate por `id` + envelope único |
| `ordenacao` como nome de coluna livre | Enum fechado de campos ordenáveis |
| Configuração de serialização por controller / `@JsonFormat` solto | Uma configuração em `application.yml` |
| Campo desconhecido ignorado em silêncio na entrada | `fail-on-unknown-properties: true` → 400 |
| `double` para dinheiro, ou dinheiro como número JSON | `Dinheiro` (escala 6); string decimal na saída ([`VALORES.md`](../codigo/VALORES.md#6-dinheiro-e-percentuais)) |
| Bean singleton com campo mutável / sessão HTTP | Stateless: corpo, cabeçalhos, banco |
| `spring.jpa.open-in-view` ligado | Desligado — a fronteira de tipos é também a de transação |
| `/h2-console` habilitado em produção | Habilitado só no perfil padrão (exigido pelo README); `application-prod.yml` o desabilita |
| Retentar `POST` de compra em falha de rede | `POST` não é idempotente; chave de idempotência é a evolução |
| Ler → somar em memória → `save` sem trava | Lock pessimista na transação do caso de uso |
| Contrato mudou sem teste de contrato no mesmo PR | Atualizar o teste no mesmo PR |

---

## Checklist

Antes de abrir um PR que mexe em endpoint:

- [ ] A rota está em `/api/v1/`, kebab-case, plural, sem verbo?
- [ ] O verbo HTTP tem o significado correto, e `GET` não altera estado?
- [ ] Nenhum dado sensível (cartão, valor, token) trafega em URL ou query string?
- [ ] O status de resposta segue a tabela da seção 8, sem improviso?
- [ ] A resposta sai como `Mensagem*` de projeção, nunca entidade de domínio ou JPA?
- [ ] A entrada é uma `Mensagem*` própria da operação, validada com `@Valid`, sem campo de saldo?
- [ ] O erro sai como `application/problem+json` do mapeamento central, com `idRequisicao`?
- [ ] Nenhum internal (SQL, stack, texto de exceção, número de cartão completo) vaza no corpo?
- [ ] Listagem nova tem paginação **e** ordenação determinística (desempate por `id`), com envelope único?
- [ ] A mudança é aditiva ou mudou de versão? Se mudou formato/remoção/status: `/api/v{n+1}/` e justificativa no PR?
- [ ] Escrita concorrente nova usa a estratégia certa (lock pessimista no saldo; versão no cadastro) e responde 409 em conflito?
- [ ] `POST` novo tem semântica de retentativa documentada?
- [ ] O filtro novo tem responsabilidade única, não lança exceção e tem a sua própria configuração, com a posição em `OrdemDosFiltros`?
- [ ] O teste de contrato do endpoint foi atualizado no mesmo PR?
- [ ] O PR lista como recomendação o que ficou fora de escopo (autenticação, idempotência)?
- [ ] O README/contrato da API descreve o estado atual, não o de quando nasceu?

---

**[Estrutura →](../fundamentos/ESTRUTURA.md)**
