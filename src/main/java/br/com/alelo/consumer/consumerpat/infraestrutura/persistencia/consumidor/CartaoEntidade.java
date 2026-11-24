package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A linha da tabela {@code cartoes}.
 *
 * <p>
 * É a única definição do schema do cartão: o número é texto de dígitos e único, o saldo é decimal
 * de ponto fixo com a mesma escala do {@code Dinheiro}, e a restrição de saldo não negativo fica no
 * banco como última linha de defesa, mesmo que uma regra do domínio falhe. O tipo é gravado pelo
 * nome, e nunca pela posição, porque reordenar o enum mudaria o significado do que está gravado.
 *
 * <p>
 * O cartão tem dono: a chave estrangeira para o consumidor não é nula, e a unicidade por consumidor
 * e tipo é a invariante que o legado expressava com três colunas, a de que um consumidor tem no
 * máximo um cartão de cada tipo. A chave estrangeira não cascateia exclusão. Não há índice só pelo
 * consumidor: a unicidade, que o tem como primeira coluna, já serve a busca dos cartões dele.
 *
 * <p>
 * Os campos de auditoria existem só aqui: o domínio não os conhece e o mapeador não os lê.
 */
@Entity
@Table(name = "cartoes",
        uniqueConstraints = { @UniqueConstraint(name = "uk_cartoes_numero", columnNames = "numero"),
                @UniqueConstraint(name = "uk_cartoes_consumidor_tipo",
                        columnNames = { "consumidor_id", "tipo" }) })
@Check(constraints = "saldo >= 0")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
class CartaoEntidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consumidor_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cartoes_consumidores"))
    private ConsumidorEntidade consumidor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoCartao tipo;

    @Column(name = "numero", nullable = false, length = 19)
    private String numero;

    @Column(name = "saldo", nullable = false, precision = 19, scale = 6)
    private BigDecimal saldo;

    @Version
    @Column(name = "versao", nullable = false)
    private Long versao;

    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private Instant dataCriacao;

    @LastModifiedDate
    @Column(name = "data_atualizacao", nullable = false)
    private Instant dataAtualizacao;
}
