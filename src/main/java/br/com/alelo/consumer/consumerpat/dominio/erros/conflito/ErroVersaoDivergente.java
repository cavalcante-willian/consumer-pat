package br.com.alelo.consumer.consumerpat.dominio.erros.conflito;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;

import java.util.List;
import java.util.Map;

/**
 * O consumidor foi alterado depois da leitura que o cliente usou.
 *
 * <p>
 * É o que a versão da linha existe para dizer: quem atualiza reenvia a versão que leu, e se ela não
 * é mais a atual outro cliente escreveu no meio. O segundo não sobrescreve o primeiro em silêncio:
 * relê o consumidor e decide de novo.
 *
 * @param id              o consumidor que divergiu
 * @param versaoInformada a versão que o cliente enviou
 */
public record ErroVersaoDivergente(ConsumidorId id, long versaoInformada) implements ErroConflito {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-CNF-CON-002";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "O consumidor " + id.valor() + " foi alterado desde a versão " + versaoInformada
                + ". Releia o cadastro e tente de novo.";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("versao", List.of("não é mais a versão atual do consumidor"));
    }
}
