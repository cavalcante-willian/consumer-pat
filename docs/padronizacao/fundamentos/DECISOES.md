[← Índice](../../PADRONIZACAO_INDEX.md) · [← ESTRUTURA.md](ESTRUTURA.md)

# Decisões do Projeto

Registro das escolhas que **definem este projeto** e que, sem registro, virariam
"deriva silenciosa": alguém desfaz uma delas sem saber que era deliberada. Cada
linha diz o que foi escolhido, o que foi descartado e por quê, e aponta o
documento que detalha a regra.

**Como usar:**

- Antes de desfazer ou contornar uma regra, procure a linha aqui.
- Uma escolha arquitetural nova entra neste arquivo **no mesmo PR** que a
  introduz, com a alternativa descartada.
- Uma decisão revogada **não é apagada**: ganha uma linha nova que a substitui,
  com o motivo.

**Premissa que pesa em quase tudo:** o enunciado do desafio **proíbe dependência
nova** e permite alterar os contratos da API. Por isso várias linhas dizem "seria
dependência nova".

---

## Estrutura e Código

Detalhe em [`ESTRUTURA.md`](ESTRUTURA.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| Um módulo Gradle, pacotes por camada (`dominio`, `aplicacao`, `infraestrutura`, `api`) | multi-módulo | `./gradlew bootRun` simples para quem avalia; multi-módulo é o próximo passo se crescer |
| Direção de dependência imposta por teste de arquitetura, visibilidade de pacote e revisão | confiar na boa vontade | o módulo único não bloqueia `import` |
| Domínio separado da entidade JPA, com mapeador por agregado | entidade JPA como modelo de domínio | o domínio fica puro e testável sem Spring |
| Validação técnica por Bean Validation nas `Mensagem*`, sem pasta `validadores/` | classes validadoras em camada própria | o starter de validação já está no projeto |
| Composição em `api.configuracao`; `ConsumerTestApplication` só inicia | composição manual em `main` | Spring Boot não tem ponto de entrada com DI manual |
| Multi-módulo e JPMS descartados por ora | adotá-los agora | complicam o `./gradlew bootRun` do desafio |

## Valores, Condicionais e Documentação de Código

Detalhe em [`VALORES.md`](../codigo/VALORES.md), [`CONDICIONAIS.md`](../codigo/CONDICIONAIS.md)
e [`DOCUMENTACAO.md`](../codigo/DOCUMENTACAO.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| Dinheiro em `BigDecimal` com escala 6, `NUMERIC(19,6)` e arredondamento `HALF_EVEN` | `double`; escala 2 | fidelidade de fração de centavo |
| Dinheiro no JSON como string | número JSON | evita perda de precisão no consumidor |
| `Instant` para o instante e `Clock` injetado | `Date`, `LocalDateTime`, `now()` solto | tempo testável e sem ambiguidade de fuso |
| Sem `null` no domínio; ausência modelada (Null Object, `Resultado`) | `Optional.orElse`, ternário de `null` | ausência explícita no tipo |
| `if` só como resíduo, no máximo um condicional por método | `if` livre | cada `if` é uma bifurcação que o leitor mantém na cabeça |
| Javadoc obrigatório em item público, imposto pelo build | documentação opcional | a documentação é parte do contrato |

## Erros, Validação e API

Detalhe em [`ERROS.md`](../qualidade/ERROS.md), [`VALIDACAO.md`](../qualidade/VALIDACAO.md)
e [`API.md`](../qualidade/API.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| Erro de domínio é dado (`ErroDominio` dentro de `Resultado<T, E>`), nunca exceção lançada | exceção para regra de negócio | a assinatura mostra a falha; sem custo de pilha |
| Exceção de infraestrutura convertida em **duas portas** (adaptador e `@RestControllerAdvice`) | uma porta só | com Spring + JPA transacional, parte dos erros só aparece no commit |
| Códigos de erro garantidos por teste, sem arquivo de registro | arquivo `CODIGOS_DE_ERRO.md` | um registro manual envelhece |
| Corpo de erro próprio no formato RFC 7807 (`MensagemProblema`) | `ProblemDetail` | só existe no Spring 6 / Boot 3 |
| Rotas versionadas em `/api/v1`, `POST` nas operações de escrita | rotas do legado (`/consumer/…`) | o enunciado autoriza mudar o contrato |
| Validação declarativa nas `Mensagem*`, com o mesmo `400` para borda e domínio | validadores próprios | o cliente não deve distinguir quem recusou |
| Locale fixo pt-BR para as mensagens de validação | `Accept-Language` | mensagem em português determinística |
| Valor rejeitado nunca no corpo do erro | devolver a mensagem do framework | vazamento de dado e de nome de classe |

## Banco

Detalhe em [`BANCO.md`](../qualidade/BANCO.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| Schema em português, plural, *snake_case*, constraints nomeadas | nomes gerados pelo Hibernate | ilegibilidade e irreversibilidade em log e erro |
| `NUMERIC(19,6)` para dinheiro | `DOUBLE`; `NUMERIC(19,2)` | fidelidade de fração de centavo |
| Lock pessimista no saldo, mais `@Version` | otimista puro | duas compras legítimas simultâneas devem ambas passar |
| `CHECK (saldo >= 0)` além da regra do domínio | só validação no domínio | última linha de defesa |
| Extrato imutável (só registrar) | `update` e `delete` no extrato | registro financeiro |
| `IDENTITY` por tabela | sequência global | simplicidade e previsibilidade |
| Sem migrações; `ddl-auto: create-drop` | Flyway ou Liquibase | dependência nova proibida |
| Sem `MODE=PostgreSQL` no H2 | aproximar o dialeto | falsa segurança sem banco real para validar |
| Banco de produção em aberto | prometer PostgreSQL | exigiria migrações, que são dependência nova |

## Concorrência, Performance e Segurança

Detalhe em [`CONCORRENCIA.md`](../runtime/CONCORRENCIA.md),
[`PERFORMANCE.md`](../qualidade/PERFORMANCE.md) e [`SEGURANCA.md`](../qualidade/SEGURANCA.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| Correção do saldo garantida pelo banco, não pela JVM | `synchronized` ou lock em memória | a API precisa escalar horizontalmente |
| Nenhum `@Async`, `@Scheduled` nem `parallelStream` sem entrar no inventário | concorrência implícita | concorrência escondida só aparece sob carga |
| Sem cache de leitura | `@Cacheable` | tudo é dado pessoal ou financeiro, e o saldo é o dado mais volátil |
| Listagem paginada e com projeção | `select *` sem limite | o legado devolvia ~50.000 consumidores de uma vez |
| Prazo (timeout) no lugar de cancelamento | confiar no cliente | o Servlet não cancela a thread |
| Sem Spring Security nem limitador de taxa | adotá-los | dependência nova; fica declarado como risco |
| Segredos por *placeholder* do Spring, de fora do repositório | cofre próprio | exigiria ferramenta e Argon2id, que não estão no JDK |
| Cabeçalhos de segurança por `Filter` próprio | biblioteca | sem dependência |
| `/h2-console` e Swagger desligados no perfil `prod` | ativos em todo perfil | exigidos pelo README só no ambiente de avaliação |

## Observabilidade

Detalhe em [`OBSERVABILIDADE.md`](../runtime/OBSERVABILIDADE.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| Logback/SLF4J do Spring Boot como infraestrutura de log | construção própria | já vem no `starter`; reimplementar é custo sem ganho |
| Porta fina `SaidaDeLogPorta` sobre o SLF4J | `@Slf4j` direto nas classes | vocabulário em português, domínio puro, ponto único de máscara e de teste |
| `critico` = `ERROR` com *marker* | `FATAL` | o SLF4J não tem `FATAL` |
| Health próprio em `/api/v1/saude` | Actuator | dependência nova |
| Sem métricas locais | Micrometer ou Prometheus | dependência nova |

## Testes

Detalhe em [`TESTES.md`](../qualidade/TESTES.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| JUnit 5 e AssertJ, vindos do `spring-boot-starter-test` | outras bibliotecas | sem dependência nova |
| H2 em memória nos testes de integração | Testcontainers | dependência nova; o limite (passa no H2, falha em outro banco) fica declarado |
| `@MockBean` e `@SpyBean` proibidos | substituir bean no contexto | é *mock* disfarçado de bean |
| Oráculo são as regras do enunciado | o comportamento do legado | o legado é o objeto da revisão e tem defeitos |
| `java.time.Clock` injetado, `Clock.fixed` no teste | `now()` solto | determinismo |

## Build

Detalhe em [`BUILD.md`](../qualidade/BUILD.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| JDK 17 como mínimo (README muda de "11 ou 17") | manter compatibilidade com 11 | `record`, `sealed` e `instanceof` com *pattern* |
| `options.release = 17`, sem *toolchain* | *toolchain* do Gradle | não corrige a JVM que roda o Gradle; `release` fixa a linguagem e a API |
| *Dependency locking* (`gradle.lockfile`) | resolução livre | build determinístico, auditável por diff |
| `-Xlint:all -Werror` e `-Xdoclint:missing` em produção | só o `-Xlint` padrão | warning é sinal; doc ausente é erro |
| Versão por fonte única, sem hash de commit | `buildInfo()` com hash | não há pipeline de release; custo maior que benefício |
| `.editorconfig` e testes de arquitetura, sem plugin de formatação ou análise | Spotless, Checkstyle, PMD, JaCoCo | baixam artefatos: dependência nova |
| Perfil do formatador Java versionado (`config/formatter/eclipse-java.xml`) e `.vscode/settings.json` que o aplica | deixar o padrão da extensão do editor decidir | sem o perfil, a quebra de linha do Java muda conforme a ferramenta de cada pessoa |
| Largura de **100 colunas** para código e comentário | 80 ou 120 | cabe a assinatura genérica do `Resultado` sem quebrar, e é estreito o bastante para revisão lado a lado |
| Verificação de **indentação ligada** no `editorconfig-checker`, com exceções declaradas no `.editorconfig` | manter a indentação desligada | desligada, o `.editorconfig` declararia a regra e só o editor a aplicaria |
| Formatação do código aplicada a **todo** o Java, o legado inclusive | formatar só o código novo | sem uma exceção para o que será removido, a árvore inteira segue uma regra só |
| `--scan` proibido | build scans | envia dados de build a terceiro |

## CI/CD

Detalhe em [`CICD.md`](../qualidade/CICD.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| CI no GitHub Actions, com fluxos reutilizáveis, filtro de paths e gate `CI` | pipeline único; sem CI | cada trilha tem uma responsabilidade e um status check cobre todas |
| Fluxo em duas etapas (fork → enunciado) | PR direto ao enunciado | CI de fork só roda em PR contra o próprio fork |
| *Runner* self-hosted | `ubuntu-latest` | ferramentas já instaladas; risco declarado para fork público |
| `JAVA_HOME` fixo por trilha | `setup-java` | o JDK padrão do *runner* é mais novo que o 19, em que o Gradle 7.6 não roda |
| Auto-aprovação e *squash* por GitHub App | merge manual | o autor não aprova o próprio PR |
| Cada item do plano de commits é um PR | um PR com todos os commits | o *squash* reduz o PR a um commit |
| Sem tag e sem release | incluí-los | não há release nem consumidor do artefato |
| Sem cobertura direcionada e sem testes instrumentados | incluí-los | o JaCoCo baixa artefato; a API não tem interface para instrumentar |
| `javadoc` como trilha própria | sem trilha | o `build` do Gradle não executa o Javadoc |
| Ações de terceiros fixadas por SHA conferido | tag móvel | a revisão vê a troca de versão |
| `.ecrc` e `.markdownlint-cli2.yaml` versionados | locais | o CI os lê |
| A etapa 2 leva `.github/` no *diff* | PR de branch sem esses arquivos | consequência aceita |

## Commits

Detalhe em [`COMMITS.md`](../runtime/COMMITS.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| Conventional Commits em português, escopos deste projeto | histórico livre | changelog útil e histórico legível |
| Mensagem em arquivo, revisada antes de usar | `-m` livre | revisar antes de agir |
| Sem `--amend` nem `--force` em histórico publicado | reescrever histórico | erro se corrige com commit novo |
| `!` no título e `BREAKING CHANGE:` no corpo, consistentes | só o rodapé | as ferramentas de changelog procuram o `!` |
| Infinitivo conferido por heurística | sem validar; dicionário | regex não resolve gramática; a revisão fecha o resto |
| Título contado em caracteres, não bytes | bytes | `ã` e `ç` ocupam 2 bytes |
| Changelog por `git log` | ferramenta de changelog | seria dependência de build |

---

**[Índice →](../../PADRONIZACAO_INDEX.md)**
