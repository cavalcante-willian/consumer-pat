package br.com.alelo.consumer.consumerpat.seguranca;

import java.util.Locale;

/**
 * Um {@code TRACE} não devolve o eco da requisição.
 *
 * <p>
 * O método existe para diagnóstico, e o eco leva de volta ao cliente os cabeçalhos que ele mandou,
 * inclusive cookie e autorização, o que abre o caminho do cross-site tracing. Aqui não há cookie
 * nem autorização, e por isso a gravidade é baixa, mas o eco não tem utilidade nenhuma numa API.
 */
public final class VerificacaoDeTrace implements Verificacao {

    @Override
    public String chave() {
        return "trace-sem-eco";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var resposta = sonda.trace();
        var eco = resposta.texto().toLowerCase(Locale.ROOT).contains("host:");
        return new Achado(chave(), "TRACE numa rota da aplicação", !eco,
                "status " + resposta.status() + ", eco dos cabeçalhos da requisição: " + eco);
    }
}
