/**
 * Persistência em banco relacional: um subpacote por agregado.
 *
 * <p>
 * O schema nasce só das anotações das entidades, sem DDL à mão e sem migração: a ferramenta de
 * migração seria dependência nova, proibida pelo enunciado. Toda restrição e todo índice é nomeado
 * na anotação, para o schema gerado ser auditável.
 */
package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia;
