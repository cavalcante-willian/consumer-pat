package br.com.alelo.consumer.consumerpat.dominio.portas;

import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;

/**
 * Porta de acesso aos extratos: só registra.
 *
 * <p>
 * Um extrato é registro financeiro e nunca é atualizado nem apagado, então a porta não oferece
 * atualizar nem excluir: o que nenhuma interface oferece, ninguém chama por engano.
 */
public interface ExtratoPorta {

    /**
     * Registra o extrato de uma compra efetivada.
     *
     * @param extrato o extrato a registrar
     */
    void registrar(Extrato extrato);
}
