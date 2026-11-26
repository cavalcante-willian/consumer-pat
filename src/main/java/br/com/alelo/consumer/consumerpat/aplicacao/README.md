# aplicacao

Camada de aplicação: os casos de uso, que orquestram o domínio e as portas.
**Depende só do domínio**, com uma exceção documentada: a anotação
`org.springframework.transaction.annotation.Transactional`, que marca a fronteira
da transação e não carrega implementação. Não conhece JPA, web, Lombok nem a
infraestrutura. Quem usa a aplicação (a API) depende dela, e nunca o contrário. A
regra é verificada por teste, não por convenção: `RegraDeDependenciaDaAplicacaoTeste`
lê os `import` do pacote e falha se aparecer qualquer coisa fora de `java.*`, do
domínio, da própria aplicação e dessa anotação — ver
`docs/padronizacao/fundamentos/ESTRUTURA.md`.

## Propósito

Responde a uma pergunta que o domínio não pode responder sozinho: **em que ordem as
regras rodam, e o que fica gravado junto**. O domínio sabe que um cartão debita e
credita, e que um tipo cobra o seu ajuste; é a aplicação que decide que uma compra
primeiro aplica o ajuste, depois busca o cartão travado, depois debita, e só então
grava o cartão e o extrato, tudo numa transação.

- **`casosdeuso.cartao`** — os dois casos de uso do cartão, **um subpacote por
  operação**: `compra` e `credito`. Como nos do consumidor, a operação **não mora na
  classe do caso de uso**: a classe (`RegistrarCompraCaso`, `CreditarCartaoCaso`) só
  guarda as dependências e declara o papel, e a operação, com o `@Transactional`, é um
  método `default` de uma interface em arquivo próprio (`RegistroDeCompra`,
  `CreditoDeCartao`), em cima de operações-base que expõem cada dependência
  (`buscador()`, `gravador()` e `relogio()`). A que os dois compartilham,
  `ComBuscadorDeCartaoParaAtualizacao`, mora na raiz.
  - `registrar` (`RegistroDeCompra`) aplica o ajuste do tipo ao valor informado, monta o
    extrato, busca o cartão para atualização, debita o valor **cobrado** (e não o
    informado: é ele que precisa caber no saldo, de modo que um desconto pode tornar
    possível uma compra maior que o saldo, e um acréscimo pode tornar impossível uma
    menor), grava e devolve o comprovante.
  - `creditar` (`CreditoDeCartao`) busca o cartão para atualização, credita o valor
    informado, sem ajuste (desconto e acréscimo valem para compras, e não para saldo),
    grava e devolve o comprovante.
  - Os dois **só orquestram**. Cada passo que não é orquestração é uma peça pública
    com arquivo próprio: `ExtratoDaCompra` (como o pedido vira extrato),
    `ComprovanteDaCompra` e `ComprovanteDoCredito` (como o resultado vira
    resposta) e `GravadorDaCompra` (o que uma compra grava: o cartão debitado e o
    extrato). As três primeiras são funções puras, e o instante da compra entra como
    argumento, lido do `Clock` pelo caso de uso.
  - Uma `Falha` devolvida não desfaz a transação, e não precisa: nada foi gravado antes
    dela. Só uma exceção de infraestrutura a desfaz.
- **Os eventos de negócio** nascem nos quatro casos que escrevem: `criar`, `atualizar`,
  `registrar` e `creditar`. Cada caso recebe a porta de log (`ComSaidaDeLogDoCaso`) e
  registra olhando o `Resultado` sem alterá-lo, com `aoSucesso` e `aoFalhar`, e cada
  evento é uma peça com uma razão só de mudar: `ConsumidorCriado`,
  `ConsumidorAtualizado`, `CompraRegistrada`, `CompraRecusada`, `CartaoCreditado` e
  `ConflitoDetectado`, que só registra o erro de categoria conflito e leva o código, e
  nunca o valor que colidiu. O cartão sai mascarado, porque o `Cartao` não conhece o
  consumidor e o número é o que há para rastrear. A recusa de crédito não tem evento, e
  a leitura e a listagem não emitem nenhum. **O log sai antes do commit**: se o commit
  falhar, o tratador registra o conflito de persistência, e as duas linhas têm o mesmo
  `idRequisicao`.
