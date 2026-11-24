package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.CreditarCartaoCaso;
import br.com.alelo.consumer.consumerpat.dominio.portas.CartaoPorta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Instancia o caso de uso de creditar cartão, com a porta do cartão.
 *
 * <p>
 * É o que torna o caso de uso um bean: o Spring o envolve num proxy, e é por ele que a anotação de
 * transação passa a valer, e com ela a trava do cartão até o fim da operação.
 */
@Configuration
public class ConfiguracaoDoCasoDeCreditarCartao {

    /**
     * O caso de uso de creditar cartão.
     *
     * @param cartaoPorta a porta do cartão
     * @return o caso de uso
     */
    @Bean
    public CreditarCartaoCaso creditarCartaoCaso(CartaoPorta cartaoPorta) {
        return new CreditarCartaoCaso(cartaoPorta);
    }
}
