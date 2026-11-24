package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;

import java.util.ArrayList;
import java.util.List;

/**
 * A porta do extrato em memória.
 *
 * <p>
 * Só guarda a lista e declara os papéis: o registro e a consulta de teste são cada um o seu
 * arquivo.
 */
public class ExtratoPortaFake
        implements RegistroDeExtratoEmMemoria, ConsultaDosExtratosRegistrados {

    private final List<Extrato> extratos = new ArrayList<>();

    @Override
    public List<Extrato> extratosEmMemoria() {
        return extratos;
    }
}
