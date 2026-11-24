/**
 * A composition root: onde as dependências são instanciadas e ligadas, uma única vez.
 *
 * <p>
 * Nenhuma camada abaixo da API instancia um serviço concreto. Aqui se escolhe o relógio, a
 * auditoria e, conforme os itens avançam, os adaptadores das portas e os casos de uso.
 */
package br.com.alelo.consumer.consumerpat.api.configuracao;