- **`casosdeuso.consumidor`** — os três casos de uso do consumidor, **um subpacote
  por operação**: `criar`, `atualizar` e `obter`. Aqui a operação **não mora na classe
  do caso de uso**: a classe (`CriarConsumidorCaso`, `AtualizarConsumidorCaso`,
  `ObterConsumidorCaso`) só guarda as portas e declara o papel, e a operação, com o
  `@Transactional`, é um método `default` de uma interface em arquivo próprio
  (`CadastroDeConsumidor`, `AtualizacaoDeCadastro`, `LeituraDeConsumidor`), escrito em
  cima de uma operação-base que expõe a porta (`ComGravadorDeNovoConsumidor`,
  `ComAtualizadorDeConsumidor`, `ComBuscadorDeConsumidor`), como `Somavel` sobre
  `ComQuantia` no `Dinheiro`. A de buscar fica na raiz do pacote, porque a atualização
  e a leitura a compartilham.
  - `criar` monta o consumidor com `NovoConsumidorDoPedido` e o entrega à porta, que
    grava o consumidor e os cartões numa só transação. Os cartões nascem de
    `CartoesIniciais`: um por tipo, com **saldo zero**, porque quem pede o cadastro
    não escolhe o saldo.
  - `atualizar` busca, confere a versão com `VersaoConfirmada`, troca os dados com
    `comDados` e grava. O pedido (`DtoAtualizacaoDeConsumidor`) não carrega cartão nem
    saldo, e uma compra feita entre a leitura e a atualização não é desfeita. Uma
    versão velha é recusada aqui, antes de gravar; dois clientes que leem a mesma
    versão ao mesmo tempo passam desta conferência, e o segundo perde na gravação,
    pela versão de linha do banco.
  - `obter` lê por id, numa transação só de leitura, e devolve a versão que a
    atualização exige de volta.
  - `saude` consulta o estado de cada dependência crítica, e **não tem transação**: a
    verificação não toca nenhuma tabela. Só entrega a consulta à porta, porque quem sabe a
    causa de uma dependência fora do ar é o adaptador, e é ele quem a registra.
  - `listar` entrega o pedido de página à porta, numa transação só de leitura, e devolve
    uma `Pagina` de `ResumoDeConsumidor` (só id e nome). Não devolve `Resultado`: um
    pedido já chega válido, e uma página além da última é uma página vazia.
  - Os três primeiros devolvem o próprio `Consumidor` do domínio, e a API o converte na sua
    mensagem: ele já é imutável, e um DTO de saída só repetiria os campos.
- **`dtos`** — a transferência entre a API e os casos de uso. Os de entrada,
  `DtoCompra` e `DtoCredito`, **já trazem os tipos do domínio** (`TipoCartao`,
  `NumeroDeCartao`, `Dinheiro`): a borda converte o que recebeu de fora com as
  leituras do domínio e só então monta o pedido, e uma conversão que falha é recusada
  lá. Os do consumidor seguem o mesmo critério: `DtoNovoConsumidor` (o cadastro e o
  número do cartão de cada tipo) e `DtoAtualizacaoDeConsumidor` (o id, a versão que o
  cliente leu e o cadastro). Os de saída, `DtoComprovanteCompra` e `DtoComprovanteCredito`, carregam
  `NumeroDeCartao`, cujo texto é mascarado, de modo que um comprovante nunca revela o
  cartão. Nunca são o contrato REST: a API tem os seus próprios tipos.

Uma falha é um dado, e não uma exceção: os casos de uso devolvem
`Resultado<DtoComprovante…, ErroDominio>`, e o primeiro erro que recusa o pedido
vira a falha. Uma `Falha` **não desfaz a transação, e não precisa**: nada é gravado
antes do último passo que pode falhar, o débito. Uma exceção de infraestrutura, como
um extrato que não grava, desfaz o débito junto.

## Estrutura

Convenção aplicada em toda a camada, a mesma do domínio: **uma peça por arquivo**, e
**nenhum método privado**. O que parece um detalhe de um caso de uso e é privado
não pode ser testado sozinho, e passa a ser uma peça pública que o caso de uso
chama. Um caso de uso é uma classe pública, **sem ser `final`**, porque o Spring cria
um proxy que a estende para aplicar `@Transactional`; as peças puras são `final`.

