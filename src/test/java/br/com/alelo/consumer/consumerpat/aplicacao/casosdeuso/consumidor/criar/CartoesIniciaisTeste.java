package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CartoesIniciaisTeste {

    private static final NumeroDeCartao UM = new NumeroDeCartao("5555000011112222");
    private static final NumeroDeCartao DOIS = new NumeroDeCartao("5555000011113333");
    private static final NumeroDeCartao TRES = new NumeroDeCartao("5555000011114444");

    @Test
    void deve_criar_um_cartao_de_saldo_zero_por_tipo_quando_o_pedido_traz_os_tres() {
        // Agir
        var cartoes = CartoesIniciais.de(Map.of(TipoCartao.ALIMENTACAO, UM, TipoCartao.FARMACIA,
                DOIS, TipoCartao.COMBUSTIVEL, TRES));

        // Verificar
        assertThat(cartoes.cartoes()).containsExactly(
                new Cartao(UM, TipoCartao.ALIMENTACAO, Dinheiro.ZERO),
                new Cartao(DOIS, TipoCartao.FARMACIA, Dinheiro.ZERO),
                new Cartao(TRES, TipoCartao.COMBUSTIVEL, Dinheiro.ZERO));
    }

    @Test
    void deve_criar_so_o_cartao_pedido_quando_o_pedido_traz_um_tipo() {
        // Agir
        var cartoes = CartoesIniciais.de(Map.of(TipoCartao.FARMACIA, DOIS));

        // Verificar
        assertThat(cartoes.cartoes())
                .containsExactly(new Cartao(DOIS, TipoCartao.FARMACIA, Dinheiro.ZERO));
    }

    @Test
    void deve_criar_nenhum_cartao_quando_o_pedido_nao_traz_numeros() {
        // Agir
        var cartoes = CartoesIniciais.de(Map.of());

        // Verificar
        assertThat(cartoes.cartoes()).isEmpty();
    }

    @Test
    void deve_ordenar_pelo_tipo_quando_o_mapa_chega_em_outra_ordem() {
        // Preparar
        var emOutraOrdem = new LinkedHashMap<TipoCartao, NumeroDeCartao>();
        emOutraOrdem.put(TipoCartao.COMBUSTIVEL, TRES);
        emOutraOrdem.put(TipoCartao.ALIMENTACAO, UM);

        // Agir
        var cartoes = CartoesIniciais.de(emOutraOrdem);

        // Verificar
        assertThat(cartoes.cartoes()).extracting(Cartao::tipo)
                .containsExactly(TipoCartao.ALIMENTACAO, TipoCartao.COMBUSTIVEL);
    }

    @Test
    void deve_nunca_criar_cartao_com_saldo_quando_o_pedido_nao_escolhe_o_saldo() {
        // Agir
        var cartoes = CartoesIniciais.de(Map.of(TipoCartao.ALIMENTACAO, UM));

        // Verificar
        assertThat(cartoes.cartoes()).allMatch(cartao -> cartao.saldo().equals(Dinheiro.ZERO));
    }
}
