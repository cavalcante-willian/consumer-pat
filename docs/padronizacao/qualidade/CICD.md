[← Índice](../../PADRONIZACAO_INDEX.md) · [← BUILD.md](BUILD.md) · [Próximo: COMMITS.md](../runtime/COMMITS.md)

# CI/CD — Java 17 / Gradle / GitHub Actions

> **Escopo**: o pipeline de integração contínua do projeto, em **GitHub
> Actions**: fluxos reutilizáveis (`_*.yml`), filtro de paths, um job-gate
> `CI` e auto-aprovação. O fluxo tem duas etapas (branch → `main` do fork →
> repositório do enunciado). Ver [Premissas do Pipeline](#premissas-do-pipeline).
>
> **Nota sobre o que foi verificado**: os workflows passam no `actionlint` e as
> **ações estão fixadas em SHA conferidos contra as tags** pela API do GitHub.
> O primeiro PR contra a `main` do fork rodou o pipeline de ponta a ponta nas
> trilhas `detectar` e `editorconfig`; as trilhas `build`, `javadoc`, `testes`
> e `markdownlint` só rodam quando um PR altera código Java ou Markdown.

Define o pipeline de integração contínua, o gate de merge, o que a
configuração do GitHub precisa ter (e que **não** fica no repositório), a
publicação — que **não existe** — e o contrato de implantação para o futuro.

---

## Premissas do Pipeline

| Tema | Regra neste projeto |
|---|---|
| Plataforma | GitHub Actions, com *runner* **self-hosted** |
| Estrutura | `ci.yml` + `_*.yml` reutilizáveis + filtro de paths + gate `CI` |
| Build | `./gradlew compileJava compileTestJava` (**JDK 17**) |
| Análise de documentação | **`./gradlew javadoc`** (o `-Werror` já está na compilação) |
| Cobertura direcionada | **não existe** (o plugin baixaria artefato — [`BUILD.md`](BUILD.md#9-formatação-e-análise-estática)) |
| Testes de interface ou instrumentados | **não se aplicam** (a API é só REST) |
| Gatilho | `pull_request` → `main` **do fork** (etapa 1 do fluxo) |
| Auto-aprovação e *squash* | GitHub App, para o PR do mantenedor |
| `taggear` e `release` | **não incluídos** — não há release (seção 3) |
| Segredos de execução | placeholder do Spring ([`SEGURANCA.md`](SEGURANCA.md#4-segredos--regra-curta)) |
| Matriz de plataformas e de bancos | **uma** combinação: JDK 17 + H2 |

---

## 1. Pipeline de CI

```text
detectar ──► build ──► testes ─┐
        ├──► javadoc ──────────┼──► CI (gate) ──► auto-aprovar
        ├──► editorconfig ─────┤
        └──► markdownlint ─────┘
```

| Arquivo | Papel | Comando |
|---|---|---|
| `ci.yml` | gatilho, ordem, **gate `CI`** e auto-aprovação | — |
| `_detectar.yml` | filtro de paths → `tem_java`, `tem_md` | `dorny/paths-filter` |
| `_build.yml` | compila código e testes | `./gradlew compileJava compileTestJava` |
| `_javadoc.yml` | gera e valida o Javadoc | `./gradlew javadoc` |
| `_testes.yml` | suíte inteira | `./gradlew test` |
| `_editorconfig.yml` | conformidade com o `.editorconfig` | `editorconfig-checker -config .ecrc` |
| `_markdownlint.yml` | documentação | `markdownlint-cli2 "**/*.md"` |
| `actionlint.yaml` | rótulos do *runner*, para o `actionlint` | — |
| `CODEOWNERS` | quem aprova cada caminho | — |

### Gatilhos e filtro de paths

| Gatilho | O que roda |
|---|---|
| `pull_request` para `main` (a do **fork**) | `detectar`, as trilhas **cujo filtro casou**, o gate e a auto-aprovação |
| Push de branch | **nada** — a verificação local antes do push (seção 6) cobre esse momento |

- **`tem_java`**: `**/*.java`, `**/*.gradle`, `src/{main,test}/resources/**`,
  `gradle.properties`, `gradle.lockfile`, `gradle/wrapper/**`, `gradlew*`. Sem
  mudança aí, `build`, `javadoc` e `testes` são **`skipped`**.
- **`tem_md`**: `**/*.md`. Sem Markdown alterado, `markdownlint` é `skipped`.
- **`editorconfig` roda sempre.**
- **`skipped` é aceito pelo gate**: "o filtro não achou mudança relevante".

### Regras

- **Os mesmos comandos do local** ([`BUILD.md`](BUILD.md#8-comandos-padrão-e-artefatos-descartáveis)).
- **JDK 17.** O *runner* tem o JDK 27 como padrão e o Gradle 7.6 não roda nele
  ([`BUILD.md`](BUILD.md#1-build-autocontido-no-repositório)): cada trilha fixa
  `JAVA_HOME=/usr/lib/jvm/java-17-openjdk` e confere com `java -version`. O caminho é
  da **máquina do *runner***.
- **Nenhum passo é pulado por "estar quebrado"**; nada de `@Disabled` sem prazo e motivo.
- **`concurrency` cancela a execução anterior do mesmo PR.**
- **Passo novo entra no mesmo PR que o implementa**, com este documento atualizado.
- **Sem telemetria:** nenhum `--scan`.

---

## 2. Gate de Merge

```text
branch ──PR──► main do fork ──PR──► main do repositório do enunciado
        (etapa 1: CI + gate)         (etapa 2: revisão de quem recebe)
```

**Etapa 1 — branch → `main` do fork.** Nenhum PR é mesclado sem o status check
**`CI`** em `success`, a aprovação do dono do caminho (`CODEOWNERS`), título e
mensagem em Conventional Commits português ([`COMMITS.md`](../runtime/COMMITS.md)) e
a seção `## Fora de escopo e riscos` no PR.

**O job-gate `CI`** é o **único** status check obrigatório: roda sempre
(`if: always()`) e falha se qualquer trilha não terminou em `success` ou `skipped`.
Trilha nova só precisa entrar no `needs:` do gate.

**Auto-aprovação (`auto-aprovar`).** Para o PR do **mantenedor**, com o gate verde, um
**GitHub App** aprova e faz *squash* (`gh pr merge --squash --delete-branch --admin`).
Cada PR vira **um** commit na `main` do fork, com o **título do PR** como mensagem — por
isso **cada item do plano de commits é um PR**
([`COMMITS.md`](../runtime/COMMITS.md#11-plano-de-commits-da-refatoração)).

**Etapa 2 — `main` do fork → repositório do enunciado**: PR aberto da `main`
do fork para a `main` do repositório do enunciado. Não há CI de lá para cá;
quem recebe revisa.

### Configuração do GitHub (à mão, **não** fica no repositório)

Em **Settings → Branches → `main`** do fork:

- Require a pull request before merging
- **Require review from Code Owners** (sem isto o `CODEOWNERS` não tem efeito)
- Require status checks to pass → **`CI`**
- Do not allow bypassing (o `--admin` do auto-merge é a exceção deliberada, do App)

### Regras do gate

- **Verde e aprovação são conjuntos.**
- **`main` do fork não recebe commit nem push direto.**
- **Mudança de comportamento sem teste não passa**
  ([`TESTES.md`](TESTES.md#8-cobertura-mínima)); mudança de contrato sem teste de
  contrato também ([`API.md`](API.md#12-contrato-público-e-testes-de-contrato)).
- **Limite conhecido:** o CI não valida o título do PR, e o *squash* o usa
  como mensagem do commit. A conferência do formato
  ([`COMMITS.md`](../runtime/COMMITS.md#1-formato-do-commit)) é de quem abre e
  de quem revisa o PR.

---

## 3. Publicação — Não Existe

Workflows de tag e de release (`taggear.yml` e `release.yml`) **não foram
incluídos** ([`DECISOES.md`](../fundamentos/DECISOES.md)):

- Não há release: o `bootJar` não tem consumidor por distribuição.
- A versão tem fonte única (`version` do `build.gradle`); a tag `vX.Y.Z`, se
  existir, deriva dela ([`BUILD.md`](BUILD.md#3-versão-de-build-rastreável)).
- O changelog é um comando ([`COMMITS.md`](../runtime/COMMITS.md#9-changelog)).
- Para incluir depois: `taggear.yml` (push na `main` que muda `version` → tag) e
  `release.yml` (tag → `bootJar` + `gh release create --generate-notes`), com o mesmo
  GitHub App, no mesmo PR que revoga esta seção.

---

## 4. Implantação — Contrato Para o Futuro

Hoje a aplicação roda com `./gradlew bootRun` sobre **H2 em memória**. **Se** um dia
houver implantação:

1. **Backup completo**, se houver banco persistente ([`BANCO.md`](BANCO.md#10-backup--recomendação)).
2. **Schema antes do código**, com migrações — que exigem dependência nova (fora do
   desafio); sem elas, `ddl-auto: validate` recusa subir sobre schema divergente
   ([`BANCO.md`](BANCO.md#9-schema-sem-migrações)).
3. **Cópia do JAR e execução** — o JRE 17 é pré-requisito do servidor:

   ```bash
   java -jar consumer-pat-0.0.1.jar --spring.profiles.active=prod
   ```

   O perfil `prod` desliga `/h2-console` e Swagger e fixa o log em `INFO`.
4. **Health check de aceitação:** `GET /api/v1/saude` = **200**
   ([`OBSERVABILIDADE.md`](../runtime/OBSERVABILIDADE.md#7-health-check--apiv1saude)).
   Não voltou saudável: **reverte**.

**Segredos:** nada real no JAR nem no repositório; *placeholder* do Spring
(`${SENHA_BANCO}`) ([`SEGURANCA.md`](SEGURANCA.md#4-segredos--regra-curta)).

**Rollback:** volta-se o JAR anterior (API *stateless*); schema só por migração
*expand/contract*; nunca `ALTER` à mão. Atenção: reiniciar no meio de um `POST` deixa o
cliente sem saber se a compra ocorreu ([`API.md`](API.md#11-concorrência-idempotência-e-conflitos)).

---

## 5. Segredos, *Runner* e Ambiente do Pipeline

### Segredos (Settings → Secrets and variables → Actions)

| Segredo | Para quê | Quem usa |
|---|---|---|
| `CLIENT_ID` | Client ID do GitHub App (`Iv23…`), no parâmetro `client-id` | `auto-aprovar` |
| `APP_PRIVATE_KEY` | chave privada do GitHub App | `auto-aprovar` |

- **Build e testes não precisam de segredo** (Maven Central público, H2 em memória).
- O App tem o mínimo (*pull requests* e *contents* em escrita); o token é gerado
  **dentro do job** por `actions/create-github-app-token`.

### O *runner* self-hosted — risco declarado

Os jobs rodam num *runner* do mantenedor do fork (`[self-hosted, linux, x64,
arch]`) com JDK 17, `gh`, `editorconfig-checker` e `markdownlint-cli2`
instalados. O GitHub desaconselha *runner* próprio em repositório público,
porque um PR de terceiro executaria código **na máquina do runner**:

- **Fork público:** em Settings → Actions → General → *Fork pull request workflows*,
  exigir aprovação para colaboradores externos; ou manter o fork **privado**.
- Não rodar o *runner* como `root` nem na conta que guarda chaves pessoais.
- *Runner* efêmero (um por job) seria melhor — fora do escopo.

### Ações de terceiros — fixadas por SHA

| Ação | Tag | Uso |
|---|---|---|
| `actions/checkout` | v7 | todas as trilhas |
| `gradle/actions/setup-gradle` | v6 | `build`, `javadoc`, `testes` |
| `dorny/paths-filter` | v4 | `_detectar` |
| `actions/create-github-app-token` | v3 | `auto-aprovar` |

- SHAs **conferidos contra as tags pela API do GitHub**; atualizar é um commit que
  troca SHA e comentário.
- A `create-github-app-token` recebe o **`client-id`** (o `app-id` está deprecado e não aceita o Client ID `Iv23…`). O `actionlint` local já valida esse nome.
- `editorconfig-checker` e `markdownlint-cli2` **não usam ação**: vêm no *runner*.
- **Permissões:** `contents: read` por padrão; escrita só em `auto-aprovar`.

---

## 6. Verificação Local Antes do Push

O CI **não substitui** a verificação local, que evita gastar uma execução em algo
que os mesmos comandos pegariam em segundos
([`COMMITS.md`](../runtime/COMMITS.md#7-verificações-antes-do-push)): `./gradlew
build`, `./gradlew javadoc`, `./gradlew test` e, ao mexer em `.github/`,
`actionlint`.

- **A verificação local é o subconjunto rápido do CI**; o CI cobre o que ela não
  garante — ela depende de quem a roda, e um clone que a pula passa por qualquer commit.
- Nenhuma das duas substitui revisão humana; bypass é decisão registrada.

---

## 7. Onde Fica Cada Coisa

```text
.github/
├── CODEOWNERS
├── actionlint.yaml
└── workflows/
    ├── ci.yml  _detectar.yml  _build.yml  _javadoc.yml
    └── _testes.yml  _editorconfig.yml  _markdownlint.yml

.editorconfig · .ecrc · .markdownlint-cli2.yaml   # versionados — o CI os lê
```

> **Consequência assumida:** a `main` do fork contém `.github/`, e o PR da etapa 2 o
> leva no *diff*. No repositório do enunciado, os *workflows* com rótulo `arch` ficam
> na fila sem *runner* e o `CODEOWNERS` cita um usuário que não é dono de lá. Se
> incomodar, abrir o PR final de uma branch sem esses arquivos (manual).

---

## Anti-Padrões

| Anti-padrão | Correto |
|---|---|
| Vários status checks obrigatórios | Um só: o gate `CI` |
| Trilha nova fora do `needs:` do gate | Entra no `needs:` e no `for` do gate |
| `JAVA_HOME` no JDK padrão do *runner* | JDK 17 fixado em cada trilha |
| Passo de CI que não roda localmente | Mesmo comando nos dois |
| `@Disabled` sem prazo e motivo | Corrigir ou remover com justificativa |
| Ação por tag móvel (`@v7`) | SHA do commit, tag em comentário |
| `permissions` amplas em todos os jobs | `contents: read`; escrita só em `auto-aprovar` |
| *Runner* self-hosted com fork público sem aprovação externa | Exigir aprovação, ou fork privado |
| `CODEOWNERS` sem "Require review from Code Owners" | Habilitar na proteção de branch |
| Título de PR fora do formato de commit | conferir o título antes de abrir: o *squash* o usa como mensagem |
| Push direto na `main` do fork | Só PR mesclado |
| `/h2-console` ou Swagger ativos em implantação | Perfil `prod` |

---

## Checklist

- [ ] `./gradlew build` passou com o JDK 17, e o resultado está na seção Testes do PR?
- [ ] O título do PR está no formato de commit, e o corpo segue o template de [`COMMITS.md`](../runtime/COMMITS.md#8-pull-requests)?
- [ ] Mudança de comportamento com teste; de contrato, com teste de contrato?
- [ ] **Ao mexer em `.github/`:** `actionlint` passa, ações por SHA conferido, `permissions` não cresceu?
- [ ] **Trilha nova:** entrou no `needs:` e no `for` do gate?
- [ ] O check `CI` e o *Code Owners* seguem obrigatórios na `main` do fork?
- [ ] Fork público: aprovação de PR externo ligada?
- [ ] PR final (etapa 2): o `.github/` no *diff* foi escolha consciente?
- [ ] Este documento descreve o estado atual?

---

## Decisões Deste Documento

O consolidado está em [`DECISOES.md`](../fundamentos/DECISOES.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| CI no GitHub Actions, com fluxos reutilizáveis, filtro de paths e gate `CI` | pipeline único; sem CI | cada trilha tem uma responsabilidade e um status check cobre todas |
| Fluxo em duas etapas (fork → enunciado) | PR direto ao enunciado | CI de fork só roda em PR contra o próprio fork |
| *Runner* self-hosted `arch` | `ubuntu-latest` | ferramentas já instaladas; risco declarado |
| `JAVA_HOME` fixo por trilha | `setup-java` | o JDK padrão do *runner* é mais novo que o 19, em que o Gradle 7.6 não roda |
| Auto-aprovação e *squash* por GitHub App | merge manual | autor não aprova o próprio PR |
| Cada item do plano é um PR | um PR com 15 commits | o *squash* reduz o PR a um commit |
| Sem `taggear` e `release` | incluí-los | não há release nem consumidor |
| Sem cobertura direcionada e sem testes instrumentados | incluí-los | JaCoCo baixa artefato; a API não tem interface para instrumentar |
| `javadoc` no lugar de `ktlint`/`detekt` | sem trilha | o `build` do Gradle não roda Javadoc |
| Ações fixadas por SHA conferido | tag móvel | a revisão vê a troca |
| `.ecrc` e markdownlint versionados | locais | o CI os lê |
| Etapa 2 leva `.github/` no *diff* | PR de branch limpa | consequência aceita |

---

**[Mensagens de Commit →](../runtime/COMMITS.md)**
