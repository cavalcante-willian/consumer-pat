package br.com.alelo.consumer.consumerpat.api.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartaoDeTexto;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartaoDoNome;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.CampoValidado;

import java.util.List;
import java.util.Map;

/**
 * Lê, um a um, o tipo e o número de cada cartão do cadastro.
 *
 * <p>
 * Os erros saem todos sob o campo {@code numerosCartoes}, e não sob o nome que o domínio daria
 * ({@code numeroCartao}, {@code tipo}): para quem manda o cadastro, o campo é o mapa de cartões.
 */
public final class CamposDosCartoes {

    /** O nome do campo na entrada. */
    public static final String CAMPO = "numerosCartoes";

    private CamposDosCartoes() {
    }

    /**
     * Lê os cartões.
     *
     * @param numerosCartoes o número de cada tipo, com o tipo pelo nome
     * @return duas leituras por cartão, a do tipo e a do número
     */
    public static List<CampoValidado> de(Map<String, String> numerosCartoes) {
        return numerosCartoes.entrySet().stream()
                .flatMap(cartao -> List
                        .of(new CampoValidado(CAMPO, TipoCartaoDoNome.converter(cartao.getKey())),
                                new CampoValidado(CAMPO,
                                        NumeroDeCartaoDeTexto.converter(cartao.getValue())))
                        .stream())
                .toList();
    }
}
