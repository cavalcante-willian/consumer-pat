package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * O acesso do Spring Data aos cartões. É pacote-privado: o que sai do pacote é a porta.
 *
 * <p>
 * Só JPQL, nunca SQL de dialeto. A espera por uma trava tem teto, mas ele não está aqui: o H2
 * ignora em silêncio a dica de timeout do JPA, por propriedade e por consulta, e o teto é o
 * parâmetro {@code LOCK_TIMEOUT} da URL do banco, na configuração.
 */
interface CartaoRepositorioJpa extends JpaRepository<CartaoEntidade, Long> {

    /**
     * Busca o cartão do número e do tipo e o trava para escrita até o fim da transação.
     *
     * @param numero o número do cartão, só dígitos
     * @param tipo   o tipo esperado
     * @return o cartão travado, ou vazio se não existe cartão com esse número e tipo
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CartaoEntidade c where c.numero = :numero and c.tipo = :tipo")
    Optional<CartaoEntidade> buscarParaAtualizacao(@Param("numero") String numero,
            @Param("tipo") TipoCartao tipo);

    /**
     * Busca o cartão só pelo número, sem trava.
     *
     * @param numero o número do cartão, só dígitos
     * @return o cartão, ou vazio se não existe
     */
    @Query("select c from CartaoEntidade c where c.numero = :numero")
    Optional<CartaoEntidade> buscarPorNumero(@Param("numero") String numero);

    /**
     * Busca os cartões de um consumidor, sem trava, ordenados pelo tipo.
     *
     * <p>
     * É a leitura do consumidor: uma consulta por consumidor, e não uma por cartão.
     *
     * @param consumidorId o id do consumidor
     * @return os cartões dele, possivelmente nenhum
     */
    @Query("select c from CartaoEntidade c where c.consumidor.id = :consumidorId "
            + "order by c.tipo")
    List<CartaoEntidade> buscarDoConsumidor(@Param("consumidorId") Long consumidorId);

    /**
     * Diz quais dos números informados já são de algum cartão.
     *
     * @param numeros os números, só dígitos
     * @return os que já existem, na ordem em que o banco os devolver
     */
    @Query("select c.numero from CartaoEntidade c where c.numero in :numeros")
    List<String> numerosJaCadastrados(@Param("numeros") Collection<String> numeros);
}
