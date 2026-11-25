package br.com.alelo.consumer.consumerpat.api.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

/**
 * Tira o valor de uma leitura que já se sabe que deu certo.
 *
 * <p>
 * Só se usa depois de reunir as falhas e não achar nenhuma: a leitura é refeita, é uma função pura,
 * e o valor sai sem desviar. Se ela falhar aqui é bug de programação, e não entrada do cliente, e
 * lança em vez de devolver um valor inventado.
 */
public final class ValorConfirmado {

    private ValorConfirmado() {
    }

    /**
     * Devolve o valor da leitura.
     *
     * @param leitura uma leitura que já foi conferida como sucesso
     * @param <T>     o tipo do valor
     * @return o valor lido
     * @throws IllegalStateException se a leitura, na verdade, falhou
     */
    public static <T> T de(Resultado<T, ErroDominio> leitura) {
        return leitura.resolver(valor -> valor, erro -> {
            throw new IllegalStateException(
                    "A leitura devia ter dado certo, e falhou: " + erro.codigo());
        });
    }
}
