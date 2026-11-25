# dominio

Camada de domínio: os tipos de valor, os erros de negócio e o modo de representar
o que pode falhar. **Não depende de nenhuma outra camada do projeto**, nem de
Spring, JPA ou Lombok: só da biblioteca padrão do Java. Quem usa o domínio (casos
de uso, adaptadores, controllers) depende dele, e nunca o contrário. A regra é
verificada por teste, não por convenção: `RegraDeDependenciaTeste` lê os
`import` do pacote e falha se aparecer qualquer coisa fora de `java.*` e do
próprio domínio — ver `docs/padronizacao/fundamentos/ESTRUTURA.md`.

## Propósito

Dá ao resto do sistema as coisas que ele não pode inventar sozinho: uma
maneira de dizer que uma operação falhou **sem lançar exceção**, uma maneira de
dizer **por que** ela falhou, um tipo para **dinheiro** que não perde fração de
centavo, e o **cartão**: o tipo, o número e o próprio cartão, que debita e
credita sem deixar o saldo ficar negativo, o **extrato** da compra e as **portas**
por onde o cartão e o extrato chegam ao banco. Nenhum código de negócio usa `double`, `float` nem `BigDecimal` solto
para dinheiro, e nenhuma regra de negócio devolve `null` para dizer "não achei".

- **`resultado`** — o resultado de uma operação que pode falhar, como dado.
  `Resultado<T, E>` é uma interface selada com duas variantes: `Sucesso`, que
  carrega o valor, e `Falha`, que carrega o erro. As variantes implementam uma
  única operação, `resolver` (`Resolvivel`), que converte o resultado num valor
  escolhendo o caminho do sucesso ou o da falha. As outras duas operações são
  escritas em cima dela, cada uma em seu arquivo: `Transformavel` (`mapear`,
  que transforma o valor sem poder falhar) e `Encadeavel` (`encadear`, que roda
  uma segunda operação que também pode falhar, e deixa a primeira falha chegar a
  quem chamou sem que as etapas seguintes executem). `AusenciaComoErro` converte
  um `Optional` em resultado, dando nome de erro à ausência. Para criar um
  resultado usa-se o construtor da variante: `new Sucesso<>(valor)` ou
  `new Falha<>(erro)`.
- **`erros`** — o contrato de todo erro de negócio. `ErroDominio` não herda de
  `Exception`: é um valor que circula dentro de um `Resultado`, e só compõe quatro
  papéis, cada um em seu arquivo: `ComCategoria` (a categoria de onde sai o
  status HTTP), `ComCodigo` (o código estável da variante), `ComMensagem` (o texto
  para uma pessoa, sem dado sensível por inteiro) e `ComCamposComErro` (os campos
  da entrada a que o erro se refere, vazio por padrão). `CategoriaErro` é o enum
  das categorias, com o status HTTP como dado. Cada categoria mora em um subpacote.
- **`erros.validacao`** — a categoria dos erros em que a entrada está malformada.
  `ErroValidacao` é uma interface selada que dá a categoria uma vez só, e as
  variantes são quatro: `ErroValorMonetarioInvalido` (o texto não é uma quantia
  válida), `ErroValorNaoPositivo` (a quantia é zero ou negativa),
  `ErroTipoEstabelecimentoInvalido` (o código de estabelecimento não corresponde a
  nenhum tipo; carrega o código recebido, que é só um número de tipo) e
  `ErroNumeroDeCartaoInvalido` (o texto não é um número de cartão). O erro de
  valor e o de número **não devolvem o texto recebido**: um valor rejeitado pode
  ser um número de cartão quase certo, e devolvê-lo é uma forma de vazar dado.
  Cada variante declara a própria constante `CODIGO`, no formato
  `CPT-<CATEGORIA>-<ÁREA>-<NNN>` (`CPT-VAL-DIN-001`, `CPT-VAL-DIN-002`,
  `CPT-VAL-CRT-001` e `CPT-VAL-CRT-002`).
- **`erros.regradenegocio`** — a categoria dos erros em que a entrada é válida e
  uma regra a recusa diante do estado, com status 422. `ErroRegraDeNegocio` é
  selada e dá a categoria, e `ErroSaldoInsuficiente` (`CPT-REG-CRT-001`) é a
  variante: o saldo não cobre o valor. Carrega o saldo e o valor para o log, mas
  a mensagem não os repete, porque ela vai para quem fez o pedido.
