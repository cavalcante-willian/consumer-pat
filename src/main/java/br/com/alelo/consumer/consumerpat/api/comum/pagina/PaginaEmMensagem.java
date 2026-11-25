package br.com.alelo.consumer.consumerpat.api.comum.pagina;

import br.com.alelo.consumer.consumerpat.dominio.pagina.Pagina;

import java.util.function.Function;

/**
 * Converte uma página do domínio no envelope da API.
 */
public final class PaginaEmMensagem {

    private PaginaEmMensagem() {
    }

    /**
     * Monta o envelope.
     *
     * @param pagina       a página do domínio
     * @param comoMensagem como cada item vira a mensagem de saída
     * @param <T>          o tipo do item do domínio
     * @param <M>          o tipo da mensagem do item
     * @return o envelope, com o total de páginas
     */
    public static <T, M> MensagemPagina<M> montar(Pagina<T> pagina, Function<T, M> comoMensagem) {
        return new MensagemPagina<>(pagina.itens().stream().map(comoMensagem).toList(),
                pagina.pagina(), pagina.tamanhoPagina(), pagina.total(), pagina.totalPaginas());
    }
}
