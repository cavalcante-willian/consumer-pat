package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.obter;

import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.ComBuscadorDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import org.springframework.transaction.annotation.Transactional;

/**
 * Papel de ler um consumidor pelo id, com os cartões e a versão.
 *
 * <p>
 * É a leitura que o cliente faz antes de atualizar, e a versão que ela devolve é a que a
 * atualização exige de volta. A transação é só de leitura.
 */
public interface LeituraDeConsumidor extends ComBuscadorDeConsumidor {

    /**
     * Busca o consumidor.
     *
     * @param id o id procurado
     * @return o consumidor, ou {@code ErroConsumidorNaoEncontrado}
     */
    @Transactional(readOnly = true)
    default Resultado<Consumidor, ErroDominio> obter(ConsumidorId id) {
        return buscador().buscarPorId(id);
    }
}
