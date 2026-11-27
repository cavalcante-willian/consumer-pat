package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidorEntidade;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidoresDeTeste;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;

/**
 * Monta a linha de um cartão para os testes de persistência, ainda não gravada.
 *
 * <p>
 * Todo cartão tem dono. Quando o teste não se importa com quem, as sobrecargas com o gerenciador
 * gravam um consumidor novo e o devolvem como dono; quando importa, o teste grava o seu e o passa.
 */
public final class CartoesDeTeste {

    private CartoesDeTeste() {
    }

    /**
     * Um cartão do número padrão, de um consumidor novo gravado agora.
     *
     * @param gerenciador onde gravar o dono
     * @param tipo        o tipo do cartão
     * @param saldo       o saldo, como decimal com ponto
     * @return a linha do cartão, ainda não gravada
     */
    public static CartaoEntidade linha(TestEntityManager gerenciador, TipoCartao tipo,
            String saldo) {
        return linha(gerenciador, tipo, FabricaDeCartoes.NUMERO_PADRAO, saldo);
    }

    /**
     * Um cartão de um consumidor novo gravado agora.
     *
     * @param gerenciador onde gravar o dono
     * @param tipo        o tipo do cartão
     * @param numero      o número, só dígitos
     * @param saldo       o saldo, como decimal com ponto
     * @return a linha do cartão, ainda não gravada
     */
    public static CartaoEntidade linha(TestEntityManager gerenciador, TipoCartao tipo,
            String numero, String saldo) {
        return linha(gerenciador.persistAndFlush(ConsumidoresDeTeste.linha()), tipo, numero, saldo);
    }

    /**
     * Um cartão do número padrão, do consumidor informado.
     *
     * @param dono  o consumidor, já gravado
     * @param tipo  o tipo do cartão
     * @param saldo o saldo, como decimal com ponto
     * @return a linha do cartão, ainda não gravada
     */
    public static CartaoEntidade linha(ConsumidorEntidade dono, TipoCartao tipo, String saldo) {
        return linha(dono, tipo, FabricaDeCartoes.NUMERO_PADRAO, saldo);
    }

    /**
     * Um cartão do consumidor informado.
     *
     * @param dono   o consumidor, já gravado
     * @param tipo   o tipo do cartão
     * @param numero o número, só dígitos
     * @param saldo  o saldo, como decimal com ponto
     * @return a linha do cartão, ainda não gravada
     */
    public static CartaoEntidade linha(ConsumidorEntidade dono, TipoCartao tipo, String numero,
            String saldo) {
        var entidade = new CartaoEntidade();
        entidade.setConsumidor(dono);
        entidade.setTipo(tipo);
        entidade.setNumero(numero);
        entidade.setSaldo(new BigDecimal(saldo));
        return entidade;
    }

    /**
     * Lê a chave da linha do cartão. Os acessores da entidade são do pacote dela, e os testes de
     * outros pacotes passam por aqui.
     *
     * @param cartao a linha do cartão
     * @return a chave gerada pelo banco, ou nula se a linha ainda não foi gravada
     */
    public static Long idDe(CartaoEntidade cartao) {
        return cartao.getId();
    }
}
