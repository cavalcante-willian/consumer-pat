package br.com.alelo.consumer.consumerpat.api.consumidor.listar;

/**
 * Um consumidor na listagem: o id e o nome, e mais nada.
 *
 * <p>
 * Não traz documento, contato, endereço nem cartões: a listagem mostra quem existe, e o detalhe é
 * do {@code GET} por id.
 *
 * @param id   o identificador
 * @param nome o nome
 */
public record MensagemResumoConsumidor(long id, String nome) {
}
