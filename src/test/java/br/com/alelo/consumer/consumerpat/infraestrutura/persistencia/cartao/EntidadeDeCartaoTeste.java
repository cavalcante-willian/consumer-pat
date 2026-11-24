package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidoresDeTeste;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EntidadeDeCartaoTeste {

    @Test
    void deve_levar_o_dono_o_tipo_o_numero_e_o_saldo_quando_criar_a_linha() {
        // Preparar
        var dono = ConsumidoresDeTeste.linha();
        var cartao = FabricaDeCartoes.doTipo(TipoCartao.COMBUSTIVEL).comSaldo("12.50").construir();

        // Agir
        var linha = EntidadeDeCartao.criar(cartao, dono);

        // Verificar
        assertThat(linha.getConsumidor()).isSameAs(dono);
        assertThat(linha.getTipo()).isEqualTo(TipoCartao.COMBUSTIVEL);
        assertThat(linha.getNumero()).isEqualTo(FabricaDeCartoes.NUMERO_PADRAO);
        assertThat(linha.getSaldo()).isEqualByComparingTo(new BigDecimal("12.50"));
    }

    @Test
    void deve_deixar_o_id_e_a_versao_para_o_banco_quando_criar_a_linha() {
        // Agir
        var linha = EntidadeDeCartao.criar(FabricaDeCartoes.doTipo(TipoCartao.FARMACIA).construir(),
                ConsumidoresDeTeste.linha());

        // Verificar
        assertThat(linha.getId()).isNull();
        assertThat(linha.getVersao()).isNull();
    }
}
