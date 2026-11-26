package br.com.alelo.consumer.consumerpat.fabricas;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;

/**
 * Monta o JSON que a API aceita no cadastro do consumidor, no crédito e na compra.
 *
 * <p>
 * Os testes que percorrem a API com o contexto inteiro precisam dos mesmos três corpos. Os valores
 * são texto, e não número, para o teste dizer exatamente o que viaja, com as casas decimais que
 * escolheu.
 */
public final class CorposDaApi {

    private CorposDaApi() {
    }

    /**
     * O corpo do cadastro de um consumidor com um só cartão.
     *
     * @param documento o documento, só dígitos
     * @param tipo      o tipo do cartão
     * @param numero    o número do cartão, só dígitos
     * @return o JSON do cadastro
     */
    public static String cadastro(String documento, TipoCartao tipo, String numero) {
        return """
                {"dados":{"nome":"Consumidor de teste","documento":"%s",\
                "dataNascimento":"1990-05-17","celular":"11999998888",\
                "email":"consumidor@exemplo.com.br","rua":"Rua A","numero":"1",\
                "cidade":"Recife","pais":"Brasil","cep":"50000000"},\
                "numerosCartoes":{"%s":"%s"}}""".formatted(documento, tipo.name(), numero);
    }

    /**
     * O corpo do cadastro de um consumidor com um cartão de cada tipo.
     *
     * @param documento   o documento, só dígitos
     * @param alimentacao o número do cartão de alimentação, só dígitos
     * @param combustivel o número do cartão de combustível, só dígitos
     * @param farmacia    o número do cartão de farmácia, só dígitos
     * @return o JSON do cadastro
     */
    public static String cadastroComTresCartoes(String documento, String alimentacao,
            String combustivel, String farmacia) {
        return """
                {"dados":{"nome":"Consumidor de teste","documento":"%s",\
                "dataNascimento":"1990-05-17","celular":"11999998888",\
                "email":"consumidor@exemplo.com.br","rua":"Rua A","numero":"1",\
                "cidade":"Recife","pais":"Brasil","cep":"50000000"},\
                "numerosCartoes":{"%s":"%s","%s":"%s","%s":"%s"}}""".formatted(documento,
                TipoCartao.ALIMENTACAO.name(), alimentacao, TipoCartao.COMBUSTIVEL.name(),
                combustivel, TipoCartao.FARMACIA.name(), farmacia);
    }

    /**
     * O corpo do crédito num cartão.
     *
     * @param numero o número do cartão, só dígitos
     * @param tipo   o tipo do cartão
     * @param valor  o valor, como decimal com ponto
     * @return o JSON do crédito
     */
    public static String credito(String numero, TipoCartao tipo, String valor) {
        return """
                {"numeroCartao":"%s","tipoEstabelecimento":%d,"valor":%s}""".formatted(numero,
                tipo.codigoDeEstabelecimento(), valor);
    }

    /**
     * O corpo de uma compra num cartão.
     *
     * @param tipo   o tipo do cartão, que é o do estabelecimento
     * @param numero o número do cartão, só dígitos
     * @param valor  o valor, como decimal com ponto
     * @return o JSON da compra
     */
    public static String compra(TipoCartao tipo, String numero, String valor) {
        return """
                {"tipoEstabelecimento":%d,"nomeEstabelecimento":"Estabelecimento",\
                "numeroCartao":"%s","descricaoProduto":"Produto","valor":%s}"""
                .formatted(tipo.codigoDeEstabelecimento(), numero, valor);
    }
}
