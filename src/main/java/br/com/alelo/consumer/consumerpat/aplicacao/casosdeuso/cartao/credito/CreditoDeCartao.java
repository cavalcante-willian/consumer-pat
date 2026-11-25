package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.credito;

import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.ComSaidaDeLogDoCaso;
// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.ComBuscadorDeCartaoParaAtualizacao;
import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCredito;
import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCredito;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import org.springframework.transaction.annotation.Transactional;

/**
 * Papel de creditar um valor no saldo de um cartão.
 *
 * <p>
 * Só orquestra, na mesma ordem da compra: o cartão é buscado, travado até o fim da transação, e só
 * depois do crédito é gravado. O crédito é sempre sobre o valor informado, sem o ajuste do tipo: o
 * desconto e o acréscimo valem para compras, e não para saldo.
 */
public interface CreditoDeCartao
        extends ComBuscadorDeCartaoParaAtualizacao, ComGravadorDeCartao, ComSaidaDeLogDoCaso {

    /**
     * Credita o valor, e registra no log o crédito efetivado.
     *
     * @param credito o pedido, com os tipos do domínio
     * @return sucesso com o comprovante, ou falha com o erro que recusou: valor não positivo ou
     *         cartão não encontrado
     */
    @Transactional
    default Resultado<DtoComprovanteCredito, ErroDominio> creditar(DtoCredito credito) {
        return buscador()
                .buscarPorNumeroETipoParaAtualizacao(credito.numeroCartao(), credito.tipo())
                .encadear(cartao -> cartao.creditar(credito.valor())).mapear(gravador()::salvar)
                .mapear(creditado -> ComprovanteDoCredito.montar(credito, creditado))
                .aoSucesso(comprovante -> CartaoCreditado.registrar(saida(), comprovante));
    }
}
