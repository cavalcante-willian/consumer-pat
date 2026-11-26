package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeCorsTeste {

    private final Verificacao verificacao = new VerificacaoDeCors();

    @Test
    void deve_proteger_quando_o_pre_voo_nao_autoriza_a_origem() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(true));

        // Verificar
        assertThat(achado.chave()).isEqualTo("cors-fechado");
        assertThat(achado.protegido()).isTrue();
        assertThat(achado.evidencia()).contains("status 403").contains("origem permitida: nenhuma");
    }

    @Test
    void deve_expor_quando_o_pre_voo_autoriza_a_origem_estranha() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.preVoo = SondaFalsa.resposta(200, "",
                Map.of("access-control-allow-origin", "http://origem-estranha.exemplo"));

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("origem permitida: http://origem-estranha.exemplo");
    }

    @Test
    void deve_expor_quando_o_pre_voo_libera_qualquer_origem() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.preVoo = SondaFalsa.resposta(200, "", Map.of("access-control-allow-origin", "*"));

        // Agir e Verificar
        assertThat(verificacao.verificar(sonda).protegido()).isFalse();
    }
}
