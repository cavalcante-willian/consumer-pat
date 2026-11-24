package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;

import br.com.alelo.consumer.consumerpat.api.configuracao.ConfiguracaoDeAuditoria;
import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.CartoesDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.NovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Contato;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Telefone;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroDocumentoJaCadastrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroNumeroDeCartaoJaCadastrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroVersaoDivergente;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroConsumidorNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.ConfiguracaoDeRelogioDeTeste;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao.CartaoRepositorioJpa;
// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidorRepositorioJpa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Prova o adaptador do consumidor contra o H2 real: o que ele devolve e o que fica gravado.
 */
@DataJpaTest
@Import({ ConfiguracaoDeAuditoria.class, ConfiguracaoDeRelogioDeTeste.class })
class ConsumidorAdaptadorTeste {

    private static final String OUTRO_DOCUMENTO = "98765432100";

    @Autowired
    private ConsumidorRepositorioJpa consumidores;

    @Autowired
    private CartaoRepositorioJpa cartoes;

    @Autowired
    private TestEntityManager gerenciador;

    private ConsumidorAdaptador adaptador() {
        return new ConsumidorAdaptador(consumidores, cartoes);
    }

    private static Cartao cartao(TipoCartao tipo, String numero) {
        return FabricaDeCartoes.doTipo(tipo).comNumero(numero).comSaldo("0").construir();
    }

    private static NovoConsumidor novo(String documento, Cartao... cartoes) {
        return new NovoConsumidor(FabricaDeConsumidores.dadosComDocumento(documento),
                new CartoesDoConsumidor(List.of(cartoes)));
    }

    private Consumidor gravado(NovoConsumidor novo) {
        var sucesso = assertInstanceOf(Sucesso.class, adaptador().criar(novo));
        gerenciador.flush();
        gerenciador.clear();
        return (Consumidor) sucesso.valor();
    }

    @Test
    void deve_dar_id_e_versao_zero_quando_criar_um_consumidor() {
        // Agir
        var criado = gravado(novo(FabricaDeConsumidores.DOCUMENTO_PADRAO));

        // Verificar
        assertThat(criado.id().valor()).isPositive();
        assertThat(criado.versao()).isZero();
        assertThat(criado.dados()).isEqualTo(
                FabricaDeConsumidores.dadosComDocumento(FabricaDeConsumidores.DOCUMENTO_PADRAO));
    }

    @Test
    void deve_gravar_os_cartoes_com_dono_e_saldo_zero_quando_criar_com_cartoes() {
        // Agir
        var criado = gravado(novo(FabricaDeConsumidores.DOCUMENTO_PADRAO,
                cartao(TipoCartao.ALIMENTACAO, "5555000011112222"),
                cartao(TipoCartao.FARMACIA, "5555000011113333")));

        // Verificar
        assertThat(criado.cartoes().cartoes()).hasSize(2)
                .allMatch(cartao -> cartao.saldo().equals(Dinheiro.ZERO));
        assertThat(cartoes.buscarDoConsumidor(criado.id().valor())).hasSize(2);
    }

    @Test
    void deve_criar_o_consumidor_sem_cartoes_quando_o_pedido_nao_traz_nenhum() {
        // Agir
        var criado = gravado(novo(FabricaDeConsumidores.DOCUMENTO_PADRAO));

        // Verificar
        assertThat(criado.cartoes().cartoes()).isEmpty();
        assertThat(cartoes.count()).isZero();
    }

    @Test
    void deve_guardar_os_telefones_ausentes_como_nulos_e_devolve_los_ausentes() {
        // Agir
        var criado = gravado(novo(FabricaDeConsumidores.DOCUMENTO_PADRAO));
        var linha = consumidores.findById(criado.id().valor()).orElseThrow();

        // Verificar
        assertThat(linha.getTelefoneResidencial()).isNull();
        assertThat(linha.getTelefone()).isNull();
        assertThat(criado.dados().contato().telefoneResidencial()).isEmpty();
    }

    @Test
    void deve_guardar_os_telefones_informados_quando_criar() {
        // Preparar
        var base = FabricaDeConsumidores.dadosComDocumento(FabricaDeConsumidores.DOCUMENTO_PADRAO);
        var contato = new Contato(base.contato().celular(), Optional.of(new Telefone("1133334444")),
                Optional.of(new Telefone("1144445555")), base.contato().email());
        var dados = new br.com.alelo.consumer.consumerpat.dominio.consumidor.DadosDoConsumidor(
                base.nome(), base.documento(), base.dataNascimento(), contato, base.endereco());

        // Agir
        var criado = gravado(new NovoConsumidor(dados, new CartoesDoConsumidor(List.of())));

        // Verificar
        assertThat(criado.dados().contato().telefoneResidencial())
                .contains(new Telefone("1133334444"));
        assertThat(criado.dados().contato().telefone()).contains(new Telefone("1144445555"));
    }

