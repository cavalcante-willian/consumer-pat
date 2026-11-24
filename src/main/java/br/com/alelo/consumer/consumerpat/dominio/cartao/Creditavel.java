package br.com.alelo.consumer.consumerpat.dominio.cartao;

import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.DinheiroPositivo;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

/**
 * Papel de um cartão em que se pode creditar um valor.
 *
 * <p>
 * Um valor zero ou negativo é recusado, porque um crédito negativo diminuiria o saldo e furaria a
 * regra de saldo do débito. O crédito não tem limite superior no domínio: o limite de uma coluna do
 * banco é preocupação de quem persiste.
 */
public interface Creditavel extends ComSaldo {

    /**
     * Credita o valor no saldo.
     *
     * @param valor a quantia a creditar; precisa ser maior que zero
     * @return sucesso com o cartão já creditado, ou falha com o erro que recusou o valor
     */
    default Resultado<Cartao, ErroDominio> creditar(Dinheiro valor) {
        return DinheiroPositivo.exigir(valor).mapear(positivo -> comSaldo(saldo().mais(positivo)));
    }
}
