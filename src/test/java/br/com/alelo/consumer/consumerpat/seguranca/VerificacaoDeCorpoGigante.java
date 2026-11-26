package br.com.alelo.consumer.consumerpat.seguranca;

/**
 * Cadastrar com um nome de seis milhões de caracteres: o sistema recusa pelo tamanho, e não grava,
 * não estoura e não responde falha de servidor.
 */
public final class VerificacaoDeCorpoGigante implements Verificacao {

    private static final int CARACTERES = 6_000_000;

    @Override
    public String chave() {
        return "corpo-gigante";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var resposta = sonda.cadastrarComNome("a".repeat(CARACTERES));
        return new Achado(chave(), "cadastro com um nome de 6 milhões de caracteres",
                Status.erroDeCliente(resposta.status()),
                "status " + resposta.status() + ", e o esperado é uma recusa de cliente");
    }
}