- **`erros.naoencontrado`** — a categoria dos erros em que o recurso nomeado não
  existe, com status 404. `ErroNaoEncontrado` é selada, e `ErroCartaoNaoEncontrado`
  (`CPT-NEN-CRT-001`) é a variante: não existe cartão com aquele número e tipo. Um
  cartão de outro tipo com o mesmo número também conta como não encontrado, para
  não revelar a quem pergunta quais cartões existem. Carrega o `NumeroDeCartao`, de
  modo que a mensagem só mostra a forma mascarada.
- **`erros.conflito`** — a categoria dos erros em que o pedido é válido e colide com
  o que já existe ou com uma escrita anterior, com status 409. `ErroConflito` é selada,
  e as variantes são `ErroDocumentoJaCadastrado` (`CPT-CNF-CON-001`),
  `ErroNumeroDeCartaoJaCadastrado` (`CPT-CNF-CRT-001`) e `ErroVersaoDivergente`
  (`CPT-CNF-CON-002`, o consumidor mudou depois da leitura que o cliente usou). O
  documento e o número vêm como tipos de valor, de modo que a mensagem só os mostra
  mascarados. O consumidor ganhou ainda quatro erros de validação, um por leitura
  (`ErroDocumentoInvalido`, `ErroTelefoneInvalido`, `ErroEmailInvalido` e
  `ErroCepInvalido`, de `CPT-VAL-CON-001` a `004`) e `ErroConsumidorNaoEncontrado`
  (`CPT-NEN-CON-001`).
- **`consumidor`** — quem é o cliente, como falar com ele, onde mora e que cartões tem.
  `Consumidor` é o agregado raiz: id, dados, cartões e a versão da linha, que quem
  atualiza reenvia para dois clientes não se sobrescreverem em silêncio. Os dados
  cadastrais (`DadosDoConsumidor`) ficam separados dos cartões (`CartoesDoConsumidor`,
  no máximo um de cada tipo) de propósito: o que a atualização troca é só o cadastro,
  e `comDados` devolve um consumidor com os mesmos cartões, de modo que atualizar um
  cadastro não tem por onde mexer em saldo. `NovoConsumidor` é o que ainda não foi
  gravado, sem id nem versão, para nenhum chamador ter de lembrar do que um zero quer
  dizer. `ResumoDeConsumidor` é a projeção da listagem, só id e nome. Em subpacotes, o
  `documento` (CPF ou CNPJ, 11 ou 14 dígitos; o dígito verificador **não** é conferido, de
  propósito: a unicidade só precisa da forma canônica, e recusar ou aceitar um número
  por ele ser ou não um CPF válido de alguém real não é papel desta camada), o
  `contato` (celular obrigatório, dois telefones opcionais e e-mail) e o `endereco`
  (número do imóvel como texto, e CEP de 8 dígitos). Cada tipo de valor guarda a forma
  canônica, e a leitura do texto de fora é uma peça à parte que não lança: aceita a
  pontuação comum, e uma falha não repete o texto recebido. Documento, telefone e
  e-mail são dados pessoais, e o `toString()` de cada um é mascarado.
- **`texto`** — `TextoLimitado`, que exige um texto não vazio e dentro do tamanho da
  coluna que o guardará, para o nome, a rua e a cidade não repetirem a conferência.
- **`dinheiro`** — quantias em reais. `Dinheiro` guarda a quantia com **seis casas
  decimais**, para não perder a fração de centavo que os cálculos produzem (10% de
  desconto sobre 33,33 é 29,997), e garante a escala: um valor com casas a mais é
  erro de quem construiu, e lança em vez de arredondar sem avisar. Como toda
  instância tem a mesma escala, `2,0` e `2,00` são iguais. As operações são papéis
  em arquivos próprios, escritos em cima de `ComQuantia` (que expõe `valor()`):
  `Somavel` (`mais`), `Subtraivel` (`menos`), `Multiplicavel` (`vezes`, a única
  operação que arredonda, uma vez, com `HALF_EVEN`) e `ComparavelPorQuantia`
  (`compareTo`). `DinheiroDeTexto` lê uma quantia de texto vindo de fora, sem
  lançar exceção: aceita decimal com ponto, de até 13 dígitos inteiros e 6 casas,
  com sinal opcional. `DinheiroPositivo` exige que a quantia seja maior que zero,
  também sem lançar exceção: sem ele, um débito de -50 aumentaria o saldo sem que
  a regra de saldo percebesse. `ApresentacaoDeDinheiro` mostra a quantia em centavos, e o
  modo de arredondar é um argumento obrigatório, sem padrão, porque `HALF_UP` e
  `HALF_EVEN` dão resultados diferentes em 29,985.
