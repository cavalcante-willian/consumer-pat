package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.NovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroDocumentoJaCadastrado;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ComRepositorioDeConsumidores; // editorconfig-checker-disable-line

import java.util.Optional;

/**
 * Diz, antes de gravar, com o que o consumidor novo colide.
 *
 * <p>
 * Existe por causa da transação: um {@code INSERT} que viola a unicidade lança exceção no meio dela
 * e a marca para reverter, e devolver uma falha depois disso faria o caso de uso confirmar uma
 * transação que já não pode ser confirmada. Conferir antes devolve o erro certo, sem exceção. A
 * unicidade do banco continua como rede de segurança para a corrida entre dois cadastros
 * simultâneos, que não passa por aqui.
 */
interface ConflitoDeNovoConsumidorNoBanco
        extends ComRepositorioDeConsumidores, NumeroDeCartaoRepetidoNoBanco {

    /**
     * Procura o conflito de documento e, na falta dele, o de número de cartão.
     *
     * @param novo o consumidor a gravar
     * @return o primeiro conflito, ou vazio se pode gravar
     */
    default Optional<ErroDominio> conflitoDe(NovoConsumidor novo) {
        Optional<ErroDominio> documentoRepetido = Optional.of(novo.dados().documento()).filter(
                documento -> repositorioDeConsumidores().existeComODocumento(documento.digitos()))
                .map(ErroDocumentoJaCadastrado::new);
        return documentoRepetido.or(() -> numeroRepetido(novo));
    }
}
