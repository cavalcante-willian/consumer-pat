package br.com.alelo.consumer.consumerpat.seguranca;

import br.com.alelo.consumer.consumerpat.medicao.CartaoDeDisputa;
import br.com.alelo.consumer.consumerpat.medicao.ClienteHttp;
import br.com.alelo.consumer.consumerpat.medicao.ServidorFalso;
import br.com.alelo.consumer.consumerpat.medicao.ServidorFalso.Requisicao;
import br.com.alelo.consumer.consumerpat.medicao.ServidorFalso.Resposta;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SondaDoNovoTeste {

    private static final Resposta OK = Resposta.de(200, "{}");

    private static final String HOSTIL = "Robert\"); DROP TABLE x;--\\ \u0001";

    @Test
    void deve_postar_o_nome_hostil_escapado_e_ainda_como_json_valido_quando_cadastrar()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoNovo(new ClienteHttp(servidor.url())).cadastrarComNome(HOSTIL);

            // Verificar: o JSON é lido, e o nome volta idêntico ao que foi mandado
            var enviada = servidor.recebidas().get(0);
            assertThat(enviada.metodo()).isEqualTo("POST");
            assertThat(enviada.caminho()).isEqualTo("/api/v1/consumidores");
            assertThat(JsonPath.<String>read(enviada.corpo(), "$.dados.nome")).isEqualTo(HOSTIL);
        }
    }

    @Test
    void deve_dar_documento_e_cartao_novos_a_cada_cadastro_quando_cadastrar_duas_vezes()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            var sonda = new SondaDoNovo(new ClienteHttp(servidor.url()));

            // Agir
            sonda.cadastrarComNome("a");
            sonda.cadastrarComNome("b");

            // Verificar: o banco é compartilhado, e documento e cartão são únicos
            var primeira = servidor.recebidas().get(0).corpo();
            var segunda = servidor.recebidas().get(1).corpo();
            assertThat(JsonPath.<String>read(primeira, "$.dados.documento"))
                    .isNotEqualTo(JsonPath.<String>read(segunda, "$.dados.documento"));
            assertThat(JsonPath.<String>read(primeira, "$.numerosCartoes.FARMACIA"))
                    .isNotEqualTo(JsonPath.<String>read(segunda, "$.numerosCartoes.FARMACIA"));
        }
    }

    @Test
    void deve_mandar_um_campo_que_o_contrato_nao_tem_quando_cadastrar_com_campo_desconhecido()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoNovo(new ClienteHttp(servidor.url())).cadastrarComCampoDesconhecido();

            // Verificar: o resto do cadastro é válido, e só o campo extra destoa
            var corpo = servidor.recebidas().get(0).corpo();
            assertThat(JsonPath.<String>read(corpo, "$.hacker")).isEqualTo("1");
            assertThat(JsonPath.<String>read(corpo, "$.dados.nome")).isNotBlank();
        }
    }

    @Test
    void deve_mandar_um_json_truncado_quando_cadastrar_com_json_malformado() throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoNovo(new ClienteHttp(servidor.url())).cadastrarComJsonMalformado();

            // Verificar
            assertThat(servidor.recebidas().get(0).corpo()).isEqualTo("{\"dados\":");
        }
    }

    @Test
    void deve_mandar_o_numero_hostil_inteiro_na_compra_quando_comprar_com_numero_de_cartao()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoNovo(new ClienteHttp(servidor.url())).comprarComNumeroDeCartao(HOSTIL);

            // Verificar
            var enviada = servidor.recebidas().get(0);
            assertThat(enviada.caminho()).isEqualTo("/api/v1/compras");
            assertThat(JsonPath.<String>read(enviada.corpo(), "$.numeroCartao")).isEqualTo(HOSTIL);
        }
    }

    @Test
    void deve_codificar_a_ordenacao_e_a_direcao_na_query_quando_listar() throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoNovo(new ClienteHttp(servidor.url())).listar("nome; DROP", "asc'--");

            // Verificar: um espaço e uma aspa não saem soltos na URL
            assertThat(servidor.recebidas().get(0).caminho())
                    .isEqualTo("/api/v1/consumidores?ordenacao=nome%3B+DROP&direcao=asc%27--");
        }
    }

    @Test
    void deve_creditar_um_cartao_que_nao_existe_quando_pedir_o_credito_inexistente()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoNovo(new ClienteHttp(servidor.url())).creditarCartaoInexistente();

            // Verificar
            var enviada = servidor.recebidas().get(0);
            assertThat(enviada.caminho()).isEqualTo("/api/v1/creditos");
            assertThat(JsonPath.<String>read(enviada.corpo(), "$.numeroCartao"))
                    .isEqualTo("9999999999999999");
        }
    }

    @Test
    void deve_tentar_a_compra_por_get_com_o_cartao_e_o_valor_na_query_quando_comprar_por_get()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoNovo(new ClienteHttp(servidor.url()))
                    .comprarPorGet(new CartaoDeDisputa("4111111111111111", "/x"), "10.00");

            // Verificar
            assertThat(servidor.recebidas()).containsExactly(new Requisicao("GET",
                    "/api/v1/compras?tipoEstabelecimento=2&nomeEstabelecimento=E"
                            + "&numeroCartao=4111111111111111&descricaoProduto=P&valor=10.00",
                    ""));
        }
    }

    @Test
    void deve_atualizar_o_consumidor_do_cartao_mandando_o_saldo_quando_atualizar_com_saldo()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoNovo(new ClienteHttp(servidor.url())).atualizarMandandoSaldo(
                    new CartaoDeDisputa("4111111111111111", "/api/v1/consumidores/7"), "999999.00");

            // Verificar
            var enviada = servidor.recebidas().get(0);
            assertThat(enviada.metodo()).isEqualTo("PUT");
            assertThat(enviada.caminho()).isEqualTo("/api/v1/consumidores/7");
            assertThat(JsonPath.<Double>read(enviada.corpo(), "$.saldo")).isEqualTo(999999.0);
            assertThat(servidor.cabecalhoDaUltima("Content-Type")).contains("application/json");
        }
    }

    @Test
    void deve_juntar_a_resposta_do_cadastro_e_a_da_leitura_quando_expor_dados() throws Exception {
        var respostas = Map.of("POST /api/v1/consumidores",
                new Resposta(201, "CRIADO", Map.of("Location", "/api/v1/consumidores/5")),
                "GET /api/v1/consumidores/5", Resposta.de(200, "LIDO"));
        try (var servidor = new ServidorFalso(
                r -> respostas.getOrDefault(r.metodo() + " " + r.caminho(), OK))) {
            // Agir
            var exposicao = new SondaDoNovo(new ClienteHttp(servidor.url()))
                    .exporDadosDeConsumidor();

            // Verificar: o que se procura é o que foi mandado no cadastro
            assertThat(exposicao.texto()).isEqualTo("CRIADOLIDO");
            assertThat(servidor.recebidas().get(0).corpo()).contains(exposicao.documento())
                    .contains(exposicao.cartao());
        }
    }

    @Test
    void deve_pedir_as_rotas_de_leitura_e_de_superficie_certas_quando_sondar() throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            var sonda = new SondaDoNovo(new ClienteHttp(servidor.url()));

            // Agir
            sonda.requisicaoValida();
            sonda.consoleDoH2();
            sonda.swagger();
            sonda.especificacao();
            sonda.trace();
            sonda.preVooDeOrigemEstranha();

            // Verificar
            assertThat(servidor.recebidas()).containsExactly(
                    new Requisicao("GET", "/api/v1/saude", ""),
                    new Requisicao("GET", "/h2-console", ""),
                    new Requisicao("GET", "/swagger-ui/index.html", ""),
                    new Requisicao("GET", "/v3/api-docs", ""),
                    new Requisicao("TRACE", "/api/v1/consumidores", ""),
                    new Requisicao("OPTIONS", "/api/v1/consumidores", ""));
            assertThat(servidor.cabecalhoDaUltima("Origin"))
                    .contains("http://origem-estranha.exemplo");
            assertThat(servidor.cabecalhoDaUltima("Access-Control-Request-Method"))
                    .contains("POST");
        }
    }
}
