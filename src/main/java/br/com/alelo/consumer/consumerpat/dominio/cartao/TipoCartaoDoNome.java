package br.com.alelo.consumer.consumerpat.dominio.cartao;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroTipoDeCartaoInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Arrays;

/**
 * Lê o tipo de um cartão pelo nome, vindo de fora do domínio.
 *
 * <p>
 * É o nome do próprio tipo ({@code ALIMENTACAO}), e não o código do estabelecimento, que
 * {@link TipoCartaoDoEstabelecimento} lê e que diz onde uma compra foi feita. Um nome que não
 * existe é um erro, e não uma exceção, e a falha não repete o texto recebido.
 */
public final class TipoCartaoDoNome {

    private TipoCartaoDoNome() {
    }

    /**
     * Converte o nome em tipo, ou devolve o erro de validação.
     *
     * @param nome o nome como o cliente o escreveu, possivelmente nulo
     * @return o tipo, ou {@link ErroTipoDeCartaoInvalido}
     */
    public static Resultado<TipoCartao, ErroDominio> converter(String nome) {
        return AusenciaComoErro.converter(Arrays.stream(TipoCartao.values())
                .filter(tipo -> tipo.name().equals(nome)).findFirst(),
                ErroTipoDeCartaoInvalido::new);
    }
}
