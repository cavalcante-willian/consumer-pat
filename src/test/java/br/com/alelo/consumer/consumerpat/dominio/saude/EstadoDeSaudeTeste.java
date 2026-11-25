package br.com.alelo.consumer.consumerpat.dominio.saude;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EstadoDeSaudeTeste {

    @Test
    void deve_ser_saudavel_quando_todas_as_dependencias_estao_disponiveis() {
        // Agir
        var estado = new EstadoDeSaude(
                Map.of(DependenciaCritica.BANCO, EstadoDeDependencia.DISPONIVEL));

        // Verificar
        assertThat(estado.situacao()).isEqualTo(SituacaoDeSaude.SAUDAVEL);
    }

    @Test
    void deve_ser_degradado_quando_alguma_dependencia_esta_indisponivel() {
        // Agir
        var estado = new EstadoDeSaude(
                Map.of(DependenciaCritica.BANCO, EstadoDeDependencia.INDISPONIVEL));

        // Verificar: processo vivo com o banco fora do ar não está saudável
        assertThat(estado.situacao()).isEqualTo(SituacaoDeSaude.DEGRADADO);
    }

    @Test
    void deve_recusar_o_estado_sem_dependencias_quando_nao_ha_o_que_conferir() {
        // Agir e Verificar: "saudável" por não ter o que conferir seria verificação vazia
        assertThatThrownBy(() -> new EstadoDeSaude(Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_recusar_o_mapa_nulo_quando_criar() {
        // Agir e Verificar
        assertThatThrownBy(() -> new EstadoDeSaude(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void deve_guardar_uma_copia_imutavel_quando_o_mapa_original_muda_depois() {
        // Preparar
        var original = new HashMap<DependenciaCritica, EstadoDeDependencia>();
        original.put(DependenciaCritica.BANCO, EstadoDeDependencia.DISPONIVEL);
        var estado = new EstadoDeSaude(original);

        // Agir
        original.put(DependenciaCritica.BANCO, EstadoDeDependencia.INDISPONIVEL);

        // Verificar
        assertThat(estado.situacao()).isEqualTo(SituacaoDeSaude.SAUDAVEL);
        assertThatThrownBy(() -> estado.dependencias().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void deve_derivar_a_situacao_sem_guardar_um_campo_que_possa_divergir() {
        // Verificar: a situação é do papel ApuracaoDaSituacao, e não um componente do record
        assertThat(EstadoDeSaude.class.getRecordComponents()).extracting("name")
                .containsExactly("dependencias");
        assertThat(ApuracaoDaSituacao.class).isAssignableFrom(EstadoDeSaude.class);
    }
}
