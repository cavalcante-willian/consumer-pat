[← Índice](../../PADRONIZACAO_INDEX.md) · [← PERFORMANCE.md](PERFORMANCE.md)

# Segurança — Java 17 / Spring Boot 2.7

> **Escopo**: a postura de segurança de **Java 17 + Spring Boot 2.7**. Boa
> parte dos controles habituais depende de bibliotecas que o desafio
> **proíbe** (Spring Security, bibliotecas de rate limiting), então este
> documento separa o que se aplica do que fica de fora. O que se aplica
> está nas seções 1 a 8; o que não se aplica está listado com motivo em
> [Controles e Destino](#controles-e-destino) e, no caso dos riscos reais que
> ficam em aberto, na
> [seção 9](#9-riscos-conhecidos-e-fora-de-escopo).
>
> **Nota sobre exemplos**: os trechos de código e configuração abaixo são
> ilustrativos e **não foram executados** — a regra normativa é o texto
> conceitual. O código atual ainda é o **legado do desafio**; este
> documento descreve a postura pretendida (ver
> [`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md#este-documento-é-vivo)).

Define a postura de segurança desta API de cartões de benefício: ausência
de telemetria, validação na entrada, privilégio mínimo, tratamento de
segredos, logs sem dado sensível, cabeçalhos de segurança, proteção contra
injeção e CORS — e o que **não** está coberto.

**Contexto que pesa em tudo abaixo:** a API **altera saldo financeiro** e
**expõe dado pessoal** (documento, telefone, endereço, número de cartão),
e hoje **não tem autenticação nenhuma**. Várias regras abaixo são
mitigação parcial desse fato — não substituem a autenticação que falta
(seção 9).

---

## Controles e Destino

| Controle | Situação | Motivo |
|---|---|---|
| Sem telemetria (seção 1) | **Vale** | critério para qualquer dependência ou ferramenta |
| Validação na entrada (seção 2) | **Vale, reduzida** | não há HTML de usuário; fica a validação de borda |
| Mínimo de privilégios (seção 3) | **Vale** | o achado mais concreto do legado (`/h2-console`) está aqui |
| Segredos (seção 4) | **Regra curta** | cofre próprio exigiria ferramenta de setup e Argon2id, que **não está no JDK** (exigiria biblioteca) |
| Autenticação e autorização (seção 9) | **Fora de escopo** | exige Spring Security — dependência nova |
| HTTPS obrigatório (seção 9) | **Fora de escopo** | o TLS é terminado fora da aplicação |
| Cabeçalhos de segurança (seção 6) | **Vale** | implementável com um `Filter` próprio, sem dependência |
| Sanitização de saída (seção 10) | **Não se aplica** | a API só devolve JSON, sem HTML |
| Logs sem dado sensível (seção 5) | **Vale, ampliada** | crítica aqui (cartão, documento) e com armadilhas próprias de Java |
| Proteção contra injeção (seção 7) | **Vale** | JPA parametriza; inclui *mass assignment* e ordenação |
| CORS (seção 8) | **Vale** | o padrão do Spring MVC já é fechado |
| Rate limiting (seção 9) | **Fora de escopo** | exigiria biblioteca; um limitador em memória conflita com API *stateless* |
| Arquivos de upload (seção 10) | **Não se aplica** | não há upload |

---

## 1. Sem Telemetria

O sistema não envia dados de uso, analytics ou telemetria para servidores
externos, e nenhum endpoint chama serviço externo em nome do sistema.

**Regras:**

- **Dependência com telemetria embutida é critério de rejeição** na
  avaliação de qualquer dependência nova — o que, neste projeto, já é raro,
  porque dependência nova é proibida pelo enunciado. Fica como critério
  para quando o projeto sair do desafio.
- **Sem Actuator exposto.** Os endpoints de gestão do Spring Boot vazam
  informação de ambiente; o `spring-boot-starter-actuator` não faz parte
  do projeto e não entra.
- **O Swagger UI não consulta serviço externo.** O Swagger UI, por padrão,
  pode chamar um **validador online** para páginas públicas. Desligar:
  `springdoc.swagger-ui.validator-url: none` *(propriedade a validar na
  versão 1.6.12 do `springdoc-openapi-ui`)*. Os recursos estáticos do
  Swagger vêm do próprio jar (webjars), sem CDN.

**Por quê:** uma API exposta à internet não deve vazar metadados de quem a
usa, e cada chamada externa é superfície de ataque e dependência de rede.

---

## 2. Entrada Validada na Borda

Toda entrada externa é validada **antes** de chegar ao caso de uso. A
régua de segurança, sobre o mecanismo de [`VALIDACAO.md`](VALIDACAO.md) e
de [`CONDICIONAIS.md`](../codigo/CONDICIONAIS.md) (nível 10):

- **A fronteira é o único ponto em que a confiança é decidida.** Dado que
  entrou validado é seguro por construção; o domínio não valida de novo o
  que a borda já garantiu (e não confia em nada que a borda não validou).
- **Todo `String` de entrada tem `@Size(max = …)`.** Sem teto, um corpo
  JSON de 50 MB num campo `nome` é aceito — e o Spring MVC não tem
  propriedade nativa para limitar o tamanho do corpo JSON
  ([`PERFORMANCE.md`](PERFORMANCE.md#9-timeouts-e-limites)).
- **Formato declarado, não presumido**: documento (`@Pattern`), e-mail
  (`@Email`), valor (`@Positive` + `@Digits`), tipo de estabelecimento
  (código conhecido, validado no domínio).
- **Campo desconhecido é recusado** (`fail-on-unknown-properties: true`,
  [`API.md`](API.md#6-serialização-determinística)): é o que impede que um
  campo de saldo enviado num `PUT` seja **ignorado em silêncio**.
- **Enum fechado onde o cliente escolhe** (`ordenacao`, `direcao`) —
  nunca texto livre que vira nome de coluna.
- **O número do cartão é `String`, não `int`.** No legado, `int
  foodCardNumber` limita o espaço a ~2 bilhões e **estoura com um número
  real de cartão** — além de ser trivialmente enumerável (seção 9).

**Por quê:** em API financeira, a entrada é a superfície de ataque
dominante. Validar na borda é a única decisão que vale para todo caso de
uso presente e futuro sem exigir que cada um se lembre da regra.

---

## 3. Mínimo de Privilégios

O processo roda com o mínimo de privilégio necessário — nunca como
root/administrador, nunca com credencial mais forte do que a operação
exige.

| Superfície | Privilégio mínimo | Estado hoje |
|---|---|---|
| Processo do servidor | usuário dedicado de sistema, sem administração | recomendação de implantação |
| Banco de dados | usuário limitado a `SELECT/INSERT/UPDATE` do próprio schema — nunca administrador | o usuário `sa` do H2 **é** administrador (aceitável: H2 em memória, descartável) |
| **Console do H2** (`/h2-console`) | **desabilitado em produção** | **habilitado por padrão — achado do legado** |
| Swagger UI | desabilitável em produção | habilitado (o README o documenta) |

### O H2 Console é uma porta dos fundos

`/h2-console` com `sa/sa` dá **leitura e escrita irrestritas no banco
inteiro** pelo navegador — contorna a API, contorna a validação, contorna
o lock do saldo. É exatamente a "superfície privada por baixo da API" que
[`API.md`](API.md#1-rest-puro-como-api-pública) proíbe.

**Tensão com o enunciado:** o README do desafio documenta o console como
parte do ambiente local (`http://localhost:8080/h2-console/`). A decisão
é:

- **Perfil padrão (desenvolvimento): console habilitado**, como o README
  descreve — o avaliador precisa dele.
- **Perfil `prod`: console desabilitado**, em `application-prod.yml`:

  ```yaml
  # application-prod.yml
  spring:
    h2:
      console:
        enabled: false
  springdoc:
    swagger-ui:
      enabled: false          # opcional: o contrato não precisa estar público em produção
  ```

- `spring.h2.console.settings.web-allow-others` **permanece `false`**
  (padrão): o console só aceita conexão da própria máquina.
- O PR registra que o perfil padrão **não é seguro para exposição** — ele
  é o ambiente de avaliação do desafio.

**Por quê:** privilégio é o que transforma falha comum em incidente. Um
console administrativo aberto com credencial pública anula todos os outros
controles deste documento.

---

## 4. Segredos — Regra Curta

A forma mais forte de guardar segredo seria um arquivo criptografado,
vinculado a hardware e à senha do administrador. **Essa opção não foi
adotada aqui** (decisão em [`DECISOES.md`](../fundamentos/DECISOES.md)): ela
depende de uma ferramenta de setup própria e de Argon2id, que não faz parte
do JDK — dependência nova, proibida.

O que fica é a regra de fundo:

- **Nenhum segredo real no repositório**: nem em código, nem em
  `application*.yml`, nem em teste, nem em log, nem em mensagem de commit.
- **A credencial `sa/sa` é demonstrativa e pública** (está no README). Ela
  protege um banco em memória, descartável. **Nunca** é reutilizada em
  ambiente real.
- **Em ambiente real, o segredo vem de fora do repositório**, por
  *placeholder* do Spring (`${SENHA_BANCO}`), resolvido por um canal que o
  repositório não conhece. **Aceita-se** variável de ambiente ou um gerenciador de segredos da plataforma, com a
  ressalva de que variável de ambiente é legível pelo processo e por quem
  administra a máquina (limitação reconhecida, não ignorada).
- **`.gitignore` cobre** `application-local.yml`, `.env`, `*.pem`, `*.key`
  — arquivos onde um segredo tende a nascer.
- **Segredo nunca é logado** nem aparece em `detail` de erro
  ([seção 5](#5-logs-sem-dados-sensíveis)).

**Por quê:** a regra "nada de segredo no histórico do git" é a que vale
em qualquer projeto — o histórico é permanente, e rotacionar uma senha que
já vazou é consertar depois do dano.

---

## 5. Logs Sem Dados Sensíveis

Logs nunca contêm senhas, tokens, strings de conexão com credencial,
**número de cartão completo, documento (CPF/CNPJ) ou telefone**. Usa-se
máscara na origem ou omissão — a mesma regra de
[`ERROS.md`](ERROS.md#7-log-de-erro-estruturado).

**Regras:**

- **A máscara é aplicada onde o dado nasce** (variantes de erro carregam
  `numeroMascarado`, `documentoMascarado`), não por quem loga — assim
  nenhum consumidor posterior consegue vazar o valor completo por
  esquecimento.
- **Log identifica quem fez o quê — nunca inclui o valor que prova o
  quem.** `consumidorId` no log é auditoria; o número do cartão não.
- **Placeholders do Log4j2/SLF4J (`{}`), nunca concatenação** — concatenar
  converte o valor antes de o logger ver.

### Armadilhas específicas do Java

| Armadilha | Por quê | Regra |
|---|---|---|
| **`toString()` gerado** por `record` e por Lombok (`@Data`, `@Value`) lista **todos os campos** | `log.info("compra {}", mensagem)` imprime `numeroCartao` | tipos com dado sensível **sobrescrevem `toString()`** (mascarado) — ou o objeto nunca é logado, só seus identificadores |
| `spring.jpa.show-sql=true` e `logging.level.org.hibernate.type.descriptor.sql=TRACE` | imprimem os **valores vinculados** das consultas (cartão, documento) | proibidos em qualquer perfil que não seja depuração local descartável |
| **Mensagem da exceção de integridade** (`DataIntegrityViolationException`) | o H2 (e outros bancos) incluem o **valor duplicado** na mensagem: `... VALUES ('5555000011112222')` | para exceção de persistência, logar **só a classe e o nome da restrição**, não `getMessage()` nem a causa — ver [`ERROS.md`](ERROS.md#4-nunca-descarte-a-informação-original) |
| Log de **corpo da requisição** | traz tudo, inclusive dado sensível | nenhum filtro de log lê o corpo; o log de conclusão registra método, caminho, status e duração |
| **Quebra de linha em dado do usuário** num log de texto | injeção de log (linhas forjadas) | o `idRequisicao` aceito do cliente só é reaproveitado se tiver formato de UUID ([`API.md`](API.md#4-pipeline-de-requisição)); texto livre do usuário não vai cru para o log |

```java
// CORRETO — identifica sem expor; o objeto não é logado
log.info("compra registrada consumidorId={} tipoCartao={} idRequisicao={}", consumidorId, tipo, idRequisicao);

// ERRADO — record/Lombok imprime numeroCartao, valor e tudo mais
log.info("compra registrada {}", mensagemRegistrarCompra);

// ERRADO — concatenação: converte o valor antes de qualquer mascaramento
log.debug("buscando cartão " + numeroCartao);
```

**Por quê:** log é o artefato mais compartilhado do sistema — operação,
suporte e eventual investigação têm acesso a ele, e ninguém pensa em log
como superfície de vazamento até o vazamento.

---

## 6. Cabeçalhos de Segurança

Toda resposta da API envia os cabeçalhos de segurança. Sem Spring Security,
o Spring Boot **não envia nenhum** — a responsabilidade é de um `Filter`
próprio, em `api.comum`, registrado em `api.configuracao`:

| Cabeçalho | Valor | Ameaça mitigada |
|---|---|---|
| `X-Content-Type-Options` | `nosniff` | o navegador inferir um tipo e executar o conteúdo |
| `X-Frame-Options` | `DENY` | *clickjacking* |
| `Content-Security-Policy` | `default-src 'none'; frame-ancestors 'none'` | a resposta é JSON e não precisa carregar nada; nenhum recurso externo é permitido |
| `Referrer-Policy` | `no-referrer` | vazamento de URL em cabeçalho `Referer` |
| `Cache-Control` | `no-store` | cache de dado pessoal por intermediário — aplicado pelo interceptor descrito em [`PERFORMANCE.md`](PERFORMANCE.md#5-cache-de-leitura--recusado-de-propósito) |

```java
// api/comum/FiltroDeCabecalhosDeSeguranca.java — esboço; validar ao implementar
public class FiltroDeCabecalhosDeSeguranca extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        resposta.setHeader("X-Content-Type-Options", "nosniff");
        resposta.setHeader("X-Frame-Options", "DENY");
        resposta.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
        resposta.setHeader("Referrer-Policy", "no-referrer");
        cadeia.doFilter(requisicao, resposta);
    }
}
```

**Regras:**

- **Aplicados num único ponto**, o filtro **mais externo** do pipeline
  ([`API.md`](API.md#4-pipeline-de-requisição), etapa 1) — nunca por
  controller, nunca "quando lembrarem". Cobre também as respostas de erro,
  porque o filtro envolve o `DispatcherServlet`.
- **Registrado só para `/api/*`** (`addUrlPatterns("/api/*")`). **Não** vale
  para `/swagger-ui`, `/v3/api-docs` nem `/h2-console`: a CSP
  `default-src 'none'` e o `X-Frame-Options: DENY` **quebram** essas
  páginas (o console do H2 usa *frames*). Elas são ambiente de
  desenvolvimento, desligadas em produção (seção 3).
- **O filtro não lança exceção** (regra do pipeline): só escreve cabeçalhos.
- **HSTS não é enviado pela aplicação.** Ele só tem efeito sobre HTTPS, e o
  TLS é terminado fora (proxy/balanceador) — é lá que o
  `Strict-Transport-Security` é configurado (seção 9).
- **Contrato testado:** o teste de contrato verifica que uma resposta de
  sucesso **e** uma de erro (`application/problem+json`) trazem os
  cabeçalhos.

**Por quê:** cabeçalho de segurança é a única defesa que o servidor
consegue impor ao navegador depois que a resposta saiu. Numa API JSON o
conjunto é pequeno — e é barato demais para ficar de fora.

---

## 7. Proteção Contra Injeção

### SQL / JPQL

O Spring Data JPA e o Hibernate **parametrizam por construção**: o valor
vindo do usuário chega como parâmetro vinculado, nunca como parte do texto
da consulta.

- **Consulta derivada (`findByNumero…`) e JPQL com parâmetro nomeado são o
  padrão.** SQL nativo só em `infraestrutura`, com **parâmetros
  vinculados** (`?1`, `:nome`) — **nunca** concatenação nem
  `String.format`.
- **Proibido** montar consulta por concatenação, mesmo "só com número".

```java
// CORRETO — parâmetro vinculado
@Query("select c from CartaoEntidade c where c.numero = :numero")
Optional<CartaoEntidade> buscarPorNumero(@Param("numero") String numero);

// PROIBIDO — dado do usuário vira código
entityManager.createNativeQuery("select * from cartao where numero = '" + numero + "'");
```

- **Ordenação: nunca aceitar `Sort`/`Pageable` do cliente.** O nome da
  propriedade de ordenação vindo de `?sort=` é um vetor conhecido de
  injeção em `@Query` e expõe o modelo interno. O cliente envia `ordenacao`
  (enum fechado) e a borda monta o `PageRequest`
  ([`API.md`](API.md#9-paginação-filtros-e-ordenação)).

### *Mass assignment*

Já tratado pela fronteira de tipos: nenhuma `Mensagem*` de entrada contém
campo de saldo, e campo desconhecido é recusado
([`API.md`](API.md#5-dtos-para-entrada-e-saída)). O legado desserializava
o `@Entity` direto do corpo — quem enviasse `foodCardBalance` num `POST`
**escolhia o próprio saldo**.

### Desserialização

- **Sem *default typing* do Jackson** (`enableDefaultTyping`,
  `@JsonTypeInfo(use = CLASS)` sobre entrada): permite instanciar classe
  arbitrária a partir do JSON.
- **XXE não se aplica**: a API é JSON-only, sem dependência de XML.

**Por quê:** injeção é a classe de ataque mais antiga e ainda a mais
frequente — ela explora a confusão entre código e dado. Quando o framework
parametriza por construção, a confusão deixa de existir; quando o código
monta texto, ela existe em cada linha.

---

## 8. CORS

CORS decide quais **origens de navegador** podem ler as respostas da API.

- **O padrão do Spring MVC é fechado**: sem configuração, nenhuma origem
  externa é permitida. **É o estado desejado enquanto não houver um cliente
  de navegador de origem conhecida.**
- **Nunca `allowedOrigins("*")` em produção.** Combinado com
  `allowCredentials(true)`, o Spring recusa em tempo de execução
  (`IllegalArgumentException`); sozinho, equivale a não ter CORS.
- **Quando houver cliente**, as origens são **explícitas e vêm de
  configuração** (`application.yml`, por perfil), registradas em
  `ConfiguracaoDeWeb` via `WebMvcConfigurer#addCorsMappings` — nunca
  *hardcoded*.
- **Só `application/json` nos `POST`/`PUT`.** Outro tipo recebe 415, e
  `application/json` cross-origin força a **requisição de pré-voo
  (preflight)**, que o servidor, sem CORS configurado, recusa.

> **Limite honesto:** CORS protege a *leitura* da resposta no navegador;
> **não** protege a API de quem chama direto (`curl`, outro servidor). Sem
> autenticação, qualquer cliente fora de navegador opera livremente
> (seção 9).

---

## 9. Riscos Conhecidos e Fora de Escopo

Esta tabela é o que o PR declara **abertamente**. Nenhum item abaixo foi
"resolvido" — todos exigem dependência nova ou infraestrutura que o
desafio não cobre, e estão aqui para que o risco seja **conhecido e
dimensionado**, não escondido.

| # | Risco | Por que fica em aberto | Mitigação recomendada | Impacto |
|---|---|---|---|---|
| 1 | **Sem autenticação nem autorização** — qualquer cliente cria, edita, credita e debita | Spring Security é dependência nova | Spring Security + JWT/OAuth2; claims por operação; `Authorization: Bearer`; 401/403/404 conforme [`API.md`](API.md#10-autenticação-autorização-e-fronteira) | **Crítico** |
| 2 | **Sem rate limiting** — um cliente pode varrer números de cartão ou esgotar o pool | exigiria biblioteca; limitador em memória conflita com *stateless* | limitação no proxy/gateway, ou biblioteca dedicada; 429 com `Retry-After` | Alto |
| 3 | **Enumeração de cartões** — 404 × 422 revelam se um número existe, e o legado usava `int` (≤ 2 bi) | consequência dos itens 1 e 2 | número de cartão de alta entropia (`String`); sem autenticação, a resposta de "inexistente" vaza existência | Alto |
| 4 | **`POST` de compra/crédito não é idempotente** — retentativa após *timeout* debita duas vezes | exige contrato novo (chave de idempotência) | cabeçalho `Idempotency-Key` com registro da chave ([`API.md`](API.md#11-concorrência-idempotência-e-conflitos)) | Alto |
| 5 | **HTTPS/HSTS** | o TLS é terminado fora da aplicação | proxy com TLS atual, redirecionamento HTTP→HTTPS e `Strict-Transport-Security` | Alto em produção |
| 6 | **Dado pessoal em repouso sem criptografia** (documento, telefone, endereço) e sem política de retenção | fora do escopo do desafio | criptografia de disco/coluna; minimização — a listagem já **não** devolve dado pessoal ([`PERFORMANCE.md`](PERFORMANCE.md#3-projeções-em-vez-de-entidades-completas)); atenção à LGPD | Médio |
| 7 | **Sem trilha de auditoria de quem alterou saldo** | sem identidade (item 1); e no legado o **crédito não gera registro no extrato** | extrato também para crédito; autor da operação quando houver autenticação | Médio |
| 8 | **Segredos de produção** (senha de banco) | cofre de segredos não adotado (seção 4) | gerenciador de segredos da plataforma | Médio |
| 9 | **Spring Boot 2.7.8 e `springdoc` 1.6.12 desatualizados** | atualizar a **linha** (3.x) é troca de plataforma, não "sem dependência nova" | atualizar ao menos para a última 2.7.x (correções publicadas) e planejar a migração; **confirmar no calendário de suporte do Spring** que a linha 2.7 saiu do suporte aberto | Médio |
| 10 | **Console do H2 e Swagger no perfil padrão** | exigidos pelo README | desabilitados em `application-prod.yml` (seção 3) | Alto se exposto |

> No PR, esta tabela entra como "O que não foi feito e por quê". Declarar o
> risco **mostra que ele foi identificado**; ocultá-lo é o que a revisão
> deveria penalizar.

---

## 10. Não se Aplicam

- **Sanitização de saída:** a API devolve apenas JSON serializado; não há
  HTML. A regra passa a valer integralmente se a API servir conteúdo
  renderizável.
- **Arquivos de upload:** não há upload. Se aparecer, extensão **e**
  conteúdo real validados, nome interno gerado pelo servidor, teto de
  tamanho antes de alocar — e esta seção é reescrita no mesmo PR.

---

## Anti-Padrões

| Anti-padrão | Correto |
|---|---|
| `/h2-console` aberto com `sa/sa` fora do perfil de desenvolvimento | Desabilitado em `application-prod.yml` (seção 3) |
| Segredo real em `application*.yml`, código ou teste | Fora do repositório, por placeholder (seção 4) |
| `sa/sa` reutilizado em ambiente real | Credencial demonstrativa nunca sai do ambiente local |
| Número de cartão como `int` | `String` (seções 2 e 9) |
| Receber `@Entity` ou objeto de domínio no `@RequestBody` | `Mensagem*` própria da operação, sem saldo (seção 7) |
| Campo desconhecido ignorado em silêncio | `fail-on-unknown-properties: true` (seção 2) |
| `String` de entrada sem `@Size` | Teto em todo texto (seção 2) |
| `log.info("…{}", recordOuObjetoComDadoSensivel)` | `toString()` mascarado ou logar só identificadores (seção 5) |
| `show-sql` / `…descriptor.sql=TRACE` fora de depuração local | Desligados — imprimem valores vinculados (seção 5) |
| `getMessage()` de `DataIntegrityViolationException` no log | Classe + nome da restrição (seção 5) |
| Consulta montada por concatenação | Parâmetro vinculado (seção 7) |
| `Sort`/`Pageable` recebido do cliente | `ordenacao` como enum fechado (seção 7) |
| Cabeçalho de segurança por controller | Um `Filter` mais externo (seção 6) |
| CSP restritiva aplicada ao Swagger/H2 Console | Filtro só em `/api/*` (seção 6) |
| `allowedOrigins("*")` em produção | Origens explícitas por configuração; padrão fechado (seção 8) |
| Tratar a tabela de riscos como "resolvida" | Declarar no PR o que ficou de fora (seção 9) |

---

## Checklist

Antes de abrir um PR que mexe em configuração, entrada, log ou erro:

- [ ] Nenhum segredo real entrou em código, `application*.yml`, teste ou log?
- [ ] O `/h2-console` e o Swagger estão desabilitados em `application-prod.yml`?
- [ ] Toda `String` de entrada nova tem `@Size`, e o número de cartão é `String`?
- [ ] Nenhuma `Mensagem*` de entrada tem campo de saldo, e `fail-on-unknown-properties` continua ligado?
- [ ] Nenhum log contém número de cartão, documento ou telefone — nem por `toString()` de `record`/Lombok?
- [ ] Erro de persistência é logado só com classe e nome da restrição, sem `getMessage()` do banco?
- [ ] `show-sql` e o `TRACE` do binder do Hibernate estão desligados?
- [ ] Toda consulta usa parâmetro vinculado, e nenhuma `Sort`/`Pageable` vem do cliente?
- [ ] O `FiltroDeCabecalhosDeSeguranca` continua registrado em `/api/*`, e o teste de contrato confere os cabeçalhos em sucesso **e** erro?
- [ ] CORS continua fechado, ou com origens explícitas vindas de configuração?
- [ ] A tabela de riscos (seção 9) está no PR, com o que ficou de fora e por quê?

---

**[Performance ←](PERFORMANCE.md)**
