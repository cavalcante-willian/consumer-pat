/**
 * O consumidor: quem é, como falar com ele, onde mora e que cartões tem.
 *
 * <p>
 * {@link br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor} é o agregado raiz, e os
 * dados cadastrais ficam separados dos cartões de propósito: o que a atualização troca é um
 * {@link br.com.alelo.consumer.consumerpat.dominio.consumidor.DadosDoConsumidor}, que não tem por
 * onde carregar saldo. Um consumidor ainda não gravado é um
 * {@link br.com.alelo.consumer.consumerpat.dominio.consumidor.NovoConsumidor}, sem id nem versão. O
 * documento, o contato e o endereço têm um pacote cada, com o tipo de valor e a leitura do texto
 * que vem de fora.
 */
package br.com.alelo.consumer.consumerpat.dominio.consumidor;
