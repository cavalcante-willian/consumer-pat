package br.com.alelo.consumer.consumerpat.api.cartao.compra;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * O corpo do pedido de compra.
 *
 * <p>
 * A validação aqui é de forma: o que é obrigatório, o tamanho dos textos e o formato do valor. O
 * que só o domínio sabe, como um tipo de estabelecimento que não existe ou um número de cartão com
 * o tamanho errado, é recusado depois, pelas leituras do domínio, e chega ao cliente no mesmo
 * formato de erro. O valor aceita até duas casas: a fração de centavo é resultado de cálculo, e não
 * dado de entrada. O saldo não existe nesta mensagem, e nem pode existir.
 *
 * @param tipoEstabelecimento código do tipo do estabelecimento: 1 alimentação, 2 farmácia, 3
 *                            combustível
 * @param nomeEstabelecimento o estabelecimento em que a compra foi feita
 * @param numeroCartao        o número do cartão, com ou sem espaços e hífens
 * @param descricaoProduto    o que foi comprado
 * @param valor               o valor da compra, antes do ajuste do tipo
 */
public record MensagemRegistrarCompra(@NotNull Integer tipoEstabelecimento,
        @NotBlank @Size(max = 120) String nomeEstabelecimento,
        @NotBlank @Size(max = 25) String numeroCartao,
        @NotBlank @Size(max = 255) String descricaoProduto,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valor) {
}
