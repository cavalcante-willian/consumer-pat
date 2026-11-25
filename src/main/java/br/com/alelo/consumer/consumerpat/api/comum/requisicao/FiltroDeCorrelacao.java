package br.com.alelo.consumer.consumerpat.api.comum.requisicao;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Abre a correlação da requisição: gera ou aceita o id, o põe no {@code MDC} e o devolve.
 *
 * <p>
 * É a etapa 2 do pipeline, logo dentro dos cabeçalhos de segurança: todo log escrito depois dela já
 * leva o id, sem que nenhum código o passe à mão. O id volta em {@code X-Request-Id} em toda
 * resposta, e não só nas de erro.
 *
 * <p>
 * O {@code MDC} é {@code ThreadLocal}, e o pool reaproveita a thread: o id é removido no
 * {@code finally}, e um id que sobrasse vazaria para a requisição seguinte. Ele também não viaja
 * sozinho para outra thread.
 */
public class FiltroDeCorrelacao extends OncePerRequestFilter {

    @Override
    public void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta,
            FilterChain cadeia) throws ServletException, IOException {
        var id = IdDeRequisicao.aceitarOuGerar(requisicao.getHeader(IdDeRequisicao.CABECALHO));
        MDC.put(IdDeRequisicao.CHAVE_NO_MDC, id);
        resposta.setHeader(IdDeRequisicao.CABECALHO, id);
        try {
            cadeia.doFilter(requisicao, resposta);
        } finally {
            MDC.remove(IdDeRequisicao.CHAVE_NO_MDC);
        }
    }
}
