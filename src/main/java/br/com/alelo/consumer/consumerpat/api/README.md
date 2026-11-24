# api

Camada de API: a fronteira com o mundo e a **composition root**. É o único lugar que
conhece todas as camadas e que decide qual implementação entra em cada porta. A
infraestrutura só é nomeada em `api.configuracao`.

Por enquanto a camada tem só a composition root. Os controllers, as mensagens de
entrada e saída e o tratamento de erro vêm com o item que versiona a API; até lá o
legado segue servindo as rotas.

## Propósito

- **`configuracao`** — liga o que as camadas de baixo declaram.
  - `ConfiguracaoDeRelogio` escolhe o **único** `Clock` da aplicação: o de parede, em
    UTC. O domínio, os casos de uso e a auditoria do banco recebem este mesmo bean, de
    modo que um teste que o troque controla o tempo de ponta a ponta. Nenhum código
    chama `Instant.now()` por conta própria.
  - `ConfiguracaoDeAuditoria` liga a auditoria automática do JPA, com o instante do
    `Clock`. As colunas `data_criacao` e `data_atualizacao` são preenchidas pelo
    mecanismo, e nenhum caso de uso as atribui.
  - `ConfiguracaoDePortas` escolhe a implementação de cada porta do domínio. Importa a
    **classe** de cada adaptador, e não instancia nenhum, porque os repositórios que
    os adaptadores recebem são pacote-privados e esta camada não consegue nem escrever
    o tipo deles.

## Estrutura

Saída real de `tree` (regenerar e colar aqui a cada PR que mudar a árvore — ver
`docs/padronizacao/codigo/DOCUMENTACAO.md`, "READMEs Por Camada"):

```text
api
├── configuracao
│   ├── ConfiguracaoDeAuditoria.java      # @EnableJpaAuditing, com o instante do Clock
│   ├── ConfiguracaoDePortas.java         # importa os adaptadores, um por porta
│   ├── ConfiguracaoDeRelogio.java        # o único Clock: de parede, em UTC
│   └── package-info.java
└── package-info.java
```

## Dependências externas

Spring (contexto e configuração) e Spring Data (a auditoria). Nenhuma dependência nova
no build.

## Testes

Os testes ficam em `src/test/java/.../api/`. `ConfiguracaoDePortasTeste` sobe o
contexto e prova que cada porta do domínio tem o seu adaptador e que o relógio é o de
parede, em UTC. Os testes de persistência importam `ConfiguracaoDeAuditoria`
explicitamente, porque `@DataJpaTest` não carrega as configurações da API, com um
relógio controlável no lugar do de parede.

## Evolução

| PR | Mudança no contrato desta camada |
|---|---|
| `#14` | Cria a camada, só com a composition root: o relógio, a auditoria e as portas do cartão e do extrato. |
