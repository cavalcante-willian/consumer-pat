[← Índice](../../PADRONIZACAO_INDEX.md) · [← SEGURANCA.md](SEGURANCA.md)

# Testes — Java 17 / JUnit 5 / Spring Boot 2.7

> **Escopo**: a política de testes para **Java 17 + JUnit 5 + Spring Boot
> Test**. A tese é que a suíte é **contrato de comportamento**, não métrica
> de linha. O ferramental vem todo do `spring-boot-starter-test`, **sem
> dependência nova**; o oráculo são as regras do enunciado; e há uma seção que
> consolida os testes que os outros documentos exigem. Ver
> [Ferramental e Premissas](#ferramental-e-premissas).
>
> **Nota sobre exemplos**: os trechos de código e nomes de classe abaixo
> são ilustrativos e **não foram executados** — a regra normativa é o texto
> conceitual. O código atual ainda é o **legado do desafio**, cuja única
> prova é um `contextLoads()`; este documento descreve a suíte pretendida
> (ver [`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md#este-documento-é-vivo)).

Define como a suíte de testes é tratada como contrato de comportamento:
prioridade de cobertura por superfície de risco, tipos de teste e seus
limites, banco real em integração, nomenclatura, fixtures, cobertura
mínima, execução local, regras do enunciado como oráculo, estrutura de um
teste, substitutos de fronteira, determinismo — e o **inventário dos testes
que o resto da padronização exige**.

**Ferramental (tudo já no classpath, nenhum item novo):**

| Para quê | Ferramenta | Vem de |
|---|---|---|
| Executor e ciclo de vida | **JUnit 5 (Jupiter)** — `@Test`, `@ParameterizedTest` | `spring-boot-starter-test`; `useJUnitPlatform()` já está no `build.gradle` |
| Asserções | **AssertJ** — `assertThat(...)` (e `assertInstanceOf` do JUnit para tipos) | `spring-boot-starter-test` |
| Contexto Spring, JPA, HTTP | `@SpringBootTest`, `@DataJpaTest`, `@AutoConfigureMockMvc`, `MockMvc` | `spring-boot-starter-test` |
| Banco | **H2 em memória** | já é dependência de execução (`runtimeOnly`) |

`@Test`, `assertThat` etc. ficam em inglês por serem API de framework; o que
é do projeto (nomes de classe, de método, comentários de fase) é em português.

---

## Ferramental e Premissas

| Tema | Regra |
|---|---|
| Framework | JUnit 5 (`@Test`/`@ParameterizedTest`) |
| Asserção | AssertJ (`assertThat`) — **uma** biblioteca em todo o projeto |
| Comando | `./gradlew test` (exige JDK 17 — ver [seção 9](#9-execução-local)) |
| Banco de integração | **H2 em memória**, schema do `ddl-auto`; **sem matriz de bancos** (Testcontainers seria dependência nova) |
| Schema do teste | o mesmo `ddl-auto` da aplicação (não há migração — Flyway seria dependência nova) |
| Oráculo | as **regras do enunciado** ([seção 10](#10-regras-do-enunciado-como-oráculo)); o legado não é referência confiável |
| Relógio | **`java.time.Clock`** injetado; `Clock.fixed(...)` no teste |
| Gate de merge | gate `CI` do fork ([`CICD.md`](CICD.md#2-gate-de-merge)) + checklist do PR |
| Substituto de contexto | **`@MockBean`/`@SpyBean` proibidos** (são *mock* disfarçado de bean) |

---

## 1. Testes Como Contrato, Não Como Métrica de Linha

"Os testes passam" só tem valor se cobrir exatamente os pontos onde erros
silenciosos são mais prováveis e caros. Cobertura de linha genérica não é
objetivo; cobertura de comportamento crítico é.

**O que faz um teste ser contrato:**

- **Ele falharia se o comportamento mudasse.** A pergunta de revisão: se
  alguém quebrar silenciosamente esta regra, este teste fica vermelho? Teste
  que passa independente do que o código faz é documentação decorativa.
  *(Não há ferramenta de mutação neste projeto — seria dependência nova; a
  pergunta é feita por quem revisa.)*
- **Ele verifica comportamento observável** — saída, efeito, resposta,
  estado persistido — nunca caminho interno
  ([seção 6](#6-nunca-teste-detalhes-de-implementação)).
- **Ele expressa a regra em linguagem de domínio**, para que ler o teste
  seja ler a especificação ([seção 5](#5-nomenclatura-de-testes)).

**O que a métrica de linha não diz:** quantas condições de borda existem, se
o assert confere o campo certo, se o teste falharia com dados diferentes.
Linha coberta é necessária, não suficiente.

**Por quê:** aqui os erros mais caros não explodem — saldo que passa de
negativo sem aviso, dois débitos simultâneos que se sobrescrevem, `PUT` que
zera o saldo. O legado tinha **todos** esses defeitos e **nenhum** teste que
os apanhasse. Meta de porcentagem produz o efeito inverso do desejado: teste
escrito para satisfazer número, não para capturar comportamento.

---

## 2. Prioridade de Cobertura

A cobertura é priorizada pela **superfície de risco** — onde um erro
silencioso é mais provável e mais caro:

| Prioridade | Área | Motivo |
|---|---|---|
| **Crítica** | `RegistrarCompraCaso` e `CreditarCartaoCaso` (fator do tipo de cartão, saldo, atomicidade com o extrato) | Dinheiro: erro silencioso aqui altera saldo de consumidor |
| **Crítica** | Concorrência sobre o saldo (lock pessimista) | O legado perdia atualizações (*lost update*) |
| **Crítica** | Contrato da API de compra e crédito (status, corpo de erro, nenhum vazamento) | Superfície externa; e o legado respondia 500 por `NullPointerException` |
| **Crítica** | `CartaoAdaptador` (trava, unicidade do número) | Ponto de maior fragilidade a dialeto e a transação |
| **Alta** | `TipoCartao` (fatores 0,90 / 1,00 / 1,35) e `Cartao.debitar/creditar` | Regras de negócio puras — baratas de testar, caras de errar |
| **Alta** | Validação das `Mensagem*` (Bean Validation) e rejeição de campo desconhecido | Primeira linha de defesa; impede o saldo no `PUT` |
| **Alta** | Cabeçalhos de segurança e `no-store` | Controles que falham em silêncio |
| **Média** | Atualização, consulta e listagem paginada de consumidor | CRUD e projeção |
| **Baixa** | Controllers (camada fina) | Delegam; cobertos pelos testes de contrato |

*(Autenticação e autorização não aparecem: estão fora de escopo —
[`SEGURANCA.md`](SEGURANCA.md#9-riscos-conhecidos-e-fora-de-escopo).)*

**Como usar a tabela:**

- **Risco = probabilidade × custo.** Dinheiro e entrada externa aparecem no
  topo porque ambas as metades são altas.
- **Área fora da tabela é julgada pela mesma régua** — nova superfície entra
  na tabela **no mesmo PR** que a cria.
- **A prioridade decide onde escrever teste primeiro**, não onde escrever
  *apenas*.

**Por quê:** esforço de teste é finito; distribuído uniformemente, atinge
com a mesma profundidade o que tem alto risco e o que não tem. Priorizar é
decidir, de propósito, onde "testado" começa.

---

## 3. Tipos de Teste

| Tipo | Quando usar | Mecanismo | Exemplo |
|---|---|---|---|
| **Unitário** | Lógica pura: domínio, caso de uso com fakes, mapeamento | JUnit puro, sem Spring | `TipoCartaoTeste`, `RegistrarCompraCasoTeste` |
| **Integração** | Adaptador de persistência contra banco real | `@DataJpaTest` + H2 | `CartaoAdaptadorTeste` |
| **Contrato (API)** | Endpoints: status, corpo, formato de erro, cabeçalhos | `@SpringBootTest` + `@AutoConfigureMockMvc` | `CartaoControllerContratoTeste` |
| **End-to-end** | Fluxo completo criar → creditar → comprar → listar | `@SpringBootTest` + `MockMvc` encadeado | `FluxoDeCompraTeste` |
| **Concorrência** | Invariante sob acesso simultâneo | `@SpringBootTest` + threads | `ConcorrenciaDeCompraTeste` |
| **Desempenho/estrutura** | Número já medido (contagem de SQL) | `@DataJpaTest` + estatísticas do Hibernate | `ListagemDeConsumidoresTeste` |
| **Arquitetura** | Regra de dependência, códigos de erro | JUnit puro, varrendo fontes/classes | `RegraDeDependenciaTeste`, `CodigosDeErroTeste` |

**O que cada tipo cobre — e o que não cobre:**

| Tipo | Detecta | Não detecta |
|---|---|---|
| Unitário | Regra de domínio, decisão, fator, saldo | Mapeamento JPA, contrato HTTP, SQL, lock |
| Integração | Mapeamento, `unique`, trava, transação, índice | Comportamento da fronteira HTTP |
| Contrato | Status, corpo, versão, formato de erro, cabeçalhos | Regras internas de negócio, SQL gerado |
| E2E | Orquestração entre camadas, fluxo feliz e seus desvios | Casos de borda individuais (caro demais) |
| Concorrência | *Lost update*, deadlock, esgotamento de pool | Correção funcional da regra (isso é unitário) |

**Regras:**

- **O tipo errado é caro nos dois sentidos.** E2E testando o fator do cartão
  é lento e opaco onde o unitário é rápido e exato; unitário "testando"
  repositório com mock só confirma que o mock foi configurado.
- **Os tipos sobem na cadeia, não substituem uns aos outros.** Unitários são
  a maioria; integração cobre as fronteiras reais; contrato protege quem
  consome; E2E é o fio mínimo que prova que as camadas se conectam.
- **E2E não é meta de quantidade.** Um por jornada real.
- **`@DataJpaTest` não carrega os `@Configuration` de `api`.** O adaptador é
  instanciado à mão no teste (`new CartaoAdaptador(repositorio)`) — o que
  **confirma** a regra de composition root único: sem `@Component` no
  adaptador, o teste constrói exatamente o que a produção constrói.
- **Entidade e repositório são públicos, mas só a persistência os importa**
  ([`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md#a-regra-é-verificável-não-de-boa-vontade)),
  e o teste de persistência mora nela — então **consegue** alcançá-los. A tentação
  é validar a entidade em vez do comportamento; a regra é testar **pela
  porta pública** (a interface de porta), como o resto do sistema faria.

**Por quê:** cada tipo é uma pergunta diferente. "O fator do cartão está
certo?" é unitário; "o `unique` impede dois cartões com o mesmo número?" é
integração; "o cliente recebe 422 com o corpo prometido?" é contrato.
Responder com o teste errado produz suíte verde que não cobre o que precisa.

---

## 4. Banco de Dados em Testes de Integração

Testes de integração que tocam banco usam um **banco real, nunca mockado**:
**H2 em memória**, o mesmo provedor da aplicação, com o schema gerado pelo
mesmo `ddl-auto` de execução.

```java
// CORRETO — banco real, adaptador construído à mão, sem Spring além do slice JPA
@DataJpaTest
class CartaoAdaptadorTeste {

    @Autowired CartaoRepositorioJpa repositorio;       // só a persistência o importa: o teste mora nela

    @Test
    void deve_recusar_numero_duplicado_quando_gravar_dois_cartoes_com_o_mesmo_numero() {
        // Preparar
        var adaptador = new CartaoAdaptador(repositorio);
        adaptador.inserir(FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comNumero("5555000011112222").construir());

        // Agir + Verificar — a restrição unique é o que o banco, e só ele, garante
        assertThatThrownBy(() -> adaptador.inserir(FabricaDeCartoes.doTipo(TipoCartao.FARMACIA).comNumero("5555000011112222").construir()))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}

// ERRADO — mock do repositório: só prova que o método foi chamado
var repositorio = Mockito.mock(CartaoRepositorioJpa.class);
```

**Regras:**

- **Mock de repositório não testa adaptador.** O mapeamento errado, o
  `unique` ausente e a consulta que não traduz aparecem apenas contra banco
  real.
- **O schema do teste é o da aplicação.** Nada de script de DDL paralelo ao
  teste. *(Não há migrações neste projeto; quando houver, o teste passa a
  aplicá-las.)*
- **Cada teste é dono do seu estado.** Nenhum teste depende de dado deixado
  por outro ([seção 13](#13-determinismo--nada-de-flaky)): cada um cria seus
  consumidores e cartões com **números únicos** (a fábrica, seção 7, gera
  de uma sequência).
- **`@DirtiesContext` é proibido.** Derruba o contexto em cache e torna a
  suíte lenta; o isolamento vem de dados próprios, não de reiniciar o
  Spring.
- **`@Transactional` na classe de teste (rollback automático) só em
  `@DataJpaTest`.** Em contrato, E2E e concorrência ele **mascara** o
  comportamento transacional real e não alcança as outras threads — esses
  testes limpam por dados únicos.

### O que uma matriz de bancos capturaria — e aqui falta

Rodar a suíte em **cada provedor** (PostgreSQL, MySQL em container) é a
prática ideal. Este projeto tem **um** provedor (H2), e Testcontainers seria
dependência nova. **Limite reconhecido:** "passa no H2, falha no PostgreSQL"
é o bug que a matriz capturaria e que aqui **não** é capturado — em
particular o comportamento do **lock pessimista** e das **mensagens de
violação de restrição** muda entre bancos. Isso vai na tabela de riscos do PR.

### Os testes de integração também verificam as suposições dos documentos

Alguns pontos de [`ERROS.md`](ERROS.md) e [`PERFORMANCE.md`](PERFORMANCE.md)
estão marcados "confirmar ao implementar". Eles viram teste (seção 14):

- o H2 **respeita** o timeout de lock (um timeout que não dispara não é
  timeout);
- capturar exceção de persistência **dentro** de `@Transactional` produz
  `UnexpectedRollbackException` no commit;
- `getPrepareStatementCount()` conta o que o teste de N+1 assume.

**Por quê:** a abstração só é verdadeira se o comportamento é verificado
onde ele acontece. Banco mockado esconde o único tipo de defeito que o
adaptador pode ter — divergência entre o que o modelo diz e o que o banco faz.

---

## 5. Nomenclatura de Testes

O nome do teste é a especificação legível do comportamento. Classes de teste
terminam em `Teste`; métodos seguem o padrão:

```text
deve_<resultado>_quando_<condição>
deve_<resultado>_se_<estado prévio>
```

`quando` introduz o evento que dispara o comportamento; `se` introduz o
estado prévio que o condiciona. Minúsculas com sublinhado (formato já
definido em [`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md#6-testes-espelham-a-estrutura-de-produção)),
sem acento nem cedilha (o identificador vai a arquivo, shell e relatório).

```java
// CORRETO — nome comunica cenário, condição e resultado
@Test void deve_aplicar_desconto_de_dez_por_cento_quando_compra_no_cartao_de_alimentacao() { /* ... */ }
@Test void deve_recusar_a_compra_quando_saldo_insuficiente() { /* ... */ }
@Test void deve_manter_o_saldo_se_a_compra_for_recusada() { /* ... */ }

// ERRADO — não comunica condição nem resultado
@Test void test1() { /* ... */ }
@Test void testeCompraComErro() { /* ... */ }
```

**Regras:**

- **Um método, um comportamento.** Várias entradas do mesmo comportamento são
  variações num `@ParameterizedTest`, não um laço de cenários dentro do teste
  ([seção 11](#11-um-ato-por-teste--preparar-agir-verificar)).
- **A classe nomeia o sistema testado, com o sufixo `Teste`**; a árvore de
  pastas espelha a de produção. Teste de contrato leva `ContratoTeste`; de
  fluxo, `Fluxo…Teste`.
- **Sem `@DisplayName`.** O nome do método já é a especificação; um segundo
  texto é duplicação que apodrece separado.

**Por quê:** teste mal nomeado só comunica alguma coisa quando falha — e,
na falha, o nome é o que diz *o que era esperado*. Se isso exige abrir o
corpo do método, ele deixou de ser documentação executável.

---

## 6. Nunca Teste Detalhes de Implementação

Teste comportamento observável, não método privado ou estado interno. Se um
método privado precisa de teste direto, ele provavelmente deveria ser uma
classe própria.

**Sinais de que o teste pegou o detalhe errado:**

- **Refatoração quebra o teste sem mudar comportamento** — renomear campo
  interno, extrair método, trocar a estrutura de dados.
- **O teste exercita método privado, estado interno ou ordem de chamadas
  internas.**
- **O teste toca `ConsumidorEntidade`/`CartaoEntidade`** (restritas à persistência)
  para conferir como o dado ficou, em vez de reler pela **porta** e conferir
  o que o domínio enxerga.
- **O mock é de um método interno da própria camada.**

**Por quê:** teste acoplado a detalhe transforma refatoração — que devia ser
barata — em revisão cara. E o efeito pior é indireto: time que sabe que
refatorar quebra teste para de refatorar.

---

## 7. Fixtures de Dados

Dados de teste são **construídos explicitamente no teste**,
programaticamente — nunca carregados de arquivos externos, nunca de dump de
produção.

```java
// CORRETO — a fábrica tem valores válidos por padrão; o teste declara só a diferença
var cartao = FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("50.00").construir();

// ERRADO — fixture misteriosa vinda de fora
var cartao = carregarJson("cartao-valido.json");   // de onde veio? o que contém?
```

**Regras:**

- **Fábricas em `src/test`** (`FabricaDeCartoes`, `FabricaDeConsumidores`,
  `FabricaDeCompras`), com **método `com*`** para sobrescrever só o que
  importa ao cenário: o teste declara a diferença, o resto vem preenchido.
- **Números únicos por padrão, onde há banco.** Nos testes de persistência a
  fábrica de cartão gera o número de uma sequência (`AtomicLong`) — dois testes
  nunca colidem no `unique`, o que sustenta o isolamento da seção 4. Enquanto só
  há testes com fakes em memória, que não compartilham nada entre si, o número
  padrão é fixo (`FabricaDeCartoes.NUMERO_PADRAO`), e a sequência chega junto do
  primeiro teste de persistência.
- **Dinheiro é construído de `String`** (`new BigDecimal("50.00")`, ou
  `Dinheiro` a partir dele), nunca de `double` — `new BigDecimal(0.1)` carrega
  o erro binário que o `BigDecimal` existe para evitar
  ([`VALORES.md`](../codigo/VALORES.md#6-dinheiro-e-percentuais)).
- **Dinheiro se compara por valor, não por escala**: `BigDecimal.equals`
  considera `2.0` diferente de `2.00`. Em `BigDecimal` cru use
  `isEqualByComparingTo`; `Dinheiro` já normaliza a escala e aceita `isEqualTo`.
  O valor **esperado** de um cálculo guarda a fração (`33.33 × 0.90 =
  29.997000`), nunca o valor já arredondado para centavos.
- **Volume grande é gerado no teste.** O `GeradorDeConsumidores` (50 mil
  registros) vive **só** em `src/test`
  ([`PERFORMANCE.md`](PERFORMANCE.md#8-medir-antes-de-otimizar)).
- **Aleatoriedade é semeada** (`new Random(42)`); dado aleatório sem semente
  é falha não reproduzível.
- **Nada de dado real**: documento, telefone e cartão de teste são
  fabricados, nunca copiados de produção
  ([`SEGURANCA.md`](SEGURANCA.md#5-logs-sem-dados-sensíveis)).

**Por quê:** fixture opaca é dependência invisível: quando o arquivo muda, o
teste muda de comportamento sem ninguém editar o teste. Fixture declarada no
teste é o cenário gravado junto do que ele promete verificar.

---

## 8. Cobertura Mínima

Não há porcentagem global obrigatória. A regra é: **toda alteração de código
de produção deve ser acompanhada por teste que cobre o comportamento
alterado**, priorizando a superfície de risco da
[seção 2](#2-prioridade-de-cobertura).

**O que "coberto" quer dizer aqui:**

- **O teste falharia sem a mudança** — comportamento verificado, não linha
  executada.
- **Erro novo tem teste que o provoca** — toda variante nova de `ErroDominio`
  é contrato ([`ERROS.md`](ERROS.md)); teste que nunca força aquela falha não
  cobre o tratamento. (O `CodigosDeErroTeste` cobre o *formato*; o
  *comportamento* é de um teste do caso de uso.)
- **Remover teste é decisão consciente** — acontece no mesmo PR da remoção
  do comportamento, com justificativa no commit.

**Onde a regra é aplicada:** o gate `CI` do fork roda `./gradlew test`
([`CICD.md`](CICD.md#2-gate-de-merge)); localmente, o checklist do PR e o comando
único `./gradlew test` verde. O
plugin `jacoco` **acompanha** o Gradle, mas **baixa** o agente do JaCoCo do
Maven Central na primeira execução — é artefato novo no build, no mesmo
espírito de uma dependência, e portanto **não é usado** neste projeto (ver
[`BUILD.md`](BUILD.md)). Cobertura se verifica pela tabela da seção 14 e pela
revisão, não por relatório de linha.

**Por quê:** porcentagem global obriga a distribuir esforço onde o risco não
está para mover o número. A regra "mudança com teste" tem o efeito certo:
cada comportamento novo nasce verificado.

---

## 9. Execução Local

Todos os testes rodam localmente **sem dependência de rede externa, serviços
de nuvem ou credenciais reais** — após a restauração de dependências, a
suíte é autocontida.

```bash
./gradlew test
```

**Regras:**

- **Exige JDK 17.** O Gradle 7.6 do wrapper **não executa o build** em JDKs
  mais novos (verificado com o JDK 27: `compileJava` falha, e um *toolchain*
  não corrige) — em máquina cujo JDK padrão seja outro, é preciso `JAVA_HOME`
  apontando para o 17. O README do enunciado dizia "11 ou 17"; com `record` e
  `sealed` passa a ser só **17**, e o README é atualizado
  ([`BUILD.md`](BUILD.md#1-build-autocontido-no-repositório)).
- **O mesmo comando no local e em qualquer pipeline futuro** — não existe
  "modo CI".
- **Credencial real jamais em teste.** A única credencial é o `sa/sa` do H2
  em memória, pública no README
  ([`SEGURANCA.md`](SEGURANCA.md#4-segredos--regra-curta)).
- **Sem passo manual para a suíte subir**: o banco é o H2 em memória, criado
  pelo próprio contexto de teste.
- **Sem porta fixa.** Testes que sobem servidor usam porta aleatória
  (`RANDOM_PORT`); MockMvc nem abre porta.
- **A suíte inteira roda antes do push.** Se demorar demais, o gargalo é da
  suíte — e o principal culpado é derrubar o contexto do Spring, o que a
  seção 4 proíbe.

**Por quê:** teste que só roda em ambiente especializado é teste que não
roda. Autocontenção é a condição de o gate local ser útil.

---

## 10. Regras do Enunciado Como Oráculo

Comparar o comportamento novo com uma implementação de referência seria o
ideal, mas aqui o código legado **não é oráculo confiável** — ele é o objeto
da revisão e está cheio de defeitos. O oráculo são as **regras escritas no enunciado**
(os comentários do `ConsumerController` legado):

| Regra do enunciado | Teste (caracterização) |
|---|---|
| Compra no cartão de **alimentação** recebe **10% de desconto** | `deve_cobrar_noventa_por_cento_quando_compra_no_cartao_de_alimentacao` |
| Compra no cartão de **combustível** tem **acréscimo de 35%** | `deve_cobrar_cento_e_trinta_e_cinco_por_cento_quando_compra_no_cartao_de_combustivel` |
| Compra no cartão de **farmácia** não tem ajuste | `deve_cobrar_o_valor_integral_quando_compra_no_cartao_de_farmacia` |
| O valor só pode ser debitado do cartão **do tipo do estabelecimento** | `deve_recusar_a_compra_quando_o_tipo_do_cartao_difere_do_estabelecimento` |
| Crédito **soma** ao saldo | `deve_somar_ao_saldo_quando_creditar` |
| Atualizar consumidor **não altera o saldo** | `deve_preservar_o_saldo_quando_atualizar_dados_cadastrais` |

**Regras:**

- **Compara-se comportamento, não código.** A regra do enunciado é a
  especificação; o legado, quando coincide com ela, não é consultado.
- **Ambiguidade vira decisão registrada, e o teste cobre a decisão.** Onde o
  enunciado é omisso, a escolha fica documentada e testada, em vez de
  deduzida do código antigo. Exemplos:
  - **Saldo negativo:** o legado permitia; a decisão é **recusar** (422).
  - **Valor gravado no extrato:** o legado gravava o valor **já ajustado**;
    a decisão é manter (o extrato registra o valor efetivamente debitado), e
    um teste fixa isso.
- **Onde o legado contradiz o enunciado**, vale o enunciado: o comentário
  "não deve ser possível alterar o saldo" é a regra; o `repository.save` que
  o violava é o defeito.
- **A medição do legado** (tempo de `GET /consumer/consumerList`) é outra
  coisa: é linha de base de **desempenho**, capturada **antes** de refatorar
  ([`PERFORMANCE.md`](PERFORMANCE.md#8-medir-antes-de-otimizar)), não teste.

**Por quê:** o oráculo existe para não reescrever o mesmo bug. Aqui a fonte
confiável da regra é o texto do enunciado; o contrato do projeto é o que ele
decide, documenta e testa.

---

## 11. Um Ato por Teste — Preparar, Agir, Verificar

Todo teste segue **Preparar → Agir → Verificar** (*Arrange-Act-Assert*), com
as fases marcadas por comentário em português.

```java
@Test
void deve_recusar_a_compra_quando_saldo_insuficiente() {
    // Preparar
    var cartao = FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("50.00").construir();
    var cartoes = new CartaoPortaFake(cartao);
    var caso = new RegistrarCompraCaso(cartoes, new GravadorDaCompra(cartoes, new ExtratoPortaFake()),
        RELOGIO_FIXO);
    var compra = FabricaDeCompras.noValor("100.00").paraOCartao(cartao).construir();

    // Agir
    var resultado = caso.registrar(compra);

    // Verificar — Resultado é sealed; o caso de falha é um record, e o erro é um
    // tipo próprio (ver codigo/CONDICIONAIS.md nível 4 e qualidade/ERROS.md §2)
    var falha = assertInstanceOf(Falha.class, resultado);
    assertThat(falha.erro()).isInstanceOf(ErroSaldoInsuficiente.class);
}

// o relógio é injetado — o teste nunca depende do tempo real
private static final Clock RELOGIO_FIXO = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);
```

**Regras:**

- **Um ato por teste.** O "Agir" executa **uma** operação; teste com dois
  atos verifica dois comportamentos e falha sem dizer qual quebrou — divida.
- **Preparar não realiza a ação.** Montar estado é montar estado.
- **Verificar o que o ato produziu** — e **o que ele não deveria ter
  produzido**, quando isso é o comportamento (ex.: o saldo permanece quando
  a compra é recusada vira um teste próprio, `deve_manter_o_saldo_se_a_compra_for_recusada`).
- **Variações do mesmo comportamento são parâmetros**, não laço:

  ```java
  @ParameterizedTest
  @CsvSource({"1,100.00,90.00", "2,100.00,100.00", "3,100.00,135.00"})
  void deve_aplicar_o_fator_do_tipo_quando_calcular_o_valor_cobrado(int codigo, String valor, String esperado) { /* ... */ }
  ```

- **Sem lógica no teste.** Teste sem `if`/laço: o teste que precisa decidir
  alguma coisa precisa de teste.

**Por quê:** a estrutura é o que torna a falha diagnosticável — quando a
suíte falha, o leitor lê três blocos e sabe onde preparar, o que executou e o
que era esperado.

---

## 12. Fake, Mock ou Banco Real — Qual Substituto em Qual Fronteira

Substituir dependência é decisão por **fronteira**, não por gosto:

| Fronteira | Substituto | Onde vive |
|---|---|---|
| Porta de domínio (`CartaoPorta`, `ConsumidorPorta`, `ExtratoPorta`) | **Fake** em memória | `src/test/.../aplicacao/fakes/` |
| Banco de dados | **H2 real** | [seção 4](#4-banco-de-dados-em-testes-de-integração) |
| Relógio | **`Clock.fixed(...)`** injetado | no próprio teste |
| HTTP externo | — | não há integração externa neste projeto |
| Detalhe interno da própria camada | **Nada** — não substitui e não testa | [seção 6](#6-nunca-teste-detalhes-de-implementação) |

**Regras:**

- **Fake ≠ mock.** Fake substitui **a porta inteira**: um mapa em memória que
  **obedece o contrato** da porta (inclusive devolver `Falha` quando não
  encontra). Mock interno de método é teste de detalhe.
- **Mockito é proibido** neste projeto: nem `Mockito.mock`, nem `@Mock`, nem
  `@MockBean`/`@SpyBean` — este último troca um bean do contexto por um mock,
  que é a mesma coisa com outro nome. *(A biblioteca vem no
  `spring-boot-starter-test` e não pode ser removida do classpath; a
  proibição é de uso, verificada em revisão.)*
- **Nenhum fake entra em `src/main`** — se só existe para teste, vive em
  `src/test`, e a composition root de produção jamais o injeta.
- **O relógio nunca é o real em teste.** O `RegistrarCompraCaso` recebe
  `java.time.Clock` — tipo do JDK, permitido no domínio e na aplicação
  (`java.*`) — em vez de chamar `Instant.now()`. Em produção a composition
  root injeta `Clock.systemUTC()`; no teste, `Clock.fixed(...)`.
- **Testar o adaptador é testar com a fronteira real de baixo** (H2): o fake
  serve para isolar quem **consome** a porta, não para simular quem a
  **implementa**.
- **Um fake é verificado contra o contrato real.** O `CartaoPortaFake` e o
  `CartaoAdaptador` devem se comportar igual nos casos que importam (ausência
  → `Falha`); um fake que diverge do adaptador faz o teste unitário passar
  sobre uma mentira.

**Por quê:** a pergunta "posso mockar isso?" está invertida: a pergunta é "o
que esta fronteira precisa prover neste teste?". Cada substituto errado falha
de um jeito específico — fake onde devia ter banco esconde o SQL quebrado;
mock de detalhe prende o teste à estrutura; relógio real põe a suíte à mercê
do horário.

---

## 13. Determinismo — Nada de Flaky

Um teste determinístico dá o **mesmo resultado em toda execução**, na mesma
ou em outra máquina, em qualquer ordem. Teste que falha às vezes é defeito do
teste — **corrigido ou removido deliberadamente**, nunca "reexecutado até
passar".

| Fonte | Exemplo | Antídoto |
|---|---|---|
| Relógio real | `Instant.now()` no caso de uso | `Clock.fixed(...)` injetado |
| Estado compartilhado entre testes | Dois testes com o mesmo número de cartão | Dados únicos por teste (fábrica com sequência) |
| Ordem / paralelismo | Só passa na ordem do arquivo | Nenhum teste depende de outro |
| Rede e serviços externos | — | não há; suíte autocontida |
| **Threads e temporização** | `Thread.sleep(500)` "para dar tempo" | **Sincronização explícita** (abaixo) |

### O teste de concorrência é o caso difícil

`ConcorrenciaDeCompraTeste` precisa de várias threads — e continua
determinístico porque **verifica uma invariante, não um entrelaçamento**:

```java
@Test
void deve_manter_o_saldo_exato_quando_compras_simultaneas_no_mesmo_cartao() throws Exception {
    // Preparar — 20 compras de 10,00 num cartão de FARMÁCIA (sem fator) com saldo 100,00
    var cartao = criarCartaoDeFarmacia("100.00");
    var largada = new CountDownLatch(1);
    var pool = Executors.newFixedThreadPool(20);

    var tarefas = IntStream.range(0, 20)
        .mapToObj(i -> pool.submit(() -> { largada.await(); return comprar(cartao, "10.00"); }))
        .collect(toList());

    // Agir — todas as threads saem juntas
    largada.countDown();
    var statuses = coletar(tarefas, 10, TimeUnit.SECONDS);     // com prazo: nunca bloqueia para sempre

    // Verificar — a invariante, não a ordem: exatamente 10 sucesso, 10 saldo insuficiente, saldo final 0,00
    assertThat(contar(statuses, 201)).isEqualTo(10);
    assertThat(contar(statuses, 422)).isEqualTo(10);
    assertThat(saldoDe(cartao)).isEqualByComparingTo("0.00");
}
```

**Regras específicas:**

- **Largada sincronizada** (`CountDownLatch`), para forçar a disputa — sem
  largada, as threads podem rodar em série e o teste passa **sem testar nada**.
- **Prazo em toda espera** (`Future.get(timeout)`): um deadlock vira falha
  clara, não um build pendurado.
- **Nenhum `Thread.sleep`.** Esperar tempo fixo é a receita do flaky.
- **O resultado conferido é a invariante** (soma e saldo final), jamais "a
  thread 3 terminou antes da 7".
- **Dados próprios**: cartão criado só para este teste; sem `@Transactional` de
  teste (as threads usam transações separadas).
- **Se o lock pessimista for removido, este teste tem que falhar.** Teste de
  concorrência que passa também **sem** a proteção é decorativo — verificar
  isso (desligar o lock localmente e ver o vermelho) faz parte de escrevê-lo.

**Regras gerais:**

- **Aleatoriedade é semeada.**
- **Ordem de execução é irrelevante** — a suíte não tem ordem canônica.
- **Flaky detectado é corrigido no PR que o revelou.**

**Por quê:** a confiança na suíte é binária. Quando falha intermitente vira
normal, todo vermelho recebe o benefício da dúvida — e é o teste sério que
pega regressão real que passa a ser descartado como "flaky".

---

## 14. Testes Exigidos Pelos Outros Documentos

Os documentos desta padronização **prometem** testes. Esta tabela os reúne
num só lugar, para que nenhum fique esquecido. **Cada linha é obrigatória**;
quem muda o que a coluna "Documento" descreve atualiza o teste no mesmo PR.

| Teste | Documento de origem | O que garante | Tipo |
|---|---|---|---|
| `RegraDeDependenciaTeste` | [`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md#a-regra-é-verificável-não-de-boa-vontade) | `dominio`, `aplicacao`, `infraestrutura` e `api` só importam o que a tabela de camadas permite | Arquitetura |
| `CodigosDeErroTeste` | [`ERROS.md`](ERROS.md#8-código-de-erro-legível-e-verificado-por-teste) | todo erro tem `CODIGO` único e no formato `CPT-…` | Arquitetura |
| `ListagemDeConsumidoresTeste` | [`PERFORMANCE.md`](PERFORMANCE.md#1-sem-n1) | a listagem executa ≤ 2 comandos SQL (sem N+1) | Desempenho |
| `deve_recusar_campo_de_saldo_quando_atualizar_consumidor` | [`API.md`](API.md#12-contrato-público-e-testes-de-contrato) | `PUT` com saldo no corpo → 400 e saldo no banco intacto | Contrato |
| `deve_responder_404_quando_cartao_inexistente` | `API.md` §12 | cartão inexistente → 404, **não** 500/NPE | Contrato |
| `deve_responder_422_quando_saldo_insuficiente` | `API.md` §12 | saldo insuficiente → 422, saldo inalterado | Contrato |
| `deve_responder_404_quando_tipo_do_cartao_difere_do_estabelecimento` | `API.md` §12 | cartão de outro tipo não é debitado | Contrato |
| `deve_responder_400_com_errors_quando_valor_nao_positivo` | `API.md` §12 | valor ≤ 0 → 400 com `errors.valor` | Contrato |
| `deve_responder_problem_json_quando_rota_inexistente` | `API.md` §3 e §12 | 404 em `application/problem+json`, não o JSON padrão do Spring | Contrato |
| `deve_responder_201_com_location_quando_criar_consumidor` | `API.md` §8 | criação devolve 201 e `Location` | Contrato |
| `deve_responder_400_quando_tamanho_da_pagina_excede_o_teto` | `API.md` §9 | `tamanhoPagina` > 100 → 400 | Contrato |
| `deve_devolver_o_id_de_requisicao_quando_qualquer_resposta` | `API.md` §4 | cabeçalho `X-Request-Id` em sucesso e erro; ID inválido do cliente é regerado | Contrato |
| `deve_enviar_cabecalhos_de_seguranca_quando_responder` | [`SEGURANCA.md`](SEGURANCA.md#6-cabeçalhos-de-segurança) | os cabeçalhos aparecem em resposta de sucesso **e** de erro | Contrato |
| `deve_enviar_no_store_quando_responder` | [`PERFORMANCE.md`](PERFORMANCE.md#5-cache-de-leitura--recusado-de-propósito) | `Cache-Control: no-store` em `/api/v1/**` | Contrato |
| `deve_desabilitar_o_h2_console_quando_perfil_prod` | `SEGURANCA.md` §3 | com `@ActiveProfiles("prod")`, `/h2-console` não responde | Contrato |
| `ConcorrenciaDeCompraTeste` | `API.md` §11, `PERFORMANCE.md` §7 | compras simultâneas → saldo exato | Concorrência |
| `deve_disparar_o_timeout_de_lock_quando_cartao_esta_travado` | `PERFORMANCE.md` §9 | o timeout de lock **de fato dispara** no H2 | Integração |
| `deve_lancar_rollback_only_quando_capturar_excecao_de_persistencia` | `ERROS.md` §3 | prova a suposição das duas portas de conversão | Integração |
| `deve_registrar_so_a_restricao_quando_violar_integridade` | `ERROS.md` §4, `SEGURANCA.md` §5 | o log **não** contém o valor duplicado (cartão/documento) | Integração |
| `CoerenciaDeEscalaTeste` | [`VALORES.md`](../codigo/VALORES.md#68-testes-que-fixam-esta-seção) §6.8, [`BANCO.md`](BANCO.md#15-testes-que-fixam-esta-página) | `scale` de toda coluna monetária == `Dinheiro.ESCALA` | Integração |
| `DinheiroTeste` (fração preservada, soma exata, arredondamento explícito) | `VALORES.md` §6.8 | `33.33 × 0.90 = 29.997000`; `0.1 + 0.2 = 0.3`; modo de arredondamento obrigatório | Unitário |
| `deve_recusar_saldo_negativo_quando_gravar_direto_no_banco` | `BANCO.md` §4 | o `CHECK (saldo >= 0)` **existe e é aplicado pelo H2** | Integração |
| `deve_recusar_documento_duplicado_quando_forma_diferente` | `BANCO.md` §11 | `123.456.789-01` e `12345678901` → 409 na segunda | Contrato |
| `deve_preencher_a_auditoria_quando_criar_e_avancar_ao_atualizar` | `BANCO.md` §6 | `data_criacao`/`data_atualizacao`, com relógio controlável | Integração |
| `deve_responder_409_quando_versao_enviada_diverge` | `BANCO.md` §7 | o `PUT` confere a `versao` explicitamente | Contrato |
| `NomenclaturaDeSchemaTeste` | `BANCO.md` §3 | todo `unique`/índice/FK é nomeado e segue `uk_`/`idx_`/`fk_` | Arquitetura |
| `SaidaDeLogAdaptadorTeste` (formato `evento chave=valor`, máscara por nome de campo, marker `CRITICO`) | [`OBSERVABILIDADE.md`](../runtime/OBSERVABILIDADE.md#10-testes-que-fixam-esta-página) §1 e §4 | a porta de log formata, mascara e marca o nível crítico | Integração |
| `deve_responder_200_quando_banco_disponivel` / `deve_responder_503_quando_banco_indisponivel` | `OBSERVABILIDADE.md` §7 | o health reflete a dependência (`SaudePorta` **fake**, por `@TestConfiguration`) | Contrato |
| `deve_manter_prod_em_info_quando_perfil_prod` | `OBSERVABILIDADE.md` §9 | nenhum `DEBUG` em produção | Integração |
| `EntradaValidadaTeste` | [`VALIDACAO.md`](VALIDACAO.md#4-ponto-único-na-api) §4 | todo `@RequestBody` tem `@Valid`; todo controller com restrição em parâmetro tem `@Validated`; todo `String` de entrada tem `@Size(max)` | Arquitetura |
| `Mensagem*Teste` (uma regra por teste, caso feliz incluído) | `VALIDACAO.md` §11 | cada anotação recusa e aceita o que deve, via o próprio `Validator` | Unitário |
| `deve_omitir_o_valor_rejeitado_quando_responder_400` | `VALIDACAO.md` §6 | o número do cartão enviado **não** aparece no corpo do `400` | Contrato |
| `deve_traduzir_o_erro_do_jackson_quando_campo_desconhecido` | `VALIDACAO.md` §7 | `"saldo": ["não é aceito nesta operação"]`, sem nome de classe | Contrato |
| `deve_responder_400_com_o_mesmo_formato_quando_tipo_de_estabelecimento_inexistente` | `VALIDACAO.md` §3 | borda e domínio produzem o mesmo `400` | Contrato |
| `deve_responder_em_portugues_quando_accept_language_ingles` | `VALIDACAO.md` §7 | o locale fixo vale | Contrato |
| `EstadoCompartilhadoTeste` | [`CONCORRENCIA.md`](../runtime/CONCORRENCIA.md#4-sem-estado-global-mutável) §4 | nenhum `static` mutável, campo mutável em bean, `synchronized`, `parallelStream`, `CompletableFuture`, `new Thread`, `@Async` ou `@Scheduled` em `src/main` | Arquitetura |

> **Esta tabela é um inventário, não uma lista de desejos.** Linha sem teste
> correspondente é pendência declarada no PR; linha cujo teste foi removido é
> decisão que exige justificativa.

---

## Anti-Padrões

| Anti-padrão | Correto |
|---|---|
| Teste que passa independente do comportamento (assert fraco) | Verificação que falharia se a regra mudasse (seção 1) |
| Meta de porcentagem de linha | Régua: mudança de comportamento vem com teste (seção 8) |
| E2E testando regra de domínio | Unitário para a regra; E2E para o fluxo (seção 3) |
| Mock de repositório em teste de adaptador | H2 real (seção 4) |
| `@MockBean`/`@SpyBean`/`Mockito.mock` | Fake de porta; banco real (seção 12) |
| `@DirtiesContext` para "limpar" estado | Dados únicos por teste (seção 4) |
| `@Transactional` de teste em contrato/E2E/concorrência | Dados próprios; sem rollback automático (seção 4) |
| Teste que toca `*Entidade` para conferir o resultado | Reler pela porta pública (seção 6) |
| `test1` / nome que não diz o resultado esperado | `deve_<resultado>_quando_<condição>` (seção 5) |
| `@DisplayName` repetindo o nome do método | Só o nome do método (seção 5) |
| Fixture de arquivo, de produção ou de dump real | Fábrica no teste (seção 7) |
| Número de cartão fixo repetido entre testes | Número único de sequência (seção 7) |
| Dinheiro como `double`/`new BigDecimal(double)` | `new BigDecimal("50.00")` (seção 7) |
| Teste que exige rede, nuvem ou credencial real | Suíte autocontida (seção 9) |
| Rodar `./gradlew test` com JDK 27 e culpar o build | JDK 17 via `JAVA_HOME`, registrado no README (seção 9) |
| Tratar o código legado como oráculo | Regras do enunciado + decisões registradas (seção 10) |
| Dois atos num único teste; lógica (`if`/laço) no teste | Um ato; `@ParameterizedTest` (seção 11) |
| `Instant.now()` no caso de uso | `Clock` injetado; `Clock.fixed` no teste (seção 12) |
| Fake em `src/main` | Fake em `src/test` (seção 12) |
| Fake que diverge do adaptador real | Mesmo comportamento nos casos de borda (seção 12) |
| `Thread.sleep` para "dar tempo" à concorrência | `CountDownLatch` + prazo + invariante (seção 13) |
| Teste de concorrência sem largada sincronizada | Largada comum; senão passa sem testar (seção 13) |
| Teste de concorrência que passa sem o lock | Verificar que ele falha quando a proteção é removida (seção 13) |
| Reexecutar teste falho até passar | Corrigir ou remover com justificativa (seção 13) |
| Linha da tabela da seção 14 sem teste, sem declarar | Pendência explícita no PR (seção 14) |

---

## Checklist

Antes de abrir um PR que escreve ou altera testes:

- [ ] O teste falharia se o comportamento que ele cobre mudasse?
- [ ] A mudança de comportamento veio com teste cobrindo-a no mesmo PR?
- [ ] O tipo de teste é o da pergunta (unitário / integração / contrato / E2E / concorrência) — não o mais cômodo?
- [ ] Teste de adaptador usa H2 real, nunca mock de repositório?
- [ ] Nome do teste comunica cenário, condição e resultado (`deve_…_quando_…`)?
- [ ] Nenhum teste acessa detalhe interno, entidade para conferir resultado, ou mock de detalhe?
- [ ] Nenhum `Mockito`, `@MockBean`, `@SpyBean` ou `@DirtiesContext`?
- [ ] Fixture vem de fábrica no teste, com número de cartão único — nada de arquivo, dump ou dado real?
- [ ] Dinheiro construído de `String` (`BigDecimal`), nunca de `double`?
- [ ] "Agir" é único; fases marcadas com comentário em português; sem `if`/laço no teste?
- [ ] O tempo vem de `Clock.fixed(...)`, nunca do relógio real?
- [ ] Fake em `src/test`, e fiel ao comportamento do adaptador real?
- [ ] A suíte roda com `./gradlew test` (JDK 17), sem rede e sem credencial real?
- [ ] Teste de concorrência usa largada sincronizada, prazo e invariante — sem `sleep` — e falha sem o lock?
- [ ] Erro novo de domínio tem teste do caso de uso que o provoca, não só do formato?
- [ ] A tabela da seção 14 foi atualizada, e linha sem teste está declarada como pendência?
- [ ] Este documento descreve a prática atual da suíte (não a de quando nasceu)?

---

**[Segurança ←](SEGURANCA.md)**
