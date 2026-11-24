package br.com.alelo.consumer.consumerpat.dominio.portas;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroDocumentoJaCadastrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroVersaoDivergente;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroConsumidorNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

/**
 * Papel de quem sabe gravar a alteração dos dados de um consumidor que já existe.
 *
 * <p>
 * Só os dados cadastrais mudam: os cartões e o saldo que vêm no {@link Consumidor} são ignorados na
 * gravação, de modo que atualizar um cadastro com um saldo velho não desfaz uma compra feita no
 * meio. A versão do consumidor é a que o cliente leu, e a gravação a compara com a atual.
 */
public interface AtualizadorDeConsumidor {

    /**
     * Grava os dados do consumidor.
     *
     * @param consumidor o consumidor com os dados novos e a versão que o cliente leu
     * @return o consumidor como ficou, com a versão nova, ou {@link ErroConsumidorNaoEncontrado},
     *         {@link ErroVersaoDivergente} se outro cliente escreveu antes, ou
     *         {@link ErroDocumentoJaCadastrado} se o documento novo é de outro consumidor
     */
    Resultado<Consumidor, ErroDominio> atualizar(Consumidor consumidor);
}