    @Test
    void deve_ser_falha_de_documento_repetido_e_nao_gravar_quando_o_documento_ja_existe() {
        // Preparar
        gravado(novo(FabricaDeConsumidores.DOCUMENTO_PADRAO));

        // Agir
        var resultado = adaptador().criar(novo(FabricaDeConsumidores.DOCUMENTO_PADRAO,
                cartao(TipoCartao.FARMACIA, "5555000011113333")));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroDocumentoJaCadastrado.class);
        assertThat(consumidores.count()).isEqualTo(1);
        assertThat(cartoes.count()).isZero();
    }

    @Test
    void deve_ser_falha_de_numero_repetido_e_nao_gravar_quando_o_cartao_ja_existe() {
        // Preparar
        gravado(novo(FabricaDeConsumidores.DOCUMENTO_PADRAO,
                cartao(TipoCartao.ALIMENTACAO, "5555000011112222")));

        // Agir
        var resultado = adaptador()
                .criar(novo(OUTRO_DOCUMENTO, cartao(TipoCartao.FARMACIA, "5555000011112222")));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isEqualTo(
                new ErroNumeroDeCartaoJaCadastrado(new NumeroDeCartao("5555000011112222")));
        assertThat(consumidores.count()).isEqualTo(1);
    }

    @Test
    void deve_devolver_o_consumidor_com_os_cartoes_ordenados_pelo_tipo_quando_buscar() {
        // Preparar
        var criado = gravado(novo(FabricaDeConsumidores.DOCUMENTO_PADRAO,
                cartao(TipoCartao.COMBUSTIVEL, "5555000011114444"),
                cartao(TipoCartao.ALIMENTACAO, "5555000011112222")));

        // Agir
        var resultado = adaptador().buscarPorId(criado.id());

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        var lido = (Consumidor) sucesso.valor();
        assertThat(lido).isEqualTo(criado);
        assertThat(lido.cartoes().cartoes()).extracting(Cartao::tipo)
                .containsExactly(TipoCartao.ALIMENTACAO, TipoCartao.COMBUSTIVEL);
    }

    @Test
    void deve_ser_falha_de_nao_encontrado_quando_o_id_nao_existe() {
        // Agir
        var resultado = adaptador().buscarPorId(new ConsumidorId(999));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isEqualTo(new ErroConsumidorNaoEncontrado(new ConsumidorId(999)));
    }

    @Test
    void deve_trocar_os_dados_e_avancar_a_versao_quando_atualizar_com_a_versao_atual() {
        // Preparar
        var criado = gravado(novo(FabricaDeConsumidores.DOCUMENTO_PADRAO));
        var novosDados = FabricaDeConsumidores.dadosComDocumento(OUTRO_DOCUMENTO);

        // Agir
        var resultado = adaptador().atualizar(criado.comDados(novosDados));

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        var atualizado = (Consumidor) sucesso.valor();
        assertThat(atualizado.dados()).isEqualTo(novosDados);
        assertThat(atualizado.versao()).isEqualTo(1);
    }

    @Test
    void deve_manter_os_cartoes_e_o_saldo_quando_atualizar_o_cadastro() {
        // Preparar
        var criado = gravado(novo(FabricaDeConsumidores.DOCUMENTO_PADRAO,
                cartao(TipoCartao.ALIMENTACAO, "5555000011112222")));
        var comSaldoVelho = new Consumidor(criado.id(),
                FabricaDeConsumidores.dadosComDocumento(OUTRO_DOCUMENTO),
                new CartoesDoConsumidor(List.of(FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO)
                        .comNumero("5555000011112222").comSaldo("999").construir())),
                criado.versao());

        // Agir
        var resultado = adaptador().atualizar(comSaldoVelho);

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        var atualizado = (Consumidor) sucesso.valor();
        assertThat(atualizado.cartoes()).isEqualTo(criado.cartoes());
        assertThat(atualizado.cartoes().cartoes().get(0).saldo()).isEqualTo(Dinheiro.ZERO);
    }

    @Test
    void deve_ser_falha_de_versao_divergente_e_nao_gravar_quando_a_versao_esta_velha() {
        // Preparar
        var criado = gravado(novo(FabricaDeConsumidores.DOCUMENTO_PADRAO));
        var velho = new Consumidor(criado.id(),
                FabricaDeConsumidores.dadosComDocumento(OUTRO_DOCUMENTO), criado.cartoes(), 7);

        // Agir
        var resultado = adaptador().atualizar(velho);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isEqualTo(new ErroVersaoDivergente(criado.id(), 7));
        gerenciador.clear();
        assertThat(consumidores.findById(criado.id().valor()).orElseThrow().getDocumento())
                .isEqualTo(FabricaDeConsumidores.DOCUMENTO_PADRAO);
    }

    @Test
    void deve_ser_falha_de_nao_encontrado_quando_atualizar_um_consumidor_ausente() {
        // Agir
        var resultado = adaptador().atualizar(FabricaDeConsumidores.consumidor(999, 0));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroConsumidorNaoEncontrado.class);
    }

    @Test
    void deve_ser_falha_de_documento_repetido_quando_atualizar_para_o_documento_de_outro() {
        // Preparar
        gravado(novo(OUTRO_DOCUMENTO));
        var criado = gravado(novo(FabricaDeConsumidores.DOCUMENTO_PADRAO));

        // Agir
        var resultado = adaptador().atualizar(
                criado.comDados(FabricaDeConsumidores.dadosComDocumento(OUTRO_DOCUMENTO)));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroDocumentoJaCadastrado.class);
    }

    @Test
    void deve_aceitar_o_proprio_documento_quando_atualizar_sem_mudar_o_documento() {
        // Preparar
        var criado = gravado(novo(FabricaDeConsumidores.DOCUMENTO_PADRAO));

        // Agir
        var resultado = adaptador().atualizar(criado);

        // Verificar
        assertInstanceOf(Sucesso.class, resultado);
    }
}
