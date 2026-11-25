# infraestrutura

Camada de infraestrutura: implementa as portas do domínio e é a **única que conhece
JPA**. Depende do domínio e da aplicação, e nunca da API: a dependência aponta
sempre para o domínio. A regra é verificada por teste, não por convenção:
`RegraDeDependenciaDaInfraestruturaTeste` lê os `import` do pacote e falha se aparecer
qualquer coisa fora da biblioteca padrão, do domínio, da aplicação, da própria
infraestrutura e das ferramentas de persistência — ver
`docs/padronizacao/fundamentos/ESTRUTURA.md`.

## Propósito

Responde a uma pergunta que o domínio se recusa a responder: **como isso é
gravado**. O domínio diz que um cartão se busca para atualizar e que um extrato só se
registra; a infraestrutura diz com que tabela, com que restrição e com que trava.

- **A persistência se divide em quatro pacotes por conceito**, que dependem uns dos
  outros num só sentido: `consumidor` (a folha), `cartao` (depende de `consumidor`),
  `extrato` (depende de `cartao`) e `cadastro` (compõe `consumidor` e `cartao`). O
  grafo não tem ciclo, e é por isso que `ConsumidorDeEntidade` recebe os cartões já
  convertidos, e não as linhas deles: quem os junta é `ConsumidorDasLinhas`, no
  `cadastro`. Cada tabela tem **uma só definição do schema**, a da sua entidade.
- **`persistencia.consumidor`** — a tabela `consumidores`: documento único
  (`uk_consumidores_documento`), os telefones residencial e geral nulos e o resto não,
  os tamanhos das colunas iguais aos dos tipos do domínio, versão de linha, auditoria,
  e o índice por nome e id da listagem. Não há coleção de cartões na entidade:
  atualizar um cadastro nunca toca nos cartões, que são o caminho quente, com trava.
- **`persistencia.cartao`** — a tabela `cartoes`: número texto de dígitos e único
  (`uk_cartoes_numero`), saldo `NUMERIC(19,6)` com a mesma escala do `Dinheiro`, e a
  restrição `saldo >= 0` no banco como última linha de defesa. O cartão tem dono: a
  chave estrangeira `fk_cartoes_consumidores` não é nula e não cascateia exclusão, e
  `uk_cartoes_consumidor_tipo` garante no máximo um cartão de cada tipo por
  consumidor. Não há índice só por `consumidor_id`, porque a unicidade, que o tem como
  primeira coluna, já serve a busca. O tipo é gravado pelo nome, e nunca pela posição.
- **`persistencia.extrato`** — a tabela `extratos`: imutável, só `data_criacao`, sem
  versão, e a chave estrangeira para o cartão nunca cascateia exclusão.
- **`persistencia.cadastro`** — o `ConsumidorAdaptador`: a busca (duas consultas por
  consumidor, a da linha e a dos cartões), a criação e a atualização. **O conflito é
  conferido antes de gravar**: um `INSERT` que viola a unicidade lança no meio da
  transação e a marca para reverter, e devolver uma falha depois disso faria o caso de
  uso confirmar uma transação que já não pode ser confirmada. A unicidade do banco fica
  como rede de segurança para a corrida entre dois cadastros, que vira o 409 genérico
  da API. A atualização grava só os dados cadastrais, confere a versão contra a linha,
  e a versão de linha do Hibernate pega a corrida na descarga.
- **A busca trava o cartão.** `BuscaDeCartaoNoBanco` lê com
  `@Lock(PESSIMISTIC_WRITE)`, e a trava dura até o fim da transação do caso de uso, de
  modo que dois débitos simultâneos no mesmo cartão se enfileiram em vez de
  sobrescreverem um ao outro. A versão de linha fica como rede de segurança: se a
  trava for removida sem querer, o conflito vira erro visível, e não perda silenciosa
  de saldo.
- **A gravação do cartão só atualiza.** `GravacaoDeCartaoNoBanco` leva o saldo novo para
  a linha que já existe. Quem cria o cartão é a criação do consumidor, que o grava junto
  do dono, e gravar um cartão que não existe é bug de programação: lança, com o número
  mascarado.
- **`ErroDeTravaNaoDescartaConexao`** — liga ao pool a regra de que estourar a espera
  por uma trava não estraga a conexão. Sem ela, o H2 sinaliza o estouro com um estado
  que o Hikari trata como conexão quebrada: a fecha, o *rollback* seguinte falha, e quem
  chamou recebe uma exceção de transação opaca, e não a de trava.

## Estrutura

Convenção da casa: **uma peça por arquivo**, **nenhum método privado** em `src/main`,
e papéis (`interface` com `default`) sobre uma operação-base. Aqui a operação-base de
cada adaptador é o repositório (`ComRepositorioDeCartoes`, `ComRepositorioDeExtratos`,
`ComRepositorioDeConsumidores`), e a busca, a gravação, a criação e o registro são
papéis em arquivos próprios. O mapeador manual é uma peça por direção.