- **`cartao`** — o cartão e os seus tipos. `Cartao` é um record imutável com
  número, tipo e saldo, e o saldo nunca é negativo: criar um cartão assim lança.
  Debitar e creditar não alteram o cartão, devolvem outro com o novo saldo, e são
  papéis em arquivos próprios, `Debitavel` e `Creditavel`, escritos em cima de
  `ComSaldo` (`saldo()` e `comSaldo(novo)`). Os dois recusam valor zero ou
  negativo com `ErroValorNaoPositivo`, e o débito recusa o que passa do saldo com
  `ErroSaldoInsuficiente`; debitar exatamente o saldo é permitido. `TipoCartao` é o enum dos três tipos, e cada
  um guarda como **dado** o código de estabelecimento e o ajuste do valor, como
  fração com sinal: alimentação `-0.10`, farmácia `0.00`, combustível `0.35`. O
  cálculo é escrito em cima desse dado, em arquivos próprios: `Cobravel`
  (`valorCobrado`, valor × (1 + ajuste), que guarda a fração de centavo) sobre
  `ComAjusteDoValor`, e `ComCodigoDeEstabelecimento`. `TipoCartaoDoEstabelecimento`
  lê o código que vem de fora e devolve o tipo, ou `ErroTipoEstabelecimentoInvalido`:
  um código que não existe é um erro, e não o ramo do combustível, que é onde o
  legado o jogava. `NumeroDeCartao` guarda de 13 a 19 dígitos **na forma canônica**,
  sem separador, e é um dado sensível: seu `toString()` devolve só a forma
  mascarada (`****2222`), de modo que nenhum log o vaza; a máscara é o papel
  `Mascaravel`, sobre `ComDigitos`. `NumeroDeCartaoDeTexto` lê o número de texto
  vindo de fora, sem lançar exceção: tira espaços e hífens, e recusa qualquer outro
  caractere em vez de descartá-lo.
- **`extrato`** — o registro de uma compra efetivada. `Extrato` é um record
  imutável sem operação nenhuma: guarda o estabelecimento, o produto, o instante,
  o cartão (número e tipo) e **os dois valores**, o informado e o cobrado, porque a
  diferença entre eles é o ajuste do tipo, e só com o cobrado um desconto de 10%
  sobre 33,33 seria indistinguível de uma compra de 29,997. O instante vem de quem
  registra, e não do relógio, para o extrato ser um dado que o teste possa fixar.
- **`portas`** — os contratos que o domínio exige do mundo de fora; a
  infraestrutura os implementa, e a dependência aponta sempre para o domínio.
  Cada operação é um papel em seu arquivo, e a porta só os compõe: `CartaoPorta`
  junta `BuscadorDeCartaoParaAtualizacao` (devolve o cartão travado até o fim da
  transação, ou `ErroCartaoNaoEncontrado`) e `GravadorDeCartao` (`salvar`, que
  atualiza o saldo de um cartão que já existe e lança se ele não existe: criar
  um cartão exige o consumidor dono). `ConsumidorPorta` junta `BuscadorDeConsumidor`
  (por id, ou `ErroConsumidorNaoEncontrado`), `GravadorDeNovoConsumidor` (o consumidor
  e os cartões numa só operação, ou o conflito de documento ou de número) e
  `AtualizadorDeConsumidor` (só os dados cadastrais; o saldo que vier no consumidor é
  ignorado na gravação).
  `ExtratoPorta` só oferece `registrar`: extrato é registro financeiro, nunca
  atualizado nem apagado, e o que nenhuma interface oferece ninguém chama por
  engano.

Uma categoria de erro só nasce junto do primeiro erro que a usa. Existem
`VALIDACAO`, `REGRA_DE_NEGOCIO`, `NAO_ENCONTRADO` e `CONFLITO`; a última entrou com
o consumidor, cujo documento e cujos números de cartão são únicos.

## Estrutura

Convenção aplicada em toda a camada, registrada em
`docs/padronizacao/fundamentos/ESTRUTURA.md`: **uma peça por arquivo**. A razão de
mudar é cirúrgica, e o motivo é de equipe: se duas operações moram no mesmo
arquivo, duas pessoas mexendo ao mesmo tempo disputam o mesmo estado dele; com cada
peça no seu arquivo, o pior que acontece é aparecerem arquivos novos ao sincronizar,
sem afetar a tarefa em curso. Em Java, uma operação é um papel (`interface`) com
método `default`, escrito em cima de uma única operação-base que o tipo expõe
(`resolver` no `Resultado`, `valor()` no `Dinheiro`). O tipo só guarda o dado e
declara os papéis. Acessor trivial, sem regra própria, fica com o tipo: é o caso
de `codigo()` e `mensagem()` em cada variante de erro, que são os dados da própria
variante.

