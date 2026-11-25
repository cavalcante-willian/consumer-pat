package br.com.alelo.consumer.consumerpat.dominio.saude;

import java.util.Optional;

/**
 * Papel de quem sabe apurar a situação geral a partir do estado de cada dependência.
 *
 * <p>
 * A situação não é um campo: é derivada, e por isso nunca diverge das dependências. É saudável só
 * se todas estão disponíveis.
 */
public interface ApuracaoDaSituacao extends ComDependenciasDeSaude {

    /**
     * A situação geral.
     *
     * @return {@link SituacaoDeSaude#SAUDAVEL} se todas as dependências estão disponíveis, ou
     *         {@link SituacaoDeSaude#DEGRADADO}
     */
    default SituacaoDeSaude situacao() {
        return Optional
                .of(dependencias().values().stream()
                        .allMatch(estado -> estado == EstadoDeDependencia.DISPONIVEL))
                .filter(Boolean::booleanValue).map(todas -> SituacaoDeSaude.SAUDAVEL)
                .orElse(SituacaoDeSaude.DEGRADADO);
    }
}
