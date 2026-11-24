package br.com.alelo.consumer.consumerpat.api.comum.resposta;

import br.com.alelo.consumer.consumerpat.api.comum.problema.MensagemProblema;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroCartaoNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.regradenegocio.ErroSaldoInsuficiente;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroValorNaoPositivo;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RespostaDeResultadoTeste {

    private static final String INSTANCIA = "/api/v1/compras";

    static Stream<Arguments> errosPorCategoria() {
        return Stream.of(Arguments.of(new ErroValorNaoPositivo(), 400),
                Arguments.of(new ErroCartaoNaoEncontrado(new NumeroDeCartao("5555000011112222"),
                        TipoCartao.FARMACIA), 404),
                Arguments.of(new ErroSaldoInsuficiente(new Dinheiro(BigDecimal.ONE),
                        new Dinheiro(BigDecimal.TEN)), 422));
    }

    @Test
    void deve_ter_o_status_declarado_e_o_corpo_na_mensagem_quando_o_resultado_e_sucesso() {
        // Preparar
        var resultado = new Sucesso<String, ErroDominio>("valor");

        // Agir
        var resposta = RespostaDeResultado.responder(resultado, texto -> "mensagem:" + texto,
                HttpStatus.CREATED, INSTANCIA);

        // Verificar
        assertThat(resposta.getStatusCodeValue()).isEqualTo(201);
        assertThat(resposta.getBody()).isEqualTo("mensagem:valor");
    }

    @Test
    void deve_nao_usar_o_tipo_de_midia_de_erro_quando_o_resultado_e_sucesso() {
        // Preparar
        var resultado = new Sucesso<String, ErroDominio>("valor");

        // Agir
        var resposta = RespostaDeResultado.responder(resultado, texto -> texto, HttpStatus.CREATED,
                INSTANCIA);

        // Verificar
        assertThat(resposta.getHeaders().getContentType())
                .isNotEqualTo(MediaType.valueOf("application/problem+json"));
    }

    @ParameterizedTest
    @MethodSource("errosPorCategoria")
    void deve_ter_o_status_da_categoria_do_erro_quando_o_resultado_e_falha(ErroDominio erro,
            int statusEsperado) {
        // Preparar
        var resultado = new Falha<String, ErroDominio>(erro);

        // Agir
        var resposta = RespostaDeResultado.responder(resultado, texto -> texto, HttpStatus.CREATED,
                INSTANCIA);

        // Verificar
        assertThat(resposta.getStatusCodeValue()).isEqualTo(statusEsperado);
        assertThat(resposta.getHeaders().getContentType())
                .isEqualTo(MediaType.valueOf("application/problem+json"));
        assertThat(resposta.getBody()).isInstanceOf(MensagemProblema.class);
    }

    @Test
    void deve_nao_chamar_a_conversao_da_mensagem_quando_o_resultado_e_falha() {
        // Preparar
        var resultado = new Falha<String, ErroDominio>(new ErroValorNaoPositivo());

        // Agir e Verificar
        var resposta = RespostaDeResultado.responder(resultado, texto -> {
            throw new AssertionError("a conversão não devia rodar numa falha");
        }, HttpStatus.CREATED, INSTANCIA);
        assertThat(resposta.getStatusCodeValue()).isEqualTo(400);
    }
}
