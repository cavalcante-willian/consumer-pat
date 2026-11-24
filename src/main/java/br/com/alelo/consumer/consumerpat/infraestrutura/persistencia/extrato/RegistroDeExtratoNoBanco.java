package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.extrato;

import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;
import br.com.alelo.consumer.consumerpat.dominio.portas.ExtratoPorta;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao.ComRepositorioDeCartoes;

/**
 * O registro do extrato no banco: só insere, nunca atualiza nem apaga.
 *
 * <p>
 * O extrato é ligado ao cartão pelo número. Registrar o extrato de um cartão que não existe é bug
 * de programação, porque o extrato nasce de uma compra que acabou de debitar esse cartão, e por
 * isso lança. A mensagem traz o número só mascarado.
 */
interface RegistroDeExtratoNoBanco
        extends ComRepositorioDeCartoes, ComRepositorioDeExtratos, ExtratoPorta {

    @Override
    default void registrar(Extrato extrato) {
        var cartao = repositorioDeCartoes().buscarPorNumero(extrato.numeroDoCartao().digitos())
                .orElseThrow(() -> new IllegalStateException(
                        "Não existe cartão para o extrato: " + extrato.numeroDoCartao()));
        repositorioDeExtratos().save(EntidadeDeExtrato.criar(extrato, cartao));
    }
}
