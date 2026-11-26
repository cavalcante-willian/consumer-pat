package br.com.alelo.consumer.consumerpat.api.comum.documentacao;

import br.com.alelo.consumer.consumerpat.api.comum.problema.MensagemProblema;
import br.com.alelo.consumer.consumerpat.api.comum.requisicao.IdDeRequisicao;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomiser;

import java.util.Map;
import java.util.Optional;

/**
 * Completa, em todas as operações, o que a API faz igual em qualquer rota e que seria repetido em
 * cada controller.
 *
 * <p>
 * Toda resposta de erro tem o mesmo corpo, {@code application/problem+json} no formato de
 * {@link MensagemProblema}, e então o controller declara só o status e o motivo, e esta classe lhe
 * dá o corpo. Toda operação pode falhar de forma inesperada, com {@code 500}, e toda resposta traz
 * o cabeçalho {@code X-Request-Id}. Declarar isso uma vez, aqui, evita que um controller novo
 * esqueça.
 */
public class RespostasDeErroDaApi implements OpenApiCustomiser {

    /** O tipo de mídia dos erros. */
    public static final String MIDIA_DO_PROBLEMA = "application/problem+json";

    /** O status que toda operação pode devolver. */
    public static final String FALHA_INESPERADA = "500";

    /**
     * O tipo de mídia que o springdoc põe numa resposta anotada só com o status e o motivo: um
     * objeto qualquer, que aqui é trocado pelo corpo de erro de verdade.
     */
    private static final String CORPO_PADRAO_DO_SPRINGDOC = "*/*";

    @Override
    public void customise(OpenAPI especificacao) {
        var problema = ModelConverters.getInstance().resolveAsResolvedSchema(
                new AnnotatedType(MensagemProblema.class).resolveAsRef(true));
        var componentes = Optional.ofNullable(especificacao.getComponents())
                .orElseGet(Components::new);
        especificacao.setComponents(componentes);
        problema.referencedSchemas.forEach(componentes::addSchemas);

        var corpoDoErro = new Content().addMediaType(MIDIA_DO_PROBLEMA,
                new MediaType().schema(problema.schema));
        var idDaRequisicao = new Header().schema(new StringSchema())
                .description("O id da requisição, o mesmo do log e do corpo de erro.");

        especificacao.getPaths().values().stream().flatMap(rota -> rota.readOperations().stream())
                .forEach(operacao -> {
                    operacao.getResponses().computeIfAbsent(FALHA_INESPERADA,
                            status -> new ApiResponse().description("Falha inesperada."));
                    operacao.getResponses().entrySet().stream()
                            .filter(resposta -> !resposta.getKey().startsWith("2"))
                            .map(Map.Entry::getValue)
                            .filter(resposta -> Optional.ofNullable(resposta.getContent())
                                    .map(corpo -> corpo.containsKey(CORPO_PADRAO_DO_SPRINGDOC))
                                    .orElse(true))
                            .forEach(resposta -> resposta.setContent(corpoDoErro));
                    operacao.getResponses().values().forEach(resposta -> resposta
                            .addHeaderObject(IdDeRequisicao.CABECALHO, idDaRequisicao));
                });
    }
}
