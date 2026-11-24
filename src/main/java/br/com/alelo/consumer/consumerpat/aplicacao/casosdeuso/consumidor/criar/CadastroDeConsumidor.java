package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoNovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import org.springframework.transaction.annotation.Transactional;

/**
 * Papel de cadastrar um consumidor com os cartões que ele traz.
 *
 * <p>
 * Só orquestra: monta o consumidor com {@link NovoConsumidorDoPedido} e o entrega à porta. O
 * consumidor e os cartões são gravados numa só transação, de modo que um número de cartão que já
 * existe desfaz o cadastro inteiro, e não deixa um consumidor sem os cartões. O documento repetido
 * e o número repetido voltam como erro de domínio, vindos da porta.
 */
public interface CadastroDeConsumidor extends ComGravadorDeNovoConsumidor {

    /**
     * Cadastra o consumidor.
     *
     * @param pedido o cadastro e os números dos cartões
     * @return o consumidor gravado, com id e versão, ou o erro que a porta devolveu
     */
    @Transactional
    default Resultado<Consumidor, ErroDominio> criar(DtoNovoConsumidor pedido) {
        return gravador().criar(NovoConsumidorDoPedido.montar(pedido));
    }
}
