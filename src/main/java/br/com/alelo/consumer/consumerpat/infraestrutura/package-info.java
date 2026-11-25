/**
 * Camada de infraestrutura: implementa as portas do domínio e é a única que conhece JPA.
 *
 * <p>
 * Depende do domínio e, se precisar, da aplicação, e nunca o contrário. As entidades e os
 * repositórios do Spring Data são públicos, porque a persistência se divide em pacotes por conceito
 * e as chaves estrangeiras os fazem se enxergar; a barreira, então, é um teste, e não o compilador:
 * nada fora de {@code infraestrutura.persistencia} pode importá-los, e o que sai daqui são as
 * portas.
 */
package br.com.alelo.consumer.consumerpat.infraestrutura;
