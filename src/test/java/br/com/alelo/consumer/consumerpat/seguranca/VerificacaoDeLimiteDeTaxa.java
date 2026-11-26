package br.com.alelo.consumer.consumerpat.seguranca;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Duzentas requisições seguidas, e o sistema limita a taxa em algum ponto, com {@code 429}.
 *
 * <p>
 * Nenhum dos dois sistemas limita, e isso é um risco aceito: limitar exige dependência nova ou um
 * proxy à frente, e está na tabela de riscos do {@code SEGURANCA.md}. A verificação existe para o
 * relatório dizer isso com um número, e para o dia em que alguém o limite. São duzentas requisições
 * de leitura, em série, e não uma inundação.
 */
public final class VerificacaoDeLimiteDeTaxa implements Verificacao {

    private static final int REQUISICOES = 200;

    private static final int EXCESSO_DE_REQUISICOES = 429;

    @Override
    public String chave() {
        return "limite-de-taxa";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var status = new ArrayList<Integer>();
        for (int i = 0; i < REQUISICOES; i++) {
            status.add(sonda.requisicaoValida().status());
        }
        var contagem = status.stream()
                .collect(Collectors.groupingBy(s -> s, TreeMap::new, Collectors.counting()));
        return new Achado(chave(), REQUISICOES + " requisições seguidas a uma rota de leitura",
                status.contains(EXCESSO_DE_REQUISICOES), "status " + contagem);
    }
}
