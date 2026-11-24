[← Índice](../../PADRONIZACAO_INDEX.md) · [← TESTES.md](TESTES.md)

# Banco de Dados — Java 17 / JPA / H2

> **Escopo**: a disciplina de banco para **Java 17 + Spring Data JPA +
> Hibernate + H2**. O projeto tem **um** banco, então não há matriz de
> provedores nem camada de escape por dialeto; valem a disciplina de schema
> (nomenclatura, versão de linha, transação, SQL portável) e as regras de
> tipos de coluna, nulidade, restrições e identificador. Ver
> [Premissas do Banco](#premissas-do-banco).
>
> **Nota sobre exemplos**: os trechos de código, nomes de coluna e
> parâmetros abaixo são ilustrativos e **não foram executados** — a regra
> normativa é o texto conceitual. O código atual ainda é o **legado do
> desafio** (entidades `Consumer` e `Extract`, colunas em inglês, saldo em
> `double`); este documento descreve o schema **pretendido** (ver
> [`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md#este-documento-é-vivo)).
>
> **Documento relacionado**: [`VALIDACAO.md`](VALIDACAO.md).

Define o schema pretendido, a nomenclatura de banco, os tipos de coluna e
as restrições, a concorrência (versão de linha e lock), a auditoria, a
exclusão, o tratamento do schema sem migrações, a forma canônica dos dados,
transações, SQL portável e conexão.

**O banco, em uma linha:** **H2 em memória** (`jdbc:h2:mem:consumer-pat`),
schema gerado pelo Hibernate a partir das entidades (`ddl-auto:
create-drop`), acessado só por Spring Data JPA — ver
[`TESTES.md`](TESTES.md#4-banco-de-dados-em-testes-de-integração) para o
mesmo banco em teste.

---

## Premissas do Banco

| Tema | Premissa | Seção |
|---|---|---|
| Modelo | definido uma vez, sem SQL de dialeto | 1 |
| Provedor | um só (H2); o de produção é decisão em aberto | 2 |
| Nomenclatura | português, plural, *snake_case*, constraints nomeadas — é o que o legado mais viola | 3 |
| Tipos, nulidade, restrições e identificador | definidos pela entidade JPA, com `NOT NULL` por padrão | 4 e 5 |
| Auditoria | Spring Data Auditing, sem colunas de usuário (não há autenticação) | 6 |
| Concorrência | versão de linha; o lock pessimista do saldo é a exceção justificada | 7 |
| Exclusão | física, sem `DELETE` na API; o extrato é imutável | 8 |
| Migrações | não há: Flyway e Liquibase seriam dependência nova | 9 |
| Backup | recomendação; o banco em memória é descartável | 10 |
| Busca e colação | não há busca por texto; vale a **forma canônica na escrita** | 11 |
| Transações | crédito exige transação mesmo com uma só escrita | 12 |
| SQL de dialeto | proibido; sem camada de escape | 13 |
| Conexão, segredos e falha | cobertos em [`SEGURANCA.md`](SEGURANCA.md) e [`PERFORMANCE.md`](PERFORMANCE.md) | 14 |

---

## 1. Modelo Definido Uma Vez, Sem SQL de Dialeto

O modelo de dados é definido **uma vez**, nas entidades JPA
(`infraestrutura/persistencia/{agregado}`), e traduzido para o dialeto pelo
Hibernate. Não há SQL cru acoplado a um banco específico.

**Por quê:** o projeto **não promete** multi-provedor, mas a
disciplina que o sustenta é barata e vale sozinha: o legado tem consultas
nativas com nomes de coluna em maiúsculas
(`select * from Consumer where FOOD_CARD_NUMBER = ?`) — acopladas ao H2 e à
grafia exata do schema gerado. Trocar o banco, ou só renomear uma coluna,
quebraria em silêncio, em tempo de execução.

**O que isso exige na prática:**

1. **Sem SQL nativo** no código do projeto (seção 13) — consulta derivada
   ou JPQL.
2. **O que muda por banco, se um dia mudar, mora na infraestrutura** — o
   domínio e a aplicação não sabem qual banco está por baixo.
3. **Comportamento observável igual em qualquer banco é contrato**, não
   acidente — por isso a regra de forma canônica (seção 11) e o teste que
   cobre cada diferença descoberta (seção 2).

---

## 2. Provedor: H2 em Memória

| Provedor | Status | Papel |
|---|---|---|
| **H2** (em memória) | **Único** | desenvolvimento, avaliação do desafio e testes |
| Banco de produção real | **Não definido** | decisão em aberto — **não prometido** |

*(Para um banco de produção, **PostgreSQL** é o candidato natural, mas
entrar exige decisão registrada em [`DECISOES.md`](../fundamentos/DECISOES.md)
e a introdução de migrações (seção 9), que são dependência nova.)*

**O que o H2 em memória significa — e o que não significa:**

- **Os dados somem a cada reinício.** É esperado e documentado no README; o
  schema é recriado do zero (`create-drop`).
- **O H2 não é o banco de produção.** O que passa no H2 pode divergir em um
  banco real em pontos específicos, listados abaixo — cada um é **risco
  declarado** no PR e **coberto por teste** quando o teste é possível no H2:

| Diferença provável (H2 × banco real) | Por que importa aqui | Cobertura |
|---|---|---|
| Semântica do **lock pessimista** (`SELECT … FOR UPDATE`) e de seu *timeout* | o saldo depende dele | teste de concorrência e de timeout ([`TESTES.md`](TESTES.md#14-testes-exigidos-pelos-outros-documentos)); **não** reproduzível em outro banco sem Testcontainers |
| **Texto da mensagem** de violação de restrição (traz o valor duplicado no H2) | [`SEGURANCA.md`](SEGURANCA.md#5-logs-sem-dados-sensíveis) | o log só leva a restrição; teste |
| **Palavras reservadas** e **maiúsculas/minúsculas** de identificador | nome de coluna/tabela | nomenclatura em português e *snake_case* (seção 3) evita colisões |
| **Comportamento de `CHECK`** e de `NUMERIC` | saldo não negativo, escala 6 | teste de integração (seção 4) |

**Não se adota `MODE=PostgreSQL` na URL do H2** (aproximaria o dialeto):
sem um PostgreSQL real para validar, ele daria falsa segurança.

---

## 3. Nomenclatura de Banco

- **Tabelas:** substantivo no **plural**, *snake_case*, em **português**:
  `consumidores`, `cartoes`, `extratos`.
- **Colunas:** *snake_case*, em português: `id`, `nome`, `documento`,
  `data_nascimento`, `data_criacao`, `consumidor_id`, `saldo`.
- **Chave primária:** sempre `id`.
- **Chave estrangeira (coluna):** `<tabela_origem_no_singular>_id`
  (`consumidor_id`).
- **Índices:** `idx_<tabela>_<coluna(s)>` — `idx_consumidores_nome_id`.
- **Chaves únicas:** `uk_<tabela>_<coluna(s)>` — `uk_cartoes_numero`.
- **Chaves estrangeiras nomeadas:** `fk_<tabela_origem>_<tabela_destino>` —
  `fk_cartoes_consumidores`.
- **Restrições de verificação:** `ck_<tabela>_<regra>` —
  `ck_cartoes_saldo_nao_negativo`.

**Por quê nomes desde a primeira definição:** constraint com nome gerado pelo
banco (`CONSTRAINT_4F`, `FK_9Z0…`) é ilegível em mensagem de erro e em log, e
**irreversível** em leitura de script. Nomear custa uma linha na anotação;
descobrir qual restrição estourou sem nome custa uma investigação.

**Regras adicionais:**

- **Todo `unique`, índice e chave estrangeira é nomeado na anotação**
  (`@UniqueConstraint(name = …)`, `@Index(name = …)`,
  `@ForeignKey(name = …)`). Constraint anônima é rejeitada em revisão.
- **Sem prefixo técnico** (`tbl_`, `tb_`).
- **Nome de tabela não se repete na coluna** (`consumidores.nome`, não
  `consumidores.consumidor_nome`).
- **Palavras reservadas** de SQL não viram identificador: o legado tem
  colunas `number` e `portal_code`; aqui `numero` e `cep`.
- **Auditoria e `versao` têm os mesmos nomes em todas as tabelas**
  (seções 6 e 7).
- **O Hibernate converte `camelCase` em *snake_case* sozinho** (estratégia de
  nomes padrão do Spring Boot): `dataNascimento` → `data_nascimento`. O nome
  da **tabela** é explícito em `@Table(name = …)`; colunas com nome fora da
  regra declaram `@Column(name = …)`.
- **O schema é descartável** (`create-drop`), então **renomear não tem custo
  de migração hoje** — a convenção entra completa agora, enquanto é de graça.

---

## 4. Tipos de Coluna, Nulidade e Restrições

### Modelo pretendido

**`consumidores`**

| Coluna | Tipo | Nulo? | Observação |
|---|---|---|---|
| `id` | `BIGINT` (identidade) | não | seção 5 |
| `nome` | `VARCHAR(150)` | não | `@Size(max = 150)` na `Mensagem*` |
| `documento` | `VARCHAR(14)` | não | só dígitos (seção 11); `uk_consumidores_documento` |
| `data_nascimento` | `DATE` | não | `LocalDate` |
| `celular` | `VARCHAR(15)` | não | só dígitos |
| `telefone_residencial`, `telefone` | `VARCHAR(15)` | **a decidir** | opcionais no legado; ver nota |
| `email` | `VARCHAR(254)` | não | |
| `rua`, `cidade`, `pais` | `VARCHAR(…)` | não | |
| `numero` | `VARCHAR(10)` | não | número do imóvel — texto (`S/N`, `12-B`) |
| `cep` | `VARCHAR(8)` | não | só dígitos |
| `versao` | `BIGINT` | não | `@Version` (seção 7) |
| `data_criacao`, `data_atualizacao` | `TIMESTAMP` | não | seção 6 |

*(Índice: `idx_consumidores_nome_id (nome, id)` — a listagem ordena por
`nome, id`.)*

**`cartoes`**

| Coluna | Tipo | Nulo? | Observação |
|---|---|---|---|
| `id` | `BIGINT` (identidade) | não | |
| `consumidor_id` | `BIGINT` | não | `fk_cartoes_consumidores`; `idx_cartoes_consumidor_id` |
| `tipo` | `VARCHAR(20)` | não | `@Enumerated(EnumType.STRING)` |
| `numero` | `VARCHAR(19)` | não | só dígitos; `uk_cartoes_numero` |
| `saldo` | **`NUMERIC(19,6)`** | não | `ck_cartoes_saldo_nao_negativo` |
| `versao` | `BIGINT` | não | `@Version` |
| `data_criacao`, `data_atualizacao` | `TIMESTAMP` | não | |

*(Único: `uk_cartoes_consumidor_tipo (consumidor_id, tipo)` — um consumidor
tem **no máximo um cartão de cada tipo**, invariante do domínio que o
legado expressava com três colunas.)*

> **Hoje a tabela nasce sem `consumidor_id` e sem `uk_cartoes_consumidor_tipo`.**
> O domínio ainda não tem `Consumidor`, e criar a coluna `NOT NULL` obrigaria a
> criar também a tabela `consumidores` e uma entidade sem mapeador nem uso. Como o
> schema nasce só das anotações (`create-drop`), as duas entram junto do
> consumidor, sem migração. Enquanto isso, o `GravadorDeCartao` só **atualiza** um
> cartão que existe; criá-lo exige o consumidor dono. O `extratos` já tem a FK
> `fk_extratos_cartoes`, e a persistência do extrato mora no pacote do cartão
> (ver [`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md)).

**`extratos`** *(imutável: só se insere — seção 8)*

| Coluna | Tipo | Nulo? | Observação |
|---|---|---|---|
| `id` | `BIGINT` (identidade) | não | |
| `cartao_id` | `BIGINT` | não | `fk_extratos_cartoes`; `idx_extratos_cartao_id` |
| `nome_estabelecimento` | `VARCHAR(120)` | não | |
| `descricao_produto` | `VARCHAR(255)` | não | |
| `valor_informado` | `NUMERIC(19,6)` | não | o que o cliente enviou |
| `valor_cobrado` | `NUMERIC(19,6)` | não | já com o ajuste do tipo, **com a fração** |
| `data_compra` | `TIMESTAMP` | não | `Instant` do `Clock` |
| `data_criacao` | `TIMESTAMP` | não | auditoria (sem `data_atualizacao`: não há atualização) |

> **Crédito não deixa rastro no legado**, e o modelo acima só registra
> compra. Registrar crédito exige um `tipo_movimento` (compra/crédito) e
> tornar `nome_estabelecimento`/`descricao_produto` opcionais **ou** uma
> tabela própria — decisão de produto, registrada como risco em
> [`SEGURANCA.md`](SEGURANCA.md#9-riscos-conhecidos-e-fora-de-escopo)
> (item 7), fora do escopo desta entrega.

*Telefones opcionais:* o legado os trata como `int` com `0` de "vazio" —
valor mágico que o banco grava como telefone real
([`VALORES.md`](../codigo/VALORES.md#8-ausência-como-tipo-nunca-como-valor-mágico)).
A decisão do que é obrigatório afeta `NOT NULL`; **decisão de domínio em
aberto**, e a coluna fica anulável **apenas** se a ausência for modelada no
domínio (Null Object), nunca `0`.

### Regras de tipo

- **Dinheiro: `NUMERIC(19,6)`** — ponto fixo com escala declarada, nunca
  `DOUBLE`/`FLOAT`/`REAL`/`DECFLOAT`
  ([`VALORES.md`](../codigo/VALORES.md#67-comparar-persistir-e-serializar)).
  `@Column(precision = 19, scale = 6, nullable = false)`.
- **Números de cartão, documento, telefone, CEP: texto de dígitos
  (`VARCHAR`), nunca numérico.** Têm zero à esquerda, não se somam e o
  legado os guardava em `int`, que **estoura** (um número de cartão de 16
  dígitos não cabe em ~2 bilhões).
- **Enum: `@Enumerated(EnumType.STRING)`, nunca `ORDINAL`.** Gravar o índice
  faz reordenar o enum **mudar o significado** do que está no banco — o mesmo
  motivo de o JSON usar o nome
  ([`API.md`](API.md#6-serialização-determinística)).
- **Instante: `TIMESTAMP` em UTC** (`Instant` +
  `hibernate.jdbc.time_zone: UTC`); **data civil: `DATE`**
  ([`VALORES.md`](../codigo/VALORES.md#1-datas-e-hora-sempre-em-utc)).
- **`VARCHAR` com comprimento declarado**, igual ao `@Size(max = …)` da
  mensagem de entrada ([`SEGURANCA.md`](SEGURANCA.md#2-entrada-validada-na-borda)):
  o limite existe em duas camadas, e o do banco é a última.

### Regras de nulidade e restrição

- **`NOT NULL` por padrão.** Coluna anulável é exceção, com ausência
  modelada no domínio. O legado tem **toda** coluna anulável e `int` com `0`.
- **`CHECK (saldo >= 0)`** — a última linha de defesa contra o bug do legado
  (saldo negativo): mesmo que uma regra do domínio falhe, **o banco recusa**.
  Via `@Check(constraints = "saldo >= 0")` do Hibernate.
  - *Ressalva:* o Hibernate 5 não permite **nomear** a restrição por essa
    anotação — o nome (`ck_cartoes_saldo_nao_negativo`) fica **não aplicado**
    até verificar a alternativa (nome via `@Table`/DDL). É uma exceção
    declarada à regra "toda restrição é nomeada".
  - **Um teste prova que o H2 a aplica** (seção 15): sem ele, o `@Check`
    pode ser ignorado em silêncio. *(Provado: sem a anotação, os dois testes
    de saldo negativo falham.)*
- **Chave estrangeira com índice explícito.** O H2 cria o índice de FK
  sozinho; PostgreSQL, não.
- **`unique` é regra de integridade e rede de segurança**, não a primeira
  linha: o domínio verifica antes (porta 1,
  [`ERROS.md`](ERROS.md#3-proibição-de-captura-genérica--e-as-duas-portas-de-conversão)).

---

## 5. Estratégia de Identificador

*(Seção nova.)*

**`GenerationType.IDENTITY`** — coluna de identidade por tabela.

O legado usa `GenerationType.AUTO`, que no Spring Boot 2.x resolve para uma
**sequência global** (`hibernate_sequence`) compartilhada por **todas** as
entidades: o consumidor 1, o cartão 2 e o extrato 3 saem da mesma contagem.
Funciona, mas confunde (ids "faltando" por tabela) e acopla as tabelas a um
objeto de banco compartilhado.

- **Escolha:** `IDENTITY` — simples, por tabela, previsível.
- **Custo conhecido:** `IDENTITY` desliga o *batch* de `INSERT` do JDBC.
  Irrelevante aqui (insere-se um registro por requisição); **se** um dia
  houver carga em lote, a decisão é revista (sequência por entidade).
- **O ID tipado vive no domínio** (`ConsumidorId`,
  [`VALORES.md`](../codigo/VALORES.md#3-ids-como-tipos-de-valor)); a entidade
  guarda `Long`, e o mapeador converte.
- **O ID é interno para cartão e extrato**: o cartão é consultado por
  `numero`; só `consumidores` expõe o `id` na API.

---

## 6. Auditoria Automática

Toda entidade tem campos de auditoria preenchidos **automaticamente** — não
confie em código de negócio para preenchê-los.

- `data_criacao` (UTC) — `@CreatedDate`
- `data_atualizacao` (UTC) — `@LastModifiedDate`

**As colunas de usuário** (`usuario_criacao_id`, `usuario_atualizacao_id`)
**não existem**: não há autenticação, então não há "quem" a registrar
([`SEGURANCA.md`](SEGURANCA.md#9-riscos-conhecidos-e-fora-de-escopo), item 1).
Entram junto com a autenticação, na mesma mudança.

**Mecanismo:** Spring Data JPA Auditing — vem no `spring-data-jpa`, **sem
dependência nova**:

```java
// infraestrutura/persistencia/cartao/CartaoEntidade.java
@Entity
@EntityListeners(AuditingEntityListener.class)
class CartaoEntidade {
    @CreatedDate     @Column(name = "data_criacao", nullable = false, updatable = false)
    private Instant dataCriacao;

    @LastModifiedDate @Column(name = "data_atualizacao", nullable = false)
    private Instant dataAtualizacao;
    // ...
}

// api/configuracao/ConfiguracaoDeAuditoria.java — composition root
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "provedorDeInstante")
public class ConfiguracaoDeAuditoria {
    @Bean
    DateTimeProvider provedorDeInstante(Clock relogio) {
        return () -> Optional.of(relogio.instant());      // o MESMO Clock do domínio
    }
}
```

**Regras:**

1. **Os campos de auditoria existem só na entidade, nunca no domínio.** O
   mapeador **não os lê nem os escreve**; nenhum caso de uso os atribui — se
   um caso de uso está atribuindo `data_criacao`, é erro.
2. **O instante vem do `Clock` injetado** (o mesmo do domínio), não do relógio
   do sistema — é o que permite ao teste fixar o tempo.
3. **`data_atualizacao` é derivada**, igual a `data_criacao` na criação.
4. **Auditoria não é superfície pública**: não aparece em `Mensagem*`
   ([`API.md`](API.md#5-dtos-para-entrada-e-saída)).
5. **`extratos` tem só `data_criacao`**: é imutável (seção 8), não há
   atualização a registrar.
6. **O teste de integração cobre o mecanismo**, não cada caso de uso — e usa
   um relógio **controlável** para provar que `data_atualizacao` avança.
   Como `@DataJpaTest` **não** carrega `@Configuration` de `api`, o teste
   importa `ConfiguracaoDeAuditoria` explicitamente
   ([`TESTES.md`](TESTES.md#3-tipos-de-teste)).

---

## 7. Concorrência: Versão de Linha e Lock

Duas estratégias, **uma por tipo de dado** — e a escolha pelo lock pessimista
é uma **exceção que exige justificativa por escrito**.

| Dado | Estratégia | Quem a sustenta |
|---|---|---|
| **Saldo do cartão** (débito/crédito) | **lock pessimista** (`@Lock(PESSIMISTIC_WRITE)`) dentro da transação do caso de uso, **mais** `@Version` como rede de segurança | [`API.md`](API.md#11-concorrência-idempotência-e-conflitos) |
| **Cadastro do consumidor** (`PUT`) | **otimista**: coluna `versao`, exposta na leitura e reenviada na escrita | esta seção |

### Cadastro: versão de linha, ponta a ponta

```text
1. cliente lê   → GET /api/v1/consumidores/{id}      (resposta inclui versao: 3)
2. cliente edita e reenvia → PUT ... { ..., versao: 3 }
3. adaptador compara versao enviada × versao atual:
     diferente → ErroVersaoDivergente (CPT-CNF-CSM-002) → 409           ← porta 1 (ERROS.md)
     igual     → grava; o Hibernate incrementa a versao
4. rede de segurança: duas gravações simultâneas passam pela comparação;
   o @Version faz a segunda falhar → ObjectOptimisticLockingFailureException → 409   ← porta 2
```

- **O Hibernate não confere a versão que o cliente enviou.** `@Version`
  protege contra duas **transações** concorrentes, não contra um cliente
  com dado velho: a comparação do passo 3 é **explícita**, no adaptador.
- **`versao` nunca vem do cliente como autoridade**: o servidor compara e
  **incrementa sozinho**.
- **Toda tela de edição expõe e reenvia `versao`** — expor no banco sem
  reenviar na edição é mecanismo sem efeito.
- **Conflito resolvido por re-leitura humana**, nunca por retentativa
  automática com *merge* improvisado.
- **Não é opcional** para qualquer registro que duas pessoas possam abrir ao
  mesmo tempo: aqui, `consumidores`.

### Saldo: por que lock pessimista — justificativa registrada

A regra é: *concorrência é otimista por padrão; lock pessimista existe, mas
só com justificativa registrada — é a origem clássica de deadlock*.
**A justificativa:**

1. **Duas compras legítimas simultâneas no mesmo cartão devem ambas ter
   sucesso, em série**, desde que o saldo cubra as duas. Com controle
   otimista, uma receberia 409 por **contenção** — um erro que o cliente não
   causou, sobre uma operação válida.
2. **A retentativa automática é proibida** (regra acima). Otimista no saldo
   empurraria a retentativa para o cliente a cada colisão — e `POST` de
   compra **não é idempotente**: o cliente que retenta às cegas pode debitar
   duas vezes ([`API.md`](API.md#11-concorrência-idempotência-e-conflitos)).
3. **O escopo do lock é uma linha** (o cartão) e a transação é curta
   ([`PERFORMANCE.md`](PERFORMANCE.md#7-transação-e-lock-curtos--o-servidor-não-é-assíncrono)).
4. **O deadlock é evitado por regra, não por sorte:**
   - **um lock por transação** — nenhuma operação trava dois cartões;
   - **se uma operação futura precisar travar mais de um** (ex.:
     transferência), trava **em ordem crescente de `id`** — a ordem
     determinística é o que impede o ciclo.
5. **`@Version` permanece como rede de segurança**, mesmo com o lock: se
   alguém um dia remover o lock sem perceber, o `@Version` transforma o
   *lost update* silencioso em 409 visível.
6. **O custo é conhecido e medido**: contenção em um cartão ocupa conexões do
   pool — mitigado por timeout de lock menor que o de conexão
   ([`PERFORMANCE.md`](PERFORMANCE.md#9-timeouts-e-limites)). **No H2 o teto é o
   `LOCK_TIMEOUT` da URL**, porque a dica do JPA é ignorada por ele em silêncio;
   `TravaDeCartaoTeste` prova que a segunda transação falha dentro do teto, com
   `PessimisticLockingFailureException`, e não espera o padrão do banco.

**Por quê:** *last write wins* sobrescreve o primeiro salvamento **sem que
nenhum dos dois autores saiba** — em cadastro é perda de dado; em saldo, é
perda de dinheiro.

---

## 8. Exclusão Física por Padrão — e Extrato Imutável

Registros são excluídos fisicamente por padrão; exclusão lógica só com
requisito explícito. **Hoje a API não tem `DELETE`** — a seção fica como
regra para quando houver.

**Regras específicas deste projeto:**

- **`extratos` é imutável (append-only).** Extrato é **registro financeiro**:
  nunca é atualizado nem apagado. A porta `ExtratoPorta` expõe **só
  `registrar`**; não existe `atualizar` nem `excluir` — o que nenhuma
  interface oferece, ninguém chama por engano. A entidade não tem *setters*.
- **A chave estrangeira não cascateia exclusão.** Apagar um consumidor (se
  um dia for permitido) **não pode** apagar seus extratos junto: a FK
  `fk_extratos_cartoes` e `fk_cartoes_consumidores` ficam em modo
  **restritivo** (padrão do DDL do Hibernate; **nunca** `cascade = REMOVE`).
  Cascata sobre dado financeiro é decisão **registrada por relação**, jamais
  default do ORM aceito sem ler.
- **Soft delete não se usa.** Se o requisito de recuperação aparecer:
  filtro global (não `WHERE` à mão), `data_exclusao`, restauração testada.

**Por quê:** soft delete por precaução multiplica custo permanente (índice
parcial, filtro em toda consulta, `UNIQUE` quebrado por registro morto). Aqui
o argumento é mais forte: o que **não pode** ser apagado — o extrato — já é
protegido por construção, sem soft delete.

---

## 9. Schema Sem Migrações

O ideal seriam migrações versionadas. **Aqui não há ferramenta de
migração**: Flyway e Liquibase seriam dependência nova, proibida pelo
enunciado. O que existe é:

```yaml
# application.yml (perfil padrão — desenvolvimento e avaliação)
spring.jpa.hibernate.ddl-auto: create-drop      # o schema nasce das entidades e some com a aplicação
```

**Regras que substituem as de migração:**

1. **As anotações das entidades são a **única** definição do schema.** Nunca
   DDL à mão, nunca `schema.sql` paralelo — dois lugares definindo o schema
   é o que as migrações existiam para evitar.
2. **`ddl-auto` nunca é `update` fora de um banco descartável.** `update`
   altera schema de forma não rastreada e **não remove** o que sai do
   modelo: o schema real e o código divergem sem registro.
3. **Banco persistente exige migrações.** Quem apontar a aplicação para um
   banco real **não** usa `create-drop`: usa `validate` (a aplicação
   **confere** o schema, não o altera) **e** uma ferramenta de migração —
   recomendação registrada, **fora do escopo** do desafio.
4. **Quando migrações existirem**, valem estas regras: uma migração
   por mudança coerente, nunca editar migração aplicada, *expand/contract*
   para mudança incompatível, migração e código no mesmo PR.
5. **Toda restrição e todo índice nasce nomeado na anotação** (seção 3) — é o
   que torna o schema gerado auditável sem migração.
6. **O schema é verificado por teste**: um teste de integração confere que as
   restrições existem e funcionam (`unique`, `CHECK`, FK) — o substituto, em
   pequeno, do que a migração aplicada garantiria.

---

## 10. Backup — Recomendação

**Não se aplica ao banco em memória**: não há dado a preservar. Para um
banco persistente: backup pré-migração **automático**, restauração
**testada** periodicamente, retenção que cubra o intervalo entre descoberta
e reação, e plano escrito para migração destrutiva. Registrado como
recomendação, junto com o item de segredos de produção
([`SEGURANCA.md`](SEGURANCA.md#9-riscos-conhecidos-e-fora-de-escopo)).

---

## 11. Forma Canônica na Escrita

*(Reinterpreta "Colação e busca sem diferenciar maiúsculas": não há busca por
texto aqui, mas a ideia de fundo — **o dado é normalizado antes de gravar**,
para o banco não precisar adivinhar — é a mais importante da seção.)*

**O problema:** `123.456.789-01` e `12345678901` são o **mesmo** documento. Se
o banco guarda a forma que o cliente enviou, o `unique` só enxerga **texto
igual** — e duas formas do mesmo documento viram **duas linhas**. O mesmo vale
para o número do cartão (`5555 0000 1111 2222` × `5555000011112222`).

**A regra:** o dado é gravado **na forma canônica**, e **toda escrita passa
pela mesma função de normalização** — o que torna o `unique` verdadeiro.

| Dado | Forma canônica | Tipo de valor (domínio) |
|---|---|---|
| Documento (CPF/CNPJ) | só dígitos, 11 ou 14 | `Documento` |
| Número do cartão | só dígitos, 13 a 19 | `NumeroDeCartao` |
| Telefone, CEP | só dígitos | a decidir (seção 4) |

```java
// dominio/cartao/NumeroDeCartao.java — o tipo guarda o dado e declara o papel
public record NumeroDeCartao(String digitos) implements Mascaravel {
    public NumeroDeCartao {
        // chega aqui já normalizado: 13 a 19 dígitos, senão lança (sem `if`, via Optional)
    }

    // o toString gerado do record imprimiria o número completo em qualquer log:
    // sobrescrever fecha esse vazamento na origem (SEGURANCA.md §5)
    @Override public String toString() { return mascarado(); }
}

// dominio/cartao/Mascaravel.java — a máscara, em arquivo próprio, sobre ComDigitos
// default String mascarado() → "****" + os 4 últimos dígitos

// dominio/cartao/NumeroDeCartaoDeTexto.java — entrada externa: remove espaços e hifens,
// valida 13–19 dígitos; devolve Resultado, nunca lança. A falha NÃO ecoa o texto recebido.
// NumeroDeCartaoDeTexto.converter("5555 0000 1111 2222") → Sucesso(5555000011112222)
```

**Consequências:**

- **Resolve o vazamento por `toString()`** de [`SEGURANCA.md`](SEGURANCA.md#5-logs-sem-dados-sensíveis):
  o tipo de valor é quem mascara, e nenhum `log.info("{}", cartao)` imprime o
  número completo.
- **A mascara é aplicada onde o dado nasce** — as variantes de erro continuam
  carregando a **forma mascarada** (`numero.mascarado()`), como já definido em
  [`ERROS.md`](ERROS.md#7-log-de-erro-estruturado).
- **A validação de formato é do tipo de valor**, em um único ponto
  ([`VALORES.md`](../codigo/VALORES.md#2-unidades-do-domínio)) — a
  `Mensagem*` valida só o **formato grosseiro** na borda (`@Pattern`).
- **Dígito verificador** (CPF/CNPJ, Luhn) **não é exigido** nesta entrega;
  entra em `NumeroDeCartaoDeTexto` se o negócio exigir, sem tocar no resto.
- **Texto livre** (`nome`, `rua`) **não** é normalizado nem pesquisado: não há
  busca por texto, então a colação do banco não influencia comportamento.

**Teste de contrato:** dois cadastros do mesmo documento em **formas
diferentes** (`123.456.789-01` e `12345678901`) → o segundo recebe **409**
(`ErroDocumentoJaCadastrado`), não uma segunda linha.

---

## 12. Transações e Escopo de Escrita

**Regras:**

1. **Transação onde há escritas múltiplas que precisam ser atômicas — e onde
   há lock.** Débito é *ler cartão → travar → calcular → gravar cartão →
   gravar extrato*: duas escritas atômicas. **Crédito tem uma só escrita e
   ainda assim exige transação**, porque o `SELECT … FOR UPDATE` só se mantém
   enquanto a transação existe — *ler, somar e gravar* sem ela é o
   *lost update* do legado. *(Transação em torno de uma escrita única seria
   ruído; aqui é o que sustenta o lock.)*
2. **Dentro da transação, só I/O deste banco.** Nada de chamada HTTP, fila ou
   e-mail com um cartão travado
   ([`PERFORMANCE.md`](PERFORMANCE.md#7-transação-e-lock-curtos--o-servidor-não-é-assíncrono)).
3. **A fronteira da transação é o caso de uso** (`@Transactional` na camada de
   aplicação — exceção documentada à pureza da camada), **nunca o controller
   nem o adaptador**. Os adaptadores de **leitura** marcam
   `@Transactional(readOnly = true)`.
4. **Sem SQL de controle de transação à mão** (`START TRANSACTION`, `BEGIN`).
5. **Um lock por transação** (seção 7); se mais de um for necessário, em
   ordem crescente de `id`.
6. **Timeout explícito**: `@Transactional(timeout = …)` e timeout de lock
   ([`PERFORMANCE.md`](PERFORMANCE.md#9-timeouts-e-limites)) — a requisição não
   é cancelável no modelo Servlet.
7. **Falha vira resposta tipada**: erro previsível, na porta 1; exceção só
   visível no commit, na porta 2
   ([`ERROS.md`](ERROS.md#3-proibição-de-captura-genérica--e-as-duas-portas-de-conversão)).
   **Retentativa automática só de operação idempotente** — nenhuma operação
   deste projeto o é; nada é retentado às cegas.

**Armadilhas do proxy transacional do Spring** (cada uma faz o
`@Transactional` **não ter efeito, em silêncio**):

- **Chamada interna** (`this.metodo()` dentro da mesma classe) **não passa
  pelo proxy** — a anotação é ignorada.
- **Método não público** não é interceptado.
- **Classe `final`** não pode ser *proxyada* (CGLIB) — por isso os casos de
  uso não são `final`
  ([`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md#5-casos-de-uso-são-classes-públicas-na-aplicação)).
- **A exceção que não é `RuntimeException`** não faz *rollback* por padrão.

---

## 13. SQL: Só JPQL e Consulta Derivada

É a detalhação da regra da seção 1.

**Proibido no código do projeto:**

- **`@Query(nativeQuery = true, …)`** e `createNativeQuery` — o legado tem
  quatro. A razão **não** é portabilidade multi-banco (que o projeto não
  promete), é a **dependência da grafia exata** de tabela e coluna, que a
  renomeação da seção 3 quebraria em silêncio.
- **Concatenação de texto em consulta** (ver
  [`SEGURANCA.md`](SEGURANCA.md#7-proteção-contra-injeção)).
- **Hint de índice, comentário de otimização ou *pragma* de dialeto**: o
  índice nasce na entidade ([`PERFORMANCE.md`](PERFORMANCE.md#4-índices-nas-colunas-consultadas)).
- **Supor comportamento de um banco**: ordem de `NULL` (colunas `NOT NULL`
  resolvem), sintaxe de paginação (`Pageable` resolve), caixa de texto.

**Permitido:** consulta derivada (`findByNumero…`), **JPQL** com parâmetro
nomeado, e as anotações do **Hibernate** que descrevem o modelo
(`@BatchSize`, `@Check`) — são metadados da entidade, não SQL de dialeto.

**Não há camada de escape por dialeto** — só há um provedor. Se um recurso genuinamente exclusivo de um banco aparecer,
ele entra com contrato único, implementação isolada na infraestrutura, teste
e registro — ou não entra.

---

## 14. Conexão, Segredos e Falha de Banco

Em grande parte coberta em outros documentos; aqui só o que é do banco:

- **Usuário do banco com o mínimo de privilégio** — o `sa` do H2 em memória é
  administrador e descartável; **em um banco real, nunca**
  ([`SEGURANCA.md`](SEGURANCA.md#3-mínimo-de-privilégios)).
- **Credencial e segredos**: regra de [`SEGURANCA.md`](SEGURANCA.md#4-segredos--regra-curta)
  — nada de segredo real no repositório; `sa/sa` é demonstrativo.
- **Nenhum acesso a banco fora do adaptador** — repositório Spring Data é
  *package-private* e só o `*Adaptador` é público
  ([`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md)).
- **Falha de conexão** responde com o corpo padronizado, **sem** URL de
  conexão, host ou SQL ([`ERROS.md`](ERROS.md)); o esgotamento do pool talvez
  mereça 503 — decisão em aberto em
  [`PERFORMANCE.md`](PERFORMANCE.md#9-timeouts-e-limites).
- **Pool e timeouts são configurados, não herdados**
  ([`PERFORMANCE.md`](PERFORMANCE.md#9-timeouts-e-limites)).
- **Nenhum `DataSource`, `EntityManager` ou conexão em estado compartilhado
  entre requisições** — beans são singletons; estado mutável neles é bug de
  concorrência.

---

## 15. Testes Que Fixam Esta Página

| Teste | Garante | Tipo |
|---|---|---|
| `CoerenciaDeEscalaTeste` | `scale` de toda coluna monetária == `Dinheiro.ESCALA` | Integração |
| `deve_recusar_saldo_negativo_quando_gravar_direto_no_banco` | o `CHECK (saldo >= 0)` **existe e é aplicado pelo H2** — valida a suposição do `@Check` | Integração |
| `deve_recusar_documento_duplicado_quando_forma_diferente` | forma canônica + `uk_consumidores_documento` | Contrato |
| `deve_recusar_dois_cartoes_do_mesmo_tipo_para_o_mesmo_consumidor` | `uk_cartoes_consumidor_tipo` | Integração |
| `deve_preencher_a_auditoria_quando_criar_e_avancar_ao_atualizar` | `data_criacao`/`data_atualizacao`, com relógio controlável | Integração |
| `deve_incrementar_a_versao_quando_atualizar` | `@Version` funciona | Integração |
| `deve_responder_409_quando_versao_enviada_diverge` | comparação explícita no `PUT` | Contrato |
| `deve_gravar_o_enum_como_texto_quando_persistir_o_tipo_do_cartao` | `EnumType.STRING`, não ordinal | Integração |
| `NomenclaturaDeSchemaTeste` | todo `unique`/índice/FK é nomeado e segue `uk_`/`idx_`/`fk_` | Arquitetura |
| `deve_recusar_atualizar_extrato` | `ExtratoPorta` só oferece `registrar` (não existe `atualizar`) | Arquitetura |
| `TravaDeCartaoTeste` | a espera por trava tem teto e ele dispara; a espera que termina antes do teto tem sucesso | Integração |

*(Existem hoje: `SchemaDoCartaoTeste` (escala, `CHECK`, `unique`, enum como texto,
auditoria), `NomenclaturaDeSchemaTeste`, `CartaoAdaptadorTeste` (inclusive o
`@Version`), `ExtratoAdaptadorTeste` (inclusive a FK sem cascata) e
`TravaDeCartaoTeste`. Dependem do consumidor: o documento duplicado e o
`uk_cartoes_consumidor_tipo`.)*

*(Entram na tabela da seção 14 de [`TESTES.md`](TESTES.md#14-testes-exigidos-pelos-outros-documentos).)*

---

## Decisões Deste Documento

Cada linha é uma escolha **deste projeto**, registrada para não virar
"deriva silenciosa". O consolidado está em
[`DECISOES.md`](../fundamentos/DECISOES.md):

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| Schema em português, plural, *snake_case*, constraints nomeadas | nomes gerados pelo Hibernate | ilegibilidade e irreversibilidade em log/erro |
| `NUMERIC(19,6)` para dinheiro | `DOUBLE`; `NUMERIC(19,2)` | fidelidade de fração de centavo ([`VALORES.md`](../codigo/VALORES.md#6-dinheiro-e-percentuais)) |
| Lock pessimista no saldo (+ `@Version`) | otimista puro | duas compras legítimas simultâneas devem ambas passar |
| `IDENTITY` por tabela | `AUTO` (sequência global) | simplicidade e previsibilidade |
| Auditoria mínima (`data_criacao`, `data_atualizacao`) | colunas de usuário | não há autenticação |
| Tipos `Documento` e `NumeroDeCartao`, forma canônica | `String` crua | `unique` verdadeiro; mascaramento na origem |
| `CHECK (saldo >= 0)` | só validação no domínio | última linha de defesa |
| Extrato imutável | `update`/`delete` no extrato | registro financeiro |
| Sem migrações; `ddl-auto: create-drop` | Flyway/Liquibase | dependência nova proibida |
| Sem `MODE=PostgreSQL` no H2 | aproximar o dialeto | falsa segurança sem banco real para validar |

---

## Anti-Padrões

| Anti-padrão | Correto |
|---|---|
| `@Query(nativeQuery = true, …)` com nome de coluna em maiúsculas | consulta derivada ou JPQL (seção 13) |
| Tabela `Consumer`, coluna `FOOD_CARD_NUMBER` | `consumidores`, snake_case em português (seção 3) |
| Constraint, índice ou FK sem nome | nomeado na anotação (seção 3) |
| Saldo em `DOUBLE`; dinheiro `NUMERIC(19,2)` | `NUMERIC(19,6)` (seção 4) |
| Número de cartão/documento como `int` ou `NUMERIC` | `VARCHAR` de dígitos (seção 4) |
| `@Enumerated(EnumType.ORDINAL)` (o padrão!) | `EnumType.STRING` (seção 4) |
| Toda coluna anulável; `0` como "não informado" | `NOT NULL`; ausência modelada (seção 4) |
| Saldo protegido só pela validação do domínio | `CHECK (saldo >= 0)` também no banco (seção 4) |
| `@ManyToOne` sem `fetch`; `cascade = REMOVE` sobre dado financeiro | `LAZY` explícito; FK restritiva (seções 4 e 8) |
| `GenerationType.AUTO` com sequência global | `IDENTITY` por tabela (seção 5) |
| Caso de uso atribuindo `data_criacao` / auditoria no domínio | `@CreatedDate`/`@LastModifiedDate` só na entidade (seção 6) |
| `@Version` no banco, mas `PUT` não confere a versão enviada | comparação explícita + `@Version` como rede (seção 7) |
| Lock pessimista sem justificativa; travar dois cartões em ordem livre | justificativa registrada; um lock por transação, ordem por `id` (seção 7) |
| `update`/`delete` em `extratos` | só `registrar` (seção 8) |
| `ddl-auto: update` em banco persistente; `schema.sql` paralelo | `validate` + migrações; entidades como única definição (seção 9) |
| Gravar o documento como veio (`123.456.789-01`) | forma canônica antes de gravar (seção 11) |
| `log.info("{}", cartao)` imprimindo o número completo | `toString()` mascarado no tipo de valor (seção 11) |
| Crédito: ler → somar → gravar sem transação | transação com lock, mesmo com uma escrita (seção 12) |
| `this.metodoTransacional()` esperando transação | chamada pelo proxy; fronteira no caso de uso (seção 12) |
| Chamada HTTP dentro da transação travada | transação só com I/O do banco (seção 12) |
| `MODE=PostgreSQL` no H2 como "prova" de compatibilidade | risco declarado; sem banco real para validar (seção 2) |

---

## Checklist

Antes de abrir um PR que mexe em schema, entidade ou acesso a dados:

- [ ] Tabela e coluna novas seguem a seção 3 (plural, *snake_case*, português)?
- [ ] Todo `unique`, índice e FK novo é nomeado na anotação (`uk_`/`idx_`/`fk_`)?
- [ ] Dinheiro é `NUMERIC(19,6)` com escala igual a `Dinheiro.ESCALA`?
- [ ] Documento, cartão e telefone são `VARCHAR` de dígitos, gravados na forma canônica?
- [ ] Enum é `EnumType.STRING`?
- [ ] A coluna é `NOT NULL`, ou a ausência está modelada no domínio?
- [ ] Toda associação declara `fetch` (e nenhuma `cascade = REMOVE` sobre dado financeiro)?
- [ ] Escrita nova concorrente tem `versao` (cadastro) ou lock com justificativa (saldo)?
- [ ] O `PUT` confere a `versao` enviada explicitamente?
- [ ] Nenhuma consulta nativa ou concatenada entrou?
- [ ] Nenhum caso de uso atribui campo de auditoria?
- [ ] A transação envolve só I/O do banco, com no máximo um lock?
- [ ] `ddl-auto` continua `create-drop` (só banco descartável)?
- [ ] O PR lista a diferença H2 × banco real que a mudança toca (lock, mensagem de erro, `CHECK`)?
- [ ] A tabela da seção 15 e a de `DECISOES.md` foram atualizadas?
- [ ] Este documento descreve o schema atual (não o de quando nasceu)?

---

**[Testes ←](TESTES.md)**
