package br.com.alelo.consumer.consumerpat.dominio.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroConsumidorIdInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Optional;

/**
 * Lê o id de um consumidor a partir de um número vindo de fora do domínio.
 *
 * <p>
 * O construtor de {@link ConsumidorId} lança para um valor que não é positivo, porque quem o chama
 * com um valor assim errou. O número que vem de uma rota não é erro de quem programou, e passa por
 * aqui, que devolve um erro de validação.
 */
public final class ConsumidorIdDeNumero {

    private ConsumidorIdDeNumero() {
    }

    /**
     * Converte o número em id, ou devolve o erro de validação.
     *
     * @param numero o número recebido
     * @return o id, ou {@link ErroConsumidorIdInvalido} se não é positivo
     */
    public static Resultado<ConsumidorId, ErroDominio> converter(long numero) {
        return AusenciaComoErro.converter(
                Optional.of(numero).filter(candidato -> candidato > 0).map(ConsumidorId::new),
                ErroConsumidorIdInvalido::new);
    }
}
