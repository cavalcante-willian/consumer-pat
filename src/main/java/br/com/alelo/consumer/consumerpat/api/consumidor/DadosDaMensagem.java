package br.com.alelo.consumer.consumerpat.api.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.DadosDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ValidacoesReunidas;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;

/**
 * Converte os dados cadastrais da mensagem nos tipos do domínio, com todos os erros de uma vez.
 *
 * <p>
 * Os campos são lidos um a um, e as falhas são reunidas num só erro de validação, de modo que o
 * cliente corrige o cadastro inteiro numa tentativa, e não um campo por tentativa.
 */
public final class DadosDaMensagem {

    private DadosDaMensagem() {
    }

    /**
     * Converte os dados.
     *
     * @param dados os dados como chegaram
     * @return os dados do domínio, ou um erro de validação com todos os campos inválidos
     */
    public static Resultado<DadosDoConsumidor, ErroDominio> converter(
            MensagemDadosConsumidor dados) {
        return ValidacoesReunidas.reunir(CamposDosDados.de(dados))
                .<Resultado<DadosDoConsumidor, ErroDominio>>map(Falha::new)
                .orElseGet(() -> new Sucesso<>(DadosConfirmados.montar(dados)));
    }
}
