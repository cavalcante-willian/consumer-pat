package br.com.alelo.consumer.consumerpat.seguranca;

import java.util.ArrayList;
import java.util.List;

/**
 * As verificações de segurança que se fazem contra os dois sistemas, e a forma de rodá-las.
 *
 * <p>
 * A bateria cobre o que se verifica por HTTP: entrada hostil, saldo, vazamento, cabeçalhos e
 * superfície exposta. Não cobre lógica de negócio nem infraestrutura, e não substitui uma revisão
 * de segurança. Cada verificação é uma tentativa, e passar nela não prova ausência de falha.
 */
public final class BateriaDeSeguranca {

    private static final List<Verificacao> VERIFICACOES = List.of(new VerificacaoDeValorNegativo(),
            new VerificacaoDeValorEnorme(), new VerificacaoDeSaldoPorAtualizacao(),
            new VerificacaoDeEstadoPorGet(), new VerificacaoDeCampoDesconhecido(),
            new VerificacaoDeCorpoGigante(), new VerificacaoDeEntradaHostil(),
            new VerificacaoDeErroDeClienteSem500(), new VerificacaoDeErroSemDetalheInterno(),
            new VerificacaoDeDadoSensivel(), new VerificacaoDeCabecalhos(),
            new VerificacaoDeTrace(), new VerificacaoDeCors(), new VerificacaoDeConsoleDoH2(),
            new VerificacaoDeDocumentacaoPublica(), new VerificacaoDeLimiteDeTaxa());

    private BateriaDeSeguranca() {
    }

    /**
     * As verificações, na ordem em que rodam.
     *
     * @return as verificações
     */
    public static List<Verificacao> verificacoes() {
        return VERIFICACOES;
    }

    /**
     * Roda todas as verificações contra um sistema.
     *
     * @param sonda o sistema
     * @return um achado por verificação, na ordem
     * @throws Exception se uma chamada falha por motivo que não é a resposta do sistema
     */
    public static List<Achado> executar(SondaDeSeguranca sonda) throws Exception {
        var achados = new ArrayList<Achado>();
        for (var verificacao : VERIFICACOES) {
            achados.add(verificacao.verificar(sonda));
        }
        return achados;
    }
}
