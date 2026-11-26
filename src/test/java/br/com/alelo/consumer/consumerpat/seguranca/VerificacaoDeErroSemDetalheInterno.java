package br.com.alelo.consumer.consumerpat.seguranca;

import java.util.stream.Stream;

/**
 * O corpo de uma resposta de erro não mostra nome de classe, pilha de chamadas nem texto de banco.
 *
 * <p>
 * Olha duas respostas de erro: a de um JSON truncado e a de um crédito num cartão que não existe.
 * Uma resposta de erro diz o que o cliente fez de errado, e não como o servidor é por dentro.
 */
public final class VerificacaoDeErroSemDetalheInterno implements Verificacao {

    @Override
    public String chave() {
        return "erro-sem-detalhe-interno";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var truncado = sonda.cadastrarComJsonMalformado();
        var inexistente = sonda.creditarCartaoInexistente();
        var marcadores = Stream.of(truncado, inexistente)
                .flatMap(resposta -> MarcadoresDeVazamento.achadosEm(resposta.texto()).stream())
                .distinct().toList();
        return new Achado(chave(), "corpo dos erros de JSON truncado e de cartão inexistente",
                marcadores.isEmpty(), "status " + truncado.status() + " e " + inexistente.status()
                        + ", marcadores: " + marcadores);
    }
}
