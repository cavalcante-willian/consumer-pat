package br.com.alelo.consumer.consumerpat.api.comum.documentacao;

import io.swagger.v3.oas.models.info.Info;

/**
 * O cabeçalho da especificação OpenAPI: o título, o que a API faz e a versão.
 *
 * <p>
 * A descrição diz o que vale para a API inteira e que o leitor não acharia em nenhuma operação: o
 * formato dos erros, como o dinheiro aparece e o cabeçalho que liga a resposta ao log. Não há
 * contato nem licença, porque a API é um desafio técnico e não um produto.
 */
public final class InformacoesDaApi {

    /** O título da API. */
    public static final String TITULO = "Consumer PAT — API de cartões de benefício";

    private static final String DESCRICAO = """
            Cadastro de consumidores com cartões de benefício, crédito nos cartões e compras, \
            com o preço de cada tipo: alimentação com 10% de desconto, combustível com acréscimo \
            de 35% e farmácia pelo valor integral.

            **Erros.** Toda falha responde `application/problem+json`, no formato da RFC 7807, \
            com um `type` estável, os campos inválidos em `errors` e o `idRequisicao`.

            **Dinheiro.** Os valores monetários das respostas são texto decimal, com seis casas, \
            para não perder precisão.

            **Rastreio.** Toda resposta traz o cabeçalho `X-Request-Id`, o mesmo que aparece no \
            log e no corpo de erro. Se a requisição o trouxer, ele é aproveitado.""";

    private InformacoesDaApi() {
    }

    /**
     * Monta o cabeçalho da especificação.
     *
     * @param versao a versão da aplicação
     * @return o título, a descrição e a versão
     */
    public static Info montar(String versao) {
        return new Info().title(TITULO).description(DESCRICAO).version(versao);
    }
}
