package br.com.alelo.consumer.consumerpat.api.saude;

import br.com.alelo.consumer.consumerpat.dominio.saude.SituacaoDeSaude;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class StatusDaSaudeTeste {

    @Test
    void deve_ser_200_quando_o_sistema_esta_saudavel() {
        // Verificar
        assertThat(StatusDaSaude.de(SituacaoDeSaude.SAUDAVEL)).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deve_ser_503_quando_o_sistema_esta_degradado() {
        // Verificar
        assertThat(StatusDaSaude.de(SituacaoDeSaude.DEGRADADO))
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ParameterizedTest
    @EnumSource(SituacaoDeSaude.class)
    void deve_ter_status_para_toda_situacao_quando_uma_nova_aparecer(SituacaoDeSaude situacao) {
        // Verificar: situação nova sem status quebra o build, e não devolve nulo em produção
        assertThat(StatusDaSaude.de(situacao)).isNotNull();
    }
}
