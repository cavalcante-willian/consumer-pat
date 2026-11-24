/**
 * DTOs de transferência entre a API e os casos de uso.
 *
 * <p>
 * Os de entrada trazem textos e inteiros crus, como chegaram depois da validação de formato da
 * borda: é o caso de uso que os converte nos tipos do domínio, e por isso a falha de uma conversão
 * vira o mesmo erro de validação de qualquer campo. Os de saída trazem tipos do domínio, e a API
 * decide como apresentá-los. Nunca são o contrato REST: a API tem os seus próprios tipos.
 */
package br.com.alelo.consumer.consumerpat.aplicacao.dtos;
