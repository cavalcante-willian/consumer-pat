package br.com.alelo.consumer.consumerpat.dominio.cartao;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroTipoEstabelecimentoInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lê o tipo de cartão que corresponde a um código de tipo de estabelecimento vindo de fora, sem
 * lançar exceção.
 *
 * <p>
 * O código 99 passa na validação de borda, porque é um inteiro, e só o domínio sabe que o tipo não
 * existe. A busca é num registro montado a partir dos próprios tipos, então um tipo novo no
 * {@link TipoCartao} é reconhecido aqui sem tocar neste arquivo.
 */
public final class TipoCartaoDoEstabelecimento {

    private static final Map<Integer, TipoCartao> POR_CODIGO = Arrays.stream(TipoCartao.values())
            .collect(Collectors.toUnmodifiableMap(TipoCartao::codigoDeEstabelecimento,
                    Function.identity()));

    private TipoCartaoDoEstabelecimento() {
    }

    /**
     * Converte o código no tipo de cartão que vale nesse estabelecimento.
     *
     * @param codigo o código do tipo de estabelecimento: 1, 2 ou 3
     * @return sucesso com o tipo, ou falha com {@link ErroTipoEstabelecimentoInvalido}
     */
    public static Resultado<TipoCartao, ErroDominio> converter(int codigo) {
        return AusenciaComoErro.converter(Optional.ofNullable(POR_CODIGO.get(codigo)),
                () -> new ErroTipoEstabelecimentoInvalido(codigo));
    }
}
