package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * O tratador de erros da API: toda exceção vira o corpo de erro padronizado.
 *
 * <p>
 * Só declara os papéis: cada tipo de exceção tem o seu, em arquivo próprio, e o framework escolhe
 * sempre o mais específico. Sem isto, o cliente receberia o JSON padrão do Spring, e dois endpoints
 * diriam a mesma falha de dois jeitos.
 */
@RestControllerAdvice
public class FiltroDeProblemaDeApi implements TraducaoDeEntradaInvalida, TraducaoDeCorpoIlegivel,
        TraducaoDeRotaInexistente, TraducaoDeMetodoNaoSuportado, TraducaoDeMidiaNaoSuportada,
        TraducaoDeTravaEsgotada, TraducaoDeConflitoDeVersao, TraducaoDeIntegridadeViolada,
        TraducaoDeArgumentoInvalido, TraducaoDeFalhaInesperada {
}
