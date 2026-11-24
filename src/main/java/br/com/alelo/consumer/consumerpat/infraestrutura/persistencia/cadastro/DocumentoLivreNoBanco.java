package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroDocumentoJaCadastrado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ComRepositorioDeConsumidores; // editorconfig-checker-disable-line
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidorEntidade;

import java.util.Optional;

/**
 * Exige, na atualização, que o documento novo não seja de outro consumidor.
 *
 * <p>
 * O documento do próprio consumidor não conta, porque quem atualiza o cadastro sem mudar o
 * documento não colide consigo mesmo. Pelo mesmo motivo de {@code ConflitoDeNovoConsumidorNoBanco},
 * confere antes de gravar, e a unicidade do banco fica como rede de segurança.
 */
interface DocumentoLivreNoBanco extends ComRepositorioDeConsumidores {

    /**
     * Confere o documento do consumidor recebido.
     *
     * @param entidade   a linha do consumidor que está sendo atualizado
     * @param consumidor o consumidor com o documento novo
     * @return a mesma linha, ou {@link ErroDocumentoJaCadastrado}
     */
    default Resultado<ConsumidorEntidade, ErroDominio> exigirDocumentoLivre(
            ConsumidorEntidade entidade, Consumidor consumidor) {
        return AusenciaComoErro.converter(
                Optional.of(entidade)
                        .filter(linha -> !repositorioDeConsumidores().existeOutroComODocumento(
                                consumidor.dados().documento().digitos(), linha.getId())),
                () -> new ErroDocumentoJaCadastrado(consumidor.dados().documento()));
    }
}
