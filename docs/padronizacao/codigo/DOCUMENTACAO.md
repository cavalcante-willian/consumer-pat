[← Índice](../../PADRONIZACAO_INDEX.md) · [← CONDICIONAIS.md](CONDICIONAIS.md)

# Documentação de Código — Java 17

> **Escopo**: o princípio, o escopo e as regras de revisão da documentação
> de código do projeto, em **Javadoc**, com a forma de impor a regra no
> build. Os exemplos usam o domínio de cartões de benefício. As
> particularidades do formato estão em [Formato Javadoc](#formato-javadoc).
>
> **Nota sobre exemplos**: os trechos de código abaixo são ilustrativos —
> a regra normativa é o texto conceitual, não os nomes ou estruturas
> exibidos.

## Sobre Este Documento

Define o **escopo, formato e profundidade** da documentação de código
exigida no projeto, via **Javadoc** (`/** ... */`, gerado pela tarefa
`javadoc` do Gradle) — o único sistema de documentação de código.

**Objetivo**: que qualquer leitor — colaborador novo, revisor, futuro eu —
entenda **o que** o elemento faz e **por que** existe, sem precisar abrir
a implementação. É a mesma tese de [`CONDICIONAIS.md`](CONDICIONAIS.md): o
projeto é lido por humanos, e documentação é o que permite ler sem
executar o código mentalmente.

---

## Formato Javadoc

| Tema | Regra no Java 17 |
|---|---|
| Formato | Javadoc (`/** */`, `{@code}`, `@param`, `{@link}`) |
| Marcação | **HTML** simples dentro do Javadoc. Markdown em Javadoc (`///`) só existe a partir do JDK 23 — **não usar** no Java 17 |
| Falha de build por doc ausente | `-Xdoclint:missing` + `-Werror` no `javac` e `-Xwerror` na tarefa `javadoc` (ver [Ferramentas](#ferramentas--javadoc)) |
| Doc de pacote | `package-info.java` por pacote |
| Referência a outro membro | `{@link X}` / `{@link X#metodo(Tipo)}` |
| Exceção documentada | `@throws` |
| Exemplo | `<pre>{@code ...}</pre>` (não executa) — o exemplo que importa vira **teste** |
| Getter trivial | gerado por Lombok / método sobrescrito: sem doc (herdado ou gerado) |

---

## Princípio

A documentação é **parte do contrato**, não um anexo opcional:

- Item público sem Javadoc = **revisão rejeita** (e o build falha, ver
  [Ferramentas](#ferramentas--javadoc)).
- Método público sem descrição dos **casos de ausência/erro** possíveis =
  revisão rejeita.
- Decisão não óbvia (por que 10% de desconto, por que 35% de acréscimo,
  workaround de comportamento de um provedor de banco) sem explicação =
  revisão pede ajuste.
- Comentário que só parafraseia o nome do item = revisão pede remoção.
- Documentação que **descreve comportamento antigo** = pior que ausência
  de documentação — atualiza ou remove.

Um nome bem escolhido já diz **o quê**. Comentário e Javadoc existem para
o que o nome não consegue carregar: uma invariante, uma decisão numérica,
um contrato de posse de recurso, um caso de borda.

```java
// PROIBIDO — só repete o nome do método
/** Debita o cartão. */
Resultado<Cartao, ErroDominio> debitar(Dinheiro valor);

// CORRETO — carrega o contrato que o nome não expressa
/**
 * Debita {@code valor} do saldo e devolve o cartão com o novo saldo,
 * sem alterar esta instância (o cartão é imutável).
 *
 * <p>O valor recebido já deve ter o ajuste do tipo de cartão aplicado
 * (ver {@link TipoCartao#valorCobrado(Dinheiro)}) — este método não
 * recalcula desconto nem acréscimo, apenas verifica o valor e o saldo.
 *
 * @param valor valor final a debitar; precisa ser maior que zero e não
 *              passar do saldo
 * @return o cartão com o saldo reduzido, ou falha:
 *         {@link ErroValorNaoPositivo} para zero ou negativo, e
 *         {@link ErroSaldoInsuficiente} quando {@code valor} excede o
 *         saldo — saldo negativo nunca é permitido
 */
Resultado<Cartao, ErroDominio> debitar(Dinheiro valor);
```

---

## Ferramentas — Javadoc

Nenhuma dependência além do toolchain padrão do JDK e do Gradle é
necessária — `javadoc` e `doclint` vêm com o JDK, então a regra "sem
dependência nova" do projeto é respeitada.

```groovy
// build.gradle — resumo; a configuração completa e a justificativa estão em
// qualidade/BUILD.md §5 (proposta: validar ao aplicar, ainda não executada)
tasks.named('compileJava') {
    // doc ausente em membro público é diagnóstico do javac — só no código de
    // produção: um método de teste não tem Javadoc a exigir
    options.compilerArgs += ['-Xdoclint:missing']
}

tasks.named('javadoc') {
    // ...e HTML inválido ou @param/@return incoerente falha a geração
    options.addBooleanOption('Xwerror', true)
}

// por padrão o `build` do Gradle NÃO executa o javadoc: sem esta linha,
// "Javadoc inválido" não quebraria nada
tasks.named('check') { dependsOn 'javadoc' }
```

`-Xlint:all` e `-Werror` valem para todo o código (produção e testes) e estão
em [`BUILD.md`](../qualidade/BUILD.md#5-warnings-tratados-como-erros).

```bash
# gera a documentação em build/docs/javadoc; doc ausente/inválida falha o build
./gradlew javadoc
```

> **Ressalva**: `-Werror` falha o build por **qualquer** aviso do
> compilador, não só os de documentação. Se o ruído de um processador de
> anotação (Lombok, Spring) for **comprovado**, a única supressão global
> admitida é excluir **aquela categoria, nominalmente**, no `build.gradle`,
> com o comentário que prova o ruído — ver
> [`BUILD.md`](../qualidade/BUILD.md#5-warnings-tratados-como-erros). Remover
> o `-Xlint:all` inteiro "para o build passar" não é a saída.

**`{@code}` em Javadoc não executa**: o exemplo fica ilustrativo e pode
ficar desatualizado silenciosamente, sem quebrar o build — por isso a regra
de [Exemplos Viram Teste](#exemplos-são-ilustrativos-no-javadoc--viram-teste):
comportamento que importa vira teste real em `src/test`, não só exemplo no
Javadoc.

**Formato do primeiro período**: o Javadoc usa a **primeira frase** (até o
primeiro ponto seguido de espaço) como resumo em listagens. Ela precisa
ser completa e terminar com ponto — não coloque abreviações com ponto
("etc.", "p. ex.") antes do fim da primeira frase.

---

## Árvore de Decisão: Documentar?

```text
Estou criando ou alterando um elemento
│
├─ É público (tipo/membro `public`, `protected`, ou pacote)?
│   ├─ Não ──> Tem regra de negócio, invariante ou decisão não óbvia?
│   │          ├─ Não ──> sem doc — nome claro basta
│   │          └─ Sim ──> comentário `//` curto, explicando o porquê
│   │
│   └─ Sim ──> É getter/setter gerado (Lombok), `record` de transporte sem
│              regra, método com `@Override` que não muda o contrato, ou
│              DTO de serialização sem regra própria?
│               ├─ Sim ──> sem doc — o nome é o contrato (ou `{@inheritDoc}`)
│               └─ Não ──> Javadoc completo obrigatório: resumo, casos de
│                          ausência/erro, posse de recurso, `@throws` quando
│                          lança de verdade
```

---

## Escopo Obrigatório

| Elemento | Obrigatório |
|---|---|
| Tipos e membros públicos de `dominio` e `aplicacao` | **SIM** |
| Cada variante da hierarquia de erro (`ErroDominio` e filhos) | **SIM** — quando ocorre e o que o chamador deve fazer (ver `ERROS.md`) |
| `interface` de porta (`dominio.portas`) | **SIM**, com ênfase em qual contrato todo adaptador concreto precisa cumprir |
| Implementação de porta que toca API externa (JPA/Hibernate, cliente HTTP, disco) | **SIM**, documentando o comportamento esperado da API e o que fazer quando ela falhar (ver `BANCO.md`, `ERROS.md`) |
| Casos de uso de `aplicacao` | **SIM** — pré-condições, quais erros retorna, efeitos colaterais, fronteira transacional |
| Endpoint da API (controller) e mensagem de entrada/saída | **SIM** — caminho, verbo, status possíveis e o corpo de erro de cada um |
| `package-info.java` | **SIM** — em todo pacote público de `dominio` e `aplicacao` |
| Membro `private`/package-private com regra ou invariante não óbvia | **SIM** (comentário `//` ou Javadoc curto) |
| Campo que guarda dependência injetada central | **SIM** |
| Constante de domínio com decisão numérica por trás (limite, taxa, fator) | **SIM** — por que aquele número, não só o que ele é |
| Getter/setter gerado por Lombok | **proibido documentar o gerado** — documente o **campo**; o Lombok copia o Javadoc do campo para o getter |
| `record` de transporte sem regra própria | opcional |
| Variável local | **proibido** — use bom nome em vez de comentário |
| Comentário que narra o óbvio | **proibido** |

---

## Formato Padrão

A primeira frase começa pelo nome do elemento (tipo) ou pela forma verbal
dele (método) e descreve **contrato e porquê** — não repete a assinatura.

### Doc de Pacote (`package-info.java`)

```java
/**
 * Domínio do projeto — cartões, consumidores, extrato e regras de compra.
 *
 * <p>Nenhum tipo deste pacote é consumido fora de
 * {@code br.com.alelo.consumer.consumerpat.aplicacao} sem passar por um
 * caso de uso — ver {@code ESTRUTURA.md}. Este pacote não depende de Spring
 * nem de JPA.
 */
package br.com.alelo.consumer.consumerpat.dominio;
```

### `interface` de Porta

A primeira frase descreve o **contrato e o papel** — não cada método (eles
têm doc próprio).

```java
/**
 * Contrato implementado por cada persistência concreta de cartões.
 *
 * <p>A camada de aplicação depende apenas desta porta, nunca de uma
 * implementação concreta — permite injetar um fake em teste. A escolha do
 * adaptador acontece na composition root, uma única vez (ver
 * {@code CONDICIONAIS.md}).
 */
public interface CartaoPorta {

    /**
     * Busca o cartão do tipo informado e o <b>trava para escrita</b> até o
     * fim da transação corrente.
     *
     * <p>Deve ser chamado dentro de uma transação — sem ela, a trava não
     * se mantém e dois débitos simultâneos poderiam sobrescrever o saldo
     * um do outro.
     *
     * @param numero número do cartão, já validado em formato
     * @param tipo   tipo esperado; um cartão de outro tipo com o mesmo
     *               número <b>não</b> é devolvido
     * @return o cartão travado, ou falha {@link ErroCartaoNaoEncontrado}
     *         quando não existe cartão com este número e tipo — ausência é
     *         retorno, não exceção
     */
    Resultado<Cartao, ErroDominio> buscarPorNumeroETipoParaAtualizacao(String numero, TipoCartao tipo);
}
```

### Classe e Construtor

A classe descreve **propósito e invariantes**. O construtor descreve o
contrato de criação — o que cada dependência representa e quem é dona
dela.

```java
/**
 * Orquestra o registro de uma compra: resolve o tipo de cartão, aplica o
 * desconto ou acréscimo, debita o saldo e grava o extrato, tudo numa única
 * transação.
 *
 * <p>Não acessa banco diretamente — recebe as portas já injetadas.
 * Construído uma única vez pela composition root, nunca instanciado ad-hoc
 * por chamador individual.
 *
 * <p><b>Exceção consciente à regra de pureza da camada:</b> este caso de
 * uso usa {@code @Transactional} do Spring, pois débito e extrato precisam
 * ser atômicos. A dependência é registrada no {@code README.md} da camada.
 */
public class RegistrarCompraCaso {

    /**
     * Fonte única de leitura/gravação de cartões. Em teste, substituída
     * por um fake em memória — nunca pelo repositório JPA real.
     */
    private final CartaoPorta cartaoPorta;
}
```

### Método

Descreve comportamento e, principalmente, os **casos de ausência/erro** —
cada valor de retorno possível, sem exceção silenciosa.

```java
/**
 * Recupera um consumidor pelo identificador, incluindo seus cartões.
 *
 * <p>Os saldos devolvidos são os do instante da leitura; não há trava —
 * para debitar, use {@link CartaoPorta#buscarPorNumeroETipoParaAtualizacao}.
 *
 * @param id identificador do consumidor
 * @return o consumidor, ou falha {@link ErroConsumidorNaoEncontrado}
 *         quando não existe consumidor com este identificador — ausência
 *         é retorno, não exceção (ver {@code VALORES.md})
 */
Resultado<Consumidor, ErroDominio> buscarPorId(long id);
```

### Variante de Hierarquia de Erro

Cada variante descreve **quando ocorre** e **o que o chamador deve fazer**.

```java
/**
 * Falhas de domínio de cartões e consumidores, agrupadas por categoria
 * (ver {@code ERROS.md}). A raiz não é {@code sealed}: o fechamento
 * acontece em cada categoria, que mora no seu próprio pacote.
 */
public interface ErroDominio {
    CategoriaErro categoria();
    String codigo();
    String mensagem();
}

/**
 * Nenhum cartão existe com o número e o tipo informados. O chamador deve
 * traduzir para 404 na API — nunca para erro de servidor.
 *
 * @param numero número consultado
 * @param tipo   tipo esperado pelo estabelecimento
 */
public record ErroCartaoNaoEncontrado(String numeroMascarado, TipoCartao tipo) implements ErroNaoEncontrado { /* ... */ }

/**
 * O saldo do cartão não cobre o valor cobrado. Repetir a compra sem novo
 * crédito falha da mesma forma. A mensagem não repete o saldo nem o valor:
 * eles vão só para o log.
 *
 * @param saldo saldo no momento da tentativa
 * @param valor valor que não coube no saldo
 */
public record ErroSaldoInsuficiente(Dinheiro saldo, Dinheiro valor) implements ErroRegraDeNegocio { /* ... */ }
```

> Em `record`, os componentes são documentados com `@param` no Javadoc do
> **próprio tipo** — não há campo separado para anotar.

### Implementação de Porta Que Toca API Externa

Documentar o comportamento esperado da API é **obrigatório** aqui — é o
ponto onde o adaptador toca um sistema fora do controle direto do projeto.

```java
/**
 * Persiste cartões via Spring Data JPA sobre o banco configurado.
 *
 * <p><b>Comportamento conhecido da API:</b> a consulta com
 * {@code @Lock(PESSIMISTIC_WRITE)} bloqueia a linha até o fim da transação;
 * se outra transação a segura, a chamada espera até o tempo limite e lança
 * {@code PessimisticLockingFailureException}. Esta classe <b>não</b> captura
 * essa exceção — ela é infraestrutura genuinamente excepcional e é
 * traduzida para 409 pelo filtro de erros da API (ver {@code ERROS.md}).
 */
public class CartaoAdaptador implements CartaoPorta { /* ... */ }
```

### Constante Com Decisão Numérica

```java
// CORRETO — documenta a decisão que o nome não carrega
/**
 * Ajuste do cartão de alimentação: {@code -0.10}, uma fração com sinal.
 *
 * <p>Compras no cartão de alimentação recebem 10% de desconto sobre o valor
 * informado (regra do enunciado). O desconto é aplicado <b>antes</b> do
 * débito, então o extrato guarda o valor já descontado — com a fração de
 * centavo preservada ({@code 33.33 → 29.997000}); arredondar para centavos é
 * decisão da apresentação (ver {@code VALORES.md} §6).
 */
ALIMENTACAO(1, "-0.10"),

// ERRADO — só diz o que o nome já diz
/** Ajuste do cartão de alimentação. */
```

### Exemplos São Ilustrativos no Javadoc — Viram Teste

`{@code}` no Javadoc **não executa** — um exemplo solto pode ficar
desatualizado silenciosamente. Quando o comportamento exemplificado
importa o suficiente para ser verificado, ele vira um teste real em
`src/test/java` (ver `TESTES.md`) e o Javadoc referencia o teste por nome.

```java
/**
 * <pre>{@code
 * var comprovante = caso.registrar(new DtoCompra(1, "Mercado", "123", "Arroz", new BigDecimal("100.00")));
 * // valor debitado: 90.00 (10% de desconto no cartão de alimentação)
 * // casos de borda cobertos em: RegistrarCompraCasoTeste#deve_aplicar_desconto_quando_cartao_de_alimentacao
 * }</pre>
 */
```

---

## O Que **Não** Documentar

| Situação | Correto |
|---|---|
| Getter/setter gerado por Lombok | Sem doc no gerado — documente o campo, se houver regra |
| Variável local com nome descritivo | Sem comentário — use bom nome |
| Método privado que faz exatamente o que o nome diz | Sem doc — o nome basta |
| Método com `@Override` que não altera o contrato | Sem doc — o Javadoc é herdado automaticamente (ou `{@inheritDoc}`) |
| Comentário que repete o nome do identificador | Remover ou reescrever |
| Estrutura de controle óbvia acima de `if` autoexplicativo | Remover — ver [`CONDICIONAIS.md`](CONDICIONAIS.md) |
| Regra que já está descrita em doc de outro membro | Referenciar por `{@link}`, não copiar |

---

## O Que Nunca Vira Comentário

- Referência ao histórico ("adicionado para corrigir o bug #4", "antes
  isso vivia em outro arquivo") — isso é conteúdo de commit/PR, não de
  código; apodrece conforme o código evolui.
- Explicação de sintaxe da própria linguagem ("incrementa `i` em 1").
- Comentário de bloco de várias linhas explicando uma função inteira
  quando quebrar a função em partes menores e bem nomeadas resolveria
  melhor.
- Código comentado ("desligado por enquanto") — o histórico guarda isso.
- Repetir o que o `DECISOES.md` já decidiu — ver seção seguinte.

---

## Comentários Inline

Use `//` para explicar uma linha específica cujo raciocínio não é óbvio a
partir do código — nunca para narrar o óbvio.

```java
// CORRETO — decisão provisória marcada como tal, não disfarçada de
// definitiva: TODO com issue, nunca comentário comum referenciando o
// estágio atual do projeto (ver O Que Nunca Vira Comentário)
// TODO(#XX): tamanho máximo de página conservador, sem dado real de uso
// ainda — revisar com base em produção.
private static final int TAMANHO_MAXIMO_DE_PAGINA = 100;

// ERRADO — narra o óbvio
// Verifica se o saldo é suficiente
if (saldo.compareTo(valorCobrado) < 0) { /* ... */ }
```

> `TODO`/`FIXME` são tolerados como marcador temporário — devem
> referenciar uma issue e ser removidos antes do merge:
> `// TODO(#12): suportar estorno de compra`.

---

## `DECISOES.md` vs. Comentário Inline

Uma decisão que afeta múltiplos arquivos ou é escolha de arquitetura (por
que o domínio é separado da entidade JPA, por que lock pessimista e não
otimista nos débitos) vai em [`DECISOES.md`](../fundamentos/DECISOES.md), nunca
repetida como comentário em cada arquivo afetado — o comentário inline
**referencia** a decisão por nome/link quando relevante, não a reexplica.

---

## READMEs Por Camada

O projeto é **um único módulo Gradle** com pacotes por camada (ver
[`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md)). Cada pacote de camada
(`dominio`, `aplicacao`, `infraestrutura`, `api`) tem seu `README.md` na
raiz da pasta do pacote, contendo:

- Propósito da camada
- Pacotes internos e o que mora em cada um
- Principais dependências externas (e as que ela **não** pode ter —
  fronteira de dependência, ver
  [`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md#regra-de-dependência))
- Como executar os testes específicos dessa camada
- Exemplos de uso rápido (quando aplicável)

```markdown
# dominio

Camada de domínio. Contém entidades, tipos de valor, hierarquia de erros e
interfaces de porta (contratos).

Não depende de nenhuma outra camada do projeto, nem de Spring, nem de JPA.

## Testes

    ./gradlew test --tests 'br.com.alelo.consumer.consumerpat.dominio.*'
```

> **Como o módulo é único, a fronteira não é imposta pelo compilador** (ao
> contrário de um projeto por camada). Ela é imposta por **revisão** e por um
> teste de arquitetura que falha quando `dominio` importa Spring/JPA ou outra
> camada — ver [`ESTRUTURA.md`](../fundamentos/ESTRUTURA.md).

### O README Evolui Com o Projeto — Regra Vinculante

Uma camada corre em duas situações distintas: **nasce** com um escopo e
pode permanecer aquilo, ou **evolui** — ganha tipos, muda contrato, muda
fronteira de dependência. Nos dois casos o README é a descrição do que a
camada *é agora*; ele **não é um texto de nascimento**.

A consequência de deixar de acompanhar é direta e cara: se o README não
acompanha, **para saber o que mudou é preciso ler todos os arquivos da
camada e comparar com os diffs dos commits** — que é exatamente o custo de
leitura que toda esta padronização existe para eliminar. O README
desatualizado não é neutro: é ativo, porque descreve algo que não é mais
verdade e obriga o leitor a desconfiar de toda a documentação.

Regras:

1. **Todo PR que altera contrato público da camada** (tipo público novo,
   removido ou com assinatura mudada, dependência nova ou removida) **atualiza
   o README da mesma camada no mesmo PR**. O revisor cobre isso no review —
   mudou a camada, mudou o README.
2. **README que contradiz o código é bug de documentação** — tem a
   prioridade de correção de um bug comum, não a de "melhoria de docs".
3. **O que mudou, e por quê, fica no commit/PR; o que *é* fica no
   README.** O README não acumula histórico narrativo — ele é regerado
   para descrever o estado atual.

### Registro de Evolução

Para que a pergunta "o que agregou esta camada?" seja respondida **sem
varrer diffs**, cada README termina com um bloco de registro com os
commits/PRs estruturantes — apenas os que mudaram o *contrato* da camada,
não o histórico completo de linhas (isso é trabalho de `git log`, não de
documentação):

```markdown
## Evolução

| PR | Mudança no contrato desta camada |
|---|---|
| #1 | Nasceu a camada: `CartaoPorta` e `ConsumidorPorta` extraídas do controller |
| #2 | `CartaoPorta` ganhou `buscarPorNumeroETipoParaAtualizacao` — trava pessimista nos débitos |
| #3 | Fronteira de dependência: proibido importar `org.springframework.*` |
```

Critério de entrada: **mudança de contrato** (assinatura, dependência,
fronteira). PRs de correção interna sem efeito no contrato não entram —
para esses, `git log` é a ferramenta certa, e o README aponta esse comando:

```markdown
## Histórico de mudanças internas

    git log --oneline -- src/main/java/br/com/alelo/consumer/consumerpat/dominio/
```

---

## Anti-Padrões

| Anti-padrão | Correto |
|---|---|
| Item público sem Javadoc | Adicionar — primeira frase começa pelo nome do item |
| Doc igual ao nome | Descrever contrato, casos de ausência/erro e posse de recurso, não traduzir o nome |
| Doc em inglês | Reescrever em português (termos técnicos sem tradução ficam como estão) |
| Comentário que narra o óbvio | Remover |
| Método que retorna `Resultado`/`Optional` sem descrever os casos possíveis | Documentar cada caso de ausência/variante de erro no `@return` |
| `@param`/`@return`/`@throws` ausente ou incoerente com a assinatura | Completar — o `doclint` acusa |
| Implementação de porta sobre API externa sem nota de comportamento esperado | Adicionar nota: o que a API faz no caso de borda e o que fazer quando falha |
| Javadoc desatualizado (descreve comportamento antigo) | Atualizar ou remover — doc que mente é pior que ausência de doc |
| Documentar getter/setter gerado por Lombok | Documentar o campo; o Lombok propaga |
| `TODO` sem issue associada | Criar issue antes do PR ou remover o `TODO` |
| `package-info.java` ausente | Todo pacote público de `dominio`/`aplicacao` precisa de um |
| PR altera contrato público da camada sem atualizar o README | Atualizar o README no mesmo PR — ver [READMEs Por Camada](#o-readme-evolui-com-o-projeto--regra-vinculante) |
| README descrevendo contrato que já mudou | Corrigir como bug de documentação, não como "melhoria de docs" |
| Mesma regra documentada em dois membros | Manter em um, referenciar por `{@link}` no outro |
| Markdown (`///`, `**negrito**`) dentro do Javadoc | Usar HTML (`<b>`, `<p>`, `{@code}`) — Markdown em Javadoc não existe no Java 17 |

---

## Checklist de Autor

- [ ] Todo pacote público tem `package-info.java`?
- [ ] Todos os membros públicos têm Javadoc iniciando pelo nome do elemento?
- [ ] Métodos públicos descrevem comportamento e cada caso de ausência/erro (`@return`, `@throws`)?
- [ ] Cada variante de hierarquia de erro descreve quando ocorre e o que o chamador deve fazer?
- [ ] Toda implementação de porta sobre API externa documenta o comportamento esperado e o risco de falha?
- [ ] Constantes com decisão numérica explicam o número, não o nome?
- [ ] Comentários estão em português técnico, objetivos e sem ambiguidade?
- [ ] Nenhum comentário é redundante com o nome do elemento?
- [ ] Nenhum `TODO`/`FIXME` sem issue associada?
- [ ] O README da camada foi atualizado no mesmo PR, se o contrato público mudou?
- [ ] `./gradlew build javadoc` passa sem avisos de documentação?

## Checklist de Review

- [ ] O Javadoc permite usar o membro sem abrir a implementação?
- [ ] Os casos de ausência/erro estão descritos e coerentes com os valores reais da hierarquia de erro?
- [ ] Toda implementação de porta sobre API externa documenta o comportamento esperado sem ambiguidade?
- [ ] Alguma decisão não óbvia (taxa, limite, workaround de provedor) ficou sem explicação?
- [ ] A documentação está coerente com [`ERROS.md`](../qualidade/ERROS.md) e com o vocabulário já usado no código?
- [ ] Algum exemplo `{@code}` que devia virar teste ficou só no Javadoc?
- [ ] O README das camadas afetadas descreve o estado atual do contrato (não o de quando nasceu)?

---

**[Estrutura →](../fundamentos/ESTRUTURA.md)**
