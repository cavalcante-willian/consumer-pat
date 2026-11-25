package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import java.time.Instant;
import java.time.LocalDate;

/**
 * A linha da tabela {@code consumidores}.
 *
 * <p>
 * É a única definição do schema do consumidor. O documento é texto de dígitos e único, e é essa
 * restrição que faz dois cadastros do mesmo documento escrito de formas diferentes serem a mesma
 * linha. Os telefones residencial e geral são os únicos que aceitam nulo, porque o cadastro do
 * legado já os aceitava vazios. Os limites de tamanho são os dos tipos do domínio, de modo que um
 * texto que passou por eles cabe na coluna.
 *
 * <p>
 * O consumidor não tem coleção de cartões: o cartão aponta para o consumidor, e o adaptador busca
 * os cartões por consulta. Assim, atualizar um cadastro nunca toca nos cartões, que são o caminho
 * quente, com trava. O índice por nome e id é o da listagem, que ordena por nome.
 *
 * <p>
 * Os campos de auditoria existem só aqui: o domínio não os conhece e o mapeador não os lê.
 */
@Entity
@Table(name = "consumidores",
        uniqueConstraints = @UniqueConstraint(name = "uk_consumidores_documento",
                columnNames = "documento"),
        indexes = @Index(name = "idx_consumidores_nome_id", columnList = "nome, id"))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class ConsumidorEntidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "documento", nullable = false, length = 14)
    private String documento;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "celular", nullable = false, length = 15)
    private String celular;

    @Column(name = "telefone_residencial", length = 15)
    private String telefoneResidencial;

    @Column(name = "telefone", length = 15)
    private String telefone;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "rua", nullable = false, length = 100)
    private String rua;

    @Column(name = "numero", nullable = false, length = 10)
    private String numero;

    @Column(name = "cidade", nullable = false, length = 100)
    private String cidade;

    @Column(name = "pais", nullable = false, length = 100)
    private String pais;

    @Column(name = "cep", nullable = false, length = 8)
    private String cep;

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
