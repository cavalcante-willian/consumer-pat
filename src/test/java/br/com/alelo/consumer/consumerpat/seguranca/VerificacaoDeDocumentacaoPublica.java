package br.com.alelo.consumer.consumerpat.seguranca;

/**
 * O Swagger e a especificação OpenAPI não estão publicados.
 *
 * <p>
 * O contrato da API não precisa estar público em produção. No perfil padrão eles ficam abertos de
 * propósito, porque o README do desafio os documenta, e o perfil {@code prod} os desliga: é um
 * risco aceito de quem roda o perfil padrão, e o relatório o mostra como exposto.
 */
public final class VerificacaoDeDocumentacaoPublica implements Verificacao {

    @Override
    public String chave() {
        return "documentacao-publica";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var swagger = sonda.swagger();
        var especificacao = sonda.especificacao();
        return new Achado(chave(), "acesso ao Swagger e à especificação OpenAPI",
                Status.erroDeCliente(swagger.status())
                        && Status.erroDeCliente(especificacao.status()),
                "Swagger: status " + swagger.status() + ", especificação: status "
                        + especificacao.status());
    }
}
