package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia;

import com.zaxxer.hikari.SQLExceptionOverride;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Diz ao pool de conexões que estourar o tempo de espera por uma trava não estraga a conexão.
 *
 * <p>
 * O H2 sinaliza esse estouro com o estado {@code HYT00}, e o pool trata esse estado como conexão
 * quebrada: a fecha na hora. O rollback que vem em seguida falha com "Connection is closed", e o
 * Spring devolve a falha do rollback no lugar da falha da trava, de modo que quem chamou recebe uma
 * exceção de transação opaca, e não a exceção de trava que se pode tratar. A conexão, na verdade,
 * está sã: só a espera acabou.
 *
 * <p>
 * É ligada na configuração do pool, e só vale para o código do H2: se o banco mudar, o código do
 * estouro de trava muda com ele, e é aqui que se atualiza.
 */
public class ErroDeTravaNaoDescartaConexao implements SQLExceptionOverride {

    /** O código do H2 para "Timeout trying to lock table". */
    public static final int CODIGO_DE_TEMPO_DE_TRAVA_DO_H2 = 50200;

    /**
     * Decide o que o pool faz com a conexão depois deste erro.
     *
     * @param erro o erro lançado pelo banco
     * @return manter a conexão se o erro é o estouro da espera por trava, e seguir o critério
     *         padrão do pool para qualquer outro
     */
    @java.lang.Override
    public Override adjudicate(SQLException erro) {
        return Optional.of(erro).filter(e -> e.getErrorCode() == CODIGO_DE_TEMPO_DE_TRAVA_DO_H2)
                .map(e -> Override.DO_NOT_EVICT).orElse(Override.CONTINUE_EVICT);
    }
}
