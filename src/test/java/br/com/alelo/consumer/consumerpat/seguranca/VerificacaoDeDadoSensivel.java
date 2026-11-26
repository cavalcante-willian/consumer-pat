package br.com.alelo.consumer.consumerpat.seguranca;

/**
 * O que o sistema devolve de um consumidor não traz o documento nem o número do cartão por inteiro.
 *
 * <p>
 * Cadastra um consumidor de documento e cartão conhecidos e procura os dois, inteiros, no que o
 * sistema mostra dele. Quem lê a resposta, ou o log do proxy que a guardou, não tem por que ver o
 * número completo de um cartão.
 */
public final class VerificacaoDeDadoSensivel implements Verificacao {

    @Override
    public String chave() {
        return "dado-sensivel-na-resposta";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var exposicao = sonda.exporDadosDeConsumidor();
        var documento = exposicao.texto().contains(exposicao.documento());
        var cartao = exposicao.texto().contains(exposicao.cartao());
        return new Achado(chave(), "documento e número do cartão na leitura de um consumidor",
                !documento && !cartao,
                "documento por inteiro: " + documento + ", cartão por inteiro: " + cartao);
    }
}
