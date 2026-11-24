package br.com.alelo.consumer.consumerpat.api.comum.campos;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrosDeCampoDeValidacaoTeste {

    private static FieldError erro(String campo, Object valorRejeitado, String mensagem) {
        return new FieldError("mensagem", campo, valorRejeitado, false, null, null, mensagem);
    }

    @Test
    void deve_reunir_todos_os_campos_de_uma_vez_quando_ha_varias_violacoes() {
        // Preparar
        var resultado = new BeanPropertyBindingResult(new Object(), "mensagem");
        resultado.addError(erro("valor", null, "deve ser maior que zero"));
        resultado.addError(erro("numeroCartao", "", "não deve estar em branco"));

        // Agir
        var erros = ErrosDeCampoDeValidacao.de(resultado);

        // Verificar
        assertThat(erros).containsOnlyKeys("numeroCartao", "valor");
    }

    @Test
    void deve_ordenar_as_chaves_quando_as_violacoes_chegam_em_outra_ordem() {
        // Preparar
        var resultado = new BeanPropertyBindingResult(new Object(), "mensagem");
        resultado.addError(erro("valor", null, "x"));
        resultado.addError(erro("descricaoProduto", "", "x"));
        resultado.addError(erro("numeroCartao", "", "x"));

        // Agir
        var erros = ErrosDeCampoDeValidacao.de(resultado);

        // Verificar
        assertThat(List.copyOf(erros.keySet())).containsExactly("descricaoProduto", "numeroCartao",
                "valor");
    }

    @Test
    void deve_ordenar_as_mensagens_do_mesmo_campo_quando_ha_mais_de_uma() {
        // Preparar
        var resultado = new BeanPropertyBindingResult(new Object(), "mensagem");
        resultado.addError(erro("numeroCartao", "", "é obrigatório"));
        resultado.addError(erro("numeroCartao", "", "não deve estar em branco"));
        resultado.addError(erro("numeroCartao", "", "deve ter no máximo 25 caracteres"));

        // Agir
        var erros = ErrosDeCampoDeValidacao.de(resultado);

        // Verificar
        assertThat(erros.get("numeroCartao")).containsExactly("deve ter no máximo 25 caracteres",
                "não deve estar em branco", "é obrigatório");
    }

    @Test
    void deve_nunca_levar_o_valor_rejeitado_quando_traduzir_a_violacao() {
        // Preparar
        var resultado = new BeanPropertyBindingResult(new Object(), "mensagem");
        resultado
                .addError(erro("numeroCartao", "5555000011112222", "deve ter no máximo 4 dígitos"));

        // Agir
        var erros = ErrosDeCampoDeValidacao.de(resultado);

        // Verificar
        assertThat(erros.toString()).doesNotContain("5555000011112222");
    }

    @Test
    void deve_devolver_vazio_quando_nao_ha_violacao() {
        // Preparar
        var resultado = new BeanPropertyBindingResult(new Object(), "mensagem");

        // Verificar
        assertThat(ErrosDeCampoDeValidacao.de(resultado)).isEmpty();
    }
}
