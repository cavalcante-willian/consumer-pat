package br.com.alelo.consumer.consumerpat.dominio.saude;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * O estado de saúde do sistema: o de cada dependência crítica.
 *
 * <p>
 * A situação geral é derivada pelo papel {@link ApuracaoDaSituacao}, e não é um campo. Um estado
 * sem nenhuma dependência é recusado, porque "saudável" por não ter o que conferir seria uma
 * verificação vazia.
 *
 * @param dependencias o estado de cada dependência crítica, e nunca vazio
 */
public record EstadoDeSaude(Map<DependenciaCritica, EstadoDeDependencia> dependencias)
        implements ApuracaoDaSituacao {

    /**
     * Cria o estado, com uma cópia das dependências.
     *
     * @param dependencias o estado de cada dependência crítica
     * @throws NullPointerException     se falta o mapa
     * @throws IllegalArgumentException se o mapa está vazio
     */
    public EstadoDeSaude {
        dependencias = Optional
                .of(Objects.requireNonNull(dependencias, "O estado precisa de dependências"))
                .filter(mapa -> !mapa.isEmpty()).map(Map::copyOf)
                .orElseThrow(() -> new IllegalArgumentException(
                        "O estado de saúde precisa de ao menos uma dependência"));
    }
}
