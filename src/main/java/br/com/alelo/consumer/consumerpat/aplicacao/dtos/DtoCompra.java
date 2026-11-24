package br.com.alelo.consumer.consumerpat.aplicacao.dtos;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;

/**
 * O pedido de uma compra, já com os tipos do domínio.
 *
 * <p>
 * Quem monta o pedido, a borda, converte o que recebeu de fora com as leituras do domínio e só
 * então o entrega ao caso de uso: a falha de uma conversão é recusada lá, antes de existir pedido.
 * O caso de uso assume o que a borda garantiu e não converte nem valida formato de novo. Os textos
 * livres, o estabelecimento e o produto, devem chegar não vazios.
 *
 * @param tipo                o tipo do cartão a debitar, que é o do estabelecimento da compra
 * @param nomeEstabelecimento o estabelecimento em que a compra foi feita
 * @param numeroCartao        o cartão a debitar
 * @param descricaoProduto    o que foi comprado
 * @param valor               o valor da compra, antes do ajuste do tipo
 */
public record DtoCompra(TipoCartao tipo, String nomeEstabelecimento, NumeroDeCartao numeroCartao,
        String descricaoProduto, Dinheiro valor) {
}