**Entidades e repositórios são públicos**, e a barreira passou do compilador para um
teste. Eram pacote-privados, para nenhuma outra camada depender da JPA, mas as chaves
estrangeiras exigem que as entidades se enxerguem, e isso só cabe num pacote só ou em
entidades públicas. A razão da regra continua de pé, e `RegraDeDependenciaDaPersistenciaTeste`
a impõe lendo os imports: nada fora de `infraestrutura.persistencia` importa uma entidade
ou um repositório, e os quatro pacotes só dependem uns dos outros no sentido acima. O que
sai da persistência são os três adaptadores, e quem os instancia é a composition root,
por `@Import` da classe.

Os `import` que passam de 100 colunas levam a diretiva
`editorconfig-checker-disable-next-line` na linha de cima, porque um `import` não se
quebra e nenhum nome de peça cabe depois de um prefixo de 74 colunas.

Saída real de `tree` (regenerar e colar aqui a cada PR que mudar a árvore — ver
`docs/padronizacao/codigo/DOCUMENTACAO.md`, "READMEs Por Camada"):

```text
infraestrutura
├── package-info.java
└── persistencia
    ├── ErroDeTravaNaoDescartaConexao.java    # estourar a espera por trava não fecha a conexão
    ├── cadastro                          # compõe consumidor e cartão
    │   ├── AtualizacaoDeConsumidorNoBanco.java   # papel: só os dados, com a versão conferida
    │   ├── BuscaDeConsumidorNoBanco.java # papel: a linha e os cartões dela
    │   ├── ConflitoDeNovoConsumidorNoBanco.java  # papel: documento e número, antes de gravar
    │   ├── ConsumidorAdaptador.java      # implementa ConsumidorPorta
    │   ├── ConsumidorDasLinhas.java      # junta a linha do consumidor e as dos cartões
    │   ├── CriacaoDeConsumidorNoBanco.java       # papel: conflito ou grava
    │   ├── DocumentoLivreNoBanco.java    # papel: o documento novo não é de outro
    │   ├── GravacaoDoNovoConsumidorNoBanco.java  # papel: o consumidor, depois os cartões
    │   ├── NumeroDeCartaoRepetidoNoBanco.java    # papel: o número já é de outro cartão
    │   ├── VersaoDaLinhaConfirmada.java  # a versão do cliente é a da linha
    │   └── package-info.java
    ├── cartao                            # depende de consumidor
    │   ├── AtualizadorDeEntidadeDeCartao.java    # leva o saldo novo para a linha existente
    │   ├── BuscaDeCartaoNoBanco.java     # papel: busca com trava de escrita
    │   ├── CartaoAdaptador.java          # implementa CartaoPorta
    │   ├── CartaoDeEntidade.java         # linha → cartão do domínio
    │   ├── CartaoEntidade.java           # a tabela cartoes
    │   ├── CartaoRepositorioJpa.java     # JPQL; a consulta travada
    │   ├── ComRepositorioDeCartoes.java  # operação-base: o repositório dos cartões
    │   ├── EntidadeDeCartao.java         # cartão do domínio, com o dono → linha
    │   ├── GravacaoDeCartaoNoBanco.java  # papel: atualiza o saldo
    │   └── package-info.java
    ├── consumidor                        # a folha: não depende de ninguém daqui
    │   ├── AtualizadorDeEntidadeDeConsumidor.java    # leva os dados cadastrais para a linha
    │   ├── ComRepositorioDeConsumidores.java  # operação-base: o repositório dos consumidores
    │   ├── ConsumidorDeEntidade.java     # linha + cartões do domínio → consumidor
    │   ├── ConsumidorEntidade.java       # a tabela consumidores
    │   ├── ConsumidorRepositorioJpa.java # só pergunta se o documento existe
    │   └── package-info.java
    ├── extrato                           # depende de cartao
    │   ├── ComRepositorioDeExtratos.java # operação-base: o repositório dos extratos
    │   ├── EntidadeDeExtrato.java        # extrato do domínio → linha
    │   ├── ExtratoAdaptador.java         # implementa ExtratoPorta
    │   ├── ExtratoEntidade.java          # a tabela extratos
    │   ├── ExtratoRepositorioJpa.java
    │   ├── RegistroDeExtratoNoBanco.java # papel: só insere
    │   └── package-info.java
    └── package-info.java
```

## Dependências externas

Spring Data JPA, Hibernate e Lombok, que o projeto já usa, e o Hikari, que vem com o
starter. O H2 é o único banco. Não entra dependência nova no build, e o enunciado a
proíbe: por isso não há ferramenta de migração, e o schema nasce das anotações
(`ddl-auto: create-drop`), sem DDL à mão e sem `schema.sql`.

## Testes

Framework do `spring-boot-starter-test`, sem dependência nova. Os testes ficam em
`src/test/java/.../infraestrutura/`, espelhando os pacotes. São `@DataJpaTest` com o H2 real:
cada restrição é conferida contra o banco, e não suposta, porque uma anotação que o
banco ignora em silêncio não protege nada.

### Executar

```bash
./gradlew test --tests '*infraestrutura*'
```

### Cobertura