Saída real de `tree` (regenerar e colar aqui a cada PR que mudar a árvore — ver
`docs/padronizacao/codigo/DOCUMENTACAO.md`, "READMEs Por Camada"):

```text
aplicacao
├── casosdeuso
│   ├── ComSaidaDeLogDoCaso.java          # operação-base: a porta de log dos casos que emitem evento
│   ├── cartao
│   │   ├── ComBuscadorDeCartaoParaAtualizacao.java  # operação-base compartilhada: buscador()
│   │   ├── compra
│   │   │   ├── ComGravadorDaCompra.java  # operação-base: quem grava o que a compra deixa
│   │   │   ├── ComRelogioDaCompra.java   # operação-base: de onde vem o instante
│   │   │   ├── CompraRecusada.java       # compra.recusada: o cartão mascarado e o código do erro
│   │   │   ├── CompraRegistrada.java     # compra.registrada: o cartão mascarado e o valor cobrado
│   │   │   ├── ComprovanteDaCompra.java  # extrato + cartão debitado → comprovante da compra
│   │   │   ├── ExtratoDaCompra.java      # pedido + valor cobrado + instante → extrato
│   │   │   ├── GravadorDaCompra.java     # grava o cartão debitado e o extrato
│   │   │   ├── RegistrarCompraCaso.java  # só guarda as dependências e declara o papel
│   │   │   ├── RegistroDeCompra.java     # papel: a operação, com @Transactional
│   │   │   └── package-info.java
│   │   ├── credito
│   │   │   ├── CartaoCreditado.java      # cartao.creditado: o cartão mascarado e o valor
│   │   │   ├── ComGravadorDeCartao.java  # operação-base: quem grava o cartão
│   │   │   ├── ComprovanteDoCredito.java # pedido + cartão creditado → comprovante do crédito
│   │   │   ├── CreditarCartaoCaso.java   # só guarda as portas e declara o papel
│   │   │   ├── CreditoDeCartao.java      # papel: a operação, com @Transactional
│   │   │   └── package-info.java
│   │   └── package-info.java
│   ├── consumidor
│   │   ├── ComBuscadorDeConsumidor.java  # operação-base: quem busca por id (atualizar e obter)
│   │   ├── ConflitoDetectado.java    # conflito.detectado, em aviso, só se o erro é de conflito
│   │   ├── atualizar
│   │   │   ├── AtualizacaoDeCadastro.java    # papel: a operação, com @Transactional
│   │   │   ├── AtualizarConsumidorCaso.java  # só guarda as portas e declara o papel
│   │   │   ├── ComAtualizadorDeConsumidor.java   # operação-base: quem grava os dados
│   │   │   ├── ConsumidorAtualizado.java # consumidor.atualizado: o id e a versão como ficou
│   │   │   ├── VersaoConfirmada.java     # a versão do cliente é a atual, ou ErroVersaoDivergente
│   │   │   └── package-info.java
│   │   ├── criar
│   │   │   ├── CadastroDeConsumidor.java # papel: a operação, com @Transactional
│   │   │   ├── CartoesIniciais.java      # um cartão por tipo, com saldo zero
│   │   │   ├── ComGravadorDeNovoConsumidor.java  # operação-base: quem grava o consumidor novo
│   │   │   ├── ConsumidorCriado.java     # consumidor.criado: só o id
│   │   │   ├── CriarConsumidorCaso.java  # só guarda as portas e declara o papel
│   │   │   ├── NovoConsumidorDoPedido.java   # pedido → consumidor a gravar
│   │   │   └── package-info.java
│   │   ├── listar
│   │   │   ├── ComListadorDeConsumidores.java  # operação-base: quem lista
│   │   │   ├── ListagemDeConsumidores.java  # papel: a operação, @Transactional(readOnly = true)
│   │   │   ├── ListarConsumidoresCaso.java  # só guarda a porta e declara o papel
│   │   │   └── package-info.java
│   │   ├── obter
│   │   │   ├── LeituraDeConsumidor.java  # papel: a operação, @Transactional(readOnly = true)
│   │   │   ├── ObterConsumidorCaso.java  # só guarda a porta e declara o papel
│   │   │   └── package-info.java
│   │   └── package-info.java
│   └── saude
│       ├── ComConsultorDeSaude.java      # operação-base: quem diz a saúde
│       ├── ConsultaDeSaude.java          # papel: consultar(), sem transação
│       ├── ConsultarSaudeCaso.java       # só guarda a porta e declara o papel
│       └── package-info.java
├── dtos
│   ├── DtoAtualizacaoDeConsumidor.java   # o pedido de atualizar: id, versão lida e cadastro
│   ├── DtoCompra.java                    # o pedido de compra, com os tipos do domínio
│   ├── DtoComprovanteCompra.java         # o comprovante: os dois valores, o saldo e o instante
│   ├── DtoComprovanteCredito.java        # o comprovante do crédito: o valor e o saldo atual
│   ├── DtoCredito.java                   # o pedido de crédito, com os tipos do domínio
│   ├── DtoNovoConsumidor.java            # o pedido de cadastro: o cadastro e o número de cada cartão
│   └── package-info.java
└── package-info.java
```

