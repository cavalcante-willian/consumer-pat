package br.com.alelo.consumer.consumerpat.medicao;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Os quatro cenários que só pedem ao sistema uma lista de pedidos e a medem: cada um tem de pedir a
 * lista certa.
 */
class CenariosDePedidosTeste {

    private final AlvoFalso alvo = new AlvoFalso("falso", "800.00");

    @Test
    void deve_medir_as_listagens_do_sistema_quando_for_o_cenario_de_listagem() throws Exception {
        // Agir
        var medidas = new CenarioDeListagem(100).medir(alvo);

        // Verificar
        assertThat(medidas).extracting(Medida::chave).containsExactly("listagem-primeira-pagina");
    }

    @Test
    void deve_medir_os_creditos_de_cada_tipo_e_posicao_quando_for_o_cenario_de_credito()
            throws Exception {
        // Agir
        var medidas = new CenarioDeCredito(100).medir(alvo);

        // Verificar: três tipos de cartão em três pontos da base
        assertThat(medidas).hasSize(9).extracting(Medida::chave).contains(
                "credito-alimentacao-inicio", "credito-farmacia-meio", "credito-combustivel-fim");
    }

    @Test
    void deve_medir_as_compras_de_cada_tipo_e_posicao_quando_for_o_cenario_de_compra()
            throws Exception {
        // Agir
        var medidas = new CenarioDeCompra(100).medir(alvo);

        // Verificar
        assertThat(medidas).hasSize(9).extracting(Medida::chave).contains(
                "compra-alimentacao-inicio", "compra-farmacia-meio", "compra-combustivel-fim");
    }

    @Test
    void deve_medir_a_consulta_por_id_quando_for_o_cenario_de_consulta() throws Exception {
        // Agir
        var medidas = new CenarioDeConsultaPorId(100).medir(alvo);

        // Verificar
        assertThat(medidas).extracting(Medida::chave).containsExactly("consulta-por-id");
    }
}
