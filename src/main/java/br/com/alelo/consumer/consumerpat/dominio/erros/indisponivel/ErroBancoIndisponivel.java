package br.com.alelo.consumer.consumerpat.dominio.erros.indisponivel;

/**
 * O banco de dados não respondeu: não deu uma conexão, ou a conexão não passou na validação.
 *
 * <p>
 * Os dois campos existem para o log de quem produz o erro, e não para o cliente: dizem o tipo da
 * falha e o estado SQL, que identificam a causa sem citar host, URL de conexão, versão do banco nem
 * a mensagem da exceção. A mensagem é a mesma qualquer que seja a causa, e por isso é segura para
 * um corpo público.
 *
 * @param tipoDaFalha o nome da classe da exceção, ou um nome estável se não houve exceção
 * @param estadoSql   o estado SQL da falha, ou {@code desconhecido}
 */
public record ErroBancoIndisponivel(String tipoDaFalha, String estadoSql)
        implements ErroIndisponivel {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-BAN-SIS-001";

    /** O estado SQL de quando o driver não informa nenhum, ou de quando não houve exceção. */
    public static final String SEM_ESTADO_SQL = "desconhecido";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "O banco de dados não está respondendo.";
    }
}
