package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

import br.com.alelo.consumer.consumerpat.api.configuracao.ConfiguracaoDeAuditoria;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import br.com.alelo.consumer.consumerpat.fabricas.RelogioAjustavel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import javax.persistence.PersistenceException;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({ ConfiguracaoDeAuditoria.class, ConfiguracaoDeRelogioDeTeste.class })
class ExtratoAdaptadorTeste {

    private static final Instant COMPRA = Instant.parse("2026-11-24T11:30:00Z");

    private static final NumeroDeCartao NUMERO = new NumeroDeCartao(FabricaDeCartoes.NUMERO_PADRAO);

    @Autowired
    private CartaoRepositorioJpa cartoes;

    @Autowired
    private ExtratoRepositorioJpa extratos;

    @Autowired
    private TestEntityManager gerenciador;

    @Autowired
    private RelogioAjustavel relogio;

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    private static Extrato extrato() {
        return new Extrato("Mercado", "Arroz", COMPRA, NUMERO, TipoCartao.ALIMENTACAO,
                dinheiro("33.33"), dinheiro("29.997"));
    }

    @Test
    void deve_gravar_os_dois_valores_e_o_instante_quando_registrar_o_extrato() {
        // Preparar
        var cartao = gerenciador
                .persistAndFlush(CartoesDeTeste.linha(TipoCartao.ALIMENTACAO, "100.00"));
        var adaptador = new ExtratoAdaptador(cartoes, extratos);

        // Agir
        adaptador.registrar(extrato());
        gerenciador.flush();
        gerenciador.clear();

        // Verificar
        var gravados = extratos.findAll();
        assertThat(gravados).hasSize(1);
        var gravado = gravados.get(0);
        assertThat(gravado.getNomeEstabelecimento()).isEqualTo("Mercado");
        assertThat(gravado.getDescricaoProduto()).isEqualTo("Arroz");
        assertThat(gravado.getValorInformado()).isEqualTo(new BigDecimal("33.330000"));
        assertThat(gravado.getValorCobrado()).isEqualTo(new BigDecimal("29.997000"));
        assertThat(gravado.getDataCompra()).isEqualTo(COMPRA);
        assertThat(gravado.getCartao().getId()).isEqualTo(cartao.getId());
    }

    @Test
    void deve_preencher_a_data_de_criacao_quando_registrar_o_extrato() {
        // Preparar
        gerenciador.persistAndFlush(CartoesDeTeste.linha(TipoCartao.ALIMENTACAO, "100.00"));
        var agora = relogio.instant();

        // Agir
        new ExtratoAdaptador(cartoes, extratos).registrar(extrato());
        gerenciador.flush();

        // Verificar
        assertThat(extratos.findAll().get(0).getDataCriacao()).isEqualTo(agora);
    }

    @Test
    void deve_guardar_um_extrato_por_registro_quando_registrar_duas_vezes() {
        // Preparar
        gerenciador.persistAndFlush(CartoesDeTeste.linha(TipoCartao.ALIMENTACAO, "100.00"));
        var adaptador = new ExtratoAdaptador(cartoes, extratos);

        // Agir
        adaptador.registrar(extrato());
        adaptador.registrar(extrato());
        gerenciador.flush();

        // Verificar
        assertThat(extratos.count()).isEqualTo(2);
    }

    @Test
    void deve_lancar_sem_mostrar_o_numero_quando_registrar_para_um_cartao_que_nao_existe() {
        // Preparar
        var adaptador = new ExtratoAdaptador(cartoes, extratos);

        // Agir e Verificar
        assertThatThrownBy(() -> adaptador.registrar(extrato()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageNotContaining(FabricaDeCartoes.NUMERO_PADRAO);
        assertThat(extratos.count()).isZero();
    }

    @Test
    void deve_recusar_o_extrato_quando_o_cartao_da_chave_estrangeira_nao_existe() {
        // Agir e Verificar
        assertThatThrownBy(() -> gerenciador.getEntityManager().createNativeQuery(
                "insert into extratos (cartao_id, nome_estabelecimento, descricao_produto, "
                        + "valor_informado, valor_cobrado, data_compra, data_criacao) "
                        + "values (999, 'a', 'b', 1, 1, current_timestamp, current_timestamp)")
                .executeUpdate()).isInstanceOf(PersistenceException.class)
                        .hasStackTraceContaining("FK_EXTRATOS_CARTOES");
    }

    @Test
    void deve_recusar_apagar_o_cartao_quando_ele_tem_extrato() {
        // Preparar
        var cartao = gerenciador
                .persistAndFlush(CartoesDeTeste.linha(TipoCartao.ALIMENTACAO, "100.00"));
        new ExtratoAdaptador(cartoes, extratos).registrar(extrato());
        gerenciador.flush();

        // Agir e Verificar
        assertThatThrownBy(() -> {
            cartoes.delete(cartao);
            cartoes.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
