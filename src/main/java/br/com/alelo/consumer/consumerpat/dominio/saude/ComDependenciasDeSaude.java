package br.com.alelo.consumer.consumerpat.dominio.saude;

import java.util.Map;

/**
 * Operação-base dos papéis do estado de saúde: expõe o estado de cada dependência crítica.
 */
public interface ComDependenciasDeSaude {

    /**
     * O estado de cada dependência crítica.
     *
     * @return o estado de cada uma, com ao menos uma entrada
     */
    Map<DependenciaCritica, EstadoDeDependencia> dependencias();
}
