package br.com.alelo.consumer.consumerpat.dominio.saude;

/**
 * O estado de uma dependência crítica: capaz de cumprir a função, ou não.
 *
 * <p>
 * Não diz por que está indisponível. A causa, que pode citar host, URL de conexão ou versão do
 * banco, nunca chega ao corpo do health, que é público e sem autenticação. Quem verifica a
 * dependência, e converte a falha dela em erro de domínio, é o adaptador de cada uma.
 */
public enum EstadoDeDependencia {

    /** A dependência responde dentro do prazo. */
    DISPONIVEL,

    /** A dependência não responde, ou não responde a tempo. */
    INDISPONIVEL
}
