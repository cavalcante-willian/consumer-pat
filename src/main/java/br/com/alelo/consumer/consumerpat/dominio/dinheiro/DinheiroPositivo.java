package br.com.alelo.consumer.consumerpat.dominio.dinheiro;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroValorNaoPositivo;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Optional;

/**
 * Exige que uma quantia seja maior que zero, sem lançar exceção.
 *
 * <p>
 * É o que impede um débito ou um crédito de valor negativo: um débito de -50 aumentaria o saldo, e
 * um crédito de -50 o diminuiria, nos dois casos sem que a regra de saldo perceba. A exigência é do
 * domínio, e não só da borda, porque o domínio é chamado também por quem não passou pela borda.
 */
public final class DinheiroPositivo {

    private DinheiroPositivo() {
    }

    /**
     * Devolve a própria quantia se ela é maior que zero.
     *
     * @param valor a quantia a verificar; nulo conta como não positivo
     * @return sucesso com a mesma quantia, ou falha com {@link ErroValorNaoPositivo}
     */
    public static Resultado<Dinheiro, ErroDominio> exigir(Dinheiro valor) {
        return AusenciaComoErro.converter(
                Optional.ofNullable(valor).filter(quantia -> quantia.compareTo(Dinheiro.ZERO) > 0),
                ErroValorNaoPositivo::new);
    }
}