Saída real de `tree` (regenerar e colar aqui a cada PR que mudar a árvore — ver
`docs/padronizacao/codigo/DOCUMENTACAO.md`, "READMEs Por Camada"):

```text
dominio
├── cartao
│   ├── Cartao.java                   # número, tipo e saldo; só guarda o dado e declara os papéis
│   ├── Cobravel.java                 # calcular o valor cobrado: valor × (1 + ajuste)
│   ├── ComAjusteDoValor.java         # operação-base: o ajuste do tipo, como fração
│   ├── ComCodigoDeEstabelecimento.java   # o código de estabelecimento do tipo
│   ├── ComDigitos.java               # operação-base: os dígitos do número
│   ├── ComSaldo.java                 # operação-base: saldo() e comSaldo(novo)
│   ├── Creditavel.java               # creditar, recusando valor não positivo
│   ├── Debitavel.java                # debitar, recusando valor não positivo e saldo insuficiente
│   ├── Mascaravel.java               # mostrar só os quatro últimos dígitos
│   ├── NumeroDeCartao.java           # o número, na forma canônica, mascarado ao imprimir
│   ├── NumeroDeCartaoDeTexto.java    # ler o número de texto vindo de fora
│   ├── TipoCartao.java               # os três tipos, com o ajuste como dado
│   ├── TipoCartaoDoEstabelecimento.java  # ler o código de estabelecimento
│   └── package-info.java
├── consumidor
│   ├── CartoesDoConsumidor.java      # os cartões, no máximo um de cada tipo
│   ├── Consumidor.java               # o agregado raiz: id, dados, cartões e versão; comDados troca só o cadastro
│   ├── ConsumidorId.java             # o id, sempre positivo
│   ├── DadosDoConsumidor.java        # o cadastro: nome, documento, nascimento, contato, endereço
│   ├── NovoConsumidor.java           # ainda não gravado: dados e cartões, sem id nem versão
│   ├── ResumoDeConsumidor.java       # a projeção da listagem: id e nome
│   ├── contato
│   │   ├── Contato.java              # celular, dois telefones opcionais e e-mail
│   │   ├── Email.java                # o e-mail; ao imprimir, só o domínio aparece
│   │   ├── EmailDeTexto.java         # ler o e-mail de texto vindo de fora
│   │   ├── Telefone.java             # de 10 a 15 dígitos, mascarado ao imprimir
│   │   ├── TelefoneDeTexto.java      # ler o telefone, apontando o campo no erro
│   │   ├── TelefoneOpcionalDeTexto.java  # ler o telefone que pode não ser informado
│   │   └── package-info.java
│   ├── documento
│   │   ├── Documento.java            # CPF ou CNPJ na forma canônica, mascarado ao imprimir
│   │   ├── DocumentoDeTexto.java     # ler o documento de texto vindo de fora
│   │   └── package-info.java
│   ├── endereco
│   │   ├── Cep.java                  # 8 dígitos
│   │   ├── CepDeTexto.java           # ler o CEP de texto vindo de fora
│   │   ├── Endereco.java             # rua, número como texto, cidade, país e CEP
│   │   └── package-info.java
│   └── package-info.java
├── dinheiro
│   ├── ApresentacaoDeDinheiro.java   # apresentar em centavos, com o modo de arredondar obrigatório
│   ├── ComQuantia.java               # operação-base: expõe o valor
│   ├── ComparavelPorQuantia.java     # ordenar pelo valor numérico
│   ├── Dinheiro.java                 # a quantia e a escala, e nada mais
│   ├── DinheiroDeTexto.java          # ler uma quantia de texto vindo de fora
│   ├── DinheiroPositivo.java         # exigir uma quantia maior que zero
│   ├── Multiplicavel.java            # multiplicar por um fator, com HALF_EVEN
│   ├── Somavel.java                  # somar, exato
│   ├── Subtraivel.java               # subtrair, exato
│   └── package-info.java
├── erros
│   ├── CategoriaErro.java            # enum de categorias, com o status HTTP como dado
│   ├── ComCamposComErro.java         # campos da entrada a que o erro se refere
│   ├── ComCategoria.java             # a categoria do erro
│   ├── ComCodigo.java                # o código estável da variante
│   ├── ComMensagem.java              # o texto para uma pessoa
│   ├── ErroDominio.java              # só compõe os quatro papéis
│   ├── conflito
│   │   ├── ErroConflito.java         # categoria: colide com o que existe ou com escrita anterior (409)
│   │   ├── ErroDocumentoJaCadastrado.java    # já existe consumidor com o documento
│   │   ├── ErroNumeroDeCartaoJaCadastrado.java   # já existe cartão com o número
│   │   ├── ErroVersaoDivergente.java # o consumidor mudou depois da leitura do cliente
│   │   └── package-info.java
│   ├── naoencontrado
│   │   ├── ErroCartaoNaoEncontrado.java  # não existe cartão com aquele número e tipo
│   │   ├── ErroConsumidorNaoEncontrado.java  # não existe consumidor com aquele id
│   │   ├── ErroNaoEncontrado.java    # categoria: o recurso nomeado não existe (404)
│   │   └── package-info.java
│   ├── package-info.java
│   ├── regradenegocio
│   │   ├── ErroRegraDeNegocio.java   # categoria: a regra recusa diante do estado (422)
│   │   ├── ErroSaldoInsuficiente.java    # o saldo não cobre o valor
│   │   └── package-info.java
│   └── validacao
│       ├── ErroCepInvalido.java      # o texto não é um CEP
│       ├── ErroDocumentoInvalido.java    # o texto não é um documento
│       ├── ErroEmailInvalido.java    # o texto não é um e-mail
│       ├── ErroNumeroDeCartaoInvalido.java   # o texto não é um número de cartão
│       ├── ErroTelefoneInvalido.java # o texto não é um telefone; aponta o campo
│       ├── ErroTipoEstabelecimentoInvalido.java   # o código não é um tipo conhecido
│       ├── ErroValidacao.java        # categoria: entrada malformada
│       ├── ErroValorMonetarioInvalido.java   # o texto não é uma quantia válida
│       ├── ErroValorNaoPositivo.java # a quantia é zero ou negativa
│       └── package-info.java
├── extrato
│   ├── Extrato.java                  # a compra efetivada, com o valor informado e o cobrado
│   └── package-info.java
├── package-info.java
├── pagina                            # a paginação das listagens (base 1, sem Spring)
│   ├── ComTotalDePaginas.java        # papel: o total de páginas, arredondado para cima
│   ├── DirecaoDeOrdenacao.java       # ASC, DESC
│   ├── DirecaoDeOrdenacaoDeTexto.java
│   ├── OrdenacaoDeConsumidor.java    # ID, NOME: conjunto fechado, nunca o nome de uma coluna
│   ├── OrdenacaoDeConsumidorDeTexto.java
│   ├── Pagina.java                   # os itens, a página, o tamanho e o total
│   ├── PaginaDeNumero.java
│   ├── PedidoDePagina.java           # página, tamanho (teto 100), ordenação e direção
│   ├── PedidoDePaginaDeParametros.java  # lê os quatro parâmetros, todos os erros juntos
│   ├── TamanhoDePaginaDeNumero.java
│   └── package-info.java
├── portas
│   ├── AtualizadorDeConsumidor.java  # papel: gravar os dados de um consumidor que existe
│   ├── BuscadorDeCartaoParaAtualizacao.java  # papel: buscar o cartão travando para escrever
│   ├── BuscadorDeConsumidor.java     # papel: buscar o consumidor por id
│   ├── ListadorDeConsumidores.java   # papel: listar os resumos, uma página por vez
│   ├── CartaoPorta.java              # só compõe os dois papéis do cartão
│   ├── ConsumidorPorta.java          # só compõe os três papéis do consumidor
│   ├── ExtratoPorta.java             # só registrar
│   ├── GravadorDeCartao.java         # papel: salvar o cartão
│   ├── GravadorDeNovoConsumidor.java # papel: gravar um consumidor novo, com os cartões
│   └── package-info.java
├── resultado
│   ├── AusenciaComoErro.java         # converter Optional em resultado
│   ├── Encadeavel.java               # encadear uma operação que também pode falhar
│   ├── Falha.java                    # variante de falha: só `resolver`
│   ├── Resolvivel.java               # consumir: a operação-base
│   ├── Resultado.java                # compõe os papéis e fecha as variantes
│   ├── Sucesso.java                  # variante de sucesso: só `resolver`
│   ├── Transformavel.java            # transformar o valor, sem poder falhar
│   └── package-info.java
└── texto
    ├── TextoLimitado.java            # exigir texto não vazio e de tamanho limitado
    └── package-info.java
```

