package br.com.alelo.consumer.consumerpat.api.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.DadosDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Contato;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.EmailDeTexto;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.TelefoneDeTexto;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.TelefoneOpcionalDeTexto;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.documento.DocumentoDeTexto;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.endereco.CepDeTexto;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.endereco.Endereco;

/**
 * Monta os dados cadastrais de uma mensagem cujos campos já foram conferidos.
 *
 * <p>
 * Só se chama depois de {@link CamposDosDados} não achar nenhuma falha, e por isso extrai os
 * valores sem tratar o erro: se um falhar aqui, é bug de programação, e {@link ValorConfirmado}
 * lança.
 */
public final class DadosConfirmados {

    private DadosConfirmados() {
    }

    /**
     * Monta os dados.
     *
     * @param dados os dados como chegaram, já conferidos
     * @return os dados nos tipos do domínio
     */
    public static DadosDoConsumidor montar(MensagemDadosConsumidor dados) {
        var contato = new Contato(
                ValorConfirmado.de(TelefoneDeTexto.converter(dados.celular(), "celular")),
                ValorConfirmado.de(TelefoneOpcionalDeTexto.converter(dados.telefoneResidencial(),
                        "telefoneResidencial")),
                ValorConfirmado.de(TelefoneOpcionalDeTexto.converter(dados.telefone(), "telefone")),
                ValorConfirmado.de(EmailDeTexto.converter(dados.email())));
        var endereco = new Endereco(dados.rua(), dados.numero(), dados.cidade(), dados.pais(),
                ValorConfirmado.de(CepDeTexto.converter(dados.cep())));
        return new DadosDoConsumidor(dados.nome(),
                ValorConfirmado.de(DocumentoDeTexto.converter(dados.documento())),
                dados.dataNascimento(), contato, endereco);
    }
}
