package br.com.alelo.consumer.consumerpat.api.comum.requisicao;

import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog.campo;

/**
 * Registra a conclusão da requisição: método, caminho, status e duração.
 *
 * <p>
 * É a etapa 3 do pipeline, logo dentro da correlação: o código depois do {@code doFilter} ainda
 * roda com o {@code MDC} do filtro de fora vivo, e por isso a linha leva o id. Se este filtro fosse
 * mais externo, o id já teria sido apagado quando ele escrevesse. O evento sai mesmo se a
 * requisição lançar, e a duração usa {@code System.nanoTime()}, que mede intervalo sem depender do
 * relógio de parede.
 *
 * <p>
 * O caminho não leva a query string, que pode carregar dado do usuário.
 */
public class FiltroDeLogDeConclusao extends OncePerRequestFilter {

    private final SaidaDeLogPorta saida;

    /**
     * Cria o filtro.
     *
     * @param saida a porta de log
     */
    public FiltroDeLogDeConclusao(SaidaDeLogPorta saida) {
        this.saida = saida;
    }

    @Override
    public void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta,
            FilterChain cadeia) throws ServletException, IOException {
        var inicio = System.nanoTime();
        try {
            cadeia.doFilter(requisicao, resposta);
        } finally {
            var status = resposta.getStatus();
            saida.registrar(NivelDaConclusao.de(status), "requisicao.concluida", Optional.empty(),
                    campo("metodo", requisicao.getMethod()),
                    campo("caminho", requisicao.getRequestURI()), campo("status", status),
                    campo("duracaoMs", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio)));
        }
    }
}
