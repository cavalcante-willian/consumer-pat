/**
 * Persistência do consumidor e do que pertence a ele: os cartões e o extrato de cada um.
 *
 * <p>
 * O pacote é do agregado, e não de uma tabela, porque as entidades são pacote-privadas e as chaves
 * estrangeiras exigem que elas se enxerguem: o cartão aponta para o consumidor, e o extrato aponta
 * para o cartão. Só os adaptadores são públicos, e cada um implementa uma porta do domínio.
 *
 * <p>
 * O cartão é lido com trava de escrita, mantida até o fim da transação do caso de uso, de modo que
 * dois débitos simultâneos no mesmo cartão se enfileiram em vez de sobrescreverem um ao outro. A
 * coluna de versão fica como rede de segurança: se a trava for removida sem querer, o conflito vira
 * um erro visível, e não uma perda silenciosa de saldo.
 */
package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;
