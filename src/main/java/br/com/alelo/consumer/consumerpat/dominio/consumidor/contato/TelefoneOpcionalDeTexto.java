package br.com.alelo.consumer.consumerpat.dominio.consumidor.contato;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;

import java.util.Optional;

/**
 * Lê um telefone que pode não ser informado.
 *
 * <p>
 * Um texto ausente ou em branco é um telefone que não existe, e não um erro. Um texto presente
 * segue a regra de {@link TelefoneDeTexto}, de modo que um telefone mal escrito continua sendo
 * recusado em vez de ignorado em silêncio.
 */
public final class TelefoneOpcionalDeTexto {

    private TelefoneOpcionalDeTexto() {
    }

    /**
     * Converte o texto em telefone, quando há texto.
     *
     * @param texto o telefone como o cliente o escreveu, possivelmente nulo ou em branco
     * @param campo o nome do campo da entrada, para o erro apontá-lo
     * @return o telefone, ou vazio se não foi informado, ou o erro de validação do campo
     */
    public static Resultado<Optional<Telefone>, ErroDominio> converter(String texto, String campo) {
        return Optional.ofNullable(texto).filter(informado -> !informado.isBlank())
                .map(informado -> TelefoneDeTexto.converter(informado, campo).mapear(Optional::of))
                .orElseGet(() -> new Sucesso<>(Optional.empty()));
    }
}
