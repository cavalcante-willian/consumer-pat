package br.com.alelo.consumer.consumerpat.dominio.erros;

/**
 * Contrato de todo erro de negócio: compõe os papéis {@link ComCategoria},
 * {@link ComCodigo}, {@link ComMensagem} e {@link ComCamposComErro}, cada um em
 * seu arquivo.
 *
 * <p>
 * Não herda de {@code Exception} de propósito: o erro é um valor que circula
 * dentro de um {@code Resultado}, sem pilha de chamadas e sem custo de
 * lançamento, e não algo que se captura com {@code catch}.
 *
 * <p>
 * A interface não é selada porque as variantes moram em subpacotes diferentes,
 * e uma interface selada exige que as permitidas estejam no mesmo pacote. O
 * fechamento acontece em cada categoria.
 */
public interface ErroDominio extends ComCategoria, ComCodigo, ComMensagem, ComCamposComErro {
}
