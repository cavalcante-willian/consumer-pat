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

class SondaDoLegadoTeste {

    private static final Resposta OK = Resposta.de(200, "[]");

    private static final String HOSTIL = "Robert\"); DROP TABLE x;--\\ \u0001";

    @Test
    void deve_postar_o_nome_hostil_escapado_e_ainda_como_json_valido_quando_cadastrar()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoLegado(new ClienteHttp(servidor.url())).cadastrarComNome(HOSTIL);

            // Verificar
            var enviada = servidor.recebidas().get(0);
            assertThat(enviada.metodo()).isEqualTo("POST");
            assertThat(enviada.caminho()).isEqualTo("/consumer/createConsumer");
            assertThat(JsonPath.<String>read(enviada.corpo(), "$.name")).isEqualTo(HOSTIL);
        }
    }

    @Test
    void deve_dar_documento_novo_a_cada_cadastro_quando_cadastrar_duas_vezes() throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            var sonda = new SondaDoLegado(new ClienteHttp(servidor.url()));

            // Agir
            sonda.cadastrarComNome("a");
            sonda.cadastrarComNome("b");

            // Verificar
            assertThat(
                    JsonPath.<Integer>read(servidor.recebidas().get(0).corpo(), "$.documentNumber"))
                            .isNotEqualTo(JsonPath.<Integer>read(
                                    servidor.recebidas().get(1).corpo(), "$.documentNumber"));
        }
    }

    @Test
    void deve_mandar_um_campo_que_o_legado_nao_tem_quando_cadastrar_com_campo_desconhecido()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoLegado(new ClienteHttp(servidor.url())).cadastrarComCampoDesconhecido();

            // Verificar
            assertThat(JsonPath.<String>read(servidor.recebidas().get(0).corpo(), "$.hacker"))
                    .isEqualTo("1");
        }
    }

    @Test
    void deve_mandar_um_json_truncado_quando_cadastrar_com_json_malformado() throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoLegado(new ClienteHttp(servidor.url())).cadastrarComJsonMalformado();

            // Verificar
            assertThat(servidor.recebidas().get(0).corpo()).isEqualTo("{\"name\":");
        }
    }

    @Test
    void deve_codificar_o_numero_hostil_na_query_quando_comprar_com_numero_de_cartao()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoLegado(new ClienteHttp(servidor.url())).comprarComNumeroDeCartao("1 OR 1=1");

            // Verificar
            assertThat(servidor.recebidas().get(0).caminho())
                    .isEqualTo("/consumer/buy?establishmentType=2&establishmentName=E"
                            + "&cardNumber=1+OR+1%3D1&productDescription=P&value=1.0");
        }
    }

    @Test
    void deve_mandar_a_ordenacao_codificada_na_query_mesmo_sem_o_legado_ordenar_quando_listar()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoLegado(new ClienteHttp(servidor.url())).listar("nome; DROP", "asc'--");

            // Verificar
            assertThat(servidor.recebidas().get(0).caminho())
                    .isEqualTo("/consumer/consumerList?ordenacao=nome%3B+DROP&direcao=asc%27--");
        }
    }

    @Test
    void deve_creditar_um_cartao_que_nao_existe_quando_pedir_o_credito_inexistente()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoLegado(new ClienteHttp(servidor.url())).creditarCartaoInexistente();

            // Verificar
            assertThat(servidor.recebidas()).containsExactly(new Requisicao("GET",
                    "/consumer/setcardbalance?cardNumber=987654321&value=1.0", ""));
        }
    }

    @Test
    void deve_comprar_por_get_com_o_cartao_e_o_valor_na_query_quando_comprar_por_get()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            // Agir
            new SondaDoLegado(new ClienteHttp(servidor.url()))
                    .comprarPorGet(new CartaoDeDisputa("900000001", "900000001"), "10.00");

            // Verificar: o legado compra por GET, e o cartão e o valor viajam na URL
            assertThat(servidor.recebidas()).containsExactly(new Requisicao("GET",
                    "/consumer/buy?establishmentType=2&establishmentName=E&cardNumber=900000001"
                            + "&productDescription=P&value=10.00",
                    ""));
        }
    }

    @Test
    void deve_achar_o_id_pela_listagem_e_atualizar_mandando_o_saldo_quando_atualizar_com_saldo()
            throws Exception {
        var listagem = Resposta.de(200,
                "[{\"id\":3,\"drugstoreNumber\":111},{\"id\":8,\"drugstoreNumber\":900000001}]");
        try (var servidor = new ServidorFalso(
                r -> Map.of("/consumer/consumerList", listagem).getOrDefault(r.caminho(), OK))) {
            // Agir
            new SondaDoLegado(new ClienteHttp(servidor.url())).atualizarMandandoSaldo(
                    new CartaoDeDisputa("900000001", "900000001"), "999999.00");

            // Verificar: o id é o do consumidor dono do cartão, e não o primeiro da lista
            var enviada = servidor.recebidas().get(1);
            assertThat(enviada.caminho()).isEqualTo("/consumer/updateConsumer");
            assertThat(JsonPath.<Integer>read(enviada.corpo(), "$.id")).isEqualTo(8);
            assertThat(JsonPath.<Double>read(enviada.corpo(), "$.drugstoreCardBalance"))
                    .isEqualTo(999999.0);
        }
    }

    @Test
    void deve_ler_a_listagem_inteira_como_texto_do_consumidor_quando_expor_dados()
            throws Exception {
        var listagem = Resposta.de(200, "[{\"documentNumber\":800000002}]");
        try (var servidor = new ServidorFalso(r -> Map.of("GET /consumer/consumerList", listagem)
                .getOrDefault(r.metodo() + " " + r.caminho(), OK))) {
            // Agir
            var exposicao = new SondaDoLegado(new ClienteHttp(servidor.url()))
                    .exporDadosDeConsumidor();

            // Verificar: o que se procura é o que foi cadastrado
            assertThat(exposicao.texto()).isEqualTo("[{\"documentNumber\":800000002}]");
            assertThat(servidor.recebidas().get(0).corpo())
                    .contains("\"documentNumber\":" + exposicao.documento())
                    .contains("\"drugstoreNumber\":" + exposicao.cartao());
        }
    }

    @Test
    void deve_pedir_as_rotas_de_leitura_e_de_superficie_certas_quando_sondar() throws Exception {
        try (var servidor = new ServidorFalso(r -> OK)) {
            var sonda = new SondaDoLegado(new ClienteHttp(servidor.url()));

            // Agir
            sonda.requisicaoValida();
            sonda.consoleDoH2();
            sonda.swagger();
            sonda.especificacao();
            sonda.trace();
            sonda.preVooDeOrigemEstranha();

            // Verificar
            assertThat(servidor.recebidas()).containsExactly(
                    new Requisicao("GET", "/consumer/consumerList", ""),
                    new Requisicao("GET", "/h2-console", ""),
                    new Requisicao("GET", "/swagger-ui/index.html", ""),
                    new Requisicao("GET", "/v3/api-docs", ""),
                    new Requisicao("TRACE", "/consumer/consumerList", ""),
                    new Requisicao("OPTIONS", "/consumer/consumerList", ""));
            assertThat(servidor.cabecalhoDaUltima("Origin"))
                    .contains("http://origem-estranha.exemplo");
        }
    }
}
