package br.com.alelo.consumer.consumerpat.dominio.log;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

import static br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog.campo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaidaDeLogPortaTeste {

    @Test
    void deve_ter_uma_unica_operacao_abstrata_quando_compor_os_seis_papeis() {
        // Agir
        var abstratas = Arrays.stream(SaidaDeLogPorta.class.getMethods())
                .filter(metodo -> Modifier.isAbstract(metodo.getModifiers())).map(Method::getName)
                .toList();

        // Verificar: quem implementa a porta escreve só o registrar
        assertThat(abstratas).containsExactly("registrar");
    }

    @Test
    void deve_compor_os_seis_papeis_sem_declarar_operacao_propria() {
        // Verificar
        assertThat(SaidaDeLogPorta.class.getDeclaredMethods()).isEmpty();
        assertThat(SaidaDeLogPorta.class.getInterfaces()).containsExactlyInAnyOrder(
                DepuracaoDeLog.class, InformacaoDeLog.class, AvisoDeLog.class, ErroDeLog.class,
                ErroComCausaDeLog.class, CriticoDeLog.class);
    }

    @Test
    void deve_registrar_no_nivel_de_cada_papel_quando_chamar_o_atalho() {
        // Preparar
        var saida = new SaidaDeLogFake();

        // Agir
        saida.depuracao("a.feita");
        saida.informacao("b.feita");
        saida.aviso("c.feita");
        saida.erro("d.feita");
        saida.critico("e.feita");

        // Verificar
        assertThat(saida.eventos()).extracting(SaidaDeLogFake.Evento::nivel).containsExactly(
                NivelDeLog.DEPURACAO, NivelDeLog.INFORMACAO, NivelDeLog.AVISO, NivelDeLog.ERRO,
                NivelDeLog.CRITICO);
        assertThat(saida.eventos()).extracting(SaidaDeLogFake.Evento::evento)
                .containsExactly("a.feita", "b.feita", "c.feita", "d.feita", "e.feita");
    }

    @Test
    void deve_levar_os_campos_na_ordem_quando_registrar_com_campos() {
        // Preparar
        var saida = new SaidaDeLogFake();

        // Agir
        saida.informacao("compra.registrada", campo("consumidorId", 42),
                campo("tipoCartao", "ALIMENTACAO"));

        // Verificar
        var evento = saida.eventos().get(0);
        assertThat(evento.valorDe("consumidorId")).contains(42);
        assertThat(evento.valorDe("tipoCartao")).contains("ALIMENTACAO");
        assertThat(evento.valorDe("inexistente")).isEmpty();
    }

    @Test
    void deve_levar_a_causa_so_no_erro_com_causa() {
        // Preparar
        var saida = new SaidaDeLogFake();
        var causa = new IllegalStateException("falhou");

        // Agir
        saida.erro("a.falhou");
        saida.erroComCausa("b.falhou", causa);

        // Verificar
        assertThat(saida.eventos().get(0).causa()).isEmpty();
        assertThat(saida.eventos().get(1).causa()).containsSame(causa);
        assertThat(saida.eventos().get(1).nivel()).isEqualTo(NivelDeLog.ERRO);
    }

    @Test
    void deve_aceitar_o_registro_direto_sem_campos_quando_chamar_o_registrar() {
        // Preparar
        var saida = new SaidaDeLogFake();

        // Agir
        saida.registrar(NivelDeLog.AVISO, "x.feito", Optional.empty());

        // Verificar
        assertThat(saida.eventos().get(0).campos()).isEmpty();
    }

    @Test
    void deve_recusar_o_campo_sem_nome_quando_criar() {
        // Agir e Verificar
        assertThatThrownBy(() -> new CampoDeLog(null, "x"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deve_aceitar_o_valor_nulo_quando_criar_o_campo() {
        // Agir
        var campo = campo("nome", null);

        // Verificar
        assertThat(campo.valor()).isNull();
    }
}
