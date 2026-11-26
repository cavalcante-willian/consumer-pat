package br.com.alelo.consumer.consumerpat.medicao;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * O que um sistema respondeu: o status, o corpo, os cabeçalhos e, quando houve, o {@code Location}.
 *
 * <p>
 * O corpo fica como bytes, e não como texto, porque o tamanho da resposta é uma das medidas: a
 * listagem do legado devolve a tabela inteira numa só resposta. Os cabeçalhos guardam o primeiro
 * valor de cada um, com o nome em minúsculas, que é como a busca os encontra.
 *
 * @param status      o status HTTP
 * @param corpo       o corpo, em bytes
 * @param localizacao o cabeçalho {@code Location}, se a resposta trouxe
 * @param cabecalhos  o primeiro valor de cada cabeçalho, com o nome em minúsculas
 */
public record RespostaHttp(int status, byte[] corpo, Optional<String> localizacao,
        Map<String, String> cabecalhos) {

    /**
     * Uma resposta sem cabeçalhos além do {@code Location}.
     *
     * @param status      o status HTTP
     * @param corpo       o corpo, em bytes
     * @param localizacao o cabeçalho {@code Location}, se a resposta trouxe
     */
    public RespostaHttp(int status, byte[] corpo, Optional<String> localizacao) {
        this(status, corpo, localizacao, Map.of());
    }

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

    /**
     * O valor de um cabeçalho, sem distinguir maiúsculas de minúsculas no nome.
     *
     * @param nome o nome do cabeçalho
     * @return o valor, ou vazio se a resposta não o trouxe
     */
    public Optional<String> cabecalho(String nome) {
        return Optional.ofNullable(cabecalhos.get(nome.toLowerCase(Locale.ROOT)));
    }
}
