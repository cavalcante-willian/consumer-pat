/**
 * A composition root: onde as dependências são instanciadas e ligadas, uma única vez.
 *
 * <p>
 * Nenhuma camada abaixo da API instancia um serviço concreto. Aqui se escolhe o relógio, a
 * auditoria e, conforme os itens avançam, os adaptadores das portas e os casos de uso.
 *
 * <p>
 * Cada classe declara, no máximo, um {@code @Bean}: uma configuração com dois beans tem duas razões
 * de mudar. Os filtros da requisição têm uma configuração cada, e a ordem entre eles, que é
 * contrato, fica em {@link br.com.alelo.consumer.consumerpat.api.configuracao.OrdemDosFiltros}.
 */
package br.com.alelo.consumer.consumerpat.api.configuracao;
