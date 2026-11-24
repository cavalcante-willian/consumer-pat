package br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.NovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroDocumentoJaCadastrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroNumeroDeCartaoJaCadastrado;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Faz no fake o que a unicidade do banco faz na porta real: o documento e o número de cartão são
 * únicos em todos os consumidores.
 */
public interface ConflitoDeNovoConsumidorEmMemoria extends ComConsumidoresEmMemoria {

    /**
     * Diz com o que o consumidor novo colide, se colide.
     *
     * @param novo o consumidor a gravar
     * @return o conflito de documento, ou o de número de cartão, ou vazio
     */
    default Optional<ErroDominio> conflitoDe(NovoConsumidor novo) {
        var existentes = memoria().consumidores().values();
        Set<NumeroDeCartao> numerosUsados = existentes.stream()
                .flatMap(consumidor -> consumidor.cartoes().cartoes().stream()).map(Cartao::numero)
                .collect(Collectors.toSet());
        Optional<ErroDominio> documentoRepetido = existentes.stream()
                .map(consumidor -> consumidor.dados().documento())
                .filter(novo.dados().documento()::equals).findFirst()
                .map(ErroDocumentoJaCadastrado::new);
        Optional<ErroDominio> numeroRepetido = novo.cartoes().cartoes().stream().map(Cartao::numero)
                .filter(numerosUsados::contains).findFirst()
                .map(ErroNumeroDeCartaoJaCadastrado::new);
        return documentoRepetido.or(() -> numeroRepetido);
    }
}