## Exemplo de uso

```java
Resultado<Dinheiro, ErroDominio> resultado = DinheiroDeTexto.converter("33.33");

Resultado<Dinheiro, ErroDominio> comDesconto =
    resultado.mapear(valor -> valor.vezes(new BigDecimal("0.90")));

String resposta = comDesconto.resolver(
    dinheiro -> "cobrado: " + ApresentacaoDeDinheiro.emCentavos(dinheiro, RoundingMode.HALF_EVEN),
    erro -> "recusado (" + erro.codigo() + "): " + erro.mensagem());
```

Para um texto que não é uma quantia, como `"1,5"`, o mesmo código devolve
`recusado (CPT-VAL-DIN-001): ...` sem lançar exceção e sem que `mapear` execute.

## Dependências externas

Nenhuma. O domínio usa só a biblioteca padrão do Java: `java.math`
(`BigDecimal`, `RoundingMode`), `java.util` (`Optional`, `Map`, `List`),
`java.util.function` e `java.util.regex`. Não depende de nenhuma outra camada do
projeto, nem de qualquer mecanismo de persistência ou de rede: essas
implementações vivem na infraestrutura. O enunciado do desafio proíbe dependência
nova, e esta camada não precisa de nenhuma.

## Testes

Framework do `spring-boot-starter-test` (JUnit 5 e AssertJ), sem dependência nova.
Os testes **não** moram aqui: ficam em `src/test/java/.../dominio/`, espelhando
estes pacotes, e cada um é teste unitário puro, sem contexto Spring. Todo teste
segue Preparar, Agir, Verificar, com as fases marcadas por comentário
(`docs/padronizacao/qualidade/TESTES.md`).

