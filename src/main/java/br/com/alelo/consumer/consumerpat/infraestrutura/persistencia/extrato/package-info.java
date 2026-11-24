/**
 * A persistência do extrato: a entidade, o repositório e o adaptador da porta.
 *
 * <p>
 * Depende do pacote do cartão, de quem o extrato é filho. É imutável, só se registra: nenhuma
 * operação o atualiza nem o apaga, e a chave estrangeira para o cartão nunca cascateia exclusão.
 */
package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.extrato;
