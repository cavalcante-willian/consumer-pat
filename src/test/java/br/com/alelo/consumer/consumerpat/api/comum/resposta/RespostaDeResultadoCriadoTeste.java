package br.com.alelo.consumer.consumerpat.api.comum.resposta;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroValorNaoPositivo;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class RespostaDeResultadoCriadoTeste {

    @Test
    void deve_ser_201_com_o_location_e_o_corpo_quando_o_resultado_e_sucesso() {
        // Agir
        var resposta = RespostaDeResultadoCriado.responder(new Sucesso<Integer, ErroDominio>(7),
                valor -> "corpo" + valor, valor -> URI.create("/rota/" + valor), "/rota");

        // Verificar
        assertThat(resposta.getStatusCodeValue()).isEqualTo(201);
        assertThat(resposta.getHeaders().getLocation()).isEqualTo(URI.create("/rota/7"));
        assertThat(resposta.getBody()).isEqualTo("corpo7");
    }

    @Test
    void deve_ser_o_problema_sem_location_quando_o_resultado_e_falha() {
        // Agir
        var resposta = RespostaDeResultadoCriado.responder(
                new Falha<Integer, ErroDominio>(new ErroValorNaoPositivo()), valor -> "corpo",
                valor -> URI.create("/nunca"), "/rota");

        // Verificar
        assertThat(resposta.getStatusCodeValue()).isEqualTo(400);
        assertThat(resposta.getHeaders().getLocation()).isNull();
        assertThat(resposta.getHeaders().getContentType())
                .isEqualTo(MediaType.valueOf("application/problem+json"));
    }

    @Test
    void deve_nao_chamar_a_localizacao_quando_o_resultado_e_falha() {
        // Agir
        var resposta = RespostaDeResultadoCriado.responder(
                new Falha<Integer, ErroDominio>(new ErroValorNaoPositivo()), valor -> "corpo",
                valor -> {
                    throw new AssertionError("a localização não devia ser calculada");
                }, "/rota");

        // Verificar
        assertThat(resposta.getStatusCodeValue()).isEqualTo(400);
    }
}
