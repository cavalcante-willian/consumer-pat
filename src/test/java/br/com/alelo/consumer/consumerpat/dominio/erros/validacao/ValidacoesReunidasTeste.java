package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ValidacoesReunidasTeste {

    private static Resultado<String, ErroDominio> ok() {
        return new Sucesso<>("valor");
    }

    private static Resultado<String, ErroDominio> falha(ErroDominio erro) {
        return new Falha<>(erro);
    }

    @Test
    void deve_devolver_vazio_quando_nenhum_campo_falhou() {
        // Agir
        var erro = ValidacoesReunidas
                .reunir(List.of(new CampoValidado("a", ok()), new CampoValidado("b", ok())));

        // Verificar
        assertThat(erro).isEmpty();
    }

    @Test
    void deve_devolver_vazio_quando_nao_ha_campo() {
        // Verificar
        assertThat(ValidacoesReunidas.reunir(List.of())).isEmpty();
    }

    @Test
    void deve_reunir_todos_os_campos_que_falharam_quando_ha_mais_de_um() {
        // Agir
        var erro = ValidacoesReunidas
                .reunir(List.of(new CampoValidado("documento", falha(new ErroDocumentoInvalido())),
                        new CampoValidado("nome", ok()),
                        new CampoValidado("cep", falha(new ErroCepInvalido()))));

        // Verificar
        var reunido = erro.orElseThrow();
        assertThat(reunido).isInstanceOf(ErroEntradaInvalida.class);
        assertThat(reunido.camposComErro()).containsOnlyKeys("cep", "documento");
    }

    @Test
    void deve_ordenar_os_campos_pelo_nome_quando_eles_chegam_em_outra_ordem() {
        // Agir
        var erro = ValidacoesReunidas
                .reunir(List.of(new CampoValidado("zeta", falha(new ErroCepInvalido())),
                        new CampoValidado("alfa", falha(new ErroCepInvalido()))))
                .orElseThrow();

        // Verificar
        assertThat(List.copyOf(erro.camposComErro().keySet())).containsExactly("alfa", "zeta");
    }

    @Test
    void deve_usar_o_nome_dado_pelo_chamador_e_nao_o_do_erro_quando_reunir() {
        // Agir
        var erro = ValidacoesReunidas
                .reunir(List.of(
                        new CampoValidado("numerosCartoes", falha(new ErroTipoDeCartaoInvalido()))))
                .orElseThrow();

        // Verificar: o erro de domínio aponta "tipo", e a entrada chama o campo de outro nome
        assertThat(erro.camposComErro()).containsOnlyKeys("numerosCartoes")
                .doesNotContainKey("tipo");
    }

    @Test
    void deve_juntar_as_mensagens_do_mesmo_campo_quando_dois_resultados_apontam_o_mesmo_nome() {
        // Agir
        var erro = ValidacoesReunidas.reunir(List.of(
                new CampoValidado("numerosCartoes", falha(new ErroTipoDeCartaoInvalido())),
                new CampoValidado("numerosCartoes", falha(new ErroNumeroDeCartaoInvalido()))))
                .orElseThrow();

        // Verificar
        assertThat(erro.camposComErro().get("numerosCartoes")).hasSize(2);
    }

    @Test
    void deve_usar_a_mensagem_do_erro_quando_ele_nao_aponta_campo() {
        // Preparar
        ErroDominio semCampo = new ErroSemCampo();

        // Agir
        var erro = ValidacoesReunidas.reunir(List.of(new CampoValidado("x", falha(semCampo))))
                .orElseThrow();

        // Verificar
        assertThat(erro.camposComErro()).isEqualTo(Map.of("x", List.of("sem campo")));
    }

    /** Um erro que não aponta campo, para provar que a mensagem dele não se perde. */
    record ErroSemCampo() implements ErroDominio {

        @Override
        public br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro categoria() {
            return br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro.VALIDACAO;
        }

        @Override
        public String codigo() {
            return "X";
        }

        @Override
        public String mensagem() {
            return "sem campo";
        }
    }
}
