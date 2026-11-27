package br.com.alelo.consumer.consumerpat.api.configuracao;

import org.springframework.core.Ordered;

/**
 * A ordem dos filtros da requisição, num lugar só, porque ela é parte do contrato.
 *
 * <p>
 * Menor número é mais externo. Os cabeçalhos de segurança vêm primeiro, para proteger toda
 * resposta, inclusive as de erro. Depois vem a correlação, e só então o log de conclusão, que logo
 * dentro dela ainda escreve com o id da requisição. Por último vem a recusa do {@code TRACE}, para
 * o método recusado também sair no log. Cada filtro é registrado na sua própria configuração, e
 * todas leem a sua posição daqui: trocar a ordem é mudar este arquivo, e é mudança revisada.
 */
public final class OrdemDosFiltros {

    /** Os cabeçalhos de segurança, a etapa 1, o filtro mais externo. */
    public static final int CABECALHOS_DE_SEGURANCA = Ordered.HIGHEST_PRECEDENCE;

    /** A correlação, a etapa 2. */
    public static final int CORRELACAO = Ordered.HIGHEST_PRECEDENCE + 1;

    /** O log de conclusão, a etapa 3. */
    public static final int LOG_DE_CONCLUSAO = Ordered.HIGHEST_PRECEDENCE + 2;

    /** A recusa do {@code TRACE}, a etapa 4. */
    public static final int REJEICAO_DE_TRACE = Ordered.HIGHEST_PRECEDENCE + 3;

    private OrdemDosFiltros() {
    }
}
