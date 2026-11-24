/**
 * Núcleo do sistema: regras, tipos de valor e erros de negócio.
 *
 * <p>
 * Este pacote não conhece Spring, JPA nem Lombok: depende só da biblioteca padrão do Java
 * ({@code java.*}). Quem usa o domínio (casos de uso, adaptadores, controllers) depende dele, e
 * nunca o contrário; a regra é verificada por teste, não por convenção.
 */
package br.com.alelo.consumer.consumerpat.dominio;
