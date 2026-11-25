package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.compra;

// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.ComBuscadorDeCartaoParaAtualizacao;
import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCompra;
import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCompra;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import org.springframework.transaction.annotation.Transactional;

/**
 * Papel de registrar uma compra: aplica o ajuste do tipo, debita o cartão e grava o que a compra
 * deixa, tudo numa transação.
 *
 * <p>
 * Só orquestra: cada passo é de uma peça. O extrato é montado antes de qualquer porta, de modo que
 * um pedido incompleto falha sem travar cartão; a busca trava o cartão até o fim da transação; e só
 * depois do último passo que pode falhar, o débito, é que algo é gravado. Uma {@code Falha} não
 * desfaz a transação, e não precisa: nada foi gravado antes dela. Uma exceção de infraestrutura,
 * como um extrato que não grava, desfaz o débito junto.
 *
 * <p>
 * O ajuste do tipo é aplicado ao valor informado, e é o valor cobrado, e não o informado, que
 * precisa caber no saldo: um desconto pode tornar possível uma compra maior que o saldo, e um
 * acréscimo pode tornar impossível uma menor.
 */
public interface RegistroDeCompra
        extends ComBuscadorDeCartaoParaAtualizacao, ComGravadorDaCompra, ComRelogioDaCompra {

    /**
     * Registra a compra.
     *
     * @param compra o pedido, com os tipos do domínio
     * @return sucesso com o comprovante, ou falha com o erro que recusou: valor não positivo,
     *         cartão não encontrado ou saldo insuficiente
     */
    @Transactional
    default Resultado<DtoComprovanteCompra, ErroDominio> registrar(DtoCompra compra) {
        var cobrado = compra.tipo().valorCobrado(compra.valor());
        var extrato = ExtratoDaCompra.montar(compra, cobrado, relogio().instant());
        return buscador()
                .buscarPorNumeroETipoParaAtualizacao(compra.numeroCartao(), compra.tipo())
                .encadear(cartao -> cartao.debitar(cobrado))
                .mapear(debitado -> gravador().gravar(debitado, extrato))
                .mapear(gravado -> ComprovanteDaCompra.montar(extrato, gravado));
    }
}
