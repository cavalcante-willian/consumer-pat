package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.CartoesDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.DadosDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Contato;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Email;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Telefone;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.documento.Documento;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.endereco.Cep;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.endereco.Endereco;

import java.util.List;
import java.util.Optional;

/**
 * Converte a linha do consumidor, com as dos cartões dele, no consumidor do domínio.
 *
 * <p>
 * É uma função pura: não consulta o banco, e quem a chama traz os cartões. Um telefone nulo na
 * coluna é um telefone ausente no domínio. O que sai do banco já passou pelos tipos do domínio na
 * ida, então os construtores o aceitam; um dado gravado por fora, que não passe, lança aqui, e é
 * isso que se quer, e não um consumidor inválido circulando.
 */
final class ConsumidorDeEntidade {

    private ConsumidorDeEntidade() {
    }

    /**
     * Monta o consumidor.
     *
     * @param entidade a linha do consumidor
     * @param cartoes  as linhas dos cartões dele
     * @return o consumidor do domínio, com id e versão
     */
    static Consumidor converter(ConsumidorEntidade entidade, List<CartaoEntidade> cartoes) {
        var contato = new Contato(new Telefone(entidade.getCelular()),
                Optional.ofNullable(entidade.getTelefoneResidencial()).map(Telefone::new),
                Optional.ofNullable(entidade.getTelefone()).map(Telefone::new),
                new Email(entidade.getEmail()));
        var endereco = new Endereco(entidade.getRua(), entidade.getNumero(), entidade.getCidade(),
                entidade.getPais(), new Cep(entidade.getCep()));
        var dados = new DadosDoConsumidor(entidade.getNome(),
                new Documento(entidade.getDocumento()), entidade.getDataNascimento(), contato,
                endereco);
        return new Consumidor(new ConsumidorId(entidade.getId()), dados,
                new CartoesDoConsumidor(cartoes.stream().map(CartaoDeEntidade::converter).toList()),
                entidade.getVersao());
    }
}
