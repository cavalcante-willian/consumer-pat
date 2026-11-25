package br.com.alelo.consumer.consumerpat.api.consumidor.criar;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;

import java.net.URI;

/**
 * O endereço de leitura do consumidor recém-criado, para o {@code Location} do {@code 201}.
 *
 * <p>
 * É relativo à rota do cadastro, e não monta host nem porta: quem fala com a API já sabe onde ela
 * está, e um endereço absoluto dependeria de cabeçalhos de proxy que a API não controla.
 */
public final class LocalizacaoDoConsumidor {

    private LocalizacaoDoConsumidor() {
    }

    /**
     * Monta o endereço.
     *
     * @param rotaDoCadastro o caminho do cadastro, como veio na requisição
     * @param consumidor     o consumidor criado
     * @return o caminho do cadastro seguido do id
     */
    public static URI de(String rotaDoCadastro, Consumidor consumidor) {
        return URI.create(rotaDoCadastro + "/" + consumidor.id().valor());
    }
}
