package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.compra.GravadorDaCompra;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.compra.RegistrarCompraCaso;
import br.com.alelo.consumer.consumerpat.dominio.portas.CartaoPorta;
import br.com.alelo.consumer.consumerpat.dominio.portas.ExtratoPorta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Instancia o caso de uso de registrar compra, com as portas e o relógio da aplicação.
 *
 * <p>
 * É o que torna o caso de uso um bean: o Spring o envolve num proxy, e é por ele que a anotação de
 * transação passa a valer, e com ela a trava do cartão até o fim da operação. O gravador da compra
 * nasce aqui, com as mesmas portas, porque só este caso de uso o usa.
 */
@Configuration
public class ConfiguracaoDoCasoDeRegistrarCompra {

    /**
     * O caso de uso de registrar compra.
     *
     * @param cartaoPorta  a porta do cartão
     * @param extratoPorta a porta do extrato
     * @param relogio      o relógio da aplicação
     * @return o caso de uso
     */
    @Bean
    public RegistrarCompraCaso registrarCompraCaso(CartaoPorta cartaoPorta,
            ExtratoPorta extratoPorta, Clock relogio) {
        return new RegistrarCompraCaso(cartaoPorta, new GravadorDaCompra(cartaoPorta, extratoPorta),
                relogio);
    }
}
