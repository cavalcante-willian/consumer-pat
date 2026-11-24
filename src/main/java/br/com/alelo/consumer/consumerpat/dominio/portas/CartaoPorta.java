package br.com.alelo.consumer.consumerpat.dominio.portas;

/**
 * Porta de acesso aos cartões: busca para atualizar e grava.
 *
 * <p>
 * Só compõe os dois papéis, cada um em seu arquivo. Em teste é substituída por um fake em memória,
 * e nunca pelo repositório real.
 */
public interface CartaoPorta extends BuscadorDeCartaoParaAtualizacao, GravadorDeCartao {
}
