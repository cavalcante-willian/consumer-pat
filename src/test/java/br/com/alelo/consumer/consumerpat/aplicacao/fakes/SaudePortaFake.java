package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.saude.DependenciaCritica;
import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeDependencia;
import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeSaude;
import br.com.alelo.consumer.consumerpat.dominio.saude.SaudePorta;

import java.util.Map;

/**
 * A porta de saúde que o teste controla: força o banco disponível ou indisponível.
 *
 * <p>
 * É assim que o contrato do {@code /api/v1/saude} é provado nos dois estados, sem derrubar um banco
 * de verdade. Começa com o banco disponível.
 */
public class SaudePortaFake implements SaudePorta {

    private volatile EstadoDeDependencia banco = EstadoDeDependencia.DISPONIVEL;

    /**
     * Força o estado do banco nas próximas consultas.
     *
     * @param estado o estado que o banco passa a ter
     */
    public void banco(EstadoDeDependencia estado) {
        this.banco = estado;
    }

    @Override
    public EstadoDeSaude consultar() {
        return new EstadoDeSaude(Map.of(DependenciaCritica.BANCO, banco));
    }
}
