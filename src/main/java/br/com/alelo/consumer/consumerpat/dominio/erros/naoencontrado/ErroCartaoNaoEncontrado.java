package br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;

/**
 * Não existe cartão com o número e o tipo informados.
 *
 * <p>
 * Um cartão de outro tipo com o mesmo número também conta como não encontrado: a compra só pode
 * debitar o cartão do tipo que corresponde ao estabelecimento, e reconhecer que o número existe com
 * outro tipo revelaria a quem pergunta quais cartões existem. O número vem como
 * {@link NumeroDeCartao}, cujo texto já é mascarado, de modo que a mensagem nunca o mostra por
 * inteiro.
 *
 * @param numero o número procurado
 * @param tipo   o tipo procurado
 */
public record ErroCartaoNaoEncontrado(NumeroDeCartao numero, TipoCartao tipo)
        implements ErroNaoEncontrado {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-NEN-CRT-001";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Não existe cartão " + tipo + " com o número " + numero + ".";
    }
}
