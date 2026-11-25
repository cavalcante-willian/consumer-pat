package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.saude;

import br.com.alelo.consumer.consumerpat.dominio.saude.SaudePorta;

/**
 * O caso de uso de consultar a saúde: só guarda a porta e declara o papel.
 *
 * <p>
 * A operação está em {@link ConsultaDeSaude}, em cima de {@link #saude()}.
 */
public class ConsultarSaudeCaso implements ConsultaDeSaude {

    private final SaudePorta saude;

    /**
     * Cria o caso de uso.
     *
     * @param saude quem diz o estado de cada dependência
     */
    public ConsultarSaudeCaso(SaudePorta saude) {
        this.saude = saude;
    }

    @Override
    public SaudePorta saude() {
        return saude;
    }
}
