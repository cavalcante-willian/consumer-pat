[← Índice](../../PADRONIZACAO_INDEX.md) · [← CONDICIONAIS.md](CONDICIONAIS.md)

# Valores, Tipos e Temporalidade — Java 17

> **Escopo**: o princípio — tipo é o único contrato que o compilador faz
> cumprir sozinho — e as regras para valores no **Java 17**. Três pontos
> concentram o documento: os tipos de data e hora (`Instant`, `LocalDate`,
> `ZonedDateTime`), a **seção 6 (dinheiro)**, que é o assunto central, e o
> fato de o Java não ter um tipo decimal primitivo — o equivalente é
> `BigDecimal`, com armadilhas próprias. Ver
> [Particularidades do Java 17](#particularidades-do-java-17).
>
> **Nota sobre exemplos**: os trechos de código abaixo são ilustrativos e
> **não foram executados** — a regra normativa é o texto conceitual, não os
> nomes exatos exibidos.
>
> **Documentos relacionados**: [`BANCO.md`](../qualidade/BANCO.md) e
> [`VALIDACAO.md`](../qualidade/VALIDACAO.md).

## Sobre Este Documento

Define **unidades canônicas do domínio**, como representá-las em tipos para
que confusão de unidade seja — sempre que possível — **erro de compilação**
e não bug em produção, e a política de relógio/tempo do projeto (dois
propósitos, dois relógios).

**Quando consultar**: ao introduzir qualquer valor numérico novo no domínio
(**dinheiro**, percentual, contagem), qualquer data/hora, qualquer ID,
qualquer coleção exposta ou qualquer valor que possa estar ausente — e ao
revisar assinaturas que recebem `double`, `Date` ou `long` "solto".

---

## Particularidades do Java 17

| Tema | Regra no Java 17 |
|---|---|
| Decimal exato | **`BigDecimal`** — precisão arbitrária, mas com **escala** própria que precisa ser governada (seção 6) |
| Comparar decimais | **`compareTo`**, nunca `equals`: `new BigDecimal("2.0").equals(new BigDecimal("2.00"))` é `false` |
| Dividir decimais | **`divide` sem contexto lança `ArithmeticException`** em dízima (1/3); exige `RoundingMode`/`MathContext` |
| Data com fuso | `ZonedDateTime` (carrega o **fuso IANA**) |
| Só o instante | `Instant` |
| Data civil | `LocalDate` |
| Data ambígua | `java.util.Date` e `LocalDateTime` no domínio — **proibidos** |
| Duração | `System.nanoTime()` |
| Tipo de valor | `record` — **o construtor canônico não pode ser mais restrito que o tipo** (seção 2) |
| Coleção só-leitura | `List.copyOf(...)` já é imutável de verdade; `Collections.unmodifiableList` é só *view* |
| Ausência | sem `null` no domínio (não há `T?` em Java; ver [`CONDICIONAIS.md`](CONDICIONAIS.md)) |

---

## Princípio

Tipo é o único contrato que o compilador faz cumprir sozinho. Um bug de
unidade — somar **reais** com **centavos**, tratar `7` como 7% quando queria
`0.07` — é do tipo mais traiçoeiro que existe: **passa por todo teste**,
porque os dois valores são o mesmo `double`/`BigDecimal`, e só se revela em
produção, quando o número ficou grande demais ou pequeno demais para ser
notado.

É também um custo de leitura, na mesma linha de [`CONDICIONAIS.md`](CONDICIONAIS.md):
uma assinatura `registrar(Date data, double valor)` obriga o leitor a
investigar o corpo, o arquivo, quem chamou — até descobrir que `valor` é em
reais e a `Date` é local. A assinatura
`registrar(Instant instante, Dinheiro valor)` **responde na assinatura**.

Regras cobradas em revisão:

- Primitivo "solto" só quando **a unidade já está no nome do parâmetro** e o
  valor é local a uma função.
- Valor que **cruza camadas** e carrega invariante vira tipo de valor
  dedicado — a invariante é validada em um ponto de criação, não espalhada
  por `if` em todos os usadores.
- Medida de **duração** usa relógio monotônico; **carimbo** usa UTC. Os dois
  nunca trocam de lugar (ver seção 5).

```java
// ERRADO — unidade implícita; o compilador não vê diferença entre real e centavo,
// nem entre fração e percentual
public void registrar(Date data, double valor, double percentual);

// CORRETO — unidade e papel na assinatura; confusão vira erro de compilação
public void registrar(Instant instante, Dinheiro valor, BigDecimal ajusteFracionario);
```

---

## Árvore de Decisão: Representar um Valor

```text
Preciso representar um valor
│
├─ É data/hora?
│   ├─ É um instante (compra, criação, atualização)? ─────> Instant, em UTC (§1)
│   ├─ A regra depende do fuso (agendamento, "é de hoje")? > ZonedDateTime com ZoneId (§1)
│   ├─ É uma data civil sem hora (nascimento)? ───────────> LocalDate (§1)
│   └─ É medir QUANTO TEMPO durou? ───────────────────────> System.nanoTime() (§5)
│
├─ É dinheiro? ──────────────────────────────────────────> Dinheiro (BigDecimal por dentro) (§6) — double/float proibido
├─ É percentual/fator? ──────────────────────────────────> BigDecimal em FRAÇÃO (0.10 = 10%) (§6)
├─ É unidade do domínio (contagem, quantidade)?
│   ├─ Cruza camadas / carrega invariante? ──────────────> tipo de valor dedicado (§2)
│   └─ Local a uma função? ──────────────────────────────> primitivo + unidade no nome do parâmetro (§2)
├─ É ID de agregado? ────────────────────────────────────> record nomeado (§3)
├─ Pode ausentar de verdade? ────────────────────────────> Resultado / Null Object (§8) — nunca null, 0, -1, ""
├─ É coleção exposta em tipo público? ───────────────────> List.copyOf(...) imutável (§7)
└─ É limite/threshold usado em +1 lugar? ────────────────> constante nomeada + doc da decisão (§10)
```

---

## 1. Datas e Hora Sempre em UTC

Todas as datas e horas armazenadas no banco e manipuladas no domínio são em
**UTC**. A conversão para o fuso do usuário acontece **apenas** na camada de
apresentação.

| Situação | Tipo Java | Exemplo neste projeto |
|---|---|---|
| Só o instante importa (compra, criação, atualização) | **`Instant`** | `dataCompra` no extrato; `dataCriacao` |
| A regra depende do fuso (agendamento, expiração, "hoje" para o usuário) | **`ZonedDateTime`** com `ZoneId` IANA (`America/Sao_Paulo`) | — (não há hoje) |
| Data civil, sem hora nem fuso | **`LocalDate`** | `dataNascimento` |
| Exibição ao usuário | conversão na apresentação | nunca persistido assim |

```java
// CORRETO — instante em UTC, tipo que não admite ambiguidade
Instant dataCompra = clock.instant();

// CORRETO — data civil: nascimento é um dia, não um instante; não tem fuso
LocalDate dataNascimento;

// ERRADO — java.util.Date: mutável, sem fuso explícito, e o legado a usava
// (Consumer.birthDate, Extract.dateBuy)
Date dataCompra = new Date();

// ERRADO — LocalDateTime: data e hora SEM fuso; ninguém sabe de que relógio vem
LocalDateTime dataCompra = LocalDateTime.now();
```

**Persistência:** `Instant` é gravado em `TIMESTAMP` em UTC. O Hibernate
precisa saber disso: `spring.jpa.properties.hibernate.jdbc.time_zone: UTC`
(sem isso, o driver converte pelo fuso da JVM e o mesmo instante é gravado
diferente em duas máquinas). `LocalDate` vai para `DATE`, sem conversão.

**O relógio nunca é o do sistema dentro de regra de domínio**: o instante vem
de um `java.time.Clock` injetado (seção 5) — é o que permite o teste fixar o
tempo.

**Por quê `ZonedDateTime` e não um offset fixo:** um offset numérico
(`OffsetDateTime`) fica **congelado** na criação; se a regra do fuso mudar entre
a criação e o disparo (horário de verão), o instante fica errado.
`ZonedDateTime` carrega a **identidade do fuso** (`ZoneId`) e resolve o
deslocamento no momento certo. Hoje nenhuma regra do projeto depende de fuso
(sem agendamento), então isto é **regra para quando aparecer**, não código a
escrever.

---

## 2. Unidades do Domínio

### Tabela Canônica

| Grandeza | Unidade canônica | Tipo Java | Observação |
|---|---|---|---|
| **Dinheiro** | reais (BRL, moeda única implícita) | **`Dinheiro`** (`BigDecimal` por dentro) | §6 — `double`/`float` proibido; escala fixa de 6 casas |
| Percentual / fator | **fração** (0.10 = 10%) | `BigDecimal` | §6 — nunca a escala 0–100 |
| Instante | UTC | `Instant` | §1 |
| Data civil | — | `LocalDate` | §1 |
| Duração entre eventos | monotônico | `Duration` medido por `System.nanoTime()` | §5 |
| Contagem (itens, página) | inteiro | `int`/`long` | sem unidade, mas nome explícito (`tamanhoPagina`) |
| Número de cartão, documento | texto de dígitos | tipo de valor (`NumeroDeCartao`, `Documento`) | **a decidir em `BANCO.md`**; nunca `int` (o legado usava e estoura) |
| Texto de domínio (nome) | — | `String` não nula | §4 |

### Regra do Nome Carregando a Unidade

Nenhuma função de domínio recebe `BigDecimal`/`long` "solto" quando o
contexto permite ambiguidade — **o nome do parâmetro sempre carrega a
unidade ou o papel**.

```java
// ERRADO — o chamador pode inverter os argumentos sem erro algum
public BigDecimal calcular(BigDecimal valor, BigDecimal taxa);

// CORRETO — papel na assinatura, ordem inequívoca, e o tipo diz que é dinheiro
public Dinheiro calcularValorCobrado(Dinheiro valorInformado, BigDecimal ajusteFracionario);
```

### Tipos de Valor Dedicados

Quando um valor **cruza camadas** e carrega invariante, o tipo de valor
dedicado transforma a confusão em erro de compilação. Em Java o veículo é o
`record`:

```java
// CORRETO — simples portador de papel, sem invariante além do tipo
public record ConsumidorId(long valor) { }

// CORRETO — com invariante: o compact constructor garante, uma vez só, que
// nenhum Dinheiro existe fora da escala canônica (ver §6)
public record Dinheiro(BigDecimal valor) {
    public Dinheiro {
        valor = valor.setScale(ESCALA, RoundingMode.UNNECESSARY);   // lança se perderia dígitos
    }
    // ...
}
```

> **Armadilha de `record`**: o construtor canônico **não pode ser mais
> restrito que o próprio `record`** — um `record` público sempre tem
> construtor público. Construtor privado com *factory* seria a forma de "só se
> constrói validado", mas em Java isso é **proibido pelo compilador** para
> `record`. Duas saídas:
>
> - **Invariante verificada no *compact constructor*, que lança** — a
>   violação é *bug de programação* (ver [`ERROS.md`](../qualidade/ERROS.md)),
>   não erro de domínio; e uma *factory* estática `criar(...)` devolve
>   `Resultado<T, ErroDominio>` para entrada **externa**, validando antes de
>   chamar o construtor. É o padrão adotado aqui para `Dinheiro`.
> - **`final class` com construtor privado**, quando o tipo não deve ter
>   `equals`/`toString` gerados (por exemplo, para não vazar dado sensível no
>   `toString()` — [`SEGURANCA.md`](../qualidade/SEGURANCA.md#5-logs-sem-dados-sensíveis)).

**Quando primitivo basta vs. tipo de valor**: primitivo com o papel no nome
do parâmetro quando o valor é **local a uma função**; tipo de valor dedicado
quando o valor **cruza camadas**, é retornado por porta, persistido ou
comparado em testes.

---

## 3. IDs como Tipos de Valor

IDs de agregados nunca são `Long`/`long` crus nas assinaturas de domínio e de
aplicação. Um tipo por agregado torna "passar `ConsumidorId` onde se espera
`CartaoId`" um **erro de compilação**:

```java
// CORRETO
public record ConsumidorId(long valor) { }
public record CartaoId(long valor) { }

// ERRADO — o compilador aceita trocar um pelo outro silenciosamente
Resultado<Consumidor, ErroDominio> buscarPorId(long id);
void transferir(long origem, long destino);
```

**A fronteira é barata em Java**, sem conversor genérico de framework:

- **Banco:** a `@Entity` guarda `Long` — **o ID tipado vive só no domínio**, e
  o mapeador converte (já existe um mapeador por agregado em
  [`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md)). Nenhum
  `AttributeConverter` é necessário.
- **HTTP:** o controller recebe `@PathVariable long id` (validado com
  `@Positive`) e constrói o `ConsumidorId` **num único ponto**, na borda.
  Registrar um `Converter<String, ConsumidorId>` global seria configuração
  extra sem ganho para dois tipos de ID.
- **Cartão não tem ID público**: é consultado por `numero`. `CartaoId` existe
  só dentro do domínio e do banco.

**Por quê:** `salvar(consumidor.id(), cartao.id())` com dois `long` crus é
exatamente a chamada que compila, passa em teste feliz e grava o cartão no
consumidor errado.

---

## 4. Strings Não Nulas por Padrão

Todas as strings de domínio são não-nulas. **Java não tem `String?`** — não há
como o tipo declarar "pode ser nulo", então a regra é estrita: o domínio não
recebe nem devolve `null`.

```java
// CORRETO — invariante verificada na construção; nunca nulo daqui para frente
public record Contato(String celular, String email) {
    public Contato {
        Objects.requireNonNull(celular);
        Objects.requireNonNull(email);
    }
}

// ERRADO — o chamador não sabe se pode ser nulo, e cada usador precisa checar
public String getEmail();   // null? vazio? só lendo o corpo
```

**Dado genuinamente opcional** (o legado tem três telefones, e nenhum é
obrigatório pelo enunciado): ausência **modelada**, não `null` — ver
[`CONDICIONAIS.md`](CONDICIONAIS.md), nível 8 (Null Object). Qual campo é
obrigatório e qual é opcional é decisão de domínio a registrar em `BANCO.md`
(afeta `NOT NULL` no schema).

**O legado usava `int` e `0` como "ausente"**: `int residencePhoneNumber`
vale `0` quando ninguém informou — valor mágico (seção 8) que o banco grava
como um telefone real.

---

## 5. Relógio — Dois Propósitos, Dois Relógios

| Propósito | Relógio | Mecanismo | Exemplo |
|---|---|---|---|
| **Carimbo**: quando aconteceu (exibir, ordenar, persistir) | de parede, em UTC | **`java.time.Clock` injetado** → `clock.instant()` | `dataCompra` |
| **Duração**: quanto tempo durou (latência, medição) | **monotônico** | `System.nanoTime()` | medição em [`PERFORMANCE.md`](../qualidade/PERFORMANCE.md#8-medir-antes-de-otimizar) |

Medições de duração usam `System.nanoTime()` (relógio monotônico),
**nunca** `System.currentTimeMillis()` nem `Instant.now()` — o relógio de
parede pode andar para trás por ajuste do sistema (NTP, VM migrando), e a
duração calculada fica negativa ou inflada sem nenhum erro.

```java
// ERRADO — sensível a ajuste de relógio do sistema
long inicio = System.currentTimeMillis();
// ... operação ...
long duracao = System.currentTimeMillis() - inicio;   // pode ser negativo

// CORRETO — monotônico, seguro para medir intervalos
long inicio = System.nanoTime();
// ... operação ...
Duration duracao = Duration.ofNanos(System.nanoTime() - inicio);
```

**Carimbo nunca vem de `Instant.now()` solto no domínio.** O `Clock` é
injetado no caso de uso; em produção a composition root fornece
`Clock.systemUTC()`, no teste `Clock.fixed(...)`
([`TESTES.md`](../qualidade/TESTES.md#12-fake-mock-ou-banco-real--qual-substituto-em-qual-fronteira)).
A proibição é a **troca de papéis**: duração nunca com relógio de parede,
carimbo sempre com `Clock`.

---

## 6. Dinheiro e Percentuais

Esta é a seção central do documento. "Decimal exato, nunca `double`" é o
princípio, **mas não é suficiente**: dinheiro tem escala, arredondamento,
armazenamento, comparação e apresentação — e cada um deles, mal definido,
reintroduz o erro que o decimal exato existe para evitar.

### 6.1 O Tipo: `BigDecimal` Dentro de `Dinheiro`

Java não tem `decimal`; o equivalente é `java.math.BigDecimal` (precisão
arbitrária, base 10). `double` e `float` são **binários** e não representam
exatamente `0.1` — em dinheiro a imprecisão é sistemática, soma ao longo do
tempo e diverge de qualquer conferência.

```java
// ERRADO — o legado: double
double foodCardBalance;
Double cashback = (value / 100) * 10;      // e a conta ainda divide antes de multiplicar

// ERRADO — construir BigDecimal a partir de double carrega o erro binário para dentro do decimal
new BigDecimal(0.1);          // 0.1000000000000000055511151231257827...
BigDecimal.valueOf(0.1);      // "0.1" — mas o double JÁ foi a fonte; proibido como origem de dinheiro

// CORRETO — de texto ou de inteiro, nunca de double
new BigDecimal("0.1");
BigDecimal.valueOf(10, 2);    // 0.10
```

O domínio não manipula `BigDecimal` solto para dinheiro: usa o tipo
**`Dinheiro`** (seção 2), que encapsula escala, arredondamento e
comparação num único lugar.

### 6.2 Representação Fiel: Guarda-se a Fração, Apresenta-se em Centavos

**Decisão:** o sistema guarda dinheiro com **frações de centavo** e só a
**apresentação** mostra duas casas.

O motivo é de domínio, não de preciosismo. Valores em reais têm duas casas
quando **informados** ou **exibidos** (o extrato mostra centavos), mas **os
resultados de cálculo não respeitam essa fronteira**:

- Hoje: 10% de desconto sobre `33.33` é `29.997` — a terceira casa existe.
- Amanhã (boleto em atraso): multa, mora proporcional ao dia e correção
  monetária produzem resultados como `0.002` de diferença **sem ser caso
  raro** — basta o boleto estar atrasado.

Arredondar para centavos a cada operação **descarta a fração** e acumula erro
sistemático; guardar a fração e arredondar **uma vez, na apresentação**,
preserva o que o cálculo produziu.

> **Consequência assumida e documentada:** o saldo **exibido** (2 casas) pode
> diferir do saldo **exato** em até meio centavo. Isso é esperado e por
> projeto: a fonte da verdade é o valor exato; a tela mostra uma
> aproximação dele.

### 6.3 Escala: Seis Casas

| Parâmetro | Valor | Onde mora |
|---|---|---|
| **Escala interna** (casas guardadas) | **6** | `Dinheiro.ESCALA` — **a única constante** |
| Coluna do banco | `NUMERIC(19,6)` | `@Column(precision = 19, scale = 6, nullable = false)` |
| Entrada pela API (valor informado) | **até 2 casas** | `@Digits(integer = 13, fraction = 2)` na `Mensagem*` |
| Apresentação | **2 casas** | `ApresentacaoDeDinheiro.emCentavos(dinheiro, RoundingMode)` — o modo é **argumento obrigatório** |

- **Por que 6.** Entrada de 2 casas × fator de 2 casas (10%, 35%) dá 4 casas
  exatas; 6 deixa folga de **duas casas** para encargos compostos, sem
  chegar a um número arbitrariamente grande. **É um parâmetro, não um
  dogma:** mudar a escala é trocar `Dinheiro.ESCALA` e o `scale` da coluna —
  e um teste (seção 6.8) falha se os dois divergirem.
- **Por que a entrada é de 2 casas.** O cliente informa valores como aparecem
  no extrato. Fração de centavo é **resultado de cálculo**, nunca dado de
  entrada nesta API; aceitar mais casas na entrada abriria a porta para
  valores que nenhum extrato conseguiria mostrar.
- **Precisão 19.** Dá 13 dígitos inteiros (até ~9,99 trilhões) — folga
  suficiente.

### 6.4 Arredondamento: Um Só Lugar Interno, Um Só Lugar Externo

O sistema tem **exatamente dois pontos** de arredondamento, ambos explícitos:

| Ponto | Quando | Regra |
|---|---|---|
| **Interno** | resultado de **multiplicação ou divisão** excede a escala de 6 | `RoundingMode.HALF_EVEN`, aplicado dentro de `Dinheiro` |
| **Externo** | apresentação em centavos | `ApresentacaoDeDinheiro.emCentavos(dinheiro, RoundingMode)` — **o chamador escolhe**, não há padrão |

- **Soma e subtração são exatas** e nunca arredondam (mesma escala).
- **O arredondamento interno é de magnitude desprezível** (sexta casa
  decimal), mas **existe e é definido** — não é deixado ao acaso nem a um
  `ArithmeticException`.
- **`HALF_EVEN`** (arredondar metade para o par) é a regra de arredondamento
  bancário e evita viés acumulado em muitas operações. *Confirmar com a
  regra contratual/regulatória que vier a se aplicar* — se o negócio exigir
  `HALF_UP`, a mudança é uma constante.
- **Divisão sem contexto é proibida**: `a.divide(b)` lança
  `ArithmeticException` quando o resultado é dízima (1 ÷ 3). Hoje nenhum caso
  de uso divide dinheiro, então `Dinheiro` não tem divisão; se um dia tiver,
  ela informa escala e `RoundingMode` e acontece **dentro de `Dinheiro`**,
  nunca espalhada.
- **A apresentação não tem modo padrão de propósito.** `HALF_UP` e `HALF_EVEN`
  dão resultados diferentes em `29.985` (`29.99` e `29.98`); deixar o modo
  implícito é decidir sem perceber. O ponto de chamada declara o que escolheu.

### 6.5 O Tipo `Dinheiro`

**Uma peça por arquivo.** O `Dinheiro` só guarda a quantia e garante a escala;
somar, subtrair, multiplicar e comparar são papéis com método `default`, cada um
em seu arquivo, escritos em cima de `valor()`; ler texto de fora e apresentar em
centavos são tipos à parte. Assim duas pessoas podem mexer na multiplicação e
na leitura de texto sem tocar no mesmo arquivo
([`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md#1-um-tipo-público-por-arquivo)).

```java
// dominio/dinheiro/Dinheiro.java — a quantia e a escala, e nada mais
public record Dinheiro(BigDecimal valor) implements Somavel, Subtraivel, Multiplicavel, ComparavelPorQuantia {

    /** Casas decimais guardadas. Deve coincidir com a escala da coluna (teste de coerência). */
    public static final int ESCALA = 6;

    /** Dígitos guardados, com a escala: o que cabe em NUMERIC(19,6). */
    public static final int PRECISAO = 19;

    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    public Dinheiro {
        // UNNECESSARY: se o valor perderia dígitos, é BUG de quem construiu — lança.
        // Entrada externa passa por DinheiroDeTexto, que valida antes.
        valor = valor.setScale(ESCALA, RoundingMode.UNNECESSARY);
    }
    // equals/hashCode gerados do record funcionam: todo Dinheiro tem a MESMA escala (6),
    // então 90.000000 e 90.000000 são iguais — a armadilha de equals do BigDecimal some
}

// dominio/dinheiro/ComQuantia.java — a operação-base de que as outras se escrevem
public interface ComQuantia { BigDecimal valor(); }

// dominio/dinheiro/Somavel.java — soma exata
public interface Somavel extends ComQuantia {
    default Dinheiro mais(Dinheiro outro) { return new Dinheiro(valor().add(outro.valor())); }
}

// dominio/dinheiro/Subtraivel.java — subtração exata (o resultado pode ser negativo)
public interface Subtraivel extends ComQuantia {
    default Dinheiro menos(Dinheiro outro) { return new Dinheiro(valor().subtract(outro.valor())); }
}

// dominio/dinheiro/Multiplicavel.java — multiplica por um fator em fração; arredonda à escala interna com HALF_EVEN
public interface Multiplicavel extends ComQuantia {
    default Dinheiro vezes(BigDecimal fator) {
        return new Dinheiro(valor().multiply(fator).setScale(Dinheiro.ESCALA, RoundingMode.HALF_EVEN));
    }
}

// dominio/dinheiro/ComparavelPorQuantia.java — ordena pelo valor numérico
public interface ComparavelPorQuantia extends ComQuantia, Comparable<Dinheiro> {
    @Override default int compareTo(Dinheiro outro) { return valor().compareTo(outro.valor()); }
}

// dominio/dinheiro/DinheiroDeTexto.java — entrada externa: valida o formato e converte, sem lançar
public final class DinheiroDeTexto {
    public static Resultado<Dinheiro, ErroDominio> converter(String texto) { /* ... */ }
}

// dominio/dinheiro/ApresentacaoDeDinheiro.java — o modo é obrigatório: não há arredondamento "padrão"
public final class ApresentacaoDeDinheiro {
    public static BigDecimal emCentavos(Dinheiro dinheiro, RoundingMode modo) { /* ... */ }
}
```

*(`Dinheiro` é `record` e **não** esconde o `toString()`: valor monetário não é
dado sensível por si; o saldo de um cartão logado ao lado do número já seria,
mas esse vazamento é do **número**, tratado em `SEGURANCA.md` §5.)*

### 6.6 Percentual e Fator: Fração, Nunca 0–100

**Escala de percentual: fração 0–1, nunca 0–100.** 10% é `0.10`, não `10`.
Quem lê o campo sem essa convenção não sabe se `7` significa 7% ou 700%, e a
confusão é silenciosa (ambas são `BigDecimal` válidos). O cálculo multiplica
pela fração direto; só a apresentação multiplica por 100.

**Desconto e acréscimo são o mesmo conceito com sinal**, expressos como
**ajuste fracionário** do valor — o que casa com o vocabulário do enunciado
("desconto de 10%", "acréscimo de 35%"):

```java
// dominio/cartao/TipoCartao.java — o enum guarda o dado e declara os papéis
public enum TipoCartao implements Cobravel, ComCodigoDeEstabelecimento {
    ALIMENTACAO(1, "-0.10"),    // desconto de 10%
    FARMACIA(2, "0.00"),        // sem ajuste
    COMBUSTIVEL(3, "0.35");     // acréscimo de 35%

    private final int codigoDeEstabelecimento;
    private final BigDecimal ajusteDoValor;      // fração, com sinal

    TipoCartao(int codigoDeEstabelecimento, String ajusteDoValor) {
        this.codigoDeEstabelecimento = codigoDeEstabelecimento;
        this.ajusteDoValor = new BigDecimal(ajusteDoValor);
    }

    @Override public int codigoDeEstabelecimento() { return codigoDeEstabelecimento; }
    @Override public BigDecimal ajusteDoValor() { return ajusteDoValor; }
}

// dominio/cartao/Cobravel.java — o cálculo, em arquivo próprio, sobre ComAjusteDoValor
public interface Cobravel extends ComAjusteDoValor {
    /** valor × (1 + ajuste): 33.33 no cartão de alimentação → 29.997000, sem arredondar a fração. */
    default Dinheiro valorCobrado(Dinheiro valorInformado) {
        return valorInformado.vezes(BigDecimal.ONE.add(ajusteDoValor()));
    }
}

// dominio/cartao/TipoCartaoDoEstabelecimento.java — a leitura do código vindo de fora
// TipoCartaoDoEstabelecimento.converter(99) → Falha(ErroTipoEstabelecimentoInvalido)
```

O enum só guarda o dado e os dois acessores triviais; o cálculo e a leitura do
código externo moram cada um em seu arquivo (uma peça por arquivo). A leitura
transforma o código que não existe em erro, em vez de um `else` que cairia no
combustível, como no legado.

O legado fazia `value - (value/100)*10` em `double`: **divide antes de
multiplicar**, em ponto flutuante — duas fontes de erro numa linha.

### 6.7 Comparar, Persistir e Serializar

**Comparar.** `BigDecimal.equals` é sensível à escala
(`new BigDecimal("2.0").equals(new BigDecimal("2.00"))` → `false`); por isso:

- **`compareTo`** para comparar quantias cruas, **nunca `equals`**.
- **`Dinheiro` normaliza a escala na construção**, então seu `equals`
  funciona — é uma das razões de o tipo existir.
- **Em teste**, `assertThat(x).isEqualByComparingTo("90")`, não `isEqualTo`.

**Persistir.**

```java
@Column(name = "saldo", nullable = false, precision = 19, scale = 6)
private BigDecimal saldo;     // NUMERIC(19,6) — nunca DOUBLE/FLOAT/REAL
```

- **Tipo de coluna `NUMERIC(19,6)`**: o legado gravava `double` em
  `DOUBLE`. `NUMERIC` e `DECIMAL` são sinônimos exatos (ponto fixo) no padrão
  SQL e no H2 — o nome é o que o Hibernate emite por padrão para
  `BigDecimal`; a regra é **ponto fixo com escala declarada**, e o nome do
  tipo é o dialeto quem escolhe a partir de `precision`/`scale` (sem
  `columnDefinition` fixo). **Proibidos** `DOUBLE`, `FLOAT`, `REAL` e o
  `DECFLOAT` do H2 (decimal de ponto *flutuante*, de precisão variável).
- **Coerência escala × coluna**: um teste lê o `scale` declarado em cada
  coluna monetária e o compara com `Dinheiro.ESCALA` — se alguém mudar um e
  esquecer o outro, o banco **arredonda ou trunca em silêncio** ao gravar.
- **O extrato guarda o valor efetivamente cobrado, exato.** Recomendado
  guardar também o **valor informado**, para que a fração seja rastreável até
  a origem (informado → ajuste → cobrado).

**Serializar (JSON).**

- **Saída: string decimal com a escala completa** (`"29.997000"`), nunca
  número JSON. Um cliente JavaScript que lê número JSON converte para
  `double` e **perde a fração** que o sistema se esforçou para guardar. A
  conversão acontece **no mapeador** (`Dinheiro` → `String`), num único ponto
  — sem depender de configuração mágica do Jackson.
- **Entrada: `BigDecimal` com `@Digits(integer = 13, fraction = 2)`.** O
  Jackson desserializa número ou texto para `BigDecimal` **sem passar por
  `double`**; a validação de casas acontece na borda
  ([`API.md`](../qualidade/API.md#6-serialização-determinística)).
- **Apresentação em centavos não é desta API hoje.** Se aparecer um
  endpoint de extrato para exibição, ele devolve uma `Mensagem*` própria com
  `ApresentacaoDeDinheiro.emCentavos(dinheiro, modo)` — nunca o valor exato
  com a casa a mais.

### 6.8 Testes Que Fixam Esta Seção

| Teste | Garante |
|---|---|
| `DinheiroTeste` — `deve_guardar_a_fracao_quando_multiplicar_por_fator` | `33.33 × 0.90 = 29.997000`, sem perda |
| `DinheiroTeste` — `deve_somar_exato_quando_acumular_fracoes` | `0.1 + 0.2 = 0.3` em escala 6 (o exemplo clássico que `double` erra) |
| `DinheiroTeste` — `deve_lancar_quando_construir_com_mais_casas_que_a_escala` | `UNNECESSARY` impede perda silenciosa |
| `DinheiroTeste` — `deve_arredondar_para_o_par_na_escala_interna_quando_a_multiplicacao_excede_a_escala` | o único arredondamento interno é o definido |
| `ApresentacaoDeDinheiroTeste` — `deve_arredondar_como_o_modo_escolhido_quando_apresentar_em_centavos` | `29.985` dá `29.98` (HALF_EVEN) e `29.99` (HALF_UP) — o chamador decide |
| `DinheiroDeTextoTeste` | só decimal com ponto, até 13 inteiros e 6 casas, é aceito; o texto recusado não volta na falha |
| `DinheiroTeste` — `deve_ser_igual_quando_mesma_quantia_com_escalas_diferentes` | `2.0` e `2.00` viram o mesmo `Dinheiro` |
| `CoerenciaDeEscalaTeste` | `scale` das colunas monetárias == `Dinheiro.ESCALA` |
| contrato — `deve_serializar_dinheiro_como_texto` | saída `"29.997000"`, não número |

*(Entram na tabela da seção 14 de [`TESTES.md`](../qualidade/TESTES.md).)*

---

## 7. Coleções Imutáveis em Superfície Pública

Toda coleção exposta por tipo público é **imutável**. Em Java, `List.copyOf`
entrega isso de verdade:

```java
// CORRETO — cópia imutável feita na construção: ninguém de fora muta,
// e a coleção que o chamador guardou NÃO muda depois (é uma fotografia)
public record Consumidor(/* ... */ List<Cartao> cartoes) {
    public Consumidor {
        cartoes = List.copyOf(cartoes);        // lança se houver null — bom: null não entra
    }
}

// ERRADO — qualquer chamador muta o estado interno do agregado fora dos métodos de domínio
public List<Cartao> getCartoes() { return cartoes; }

// INSUFICIENTE — apenas uma view: se o original mudar, a "somente-leitura" muda junto
public List<Cartao> getCartoes() { return Collections.unmodifiableList(cartoes); }
```

**Armadilha**: `Collections.unmodifiableList(x)` é só uma *view*: o chamador
não consegue `add`, mas enxerga as mudanças que o dono fizer. `List.copyOf(x)` (Java 10+) devolve uma cópia
**genuinamente imutável**. Para o domínio, que se constrói imutável, a
segunda é o padrão.

**Por quê:** a invariante do agregado ("cartões só entram pelo método do
domínio") é burlada por um `.add()` de fora — e o leitor do chamador não tem
como saber que aquele `List` é proibido de mutar.

---

## 8. Ausência Como Tipo, Nunca Como Valor Mágico

**Em Java não há anotação de nulidade no tipo**, então `null` não documenta
nada — o domínio **não usa `null`** (ver [`CONDICIONAIS.md`](CONDICIONAIS.md#proibições-absolutas)).
Ausência é:

- **`Resultado<T, E>`** quando é erro (cartão não encontrado) —
  [`ERROS.md`](../qualidade/ERROS.md);
- **Null Object** quando tem comportamento padrão
  ([`CONDICIONAIS.md`](CONDICIONAIS.md), nível 8);
- **`Optional`** só como **retorno de consulta de porta**, convertido para
  `Resultado` na primeira chamada.

Nunca valor mágico: `-1`, `""`, `0`, `Guid.Empty`. **O legado é o catálogo
do anti-padrão:** `int number = 0`, `int phoneNumber = 0`, `int
drugstoreNumber = 0` — o `0` significa "sem cartão de farmácia", e o
`findByDrugstoreNumber(0)` **casa com qualquer consumidor sem esse cartão**.
É um valor mágico que vira consulta errada.

```java
// ERRADO — 0 faz papel de "não tem cartão"; a consulta por 0 encontra gente que não tem
int drugstoreNumber;

// CORRETO — a ausência do cartão é um estado do domínio, não um número
// (consumidor.cartoes() simplesmente não contém o cartão de farmácia)
List<Cartao> cartoes;
```

---

## 9. Tipos Explícitos em Assinaturas Públicas

Todo método público declara seu tipo de retorno e de parâmetros
explicitamente — o Java **exige**, o que torna a regra automática. `var` é
permitido em **variável local** quando o tipo é óbvio pelo lado direito
(`var consumidores = repositorio.listar(...)`), e proibido onde esconderia o
tipo de um valor monetário ou temporal (`var x = valor.vezes(f)` não diz se
é `Dinheiro` ou `BigDecimal`).

---

## 10. Constantes de Domínio Nomeadas

Valores "mágicos" usados em **mais de um lugar** (limites, taxas, escalas)
são constantes nomeadas — e, quando o número carrega uma decisão, essa
decisão é documentada na própria constante
([`DOCUMENTACAO.md`](DOCUMENTACAO.md)).

```java
// CORRETO — nome + a decisão por trás do número
/**
 * Casas decimais guardadas em toda quantia monetária.
 *
 * <p>6: entrada de 2 casas × fator de 2 casas dá 4 exatas, e a folga de 2 casas
 * comporta encargos compostos. Deve coincidir com o {@code scale} das colunas
 * monetárias (ver {@code CoerenciaDeEscalaTeste}).
 */
public static final int ESCALA = 6;

// ERRADO — o que significa 6? quem lê não sabe, e cada uso pode divergir
valor.setScale(6, RoundingMode.HALF_EVEN);
```

---

## Proibições Absolutas

- **`double`/`float` para dinheiro ou percentual** → `Dinheiro`/`BigDecimal`
  (§6).
- **`new BigDecimal(double)`** e dinheiro originado de `double` → texto ou
  inteiro com escala (§6.1).
- **`BigDecimal.equals` para comparar quantias** → `compareTo` (§6.7).
- **`divide` sem `RoundingMode`/`MathContext`** → dentro de `Dinheiro`, com
  modo explícito (§6.4).
- **Arredondar dinheiro para 2 casas dentro de cálculo ou antes de gravar** →
  só na apresentação, com o modo escolhido pelo chamador (§6.2, §6.4).
- **Dinheiro como número JSON** → string decimal (§6.7).
- **Percentual na escala 0–100** → fração (§6.6).
- **Medir duração com `currentTimeMillis`/`Instant.now()`** →
  `System.nanoTime()` (§5).
- **`java.util.Date`, `LocalDateTime` ou `Calendar` no domínio** → `Instant`,
  `ZonedDateTime` ou `LocalDate` (§1).
- **`Instant.now()`/`LocalDate.now()` solto em regra de domínio** → `Clock`
  injetado (§5).
- **`long`/`int` crus como ID em assinatura de domínio** → `record` do
  agregado (§3).
- **Número de cartão, documento ou telefone como `int`** (o legado) → texto de
  dígitos em tipo de valor (§2).
- **`null` como ausência; `0`, `-1`, `""` como "não informado"** →
  `Resultado`/Null Object (§8).
- **`List<T>` mutável em superfície pública; `unmodifiableList` onde se
  precisa de fotografia** → `List.copyOf` (§7).
- **Literal repetido em mais de um lugar** → constante nomeada, com decisão
  documentada (§10).

---

## Checklist de Autor

- [ ] O valor segue a tabela canônica de unidades (§2)?
- [ ] Dinheiro é `Dinheiro`, sem `double`/`float` em nenhum ponto do caminho (entrada → domínio → banco → saída)?
- [ ] Nenhum `BigDecimal` nasce de `double`; todo nasce de texto ou de inteiro com escala?
- [ ] Quantias comparadas com `compareTo` (ou como `Dinheiro`), nunca com `equals` cru?
- [ ] Toda divisão informa `RoundingMode`, e acontece dentro de `Dinheiro`?
- [ ] Nada arredonda para 2 casas antes da apresentação; a apresentação passa o `RoundingMode` explicitamente?
- [ ] A coluna monetária é `NUMERIC(19,6)` com `scale` igual a `Dinheiro.ESCALA`?
- [ ] Dinheiro sai no JSON como string decimal?
- [ ] Percentual é fração (0.10), com o nome carregando "Percentual"/"Ajuste"?
- [ ] Instante é `Instant` em UTC, data civil é `LocalDate`, e o relógio é o `Clock` injetado?
- [ ] Duração medida com `System.nanoTime()`?
- [ ] ID de agregado como `record` próprio, não `long` cru?
- [ ] Coleção pública imutável via `List.copyOf`?
- [ ] Ausência é `Resultado`/Null Object, nunca `null` nem valor mágico?
- [ ] Literais repetidos viraram constantes nomeadas e documentadas?

## Checklist de Review

- [ ] Há `double` ou `float` em qualquer ponto do caminho de um valor monetário?
- [ ] Há `equals` comparando `BigDecimal`, ou `new BigDecimal(double)`?
- [ ] Há `setScale(2, …)` dentro de cálculo ou antes de gravar (perde a fração)?
- [ ] Há `divide` sem modo de arredondamento?
- [ ] A escala da coluna e `Dinheiro.ESCALA` continuam iguais?
- [ ] Há `Date`, `LocalDateTime` ou `currentTimeMillis` no domínio?
- [ ] Algum `0`/`-1` faz papel de "não encontrado" ou "não informado"?
- [ ] A assinatura responde sozinha, sem abrir o corpo, às perguntas de unidade e de fuso?

---

**[Estruturas Condicionais ←](CONDICIONAIS.md)**
