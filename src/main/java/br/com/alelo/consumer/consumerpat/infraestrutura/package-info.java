/**
 * Camada de infraestrutura: implementa as portas do domínio e é a única que conhece JPA.
 *
 * <p>
 * Depende do domínio e, se precisar, da aplicação, e nunca o contrário. Entidades e repositórios do
 * Spring Data ficam pacote-privados, de modo que nada fora do pacote enxerga o mapeamento: o que
 * sai daqui são as portas.
 */
package br.com.alelo.consumer.consumerpat.infraestrutura;
