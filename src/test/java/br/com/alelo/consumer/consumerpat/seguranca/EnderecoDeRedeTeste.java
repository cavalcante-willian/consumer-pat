package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnderecoDeRedeTeste {

    private static InetAddress endereco(int... bytes) throws UnknownHostException {
        var octetos = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            octetos[i] = (byte) bytes[i];
        }
        return InetAddress.getByAddress(octetos);
    }

    @Test
    void deve_escolher_o_primeiro_ipv4_que_nao_e_loopback_quando_ha_varios() throws Exception {
        // Preparar: loopback, enlace local, IPv6 e dois endereços de rede
        var enderecos = List.of(endereco(127, 0, 0, 1), endereco(169, 254, 1, 1),
                endereco(0xfe, 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                endereco(192, 168, 1, 5), endereco(10, 0, 0, 2));

        // Agir e Verificar
        assertThat(EnderecoDeRede.escolher(enderecos)).contains("192.168.1.5");
    }

    @Test
    void deve_nao_escolher_nada_quando_so_ha_loopback_e_enlace_local() throws Exception {
        // Preparar
        var enderecos = List.of(endereco(127, 0, 0, 1), endereco(169, 254, 1, 1),
                endereco(0, 0, 0, 0));

        // Agir e Verificar: sem endereço de verdade, o resultado é vazio, e não um inventado
        assertThat(EnderecoDeRede.escolher(enderecos)).isEmpty();
    }

    @Test
    void deve_nao_escolher_ipv6_quando_so_ha_ipv6_de_rede() throws Exception {
        // Preparar: um endereço global de IPv6
        var enderecos = List
                .of(endereco(0x20, 0x01, 0x0d, 0xb8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1));

        // Agir e Verificar: o servidor do teste escuta em IPv4
        assertThat(EnderecoDeRede.escolher(enderecos)).isEmpty();
    }

    @Test
    void deve_nao_escolher_nada_quando_a_lista_esta_vazia() {
        // Agir e Verificar
        assertThat(EnderecoDeRede.escolher(List.of())).isEmpty();
    }

    @Test
    void deve_devolver_ipv4_fora_do_loopback_ou_nada_quando_ler_as_interfaces() {
        // Agir
        var endereco = EnderecoDeRede.deUmaInterface();

        // Verificar: o que a máquina tem muda de um ambiente para outro, e o que se afirma é a
        // forma: quando há endereço, ele é um IPv4 de verdade e não é o loopback. Num ambiente sem
        // interface de rede o resultado é vazio, e o teste não depende disso
        assertThat(endereco.orElse("10.0.0.1")).matches("\\d{1,3}(\\.\\d{1,3}){3}")
                .doesNotStartWith("127.");
    }
}
