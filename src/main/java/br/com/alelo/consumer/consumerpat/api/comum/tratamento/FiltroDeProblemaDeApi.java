package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * O tratador de erros da API: toda exceção vira o corpo de erro padronizado.
 *
 * <p>
 * Só declara os papéis: cada tipo de exceção tem o seu, em arquivo próprio, e o framework escolhe
 * sempre o mais específico. Sem isto, o cliente receberia o JSON padrão do Spring, e dois endpoints
 * diriam a mesma falha de dois jeitos. Os papéis que registram o que traduzem usam a porta de log
 * que este tratador guarda.
 */
@RestControllerAdvice
public class FiltroDeProblemaDeApi implements TraducaoDeEntradaInvalida, TraducaoDeCorpoIlegivel,
        TraducaoDeRotaInexistente, TraducaoDeMetodoNaoSuportado, TraducaoDeMidiaNaoSuportada,
        TraducaoDeTravaEsgotada, TraducaoDeConflitoDeVersao, TraducaoDeIntegridadeViolada,
        TraducaoDeArgumentoInvalido, TraducaoDeFalhaInesperada {

    private final SaidaDeLogPorta saida;

    /**
     * Cria o tratador.
     *
     * @param saida a porta de log
     */
    public FiltroDeProblemaDeApi(SaidaDeLogPorta saida) {
        this.saida = saida;
    }

    @Override
    public SaidaDeLogPorta saida() {
        return saida;
    }
}
