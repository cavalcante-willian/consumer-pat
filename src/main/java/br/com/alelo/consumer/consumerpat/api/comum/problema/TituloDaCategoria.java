package br.com.alelo.consumer.consumerpat.api.comum.problema;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;

import java.util.EnumMap;
import java.util.Map;

/**
 * O título humano de cada categoria de erro de domínio.
 *
 * <p>
 * O domínio guarda o status de cada categoria, mas o texto que o cliente lê é da API. Um teste
 * percorre todas as categorias, de modo que uma categoria nova sem título quebra o build, e não
 * devolve um título nulo em produção.
 */
public final class TituloDaCategoria {

    private static final Map<CategoriaErro, String> TITULOS = new EnumMap<>(
            Map.of(CategoriaErro.VALIDACAO, "Entrada inválida", CategoriaErro.REGRA_DE_NEGOCIO,
                    "Regra de negócio não atendida", CategoriaErro.NAO_ENCONTRADO,
                    "Recurso não encontrado"));

    private TituloDaCategoria() {
    }

    /**
     * Devolve o título da categoria.
     *
     * @param categoria a categoria do erro
     * @return o título humano
     */
    public static String de(CategoriaErro categoria) {
        return TITULOS.get(categoria);
    }
}
