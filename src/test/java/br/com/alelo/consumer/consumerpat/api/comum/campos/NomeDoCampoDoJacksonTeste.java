package br.com.alelo.consumer.consumerpat.api.comum.campos;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NomeDoCampoDoJacksonTeste {

    @Test
    void deve_dar_o_nome_do_campo_quando_o_erro_e_de_um_campo_do_corpo() {
        // Preparar
        var excecao = (JsonMappingException) FalhasDeLeituraDeTeste
                .excecaoDoExemplo("{\"numero\":\"abc\"}");

        // Agir
        var nome = NomeDoCampoDoJackson.de(excecao);

        // Verificar
        assertThat(nome).isEqualTo("numero");
    }

    @Test
    void deve_dar_o_caminho_com_ponto_quando_o_campo_esta_dentro_de_outro() {
        // Preparar
        var excecao = FalhasDeLeituraDeTeste.excecaoDoAninhado("{\"exemplo\":{\"saldo\":1}}");

        // Agir
        var nome = NomeDoCampoDoJackson.de(excecao);

        // Verificar
        assertThat(nome).isEqualTo("exemplo.saldo");
    }

    @Test
    void deve_dizer_corpo_quando_o_erro_nao_tem_caminho() {
        // Preparar
        var semCaminho = JsonMappingException.from((JsonParser) null, "sem caminho");

        // Agir
        var nome = NomeDoCampoDoJackson.de(semCaminho);

        // Verificar
        assertThat(nome).isEqualTo(NomeDoCampoDoJackson.CORPO).isEqualTo("corpo");
    }

    @Test
    void deve_nunca_levar_o_nome_de_classe_quando_traduzir_o_campo() {
        // Preparar
        var excecao = (JsonMappingException) FalhasDeLeituraDeTeste
                .excecaoDoExemplo("{\"numero\":\"abc\"}");

        // Agir
        var nome = NomeDoCampoDoJackson.de(excecao);

        // Verificar
        assertThat(nome).doesNotContain("Exemplo").doesNotContain("br.com");
    }
}