Não há medição de cobertura por ferramenta: o plugin do Gradle baixaria um artefato novo
no build, e o enunciado proíbe dependência nova. A garantia é por regra: cada peça tem o
teste que falharia sem ela, e a tabela abaixo é o inventário.

| Peça | Teste que a cobre |
|---|---|
| `CartaoAdaptador`, `BuscaDeCartaoNoBanco`, `GravacaoDeCartaoNoBanco`, `CartaoDeEntidade`, `AtualizadorDeEntidadeDeCartao` | `CartaoAdaptadorTeste` (busca, ausente e outro tipo, gravação, fração de centavo, cartão inexistente, versão) |
| `EntidadeDeCartao` | `EntidadeDeCartaoTeste` (dono, tipo, número e saldo; id e versão ficam para o banco) |
| `ExtratoAdaptador`, `RegistroDeExtratoNoBanco`, `EntidadeDeExtrato`, `ExtratoEntidade` | `ExtratoAdaptadorTeste` (valores e instante, auditoria, cartão ausente, FK, sem cascata) |
| `CartaoEntidade` (schema) | `SchemaDoCartaoTeste` (`CHECK` de saldo, número único, enum como texto, escala, auditoria, o dono obrigatório, um cartão de cada tipo por consumidor, a FK sem cascata) |
| `ConsumidorEntidade` (schema) | `SchemaDoConsumidorTeste` (documento único, telefones nulos, tamanhos das colunas, índice por nome e id, versão, a corrida pega pela versão de linha, auditoria) |
| `ConsumidorAdaptador`, os papéis do `cadastro`, `ConsumidorDasLinhas` | `ConsumidorAdaptadorTeste` (criar com e sem cartões, documento e número repetidos sem gravar, buscar com os cartões ordenados, atualizar só os dados, versão velha, documento de outro) |
| `ConsumidorDeEntidade`, `AtualizadorDeEntidadeDeConsumidor` | `ConsumidorDeEntidadeTeste` e `AtualizadorDeEntidadeDeConsumidorTeste` (telefones nulos e preenchidos, o que não muda na atualização, dado inválido que lança) |
| `VersaoDaLinhaConfirmada` | `VersaoDaLinhaConfirmadaTeste` |
| Nomes das restrições e dos índices | `NomenclaturaDeSchemaTeste` (as três tabelas) |
| A fronteira da persistência | `RegraDeDependenciaDaPersistenciaTeste` (nada fora importa entidade ou repositório, sem ciclo entre os pacotes, e um pacote novo sem regra é recusado) |
| A trava e o teto da espera | `TravaDeCartaoTeste` (a segunda transação falha dentro do teto, e a que termina antes tem sucesso) |
| `ErroDeTravaNaoDescartaConexao` | `ErroDeTravaNaoDescartaConexaoTeste` |
| A configuração do banco | `ConfiguracaoDoBancoTeste` (teto de trava na URL, menor que a espera por conexão; o avaliador ligado ao pool) |
| Regra de dependência da infraestrutura | `RegraDeDependenciaDaInfraestruturaTeste`, na raiz de `src/test/.../infraestrutura` |

Peças sem teste próprio, cobertas junto de quem as torna observáveis:

| Peça sem teste próprio | Coberta junto de | Por quê |
|---|---|---|
| `ComRepositorioDeCartoes`, `ComRepositorioDeExtratos`, `ComRepositorioDeConsumidores`, `CartaoRepositorioJpa`, `ExtratoRepositorioJpa`, `ConsumidorRepositorioJpa` | os testes dos adaptadores | são só a operação-base e o acesso do Spring Data; o único caminho até eles é o adaptador |
| `ConflitoDeNovoConsumidorNoBanco`, `NumeroDeCartaoRepetidoNoBanco`, `GravacaoDoNovoConsumidorNoBanco`, `CriacaoDeConsumidorNoBanco`, `DocumentoLivreNoBanco`, `AtualizacaoDeConsumidorNoBanco`, `BuscaDeConsumidorNoBanco` | `ConsumidorAdaptadorTeste` | são os papéis do adaptador, e o único caminho até eles é a porta |

## Evolução

| PR | Mudança no contrato desta camada |
|---|---|
| `#14` | Cria a camada: `CartaoAdaptador` e `ExtratoAdaptador`, as entidades `cartoes` e `extratos`, a busca com trava de escrita, e `ErroDeTravaNaoDescartaConexao`. O teto de espera por trava é o `LOCK_TIMEOUT` da URL do H2, e não a dica do JPA, que ele ignora. A tabela de cartões nasce sem `consumidor_id`, que entra com o consumidor. |
| `#18` | Acrescenta a persistência do consumidor: a tabela `consumidores`, o `consumidor_id` não nulo e `uk_cartoes_consumidor_tipo` no cartão, e o `ConsumidorAdaptador` (busca, criação e atualização, com o conflito conferido antes de gravar). A persistência passa a quatro pacotes por conceito (`consumidor`, `cartao`, `extrato` e `cadastro`), e entidades e repositórios passam de pacote-privados a públicos, com a barreira em `RegraDeDependenciaDaPersistenciaTeste`. Nenhum código do legado depende ainda dela. |
