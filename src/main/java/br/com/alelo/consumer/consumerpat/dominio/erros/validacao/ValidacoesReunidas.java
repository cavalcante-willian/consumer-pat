package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reúne os erros de vários campos lidos de uma vez, em vez de parar no primeiro.
 *
 * <p>
 * O {@code encadear} de um {@code Resultado} para na primeira falha, que é o certo quando um passo
 * depende do anterior. Campos independentes não dependem uns dos outros, e o cliente precisa saber
 * de todos os que estão errados. Os campos saem ordenados pelo nome, para a resposta não depender
 * da ordem em que foram lidos.
 */
public final class ValidacoesReunidas {

    private ValidacoesReunidas() {
    }

    /**
     * Reúne as falhas dos campos.
     *
     * @param campos os campos lidos, cada um com o seu resultado
     * @return {@link ErroEntradaInvalida} com todos os campos que falharam, ou vazio se nenhum
     *         falhou
     */
    public static Optional<ErroDominio> reunir(List<CampoValidado> campos) {
        Map<String, List<String>> porCampo = campos.stream()
                .flatMap(campo -> campo.resultado().<Stream<Map.Entry<String, String>>>resolver(
                        valor -> Stream.empty(),
                        erro -> MensagensDaFalha.de(erro).stream()
                                .map(mensagem -> Map.entry(campo.campo(), mensagem))))
                .collect(Collectors.groupingBy(Map.Entry::getKey, TreeMap::new,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        return Optional.of(porCampo).filter(reunidos -> !reunidos.isEmpty())
                .map(ErroEntradaInvalida::new);
    }
}
