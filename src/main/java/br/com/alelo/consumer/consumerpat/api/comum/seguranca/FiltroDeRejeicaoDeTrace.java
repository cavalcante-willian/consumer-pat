package br.com.alelo.consumer.consumerpat.api.comum.seguranca;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Recusa o {@code TRACE} com {@code 405} e sem corpo.
 *
 * <p>
 * O Spring MVC responde um {@code TRACE} ecoando a requisição inteira, e não há configuração que o
 * evite: ainda que a propriedade que despacha o {@code TRACE} esteja ligada, o eco do
 * {@code HttpServlet} é escrito depois da resposta normal, sempre que o conteúdo não é
 * {@code message/http}. O eco devolve ao cliente os cabeçalhos que ele mandou, inclusive cookie e
 * autorização, o que abre o caminho do cross-site tracing, e não tem utilidade nenhuma numa API.
 *
 * <p>
 * O {@code TRACE} já termina em {@code 405}, mas o eco sai na página de erro, numa segunda
 * passagem pela aplicação: o corpo mostra {@code TRACE /error}, e não a rota pedida. Por isso o
 * filtro vale também para o despacho de erro, que um filtro comum ignora, e não deixa a requisição
 * seguir em nenhum dos dois casos. Só age no {@code TRACE}.
 */
public class FiltroDeRejeicaoDeTrace extends OncePerRequestFilter {

    private static final String TRACE = "TRACE";

    @Override
    public boolean shouldNotFilter(HttpServletRequest requisicao) {
        return !TRACE.equalsIgnoreCase(requisicao.getMethod());
    }

    @Override
    public boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    public void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta,
            FilterChain cadeia) throws ServletException, IOException {
        resposta.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