## Exemplo de uso

```java
// a borda já converteu o que recebeu de fora; o caso de uso recebe tipos do domínio
var compra = new DtoCompra(TipoCartao.ALIMENTACAO, "Mercado", numero, "Arroz", cem);

Resultado<DtoComprovanteCompra, ErroDominio> resultado = caso.registrar(compra);

String resposta = resultado.resolver(
    comprovante -> "cobrado: " + comprovante.valorCobrado() + ", sobrou: " + comprovante.saldoRestante(),
    erro -> "recusado (" + erro.codigo() + "): " + erro.mensagem());
```

Num cartão de alimentação com saldo suficiente, os cem reais informados cobram 90
(10% de desconto). Num de combustível, a mesma compra cobra 135, e um saldo de 120 a
recusa com `ErroSaldoInsuficiente`, sem gravar nada.

## Dependências externas

Só uma: `org.springframework.transaction.annotation.Transactional`, do `spring-tx`,
que já vem com o starter de JPA que o projeto usa. Não entra dependência nova no
build, e o enunciado do desafio a proíbe. O resto é a biblioteca padrão
(`java.time.Clock`, `java.time.Instant`) e o domínio.

## Testes

Framework do `spring-boot-starter-test` (JUnit 5 e AssertJ), sem dependência nova.
Os testes **não** moram aqui: ficam em `src/test/java/.../aplicacao/`, espelhando
estes pacotes. São testes unitários puros, sem contexto Spring e sem banco: as portas
são **fakes em memória**, que obedecem ao contrato da porta real, e o relógio é
`Clock.fixed`. O que o Spring faz com `@Transactional` não é provado aqui: o teste só
confere que a anotação está no método, e o efeito real (a transação e a trava) é
provado com o banco, no item de persistência.

### Executar

```bash
./gradlew test --tests '*aplicacao*'
```

### Cobertura

Não há medição de cobertura por ferramenta: o plugin do Gradle baixaria um
artefato novo no build, e o enunciado proíbe dependência nova. A garantia é por
regra: cada peça tem o teste que falharia sem ela, e a tabela abaixo é o
inventário. Atualize-a a cada PR que mudar a árvore.

