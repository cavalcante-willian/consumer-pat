[← Índice](../../PADRONIZACAO_INDEX.md) · [← ERROS.md](ERROS.md) · [Próximo: SEGURANCA.md](SEGURANCA.md)

# Validação — Java 17 / Bean Validation / Spring MVC

> **Escopo**: a validação de entrada em **Java 17 + Bean Validation
> (Hibernate Validator) + Spring MVC**. O princípio é que **toda entrada
> externa é validada na fronteira, uma vez, antes de chegar ao domínio**. O
> mecanismo são **anotações declarativas** nas `Mensagem*` (nível 10 de
> [`CONDICIONAIS.md`](../codigo/CONDICIONAIS.md)), acionadas pelo framework
> **antes** do controller — decisão já tomada em
> [`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md) (não há pasta `validadores/`).
> Ver [Mecanismo de Validação](#mecanismo-de-validação).
>
> **Nota sobre exemplos**: os trechos de código abaixo são ilustrativos e
> **não foram executados** — a regra normativa é o texto conceitual. O código
> atual ainda é o **legado do desafio**, que **não valida nada**: o
> `ConsumerController` recebe a entidade JPA no `@RequestBody`, sem `@Valid`.

Define onde toda entrada externa é validada: inventário dos pontos de
entrada, separação entre validação de borda, validação que só o domínio
consegue fazer e validação de estado, ponto único na API, as regras de entrada
de cada mensagem, o formato único do `400`, mensagens acionáveis, texto livre
e a cobertura de teste.

---

## Mecanismo de Validação

| Tema | Regra neste projeto |
|---|---|
| Onde mora a regra de borda | **anotação no `record` da `Mensagem*`** (`@NotBlank`, `@Size`, `@Digits`…) |
| Regra entre campos | *constraint* de classe ou `@AssertTrue` no `record`; classe `Validador*` só se nada disso bastar |
| Quem aciona | **o Spring**, ao resolver o argumento: `@Valid` no corpo, `@Validated` no controller |
| Resultado de validação | o framework **lança** `MethodArgumentNotValidException`; **converte-se uma vez**, no `@RestControllerAdvice`. `Resultado<T, E>` só no domínio |
| Idioma das mensagens | depende do **locale da requisição** — fixado em pt-BR + *bundle* próprio (seção 7) |
| Valor rejeitado | **vaza por padrão** no texto da exceção e no `FieldError` — nunca vai ao corpo (seção 6) |
| Erros de desserialização | o **Jackson** gera mensagem com **nome de classe**; traduzida (seção 7) |
| Uploads e HTML | **não se aplicam**; fica a regra de texto livre (seção 10) |
| "Todo endpoint valida" | por construção **mais um teste de arquitetura** (seção 4) |

---

## 1. Validação Centralizada de Toda Entrada Externa

Toda entrada que vem de fora do domínio passa por **validação declarada na
fronteira**, antes de entrar na lógica. Nunca validação espalhada inline nas
funções de negócio.

```java
// CORRETO — a regra mora na mensagem; o caso de uso recebe um Dto já válido
public record MensagemCreditarCartao(
    @NotBlank @Size(max = 25) String numeroCartao,
    @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valor) { }

@PostMapping("/creditos")
ResponseEntity<Object> creditar(@Valid @RequestBody MensagemCreditarCartao mensagem) {
    // aqui a mensagem JÁ passou: o Spring recusou com 400 antes de chamar este método
    return RespostaDeResultado.responder(caso.creditar(mensagem.paraDto()), /* ... */);
}

// ERRADO — validação espalhada no caso de uso (e foi o legado: nenhuma validação,
// valor negativo em /setcardbalance virava DÉBITO)
public Resultado<Cartao, ErroDominio> creditar(String numero, BigDecimal valor) {
    if (valor.signum() <= 0) return Resultado.falha(/* ... */);   // regra de formato dentro do caso de uso
    // ...
}
```

**Por quê:** centralizar é o que torna possível auditar, com uma leitura só,
se todo ponto de entrada está coberto. Validação espalhada é uma regra que
precisa ser lembrada em cada função — e a função em que ninguém lembra é por
onde dado malformado chega ao núcleo. **No legado, valor negativo em
`/setcardbalance` somava um número negativo ao saldo: um crédito que debita.**

**O domínio assume, não confia.** `dominio` e `aplicacao` **assumem** o que a
borda garantiu — não revalidam formato. As invariantes que o **tipo** carrega
(`Dinheiro` na escala, `Documento` só com dígitos) são garantidas pelo
construtor do tipo, e quebrá-las é *bug de programação*
([`VALORES.md`](../codigo/VALORES.md#2-unidades-do-domínio)), não dado malformado.

---

## 2. Pontos de Entrada Externa

Inventário dos pontos em que dado de fora entra no sistema. **Ponto novo entra
nesta tabela no mesmo PR que o cria.**

| Origem | Onde valida | O quê |
|---|---|---|
| **Corpo da requisição** (`Mensagem*`) | `@Valid @RequestBody` — Bean Validation, antes do controller | tipo, obrigatoriedade, tamanho, formato grosso, faixa; campo desconhecido é recusado (Jackson) |
| **Parâmetros de query** | `@Validated` no controller + `@Min`/`@Max`; enum fechado | paginação dentro do teto, ordenação conhecida ([`API.md`](API.md#9-paginação-filtros-e-ordenação)) |
| **Variáveis de rota** (`/{id}`) | `@Validated` + `@Positive`; conversão de tipo do Spring | identificador com forma esperada |
| **Cabeçalho `X-Request-Id`** | `FiltroDeCorrelacao` | só é aceito se for UUID; senão é regerado ([`API.md`](API.md#4-pipeline-de-requisição)) |
| **Configuração** (`application*.yml`, perfil) | na inicialização (`@Value`/propriedades tipadas) | valor ausente ou inválido **derruba a inicialização** — falhar cedo é a validação |
| ~~Arquivos enviados~~ | — | **não existe** neste projeto |
| ~~Conteúdo importado, *webhooks*, preferências persistidas~~ | — | **não existem** neste projeto |

> **Banco de dados não é fronteira de entrada**: o dado que o adaptador lê foi
> gravado por este sistema, que já o validou — o `CHECK`, o `NOT NULL` e o
> `unique` do schema são a **última** rede, não a primeira
> ([`BANCO.md`](BANCO.md#4-tipos-de-coluna-nulidade-e-restrições)).

---

## 3. Borda, Domínio e Estado — Três Perguntas

Duas categorias são clássicas (borda e estado). **Há uma terceira**, que o
Java traz à tona:

| Tipo | Pergunta | Onde acontece | Falha vira |
|---|---|---|---|
| **Borda** | o dado tem o **formato** certo? | `Mensagem*`, anotações, **sem consulta alguma** | `400` + `errors` campo a campo |
| **Domínio (forma)** | o dado é um valor **válido do tipo**? | `criar()` do tipo de valor, ou conversão de domínio (`TipoCartao.doCodigoDeEstabelecimento`) | `ErroValidacao` → **o mesmo `400` + `errors`** |
| **Estado** | o dado, bem formado, é compatível com o **estado do sistema**? | caso de uso, **com** acesso às portas | `ErroDominio` tipado → `404`/`409`/`422` |

- **Borda** pergunta: formato, tipo, tamanho, obrigatoriedade. **Não acessa o
  banco e não deve.**
- **Domínio (forma)** é o caso intermediário: `tipoEstabelecimento = 99` passa
  no Bean Validation (é um `Integer` não nulo) e só o domínio sabe que **não
  existe** o tipo 99. É **entrada malformada** — logo `400`, não `404` nem
  `422` — mas só o domínio consegue dizer. O mesmo vale para um documento com
  11 dígitos que não passa na regra do `Documento`.
- **Estado** pergunta: consumidor inexistente, saldo insuficiente, documento já
  cadastrado. Só se responde consultando o sistema
  ([seção 8](#8-validação-de-estado-no-caso-de-uso)).

**A regra de ouro: borda e domínio produzem o mesmo `400`.** O cliente não
pode distinguir *quem* recusou. Para isso o `ErroValidacao` de domínio
**carrega o campo** da fronteira a que se refere:

```java
// dominio/erros/ErroDominio.java — acrescenta um método com comportamento padrão
public interface ErroDominio {
    CategoriaErro categoria();
    String codigo();
    String mensagem();

    /** Campos da fronteira a que o erro se refere, para o corpo `errors`; vazio quando não há campo. */
    default Map<String, List<String>> camposComErro() { return Map.of(); }
}

// dominio/erros/validacao/ErroTipoEstabelecimentoInvalido.java
public record ErroTipoEstabelecimentoInvalido(int codigoRecebido) implements ErroValidacao {
    public static final String CODIGO = "CPT-VAL-CRT-001";
    @Override public String codigo() { return CODIGO; }
    @Override public String mensagem() { return "O tipo de estabelecimento informado não existe."; }
    @Override public Map<String, List<String>> camposComErro() {
        return Map.of("tipoEstabelecimento", List.of("não corresponde a um tipo de estabelecimento conhecido"));
    }
}
```

`RespostaDeResultado` monta o `errors` a partir de `camposComErro()` —
**polimorfismo, sem `instanceof`** (nível 2 de
[`CONDICIONAIS.md`](../codigo/CONDICIONAIS.md)). O erro de estado devolve o
mapa vazio e não ganha `errors`.

### A regra de formato tem um dono só

`Documento` e `NumeroDeCartao` têm regra de formato (11 ou 14 dígitos; 13 a
19). Validá-la **na mensagem e no tipo** são duas fontes que divergem. A
divisão:

- **A `Mensagem*` valida o formato grosso** — o que protege a camada de
  texto livre: `@NotBlank`, `@Size(max = 25)`, `@Pattern` que só admite
  dígitos e a pontuação comum (`.`, `-`, espaço). Recusa lixo óbvio e
  limita o tamanho.
- **O tipo de valor é a regra única** do formato real: `criar()` normaliza (só
  dígitos) e valida o comprimento
  ([`BANCO.md`](BANCO.md#11-forma-canônica-na-escrita)). Mudar a regra é mudar
  **um** lugar.

**Por quê:** "forma pura que é invariante do tipo mora no tipo, não em
validador" — o tipo só nasce válido, e não existe "função de validação de
documento" chamada à mão em cada uso.

---

## 4. Ponto Único na API

Todo endpoint que recebe dado passa pela validação **antes** do corpo do
controller. Neste projeto o mecanismo é o do Spring, na etapa 6 do pipeline
([`API.md`](API.md#4-pipeline-de-requisição)): não há *middleware* a escrever.

- **`@Valid @RequestBody`** em todo parâmetro de corpo → `MethodArgumentNotValidException`.
- **`@Validated` na classe do controller** em todo controller que tem
  `@RequestParam`/`@PathVariable` com restrição → `ConstraintViolationException`.
  *(O controller não pode ser `final`: o Spring cria um proxy.)*
- **O `@RestControllerAdvice` converte as duas** em `400` +
  `application/problem+json` com `errors`
  ([`API.md`](API.md#3-erro-padronizado-rfc-7807)) — **um lugar só**, formato
  único.
- **Nenhum controller reimplementa a checagem à mão** nem devolve `400` por
  conta própria.

```java
// CORRETO — o controller só traduz fronteira e delega; a validação já aconteceu
@PostMapping("/compras")
ResponseEntity<Object> comprar(@Valid @RequestBody MensagemRegistrarCompra mensagem) { /* ... */ }

// ERRADO — validação improvisada dentro do endpoint: status e corpo fora do contrato central
@PostMapping("/compras")
ResponseEntity<Object> comprar(@RequestBody MensagemRegistrarCompra mensagem) {
    if (mensagem.valor() == null) return ResponseEntity.badRequest().body("valor obrigatório");
    // ...
}
```

### "Todo endpoint valida" é verificável

Num middleware isso seria verdade por construção; aqui, **um `@RequestBody`
sem `@Valid` compila e funciona** — e simplesmente não valida, em silêncio. Por
isso é um **teste de arquitetura**, no estilo dos outros:

- **`EntradaValidadaTeste`**: percorre os `@RestController` e falha se
  - algum parâmetro `@RequestBody` **não** tiver `@Valid`;
  - algum controller com `@RequestParam`/`@PathVariable` anotado com restrição
    (`@Min`, `@Max`, `@Positive`…) **não** tiver `@Validated`;
  - algum campo `String` de `record` de `Mensagem*` de entrada **não** tiver
    `@Size(max = …)` ([`SEGURANCA.md`](SEGURANCA.md#2-entrada-validada-na-borda)).

*(Por reflexão sobre as classes compiladas; esboço — validar ao implementar.)*

**Por quê:** um único ponto é o que faz "todo endpoint valida" ser verdade
**por construção, não por revisão** — e mantém o `400`, o `errors` e o
mapeamento de status em um lugar só. Validação duplicada em controller ou caso
de uso é sinal de regra no lugar errado — ou de um caminho que não passou pelo
ponto único.

---

## 5. Regras de Entrada das Mensagens

*(Substitui "Validadores dedicados": a regra mora na anotação, e esta tabela é
a **especificação** do que cada `Mensagem*` exige — fixada no PR da API.)*

**Regras gerais:**

- **Uma regra por anotação, com a restrição mais específica que o campo
  admite.** `@NotBlank` (não só `@NotNull`) em texto; `@Positive` em dinheiro;
  `@Size(max = …)` em **todo** `String`.
- **O `@Size` espelha o `VARCHAR`** da coluna correspondente
  ([`BANCO.md`](BANCO.md#4-tipos-de-coluna-nulidade-e-restrições)) — o limite
  existe em duas camadas, e o do banco é a última.
- **Um tipo de mensagem por operação** ([`API.md`](API.md#5-dtos-para-entrada-e-saída));
  criar e atualizar aceitam campos diferentes, mesmo parecidos.
- **Regra entre campos** vive na própria mensagem (`@AssertTrue` sobre um
  método, ou *constraint* de classe) — **nunca** uma classe `Validador*` com
  acesso a repositório (essa é a validação de estado, seção 8).
- **Sem dinheiro em `double`** — nem na mensagem: `BigDecimal` com
  `@Digits(integer = 13, fraction = 2)`
  ([`VALORES.md`](../codigo/VALORES.md#6-dinheiro-e-percentuais)).

**Dados do consumidor** (`MensagemDadosConsumidor` — corpo do `PUT` e parte do
`POST`):

| Campo | Regras | Observação |
|---|---|---|
| `nome` | `@NotBlank`, `@Size(max = 150)`, sem caractere de controle | seção 10 |
| `documento` | `@NotBlank`, `@Pattern` (dígitos, `.`, `-`, `/`), `@Size(max = 18)` | formato real: `Documento.criar()` |
| `dataNascimento` | `@NotNull`, `@Past` | `@Past` usa o relógio do sistema (**exceção declarada**, seção 10) |
| `celular` | `@NotBlank`, `@Pattern` (dígitos e pontuação), `@Size(max = 15)` | |
| `telefoneResidencial`, `telefone` | `@Size(max = 15)`, mesmo `@Pattern` — **opcionais?** | decisão de domínio em aberto ([`BANCO.md`](BANCO.md#4-tipos-de-coluna-nulidade-e-restrições)) |
| `email` | `@NotBlank`, `@Email`, `@Size(max = 254)` | |
| `rua`, `cidade`, `pais` | `@NotBlank`, `@Size(max = …)`, sem caractere de controle | |
| `numero` | `@NotBlank`, `@Size(max = 10)` | número do imóvel — texto |
| `cep` | `@NotBlank`, `@Pattern` (8 dígitos, hífen opcional) | |

**Criar consumidor** (`MensagemCriarConsumidor`): os dados acima **mais**
`numerosCartoes` (mapa tipo → número, cada valor com `@Pattern` e `@Size`).
**Atualizar:** os dados acima **mais** `versao` (`@NotNull`, `@PositiveOrZero`) —
e **nenhum campo de saldo**, que o Jackson recusa como desconhecido.

**Creditar** (`MensagemCreditarCartao`): `numeroCartao` (`@NotBlank`,
`@Pattern`, `@Size`), `valor` (`@NotNull`, `@Positive`, `@Digits(13, 2)`).

**Compra** (`MensagemRegistrarCompra`): `tipoEstabelecimento` (`@NotNull`;
existência: domínio), `nomeEstabelecimento` (`@NotBlank`, `@Size(max = 120)`),
`numeroCartao`, `descricaoProduto` (`@NotBlank`, `@Size(max = 255)`), `valor`.

**Listagem:** `pagina` (`@Min(1)`), `tamanhoPagina` (`@Min(1)`, `@Max(100)`),
`ordenacao` e `direcao` (enum fechado).

---

## 6. Um Só Formato de `400`

*(O Spring sinaliza falha de validação por exceção, não por um tipo de
resultado. O que se mantém é a **forma única** da falha.)*

Os três caminhos de falha de entrada chegam ao **mesmo** corpo
([`API.md`](API.md#erros-de-validação-campo-a-campo)):

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

| Origem da falha | Como chega | Quem converte |
|---|---|---|
| Bean Validation no corpo | `MethodArgumentNotValidException` | `FiltroDeProblemaDeApi` |
| Bean Validation em parâmetro | `ConstraintViolationException` | `FiltroDeProblemaDeApi` |
| Desserialização do corpo | `HttpMessageNotReadableException` | `FiltroDeProblemaDeApi` (seção 7) |
| Domínio (forma) | `Resultado.Falha(ErroValidacao)` | `RespostaDeResultado`, via `camposComErro()` |

**Regras:**

- **Todos os erros de uma vez.** O Bean Validation coleta **todas** as
  violações do objeto; o `errors` as agrupa por campo. Parar no primeiro erro
  obriga o usuário a um ciclo de tentativa por campo. *(O domínio devolve um
  erro por vez — é uma conversão, não um objeto inteiro.)*
- **A chave é o campo da fronteira** (`numeroCartao`, `endereco.cep`) — o
  nome do campo **do JSON**, não da variável Java nem da coluna.
- **Ordem determinística.** O Hibernate Validator **não garante** a ordem das
  violações; as chaves de `errors` saem **ordenadas**, e as mensagens de cada
  campo também — duas respostas iguais produzem bytes iguais
  ([`API.md`](API.md#6-serialização-determinística)).
- **O valor rejeitado nunca vai ao corpo — nem ao log.** O `FieldError` guarda
  o `rejectedValue`, e o `getMessage()` da exceção o **inclui**:
  `... rejected value [5555000011112222]`. Só a **mensagem da regra** é
  usada. *(Exceção que não é vazamento: o **nome** de um campo desconhecido, que
  é a chave que o próprio cliente enviou.)*
- **Exceção não é controle de fluxo do domínio.** Exceção de validação existe
  **só na fronteira com o framework**; o domínio devolve `Resultado`
  ([`ERROS.md`](ERROS.md)).

---

## 7. Mensagens de Erro Acionáveis

A mensagem é a interface entre o sistema e quem tentou a operação inválida. O
critério: **um usuário sem conhecimento técnico do sistema consegue corrigir a
entrada só lendo a mensagem?**

- **Em português, sempre — de forma determinística.** A mensagem padrão do
  Bean Validation depende do **locale da requisição**; sem ajuste, um cliente
  com `Accept-Language: en` recebe inglês. Duas medidas, **ambas**:

  ```yaml
  # application.yml
  spring:
    web:
      locale: pt_BR
      locale-resolver: fixed        # ignora o Accept-Language do cliente
  ```

  ```properties
  # src/main/resources/ValidationMessages.properties — sobrescreve as mensagens-padrão
  javax.validation.constraints.NotBlank.message=não deve estar em branco
  javax.validation.constraints.NotNull.message=é obrigatório
  javax.validation.constraints.Positive.message=deve ser maior que zero
  javax.validation.constraints.Size.message=deve ter no máximo {max} caracteres
  javax.validation.constraints.Digits.message=deve ter no máximo {integer} dígitos inteiros e {fraction} casas decimais
  javax.validation.constraints.Past.message=deve ser uma data passada
  javax.validation.constraints.Email.message=não é um e-mail válido
  ```

  O *locale* fixo cobre o que o *bundle* não sobrescreve; o *bundle* faz o
  texto independer do idioma da JVM (inclusive no teste unitário).
- **O campo já está na chave do `errors`**, então a mensagem pode ser curta e
  sem repetir o nome: `"numeroCartao": ["não deve estar em branco"]`.
- **Descreve o problema e orienta a correção**; onde "não deve estar em
  branco" não basta, a `message` da anotação é mais específica
  (`"deve ter no máximo 2 casas decimais"`).
- **Sem jargão de implementação** — nome de classe, SQL, "enum inválido",
  expressão regular, pilha: detalhe que não serve ao usuário e serve ao
  atacante.
- **`title`/`detail` são texto humano e podem mudar sem nova versão**; o
  identificador de máquina é o `type`, nunca a mensagem.

### O Jackson vaza o modelo — traduzir

`HttpMessageNotReadableException` carrega o texto do Jackson, que cita **nomes
de classe** do projeto (`Unrecognized field "saldo" (class
br.com.alelo…MensagemDadosConsumidor)`) e posição no corpo. **Nada disso vai
ao cliente.** A causa é inspecionada e traduzida:

| Causa | Resposta em `errors` |
|---|---|
| Campo desconhecido (`UnrecognizedPropertyException`) | `"saldo": ["não é aceito nesta operação"]` |
| Tipo errado (`InvalidFormatException`, ex.: texto no lugar de número) | `"valor": ["não é um número válido"]` |
| JSON malformado (`JsonParseException`) | `"corpo": ["não é um JSON válido"]` |
| Corpo ausente | `"corpo": ["é obrigatório"]` |
| Parâmetro de query com tipo/enum inválido (`MethodArgumentTypeMismatchException`) | `"ordenacao": ["não é um valor aceito"]` |
| Parâmetro obrigatório ausente (`MissingServletRequestParameterException`) | `"pagina": ["é obrigatório"]` |

```text
// CORRETO — descreve e orienta
"valor": ["deve ser maior que zero"]
"saldo": ["não é aceito nesta operação"]

// ERRADO — vago: não diz o que corrigir
"Dados inválidos"

// ERRADO — técnico: expõe implementação
"Unrecognized field \"saldo\" (class br.com.alelo.consumer.consumerpat.api.consumidor.MensagemDadosConsumidor)"
"Failed to convert value of type 'java.lang.String' to required type 'int'"
```

---

## 8. Validação de Estado no Caso de Uso

Regras que dependem do **estado do sistema** acontecem **depois** da borda, no
caso de uso, com acesso às portas. O resultado é um **erro tipado de domínio**,
mapeado para status pela tabela central — **nunca disfarçado de erro de
validação**.

Já está definido e não se repete aqui: a verificação explícita antes de gravar
(documento duplicado, versão divergente) e o `unique` do banco como rede de
segurança são as **duas portas** de
[`ERROS.md`](ERROS.md#3-proibição-de-captura-genérica--e-as-duas-portas-de-conversão);
o limite honesto — sob concorrência, a checagem é **conforto de mensagem, não
garantia**, e quem fecha a janela é o banco — está em
[`BANCO.md`](BANCO.md#7-concorrência-versão-de-linha-e-lock).

**Por quê o status importa:** `400` diz "seu dado está malformado — corrija o
corpo". Dado bem formado que conflita com o estado é outra informação: `404`
(cartão inexistente), `409` (documento duplicado), `422` (saldo insuficiente).
Devolver `400` para tudo obriga o consumidor a ler texto para descobrir o que
aconteceu.

---

## 9. Nunca Confie no Remetente

Toda requisição é validada **completamente** pelo servidor, seja quem for o
cliente. Não há front-end próprio neste projeto; qualquer cliente é um
terceiro, pode estar desatualizado ou ser hostil.

- **Nenhum campo é usado sem passar pela fronteira**, inclusive os que "o
  cliente jamais envia errado".
- **A validação do cliente, se existir, é cópia para UX**, não caminho de
  segurança.
- **As restrições são documentação executável**: o `springdoc` lê as anotações
  do Bean Validation e as publica no OpenAPI (`/v3/api-docs`) — o contrato que
  o cliente vê é derivado das **mesmas** regras que o servidor aplica, sem
  segunda fonte para divergir ([`API.md`](API.md#12-contrato-público-e-testes-de-contrato)).

---

## 10. Texto Livre — Validar Não é Sanitizar

*(Substitui "Uploads e conteúdo": não há upload nem HTML.)*

- **Campo de texto livre** (`nome`, `rua`, `cidade`, `descricaoProduto`, nome do
  estabelecimento) **recusa caractere de controle** (quebra de linha, tabulação,
  `\u0000`): `@Pattern(regexp = "^[^\\p{Cntrl}]*$")`. O motivo é de **segurança**:
  quebra de linha em dado do usuário que acaba num log de texto é **injeção de
  log** ([`SEGURANCA.md`](SEGURANCA.md#5-logs-sem-dados-sensíveis)).
- **A fronteira decide se aceita; não limpa.** O sistema **não sanitiza** texto
  livre: recusa o que não admite, e guarda o resto como veio. Saída é JSON,
  escapado pelo serializador — não há HTML a sanitizar.
- **Se um dia o sistema aceitar HTML**, vale esta regra: *allowlist* de
  tags e atributos **no momento de salvar**, nunca *blacklist*, nunca "limpar
  na renderização" — e esta seção é reescrita no mesmo PR.
- **Uploads**: não existem. Se aparecerem, valem tamanho máximo antes de
  alocar, extensão **e** conteúdo real, nome interno gerado pelo servidor
  ([`SEGURANCA.md`](SEGURANCA.md)).
- **`@Past` usa o relógio do sistema**, não o `Clock` injetado — **exceção
  declarada** à regra de que o tempo nunca é o real
  ([`VALORES.md`](../codigo/VALORES.md#5-relógio--dois-propósitos-dois-relógios)).
  É a única validação de borda que depende do tempo, e não afeta saldo; o
  custo de injetar um `ClockProvider` no Hibernate Validator não compensa. Se a
  regra passar a importar (por exemplo, "maior de 18 anos"), ela **sobe para o
  domínio**, onde o `Clock` já existe.

---

## 11. Validação é Coberta por Teste

Validação é cobertura de **prioridade alta** — primeira linha de defesa
([`TESTES.md`](TESTES.md#2-prioridade-de-cobertura)):

- **Uma regra, um teste** — `deve_recusar_quando_valor_nao_positivo`,
  `deve_aceitar_quando_valor_no_limite`. O caso feliz também: um validador que
  rejeita dado **válido** é um bug tão caro quanto o que deixa passar sujeira.
- **O teste unitário usa o próprio `Validator`**, sem subir o Spring (o
  Hibernate Validator e o EL vêm no `starter-validation`):

  ```java
  class MensagemRegistrarCompraTeste {
      private static final Validator VALIDADOR = Validation.buildDefaultValidatorFactory().getValidator();

      @Test
      void deve_recusar_quando_valor_nao_positivo() {
          // Preparar
          var mensagem = FabricaDeMensagens.compra().comValor("0").construir();

          // Agir
          var violacoes = VALIDADOR.validate(mensagem);

          // Verificar
          assertThat(violacoes).extracting(v -> v.getPropertyPath().toString()).containsExactly("valor");
      }
  }
  ```

- **O `400` é teste de contrato** — status, `type`, `errors` campo a campo, e
  **a ausência do valor rejeitado** no corpo
  ([`API.md`](API.md#12-contrato-público-e-testes-de-contrato)).
- **Erro de estado tem teste no caso de uso**, não na mensagem — um teste de
  mensagem que consulta repositório está testando a seção errada.
- **Erro de domínio (forma)** tem teste do tipo de valor (`DocumentoTeste`,
  `TipoCartaoTeste`) **e** um de contrato mostrando o **mesmo** `400`.

| Teste | Garante | Tipo |
|---|---|---|
| `Mensagem*Teste` (uma regra por teste, caso feliz incluído) | cada anotação recusa e aceita o que deve | Unitário |
| `EntradaValidadaTeste` | todo `@RequestBody` tem `@Valid`; todo controller com restrição em parâmetro tem `@Validated`; todo `String` de entrada tem `@Size(max)` | Arquitetura |
| `deve_responder_400_com_errors_quando_valor_nao_positivo` | formato único do `400` | Contrato |
| `deve_omitir_o_valor_rejeitado_quando_responder_400` | o número do cartão enviado **não** aparece no corpo | Contrato |
| `deve_traduzir_o_erro_do_jackson_quando_campo_desconhecido` | `"saldo": ["não é aceito nesta operação"]`, sem nome de classe | Contrato |
| `deve_responder_400_com_o_mesmo_formato_quando_tipo_de_estabelecimento_inexistente` | borda e domínio produzem o mesmo `400` | Contrato |
| `deve_responder_em_portugues_quando_accept_language_ingles` | o locale fixo vale | Contrato |
| `deve_recusar_quando_texto_livre_tem_quebra_de_linha` | caractere de controle recusado | Unitário |

*(Entram na tabela da seção 14 de [`TESTES.md`](TESTES.md#14-testes-exigidos-pelos-outros-documentos).)*

---

## Anti-Padrões

| Anti-padrão | Correto |
|---|---|
| `@RequestBody` sem `@Valid` | `@Valid` em todo corpo; `EntradaValidadaTeste` (seção 4) |
| Receber `@Entity` no `@RequestBody` (o legado) | `Mensagem*` própria da operação (seção 1) |
| Regra de formato dentro do caso de uso ou do domínio | anotação na `Mensagem*`; tipo de valor para a regra real (seções 1 e 3) |
| Validação improvisada no controller, `400` por conta própria | ponto único; o *advice* converte (seção 4) |
| Mesma regra de formato na mensagem **e** no tipo | mensagem valida o grosso; o tipo é a regra única (seção 3) |
| `Validador*` com acesso a repositório | só o objeto; estado no caso de uso (seções 5 e 8) |
| Erro de estado devolvido como `400` | `ErroDominio` mapeado para 404/409/422 (seção 8) |
| Tipo de estabelecimento inexistente devolvido como `404`/`422` | é entrada malformada: `400` com `errors` (seção 3) |
| Erro de domínio (forma) com formato de `400` diferente do da borda | `camposComErro()` — o mesmo `400` (seção 3) |
| Devolver o valor rejeitado (`rejectedValue`, `getMessage()`) | só a mensagem da regra (seção 6) |
| Devolver o texto do Jackson (nome de classe, posição) | traduzir a causa (seção 7) |
| Mensagem em inglês conforme o `Accept-Language` | locale fixo em pt-BR + `ValidationMessages.properties` (seção 7) |
| `errors` em ordem não determinística | chaves e mensagens ordenadas (seção 6) |
| Parar no primeiro erro | todos os erros de uma vez (seção 6) |
| `String` de entrada sem `@Size(max)` | teto em todo texto, espelhando o `VARCHAR` (seção 5) |
| Dinheiro em `double` na mensagem | `BigDecimal` + `@Digits(13, 2)` (seção 5) |
| Texto livre aceitando quebra de linha | recusa caractere de controle (seção 10) |
| "Sanitizar" texto livre por limpeza | recusar o que não admite; sem HTML a sanitizar (seção 10) |
| Só o cliente valida; o servidor confia | o servidor valida sempre (seção 9) |
| Ponto de entrada novo sem linha na tabela da seção 2 | tabela atualizada no mesmo PR |
| Regra nova sem teste unitário, ou `400` sem teste de contrato | teste por regra + contrato no mesmo PR (seção 11) |

---

## Checklist

Antes de abrir um PR que mexe em entrada de dados:

- [ ] Todo ponto de entrada novo está na tabela da [seção 2](#2-pontos-de-entrada-externa)?
- [ ] Todo `@RequestBody` tem `@Valid`, e todo controller com restrição em parâmetro tem `@Validated`?
- [ ] Todo `String` de entrada tem `@Size(max)` igual ao `VARCHAR` da coluna?
- [ ] Dinheiro é `BigDecimal` com `@Positive` e `@Digits(13, 2)`?
- [ ] A regra de formato real está no tipo de valor, e a mensagem só valida o grosso?
- [ ] Erro de entrada malformada que só o domínio detecta devolve `ErroValidacao` com `camposComErro()`?
- [ ] Nenhum erro de estado (existência, duplicidade, saldo) voltou como `400`?
- [ ] O `400` sai no formato único, com `errors` campo a campo, ordenado?
- [ ] O corpo do `400` **não** contém o valor rejeitado nem texto do Jackson (nome de classe)?
- [ ] Toda mensagem é curta, em português, sem jargão, e vem do `ValidationMessages.properties`?
- [ ] Texto livre recusa caractere de controle?
- [ ] Cada regra nova tem teste unitário (inclusive o caso feliz), e o contrato do `400` foi atualizado no mesmo PR?
- [ ] O `EntradaValidadaTeste` continua passando?
- [ ] Este documento descreve o estado atual (não o de quando nasceu)?

---

## Decisões Deste Documento

O consolidado está em [`DECISOES.md`](../fundamentos/DECISOES.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| Validação **declarativa** (Bean Validation) nas `Mensagem*` | classes `Validador<Entrada>` em camada própria | `starter-validation` já está no projeto; nível 10 de `CONDICIONAIS.md` |
| `ErroValidacao` de domínio com `camposComErro()`, produzindo o mesmo `400` | `400` sem campo para erro de domínio | o cliente não deve distinguir quem recusou |
| Regra de formato real no tipo de valor; mensagem valida o grosso | validar nos dois lugares | uma fonte para não divergir |
| Locale fixo pt-BR + `ValidationMessages.properties` | mensagem conforme `Accept-Language` | mensagem em português determinística |
| Valor rejeitado nunca no corpo; erro do Jackson traduzido | devolver a mensagem do framework | vazamento de dado e de nome de classe |
| `EntradaValidadaTeste` | confiar na revisão | `@RequestBody` sem `@Valid` compila e não valida |
| `@Past` com o relógio do sistema | injetar `ClockProvider` | única validação de borda dependente do tempo; regra que importe sobe ao domínio |
| Texto livre recusa caractere de controle, sem sanitizar | limpar o texto | injeção de log; recusar é mais honesto que alterar |

---

**[Segurança →](SEGURANCA.md)**
