package br.com.alelo.consumer.consumerpat.seguranca;

import br.com.alelo.consumer.consumerpat.medicao.ClienteHttp;
import br.com.alelo.consumer.consumerpat.medicao.RespostaHttp;

import java.util.Optional;

/**
 * Faz um {@code GET} a um sistema pelo endereço de uma interface de rede da própria máquina, e não
 * por {@code localhost}.
 *
 * <p>
 * O sistema vê uma origem que não é loopback, o que deixa a bateria perguntar o que ele faz com
 * quem vem de fora. Sem endereço de rede para usar, devolve vazio: a verificação diz que não pôde
 * verificar, em vez de dar o resultado do pedido local como se fosse o de fora.
 */
public final class PedidoPelaRede {

    private PedidoPelaRede() {
    }

    /**
     * Pede por um endereço de rede desta máquina, o primeiro que ela tiver.
     *
     * @param http    o cliente do sistema, que define o esquema e a porta
     * @param caminho o caminho, começando por barra
     * @return a resposta, ou vazio se a máquina não tem endereço fora do loopback
     * @throws Exception se a requisição falha
     */
    public static Optional<RespostaHttp> get(ClienteHttp http, String caminho) throws Exception {
        return get(http, EnderecoDeRede.deUmaInterface(), caminho);
    }

    /**
     * Pede por um endereço dado.
     *
     * @param http     o cliente do sistema, que define o esquema e a porta
     * @param endereco o endereço a usar, se houver
     * @param caminho  o caminho, começando por barra
     * @return a resposta, ou vazio se não há endereço
     * @throws Exception se a requisição falha
     */
    static Optional<RespostaHttp> get(ClienteHttp http, Optional<String> endereco, String caminho)
            throws Exception {
        for (var usado : endereco.stream().toList()) {
            return Optional.of(http.noEndereco(usado).get(caminho));
        }
        return Optional.empty();
    }
}
