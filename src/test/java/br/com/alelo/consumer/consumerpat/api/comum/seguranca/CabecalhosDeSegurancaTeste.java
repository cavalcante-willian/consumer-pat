package br.com.alelo.consumer.consumerpat.api.comum.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CabecalhosDeSegurancaTeste {

    @Test
    void deve_ter_exatamente_os_cinco_cabecalhos_do_contrato() {
        // Verificar
        assertThat(CabecalhosDeSeguranca.PADRAO).containsOnlyKeys("X-Content-Type-Options",
                "X-Frame-Options", "Content-Security-Policy", "Referrer-Policy", "Cache-Control");
    }

    @Test
    void deve_impedir_a_inferencia_de_tipo_o_quadro_e_o_referer() {
        // Verificar
        assertThat(CabecalhosDeSeguranca.PADRAO).containsEntry("X-Content-Type-Options", "nosniff")
                .containsEntry("X-Frame-Options", "DENY")
                .containsEntry("Referrer-Policy", "no-referrer");
    }

    @Test
    void deve_nao_permitir_nenhum_recurso_nem_moldura_na_politica_de_conteudo() {
        // Verificar: a resposta é JSON e não precisa carregar nada
        assertThat(CabecalhosDeSeguranca.PADRAO).containsEntry("Content-Security-Policy",
                "default-src 'none'; frame-ancestors 'none'");
    }

    @Test
    void deve_proibir_o_cache_do_dado_pessoal_e_do_saldo() {
        // Verificar
        assertThat(CabecalhosDeSeguranca.PADRAO).containsEntry("Cache-Control", "no-store");
    }

    @Test
    void deve_deixar_o_hsts_para_quem_termina_o_tls() {
        // Verificar: o Strict-Transport-Security só vale sobre HTTPS, e o TLS é terminado fora
        assertThat(CabecalhosDeSeguranca.PADRAO).doesNotContainKey("Strict-Transport-Security");
    }

    @Test
    void deve_ser_imutavel_para_nenhum_codigo_trocar_um_cabecalho_em_tempo_de_execucao() {
        // Agir e Verificar
        assertThatThrownBy(() -> CabecalhosDeSeguranca.PADRAO.put("X-Frame-Options", "ALLOWALL"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
