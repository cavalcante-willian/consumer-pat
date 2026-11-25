package br.com.alelo.consumer.consumerpat.api.consumidor.criar;

import br.com.alelo.consumer.consumerpat.api.consumidor.CamposDosCartoes;
import br.com.alelo.consumer.consumerpat.api.consumidor.CamposDosDados;
import br.com.alelo.consumer.consumerpat.api.consumidor.CartoesConfirmados;
import br.com.alelo.consumer.consumerpat.api.consumidor.DadosConfirmados;
import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoNovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ValidacoesReunidas;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;

import java.util.stream.Stream;

/**
 * Converte o corpo do cadastro no pedido do caso de uso, com todos os erros de uma vez.
 *
 * <p>
 * Os dados e os cartões são lidos juntos e as falhas de todos os campos saem num só erro: o cliente
 * corrige o cadastro inteiro numa tentativa.
 */
public final class NovoConsumidorDaMensagem {

    private NovoConsumidorDaMensagem() {
    }

    /**
     * Converte o corpo.
     *
     * @param mensagem o corpo do cadastro
     * @return o pedido do caso de uso, ou um erro de validação com todos os campos inválidos
     */
    public static Resultado<DtoNovoConsumidor, ErroDominio> converter(
            MensagemCriarConsumidor mensagem) {
        var campos = Stream.concat(CamposDosDados.de(mensagem.dados()).stream(),
                CamposDosCartoes.de(mensagem.numerosCartoes()).stream()).toList();
        return ValidacoesReunidas.reunir(campos)
                .<Resultado<DtoNovoConsumidor, ErroDominio>>map(Falha::new)
                .orElseGet(() -> new Sucesso<>(
                        new DtoNovoConsumidor(DadosConfirmados.montar(mensagem.dados()),
                                CartoesConfirmados.montar(mensagem.numerosCartoes()))));
    }
}
