package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.pagina.PedidoDePagina;

import java.util.List;
import java.util.Map;

/**
 * O tamanho de página pedido está fora de 1 até o teto.
 *
 * <p>
 * O teto existe para nenhuma requisição levar o servidor ao chão com uma página enorme, e acima
 * dele a resposta é um erro, e não um corte silencioso no teto.
 */
public record ErroTamanhoDePaginaInvalido() implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-PAG-002";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Tamanho de página inválido: use de 1 a " + PedidoDePagina.TAMANHO_MAXIMO + ".";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("tamanhoPagina", List.of("deve ser de 1 a " + PedidoDePagina.TAMANHO_MAXIMO));
    }
}
