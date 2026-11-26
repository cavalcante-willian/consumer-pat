package br.com.alelo.consumer.consumerpat.seguranca;

/**
 * As famílias de status HTTP que as verificações perguntam.
 */
public final class Status {

    private static final int CENTENA_DO_ERRO_DE_CLIENTE = 4;

    private static final int CENTENA_DO_ERRO_DE_SERVIDOR = 5;

    private Status() {
    }

    /**
     * Diz se o status é uma recusa do cliente, de {@code 400} a {@code 499}.
     *
     * @param status o status
     * @return se é erro de cliente
     */
    public static boolean erroDeCliente(int status) {
        return status / 100 == CENTENA_DO_ERRO_DE_CLIENTE;
    }

    /**
     * Diz se o status é uma falha do servidor, de {@code 500} a {@code 599}.
     *
     * @param status o status
     * @return se é erro de servidor
     */
    public static boolean erroDeServidor(int status) {
        return status / 100 == CENTENA_DO_ERRO_DE_SERVIDOR;
    }
}
