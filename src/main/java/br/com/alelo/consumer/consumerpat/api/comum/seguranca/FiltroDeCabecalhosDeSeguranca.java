package br.com.alelo.consumer.consumerpat.api.comum.seguranca;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Escreve os cabeçalhos de segurança em toda resposta da API.
 *
 * <p>
 * É a etapa 1 do pipeline, o filtro mais externo: os cabeçalhos são escritos antes de a requisição
 * seguir, e por isso cobrem também a resposta de erro, a de uma rota que não existe e a de um
 * método que não é aceito, que nunca chegam a um controller. Aplicados num único ponto, nunca por
 * controller nem "quando lembrarem". Só escreve cabeçalhos: não decide nada e não lança exceção.
 *
 * <p>
 * É registrado só para {@code /api/*}. O Swagger e o console do H2 ficam de fora: a política de
 * conteúdo e o {@code X-Frame-Options: DENY} quebram essas páginas, que usam quadros e carregam
 * recursos.
 */
public class FiltroDeCabecalhosDeSeguranca extends OncePerRequestFilter {

    @Override
    public void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta,
            FilterChain cadeia) throws ServletException, IOException {
        CabecalhosDeSeguranca.PADRAO.forEach(resposta::setHeader);
        cadeia.doFilter(requisicao, resposta);
    }
}
