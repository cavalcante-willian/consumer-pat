package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.NovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroNumeroDeCartaoJaCadastrado;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao.ComRepositorioDeCartoes;

import java.util.Optional;

/**
 * Procura, entre os cartões do consumidor novo, um número que já é de outro cartão.
 *
 * <p>
 * O número é único no sistema inteiro, e não por consumidor, e a consulta é uma só para todos os
 * números do pedido.
 */
interface NumeroDeCartaoRepetidoNoBanco extends ComRepositorioDeCartoes {

    /**
     * Procura o primeiro número repetido.
     *
     * @param novo o consumidor a gravar
     * @return o conflito do primeiro número repetido, ou vazio
     */
    default Optional<ErroDominio> numeroRepetido(NovoConsumidor novo) {
        var numeros = novo.cartoes().cartoes().stream().map(Cartao::numero)
                .map(NumeroDeCartao::digitos).toList();
        return repositorioDeCartoes().numerosJaCadastrados(numeros).stream().findFirst()
                .map(NumeroDeCartao::new).map(ErroNumeroDeCartaoJaCadastrado::new);
    }
}
