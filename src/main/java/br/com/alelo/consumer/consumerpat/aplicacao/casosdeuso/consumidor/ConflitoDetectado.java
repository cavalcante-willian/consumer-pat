package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;

import java.util.Optional;

import static br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog.campo;

/**
 * Registra, em aviso, que um pedido colidiu com o que já existe: o evento
 * {@code conflito.detectado}.
 *
 * <p>
 * Só registra o erro de conflito: um consumidor ausente ou um dado inválido não é conflito, e passa
 * sem registro. O evento leva a operação e o código do erro, que identifica a regra violada, e
 * nunca o valor que colidiu: o documento e o número do cartão não vão ao log. O conflito de
 * persistência, o que o banco recusa na corrida entre duas gravações, é registrado à parte, pelo
 * tratador, com o nome da restrição.
 */
public final class ConflitoDetectado {

    private ConflitoDetectado() {
    }

    /**
     * Registra o conflito, se o erro for de conflito.
     *
     * @param saida    a porta de log
     * @param operacao a operação que colidiu, como {@code criar-consumidor}
     * @param erro     o erro que o domínio devolveu
     */
    public static void registrar(SaidaDeLogPorta saida, String operacao, ErroDominio erro) {
        Optional.of(erro).filter(devolvido -> devolvido.categoria() == CategoriaErro.CONFLITO)
                .ifPresent(conflito -> saida.aviso("conflito.detectado",
                        campo("operacao", operacao), campo("codigo", conflito.codigo())));
    }
}
