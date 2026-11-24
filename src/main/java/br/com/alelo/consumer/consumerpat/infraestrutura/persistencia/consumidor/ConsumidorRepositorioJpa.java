package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * O acesso Spring Data à tabela {@code consumidores}.
 *
 * <p>
 * É pacote-privado: quem enxerga o banco é o adaptador, e nenhuma outra camada pode escrever o tipo
 * dele. As conferências de unicidade respondem só se existe, sem carregar a linha, porque o
 * adaptador as usa antes de gravar e não precisa do consumidor que colide.
 */
interface ConsumidorRepositorioJpa extends JpaRepository<ConsumidorEntidade, Long> {

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
}
