package br.com.alelo.consumer.consumerpat.api.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.EmailDeTexto;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.TelefoneDeTexto;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.TelefoneOpcionalDeTexto;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.documento.DocumentoDeTexto;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.endereco.CepDeTexto;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.CampoValidado;

import java.util.List;

/**
 * Lê, um a um, os campos dos dados cadastrais que o domínio sabe conferir.
 *
 * <p>
 * Os campos são independentes entre si, e é por isso que o resultado é uma lista de leituras, e não
 * uma cadeia: quem a recebe reúne as falhas de todos, em vez de parar na primeira.
 */
public final class CamposDosDados {

    private CamposDosDados() {
    }

    /**
     * Lê o documento, os telefones, o e-mail e o CEP.
     *
     * @param dados os dados como chegaram
     * @return uma leitura por campo, com o nome do campo na entrada
     */
    public static List<CampoValidado> de(MensagemDadosConsumidor dados) {
        return List.of(
                new CampoValidado("documento", DocumentoDeTexto.converter(dados.documento())),
                new CampoValidado("celular", TelefoneDeTexto.converter(dados.celular(), "celular")),
                new CampoValidado("telefoneResidencial",
                        TelefoneOpcionalDeTexto.converter(dados.telefoneResidencial(),
                                "telefoneResidencial")),
                new CampoValidado("telefone",
                        TelefoneOpcionalDeTexto.converter(dados.telefone(), "telefone")),
                new CampoValidado("email", EmailDeTexto.converter(dados.email())),
                new CampoValidado("cep", CepDeTexto.converter(dados.cep())));
    }
}
