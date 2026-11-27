package br.com.alelo.consumer.consumerpat.api.consumidor.listar;

import br.com.alelo.consumer.consumerpat.api.comum.documentacao.TagsDaApi;
import br.com.alelo.consumer.consumerpat.api.comum.pagina.MensagemPagina;
import br.com.alelo.consumer.consumerpat.api.comum.pagina.PaginaEmMensagem;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeResultado;
// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.listar.ListarConsumidoresCaso;
import br.com.alelo.consumer.consumerpat.dominio.pagina.PedidoDePaginaDeParametros;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * {@code GET /api/v1/consumidores}: lista os consumidores, uma página por vez.
 *
 * <p>
 * Nenhuma listagem devolve coleção ilimitada: a página começa em 1, o tamanho vai até 100, e a
 * ordenação é um conjunto fechado, nunca o nome de uma coluna. Os quatro parâmetros são lidos
 * juntos, e todos os erros saem de uma vez. O controller nunca recebe um {@code Pageable} do
 * cliente.
 */
@Tag(name = TagsDaApi.CONSUMIDORES)
@RestController
@RequestMapping("/api/v1/consumidores")
public class ListarConsumidoresController {

    private final ListarConsumidoresCaso caso;

    ListarConsumidoresController(ListarConsumidoresCaso caso) {
        this.caso = caso;
    }

    /**
     * Lista uma página de consumidores.
     *
     * @param pagina        a página, a partir de 1
     * @param tamanhoPagina quantos itens por página, de 1 a 100
     * @param ordenacao     por que campo ordenar: {@code id} ou {@code nome}
     * @param direcao       em que sentido: {@code asc} ou {@code desc}
     * @param requisicao    a requisição, para o caminho do problema
     * @return {@code 200} com o envelope da página, ou o problema
     */
    @Operation(summary = "Lista os consumidores, paginados", description = """
            Devolve uma página só com o `id` e o `nome` de cada consumidor, sem os cartões nem os \
            dados pessoais. Os itens vêm em `itens`, e o envelope traz a página, o tamanho, o \
            total de consumidores e o total de páginas. Uma página além da última vem vazia.""")
    @ApiResponse(responseCode = "200", description = "A página pedida.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MensagemPagina.class)))
    @ApiResponse(responseCode = "400",
            description = "Parâmetro inválido: página menor que 1, tamanho fora de 1 a 100, "
                    + "ordenação diferente de `id` e `nome`, ou direção diferente de `asc` e "
                    + "`desc`. Todos os erros vêm juntos.")
    @GetMapping
    public ResponseEntity<Object> listar(@RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "20") int tamanhoPagina,
            @RequestParam(defaultValue = "nome") String ordenacao,
            @RequestParam(defaultValue = "asc") String direcao, HttpServletRequest requisicao) {
        return RespostaDeResultado.responder(
                PedidoDePaginaDeParametros.converter(pagina, tamanhoPagina, ordenacao, direcao)
                        .mapear(caso::listar),
                resultado -> PaginaEmMensagem.montar(resultado, ResumoEmMensagem::montar),
                HttpStatus.OK, requisicao.getRequestURI());
    }
}
