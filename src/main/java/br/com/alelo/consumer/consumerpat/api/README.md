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
    `default`, e o único que captura `Exception` é o da falha inesperada. Os quatro
    papéis de falha de infraestrutura registram o que traduzem, pela porta de log que o
    tratador guarda (`ComSaidaDeLog`): `falha.inesperada` (a pilha, **sem a mensagem**
    da exceção, em `FalhaSemMensagem`), `conflito.detectado` (o nome da restrição, em
    `NomeDaRestricao`, ou a entidade, e nunca o valor) e `lock.timeout`.
  - `requisicao`: o que acontece antes e depois do controller. `FiltroDeCorrelacao` gera
    ou aceita o `idRequisicao` (só se for um UUID: texto livre do cliente num log é
    injeção de log), o põe no `MDC`, o devolve em `X-Request-Id` **em toda resposta** e o
    remove no `finally`. `FiltroDeLogDeConclusao`, logo dentro dele, registra
    `requisicao.concluida` com método, caminho (sem a query), status e duração, e por
    isso a linha ainda leva o id. O nível vem do status (`NivelDaConclusao`): 5xx é erro,
    409 é aviso, e o resto, inclusive 404 e 422, é informação. O corpo de erro leva o mesmo
    `idRequisicao`, lido por `IdDaRequisicaoAtual`. **É o único pacote fora de
    `infraestrutura.log` que importa o SLF4J, e só o `MDC`.**
  - `seguranca`: os cabeçalhos de segurança de toda resposta de `/api/*`.
    `CabecalhosDeSeguranca` guarda os cinco valores num `Map` imutável
    (`X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, a política de conteúdo que
    não permite nenhum recurso nem moldura, `Referrer-Policy: no-referrer` e
    `Cache-Control: no-store`), e `FiltroDeCabecalhosDeSeguranca`, o filtro mais externo,
    só os aplica, antes de a requisição seguir. Por isso cobrem também a resposta de erro,
    a de uma rota inexistente e a de um método não aceito, que nunca chegam a um controller:
    é a razão de o `no-store` estar no filtro, e não num interceptor. **Não valem para o
    Swagger, a especificação e o console do H2**, que a política de conteúdo e o `DENY`
    quebrariam, nem para as rotas do legado, fora de `/api/*`. O HSTS não é enviado: o TLS é
    terminado fora da aplicação. `FiltroDeRejeicaoDeTrace` recusa o `TRACE` com `405` e sem
    corpo, em qualquer rota e também no despacho de erro: o Spring MVC responde um `TRACE`
    ecoando a requisição inteira, com os cabeçalhos que o cliente mandou, e nenhuma
    propriedade o evita. Vem depois do log de conclusão, para o `TRACE` recusado sair no log.
- **`consumidor`** — `POST /api/v1/consumidores` (cadastra com os cartões, `201` com
  `Location`), `PUT /api/v1/consumidores/{id}` (só o cadastro, com a `versao` que o
  cliente leu) e `GET /api/v1/consumidores/{id}`, um pacote por operação. **Todos os
  erros de conversão saem de uma vez**: os campos são independentes, e o cliente
  corrige o cadastro numa tentativa. `ValidacoesReunidas`, do domínio, reúne as
  falhas, e cada uma sai sob o nome do campo da entrada (os cartões, por exemplo, sob
  `numerosCartoes`). O que sai é mascarado: documento, celular e números de cartão. Os
  cartões entram pelo nome do tipo e nascem com saldo zero, e nenhum corpo aceita saldo.
- **`saude`** — `GET /api/v1/saude`, o health check, com um status por dependência:
  `200` com `SAUDAVEL` se todas respondem e `503` com `DEGRADADO` se uma não responde. É o
  **único endpoint que responde 503**, e é público e sem autenticação, então o corpo é só
  a situação e o estado de cada dependência (`{"estado":"SAUDAVEL","dependencias":
  {"banco":"DISPONIVEL"}}`): nenhuma URL, host, versão nem mensagem de exceção. A resposta
  leva `Cache-Control: no-store`, porque um health velho diz o que não é mais verdade, e
  quem o escreve é o filtro dos cabeçalhos de segurança, como em toda resposta da API.
  O status vem de `StatusDaSaude`, um `EnumMap`: o domínio não conhece HTTP.
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
├── consumidor                                # compartilhado entre as três operações
│   ├── CamposDosCartoes.java                 # lê o tipo e o número de cada cartão do cadastro
│   ├── CamposDosDados.java                   # lê documento, telefones, e-mail e CEP
│   ├── CartaoEmMensagem.java                 # cartão → mensagem, número mascarado
│   ├── CartoesConfirmados.java               # monta o mapa de cartões já conferido
│   ├── ConsumidorEmMensagem.java             # consumidor → mensagem, dados mascarados
│   ├── DadosConfirmados.java                 # monta os dados já conferidos
│   ├── DadosDaMensagem.java                  # dados → domínio, com todos os erros
│   ├── MensagemCartao.java                   # saída do cartão
│   ├── MensagemConsumidor.java               # saída: id, versão, dados mascarados, cartões
│   ├── MensagemDadosConsumidor.java          # entrada dos dados cadastrais
│   ├── ValorConfirmado.java                  # o valor de uma leitura já conferida
│   ├── atualizar                             # PUT /api/v1/consumidores/{id}
│   │   ├── AtualizacaoDaMensagem.java
│   │   ├── AtualizarConsumidorController.java
│   │   ├── MensagemAtualizarConsumidor.java  # versão + dados
│   │   └── package-info.java
│   ├── criar                                 # POST /api/v1/consumidores
│   │   ├── CriarConsumidorController.java
│   │   ├── LocalizacaoDoConsumidor.java      # o Location do 201
│   │   ├── MensagemCriarConsumidor.java      # dados + número de cada cartão
│   │   ├── NovoConsumidorDaMensagem.java
│   │   └── package-info.java
│   ├── listar                                # GET /api/v1/consumidores (paginado)
│   │   ├── ListarConsumidoresController.java
│   │   ├── MensagemResumoConsumidor.java     # id e nome, e mais nada
│   │   ├── ResumoEmMensagem.java
│   │   └── package-info.java
│   ├── obter                                 # GET /api/v1/consumidores/{id}
│   │   ├── ObterConsumidorController.java
│   │   └── package-info.java
│   └── package-info.java
├── saude
│   ├── MensagemSaude.java                    # a situação e o estado de cada dependência
│   ├── SaudeController.java                  # GET /api/v1/saude: 200 ou 503, sem cache
│   ├── SaudeEmMensagem.java
│   ├── StatusDaSaude.java                    # situação → status, por EnumMap
│   └── package-info.java
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
│   ├── requisicao
│   │   ├── FiltroDeCorrelacao.java               # id no MDC e em X-Request-Id, etapa 2
│   │   ├── FiltroDeLogDeConclusao.java           # requisicao.concluida, etapa 3
│   │   ├── IdDaRequisicaoAtual.java              # lê o id do MDC, para o corpo de erro
│   │   ├── IdDeRequisicao.java                   # aceita só UUID; senão gera
│   │   ├── NivelDaConclusao.java                 # 5xx erro, 409 aviso, o resto informação
│   │   └── package-info.java
│   ├── resposta
│   │   ├── package-info.java
│   │   ├── RespostaDeProblema.java
│   │   └── RespostaDeResultado.java              # único ponto que decide o status
│   ├── seguranca
│   │   ├── CabecalhosDeSeguranca.java            # os cinco cabeçalhos, num Map imutável
│   │   ├── FiltroDeCabecalhosDeSeguranca.java    # filtro mais externo, só /api/*: aplica o Map
│   │   ├── FiltroDeRejeicaoDeTrace.java          # recusa o TRACE com 405 e sem corpo, inclusive no despacho de erro
│   │   └── package-info.java
│   └── tratamento
│       ├── ComSaidaDeLog.java                    # operação-base: a porta de log do tratador
│       ├── FalhaSemMensagem.java                 # a pilha sem a mensagem da exceção
│       ├── FiltroDeProblemaDeApi.java            # @RestControllerAdvice
│       ├── NomeDaRestricao.java                  # a restrição violada, nunca o valor
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
│   ├── ConfiguracaoDeFiltros.java                # registra os filtros na ordem do contrato
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
| `consumidor.*.*ControllerTeste` | `@WebMvcTest` sobre fakes: `201` com `Location` que aponta o id certo, documento e número nunca inteiros, `400` com todos os campos inválidos de uma vez (documento, CEP, tipo e número do cartão), saldo recusado no cadastro e na atualização, `409` de documento e de versão, `404`, id que não é positivo ou número, e `405` |
| `consumidor.criar.LocalizacaoDoConsumidorTeste`, `comum.resposta.RespostaDeResultadoCriadoTeste` | o `Location` relativo, sem host; o `201` só no sucesso e a localização não calculada numa falha |
| `comum.campos.*Teste` | tradução com exceções **reais** do Jackson; subclasse cai no tradutor mais geral; corpo ausente de verdade; nada de classe ou valor no texto |
| `comum.tratamento.FiltroDeProblemaDeApiTeste` | 409 de trava e de versão; 500 sem a causa; o log de cada falha (`lock.timeout`, `conflito.detectado`, `falha.inesperada`) sem a mensagem da exceção nem o valor |
| `comum.tratamento.FalhaSemMensagemTeste`, `NomeDaRestricaoTeste` | a pilha e a cadeia de causas sem nenhuma mensagem; o nome da restrição contra o **H2 de verdade**, sem o valor que colidiu |
| `comum.requisicao.IdDeRequisicaoTeste`, `FiltroDeCorrelacaoTeste` | só UUID é aceito (quebra de linha, tamanho e caracteres fora do formato são regerados); o id volta em toda resposta, está no `MDC` durante a requisição e é removido no `finally`, inclusive se ela lançar |
| `comum.requisicao.NivelDaConclusaoTeste`, `FiltroDeLogDeConclusaoTeste` | 5xx erro, 409 aviso, o resto informação; método, caminho sem query, status e duração medida; o evento sai mesmo se a requisição lançar |
| `saude.SaudeControllerTeste`, `SaudeEmMensagemTeste`, `StatusDaSaudeTeste`, `SaudeDePontaAPontaTeste` | `200` com o banco disponível e `503` com ele indisponível (`SaudePorta` **fake**), corpo só com a situação e as dependências, nada interno, `Cache-Control: no-store`, e o contexto inteiro contra o H2 real |
| `comum.seguranca.CabecalhosDeSegurancaTeste`, `FiltroDeCabecalhosDeSegurancaTeste` | os cinco valores e o que não está neles (HSTS); o filtro os escreve antes de a requisição seguir, mantém quando ela lança, substitui um `Cache-Control` que já existia, e não escreve nenhum cabeçalho fora do contrato |
| `comum.seguranca.FiltroDeRejeicaoDeTraceTeste` | o filtro age só no `TRACE`, sem distinguir maiúsculas, vale também para o despacho de erro, responde `405` sem corpo e **não deixa a requisição seguir**, nem com cookie no pedido. O contrato inteiro, com o servidor de verdade, é provado pela bateria de segurança (`SegurancaDoSistemaNovoTeste`) |
| `comum.seguranca.CabecalhosDeSegurancaDePontaAPontaTeste` | o contrato com o contexto inteiro: os cabeçalhos em `200`, `201`, `400`, `404`, `405`, `409`, `415` e `422`, com o `X-Request-Id`; a especificação, o Swagger e o console do H2 sem eles; nenhum cabeçalho de CORS, nem no pré-voo |
| `comum.seguranca.SegurancaDoPerfilDeProducaoTeste`, `configuracao.ConfiguracaoDeFiltrosTeste` | o perfil `prod` sem o console do H2, o Swagger e a especificação, e com os cabeçalhos; o filtro de segurança como o mais externo e só em `/api/*` (a mutação que tira o `/api/*` derruba quatro testes) |
| `comum.requisicao.PipelineDeRequisicaoTeste` | o contexto inteiro: o id em todo cabeçalho e em todo corpo de erro, e **na linha de conclusão do log** (a mutação que inverte a ordem dos filtros a derruba) |
| `cartao.*.*DaMensagemTeste` | conversão em tipos do domínio e primeira falha |
| `cartao.*.Comprovante*Teste` | número mascarado, escala completa, sem notação científica |
| `cartao.*.*ControllerTeste` | `@WebMvcTest` sobre fakes e relógio fixo: 201, 400 (todos os campos, sem valor rejeitado), 404, 405 com `Allow`, 415, 422 |
| `cartao.compra.CompraDePontaAPontaTeste` | o contexto inteiro e o H2 real, com um consumidor de três cartões e 200,00 em cada: a compra de 100,00 deixa o alimentação em 110 (extrato 90), o combustível em 65 (extrato 135) e a farmácia em 100 (extrato 100), **e os outros dois cartões em 200**. É o que o legado provava, passando pelo banco, e que no novo estava provado só em pedaços. Provado por mutação: trocar o acréscimo do combustível derruba só o caso dele, e gravar o valor informado no extrato derruba o alimentação e o combustível |
| `cartao.concorrencia.*Teste` | o contexto inteiro e o H2 real, com vinte requisições na mesma largada: compras (10 `201`, 10 `422`, saldo zero), créditos (nenhum se perde), compra e crédito juntos (saldo intacto) e cadastros do mesmo documento (um `201`, dezenove `409`). A mutação que tira o `@Lock` derruba os três de saldo, com `409` do `@Version`; a que tira a restrição `unique` derruba o de cadastro |

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
| `#19` | Cria `POST`, `PUT` e `GET` de `/api/v1/consumidores`, com `201` e `Location`, a versão no corpo e os erros de conversão reunidos. O tratador ganha dois papéis: a restrição do banco violada (`409`) e o parâmetro de tipo errado (`400`). Os três casos de uso do consumidor viram beans. |
| `#22` | Acrescenta `comum.requisicao` e `ConfiguracaoDeFiltros`: a correlação (`X-Request-Id` em toda resposta, `idRequisicao` no corpo de erro) e o log de conclusão. Os papéis de falha de infraestrutura do tratador passam a registrar `falha.inesperada`, `conflito.detectado` e `lock.timeout`. `MensagemProblema` ganha o campo `idRequisicao`. |
| `#23` | Acrescenta `GET /api/v1/saude`, o health check por dependência, com `200` e `503`. `CategoriaErro` ganha `INDISPONIVEL` (503), e o título dela entra em `TituloDaCategoria`. |
| `#26` | Acrescenta `comum.seguranca`: o `FiltroDeCabecalhosDeSeguranca`, o filtro mais externo, só em `/api/*`, que escreve os cinco cabeçalhos de segurança em toda resposta, inclusive as de erro. O `Cache-Control: no-store` passa a vir dele, e o `SaudeController` deixa de escrevê-lo. O perfil `prod` desliga o console do H2, o Swagger e a especificação. |
| `#29` | Acrescenta o `FiltroDeRejeicaoDeTrace` a `comum.seguranca`: o `TRACE` passa a responder `405` sem corpo, em vez de ecoar a requisição inteira. O defeito estava no sistema novo, e foi a bateria de segurança que o achou. |
| `#30` | Os eventos que o tratador de erros registra (`conflito.detectado`, `lock.timeout` e `falha.inesperada`) passam a gravar `metodo` e `caminho`, os mesmos campos do `requisicao.concluida`, no lugar de `operacao`, que ficava com o texto `POST /api/v1/consumidores` enquanto os casos de uso gravavam o nome lógico (`criar-consumidor`). `operacao` passa a ser só o nome da operação de negócio. `OperacaoDaRequisicao` sai, porque só o tratador a usava. |
