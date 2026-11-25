package br.com.alelo.consumer.consumerpat.dominio.pagina;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroOrdenacaoInvalida;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Arrays;

/**
 * Lê a ordenação pedida pelo nome, sem diferenciar maiúsculas de minúsculas.
 *
 * <p>
 * Aceita {@code nome} e {@code NOME}, como o contrato exemplifica. Um valor que não é de nenhum
 * campo é erro, e nunca vira o nome de uma coluna.
 */
public final class OrdenacaoDeConsumidorDeTexto {

    private OrdenacaoDeConsumidorDeTexto() {
    }

    /**
     * Converte o texto em ordenação.
     *
     * @param texto o valor recebido, possivelmente nulo
     * @return a ordenação, ou {@link ErroOrdenacaoInvalida}
     */
    public static Resultado<OrdenacaoDeConsumidor, ErroDominio> converter(String texto) {
        return AusenciaComoErro.converter(
                Arrays.stream(OrdenacaoDeConsumidor.values())
                        .filter(ordenacao -> ordenacao.name().equalsIgnoreCase(texto)).findFirst(),
                ErroOrdenacaoInvalida::new);
    }
}
