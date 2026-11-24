[← Índice](../../PADRONIZACAO_INDEX.md) · [← ERROS.md](ERROS.md)

# Performance — Java 17 / Spring Boot 2.7

> **Escopo**: o que sustenta resposta rápida e previsível em **Java 17 +
> Spring Boot 2.7 + Spring Data JPA/Hibernate**. Das 11 seções, **8 se
> aplicam**, **2 só descrevem o que não se aplica** (binário executável e
> mídia) e **1 é uma recusa de propósito** (cache de leitura — ver
> [seção 5](#5-cache-de-leitura--recusado-de-propósito)). Ver
> [Particularidades do Spring Data JPA](#particularidades-do-spring-data-jpa).
>
> **Nota sobre exemplos**: os trechos de código e parâmetros abaixo são
> ilustrativos — a regra normativa é o texto conceitual, não os nomes
> exatos de tipo ou parâmetro. **Os valores numéricos de configuração
> (timeouts, tamanhos de pool) são pontos de partida, não medições**: a
> [seção 8](#8-medir-antes-de-otimizar) manda decidi-los com medição. O
> código atual ainda é o **legado do desafio**; este documento descreve o
> estado pretendido (ver
> [`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md#este-documento-é-vivo)).
>
> **Documentos relacionados**: [`BANCO.md`](BANCO.md),
> [`SEGURANCA.md`](SEGURANCA.md), [`TESTES.md`](TESTES.md),
> [`CONCORRENCIA.md`](../runtime/CONCORRENCIA.md) e
> [`OBSERVABILIDADE.md`](../runtime/OBSERVABILIDADE.md).

Define o que sustenta resposta rápida e previsível nesta API de cartões de
benefício: acesso ao banco sem N+1, paginação obrigatória, projeções,
índices, a decisão de **não** cachear, transação e lock curtos, medição
antes de otimização, timeouts e limites e compressão.

**O ponto de partida do legado** (que este documento existe para impedir
que se repita): `GET /consumer/consumerList` executa `select * from Consumer`
e devolve **~50.000 consumidores de uma vez**, todos os campos, incluindo
telefones, endereço e saldos — **medido em ~1,1 s e 23 MB numa só resposta**
([seção 8](#linha-de-base-do-legado--medida)); as três buscas por cartão fazem
varredura de tabela, mas **medidas em 0,2 ms** com 50.000 linhas no H2, então o
custo está na listagem e não na busca; e o fluxo ler → somar → salvar mantém a
transação aberta sem controle algum.

---

## Particularidades do Spring Data JPA

| Tema | Regra neste projeto |
|---|---|
| Carregar relação | `join fetch` (JPQL), `@EntityGraph` ou `@BatchSize` — e **cuidado**: com `Pageable` + coleção, o Hibernate pagina **em memória** (aviso `HHH000104`) |
| Associação preguiçosa | `@OneToMany` é `LAZY` por padrão, mas **`@ManyToOne` é `EAGER` por padrão** — toda associação declara `fetch` explicitamente |
| Leitura sem rastreio | `@Transactional(readOnly = true)` + projeção |
| Paginação | `Pageable` / `Page` do Spring Data (no banco) |
| Índices | `@Table(indexes = …)`, `uniqueConstraints` — criados pelo `ddl-auto` neste projeto (sem Flyway, que seria dependência nova) |
| Thread de requisição | Servlet: **uma thread por requisição, bloquear é normal** — a regra vira "transação e lock curtos" |
| Cache de saída | **Recusado** (dados pessoais e saldo mutável) |
| Binário executável | `bootJar` já gerado pelo plugin; exige JRE no servidor — sem `jlink` |
| Relógio de duração | `System.nanoTime()` |
| Compressão | `server.compression.*` (nativo do Spring Boot) |

---

## 1. Sem N+1

Nunca faça uma consulta ao banco por item em um loop. O relacionamento é
resolvido em **uma** consulta (`join fetch`, `@EntityGraph`, subconsulta) —
nunca uma consulta para a lista e outra por item dela.

```java
// CORRETO — detalhe de um consumidor: uma consulta traz o consumidor e os cartões
@Query("select c from ConsumidorEntidade c left join fetch c.cartoes where c.id = :id")
Optional<ConsumidorEntidade> buscarComCartoes(@Param("id") long id);

// CORRETO — a listagem nem toca a relação: projeção só com o que o resumo usa (seção 3)

// ERRADO — N+1: uma consulta para a página, uma por consumidor para os cartões
var consumidores = repositorio.findAll(pagina);
consumidores.forEach(c -> mapeador.paraDominio(c));   // mapeador chama c.getCartoes() → SELECT por item
```

### Regras específicas do JPA

1. **Toda associação declara `fetch` explicitamente.** `@ManyToOne` e
   `@OneToOne` são `EAGER` por padrão: sem `fetch = LAZY`, carregar um
   `CartaoEntidade` já carrega o `ConsumidorEntidade` inteiro.
2. **Coleção + paginação = nunca `join fetch`.** Com `Pageable`, o
   Hibernate não consegue aplicar `LIMIT` sobre uma junção de coleção e
   **carrega tudo e pagina na memória** (aviso `HHH000104`) — exatamente a
   falha dos 50.000 registros, de volta. A listagem não carrega a coleção
   (seção 3); o detalhe por id, que não pagina, usa `join fetch`.
3. **`@BatchSize` é a rede de segurança**, não o plano: onde uma coleção
   precise ser acessada em lote, `@BatchSize(size = 100)` troca N consultas
   por ⌈N/100⌉.
4. **`spring.jpa.open-in-view: false`** (ver [`API.md`](API.md#5-dtos-para-entrada-e-saída))
   — sem isso, um `LAZY` esquecido dispara consulta **durante a
   serialização da resposta**, fora de qualquer revisão de caso de uso.

**Como o N+1 escapa do código (regra de revisão):**

| Sinal | O que verificar |
|---|---|
| Mapeador entidade → domínio chamando `getCartoes()` dentro de um laço/`stream` | a coleção foi carregada na consulta que originou a lista? |
| `@ManyToOne` sem `fetch` | virou `EAGER` sem ninguém pedir |
| Consulta que usa `join fetch` de coleção **e** `Pageable` | paginação em memória |
| "Só umas dezenas de itens" como justificativa | dezenas hoje, 50 mil no próximo import — o padrão é o alvo, não o volume atual |

### Guarda mecânica contra regressão

O Hibernate expõe estatísticas **sem dependência extra**. Um teste fixa o
número de comandos SQL da listagem, de modo que reintroduzir N+1 falha o
build:

```java
// src/test/.../infraestrutura/persistencia/consumidor/ListagemDeConsumidoresTeste.java
// esboço — validar ao implementar
@Test
void deve_executar_no_maximo_dois_comandos_quando_listar_uma_pagina() {
    var estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    estatisticas.setStatisticsEnabled(true);
    estatisticas.clear();

    caso.listar(1, 20);                                   // 20 consumidores, cada um com cartões

    assertTrue(estatisticas.getPrepareStatementCount() <= 2);   // 1 página + 1 count
}
```

**Por quê:** N+1 é o defeito de performance mais caro e menos visível —
a listagem de 20 consumidores vira 21 idas ao banco. Não aparece com 10
registros de teste; aparece em produção com 50 mil, como lentidão
generalizada de que ninguém lembra o motivo.

---

## 2. Paginação Obrigatória

Toda listagem da API impõe paginação (`pagina`/`tamanhoPagina`, com teto
fixo documentado no contrato). **Nenhum** endpoint retorna coleção
ilimitada — nem "temporária", nem "interna", nem "de teste".

```java
// CORRETO — a página é pedida ao banco (LIMIT/OFFSET + ORDER BY), na borda do adaptador
Page<ConsumidorResumoLinha> pagina = repositorio.listarResumos(
    PageRequest.of(numeroDaPagina - 1, tamanho, Sort.by("nome").and(Sort.by("id"))));

// ERRADO — o legado: tudo do banco para a memória, sem limite
@Query(nativeQuery = true, value = "select * from Consumer")
List<Consumer> getAllConsumersList();

// ERRADO — paginação de fachada: o banco devolveu os 50 mil, a aplicação só escondeu
repositorio.findAll().stream().skip(inicio).limit(tamanho).collect(toList());
```

**Regras:**

- **A paginação é regra de desempenho; o formato da fronteira é contrato da
  API** — parâmetros, envelope (`itens`, `total`, `totalPaginas`), teto de
  `tamanhoPagina` (100) e ordenação determinística (desempate por `id`)
  estão em [`API.md`](API.md#9-paginação-filtros-e-ordenação). Este
  documento é a régua de *por que* nenhuma listagem escapa dela.
- **A paginação é aplicada no banco, não na memória.** `findAll()` +
  `stream().skip()` é paginação fingida.
- **Nunca aceitar `Pageable` direto do cliente.** O Spring o monta a partir
  de `?sort=qualquerCampo`, expondo o modelo e permitindo ordenar por
  coluna sem índice. O cliente envia `ordenacao` (enum fechado) e a borda
  monta o `PageRequest`.
- **`Page` custa um `count(*)` a mais por requisição** — é o preço do
  `total`/`totalPaginas` do envelope. Decisão consciente: o contrato quer o
  total. Se a contagem virar gargalo medido (seção 8), o caminho é `Slice`
  (sem total), o que é mudança de contrato e vai para `/api/v2/`.

**Por quê:** coleção ilimitada é a única rota para levar o servidor ao chão
com uma requisição — memória cheia, GC em pânico e rede saturada por dados
que o cliente não ia exibir.

---

## 3. Projeções em vez de Entidades Completas

Para leitura, projete somente os campos que a fronteira vai usar, em vez de
materializar entidades completas. Entidade carregada é memória,
verificação de alteração (*dirty checking*) e payload que ninguém pediu.

**Decisão deste projeto — a listagem devolve um resumo, não o consumidor:**

| Operação | Devolve | Carrega |
|---|---|---|
| `GET /api/v1/consumidores` (lista) | `MensagemResumoConsumidor` — `id`, `nome` | projeção, **sem** cartões, endereço, contatos nem saldos |
| `GET /api/v1/consumidores/{id}` (detalhe) | `MensagemConsumidor` — tudo, incluindo cartões | entidade com `join fetch` dos cartões |

```java
// CORRETO — só as colunas do resumo; sem entidade, sem relação, sem N+1 possível
@Query("select c.id as id, c.nome as nome from ConsumidorEntidade c")
Page<ConsumidorResumoLinha> listarResumos(Pageable pageable);

interface ConsumidorResumoLinha { Long getId(); String getNome(); }   // projeção do Spring Data

// ERRADO — o legado: todas as colunas de todos os consumidores para montar uma lista
@Query(nativeQuery = true, value = "select * from Consumer")
List<Consumer> getAllConsumersList();
```

**Regras:**

- **Leitura não materializa entidade de domínio.** A projeção vira um tipo
  de domínio enxuto (`ResumoDeConsumidor`) no adaptador, e daí uma
  `Mensagem*` na borda ([`API.md`](API.md#5-dtos-para-entrada-e-saída)).
- **`@Transactional(readOnly = true)` em toda consulta de leitura do
  adaptador.** O Spring marca a sessão como somente leitura e o Hibernate
  dispensa o *dirty checking* dessas entidades — o análogo ao
  `AsNoTracking`. Em caminho de escrita (débito, crédito) a transação é de
  escrita, e só ali o rastreio é custo justificado.
- **O objeto completo é carregado só quando é o recurso pedido** — o
  detalhe pode trazer cartões e endereço; a lista, jamais.
- **Projeção e paginação andam juntas** — projetar depois de paginar na
  memória é a paginação fingida da seção 2 com traje de otimização.
- **O saldo nunca vai numa listagem.** Além de peso, é dado financeiro:
  quem lista não precisa dele (ver [seção 5](#5-cache-de-leitura--recusado-de-propósito)).

**Por quê:** materializar entidade completa em leitura paga três vezes —
rede do banco, memória do processo e rede da resposta — e nenhuma das três
foi pedida pelo cliente.

---

## 4. Índices nas Colunas Consultadas

Toda coluna usada em `WHERE`, `JOIN`, `ORDER BY` ou como chave estrangeira
tem índice correspondente — e o índice nasce **com a consulta**, no mesmo
PR, não depois do primeiro relato de lentidão.

```java
// infraestrutura/persistencia/consumidor/CartaoEntidade.java
@Entity
// nomes seguem a convenção de BANCO.md §3: tabela plural, uk_/idx_/fk_
@Table(
    name = "cartoes",
    uniqueConstraints = @UniqueConstraint(name = "uk_cartoes_numero", columnNames = "numero"),
    indexes           = @Index(name = "idx_cartoes_consumidor_id", columnList = "consumidor_id"))
class CartaoEntidade { /* ... */ }

// infraestrutura/persistencia/consumidor/ConsumidorEntidade.java
@Table(
    name = "consumidores",
    uniqueConstraints = @UniqueConstraint(name = "uk_consumidores_documento", columnNames = "documento"),
    indexes           = @Index(name = "idx_consumidores_nome_id", columnList = "nome, id"))   // ORDER BY nome, id
class ConsumidorEntidade { /* ... */ }
```

**Regras:**

- **Índice acompanha a consulta nova.** O legado fazia
  `where FOOD_CARD_NUMBER = ?` (e `FUEL_`, `DRUGSTORE_`) **sem índice
  algum** — três varreduras de tabela por `setBalance`. O número do cartão
  é consultado em todo débito e todo crédito: `unique` + índice nascem
  junto da consulta. **A medição do legado mostra o tamanho real do ganho:**
  com 50.000 consumidores no H2 em memória a varredura custa ~0,2 ms (o plano
  confirma `tableScan`), então o índice **não** é o que resolve a lentidão
  deste projeto; ele se justifica pela integridade (`unique`), pela trava de
  uma linha só e pelo dia em que o volume ou o banco forem outros.
- **O `unique` cumpre dois papéis.** Garante a integridade (rede de
  segurança da porta 1 de [`ERROS.md`](ERROS.md#3-proibição-de-captura-genérica--e-as-duas-portas-de-conversão))
  **e** é o índice que o `@Lock(PESSIMISTIC_WRITE)` usa para travar
  **uma** linha — sem ele, o lock pode varrer e travar mais do que deve.
- **Todo campo ordenável tem índice.** `ordenacao` é um enum fechado
  ([`API.md`](API.md#9-paginação-filtros-e-ordenação)); cada constante dele
  é `ORDER BY` de uma consulta paginada e precisa de índice composto com o
  desempate (`nome, id`).
- **Chave estrangeira com índice explícito.** O H2 cria um índice de FK
  sozinho; o PostgreSQL, não — declarar evita descobrir a diferença em
  produção.
- **Índice tem custo de escrita.** Cada índice a mais encarece todo
  `insert`/`update` da tabela; coluna muito escrita (o `saldo`, que muda a
  cada compra) **não** ganha índice.
- **O plano de execução confirma, o palpite não.** No H2, `EXPLAIN SELECT …`
  mostra se o índice foi usado; "acho que falta índice" é hipótese.
- **Neste projeto o índice vem do `ddl-auto`** a partir das anotações —
  não há migração (Flyway seria dependência nova). Em produção real, a
  mesma definição vira migração versionada (ver `BANCO.md`).

**Por quê:** a diferença entre `WHERE numero = ?` com e sem índice é
varredura de tabela inteira — de milhares de linhas contra uma. É o caso
mais frequente de "ficou lento do nada": o volume cresceu, a consulta
continuou a mesma. Mas a escala conta: num banco em memória e com dezenas de
milhares de linhas, a varredura ainda é barata, e quem escolhe o que otimizar
primeiro é a medição ([seção 8](#8-medir-antes-de-otimizar)), não a regra.

---

## 5. Cache de Leitura — Recusado de Propósito

Cache de leitura costuma valer para conteúdo frequente e estável, com
invalidação por evento. Este projeto **não cacheia**, e isso é decisão, não
omissão — registrada em [`DECISOES.md`](../fundamentos/DECISOES.md):

| O que costuma ser cacheado | Por que aqui não |
|---|---|
| Conteúdo **público** (postagem publicada, categorias) | Tudo aqui é **dado pessoal** (documento, telefone, endereço) ou **financeiro** (saldo) |
| Dado que muda raramente | O saldo muda a **cada compra e a cada crédito** — é o dado mais volátil do sistema |
| Leitura servida a milhares de visitantes iguais | Cada consumidor vê o seu; não há "mesmo conteúdo para todos" a reaproveitar |

**Por quê o saldo nunca é cacheado:** um saldo velho exibido ao cliente é
um erro financeiro visível; pior, se qualquer decisão de débito lesse o
cache em vez do banco, permitiria gastar saldo que já foi gasto. **A
verdade do saldo é a linha travada no banco, e só ela.**

**Consequências práticas (a regra vira proibição):**

- **Nenhum `@Cacheable`/`@EnableCaching`**, nenhum cache de segundo nível
  do Hibernate, nenhum mapa em memória guardando consumidor ou cartão.
- **Respostas da API saem com `Cache-Control: no-store`** — nenhum cache
  intermediário (proxy, CDN, navegador) guarda dado pessoal. Aplicado **uma
  vez**, para todo `/api/v1/**`, por um `WebContentInterceptor` registrado
  em `api.configuracao` (`ConfiguracaoDeWeb`) — nunca por controller:

  ```java
  @Configuration
  public class ConfiguracaoDeWeb implements WebMvcConfigurer {
      @Override
      public void addInterceptors(InterceptorRegistry registro) {
          var interceptor = new WebContentInterceptor();
          interceptor.addCacheMapping(CacheControl.noStore(), "/api/v1/**");
          registro.addInterceptor(interceptor);
      }
  }
  ```

  *(esboço; validar ao implementar)*

- **A saída é tratada como não cacheável**: sem `ETag`, sem `Last-Modified`.
  Revalidação (`304`) só faz sentido para conteúdo que **pode** ser
  guardado.

> **Se um dia houver dado público e estável** (por exemplo, uma tabela de
> tipos de estabelecimento), cache passa a ser permitido **para aquele
> recurso** — invalidação por evento de escrita, TTL só como rede de
> segurança —, nunca para consumidor, cartão ou extrato.

---

## 6. Binário Executável — Não Há Self-Contained

Em Java não existe binário que dispense o runtime no servidor: o artefato é
um *fat jar* executável que precisa de um JRE 17 no destino.

- O artefato de implantação é o **`bootJar`** do plugin do Spring Boot, já
  configurado: `./gradlew bootJar` e `java -jar build/libs/*.jar`.
- **O JRE exigido (17) é parte do contrato de implantação** e fica no
  `README` — não há instalação implícita.
- Empacotar o JRE junto (`jlink`, `jpackage`) ou imagem de contêiner está
  **fora de escopo** do desafio.

---

## 7. Transação e Lock Curtos — o Servidor Não é Assíncrono

No modelo Servlet, **cada requisição ocupa uma thread** (pool do Tomcat,
200 por padrão) e **bloquear essa thread esperando o banco é o modo normal
de operar**; não há proibição de bloquear a thread.

O recurso que realmente se esgota é a **conexão do banco**: o pool Hikari
padrão tem **10 conexões**. E aqui o lock pessimista do débito introduz o
risco:

> Se 10 requisições disputam o **mesmo cartão**, uma segura o lock e as 9
> restantes ficam **bloqueadas dentro de uma transação, cada uma segurando
> uma conexão**. O pool zera, e **qualquer outra requisição do sistema**
> (de outros cartões!) passa a esperar por conexão.

**Regras:**

- **A transação do caso de uso contém só o necessário**: travar o cartão →
  calcular → gravar o cartão → gravar o extrato. Nada mais.
- **Nenhuma chamada externa** (HTTP, fila, e-mail) dentro de transação que
  mantém um cartão travado.
- **Nenhum cálculo caro nem leitura grande entre o lock e o commit.**
- **A trava é tomada o mais tarde possível**: validar entrada, resolver o
  tipo de cartão e calcular o fator **antes** de abrir a transação
  travada, onde isso for possível.
- **A espera pelo lock é menor que a espera por conexão** (seção 9) — para
  que a contenção em um cartão vire um erro rápido e específico (409, tente
  novamente), e não esgote o pool.
- **Dimensionar o pool por medição** (seção 8), não por palpite; o valor
  padrão é ponto de partida.

**Por quê:** o lock pessimista é a escolha **correta** para o saldo (duas
compras legítimas simultâneas devem ambas ter sucesso, em série — ver
[`API.md`](API.md#11-concorrência-idempotência-e-conflitos)); mas ele só é
barato se a janela travada for curta. Transação longa com lock converte
contenção local em indisponibilidade global.

---

## 8. Medir Antes de Otimizar

Toda otimização de performance nasce de **medição**, não de sensação.
Antes de reescrever um caminho quente: meça, registre o número, otimize,
meça de novo — e o ganho (ou a ausência dele) fica registrado no PR.

**O primeiro número a capturar é o do legado, antes de mexer em qualquer
código.** Com a aplicação legada ainda intacta, meça `GET
/consumer/consumerList` contra uma base de ~50.000 consumidores — é a linha
de base de todo o PR. Depois da refatoração, esse número não pode ser
reconstruído.

### Linha de base do legado — medida

Medida antes de qualquer mudança no código de produção, com 50.000
consumidores gerados em código (`GeradorDeConsumidores`, só em `src/test`), em
JDK 17, 12 CPUs, H2 em memória e `MockMvc`, **mediana de 10 chamadas depois de
3 de aquecimento**, em três execuções com JVM nova:

| Caminho | Resultado | O que significa |
|---|---|---|
| `GET /consumer/consumerList` | **1130 a 1145 ms**; resposta de **23 MB** | é o gargalo: devolve a tabela inteira, com todos os campos |
| Busca por cartão (alimentação, combustível, farmácia) | 0,18 a 0,33 ms, igual no meio e no fim | varredura de tabela confirmada pelo plano, mas barata neste volume |
| `GET /consumer/buy` | 5 a 7 ms | a compra inteira, com três buscas, atualização e extrato |

Estes números são a referência a comparar depois da refatoração. **Vale o
relativo, não o absoluto**: o H2 em memória não tem latência de rede nem de
disco, e a máquina de medição não é a de produção. Para repetir:

```bash
_JAVA_OPTIONS=-Xmx2g MEDICAO=1 ./gradlew test --tests '*LinhaDeBaseDoLegadoTeste'
```

O relatório sai em `build/medicao/linha-de-base-do-legado.txt`. O `-Xmx2g` é
necessário porque o JVM de teste do Gradle tem 512 MB por padrão, e a medição
(que carrega a base inteira e respostas de 23 MB) não cabe nele. A variável
`_JAVA_OPTIONS` é usada porque tem precedência sobre o `-Xmx` que o próprio
Gradle passa ao worker.

**Regras:**

- **Duração é medida com relógio monotônico**: `System.nanoTime()`, nunca a
  diferença de `System.currentTimeMillis()`/`Instant.now()` — o relógio de
  parede pode andar para trás (ajuste de NTP) e a duração vira negativa sem
  erro.

  ```java
  long inicio = System.nanoTime();
  caso.listar(1, 20);
  long milissegundos = Duration.ofNanos(System.nanoTime() - inicio).toMillis();
  ```

- **Mais de uma medição, com aquecimento.** A primeira chamada paga
  carregamento de classe e abertura de conexão; registre a **mediana** de
  várias execuções após descartar as primeiras. (O JMH seria a ferramenta
  correta, mas é dependência nova — proibida.)
- **O dado de teste é construído no teste.** Um gerador **só em
  `src/test`** (`GeradorDeConsumidores`) cria os 50.000 registros —
  programaticamente, nunca carregados de arquivo — e **não entra na
  aplicação**.
- **O número medido vai no PR, com o contexto honesto.** "De 840 ms para 95
  ms com 50.000 consumidores, mediana de 10 execuções" é revisável — e a
  nota "medido em H2 em memória; o ganho **relativo** vale, o absoluto
  não" também.
- **Sem otimização prematura fora do caminho quente.** Abstração, mapeador
  manual e `Resultado<T,E>` têm custo desprezível frente ao I/O de banco —
  não se troca legibilidade por micro-ganho em código que executa uma vez
  por requisição. O custo está nas consultas (seções 1 a 4), não nos
  objetos.
- **Regressão de performance é regressão.** Onde um número já quebrou uma
  vez, ele vira teste com tolerância (como o de contagem de comandos SQL da
  seção 1) — número que ninguém mede volta sem aviso.

**Por quê:** otimizar sem medir paga o custo da mudança (complexidade,
código menos legível, risco de defeito) por um ganho que ninguém confirma
existir — frequentemente em código que não era o gargalo. Aqui o gargalo
quase sempre é o banco.

---

## 9. Timeouts e Limites

Toda fronteira de I/O tem **timeout explícito e limite de tamanho**
declarados — o padrão da ferramenta não é aceito por omissão.

| Fronteira | Limite | Onde se decide |
|---|---|---|
| Obter conexão do pool | `spring.datasource.hikari.connection-timeout` (padrão 30 s — **longo demais**) e `maximum-pool-size` | `application.yml` |
| Espera por lock pessimista | no H2, o parâmetro `LOCK_TIMEOUT` (ms) da URL; a dica `javax.persistence.lock.timeout` **é ignorada pelo H2** (ver a nota abaixo) | `spring.datasource.url` |
| Duração de transação | `@Transactional(timeout = …)` (segundos) | caso de uso |
| Execução de consulta | dica `javax.persistence.query.timeout` — **a validar**, como a de trava | `@QueryHints` ou propriedade do JPA |
| Listagem (`tamanhoPagina`) | teto 100, validado com `@Max` | [`API.md`](API.md#9-paginação-filtros-e-ordenação) |
| Tamanho de cada campo de texto | `@Size(max = …)` nas `Mensagem*` | Bean Validation |
| Corpo da requisição | **sem propriedade nativa para JSON** no Spring MVC — o teto prático é o `@Size` de cada campo e o limite do servidor/proxy à frente | validar ao implementar |
| HTTP de saída | — | não há integração externa neste projeto |

```yaml
# application.yml — pontos de partida; decidir com medição (seção 8)
spring:
  datasource:
    url: jdbc:h2:mem:consumer-pat;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=3000   # ms — MENOR que a espera por conexão
    hikari:
      connection-timeout: 5000        # ms — o padrão de 30 s só adia o esgotamento do pool
      maximum-pool-size: 10
      exception-override-class-name:  # estourar a trava não pode descartar a conexão (nota abaixo)
        br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.ErroDeTravaNaoDescartaConexao
```

> **Validado, e a suposição estava errada.** Com o H2 2.1 e o Hibernate 5.6, a
> dica `javax.persistence.lock.timeout` é ignorada **em silêncio**, tanto como
> propriedade global quanto como `@QueryHints` na consulta: a espera por uma
> trava seguiu o padrão do H2, de 4 segundos, com o teto configurado em 500 ms.
> O que funciona é o parâmetro `LOCK_TIMEOUT` da URL. Um timeout que não dispara
> não é um timeout, e o teste de trava existe para não deixar a configuração
> ser decorativa.
>
> **Segunda armadilha:** o estouro da trava chega do H2 com o estado `HYT00`, e o
> Hikari o trata como conexão quebrada: fecha a conexão, o *rollback* seguinte
> falha com "Connection is closed" e o Spring devolve essa falha no lugar da
> exceção de trava. `ErroDeTravaNaoDescartaConexao` diz ao pool que esse código
> não estraga a conexão, e a exceção chega como `PessimisticLockingFailureException`.
>
> **Ainda não validado:** o teto de execução de consulta
> (`javax.persistence.query.timeout`). Não está configurado: configurá-lo sem
> prova repetiria o erro acima.

**Regras:**

- **Timeout é configurado, não herdado.** Herdar o infinito (ou os 30 s) da
  ferramenta mantém a requisição pendurada segurando thread e conexão.
- **Timeout vira erro tratado, nunca exceção crua na fronteira** —
  `PessimisticLockingFailureException`, `QueryTimeoutException` e falha de
  conexão são tratadas por `@ExceptionHandler` por tipo exato (porta 2 de
  [`ERROS.md`](ERROS.md#3-proibição-de-captura-genérica--e-as-duas-portas-de-conversão)),
  com o corpo padronizado — jamais 500 com stack trace. *(Esgotamento do
  pool talvez mereça 503 em vez de 500; decisão em aberto, fora das
  tabelas atuais de `API.md`.)*
- **Limite é validado na entrada, antes de processar.** `@Valid` recusa o
  corpo inválido **antes** de o caso de uso abrir transação.

**Por quê:** sem timeout, a falha lenta é pior que a falha rápida: o banco
pendurado acumula requisições esperando, cada uma segurando conexão e
thread, até o servidor inteiro recusar atendimento — um problema de um
cartão vira indisponibilidade de todos.

---

## 10. Compressão e Payload Enxuto

Resposta da API sai comprimida, e o que viaja na rede é o mínimo
compatível com o contrato.

```yaml
# application.yml — nativo do Spring Boot, sem dependência
server:
  compression:
    enabled: true
    mime-types: application/json,application/problem+json
    min-response-size: 1024        # bytes — abaixo disso a compressão não compensa
```

**Regras:**

- **Compressão na configuração do servidor**, uma vez — nunca por
  controller.
- **Não há `ETag`/`Cache-Control` de revalidação**: a seção 5 proíbe cache
  de dado pessoal, então toda resposta sai `no-store`. O ganho de
  resposta `304` não existe aqui, **de propósito**.
- **JSON sem enfeite**: o Spring Boot não indenta a resposta por padrão
  (manter assim — nunca `spring.jackson.serialization.indent-output`);
  `default-property-inclusion: non_null`
  ([`API.md`](API.md#6-serialização-determinística)); sem coleção inteira
  embutida onde o cliente vai paginar.
- **Projeção é a primeira compressão.** Antes de comprimir, o payload
  precisa ser pequeno — a lista de resumos (`id`, `nome`) é ordens de
  grandeza menor que a lista de consumidores completos do legado, e
  compressão nenhuma conserta payload inchado.

**Por quê:** a resposta típica é texto (JSON) — a compressão tira a maior
parte do tráfego sem mudar o contrato. O ganho maior, porém, vem de não
enviar o que ninguém pediu (seção 3).

---

## 11. Mídia — Não se Aplica

O projeto **não recebe nem serve arquivos**: não há upload, download nem
processamento de imagem, então processar mídia no upload, transmitir em
stream e usar cache longo com URL imutável não se aplicam.

Se um upload aparecer: processamento no momento da entrada, transmissão
**em stream** com memória constante e teto de tamanho validado **antes** de
alocar — e esta seção é reescrita no mesmo PR.

---

## Anti-Padrões

| Anti-padrão | Correto |
|---|---|
| `select * from Consumer` devolvendo tudo (legado) | Página + projeção (seções 2 e 3) |
| `findAll().stream().skip().limit()` | `Pageable` aplicado no banco (seção 2) |
| `Pageable` recebido direto do cliente (`?sort=…`) | `ordenacao` como enum fechado; a borda monta o `PageRequest` |
| Consulta por item dentro de laço / mapeador chamando `getCartoes()` em loop | Projeção sem relação, ou `join fetch` fora de paginação (seção 1) |
| `join fetch` de coleção com `Pageable` | Listar sem a coleção; detalhe com `join fetch` (aviso `HHH000104`) |
| `@ManyToOne`/`@OneToOne` sem `fetch = LAZY` | `fetch` explícito em toda associação |
| `open-in-view` ligado | Desligado — `LAZY` não dispara consulta na serialização |
| Retornar entidade completa em leitura / saldo numa listagem | Projeção; saldo só no detalhe |
| Consulta de leitura sem `@Transactional(readOnly = true)` | Leitura somente leitura |
| Coluna consultada sem índice (`where FOOD_CARD_NUMBER = ?`) | `unique`/`@Index` na mesma entrega (seção 4) |
| Índice em coluna muito escrita (`saldo`) | Sem índice — custo de escrita sem ganho |
| `@Cacheable`, cache 2º nível ou mapa em memória de consumidor/cartão | Nenhum cache de dado pessoal ou financeiro (seção 5) |
| Resposta com dado pessoal sem `Cache-Control: no-store` | `no-store` em `/api/v1/**`, uma vez, no interceptor |
| Chamada externa dentro de transação com cartão travado | Transação só com lock → cálculo → gravação (seção 7) |
| Espera por lock maior que a espera por conexão | Lock timeout < connection timeout (seções 7 e 9) |
| Otimizar por sensação, sem medir | Medir antes (inclusive o legado), medir depois, registrar no PR (seção 8) |
| Duração medida com `currentTimeMillis`/`Instant.now()` | `System.nanoTime()` |
| Dado de teste em arquivo, ou gerador dentro de `src/main` | Gerador programático em `src/test` |
| I/O herdando o timeout padrão da ferramenta | Timeout explícito configurado (seção 9) |
| Timeout que a ferramenta ignora em silêncio | Validar que dispara (teste) |
| Compressão ligada por controller | `server.compression` uma vez |

---

## Checklist

Antes de abrir um PR que mexe em consulta, listagem, transação ou
configuração de desempenho:

- [ ] Nenhuma consulta nova foi feita dentro de um laço (nem por um mapeador que acessa coleção `LAZY`)?
- [ ] Toda associação tem `fetch` explícito, e nenhum `join fetch` de coleção convive com `Pageable`?
- [ ] Toda listagem tem paginação com teto e ordenação determinística — no banco, não na memória?
- [ ] O `ListagemDeConsumidoresTeste` (contagem de comandos SQL) continua passando?
- [ ] Leitura projeta só os campos usados, em `@Transactional(readOnly = true)`, sem saldo na listagem?
- [ ] Toda coluna nova em `WHERE`/`JOIN`/`ORDER BY`/FK veio com índice ou `unique` na mesma entrega?
- [ ] Nenhum cache novo de dado pessoal ou financeiro; respostas continuam `no-store`?
- [ ] A transação travada contém só lock → cálculo → gravação, sem chamada externa?
- [ ] O timeout de lock é menor que o de conexão, e foi validado que dispara?
- [ ] Se otimizei algo: medi antes (inclusive o legado), medi depois, e os números estão no PR, com o contexto (H2, volume, mediana)?
- [ ] O gerador de dados de teste ficou em `src/test`?
- [ ] Toda fronteira de I/O nova tem timeout explícito e limite de tamanho declarados?

---

**[Estrutura →](../fundamentos/ESTRUTURA.md)**
