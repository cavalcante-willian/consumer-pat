package br.com.alelo.consumer.consumerpat.seguranca;

import br.com.alelo.consumer.consumerpat.medicao.ClienteHttp;
import br.com.alelo.consumer.consumerpat.medicao.ServidorFalso;
import br.com.alelo.consumer.consumerpat.medicao.ServidorFalso.Requisicao;
import br.com.alelo.consumer.consumerpat.medicao.ServidorFalso.Resposta;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PedidoPelaRedeTeste {

    @Test
    void deve_pedir_pelo_endereco_dado_mantendo_a_porta_quando_ha_endereco() throws Exception {
        try (var servidor = new ServidorFalso(r -> Resposta.de(200, "pela rede"))) {
            // Agir: o endereço do loopback faz as vezes de um endereço de rede, porque o servidor
            // falso só escuta nele
            var resposta = PedidoPelaRede.get(new ClienteHttp(servidor.url()),
                    Optional.of("127.0.0.1"), "/h2-console/login.jsp");

            // Verificar
            assertThat(resposta).hasValueSatisfying(r -> {
                assertThat(r.status()).isEqualTo(200);
                assertThat(r.texto()).isEqualTo("pela rede");
            });
            assertThat(servidor.recebidas())
                    .containsExactly(new Requisicao("GET", "/h2-console/login.jsp", ""));
        }
    }

    @Test
    void deve_devolver_vazio_e_nao_pedir_nada_quando_nao_ha_endereco() throws Exception {
        try (var servidor = new ServidorFalso(r -> Resposta.de(200, "x"))) {
            // Agir
            var resposta = PedidoPelaRede.get(new ClienteHttp(servidor.url()), Optional.empty(),
                    "/h2-console/login.jsp");

            // Verificar: sem endereço, não há resposta de fora para fingir
            assertThat(resposta).isEmpty();
            assertThat(servidor.recebidas()).isEmpty();
        }
    }
}