| Peça | Teste que a cobre |
|---|---|
| `RegistrarCompraCaso` | `RegistrarCompraCasoTeste` (ajuste dos três tipos, a fração de centavo, o valor cobrado que cabe ou não no saldo, o saldo exato, o que não é gravado numa recusa, valor não positivo, cartão ausente ou de outro tipo, a anotação de transação) |
| `CreditarCartaoCaso` | `CreditarCartaoCasoTeste` (crédito sem ajuste em qualquer tipo, valor não positivo, cartão ausente ou de outro tipo, a anotação de transação) |
| `RegistroDeCompra`, `CreditoDeCartao` | `TransacaoDaCompraTeste` e `TransacaoDoCreditoTeste`: num contexto Spring de verdade, o proxy enxerga o método `default` do papel, abre uma transação de **escrita** e a confirma, também quando o resultado é uma `Falha` |
| `ConsumidorCriado`, `ConsumidorAtualizado`, `CompraRegistrada`, `CompraRecusada`, `CartaoCreditado` | Um teste por peça, com o `SaidaDeLogFake`: o nível, o nome do evento, os campos, o cartão só mascarado, e o que **não** vai ao log (documento, nome, contato, saldo, mensagem do erro e texto do cliente) |
| `ConflitoDetectado` | `ConflitoDetectadoTeste`: registra em aviso o conflito de versão, de documento e de número, sem o valor, e **não** registra a ausência, a regra de negócio, a validação nem a indisponibilidade |
| Os eventos dos quatro casos | os testes dos casos (o evento certo, uma vez, e nenhum de sucesso numa recusa), mais `EventosDeNegocioDePontaAPontaTeste`: o contexto inteiro, com o Logback de verdade e o `idRequisicao` na mesma linha. A barreira de dígitos do padrão do log também mascararia um número de cartão que vazasse, e por isso quem protege cada peça são os testes unitários |
| `ExtratoDaCompra` | `ExtratoDaCompraTeste` (os dois valores distintos, o instante recebido, texto nulo) |
| `ComprovanteDaCompra` | `ComprovanteDaCompraTeste` (valores e instante do extrato, saldo do cartão, número mascarado) |
| `ComprovanteDoCredito` | `ComprovanteDoCreditoTeste` |
| `GravadorDaCompra` | `GravadorDaCompraTeste` (grava o cartão, registra o extrato, devolve o cartão gravado) |
| `CriarConsumidorCaso`, `CadastroDeConsumidor` | `CriarConsumidorCasoTeste` (cadastro sem cartões e com cartões de saldo zero, documento repetido e número repetido sem gravar, a anotação de transação) e `TransacaoDoCadastroTeste` |
| `CartoesIniciais`, `NovoConsumidorDoPedido` | `CartoesIniciaisTeste` e `NovoConsumidorDoPedidoTeste` (um cartão por tipo, saldo zero, ordem pelo tipo, nenhum cartão) |
| `AtualizarConsumidorCaso`, `AtualizacaoDeCadastro` | `AtualizarConsumidorCasoTeste` (troca só os dados, mantém cartões e saldo, versão velha e consumidor ausente sem gravar) e `TransacaoDaAtualizacaoTeste` |
| `VersaoConfirmada` | `VersaoConfirmadaTeste` (a versão atual passa, qualquer outra é recusada, inclusive a zero) |
| `ObterConsumidorCaso`, `LeituraDeConsumidor` | `ObterConsumidorCasoTeste` (devolve com cartões e versão, ausente, não grava, só de leitura) e `TransacaoDaLeituraTeste` |
| `ListarConsumidoresCaso`, `ListagemDeConsumidores` | `ListarConsumidoresCasoTeste` (ordena e desempata por id, inverte, corta sem pular nem repetir, página além da última, só de leitura) e `TransacaoDaListagemTeste` |
| Regra de dependência da aplicação | `RegraDeDependenciaDaAplicacaoTeste`, na raiz de `src/test/.../aplicacao` |

Peças sem teste próprio, cobertas junto de quem as torna observáveis:

| Peça sem teste próprio | Coberta junto de | Por quê |
|---|---|---|
| `DtoCompra`, `DtoCredito`, `DtoComprovanteCompra`, `DtoComprovanteCredito`, `DtoNovoConsumidor`, `DtoAtualizacaoDeConsumidor` | os testes dos casos de uso e das peças de comprovante | são só dados, sem operação; o único caminho até eles é o uso |
| `ComGravadorDeNovoConsumidor`, `ComAtualizadorDeConsumidor`, `ComBuscadorDeConsumidor` | os testes dos casos de uso | são só a operação-base de um papel, e o único caminho até elas é a operação |

