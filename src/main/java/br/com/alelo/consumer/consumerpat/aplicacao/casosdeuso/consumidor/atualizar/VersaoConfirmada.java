package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.atualizar;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroVersaoDivergente;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Optional;

/**
 * Exige que a versão que o cliente leu seja a versão atual do consumidor.
 *
 * <p>
 * É o caso comum de uma atualização feita sobre uma leitura velha, recusado antes de gravar. A
 * corrida entre dois clientes que leem a mesma versão ao mesmo tempo não passa por aqui: quem a
 * pega é a versão de linha do banco, na gravação.
 */
public final class VersaoConfirmada {

    private VersaoConfirmada() {
    }

    /**
     * Confere a versão do cliente contra a do consumidor lido.
     *
     * @param atual           o consumidor como está gravado
     * @param versaoInformada a versão que o cliente leu
     * @return o mesmo consumidor, ou {@link ErroVersaoDivergente} se a versão não é a atual
     */
    public static Resultado<Consumidor, ErroDominio> exigir(Consumidor atual,
            long versaoInformada) {
        return AusenciaComoErro.converter(
                Optional.of(atual).filter(lido -> lido.versao() == versaoInformada),
                () -> new ErroVersaoDivergente(atual.id(), versaoInformada));
    }
}
