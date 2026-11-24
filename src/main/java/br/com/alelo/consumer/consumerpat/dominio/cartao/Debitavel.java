package br.com.alelo.consumer.consumerpat.dominio.cartao;

import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.DinheiroPositivo;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.regradenegocio.ErroSaldoInsuficiente;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Optional;

/**
 * Papel de um cartão de que se pode debitar um valor.
 *
 * <p>
 * Debitar exatamente o saldo é permitido e deixa o cartão em zero. Um valor maior que o saldo é
 * recusado com {@link ErroSaldoInsuficiente}, e um valor zero ou negativo é recusado antes de
 * qualquer conta, porque um débito negativo aumentaria o saldo sem que a regra percebesse.
 */
public interface Debitavel extends ComSaldo {

    /**
     * Debita o valor do saldo.
     *
     * @param valor a quantia a debitar; precisa ser maior que zero e não passar do saldo
     * @return sucesso com o cartão já debitado, ou falha com o erro que recusou o valor
     */
    default Resultado<Cartao, ErroDominio> debitar(Dinheiro valor) {
        return DinheiroPositivo.exigir(valor).encadear(positivo -> AusenciaComoErro.converter(
                Optional.of(saldo().menos(positivo))
                        .filter(resto -> resto.compareTo(Dinheiro.ZERO) >= 0).map(this::comSaldo),
                () -> new ErroSaldoInsuficiente(saldo(), positivo)));
    }
}
