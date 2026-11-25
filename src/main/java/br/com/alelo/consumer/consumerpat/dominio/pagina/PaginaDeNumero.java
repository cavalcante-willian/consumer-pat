package br.com.alelo.consumer.consumerpat.dominio.pagina;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroPaginaInvalida;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Optional;

/**
 * Lê o número da página pedida, vindo de fora do domínio.
 */
public final class PaginaDeNumero {

    private PaginaDeNumero() {
    }

    /**
     * Confere que a página começa em 1.
     *
     * @param numero o número recebido
     * @return o mesmo número, ou {@link ErroPaginaInvalida}
     */
    public static Resultado<Integer, ErroDominio> converter(int numero) {
        return AusenciaComoErro.converter(Optional.of(numero).filter(pagina -> pagina >= 1),
                ErroPaginaInvalida::new);
    }
}
