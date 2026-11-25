package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * O acesso Spring Data à tabela {@code consumidores}.
 *
 * <p>
 * É público porque o adaptador está em outro pacote, mas nenhuma camada fora da persistência pode
 * importá-lo, e um teste impõe isso. As conferências de unicidade respondem só se existe, sem
 * carregar a linha, porque o adaptador as usa antes de gravar e não precisa do consumidor que
 * colide.
 */
public interface ConsumidorRepositorioJpa extends JpaRepository<ConsumidorEntidade, Long> {

    /**
     * Diz se algum consumidor tem o documento.
     *
     * @param documento os dígitos do documento
     * @return verdadeiro se já existe
     */
    @Query("select count(c) > 0 from ConsumidorEntidade c where c.documento = :documento")
    boolean existeComODocumento(@Param("documento") String documento);

    /**
     * Diz se algum consumidor, além do informado, tem o documento.
     *
     * @param documento os dígitos do documento
     * @param id        o consumidor que não conta
     * @return verdadeiro se outro consumidor já o tem
     */
    @Query("select count(c) > 0 from ConsumidorEntidade c "
            + "where c.documento = :documento and c.id <> :id")
    boolean existeOutroComODocumento(@Param("documento") String documento, @Param("id") Long id);

    /**
     * Lista uma página de resumos, com a ordenação do pedido.
     *
     * <p>
     * A página é pedida ao banco, com {@code LIMIT} e {@code OFFSET}, e a contagem do total é uma
     * segunda consulta. São duas consultas por listagem, e nunca uma por consumidor.
     *
     * @param pagina a página, a partir de 0, e a ordenação
     * @return os resumos da página, com o total
     */
    @Query(value = "select new br.com.alelo.consumer.consumerpat.infraestrutura.persistencia."
            + "consumidor.ResumoDeConsumidorLinha(c.id, c.nome) from ConsumidorEntidade c",
            countQuery = "select count(c) from ConsumidorEntidade c")
    Page<ResumoDeConsumidorLinha> listarResumos(Pageable pagina);
}
