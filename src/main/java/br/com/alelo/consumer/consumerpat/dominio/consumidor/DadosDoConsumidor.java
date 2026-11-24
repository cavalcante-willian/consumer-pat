package br.com.alelo.consumer.consumerpat.dominio.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Contato;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.documento.Documento;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.endereco.Endereco;
import br.com.alelo.consumer.consumerpat.dominio.texto.TextoLimitado;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Os dados cadastrais de um consumidor: quem é, como falar com ele e onde mora.
 *
 * <p>
 * É o que a atualização troca, e só isso. Os cartões e o saldo ficam de fora, de modo que atualizar
 * um cadastro não pode, nem por engano, mexer em dinheiro: o tipo não tem por onde carregá-lo.
 *
 * @param nome           o nome, de 1 a {@value #MAXIMO_DO_NOME} caracteres
 * @param documento      o documento, na forma canônica
 * @param dataNascimento a data de nascimento
 * @param contato        como falar com ele
 * @param endereco       onde mora
 */
public record DadosDoConsumidor(String nome, Documento documento, LocalDate dataNascimento,
        Contato contato, Endereco endereco) {

    /** Maior quantidade de caracteres do nome. */
    public static final int MAXIMO_DO_NOME = 150;

    /**
     * Cria os dados.
     *
     * @param nome           o nome
     * @param documento      o documento
     * @param dataNascimento a data de nascimento
     * @param contato        como falar com ele
     * @param endereco       onde mora
     * @throws IllegalArgumentException se o nome é vazio ou passa do limite
     * @throws NullPointerException     se falta o documento, a data, o contato ou o endereço
     */
    public DadosDoConsumidor {
        nome = TextoLimitado.exigir(nome, "O nome", MAXIMO_DO_NOME);
        Objects.requireNonNull(documento, "O consumidor precisa de um documento");
        Objects.requireNonNull(dataNascimento, "O consumidor precisa de uma data de nascimento");
        Objects.requireNonNull(contato, "O consumidor precisa de um contato");
        Objects.requireNonNull(endereco, "O consumidor precisa de um endereço");
    }
}
