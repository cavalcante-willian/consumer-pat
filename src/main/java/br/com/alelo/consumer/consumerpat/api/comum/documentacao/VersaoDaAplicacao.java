package br.com.alelo.consumer.consumerpat.api.comum.documentacao;

import java.util.Optional;

/**
 * Lê a versão da aplicação do {@code MANIFEST.MF} do JAR, onde o plugin do Spring Boot a escreve
 * como {@code Implementation-Version}.
 *
 * <p>
 * A fonte única da versão é a linha {@code version} do {@code build.gradle}, e é dali que ela chega
 * ao manifesto. Esta classe a lê, em vez de repeti-la numa constante, para que não existam duas
 * versões. Fora do JAR, como na IDE ou nos testes, o manifesto não existe, e a resposta é um texto
 * que diz isso, e não um valor inventado.
 */
public final class VersaoDaAplicacao {

    /** O que se responde quando a classe não vem de um JAR com versão. */
    public static final String SEM_VERSAO = "sem versão (fora do JAR)";

    private VersaoDaAplicacao() {
    }

    /**
     * Devolve a versão do JAR de onde a classe foi carregada.
     *
     * @param classe uma classe da aplicação
     * @return o {@code Implementation-Version} do manifesto, ou {@link #SEM_VERSAO}
     */
    public static String de(Class<?> classe) {
        return Optional.ofNullable(classe.getPackage()).map(Package::getImplementationVersion)
                .orElse(SEM_VERSAO);
    }
}
