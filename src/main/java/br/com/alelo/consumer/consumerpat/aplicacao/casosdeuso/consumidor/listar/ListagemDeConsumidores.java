package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.listar;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.ResumoDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.pagina.Pagina;
import br.com.alelo.consumer.consumerpat.dominio.pagina.PedidoDePagina;
import org.springframework.transaction.annotation.Transactional;

/**
 * Papel de listar os consumidores, uma página por vez.
 *
 * <p>
 * Só entrega o pedido à porta: a ordenação, o desempate e o corte da página são decididos antes, na
 * leitura dos parâmetros, e depois, no banco. A transação é só de leitura. Não devolve
 * {@code Resultado}, porque listar não tem falha de domínio: um pedido já chega válido, e uma
 * página além da última é uma página vazia.
 */
public interface ListagemDeConsumidores extends ComListadorDeConsumidores {

    /**
     * Lista uma página de consumidores.
     *
     * @param pedido a página, o tamanho e a ordenação
     * @return a página, com o total do resultado todo
     */
    @Transactional(readOnly = true)
    default Pagina<ResumoDeConsumidor> listar(PedidoDePagina pedido) {
        return listador().listar(pedido);
    }
}
