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

- **`persistencia.cartao`** — o cartão e o seu extrato. A entidade do cartão é a
  **única definição do schema**: o número é texto de dígitos e único
  (`uk_cartoes_numero`), o saldo é `NUMERIC(19,6)`, com a mesma escala do `Dinheiro`,
  e a restrição `saldo >= 0` fica no banco como última linha de defesa, mesmo que uma
  regra do domínio falhe. O tipo é gravado pelo nome, e nunca pela posição. Há
  versão de linha, e auditoria automática de `data_criacao` e `data_atualizacao`,
  com o instante do mesmo `Clock` do resto da aplicação. O extrato é imutável: só
  `data_criacao`, sem versão, e a chave estrangeira para o cartão nunca cascateia
  exclusão. Mora no pacote do cartão porque a FK exige que as duas entidades, ambas
  pacote-privadas, se vejam.
- **A busca trava o cartão.** `BuscaDeCartaoNoBanco` lê com
  `@Lock(PESSIMISTIC_WRITE)`, e a trava dura até o fim da transação do caso de uso, de
  modo que dois débitos simultâneos no mesmo cartão se enfileiram em vez de
  sobrescreverem um ao outro. A versão de linha fica como rede de segurança: se a
  trava for removida sem querer, o conflito vira erro visível, e não perda silenciosa
  de saldo.
- **A gravação só atualiza.** `GravacaoDeCartaoNoBanco` leva o saldo novo para a linha
  que já existe. Criar um cartão exige o consumidor dono, que o domínio ainda não tem, e
  gravar um cartão que não existe é bug de programação: lança, com o número mascarado.
- **`ErroDeTravaNaoDescartaConexao`** — liga ao pool a regra de que estourar a espera
  por uma trava não estraga a conexão. Sem ela, o H2 sinaliza o estouro com um estado
  que o Hikari trata como conexão quebrada: a fecha, o *rollback* seguinte falha, e quem
  chamou recebe uma exceção de transação opaca, e não a de trava.

## Estrutura

Convenção da casa: **uma peça por arquivo**, **nenhum método privado** em `src/main`,
e papéis (`interface` com `default`) sobre uma operação-base. Aqui a operação-base de
cada adaptador é o repositório (`ComRepositorioDeCartoes`, `ComRepositorioDeExtratos`),
e a busca, a gravação e o registro são papéis em arquivos próprios. O mapeador manual é
uma peça por direção. **Entidades e repositórios são pacote-privados**: o que sai do
pacote são os dois adaptadores, e quem os instancia é a composition root, por `@Import`
da classe, porque ela não consegue nem escrever o tipo do repositório.

Saída real de `tree` (regenerar e colar aqui a cada PR que mudar a árvore — ver
`docs/padronizacao/codigo/DOCUMENTACAO.md`, "READMEs Por Camada"):

```text
infraestrutura
├── package-info.java
└── persistencia
    ├── ErroDeTravaNaoDescartaConexao.java    # estourar a espera por trava não fecha a conexão
    ├── cartao
    │   ├── AtualizadorDeEntidadeDeCartao.java    # leva o saldo novo para a linha existente
    │   ├── BuscaDeCartaoNoBanco.java     # papel: busca com trava de escrita
    │   ├── CartaoAdaptador.java          # implementa CartaoPorta
    │   ├── CartaoDeEntidade.java         # linha → cartão do domínio
    │   ├── CartaoEntidade.java           # a tabela cartoes
    │   ├── CartaoRepositorioJpa.java     # JPQL; a consulta travada
    │   ├── ComRepositorioDeCartoes.java  # operação-base: o repositório dos cartões
    │   ├── ComRepositorioDeExtratos.java # operação-base: o repositório dos extratos
    │   ├── EntidadeDeExtrato.java        # extrato do domínio → linha
    │   ├── ExtratoAdaptador.java         # implementa ExtratoPorta
    │   ├── ExtratoEntidade.java          # a tabela extratos
    │   ├── ExtratoRepositorioJpa.java
    │   ├── GravacaoDeCartaoNoBanco.java  # papel: atualiza o saldo
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
`src/test/java/.../infraestrutura/`, no **mesmo pacote** das peças que provam, porque
as entidades e os repositórios são pacote-privados. São `@DataJpaTest` com o H2 real:
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
| `ExtratoAdaptador`, `RegistroDeExtratoNoBanco`, `EntidadeDeExtrato`, `ExtratoEntidade` | `ExtratoAdaptadorTeste` (valores e instante, auditoria, cartão ausente, FK, sem cascata) |
| `CartaoEntidade` (schema) | `SchemaDoCartaoTeste` (`CHECK` de saldo, número único, enum como texto, escala, auditoria) |
| Nomes das restrições e dos índices | `NomenclaturaDeSchemaTeste` |
| A trava e o teto da espera | `TravaDeCartaoTeste` (a segunda transação falha dentro do teto, e a que termina antes tem sucesso) |
| `ErroDeTravaNaoDescartaConexao` | `ErroDeTravaNaoDescartaConexaoTeste` |
| A configuração do banco | `ConfiguracaoDoBancoTeste` (teto de trava na URL, menor que a espera por conexão; o avaliador ligado ao pool) |
| Regra de dependência da infraestrutura | `RegraDeDependenciaDaInfraestruturaTeste`, na raiz de `src/test/.../infraestrutura` |

Peças sem teste próprio, cobertas junto de quem as torna observáveis:

| Peça sem teste próprio | Coberta junto de | Por quê |
|---|---|---|
| `ComRepositorioDeCartoes`, `ComRepositorioDeExtratos`, `CartaoRepositorioJpa`, `ExtratoRepositorioJpa` | os testes dos adaptadores | são só a operação-base e o acesso do Spring Data; o único caminho até eles é o adaptador |

## Evolução

| PR | Mudança no contrato desta camada |
|---|---|
| `#14` | Cria a camada: `CartaoAdaptador` e `ExtratoAdaptador`, as entidades `cartoes` e `extratos`, a busca com trava de escrita, e `ErroDeTravaNaoDescartaConexao`. O teto de espera por trava é o `LOCK_TIMEOUT` da URL do H2, e não a dica do JPA, que ele ignora. A tabela de cartões nasce sem `consumidor_id`, que entra com o consumidor. |
