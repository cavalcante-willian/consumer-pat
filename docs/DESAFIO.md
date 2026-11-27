# O desafio técnico

Este projeto nasceu como resposta a um desafio técnico de *code review* da
Alelo. O enunciado está no
[README do repositório original](https://github.com/alelo-dev/consumer-pat/blob/main/README.md),
e este documento diz o que ele pedia, o que o código original fazia, o que
foi feito e como cada afirmação se comprova.

O repositório original não declara licença, e este derivado também não.

## O que o desafio pedia

| O enunciado | Como foi atendido |
|---|---|
| Fazer o *code review* de uma API Spring Boot de consumidores e de transações de cartão de benefícios, e melhorá-la | O legado foi lido, as regras do enunciado viraram testes, e a API foi reescrita em camadas ([abaixo](#o-que-foi-feito)) |
| Que ela seja escalável, performática, de fácil manutenção e resiliente | Listagem paginada, compra e crédito sem perda de saldo sob concorrência, uma responsabilidade por arquivo, e erros padronizados ([abaixo](#como-se-comprova)) |
| Código limpo, legível, com boas práticas | A [padronização do projeto](PADRONIZACAO_INDEX.md), imposta por testes de arquitetura e pelo build |
| **Não incluir dependência nova** | Cumprido, e conferível: ver [a seção própria](#sem-dependência-nova) |
| Os contratos da API podem mudar | Mudaram, e o mapa antigo para o novo está no [`API.md`](padronizacao/qualidade/API.md) |
| JDK 11 ou 17 | Hoje, JDK 17: o código usa `record` e `sealed`, que o 11 não tem, e o Gradle 7.6 do projeto não roda em JDK a partir do 20 |
| Entrega por *fork* e *pull request* | Cada etapa entrou por um PR, com o CI como critério de aceite |

## O que o código original fazia

O original é o commit
[`3b2388a`](https://github.com/alelo-dev/consumer-pat/commit/3b2388a), o
último antes da padronização. Tinha um controller, duas entidades e dois
repositórios, com cinco rotas:

| Rota original | O que fazia | Rota hoje |
|---|---|---|
| `GET /consumer/consumerList` | Devolve **todos** os consumidores | `GET /api/v1/consumidores`, paginada |
| `POST /consumer/createConsumer` | Cadastra um consumidor | `POST /api/v1/consumidores` |
| `POST /consumer/updateConsumer` | Atualiza o consumidor | `PUT /api/v1/consumidores/{id}` |
| `GET /consumer/setcardbalance` | Credita um valor num cartão | `POST /api/v1/creditos` |
| `GET /consumer/buy` | Debita uma compra de um cartão | `POST /api/v1/compras` |

As regras de negócio estavam nos comentários do controller: a compra só debita
o cartão do tipo do estabelecimento, a alimentação tem 10% de desconto, o
combustível tem 35% de acréscimo, e a atualização não pode alterar o saldo.

## O que estava errado

O código original funcionava no caminho feliz, e falhava fora dele. As nove
primeiras linhas abaixo estavam documentadas em 12 testes ignorados, escritos
antes de qualquer mudança, e hoje cada uma tem testes que passam. As três
últimas vieram da leitura do código e das medições.

| Defeito no original | Efeito | Como ficou |
|---|---|---|
| Crédito ou compra em cartão que não existe | `NullPointerException`, resposta `500` | `404` com o corpo de erro padronizado |
| Valor zero ou negativo aceito | Um crédito negativo **debitava** o cartão | `400`, validado na borda e no domínio |
| Tipo de estabelecimento desconhecido | Caía no cartão de combustível | `400` |
| Atualizar o consumidor | Gravava o objeto recebido, **inclusive o saldo** | O corpo não aceita saldo, e a atualização exige a versão lida |
| Compra sem conferir o saldo | Deixava o cartão negativo | `422`, e o banco recusa saldo negativo |
| Cartão de outro tipo | Não era achado, e o controller lançava exceção | `404` |
| Documento ou número de cartão repetido | Aceitos | `409`, com restrição única no banco |
| `equals` sem `hashCode` nas entidades | Comportamento incorreto em coleções | As entidades não têm `equals` próprio |
| Compras simultâneas no mesmo cartão | Lia o saldo, subtraía em memória e gravava: o `lost update` | Trava pessimista e versão de linha; 20 compras simultâneas deixam o saldo exato |
| Listagem sem paginação | A tabela inteira numa resposta: 21,2 MB com 50.000 consumidores | Uma página de 20 tem 846 bytes |
| `GET` que altera estado, com número de cartão e valor na URL | Uma pré-busca de navegador gerava uma compra, e o cartão ia parar no log de acesso | `POST`, com tudo no corpo |
| Cartão como `int` e dinheiro como `double` | O cartão não passava de 2 bilhões, e o dinheiro perdia precisão | Número em texto, de 13 a 19 dígitos, e dinheiro decimal com seis casas |

Na segurança, a [bateria de 16 verificações](padronizacao/qualidade/SEGURANCA.md)
mostrou o original protegido em **4** e o novo em **14**. O original expunha, por
exemplo, o `TRACE` com eco dos cabeçalhos, valores negativos aceitos e dados
sensíveis devolvidos por inteiro. As duas verificações em que o novo também
aparece exposto, a documentação pública e a falta de limite de taxa, são
riscos aceitos e documentados.

## O que foi feito

1. **A padronização veio primeiro.** Os 17 documentos de
   [`docs/padronizacao/`](PADRONIZACAO_INDEX.md) foram escritos antes de se
   alterar qualquer arquivo do código. Por isso os exemplos de código dos
   documentos foram corrigidos durante a refatoração, para refletir o que se
   construiu: o texto conceitual prevaleceu sobre o exemplo, e não o contrário.
2. **As regras do enunciado viraram testes** de caracterização contra o
   legado, que passaram a ser o critério de que nada se perdeu.
3. **As camadas novas entraram uma por PR**, de dentro para fora: domínio,
   aplicação, persistência e API, convivendo com o legado sem tocá-lo.
4. **Observabilidade, saúde e segurança** vieram em seguida: log em português
   com o id da requisição, o `GET /api/v1/saude` e os cabeçalhos de segurança.
5. **A medição**: testes de concorrência na API, um comparativo com o original
   por HTTP real e a bateria de segurança.
6. **A remoção do legado**, que foi a quebra de contrato, e um build que não
   aceita nenhum aviso.

## Sem dependência nova

A regra mais explícita do enunciado se confere comparando o `build.gradle` de
hoje com o do original:

```bash
git diff 3b2388a -- build.gradle
```

A única mudança em dependências é uma **remoção**: o `springdoc-openapi-data-rest`,
que o projeto não usava. A versão do `springdoc` passou para o
`gradle.properties`, e o `gradle.lockfile` trava as versões resolvidas. Nada
foi acrescentado, e por isso o projeto não tem ferramenta de migração de banco,
cliente de métricas nem biblioteca de limite de taxa.

## Como se comprova

| O que | Como |
|---|---|
| A suíte | `./gradlew test`: 1332 testes de unidade, integração, contrato, concorrência e arquitetura |
| O build sem avisos | `./gradlew build` falha por aviso do compilador e por Javadoc inválido |
| O desempenho contra o original | `scripts/medir-contra-o-original.sh`: extrai o original do histórico, sobe os dois sistemas e mede por HTTP |
| A segurança contra o original | `scripts/medir-contra-o-original.sh seguranca` |

No comparativo, com 50.000 consumidores em cada sistema, listar no original
levou de 126 a 129 ms e devolveu a tabela inteira, e no novo levou de 1,3 a
2,1 ms e devolveu uma página de 20. Creditar e comprar ficaram de 2 a 5 vezes
mais rápidos. Os tempos absolutos valem só na máquina em que foram medidos, e o
[`PERFORMANCE.md`](padronizacao/qualidade/PERFORMANCE.md) descreve essa máquina
e as condições.

## O que ficou de fora, de propósito

- **Autenticação e autorização.** O enunciado não as pede, e o
  [`SEGURANCA.md`](padronizacao/qualidade/SEGURANCA.md) as lista entre os
  riscos conhecidos.
- **Limite de taxa**, pelo mesmo motivo e por exigir uma biblioteca.
- **Persistência em disco.** O banco é o H2 em memória, como no original, e os
  dados somem ao desligar.
- **Uma imagem de contêiner de implantação.**
