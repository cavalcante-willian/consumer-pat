/**
 * Persistência do cartão e do seu extrato.
 *
 * <p>
 * O extrato mora aqui, e não em pacote próprio, porque é filho do cartão: a chave estrangeira exige
 * que a entidade do extrato enxergue a do cartão, e as duas são pacote-privadas. Só os adaptadores
 * são públicos, e cada um implementa uma porta do domínio.
 *
 * <p>
 * O cartão é lido com trava de escrita, mantida até o fim da transação do caso de uso, de modo que
 * dois débitos simultâneos no mesmo cartão se enfileiram em vez de sobrescreverem um ao outro. A
 * coluna de versão fica como rede de segurança: se a trava for removida sem querer, o conflito vira
 * um erro visível, e não uma perda silenciosa de saldo.
 */
package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;
