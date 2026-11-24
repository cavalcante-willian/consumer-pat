# dominio

Camada de domínio: os tipos de valor, os erros de negócio e o modo de representar
o que pode falhar. **Não depende de nenhuma outra camada do projeto**, nem de
Spring, JPA ou Lombok: só da biblioteca padrão do Java. Quem usa o domínio (casos
de uso, adaptadores, controllers) depende dele, e nunca o contrário. A regra é
verificada por teste, não por convenção: `RegraDeDependenciaTeste` lê os
`import` do pacote e falha se aparecer qualquer coisa fora de `java.*` e do
próprio domínio — ver `docs/padronizacao/fundamentos/ESTRUTURA.md`.

## Propósito

Dá ao resto do sistema quatro coisas que ele não pode inventar sozinho: uma
maneira de dizer que uma operação falhou **sem lançar exceção**, uma maneira de
dizer **por que** ela falhou, um tipo para **dinheiro** que não perde fração de
centavo, e os tipos do **cartão**: o tipo e o número, com a regra de cobrança de
cada tipo. Nenhum código de negócio usa `double`, `float` nem `BigDecimal` solto
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
  variantes são três: `ErroValorMonetarioInvalido` (o texto não é uma quantia
  válida), `ErroTipoEstabelecimentoInvalido` (o código de estabelecimento não
  corresponde a nenhum tipo; carrega o código recebido, que é só um número de
  tipo) e `ErroNumeroDeCartaoInvalido` (o texto não é um número de cartão).
  O erro de valor e o de número **não devolvem o texto recebido**: um valor
  rejeitado pode ser um número de cartão quase certo, e devolvê-lo é uma forma de
  vazar dado. Cada variante declara a própria constante `CODIGO`, no formato
  `CPT-<CATEGORIA>-<ÁREA>-<NNN>` (`CPT-VAL-DIN-001`, `CPT-VAL-CRT-001` e
  `CPT-VAL-CRT-002`).
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
  com sinal opcional. `ApresentacaoDeDinheiro` mostra a quantia em centavos, e o
  modo de arredondar é um argumento obrigatório, sem padrão, porque `HALF_UP` e
  `HALF_EVEN` dão resultados diferentes em 29,985.
- **`cartao`** — os tipos do cartão. `TipoCartao` é o enum dos três tipos, e cada
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

Uma categoria de erro só nasce junto do primeiro erro que a usa. Por isso só existe
`VALIDACAO` por enquanto; não encontrado, regra de negócio e conflito entram com
os casos de uso que os produzem. Da mesma forma, o `Cartao`, que debita e credita,
não está aqui ainda: vem na etapa seguinte, quando há a regra de saldo que o usa.

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
│   ├── Cobravel.java                 # calcular o valor cobrado: valor × (1 + ajuste)
│   ├── ComAjusteDoValor.java         # operação-base: o ajuste do tipo, como fração
│   ├── ComCodigoDeEstabelecimento.java   # o código de estabelecimento do tipo
│   ├── ComDigitos.java               # operação-base: os dígitos do número
│   ├── Mascaravel.java               # mostrar só os quatro últimos dígitos
│   ├── NumeroDeCartao.java           # o número, na forma canônica, mascarado ao imprimir
│   ├── NumeroDeCartaoDeTexto.java    # ler o número de texto vindo de fora
│   ├── TipoCartao.java               # os três tipos, com o ajuste como dado
│   ├── TipoCartaoDoEstabelecimento.java  # ler o código de estabelecimento
│   └── package-info.java
├── dinheiro
│   ├── ApresentacaoDeDinheiro.java   # apresentar em centavos, com o modo de arredondar obrigatório
│   ├── ComQuantia.java               # operação-base: expõe o valor
│   ├── ComparavelPorQuantia.java     # ordenar pelo valor numérico
│   ├── Dinheiro.java                 # a quantia e a escala, e nada mais
│   ├── DinheiroDeTexto.java          # ler uma quantia de texto vindo de fora
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
│   ├── package-info.java
│   └── validacao
│       ├── ErroNumeroDeCartaoInvalido.java   # o texto não é um número de cartão
│       ├── ErroTipoEstabelecimentoInvalido.java   # o código não é um tipo conhecido
│       ├── ErroValidacao.java        # categoria: entrada malformada
│       ├── ErroValorMonetarioInvalido.java   # o texto não é uma quantia válida
│       └── package-info.java
├── package-info.java
└── resultado
    ├── AusenciaComoErro.java         # converter Optional em resultado
    ├── Encadeavel.java               # encadear uma operação que também pode falhar
    ├── Falha.java                    # variante de falha: só `resolver`
    ├── Resolvivel.java               # consumir: a operação-base
    ├── Resultado.java                # compõe os papéis e fecha as variantes
    ├── Sucesso.java                  # variante de sucesso: só `resolver`
    ├── Transformavel.java            # transformar o valor, sem poder falhar
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
| Códigos de erro (formato e unicidade) | `CodigosDeErroTeste`, que percorre a hierarquia |
| Regra de dependência do domínio | `RegraDeDependenciaTeste`, na raiz de `src/test` |

Peças sem teste próprio, cobertas junto de quem as torna observáveis:

| Peça sem teste próprio | Coberta junto de | Por quê |
|---|---|---|
| `Somavel`, `Subtraivel`, `Multiplicavel`, `ComparavelPorQuantia`, `ComQuantia` | `DinheiroTeste` | existem só como partes do `Dinheiro`; o único caminho até elas é `mais`, `menos`, `vezes` e `compareTo` |
| `Resolvivel` | `SucessoTeste`, `FalhaTeste` | é a operação-base, e o único caminho até ela é o `resolver` das variantes |
| `ComCategoria`, `ComCodigo`, `ComMensagem`, `ComCamposComErro`, `ErroDominio`, `ErroValidacao`, `CategoriaErro` | os testes das variantes de erro | só existem como contrato de uma variante, e cada variante os exercita |

## Evolução

| PR | Mudança no contrato desta camada |
|---|---|
| `#6` | Cria a camada: `resultado` (`Resultado`, `Sucesso`, `Falha`, os papéis `Resolvivel`, `Transformavel` e `Encadeavel`, e `AusenciaComoErro`), `erros` (`ErroDominio` e os papéis `ComCategoria`, `ComCodigo`, `ComMensagem` e `ComCamposComErro`, `CategoriaErro`), `erros.validacao` (`ErroValidacao` e `ErroValorMonetarioInvalido`) e `dinheiro` (`Dinheiro`, os papéis `Somavel`, `Subtraivel`, `Multiplicavel`, `ComparavelPorQuantia` e `ComQuantia`, `DinheiroDeTexto` e `ApresentacaoDeDinheiro`). Nenhum código do legado depende ainda dela. |
| `#8` | Acrescenta o pacote `cartao` (`TipoCartao`, os papéis `Cobravel`, `ComAjusteDoValor` e `ComCodigoDeEstabelecimento`, `TipoCartaoDoEstabelecimento`, `NumeroDeCartao`, os papéis `Mascaravel` e `ComDigitos`, e `NumeroDeCartaoDeTexto`) e duas variantes de `ErroValidacao`: `ErroTipoEstabelecimentoInvalido` (`CPT-VAL-CRT-001`) e `ErroNumeroDeCartaoInvalido` (`CPT-VAL-CRT-002`). O `CodigosDeErroTeste` passa a instanciar as variantes por reflexão. Nenhum código do legado depende ainda dos tipos novos. |
