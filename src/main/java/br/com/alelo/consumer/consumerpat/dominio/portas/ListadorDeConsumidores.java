package br.com.alelo.consumer.consumerpat.dominio.portas;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.ResumoDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.pagina.Pagina;
import br.com.alelo.consumer.consumerpat.dominio.pagina.PedidoDePagina;

/**
 * Papel de quem sabe listar os consumidores, uma página por vez.
 *
 * <p>
 * Devolve resumos, e não consumidores: a listagem não precisa dos cartões nem do endereço, e
 * carregá-los por consumidor é o que fazia o legado devolver tudo de todos. A página é pedida ao
 * banco, e nunca montada na memória depois de ler tudo. Não devolve {@code Resultado}: listar não
 * tem falha de domínio, e uma página além da última é uma página vazia.
 */
public interface ListadorDeConsumidores {

    /**
     * Lista uma página de consumidores.
     *
     * @param pedido a página, o tamanho e a ordenação
     * @return a página, com o total do resultado todo
     */
    Pagina<ResumoDeConsumidor> listar(PedidoDePagina pedido);
}
