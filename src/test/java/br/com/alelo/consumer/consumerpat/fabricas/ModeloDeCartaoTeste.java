package br.com.alelo.consumer.consumerpat.fabricas;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModeloDeCartaoTeste {

    @Test
    void deve_comecar_com_o_numero_e_o_saldo_padrao_quando_partir_de_um_tipo() {
        // Agir
        var modelo = FabricaDeCartoes.doTipo(TipoCartao.COMBUSTIVEL);

        // Verificar
        assertThat(modelo.tipo()).isEqualTo(TipoCartao.COMBUSTIVEL);
        assertThat(modelo.numero()).isEqualTo(FabricaDeCartoes.NUMERO_PADRAO);
        assertThat(modelo.saldo()).isEqualTo(FabricaDeCartoes.SALDO_PADRAO);
    }

    @Test
    void deve_trocar_so_o_saldo_e_manter_o_original_quando_trocar_o_saldo() {
        // Preparar
        var original = FabricaDeCartoes.doTipo(TipoCartao.FARMACIA);

        // Agir
        var trocado = original.comSaldo("7.50");

        // Verificar
        assertThat(trocado.saldo()).isEqualTo("7.50");
        assertThat(trocado.numero()).isEqualTo(original.numero());
        assertThat(trocado.tipo()).isEqualTo(original.tipo());
        assertThat(original.saldo()).isEqualTo(FabricaDeCartoes.SALDO_PADRAO);
    }

    @Test
    void deve_trocar_so_o_numero_e_manter_o_original_quando_trocar_o_numero() {
        // Preparar
        var original = FabricaDeCartoes.doTipo(TipoCartao.FARMACIA);

        // Agir
        var trocado = original.comNumero("1234567890123");

        // Verificar
        assertThat(trocado.numero()).isEqualTo("1234567890123");
        assertThat(trocado.saldo()).isEqualTo(original.saldo());
        assertThat(original.numero()).isEqualTo(FabricaDeCartoes.NUMERO_PADRAO);
    }

    @Test
    void deve_construir_o_cartao_do_dominio_quando_construir() {
        // Agir
        var cartao = FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("33.33").construir();

        // Verificar
        assertThat(cartao.numero()).isEqualTo(new NumeroDeCartao(FabricaDeCartoes.NUMERO_PADRAO));
        assertThat(cartao.tipo()).isEqualTo(TipoCartao.ALIMENTACAO);
        assertThat(cartao.saldo()).isEqualTo(new Dinheiro(new BigDecimal("33.33")));
    }

    @Test
    void deve_lancar_so_na_construcao_quando_o_numero_do_modelo_e_invalido() {
        // Preparar
        var modelo = FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comNumero("123");

        // Agir e Verificar
        assertThatThrownBy(modelo::construir).isInstanceOf(IllegalArgumentException.class);
    }
}
