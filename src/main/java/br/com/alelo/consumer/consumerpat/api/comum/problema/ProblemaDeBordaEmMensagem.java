package br.com.alelo.consumer.consumerpat.api.comum.problema;

import java.util.List;
import java.util.Map;

/**
 * Monta o corpo de erro a partir de um problema de borda.
 *
 * <p>
 * É o par de {@link ProblemaDeErroDeDominio} para o que não passa pelo domínio. O tipo, o título e
 * o status vêm do enum, e o que muda por ocorrência é o detalhe, o caminho e os campos.
 */
public final class ProblemaDeBordaEmMensagem {

    private ProblemaDeBordaEmMensagem() {
    }

    /**
     * Monta o corpo de erro.
     *
     * @param problema  o problema de borda
     * @param detalhe   o caso concreto, sem detalhe interno e sem dado sensível
     * @param instancia o caminho da requisição que falhou
     * @param erros     os campos com erro, ou vazio se o problema não é de campo
     * @return o corpo de erro
     */
    public static MensagemProblema montar(ProblemaDeBorda problema, String detalhe,
            String instancia, Map<String, List<String>> erros) {
        return new MensagemProblema(ProblemaDeErroDeDominio.PREFIXO_DO_TIPO + problema.codigo(),
                problema.titulo(), problema.statusHttp(), detalhe, instancia, erros);
    }
}
