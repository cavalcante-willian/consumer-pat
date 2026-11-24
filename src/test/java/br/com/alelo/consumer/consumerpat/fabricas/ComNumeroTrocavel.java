package br.com.alelo.consumer.consumerpat.fabricas;

/**
 * Papel de um modelo de cartão cujo número o teste pode trocar.
 */
public interface ComNumeroTrocavel extends ComDadosDeCartao {

    /**
     * Troca o número.
     *
     * @param numero o número, só dígitos
     * @return um novo modelo com o número informado; este não é alterado
     */
    default ModeloDeCartao comNumero(String numero) {
        return new ModeloDeCartao(tipo(), numero, saldo());
    }
}
