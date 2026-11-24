package br.com.alelo.consumer.consumerpat.api.comum.campos;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ErrosDeCampoDoCorpoTeste {

    /** Uma causa que o registro não conhece, mas que descende de uma que ele conhece. */
    static class CausaDesconhecida extends MismatchedInputException {

        private static final long serialVersionUID = 1L;

        CausaDesconhecida() {
            super(null, "causa desconhecida");
        }
    }

    @Test
    void deve_dizer_que_o_campo_nao_e_aceito_quando_o_corpo_tem_campo_desconhecido() {
        // Agir
        var erros = ErrosDeCampoDoCorpo
                .de(FalhasDeLeituraDeTeste.leituraDoExemplo("{\"saldo\":9999}"));

        // Verificar
        assertThat(erros).isEqualTo(Map.of("saldo", List.of("não é aceito nesta operação")));
    }

    @Test
    void deve_dar_o_caminho_com_ponto_quando_o_campo_desconhecido_esta_dentro_de_outro() {
        // Preparar
        var json = "{\"exemplo\":{\"saldo\":1}}";
        var leitura = new HttpMessageNotReadableException("texto do framework",
                FalhasDeLeituraDeTeste.excecaoDoAninhado(json),
                new MockHttpInputMessage(new byte[0]));

        // Agir
        var erros = ErrosDeCampoDoCorpo.de(leitura);

        // Verificar
        assertThat(erros).containsOnlyKeys("exemplo.saldo");
    }

    @Test
    void deve_dizer_que_o_formato_nao_e_o_esperado_quando_um_campo_tem_o_tipo_errado() {
        // Agir
        var erros = ErrosDeCampoDoCorpo
                .de(FalhasDeLeituraDeTeste.leituraDoExemplo("{\"numero\":\"abc\"}"));

        // Verificar
        assertThat(erros).isEqualTo(Map.of("numero", List.of("não tem o formato esperado")));
    }

    @Test
    void deve_dizer_que_o_formato_nao_e_o_esperado_quando_o_campo_tem_a_estrutura_errada() {
        // Agir
        var erros = ErrosDeCampoDoCorpo
                .de(FalhasDeLeituraDeTeste.leituraDoExemplo("{\"numero\":[1]}"));

        // Verificar
        assertThat(erros).isEqualTo(Map.of("numero", List.of("não tem o formato esperado")));
    }

    @Test
    void deve_dizer_que_o_json_e_invalido_quando_o_corpo_esta_malformado() {
        // Agir
        var erros = ErrosDeCampoDoCorpo.de(FalhasDeLeituraDeTeste.leituraDoExemplo("{\"numero\":"));

        // Verificar
        assertThat(erros).isEqualTo(Map.of("corpo", List.of("não é um JSON válido")));
    }

    @Test
    void deve_dizer_que_o_corpo_e_obrigatorio_quando_o_corpo_nem_veio() {
        // Preparar
        var leitura = new HttpMessageNotReadableException(
                "Required request body is missing: public void metodo()",
                new MockHttpInputMessage(new byte[0]));

        // Agir
        var erros = ErrosDeCampoDoCorpo.de(leitura);

        // Verificar
        assertThat(erros).isEqualTo(Map.of("corpo", List.of("é obrigatório")));
    }

    @Test
    void deve_dizer_que_o_corpo_nao_pode_ser_lido_quando_nenhuma_causa_e_reconhecida() {
        // Preparar
        var leitura = new HttpMessageNotReadableException("alguma outra coisa",
                new MockHttpInputMessage(new byte[0]));

        // Agir
        var erros = ErrosDeCampoDoCorpo.de(leitura);

        // Verificar
        assertThat(erros).isEqualTo(Map.of("corpo", List.of("não pôde ser lido")));
    }

    @Test
    void deve_traduzir_pela_classe_mais_geral_conhecida_quando_a_causa_e_de_uma_subclasse() {
        // Preparar
        var leitura = new HttpMessageNotReadableException("texto do framework",
                new CausaDesconhecida(), new MockHttpInputMessage(new byte[0]));

        // Agir
        var erros = ErrosDeCampoDoCorpo.de(leitura);

        // Verificar
        assertThat(erros).isEqualTo(Map.of("corpo", List.of("não tem o formato esperado")));
    }

    @Test
    void deve_preferir_a_causa_mais_especifica_quando_ha_uma_mais_geral_na_hierarquia() {
        // Agir
        var erros = ErrosDeCampoDoCorpo
                .de(FalhasDeLeituraDeTeste.leituraDoExemplo("{\"saldo\":1}"));

        // Verificar
        assertThat(erros.get("saldo")).containsExactly("não é aceito nesta operação")
                .doesNotContain("não tem o formato esperado");
    }

    @Test
    void deve_nunca_levar_nome_de_classe_nem_texto_do_jackson_quando_traduzir() {
        // Agir
        var textos = List.of(
                ErrosDeCampoDoCorpo.de(FalhasDeLeituraDeTeste.leituraDoExemplo("{\"saldo\":1}")),
                ErrosDeCampoDoCorpo
                        .de(FalhasDeLeituraDeTeste.leituraDoExemplo("{\"numero\":\"abc\"}")),
                ErrosDeCampoDoCorpo.de(FalhasDeLeituraDeTeste.leituraDoExemplo("{\"numero\":")))
                .toString();

        // Verificar
        assertThat(textos).doesNotContain("br.com").doesNotContain("Exemplo")
                .doesNotContain("Unrecognized").doesNotContain("Cannot deserialize");
    }
}
