package br.com.alelo.consumer.consumerpat.seguranca;

/**
 * Pedir algo que não existe é erro de quem pediu, e nunca falha do servidor.
 *
 * <p>
 * Um {@code 500} para um cartão inexistente diz ao cliente que o sistema quebrou, esconde do
 * monitoramento a diferença entre um erro de uso e uma falha de verdade, e, no legado, é uma
 * exceção de ponteiro nulo.
 */
public final class VerificacaoDeErroDeClienteSem500 implements Verificacao {

    @Override
    public String chave() {
        return "erro-de-cliente-nao-vira-500";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var resposta = sonda.creditarCartaoInexistente();
        return new Achado(chave(), "crédito num cartão que não existe",
                !Status.erroDeServidor(resposta.status()),
                "status " + resposta.status() + ", e o esperado é um erro de cliente");
    }
}
