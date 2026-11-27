# Consumer PAT

API REST para gerenciar **consumidores** e os seus **cartões de benefício**
(alimentação, farmácia e combustível): cadastro, crédito de saldo e compras em
estabelecimentos, com o preço de cada tipo de cartão aplicado pelo sistema.

> **Origem.** Este projeto é a resolução de um desafio técnico de *code review*.
> O enunciado está no
> [README do repositório original](https://github.com/alelo-dev/consumer-pat/blob/main/README.md),
> e o que ele pedia e o que foi feito estão em [`docs/DESAFIO.md`](docs/DESAFIO.md).

## O que ele faz

- **Consumidores:** cadastro com os cartões, atualização dos dados cadastrais,
  consulta por id e listagem paginada.
- **Crédito:** soma um valor ao saldo de um cartão.
- **Compra:** debita o cartão do tipo do estabelecimento, com o preço do tipo, e
  guarda o extrato com o valor cobrado.
- **Seguro sob concorrência:** compras e créditos simultâneos no mesmo cartão
  nunca perdem saldo.
- **Erros previsíveis:** toda falha responde no formato
  [RFC 7807](https://datatracker.ietf.org/doc/html/rfc7807), com um código
  estável.
- **Operável:** saúde por dependência, log estruturado com o id de cada
  requisição, e a especificação OpenAPI com Swagger.

## Regras de negócio

| Tipo do cartão | Código do estabelecimento | O que a compra cobra |
|---|---|---|
| Alimentação | `1` | **90%** do valor: 10% de desconto |
| Farmácia | `2` | **100%** do valor |
| Combustível | `3` | **135%** do valor: 35% de acréscimo |

- A compra só debita o cartão **do tipo do estabelecimento**.
- O saldo **nunca fica negativo**: uma compra que o saldo não cobre é recusada.
- Cada consumidor tem, no máximo, **um cartão de cada tipo**.
- O documento e o número de cada cartão são **únicos**.
- Os valores monetários têm **seis casas decimais** e viajam como texto, para não
  perder precisão.
- Documento, telefones e número de cartão voltam **mascarados** nas respostas, e
  o log nunca os traz inteiros.
- Atualizar o consumidor **não altera o saldo** nem os cartões.

## Começando

### Requisitos

- **JDK 17**, com o `JAVA_HOME` apontando para ele: é o que o projeto usa e
  verifica. O código usa recursos do Java 17, e o Gradle 7.6 do projeto não roda
  em JDK a partir do 20.
- Nada mais: o Gradle vem no repositório (`gradlew`), e o banco é o H2 em memória.

### Executar

```bash
./gradlew bootRun
```

No Windows, `gradlew bootRun`. A API sobe em `http://localhost:8080`.

### Gerar e rodar o jar

```bash
./gradlew bootJar
java -jar build/libs/consumer-pat-0.0.1.jar
```

O banco é em memória: os dados **somem** quando a aplicação desliga.

## Usando a API

Todas as rotas ficam em `/api/v1`. O exemplo abaixo percorre o fluxo inteiro.

**1. Cadastrar um consumidor com os três cartões.** O documento aceita
pontuação, e a resposta traz o `Location` do consumidor criado.

```bash
curl -i -X POST http://localhost:8080/api/v1/consumidores \
  -H 'Content-Type: application/json' -d '{
    "dados": {
      "nome": "Maria Souza", "documento": "39053344705",
      "dataNascimento": "1990-05-17", "celular": "11999998888",
      "email": "maria@exemplo.com.br", "rua": "Rua das Flores", "numero": "120",
      "cidade": "Recife", "pais": "Brasil", "cep": "50000000"
    },
    "numerosCartoes": {
      "ALIMENTACAO": "4111111111111111",
      "COMBUSTIVEL": "4222222222222222",
      "FARMACIA": "4333333333333333"
    }
  }'
```

```text
HTTP/1.1 201
Location: /api/v1/consumidores/1
X-Request-Id: 932da00c-3398-4b7a-a533-ee8e3fc12d20

{"id":1,"versao":0,"nome":"Maria Souza","documento":"****4705", ...,
 "cartoes":[{"numero":"****1111","tipo":"ALIMENTACAO","saldo":"0.000000"}, ...]}
```

**2. Creditar 200,00 no cartão de alimentação.**

```bash
curl -X POST http://localhost:8080/api/v1/creditos \
  -H 'Content-Type: application/json' \
  -d '{"numeroCartao": "4111111111111111", "tipoEstabelecimento": 1, "valor": 200.00}'
```

```json
{"numeroCartao":"****1111","tipo":"ALIMENTACAO","valorCreditado":"200.000000","saldoAtual":"200.000000"}
```

**3. Comprar 100,00 num estabelecimento de alimentação.** O desconto de 10% sai
no valor cobrado.

```bash
curl -X POST http://localhost:8080/api/v1/compras \
  -H 'Content-Type: application/json' \
  -d '{"tipoEstabelecimento": 1, "nomeEstabelecimento": "Mercado Central",
       "numeroCartao": "4111111111111111", "descricaoProduto": "Arroz", "valor": 100.00}'
```

```json
{"numeroCartao":"****1111","tipo":"ALIMENTACAO","valorInformado":"100.000000","valorCobrado":"90.000000","saldoRestante":"110.000000","instante":"2026-11-27T01:39:45.315243272Z"}
```

**4. Listar os consumidores**, paginados:

```bash
curl 'http://localhost:8080/api/v1/consumidores?pagina=1&tamanhoPagina=20&ordenacao=nome&direcao=asc'
```

```json
{"itens":[{"id":1,"nome":"Maria Souza"}],"pagina":1,"tamanhoPagina":20,"total":1,"totalPaginas":1}
```

### Rotas

| Rota | O que faz | Sucesso |
|---|---|---|
| `POST /api/v1/consumidores` | Cadastra o consumidor e os cartões | `201` com `Location` |
| `GET /api/v1/consumidores/{id}` | Consulta um consumidor, com os cartões | `200` |
| `PUT /api/v1/consumidores/{id}` | Atualiza os dados cadastrais; exige a `versao` lida | `200` |
| `GET /api/v1/consumidores` | Lista paginada, só com `id` e `nome` | `200` |
| `POST /api/v1/creditos` | Credita um valor num cartão | `201` |
| `POST /api/v1/compras` | Registra uma compra | `201` |
| `GET /api/v1/saude` | Estado da aplicação e do banco | `200` ou `503` |

A listagem aceita `pagina` (a partir de 1), `tamanhoPagina` (de 1 a 100, padrão
20), `ordenacao` (`id` ou `nome`) e `direcao` (`asc` ou `desc`).

### Atualizar sem perder uma alteração alheia

A atualização leva a `versao` que você leu. Se outra atualização passou na
frente, a resposta é `409`, e basta reler o consumidor e tentar de novo:

```bash
curl -X PUT http://localhost:8080/api/v1/consumidores/1 \
  -H 'Content-Type: application/json' \
  -d '{"versao": 0, "dados": { ...os dados cadastrais... }}'
```

## Erros

Toda falha responde `application/problem+json`, com um `type` estável que
identifica o erro, o `instance` da rota e o `idRequisicao` para achá-la no log.

```json
{"type":"urn:consumer-pat:erro:CPT-REG-CRT-001","title":"Regra de negócio não atendida",
 "status":422,"detail":"O saldo do cartão não cobre o valor da compra.",
 "instance":"/api/v1/compras","idRequisicao":"e303fb2c-f909-45d4-b50d-3d64846d3966"}
```

| Status | Quando |
|---|---|
| `400` | Entrada inválida: campo ausente, formato errado, valor que não é positivo, tipo de estabelecimento desconhecido. A resposta traz **todos** os campos inválidos de uma vez, em `errors` |
| `404` | Consumidor ou cartão que não existe |
| `409` | Documento ou número de cartão já cadastrado, versão desatualizada, ou cartão em uso por outra requisição (tente de novo) |
| `415` | O corpo não é `application/json` |
| `422` | A regra de negócio recusou: saldo insuficiente |
| `500` | Falha inesperada; o corpo não vaza a causa |

O código do erro tem o formato `CPT-<CATEGORIA>-<ÁREA>-<SEQ>`, e os testes
garantem que cada um é único. A regra está em
[`ERROS.md`](docs/padronizacao/qualidade/ERROS.md).

## Documentação interativa e banco

No perfil padrão:

| O que | Onde |
|---|---|
| Swagger UI | <http://localhost:8080/swagger-ui/index.html> |
| Especificação OpenAPI | <http://localhost:8080/v3/api-docs> |
| Console do H2 | <http://localhost:8080/h2-console> |

Para o console: **JDBC URL** `jdbc:h2:mem:consumer-pat`, **usuário** `sa` e
**senha** `sa`. Ele só abre para quem está na própria máquina.

## Configuração e perfis

| Perfil | O que muda |
|---|---|
| padrão | Log legível no console, Swagger, especificação e console do H2 ligados |
| `prod` | Log também em `logs/consumer-pat.log`, com rotação (10 MB por arquivo, 14 dias, teto de 500 MB), e **sem** Swagger, especificação nem console do H2 |

```bash
java -jar build/libs/consumer-pat-0.0.1.jar --spring.profiles.active=prod
```

## Operação

- **Saúde.** `GET /api/v1/saude` devolve `200` com `SAUDAVEL` quando o banco
  responde, e `503` com `DEGRADADO` quando não. É público e não traz nada
  interno.
- **Log.** Cada requisição gera uma linha ao terminar, com o método, o caminho, o
  status e a duração, e os eventos de negócio (compra registrada, conflito
  detectado) saem em linhas próprias:

  ```text
  2026-11-27T01:39:45.265Z  INFO [932da00c-...] ... requisicao.concluida metodo=POST caminho=/api/v1/consumidores status=201 duracaoMs=170
  ```

- **Rastreio.** As respostas de `/api/v1` trazem o cabeçalho `X-Request-Id`, o
  mesmo que aparece no log e no corpo de erro. Se a requisição o trouxer, e for
  um UUID, ele é aproveitado.
- **Concorrência.** Compra e crédito travam a linha do cartão, e a linha tem
  versão. Quem não obtém a trava em 3 segundos recebe `409`, com a orientação de
  tentar de novo. Vinte compras simultâneas no mesmo cartão deixam o saldo exato.
- **Segurança.** As respostas de `/api/v1` levam os cabeçalhos de segurança
  (`nosniff`, `X-Frame-Options`, `Content-Security-Policy`, `no-referrer` e
  `no-store`), o `TRACE` é recusado e os erros não vazam pilha nem SQL.
  **Não há autenticação nem autorização**: o serviço deve ficar atrás de quem as
  faça. A lista do que ficou de fora está em
  [`SEGURANCA.md`](docs/padronizacao/qualidade/SEGURANCA.md).

## Arquitetura

Quatro camadas, e a dependência vai sempre para dentro:

```text
api ──► aplicacao ──► dominio ◄── infraestrutura
```

| Camada | O que tem | README |
|---|---|---|
| `dominio` | As regras e os tipos de valor (`Dinheiro`, `NumeroDeCartao`), as portas e os erros. Não depende de framework | [dominio](src/main/java/br/com/alelo/consumer/consumerpat/dominio/README.md) |
| `aplicacao` | Os casos de uso: registrar compra, creditar, cadastrar, atualizar | [aplicacao](src/main/java/br/com/alelo/consumer/consumerpat/aplicacao/README.md) |
| `infraestrutura` | Os adaptadores das portas: persistência JPA, saúde e log | [infraestrutura](src/main/java/br/com/alelo/consumer/consumerpat/infraestrutura/README.md) |
| `api` | Os controllers, os filtros, o formato de erro e a composição de tudo | [api](src/main/java/br/com/alelo/consumer/consumerpat/api/README.md) |

Duas regras sustentam o resto:

- **A regra de dependência**: o domínio não depende de nenhuma outra camada nem
  de framework, e só a camada `api` conhece todas as outras. Testes de
  arquitetura a impõem.
- **Uma responsabilidade por arquivo**: uma classe nova nasce no lugar certo, e
  duas pessoas mexendo em peças diferentes não disputam o mesmo arquivo. É a
  convenção do projeto, revisada em cada PR, e um teste impõe o caso da
  composição: no máximo um `@Bean` por classe de configuração.

O erro de domínio é **dado**, e não exceção: as operações devolvem um
`Resultado`, e só a borda o traduz em status HTTP.

## Qualidade

```bash
./gradlew test    # a suíte inteira
./gradlew build   # compila, testa e gera o Javadoc
```

- **1332 testes**: unidade, integração com o H2 real, contrato, concorrência e
  arquitetura.
- **Nenhum aviso é aceito.** O build falha por aviso do compilador, por membro
  público sem Javadoc e por Javadoc inválido.
- **O CI** roda a compilação, o Javadoc, o `editorconfig`, o `markdownlint` e os
  testes em cada PR.
- **Comparativo e segurança.** O roteiro `scripts/medir-contra-o-original.sh`
  sobe o sistema ao lado da versão original do desafio e mede por HTTP. Com 50.000
  consumidores, listar leva cerca de 80 vezes menos tempo e devolve 846 bytes, em
  vez de 21 MB. A bateria de segurança passa em 14 de 16 verificações, contra 4 do
  original. Os números e as condições estão em
  [`PERFORMANCE.md`](docs/padronizacao/qualidade/PERFORMANCE.md) e
  [`SEGURANCA.md`](docs/padronizacao/qualidade/SEGURANCA.md).

## Documentação do projeto

| Documento | Assunto |
|---|---|
| [`docs/padronizacao`](docs/PADRONIZACAO_INDEX.md) | As regras de código, qualidade e entrega, e as decisões tomadas |
| [`docs/DESAFIO.md`](docs/DESAFIO.md) | O desafio que originou o projeto: o que pedia e o que foi feito |
| README de cada camada | Os pacotes, os testes e a evolução da camada, PR a PR |

A padronização foi escrita **antes** do código, e por isso os exemplos dos
documentos foram corrigidos durante a refatoração para refletir o que se
construiu. Onde um exemplo divergiu, valeu o texto conceitual.
