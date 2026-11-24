package br.com.alelo.consumer.consumerpat.api.cartao.credito;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * O corpo do pedido de crédito.
 *
 * <p>
 * O cartão é identificado pelo número e pelo tipo, e o tipo vem com o mesmo código da compra. O
 * valor é positivo e aceita até duas casas. O legado aceitava um valor negativo e o somava ao
 * saldo: um crédito que debitava.
 *
 * @param numeroCartao        o número do cartão, com ou sem espaços e hífens
 * @param tipoEstabelecimento código do tipo em que o cartão vale: 1 alimentação, 2 farmácia, 3
 *                            combustível
 * @param valor               o valor a creditar
 */
public record MensagemCreditarCartao(@NotBlank @Size(max = 25) String numeroCartao,
        @NotNull Integer tipoEstabelecimento,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valor) {
}
