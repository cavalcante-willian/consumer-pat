package br.com.alelo.consumer.consumerpat.infraestrutura.log;

import br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * O texto do valor de um campo de log, com o dado sensível mascarado pelo nome.
 *
 * <p>
 * É a segunda camada de proteção, depois do {@code toString()} mascarado dos tipos de valor: a
 * lista de nomes é fechada, e o valor de um campo cujo nome contém um deles é trocado por
 * {@value #MASCARA}, qualquer que seja o tipo. O nome descreve o dado, e a convenção só reconhece o
 * que está bem nomeado. Quem tem nome parecido com um sensível é mascarado também, porque errar
 * para o lado da máscara custa uma linha menos legível, e errar para o outro custa um vazamento.
 */
final class ValorDeCampoDeLog {

    /** O que aparece no lugar do valor sensível. */
    static final String MASCARA = "***";

    private static final Set<String> NOMES_SENSIVEIS = Set.of("senha", "token", "segredo",
            "numerocartao", "documento", "telefone", "celular", "email");

    private ValorDeCampoDeLog() {
    }

    /**
     * Converte o valor do campo em texto de uma linha só.
     *
     * @param campo o campo
     * @return {@value #MASCARA} se o nome é sensível, ou o valor sem quebras de linha
     */
    static String paraTexto(CampoDeLog campo) {
        var nome = campo.nome().toLowerCase(Locale.ROOT);
        return Optional.of(nome)
                .filter(candidato -> NOMES_SENSIVEIS.stream().anyMatch(candidato::contains))
                .map(sensivel -> MASCARA)
                .orElseGet(() -> TextoDeUmaLinha.de(String.valueOf(campo.valor())));
    }
}
