package br.com.alelo.consumer.consumerpat.api.comum.requisicao;

import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;

import java.util.List;
import java.util.function.IntPredicate;

/**
 * O nível do log de conclusão de uma requisição, escolhido pelo status.
 *
 * <p>
 * Nível é semântica, e não filtro de volume. A falha do servidor (5xx) é {@code ERRO}, o conflito
 * (409) é {@code AVISO}, e o resto é {@code INFORMACAO}: um 404, um 400 ou um 422 de saldo
 * insuficiente são resultado esperado, e não falha do sistema, e contá-los como erro gera a fadiga
 * de alerta que faz ignorar o erro de verdade.
 */
public final class NivelDaConclusao {

    private record Regra(IntPredicate vale, NivelDeLog nivel) {
    }

    private static final List<Regra> REGRAS = List.of(
            new Regra(status -> status >= 500, NivelDeLog.ERRO),
            new Regra(status -> status == 409, NivelDeLog.AVISO));

    private NivelDaConclusao() {
    }

    /**
     * Escolhe o nível.
     *
     * @param status o status da resposta
     * @return o nível do log de conclusão
     */
    public static NivelDeLog de(int status) {
        return REGRAS.stream().filter(regra -> regra.vale().test(status)).map(Regra::nivel)
                .findFirst().orElse(NivelDeLog.INFORMACAO);
    }
}
