[← Índice](../../PADRONIZACAO_INDEX.md) · [← BANCO.md](BANCO.md)

# Build — Java 17 / Gradle 7.6 / Spring Boot 2.7

> **Escopo**: o build de **Java 17 + Gradle**. A tese é que o build é
> autocontido no repositório, reprodutível e falha em warning. O ferramental é
> o Gradle Wrapper, o artefato é um *fat jar* que precisa de JRE, e há uma
> restrição de fundo: **o enunciado proíbe dependência nova**, o que também
> exclui os plugins de qualidade do Gradle que baixam artefatos (seção 9). Ver
> [Premissas do Build](#premissas-do-build).
>
> **Nota sobre exemplos**: os comandos e trechos de `build.gradle` abaixo são
> ilustrativos e **não foram executados** — a regra normativa é o texto
> conceitual. O que **foi verificado** está marcado como *verificado*: um JDK
> bem mais novo que o 17 (testado com o 27) **não** executa este build, e
> `./gradlew compileJava` com `JAVA_HOME` no JDK 17 **compila** o legado.
>
> **Documentos relacionados**: [`CICD.md`](CICD.md) descreve o pipeline que
> roda estes comandos, e [`COMMITS.md`](../runtime/COMMITS.md) o fluxo de
> entrega.

Define a construção do sistema: JDK e Gradle fixados, versões de dependência
centralizadas e travadas, artefato único, versão rastreável e o que faz o
build falhar.

---

## Premissas do Build

| Tema | Regra neste projeto |
|---|---|
| Comando único | **`./gradlew`** (wrapper versionado no repositório) |
| Toolchain fixada | Gradle **7.6** no `gradle-wrapper.properties` + **JDK 17** exigido (a JVM do Gradle precisa ser **≤ 19**) |
| Fixar o alvo | `options.release = 17` |
| Artefato de produção | **`bootJar`** (*fat jar*): exige **JRE 17** no servidor |
| Alvos de plataforma | **um só JAR**, portável |
| Lock file | **`gradle.lockfile`** (*dependency locking*, nativo) |
| Fonte única de versões | **BOM do Spring Boot** + `gradle.properties` (versão do springdoc) |
| Warnings como erro | `-Xlint:all -Werror` (e `-Xdoclint:missing` só no código de produção) |
| Supressão pontual | `@SuppressWarnings("…")` na linha/método |
| Análise estática / formatação | **`.editorconfig`** + javac lint + **testes de arquitetura** (Checkstyle/PMD/Spotless exigiriam artefatos novos) |
| Repositório local de dependências | `mavenLocal()` **sai** do build (seção 4) |
| Hash de commit no binário | **não adotado** (seção 3) |

---

## 1. Build Autocontido no Repositório

O projeto é construído com **Gradle**, pelo **wrapper** (`gradlew`) que está
no repositório. O build não depende de nada que não esteja declarado **no
repositório** além do **JDK 17** da máquina: o Gradle em si é baixado na
versão fixada, e as dependências vêm do Maven Central na versão travada.

| Etapa | Comando |
|---|---|
| Compilar (código e testes) | `./gradlew compileJava compileTestJava` |
| Construir **e** testar **e** verificar | `./gradlew build` |
| Só testar | `./gradlew test` |
| Documentação (Javadoc) | `./gradlew javadoc` |
| Executar localmente | `./gradlew bootRun` |
| Gerar o artefato | `./gradlew bootJar` → `build/libs/consumer-pat-<versão>.jar` |
| Rodar o artefato | `java -jar build/libs/consumer-pat-<versão>.jar` |

### O JDK é pré-requisito, não detalhe

> **Verificado:** com o JDK 27 como padrão da máquina, `./gradlew --version`
> funciona, mas `./gradlew compileJava` **falha** (`ExceptionInInitializerError`),
> e um *toolchain* do Gradle apontando para o 17 **não resolve** — o script de
> build falha antes (`Unsupported class file major version 71`). **A JVM que
> executa o Gradle 7.6 precisa ser a 17 (ou, no máximo, a 19).** O *toolchain*
> do Gradle escolhe o JDK que **compila**, não o que **roda o Gradle**.

Por isso:

- **O README declara o requisito**: JDK **17** (não "11 ou 17" — ver
  [seção 6](#6-toolchain-fixada--mesmo-ambiente-local-e-ci)) e o comando para
  apontá-lo:

  ```bash
  export JAVA_HOME=/usr/lib/jvm/java-17-openjdk     # o caminho varia por máquina
  ./gradlew build
  ```

- **O README lista o sintoma**: "`ExceptionInInitializerError` ou
  `Unsupported class file major version` ⇒ `java -version` não é 17". A
  mensagem do Gradle **não** diz isso — quem não souber culpa o projeto.
- **`org.gradle.java.home` em `gradle.properties` não é commitado**: é um
  caminho de máquina; fixá-lo no repositório quebra todas as outras.

**Regras:**

- **O build é autocontido no repositório**: ferramenta nova é fixada em
  arquivo versionado ([seções 6](#6-toolchain-fixada--mesmo-ambiente-local-e-ci)
  e [10](#10-proveniência--de-onde-vêm-ferramentas-e-dependências)), nunca
  instalada à mão.
- **Uma eventual mudança de stack é decisão nova** em
  [`DECISOES.md`](../fundamentos/DECISOES.md) — o build da stack nova nasce
  seguindo estas mesmas regras.
- **Frontends não são parte deste build.**

**Por quê:** build que depende do que está instalado na máquina de quem roda
é build não reprodutível — e aqui isso não é abstração: **o mesmo repositório
compila em uma máquina e falha em outra só pelo JDK padrão**.

---

## 2. Artefato: `bootJar`

Em Java não existe binário que dispense o runtime: o artefato é um **JAR
executável** (*fat jar*) gerado pelo plugin do Spring
Boot, que carrega as dependências **mas não a JVM**.

```bash
./gradlew bootJar            # build/libs/consumer-pat-0.0.1.jar
java -jar build/libs/consumer-pat-0.0.1.jar
```

**Regras:**

1. **O artefato de implantação é o `bootJar`, e só ele.** O `build` também
   gera `consumer-pat-<versão>-plain.jar` (sem as dependências): **não é
   implantável**. `bootRun` e o JAR rodado a partir da IDE são ferramentas de
   desenvolvimento local, nunca alvo de implantação.
2. **O JRE exigido é parte do contrato de implantação**: **JRE 17**, declarado
   no README. Não viaja com o JAR; atualização de segurança do runtime é
   responsabilidade de quem opera o servidor (limite registrado em
   [`PERFORMANCE.md`](PERFORMANCE.md#6-binário-executável--não-há-self-contained)).
3. **Um JAR só, para qualquer sistema operacional.** Não há alvo por
   plataforma.
4. **Empacotar o JRE** (`jlink`, `jpackage`) **ou imagem de contêiner** está
   **fora de escopo** do desafio.

**Por quê:** o que roda no servidor deve ser exatamente o que foi construído
e testado — o JAR único, com dependências travadas (seção 7), é o que garante
isso, sem o runtime embutido.

---

## 3. Versão de Build Rastreável

**Fonte única da versão:** a linha `version = '0.0.1'` do `build.gradle`.
Dali a versão chega ao nome do JAR, ao `MANIFEST.MF`
(`Implementation-Version`, escrito pelo plugin do Spring Boot) e ao
**banner** de inicialização:

```text
# src/main/resources/banner.txt  (já existe)
... :: consumer-pat ${application.version} ::
```

**Regras:**

- **Um lugar só.** A versão não é digitada no banner, no `README` nem em
  constante de código — o banner a **lê**. Duas fontes são duas versões, e
  uma delas mente.
- **Padrão `X.Y.Z` obrigatório** (semver): `1.2` ou `1` não são versões.
- **`${application.version}` só existe quando se executa o JAR** (vem do
  `MANIFEST.MF`); em `bootRun`/IDE aparece vazio. É esperado, não defeito.
- **Hash de commit: não adotado.** Exigiria um trecho `exec` no
  `build.gradle` (ou plugin externo) e não há pipeline de release.
  **Se** o projeto sair do desafio, adota-se com `springBoot { buildInfo() }`
  mais o hash — registrar em [`DECISOES.md`](../fundamentos/DECISOES.md).
- **A tag de release deriva da mesma fonte** quando existir pipeline.

**Por quê:** "qual versão está em produção?" é a pergunta de primeira linha de
qualquer incidente. Uma fonte única é o que a torna respondível.

---

## 4. Checkout Limpo Constrói — e `mavenLocal()` Sai

O build **passa em um checkout limpo**: clonado do zero, sem cache prévio e
sem nada que só exista na máquina de quem escreveu. Para o Gradle:

```bash
git clone <repositório> && cd consumer-pat
export JAVA_HOME=<JDK 17>
./gradlew build                     # só com acesso ao Maven Central na primeira vez
./gradlew build --offline           # depois, funciona sem rede
```

**O legado viola esta regra.** O `build.gradle` declara:

```groovy
repositories {
    mavenLocal()        // ← o build depende do que estiver em ~/.m2 NA MÁQUINA
    mavenCentral()
}
```

`mavenLocal()` faz o build usar **o que estiver no repositório Maven local**
de quem roda — uma versão instalada à mão, de origem desconhecida, que o
repositório não declara. O mesmo commit constrói com artefatos diferentes em
duas máquinas, e **nada no diff mostra isso**. Sai; fica só
`mavenCentral()`.

**Regras:**

1. **Nenhum repositório local ou de máquina** (`mavenLocal()`, caminho
   `file://` absoluto, repositório de rede interna não declarado).
2. **Nenhum script de build lê arquivo fora do repositório** — nem para
   "conferir", nem para gerar fixture.
3. **Nenhuma dependência chega por download fora do gerenciador** — o que não
   está no `build.gradle` e resolvido pelo lock file (seção 7) não entra.
4. **Nenhuma citação de código de outra origem** em comentário, documento ou
   commit como origem de código reutilizado — o código é do projeto.

**Por quê:** o checkout limpo que constrói é a prova auditável de que o build
não depende de estado oculto.

---

## 5. Warnings Tratados Como Erros

O build **falha** com warning de compilador, de lint ou de Javadoc. Código
limpo é pré-requisito para build.

```groovy
tasks.withType(JavaCompile).configureEach {            // código E testes
    options.encoding = 'UTF-8'
    options.release  = 17
    options.compilerArgs += ['-Xlint:all', '-Werror']
}

tasks.named('compileJava') {                           // só o código de produção
    options.compilerArgs += ['-Xdoclint:missing']      // membro público sem Javadoc = erro
}

tasks.named('javadoc') {
    options.addBooleanOption('Xwerror', true)          // HTML inválido, @param incoerente = erro
}

tasks.named('check') { dependsOn 'javadoc' }           // `build` passa a falhar por doc inválida
```

**O que é warning aqui:**

- **Aviso do `javac`** (`-Xlint:all`): `unchecked`, `rawtypes`, `deprecation`,
  `serial`, `fallthrough`, `try`, `overrides`…
- **Documentação ausente** em membro público (`-Xdoclint:missing`) —
  equivalente do `CS1591`; **só em `compileJava`**, não nos testes (um método
  de teste não tem Javadoc a exigir).
- **Javadoc inválido** (`-Xwerror` na tarefa `javadoc`) — e a tarefa **entra
  no `check`**: por padrão o `build` do Gradle **não** executa `javadoc`, e
  sem essa linha "doc inválida" não quebraria nada.

> **O legado já falha nesta regra.** A compilação de `Consumer` emite
> *"Not generating hashCode: One of equals or hashCode exists"* (Lombok
> `@Data` junto de `equals` manual). Com `-Werror`, o legado **não compila**
> até esse defeito ser corrigido — o que é o comportamento desejado.

### Supressão: pontual e justificada

| | Correto | ERRADO |
|---|---|---|
| Escopo | `@SuppressWarnings("unchecked")` **no método ou na linha** | `-Xlint:-unchecked` global no `build.gradle` |
| Motivo | comentário no ponto explicando **por que** aquele caso é aceito | silêncio |
| Prazo | enquanto o caso vale; issue quando é débito | supressão permanente que mascara warnings novos da mesma categoria |

> **Limite honesto do `javac`:** ele não tem supressão por categoria *por arquivo* fora de `@SuppressWarnings`. Se
> `-Xlint:all` produzir ruído **comprovado** de um processador de anotação
> (Lombok, Spring) — a categoria `processing` é a suspeita —, a **única**
> supressão global admitida é **excluir essa categoria**, **nominalmente**, no
> próprio `build.gradle`, **com o comentário que prova o ruído**. Excluir
> categoria por incômodo é a supressão global que esta seção proíbe.
> *(Ponto a validar ao aplicar: ainda não foi executado `-Xlint:all -Werror`
> sobre o código novo.)*

**Por quê:** warning é o compilador pagando para te contar algo. Build que
passa com warning treinou todo mundo a ignorar o compilador — e, no próximo
warning real (cast não verificado num `Resultado<T, E>`, `switch` sem caso),
ninguém olha. Tratar como erro desde o primeiro dia mantém o sinal legível;
adotar depois custa semanas de limpeza.

---

## 6. Toolchain Fixada — Mesmo Ambiente Local e CI

A versão da toolchain é fixada **no repositório**, nunca deixada ao que a
máquina tem instalado:

- **Gradle 7.6**, em `gradle/wrapper/gradle-wrapper.properties`. Recomendado
  acrescentar `distributionSha256Sum=<valor oficial>`: sem ele, o wrapper
  baixa **qualquer** arquivo servido naquela URL e o executa.
- **Plugins com versão exata** no bloco `plugins {}`: Spring Boot `2.7.8`,
  `io.spring.dependency-management` `1.0.15.RELEASE`. Plugin sem versão ou com
  intervalo não entra.
- **JDK 17**, declarado no README (seção 1) e imposto no compilador por
  `options.release = 17`.

### Substituir o `assert` quebrado do legado

O legado tenta validar o JDK assim:

```groovy
assert System.properties["java.specification.version"] == "11" || "17"
```

**Esta linha nunca falha.** Em Groovy, `A == "11" || "17"` avalia
`(A == "11") || "17"` — e a string `"17"` é *truthy*. A "verificação" aprova
qualquer JDK. Sai. Quando a JVM do Gradle é inadequada, o Gradle falha antes
de qualquer linha do script (verificado — seção 1), e o **README** é a
barreira real; um *guard* no script só protegeria as JVMs em que o script
chega a compilar, e a mensagem que ele daria ("use o JDK 17") é exatamente a
do README.

**Regras:**

- **Alvo único: Java 17.** Sem multi-versão, sem `#if` de versão de runtime,
  sem *polyfill*.
- **A exigência de JDK 17 muda o README do enunciado**, que diz "JDK 11 ou
  17". Com `record`, `sealed` e `instanceof` com *pattern*, o 11 **deixa de
  funcionar** — e quem avaliar com JDK 11 não compila. É decisão consciente,
  registrada em [`DECISOES.md`](../fundamentos/DECISOES.md), e o README é
  atualizado **no mesmo PR**.
- **Subir de versão de JDK/Gradle é decisão registrada**, nunca o JDK da
  máquina mudando e o repositório acompanhando por acidente.
- **O que passa local passa em qualquer pipeline futuro, e vice-versa.**

**Por quê:** a mesma classe de problema que um *pin* de toolchain evita é a
clássica de "na minha máquina passa" — e aqui ela é concreta: **uma máquina
com um JDK padrão mais novo (como o 27) não roda este build sem o
`JAVA_HOME`**.

---

## 7. Dependências: Fonte Única e Lock File

**Regras:**

1. **Versões em um único ponto.** As versões dos starters vêm do **BOM do
   Spring Boot** (plugin `io.spring.dependency-management`) — nunca
   duplicadas. A versão do **springdoc**, que o BOM não gerencia e hoje está
   repetida em duas linhas, vai para uma variável em `gradle.properties`:

   ```properties
   # gradle.properties
   springdocVersion=1.6.12
   ```

   ```groovy
   implementation "org.springdoc:springdoc-openapi-ui:${springdocVersion}"
   ```

2. **Lock file versionado.** O *dependency locking* do Gradle é nativo:

   ```groovy
   dependencyLocking { lockAllConfigurations() }
   ```

   ```bash
   ./gradlew dependencies --write-locks     # gera/atualiza gradle.lockfile — COMMITADO
   ```

   Com o lock, o mesmo commit resolve **as mesmas versões** em qualquer dia; sem
   ele, uma versão transitiva nova no Maven Central muda o build sem nenhum
   diff. Atualizar dependência é um commit que **altera o `gradle.lockfile`**
   — a revisão enxerga.
3. **Nenhuma versão curinga.** `1.+`, `latest.release` ou intervalo não
   entram.
4. **Dependência nova é proibida pelo enunciado** do desafio — regra mais forte
   que o "PR dedicado". Fora do desafio, vale o PR dedicado, com
   justificativa e sem telemetria
   ([`SEGURANCA.md`](SEGURANCA.md#1-sem-telemetria)).
5. **Subir a versão do Spring Boot/springdoc é decisão**, não "atualizar o
   que estiver disponível": a versão **2.7.8 está desatualizada** e é risco
   declarado em [`SEGURANCA.md`](SEGURANCA.md#9-riscos-conhecidos-e-fora-de-escopo)
   (item 9).
6. **O repositório de dependências é `mavenCentral()`** — e só ele (seção 4).

**Por quê:** dependência é código de terceiro executado no servidor, e build
determinístico é a condição de investigação: com lock file, "o que mudou?"
tem resposta no diff de um commit. Cadeia de suprimentos e reprodutibilidade
são o mesmo problema.

---

## 8. Comandos Padrão e Artefatos Descartáveis

Existe **uma** sequência de build, a mesma no notebook e em qualquer
pipeline futuro:

| Etapa | Comando |
|---|---|
| 1. Resolver dependências (a partir do lock) | implícito em `./gradlew build` |
| 2. Compilar, com warnings como erros | `./gradlew compileJava compileTestJava` |
| 3. Verificar documentação | parte do `check` (`javadoc`, seção 5) |
| 4. Testar — unitário, integração, contrato, concorrência, arquitetura | `./gradlew test` — **uma** execução; as categorias são distinguidas pelo pacote/nome da classe ([`TESTES.md`](TESTES.md#3-tipos-de-teste)) |
| 5. Gerar o artefato | `./gradlew bootJar` |

`./gradlew build` executa as etapas 1 a 4 (e gera o JAR); **é o comando que
vale antes do push**.

- **O pipeline executa exatamente estes comandos** — não existe "modo CI" com
  flags especiais. Se algum passo é necessário, está documentado aqui e roda
  igualmente no desenvolvimento.
- **Testes rodam sem rede, sem serviço externo e sem credencial real** após a
  primeira resolução ([`TESTES.md`](TESTES.md#9-execução-local)); `--offline`
  prova isso.
- **Saída de build nunca é commitada.** `build/`, `.gradle/` e `bin/` já
  estão no `.gitignore`. **`gradle/wrapper/gradle-wrapper.jar` é a exceção
  versionada** (o `.gitignore` já a preserva) — é o que torna o `gradlew`
  autocontido.
- **`gradle.lockfile` e `gradle.properties` são versionados**; caminhos de
  máquina (`org.gradle.java.home`) ficam em `~/.gradle/gradle.properties`,
  **fora** do repositório.

**Por quê:** o custo de um build misterioso é que só uma máquina entende como
construir o projeto. Sequência única e documentada mantém o conhecimento de
build no mesmo lugar do resto da padronização.

---

## 9. Formatação e Análise Estática

**Formatação não se discute em review, e nada dela fica por conta do que o
editor decide sozinho.** Como não há plugin de formatação no build, a regra vive
em dois arquivos versionados e é conferida por uma terceira ferramenta, tudo
**sem dependência nova**:

- **`.editorconfig`** é a fonte da formatação de texto de **todo** arquivo:
  `charset = utf-8`, `end_of_line = lf`, `insert_final_newline`,
  `trim_trailing_whitespace`, `indent_style = space`, `indent_size = 4` (2 para
  yml, yaml, json e xml), `tab_width = 4` e **`max_line_length = 100`**. As
  exceções são declaradas e têm motivo: Markdown (dois espaços no fim da linha
  são quebra forçada, e tabelas e blocos de código não seguem múltiplos de 4), o
  banner, o `gradle.lockfile` (gerado) e o perfil do formatador. O arquivo traz
  também as propriedades `ij_*`, que o IntelliJ lê e que repetem o perfil abaixo.
- **`config/formatter/eclipse-java.xml`** é a fonte da formatação do **código
  Java**, o que o `.editorconfig` não expressa: quebra de linha, continuação de 8
  espaços, Javadoc com tags alinhadas, linhas em branco, chaves e uma instrução
  por linha. É o perfil do formatador do Eclipse, o motor da extensão
  `redhat.java` do VS Code, e o `.vscode/settings.json` faz a extensão usá-lo, em
  vez do padrão dela, e organizar os imports em ordem fixa (os demais, `javax`,
  `java` e os estáticos). Um id de ajuste errado é **ignorado em silêncio** pelo
  Eclipse, então todo id do perfil foi conferido contra o formatador, e o perfil
  é **idempotente**: formatar um arquivo já formatado não o altera.
- **`editorconfig-checker` confere o `.editorconfig` no CI e no pre-commit**,
  com **todas** as verificações ligadas, a indentação inclusive: largura de
  linha, indentação, espaço no fim da linha, fim de arquivo e quebra de linha.
- **`shellcheck` confere os scripts de `scripts/`**, com a configuração do
  `.shellcheckrc` versionado (dialeto `bash`, severidade `warning`), para o
  resultado não depender do que cada pessoa passa na linha de comando. Hoje ele
  roda **só na máquina de quem mexe no script**: nenhum workflow nem hook o
  chama, e levá-lo ao CI é um passo à parte.
- **Lacuna declarada:** o CI **não** confere o que só o perfil expressa, como a
  quebra de linha do Java, porque isso exigiria baixar o formatador. Quem usa o
  VS Code com as extensões recomendadas recebe isso pronto ao salvar; quem usa
  outro editor aplica o perfil ou o espelho `ij_*`. É uma limitação reconhecida,
  não uma omissão silenciosa, e a revisão cobre o que sobra.

**Por que não Checkstyle, PMD, Spotless ou JaCoCo**: o `checkstyle`, o `pmd`
e o `jacoco` **acompanham o Gradle** como plugins nativos, mas **baixam** as
ferramentas do Maven Central ao rodar; o Spotless é plugin externo. Todos
acrescentam artefatos ao build — e o enunciado proíbe dependência nova. São
tratados como dependência (e a restrição vale para os plugins de
**qualidade**, não para os plugins de **empacotamento** que o projeto já usa).
Se o projeto sair do desafio, entram — registrar em
[`DECISOES.md`](../fundamentos/DECISOES.md).

**A análise estática do projeto é feita por três coisas**:

1. **O `javac`**: `-Xlint:all`, `-Xdoclint:missing` e `-Werror` (seção 5).
2. **Os testes de arquitetura** — a forma, neste projeto, de "regra nova
   verificável vira regra de análise, não item de checklist humano":

   | Teste | Regra que torna mecânica |
   |---|---|
   | `RegraDeDependenciaTeste` | direção de dependência entre camadas ([`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md#a-regra-é-verificável-não-de-boa-vontade)) |
   | `CodigosDeErroTeste` | código de erro único e no formato ([`ERROS.md`](ERROS.md#8-código-de-erro-legível-e-verificado-por-teste)) |
   | `NomenclaturaDeSchemaTeste` | constraint nomeada e padronizada ([`BANCO.md`](BANCO.md#3-nomenclatura-de-banco)) |
   | `CoerenciaDeEscalaTeste` | escala da coluna == `Dinheiro.ESCALA` ([`VALORES.md`](../codigo/VALORES.md#6-dinheiro-e-percentuais)) |
   | `EntradaValidadaTeste` | todo `@RequestBody` tem `@Valid`; todo `String` de entrada tem `@Size(max)` ([`VALIDACAO.md`](VALIDACAO.md#4-ponto-único-na-api)) |
   | `EstadoCompartilhadoTeste` | sem `static` mutável, sem campo mutável em bean, sem construção de concorrência fora do inventário ([`CONCORRENCIA.md`](../runtime/CONCORRENCIA.md#4-sem-estado-global-mutável)) |

3. **A revisão**, para o que nenhum dos dois alcança.

**Por quê:** comentário de review sobre formatação treina o autor a ignorar o
resto do comentário; e convenção que só vive na cabeça de quem fundou o projeto
morre na primeira revisão feita por outra pessoa. Teste de arquitetura é
convenção com memória.

---

## 10. Proveniência — De Onde Vêm Ferramentas e Dependências

Todo código de terceiro que entra no build tem origem conhecida e versão
fixada:

- **Fontes oficiais, declaradas no repositório**: `mavenCentral()` para
  dependências; o **Gradle Plugin Portal** (padrão do Gradle) para os plugins
  `org.springframework.boot` e `io.spring.dependency-management`; e
  `services.gradle.org` para o próprio Gradle. Nenhum *mirror* pessoal,
  nenhuma fonte acrescentada em máquina particular.
- **Nenhum script baixado é executado como parte do build** — sem
  `curl | sh`, sem `apply from: 'https://…'`, sem baixar binário em tempo de
  execução do build.
- **`./gradlew build --scan` é proibido.** O *build scan* **envia dados do
  build a um serviço de terceiros** — telemetria de build, vedada pelo mesmo
  critério de [`SEGURANCA.md`](SEGURANCA.md#1-sem-telemetria). O Gradle não
  envia nada sem `--scan`; o risco está em alguém ligá-lo "para depurar".
- **Ferramentas são versionadas no repositório e instaladas pelo build**: o
  wrapper (e seu `distributionSha256Sum`), os plugins com versão exata, o
  lock file.
- **Toda dependência nova passa antes pelos critérios**: (1) é genuinamente
  necessária, ou o problema se resolve com a plataforma Java? (2) carrega
  telemetria ou chamada de rede embutida? — rejeitada de imediato se sim;
  (3) é mantida e conhecida? (4) licença compatível.

**Por quê:** build é a primeira porta de entrada da cadeia de suprimentos —
tudo que o build executa roda com permissão de ler o código e de publicar
artefato. Reduzir essa superfície ao mínimo declarado e versionado é o mesmo
racional do menor privilégio, aplicado a quem constrói.

---

## 11. Alterações Exatas no `build.gradle`

O que muda no arquivo atual para que ele cumpra este documento:

| # | Hoje (legado) | Passa a ser | Seção |
|---|---|---|---|
| 1 | `sourceCompatibility = 11` / `targetCompatibility = 11` | `options.release = 17` | 6 |
| 2 | `assert … == "11" \|\| "17"` (nunca falha) | removido; o README exige o JDK 17 | 6 |
| 3 | `mavenLocal()` | removido; só `mavenCentral()` | 4 |
| 4 | versão do springdoc repetida em duas linhas | `${springdocVersion}` de `gradle.properties` | 7 |
| 5 | — | `dependencyLocking { lockAllConfigurations() }` + `gradle.lockfile` commitado | 7 |
| 6 | só `options.encoding = 'UTF-8'` | + `-Xlint:all`, `-Werror`; `-Xdoclint:missing` em `compileJava` | 5 |
| 7 | — | `javadoc` com `Xwerror`; `check.dependsOn javadoc` | 5 |
| 8 | — | `.editorconfig` versionado | 9 |
| 9 | — | `distributionSha256Sum` no `gradle-wrapper.properties` | 6 |
| 10 | README: "JDK 11 ou 17" | "JDK **17**", `JAVA_HOME`, e o sintoma de JDK errado | 1 e 6 |
| 11 | — | **nenhuma**: a verificação antes do push usa os comandos do Gradle e do git já descritos, sem tarefa nova ([`COMMITS.md`](../runtime/COMMITS.md#7-verificações-antes-do-push)) | 8 |
| 12 | `.gitignore` sem `logs/` | acrescentar `logs/` (arquivo de log de produção — [`OBSERVABILIDADE.md`](../runtime/OBSERVABILIDADE.md#9-destinos-rotação-e-configuração)) | 8 |

```groovy
// build.gradle — resultado pretendido (esboço; validar ao aplicar)
plugins {
    id 'java'
    id 'org.springframework.boot' version '2.7.8'
    id 'io.spring.dependency-management' version '1.0.15.RELEASE'
}

group = 'br.com.alelo.consumer'
version = '0.0.1'                         // fonte única da versão (seção 3)
description = 'consumer-test'

configurations {
    compileOnly { extendsFrom annotationProcessor }
}

repositories {
    mavenCentral()                        // sem mavenLocal() (seção 4)
}

dependencyLocking { lockAllConfigurations() }      // seção 7

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'com.h2database:h2'
    implementation "org.springdoc:springdoc-openapi-ui:${springdocVersion}"
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') { useJUnitPlatform() }

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
    options.release  = 17
    options.compilerArgs += ['-Xlint:all', '-Werror']
}
tasks.named('compileJava') { options.compilerArgs += ['-Xdoclint:missing'] }
tasks.named('javadoc')     { options.addBooleanOption('Xwerror', true) }
tasks.named('check')       { dependsOn 'javadoc' }
```

> **Nenhuma linha acima adiciona dependência.** Todas as bibliotecas já estão
> no arquivo atual; o que muda é a configuração. *(A dependência
> `springdoc-openapi-data-rest` não era usada pelo projeto — sem Spring Data
> REST — e foi removida junto com o legado, com o `gradle.lockfile`
> regenerado.)*

---

## Decisões Deste Documento

O consolidado está em [`DECISOES.md`](../fundamentos/DECISOES.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| **JDK 17 como mínimo** (README muda de "11 ou 17") | manter compatibilidade com 11 | `record`, `sealed`, `instanceof` com *pattern* |
| `options.release = 17`, sem *toolchain* | *toolchain* do Gradle | não corrige a JVM que roda o Gradle (verificado); `release` fixa a linguagem e a API |
| *Dependency locking* (`gradle.lockfile`) | resolução livre | build determinístico, auditável por diff |
| `-Xlint:all -Werror` + `-Xdoclint:missing` em produção | só `-Xlint` padrão | warning é sinal; doc ausente é erro |
| Versão por fonte única, **sem hash de commit** | `buildInfo()` + hash | fork sem pipeline de release; custo > benefício |
| `.editorconfig`, sem plugin de formatação/análise | Spotless, Checkstyle, PMD, JaCoCo | baixam artefatos: dependência nova |
| `--scan` proibido | build scans | envia dados de build a terceiro |

---

## Anti-Padrões

| Anti-padrão | Correto |
|---|---|
| Rodar `./gradlew` com JDK diferente do 17 e culpar o projeto | `JAVA_HOME` no JDK 17, requisito no README (seção 1) |
| `mavenLocal()` no `build.gradle` | só `mavenCentral()` (seção 4) |
| `assert` de versão de JDK que nunca falha | README como barreira; sem falsa verificação (seção 6) |
| `sourceCompatibility` no JDK mínimo errado | `options.release = 17` (seção 6) |
| `*toolchain*` como "solução" para JDK errado | não corrige a JVM do Gradle (seção 1) |
| Subir o JAR `-plain.jar` ou rodar `bootRun` em produção | só o `bootJar` (seção 2) |
| Versão digitada em mais de um lugar | `version` no `build.gradle`; banner lê (seção 3) |
| Versão curinga (`1.+`) ou sem lock file | versão exata + `gradle.lockfile` commitado (seção 7) |
| Versão do springdoc repetida | `gradle.properties` (seção 7) |
| Dependência nova entrando "só para testar" | proibida pelo enunciado (seção 7) |
| `-Xlint:-categoria` global para "limpar" o build | `@SuppressWarnings` pontual; exclusão global só nominal e provada (seção 5) |
| `build` que não falha por Javadoc inválido | `check` depende de `javadoc` (seção 5) |
| `-Xdoclint:missing` também em teste | só em `compileJava` (seção 5) |
| `checkstyle`/`pmd`/`jacoco` "porque são do Gradle" | baixam artefatos: tratados como dependência (seção 9) |
| `--scan` "para depurar" | proibido: envia dados a terceiro (seção 10) |
| `org.gradle.java.home` com caminho absoluto no repositório | em `~/.gradle/gradle.properties` (seção 8) |
| Wrapper sem `distributionSha256Sum` | checksum oficial (seção 6) |
| Saída de build (`build/`, `.gradle/`) commitada | gerada e descartável (seção 8) |
| Formatação discutida em review | `.editorconfig`; lacuna declarada (seção 9) |
| Regra verificável como item de checklist humano | teste de arquitetura (seção 9) |

---

## Checklist

Antes de abrir um PR que mexe em build, toolchain ou dependências:

- [ ] `./gradlew build` passa com `JAVA_HOME` no **JDK 17**, em checkout limpo?
- [ ] O build passa também com `--offline` (depois da primeira resolução)?
- [ ] O `build.gradle` não tem `mavenLocal()`, caminho absoluto nem repositório de máquina?
- [ ] Warnings novos foram **corrigidos**? Se suprimido: `@SuppressWarnings` pontual, com comentário de motivo?
- [ ] Nenhum `-Xlint:-…` global foi acrescentado (ou, se foi, é nominal e prova o ruído)?
- [ ] O build falha por membro público sem Javadoc e por Javadoc inválido?
- [ ] Nenhuma dependência nova entrou (enunciado)? O `gradle.lockfile` mudou **só** com justificativa?
- [ ] Nenhuma versão curinga entrou, e a versão do springdoc vem de `gradle.properties`?
- [ ] Plugin ou ferramenta nova foi fixado com versão exata, sem baixar artefato novo?
- [ ] A versão sai de fonte única (`version`), padrão `X.Y.Z`?
- [ ] A saída do build (`build/`, `.gradle/`) continua fora do versionamento?
- [ ] O README descreve o JDK 17, o `JAVA_HOME` e o sintoma de JDK errado?
- [ ] Este documento descreve o estado atual do build (não o de quando nasceu)?

---

**[Banco de Dados ←](BANCO.md)**
