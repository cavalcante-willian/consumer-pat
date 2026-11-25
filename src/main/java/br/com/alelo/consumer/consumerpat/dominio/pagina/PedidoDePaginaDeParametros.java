package br.com.alelo.consumer.consumerpat.dominio.pagina;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.CampoValidado;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ValidacoesReunidas;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.List;

/**
 * Lê os quatro parâmetros da listagem de uma vez, com todos os erros.
 *
 * <p>
 * Os parâmetros são independentes entre si, e quem pede {@code pagina=0}, {@code tamanhoPagina=500}
 * e {@code ordenacao=xyz} recebe os três erros na mesma resposta, e não um por tentativa.
 */
public final class PedidoDePaginaDeParametros {

    private PedidoDePaginaDeParametros() {
    }

    /**
     * Converte os parâmetros.
     *
     * @param pagina        a página pedida
     * @param tamanhoPagina o tamanho pedido
     * @param ordenacao     o campo de ordenação, pelo nome
     * @param direcao       o sentido, pelo nome
     * @return o pedido, ou um erro de validação com todos os parâmetros inválidos
     */
    public static Resultado<PedidoDePagina, ErroDominio> converter(int pagina, int tamanhoPagina,
            String ordenacao, String direcao) {
        var paginaLida = PaginaDeNumero.converter(pagina);
        var tamanhoLido = TamanhoDePaginaDeNumero.converter(tamanhoPagina);
        var ordenacaoLida = OrdenacaoDeConsumidorDeTexto.converter(ordenacao);
        var direcaoLida = DirecaoDeOrdenacaoDeTexto.converter(direcao);
        return ValidacoesReunidas
                .reunir(List.of(new CampoValidado("pagina", paginaLida),
                        new CampoValidado("tamanhoPagina", tamanhoLido),
                        new CampoValidado("ordenacao", ordenacaoLida),
                        new CampoValidado("direcao", direcaoLida)))
                .<Resultado<PedidoDePagina, ErroDominio>>map(Falha::new)
                .orElseGet(() -> paginaLida.encadear(numero -> tamanhoLido
                        .encadear(tamanho -> ordenacaoLida.encadear(campo -> direcaoLida.mapear(
                                sentido -> new PedidoDePagina(numero, tamanho, campo, sentido))))));
    }
}
