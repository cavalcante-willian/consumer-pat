package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar;

import br.com.alelo.consumer.consumerpat.dominio.portas.GravadorDeNovoConsumidor;

/**
 * Operação-base do papel de cadastrar: expõe quem grava o consumidor novo.
 *
 * <p>
 * O caso de uso guarda a porta e a entrega por aqui, e a operação é escrita em cima dela, em outro
 * arquivo.
 */
public interface ComGravadorDeNovoConsumidor {

    /**
     * Quem grava o consumidor novo.
     *
     * @return a porta de gravação
     */
    GravadorDeNovoConsumidor gravador();
}
