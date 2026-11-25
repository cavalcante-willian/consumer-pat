package br.com.alelo.consumer.consumerpat.api.comum.resposta;

import br.com.alelo.consumer.consumerpat.api.comum.problema.MensagemProblema;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RespostaDeProblemaTeste {

    private static final MensagemProblema PROBLEMA = new MensagemProblema("urn:x", "Título", 422,
            "detalhe", "/api/v1/compras", null, Map.of());

    @Test
    void deve_ter_o_status_do_proprio_corpo_quando_montar_a_resposta() {
        // Agir
        var resposta = RespostaDeProblema.de(PROBLEMA);

        // Verificar
        assertThat(resposta.getStatusCodeValue()).isEqualTo(422);
    }

    @Test
    void deve_ter_o_tipo_de_midia_de_problem_details_quando_montar_a_resposta() {
        // Agir
        var resposta = RespostaDeProblema.de(PROBLEMA);

        // Verificar
        assertThat(resposta.getHeaders().getContentType())
                .isEqualTo(MediaType.valueOf("application/problem+json"));
    }

    @Test
    void deve_levar_o_corpo_de_erro_quando_montar_a_resposta() {
        // Agir
        var resposta = RespostaDeProblema.de(PROBLEMA);

        // Verificar
        assertThat(resposta.getBody()).isSameAs(PROBLEMA);
    }
}
