package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.atualizar;

import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.ComSaidaDeLogDoCaso;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.ComBuscadorDeConsumidor;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.ConflitoDetectado;
import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoAtualizacaoDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import org.springframework.transaction.annotation.Transactional;

/**
 * Papel de atualizar o cadastro de um consumidor, e só o cadastro.
 *
 * <p>
 * Busca o consumidor, confere que a versão que o cliente leu ainda é a atual, troca os dados com
 * {@code comDados} e grava. Os cartões e o saldo são os que a busca devolveu, e nada no pedido pode
 * alterá-los: o pedido não os carrega. Uma compra feita entre a leitura e a atualização do cadastro
 * não é desfeita.
 *
 * <p>
 * Uma versão velha é recusada aqui, antes de gravar. Dois clientes que leem a mesma versão ao mesmo
 * tempo passam desta conferência, e o segundo a perde na gravação, pela versão de linha do banco.
 */
public interface AtualizacaoDeCadastro
        extends ComBuscadorDeConsumidor, ComAtualizadorDeConsumidor, ComSaidaDeLogDoCaso {

    /**
     * Atualiza o cadastro, e registra a atualização ou o conflito.
     *
     * @param pedido o consumidor, a versão que o cliente leu e os dados novos
     * @return o consumidor como ficou, ou o erro de consumidor ausente, de versão divergente ou de
     *         documento repetido
     */
    @Transactional
    default Resultado<Consumidor, ErroDominio> atualizar(DtoAtualizacaoDeConsumidor pedido) {
        return buscador().buscarPorId(pedido.id())
                .encadear(atual -> VersaoConfirmada.exigir(atual, pedido.versao()))
                .mapear(atual -> atual.comDados(pedido.dados())).encadear(atualizador()::atualizar)
                .aoSucesso(atualizado -> ConsumidorAtualizado.registrar(saida(), atualizado))
                .aoFalhar(
                        erro -> ConflitoDetectado.registrar(saida(), "atualizar-consumidor", erro));
    }
}
