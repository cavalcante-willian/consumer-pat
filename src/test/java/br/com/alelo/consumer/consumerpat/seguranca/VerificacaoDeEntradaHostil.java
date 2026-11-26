package br.com.alelo.consumer.consumerpat.seguranca;

import br.com.alelo.consumer.consumerpat.medicao.RespostaHttp;

import java.util.ArrayList;
import java.util.List;

/**
 * Entrada hostil nunca vira SQL.
 *
 * <p>
 * Não há login a burlar, e por isso a pergunta é outra: o que um cliente manda chega a uma consulta
 * como dado, ou como pedaço dela? Cada carga vai ao nome do consumidor, ao número do cartão numa
 * compra e à ordenação e à direção da listagem. A ordenação é o ponto de risco de verdade, porque
 * {@code ORDER BY} não aceita parâmetro ligado, e é onde uma concatenação futura nasceria. No H2,
 * uma injeção chega a executar código, por {@code CREATE ALIAS}.
 *
 * <p>
 * Passa se nenhuma tentativa derruba o sistema ou vaza texto de banco, e se a listagem segue
 * respondendo depois. É uma verificação por tentativa, e não uma prova de ausência.
 */
public final class VerificacaoDeEntradaHostil implements Verificacao {

    private static final List<String> CARGAS = List.of("Robert'); DROP TABLE CONSUMER;--",
            "' OR '1'='1", "'; SHUTDOWN; --", "1 UNION SELECT NULL--",
            "'; CREATE ALIAS EVIL AS 'void e() {}';--");

    @Override
    public String chave() {
        return "entrada-hostil-nao-vira-sql";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var respostas = new ArrayList<RespostaHttp>();
        for (var carga : CARGAS) {
            respostas.add(sonda.cadastrarComNome(carga));
            respostas.add(sonda.comprarComNumeroDeCartao(carga));
            respostas.add(sonda.listar(carga, carga));
        }
        var depois = sonda.listar("nome", "asc");
        var falhas = respostas.stream().filter(resposta -> Status.erroDeServidor(resposta.status()))
                .count();
        var marcadores = respostas.stream()
                .flatMap(resposta -> MarcadoresDeVazamento.achadosEm(resposta.texto()).stream())
                .distinct().toList();
        return new Achado(chave(),
                "aspas, comentário, UNION, SHUTDOWN e CREATE ALIAS no nome, no "
                        + "número do cartão e na ordenação",
                falhas == 0 && marcadores.isEmpty() && depois.status() == 200,
                respostas.size() + " tentativas, " + falhas + " falhas de servidor, marcadores: "
                        + marcadores + ", listagem depois: " + depois.status());
    }
}
