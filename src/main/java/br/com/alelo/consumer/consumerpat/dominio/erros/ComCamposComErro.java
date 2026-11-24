package br.com.alelo.consumer.consumerpat.dominio.erros;

import java.util.List;
import java.util.Map;

/**
 * Papel de um erro que pode apontar os campos da entrada a que se refere.
 *
 * <p>
 * A maioria dos erros não é sobre um campo específico, então o padrão é não apontar nenhum. Só o
 * erro que de fato se refere a campos sobrescreve.
 */
public interface ComCamposComErro {

    /**
     * Campos da entrada a que o erro se refere, com as mensagens de cada um. Vazio quando o erro
     * não é sobre um campo específico.
     *
     * @return mapa de nome do campo para suas mensagens
     */
    default Map<String, List<String>> camposComErro() {
        return Map.of();
    }
}
