package br.com.alelo.consumer.consumerpat.seguranca;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Acha um endereço da própria máquina que não é o loopback.
 *
 * <p>
 * Um sistema que roda aqui e é pedido por {@code localhost} vê uma origem local, e há comportamento
 * que depende disso: o console do H2 só abre para o loopback. Pedido pelo endereço de uma interface
 * de rede, o mesmo sistema vê uma origem que não é loopback, e é assim que se testa o que ele faz
 * com quem vem de fora, sem outra máquina e sem contêiner.
 *
 * <p>
 * Quando a máquina não tem nenhuma interface fora do loopback, não há o que devolver, e quem pede
 * sabe que não pôde verificar, em vez de receber um endereço inventado.
 */
public final class EnderecoDeRede {

    private EnderecoDeRede() {
    }

    /**
     * Um endereço IPv4 de uma interface de rede desta máquina.
     *
     * @return o endereço, ou vazio se a máquina não tem nenhum fora do loopback
     */
    public static Optional<String> deUmaInterface() {
        try {
            return escolher(Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .flatMap(interfaceDeRede -> Collections.list(interfaceDeRede.getInetAddresses())
                            .stream())
                    .toList());
        } catch (SocketException e) {
            return Optional.empty();
        }
    }

    /**
     * Escolhe, entre endereços, o primeiro IPv4 que não é loopback nem de enlace local.
     *
     * <p>
     * O IPv6 fica de fora porque o servidor que o teste sobe escuta em IPv4, e o de enlace local
     * não é roteável por onde um cliente de verdade chegaria.
     *
     * @param enderecos os endereços da máquina
     * @return o endereço escolhido, ou vazio se nenhum serve
     */
    static Optional<String> escolher(List<InetAddress> enderecos) {
        return enderecos.stream()
                .filter(endereco -> endereco instanceof Inet4Address
                        && !endereco.isLoopbackAddress() && !endereco.isLinkLocalAddress()
                        && !endereco.isAnyLocalAddress())
                .map(InetAddress::getHostAddress).findFirst();
    }
}
