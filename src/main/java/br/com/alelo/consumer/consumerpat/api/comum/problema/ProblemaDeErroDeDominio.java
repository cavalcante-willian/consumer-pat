package br.com.alelo.consumer.consumerpat.api.comum.problema;

import br.com.alelo.consumer.consumerpat.api.comum.requisicao.IdDaRequisicaoAtual;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;

/**
 * Monta o corpo de erro a partir de um erro de domínio.
 *
 * <p>
 * O status vem da categoria do erro, e o tipo, do código da variante: nada é escolhido aqui. A
 * mensagem do erro já não leva dado sensível, e os campos com erro saem como o erro os declara, de
 * modo que o cliente não distingue se foi a borda ou o domínio quem recusou a entrada.
 */
public final class ProblemaDeErroDeDominio {

    /** O prefixo do tipo: o código do erro vem depois. */
    public static final String PREFIXO_DO_TIPO = "urn:consumer-pat:erro:";

    private ProblemaDeErroDeDominio() {
    }

    /**
     * Monta o corpo de erro.
     *
     * @param erro      o erro de domínio
     * @param instancia o caminho da requisição que falhou
     * @return o corpo de erro
     */
    public static MensagemProblema montar(ErroDominio erro, String instancia) {
        return new MensagemProblema(PREFIXO_DO_TIPO + erro.codigo(),
                TituloDaCategoria.de(erro.categoria()), erro.categoria().statusHttp(),
                erro.mensagem(), instancia, IdDaRequisicaoAtual.ler(), erro.camposComErro());
    }
}
