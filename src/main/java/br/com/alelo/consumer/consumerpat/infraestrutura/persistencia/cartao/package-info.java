/**
 * A persistência do cartão: a entidade, o repositório, o adaptador da porta e a conversão.
 *
 * <p>
 * Depende do pacote do consumidor, de quem é o dono do cartão. O cartão é lido com trava de
 * escrita, mantida até o fim da transação do caso de uso, de modo que dois débitos simultâneos no
 * mesmo cartão se enfileiram em vez de sobrescreverem um ao outro. A coluna de versão fica como
 * rede de segurança: se a trava for removida sem querer, o conflito vira um erro visível, e não uma
 * perda silenciosa de saldo.
 */
package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;
