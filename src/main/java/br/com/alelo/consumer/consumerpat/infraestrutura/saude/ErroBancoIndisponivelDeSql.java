package br.com.alelo.consumer.consumerpat.infraestrutura.saude;

import br.com.alelo.consumer.consumerpat.dominio.erros.indisponivel.ErroBancoIndisponivel;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Traduz a falha do JDBC no erro de domínio, sem levar o que pode vazar.
 *
 * <p>
 * Do que o driver disse fica o tipo da exceção e o estado SQL, que identificam a causa. A mensagem
 * fica de fora, porque pode citar host, URL de conexão ou a versão do banco.
 */
final class ErroBancoIndisponivelDeSql {

    private ErroBancoIndisponivelDeSql() {
    }

    /**
     * Converte a falha.
     *
     * @param falha a exceção que o JDBC lançou
     * @return o erro, com o nome da classe da exceção e o estado SQL, ou
     *         {@value ErroBancoIndisponivel#SEM_ESTADO_SQL} se o driver não informou
     */
    static ErroBancoIndisponivel converter(SQLException falha) {
        return new ErroBancoIndisponivel(falha.getClass().getName(), Optional
                .ofNullable(falha.getSQLState()).orElse(ErroBancoIndisponivel.SEM_ESTADO_SQL));
    }
}
