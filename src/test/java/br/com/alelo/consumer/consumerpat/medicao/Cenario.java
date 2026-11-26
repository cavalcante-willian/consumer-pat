package br.com.alelo.consumer.consumerpat.medicao;

import java.util.List;

/**
 * Uma situação que se mede num sistema: o cadastro de toda a base, a listagem, a compra, a disputa.
 *
 * <p>
 * O cenário só pede ao sistema pelas {@link OperacoesDoAlvo}, e por isso o mesmo código mede o
 * legado e o novo. O que ele devolve são medidas, uma por chamada, que o relatório junta.
 */
public interface Cenario {

    /**
     * Mede o cenário num sistema.
     *
     * @param alvo o sistema
     * @return as medidas, uma por chamada
     * @throws Exception se uma chamada falha
     */
    List<Medida> medir(OperacoesDoAlvo alvo) throws Exception;
}
