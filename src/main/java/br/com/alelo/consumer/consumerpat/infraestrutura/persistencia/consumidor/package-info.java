/**
 * A tabela do consumidor: a entidade, o repositório e a conversão dos dados cadastrais.
 *
 * <p>
 * É a folha da persistência: não conhece o cartão, que aponta para ele pela chave estrangeira. Por
 * isso a conversão do consumidor recebe os cartões já convertidos, e quem os junta é o pacote
 * {@code cadastro}. O consumidor não tem coleção de cartões: atualizar um cadastro nunca toca nos
 * cartões, que são o caminho quente, com trava.
 */
package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;
