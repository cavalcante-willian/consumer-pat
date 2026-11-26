package br.com.alelo.consumer.consumerpat.medicao;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * O que um sistema respondeu: o status, o corpo e, quando houve, o {@code Location}.
 *
 * <p>
 * O corpo fica como bytes, e não como texto, porque o tamanho da resposta é uma das medidas: a
 * listagem do legado devolve a tabela inteira numa só resposta.
 *
 * @param status      o status HTTP
 * @param corpo       o corpo, em bytes
 * @param localizacao o cabeçalho {@code Location}, se a resposta trouxe
 */
public record RespostaHttp(int status, byte[] corpo, Optional<String> localizacao) {

    /**
     * O corpo como texto UTF-8.
     *
     * @return o corpo decodificado
     */
    public String texto() {
        return new String(corpo, StandardCharsets.UTF_8);
    }

    /**
     * O tamanho do corpo.
     *
     * @return a quantidade de bytes
     */
    public int tamanho() {
        return corpo.length;
    }
}
