package br.com.alelo.consumer.consumerpat.dominio.pagina;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroTamanhoDePaginaInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Optional;

/**
 * Lê o tamanho de página pedido, vindo de fora do domínio.
 */
public final class TamanhoDePaginaDeNumero {

    private TamanhoDePaginaDeNumero() {
    }

    /**
     * Confere que o tamanho vai de 1 até o teto.
     *
     * @param numero o tamanho recebido
     * @return o mesmo número, ou {@link ErroTamanhoDePaginaInvalido}
     */
    public static Resultado<Integer, ErroDominio> converter(int numero) {
        return AusenciaComoErro
                .converter(
                        Optional.of(numero)
                                .filter(tamanho -> tamanho >= 1
                                        && tamanho <= PedidoDePagina.TAMANHO_MAXIMO),
                        ErroTamanhoDePaginaInvalido::new);
    }
}
