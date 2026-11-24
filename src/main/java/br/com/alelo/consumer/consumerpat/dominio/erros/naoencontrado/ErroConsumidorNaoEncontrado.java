package br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;

/**
 * Não existe consumidor com o id informado.
 *
 * @param id o id procurado
 */
public record ErroConsumidorNaoEncontrado(ConsumidorId id) implements ErroNaoEncontrado {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-NEN-CON-001";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Não existe consumidor com o id " + id.valor() + ".";
    }
}
