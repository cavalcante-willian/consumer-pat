package br.com.alelo.consumer.consumerpat.api.comum.seguranca;

import java.util.Map;

/**
 * Os cabeçalhos que toda resposta da API leva, e o que cada um impede.
 *
 * <p>
 * Sem Spring Security o Spring Boot não envia nenhum, e a defesa que o servidor consegue impor ao
 * navegador depois que a resposta saiu é só esta. Numa API que só devolve JSON o conjunto é pequeno
 * e barato demais para ficar de fora.
 *
 * <ul>
 * <li>{@code X-Content-Type-Options: nosniff}: o navegador não infere um tipo e não executa o
 * conteúdo.</li>
 * <li>{@code X-Frame-Options: DENY}: a resposta não aparece num quadro de outra página
 * (<i>clickjacking</i>).</li>
 * <li>{@code Content-Security-Policy}: a resposta é JSON e não precisa carregar nada, então nenhum
 * recurso é permitido, e nenhuma página a emoldura.</li>
 * <li>{@code Referrer-Policy: no-referrer}: a URL não vaza num cabeçalho {@code Referer}.</li>
 * <li>{@code Cache-Control: no-store}: nenhum intermediário, nem o navegador, guarda dado pessoal
 * ou o saldo, que um cache deixaria velho.</li>
 * </ul>
 *
 * <p>
 * O {@code Strict-Transport-Security} não está aqui: ele só tem efeito sobre HTTPS, e o TLS é
 * terminado fora da aplicação, onde ele é configurado.
 */
public final class CabecalhosDeSeguranca {

    /** O nome de cada cabeçalho e o valor com que toda resposta sai. */
    public static final Map<String, String> PADRAO = Map.of("X-Content-Type-Options", "nosniff",
            "X-Frame-Options", "DENY", "Content-Security-Policy",
            "default-src 'none'; frame-ancestors 'none'", "Referrer-Policy", "no-referrer",
            "Cache-Control", "no-store");

    private CabecalhosDeSeguranca() {
    }
}
