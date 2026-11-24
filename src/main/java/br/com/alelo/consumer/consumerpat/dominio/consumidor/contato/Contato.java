package br.com.alelo.consumer.consumerpat.dominio.consumidor.contato;

import java.util.Objects;
import java.util.Optional;

/**
 * Como falar com o consumidor: o celular, que é obrigatório, os telefones residencial e geral, que
 * podem não existir, e o e-mail.
 *
 * <p>
 * Os dois telefones opcionais são {@link Optional} porque o cadastro do legado já os aceitava
 * vazios, e a ausência é um estado do domínio, e não um {@code null} que cada chamador precisa
 * lembrar de tratar.
 *
 * @param celular             o celular de contato
 * @param telefoneResidencial o telefone residencial, se há
 * @param telefone            outro telefone, se há
 * @param email               o e-mail
 */
public record Contato(Telefone celular, Optional<Telefone> telefoneResidencial,
        Optional<Telefone> telefone, Email email) {

    /**
     * Cria o contato.
     *
     * @param celular             o celular de contato
     * @param telefoneResidencial o telefone residencial, se há
     * @param telefone            outro telefone, se há
     * @param email               o e-mail
     * @throws NullPointerException se falta o celular, o e-mail ou qualquer um dos opcionais (que,
     *                              se ausentes, são {@link Optional#empty()}, e nunca nulos)
     */
    public Contato {
        Objects.requireNonNull(celular, "O contato precisa de um celular");
        Objects.requireNonNull(telefoneResidencial, "O telefone residencial não pode ser nulo");
        Objects.requireNonNull(telefone, "O telefone não pode ser nulo");
        Objects.requireNonNull(email, "O contato precisa de um e-mail");
    }
}
