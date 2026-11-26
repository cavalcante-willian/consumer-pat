package br.com.alelo.consumer.consumerpat.medicao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.medicao.ServidorFalso.Requisicao;
import br.com.alelo.consumer.consumerpat.medicao.ServidorFalso.Resposta;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OperacoesDoLegadoTeste {

    private static final Resposta VAZIA = Resposta.de(200, "");

    @Test
    void deve_postar_o_consumidor_da_base_com_os_tres_cartoes_quando_cadastrar() throws Exception {
        try (var servidor = new ServidorFalso(r -> Resposta.de(404, ""))) {
            // Agir
            var resposta = new OperacoesDoLegado(new ClienteHttp(servidor.url())).cadastrar(7);

            // Verificar: o legado grava e responde 404, e isso não é erro do driver
            assertThat(resposta.status()).isEqualTo(404);
            var enviada = servidor.recebidas().get(0);
            assertThat(enviada.metodo()).isEqualTo("POST");
            assertThat(enviada.caminho()).isEqualTo("/consumer/createConsumer");
            assertThat(JsonPath.<Integer>read(enviada.corpo(), "$.documentNumber"))
                    .isEqualTo(1_000_007);
            assertThat(JsonPath.<Integer>read(enviada.corpo(), "$.foodCardNumber"))
                    .isEqualTo(100_000_021);
            assertThat(JsonPath.<Integer>read(enviada.corpo(), "$.fuelCardNumber"))
                    .isEqualTo(100_000_023);
            assertThat(JsonPath.<Integer>read(enviada.corpo(), "$.drugstoreNumber"))
                    .isEqualTo(100_000_022);
        }
    }

    @Test
    void deve_pedir_a_tabela_inteira_numa_so_listagem_quando_listar() throws Exception {
        try (var servidor = new ServidorFalso(r -> Resposta.de(200, "[]"))) {
            // Agir
            var pedidos = new OperacoesDoLegado(new ClienteHttp(servidor.url())).listagens(100);
            pedidos.get(0).chamada().call();

            // Verificar: só há uma listagem, e ela não pagina
            assertThat(pedidos).extracting(Pedido::chave)
                    .containsExactly("listagem-primeira-pagina");
            assertThat(servidor.recebidas())
                    .containsExactly(new Requisicao("GET", "/consumer/consumerList", ""));
        }
    }

    @Test
    void deve_nao_ter_consulta_por_id_quando_pedir_as_consultas() throws Exception {
        try (var servidor = new ServidorFalso(r -> VAZIA)) {
            // Agir e Verificar: o legado não tem a operação, e isso é dado, e não falha
            assertThat(new OperacoesDoLegado(new ClienteHttp(servidor.url())).consultas(100))
                    .isEmpty();
        }
    }

    @Test
    void deve_creditar_pelo_numero_do_cartao_quando_pedir_os_creditos() throws Exception {
        try (var servidor = new ServidorFalso(r -> VAZIA)) {
            // Agir: o nono pedido é o de combustível do fim da base
            var pedidos = new OperacoesDoLegado(new ClienteHttp(servidor.url())).creditos(100);
            pedidos.get(0).chamada().call();
            pedidos.get(8).chamada().call();

            // Verificar
            assertThat(pedidos).hasSize(9);
            assertThat(pedidos.get(0).chave()).isEqualTo("credito-alimentacao-inicio");
            assertThat(pedidos.get(8).chave()).isEqualTo("credito-combustivel-fim");
            assertThat(servidor.recebidas()).extracting(Requisicao::caminho).containsExactly(
                    "/consumer/setcardbalance?cardNumber=100000000&value=10.00",
                    "/consumer/setcardbalance?cardNumber=100000299&value=10.00");
        }
    }

    @Test
    void deve_levar_o_codigo_do_estabelecimento_quando_comprar() throws Exception {
        try (var servidor = new ServidorFalso(r -> VAZIA)) {
            // Agir
            new OperacoesDoLegado(new ClienteHttp(servidor.url())).comprar(TipoCartao.COMBUSTIVEL,
                    5, "1.00");

            // Verificar
            assertThat(servidor.recebidas().get(0).caminho())
                    .isEqualTo("/consumer/buy?establishmentType=3&establishmentName=Estabelecimento"
                            + "&cardNumber=100000017&productDescription=Produto&value=1.00");
        }
    }

    @Test
    void deve_preparar_comprar_e_ler_o_saldo_da_farmacia_pela_listagem_quando_disputar()
            throws Exception {
        var listagem = Resposta.de(200,
                "[{\"drugstoreNumber\":900000001,\"drugstoreCardBalance\":990.0},"
                        + "{\"drugstoreNumber\":900000002,\"drugstoreCardBalance\":5.0}]");
        try (var servidor = new ServidorFalso(
                r -> Map.of("/consumer/consumerList", listagem).getOrDefault(r.caminho(), VAZIA))) {
            var operacoes = new OperacoesDoLegado(new ClienteHttp(servidor.url()));

            // Agir
            var cartao = operacoes.prepararFarmaciaComSaldo("1000.00");
            var status = operacoes.comprarNaFarmacia(cartao, "10.00");
            var saldo = operacoes.saldoDe(cartao);

            // Verificar
            assertThat(cartao.numero()).isEqualTo("900000001");
            assertThat(status).isEqualTo(200);
            assertThat(saldo).isEqualByComparingTo(new BigDecimal("990.0"));
            assertThat(JsonPath.<Double>read(servidor.recebidas().get(0).corpo(),
                    "$.drugstoreCardBalance")).isEqualTo(1000.0);
            assertThat(servidor.recebidas().get(1).caminho()).contains("establishmentType=2")
                    .contains("cardNumber=900000001");
        }
    }

    @Test
    void deve_dar_a_cada_cartao_da_disputa_um_numero_proprio_quando_preparar_varios()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> VAZIA)) {
            var operacoes = new OperacoesDoLegado(new ClienteHttp(servidor.url()));

            // Agir
            var primeiro = operacoes.prepararFarmaciaComSaldo("1000.00");
            var segundo = operacoes.prepararFarmaciaComSaldo("1000.00");

            // Verificar
            assertThat(primeiro.numero()).isNotEqualTo(segundo.numero());
        }
    }
}
