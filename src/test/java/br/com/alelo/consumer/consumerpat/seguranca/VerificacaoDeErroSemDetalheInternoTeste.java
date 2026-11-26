package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeErroSemDetalheInternoTeste {

    private final Verificacao verificacao = new VerificacaoDeErroSemDetalheInterno();

    @Test
    void deve_proteger_quando_os_erros_dizem_so_o_que_o_cliente_fez() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(true));

        // Verificar
        assertThat(achado.chave()).isEqualTo("erro-sem-detalhe-interno");
        assertThat(achado.protegido()).isTrue();
        assertThat(achado.evidencia()).contains("marcadores: []");
    }

    @Test
    void deve_expor_quando_o_erro_de_json_traz_uma_pilha_de_chamadas() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.recusa = SondaFalsa.resposta(400,
                "com.fasterxml.jackson.core.JsonParseException\\n\\tat br.com.alelo.X.y(X.java:1)");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("Exception").contains("com.fasterxml");
    }

    @Test
    void deve_expor_quando_o_erro_de_cartao_inexistente_traz_texto_de_banco() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.naoEncontrado = SondaFalsa.resposta(404, "Hibernate: select * from cartao");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("Hibernate");
    }
}
