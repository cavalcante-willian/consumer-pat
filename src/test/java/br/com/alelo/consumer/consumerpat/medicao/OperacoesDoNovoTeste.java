package br.com.alelo.consumer.consumerpat.medicao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.medicao.ServidorFalso.Requisicao;
import br.com.alelo.consumer.consumerpat.medicao.ServidorFalso.Resposta;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OperacoesDoNovoTeste {

    @Test
    void deve_numerar_o_documento_e_os_cartoes_de_forma_determinista_quando_numerar() {
        // Agir e Verificar: o consumidor 7 ocupa os números 21, 22 e 23 da faixa, na ordem do enum
        assertThat(OperacoesDoNovo.documento(7)).isEqualTo("80000000007");
        assertThat(OperacoesDoNovo.numeroDoCartao(7, TipoCartao.ALIMENTACAO))
                .isEqualTo("8000000000000021");
        assertThat(OperacoesDoNovo.numeroDoCartao(7, TipoCartao.FARMACIA))
                .isEqualTo("8000000000000022");
        assertThat(OperacoesDoNovo.numeroDoCartao(7, TipoCartao.COMBUSTIVEL))
                .isEqualTo("8000000000000023");
    }

    @Test
    void deve_postar_o_consumidor_da_base_com_os_tres_cartoes_quando_cadastrar() throws Exception {
        try (var servidor = new ServidorFalso(r -> Resposta.de(201, ""))) {
            // Agir
            new OperacoesDoNovo(new ClienteHttp(servidor.url())).cadastrar(7);

            // Verificar
            var enviada = servidor.recebidas().get(0);
            assertThat(enviada.metodo()).isEqualTo("POST");
            assertThat(enviada.caminho()).isEqualTo("/api/v1/consumidores");
            assertThat(JsonPath.<String>read(enviada.corpo(), "$.dados.documento"))
                    .isEqualTo("80000000007");
            assertThat(JsonPath.<String>read(enviada.corpo(), "$.numerosCartoes.FARMACIA"))
                    .isEqualTo("8000000000000022");
        }
    }

    @Test
    void deve_paginar_do_inicio_ao_fim_da_base_quando_listar() throws Exception {
        try (var servidor = new ServidorFalso(r -> Resposta.de(200, "{}"))) {
            // Agir: cem consumidores, de vinte em vinte, são cinco páginas
            var pedidos = new OperacoesDoNovo(new ClienteHttp(servidor.url())).listagens(100);
            for (var pedido : pedidos) {
                pedido.chamada().call();
            }

            // Verificar
            assertThat(pedidos).extracting(Pedido::chave).containsExactly(
                    "listagem-primeira-pagina", "listagem-pagina-do-meio", "listagem-ultima-pagina",
                    "listagem-primeira-pagina-de-100");
            assertThat(servidor.recebidas()).extracting(Requisicao::caminho).containsExactly(
                    "/api/v1/consumidores?pagina=1&tamanhoPagina=20",
                    "/api/v1/consumidores?pagina=2&tamanhoPagina=20",
                    "/api/v1/consumidores?pagina=5&tamanhoPagina=20",
                    "/api/v1/consumidores?pagina=1&tamanhoPagina=100");
        }
    }

    @Test
    void deve_nao_sair_da_primeira_pagina_quando_a_base_e_menor_que_uma_pagina() throws Exception {
        try (var servidor = new ServidorFalso(r -> Resposta.de(200, "{}"))) {
            // Agir
            var pedidos = new OperacoesDoNovo(new ClienteHttp(servidor.url())).listagens(3);
            pedidos.get(1).chamada().call();

            // Verificar: o meio de uma só página é a página um, e nunca a zero
            assertThat(servidor.recebidas().get(0).caminho())
                    .isEqualTo("/api/v1/consumidores?pagina=1&tamanhoPagina=20");
        }
    }

    @Test
    void deve_consultar_o_id_lido_da_listagem_quando_pedir_a_consulta() throws Exception {
        var umItem = Resposta.de(200, "{\"itens\":[{\"id\":42,\"nome\":\"Ana\"}]}");
        try (var servidor = new ServidorFalso(
                r -> Map.of("/api/v1/consumidores?pagina=2&tamanhoPagina=1", umItem)
                        .getOrDefault(r.caminho(), Resposta.de(200, "{}")))) {
            // Agir
            var pedidos = new OperacoesDoNovo(new ClienteHttp(servidor.url())).consultas(100);
            pedidos.get(0).chamada().call();

            // Verificar: nenhuma suposição sobre a numeração do banco
            assertThat(pedidos).extracting(Pedido::chave).containsExactly("consulta-por-id");
            assertThat(servidor.recebidas()).extracting(Requisicao::caminho).containsExactly(
                    "/api/v1/consumidores?pagina=2&tamanhoPagina=1", "/api/v1/consumidores/42");
        }
    }

    @Test
    void deve_creditar_e_comprar_pelo_numero_do_cartao_quando_chamar() throws Exception {
        try (var servidor = new ServidorFalso(r -> Resposta.de(201, "{}"))) {
            var operacoes = new OperacoesDoNovo(new ClienteHttp(servidor.url()));

            // Agir
            operacoes.creditar(TipoCartao.COMBUSTIVEL, 7, "10.00");
            operacoes.comprar(TipoCartao.ALIMENTACAO, 7, "1.00");

            // Verificar
            var credito = servidor.recebidas().get(0);
            assertThat(credito.caminho()).isEqualTo("/api/v1/creditos");
            assertThat(JsonPath.<String>read(credito.corpo(), "$.numeroCartao"))
                    .isEqualTo("8000000000000023");
            assertThat(JsonPath.<Integer>read(credito.corpo(), "$.tipoEstabelecimento"))
                    .isEqualTo(3);
            var compra = servidor.recebidas().get(1);
            assertThat(compra.caminho()).isEqualTo("/api/v1/compras");
            assertThat(JsonPath.<String>read(compra.corpo(), "$.numeroCartao"))
                    .isEqualTo("8000000000000021");
        }
    }

    @Test
    void deve_preparar_comprar_e_ler_o_saldo_pelo_location_quando_disputar() throws Exception {
        var respostas = Map.of("POST /api/v1/consumidores",
                new Resposta(201, "{}", Map.of("Location", "/api/v1/consumidores/9")),
                "GET /api/v1/consumidores/9",
                Resposta.de(200, "{\"cartoes\":[{\"saldo\":\"90.000000\"}]}"));
        try (var servidor = new ServidorFalso(r -> respostas
                .getOrDefault(r.metodo() + " " + r.caminho(), Resposta.de(201, "{}")))) {
            var operacoes = new OperacoesDoNovo(new ClienteHttp(servidor.url()));

            // Agir
            var cartao = operacoes.prepararFarmaciaComSaldo("100.00");
            var status = operacoes.comprarNaFarmacia(cartao, "10.00");
            var saldo = operacoes.saldoDe(cartao);

            // Verificar
            assertThat(cartao.referencia()).isEqualTo("/api/v1/consumidores/9");
            assertThat(cartao.numero()).matches("\\d{16}");
            assertThat(status).isEqualTo(201);
            assertThat(saldo).isEqualByComparingTo("90.00");
        }
    }
}
