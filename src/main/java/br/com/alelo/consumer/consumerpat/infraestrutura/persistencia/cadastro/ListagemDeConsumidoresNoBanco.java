package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ResumoDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.pagina.Pagina;
import br.com.alelo.consumer.consumerpat.dominio.pagina.PedidoDePagina;
import br.com.alelo.consumer.consumerpat.dominio.portas.ListadorDeConsumidores;
// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ComRepositorioDeConsumidores;

/**
 * Lista os consumidores no banco, uma página por vez.
 *
 * <p>
 * A página é pedida ao banco, e só o id e o nome são lidos. São duas consultas por listagem, a da
 * página e a da contagem, e nunca uma por consumidor. Uma página além da última é uma página vazia.
 */
interface ListagemDeConsumidoresNoBanco
        extends ComRepositorioDeConsumidores, ListadorDeConsumidores {

    @Override
    default Pagina<ResumoDeConsumidor> listar(PedidoDePagina pedido) {
        var lida = repositorioDeConsumidores().listarResumos(OrdenacaoNoBanco.de(pedido));
        return new Pagina<>(
                lida.getContent().stream()
                        .map(linha -> new ResumoDeConsumidor(new ConsumidorId(linha.id()),
                                linha.nome()))
                        .toList(),
                pedido.pagina(), pedido.tamanhoPagina(), lida.getTotalElements());
    }
}
