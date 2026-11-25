package br.com.alelo.consumer.consumerpat.api;

import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.CreditarCartaoCaso;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.GravadorDaCompra;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.RegistrarCompraCaso;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.CartaoPortaFake;
// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.atualizar.AtualizarConsumidorCaso;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar.CriarConsumidorCaso;
// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.listar.ListarConsumidoresCaso;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.obter.ObterConsumidorCaso;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.ExtratoPortaFake;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.saude.ConsultarSaudeCaso;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaudePortaFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor.ConsumidorPortaFake;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Monta os casos de uso com os fakes das portas e um relógio fixo, para os testes dos controllers
 * exercitarem a API de ponta a ponta sem banco.
 *
 * <p>
 * Os fakes são beans, e o teste os recebe por injeção para semear o estado e conferir o que foi
 * gravado. O relógio parado em {@link #INSTANTE} faz o instante do comprovante ser previsível.
 */
@TestConfiguration
public class ConfiguracaoDeCasosDeUsoComFakes {

    /** O instante em que o relógio dos testes está parado. */
    public static final Instant INSTANTE = Instant.parse("2026-11-24T12:00:00Z");

    @Bean
    CartaoPortaFake cartaoPortaFake() {
        return new CartaoPortaFake();
    }

    @Bean
    SaidaDeLogFake saidaDeLogFake() {
        return new SaidaDeLogFake();
    }

    @Bean
    SaudePortaFake saudePortaFake() {
        return new SaudePortaFake();
    }

    @Bean
    ConsultarSaudeCaso consultarSaudeCaso(SaudePortaFake saude) {
        return new ConsultarSaudeCaso(saude);
    }

    @Bean
    ExtratoPortaFake extratoPortaFake() {
        return new ExtratoPortaFake();
    }

    @Bean
    Clock relogio() {
        return Clock.fixed(INSTANTE, ZoneOffset.UTC);
    }

    @Bean
    RegistrarCompraCaso registrarCompraCaso(CartaoPortaFake cartoes, ExtratoPortaFake extratos,
            Clock relogio) {
        return new RegistrarCompraCaso(cartoes, new GravadorDaCompra(cartoes, extratos), relogio);
    }

    @Bean
    CreditarCartaoCaso creditarCartaoCaso(CartaoPortaFake cartoes) {
        return new CreditarCartaoCaso(cartoes);
    }

    @Bean
    ConsumidorPortaFake consumidorPortaFake() {
        return new ConsumidorPortaFake();
    }

    @Bean
    CriarConsumidorCaso criarConsumidorCaso(ConsumidorPortaFake consumidores) {
        return new CriarConsumidorCaso(consumidores);
    }

    @Bean
    AtualizarConsumidorCaso atualizarConsumidorCaso(ConsumidorPortaFake consumidores) {
        return new AtualizarConsumidorCaso(consumidores, consumidores);
    }

    @Bean
    ObterConsumidorCaso obterConsumidorCaso(ConsumidorPortaFake consumidores) {
        return new ObterConsumidorCaso(consumidores);
    }

    @Bean
    ListarConsumidoresCaso listarConsumidoresCaso(ConsumidorPortaFake consumidores) {
        return new ListarConsumidoresCaso(consumidores);
    }
}
