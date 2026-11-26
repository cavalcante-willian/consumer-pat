package br.com.alelo.consumer.consumerpat.api.comum.documentacao;

import io.swagger.v3.oas.models.tags.Tag;

import java.util.List;

/**
 * Os grupos em que a especificação organiza as operações: um por recurso, e não um por classe de
 * controller.
 *
 * <p>
 * Os nomes são constantes para que a anotação {@code @Tag} de cada controller e a lista declarada
 * aqui não possam divergir: um nome errado numa anotação seria um grupo novo, sem descrição.
 */
public final class TagsDaApi {

    /** O cadastro, a atualização, a leitura e a listagem de consumidores. */
    public static final String CONSUMIDORES = "Consumidores";

    /** O crédito e a compra, que mexem no saldo dos cartões. */
    public static final String CARTOES = "Cartões";

    /** A consulta do estado da aplicação e das suas dependências. */
    public static final String SAUDE = "Saúde";

    private TagsDaApi() {
    }

    /**
     * Devolve os grupos, na ordem em que aparecem na documentação.
     *
     * @return os grupos, cada um com a sua descrição
     */
    public static List<Tag> lista() {
        return List.of(
                new Tag().name(CONSUMIDORES).description(
                        "Cadastro, atualização, leitura e listagem paginada. Cada consumidor tem "
                                + "um cartão de cada tipo."),
                new Tag().name(CARTOES).description(
                        "Crédito e compra. O saldo nunca fica negativo, e a compra só debita o "
                                + "cartão do tipo do estabelecimento."),
                new Tag().name(SAUDE).description(
                        "Estado da aplicação e do banco, para o balanceador e o operador."));
    }
}
