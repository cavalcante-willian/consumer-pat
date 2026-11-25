package br.com.alelo.consumer.consumerpat.infraestrutura.saude;

import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;
import br.com.alelo.consumer.consumerpat.dominio.saude.DependenciaCritica;
import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeSaude;
import br.com.alelo.consumer.consumerpat.dominio.saude.SaudePorta;

import javax.sql.DataSource;

import java.util.Map;

/**
 * Cumpre a porta de saúde sobre o pool de conexões do banco.
 *
 * <p>
 * Só compõe as peças: verifica a conexão, registra se o banco não respondeu e monta o estado. O
 * prazo da validação é curto, de {@value #PRAZO_EM_SEGUNDOS} segundos: o health é chamado a toda
 * hora por quem o monitora, e não pode ficar pendurado. O prazo da espera por uma conexão do pool,
 * porém, é o do próprio pool ({@code connection-timeout}), que o health não muda por chamada. Nada
 * interno vai ao estado: nem a URL, nem o host, nem a versão, nem a mensagem de uma exceção.
 */
public class SaudeDoBancoAdaptador implements SaudePorta {

    /** O prazo da validação da conexão. */
    public static final int PRAZO_EM_SEGUNDOS = 2;

    private final DataSource fonte;

    private final SaidaDeLogPorta saida;

    SaudeDoBancoAdaptador(DataSource fonte, SaidaDeLogPorta saida) {
        this.fonte = fonte;
        this.saida = saida;
    }

    @Override
    public EstadoDeSaude consultar() {
        return new EstadoDeSaude(Map.of(DependenciaCritica.BANCO,
                VerificacaoDaConexao.verificar(fonte, PRAZO_EM_SEGUNDOS).resolver(
                        disponivel -> disponivel, indisponivel -> RegistroDaIndisponibilidade
                                .registrar(saida, indisponivel))));
    }
}
