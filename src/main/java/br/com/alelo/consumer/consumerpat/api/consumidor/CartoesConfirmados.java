package br.com.alelo.consumer.consumerpat.api.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartaoDeTexto;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartaoDoNome;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Monta o número de cada tipo de cartão de um cadastro cujos cartões já foram conferidos.
 *
 * <p>
 * Só se chama depois de {@link CamposDosCartoes} não achar nenhuma falha. As chaves do mapa de
 * entrada são únicas, e por isso o mapa de saída nunca tem tipo repetido.
 */
public final class CartoesConfirmados {

    private CartoesConfirmados() {
    }

    /**
     * Monta o mapa.
     *
     * @param numerosCartoes o número de cada tipo, com o tipo pelo nome, já conferido
     * @return o número de cada tipo, nos tipos do domínio
     */
    public static Map<TipoCartao, NumeroDeCartao> montar(Map<String, String> numerosCartoes) {
        return numerosCartoes.entrySet().stream().collect(Collectors.toMap(
                cartao -> ValorConfirmado.de(TipoCartaoDoNome.converter(cartao.getKey())),
                cartao -> ValorConfirmado.de(NumeroDeCartaoDeTexto.converter(cartao.getValue()))));
    }
}
