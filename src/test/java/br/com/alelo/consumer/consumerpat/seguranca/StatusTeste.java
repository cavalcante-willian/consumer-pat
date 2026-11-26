package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StatusTeste {

    @ParameterizedTest
    @ValueSource(ints = { 400, 404, 409, 422, 499 })
    void deve_ser_erro_de_cliente_quando_o_status_e_de_quatrocentos(int status) {
        // Agir e Verificar
        assertThat(Status.erroDeCliente(status)).isTrue();
        assertThat(Status.erroDeServidor(status)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = { 500, 503, 599 })
    void deve_ser_erro_de_servidor_quando_o_status_e_de_quinhentos(int status) {
        // Agir e Verificar
        assertThat(Status.erroDeServidor(status)).isTrue();
        assertThat(Status.erroDeCliente(status)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 201, 204, 302, 399, 600 })
    void deve_nao_ser_nenhum_dos_dois_quando_o_status_esta_fora_das_faixas(int status) {
        // Agir e Verificar: um 302 do console do H2 não é recusa
        assertThat(Status.erroDeCliente(status)).isFalse();
        assertThat(Status.erroDeServidor(status)).isFalse();
    }
}
