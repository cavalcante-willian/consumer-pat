package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
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
 * Converte a linha do consumidor, com os cartões dele já convertidos, no consumidor do domínio.
 *
 * <p>
 * É uma função pura: não consulta o banco, e quem a chama traz os cartões. Recebe os cartões do
 * domínio, e não as linhas deles, de propósito: assim este pacote, que é o da tabela do consumidor,
 * não depende do pacote do cartão, que depende dele pela chave estrangeira. Um telefone nulo na
 * coluna é um telefone ausente no domínio. O que sai do banco já passou pelos tipos do domínio na
 * ida, então os construtores o aceitam; um dado gravado por fora, que não passe, lança aqui, e é
 * isso que se quer, e não um consumidor inválido circulando.
 */
public final class ConsumidorDeEntidade {

    private ConsumidorDeEntidade() {
    }

    /**
     * Monta o consumidor.
     *
     * @param entidade a linha do consumidor
     * @param cartoes  os cartões dele, já convertidos
     * @return o consumidor do domínio, com id e versão
     */
    public static Consumidor converter(ConsumidorEntidade entidade, List<Cartao> cartoes) {
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
                new CartoesDoConsumidor(cartoes), entidade.getVersao());
    }
}
