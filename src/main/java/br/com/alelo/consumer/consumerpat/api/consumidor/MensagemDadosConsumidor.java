package br.com.alelo.consumer.consumerpat.api.consumidor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Os dados cadastrais do consumidor, como chegam no corpo do cadastro e da atualização.
 *
 * <p>
 * O Bean Validation confere só o grosso: presença e tamanho. O formato de cada dado, o que é um
 * documento, um telefone, um e-mail ou um CEP, é do domínio, que o lê sem lançar exceção e devolve
 * todos os erros de uma vez. Os tamanhos são os das colunas, com folga para a pontuação que o
 * domínio remove. A data de nascimento usa o relógio do sistema, a única exceção declarada à regra
 * de um relógio só.
 *
 * <p>
 * Não há campo de saldo nem de cartão: o Jackson recusa o que não está aqui, de modo que um
 * cadastro não consegue alterar dinheiro.
 *
 * @param nome                o nome
 * @param documento           o CPF ou CNPJ, com ou sem pontuação
 * @param dataNascimento      a data de nascimento
 * @param celular             o celular, com ou sem pontuação
 * @param telefoneResidencial o telefone residencial, se há
 * @param telefone            outro telefone, se há
 * @param email               o e-mail
 * @param rua                 a rua
 * @param numero              o número do imóvel, como texto
 * @param cidade              a cidade
 * @param pais                o país
 * @param cep                 o CEP, com ou sem hífen
 */
public record MensagemDadosConsumidor(@NotBlank @Size(max = 150) String nome,
        @NotBlank @Size(max = 18) String documento, @NotNull @Past LocalDate dataNascimento,
        @NotBlank @Size(max = 20) String celular, @Size(max = 20) String telefoneResidencial,
        @Size(max = 20) String telefone, @NotBlank @Size(max = 254) String email,
        @NotBlank @Size(max = 100) String rua, @NotBlank @Size(max = 10) String numero,
        @NotBlank @Size(max = 100) String cidade, @NotBlank @Size(max = 100) String pais,
        @NotBlank @Size(max = 9) String cep) {
}
