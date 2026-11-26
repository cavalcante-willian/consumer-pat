package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeLimiteDeTaxaTeste {

    private final Verificacao verificacao = new VerificacaoDeLimiteDeTaxa();

    @Test
    void deve_proteger_quando_o_sistema_passa_a_responder_429_no_meio_da_rajada() throws Exception {
        // Preparar: a partir da requisição cento e uma, o limite estoura
        var sonda = new SondaFalsa(true);
        sonda.aCadaRequisicao = n -> SondaFalsa.resposta(Map.of(true, 429, false, 200).get(n > 100),
                "");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.chave()).isEqualTo("limite-de-taxa");
        assertThat(achado.protegido()).isTrue();
        assertThat(achado.evidencia()).isEqualTo("status {200=100, 429=100}");
    }

    @Test
    void deve_expor_quando_as_duzentas_requisicoes_passam_sem_limite() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar: duzentas requisições, todas 200
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).isEqualTo("status {200=200}");
    }
}
