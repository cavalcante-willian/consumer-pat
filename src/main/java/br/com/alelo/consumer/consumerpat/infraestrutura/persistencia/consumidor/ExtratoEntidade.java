package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A linha da tabela {@code extratos}.
 *
 * <p>
 * Guarda o valor informado e o cobrado, os dois com a escala do {@code Dinheiro}, porque a
 * diferença entre eles é o ajuste do tipo. É imutável: o extrato é registro financeiro, nunca é
 * atualizado nem apagado, e por isso não tem versão nem data de atualização, e a chave estrangeira
 * para o cartão nunca cascateia exclusão.
 *
 * <p>
 * Os limites de comprimento são os do banco, e quem os confere antes é a borda.
 */
@Entity
@Table(name = "extratos",
        indexes = @Index(name = "idx_extratos_cartao_id", columnList = "cartao_id"))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
class ExtratoEntidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cartao_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_extratos_cartoes"))
    private CartaoEntidade cartao;

    @Column(name = "nome_estabelecimento", nullable = false, length = 120)
    private String nomeEstabelecimento;

    @Column(name = "descricao_produto", nullable = false, length = 255)
    private String descricaoProduto;

    @Column(name = "valor_informado", nullable = false, precision = 19, scale = 6)
    private BigDecimal valorInformado;

    @Column(name = "valor_cobrado", nullable = false, precision = 19, scale = 6)
    private BigDecimal valorCobrado;

    @Column(name = "data_compra", nullable = false)
    private Instant dataCompra;

    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private Instant dataCriacao;
}
