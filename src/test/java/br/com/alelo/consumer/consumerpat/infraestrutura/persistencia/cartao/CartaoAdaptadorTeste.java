package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

import br.com.alelo.consumer.consumerpat.api.configuracao.ConfiguracaoDeAuditoria;
import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroCartaoNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DataJpaTest
@Import({ ConfiguracaoDeAuditoria.class, ConfiguracaoDeRelogioDeTeste.class })
class CartaoAdaptadorTeste {

    private static final NumeroDeCartao NUMERO = new NumeroDeCartao(FabricaDeCartoes.NUMERO_PADRAO);

    @Autowired
    private CartaoRepositorioJpa repositorio;

    @Autowired
    private TestEntityManager gerenciador;

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    @Test
    void deve_devolver_o_cartao_quando_existe_cartao_com_o_numero_e_o_tipo() {
        // Preparar
        gerenciador.persistAndFlush(CartoesDeTeste.linha(TipoCartao.ALIMENTACAO, "100.00"));
        var adaptador = new CartaoAdaptador(repositorio);

        // Agir
        var resultado = adaptador.buscarPorNumeroETipoParaAtualizacao(NUMERO,
                TipoCartao.ALIMENTACAO);

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor())
                .isEqualTo(new Cartao(NUMERO, TipoCartao.ALIMENTACAO, dinheiro("100.00")));
    }

    @Test
    void deve_ser_cartao_nao_encontrado_quando_o_cartao_nao_existe() {
        // Preparar
        var adaptador = new CartaoAdaptador(repositorio);

        // Agir
        var resultado = adaptador.buscarPorNumeroETipoParaAtualizacao(NUMERO,
                TipoCartao.ALIMENTACAO);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro())
                .isEqualTo(new ErroCartaoNaoEncontrado(NUMERO, TipoCartao.ALIMENTACAO));
    }

    @Test
    void deve_ser_cartao_nao_encontrado_quando_o_numero_existe_com_outro_tipo() {
        // Preparar
        gerenciador.persistAndFlush(CartoesDeTeste.linha(TipoCartao.FARMACIA, "100.00"));
        var adaptador = new CartaoAdaptador(repositorio);

        // Agir
        var resultado = adaptador.buscarPorNumeroETipoParaAtualizacao(NUMERO,
                TipoCartao.ALIMENTACAO);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroCartaoNaoEncontrado.class);
    }

    @Test
    void deve_gravar_o_novo_saldo_quando_salvar_um_cartao_que_existe() {
        // Preparar
        var linha = gerenciador
                .persistAndFlush(CartoesDeTeste.linha(TipoCartao.COMBUSTIVEL, "100.00"));
        var adaptador = new CartaoAdaptador(repositorio);
        var novo = new Cartao(NUMERO, TipoCartao.COMBUSTIVEL, dinheiro("65.00"));

        // Agir
        var gravado = adaptador.salvar(novo);
        gerenciador.flush();
        gerenciador.clear();

        // Verificar
        assertThat(gravado).isEqualTo(novo);
        assertThat(gerenciador.find(CartaoEntidade.class, linha.getId()).getSaldo())
                .isEqualTo(new BigDecimal("65.000000"));
    }

    @Test
    void deve_guardar_a_fracao_de_centavo_quando_salvar_um_saldo_com_tres_casas() {
        // Preparar
        var linha = gerenciador
                .persistAndFlush(CartoesDeTeste.linha(TipoCartao.ALIMENTACAO, "100.00"));
        var adaptador = new CartaoAdaptador(repositorio);

        // Agir
        adaptador.salvar(new Cartao(NUMERO, TipoCartao.ALIMENTACAO, dinheiro("70.003")));
        gerenciador.flush();
        gerenciador.clear();

        // Verificar
        assertThat(gerenciador.find(CartaoEntidade.class, linha.getId()).getSaldo())
                .isEqualTo(new BigDecimal("70.003000"));
    }

    @Test
    void deve_lancar_sem_mostrar_o_numero_quando_salvar_um_cartao_que_nao_existe() {
        // Preparar
        var adaptador = new CartaoAdaptador(repositorio);
        var cartao = FabricaDeCartoes.doTipo(TipoCartao.FARMACIA).construir();

        // Agir e Verificar
        assertThatThrownBy(() -> adaptador.salvar(cartao)).isInstanceOf(IllegalStateException.class)
                .hasMessageNotContaining(FabricaDeCartoes.NUMERO_PADRAO);
    }

    @Test
    void deve_incrementar_a_versao_quando_atualizar_o_saldo() {
        // Preparar
        var linha = gerenciador
                .persistAndFlush(CartoesDeTeste.linha(TipoCartao.ALIMENTACAO, "100.00"));
        var versaoInicial = linha.getVersao();
        var adaptador = new CartaoAdaptador(repositorio);

        // Agir
        adaptador.salvar(new Cartao(NUMERO, TipoCartao.ALIMENTACAO, dinheiro("90.00")));
        gerenciador.flush();
        gerenciador.clear();

        // Verificar
        assertThat(gerenciador.find(CartaoEntidade.class, linha.getId()).getVersao())
                .isEqualTo(versaoInicial + 1);
    }
}
