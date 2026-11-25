package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroVersaoDivergente;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidorEntidade;

import java.util.Optional;

/**
 * Exige que a versão que o cliente leu seja a da linha que está no banco.
 *
 * <p>
 * É a conferência da porta, com o contrato que o domínio declarou, e vale para o caso comum de uma
 * leitura velha. A corrida entre dois clientes que leem a mesma versão ao mesmo tempo passa por
 * aqui, e quem a pega é a versão de linha do Hibernate, na descarga: o {@code UPDATE} só atinge a
 * linha se a versão ainda é a que foi lida.
 */
final class VersaoDaLinhaConfirmada {

    private VersaoDaLinhaConfirmada() {
    }

    /**
     * Confere a versão do consumidor recebido contra a da linha.
     *
     * @param entidade   a linha como está no banco
     * @param consumidor o consumidor com a versão que o cliente leu
     * @return a mesma linha, ou {@link ErroVersaoDivergente}
     */
    static Resultado<ConsumidorEntidade, ErroDominio> exigir(ConsumidorEntidade entidade,
            Consumidor consumidor) {
        return AusenciaComoErro.converter(
                Optional.of(entidade).filter(linha -> linha.getVersao() == consumidor.versao()),
                () -> new ErroVersaoDivergente(consumidor.id(), consumidor.versao()));
    }
}
