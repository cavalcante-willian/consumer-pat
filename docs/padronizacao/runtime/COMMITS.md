[← Índice](../../PADRONIZACAO_INDEX.md) · [← OBSERVABILIDADE.md](OBSERVABILIDADE.md)

# Mensagens de Commit — Java 17 / Gradle / Fork

> **Escopo**: o padrão de mensagens de commit e de Pull Request do projeto:
> Conventional Commits em português, o vocabulário de escopos (as áreas
> **deste** projeto), o fluxo (**branch → PR para a `main` do fork, com CI →
> PR final para o repositório do enunciado**), a mensagem escrita em arquivo,
> o changelog (um comando) e o **plano de commits** da refatoração. Sendo um
> **fork**, o histórico pode seguir o padrão desde o primeiro commit.
>
> **Nota sobre exemplos**: as mensagens de commit abaixo são ilustrativas — a
> regra normativa é o texto conceitual. As verificações da
> [seção 7](#7-verificações-antes-do-push) são comandos do Gradle e do git que
> qualquer pessoa roda; o CI do fork repete as mesmas
> ([`CICD.md`](../qualidade/CICD.md)).

Define Conventional Commits 1.0.0 em português: formato, tipos, vocabulário
fechado de escopos, exemplos, rodapés, atomicidade, branches, o fluxo e as
verificações que o impõem, estrutura do Pull Request, o changelog e as regras
de markdown que mantêm a mensagem legível em qualquer dispositivo.

**Quando consultar:**

- Antes de qualquer commit — tipo, escopo e formato da descrição
- Ao nomear uma branch
- Ao escrever a mensagem do commit e a descrição do PR
- Ao planejar a ordem dos commits da refatoração ([seção 11](#11-plano-de-commits-da-refatoração))

---

## Premissas do Fluxo

| Tema | Regra neste projeto |
|---|---|
| Stack nos exemplos | Java 17 / Gradle (`./gradlew test`) |
| Escopos | **áreas deste projeto**: `consumidores`, `cartoes`, `extratos`, `saude`… |
| Gate remoto | **CI do fork** (gate `CI`, [`CICD.md`](../qualidade/CICD.md#2-gate-de-merge)) + verificação local + checklist do PR |
| Fluxo | **em duas etapas**: `branch` → PR para a `main` **do fork** (com CI) → PR da `main` do fork para o repositório do enunciado |
| Onde mora a mensagem | **em arquivo**, revisada antes do uso (seção 7) |
| Breaking change | **`!` no título e** `BREAKING CHANGE:` no corpo, **consistentes** |
| `release/vX.Y.Z` e escopo `ci` | `release/` **não existe** (sem release); o escopo `ci` **existe** |
| Changelog | **um comando** sobre `git log` (seção 9) |
| Schema no mesmo PR | **entidade/schema** no mesmo PR (não há migração — [`BANCO.md`](../qualidade/BANCO.md#9-schema-sem-migrações)) |
| Corpo do PR | **4 seções + 2 condicionais** (seção 8) |

---

## 1. Formato do Commit

Todo commit segue Conventional Commits 1.0.0, com descrição em **português, no
infinitivo**:

```text
tipo(escopo): descricao no infinitivo

[corpo opcional — o porquê, não o que: o diff já mostra o que]

[rodapé opcional — BREAKING CHANGE:, Refs:]
```

Com *breaking change*, o título leva `!` antes dos dois-pontos:

```text
tipo(escopo)!: descricao no infinitivo
```

**Por quê:** o histórico é matéria-prima de duas coisas que ninguém faz à
mão — o changelog (seção 9) e a leitura do `git log` na mesma língua do
código. Mensagem fora do padrão não é estética: é entrada ruim no changelog e
histórico que máquina não processa.

### Tipos válidos

| Tipo | Quando usar |
|---|---|
| `feat` | Nova funcionalidade para quem consome a API |
| `fix` | Correção de bug |
| `docs` | Alteração apenas de documentação, incluindo `docs/padronizacao/` |
| `style` | Formatação sem mudança de comportamento — raro à mão ([`BUILD.md`](../qualidade/BUILD.md#9-formatação-e-análise-estática)) |
| `refactor` | Refatoração sem mudança de comportamento observável |
| `perf` | Melhoria de desempenho ([`PERFORMANCE.md`](../qualidade/PERFORMANCE.md)) |
| `test` | Adição ou correção de testes ([`TESTES.md`](../qualidade/TESTES.md)) |
| `build` | Build, dependências e lock file ([`BUILD.md`](../qualidade/BUILD.md)) |
| `ci` | Pipeline de CI/CD (`.github/`) |
| `chore` | Manutenção que não se encaixa nos tipos acima |

Tipo inventado (`update`, `hotfix`, `wip`, `minor`) não existe: o conjunto é
fechado.

### Regras da descrição

- **Modo infinitivo**: "adicionar", "corrigir", "remover", "extrair" — nunca
  "adiciona", "adicionado", "adicionando".
- **Minúscula após os dois pontos.**
- **Sem ponto final.**
- **Até 72 caracteres** na primeira linha, contados em **caracteres** (não em
  bytes: `ã` e `ç` contam um) — `git log --oneline` não quebra linha; o que
  passa disso é truncado na leitura.
- **Escopo presente e válido**, da [seção 2](#2-escopos).
- **Português**, descrição e corpo, sem exceção. As palavras-chave de tipo
  (`feat`, `fix`…) permanecem em inglês: são o vocabulário do padrão.
- **Segunda linha em branco** quando há corpo.
- **`!` e `BREAKING CHANGE:` andam juntos**: o corpo com `BREAKING CHANGE:` sem
  `!` no título é recusado, porque as ferramentas de changelog procuram o `!`.
- **Se há `Refs:`, o formato é `Refs: #NNN`** (vários: `Refs: #12, #13`).
- **O corpo explica o porquê, não o quê** — o diff mostra o quê; o corpo diz
  por que aquilo precisava mudar, o que foi considerado em vez disso, o que o
  teste cobre.

```text
# CORRETO
feat(cartoes): registrar compra com ajuste fracionário do tipo

# ERRADO — particípio ou 3ª pessoa
feat(cartoes): registrada compra com ajuste fracionário do tipo
feat(cartoes): registra compra com ajuste fracionário do tipo
```

---

## 2. Escopos

O escopo é **obrigatório** e o vocabulário é **fechado**. A tabela é o
inventário das áreas do sistema; área nova que não couber em nenhuma linha
entra na tabela **no mesmo PR** que cria o commit com aquele escopo.

**Áreas de negócio:**

| Escopo | Área |
|---|---|
| `consumidores` | Cadastro, atualização, consulta e listagem de consumidores |
| `cartoes` | Cartões, crédito de saldo e registro de compra |
| `extratos` | Extrato de compras |
| `saude` | Health check (`/api/v1/saude`) |

**Camadas do sistema** (mudança que cruza áreas ou é específica de uma
camada):

| Escopo | Área |
|---|---|
| `dominio` | Entidades, tipos de valor (`Dinheiro`…), regras, erros |
| `aplicacao` | Casos de uso e DTOs internos |
| `infraestrutura` | Adaptadores, JPA, log |
| `api` | Controllers, mensagens, filtros, contrato REST |
| `banco` | Schema, entidades, constraints, auditoria |

**Transversais:**

| Escopo | Área |
|---|---|
| `seguranca` | Cabeçalhos, validação de entrada, CORS, mascaramento |
| `performance` | Consultas, paginação, limites, timeouts |
| `observabilidade` | Log, correlação, health |
| `configuracao` | `application*.yml`, perfis |
| `padronizacao` | A suíte `docs/padronizacao/` |
| `testes` | Suíte de testes, fábricas e fakes |
| `docs` | Documentação geral sem assunto próprio (README) |
| `build` | `build.gradle`, wrapper, lock file, `.editorconfig` |

Escopo é em português, minúsculo, com hífens quando composto, nunca caractere
especial nem espaço.

---

## 3. Exemplos

### Corretos

```text
feat(cartoes): registrar compra com ajuste fracionário do tipo

fix(cartoes): impedir saldo negativo em compra simultânea

perf(consumidores): paginar listagem e projetar só id e nome

refactor(dominio): extrair Resultado e erros tipados do controller

test(cartoes): adicionar teste de concorrência de compra

docs(padronizacao): adaptar CONDICIONAIS.md para Java 17

build(build): fixar o JDK 17 e remover mavenLocal

chore(build): atualizar o gradle.lockfile

fix(consumidores)!: preservar o saldo ao atualizar dados cadastrais

O legado fazia repository.save() do objeto recebido, então um PUT com
saldo no corpo sobrescrevia o saldo do cartão. A mensagem de entrada
passa a não ter campo de saldo, e o campo desconhecido é recusado (400).

BREAKING CHANGE: PUT /consumer/updateConsumer foi substituído por
PUT /api/v1/consumidores/{id}; clientes devem migrar de rota e deixar de
enviar saldo no corpo

Refs: #7
```

### Proibidos

```text
# Idioma errado
feat(cartoes): add purchase support

# Verbo não está no infinitivo
fix(cartoes): corrige cálculo do desconto

# Sem escopo
feat: registrar compra

# Escopo fora da tabela
fix(compras): corrigir cálculo do desconto

# Muito vago — não diz o que mudou (nenhuma regra automática pega isto; a revisão pega)
fix(cartoes): corrigir bug

# Com ponto final
fix(banco): nomear a constraint de número do cartão.

# Tipo inventado
update(api): melhorar listagem

# BREAKING CHANGE no corpo, mas sem "!" no título
feat(api): trocar as rotas do legado

# Dois assuntos em um commit
feat(cartoes): registrar compra e extrair o log para a porta
```

---

## 4. Branches

```text
<tipo>/<descricao-kebab-case>
```

- **Mesmo vocabulário de tipos do commit** — o nome da branch já diz o tipo de
  mudança que ela carrega.
- **Minúsculo, com hífens**, curto e descritivo: sem caractere especial, sem
  nome de pessoa, sem data.
- **`main` é a única branch permanente** e **não recebe commit nem push
  direto** — a proteção de branch do fork exige PR com o check `CI`
  ([`CICD.md`](../qualidade/CICD.md#2-gate-de-merge)). O trabalho acontece em
  branch do fork.
- **O fluxo deste projeto é `branch (fork) → Pull Request → repositório do
  enunciado`.** A `main` do fork é a branch de integração: recebe PRs
  (etapa 1, com CI e *squash*) e é a origem do PR final (etapa 2). Para trazer
  novidades do repositório original, use **Sync fork** do GitHub.
- **A branch tem de estar atualizada com a `main` do fork** antes de abrir o
  PR. Atrasada, atualiza-se com `git rebase origin/main`. A sincronia da `main`
  do fork com o `main` do repositório do enunciado só importa ao abrir o PR
  final.
- **A mensagem que vira histórico segue a [seção 1](#1-formato-do-commit)** —
  em *squash*, o título do PR é essa mensagem, por isso também é escrito no
  formato de commit ([seção 8](#8-pull-requests)).

```text
refactor/estrutura-em-camadas
fix/saldo-negativo-em-compra
feat/health-check-proprio
test/concorrencia-de-compra
docs/padronizacao-java
build/jdk-17-e-lock-file
```

---

## 5. Rodapés — Issues e Breaking Changes

Rodapé vem depois do corpo, separado por linha em branco:

| Rodapé | Quando usar |
|---|---|
| `Refs: #42` | A issue é contexto da mudança (formato **validado**) |
| `Closes #42` | O commit fecha a issue citada |
| `BREAKING CHANGE: <descrição em português>` | A mudança quebra consumidores da API ([`API.md`](../qualidade/API.md#2-versionamento-apiv1)) — **exige `!` no título** |

`BREAKING CHANGE` diz **o que quebra e o que o consumidor deve fazer**, em
português, porque aparece no changelog para humanos lerem.

**Aqui o `BREAKING CHANGE` é a regra, não a exceção, num ponto específico:** o
README do enunciado **autoriza** alterar os contratos, e a refatoração troca
as cinco rotas do legado (`/consumer/…`) por rotas versionadas em
`/api/v1/…`, `GET` por `POST` nas operações de escrita, e dinheiro como string.
O commit que **introduz** cada mudança de contrato leva `!` no título e o
rodapé — o mapa completo legado → novo está em
[`API.md`](../qualidade/API.md#mapa-do-legado-para-o-novo-contrato).

**Neste repositório, o contrato quebrou na remoção, e não na criação das rotas
novas.** Elas entraram ao lado das do legado, sem tirar nenhuma, e por isso não
quebraram ninguém: esses commits não levam `!`. Quem quebra é o commit que
**remove** o legado, e é ele que leva o `!` e o `BREAKING CHANGE:` com o mapa
das cinco rotas.

---

## 6. Commits Atômicos

- **Um commit = uma mudança lógica coerente.** A mensagem faz sentido isolada
  em `git log --oneline`.
- **Não misturar refatoração com funcionalidade** no mesmo commit — são
  revisões diferentes e revertíveis separadamente.
- **Cada commit compila e passa `./gradlew test` individualmente**, não só o
  último da branch — é a condição de o `git bisect` achar a regressão em
  qualquer ponto. O commit só se faz depois de compilar; a suíte inteira roda
  antes do push (seção 7).
- **A entidade, o schema e o código que os usam vão no mesmo PR**, em commits
  próprios e ordenados — não há migração, então "schema" é a anotação da
  entidade.
- **Documentação da mudança vai no mesmo PR, em commit próprio** — descrever a
  mudança num PR seguinte é documentação que nasce defasada.
- **Mudança de comportamento vem com o teste que a cobre** no mesmo PR — a
  tabela da seção 14 de [`TESTES.md`](../qualidade/TESTES.md#14-testes-exigidos-pelos-outros-documentos)
  é o inventário.
- **Não se reescreve o histórico publicado**: `git push --force` não se usa (a
  proteção da `main` o bloqueia). Um erro se corrige com **um commit novo**; o
  commit errado que **ainda não foi empurrado** é refeito com `git reset
  --soft HEAD~1`.
- **`-Werror` impõe uma ordem**: como o legado **não compila** com
  `-Xlint:all -Werror` (aviso do Lombok em `Consumer`), o commit que liga os
  warnings como erro vem **depois** dos que limpam o código — ver
  [seção 11](#11-plano-de-commits-da-refatoração).

---

## 7. Verificações Antes do Push

A disciplina desta página é conferida por **comandos que qualquer pessoa
roda**, e o CI do fork repete os mesmos
([`CICD.md`](../qualidade/CICD.md#1-pipeline-de-ci)). O fluxo é **manual e em
etapas separadas**:

```text
1. git checkout -b refactor/estrutura-em-camadas      # a branch vem antes de qualquer arquivo

2. # escreva a mensagem num arquivo (ex.: .commit-msg) e revise
   git add <arquivos>
   git commit -F .commit-msg

3. # verificação completa (abaixo), depois
   git push -u origin HEAD

4. # escreva o PR num arquivo (ex.: .pr-msg): título na 1ª linha, corpo no resto
   gh pr create --base main --title "$(head -n 1 .pr-msg)" --body "$(tail -n +3 .pr-msg)"
```

**A mensagem é escrita em arquivo, não inline** (`-m`, `--title` digitado): o
arquivo existe para ser **revisado** antes de qualquer ação — escrito, relido,
só então usado. Esses rascunhos são do clone de quem contribui: **não entram
no commit** (ex.: no `.git/info/exclude`).

### O que se confere antes do push

| Verificação | Comando |
|---|---|
| Compila (código e testes), com **JDK 17** | `./gradlew compileJava compileTestJava` |
| Javadoc válido (o `build` do Gradle não o executa sozinho) | `./gradlew javadoc` |
| Suíte inteira | `./gradlew test` |
| Formatação (`.editorconfig`) | `editorconfig-checker -config .ecrc` |
| Markdown, se mudou documentação | `markdownlint-cli2 "**/*.md"` |
| Workflows, se mudou `.github/` | `actionlint -config-file .github/actionlint.yaml .github/workflows/*.yml` |

**Antes do commit**, olhar o que vai nele:

- sem arquivo que nunca se versiona (`.env`, `*.pem`, `*.key`, `*.jks`,
  `application-local.yml`, `build/`, `.gradle/`, `logs/`) e sem arquivo acima
  de 5 MB
- sem marcador de conflito (`<<<<<<<`, `>>>>>>>`, a linha exata `=======`)
- sem código de debug em `.java`: `System.out`, `System.err`, `printStackTrace`,
  `TODO` de remoção
- sem segredo no diff

**Na mensagem**, conferir: formato `tipo(escopo): descrição`, escopo da lista
fechada (seção 2), até 72 caracteres, sem ponto final, descrição no infinitivo,
`!` coerente com `BREAKING CHANGE:` e formato de `Refs:`.

### Regras e limites

- **O CI é o gate; esta verificação é o subconjunto rápido dele.** O que
  protege a `main` do fork é o check `CI` e a proteção de branch
  ([`CICD.md`](../qualidade/CICD.md#2-gate-de-merge)); rodar os comandos antes
  evita gastar uma execução em algo que se pegaria em segundos.
- **O que a mensagem tem de verificável, o revisor confere.** Duas coisas
  nenhuma regra decide: se a descrição **diz o que mudou** (`fix(cartoes):
  corrigir bug` passa) e se o texto está **em português**. O infinitivo é uma
  **heurística** — a primeira palavra termina em `ar`, `er`, `ir`, `or` ou
  `ôr` — e tem falsos positivos (`valor`, `cor` passam).
- **Bypass não é rotina.** `--no-verify` e `git push --force` não se usam; pular
  uma verificação é decisão registrada no PR.
- **A lista de escopos é uma só**: a tabela da seção 2. **Escopo novo entra nela,
  no mesmo PR.**
- **Conferir segredo à mão é só o óbvio**: não pega uma senha em texto comum — a
  regra de fundo é de
  [`SEGURANCA.md`](../qualidade/SEGURANCA.md#4-segredos--regra-curta).
- **O histórico é linear: atualiza-se a branch com `git rebase`, não com *merge*.**
  A mensagem padrão do `git merge` não segue o formato. Um `git revert` ou
  `git cherry-pick` gera a mensagem do Git: **reescreva-a** no formato.

---

## 8. Pull Requests

O PR é a unidade de revisão; a branch é a unidade de trabalho. Aqui o PR é
também **a entrega do desafio**: quem o lê chega sem nenhum contexto — é o que
será lido antes do primeiro diff.

**O PR sai de um arquivo** (ex.: `.pr-msg`, título na primeira linha e corpo
no resto), revisado antes de abrir, e segue o template abaixo. O revisor
confere o template; nada o valida automaticamente.

**Título** (primeira linha do arquivo): segue o formato de commit —
`tipo(escopo): descrição` (até 72 caracteres, mesmas regras da seção 1). Em
*squash*, o título **é** a mensagem do histórico.

**Corpo:** quatro seções obrigatórias e **duas condicionais**, nesta ordem:

| Seção | A pergunta que responde | Exigência |
|---|---|---|
| `## Motivação` | Por que este PR existe? — o problema, não a solução | **sempre**; prosa, não lista |
| `## Mudanças` | O que passou a ser verdade? | **sempre**; ao menos um item de lista |
| `## Contrato da API` | O que mudou para quem consome? | **condicional** (aqui: quase sempre); se presente, ao menos um item |
| `## Testes` | Qual é a prova? | **sempre**; ao menos um item de **checklist** (`- [x]`) |
| `## Fora de escopo e riscos` | O que **não** foi feito, e por quê? | **condicional** (aqui: sempre); se presente, ao menos um item |
| `## Commits` | De onde vem cada parte? | **sempre**; ao menos um item |

- **Motivação** é prosa, não lista: o problema concreto, o contexto em que
  aparece e a decisão que guiou a mudança, com o motivo. Solução descrita sem
  problema não é motivação.
- **Mudanças** é lista no infinitivo, com itens verificáveis no diff —
  inclui documentação e testes, porque também são mudança.
- **Contrato da API** é o **mapa legado → novo** (rotas, verbos, formato de
  dinheiro) e as mudanças quebradoras, em lista de `campo: valor` — não
  tabela ([seção 10](#10-mensagens-em-markdown--leitura-em-qualquer-dispositivo)).
- **Testes** é **evidência, não afirmação**: comando executado e resultado
  (`./gradlew build`, com o JDK 17), contagem de testes, cada commit testado
  isoladamente. Que o checklist traga prova é trabalho da revisão. Checkbox marcado sem prova é documentação decorativa.
- **Fora de escopo e riscos** é a tabela de
  [`SEGURANCA.md`](../qualidade/SEGURANCA.md#9-riscos-conhecidos-e-fora-de-escopo)
  em lista: autenticação, *rate limiting*, idempotência de `POST`, HTTPS,
  versão desatualizada do Spring Boot, a limitação do H2 frente a um banco real.
  **Declarar o risco mostra que ele foi identificado; omiti-lo é o que a
  revisão deveria penalizar.**
- **Commits** lista hash abreviado e mensagem de cada commit
  (`git log main..HEAD --oneline`).
- **`Refs: #NNN`** no fim do corpo, se houver issue, no mesmo formato da seção 5.

Exemplo de descrição de PR (valores **ilustrativos**):

```text
refactor(api): reestruturar em camadas e corrigir os defeitos do legado

## Motivação

O legado concentrava regra, persistência e contrato num único controller,
e os defeitos não eram de estilo: dois débitos simultâneos no mesmo
cartão se sobrescreviam, o PUT aceitava saldo no corpo, e cartão
inexistente respondia 500 por NullPointerException. A lista devolvia
50.000 consumidores de uma vez, com todos os campos.

A refatoração separa domínio, aplicação, infraestrutura e API, e põe a
garantia de correção do saldo no banco (lock pessimista), não na JVM.

## Mudanças

- Extrair Dinheiro (BigDecimal, escala 6) e trocar double por decimal
- Registrar compra e crédito em casos de uso transacionais
- Paginar a listagem, projetando só id e nome
- Traduzir erros para application/problem+json, com código CPT-…
- Substituir mavenLocal e fixar o JDK 17, com lock file

## Contrato da API

- GET /consumer/consumerList: GET /api/v1/consumidores, paginado
- GET /consumer/buy: POST /api/v1/compras
- GET /consumer/setcardbalance: POST /api/v1/creditos
- dinheiro: número JSON para string decimal ("29.997000")

## Testes

- [x] `./gradlew build` (JDK 17, -Werror): 0 falhas, 0 avisos
- [x] 58 testes (todos novos); concorrência: 20 compras, saldo exato
- [x] Cada commit testado isoladamente antes do seguinte
- [x] O teste de concorrência falha quando o lock é removido

## Fora de escopo e riscos

- sem autenticação nem rate limiting (exigiriam dependência nova)
- POST de compra não é idempotente: retentar após timeout duplica o débito
- H2 em memória: o lock pode se comportar diferente em um banco real

## Commits

- `a1b2c3d` docs(padronizacao): adaptar a suíte de padronização
- `b2c3d4e` build(build): fixar o JDK 17 e remover mavenLocal
- `c3d4e5f` refactor(dominio): extrair Dinheiro, Resultado e erros
```

**Gate** (CI do fork na etapa 1 + isto, localmente):

- **Check `CI` verde** e o `./gradlew build` completo com o JDK 17, no commit
  exato entregue, **registrado na seção Testes**
- **Checklist de conformidade** preenchido
- **Documento vivo alterado junto da mudança que o exigiu**
  ([`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md#este-documento-é-vivo))

---

## 9. Changelog

**Aqui não há release nem ferramenta de changelog** (seria dependência de
build): o changelog é **um comando** sobre o histórico, que só presta se as
mensagens forem boas.

```bash
# mudanças percebidas por quem consome: funcionalidade, correção e desempenho
git log --pretty='- %s (%h)' --extended-regexp --grep='^(feat|fix|perf)(\(|!)'

# mudanças quebradoras, em destaque (o "!" no título e o rodapé andam juntos)
git log --pretty='- %s (%h)' --extended-regexp --grep='^[a-z]+\([a-z-]+\)!:'
```

| Tipo | O que vira no changelog |
|---|---|
| `feat` | Funcionalidade nova |
| `fix` | Correção |
| `perf` | Melhoria de desempenho |
| `!` no título (`BREAKING CHANGE`) | Mudança quebradora, em destaque |
| `docs`, `test`, `refactor`, `style`, `ci`, `build`, `chore` | Não entram: histórico técnico |

**Consequência:** mensagem vaga vira entrada inútil. A qualidade da lista é a
qualidade do histórico — e um changelog escrito à mão em paralelo **diverge**
dos commits, virando a segunda fonte de verdade do que mudou.

---

## 10. Mensagens em Markdown — Leitura em Qualquer Dispositivo

Mensagem de commit e corpo de PR são documentos lidos em **pelo menos três
lugares**: terminal monoespaçado (`git log`), web desktop e aplicativo mobile —
este com viewport estreito e renderização restrita. **O formato é definido pelo
cliente mais restrito**: o que se lê num celular de 360 px e num terminal de
80 colunas lê-se bem no desktop; o inverso não vale.

> **Estas regras não são impostas por ferramenta** — renderização não é
> coisa que regex decida (a verificação de Markdown cobre a **documentação**,
> não a mensagem de commit). Valem como item de revisão do PR, onde a mensagem
> aparece já renderizada.

**Regras:**

- **Quebra manual a ~72 colunas em todo o corpo**, não só na primeira linha.
- **Lista antes de parágrafo denso** — uma ideia por item; aninhamento no
  máximo 2 níveis.
- **Nada de tabela markdown na mensagem** — em tela estreita vira rolagem
  horizontal. Informação tabular vira lista de `campo: valor`. (Na
  documentação da suíte a tabela é bem-vinda; o alvo desta regra é a
  mensagem.)
- **HTML embutido não** (`<br>`, `<details>`, `<img>`).
- **Headings só `##` e `###`, e só no corpo do PR** — o corpo de commit é curto
  por definição e não precisa de heading.
- **Caminho é texto, não link, em mensagem de commit** (`API.md`,
  `build.gradle` entre crases); link `[texto](url)` só no corpo de PR.
- **Código em bloco com linguagem declarada e linha curta** (`java`, `bash`);
  comando extenso quebra com continuação (`\`).

```text
# ERRADO — tabela e HTML: o celular rola, o terminal não entende
| Campo | Valor | Observação |
|-------|-------|------------|
| taxa  | 42    | ver issue  |

# CORRETO — a mesma informação cabe em 360 px e num terminal de 80
Resultado dos testes (7 commits, 0 falhas):

- `./gradlew test`: 58 testes, 0 falhas
- teste manual: compra de R$ 33,33 gravou 29.997000
```

---

## 11. Plano de Commits da Refatoração

*(Seção nova.)* A refatoração é grande; sem um plano, ela vira um commit único
de "refatorar tudo". A ordem abaixo cumpre a [seção 6](#6-commits-atômicos):
**cada commit compila e passa `./gradlew test`**, e o que quebra o legado
(`-Werror`) entra **depois** de o legado sair.

1. `docs(padronizacao): adaptar a suíte de padronização para Java 17`
2. `build(build): fixar o JDK 17, o lock file e o springdoc em propriedade`
   — **sem** `-Werror` ainda (o legado não compilaria); remove `mavenLocal()`
3. `build(build): adicionar o .editorconfig do projeto`
4. `test(testes): adicionar fábricas, fakes e teste de arquitetura`
5. `refactor(dominio): extrair Dinheiro, Resultado e erros tipados`
6. `feat(cartoes): registrar compra e crédito em casos de uso`
   — com fakes; **a regra do enunciado vira teste** (desconto 10%, acréscimo 35%)
7. `feat(banco): mapear entidades e adaptadores com lock pessimista`
   — schema em português, `NUMERIC(19,6)`, `CHECK`, auditoria
8. `feat(api): expor o contrato versionado e o corpo de erro padronizado`
   — aditivo: as rotas novas convivem com as do legado, então **não** leva `!`
9. `perf(consumidores): paginar a listagem e projetar só id e nome`
10. `feat(observabilidade): adicionar porta de log e health check próprio`
11. `feat(seguranca): enviar cabeçalhos de segurança em /api/*`
12. `refactor(api)!: remover o controller e as entidades do legado`
    — só aqui o legado sai, e por isso é aqui que entram o `!` **e** o
    `BREAKING CHANGE:` no corpo (rotas, verbos, dinheiro em string)
13. `test(cartoes): adicionar teste de concorrência da compra`
    — verificado: **falha** sem o lock
14. `build(build): tratar warnings como erros e validar o javadoc`
    — **último** código: agora o código está limpo e `-Werror` passa
15. `docs(docs): atualizar o README com JDK 17 e os comandos`

> **Este plano é uma proposta, não um contrato.** A ordem pode mudar — a
> **regra** não: nenhum commit deixa o projeto sem compilar, e nenhum mistura
> refatoração com funcionalidade.

---

## Anti-Padrões

| Anti-padrão | Correto |
|---|---|
| `git commit -m "..."` com texto não revisado | escrever a mensagem em arquivo e `git commit -F` (seção 7) |
| PR aberto com título e corpo digitados no comando | título e corpo vêm de um arquivo revisado (seção 8) |
| `git push --force`, `--no-verify`, `--amend` em commit já publicado | erro se corrige com commit novo (seções 6 e 7) |
| Descrição em inglês, particípio ou 3ª pessoa | Português no infinitivo (seção 1) |
| Sem escopo, ou escopo fora da tabela (`compras`) | Escopo da tabela; área nova entra no mesmo PR (seção 2) |
| Tipo inventado (`update`, `hotfix`, `wip`) | Os dez tipos da tabela (seção 1) |
| `fix(cartoes): corrigir bug` | Dizer o que mudou — nenhuma regra automática pega isto (seção 7) |
| Primeira linha > 72 caracteres ou com ponto final | Até 72, sem ponto (seção 1) |
| `BREAKING CHANGE:` no corpo sem `!` no título | Os dois juntos (seção 1) |
| Refatoração e funcionalidade no mesmo commit | Commits atômicos, cada um verde (seção 6) |
| Commit que quebra a compilação "para consertar no próximo" | Cada commit compila e passa (seção 6) |
| `-Werror` ligado antes de o código estar limpo | `-Werror` depois de limpar o legado (seção 11) |
| Entidade num PR, código que a usa em outro | Mesmo PR, commits ordenados (seção 6) |
| `./gradlew` com o JDK errado, culpando o projeto | `JAVA_HOME` no JDK 17 ([`BUILD.md`](../qualidade/BUILD.md#1-build-autocontido-no-repositório)) |
| Arquivos de rascunho da mensagem indo para o commit | excluir no clone (ex.: `.git/info/exclude`) (seção 7) |
| Pular uma verificação sem avisar | Declarar no PR (seção 7) |
| Escopo novo usado sem entrar na tabela | Tabela atualizada no mesmo PR (seção 2) |
| Título de PR fora do formato de commit | `tipo(escopo): descrição` (seção 8) |
| Corpo de PR que só resume o diff | Motivação em prosa, com o problema (seção 8) |
| Itens de Testes marcados sem evidência | Comando, contagem, observação (seção 8) |
| PR que omite o que ficou de fora | `## Fora de escopo e riscos` (seção 8) |
| Mudança de contrato sem `!` e `BREAKING CHANGE` | Os dois no commit que a introduz (seção 5) |
| Changelog escrito à mão | `git log` filtrado por tipo (seção 9) |
| Tabela markdown ou HTML na mensagem | Lista de `campo: valor` (seção 10) |
| Link `[texto](url)` em mensagem de commit | Caminho como texto; link só no PR (seção 10) |

---

## Checklist

Antes de commitar ou abrir o PR:

- [ ] A mensagem foi escrita em arquivo, revisada, e o commit saiu com `git commit -F`?
- [ ] Tipo válido e escopo presente, da tabela da seção 2?
- [ ] Descrição em português, no infinitivo, minúscula, sem ponto final, ≤ 72 caracteres?
- [ ] A descrição diz **o que mudou** (coisa que nenhuma regra automática confere)?
- [ ] O commit é atômico — um motivo só — e compila e passa `./gradlew test` sozinho?
- [ ] Entidade, código, documentação e teste da mudança estão no mesmo PR?
- [ ] A branch segue `tipo/descricao-kebab-case`, **não** é a `main` e está atualizada com ela?
- [ ] Mudança de contrato leva `!` no título **e** `BREAKING CHANGE:` com o que o consumidor deve fazer?
- [ ] As verificações da seção 7 passaram antes do push (e, se alguma foi pulada, isso está declarado no PR)?
- [ ] O PR tem Motivação, Mudanças, Testes e Commits — e, quando cabe, Contrato da API e **Fora de escopo e riscos**?
- [ ] Itens de teste marcados trazem prova (comando verde com o JDK 17, contagem), não só "ok"?
- [ ] Corpo com quebra a ~72 colunas, listas em vez de tabelas e sem HTML?
- [ ] Escopo novo entrou na tabela da seção 2?

---

## Decisões Deste Documento

O consolidado está em [`DECISOES.md`](../fundamentos/DECISOES.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| Conventional Commits em português, escopos deste projeto | histórico livre | changelog útil e histórico legível; é um fork, o histórico é novo |
| Mensagem **em arquivo**, revisada antes de usar; sem `--amend` nem `--force` em histórico publicado | `-m` livre | revisar antes de agir; histórico não reescrito |
| Verificação local com os comandos do Gradle e do git | ferramenta própria versionada no repositório | não é do projeto: uma ferramenta de apoio num PR de desafio é ruído; o CI repete os mesmos comandos |
| `!` no título **e** `BREAKING CHANGE:` no corpo, consistentes | só o rodapé | as ferramentas de changelog procuram o `!` |
| Validação do infinitivo por heurística | sem validar; dicionário | regex não resolve gramática; o falso positivo é barato, e a revisão fecha o resto |
| Título contado em **caracteres**, não bytes | bytes | `ã` e `ç` ocupam 2 bytes e reprovariam títulos de 70 caracteres |
| Sem `release/`, com escopo `ci` | incluir release | não há release; o pipeline existe |
| Changelog por `git log` | ferramenta de changelog | ferramenta seria dependência de build |

---

**[Observabilidade ←](OBSERVABILIDADE.md)**
