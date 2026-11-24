[← Índice](../../PADRONIZACAO_INDEX.md) · [← DOCUMENTACAO.md](../codigo/DOCUMENTACAO.md)

# Organização Estrutural — Java 17

> **Escopo**: a regra de dependência, os princípios de organização de
> arquivo e a tabela "cada tipo em seu lugar" para o código **Java 17**
> deste projeto. A unidade de separação é o **pacote dentro de um único
> módulo Gradle**, a regra é imposta por teste de arquitetura, e os exemplos
> usam o domínio de cartões de benefício. Ver
> [Decisões de Estrutura](#decisões-de-estrutura).
>
> **Nota sobre exemplos — e estado deste documento**: os trechos de código
> e as árvores de diretório abaixo são **ilustrativos** — a regra
> normativa é o texto conceitual, não os nomes exatos de arquivo/tipo
> exibidos. Além disso, o código atual do projeto ainda é o **legado do
> desafio** (um único controller, entidades no mesmo pacote): aqui as
> árvores são a **estrutura pretendida** da refatoração, não um espelho do
> que já existe. Ver [Este Documento É Vivo](#este-documento-é-vivo).

---

## Sobre Este Documento

Define a **organização de pacotes por camada**, a **regra de dependência**
entre camadas e os **princípios de organização de arquivo** do projeto.
Nenhuma pasta aqui é "a forma como aconteceu" — cada uma existe para
impor, de forma verificável, a direção de dependência da Clean
Architecture, não só para organizar por gosto.

**Quando consultar:**

- Ao criar um arquivo novo e não saber em qual pacote ele deveria morar
- Ao revisar um PR que adiciona um `import` novo entre camadas
- Ao propor mover um tipo de um lugar para outro
- Ao criar um pacote novo
- Ao decidir onde um fake ou fixture de teste deve viver
- Ao mudar a árvore de pastas do repositório — para saber onde registrar a mudança

---

## Este Documento É Vivo

**Estado do repositório ao tempo desta escrita:** código legado do desafio
em `src/main/java/.../consumerpat/` (um `ConsumerController`, entidades
`Consumer`/`Extract` e repositórios em pacotes técnicos
`controller`/`entity`/`respository`) e a pasta `docs/`. Tudo que segue é
desenho de uma estrutura que a refatoração ainda vai criar.

| | Documento de um projeto já refatorado | Este documento, hoje |
|---|---|---|
| Árvore de pastas | espelho — registrada a partir do que existe | desenho — o código ainda vai alcançá-la |
| Exemplos de tipo/arquivo | fieis ao repositório (evoluíram com ele) | candidatos — podem mudar quando o domínio real for escrito |
| Quando muda | no mesmo PR que muda a estrutura real | a cada PR que ajusta o desenho — e, depois, no mesmo PR que muda a estrutura real |

O mesmo critério que torna os READMEs de cada camada documentos vivos
(ver [`DOCUMENTACAO.md`](../codigo/DOCUMENTACAO.md#o-readme-evolui-com-o-projeto--regra-vinculante))
vale aqui, em dois tempos:

1. **Antes da refatoração (agora):** a árvore é **contrato**. Quem vai
   escrever código está obrigado a alcançá-la; quem quer desviar dela
   justifica no PR — e, se for escolha arquitetural, a mudança entra em
   [`DECISOES.md`](DECISOES.md), nunca acontece por deriva silenciosa.
2. **Depois do PR que cria a estrutura:** a árvore passa a ser
   **espelho**. Mudou a estrutura real, este documento mudou no mesmo PR.

**Regras vinculantes deste documento:**

- **Nasce junto.** O PR que cria o primeiro arquivo de uma camada atualiza
  a árvore deste documento no mesmo PR — mesmo ciclo do README que nasce
  com a camada.
- **Exemplo que contradiz código é bug de documentação** — tem a
  prioridade de correção de um bug, não a de "melhoria de docs".
- **Sem histórico narrativo.** O que mudou, e por quê, fica no commit/PR;
  o que *é* fica aqui. Este documento é regerado para descrever o estado
  atual — ele não acumula narrativa de evolução.
- **Vinculante mesmo antes do código:** a regra de dependência entre
  camadas, a camada de cada tipo (tabela [Cada Tipo em Seu Lugar](#11-cada-tipo-em-seu-lugar)),
  um tipo público por arquivo, testes espelhando produção, composition
  root única na API.
- **Ilustrativo até o primeiro PR que nascer:** nomes de entidades e de
  casos de uso, subpacotes dentro de cada camada e a forma exata dos
  identificadores nos exemplos.
- **Divergência entre desenho e código se resolve na hora:** ou o código
  volta para o desenho, ou o desenho muda com motivo registrado — nunca
  "o documento já estava velho".

---

## Visão Geral da Solução

O projeto segue Clean Architecture, com separação de camadas em
**pacotes distintos de um único módulo Gradle**. Como o compilador não
impede um pacote de importar outro (ao contrário de módulos separados, onde
a dependência ausente não compila), a separação é imposta por **revisão + teste de arquitetura** — ver
[A Regra é Verificável, Não de Boa Vontade](#a-regra-é-verificável-não-de-boa-vontade).

Raiz de pacotes: `br.com.alelo.consumer.consumerpat` (abreviada `…consumerpat`
nas árvores abaixo).

### Camadas da Solução

| Pacote | Camada | Pode importar |
|---|---|---|
| `…consumerpat.dominio` | Domínio | apenas a biblioteca padrão do Java (`java.*`) |
| `…consumerpat.aplicacao` | Aplicação | `dominio` (+ `@Transactional` do Spring — exceção documentada) |
| `…consumerpat.infraestrutura` | Infraestrutura | `dominio`, `aplicacao`, Spring Data/JPA, Lombok; **SLF4J/Logback somente em `infraestrutura.log`** |
| `…consumerpat.api` | API / Composition Root | `dominio`, `aplicacao`; **`infraestrutura` somente em `api.configuracao`**; **`org.slf4j.MDC` somente em `api.comum`** |
| `src/test/java/…` | Testes | todas as camadas acima |

**Pacote herdado do enunciado:** `…consumerpat.config` (`OpenApiConfig`)
é mantido onde está — o enunciado do desafio o exclui da revisão. Para fins
de camada, é tratado como parte de `api`.

---

## Estrutura Pretendida por Camada

Árvore **pretendida** da refatoração. Cada comentário indica a razão de
existir da pasta, não um conteúdo já escrito. Quando o código real nascer,
o PR substitui este desenho; qualquer diferença se resolve pelas regras de
[Este Documento É Vivo](#este-documento-é-vivo).

### `…consumerpat.dominio`

```text
dominio/                                   # zero Spring, zero JPA, zero Lombok — só java.*
├── README.md                              # nasce com a camada — ver codigo/DOCUMENTACAO.md
├── package-info.java
├── consumidor/
│   ├── Consumidor.java                    # agregado raiz — record imutável; dados cadastrais e cartões
│   ├── ResumoDeConsumidor.java            # projeção de listagem: id e nome, sem cartões — qualidade/PERFORMANCE.md §3
│   ├── Documento.java                     # tipo de valor: só dígitos (11/14), mascarado(), toString() mascarado — qualidade/BANCO.md §11
│   ├── Contato.java                       # tipo de valor: celular, residencial, telefone, e-mail
│   └── Endereco.java                      # tipo de valor: rua, número, cidade, país, CEP
├── cartao/
│   ├── Cartao.java                        # saldo e regra de débito/crédito — record imutável
│   ├── NumeroDeCartao.java                # tipo de valor: só dígitos (13–19), mascarado(), toString() mascarado — qualidade/BANCO.md §11
│   └── TipoCartao.java                    # enum: código do estabelecimento + ajuste fracionário (-0.10 / 0.00 / +0.35)
├── dinheiro/
│   └── Dinheiro.java                      # record com BigDecimal, escala fixa de 6 casas — codigo/VALORES.md §6
├── extrato/
│   └── Extrato.java                       # registro de uma compra efetivada
├── pagina/
│   └── Pagina.java                        # página de resultados sem depender de Pageable do Spring
├── resultado/
│   ├── Resultado.java                     # sealed interface — ver codigo/CONDICIONAIS.md §4
│   ├── Sucesso.java
│   ├── Falha.java
│   └── Unit.java                          # "sem valor" — para Resultado<Unit, E>
├── erros/                                 # hierarquia por categoria de falha — ver qualidade/ERROS.md
│   ├── ErroDominio.java                   # interface base (não sealed: permitidos têm de estar no mesmo pacote)
│   ├── CategoriaErro.java                 # enum → status HTTP (dado no enum)
│   ├── naoencontrado/                     # cada categoria é sealed interface + variantes, no mesmo pacote
│   │   ├── ErroNaoEncontrado.java
│   │   ├── ErroConsumidorNaoEncontrado.java
│   │   └── ErroCartaoNaoEncontrado.java
│   ├── regradenegocio/
│   │   ├── ErroRegraDeNegocio.java
│   │   └── ErroSaldoInsuficiente.java
│   ├── conflito/
│   │   ├── ErroConflito.java
│   │   ├── ErroDocumentoJaCadastrado.java
│   │   ├── ErroNumeroDeCartaoJaCadastrado.java
│   │   └── ErroVersaoDivergente.java
│   └── validacao/
│       ├── ErroValidacao.java
│       └── ErroTipoEstabelecimentoInvalido.java
├── log/                                   # vocabulário de log em português — sem tipo de biblioteca de log
│   ├── SaidaDeLogPorta.java               # depuracao/informacao/aviso/erro/critico — runtime/OBSERVABILIDADE.md §1
│   └── CampoDeLog.java                    # nome + valor; o dado separado do evento
├── saude/
│   ├── SaudePorta.java                    # estado de cada dependência crítica (hoje: banco)
│   └── EstadoDeSaude.java                 # SAUDAVEL / DEGRADADO + status por dependência
└── portas/                                # contratos — pasta única enquanto forem poucas
    ├── ConsumidorPorta.java
    ├── CartaoPorta.java
    └── ExtratoPorta.java                  # só registrar(): extrato é imutável — qualidade/BANCO.md §8
```

### `…consumerpat.aplicacao`

```text
aplicacao/                                 # depende só de dominio (+ @Transactional)
├── README.md
├── package-info.java
├── casosdeuso/
│   ├── consumidor/
│   │   ├── CriarConsumidorCaso.java       # DtoNovoConsumidor → Consumidor → Resultado<T, E>
│   │   ├── AtualizarConsumidorCaso.java   # nunca altera saldo: só dados cadastrais
│   │   ├── ObterConsumidorCaso.java       # leitura por id — alvo do Location do POST
│   │   └── ListarConsumidoresCaso.java    # paginado
│   ├── cartao/
│   │   ├── CreditarCartaoCaso.java
│   │   └── RegistrarCompraCaso.java       # aplica fator do tipo, debita, grava extrato — uma transação
│   └── saude/
│       └── ConsultarSaudeCaso.java        # consulta a SaudePorta; o controller nunca toca o DataSource
└── dtos/                                  # transferência interna entre camadas (nunca fronteira REST)
    ├── DtoDadosCadastrais.java
    ├── DtoNovoConsumidor.java
    ├── DtoCompra.java
    └── DtoComprovanteCompra.java
```

> Não há pasta `validadores/` aqui: a validação
> **técnica** de entrada é declarativa (Bean Validation) e vive nas
> `Mensagem*` da API — ver [`CONDICIONAIS.md`](../codigo/CONDICIONAIS.md), nível 10.
> A validação de **domínio** é guard/erro tipado dentro do próprio domínio.

### `…consumerpat.infraestrutura`

```text
infraestrutura/                            # implementa as portas de dominio; conhece JPA
├── README.md
├── package-info.java
├── log/
│   └── SaidaDeLogAdaptador.java           # ÚNICA classe com SLF4J/Logback: formata evento chave=valor, máscara por nome, marker CRITICO
├── saude/
│   └── SaudeDoBancoAdaptador.java         # Connection.isValid(timeout) no DataSource — sem expor internals
└── persistencia/                          # organizada por agregado (conceito), não por tipo técnico
    ├── consumidor/
    │   ├── ConsumidorEntidade.java        # @Entity — package-private: ninguém fora daqui a vê
    │   ├── ConsumidorRepositorioJpa.java  # Spring Data — package-private
    │   ├── ConsumidorMapeador.java        # entidade ⇄ domínio, manual (sem MapStruct)
    │   └── ConsumidorAdaptador.java       # único tipo público: implementa ConsumidorPorta
    ├── cartao/
    │   ├── CartaoEntidade.java            # unique(numero), @Version
    │   ├── CartaoRepositorioJpa.java      # consultas com @Lock(PESSIMISTIC_WRITE)
    │   ├── CartaoMapeador.java
    │   └── CartaoAdaptador.java           # implementa CartaoPorta
    └── extrato/
        ├── ExtratoEntidade.java
        ├── ExtratoRepositorioJpa.java
        ├── ExtratoMapeador.java
        └── ExtratoAdaptador.java          # implementa ExtratoPorta
```

### `…consumerpat.api`

```text
api/                                       # composition root — único lugar que conhece todas as camadas
├── README.md
├── package-info.java
├── consumidor/                            # por conceito: o que muda junto, mora junto
│   ├── ConsumidorController.java          # fino: decodifica Mensagem*, chama caso de uso, devolve Mensagem*
│   ├── MensagemDadosConsumidor.java       # entrada validada (Bean Validation)
│   ├── MensagemCriarConsumidor.java
│   ├── MensagemConsumidor.java            # saída do detalhe (inclui cartões)
│   ├── MensagemResumoConsumidor.java      # saída da listagem: id e nome
│   └── MensagemCartao.java
├── cartao/
│   ├── CartaoController.java              # crédito e compra — POST, nunca GET
│   ├── MensagemCreditarCartao.java
│   ├── MensagemRegistrarCompra.java
│   └── MensagemComprovanteCompra.java
├── saude/
│   ├── SaudeController.java               # GET /api/v1/saude — 200 saudável, 503 degradado
│   └── MensagemSaude.java
├── comum/
│   ├── MensagemPagina.java
│   ├── MensagemProblema.java              # corpo de erro RFC 7807 (Spring 5 não tem ProblemDetail) — ver qualidade/API.md §3
│   ├── ProblemaDeBorda.java               # enum: código + título + status dos erros que não passam por ErroDominio — ERROS.md §8
│   ├── FiltroDeProblemaDeApi.java         # @RestControllerAdvice: exceção de borda → MensagemProblema
│   ├── RespostaDeResultado.java           # Resultado<T, ErroDominio> → ResponseEntity (categoria → status)
│   ├── FiltroDeCabecalhosDeSeguranca.java # Servlet Filter mais externo, só /api/*: nosniff, frame, CSP — qualidade/SEGURANCA.md §6
│   ├── FiltroDeCorrelacao.java            # Servlet Filter: gera/aceita idRequisicao, popula o MDC (única classe fora de infraestrutura.log com org.slf4j — só MDC)
│   └── FiltroDeLogDeConclusao.java        # Servlet Filter: status e duração, com o id da correlação
└── configuracao/                          # ÚNICO lugar com @Bean e que importa infraestrutura
    ├── ConfiguracaoDePortas.java          # instancia adaptadores concretos
    ├── ConfiguracaoDeCasosDeUso.java      # instancia casos de uso com as portas injetadas e o Clock (Clock.systemUTC())
    ├── ConfiguracaoDeAuditoria.java       # @EnableJpaAuditing com o Clock do domínio (qualidade/BANCO.md §6)
    ├── ConfiguracaoDeFiltros.java         # registra os filtros na ordem do contrato (qualidade/API.md §4)
    └── ConfiguracaoDeWeb.java             # Cache-Control: no-store em /api/v1/** (PERFORMANCE.md §5) e CORS fechado por padrão (SEGURANCA.md §8)
```

A classe `ConsumerTestApplication` (`@SpringBootApplication`) fica na raiz
`…consumerpat` e **só inicializa**, sem regra de negócio. O pacote herdado `…consumerpat.config` segue como está.

### Testes (`src/test/java`)

```text
src/test/java/…consumerpat/                # espelha produção, pacote a pacote
├── dominio/
│   ├── cartao/
│   │   ├── CartaoTeste.java
│   │   └── TipoCartaoTeste.java
│   └── resultado/
│       └── ResultadoTeste.java
├── aplicacao/
│   ├── casosdeuso/
│   │   ├── cartao/
│   │   │   └── RegistrarCompraCasoTeste.java
│   │   └── consumidor/
│   │       └── AtualizarConsumidorCasoTeste.java   # saldo preservado
│   └── fakes/                             # fakes de porta — vivem aqui, nunca em src/main
│       ├── CartaoPortaFake.java           # mapa em memória, sem banco; obedece o contrato da porta (ausência → Falha)
│       ├── ConsumidorPortaFake.java
│       ├── SaidaDeLogFake.java            # captura eventos e campos: prova "o log não contém o número do cartão"
│       ├── SaudePortaFake.java            # força SAUDAVEL/DEGRADADO no contrato do /api/v1/saude
│       └── ExtratoPortaFake.java
├── fabricas/                              # fixtures programáticas — valores válidos por padrão, com*() para variar
│   ├── FabricaDeCartoes.java              # número único por sequência (AtomicLong): testes nunca colidem no unique
│   ├── FabricaDeConsumidores.java
│   └── FabricaDeCompras.java
├── infraestrutura/
│   └── persistencia/
│       ├── cartao/
│       │   └── CartaoAdaptadorTeste.java  # @DataJpaTest contra H2: trava, unicidade
│       └── consumidor/
│           ├── ListagemDeConsumidoresTeste.java  # conta comandos SQL da listagem — guarda contra N+1
│           └── GeradorDeConsumidores.java        # 50 mil registros, só para medir — nunca em src/main
├── api/
│   ├── cartao/
│   │   ├── CartaoControllerContratoTeste.java   # status, serialização, corpo de erro
│   │   └── ConcorrenciaDeCompraTeste.java       # N compras simultâneas, saldo final exato
│   └── consumidor/
│       └── ConsumidorControllerContratoTeste.java
└── arquitetura/
    ├── RegraDeDependenciaTeste.java       # varre imports: viola a tabela de camadas → falha
    ├── EntradaValidadaTeste.java          # @RequestBody sem @Valid, controller sem @Validated, String de entrada sem @Size → falha (qualidade/VALIDACAO.md §4)
    └── EstadoCompartilhadoTeste.java      # static mutável, campo não final em bean, synchronized/parallelStream/@Async → falha (runtime/CONCORRENCIA.md §4)
```

---

## Regra de Dependência

**Dependências só fluem para dentro:**

```text
api → aplicacao → dominio
          ↑
infraestrutura → aplicacao → dominio
```

Ou seja:

- **Domínio** não importa nada do projeto nem de framework (nem Spring,
  nem JPA, nem Lombok) — só `java.*`.
- **Aplicação** importa apenas **Domínio**; a única classe de framework
  permitida é `org.springframework.transaction.annotation.Transactional`
  (fronteira de transação — exceção documentada no README da camada).
- **Infraestrutura** importa **Domínio** (e, se precisar, **Aplicação**) e
  implementa as portas definidas no domínio.
- **API** importa **Domínio** e **Aplicação**. Apenas o subpacote
  `api.configuracao` importa **Infraestrutura** — é o composition root.
  Controller e mensagem que importam `infraestrutura` violam a regra.

Qualquer dependência que viole essa regra é rejeitada em code review **ou
derruba o teste de arquitetura**.

### A Regra é Verificável, Não de Boa Vontade

Num projeto multi-módulo, o grafo de dependências impede a violação em
compilação. Num módulo Gradle único, `import br.com.…api.X` a partir de
`dominio` **compila** — então a direção de dependência não pode viver só
na cabeça de ninguém. Três mecanismos, do mais forte ao mais fraco:

1. **Teste de arquitetura** (`RegraDeDependenciaTeste`) — varre os `import`
   de cada camada em `src/main` e falha se achar prefixo proibido.
   Escrito em JUnit puro, **sem ArchUnit** (o desafio proíbe dependência
   nova). Esboço — validar ao implementar:

   ```java
   class RegraDeDependenciaTeste {

       private static final String BASE = "br.com.alelo.consumer.consumerpat.";

       @Test
       void deve_falhar_quando_dominio_importa_framework_ou_outra_camada() {
           var violacoes = importsProibidos("dominio", List.of(
               BASE + "aplicacao", BASE + "infraestrutura", BASE + "api",
               "org.springframework", "javax.persistence", "lombok"));

           assertEquals(List.of(), violacoes);
       }

       // percorre src/main/java/.../<camada>, lê as linhas `import ...;`
       // e devolve "arquivo -> import" para cada prefixo proibido
       private static List<String> importsProibidos(String camada, List<String> prefixos) { /* ... */ }
   }
   ```

   Um teste por linha da tabela de camadas (domínio, aplicação,
   infraestrutura, api fora de `configuracao`).

2. **Visibilidade de pacote.** Dentro de `infraestrutura.persistencia.*`,
   entidade, repositório Spring Data e mapeador são **package-private**;
   só o `*Adaptador` é `public`. Nada fora da infraestrutura *consegue*
   referenciar uma `@Entity` — o compilador impede, sem teste.

3. **Revisão.** O revisor confronta cada `import` novo com a tabela de
   camadas.

> **Se o projeto crescer**, o caminho natural é **multi-módulo Gradle**
> (`implementation project(':dominio')`): aí a regra volta a ser falha de
> build. Foi descartado agora para não complicar o `./gradlew bootRun` do
> desafio — decisão registrada em [`DECISOES.md`](DECISOES.md).
> **JPMS** (`module-info.java`) também foi descartado: a integração com
> Spring Boot/Hibernate é frágil.

---

## Onde Vivem Fakes e Fixtures de Porta

Toda implementação de uma porta usada **apenas** em teste (nunca em
código de produção) vive em `src/test/java`, espelhando o pacote que a
porta serve:

```text
src/test/java/…consumerpat/aplicacao/fakes/
├── CartaoPortaFake.java           # implementa CartaoPorta com mapa em memória
└── ExtratoPortaFake.java
```

Regras:

1. **Um fake não entra em `src/main`.** Nenhum código de produção
   referencia um tipo de teste — se o adaptador só existe para teste, ele
   mora em `src/test`, e a composition root de produção jamais o injeta.
2. **Fixtures são construídas no teste**, programaticamente — nunca
   carregadas de arquivos externos.
3. **Fakes de porta ≠ mock de detalhe.** Fake substitui uma porta inteira
   (fronteira real, sem I/O); mock interno de método privado é teste de
   detalhe de implementação, proibido.

---

## Princípios de Organização

A árvore das seções acima define *onde* cada camada mora. Esta seção
define, dentro de cada pacote, *como* um arquivo deve ser organizado —
regras que costumam ficar implícitas e que aqui ficam explícitas, porque a disciplina de "um tipo por arquivo" só tem
valor se for seguida sem exceção silenciosa.

### 1. Um Tipo Público por Arquivo

Cada arquivo declara um único tipo público principal. Nome do arquivo =
nome do tipo. **O compilador Java já exige isso para tipos de topo** — o
que este princípio acrescenta é fechar a **brecha dos tipos aninhados**:

```text
// CORRETO
dominio/cartao/Cartao.java                 → public record Cartao
dominio/cartao/TipoCartao.java             → public enum TipoCartao
dominio/erros/ErroSaldoInsuficiente.java   → public record ErroSaldoInsuficiente

// ERRADO — vários tipos "públicos" empilhados como aninhados
dominio/erros/Erros.java                   → public final class Erros {
                                                 public record SaldoInsuficiente(...) {}  ← motivo de mudança diferente
                                                 public record CartaoNaoEncontrado(...) {}
                                             }
```

**"É auxiliar, fica aninhado" está proibido como justificativa de
coabitação.** Uma `record`/`class`/`enum` aninhada — `private`, `static`
ou não — não é licença para empilhar um segundo tipo com identidade e
campos próprios no mesmo arquivo: cada um já é uma razão de mudança
distinta, mesmo pequeno, mesmo sem reuso aparente.

```java
// ERRADO — tipo auxiliar escondido dentro da implementação
public class ValidadorDeCompra {
    private record ResultadoDeRegra(boolean atende, String motivo) { } // segunda razão de mudar, disfarçada
    // ...
}

// CORRETO — extraído para arquivo próprio
// dominio/cartao/regras/ResultadoDeRegra.java
record ResultadoDeRegra(boolean atende, String motivo) { }
```

Em Java isso inclui as **variantes de uma `sealed interface`**: cada
`record` listado em `permits` é um arquivo próprio, no mesmo pacote
(`Sucesso.java`, `Falha.java`) — nunca aninhados na interface.

**Viés padrão: extrair, mesmo sem segundo consumidor visível.** Na dúvida
entre marcar um tipo como aninhado só para justificá-lo junto de outro, o
caminho correto na maioria dos casos é arquivo próprio — um teste contra
esse nome de arquivo cobre exatamente aquela unidade, e o erro relatado
aponta direto para o arquivo certo.

As únicas exceções genuínas são lambda e classe anônima sem identidade
própria, usadas inline e descartadas imediatamente (ex.: um `Comparator`
passado a `sorted`), e o `Builder` gerado por Lombok.

**Por quê:** localizar "onde está definido X" nunca deveria exigir grep —
o nome do arquivo já responde.

---

### 2. Pastas por Conceito, Não por Tipo Técnico

Dentro de cada camada, a organização é por **conceito de domínio**, não
por tipo técnico.

A **homogeneidade semântica decide a fronteira** do subpacote (o que entra
junto e o que fica de fora) — mas isso **não faz da quantidade um
critério irrelevante**. Dizer "criar subpacote exige motivo semântico, não
contagem" jamais pode virar a justificativa para um pacote plano com 30
arquivos "porque são todos do mesmo conceito": pode ser verdade no papel
e ainda assim descrever uma pasta ilegível.

Por isso a regra é **dupla**:

1. **Homogeneidade semântica** decide *onde* o subpacote nasce e *o que*
   ele contém — nunca quantidade sozinha, nunca tipo técnico.
2. **Acúmulo obriga a análise.** Sempre que um pacote plano estiver
   juntando muitos arquivos, o PR analisa a criação de subpacotes e
   registra a conclusão. Criar o subpacote é o padrão; manter o pacote
   plano é o que exige motivo explícito.

> **Régua de revisão:** passou de uma dúzia de arquivos no mesmo pacote
> plano, a pergunta "por que ele ainda é plano?" precisa ser respondida no
> review — e a resposta nunca pode ser apenas "é tudo do mesmo conceito".

```text
// CORRETO — organizado por conceito
dominio/
  cartao/       Cartao.java, TipoCartao.java
  consumidor/   Consumidor.java, Contato.java, Endereco.java
  erros/        ErroDominio.java, ErroSaldoInsuficiente.java
  portas/       CartaoPorta.java, ConsumidorPorta.java

// ERRADO — organizado por tipo técnico (anátema) — é o que o legado faz
consumerpat/
  controller/   ConsumerController.java
  entity/       Consumer.java, Extract.java
  respository/  ConsumerRepository.java, ExtractRepository.java
  util/  models/  services/  helpers/
```

Isto vale **dentro de cada camada**. As próprias camadas (`dominio`,
`aplicacao`, `infraestrutura`, `api`) são, por construção, uma divisão
técnica — e é a única que existe, porque é ela que carrega a regra de
dependência.

```text
// ERRADO — responsabilidades distintas misturadas na raiz do pacote
dominio/cartao/
├── Cartao.java
├── TipoCartao.java
└── MascaraDeNumero.java      ← formatação de apresentação não é regra de cartão

// CORRETO — a fronteira do subpacote segue o motivo de mudar
dominio/cartao/
├── Cartao.java
└── TipoCartao.java
```

**Por quê:** alterações de negócio tocam arquivos próximos uns dos outros,
facilitando navegação e code review — e o acúmulo é o sintoma mais
visível de que a fronteira deixou de ser óbvia. O inverso também vale: "o
pacote está pequeno" nunca é motivo para misturar responsabilidades
diferentes.

---

### 3. Pacotes Espelham Pastas

O `package` declarado deve ser exatamente o caminho da pasta a partir de
`src/main/java`. O compilador aceita divergência, mas ferramentas e
revisores não.

```java
// Arquivo: src/main/java/br/com/alelo/consumer/consumerpat/dominio/cartao/Cartao.java
package br.com.alelo.consumer.consumerpat.dominio.cartao;

public record Cartao(Long id, TipoCartao tipo, String numero, BigDecimal saldo) { /* ... */ }
```

Nomes de pacote: **minúsculas, sem acento, sem cedilha, sem sublinhado**
(`aplicacao`, `casosdeuso`, `infraestrutura`) — o Java aceita acento em
identificador, mas ele quebra em arquivo, shell e CI. A grafia sem acento é
a única convenção do projeto.

---

### 4. Interfaces de Porta No Domínio, Implementação Na Infraestrutura

Interfaces que definem contratos entre aplicação e adaptador (portas)
vivem no **Domínio** (`dominio.portas`). Sua implementação concreta
(adaptador) vive na **Infraestrutura**. A camada é vinculante; o arranjo
interno (uma pasta `portas/` única ou a porta junto do conceito que ela
serve) é detalhe que o PR de criação fixa — aqui, pasta única enquanto
forem poucas.

```java
// Domínio — define o contrato
package br.com.alelo.consumer.consumerpat.dominio.portas;

public interface CartaoPorta {
    Resultado<Cartao, ErroDominio> buscarPorNumeroETipoParaAtualizacao(String numero, TipoCartao tipo);
    Cartao salvar(Cartao cartao);
}

// Infraestrutura — implementa o contrato usando Spring Data JPA
package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

public class CartaoAdaptador implements CartaoPorta {
    private final CartaoRepositorioJpa repositorio;   // package-private: invisível fora daqui

    public CartaoAdaptador(CartaoRepositorioJpa repositorio) { this.repositorio = repositorio; }
    // implementação...
}
```

Convenções: porta termina em `Porta`, adaptador em `Adaptador`, repositório
Spring Data em `RepositorioJpa` — **sem prefixo `I`** nas interfaces
(convenção do Java).

A **Aplicação** só declara e consome portas — nunca referencia um
adaptador concreto. Quem escolhe qual adaptador injetar é a API, uma vez só
(ver [princípio 7](#7-composition-root-único-na-api)) — é a aplicação
prática da inversão de dependência que sustenta a decisão de Clean
Architecture.

---

### 5. Casos de Uso São Classes Públicas na Aplicação

Cada caso de uso (ex.: `CriarConsumidorCaso`, `RegistrarCompraCaso`) é uma
classe pública isolada na camada de Aplicação, recebendo DTOs de entrada e
retornando `Resultado<T, E>`.

```java
// aplicacao/casosdeuso/cartao/RegistrarCompraCaso.java
public class RegistrarCompraCaso {            // não é final — ver nota abaixo
    private final CartaoPorta cartaoPorta;
    private final ExtratoPorta extratoPorta;

    public RegistrarCompraCaso(CartaoPorta cartaoPorta, ExtratoPorta extratoPorta) { /* ... */ }

    @Transactional
    public Resultado<DtoComprovanteCompra, ErroDominio> registrar(DtoCompra compra) { /* ... */ }
}
```

> **Por que não `final`:** o Spring cria um *proxy CGLIB* para aplicar
> `@Transactional`, e uma classe `final` não pode ser estendida. É a única
> classe de aplicação que foge do padrão `final` por padrão — documentado
> no README da camada.

**Um caso de uso, um arquivo — e nenhum caso de uso dentro de controller,
adaptador ou classe de configuração.** O controller traduz fronteira e
delega; o caso de uso decide; o adaptador executa. Quando um método de
controller cresce lógica de negócio, isso não é "controller gordo
aceitável" — é a responsabilidade no lugar errado (ver
[princípio 9](#9-anti-padrão-responsabilidade-misturada-na-composition-root)).

---

### 6. Testes Espelham a Estrutura de Produção

`src/test/java` espelha `src/main/java`, pacote a pacote, com nomes de
classe de teste terminados em `Teste`.

```text
src/test/java/…consumerpat/
  dominio/cartao/CartaoTeste.java
  aplicacao/casosdeuso/cartao/RegistrarCompraCasoTeste.java
  infraestrutura/persistencia/cartao/CartaoAdaptadorTeste.java
  api/cartao/CartaoControllerContratoTeste.java
```

Métodos de teste seguem `deve_X_quando_Y` (ver `TESTES.md`). Se a
estrutura de teste para de espelhar a de produção, o teste passa a apontar
para um lugar que ninguém olha ao mudar o código.

> **Convenção de descoberta**: o JUnit 5 sob Gradle descobre qualquer
> classe com `@Test`, independentemente do sufixo — `Teste` é convenção do
> projeto, não exigência da ferramenta.

---

### 7. Composition Root Único na API

A API (`api.configuracao`) é o único lugar onde dependências são
instanciadas e injetadas (Injeção de Dependência). Nenhuma camada abaixo
da API instancia serviços concretos diretamente.

```java
// api/configuracao/ConfiguracaoDePortas.java — composition root
@Configuration
public class ConfiguracaoDePortas {

    @Bean
    CartaoPorta cartaoPorta(CartaoRepositorioJpa repositorio) {
        return new CartaoAdaptador(repositorio);
    }
    // ...
}
```

Regras decorrentes, que diferem do uso comum de Spring:

- **Nenhum `@Component`, `@Service` ou `@Repository` em `dominio`,
  `aplicacao` ou `infraestrutura`.** Estereótipo de classe faz o Spring
  instanciá-la por varredura — o oposto de "só a composition root
  compõe". Bean nasce de método `@Bean` em `api.configuracao`.
- **Exceção:** interfaces Spring Data (`*RepositorioJpa`) são descobertas
  pelo framework e geradas em runtime — não há classe nossa para anotar.
- **Injeção por construtor**, nunca `@Autowired` em campo.

A classe `ConsumerTestApplication` **não tem regra de negócio** — só
inicializa a aplicação. Tipo de domínio ou caso de uso declarado dentro
dela ou de uma classe de configuração é anti-padrão (ver
[princípio 9](#9-anti-padrão-responsabilidade-misturada-na-composition-root)).

---

### 8. Nenhum Tipo Interno Cruza Fronteiras Sem Serialização

Tipos de domínio e entidades JPA nunca são retornados diretamente pela
API. A API sempre retorna `Mensagem*` serializadas, nunca objetos de
domínio crus.

```java
// CORRETO — retorna mensagem de fronteira
@GetMapping("/api/v1/consumidores")
public MensagemPagina<MensagemResumoConsumidor> listar(@RequestParam int pagina, @RequestParam int tamanhoPagina) { /* ... */ }

// ERRADO — retorna entidade (foi o que o legado fazia: List<Consumer> com saldos expostos)
@GetMapping("/consumer/consumerList")
public List<Consumer> listar() { /* ... */ }
```

O mesmo vale no sentido inverso: corpo de requisição vira `Mensagem*`
decodificada na borda, validada (`@Valid`), e só então vira entrada do
caso de uso — nunca `@Entity` ou objeto de domínio montado a partir do
JSON. Foi assim, no legado, que `updateConsumer` permitia sobrescrever o
saldo (ver `API.md`).

---

### 9. Anti-Padrão: Responsabilidade Misturada na Composition Root

**Nunca** defina tipos de domínio, casos de uso ou validadores dentro de
`ConsumerTestApplication`, de classes `@Configuration`, de adaptadores ou
de controllers.

```java
// ERRADO — tipo de domínio e caso de uso definidos na composição
// api/configuracao/ConfiguracaoDeCasosDeUso.java
@Configuration
public class ConfiguracaoDeCasosDeUso {
    public record FatorDeCobranca(BigDecimal valor) { }              // isso é domínio
    public static BigDecimal calcular(int tipo, BigDecimal valor) { /* ... */ } // isso é aplicação
}
```

```text
// CORRETO — cada responsabilidade em sua camada
// dominio/cartao/TipoCartao.java
// aplicacao/casosdeuso/cartao/RegistrarCompraCaso.java
// api/configuracao/ConfiguracaoDeCasosDeUso.java — apenas composição (instancia casos de uso)
```

O equivalente na API é o **controller gordo**: orquestrar chamada ao caso
de uso é papel do controller; decidir desconto de cartão, não — era
exatamente o que o `ConsumerController` legado fazia.

**Por quê:** um arquivo que mistura composição com regra é o único lugar
onde a ordem de instanciação passa a importar para o comportamento — é a
origem estrutural dos "bugs que só aparecem com o sistema inteiro
ligado".

---

### 10. Profundidade Hierárquica é a Solução, Não o Problema

A organização limpa é obrigatória, e a regra da homogeneidade semântica
(princípio 2) **não pode ser invocada para sobreviver ao acúmulo**:
sempre que um pacote plano parecer que está juntando arquivos demais, a
análise de subpacotes é obrigatória — e o resultado esperado, na maioria
dos casos, é criar.

Mais vale profundidade bem nomeada — mesmo muitos níveis, cada um com um
nome que se explica sozinho — do que cinquenta arquivos jogados na raiz
de um pacote, ou dois ou três subpacotes criados às pressas com arquivos
mal distribuídos entre eles. Profundidade mal feita se conserta
renomeando, unindo ou movendo arquivos; pacote plano gigante se conserta
só reorganizando a navegação inteira — e cada arquivo novo piora o custo.

O que é proibido é a **divisão arbitrária**, não a profundidade:

```text
// ERRADO — dividido por volume, sem mudança de motivo
aplicacao/casosdeuso/cartao1/
aplicacao/casosdeuso/cartao2/

// ERRADO — acumulado no pacote plano, mesmo semanticamente homogêneo (ver princípio 2)
aplicacao/casosdeuso/cartao/            # 30+ arquivos no mesmo nível

// CORRETO — dividido por motivo de mudança (se o pacote crescer)
aplicacao/casosdeuso/cartao/
├── compra/
├── credito/
└── consulta/
aplicacao/casosdeuso/consumidor/
```

Três erros que continuam proibidos: dividir por número (`cartao1/`,
`cartao2/` — vazio de significado), dividir antes de achar o eixo
(conceito, fluxo, tipo — os arquivos ficam espalhados e o motivo de
mudança volta a cruzar pastas), e criar subpacote cosmético de 1–2
arquivos só para parecer organizado.

---

### 11. Cada Tipo em Seu Lugar

Síntese de tudo acima — a referência rápida de revisão:

```text
1 Arquivo = 1 Responsabilidade = 1 Tipo Público

Entidade de domínio / agregado    -> dominio/{conceito}/
Regra de domínio pura             -> dominio/{conceito}/
Interface de porta                -> dominio/portas/
Hierarquia de erro                -> dominio/erros/
Resultado<T, E>                   -> dominio/resultado/
Tipo de valor / constante / enum  -> dominio/{conceito}/
Caso de uso                       -> aplicacao/casosdeuso/{conceito}/
DTO interno entre camadas         -> aplicacao/dtos/
Entidade JPA                      -> infraestrutura/persistencia/{agregado}/   (package-private)
Repositório Spring Data           -> infraestrutura/persistencia/{agregado}/   (package-private)
Mapeador entidade ⇄ domínio       -> infraestrutura/persistencia/{agregado}/
Implementação de porta            -> infraestrutura/persistencia/{agregado}/   (único tipo público)
Mensagem REST (fronteira)         -> api/{conceito}/   ou   api/comum/
Controller                        -> api/{conceito}/
Filtro / tradução de erro REST    -> api/comum/
Composition root                  -> api/configuracao/   (só composição)
Fake de porta (só teste)          -> src/test/.../aplicacao/fakes/
Teste unitário/integração         -> src/test/... espelhando a produção
Teste de arquitetura              -> src/test/.../arquitetura/
```

---

## Decisões de Estrutura

Este projeto é um módulo único de um desafio técnico. As escolhas abaixo são
**decisões**, não deriva — cada uma está registrada em
[`DECISOES.md`](DECISOES.md):

| Decisão | Escolha | Motivo |
|---|---|---|
| Unidade de camada | pacotes num módulo Gradle único | `./gradlew bootRun` simples para quem avalia; multi-módulo é o próximo passo se crescer |
| Imposição da regra | teste de arquitetura + visibilidade de pacote + revisão | o módulo único não bloqueia `import` |
| Validação técnica | Bean Validation nas `Mensagem*`, sem pasta `validadores/` | `spring-boot-starter-validation` já está no projeto; nível 10 de [`CONDICIONAIS.md`](../codigo/CONDICIONAIS.md) |
| Organização de `api` | por conceito (`consumidor/`, `cartao/`, `comum/`) | princípio 2: o que muda junto mora junto |
| Organização de `infraestrutura` | por agregado, com entidade e repositório package-private | princípio 2 + esconde a JPA do resto do código |
| Log | SLF4J/Logback, já presentes | proibido adicionar dependência no desafio |
| Banco | só H2 em memória | é o que o desafio usa; sem fila nem armazenamento externo |
| Composição | `ConsumerTestApplication` só inicia; composição em `api.configuracao` | Spring Boot não tem ponto de entrada com DI manual |

---

## Documentação

```text
docs/
├── PADRONIZACAO_INDEX.md
└── padronizacao/
    ├── fundamentos/      # DECISOES, ESTRUTURA (este documento)
    ├── codigo/           # VALORES, CONDICIONAIS, DOCUMENTACAO
    ├── qualidade/        # TESTES, ERROS, VALIDACAO, SEGURANCA, PERFORMANCE, BANCO, API, BUILD, CICD
    └── runtime/          # COMMITS, OBSERVABILIDADE, CONCORRENCIA
```

Cada documento vive exatamente em uma dessas pastas, sem duplicação de
conteúdo: quando dois documentos tocam o mesmo assunto, um detalha a regra e
o outro aponta para ele.

Fora de `docs/`, cada camada tem seu `README.md` na raiz do pacote,
**vivo junto com a camada** (nasce com ela, muda no mesmo PR que mudou o
contrato) — ver
[`codigo/DOCUMENTACAO.md`](../codigo/DOCUMENTACAO.md#readmes-por-camada).
Este documento faz o mesmo para a **árvore inteira**: mesma disciplina,
mesma consequência de ficar velho.

---

## Checklist de Estrutura

Antes de abrir um PR que cria ou move arquivo:

- [ ] O arquivo está no pacote da camada certa (tabela da Visão Geral)?
- [ ] A direção da dependência segue "só para dentro" (nenhum `import` novo contra a tabela)?
- [ ] O `RegraDeDependenciaTeste` continua passando?
- [ ] O `build.gradle` não ganhou dependência nova (regra do desafio) nem framework onde a camada não permite?
- [ ] Um arquivo novo declara um só tipo público, nomeado como o arquivo — e **nenhum tipo aninhado** que devia ser arquivo próprio?
- [ ] O `package` declarado espelha a pasta, em minúsculas e sem acento?
- [ ] O subpacote novo existe por motivo de mudança (fronteira semântica), não só por volume ou por número?
- [ ] Algum pacote plano acumulou arquivos? Se sim, a análise de subpacotes foi feita no PR — e manter plano tem motivo explícito?
- [ ] Nenhum tipo de domínio/caso de uso nasceu em classe de configuração, adaptador ou controller?
- [ ] Nenhum `@Component`/`@Service`/`@Repository` fora de `api.configuracao`?
- [ ] Entidade JPA, repositório e mapeador continuam package-private?
- [ ] Controller devolve `Mensagem*`, nunca domínio nem entidade?
- [ ] Fake/fixture ficou em `src/test`, nunca em `src/main`?
- [ ] **A árvore deste documento foi atualizada no mesmo PR**, se a estrutura mudou?
- [ ] Se a mudança é escolha arquitetural (não apenas arranjo), ela foi registrada em [`DECISOES.md`](DECISOES.md)?
- [ ] README da camada afetada descreve o estado atual (não o de quando nasceu)?

---

**[Índice →](../../PADRONIZACAO_INDEX.md)**
