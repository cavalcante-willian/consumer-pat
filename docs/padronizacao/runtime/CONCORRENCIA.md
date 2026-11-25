[← Índice](../../PADRONIZACAO_INDEX.md) · [← BUILD.md](../qualidade/BUILD.md)

# Concorrência — Java 17 / Spring Boot 2.7

> **Escopo**: a concorrência em **Java 17 + Spring MVC (Servlet)**: **uma
> thread por requisição, bloquear é normal**. A tese é a que mais importa numa
> API que **altera saldo de forma concorrente**: *nenhuma concorrência
> implícita, nenhum estado global mutável, e cada concorrência descrita num
> inventário antes de existir*. Ver
> [Premissas de Concorrência](#premissas-de-concorrência).
>
> **Nota sobre exemplos**: os trechos de código abaixo são ilustrativos e
> **não foram executados** — a regra normativa é o texto conceitual. O código
> atual ainda é o **legado do desafio**, que **não tem proteção alguma**
> contra concorrência: lê o saldo, soma em memória e grava (*lost update*).
>
> **Documento relacionado**: [`OBSERVABILIDADE.md`](OBSERVABILIDADE.md).

Define onde nasce cada concorrência do sistema e quem a controla, onde mora a
garantia de correção dos dados, a proibição de estado global mutável, o que
vale no lugar do cancelamento e as regras para o dia em que houver trabalho de
fundo.

**Quando consultar:**

- Antes de criar qualquer thread, executor, `CompletableFuture`, `@Async`,
  `@Scheduled` ou `parallelStream()`
- Ao introduzir estado que sobrevive a uma requisição (campo `static`, campo
  de bean, cache, `ThreadLocal`)
- Ao escrever código que lê um dado persistido, calcula e grava de volta
- Ao testar comportamento sob acesso simultâneo

---

## Premissas de Concorrência

| Tema | Regra neste projeto |
|---|---|
| Modelo de execução | **uma thread por requisição** (pool do Tomcat, 200 por padrão); **bloquear é o normal** |
| Cancelamento | **não existe**: cliente que desconecta **não** cancela a thread. Vale o **prazo** (timeout) em cada fronteira — [seção 5](#5-prazo-no-lugar-do-cancelamento) |
| Tarefa sem observador | proibida: toda tarefa de fundo tem dono e erro tratado |
| Concorrência implícita | `new Thread`, `CompletableFuture.*Async`, `@Async`, `@Scheduled`, **`parallelStream()`** |
| Estado compartilhado | `static` **e todo bean do Spring** (singleton por padrão) |
| Tipos não *thread-safe* | `SimpleDateFormat`, `DecimalFormat` — **já evitados** por `java.time` ([`VALORES.md`](../codigo/VALORES.md#1-datas-e-hora-sempre-em-utc)) |
| Fila e tarefas de fundo | **não existem** neste projeto — [seção 7](#7-trabalho-de-fundo--não-existe-hoje-regras-para-quando-existir) |
| Onde mora a correção do dado | **no banco**: lock pessimista no saldo + versão no cadastro — [seção 3](#3-a-garantia-de-correção-vem-do-banco-não-da-jvm) |

---

## 1. Concorrência É a Regra, Não a Exceção

Esta API é concorrente por natureza: múltiplas requisições chegam ao mesmo
tempo, e **duas compras no mesmo cartão podem chegar no mesmo milissegundo**.
O legado trata isso como se não existisse:

```java
// legado — ConsumerController.buy: ler → calcular em memória → gravar
consumer.setFoodCardBalance(consumer.getFoodCardBalance() - value);
repository.save(consumer);
// duas requisições leem o MESMO saldo; a segunda grava por cima da primeira
```

A regra não é "evitar concorrência", é **nenhuma concorrência implícita ou
não descrita**: toda thread, executor e estado compartilhado é descrito neste
documento ([inventário, seção 6](#6-inventário-de-concorrência)) **antes** de
existir no código — não depois, como descoberta de code review.

**Por quê:** concorrência escondida é a fonte de bugs que só aparecem em
produção sob carga — e, como ninguém sabe onde ela mora, ninguém a reproduz
fora de lá. Descrever antes custa uma linha na tabela; descobrir depois custa
dinheiro de alguém.

---

## 2. Nenhuma Concorrência Implícita

O projeto **não cria threads**. Quem executa o código é o pool do Tomcat, e
nada mais. Tudo o que criaria concorrência nova é proibido **sem registro
prévio no inventário**:

| Construção | Por que cria concorrência escondida |
|---|---|
| `new Thread(...)`, `Executors.*` | thread própria, sem dono nem prazo |
| `CompletableFuture.supplyAsync/runAsync` (sem executor) | usa o `ForkJoinPool.commonPool()` — **compartilhado pela JVM inteira** |
| **`stream().parallel()` / `parallelStream()`** | também usa o `commonPool()`; parece uma otimização inofensiva de uma linha |
| `@Async`, `@EnableAsync` | o Spring cria um executor com **fila ilimitada** (Boot) ou **uma thread por tarefa** (Spring puro) |
| `@Scheduled`, `@EnableScheduling` | roda em **toda instância** da aplicação ao mesmo tempo |
| `Timer`, `ScheduledExecutorService` | laço de fundo sem prazo |

**Regras:**

- **Nenhuma construção da tabela entra em código de produção** sem linha
  prévia no inventário (seção 6), com dono, limite e teste.
- **`parallelStream()` e `CompletableFuture` são proibidos no caminho de
  requisição**, sem exceção. Dois motivos, não só o primeiro:
  1. **Esgotam o `commonPool()`** (compartilhado, tamanho = nº de núcleos − 1)
     com trabalho de I/O que ele não foi feito para absorver.
  2. **A transação é presa à thread.** Uma thread filha **não participa** da
     transação do caso de uso — e, portanto, **nem do lock** sobre o cartão. O
     paralelismo dentro de um caso de uso destrói exatamente o que o
     `@Transactional` garante ([`BANCO.md`](../qualidade/BANCO.md#12-transações-e-escopo-de-escrita)).
- **Nenhuma tarefa sem observador**: `executor.execute(...)` engole a exceção
  (vai para o *uncaught handler*, que só imprime); `submit(...)` devolve um
  `Future` que **alguém precisa consultar**. Tarefa criada é aguardada ou
  observada — senão a falha some sem rastro.
- **Nenhuma espera por tempo** (`Thread.sleep`) em código de produção; em
  teste, a mesma proibição
  ([`TESTES.md`](../qualidade/TESTES.md#13-determinismo--nada-de-flaky)).

```java
// CORRETO — sequencial, dentro da transação do caso de uso
var total = cartoes.stream().map(Cartao::saldo).reduce(Dinheiro.ZERO, Dinheiro::mais);

// ERRADO — "otimização" que usa o commonPool e sai da transação
var total = cartoes.parallelStream().map(Cartao::saldo).reduce(Dinheiro.ZERO, Dinheiro::mais);

// ERRADO — tarefa lançada e esquecida: sem dono, sem prazo, sem observador
CompletableFuture.runAsync(() -> extratoPorta.registrar(extrato));
```

**Por quê:** `parallelStream()` é a forma mais comum de introduzir
concorrência **sem perceber que a introduziu** — uma palavra a mais, e o
código passa a rodar em threads que ninguém escolheu.

---

## 3. A Garantia de Correção Vem do Banco, Não da JVM

*(O saldo exige mais que a concorrência otimista por versão de linha, e a
tentação de resolver "no código" é a mais perigosa.)*

**A regra:** a correção de um **dado persistido** é garantida **pelo banco**
(lock, versão, restrição), **nunca** por mecanismo da JVM.

**Proibido, para proteger dado persistido:**

- **`synchronized`**, `ReentrantLock`, `ReadWriteLock`, `Semaphore`;
- **`ConcurrentHashMap`/`AtomicReference`** como "fonte da verdade" de saldo,
  de contagem ou de qualquer estado de negócio;
- **cache em memória** do saldo (já vedado em
  [`PERFORMANCE.md`](../qualidade/PERFORMANCE.md#5-cache-de-leitura--recusado-de-propósito)).

```java
// ERRADO — "resolve" na JVM: funciona em UMA instância, para de funcionar na segunda
public synchronized void debitar(String numero, Dinheiro valor) { /* ler, calcular, gravar */ }
```

**Por quê:** um bloqueio da JVM trava **uma instância**. A API é *stateless*
justamente para poder rodar em várias
([`API.md`](../qualidade/API.md#7-endpoints-são-stateless)) — e na segunda
instância, ou após um reinício no meio da operação, o `synchronized` não
protege nada. Pior: **funciona em todo teste local**, porque o teste roda uma
instância. É a falha que só aparece em produção.

**O mecanismo certo, por tipo de dado:**

| O que protege | Mecanismo | Onde está definido |
|---|---|---|
| **Saldo** (débito/crédito) | **lock pessimista** (`@Lock(PESSIMISTIC_WRITE)`) dentro do `@Transactional` do caso de uso, **mais** `@Version` como rede de segurança | [`BANCO.md`](../qualidade/BANCO.md#7-concorrência-versão-de-linha-e-lock) |
| **Cadastro do consumidor** (`PUT`) | **versão de linha** (`versao`), comparada explicitamente | `BANCO.md` §7 |
| **Unicidade** (documento, número do cartão) | restrição `unique` (rede de segurança) + verificação prévia (mensagem boa) | [`ERROS.md`](../qualidade/ERROS.md#3-proibição-de-captura-genérica--e-as-duas-portas-de-conversão) |
| **Extrato** | *append-only* — nunca é atualizado, logo não há disputa | `BANCO.md` §8 |

**Regras que acompanham o lock:**

- **Um lock por transação.** Se uma operação futura precisar de mais de um
  (transferência entre cartões), trava **em ordem crescente de `id`** — a
  ordem determinística é o que impede o *deadlock*.
- **Não "resolver" contenção subindo o nível de isolamento** para
  `SERIALIZABLE`: esconde a disputa em vez de a tornar explícita, e a trava
  passa a ser do banco inteiro, não de uma linha.
- **A contenção é aceita e medida**, não evitada: duas compras no mesmo
  cartão **esperam uma pela outra**, e isso é o comportamento correto
  ([`PERFORMANCE.md`](../qualidade/PERFORMANCE.md#7-transação-e-lock-curtos--o-servidor-não-é-assíncrono)).

---

## 4. Sem Estado Global Mutável

Nenhuma variável global mutável em nenhuma camada: todo estado é passado
explicitamente, por construtor ou parâmetro.

**Em Spring isto é mais amplo do que parece: todo bean é singleton, e todo
singleton é estado compartilhado por todas as threads.**

| Armadilha | Por que quebra | Regra |
|---|---|---|
| **Campo mutável em bean** (controller, caso de uso, adaptador, `@Configuration`) | uma instância, N threads: escrita concorrente sem sincronização | campos de bean são **`final`** (portas e `Clock` injetados por construtor) |
| **Coleção mutável em bean** (`List`, `Map` como "cache") | condição de corrida silenciosa: passa no teste, estoura no terceiro usuário simultâneo | proibida; estado de sistema mora no **banco** |
| **`static` não `final`** / `static` com coleção | estado global com outro nome | proibido |
| **Lombok `@Data`/`@Setter` num bean** | gera *setters*: o bean passa a ser mutável | beans não usam `@Data`; só construtor |
| **`SimpleDateFormat`/`DecimalFormat` compartilhados** | não são *thread-safe* | `java.time` (imutável) — já decidido |
| **`ThreadLocal` / `MDC` em pool de threads reaproveitado** | o valor da requisição anterior **vaza** para a próxima | `MDC.clear()` no `finally` do `FiltroDeCorrelacao` ([`API.md`](../qualidade/API.md#4-pipeline-de-requisição)); nunca dado de negócio em `ThreadLocal` |
| **Entidade JPA guardada em campo de bean** | mutável, desatualizada e ligada a um *persistence context* que já fechou | a entidade vive **dentro** de uma transação, numa thread; nunca é guardada |

```java
// CORRETO — bean sem estado próprio: só dependências finais, injetadas por construtor
public class RegistrarCompraCaso {
    private final CartaoPorta cartaoPorta;
    private final ExtratoPorta extratoPorta;
    private final Clock relogio;
    // ... construtor; nenhum campo mutável
}

// ERRADO — estado compartilhado por todas as requisições
public class RegistrarCompraCaso {
    private int comprasProcessadas;                 // contador mutável: corrida de dados
    private final Map<String, Cartao> cache = new HashMap<>();   // idem, e dado desatualizado
}
```

**O domínio já é seguro por construção:** `record`s imutáveis, `Dinheiro`
imutável, coleções via `List.copyOf`
([`VALORES.md`](../codigo/VALORES.md#7-coleções-imutáveis-em-superfície-pública))
— **não há como** duas threads corromperem um `Cartao`, porque nenhuma delas
consegue mudá-lo; "alterar" devolve outro objeto.

**Por quê:** estado global mutável é a causa mais comum de bugs de
reentrância e de condição de corrida — e é também a condição de os casos de
uso serem testáveis sem subir o sistema inteiro.

### Verificação mecânica

A regra é **verificável**, no mesmo estilo dos outros testes de arquitetura do
projeto: `EstadoCompartilhadoTeste` varre `src/main` e falha se encontrar:

- campo `static` que não seja `final`;
- campo de instância não `final` em classe de `aplicacao`, `api` ou
  `infraestrutura` que não seja entidade nem *record* de mensagem;
- qualquer construção da tabela da seção 2 (`synchronized`, `parallelStream`,
  `CompletableFuture`, `new Thread`, `Executors.`, `@Async`, `@Scheduled`).

*(Esboço de varredura por texto, com limites conhecidos — não vê código gerado
por Lombok; é o piso, não o teto. Validar ao implementar.)*

---

## 5. Prazo no Lugar do Cancelamento

Cancelada a requisição, o trabalho não para: **o Servlet não tem
cancelamento.** Se o cliente desconecta ou estoura o próprio
*timeout*, **a thread continua até o fim** — a compra pode ser concluída com
ninguém esperando a resposta.

**Consequências:**

- **O que protege a thread é o prazo em cada fronteira**, já definidos em
  [`PERFORMANCE.md`](../qualidade/PERFORMANCE.md#9-timeouts-e-limites):
  timeout de transação, de espera por lock, de consulta e de obtenção de
  conexão. Sem eles, a requisição pendurada segura thread e conexão para
  sempre.
- **Cliente com *timeout* sem resposta não sabe se a compra aconteceu** — por
  isso `POST /compras` e `POST /creditos` **não podem ser retentados às
  cegas** ([`API.md`](../qualidade/API.md#11-concorrência-idempotência-e-conflitos)).
  É a face observável, para quem consome, de "não existe cancelamento".
- **Cancelamento não é falha**: não há `OperationCanceledException` a tratar
  neste modelo, mas a ideia vale — **uma parada pedida pelo dono não vira erro
  500 nem log crítico**.

**Para o dia em que houver thread própria** (hoje só o teste, seção 6):

- **`InterruptedException` nunca é engolida.** Quem a captura **restaura a
  flag** e para — "cancelado não volta":

  ```java
  // CORRETO
  try { tarefa.call(); }
  catch (InterruptedException e) {
      Thread.currentThread().interrupt();     // restaura a flag; a pilha acima ainda precisa saber
      return;                                  // e PARA: não segue como se nada tivesse acontecido
  }

  // ERRADO — engole a interrupção: a thread "ignora" a ordem de parar
  catch (InterruptedException e) { /* ignora */ }
  ```

- **Espera sempre com prazo** (`Future.get(timeout)`, `awaitTermination`) —
  nunca `get()` sem limite.
- **Uma unidade começada é terminada**; parar é *entre* unidades.
- **Executor se encerra**: `shutdown()` seguido de `awaitTermination`, e
  `shutdownNow()` no `finally`. (O `ExecutorService` do Java 17 **não** é
  `AutoCloseable` — isso só vem no Java 19; o `try-with-resources` não
  serve.)

---

## 6. Inventário de Concorrência

Nenhuma concorrência nova existe no código antes de ter, aqui, **um dono e um
limite**. A tabela é o inventário **completo** do sistema; a que não couber em
nenhuma linha entra **no mesmo PR** que cria o código.

| Concorrência | Dono — quem cria | Estado compartilhado? | Limite / prazo |
|---|---|---|---|
| **Thread de requisição** | o contêiner (Tomcat) | **nenhum**: beans só com campos `final`; domínio imutável | pool de 200 threads (`server.tomcat.threads.max`); timeout de conexão |
| **Transação e lock sobre cartão** | o caso de uso (`@Transactional`) | a **linha do cartão no banco** (única disputa real do sistema) | `@Transactional(timeout)`; `javax.persistence.lock.timeout`; **um lock por transação**, em ordem de `id` se houver mais de um |
| **Pool de conexões (Hikari)** | configuração (`application.yml`) | as conexões | `connection-timeout`; `maximum-pool-size` ([`PERFORMANCE.md`](../qualidade/PERFORMANCE.md#7-transação-e-lock-curtos--o-servidor-não-é-assíncrono)) |
| **`MDC` (`ThreadLocal`)** | `FiltroDeCorrelacao` | o valor de uma requisição, **na thread dela** | `MDC.clear()` no `finally` |
| **Executor do teste de concorrência** | o próprio teste | o cartão criado só para ele | `Future.get(timeout)`; `shutdownNow()` no `finally`; largada com `CountDownLatch` ([`TESTES.md`](../qualidade/TESTES.md#13-determinismo--nada-de-flaky)) |

**Não há mais nada.** Nenhum executor, fila, agendador, cache nem
`CompletableFuture` em código de produção.

**Regras:**

- **Sem concorrência sem dono.** Dono é quem cria e quem encerra — nomeado.
  "Tem uma thread rodando em algum lugar" não passa por revisão.
- **Sem criador anônimo global.** Toda tarefa nasce num ponto rastreável.
- **Trocou o dono, trocou o limite**, e a mudança é documentada no mesmo PR.

---

## 7. Trabalho de Fundo — Não Existe Hoje; Regras Para Quando Existir

Não há publicação agendada, limpeza, fila nem `@Async` neste projeto. **Se um
dia houver** (por exemplo, encargos de boleto vencido calculados em lote), ele
**não nasce na requisição**, e vale tudo abaixo — escrito agora para que a
primeira tarefa de fundo já nasça conforme:

- **A requisição que dispara não espera.** Responde com o aceite (`202`); o
  resultado aparece quando a tarefa rodar. `get()` da tarefa dentro do
  endpoint transforma o fundo em trabalho síncrono.
- **Executor nomeado, **limitado** e criado na composition root**
  (`api.configuracao`), nunca o padrão: o executor do `@Async` do Spring Boot
  tem **fila ilimitada** (a memória é o limite) e o do Spring puro cria **uma
  thread por tarefa**. Fila com **tamanho máximo** e política de rejeição
  explícita — rejeitar vira resposta tipada, não *crash*.
- **Encerramento gracioso**: aguardar as tarefas em andamento ao desligar
  (`setWaitForTasksToCompleteOnShutdown` + `setAwaitTerminationSeconds`).
- **A tarefa não herda a transação nem o `MDC` da requisição.** A transação
  é presa à thread; o `MDC` também. Para a log da tarefa se ligar ao pedido que
  a originou, o `idRequisicao` precisa **viajar explicitamente** (um
  `TaskDecorator` que copia o `MDC`).
- **O agendamento é estado no banco, não item em memória** — a fila dispara o
  trabalho; a verdade do que existe é a tabela.
- **`@Scheduled` roda em toda instância.** Com duas instâncias, o trabalho
  corre **duas vezes**. Coordenar isso **no banco** (linha de controle
  travada) — bibliotecas de *distributed lock* seriam dependência nova.
- **Tarefa de fundo registra início, fim e falha**, com contexto
  ([`ERROS.md`](../qualidade/ERROS.md#7-log-de-erro-estruturado)).
- **No teste, o executor é síncrono** (`SyncTaskExecutor`, do próprio
  Spring): a tarefa roda na hora, sem esperar relógio nem ordem de execução.

---

## 8. Testes de Concorrência

A regra de como escrever está em
[`TESTES.md`](../qualidade/TESTES.md#13-determinismo--nada-de-flaky) e não se
repete aqui: **largada sincronizada**, **prazo em toda espera**,
**invariante** (não entrelaçamento), **dados próprios**, **nenhum `sleep`**, e
o teste **tem que falhar se o lock for removido**.

O que este documento acrescenta é **o que cada teste prova**:

| Teste | Prova |
|---|---|
| `ConcorrenciaDeCompraTeste` | 20 compras simultâneas de 10,00 num cartão de 100,00 → exatamente 10 `201`, 10 `422` e saldo final 0,00; **a garantia é do banco** |
| `ConcorrenciaDeCreditoTeste` | 20 créditos simultâneos no mesmo cartão → todos `201` e saldo igual à soma: nenhum crédito se perde |
| `ConcorrenciaDeCompraECreditoTeste` | 10 compras e 10 créditos simultâneos → todos `201` e saldo inicial intacto: débito e crédito travam a mesma linha, e um não apaga o outro |
| `ConcorrenciaDeCadastroTeste` | 20 cadastros simultâneos do mesmo documento → um `201` e dezenove `409`, nunca erro de servidor: a janela da consulta prévia é coberta pela restrição `unique` |
| `deve_disparar_o_timeout_de_lock_quando_cartao_esta_travado` | a espera pelo lock **tem prazo** e ele dispara |
| `EstadoCompartilhadoTeste` | nenhum estado mutável compartilhado nem construção de concorrência proibida em `src/main` |

Os quatro primeiros percorrem a API com o contexto inteiro e o H2 real: o
cartão é preparado e o saldo é lido pela própria API, como um cliente faria,
e a largada é a de `DisputaSimultanea`, em `src/test`. Cada teste confere
primeiro que **só** os status esperados apareceram, para uma falha dizer o que
veio no lugar.

**Provado por mutação**, que é o que separa um teste de concorrência de um
decorativo:

- **Sem o `@Lock(PESSIMISTIC_WRITE)`**, os três testes de saldo caem, e não
  por perda de atualização: o `@Version` a impede, e a disputa vira `409`.
  Das 20 compras, 17 voltaram `409` e só 3 passaram (e em outra execução, 4),
  quando 10 eram legítimas. É a razão de a decisão ser o lock pessimista, e
  não a versão sozinha: com ela, compras legítimas simultâneas são recusadas.
- **Sem a restrição `unique` do documento**, 10 dos 20 cadastros do mesmo
  documento passaram, e o teste de cadastro cai. A verificação prévia sozinha
  não basta, como o `ERROS.md` avisa.

*(Entram na tabela da seção 14 de [`TESTES.md`](../qualidade/TESTES.md).)*

---

## Anti-Padrões

| Anti-padrão | Correto |
|---|---|
| Ler saldo → somar em memória → gravar, sem trava (o legado) | lock pessimista dentro do `@Transactional` (seção 3) |
| `synchronized`/`ReentrantLock` para proteger saldo | o banco garante: lock + versão (seção 3) |
| `ConcurrentHashMap`/`AtomicReference` como fonte da verdade de dado persistido | o banco é a fonte da verdade (seção 3) |
| Subir o isolamento para `SERIALIZABLE` "para resolver" | lock explícito sobre a linha (seção 3) |
| `parallelStream()` / `CompletableFuture` no caminho de requisição | processamento sequencial dentro da transação (seção 2) |
| `new Thread`, `Executors.*`, `@Async`, `@Scheduled` sem linha no inventário | descrever antes, na tabela da seção 6 |
| `executor.execute(...)` cuja falha ninguém observa | `submit` + `Future` consultado, ou *handler* que registra (seção 2) |
| Campo mutável ou coleção em bean (controller, caso de uso, adaptador) | campos `final`, injetados por construtor (seção 4) |
| `@Data`/`@Setter` em bean | só construtor (seção 4) |
| `static` não `final` | proibido (seção 4) |
| `SimpleDateFormat`/`DecimalFormat` compartilhados | `java.time` |
| Entidade JPA guardada em campo de bean | a entidade vive dentro da transação, numa thread só |
| `ThreadLocal`/`MDC` sem limpar no `finally` | `MDC.clear()` no `finally` (seção 4) |
| `catch (InterruptedException e) { }` | restaurar a flag e **parar** (seção 5) |
| `Future.get()` sem prazo, `Thread.sleep` | `get(timeout)`; sincronização explícita (seção 5) |
| `ExecutorService` sem `shutdown`/`shutdownNow` no `finally` | encerrar sempre (seção 5) |
| Retentar `POST` de compra após *timeout* do cliente | não é idempotente (seção 5) |
| Executor padrão do `@Async` (fila ilimitada) | executor nomeado e limitado na composition root (seção 7) |
| Concorrência nova descoberta em code review | descrita aqui, no mesmo PR (seção 6) |

---

## Checklist

Antes de escrever (ou revisar) código que toque concorrência:

- [ ] Nenhuma thread, executor, `CompletableFuture`, `@Async`, `@Scheduled` ou `parallelStream()` entrou em código de produção sem linha no inventário (seção 6)?
- [ ] Todo dado persistido modificado a partir de uma leitura é protegido **pelo banco** (lock, versão ou restrição) — e não por `synchronized`, `Lock` ou estrutura da JVM?
- [ ] A transação do caso de uso contém **um** lock (ou vários em ordem crescente de `id`), e nenhuma thread filha?
- [ ] Nenhum campo mutável ou coleção em bean; nenhum `static` não `final`; nenhum `@Data`/`@Setter` em bean?
- [ ] Nenhuma entidade JPA guardada fora da transação que a carregou?
- [ ] `ThreadLocal`/`MDC`, se tocado, é limpo no `finally`?
- [ ] Toda espera tem prazo; toda `InterruptedException` restaura a flag e para?
- [ ] Toda tarefa criada tem observador, dono e encerramento?
- [ ] Se há trabalho de fundo novo: executor nomeado e limitado, encerramento gracioso, `MDC` propagado, agendamento no banco, instância dupla considerada?
- [ ] O teste de concorrência usa largada sincronizada, prazo e invariante, e **falha sem a proteção**?
- [ ] O `EstadoCompartilhadoTeste` continua passando?
- [ ] O inventário da seção 6 foi atualizado no mesmo PR?

---

**[Fim da suíte de padronização]**

**Voltar ao:** [Índice Principal](../../PADRONIZACAO_INDEX.md)
