package br.com.alelo.consumer.consumerpat.infraestrutura.saude;

import br.com.alelo.consumer.consumerpat.dominio.erros.indisponivel.ErroBancoIndisponivel;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeDependencia;

import java.util.Optional;

/**
 * Lê o veredito da validação de uma conexão que o pool entregou.
 *
 * <p>
 * Uma conexão que veio, mas não passou na validação, não é uma exceção: o driver só diz que não.
 * Ela é tão indisponível quanto a que nem veio, e vira o mesmo erro.
 */
final class ValidacaoDaConexao {

    /** O tipo que se registra quando a conexão veio, mas não passou na validação. */
    static final String CONEXAO_INVALIDA = "conexao.invalida";

    private ValidacaoDaConexao() {
    }

    /**
     * Converte o veredito.
     *
     * @param valida o que a validação respondeu
     * @return {@link EstadoDeDependencia#DISPONIVEL} se a conexão é válida, ou
     *         {@link ErroBancoIndisponivel}
     */
    static Resultado<EstadoDeDependencia, ErroBancoIndisponivel> de(boolean valida) {
        return Optional.of(valida).filter(Boolean::booleanValue)
                .<Resultado<EstadoDeDependencia, ErroBancoIndisponivel>>map(
                        sim -> new Sucesso<>(EstadoDeDependencia.DISPONIVEL))
                .orElseGet(() -> new Falha<>(new ErroBancoIndisponivel(CONEXAO_INVALIDA,
                        ErroBancoIndisponivel.SEM_ESTADO_SQL)));
    }
}
