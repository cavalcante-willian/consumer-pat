package br.com.alelo.consumer.consumerpat.infraestrutura.saude;

import br.com.alelo.consumer.consumerpat.dominio.erros.indisponivel.ErroBancoIndisponivel;
import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;
import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeDependencia;

import static br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog.campo;

/**
 * Registra que o banco não respondeu, no nível crítico.
 *
 * <p>
 * É erro que afeta o sistema todo, e quem opera precisa ver. O evento leva o tipo e o estado SQL da
 * falha, e nunca a mensagem da exceção. É aqui, no adaptador, que a causa existe, e por isso é aqui
 * que ela é registrada.
 */
final class RegistroDaIndisponibilidade {

    private RegistroDaIndisponibilidade() {
    }

    /**
     * Registra a indisponibilidade.
     *
     * @param saida a porta de log
     * @param erro  o erro que a verificação devolveu
     * @return {@link EstadoDeDependencia#INDISPONIVEL}, o estado que o erro representa
     */
    static EstadoDeDependencia registrar(SaidaDeLogPorta saida, ErroBancoIndisponivel erro) {
        saida.critico("banco.indisponivel", campo("operacao", "saude"),
                campo("tipoDaFalha", erro.tipoDaFalha()), campo("estadoSql", erro.estadoSql()));
        return EstadoDeDependencia.INDISPONIVEL;
    }
}
