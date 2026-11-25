package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Os três erros de validação que a API do consumidor precisa: a entrada com vários campos
 * inválidos, o tipo de cartão que não existe e o id que não é positivo.
 */
class ErrosNovosDeValidacaoTeste {

    @Test
    void deve_ser_validacao_com_os_campos_que_recebeu_quando_a_entrada_tem_varios_invalidos() {
        // Preparar
        var campos = Map.of("cep", List.of("deve ter 8 dígitos"));

        // Agir
        var erro = new ErroEntradaInvalida(campos);

        // Verificar
        assertThat(erro.categoria()).isEqualTo(CategoriaErro.VALIDACAO);
        assertThat(erro.codigo()).isEqualTo(ErroEntradaInvalida.CODIGO);
        assertThat(erro.camposComErro()).isEqualTo(campos);
    }

    @Test
    void deve_listar_os_nomes_aceitos_quando_o_tipo_de_cartao_nao_existe() {
        // Agir
        var erro = new ErroTipoDeCartaoInvalido();

        // Verificar
        assertThat(erro.categoria()).isEqualTo(CategoriaErro.VALIDACAO);
        assertThat(erro.codigo()).isEqualTo(ErroTipoDeCartaoInvalido.CODIGO);
        assertThat(erro.mensagem()).contains("ALIMENTACAO", "FARMACIA", "COMBUSTIVEL");
        assertThat(erro.camposComErro()).containsOnlyKeys("tipo");
    }

    @Test
    void deve_apontar_o_campo_id_quando_o_id_do_consumidor_nao_e_positivo() {
        // Agir
        var erro = new ErroConsumidorIdInvalido();

        // Verificar
        assertThat(erro.categoria()).isEqualTo(CategoriaErro.VALIDACAO);
        assertThat(erro.codigo()).isEqualTo(ErroConsumidorIdInvalido.CODIGO);
        assertThat(erro.camposComErro()).containsOnlyKeys("id");
    }
}
