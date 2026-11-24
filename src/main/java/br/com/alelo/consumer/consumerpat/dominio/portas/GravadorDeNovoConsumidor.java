package br.com.alelo.consumer.consumerpat.dominio.portas;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.NovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroDocumentoJaCadastrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroNumeroDeCartaoJaCadastrado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

/**
 * Papel de quem sabe gravar um consumidor que ainda não existe, com os cartões dele.
 *
 * <p>
 * A unicidade do documento e a do número de cartão são garantidas pelo banco, que é o único que
 * enxerga duas gravações ao mesmo tempo. A colisão volta como erro de domínio, e não como exceção
 * da tecnologia.
 */
public interface GravadorDeNovoConsumidor {

    /**
     * Grava o consumidor e os cartões numa só operação.
     *
     * @param novo o consumidor a gravar
     * @return o consumidor gravado, com id e versão, ou {@link ErroDocumentoJaCadastrado} ou
     *         {@link ErroNumeroDeCartaoJaCadastrado} se algum dos dois já existe
     */
    Resultado<Consumidor, ErroDominio> criar(NovoConsumidor novo);
}
