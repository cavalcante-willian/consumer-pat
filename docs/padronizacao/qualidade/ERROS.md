[← Índice](../../PADRONIZACAO_INDEX.md) · [← API.md](API.md)

# Erros — Java 17 / Spring Boot 2.7

> **Escopo**: o tratamento de erros em **Java 17 + Spring Boot 2.7**. A tese
> é que erro de domínio é **dado**, nunca exceção lançada. A exceção de
> infraestrutura é convertida em **duas portas** (ver
> [seção 3](#3-proibição-de-captura-genérica--e-as-duas-portas-de-conversão))
> e os códigos de erro são garantidos por um **teste**, não por um arquivo de
> registro (ver [seção 8](#8-código-de-erro-legível-e-verificado-por-teste)).
>
> **Nota sobre exemplos**: os trechos de código abaixo são ilustrativos —
> a regra normativa é o texto conceitual, não os nomes exatos exibidos.
>
> **Documento relacionado**: [`VALIDACAO.md`](VALIDACAO.md).

Define hierarquia de erro tipada por domínio de falha, proibição de códigos
mágicos e de captura genérica.

## Sobre Este Documento

Erro de domínio é **dado**, nunca exceção lançada. `ErroDominio` é uma
interface implementada por `record`s que fluem como valor dentro de
`Resultado<T, E>` (ver [`CONDICIONAIS.md`](../codigo/CONDICIONAIS.md),
nível 4) — nunca um tipo que herda de `Exception`, nunca algo pego com
`catch` ao longo do domínio.

**Por quê isso, e não exceção lançada/capturada:**

- **A assinatura mente.** Exceção não verificada (`RuntimeException`) não
  aparece no tipo de retorno: o chamador só descobre que `debitar` pode
  falhar abrindo o corpo — a mesma bifurcação invisível que o `CONDICIONAIS.md`
  existe para eliminar. Exceção verificada (`checked`) aparece, mas contamina
  toda lambda e todo `Stream`.
- **Custo e acoplamento.** Herdar de `Exception` importa `getMessage()`,
  `getStackTrace()`, `getCause()` e o preenchimento da pilha em tempo de
  execução — custo pago a cada "saldo insuficiente", que não é falha
  excepcional, é um resultado esperado do negócio.
- **Controle de forma.** Um `record` próprio tem campos nomeados no
  vocabulário do domínio, em português, sem membro herdado em inglês.

Exceção real continua existindo — só não é erro de domínio: é o que a
própria infraestrutura (JPA/Hibernate, driver, sistema de arquivos)
genuinamente lança. Ela é tratada **na fronteira**, por uma de duas portas
(seção 3), e nunca atravessa o domínio.

---

## 1. Erros Sempre Tipados

Toda operação falível de domínio retorna `Resultado<T, E>` — nunca valor
sentinela (`-1`, `0`), nunca `null`, nunca uma exceção de domínio lançada e
capturada.

```java
// CORRETO — ausência é um estado explícito, no tipo de retorno
Resultado<Cartao, ErroDominio> buscarPorNumeroETipoParaAtualizacao(NumeroDeCartao numero, TipoCartao tipo);

// ERRADO — retorna null se não encontrado, sem dizer isso na assinatura
// (foi o legado: findByFoodCardNumber devolvia null e o chamador chamava
// consumer.setFoodCardBalance(...) → NullPointerException → HTTP 500)
Consumer findByFoodCardNumber(int cardNumber);
```

**Por quê:** um retorno nulo não documentado na assinatura obriga o
chamador a abrir o corpo do método para saber se `null` é um caso
possível. No legado, cada um dos três `findBy…` do `setBalance` podia
devolver `null`, e o último ramo nem checava.

**Erro de programação não é erro de domínio.** `NullPointerException`,
`IllegalArgumentException` de `Objects.requireNonNull` ou
`IllegalStateException` são **bugs**: não viram `ErroDominio`, não são
"tratados" — propagam até a última rede (seção 3, porta 2) e viram 500
com `idRequisicao` para diagnóstico.

---

## 2. Hierarquia de Erros por Domínio

Erros são organizados por **categoria de falha** — não por camada técnica.
`ErroDominio` é a raiz; cada categoria é uma `sealed interface` própria; e
cada caso concreto é um `record` nomeado pela situação que ocorreu.

```java
// dominio/erros/ErroDominio.java — contrato mínimo de todo erro de domínio: só compõe os papéis,
// cada um em seu arquivo (uma peça por arquivo, ver ESTRUTURA.md)
public interface ErroDominio extends ComCategoria, ComCodigo, ComMensagem, ComCamposComErro { }

// dominio/erros/ComCategoria.java
public interface ComCategoria { CategoriaErro categoria(); }

// dominio/erros/ComCodigo.java
public interface ComCodigo { String codigo(); }       // ver seção 8

// dominio/erros/ComMensagem.java
public interface ComMensagem { String mensagem(); }   // texto para humano; nunca com dado sensível completo

// dominio/erros/ComCamposComErro.java — campos da fronteira a que o erro se refere (corpo `errors` do 400)
public interface ComCamposComErro {
    default Map<String, List<String>> camposComErro() { return Map.of(); }   // vazio quando não há campo; VALIDACAO.md §3
}

// dominio/erros/naoencontrado/ErroNaoEncontrado.java — a categoria dá o status uma vez só
public sealed interface ErroNaoEncontrado extends ErroDominio
        permits ErroCartaoNaoEncontrado, ErroConsumidorNaoEncontrado {

    @Override
    default CategoriaErro categoria() { return CategoriaErro.NAO_ENCONTRADO; }
}

// dominio/erros/naoencontrado/ErroCartaoNaoEncontrado.java — caso concreto carrega os dados do caso
public record ErroCartaoNaoEncontrado(NumeroDeCartao numero, TipoCartao tipo) implements ErroNaoEncontrado {
    public static final String CODIGO = "CPT-NEN-CRT-001";

    @Override public String codigo() { return CODIGO; }
    @Override public String mensagem() {
        // NumeroDeCartao.toString() já é a forma mascarada: a mensagem nunca mostra o número inteiro
        return "Não existe cartão " + tipo + " com o número " + numero + ".";
    }
}

// ERRADO — herdar de Exception amarra o erro ao throw/catch, à stack trace
// e ao tratamento de exceção do framework
public class ErroDominioErrado extends RuntimeException {
    public ErroDominioErrado(String mensagem) { super(mensagem); }
}
```

Categorias do projeto (lista de exemplo, não fechada — categoria nova nasce
quando um tipo novo de falha aparecer):

| Categoria (`sealed interface`) | `CategoriaErro` | Status | Exemplos |
|---|---|---|---|
| `ErroNaoEncontrado` | `NAO_ENCONTRADO` | 404 | `ErroCartaoNaoEncontrado`, `ErroConsumidorNaoEncontrado` |
| `ErroRegraDeNegocio` | `REGRA_DE_NEGOCIO` | 422 | `ErroSaldoInsuficiente` |
| `ErroConflito` | `CONFLITO` | 409 | `ErroDocumentoJaCadastrado`, `ErroVersaoDivergente`, `ErroNumeroDeCartaoJaCadastrado` |
| `ErroValidacao` | `VALIDACAO` | 400 | `ErroTipoEstabelecimentoInvalido` |
| `ErroIndisponivel` | `INDISPONIVEL` | 503 | `ErroBancoIndisponivel` (`CPT-BAN-SIS-001`) |

> **`ErroDominio` não é `sealed`.** Em Java, os tipos permitidos por uma
> `sealed interface` precisam estar **no mesmo pacote** (num projeto sem
> `module-info`). Como cada categoria mora em seu subpacote (para não passar
> de uma dúzia de arquivos numa pasta plana — ver
> [`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md)), a raiz é uma `interface`
> comum e o fechamento acontece **por categoria**. Nada no projeto faz
> `switch` sobre `ErroDominio`: o que decide é `categoria()` (dado), então
> a exaustividade da raiz não é necessária.

**Por quê:** cada categoria dá nome de domínio à falha, e **o status HTTP é
dado da categoria** (`CategoriaErro.statusHttp()`, nível 1 de
`CONDICIONAIS.md`) — quem recebe o erro decide por tipo ou por categoria,
nunca por string de mensagem comparada.

---

## 3. Proibição de Captura Genérica — e as Duas Portas de Conversão

Nunca capture `Exception`, `RuntimeException`, `Throwable` ou qualquer
base sem tipo específico. Toda captura ou tratamento é do tipo exato que a
infraestrutura genuinamente lança.

A regra ideal seria capturar **uma vez**, no adaptador, e converter em
`ErroDominio`. Com **Spring + JPA transacional isso não funciona sempre**, e
a regra foi dividida em duas portas, escolhidas pelo **momento em que o erro
nasce**:

### Por que uma porta só não basta

1. **O erro nasce tarde.** O Hibernate adia a gravação até o `flush`, que
   acontece no **commit** da transação — depois que o método do caso de uso
   já retornou. `DataIntegrityViolationException` (documento duplicado) e
   `ObjectOptimisticLockingFailureException` (versão divergente) saem do
   *proxy* transacional, **fora** de qualquer `try` que o adaptador tenha.
2. **Capturar dentro da transação a envenena.** Uma exceção de persistência
   lançada dentro de `@Transactional` marca a transação como
   *rollback-only*. Se o adaptador a captura e devolve `Resultado.Falha`
   normalmente, o commit seguinte lança `UnexpectedRollbackException`.
   *(Comportamento conhecido do Spring; confirmar com teste ao implementar.)*

**Regra decorrente:** nenhum `catch` de exceção de persistência dentro de
código que roda sob `@Transactional`.

### Porta 1 — Verificação explícita no adaptador (erro **previsível**)

O que o domínio **consegue prever** antes de gravar vira **consulta
explícita** e devolve `ErroDominio` — sem exceção, sem `catch`:

```java
// infraestrutura/persistencia/consumidor/ConsumidorAdaptador.java
@Override
public Resultado<Consumidor, ErroDominio> inserir(Consumidor consumidor) {
    // guard (nível 11): documento duplicado é pré-condição de domínio sim/não
    if (repositorio.existsByDocumento(consumidor.documento()))
        return new Falha<>(new ErroDocumentoJaCadastrado(consumidor.documentoMascarado()));

    return new Sucesso<>(mapeador.paraDominio(repositorio.save(mapeador.paraEntidade(consumidor))));
}
```

Aplica-se a: documento já cadastrado, número de cartão já cadastrado,
`versao` enviada no `PUT` diferente da atual — todos viram `ErroConflito`
(409) com **mensagem útil**, não um "conflito" genérico.

> **Limite honesto:** "consultar e depois inserir" tem janela — duas
> requisições simultâneas com o mesmo documento passam as duas pela
> consulta. A restrição `unique` no banco é a **rede de segurança**, e quem
> a dispara cai na porta 2. A porta 1 existe para o caso comum, que merece
> mensagem boa; a porta 2, para o caso raro, que merece 409 correto.

### Porta 2 — `@ExceptionHandler` tipado na API (erro que só existe no commit)

O que **só se sabe na hora do commit** — ou que é genuinamente
excepcional — **não é capturado**: atravessa até `FiltroDeProblemaDeApi`
(`@RestControllerAdvice`), que tem **um `@ExceptionHandler` por tipo
exato**:

| Exceção (tipo exato) | Resposta | Código |
|---|---|---|
| `DataIntegrityViolationException` | 409 | `CPT-CNF-SIS-001` |
| `ObjectOptimisticLockingFailureException` | 409 | `CPT-CNF-SIS-001` |
| `PessimisticLockingFailureException` | 409 (tente novamente) | `CPT-CNF-SIS-002` |
| `MethodArgumentNotValidException`, `ConstraintViolationException`, `HttpMessageNotReadableException` | 400 | `CPT-VAL-SIS-001` |
| `NoHandlerFoundException` | 404 | `CPT-NEN-SIS-001` |
| `HttpRequestMethodNotSupportedException` | 405 (com `Allow`) | `CPT-VAL-SIS-002` |
| `HttpMediaTypeNotSupportedException` | 415 | `CPT-VAL-SIS-003` |
| `MethodArgumentTypeMismatchException` (id da rota que não é número) | 400, com o parâmetro apontado | `CPT-VAL-SIS-001` |
| `Exception` (o único handler que a captura) | 500, sem a causa no corpo | `CPT-INT-SIS-001` |

Cada linha é um papel em `api.comum.tratamento` (`TraducaoDe*`), e
`FiltroDeProblemaDeApi` só os declara. O texto das bibliotecas nunca chega
ao cliente: os campos do 400 vêm de `api.comum.campos`, que dá só o nome do
campo do JSON e uma mensagem em português. O `DataIntegrityViolationException`
só ganha handler junto com o consumidor, que é quem tem unicidade a violar.

```java
// CORRETO — um handler por tipo exato; converte, loga com contexto e causa, não vaza internals
@ExceptionHandler(DataIntegrityViolationException.class)
ResponseEntity<MensagemProblema> aoViolarIntegridade(DataIntegrityViolationException e, HttpServletRequest requisicao) {
    // a mensagem do banco traz o VALOR duplicado (documento, número de cartão):
    // logar só a restrição violada, nunca getMessage() nem a exceção (SEGURANCA.md §5)
    log.warn("violação de integridade operacao={} restricao={}", requisicao.getRequestURI(), nomeDaRestricao(e));
    return ProblemaDeBorda.CONFLITO_DE_PERSISTENCIA.paraResposta(requisicao);
}

// ERRADO — captura genérica relançando outra exceção: nenhuma das duas é
// código de domínio, e o chamador não tem como tratar por tipo
try { repositorio.save(consumidor); }
catch (Exception e) { throw new RuntimeException("falha no banco", e); }
```

**A última rede.** Existe **exatamente um** `@ExceptionHandler(Exception.class)`
no projeto — a falha inesperada (bug, `NullPointerException`, banco fora do
ar). É a **única** exceção declarada à proibição de captura genérica: ele
não trata, não decide nada; só **registra a pilha completa, sem a mensagem da
exceção** (que pode trazer SQL ou um valor vinculado, e nada no log a
mascara) e devolve 500 genérico com `idRequisicao`. Um segundo handler genérico, em qualquer outro
lugar, é rejeitado em revisão.

### Outras proibições (Java)

- `catch (Exception | Throwable | RuntimeException e)` fora da última rede.
- `catch` vazio, ou que só faz `e.printStackTrace()`.
- `@SneakyThrows` (Lombok) — esconde exceção verificada sem tratá-la.
- Exceção **verificada** em código novo; as que as APIs impõem são
  convertidas na hora, na borda (ex.: `IOException` num adaptador de
  arquivo).
- `multi-catch` (`catch (A | B e)`) só entre **tipos específicos** e que
  recebem o **mesmo** tratamento.

**Por quê:** `catch (Exception)` não diz qual falha aconteceu — some a
distinção entre "versão divergente" (negócio, 409) e "banco fora do ar"
(infraestrutura, 500) exatamente no ponto em que ela precisa existir.

---

## 4. Nunca Descarte a Informação Original

Ao tratar uma exceção de infraestrutura, a **causa original** vai para o
**log**, com a pilha — **nunca** para o corpo da resposta, e **nunca**
descartada:

```java
// CORRETO — a exceção vai como ÚLTIMO argumento: o Log4j2 registra a pilha completa
log.error("falha inesperada operacao={} idRequisicao={}", operacao, idRequisicao, e);

// ERRADO — só a mensagem; a pilha e a causa-raiz se perdem
log.error("falha: " + e.getMessage());

// ERRADO — causa para o cliente: vaza SQL, nome de tabela, constraint
return ResponseEntity.status(409).body(new MensagemProblema(/* ... */ e.getMessage() /* ... */));
```

**Exceção a esta regra — a causa que carrega o dado sensível.** A mensagem
de uma `DataIntegrityViolationException` inclui o **valor** que colidiu
(o H2 escreve `... VALUES ('5555000011112222')`); passá-la como último
argumento grava o número do cartão ou o documento no log, em texto simples.
Para exceções de persistência desse tipo, o log leva **a classe e o nome da
restrição** (`uk_cartoes_numero`), nunca `getMessage()` nem a exceção
inteira. A regra de [`SEGURANCA.md`](SEGURANCA.md#5-logs-sem-dados-sensíveis)
prevalece sobre a de "pilha completa" sempre que houver conflito.

Para erro de domínio produzido pela porta 1 não existe causa de
infraestrutura: o dado do caso (`numeroMascarado`, `tipo`) **é** o
diagnóstico.

**Por quê:** "algo falhou" é exatamente o tipo de erro que não dá pista de
por onde procurar — e o `idRequisicao` só ajuda o operador se o log tiver a
pilha.

---

## 5. Fronteira da API Traduz Para o Corpo de Erro Padrão

`Resultado<T, E>` flui até a borda da API **sem conversão**; o mapeamento
`ErroDominio` → resposta HTTP é **único e central**, nunca `catch` em
controller nem `status` escolhido dentro de cada um. A tabela completa vive
em [`API.md`](API.md#3-erro-padronizado-rfc-7807).

```java
// api/comum/resposta/RespostaDeResultado.java — o único ponto que traduz domínio em HTTP
public static <T> ResponseEntity<Object> responder(
        Resultado<T, ErroDominio> resultado, Function<T, ?> paraMensagem, HttpStatus aoSucesso) {

    return resultado.<ResponseEntity<Object>>resolver(
        valor -> ResponseEntity.status(aoSucesso).body(paraMensagem.apply(valor)),
        erro  -> ProblemaDeErro.paraResposta(erro));   // status = erro.categoria().statusHttp()
}
```

Sem `switch`, sem `if`, sem `instanceof`: `resolver` é o despacho
polimórfico de `Sucesso`/`Falha` (nível 4 de `CONDICIONAIS.md`). Um erro
novo **não exige alterar** este método — só ser criado numa categoria.

**Por quê:** nenhum controller decide o próprio status — o legado tratava
tudo como 200 ou 500.

---

## 6. Validação de Entrada vs. Erro de Domínio

Erros de **validação de borda** (entrada externa malformada: campo
obrigatório ausente, valor não positivo, e-mail inválido) são tratados pelo
**Bean Validation** nas `Mensagem*`, antes do caso de uso, e **não** são
`ErroDominio`: chegam ao advice como `MethodArgumentNotValidException` e
viram 400 com `errors` campo a campo.

Erros de **domínio** (regra de negócio violada) são variantes de
`ErroDominio` e viram o status da sua categoria. Nunca misture os dois na
mesma variante — campo obrigatório ausente não é a mesma categoria de falha
que "saldo insuficiente".

**Caso de fronteira:** `tipoEstabelecimento = 99` passa pelo Bean Validation
(é um `Integer` não nulo) e só o domínio sabe que não existe tipo 99. Por
isso `ErroTipoEstabelecimentoInvalido` é `ErroDominio` da categoria
`ErroValidacao` (400) — **validação que só o domínio consegue fazer**, não
a validação de formato da borda. Esse erro **sobrescreve `camposComErro()`**
com o campo da fronteira (`tipoEstabelecimento`), de modo que o `400` sai
**idêntico** ao da borda — o cliente não distingue quem recusou. A divisão
completa (borda, domínio-forma, estado) está em
[`VALIDACAO.md`](VALIDACAO.md#3-borda-domínio-e-estado--três-perguntas).

---

## 7. Log de Erro Estruturado

Todo tratamento de exceção de infraestrutura é logado com **contexto
estruturado** (operação, identificadores, causa), nunca apenas a mensagem
solta. Sem biblioteca de log JSON (proibido adicionar dependência), a forma
estruturada é **pares `chave=valor`** na mensagem, mais o `idRequisicao` já
presente no `MDC` (ver [`API.md`](API.md#4-pipeline-de-requisição)):

```java
// CORRETO — chave=valor, identificadores, e a exceção como último argumento (pilha)
log.warn("conflito de versão operacao=atualizar-consumidor consumidorId={} versaoEnviada={}", id, versao, e);

// ERRADO — mensagem solta, sem contexto
log.warn(e.getMessage());
```

### Dado sensível nunca entra no log nem na mensagem

Número de cartão, documento e telefone são **dado pessoal**. Em
`mensagem()` de erro, em `detail` da resposta e em linha de log, aparecem
**mascarados** (apenas os 4 últimos dígitos: `****2222`). Por isso as
variantes carregam `numeroMascarado`/`documentoMascarado`, e a máscara é
aplicada **onde o erro nasce**, não por quem o formata — assim nenhum
consumidor posterior consegue vazar o valor completo por esquecimento.

---

## 8. Código de Erro Legível e Verificado Por Teste

Toda variante concreta de `ErroDominio` e todo problema de borda carregam um
**código estável**, no formato:

```text
CPT-<CATEGORIA>-<ÁREA>-<SEQUENCIAL>
```

- **`CPT`** — prefixo do projeto (`consumer-pat`), para que o código seja
  reconhecível num log ou ticket.
- **`<CATEGORIA>`** — 3 letras, a natureza da falha: `VAL` validação, `NEN`
  não encontrado, `REG` regra de negócio, `CNF` conflito, `BAN`
  banco/infraestrutura. **Lista de exemplo, não fechada.**
- **`<ÁREA>`** — 3 letras, onde o erro nasce: `CSM` consumidor, `CRT`
  cartão, `EXT` extrato, `SIS` sistema/borda quando não há área de domínio.
  **Também aberta** — conceito novo no domínio é área nova.
- **`<SEQUENCIAL>`** — 3 dígitos, desambigua dois erros da mesma
  categoria+área. **Nunca reaproveitado**: se uma variante é removida, o
  sequencial dela não volta.

```java
// CORRETO — código como constante no próprio tipo; vira o `type` do corpo de erro:
// "urn:consumer-pat:erro:CPT-REG-CRT-001"
public record ErroSaldoInsuficiente(Dinheiro saldo, Dinheiro valor)
        implements ErroRegraDeNegocio {
    public static final String CODIGO = "CPT-REG-CRT-001";
    @Override public String codigo() { return CODIGO; }
    // ...
}
```

Os problemas de **borda** (que não passam por `ErroDominio`) são **dado** de
um `enum` em `api.comum.problema` (nível 1 de `CONDICIONAIS.md`): código,
título e status juntos, de modo que esquecer um deles não compila.

```java
public enum ProblemaDeBorda {
    ENTRADA_INVALIDA("CPT-VAL-SIS-001", "Entrada inválida", 400),
    CONFLITO_DE_CONCORRENCIA("CPT-CNF-SIS-001", "Conflito de concorrência", 409),
    // ... METODO_NAO_SUPORTADO 002/405, MIDIA_NAO_SUPORTADA 003/415,
    // ROTA_NAO_ENCONTRADA NEN-001/404, RECURSO_EM_USO CNF-002/409,
    // FALHA_INESPERADA INT-001/500
}
```

### Sem arquivo de registro — o teste é o registro

Registrar cada código num arquivo à parte foi **descartado** (decisão em
[`DECISOES.md`](../fundamentos/DECISOES.md)): com poucos erros, um
arquivo à parte é custo sem ganho, e **um registro manual envelhece**. A
garantia mecânica vem de um teste que **percorre a hierarquia**:

```java
// src/test/.../dominio/erros/CodigosDeErroTeste.java — esboço; validar ao implementar
class CodigosDeErroTeste {

    @Test
    void deve_ter_codigo_unico_e_no_formato_quando_percorrer_todas_as_variantes() {
        var codigos = Stream.of(ErroNaoEncontrado.class, ErroRegraDeNegocio.class, ErroConflito.class, ErroValidacao.class)
            .flatMap(CodigosDeErroTeste::folhas)               // getPermittedSubclasses() — Java 17
            .map(CodigosDeErroTeste::codigoDe)                 // lê a constante estática CODIGO
            .collect(Collectors.toList());

        assertEquals(codigos.size(), Set.copyOf(codigos).size());            // nenhum repetido
        assertTrue(codigos.stream().allMatch(c -> c.matches("CPT-[A-Z]{3}-[A-Z]{3}-\\d{3}")));
    }
    // + o mesmo para ProblemaDeBorda.values()
}
```

O `sealed` ajuda aqui: `getPermittedSubclasses()` enumera as variantes sem
varrer o classpath. Variante nova sem `CODIGO`, com código repetido ou fora
do formato **quebra o build** — o que o registro em Markdown só prometia.

**Por quê:** o formato (categoria + área) já reduz onde procurar antes de
abrir qualquer código — e quem vê o código num log, numa tela de suporte ou
num ticket não deveria precisar abrir o repositório para saber a natureza
dele.

---

## Anti-Padrões

| Anti-padrão | Correto |
|---|---|
| `ErroDominio` herdando de `Exception`/`RuntimeException` | `record` implementando a interface da categoria — dado puro (seção 2) |
| Repositório devolvendo `null` para "não encontrado" | `Resultado<T, ErroDominio>` com `ErroNaoEncontrado` (seção 1) |
| `throw`/`catch` como controle de fluxo de domínio | `new Falha<>(...)` / `.resolver(...)` (seção 1) |
| `catch (Exception)` / `Throwable` / `RuntimeException` | Tipo exato; ou `@ExceptionHandler` por tipo; só **uma** última rede (seção 3) |
| `catch` de exceção de persistência **dentro** de `@Transactional` devolvendo `Falha` | Verificação explícita antes (porta 1) ou deixar subir até o advice (porta 2) |
| Esperar o banco falhar para descobrir documento duplicado | `existsBy…` antes, com erro tipado; `unique` só como rede de segurança |
| `catch` vazio, `printStackTrace()`, `@SneakyThrows` | Tratar tipado ou propagar |
| `NullPointerException`/`IllegalArgumentException` tratada como erro de domínio | É bug: propaga e vira 500 com `idRequisicao` |
| `log.error(e.getMessage())` | Contexto `chave=valor` + exceção como último argumento (pilha) |
| `e.getMessage()` no corpo da resposta | `detail` genérico; causa só no log |
| Número de cartão/documento completo em mensagem, `detail` ou log | Mascarado onde o erro nasce (seção 7) |
| `status` escolhido em controller | `CategoriaErro.statusHttp()` + `RespostaDeResultado` (seção 5) |
| Validação de borda e erro de domínio na mesma variante | Bean Validation na borda; `ErroDominio` no domínio (seção 6) |
| Variante nova sem `CODIGO`, ou com código repetido | `CPT-<CAT>-<ÁREA>-<SEQ>`; o `CodigosDeErroTeste` falha o build (seção 8) |
| Registro de códigos em arquivo Markdown mantido à mão | O teste percorre a hierarquia (seção 8) |
| Sequencial reaproveitado de variante removida | Sequencial nunca volta a ser usado |
| Mais de um `@ExceptionHandler(Exception.class)` | Exatamente um, na última rede |

---

## Checklist

- [ ] Toda função falível de domínio retorna `Resultado<T, E>`, sem `null` nem exceção de domínio?
- [ ] Os `ErroDominio` são `record`s de uma categoria, nenhum herdando de `Exception`?
- [ ] Nenhum `catch` genérico fora da última rede (um único `@ExceptionHandler(Exception.class)`)?
- [ ] Nenhum `catch` de exceção de persistência dentro de código `@Transactional`?
- [ ] O que é previsível (duplicidade, versão) tem verificação explícita e erro tipado, e a restrição `unique`/`@Version` está lá como rede de segurança?
- [ ] O que só existe no commit tem `@ExceptionHandler` por tipo exato, com a causa no log?
- [ ] O log tem contexto `chave=valor` e a exceção como último argumento?
- [ ] Nenhum dado sensível completo (cartão, documento) em mensagem, `detail` ou log?
- [ ] O status HTTP vem de `CategoriaErro`, sem escolha em controller?
- [ ] Erro de validação de borda e erro de domínio estão em categorias distintas?
- [ ] Toda variante nova tem `CODIGO` no formato `CPT-<CAT>-<ÁREA>-<SEQ>` e o `CodigosDeErroTeste` passa?
- [ ] O sequencial novo não colide com nenhum da mesma categoria+área?

---

**[API ←](API.md)**
