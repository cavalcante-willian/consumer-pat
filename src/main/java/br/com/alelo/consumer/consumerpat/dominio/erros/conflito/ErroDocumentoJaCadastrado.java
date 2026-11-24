package br.com.alelo.consumer.consumerpat.dominio.erros.conflito;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.documento.Documento;

import java.util.List;
import java.util.Map;

/**
 * Já existe um consumidor com o documento informado.
 *
 * <p>
 * O documento vem como {@link Documento}, cujo texto já é mascarado, e a mensagem não o mostra por
 * inteiro nem confirma a quem pergunta de quem é.
 *
 * @param documento o documento que colidiu
 */
public record ErroDocumentoJaCadastrado(Documento documento) implements ErroConflito {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-CNF-CON-001";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Já existe um consumidor cadastrado com o documento " + documento + ".";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("documento", List.of("já está cadastrado"));
    }
}