### Executar

```bash
./gradlew test --tests '*dominio*'
```

### Cobertura

Não há medição de cobertura por ferramenta: o plugin do Gradle baixaria um
artefato novo no build, e o enunciado proíbe dependência nova. A garantia é por
regra: cada peça tem o teste que falharia sem ela, e a tabela abaixo é o
inventário. Atualize-a a cada PR que mudar a árvore.

| Peça | Teste que a cobre |
|---|---|
| `Sucesso`, `Falha` e a operação-base `resolver` | `SucessoTeste`, `FalhaTeste` |
| `Transformavel.mapear` | `SucessoTeste`, `FalhaTeste` (a função não executa numa falha) |
| `Encadeavel.encadear` | `SucessoTeste`, `FalhaTeste` (a operação seguinte não executa numa falha) |
| `Resultado` (variantes fechadas, papéis compostos) | `ResultadoTeste` |
| `AusenciaComoErro` | `AusenciaComoErroTeste` |
| `Dinheiro` (escala, igualdade) | `DinheiroTeste` |
| `DinheiroDeTexto` | `DinheiroDeTextoTeste` (formatos aceitos e recusados, e o texto recusado não volta) |
| `ApresentacaoDeDinheiro` | `ApresentacaoDeDinheiroTeste` |
| `ErroValorMonetarioInvalido` | `ErroValorMonetarioInvalidoTeste` |
| `TipoCartao`, `Cobravel`, `ComAjusteDoValor`, `ComCodigoDeEstabelecimento` | `TipoCartaoTeste` (valor cobrado, com a fração de centavo; código; ajuste) |
| `TipoCartaoDoEstabelecimento` | `TipoCartaoDoEstabelecimentoTeste` (1, 2 e 3; e os códigos que não existem) |
| `ErroTipoEstabelecimentoInvalido` | `ErroTipoEstabelecimentoInvalidoTeste` |
| `NumeroDeCartao`, `Mascaravel`, `ComDigitos` | `NumeroDeCartaoTeste` (formato, máscara, `toString` que não vaza, igualdade) |
| `NumeroDeCartaoDeTexto` | `NumeroDeCartaoDeTextoTeste` (forma canônica, formatos recusados, texto recusado que não volta) |
| `ErroNumeroDeCartaoInvalido` | `ErroNumeroDeCartaoInvalidoTeste` |
| `DinheiroPositivo` | `DinheiroPositivoTeste` (zero, negativo e nulo recusados; o menor valor positivo aceito) |
| `ErroValorNaoPositivo` | `ErroValorNaoPositivoTeste` |
| `Cartao`, `Debitavel`, `Creditavel`, `ComSaldo` | `CartaoTeste` (débito até o saldo exato, e um milionésimo a mais; crédito; valor não positivo e nulo; saldo negativo no construtor; o original não muda) |
| `ErroSaldoInsuficiente` | `ErroSaldoInsuficienteTeste` (categoria 422, e a mensagem que não repete saldo nem valor) |
| `ErroCartaoNaoEncontrado` | `ErroCartaoNaoEncontradoTeste` (categoria 404, e a mensagem que só mostra o número mascarado) |
| `Extrato` | `ExtratoTeste` (os dois valores guardados, componente nulo lança, `toString` que não vaza) |
| `ExtratoPorta` | `ExtratoPortaTeste` (só oferece `registrar`: não há atualizar nem excluir) |
| `CartaoPorta`, `BuscadorDeCartaoParaAtualizacao`, `GravadorDeCartao` | `CartaoPortaTeste` (compõe os dois papéis e não declara operação própria) |
| `Documento`, `DocumentoDeTexto`, `ErroDocumentoInvalido` | `DocumentoTeste`, `DocumentoDeTextoTeste` (11 e 14 dígitos, pontuação aceita, `toString` mascarado, texto recusado que não volta), `ErroDocumentoInvalidoTeste` |
| `Telefone`, `TelefoneDeTexto`, `TelefoneOpcionalDeTexto`, `ErroTelefoneInvalido` | os quatro `*Teste` de mesmo nome (de 10 a 15 dígitos; o opcional ausente é vazio e o mal escrito é recusado; o campo vai no erro) |
| `Email`, `EmailDeTexto`, `ErroEmailInvalido` | `EmailTeste` (formato, limite de 254, `toString` que esconde o nome), `EmailDeTextoTeste`, `ErroEmailInvalidoTeste` |
| `Contato` | `ContatoTeste` (opcionais vazios, e nulo no lugar de vazio lança) |
| `Cep`, `CepDeTexto`, `Endereco`, `ErroCepInvalido` | `CepTeste`, `CepDeTextoTeste`, `EnderecoTeste` (número como texto, limites das colunas), `ErroCepInvalidoTeste` |
| `TextoLimitado` | `TextoLimitadoTeste` (vazio, nulo, limite exato e um a mais; a mensagem que não traz o valor) |
| `ConsumidorId`, `DadosDoConsumidor`, `CartoesDoConsumidor`, `NovoConsumidor`, `ResumoDeConsumidor` | os `*Teste` de mesmo nome; `DadosDoConsumidorTeste` e `NovoConsumidorTeste` provam que o tipo não tem por onde carregar saldo, id nem versão |
| `Consumidor` | `ConsumidorTeste` (`comDados` troca só o cadastro e mantém cartões, saldo e versão; versão negativa lança; o `toString` não vaza documento nem cartão) |
| `ErroConsumidorNaoEncontrado` | `ErroConsumidorNaoEncontradoTeste` |
| `ErroDocumentoJaCadastrado`, `ErroNumeroDeCartaoJaCadastrado`, `ErroVersaoDivergente` | os três `*Teste` de mesmo nome (categoria 409, e a mensagem que só mostra o documento e o número mascarados) |
| `ConsumidorPorta`, `BuscadorDeConsumidor`, `ListadorDeConsumidores`, `GravadorDeNovoConsumidor`, `AtualizadorDeConsumidor` | `ConsumidorPortaTeste` (compõe os quatro papéis, não declara operação própria, uma operação por papel) |
| `Pagina`, `PedidoDePagina`, `PedidoDePaginaDeParametros`, as leituras de página, tamanho, ordenação e direção | `PaginaTeste`, `PedidoDePaginaTeste`, `PedidoDePaginaDeParametrosTeste` (bordas, maiúsculas, texto recusado sem eco, quatro erros de uma vez) |
| `ErroPaginaInvalida`, `ErroTamanhoDePaginaInvalido`, `ErroOrdenacaoInvalida`, `ErroDirecaoInvalida` | `ErrosDePaginacaoTeste` e o teste de códigos por reflexão |
| Códigos de erro (formato e unicidade) | `CodigosDeErroTeste`, que percorre a hierarquia |
| Regra de dependência do domínio | `RegraDeDependenciaTeste`, na raiz de `src/test` |

