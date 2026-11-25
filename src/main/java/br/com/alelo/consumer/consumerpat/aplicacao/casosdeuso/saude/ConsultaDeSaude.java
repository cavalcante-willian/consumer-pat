package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.saude;

import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeSaude;

/**
 * Papel de consultar a saúde do sistema.
 *
 * <p>
 * Só entrega a consulta à porta: quem sabe a causa de uma dependência fora do ar é o adaptador, e é
 * ele quem a registra. Não há transação, porque a verificação não toca nenhuma tabela. Não devolve
 * {@code Resultado}: uma dependência fora do ar é o próprio dado que a consulta devolve, e não uma
 * falha dela.
 */
public interface ConsultaDeSaude extends ComConsultorDeSaude {

    /**
     * Consulta o estado de cada dependência crítica.
     *
     * @return o estado, com a situação geral derivada dele
     */
    default EstadoDeSaude consultar() {
        return saude().consultar();
    }
}
