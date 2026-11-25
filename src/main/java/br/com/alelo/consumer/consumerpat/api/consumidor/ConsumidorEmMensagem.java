package br.com.alelo.consumer.consumerpat.api.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Telefone;

/**
 * Converte o consumidor do domínio na mensagem de saída.
 *
 * <p>
 * É onde se decide o que sai: o documento e os telefones mascarados, o e-mail e o endereço como
 * foram cadastrados, e nenhuma coluna interna além da versão, que o cliente precisa devolver.
 */
public final class ConsumidorEmMensagem {

    private ConsumidorEmMensagem() {
    }

    /**
     * Monta a mensagem.
     *
     * @param consumidor o consumidor do domínio
     * @return a mensagem de saída
     */
    public static MensagemConsumidor montar(Consumidor consumidor) {
        var dados = consumidor.dados();
        var contato = dados.contato();
        var endereco = dados.endereco();
        return new MensagemConsumidor(consumidor.id().valor(), consumidor.versao(), dados.nome(),
                dados.documento().toString(), dados.dataNascimento(), contato.celular().toString(),
                contato.telefoneResidencial().map(Telefone::toString).orElse(null),
                contato.telefone().map(Telefone::toString).orElse(null), contato.email().endereco(),
                endereco.rua(), endereco.numero(), endereco.cidade(), endereco.pais(),
                endereco.cep().digitos(),
                consumidor.cartoes().cartoes().stream().map(CartaoEmMensagem::montar).toList());
    }
}
