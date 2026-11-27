package br.com.alelo.consumer.consumerpat.api.comum.documentacao;

import br.com.alelo.consumer.consumerpat.api.comum.requisicao.IdDeRequisicao;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RespostasDeErroDaApiTeste {

    private static final String JSON = "application/json";

    private static final String REFERENCIA_DO_PROBLEMA = "#/components/schemas/MensagemProblema";

    private final RespostasDeErroDaApi customizador = new RespostasDeErroDaApi();

    private OpenAPI especificacao;

    private static Content corpo(String midia) {
        return new Content().addMediaType(midia, new MediaType().schema(new StringSchema()));
    }

    private ApiResponses respostas() {
        return especificacao.getPaths().get("/rota").getPost().getResponses();
    }

    @BeforeEach
    void montar() {
        var respostas = new ApiResponses()
                .addApiResponse("201", new ApiResponse().description("criado").content(corpo(JSON)))
                .addApiResponse("404",
                        new ApiResponse().description("não existe")
                                .content(new Content().addMediaType("*/*",
                                        new MediaType().schema(new ObjectSchema()))))
                .addApiResponse("422", new ApiResponse().description("saldo insuficiente"))
                .addApiResponse("503",
                        new ApiResponse().description("degradado").content(corpo(JSON)));
        especificacao = new OpenAPI().paths(new Paths().addPathItem("/rota",
                new PathItem().post(new Operation().responses(respostas))));
    }

    @Test
    void deve_dar_o_corpo_de_problema_quando_o_erro_nao_tem_corpo_ou_tem_o_padrao_do_springdoc() {
        // Agir
        customizador.customise(especificacao);

        // Verificar
        assertThat(respostas().get("404").getContent())
                .containsOnlyKeys(RespostasDeErroDaApi.MIDIA_DO_PROBLEMA);
        assertThat(respostas().get("422").getContent())
                .containsOnlyKeys(RespostasDeErroDaApi.MIDIA_DO_PROBLEMA);
        assertThat(respostas().get("404").getContent().get(RespostasDeErroDaApi.MIDIA_DO_PROBLEMA)
                .getSchema().get$ref()).isEqualTo(REFERENCIA_DO_PROBLEMA);
    }

    @Test
    void deve_manter_o_corpo_declarado_quando_a_resposta_ja_tem_um() {
        // Agir
        customizador.customise(especificacao);

        // Verificar: o sucesso e o 503 da saúde, que tem corpo próprio, ficam como o controller os
        // declarou
        assertThat(respostas().get("201").getContent()).containsOnlyKeys(JSON);
        assertThat(respostas().get("503").getContent()).containsOnlyKeys(JSON);
    }

    @Test
    void deve_acrescentar_o_500_quando_a_operacao_nao_o_declara() {
        // Agir
        customizador.customise(especificacao);

        // Verificar
        assertThat(respostas()).containsKey(RespostasDeErroDaApi.FALHA_INESPERADA);
        assertThat(respostas().get(RespostasDeErroDaApi.FALHA_INESPERADA).getContent())
                .containsOnlyKeys(RespostasDeErroDaApi.MIDIA_DO_PROBLEMA);
    }

    @Test
    void deve_manter_o_500_do_controller_quando_a_operacao_o_declara() {
        // Preparar
        respostas().addApiResponse("500", new ApiResponse().description("o texto do controller"));

        // Agir
        customizador.customise(especificacao);

        // Verificar
        assertThat(respostas().get("500").getDescription()).isEqualTo("o texto do controller");
    }

    @Test
    void deve_por_o_x_request_id_em_toda_resposta_quando_customizar() {
        // Agir
        customizador.customise(especificacao);

        // Verificar
        assertThat(respostas().values()).isNotEmpty()
                .allSatisfy(resposta -> assertThat(resposta.getHeaders())
                        .containsKey(IdDeRequisicao.CABECALHO));
    }

    @Test
    void deve_registrar_o_esquema_do_problema_quando_a_especificacao_nao_tem_componentes() {
        // Preparar
        assertThat(especificacao.getComponents()).isNull();

        // Agir
        customizador.customise(especificacao);

        // Verificar
        assertThat(especificacao.getComponents().getSchemas()).containsKey("MensagemProblema");
    }
}
