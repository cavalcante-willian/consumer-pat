package br.com.alelo.consumer.consumerpat.api.comum.campos;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Traduz o resultado da validação do corpo nos campos com erro da resposta.
 *
 * <p>
 * Reúne todas as violações de uma vez, agrupadas pelo nome do campo do JSON, e não para na
 * primeira: parar obrigaria quem corrige a tentar campo por campo. As chaves saem ordenadas, e as
 * mensagens de cada campo também, porque o validador não garante ordem e duas respostas iguais
 * precisam dar os mesmos bytes. Só a mensagem da regra é usada: o valor rejeitado nunca sai, nem no
 * corpo nem no log, porque pode ser um número de cartão quase certo.
 */
public final class ErrosDeCampoDeValidacao {

    private ErrosDeCampoDeValidacao() {
    }

    /**
     * Traduz as violações.
     *
     * @param resultado o resultado da validação do corpo
     * @return o campo com erro e as suas mensagens, em ordem estável
     */
    public static Map<String, List<String>> de(BindingResult resultado) {
        return resultado.getFieldErrors().stream()
                .collect(Collectors.groupingBy(FieldError::getField, TreeMap::new,
                        Collectors.mapping(FieldError::getDefaultMessage,
                                Collectors.collectingAndThen(Collectors.toList(),
                                        mensagens -> mensagens.stream().sorted().toList()))));
    }
}
