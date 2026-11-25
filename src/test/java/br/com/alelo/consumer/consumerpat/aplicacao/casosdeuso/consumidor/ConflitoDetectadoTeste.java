package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.documento.Documento;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroDocumentoJaCadastrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroNumeroDeCartaoJaCadastrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroVersaoDivergente;
import br.com.alelo.consumer.consumerpat.dominio.erros.indisponivel.ErroBancoIndisponivel;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroCartaoNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroConsumidorNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.regradenegocio.ErroSaldoInsuficiente;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroValorNaoPositivo;
import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ConflitoDetectadoTeste {

    private final SaidaDeLogFake saida = new SaidaDeLogFake();

    private static Stream<ErroDominio> naoConflitos() {
        return Stream.of(new ErroConsumidorNaoEncontrado(new ConsumidorId(1)),
                new ErroCartaoNaoEncontrado(new NumeroDeCartao("5555000011112222"),
                        TipoCartao.ALIMENTACAO),
                new ErroSaldoInsuficiente(Dinheiro.ZERO, Dinheiro.ZERO), new ErroValorNaoPositivo(),
                new ErroBancoIndisponivel("x", "y"));
    }

    @Test
    void deve_registrar_aviso_com_a_operacao_e_o_codigo_quando_o_erro_e_de_conflito() {
        // Agir
        ConflitoDetectado.registrar(saida, "atualizar-consumidor",
                new ErroVersaoDivergente(new ConsumidorId(1), 3));

        // Verificar
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.AVISO);
            assertThat(evento.evento()).isEqualTo("conflito.detectado");
            assertThat(evento.valorDe("operacao")).contains("atualizar-consumidor");
            assertThat(evento.valorDe("codigo")).contains(ErroVersaoDivergente.CODIGO);
        });
    }

    @Test
    void deve_registrar_o_conflito_de_documento_sem_o_documento() {
        // Agir
        ConflitoDetectado.registrar(saida, "criar-consumidor",
                new ErroDocumentoJaCadastrado(new Documento("12345678901")));

        // Verificar: o código identifica a regra, e o valor que colidiu fica fora do log
        assertThat(saida.eventos().get(0).valorDe("codigo"))
                .contains(ErroDocumentoJaCadastrado.CODIGO);
        assertThat(saida.eventos().get(0).texto()).doesNotContain("12345678901");
    }

    @Test
    void deve_registrar_o_conflito_de_numero_sem_o_numero() {
        // Agir
        ConflitoDetectado.registrar(saida, "criar-consumidor",
                new ErroNumeroDeCartaoJaCadastrado(new NumeroDeCartao("5555000011112222")));

        // Verificar
        assertThat(saida.eventos().get(0).valorDe("codigo"))
                .contains(ErroNumeroDeCartaoJaCadastrado.CODIGO);
        assertThat(saida.eventos().get(0).texto()).doesNotContain("5555000011112222");
    }

    @ParameterizedTest
    @MethodSource("naoConflitos")
    void deve_nao_registrar_nada_quando_o_erro_nao_e_de_conflito(ErroDominio erro) {
        // Agir
        ConflitoDetectado.registrar(saida, "criar-consumidor", erro);

        // Verificar: ausência, regra de negócio, validação e indisponibilidade não são conflito
        assertThat(saida.eventos()).isEmpty();
    }
}
