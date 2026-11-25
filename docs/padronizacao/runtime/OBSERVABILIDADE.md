[← Índice](../../PADRONIZACAO_INDEX.md) · [← CONCORRENCIA.md](CONCORRENCIA.md)

# Observabilidade — Java 17 / Spring Boot 2.7 / Logback

> **Escopo**: a observabilidade em **Java 17 + Spring Boot 2.7**. O Spring
> Boot já traz o **Logback** e o **SLF4J** no `starter` que o projeto usa —
> nenhuma dependência nova — e destinos, rotação, níveis por ambiente e
> formato se configuram por **propriedade**. Por isso o projeto **não
> reimplementa** a infraestrutura de log: mantém só uma **interface única em
> português** (uma porta fina sobre o SLF4J). Ver
> [Premissas de Observabilidade](#premissas-de-observabilidade).
>
> **Nota sobre exemplos**: os trechos de código e configuração abaixo são
> ilustrativos e **não foram executados** — a regra normativa é o texto
> conceitual. Onde outros documentos (`ERROS.md`, `SEGURANCA.md`) mostram
> `log.warn(...)`, é **abreviação**: a forma normativa é a porta da
> [seção 1](#1-logging-estruturado--uma-porta-em-português).

Define como o sistema expõe seu estado em operação: a porta de log em
português, a correlação por requisição, o mascaramento que mantém dado
sensível fora do log, níveis e eventos, a ausência de telemetria, o health
check por dependência e os destinos com rotação — o que precisa ser visível às
três da manhã, e o que nunca sai do servidor.

**Quando consultar:**

- Ao adicionar qualquer log novo — nível, campos e evento
- Ao decidir se uma informação deve ser logada
- Ao mexer no pipeline de filtros (correlação, log de conclusão)
- Ao configurar níveis, destinos ou o health check

---

## Premissas de Observabilidade

| Tema | Regra neste projeto |
|---|---|
| Infraestrutura de log | **Logback/SLF4J do Boot**, configurado por propriedade |
| Interface única em português | `SaidaDeLogPorta`, uma porta fina sobre o SLF4J |
| Campos nomeados | `CampoDeLog` → `chave=valor` (o SLF4J 1.7 do Boot 2.7 **não tem** campos estruturados) |
| Nível `Critico` | o SLF4J **não tem `FATAL`**: `ERROR` com *marker* `CRITICO` |
| Correlação | filtro + **`MDC`** do SLF4J + `%X{idRequisicao}` no padrão |
| Rotação de arquivo | `logging.logback.rollingpolicy.*` (nativo) |
| Métricas locais | **não se aplica**: exigiria Micrometer/Actuator (dependência nova) |
| Health check | **`/api/v1/saude` próprio**: o Actuator seria dependência nova |
| Hash de commit na inicialização | **não**: [`BUILD.md`](../qualidade/BUILD.md#3-versão-de-build-rastreável) não o adotou; o **banner** traz a versão |

---

## 1. Logging Estruturado — Uma Porta em Português

Todo log passa pela **interface única do projeto**, injetada onde é usada.
Nenhuma classe do projeto — fora do adaptador — importa SLF4J, Logback ou
Log4j, nem chama `System.out`/`printStackTrace`.

```java
// dominio/log/RegistradorDeLog.java — a ÚNICA operação que um adaptador ou um fake implementa
public interface RegistradorDeLog {
    void registrar(NivelDeLog nivel, String evento, Optional<Throwable> causa,
            CampoDeLog... campos);
}

// dominio/log/InformacaoDeLog.java — um papel por nível, cada um em seu arquivo
public interface InformacaoDeLog extends RegistradorDeLog {
    default void informacao(String evento, CampoDeLog... campos) {
        registrar(NivelDeLog.INFORMACAO, evento, Optional.empty(), campos);
    }
}
// DepuracaoDeLog, AvisoDeLog, ErroDeLog, ErroComCausaDeLog e CriticoDeLog seguem o mesmo molde.

// dominio/log/SaidaDeLogPorta.java — só compõe os seis papéis; o vocabulário não tem tipo de biblioteca
public interface SaidaDeLogPorta extends DepuracaoDeLog, InformacaoDeLog, AvisoDeLog, ErroDeLog,
        ErroComCausaDeLog, CriticoDeLog { }     // critico = ERROR + marker CRITICO, no adaptador

// dominio/log/CampoDeLog.java — o dado, separado da mensagem
public record CampoDeLog(String nome, Object valor) {
    public static CampoDeLog campo(String nome, Object valor) { return new CampoDeLog(nome, valor); }
}
```

```java
// CORRETO — evento em português, dados em campos nomeados
saida.informacao("compra.registrada",
    campo("consumidorId", consumidor.id()),
    campo("tipoCartao", tipo),
    campo("valorCobrado", cobrado.paraTexto()));
// → 2026-...Z  INFO [0f1c9c1e-…] b.c.a.c.c.eventos - compra.registrada consumidorId=42 tipoCartao=ALIMENTACAO valorCobrado=29.997000
//   (o logger é um só, `…consumerpat.eventos`: a porta é injetada, e não conhece quem a chamou)

// ERRADO — saída solta, e valor convertido antes de qualquer mascaramento
System.out.println("compra " + numeroCartao + " registrada");
log.info("compra do cartão " + numeroCartao);
```

**A implementação** (`infraestrutura/log/SaidaDeLogAdaptador`) é a **única**
classe do projeto que fala com o SLF4J: monta `evento chave=valor …`, aplica a
máscara por nome de campo (seção 4) e delega ao Logback. Um valor com quebra de
linha não forja outra linha: cada quebra vira `_` (injeção de log). **Nada de destino,
formato ou rotação é código nosso** — vem da configuração (seção 9).

**Regras:**

- **Uma única superfície.** `dominio` e `aplicacao` veem só a porta, com
  vocabulário em português. Quem conversa com a biblioteca é o adaptador.
- **O dado chega como campo nomeado, separado do evento** — é o que permite
  filtrar, agrupar e mascarar. O SLF4J 1.7 não tem campos estruturados, então
  o formato é a convenção `evento chave=valor` (a mesma de
  [`ERROS.md`](../qualidade/ERROS.md#7-log-de-erro-estruturado)).
- **O evento é um nome estável, não uma frase**: minúsculas, em português,
  `substantivo.particípio` (`compra.registrada`, `conflito.detectado`). Nome
  estável é o que se filtra e se conta; frase muda de redação.
- **Evento descreve o fato, campos descrevem os dados.** "falha ao processar"
  não diz o quê; tudo embutido na frase não deixa campo para filtrar.
- **`SaidaDeLogPorta` não loga o corpo da requisição nem a exceção por
  padrão.** `erroComCausa` existe para a causa que **não** carrega dado
  sensível ([`ERROS.md`](../qualidade/ERROS.md#4-nunca-descarte-a-informação-original)).

**Dependência e teste:**

- A regra é **verificável**: `RegraDeDependenciaTeste` falha se `org.slf4j`,
  `ch.qos.logback` ou `org.apache.logging` aparecer fora de
  `infraestrutura.log` — com **uma exceção declarada**: `org.slf4j.MDC` em
  `api.comum` (o filtro de correlação, seção 2) — ou se `System.out`/`.err`/
  `printStackTrace` aparecer em `src/main`.
- **Um fake captura os eventos** (`SaidaDeLogFake`, em `src/test`): é assim que
  se prova "o log **não contém** o número do cartão" sem mexer no Logback.

**Por quê:** consistência de formato, possibilidade de redirecionar e filtrar
sem recompilar, e **um único ponto** onde o mascaramento acontece. A
infraestrutura de baixo **já existe**, e reimplementá-la seria custo sem
ganho.

---

## 2. Correlação por Requisição

Todo log de uma requisição HTTP inclui um `idRequisicao`, gerado na entrada da
API e propagado por todo o processamento.

- **Quem abre:** o `FiltroDeCorrelacao`, **etapa 2 do pipeline**
  ([`API.md`](../qualidade/API.md#4-pipeline-de-requisição)): gera (ou aceita,
  se for UUID válido) o ID, o coloca no **`MDC`** e o devolve em `X-Request-Id`.
- **Como o ID chega a cada linha:** pelo **padrão de log**, não por
  argumento — `%X{idRequisicao}` — então **nenhum código passa o ID à mão**:

  ```yaml
  logging.pattern.level: "%5p [%X{idRequisicao:-sem-requisicao}]"
  ```

- **Quem fecha:** o `FiltroDeLogDeConclusao`, **etapa 3**, registra o evento
  `requisicao.concluida` com método, caminho, status e duração
  (`System.nanoTime()`, [`VALORES.md`](../codigo/VALORES.md#5-relógio--dois-propósitos-dois-relógios)).
- **O `MDC` é `ThreadLocal`:** vive na thread da requisição e **é limpo no
  `finally`** — o pool reaproveita a thread, e um ID não limpo vaza para a
  requisição seguinte
  ([`CONCORRENCIA.md`](CONCORRENCIA.md#4-sem-estado-global-mutável)).
- **Não viaja sozinho para outra thread.** Quando houver tarefa de fundo, o ID
  precisa ser copiado explicitamente
  ([`CONCORRENCIA.md`](CONCORRENCIA.md#7-trabalho-de-fundo--não-existe-hoje-regras-para-quando-existir)).
- **Investigação é filtro, não caça:** aberto o ID, a requisição inteira —
  filtro, validação, caso de uso, adaptador — aparece em ordem.

**Por quê:** uma única operação gera dezenas de linhas em camadas diferentes.
Sem correlação, reconstruir um incidente é caça manual por arquivos com
horário parecido; com ela, é colar o ID e ler a sequência.

---

## 3. Níveis de Log

| Nível da porta | SLF4J / Logback | Uso |
|---|---|---|
| `depuracao` | `DEBUG` | detalhe para desenvolvimento; **desativado em produção** |
| `informacao` | `INFO` | eventos normais de operação (compra registrada, requisição concluída) |
| `aviso` | `WARN` | situação inesperada mas não crítica (conflito 409, saldo insuficiente em volume anormal, timeout de lock) |
| `erro` | `ERROR` | erro tratado que afeta a operação atual (falha inesperada, 500) |
| `critico` | `ERROR` + *marker* **`CRITICO`** | erro que afeta o sistema todo (banco indisponível, falha de inicialização) |

> **O SLF4J não tem `FATAL`** (o Log4j2 tem; o Logback, não). `critico` é
> `ERROR` marcado com `MarkerFactory.getMarker("CRITICO")`. O marcador é o que
> permite **rotear ou alertar** só o crítico — por exemplo, um destino
> exclusivo para ele na configuração do Logback.

**Regras:**

- **Nível é semântica, não filtro de volume.** `erro` em situação que se
  recuperou sozinha gera fadiga de alerta: quem lê aprende a ignorar `erro` e
  perde o sinal quando o erro de verdade chegar. Recuperou sem intervenção?
  É `aviso`.
- **`depuracao` não existe em produção** — a configuração do perfil `prod` o
  desliga (seção 9), e um teste confere.
- **Resultado de negócio esperado não é `erro`.** `422 saldo insuficiente` é
  `informacao` (ou `aviso` se a taxa fugir do normal) — não é falha do sistema.
- **A tabela é vocabulário fechado** — o que não couber, com o evento
  correspondente, entra na [seção 8](#8-eventos-que-o-sistema-registra) no
  mesmo PR.

---

## 4. Logs Não Contêm Dados Sensíveis

Logs nunca contêm senhas, tokens, strings de conexão, **número de cartão
completo, documento ou telefone**. Aplica-se **máscara** quando o dado pode
ser mascarado com segurança; quando não pode, ele é **omitido**. Esta seção
**consolida** [`SEGURANCA.md`](../qualidade/SEGURANCA.md#5-logs-sem-dados-sensíveis)
e [`BANCO.md`](../qualidade/BANCO.md#11-forma-canônica-na-escrita) — três
camadas, da mais forte à mais fraca:

1. **Máscara na origem.** `NumeroDeCartao` e `Documento` têm `toString()` e
   `mascarado()` mascarados: nenhum `campo("cartao", numero)` imprime o
   número completo, **mesmo que o desenvolvedor esqueça**.
2. **Máscara por nome de campo, no adaptador.** `SaidaDeLogAdaptador` mantém
   uma lista **fechada** de nomes sensíveis (`senha`, `token`, `segredo`,
   `numeroCartao`, `documento`, `telefone`, `celular`, `email`…): o valor de um
   campo com esse nome é **substituído por `***`**, qualquer que seja o tipo.
   O **nome do campo descreve o dado** (`numeroCartao`, não `n`): a convenção só
   reconhece o que está bem nomeado.
3. **Barreira de padrão, no Logback — rede de segurança.** Uma expressão
   regular no padrão do log mascara sequências de dígitos com cara de cartão
   (13 a 19) ou documento (11 ou 14), **mesmo vindas de texto livre ou de
   mensagem de exceção**:

   ```yaml
   # application.yml — validado por teste. Num bloco `>-` a barra não é escape do YAML: UMA barra
   # (`\b`, `\d`) chega ao Logback. Com duas, a expressão procuraria uma barra literal e não
   # mascararia nada, em silêncio. O mesmo padrão vale para `console` e para `file`.
   logging.pattern.console: >-
     %d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX,UTC} %5p [%X{idRequisicao:-sem-requisicao}] %logger{36} -
     %replace(%msg){'\b(\d{11}|\d{13,19})\b','***'}%n%wEx
   ```

   *Limites declarados:* pode mascarar um identificador legítimo longo; **não**
   alcança a pilha de uma exceção (`%ex`), que pode trazer o valor na mensagem
   — por isso a regra de logar só a restrição, não a causa
   ([`ERROS.md`](../qualidade/ERROS.md#4-nunca-descarte-a-informação-original)).

**Regras:**

- **Rastreabilidade sem conteúdo.** Do dado sensível registra-se o suficiente
  para rastrear (ID, tipo, últimos 4 dígitos) — nunca o valor. Erro não repete
  o corpo da requisição.
- **Valor monetário não é o problema; o par o é.** `valorCobrado` ao lado de
  `consumidorId` é auditável; ao lado do **número completo do cartão**, não.
- **O log não é a trilha de auditoria.** A trilha financeira é o **extrato**
  ([`BANCO.md`](../qualidade/BANCO.md#8-exclusão-física-por-padrão--e-extrato-imutável));
  o log é diagnóstico, rotaciona e some.
- **`spring.jpa.show-sql` e o `TRACE` do binder do Hibernate são proibidos**:
  imprimem os valores vinculados das consultas
  ([`SEGURANCA.md`](../qualidade/SEGURANCA.md#5-logs-sem-dados-sensíveis)).

**Por quê:** o arquivo de log não tem o controle de acesso do banco — circula
em terminal de operação, em cópia de suporte, em arquivo que alguém pede por
e-mail. Dado sensível no log vaza por um caminho que ninguém protegeu.

---

## 5. Sem Telemetria

O sistema não envia métricas de uso, analytics ou relatórios de erro para
servidores externos. Observabilidade é **integralmente local**: log no console
e no arquivo do próprio servidor.

- **Nenhum destino remoto de log** (Logstash, Splunk, Sentry, Datadog) —
  *appender* remoto é dependência nova **e** exportação de dado.
- **Nenhum `--scan` do Gradle**
  ([`BUILD.md`](../qualidade/BUILD.md#10-proveniência--de-onde-vêm-ferramentas-e-dependências)).
- **O Swagger não consulta validador online**
  ([`SEGURANCA.md`](../qualidade/SEGURANCA.md#1-sem-telemetria)).
- **Telemetria futura seria decisão nova com registro próprio**, nunca uma
  adição silenciosa de biblioteca que "já manda para fora".

**Por quê:** o log pode conter dado de clientes; exportá-lo sem decisão
registrada é exportar dado sensível por um caminho que ninguém revisou. Quem
implanta decide o que sai da máquina — por enquanto, nada.

---

## 6. Métricas — Não se Aplica

Métricas locais em endpoint estilo Prometheus **não se aplicam aqui:** isso exige **Micrometer / Spring Boot Actuator**, que
é dependência nova, proibida pelo enunciado.

O que se perde, e o substituto honesto:

- **Tendência** (consulta lenta às segundas de manhã) **não** é medida por
  série temporal. O substituto parcial é o evento `requisicao.concluida`, que
  registra a duração de cada requisição — contável com ferramenta de texto
  sobre o arquivo de log, **sem** gráfico.
- **A medição de caminho quente** continua sendo a de
  [`PERFORMANCE.md`](../qualidade/PERFORMANCE.md#8-medir-antes-de-otimizar),
  no PR — e é o que decide otimização.

Registrado como **limitação conhecida**, não como omissão.

---

## 7. Health Check — `/api/v1/saude`

A API expõe um endpoint de **health check próprio**, que reporta o estado de
cada dependência crítica em JSON estruturado — para balanceador, orquestrador
ou operador. O `/health` do Actuator não está disponível (dependência nova),
e a regra de que **toda rota vive em `/api/v1/`**
([`API.md`](../qualidade/API.md#1-rest-puro-como-api-pública)) vale também
aqui.

```json
GET /api/v1/saude   →   200
{
  "estado": "SAUDAVEL",
  "dependencias": { "banco": "DISPONIVEL" }
}

GET /api/v1/saude   →   503     (banco fora do ar)
{
  "estado": "DEGRADADO",
  "dependencias": { "banco": "INDISPONIVEL" }
}
```

**Regras:**

- **Saudável é funcionar, não existir.** Processo vivo com banco inacessível
  **não** está saudável: o health mede a capacidade de cumprir a função.
- **Status por dependência**, não "algo está errado". Hoje só há uma
  dependência crítica, o **banco**.
- **A verificação é barata e tem prazo**: pede uma conexão ao pool e a valida
  (`Connection.isValid(timeout)`) com **timeout curto** (1–2 s). Se o pool
  estiver esgotado, **o próprio health falha** — e esse é o sinal certo, não
  um defeito do health.
- **Sem nada interno no corpo**: nem URL de conexão, host, versão do banco ou
  mensagem de exceção
  ([`SEGURANCA.md`](../qualidade/SEGURANCA.md#6-cabeçalhos-de-segurança)). O
  endpoint é **público e sem autenticação** (seção 9 do `SEGURANCA.md`), então
  expõe o mínimo.
- **É o único endpoint que responde 503** neste projeto: 503 = "dependência
  crítica indisponível", entrada nova na tabela de status de
  [`API.md`](../qualidade/API.md#8-verbos-recursos-e-códigos-de-status).
- **Não exige o Actuator, mas respeita a arquitetura**: `SaudePorta` no
  domínio, `SaudeDoBancoAdaptador` na infraestrutura (usa o `DataSource`),
  `ConsultarSaudeCaso`, `SaudeController` — o controller não toca o
  `DataSource`.

**Por quê:** health check é o contrato entre o sistema e quem o opera —
balanceador tirando instância de rotação, orquestrador reiniciando. Contrato
que só diz "vivo" não serve a nenhum dos dois.

---

## 8. Eventos Que o Sistema Registra

Critério: **alguém precisaria saber que isso aconteceu** — para suporte ou
diagnóstico? Se sim, gera log com o nível da tabela; se não, não gera — log que
ninguém lê ocupa rotação e tempo de leitura de quem investiga.

| Evento | Nível | Campos principais |
|---|---|---|
| `requisicao.concluida` | `informacao`; `aviso` se o status é 409; `erro` se é 5xx (um 4xx de negócio, como 404 ou 422, é resultado esperado) | `metodo`, `caminho` (sem a query), `status`, `duracaoMs` |
| `consumidor.criado` | `informacao` | `consumidorId` |
| `consumidor.atualizado` | `informacao` | `consumidorId`, `versao` |
| `cartao.creditado` | `informacao` | `consumidorId`, `tipoCartao`, `valor` |
| `compra.registrada` | `informacao` | `consumidorId`, `tipoCartao`, `valorCobrado` |
| `compra.recusada` | `informacao` (`aviso` se a taxa fugir do normal) | `consumidorId`, `tipoCartao`, `motivo` (código do erro) |
| `conflito.detectado` | `aviso` | `operacao` e `restricao` (violação de integridade: só o nome, **nunca** o valor duplicado) **ou** `entidade` (versão de linha divergente) |
| `lock.timeout` | `aviso` | `operacao` (o tratador não sabe o tipo do cartão; ele fica no corpo da requisição, que o log não leva) |
| `falha.inesperada` | `erro` | `operacao` e a pilha completa, **sem a mensagem** da exceção (`FalhaSemMensagem`), que pode trazer SQL ou um valor vinculado |
| `banco.indisponivel` | `critico` | `operacao` |

**Regras:**

- **Evento novo entra na tabela no mesmo PR** que passa a emiti-lo — tabela
  que não cresce com o sistema vira lista antiga.
- **A inicialização é registrada pelo próprio Spring Boot** (`Started
  ConsumerTestApplication in … seconds`, com o perfil ativo) e o **banner**
  traz a versão ([`BUILD.md`](../qualidade/BUILD.md#3-versão-de-build-rastreável)).
  Não há evento próprio: **não se escreve código para repetir o que o
  framework já diz**.
- **Evento de falha leva o código do erro** (`CPT-…`,
  [`ERROS.md`](../qualidade/ERROS.md#8-código-de-erro-legível-e-verificado-por-teste)) —
  é o que liga a linha de log ao `type` da resposta.
- **Tarefa de fundo** (se existir) registra início, fim e falha
  ([`CONCORRENCIA.md`](CONCORRENCIA.md#7-trabalho-de-fundo--não-existe-hoje-regras-para-quando-existir)).

---

## 9. Destinos, Rotação e Configuração

Tudo **por propriedade** — nenhum código:

```yaml
# application.yml — perfil padrão (desenvolvimento e avaliação)
logging:
  level:
    root: INFO
    br.com.alelo.consumer: DEBUG           # depuração só aqui
  pattern:
    console: "…"     # padrão completo da seção 4: data em UTC, id da requisição e a barreira
    file: "…"        # o mesmo padrão: um teste confere que os dois são iguais

# application-prod.yml — produção
logging:
  level:
    root: INFO
    br.com.alelo.consumer: INFO            # depuracao NÃO existe em produção
  file:
    name: logs/consumer-pat.log
  logback:
    rollingpolicy:
      max-file-size: 10MB
      max-history: 14
      total-size-cap: 500MB
```

**Regras:**

- **Destinos: console e arquivo.** Console no perfil padrão (o terminal do
  operador); **arquivo com rotação em `prod`**.
- **Rotação é da ferramenta, não de hábito.** Arquivo sem rotação cresce até
  encher o disco — e o histórico some exatamente quando mais se precisa dele.
  `max-history` e `total-size-cap` limitam por tempo e por tamanho.
- **Configuração por perfil, não por código.** `DEBUG` no perfil padrão,
  `INFO` em `prod`. **Um teste confere** (`@ActiveProfiles("prod")` →
  nenhum pacote do projeto abaixo de `INFO`).
- **`logs/` entra no `.gitignore`** — saída de runtime nunca é versionada.
- **Destino é local por definição** (seção 5).
- **O Spring Boot aplica padrão e arquivo só na primeira inicialização do Logback da JVM**:
  os contextos de teste seguintes só reaplicam os níveis. Por isso o padrão é provado
  montando o `PatternLayout` direto do YAML (`PadraoDeLinhaDoLogTeste`), e não por contexto.
- **O formato leva UTC e o ID de requisição**
  (`%d{…,UTC}`, `%X{idRequisicao}`), para o log de uma máquina ser
  comparável ao de outra.

**Por quê:** o valor do log depende de ele existir quando a pergunta chegar —
e a pergunta chega dias depois. Rotação garante que "dias antes" ainda está
legível; configuração por perfil garante que o ruído de desenvolvimento não
ocupe o lugar do log real em produção.

---

## 10. Testes Que Fixam Esta Página

| Teste | Garante | Tipo |
|---|---|---|
| `PadraoDeLinhaDoLogTeste`, `ConfiguracaoDeLogTeste`, `ConfiguracaoDeLogDeProducaoTeste` | a barreira de dígitos no padrão (console e arquivo), UTC, `idRequisicao`, `INFO` em `prod` e a rotação | Integração |
| `SaidaDeLogAdaptadorTeste` — `deve_formatar_evento_e_campos_quando_registrar` | `evento chave=valor …` | Integração (Logback `ListAppender`) |
| `SaidaDeLogAdaptadorTeste` — `deve_mascarar_o_valor_quando_o_nome_do_campo_e_sensivel` | `numeroCartao=***` | Integração |
| `SaidaDeLogAdaptadorTeste` — `deve_marcar_como_critico_quando_registrar_critico` | `ERROR` + marker `CRITICO` | Integração |
| `NumeroDeCartaoTeste` / `DocumentoTeste` — `deve_mascarar_no_to_string` | `toString()` não imprime o número completo | Unitário |
| `deve_devolver_o_id_de_requisicao_quando_qualquer_resposta` | `X-Request-Id` em toda resposta; ID inválido do cliente é regerado | Contrato |
| `deve_limpar_o_mdc_quando_a_requisicao_termina` | nada vaza para a requisição seguinte | Integração |
| `deve_responder_200_quando_banco_disponivel` e `deve_responder_503_quando_banco_indisponivel` | o health reflete a dependência | Contrato (`SaudePorta` **fake**, via `@TestConfiguration`) |
| `deve_manter_prod_em_info_quando_perfil_prod` | nenhum `DEBUG` em produção | Integração |
| `RegraDeDependenciaTeste` | SLF4J/Logback só no adaptador; sem `System.out`/`printStackTrace` | Arquitetura |

*(Entram na tabela da seção 14 de [`TESTES.md`](../qualidade/TESTES.md).)*

---

## Anti-Padrões

| Anti-padrão | Correto |
|---|---|
| `System.out.println` / `e.printStackTrace()` | a porta de log (seção 1) |
| `@Slf4j` / `@Log4j2` em caso de uso, controller ou domínio | `SaidaDeLogPorta` injetada (seção 1) |
| Reimplementar destino, formato ou rotação em código | propriedades do Logback (seção 9) |
| Concatenar valor na mensagem (`"cartão " + numero`) | campo nomeado (seção 1) |
| Evento como frase livre, variável | nome estável `substantivo.particípio` (seção 1) |
| Nome de campo que não descreve o dado (`n`, `v`) | nome pelo qual a convenção de máscara reconhece (seção 4) |
| Número de cartão, documento ou telefone completo no log | máscara na origem, por nome e por padrão (seção 4) |
| `show-sql` / `TRACE` do binder do Hibernate | proibidos (seção 4) |
| `MDC` sem `clear()` no `finally` | limpo ao fim da requisição (seção 2) |
| Passar o `idRequisicao` por argumento em cada chamada | `%X{idRequisicao}` no padrão (seção 2) |
| `erro` para situação que se recuperou sozinha | `aviso` (seção 3) |
| Resultado de negócio esperado (saldo insuficiente) como `erro` | `informacao` (seção 3) |
| `DEBUG` ligado em produção | `INFO` no perfil `prod`, com teste (seção 9) |
| Arquivo de log sem rotação | `rollingpolicy` com tamanho e histórico (seção 9) |
| Usar o log como trilha de auditoria financeira | o extrato é a trilha (seção 4) |
| Appender remoto, SDK de analytics, `--scan` | tudo local (seção 5) |
| Health que só diz "vivo", ou que devolve URL/exceção do banco | status por dependência, sem internals (seção 7) |
| `/health` fora de `/api/v1/` ou via Actuator | `/api/v1/saude` próprio (seção 7) |
| Evento novo sem linha na tabela | tabela da seção 8, no mesmo PR |

---

## Checklist

Antes de adicionar (ou revisar) qualquer log:

- [ ] O log passa pela `SaidaDeLogPorta`, nunca por `System.out`, `@Slf4j` ou `LoggerFactory`?
- [ ] O evento é um nome estável em português, e os dados estão em `campo(...)` nomeados, sem concatenação?
- [ ] Os nomes dos campos descrevem o dado (`numeroCartao`, `consumidorId`)?
- [ ] Nenhum dado sensível completo aparece — e o tipo do campo (`NumeroDeCartao`, `Documento`) mascara no `toString()`?
- [ ] O nível corresponde à semântica (sem `erro` recuperável, sem `informacao` de falha, sem `erro` para recusa de negócio)?
- [ ] O evento está na tabela da seção 8 — ou entrou nela no mesmo PR?
- [ ] Evento de falha leva o código `CPT-…`?
- [ ] Nenhum `show-sql`, `TRACE` de binder, destino remoto ou `--scan`?
- [ ] O `MDC` continua sendo limpo no `finally`?
- [ ] `application-prod.yml` continua em `INFO`, e `logs/` está no `.gitignore`?
- [ ] O health check responde por dependência, com prazo, sem internals?
- [ ] O adaptador é a única classe com SLF4J/Logback (e `MDC` só em `api.comum`)?

---

## Decisões Deste Documento

O consolidado está em [`DECISOES.md`](../fundamentos/DECISOES.md).

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| Logback/SLF4J do Spring Boot como infraestrutura de log | construção própria de níveis, formato, destinos e rotação | já vem no `starter`; reimplementar é custo sem ganho |
| Porta fina `SaidaDeLogPorta` sobre o SLF4J | `@Slf4j` direto nas classes | vocabulário em português, domínio puro, ponto único de máscara e de teste |
| `critico` = `ERROR` + marker | `FATAL` | o SLF4J não tem `FATAL` |
| Health próprio em `/api/v1/saude` | Actuator `/health` | Actuator é dependência nova |
| Sem métricas locais | Micrometer/Prometheus | dependência nova |
| Sem hash de commit na inicialização | `buildInfo()` + hash | [`BUILD.md`](../qualidade/BUILD.md#3-versão-de-build-rastreável) |

---

**[Concorrência ←](CONCORRENCIA.md)**
