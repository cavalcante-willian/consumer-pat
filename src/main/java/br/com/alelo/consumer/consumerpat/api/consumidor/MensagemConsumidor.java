package br.com.alelo.consumer.consumerpat.api.consumidor;

import java.time.LocalDate;
import java.util.List;

/**
 * O consumidor, como a resposta o mostra.
 *
 * <p>
 * A versão sai porque é o que a atualização exige de volta. O documento e os telefones saem
 * mascarados: o cliente os enviou inteiros uma vez, e a resposta não os devolve. O e-mail e o
 * endereço saem como foram cadastrados. Um telefone que não existe não aparece no corpo.
 *
 * @param id                  o identificador
 * @param versao              a versão que o cliente reenvia ao atualizar
 * @param nome                o nome
 * @param documento           o documento, mascarado
 * @param dataNascimento      a data de nascimento
 * @param celular             o celular, mascarado
 * @param telefoneResidencial o telefone residencial, mascarado, se há
 * @param telefone            outro telefone, mascarado, se há
 * @param email               o e-mail
 * @param rua                 a rua
 * @param numero              o número do imóvel
 * @param cidade              a cidade
 * @param pais                o país
 * @param cep                 o CEP, só dígitos
 * @param cartoes             os cartões, ordenados pelo tipo
 */
public record MensagemConsumidor(long id, long versao, String nome, String documento,
        LocalDate dataNascimento, String celular, String telefoneResidencial, String telefone,
        String email, String rua, String numero, String cidade, String pais, String cep,
        List<MensagemCartao> cartoes) {
}
