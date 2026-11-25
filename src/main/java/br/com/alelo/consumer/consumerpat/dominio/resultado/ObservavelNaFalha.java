package br.com.alelo.consumer.consumerpat.dominio.resultado;

import java.util.function.Consumer;

/**
 * Papel de quem sabe olhar o erro de uma falha sem alterar o resultado.
 *
 * <p>
 * É para o efeito que não muda o fluxo, como registrar uma recusa: a ação roda só se houve falha, e
 * o resultado que sai é igual ao que entrou. Num sucesso a ação não executa e o valor segue
 * adiante. A falha continua sendo um valor devolvido, e não uma exceção: observá-la não a trata nem
 * a esconde.
 *
 * <p>
 * É escrita só em cima de {@link #resolver}, a operação-base. Por isso as variantes não a
 * implementam: a observação vive inteira neste arquivo.
 *
 * @param <T> tipo do valor, quando há sucesso
 * @param <E> tipo do erro, quando há falha
 */
public interface ObservavelNaFalha<T, E> extends Resolvivel<T, E> {

    /**
     * Executa a ação com o erro, se houver, e devolve um resultado igual ao original.
     *
     * @param acao efeito aplicado só ao erro de uma falha
     * @return um sucesso com o mesmo valor, ou uma falha com o mesmo erro
     */
    default Resultado<T, E> aoFalhar(Consumer<? super E> acao) {
        return this.<Resultado<T, E>>resolver(valor -> new Sucesso<>(valor), erro -> {
            acao.accept(erro);
            return new Falha<>(erro);
        });
    }
}
