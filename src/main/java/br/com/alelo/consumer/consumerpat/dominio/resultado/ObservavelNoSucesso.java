package br.com.alelo.consumer.consumerpat.dominio.resultado;

import java.util.function.Consumer;

/**
 * Papel de quem sabe olhar o valor de um sucesso sem alterar o resultado.
 *
 * <p>
 * É para o efeito que não muda o fluxo, como registrar um evento: a ação roda só se houve sucesso,
 * e o resultado que sai é igual ao que entrou. Numa falha a ação não executa e o erro segue
 * adiante. Quem precisa transformar o valor usa {@link Transformavel#mapear}, e quem precisa de uma
 * segunda operação que também pode falhar usa {@link Encadeavel#encadear}.
 *
 * <p>
 * É escrita só em cima de {@link #resolver}, a operação-base. Por isso as variantes não a
 * implementam: a observação vive inteira neste arquivo.
 *
 * @param <T> tipo do valor, quando há sucesso
 * @param <E> tipo do erro, quando há falha
 */
public interface ObservavelNoSucesso<T, E> extends Resolvivel<T, E> {

    /**
     * Executa a ação com o valor, se houver, e devolve um resultado igual ao original.
     *
     * @param acao efeito aplicado só ao valor de um sucesso
     * @return um sucesso com o mesmo valor, ou a mesma falha
     */
    default Resultado<T, E> aoSucesso(Consumer<? super T> acao) {
        return this.<Resultado<T, E>>resolver(valor -> {
            acao.accept(valor);
            return new Sucesso<>(valor);
        }, erro -> new Falha<>(erro));
    }
}
