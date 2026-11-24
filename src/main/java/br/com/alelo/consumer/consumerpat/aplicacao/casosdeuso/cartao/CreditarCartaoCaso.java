package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCredito;
import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCredito;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.portas.CartaoPorta;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import org.springframework.transaction.annotation.Transactional;

/**
 * Credita um valor no saldo de um cartão.
 *
 * <p>
 * Só orquestra, na mesma ordem da compra: o cartão é buscado, travado até o fim da transação, e só
 * depois do crédito é gravado. O crédito é sempre sobre o valor informado, sem o ajuste do tipo: o
 * desconto e o acréscimo valem para compras, e não para saldo.
 *
 * <p>
 * A classe não é {@code final} porque o Spring a estende para aplicar a transação.
 */
public class CreditarCartaoCaso {

    private final CartaoPorta cartaoPorta;

    /**
     * Cria o caso de uso.
     *
     * @param cartaoPorta onde o cartão é buscado e gravado
     */
    public CreditarCartaoCaso(CartaoPorta cartaoPorta) {
        this.cartaoPorta = cartaoPorta;
    }

    /**
     * Credita o valor.
     *
     * @param credito o pedido, com os tipos do domínio
     * @return sucesso com o comprovante, ou falha com o erro que recusou: valor não positivo ou
     *         cartão não encontrado
     */
    @Transactional
    public Resultado<DtoComprovanteCredito, ErroDominio> creditar(DtoCredito credito) {
        return cartaoPorta
                .buscarPorNumeroETipoParaAtualizacao(credito.numeroCartao(), credito.tipo())
                .encadear(cartao -> cartao.creditar(credito.valor())).mapear(cartaoPorta::salvar)
                .mapear(creditado -> ComprovanteDoCredito.montar(credito, creditado));
    }
}
