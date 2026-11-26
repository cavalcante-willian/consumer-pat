package br.com.alelo.consumer.consumerpat.seguranca;

import java.util.List;

/**
 * A resposta traz os cinco cabeçalhos de segurança do contrato: a que impede a inferência de tipo,
 * o quadro, a política de conteúdo, o {@code Referer} e o cache.
 */
public final class VerificacaoDeCabecalhos implements Verificacao {

    private static final List<String> EXIGIDOS = List.of("X-Content-Type-Options",
            "X-Frame-Options", "Content-Security-Policy", "Referrer-Policy", "Cache-Control");

    @Override
    public String chave() {
        return "cabecalhos-de-seguranca";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var resposta = sonda.requisicaoValida();
        var faltam = EXIGIDOS.stream().filter(nome -> resposta.cabecalho(nome).isEmpty()).toList();
        return new Achado(chave(), "os cinco cabeçalhos de segurança numa resposta válida",
                faltam.isEmpty(), "status " + resposta.status() + ", faltam: " + faltam);
    }
}
