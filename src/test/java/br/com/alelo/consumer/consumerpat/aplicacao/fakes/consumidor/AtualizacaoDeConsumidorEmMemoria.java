package br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroVersaoDivergente;
import br.com.alelo.consumer.consumerpat.dominio.portas.AtualizadorDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Optional;

/**
 * Grava a atualização em memória, com o contrato da porta real: só os dados cadastrais mudam, os
 * cartões são os que já estão gravados, a versão que não é a atual é recusada, e a gravação avança
 * a versão.
 *
 * <p>
 * A conferência da versão é própria do fake, e não a do caso de uso: o fake faz o papel da versão
 * de linha do banco, que pega o que o caso de uso deixa passar, e um teste só prova alguma coisa se
 * as duas conferências forem independentes.
 */
public interface AtualizacaoDeConsumidorEmMemoria
        extends BuscaDeConsumidorEmMemoria, AtualizadorDeConsumidor {

    @Override
    default Resultado<Consumidor, ErroDominio> atualizar(Consumidor consumidor) {
        return buscarPorId(consumidor.id())
                .encadear(gravado -> confirmarVersao(gravado, consumidor.versao()))
                .mapear(gravado -> gravarDados(gravado, consumidor));
    }

    /**
     * Confere a versão como a versão de linha do banco: igual à gravada, ou conflito.
     *
     * @param gravado         o consumidor como está
     * @param versaoInformada a versão que veio na gravação
     * @return o consumidor gravado, ou {@link ErroVersaoDivergente}
     */
    default Resultado<Consumidor, ErroDominio> confirmarVersao(Consumidor gravado,
            long versaoInformada) {
        return AusenciaComoErro.converter(
                Optional.of(gravado).filter(atual -> atual.versao() == versaoInformada),
                () -> new ErroVersaoDivergente(gravado.id(), versaoInformada));
    }

    /**
     * Troca só os dados do consumidor gravado, mantém os cartões dele e avança a versão.
     *
     * @param gravado o consumidor como está
     * @param enviado o consumidor que veio na gravação, de quem só os dados valem
     * @return o consumidor como ficou
     */
    default Consumidor gravarDados(Consumidor gravado, Consumidor enviado) {
        var atualizado = new Consumidor(gravado.id(), enviado.dados(), gravado.cartoes(),
                gravado.versao() + 1);
        memoria().consumidores().put(atualizado.id(), atualizado);
        memoria().gravacoes().add(atualizado);
        return atualizado;
    }
}
