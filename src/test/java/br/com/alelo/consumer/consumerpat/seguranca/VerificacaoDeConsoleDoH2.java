package br.com.alelo.consumer.consumerpat.seguranca;

/**
 * O console do H2 não está aberto.
 *
 * <p>
 * Com {@code sa/sa}, o console dá leitura e escrita irrestritas no banco pelo navegador, e contorna
 * a API, a validação e a trava do saldo. No perfil padrão ele fica aberto de propósito, porque o
 * README do desafio o documenta, e o perfil {@code prod} o desliga: este é um risco aceito de quem
 * roda o perfil padrão, e o relatório o mostra como exposto.
 */
public final class VerificacaoDeConsoleDoH2 implements Verificacao {

    @Override
    public String chave() {
        return "console-do-h2";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var resposta = sonda.consoleDoH2();
        return new Achado(chave(), "acesso ao console do H2 pelo navegador",
                Status.erroDeCliente(resposta.status()), "status " + resposta.status());
    }
}
