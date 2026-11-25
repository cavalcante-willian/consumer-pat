package br.com.alelo.consumer.consumerpat.api.consumidor.criar;

import br.com.alelo.consumer.consumerpat.api.consumidor.MensagemDadosConsumidor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.Map;

/**
 * O corpo do cadastro: os dados cadastrais e o número do cartão de cada tipo que o consumidor traz.
 *
 * <p>
 * Os cartões entram pelo nome do tipo ({@code ALIMENTACAO}, {@code FARMACIA}, {@code COMBUSTIVEL}),
 * e nascem com saldo zero: não há campo de saldo, e o Jackson recusa um que apareça. O mapa pode
 * ser vazio, e o consumidor nasce sem cartão.
 *
 * @param dados          os dados cadastrais
 * @param numerosCartoes o número de cada cartão, pelo nome do tipo
 */
public record MensagemCriarConsumidor(@NotNull @Valid MensagemDadosConsumidor dados,
        @NotNull Map<String, @NotBlank @Size(max = 25) String> numerosCartoes) {
}
