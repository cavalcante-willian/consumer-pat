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

- **`casosdeuso.cartao`** — os dois casos de uso do cartão, e as peças de que cada
  um é feito.
  - `RegistrarCompraCaso` aplica o ajuste do tipo ao valor informado, monta o
    extrato, busca o cartão para atualização, debita o valor **cobrado** (e não o
    informado: é ele que precisa caber no saldo, de modo que um desconto pode tornar
    possível uma compra maior que o saldo, e um acréscimo pode tornar impossível uma
    menor), grava e devolve o comprovante.
  - `CreditarCartaoCaso` busca o cartão para atualização, credita o valor
    informado, sem ajuste (desconto e acréscimo valem para compras, e não para saldo),
    grava e devolve o comprovante.
  - Os dois **só orquestram**. Cada passo que não é orquestração é uma peça pública
    com arquivo próprio: `ExtratoDaCompra` (como o pedido vira extrato),
    `ComprovanteDaCompra` e `ComprovanteDoCredito` (como o resultado vira
    resposta) e `GravadorDaCompra` (o que uma compra grava: o cartão debitado e o
    extrato). As três primeiras são funções puras, e o instante da compra entra como
    argumento, lido do `Clock` pelo caso de uso.
- **`dtos`** — a transferência entre a API e os casos de uso. Os de entrada,
  `DtoCompra` e `DtoCredito`, **já trazem os tipos do domínio** (`TipoCartao`,
  `NumeroDeCartao`, `Dinheiro`): a borda converte o que recebeu de fora com as
  leituras do domínio e só então monta o pedido, e uma conversão que falha é recusada
  lá. Os de saída, `DtoComprovanteCompra` e `DtoComprovanteCredito`, carregam
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
│   └── cartao
│       ├── ComprovanteDaCompra.java      # extrato + cartão debitado → comprovante da compra
│       ├── ComprovanteDoCredito.java     # pedido + cartão creditado → comprovante do crédito
│       ├── CreditarCartaoCaso.java       # só orquestra o crédito
│       ├── ExtratoDaCompra.java          # pedido + valor cobrado + instante → extrato
│       ├── GravadorDaCompra.java         # grava o cartão debitado e o extrato
│       ├── RegistrarCompraCaso.java      # só orquestra a compra
│       └── package-info.java
├── dtos
│   ├── DtoCompra.java                    # o pedido de compra, com os tipos do domínio
│   ├── DtoComprovanteCompra.java         # o comprovante: os dois valores, o saldo e o instante
│   ├── DtoComprovanteCredito.java        # o comprovante do crédito: o valor e o saldo atual
│   ├── DtoCredito.java                   # o pedido de crédito, com os tipos do domínio
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
| `ExtratoDaCompra` | `ExtratoDaCompraTeste` (os dois valores distintos, o instante recebido, texto nulo) |
| `ComprovanteDaCompra` | `ComprovanteDaCompraTeste` (valores e instante do extrato, saldo do cartão, número mascarado) |
| `ComprovanteDoCredito` | `ComprovanteDoCreditoTeste` |
| `GravadorDaCompra` | `GravadorDaCompraTeste` (grava o cartão, registra o extrato, devolve o cartão gravado) |
| Regra de dependência da aplicação | `RegraDeDependenciaDaAplicacaoTeste`, na raiz de `src/test/.../aplicacao` |

Peças sem teste próprio, cobertas junto de quem as torna observáveis:

| Peça sem teste próprio | Coberta junto de | Por quê |
|---|---|---|
| `DtoCompra`, `DtoCredito`, `DtoComprovanteCompra`, `DtoComprovanteCredito` | os testes dos casos de uso e das peças de comprovante | são só dados, sem operação; o único caminho até eles é o uso |

Os fakes e a fábrica de cartões também seguem a regra de uma peça por arquivo, e têm
os seus testes (`CartaoPortaFakeTeste`, `ExtratoPortaFakeTeste`,
`ModeloDeCartaoTeste`): um fake que aceita o que a porta real recusa deixaria os
testes dos casos de uso verdes por um motivo falso.

## Evolução

| PR | Mudança no contrato desta camada |
|---|---|
| `#13` | Cria a camada: `RegistrarCompraCaso` e `CreditarCartaoCaso`, as peças `ExtratoDaCompra`, `ComprovanteDaCompra`, `ComprovanteDoCredito` e `GravadorDaCompra`, e os DTOs `DtoCompra`, `DtoCredito`, `DtoComprovanteCompra` e `DtoComprovanteCredito`. O crédito passa a identificar o cartão por número **e tipo**, e a compra passa a devolver comprovante (o legado devolvia vazio). Nenhum código do legado depende ainda dela. |
