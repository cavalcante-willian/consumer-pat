package br.com.alelo.consumer.consumerpat.dominio.saude;

/**
 * A porta que diz o estado de cada dependência crítica.
 *
 * <p>
 * A verificação é barata e tem prazo: uma dependência que não responde a tempo está indisponível, e
 * a porta nunca lança por causa disso. O que é interno, como a URL de conexão, o host, a versão do
 * banco e a mensagem de uma exceção, fica do lado de quem a implementa.
 */
public interface SaudePorta {

    /**
     * Consulta o estado de cada dependência crítica.
     *
     * @return o estado, com uma entrada por dependência
     */
    EstadoDeSaude consultar();
}