Os fakes e a fábrica de cartões também seguem a regra de uma peça por arquivo, e têm
os seus testes (`CartaoPortaFakeTeste`, `ExtratoPortaFakeTeste`,
`ModeloDeCartaoTeste`): um fake que aceita o que a porta real recusa deixaria os
testes dos casos de uso verdes por um motivo falso. O fake do consumidor mora em
`fakes/consumidor`, já dividido por papel, e o `ConsumidorPortaFakeTeste` o confere
contra o contrato da porta (consumidor ausente, documento e número repetidos, versão
velha e o saldo que não muda na atualização). A conferência de versão do fake é própria
dele, e não a do caso de uso: ele faz o papel da versão de linha do banco, e um teste só
prova alguma coisa se as duas forem independentes.

**A transação num método `default` é provada em contexto Spring**, e não só por
reflexão: cada operação tem um teste (`TransacaoDoCadastroTeste`,
`TransacaoDaAtualizacaoTeste` e `TransacaoDaLeituraTeste`) que sobe um contexto com
`@EnableTransactionManagement` e um `GerenciadorDeTransacaoFake`, chama o bean e confere
que a transação foi pedida, e só de leitura onde deve. O proxy é **por classe**, como o
Spring Boot configura por padrão: com o padrão do Spring puro (proxy de interface), a
classe, que agora implementa interfaces, não seria injetável pelo próprio tipo.

## Evolução

| PR | Mudança no contrato desta camada |
|---|---|
| `#13` | Cria a camada: `RegistrarCompraCaso` e `CreditarCartaoCaso`, as peças `ExtratoDaCompra`, `ComprovanteDaCompra`, `ComprovanteDoCredito` e `GravadorDaCompra`, e os DTOs `DtoCompra`, `DtoCredito`, `DtoComprovanteCompra` e `DtoComprovanteCredito`. O crédito passa a identificar o cartão por número **e tipo**, e a compra passa a devolver comprovante (o legado devolvia vazio). Nenhum código do legado depende ainda dela. |
| `#17` | Acrescenta `casosdeuso.consumidor`, em um subpacote por operação (`criar`, `atualizar` e `obter`): os casos `CriarConsumidorCaso`, `AtualizarConsumidorCaso` e `ObterConsumidorCaso`, cuja operação mora em um papel de método `default` (`CadastroDeConsumidor`, `AtualizacaoDeCadastro` e `LeituraDeConsumidor`) sobre uma operação-base, as peças `CartoesIniciais`, `NovoConsumidorDoPedido` e `VersaoConfirmada`, e os DTOs `DtoNovoConsumidor` e `DtoAtualizacaoDeConsumidor`. Os casos de uso do cartão continuam com a operação na classe. Nenhum código do legado depende ainda dos novos. |
| `#20` | Acrescenta `casosdeuso.consumidor.listar`: `ListarConsumidoresCaso`, que entrega o pedido de página à porta, numa transação só de leitura (papel `ListagemDeConsumidores`, sobre a operação-base `ComListadorDeConsumidores`), e devolve uma `Pagina` de `ResumoDeConsumidor`, só com id e nome. Não devolve `Resultado`: um pedido já chega válido, e uma página além da última é uma página vazia. |
| `#23` | Acrescenta `casosdeuso.saude`: `ConsultarSaudeCaso`, que entrega a consulta à porta de saúde (papel `ConsultaDeSaude`, sobre a operação-base `ComConsultorDeSaude`) e devolve o `EstadoDeSaude`, com a situação geral derivada dele. Sem transação, porque a verificação não toca nenhuma tabela, e sem `Resultado`: uma dependência fora do ar é o próprio dado que a consulta devolve, e não uma falha dela. |
| `#24` | Converge os casos de uso do cartão ao desenho dos do consumidor, sem mudar comportamento: a operação sai da classe para um papel de método `default` (`RegistroDeCompra` e `CreditoDeCartao`), `casosdeuso.cartao` passa a ter um subpacote por operação (`compra` e `credito`), e o construtor de `CreditarCartaoCaso` passa a receber o buscador e o gravador separados. |
| `#25` | Os casos de uso que escrevem passam a registrar o que fizeram, pela porta de log: `consumidor.criado`, `consumidor.atualizado`, `compra.registrada`, `compra.recusada`, `cartao.creditado` e `conflito.detectado`, uma peça por evento. Os quatro construtores ganham `SaidaDeLogPorta`, a leitura e a listagem não mudam, e o `Resultado` ganha `aoSucesso` e `aoFalhar`. |
