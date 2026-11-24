package br.com.alelo.consumer.consumerpat.fabricas;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.CartoesDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.DadosDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.NovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Contato;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Email;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Telefone;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.documento.Documento;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.endereco.Cep;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.endereco.Endereco;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * O ponto de partida de todo consumidor de teste: dados válidos e fixos, que o teste troca só no
 * que quer destacar.
 *
 * <p>
 * Os dados são sempre os mesmos, e é o que torna a falha de um teste legível: o que difere do
 * padrão é o que o teste está dizendo.
 */
public final class FabricaDeConsumidores {

    /** O documento de todo consumidor da fábrica. */
    public static final String DOCUMENTO_PADRAO = "12345678901";

    private FabricaDeConsumidores() {
    }

    /**
     * Dados cadastrais válidos, com o documento padrão.
     *
     * @return os dados
     */
    public static DadosDoConsumidor dados() {
        return dadosComDocumento(DOCUMENTO_PADRAO);
    }

    /**
     * Dados cadastrais válidos, com o documento informado.
     *
     * @param documento os dígitos do documento
     * @return os dados
     */
    public static DadosDoConsumidor dadosComDocumento(String documento) {
        return new DadosDoConsumidor("Maria Souza", new Documento(documento),
                LocalDate.of(1990, 5, 17),
                new Contato(new Telefone("11999998888"), Optional.empty(), Optional.empty(),
                        new Email("maria@exemplo.com.br")),
                new Endereco("Rua das Flores", "12-B", "São Paulo", "Brasil", new Cep("01001000")));
    }

    /**
     * Um consumidor ainda não gravado, com um cartão de alimentação de saldo zero.
     *
     * @return o consumidor a gravar
     */
    public static NovoConsumidor novo() {
        return new NovoConsumidor(dados(), new CartoesDoConsumidor(List
                .of(FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("0").construir())));
    }

    /**
     * Um consumidor gravado, com o id e a versão informados e um cartão de alimentação.
     *
     * @param id     o id
     * @param versao a versão da linha
     * @return o consumidor
     */
    public static Consumidor consumidor(long id, long versao) {
        return new Consumidor(new ConsumidorId(id), dados(),
                new CartoesDoConsumidor(
                        List.of(FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).construir())),
                versao);
    }
}
