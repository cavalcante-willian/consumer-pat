package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.portas.CartaoPorta;

import java.util.Arrays;

/**
 * A porta do cartão em memória, sem banco e sem trava.
 *
 * <p>
 * Só guarda a memória e declara os papéis: a busca, a gravação e as duas consultas de teste são
 * cada uma o seu arquivo. Obedece ao contrato da porta real, e os cartões iniciais não contam como
 * gravações.
 */
public class CartaoPortaFake implements CartaoPorta, BuscaDeCartaoEmMemoria,
        GravacaoDeCartaoEmMemoria, ConsultaDoEstadoDoCartao, ConsultaDasGravacoesDeCartao {

    private final CartoesEmMemoria memoria = new CartoesEmMemoria();

    /**
     * Cria o fake com os cartões que já existiam.
     *
     * @param iniciais os cartões presentes antes do teste
     */
    public CartaoPortaFake(Cartao... iniciais) {
        Arrays.stream(iniciais).forEach(cartao -> memoria.cartoes()
                .put(new ChaveDeCartao(cartao.numero(), cartao.tipo()), cartao));
    }

    @Override
    public CartoesEmMemoria memoria() {
        return memoria;
    }
}
