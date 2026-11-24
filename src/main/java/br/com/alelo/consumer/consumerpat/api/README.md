# api

Camada de API: a fronteira com o mundo e a **composition root**. É o único lugar que
conhece todas as camadas e que decide qual implementação entra em cada porta. A
infraestrutura só é nomeada em `api.configuracao`.

A camada serve, por enquanto, duas operações novas: `POST /api/v1/compras` e
`POST /api/v1/creditos`. O legado segue servindo as demais rotas até o item que o
remove; as duas convivem sem se tocar.

## Propósito

- **`cartao`** — um subpacote por operação, com tudo o que muda junto: a mensagem de
  entrada (com Bean Validation), a de saída, a conversão entre elas e o pedido do caso
  de uso, e o controller. A entrada converte nos tipos do domínio com as leituras que
  ele tem (`TipoCartaoDoEstabelecimento`, `NumeroDeCartaoDeTexto`), e uma conversão que
  falha vira o erro de validação do próprio domínio. A saída traz o número mascarado e o
  dinheiro em texto, com a escala toda. O sucesso é `201`, sem `Location`, porque não
  há recurso endereçável. As operações são `POST`, nunca `GET`, porque o corpo leva
  dado sensível.
- **`comum`** — o que vale para todo endpoint, dividido pelo que muda junto.
  - `problema`: o corpo de erro, no formato RFC 7807 (`application/problem+json`),
    com `type` igual a `urn:consumer-pat:erro:` mais o código. Um erro de domínio
    traz código e status da própria variante e da categoria; um problema de borda traz
    os seus do enum `ProblemaDeBorda`.
  - `resposta`: a resposta HTTP. `RespostaDeResultado` é o **único** lugar em que um
    resultado vira status: o sucesso leva o que o endpoint declarou, e a falha, o da
    categoria do erro.
  - `campos`: a tradução do que o framework diz de uma entrada ruim em campos com erro.
    O texto das bibliotecas cita classes do projeto e o valor rejeitado, e nada disso
    vai ao cliente. O registro causa → tradutor é um `Map` (`TradutoresDoCorpo`) e a
    consulta sobe a hierarquia de classes, então a ordem não importa.
  - `tratamento`: `FiltroDeProblemaDeApi`, o `@RestControllerAdvice`, que só declara os
    papéis `TraducaoDe*`. Cada papel tem o seu `@ExceptionHandler` num método
    `default`, e o único que captura `Exception` é o da falha inesperada.
- **`configuracao`** — liga o que as camadas de baixo declaram.
  - `ConfiguracaoDeRelogio` escolhe o **único** `Clock` da aplicação: o de parede, em
    UTC. O domínio, os casos de uso e a auditoria do banco recebem este mesmo bean, de
    modo que um teste que o troque controla o tempo de ponta a ponta. Nenhum código
    chama `Instant.now()` por conta própria.
  - `ConfiguracaoDeAuditoria` liga a auditoria automática do JPA, com o instante do
    `Clock`. As colunas `data_criacao` e `data_atualizacao` são preenchidas pelo
    mecanismo, e nenhum caso de uso as atribui.
  - `ConfiguracaoDePortas` escolhe a implementação de cada porta do domínio. Importa a
    **classe** de cada adaptador, e não instancia nenhum, porque os repositórios que
    os adaptadores recebem são pacote-privados e esta camada não consegue nem escrever
    o tipo deles.
  - `ConfiguracaoDoCasoDeRegistrarCompra` e `ConfiguracaoDoCasoDeCreditarCartao`
    expõem cada caso de uso como bean, um por arquivo. É isso que ativa o
    `@Transactional` dos casos de uso, e é aqui que a camada de aplicação, que não
    conhece o Spring, ganha o seu ciclo de vida.

## Estrutura

Saída real de `tree` (regenerar e colar aqui a cada PR que mudar a árvore — ver
`docs/padronizacao/codigo/DOCUMENTACAO.md`, "READMEs Por Camada"):

