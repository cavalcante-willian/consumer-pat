package br.com.alelo.consumer.consumerpat.infraestrutura.saude;

import br.com.alelo.consumer.consumerpat.dominio.erros.indisponivel.ErroBancoIndisponivel;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeDependencia;

import javax.sql.DataSource;

import java.sql.SQLException;

/**
 * A fronteira com o JDBC: pede uma conexão ao pool, a valida e a devolve.
 *
 * <p>
 * É a única peça que captura {@link SQLException}, porque o JDBC genuinamente lança uma exceção
 * verificada, e a converte na hora em {@link Falha}: depois dela nada lança. Não decide nada além
 * de onde o JDBC pode falhar. O veredito da validação é lido em {@link ValidacaoDaConexao}, e a
 * falha do driver é traduzida em {@link ErroBancoIndisponivelDeSql}.
 */
final class VerificacaoDaConexao {

    private VerificacaoDaConexao() {
    }

    /**
     * Verifica o banco.
     *
     * @param fonte           o pool de conexões
     * @param prazoEmSegundos o prazo da validação da conexão
     * @return {@link EstadoDeDependencia#DISPONIVEL} se obteve a conexão e ela respondeu dentro do
     *         prazo, ou {@link ErroBancoIndisponivel}
     */
    static Resultado<EstadoDeDependencia, ErroBancoIndisponivel> verificar(DataSource fonte,
            int prazoEmSegundos) {
        try (var conexao = fonte.getConnection()) {
            return ValidacaoDaConexao.de(conexao.isValid(prazoEmSegundos));
        } catch (SQLException falha) {
            return new Falha<>(ErroBancoIndisponivelDeSql.converter(falha));
        }
    }
}
