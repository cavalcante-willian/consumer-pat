package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;

/**
 * Converte o extrato do domínio na linha a gravar.
 *
 * <p>
 * É a única peça que sabe como um {@link Extrato} vira uma linha de {@code extratos}. O número e o
 * tipo do cartão não são gravados no extrato: vêm da linha do cartão a que ele pertence, e é a
 * chave estrangeira que os liga.
 */
final class EntidadeDeExtrato {

    private EntidadeDeExtrato() {
    }

    /**
     * Monta a linha do extrato.
     *
     * @param extrato o extrato do domínio
     * @param cartao  a linha do cartão a que o extrato pertence
     * @return a linha, ainda não gravada
     */
    static ExtratoEntidade criar(Extrato extrato, CartaoEntidade cartao) {
        var entidade = new ExtratoEntidade();
        entidade.setCartao(cartao);
        entidade.setNomeEstabelecimento(extrato.nomeDoEstabelecimento());
        entidade.setDescricaoProduto(extrato.descricaoDoProduto());
        entidade.setValorInformado(extrato.valorInformado().valor());
        entidade.setValorCobrado(extrato.valorCobrado().valor());
        entidade.setDataCompra(extrato.dataDaCompra());
        return entidade;
    }
}