Peças sem teste próprio, cobertas junto de quem as torna observáveis:

| Peça sem teste próprio | Coberta junto de | Por quê |
|---|---|---|
| `Somavel`, `Subtraivel`, `Multiplicavel`, `ComparavelPorQuantia`, `ComQuantia` | `DinheiroTeste` | existem só como partes do `Dinheiro`; o único caminho até elas é `mais`, `menos`, `vezes` e `compareTo` |
| `Resolvivel` | `SucessoTeste`, `FalhaTeste` | é a operação-base, e o único caminho até ela é o `resolver` das variantes |
| `ComCategoria`, `ComCodigo`, `ComMensagem`, `ComCamposComErro`, `ErroDominio`, `ErroValidacao`, `ErroRegraDeNegocio`, `ErroNaoEncontrado`, `ErroConflito`, `CategoriaErro` | os testes das variantes de erro | só existem como contrato de uma variante, e cada variante os exercita |

## Evolução

| PR | Mudança no contrato desta camada |
|---|---|
| `#6` | Cria a camada: `resultado` (`Resultado`, `Sucesso`, `Falha`, os papéis `Resolvivel`, `Transformavel` e `Encadeavel`, e `AusenciaComoErro`), `erros` (`ErroDominio` e os papéis `ComCategoria`, `ComCodigo`, `ComMensagem` e `ComCamposComErro`, `CategoriaErro`), `erros.validacao` (`ErroValidacao` e `ErroValorMonetarioInvalido`) e `dinheiro` (`Dinheiro`, os papéis `Somavel`, `Subtraivel`, `Multiplicavel`, `ComparavelPorQuantia` e `ComQuantia`, `DinheiroDeTexto` e `ApresentacaoDeDinheiro`). Nenhum código do legado depende ainda dela. |
| `#8` | Acrescenta o pacote `cartao` (`TipoCartao`, os papéis `Cobravel`, `ComAjusteDoValor` e `ComCodigoDeEstabelecimento`, `TipoCartaoDoEstabelecimento`, `NumeroDeCartao`, os papéis `Mascaravel` e `ComDigitos`, e `NumeroDeCartaoDeTexto`) e duas variantes de `ErroValidacao`: `ErroTipoEstabelecimentoInvalido` (`CPT-VAL-CRT-001`) e `ErroNumeroDeCartaoInvalido` (`CPT-VAL-CRT-002`). O `CodigosDeErroTeste` passa a instanciar as variantes por reflexão. Nenhum código do legado depende ainda dos tipos novos. |
| `#11` | Acrescenta o `Cartao` (record imutável de número, tipo e saldo, que não aceita saldo negativo) e os papéis `ComSaldo`, `Debitavel` e `Creditavel`, `DinheiroPositivo`, a categoria `REGRA_DE_NEGOCIO` (422) com `erros.regradenegocio` (`ErroRegraDeNegocio` e `ErroSaldoInsuficiente`, `CPT-REG-CRT-001`) e `ErroValorNaoPositivo` (`CPT-VAL-DIN-002`) em `ErroValidacao`. O `CodigosDeErroTeste` passa a percorrer as duas categorias. Nenhum código do legado depende ainda do cartão. |
| `#12` | Acrescenta o `Extrato` (compra efetivada, com o valor informado e o cobrado) e o pacote `portas`: `CartaoPorta`, que compõe os papéis `BuscadorDeCartaoParaAtualizacao` e `GravadorDeCartao`, e `ExtratoPorta`, que só registra. Acrescenta também a categoria `NAO_ENCONTRADO` (404) com `erros.naoencontrado` (`ErroNaoEncontrado` e `ErroCartaoNaoEncontrado`, `CPT-NEN-CRT-001`); o `CodigosDeErroTeste` passa a percorrer as três categorias. Nenhum código do legado depende ainda das portas. |
| `#14` | Fixa o contrato do `GravadorDeCartao`: grava o novo estado de um cartão **que já existe**, e lança `IllegalStateException` se ele não existe, em vez de criá-lo. Nenhuma outra peça do domínio muda. |
| `#19` | Acrescenta o que a API do consumidor precisa: `ValidacoesReunidas` (com `CampoValidado` e `MensagensDaFalha`), que reúne as falhas de campos independentes em `ErroEntradaInvalida` (`CPT-VAL-GER-001`), `TipoCartaoDoNome` e `ErroTipoDeCartaoInvalido` (`CPT-VAL-CRT-003`), e `ConsumidorIdDeNumero` e `ErroConsumidorIdInvalido` (`CPT-VAL-CON-005`). |
| `#16` | Acrescenta o `consumidor` (`Consumidor`, `DadosDoConsumidor`, `CartoesDoConsumidor`, `NovoConsumidor`, `ConsumidorId`, `ResumoDeConsumidor`, e os subpacotes `documento`, `contato` e `endereco`, com as leituras de texto), `texto` (`TextoLimitado`) e o `ConsumidorPorta`, que compõe três papéis. Acrescenta a categoria `CONFLITO` (409) com `erros.conflito`, e variantes de validação (`CPT-VAL-CON-001` a `004`), de não encontrado (`CPT-NEN-CON-001`) e de conflito (`CPT-CNF-CON-001`, `CPT-CNF-CON-002` e `CPT-CNF-CRT-001`). O `CodigosDeErroTeste` passa a percorrer as quatro categorias. Nenhum código do legado depende ainda do consumidor novo. |
