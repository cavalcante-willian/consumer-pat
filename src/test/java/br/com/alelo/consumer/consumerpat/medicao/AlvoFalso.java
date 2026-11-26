package br.com.alelo.consumer.consumerpat.medicao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Um sistema de mentira para provar os cenários sem subir nada: responde sempre com sucesso, conta
 * as chamadas que recebeu e devolve o saldo da disputa que o teste escolher.
 */
final class AlvoFalso implements OperacoesDoAlvo {

    private final String nome;

    private final BigDecimal saldoDaDisputa;

    private final AtomicInteger cadastros = new AtomicInteger();

    private final AtomicInteger comprasNaFarmacia = new AtomicInteger();

    AlvoFalso(String nome, String saldoDaDisputa) {
        this.nome = nome;
        this.saldoDaDisputa = new BigDecimal(saldoDaDisputa);
    }

    static RespostaHttp resposta(int status, int bytes) {
        return new RespostaHttp(status, new byte[bytes], Optional.empty());
    }

    int cadastros() {
        return cadastros.get();
    }

    int comprasNaFarmacia() {
        return comprasNaFarmacia.get();
    }

    @Override
    public String nome() {
        return nome;
    }

    @Override
    public RespostaHttp cadastrar(int indice) {
        cadastros.incrementAndGet();
        return resposta(201, 0);
    }

    @Override
    public List<Pedido> listagens(int total) {
        return List.of(new Pedido("listagem-primeira-pagina", "listagem falsa",
                () -> resposta(200, 1_234)));
    }

    @Override
    public List<Pedido> consultas(int total) {
        return List.of(new Pedido("consulta-por-id", "consulta falsa", () -> resposta(200, 50)));
    }

    @Override
    public RespostaHttp creditar(TipoCartao tipo, int indice, String valor) {
        return resposta(201, 0);
    }

    @Override
    public RespostaHttp comprar(TipoCartao tipo, int indice, String valor) {
        return resposta(201, 0);
    }

    @Override
    public CartaoDeDisputa prepararFarmaciaComSaldo(String saldo) {
        return new CartaoDeDisputa("1", "1");
    }

    @Override
    public int comprarNaFarmacia(CartaoDeDisputa cartao, String valor) {
        comprasNaFarmacia.incrementAndGet();
        return 201;
    }

    @Override
    public BigDecimal saldoDe(CartaoDeDisputa cartao) {
        return saldoDaDisputa;
    }
}
