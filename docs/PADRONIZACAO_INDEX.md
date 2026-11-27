# Padronização do Projeto

Regras de código, qualidade e entrega do **consumer-pat**: Java 17, Spring Boot
2.7, Gradle 7.6 e H2. Cada documento define uma parte, e o
[`DECISOES.md`](padronizacao/fundamentos/DECISOES.md) registra o que foi
escolhido e o que foi descartado.

**Premissa do desafio:** é proibido adicionar dependência, e os contratos da API
podem mudar. Várias regras existem por causa disso.

> Os documentos foram escritos **antes** do código, a partir do **legado do
> desafio**, que já saiu da árvore. Onde um exemplo divergiu do que se
> construiu, vale o texto conceitual, e o exemplo foi corrigido quando se notou.

## Fundamentos

| Documento | Assunto |
|---|---|
| [`ESTRUTURA.md`](padronizacao/fundamentos/ESTRUTURA.md) | camadas, pacotes, regra de dependência e onde mora cada tipo |
| [`DECISOES.md`](padronizacao/fundamentos/DECISOES.md) | as escolhas do projeto e as alternativas descartadas |

## Código

| Documento | Assunto |
|---|---|
| [`CONDICIONAIS.md`](padronizacao/codigo/CONDICIONAIS.md) | como eliminar `if`: a hierarquia de 12 níveis |
| [`VALORES.md`](padronizacao/codigo/VALORES.md) | tipos de valor, dinheiro, data e hora, ausência |
| [`DOCUMENTACAO.md`](padronizacao/codigo/DOCUMENTACAO.md) | Javadoc, comentários e READMEs por camada |

## Qualidade

| Documento | Assunto |
|---|---|
| [`API.md`](padronizacao/qualidade/API.md) | REST, versionamento, erro padronizado, contrato |
| [`ERROS.md`](padronizacao/qualidade/ERROS.md) | erro de domínio como dado e conversão de exceções |
| [`VALIDACAO.md`](padronizacao/qualidade/VALIDACAO.md) | validação de entrada na fronteira |
| [`BANCO.md`](padronizacao/qualidade/BANCO.md) | schema, tipos, restrições, versão de linha e lock |
| [`PERFORMANCE.md`](padronizacao/qualidade/PERFORMANCE.md) | N+1, paginação, projeções, timeouts e a decisão de não cachear |
| [`SEGURANCA.md`](padronizacao/qualidade/SEGURANCA.md) | postura de segurança e riscos fora de escopo |
| [`TESTES.md`](padronizacao/qualidade/TESTES.md) | a suíte como contrato de comportamento |
| [`BUILD.md`](padronizacao/qualidade/BUILD.md) | Gradle, JDK 17, lock file e warnings como erro |
| [`CICD.md`](padronizacao/qualidade/CICD.md) | pipeline de CI, gate de merge e implantação |

## Runtime e Entrega

| Documento | Assunto |
|---|---|
| [`CONCORRENCIA.md`](padronizacao/runtime/CONCORRENCIA.md) | concorrência no saldo e inventário de threads |
| [`OBSERVABILIDADE.md`](padronizacao/runtime/OBSERVABILIDADE.md) | log em português, correlação, mascaramento e health check |
| [`COMMITS.md`](padronizacao/runtime/COMMITS.md) | Conventional Commits, branches, PRs e o plano de commits |

## Por onde começar

- **Vai escrever código:** [`ESTRUTURA.md`](padronizacao/fundamentos/ESTRUTURA.md),
  depois [`CONDICIONAIS.md`](padronizacao/codigo/CONDICIONAIS.md) e
  [`VALORES.md`](padronizacao/codigo/VALORES.md).
- **Vai abrir um PR:** [`COMMITS.md`](padronizacao/runtime/COMMITS.md) e
  [`CICD.md`](padronizacao/qualidade/CICD.md).
- **Vai mexer em saldo ou banco:** [`BANCO.md`](padronizacao/qualidade/BANCO.md)
  e [`CONCORRENCIA.md`](padronizacao/runtime/CONCORRENCIA.md).
