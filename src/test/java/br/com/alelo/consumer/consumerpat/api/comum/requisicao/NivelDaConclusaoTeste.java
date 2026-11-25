package br.com.alelo.consumer.consumerpat.api.comum.requisicao;

import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class NivelDaConclusaoTeste {

    @ParameterizedTest
    @CsvSource({ "200, INFORMACAO", "201, INFORMACAO", "204, INFORMACAO", "304, INFORMACAO",
            "400, INFORMACAO", "404, INFORMACAO", "405, INFORMACAO", "422, INFORMACAO",
            "408, INFORMACAO", "409, AVISO", "500, ERRO", "503, ERRO", "599, ERRO" })
    void deve_escolher_o_nivel_pelo_status_quando_a_requisicao_conclui(int status,
            NivelDeLog esperado) {
        // Agir e Verificar: o resultado de negócio esperado não é erro do sistema
        assertThat(NivelDaConclusao.de(status)).isEqualTo(esperado);
    }
}
