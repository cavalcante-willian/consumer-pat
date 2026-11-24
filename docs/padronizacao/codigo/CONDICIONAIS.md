[← Índice](../../PADRONIZACAO_INDEX.md) · [Próximo: DOCUMENTACAO.md](DOCUMENTACAO.md)

# Condicionais e Fluxo de Controle — Java 17

> **Escopo**: regra de condicionais e fluxo de controle para o código **Java
> 17** deste projeto: a tese, a hierarquia de 12 níveis e as regras de revisão.
> Os exemplos usam o domínio de cartões de benefício. Os pontos em que o Java
> 17 oferece menos recursos que linguagens mais novas estão em
> [Particularidades do Java 17](#particularidades-do-java-17).
>
> **Nota sobre exemplos**: os trechos de código abaixo são ilustrativos —
> nomes de tipos e métodos podem não coincidir exatamente com o código
> real, mas a estrutura e a intenção são vinculantes.
>
> **Documentos relacionados**: [`ERROS.md`](../qualidade/ERROS.md),
> [`VALORES.md`](VALORES.md), [`VALIDACAO.md`](../qualidade/VALIDACAO.md) e
> [`CONCORRENCIA.md`](../runtime/CONCORRENCIA.md).

## Sobre Este Documento

Este documento **não existe para ensinar a usar `if` corretamente.**
Ele existe para eliminar `if` do código.

**A tese**: máquina não lê código — para ela, tanto faz se há um `if` em
cada linha ou nenhum. Quem lê é humano, e o projeto será lido por humanos
*muito* e ao longo de anos: em revisões de PR, em correção de bug às 2h da
manhã, em onboarding de quem acabou de chegar, em crescimento de
funcionalidade. Cada `if` é uma **bifurcação que o leitor é forçado a
manter na cabeça** enquanto lê o resto — porque o resto só faz sentido
depois de saber qual caminho foi tomado. `if` aninhado é memória de
trabalho dobrada. `if` espalhado é memória de trabalho multiplicada pelo
número de arquivos.

**O número é uma estimativa empírica, não um valor universal**: vem da
experiência de aplicar esta regra em código real, e não de um estudo sobre
programação em geral. Nessa experiência, cerca de **7% dos casos exigiram `if`
genuinamente** — os outros **93% eram modelagem incompleta**: uma porta
não criada, um tipo que devia saber fazer, uma regra que devia ter nome.
O que é vinculante não é o percentual: é a regra de analisar todo `if`
antes de deixá-lo entrar. Portanto:

- **`if` que constrói condicional é proibido** — a primeira análise de
  qualquer `if` é "isto é uma condicional?"; se sim, ele não entra: a
  resposta é eliminar a bifurcação (níveis 1 → 10), nunca escolher uma
  técnica melhor de condicional;
- **`if` só entra como resíduo** — quando a linguagem/compilador exige
  (uma saída sim/não que nenhuma das 10 técnicas anteriores substitui)
  **e** a [pergunta zero](#regra-de-ouro) não justifica um arquivo novo;
  é aí que moram os ~7%;
- todo `if` novo no diff **precisa identificar qual nível da hierarquia
  abaixo falhou** e por quê;
- **no máximo um condicional por método** — guard (nível 11) ou bloco
  (nível 12), tanto faz: o segundo é pedido de refatoração, não questão
  de estilo (ver [Regra Mensurável de Revisão](#regra-mensurável-de-revisão)).

Hierarquias de "como escolher a estrutura condicional certa" costumam tratar
o `if` como técnica de nível médio, e isso dá espaço demais a ele. Aqui a
hierarquia tem outro objetivo: **cada nível
acima remove a bifurcação do fluxo de leitura**; o `if` só sobrevive no
fim, quando nada mais removeu.

---

## Particularidades do Java 17

O Java 17 tem `record`, `sealed` e `instanceof` com pattern, mas **não
tem** `switch` com pattern matching (só em Java 21; no 17 é *preview* e
não pode ser usado) nem operadores nulos. Isso define quatro pontos:

| Tema | Regra no Java 17 |
|---|---|
| Hierarquia fechada | `sealed interface` + `record`; o compilador conhece as variantes, mas **não** checa `instanceof` encadeado |
| Exaustividade garantida | Visitor (erro de compilação: *não implementa o método abstrato*). `switch` **expressão** sobre `enum` sem `default` também é exaustivo no Java 17 |
| Ausência e nulos | Proibidos: `null` como retorno, `Optional.orElse`, `Optional.get` e o ternário `x != null ? x : y` |
| Coleções | `Stream` (`filter`/`map`/`anyMatch`/`reduce`); o `.forEach()` com efeito que aborta continua proibido (I3) |

> Se o projeto subir para Java 21, o `switch` com pattern sobre `sealed`
> passa a ser exaustivo pelo compilador e o nível 3 (Visitor) deve ser
> reavaliado — a decisão fica registrada aqui, não por deriva silenciosa.

---

## Regra de Ouro

`if`/`switch` solto é sintoma de modelagem incompleta.

**A primeira questão — sempre, antes de qualquer técnica:**

> **"Este `if` está construindo uma CONDICIONAL — fazendo o método
> escolher entre caminhos?"**

Se a resposta for sim, **ele está errado**. Não é questão de estilo,
nem de indentação, nem de "usar com cuidado": condicionar em tempo de
execução é modelagem incompleta. Um `if` condicional empacota no mesmo
arquivo várias razões de mudança (a razão do caminho A, a razão do
caminho B, a razão do fallback…); quando ele é refatorado, cada razão
vira seu próprio arquivo — se o `if` estava ali, o arquivo já tinha mais
de uma razão para mudar e estava incorreto.

A resposta nunca é "escolher uma técnica melhor de condicional": é
**eliminar a bifurcação**. A hierarquia abaixo não ensina a construir
condicionais — são as formas de remover a bifurcação da leitura
(níveis 1 → 10). O que sobra depois de eliminar é resíduo, não técnica
(níveis 11 e 12).

Depois da primeira questão, faça a **pergunta zero**:

> **"Esta variação de comportamento merece um tipo próprio?"**

Na maioria dos casos em que um `if` parece necessário para escolher "qual
regra de cálculo usar por tipo de cartão" ou "qual canal de notificação
acionar", o que está acontecendo é que existe um tipo ainda não criado.
Extrair essa responsabilidade para seu próprio arquivo elimina o `if` — a
decisão passa a estar codificada no sistema de tipos, não no fluxo de
execução.

```java
// ANTES — if mascarando variação de comportamento por tipo de estabelecimento
public BigDecimal valorCobrado(int tipoEstabelecimento, BigDecimal valor) {
    if (tipoEstabelecimento == 1) return valor.multiply(new BigDecimal("0.90"));
    if (tipoEstabelecimento == 2) return valor;
    return valor.multiply(new BigDecimal("1.35"));
}

// DEPOIS — a variação virou dado no próprio tipo; o if desaparece do chamador
// dominio/cartao/TipoCartao.java
public enum TipoCartao {
    ALIMENTACAO(1, "-0.10"),    // desconto de 10%
    FARMACIA(2, "0.00"),        // sem ajuste
    COMBUSTIVEL(3, "0.35");     // acréscimo de 35%

    private final int codigoEstabelecimento;
    private final BigDecimal ajusteDoValor;      // fração com sinal — ver VALORES.md §6.6

    TipoCartao(int codigoEstabelecimento, String ajusteDoValor) {
        this.codigoEstabelecimento = codigoEstabelecimento;
        this.ajusteDoValor = new BigDecimal(ajusteDoValor);
    }

    // não arredonda para centavos: a fração é guardada (VALORES.md §6.2)
    public Dinheiro valorCobrado(Dinheiro valorInformado) {
        return valorInformado.vezes(BigDecimal.ONE.add(ajusteDoValor));
    }
}
```

Quando a variação é de **colaborador** (e não de dado), a escolha de *qual*
adaptador construir acontece **uma única vez**, na composition root (a
camada `api`, ver [`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md)), nunca
repetida em cada caso de uso:

```java
// api/configuracao/ConfiguracaoDeNotificacao.java — composição decide uma única vez
@Bean
NotificacaoPorta notificacaoPorta(PropriedadesDeNotificacao propriedades) {
    // switch EXPRESSÃO sobre enum, sem default: no Java 17 o compilador exige
    // todos os casos — novo canal no enum quebra o build aqui, não em produção
    return switch (propriedades.canal()) {
        case EMAIL -> new AdaptadorEmail(propriedades);
        case SMS -> new AdaptadorSms(propriedades);
    };
}
```

Se depois da pergunta zero a resposta for "não, não justifica um tipo",
consulte a hierarquia abaixo — na ordem em que está. Ela não é uma lista
de alternativas equivalentes: **é a sequência de perguntas na ordem em que
mais eliminam bifurcações da leitura.** Os níveis 1 → 10 **eliminam** a
condicional; os níveis 11 e 12 são o **resíduo**.

---

## Hierarquia de Preferência — 12 Níveis

| # | Técnica | O que elimina da leitura |
|---|---|---|
| 1 | **Mapeamento estático** (enum → dado fixo) | Elimina a lógica: sobra dado puro |
| 2 | **Comportamento mora no tipo** (`enum` com campo/método, `sealed interface` + `record`; Strategy/State) | Elimina a decisão: cada tipo já sabe o que fazer |
| 3 | **Visitor** (despacho duplo) sobre hierarquia `sealed` | Decisão mora fora do tipo; esquecer uma variante é erro de compilação, não aviso |
| 4 | **`Resultado<T, E>` + propagação sem `try`/`catch`** | O erro vira dado que atravessa o fluxo |
| 5 | **Specification/Policy** (regra de negócio com nome e tipo) | Elimina condição inline muda: a regra vira vocabulário do domínio |
| 6 | **Pipeline / cadeia de responsabilidade** | Elimina sequência de `if (tem X)`: viram etapas encadeadas |
| 7 | **`Map`/registro de runtime** | Elimina `else if` de chave dinâmica: viram consulta |
| 8 | **Null Object** | Elimina checagem de ausência: a ausência já tem comportamento |
| 9 | **Comportamento injetado** (`Function`/`Predicate`/`Comparator`/`Supplier`) | Elimina "perguntar e decidir": o comportamento chega pronto |
| 10 | **Validação declarativa** (Bean Validation, `Objects.requireNonNull`) | Elimina guard manual: a pré-condição vira infraestrutura |
| 11 | **Guard clause (early return)** | Resíduo: pré-condição sim/não que a linguagem só expressa com `if` — único ponto, linha reta depois dele |
| 12 | **`if`/`switch` com bloco** | ⚠ Resíduo — ~7% (medição própria). Exige justificativa no diff |

> **Resíduo (níveis 11 e 12)**: os níveis 1 → 10 eliminam a condicional.
> Os níveis 11 e 12 só entram quando a linguagem/compilador exige um `if`
> (uma saída sim/não que nenhuma das 10 técnicas substitui, ou efeitos
> compostos sem outra estrutura) **e** a pergunta zero não justifica um
> arquivo novo — é aí que moram os ~7%.

---

## 1. Mapeamento Estático

### Quando usar

- Enum → valor fixo que nunca muda em runtime.
- A tabela pode ser lida como dado, não como lógica.
- Adicionar um novo caso significa acrescentar uma entrada, não editar código de fluxo.

**Prefira o dado dentro do próprio `enum`**: o construtor obriga cada
constante a informar o valor — esquecer um caso é erro de compilação. Um
`Map.of(...)` externo não tem essa garantia (esquecer uma chave só falha em
runtime) e fica reservado a chaves que **não** são enum.

```java
// CORRETO — dado puro dentro do enum; esquecer o status HTTP não compila
public enum CategoriaErro {
    NAO_ENCONTRADO(404),
    REGRA_DE_NEGOCIO(422),
    CONFLITO(409),
    VALIDACAO(400);

    private final int statusHttp;

    CategoriaErro(int statusHttp) { this.statusHttp = statusHttp; }

    public int statusHttp() { return statusHttp; }
}

// ERRADO — lógica onde só cabe dado; cada `if` é uma pergunta extra para o leitor
public static int statusHttp(CategoriaErro categoria) {
    if (categoria == CategoriaErro.NAO_ENCONTRADO) return 404;
    if (categoria == CategoriaErro.REGRA_DE_NEGOCIO) return 422;
    if (categoria == CategoriaErro.CONFLITO) return 409;
    return 400;
}
```

---

## 2. Comportamento Mora no Tipo

### Quando usar

- Cada variante de um estado/tipo **sabe fazer** as coisas — não recebe
  ordem de um `switch` que sabe o que ela faz.
- A lista de variantes tende a crescer; com polimorfismo, crescer é
  adicionar arquivo — sem `switch` existente para lembrar de atualizar.
- Esta é a aplicação direta da pergunta zero: o `if` é substituído por um
  sistema de tipos que torna a decisão desnecessária.

```java
// CORRETO — State: cada estado implementa seu comportamento; quem chama não decide nada
// dominio/cartao/EstadoDoCartao.java
public sealed interface EstadoDoCartao permits Ativo, Bloqueado {
    Resultado<BigDecimal, ErroDominio> saldoDisponivel(Cartao cartao);
}

// dominio/cartao/Ativo.java
public record Ativo() implements EstadoDoCartao {
    @Override
    public Resultado<BigDecimal, ErroDominio> saldoDisponivel(Cartao cartao) {
        return new Sucesso<>(cartao.saldo());
    }
}

// dominio/cartao/Bloqueado.java
public record Bloqueado(String motivo) implements EstadoDoCartao {
    @Override
    public Resultado<BigDecimal, ErroDominio> saldoDisponivel(Cartao cartao) {
        return new Falha<>(new ErroCartaoBloqueado(cartao.numero(), motivo));
    }
}

// uso — linha reta, sem if, sem switch: o leitor não precisa escolher caminho
var disponivel = cartao.estado().saldoDisponivel(cartao);

// ERRADO — o chamador precisa conhecer todos os estados para poder chamá-lo
public BigDecimal saldoDisponivelErrado(Cartao cartao) {
    if (cartao.estado() instanceof Bloqueado) {
        throw new IllegalStateException("cartão bloqueado");
    }
    return cartao.saldo(); // um terceiro estado exige lembrar de editar este if
}
```

**Por quê**: sob o `if`, o leitor precisa reconstruir mentalmente a matriz
de estados × ações; sob polimorfismo, a resposta está no arquivo do tipo —
cada pergunta respondida no lugar em que ela nasce.

---

## 3. Visitor (Despacho Duplo) sobre Hierarquia `sealed`

### Quando usar

- A variação **é real** (não é estado com comportamento, é forma com
  dados) e a decisão **pertence a quem consome** — formatação,
  mapeamento, serialização — não ao domínio. Colocar isso dentro do tipo
  (nível 2) misturaria responsabilidade de apresentação com
  responsabilidade de domínio.
- A exaustividade precisa ser garantida pelo compilador **de verdade**,
  não por convenção: esquecer uma variante é **erro de compilação**
  (`... is not abstract and does not override abstract method`) em todo
  lugar que implementa a visita — não um aviso que se cala com `default`.
- Regra: **uma decisão por visitor**, e o visitor não tem outra
  responsabilidade.

```java
// CORRETO — Visitor: decisão fora do tipo, exaustividade garantida de verdade.
// Esquecer visitarBloqueado é erro de compilação em todo visitor — não existe
// equivalente a `default` para "método de interface não implementado"
public interface EstadoDoCartaoVisitor<R> {
    R visitarAtivo(Ativo ativo);
    R visitarBloqueado(Bloqueado bloqueado);
}

public sealed interface EstadoDoCartao permits Ativo, Bloqueado {
    <R> R aceitar(EstadoDoCartaoVisitor<R> visitor);
}

public record Ativo() implements EstadoDoCartao {
    @Override
    public <R> R aceitar(EstadoDoCartaoVisitor<R> visitor) { return visitor.visitarAtivo(this); }
}

public record Bloqueado(String motivo) implements EstadoDoCartao {
    @Override
    public <R> R aceitar(EstadoDoCartaoVisitor<R> visitor) { return visitor.visitarBloqueado(this); }
}

// a decisão mora aqui — fora do domínio, mas obrigada a tratar toda variante
public final class DescritorDeEstado implements EstadoDoCartaoVisitor<String> {
    @Override public String visitarAtivo(Ativo ativo) { return "ativo"; }
    @Override public String visitarBloqueado(Bloqueado bloqueado) { return "bloqueado: " + bloqueado.motivo(); }
}

// uso — sem if, sem switch
var descricao = estado.aceitar(new DescritorDeEstado());

// ERRADO — cadeia de instanceof sobre a mesma hierarquia: parece exaustiva, mas
// não é. O compilador nunca comprova que todas as variantes `sealed` foram
// tratadas num instanceof — compila sem aviso mesmo faltando Bloqueado, e continua
// compilando quando uma variante nova (ex.: Cancelado) for adicionada
public static String descreverEstadoErrado(EstadoDoCartao estado) {
    if (estado instanceof Ativo) return "ativo";
    throw new IllegalStateException("estado não tratado"); // compila mesmo sem Bloqueado
}
```

> O ganho do Visitor não é estilo de escrita, é mecânico:
> `DescritorDeEstado` não compila enquanto não implementar os dois métodos
> da interface. Quem precisar de mais de uma decisão sobre a mesma
> hierarquia (ex.: um visitor para exibição, outro para serialização)
> escreve outra classe que implementa a mesma interface — cada uma
> protegida igualmente pelo compilador, sem depender de disciplina do
> autor.

---

## 4. `Resultado<T, E>` + Propagação sem `try`/`catch`

### Quando usar

- Propagação de erro tipado entre funções de domínio/aplicação.
- Conversão entre tipos de erro no ponto de cruzamento de camada (ex.:
  erro de infraestrutura convertido para erro de domínio).
- Substitui `try`/`catch` como controle de fluxo corrente: sobram
  `catch`s apenas onde a API externa realmente lança.

Convenção do projeto (ver `ERROS.md`): toda função falível de domínio
retorna `Resultado<T, E>` explícito; exceção é para falha genuinamente
excepcional na fronteira com infraestrutura, nunca rotina de domínio.

O `Resultado` é uma `sealed interface` com duas variantes `record`. **Cada
operação é um papel em arquivo próprio**, escrito como método `default` em cima
de uma única operação-base, `resolver`: as variantes implementam só ela, e as
demais vivem inteiras em seus arquivos. Assim ninguém precisa editar `Sucesso`
e `Falha` para mudar `mapear`, e a biblioteca em si não contém `if` — cada
variante sabe o que fazer, **por polimorfismo**. Criar um resultado é o
construtor da variante, e converter uma origem de ausência é um tipo à parte:

```java
// dominio/resultado/Resolvivel.java — consumir: a operação-base, as demais se escrevem nela
public interface Resolvivel<T, E> {
    <R> R resolver(Function<? super T, ? extends R> aoSucesso, Function<? super E, ? extends R> aoFalhar);
}

// dominio/resultado/Transformavel.java — transformar o valor, sem poder falhar
public interface Transformavel<T, E> extends Resolvivel<T, E> {
    default <U> Resultado<U, E> mapear(Function<? super T, ? extends U> funcao) {
        return this.<Resultado<U, E>>resolver(valor -> new Sucesso<>(funcao.apply(valor)), erro -> new Falha<>(erro));
    }
}

// dominio/resultado/Encadeavel.java — encadear uma operação que também pode falhar
public interface Encadeavel<T, E> extends Resolvivel<T, E> {
    default <U> Resultado<U, E> encadear(Function<? super T, Resultado<U, E>> funcao) {
        return this.<Resultado<U, E>>resolver(valor -> funcao.apply(valor), erro -> new Falha<>(erro));
    }
}

// dominio/resultado/Resultado.java — só compõe os papéis e fecha as variantes
public sealed interface Resultado<T, E> extends Resolvivel<T, E>, Transformavel<T, E>, Encadeavel<T, E>
    permits Sucesso, Falha { }

// dominio/resultado/Sucesso.java — uma única operação
public record Sucesso<T, E>(T valor) implements Resultado<T, E> {
    @Override public <R> R resolver(Function<? super T, ? extends R> aoSucesso, Function<? super E, ? extends R> aoFalhar) {
        return aoSucesso.apply(valor);
    }
}

// dominio/resultado/Falha.java — uma única operação
public record Falha<T, E>(E erro) implements Resultado<T, E> {
    @Override public <R> R resolver(Function<? super T, ? extends R> aoSucesso, Function<? super E, ? extends R> aoFalhar) {
        return aoFalhar.apply(erro);
    }
}

// dominio/resultado/AusenciaComoErro.java — ausência vira erro tipado na fronteira; nenhum null atravessa daqui
public final class AusenciaComoErro {
    private AusenciaComoErro() { }

    public static <T, E> Resultado<T, E> converter(Optional<T> opcional, Supplier<E> erroSeAusente) {
        return opcional.<Resultado<T, E>>map(valor -> new Sucesso<>(valor))
                       .orElseGet(() -> new Falha<>(erroSeAusente.get()));
    }
}
```

> **Por que não há `Resultado.sucesso(...)` nem `Resultado.falha(...)`.** Uma
> fábrica estática dentro da interface é uma terceira razão de mudar (criar) no
> arquivo que já define o contrato. O construtor da variante, `new Sucesso<>(v)`
> e `new Falha<>(e)`, faz o mesmo sem nada a mais.

```java
// CORRETO — propagação explícita, linha reta: cada etapa só roda se a anterior teve sucesso
public Resultado<DtoComprovante, ErroDominio> registrar(DtoCompra compra) {
    return TipoCartaoDoEstabelecimento.converter(compra.tipoEstabelecimento())
        .encadear(tipo -> cartaoPorta.buscarPorNumeroETipoParaAtualizacao(compra.numeroCartao(), tipo)
            .encadear(cartao -> cartao.debitar(tipo.valorCobrado(compra.valor())))
            .mapear(cartaoDebitado -> concluir(cartaoDebitado, compra)));
}

// ERRADO — exceção genérica como controle de fluxo de domínio corrente:
// o leitor precisa rastrear try/catch espalhados para entender o fluxo
public DtoComprovante registrarErrado(DtoCompra compra) {
    try {
        var cartao = cartaoPorta.buscarPorNumeroOuLancar(compra.numeroCartao());
        cartao.debitarOuLancar(compra.valor());
        return concluir(cartao, compra);
    } catch (Exception e) {
        throw new RuntimeException("erro", e); // não diz qual etapa falhou, nem por quê
    }
}
```

**Regra**: um `catch` só é justificado na fronteira com API que
genuinamente lança (JPA, cliente HTTP, sistema de arquivos) e só quando
**converte** para `Resultado<T, E>` — nunca re-lançamento vazio que só
adiciona camada sem informação nova (ver `ERROS.md`).

---

## 5. Specification / Policy

### Quando usar

- Regra de negócio que aparece **inline** em mais de um lugar, ou numa
  condição longa com `&&`/`||` misturado.
- A condição tem nome no domínio ("saldo suficiente", "cartão do tipo do
  estabelecimento", "dentro do limite") — se você consegue nomeá-la, ela
  merece um tipo.

```java
// CORRETO — a regra vira vocabulário: o leitor lê o nome, não reconstrói a fórmula
// dominio/cartao/regras/RegraDeCompra.java
@FunctionalInterface
public interface RegraDeCompra {
    boolean atende(Cartao cartao, BigDecimal valor);

    default RegraDeCompra e(RegraDeCompra outra) {
        return (cartao, valor) -> atende(cartao, valor) && outra.atende(cartao, valor);
    }
}

// dominio/cartao/regras/SaldoSuficiente.java
public final class SaldoSuficiente implements RegraDeCompra {
    @Override public boolean atende(Cartao cartao, BigDecimal valor) {
        return cartao.saldo().compareTo(valor) >= 0;
    }
}

// dominio/cartao/regras/CartaoAtivo.java
public final class CartaoAtivo implements RegraDeCompra {
    @Override public boolean atende(Cartao cartao, BigDecimal valor) {
        return cartao.estado() instanceof Ativo;
    }
}

// uso — lê-se como frase, não como fórmula; o único `if` restante é um guard
// (nível 11) cuja condição lê como frase de domínio
var regra = new CartaoAtivo().e(new SaldoSuficiente());
if (!regra.atende(cartao, valorCobrado))
    return new Falha<>(new ErroCompraNaoPermitida(cartao.numero()));

// ERRADO — condição muda inline; quem lê precisa decodificar a fórmula toda vez,
// e a mesma fórmula reaparece copiada em outro caso de uso
if (cartao.saldo().compareTo(valor) >= 0 && cartao.estado() instanceof Ativo
    && cartao.numero() != null && !cartao.emAnaliseAntifraude()) { /* ... */ }
```

**Por quê**: a condição inline obriga o leitor a *reescrever* a regra na
cabeça a cada leitura; a specification obriga apenas a *ler* o nome. E
quando a regra muda, ela muda em um arquivo — não em todos os `if` que a
duplicaram.

---

## 6. Pipeline / Cadeia de Responsabilidade

### Quando usar

- Sequência do tipo "se tem X, faça Y; depois se tem Z, faça W" — típico
  de fluxo de compra, moderação, exportação.
- Cada etapa é uma ação com pré-condição própria; a ordem importa.

```java
// CORRETO — cada etapa é um tipo; a leitura é uma lista, não um labirinto
public interface EtapaDeCompra {
    Resultado<ContextoDeCompra, ErroDominio> aplicar(ContextoDeCompra contexto);
}

// aplicacao/pipeline/PipelineDeCompra.java — percorre as etapas; o único ponto de propagação
public final class PipelineDeCompra {
    private final List<EtapaDeCompra> etapas;

    public PipelineDeCompra(List<EtapaDeCompra> etapas) { this.etapas = List.copyOf(etapas); }

    public Resultado<ContextoDeCompra, ErroDominio> executar(ContextoDeCompra inicial) {
        Resultado<ContextoDeCompra, ErroDominio> atual = new Sucesso<>(inicial);
        for (EtapaDeCompra etapa : etapas) {
            atual = atual.encadear(etapa::aplicar); // Falha curto-circuita sem if
        }
        return atual;
    }
}

// ordem declarada uma única vez, na composition root — legível como lista
@Bean
PipelineDeCompra pipelineDeCompra(ValidarTipoDoCartao validarTipo,
                                  ReservarSaldo reservarSaldo,
                                  RegistrarExtrato registrarExtrato) {
    return new PipelineDeCompra(List.of(validarTipo, reservarSaldo, registrarExtrato));
}

// ERRADO — cada etapa escondida num if; o leitor precisa executar o método
// mentalmente para descobrir a ordem e o que acontece quando algo falha
if (validarTipo) { /* ... */ }
if (cartao.saldo().compareTo(valor) >= 0) { /* ... */ }
if (config.registrarExtrato()) { /* ... */ }
```

---

## 7. `Map` / Registro de Runtime

### Quando usar

- Chave dinâmica (não fixa em tempo de compilação) seleciona ação ou dado.
- Diferente do nível 1: a chave não é um enum consultado por valor fixo,
  é um identificador (slug, extensão, código recebido na requisição).

```java
// CORRETO — busca por chave dinâmica; adicionar formato = novo bean, não novo if
// o null de Map.get é convertido em erro tipado AQUI, na fronteira do registro —
// nenhum null atravessa para o chamador
public final class RegistroDeExportadores {
    private final Map<String, ExportadorPorta> porExtensao;

    public RegistroDeExportadores(List<ExportadorPorta> exportadores) {
        this.porExtensao = exportadores.stream()
            .collect(Collectors.toUnmodifiableMap(ExportadorPorta::extensao, Function.identity()));
    }

    public Resultado<ExportadorPorta, ErroDominio> buscar(String extensao) {
        return AusenciaComoErro.converter(
            Optional.ofNullable(porExtensao.get(extensao)),
            () -> new ErroFormatoNaoSuportado(extensao));
    }
}

// ERRADO — else if de chave dinâmica; cada formato novo edita este método
public ExportadorPorta buscarErrado(String extensao) {
    if (extensao.equals("pdf")) return new ExportadorPdf();
    if (extensao.equals("csv")) return new ExportadorCsv();
    throw new UnsupportedOperationException(extensao);
}
```

---

## 8. Null Object

### Quando usar

- A ausência de valor precisa de **comportamento padrão**, não de
  checagem repetida em cada uso.
- Se `if (x == null)` aparece em mais de um lugar para a mesma ausência, é
  Null Object faltando.

```java
// CORRETO — a ausência já sabe se comportar; zero checagem espalhada
public interface LimiteDeCredito {
    BigDecimal disponivel();
}
public record LimiteDefinido(BigDecimal valor) implements LimiteDeCredito {
    @Override public BigDecimal disponivel() { return valor; }
}
public record SemLimite() implements LimiteDeCredito {
    @Override public BigDecimal disponivel() { return BigDecimal.ZERO; }
}

// uso — linha reta, sem if
var disponivel = consumidor.limite().disponivel();

// ERRADO — cada chamador repete a mesma bifurcação
var disponivel = consumidor.limite() != null
    ? consumidor.limite().disponivel()
    : BigDecimal.ZERO;
```

---

## 9. Comportamento Injetado (`Function`, `Predicate`, `Comparator`, `Supplier`)

### Quando usar

- O código pergunta "qual forma de X devo usar?" e a resposta vem de
  fora (configuração, caso de uso, preferência do usuário).
- Em vez de decidir no lugar, **receba o comportamento pronto por
  parâmetro** — "tell, don't ask".

```java
// CORRETO — quem chama decide uma vez, fora do método; o método não pergunta nada
public List<Consumidor> ordenar(Collection<Consumidor> consumidores, Comparator<Consumidor> criterio) {
    return consumidores.stream().sorted(criterio).toList();
}

// na camada api — a escolha vive em um lugar só; `ordem` é um enum validado na
// borda (nível 10), então só valores conhecidos chegam aqui — sem default silencioso
var criterio = switch (ordem) {
    case NOME -> Comparator.comparing(Consumidor::nome);
    case NASCIMENTO -> Comparator.comparing(Consumidor::dataNascimento);
};
var resultado = ordenador.ordenar(consumidores, criterio);

// ERRADO — o método pergunta, e cada pergunta nova edita o método
public List<Consumidor> ordenarErrado(Collection<Consumidor> consumidores, String ordem) {
    if (ordem.equals("nome")) return consumidores.stream().sorted(Comparator.comparing(Consumidor::nome)).toList();
    if (ordem.equals("nascimento")) return consumidores.stream().sorted(Comparator.comparing(Consumidor::dataNascimento)).toList();
    return List.copyOf(consumidores);
}
```

---

## 10. Validação Declarativa

### Quando usar

- Pré-condição técnica de entrada: não-nulo, faixa, formato.
- Em Java, a maioria dos guards manuais é infraestrutura repetida — use
  **Bean Validation** (`jakarta`/`javax.validation`, já presente via
  `spring-boot-starter-validation`) e `Objects.requireNonNull` para
  invariante de construtor.

```java
// CORRETO — pré-condição declarada, não ramificada
public record MensagemRegistrarCompra(
    @NotNull Integer tipoEstabelecimento,
    @NotBlank String nomeEstabelecimento,
    @NotBlank String numeroCartao,
    @NotBlank String descricaoProduto,
    @NotNull @Positive @Digits(integer = 15, fraction = 2) BigDecimal valor) { }

@PostMapping("/compras")
ResponseEntity<?> comprar(@Valid @RequestBody MensagemRegistrarCompra mensagem) { /* ... */ }

// invariante de construtor — a checagem de nulo é infraestrutura do JDK, não nosso if
public Cartao(Long id, TipoCartao tipo, String numero, BigDecimal saldo) {
    this.tipo = Objects.requireNonNull(tipo);
    this.numero = Objects.requireNonNull(numero);
    // ...
}

// ERRADO — guard manual repetido em todo método que recebe a mesma entrada
public void comprarErrado(MensagemRegistrarCompra m) {
    if (m.tipoEstabelecimento() == null) throw new IllegalArgumentException("tipo");
    if (m.numeroCartao() == null || m.numeroCartao().isBlank()) throw new IllegalArgumentException("cartão");
    if (m.valor() == null || m.valor().signum() <= 0) throw new IllegalArgumentException("valor");
}
```

---

## 11. Guard Clause (Early Return) — resíduo de pré-condição sim/não

### Quando usar

- Pré-condição de **domínio** que não tem expressão em tipo nem em
  validador, e a falha encerra o método — e a linguagem não tem outra
  forma de expressar essa saída: é o caso em que o compilador exige o
  `if`. A primeira questão responde "não": o método não escolhe caminho
  — ele encerra na pré-condição e segue em linha reta.
- **Um ponto, depois linha reta.** **Teto único: um condicional por
  método** — o guard é *o* condicional do método; não existe "vaga" para
  um segundo `if` nem para um bloco de nível 12 ao lado. Se a
  pré-condição exige dois `if`s, volte ao nível 2 (comportamento mora no
  tipo) ou 4 (erro tipado) — dois guards seguidos já são modelagem
  incompleta. E nunca `else` após guard.

```java
// CORRETO — o ÚNICO condicional do método é um guard no topo; depois, linha reta
// (valor positivo é pré-condição de entrada: validação na borda, nível 10 — não este método)
public Resultado<Cartao, ErroDominio> debitar(BigDecimal valorCobrado) {
    if (saldo.compareTo(valorCobrado) < 0)
        return new Falha<>(new ErroSaldoInsuficiente(numero, saldo, valorCobrado));

    // fluxo principal: sem aninhamento, sem distração
    return new Sucesso<>(comSaldo(saldo.subtract(valorCobrado)));
}

// ERRADO — lógica principal aninhada dentro de ifs; o leitor precisa segurar
// 4 caminhos na cabeça antes de entender a linha que realmente importa
public Resultado<Cartao, ErroDominio> debitarErrado(BigDecimal valorCobrado) {
    if (valorCobrado != null) {
        if (valorCobrado.signum() > 0) {
            if (saldo.compareTo(valorCobrado) >= 0) {
                return new Sucesso<>(comSaldo(saldo.subtract(valorCobrado)));
            } else {
                return new Falha<>(new ErroSaldoInsuficiente(numero, saldo, valorCobrado));
            }
        } else {
            return new Falha<>(new ErroValorInvalido(valorCobrado));
        }
    } else {
        return new Falha<>(new ErroValorInvalido(null));
    }
}
```

---

## 12. `if`/`switch` com Bloco — ⚠ Resíduo (~7%, medição própria)

Se você chegou até aqui, **pare e reavalie**. Antes de escrever `if`/`switch`
solto, responda:

- Enum → dado fixo sem lógica? → nível 1
- O tipo pode saber fazer isso (State/Strategy)? → nível 2
- É decisão de quem consome (não do tipo), com exaustividade garantida de verdade? → nível 3 (Visitor)
- É propagação de erro tipado? → nível 4
- A condição tem nome de regra de domínio? → nível 5
- É sequência de etapas "se tem X, faça Y"? → nível 6
- É chave dinâmica selecionando dado/ação? → nível 7
- É ausência com comportamento padrão? → nível 8
- Dá para injetar o comportamento pronto? → nível 9
- É pré-condição técnica de entrada? → nível 10
- É pré-condição de domínio com falha rápida? → nível 11 (resíduo)

Se **nenhuma** das alternativas acima se aplica — nem a eliminação
(níveis 1 → 10) resolve, nem um guard é suficiente (nível 11) — **e** a
linguagem exige o `if` (um bloco de efeitos compostos sem outra
estrutura possível), **e** a pergunta zero respondeu "não, não justifica
um arquivo novo", o `if` é admitido como resíduo: é aqui que moram os
~7% (medição própria, ver o início do documento). Anote no PR qual nível
falhou e por que a linguagem exigiu o `if`.

```java
// Nível 12 — a condição NÃO é regra de negócio: é checagem de ambiente,
// local a este método, sem nome de domínio (nível 5 não se aplica); pergunta
// zero: não justifica arquivo novo só para isto; bloco de efeitos compostos
// que a linguagem exige; único condicional do método
if (ambiente.ehDesenvolvimento()) {
    log.debug("compra concluída — payload completo para diagnóstico: {}", compra);
    instrumentacao.registrarEvento("compra.concluida.debug", compra.numeroCartao());
}
```

Se **qualquer** `if`/`switch` — guard ou bloco — aparecer **mais de uma
vez** no mesmo método, ou na **maioria** dos métodos de um arquivo, é
sinal de refatoração — **não de estilo, mas de modelagem.**

---

## Árvore de Decisão Rápida

```text
PRIMEIRA QUESTÃO
Este if está construindo uma CONDICIONAL — fazendo o método escolher
entre caminhos?
→ Sim: está errado (modelagem incompleta) — elimine com os níveis 1 → 10

PERGUNTA ZERO
Esta variação (ex.: por tipo de cartão, por estado, por canal) merece um
tipo próprio?
→ Sim: extraia para novo arquivo/interface/record/enum — o if desaparece

CONDICIONAIS (se a pergunta zero respondeu não)

Enum → dado fixo sem lógica?                                  → 1  Mapeamento estático (dado no enum)
O tipo pode saber fazer? (State/Strategy)                     → 2  Comportamento no tipo
Decisão de quem consome (não do tipo), exaustividade real?    → 3  Visitor / despacho duplo
Propagação de erro entre funções de domínio?                  → 4  Resultado<T, E> + encadear/mapear
A condição tem nome de regra de domínio?                      → 5  Specification/Policy
Sequência de etapas "se tem X, faça Y"?                       → 6  Pipeline / cadeia
Chave dinâmica (código, extensão)?                            → 7  Map/registro de runtime
Ausência com comportamento padrão?                            → 8  Null Object
Dá para receber o comportamento pronto?                       → 9  Function/Comparator/Supplier
Pré-condição técnica de entrada?                              → 10 Validação declarativa
Pré-condição de domínio com falha rápida?                     → 11 Guard clause — resíduo
Side-effect composto sem alternativa — e raramente?           → 12 if/switch com bloco ⚠ — resíduo

ITERAÇÃO (coleções e processamento em lote)

Filtrar / projetar / verificar sobre coleção?                 → stream().filter()/map()/anyMatch()
Agregar / contar / somar?                                     → reduce()/count()/mapToInt().sum()
Side-effect por item (persistir, logar, notificar)?           → for (var item : colecao)
Side-effect que precisa abortar no meio?                      → for + retorno tipado
Offset/índice participa do cálculo (parse de stream)?         → acesso indexado direto — exceção estrita
```

---

## PARTE 2 — Iteração

A mesma tese vale para laços: **laço é o `if` da repetição** — obriga o
leitor a simular repetições na cabeça. Preferência por operações que
descrevem o *resultado* em vez de simular o *processo*.

### Hierarquia de Preferência

| # | Técnica | Quando usar |
|---|---|---|
| 1 | **Stream (`filter`/`map`/`anyMatch`/`noneMatch`/`flatMap`)** | Projeção, filtragem, verificação sobre coleção |
| 2 | **Agregação (`reduce`/`count`/`mapToInt().sum()`/`Collectors`)** | Cálculo e redução |
| 3 | **`for` aprimorado (`for (var x : colecao)`)** | Side-effects necessários por item (persistir, logar, notificar) |
| 4 | **Acesso indexado direto** | O índice participa da estrutura do cálculo (ex.: parse de offset em stream de upload) — exceção estrita, não norma |

### I1. Projeção, Filtragem e Verificação

```java
// CORRETO
public List<Cartao> cartoesDoTipo(Collection<Cartao> cartoes, TipoCartao tipo) {
    return cartoes.stream().filter(c -> c.tipo() == tipo).toList();
}

public boolean possuiCartaoAtivo(Collection<Cartao> cartoes) {
    return cartoes.stream().anyMatch(c -> c.estado() instanceof Ativo);
}
```

### I2. Agregação

```java
// CORRETO — dinheiro é BigDecimal, nunca double: reduce com identidade ZERO
public BigDecimal saldoTotal(Collection<Cartao> cartoes) {
    return cartoes.stream().map(Cartao::saldo).reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

### I3. Side-Effect por Item

O `if` não entra nem aqui. Side-effect com aborto no meio tem duas partes
— **o laço** e **a propagação** — e elas não precisam morar na mesma
função: o laço fica centralizado em um único lugar (o adaptador de
persistência), e o caso de uso recebe o resultado pronto, sem nenhuma
bifurcação.

```java
// CORRETO — caso de uso: nenhuma bifurcação, o laço e sua propagação moram no adaptador
public Resultado<Unit, ErroDominio> persistirExtratos(List<Extrato> extratos, ExtratoPorta porta) {
    return porta.salvarEmLote(extratos);
}

// implementação — o ÚNICO ramo é o nível 4 (propagação de um Resultado),
// em um só arquivo, um só ponto, sem else
public Resultado<Unit, ErroDominio> salvarEmLote(List<Extrato> extratos) {
    for (Extrato extrato : extratos) {
        Resultado<Unit, ErroDominio> resultado = salvar(extrato);
        if (resultado instanceof Falha<Unit, ErroDominio> falha)
            return falha;
    }
    return new Sucesso<>(Unit.INSTANCIA);
}

// ERRADO — .forEach() com lambda: não consegue abortar com retorno tipado, e
// qualquer falha vira exceção não verificada engolida pela lambda
public void persistirErrado(List<Extrato> extratos, ExtratoPorta porta) {
    extratos.forEach(extrato -> porta.salvarOuLancar(extrato));
}
```

O `if` da implementação é a forma reduzida do nível 4 — não uma decisão
nova: um ponto, um braço, sem `else`. Se esse método ganhar um segundo
ramo, não é o `for` que está errado — é modelagem: ou a propagação vira um
auxiliar genérico de biblioteca (propaga `Resultado<T, E>` sobre
`Iterable<T>`), ou o processamento vira pipeline (nível 6).

`Iterable.forEach` e `Stream.forEach` aceitam apenas `Consumer` — não
permitem `return` do método, não propagam exceção verificada e escondem a
ordem de efeitos. Sempre que houver side-effect, use `for` explícito.
`Stream.peek` com side-effect é proibido (existe só para depuração).

### I4. Acesso Indexado — Exceção Estrita

Índice explícito em código de aplicação é sinal de que `Stream` resolveria
melhor. A exceção é quando o índice **participa da estrutura do
cálculo** — por exemplo, parser que anda por offsets em um `ByteBuffer`:

```java
// CORRETO — offset participa da estrutura do parse, não é substituível por Stream
private static List<Bloco> dividirEmBlocos(ByteBuffer conteudo) {
    var blocos = new ArrayList<Bloco>();
    var offset = 0;
    while (offset < conteudo.limit() - Cabecalho.TAMANHO) {
        var tamanho = conteudo.getInt(offset);
        blocos.add(new Bloco(offset, tamanho));
        offset += Cabecalho.TAMANHO + tamanho;
    }
    return List.copyOf(blocos);
}
```

O que continua proibido: índice quando a operação é puramente consulta
sobre coleção de alto nível (ver I1) — a exceção é estritamente para o
laço estruturalmente indexado.

---

## Regra Mensurável de Revisão

Regra contável, aplicada por quem revisa — não depende de gosto:

1. **Teto por método: um único condicional** — qualquer `if`/`switch`
   conta, guard (nível 11) ou bloco (nível 12). Dois, no diff, é pedido
   de refatoração. Não há vaga extra: guard não abre espaço para um
   segundo `if`, nem para um bloco ao lado.
2. **Todo `if` novo identifica o nível da hierarquia** que foi tentado e
   por que falhou — e, quando é resíduo (níveis 11/12), por que a
   linguagem exige o `if` e por que a pergunta zero não justificou um
   arquivo novo. A justificativa vai na descrição do PR. Sem
   justificativa, o `if` não entra.
3. **Padrão de repetição é recusa imediata**: o mesmo `if` em dois
   arquivos, ou `else if` encadeado com 3+ ramos, não vai a `main` —
   refatora-se primeiro (níveis 1, 2, 5 ou 7 conforme o caso).
4. **Contagem por arquivo**: `if`/`switch` com bloco em maioria dos
   métodos de um arquivo = o arquivo tem uma responsabilidade que ainda
   não virou tipo.

---

## Proibições Absolutas

- **`else if` encadeado com 3+ ramos** para variante que tem
  comportamento próprio → nível 2 (tipo); para chave dinâmica → nível 7
  (registro); para enum sem lógica → nível 1 (dado no enum).
- **`else` após guard clause** — o guard já retornou; `else` aqui é o
  aninhamento disfarçado. Reescreva com linha reta (nível 11).
- **`null` como retorno, argumento de domínio ou fallback**, e todo
  operador ou método que o esconde — **ausência é modelada, não operada;
  tudo deve ser explícito.** No Java isso inclui:
  - método de domínio que **retorna `null`**;
  - `Optional.get()` (lança sem sinal) e **`Optional.orElse(...)`/
    `orElseGet(...)` com fallback silencioso** — trocam a ausência por um
    valor inventado, sem que o validador, o erro ou o teste enxerguem;
  - ternário `x != null ? x : y` e `Objects.requireNonNullElse(...)`;
  - `Optional` como campo, parâmetro de método ou atributo de entidade.

  A ausência vira **Null Object** (nível 8), **`Resultado<T, E>`**
  (nível 4, via `AusenciaComoErro.converter` na fronteira), **comportamento/
  padrão nomeado no tipo** (nível 2) ou **validação na borda** (nível 10)
  — fallback nomeado mora na construção do valor, visível e testado, não
  escondido em cada uso. `Optional` só existe como **retorno de porta de
  consulta**, convertido para `Resultado` na primeira chamada.
- **Condição inline com `&&`/`||` de 3+ operandos representando regra de
  negócio** → nível 5 (Specification). *(Limiar de convenção do projeto, não
  medição: com 2 operandos a condição ainda se lê de uma vez; a partir de 3,
  o leitor precisa computar precedência de `&&`/`||` de cabeça para saber o
  que a condição realmente afirma — ponto em que o custo de ler supera o
  custo de extrair a regra.)*
- **`default` silencioso em `switch` de domínio** sem justificativa
  documentada — num `switch` expressão sobre `enum`, o `default` **desliga**
  a checagem de exaustividade do compilador; sem `default`, esquecer uma
  constante não compila.
- **`switch` statement (com `case X:` e `break`)** — é sujeito a
  *fall-through* e não é exaustivo. Use `switch` **expressão** (`->`).
- **Cadeia de `instanceof` tratada como garantia de exaustividade** sobre
  hierarquia `sealed` — não é: o compilador nunca comprova isso para
  `instanceof`, com ou sem `else`. Quem precisa de exaustividade garantida
  de verdade usa nível 3 (Visitor).
- **Ternário aninhado (3+ níveis)** → `switch` expressão ou tipo dedicado.
- **Negações duplas** (`!!x`, `x == false`, `!(a == b)`) — inverta para
  afirmação positiva (ver [Negações Duplas](#negações-duplas)).
- **`throw new RuntimeException()`/`throw new Exception()` genérica ou
  `catch (Exception)` que só re-lança** sem converter para
  `Resultado<T, E>` → seção 4 e `ERROS.md`. Também proibido: `catch`
  vazio e `@SneakyThrows`.
- **`if`/`switch` para decidir qual adaptador usar fora do ponto único de
  composição** — a escolha acontece uma única vez na composition root
  (`@Configuration`/`@Bean` da camada `api`); duplicá-la é a duplicação que
  este documento existe para eliminar.
- **`.forEach()`/`Stream.forEach` com side-effect que aborta no meio** e
  **`Stream.peek` com side-effect** → I3.
- **Acesso indexado fora do laço estruturalmente indexado** quando `Stream`
  resolve com mais clareza → I4.

---

## Negações Duplas

Nunca escreva condições que exijam leitura dupla de negação. Inverta a
condição para afirmação positiva — mesma lógica de todo o documento:
menos passos de interpretação para o leitor.

```java
// CORRETO — cada `if` é o único condicional do seu método (dois métodos distintos)
if (titulo.isBlank()) return Erro.TITULO_OBRIGATORIO;
if (!usuarioEstaAutenticado) return Erro.NAO_AUTENTICADO;

// ERRADO — negação dupla
if (!!titulo.isEmpty()) { /* ... */ }
if (!(usuarioEstaAutenticado == false)) { /* ... */ }
```

---

## Checklist de Controle de Fluxo

- [ ] A primeira questão foi respondida: o `if` não está construindo uma condicional?
- [ ] A pergunta zero foi feita antes de qualquer `if`/`switch`?
- [ ] A hierarquia 1 → 10 foi percorrida nesta ordem, e o nível que falhou está justificado?
- [ ] Se o `if` é resíduo (11/12), o PR justifica por que a linguagem exige e por que não há arquivo novo?
- [ ] O método tem **no máximo um** condicional (guard ou bloco)?
- [ ] Mapeamentos enum → dado vivem no próprio `enum`, sem lógica condicional?
- [ ] Estado/comportamento varia por tipo (State/Strategy), não por `switch`/`instanceof` no chamador?
- [ ] `switch` de domínio é **expressão** sobre `enum` e **sem `default`** não-justificado?
- [ ] Regra de negócio com nome virou Specification/Policy, não condição inline?
- [ ] Sequência de etapas virou pipeline/cadeia, não `if` encadeado?
- [ ] Chave dinâmica consulta registro, não `else if`?
- [ ] Ausência com comportamento padrão virou Null Object?
- [ ] Ausência é modelada (Null Object, `Resultado`, padrão no tipo, validação na borda) — nenhum `return null`, `Optional.get`, `orElse` ou ternário de null?
- [ ] Comportamento variável chega pronto por parâmetro (`Function`/`Comparator`/`Supplier`)?
- [ ] Pré-condição técnica usa primitivas declarativas (Bean Validation, `Objects.requireNonNull`)?
- [ ] Propagação de erro usa `Resultado<T, E>`, não exceção corrente?
- [ ] A escolha de adaptador passa exclusivamente pela composition root?
- [ ] Consulta sobre coleção usa `Stream`, não loop manual com acumulador?
- [ ] `for` explícito para side-effect (não `.forEach()` com lambda)?
- [ ] Acesso indexado restrito ao laço estruturalmente indexado?
- [ ] Dinheiro em `BigDecimal`, nunca `double`?

---

**[Documentação de Código →](DOCUMENTACAO.md)**