```text
api
├── cartao
│   ├── compra
│   │   ├── CompraController.java                 # POST /api/v1/compras
│   │   ├── CompraDaMensagem.java                 # mensagem → pedido do caso de uso
│   │   ├── ComprovanteDaCompraEmMensagem.java    # comprovante → mensagem
│   │   ├── MensagemComprovanteCompra.java        # saída
│   │   ├── MensagemRegistrarCompra.java          # entrada
│   │   └── package-info.java
│   ├── credito
│   │   ├── ComprovanteDoCreditoEmMensagem.java
│   │   ├── CreditoController.java                # POST /api/v1/creditos
│   │   ├── CreditoDaMensagem.java
│   │   ├── MensagemComprovanteCredito.java
│   │   ├── MensagemCreditarCartao.java
│   │   └── package-info.java
│   └── package-info.java
├── comum
│   ├── campos
│   │   ├── ErrosDeCampoDeValidacao.java          # Bean Validation → campos
│   │   ├── ErrosDeCampoDoCorpo.java              # corpo ilegível → campos
│   │   ├── NomeDoCampoDoJackson.java             # caminho do campo, sem nome de classe
│   │   ├── package-info.java
│   │   ├── TradutorDeCausa.java
│   │   └── TradutoresDoCorpo.java                # registro causa → tradutor
│   ├── package-info.java
│   ├── problema
│   │   ├── MensagemProblema.java                 # o corpo RFC 7807
│   │   ├── package-info.java
│   │   ├── ProblemaDeBordaEmMensagem.java
│   │   ├── ProblemaDeBorda.java                  # enum: código, título e status
│   │   ├── ProblemaDeErroDeDominio.java
│   │   └── TituloDaCategoria.java
│   ├── resposta
│   │   ├── package-info.java
│   │   ├── RespostaDeProblema.java
│   │   └── RespostaDeResultado.java              # único ponto que decide o status
│   └── tratamento
│       ├── FiltroDeProblemaDeApi.java            # @RestControllerAdvice
│       ├── package-info.java
│       ├── TraducaoDeConflitoDeVersao.java       # 409
│       ├── TraducaoDeCorpoIlegivel.java          # 400
│       ├── TraducaoDeEntradaInvalida.java        # 400
│       ├── TraducaoDeFalhaInesperada.java        # 500
│       ├── TraducaoDeMetodoNaoSuportado.java     # 405, com Allow
│       ├── TraducaoDeMidiaNaoSuportada.java      # 415
│       ├── TraducaoDeRotaInexistente.java        # 404
│       └── TraducaoDeTravaEsgotada.java          # 409
├── configuracao
│   ├── ConfiguracaoDeAuditoria.java              # @EnableJpaAuditing, com o instante do Clock
│   ├── ConfiguracaoDePortas.java                 # importa os adaptadores, um por porta
│   ├── ConfiguracaoDeRelogio.java                # o único Clock: de parede, em UTC
│   ├── ConfiguracaoDoCasoDeCreditarCartao.java
│   ├── ConfiguracaoDoCasoDeRegistrarCompra.java
│   └── package-info.java
└── package-info.java
```

## Dependências externas

Spring MVC (controllers e tratador), Bean Validation (já no build), Jackson (leitura
do corpo e as exceções que a tradução reconhece), Spring Data (a auditoria). Nenhuma
dependência nova no build.

A configuração de `application.yml` que esta camada pressupõe: `open-in-view`
desligado; Jackson estrito (`fail-on-unknown-properties`), em UTC e sem notação
científica; `throw-exception-if-no-handler-found` e `add-mappings: false`, para a
rota inexistente cair no tratador; e `server.error.*` sem mensagem, pilha nem
detalhe de binding.

## Testes

Os testes ficam em `src/test/java/.../api/` e espelham os pacotes.

| Teste | O que prova |
|---|---|
| `comum.problema.*Teste` | títulos de categoria distintos; códigos de borda únicos e no formato; corpo montado do enum e do erro de domínio, sem número inteiro |
| `comum.resposta.*Teste` | status e tipo de mídia, no sucesso e em cada categoria de falha; a conversão da mensagem não roda numa falha |
| `comum.campos.*Teste` | tradução com exceções **reais** do Jackson; subclasse cai no tradutor mais geral; corpo ausente de verdade; nada de classe ou valor no texto |
| `comum.tratamento.FiltroDeProblemaDeApiTeste` | 409 de trava e de versão; 500 sem a causa |
| `cartao.*.*DaMensagemTeste` | conversão em tipos do domínio e primeira falha |
| `cartao.*.Comprovante*Teste` | número mascarado, escala completa, sem notação científica |
| `cartao.*.*ControllerTeste` | `@WebMvcTest` sobre fakes e relógio fixo: 201, 400 (todos os campos, sem valor rejeitado), 404, 405 com `Allow`, 415, 422 |

`ConfiguracaoDeCasosDeUsoComFakes` monta os casos de uso sobre os fakes das portas.
`ConfiguracaoDePortasTeste` sobe o contexto e prova que cada porta do domínio tem o
seu adaptador. Os testes de persistência importam `ConfiguracaoDeAuditoria`
explicitamente, porque `@DataJpaTest` não carrega as configurações da API.

`MARCA_DO_CORPO_AUSENTE`, em `ErrosDeCampoDoCorpo`, é o único ponto que depende de
texto em inglês do Spring (a mensagem de corpo ausente não tem tipo próprio). O teste
do controller que posta sem corpo faz o build quebrar se o Spring mudar o texto.

## Evolução

| PR | Mudança no contrato desta camada |
|---|---|
| `#14` | Cria a camada, só com a composition root: o relógio, a auditoria e as portas do cartão e do extrato. |
| `#15` | Cria `POST /api/v1/compras` e `POST /api/v1/creditos`, o corpo de erro RFC 7807 e o tratador de exceções. O Jackson passa a ser estrito para toda a aplicação, inclusive o legado. |
