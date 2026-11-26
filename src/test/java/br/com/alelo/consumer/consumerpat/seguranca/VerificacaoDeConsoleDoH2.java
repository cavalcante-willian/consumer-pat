package br.com.alelo.consumer.consumerpat.seguranca;

import br.com.alelo.consumer.consumerpat.medicao.RespostaHttp;

/**
 * O console do H2 não está aberto a quem vem de fora.
 *
 * <p>
 * Com {@code sa/sa}, o console dá leitura e escrita irrestritas no banco pelo navegador, e contorna
 * a API, a validação e a trava do saldo. A resposta depende da origem do pedido: o H2 só abre para
 * o loopback, e a quem vem de fora devolve uma página de recusa, com status {@code 200}, e não um
 * erro HTTP. Por isso a verificação faz duas coisas.
 *
 * <p>
 * Pede o console pelo endereço de uma interface de rede da máquina, onde o sistema vê uma origem
 * que não é loopback, e conclui pelo status e pelo corpo: recusado é um erro de cliente ou a página
 * de recusa do H2. Pede também por {@code localhost}, só para a evidência: a própria máquina abre o
 * console de propósito, porque o README do desafio o documenta, e o perfil {@code prod} o desliga.
 *
 * <p>
 * Uma primeira versão olhava só o status de um pedido local, e por isso marcava como exposto um
 * console que de fora já era recusado. Sem endereço de rede, a verificação diz que não pôde ser
 * feita, e não dá o resultado do pedido local como se fosse o de fora.
 */
public final class VerificacaoDeConsoleDoH2 implements Verificacao {

    private static final String PAGINA_DE_RECUSA = "Sorry, remote connections";

    private static final String DESCRICAO = "acesso ao console do H2 por quem vem de fora";

    @Override
    public String chave() {
        return "console-do-h2";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var local = sonda.consoleDoH2();
        return sonda.consoleDoH2DeFora()
                .map(deFora -> new Achado(chave(), DESCRICAO, recusado(deFora),
                        "de fora (pela interface de rede): status " + deFora.status()
                                + ", página de recusa do H2: " + paginaDeRecusa(deFora)
                                + "; pela própria máquina: status " + local.status()
                                + ", aberto por desenho"))
                .orElseGet(() -> Achado.naoVerificado(chave(), DESCRICAO,
                        "a máquina não tem interface de rede fora do loopback; pela própria "
                                + "máquina: status " + local.status()));
    }

    private static boolean recusado(RespostaHttp resposta) {
        return Status.erroDeCliente(resposta.status()) || paginaDeRecusa(resposta);
    }

    private static boolean paginaDeRecusa(RespostaHttp resposta) {
        return resposta.texto().contains(PAGINA_DE_RECUSA);
    }
}
