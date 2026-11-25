package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

/**
 * O resultado da leitura de um campo, com o nome do campo a que ele pertence.
 *
 * <p>
 * O nome é dado por quem lê, e não o que o erro de domínio carrega: um número de cartão dentro de
 * um mapa de cartões é o campo {@code numerosCartoes}, e não {@code numeroCartao}.
 *
 * @param campo     o nome do campo na entrada
 * @param resultado o que a leitura devolveu
 */
public record CampoValidado(String campo, Resultado<?, ErroDominio> resultado) {
}
