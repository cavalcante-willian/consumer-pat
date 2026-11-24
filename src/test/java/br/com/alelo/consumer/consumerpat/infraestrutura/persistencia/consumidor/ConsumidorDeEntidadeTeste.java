package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Telefone;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsumidorDeEntidadeTeste {

    private static ConsumidorEntidade linha() {
        var linha = AtualizadorDeEntidadeDeConsumidor.atualizar(new ConsumidorEntidade(),
                FabricaDeConsumidores.dados());
        linha.setId(7L);
        linha.setVersao(4L);
        return linha;
    }

    @Test
    void deve_dar_o_id_e_a_versao_da_linha_quando_converter() {
        // Agir
        var consumidor = ConsumidorDeEntidade.converter(linha(), List.of());

        // Verificar
        assertThat(consumidor.id()).isEqualTo(new ConsumidorId(7));
        assertThat(consumidor.versao()).isEqualTo(4);
    }

    @Test
    void deve_dar_os_dados_do_cadastro_quando_converter() {
        // Agir
        var consumidor = ConsumidorDeEntidade.converter(linha(), List.of());

        // Verificar
        assertThat(consumidor.dados()).isEqualTo(FabricaDeConsumidores.dados());
    }

    @Test
    void deve_dar_os_telefones_ausentes_quando_as_colunas_sao_nulas() {
        // Agir
        var consumidor = ConsumidorDeEntidade.converter(linha(), List.of());

        // Verificar
        assertThat(consumidor.dados().contato().telefoneResidencial()).isEmpty();
        assertThat(consumidor.dados().contato().telefone()).isEmpty();
    }

    @Test
    void deve_dar_os_telefones_quando_as_colunas_estao_preenchidas() {
        // Preparar
        var linha = linha();
        linha.setTelefoneResidencial("1133334444");

        // Agir
        var consumidor = ConsumidorDeEntidade.converter(linha, List.of());

        // Verificar
        assertThat(consumidor.dados().contato().telefoneResidencial())
                .contains(new Telefone("1133334444"));
    }

    @Test
    void deve_levar_os_cartoes_recebidos_quando_converter() {
        // Preparar
        var cartao = FabricaDeCartoes.doTipo(TipoCartao.FARMACIA).construir();

        // Agir
        var consumidor = ConsumidorDeEntidade.converter(linha(), List.<Cartao>of(cartao));

        // Verificar
        assertThat(consumidor.cartoes().cartoes()).containsExactly(cartao);
    }

    @Test
    void deve_lancar_quando_a_linha_tem_um_dado_que_os_tipos_do_dominio_recusam() {
        // Preparar: um dado gravado por fora, que não passaria pelos tipos na ida
        var linha = linha();
        linha.setCep("123");

        // Agir e Verificar
        assertThatThrownBy(() -> ConsumidorDeEntidade.converter(linha, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
