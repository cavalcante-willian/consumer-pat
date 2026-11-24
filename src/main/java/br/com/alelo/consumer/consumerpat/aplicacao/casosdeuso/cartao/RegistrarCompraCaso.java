package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCompra;
import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCompra;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.portas.CartaoPorta;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * Registra uma compra: aplica o ajuste do tipo, debita o cartão e grava o que a compra deixa, tudo
 * numa transação.
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
 *
 * <p>
 * A classe não é {@code final} porque o Spring a estende para aplicar a transação.
 */
public class RegistrarCompraCaso {

    private final CartaoPorta cartaoPorta;

    private final GravadorDaCompra gravador;

    private final Clock relogio;

    /**
     * Cria o caso de uso.
     *
     * @param cartaoPorta de onde o cartão é buscado
     * @param gravador    quem grava o cartão debitado e o extrato
     * @param relogio     de onde vem o instante da compra
     */
    public RegistrarCompraCaso(CartaoPorta cartaoPorta, GravadorDaCompra gravador, Clock relogio) {
        this.cartaoPorta = cartaoPorta;
        this.gravador = gravador;
        this.relogio = relogio;
    }

    /**
     * Registra a compra.
     *
     * @param compra o pedido, com os tipos do domínio
     * @return sucesso com o comprovante, ou falha com o erro que recusou: valor não positivo,
     *         cartão não encontrado ou saldo insuficiente
     */
    @Transactional
    public Resultado<DtoComprovanteCompra, ErroDominio> registrar(DtoCompra compra) {
        var cobrado = compra.tipo().valorCobrado(compra.valor());
        var extrato = ExtratoDaCompra.montar(compra, cobrado, relogio.instant());
        return cartaoPorta.buscarPorNumeroETipoParaAtualizacao(compra.numeroCartao(), compra.tipo())
                .encadear(cartao -> cartao.debitar(cobrado))
                .mapear(debitado -> gravador.gravar(debitado, extrato))
                .mapear(gravado -> ComprovanteDaCompra.montar(extrato, gravado));
    }
}
