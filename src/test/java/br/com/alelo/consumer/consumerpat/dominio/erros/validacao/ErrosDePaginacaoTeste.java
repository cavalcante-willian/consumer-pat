package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrosDePaginacaoTeste {

    @Test
    void deve_apontar_a_pagina_quando_ela_nao_comeca_em_um() {
        // Verificar
        var erro = new ErroPaginaInvalida();
        assertThat(erro.categoria()).isEqualTo(CategoriaErro.VALIDACAO);
        assertThat(erro.codigo()).isEqualTo(ErroPaginaInvalida.CODIGO);
        assertThat(erro.camposComErro()).containsOnlyKeys("pagina");
    }

    @Test
    void deve_dizer_o_teto_quando_o_tamanho_esta_fora_dos_limites() {
        // Verificar
        var erro = new ErroTamanhoDePaginaInvalido();
        assertThat(erro.codigo()).isEqualTo(ErroTamanhoDePaginaInvalido.CODIGO);
        assertThat(erro.camposComErro()).containsOnlyKeys("tamanhoPagina");
        assertThat(erro.mensagem()).contains("100");
    }

    @Test
    void deve_listar_os_campos_aceitos_quando_a_ordenacao_e_invalida() {
        // Verificar
        var erro = new ErroOrdenacaoInvalida();
        assertThat(erro.codigo()).isEqualTo(ErroOrdenacaoInvalida.CODIGO);
        assertThat(erro.camposComErro()).containsOnlyKeys("ordenacao");
        assertThat(erro.mensagem()).contains("ID", "NOME");
    }

    @Test
    void deve_listar_os_sentidos_aceitos_quando_a_direcao_e_invalida() {
        // Verificar
        var erro = new ErroDirecaoInvalida();
        assertThat(erro.codigo()).isEqualTo(ErroDirecaoInvalida.CODIGO);
        assertThat(erro.camposComErro()).containsOnlyKeys("direcao");
        assertThat(erro.mensagem()).contains("ASC", "DESC");
    }
}
