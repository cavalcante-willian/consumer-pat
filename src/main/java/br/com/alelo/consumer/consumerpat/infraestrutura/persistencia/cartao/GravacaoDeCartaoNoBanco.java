package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.portas.GravadorDeCartao;

/**
 * A gravação do cartão no banco: atualiza o saldo de um cartão que já existe.
 *
 * <p>
 * Não cria cartão: criar exige o consumidor dono, que o domínio ainda não tem, e a criação virá com
 * o cadastro. Gravar um cartão que não existe é bug de programação, porque o caso de uso sempre o
 * lê antes, e por isso lança em vez de devolver um erro de domínio. A mensagem traz o número só
 * mascarado.
 */
interface GravacaoDeCartaoNoBanco extends ComRepositorioDeCartoes, GravadorDeCartao {

    @Override
    default Cartao salvar(Cartao cartao) {
        var entidade = repositorioDeCartoes()
                .buscarParaAtualizacao(cartao.numero().digitos(), cartao.tipo())
                .orElseThrow(() -> new IllegalStateException(
                        "Não existe cartão para gravar: " + cartao.numero()));
        return CartaoDeEntidade.converter(repositorioDeCartoes()
                .save(AtualizadorDeEntidadeDeCartao.atualizar(entidade, cartao)));
    }
}
