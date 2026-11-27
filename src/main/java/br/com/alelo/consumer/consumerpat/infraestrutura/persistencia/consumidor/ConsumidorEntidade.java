package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import lombok.AccessLevel;
import lombok.Getter;
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
@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
public class ConsumidorEntidade {

    /** A chave da linha, gerada pelo banco. É nula até a primeira gravação. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** O nome, obrigatório e de até 150 caracteres. */
    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    /**
     * O documento, só com dígitos e sem pontuação, obrigatório e único: é o que faz o mesmo
     * documento escrito de formas diferentes ser a mesma linha.
     */
    @Column(name = "documento", nullable = false, length = 14)
    private String documento;

    /** A data de nascimento, obrigatória. */
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    /** O celular, obrigatório e de até 15 caracteres. */
    @Column(name = "celular", nullable = false, length = 15)
    private String celular;

    /** O telefone residencial, opcional: o cadastro o aceita vazio. */
    @Column(name = "telefone_residencial", length = 15)
    private String telefoneResidencial;

    /** O telefone geral, opcional: o cadastro o aceita vazio. */
    @Column(name = "telefone", length = 15)
    private String telefone;

    /** O e-mail, obrigatório e de até 254 caracteres, o tamanho máximo de um endereço. */
    @Column(name = "email", nullable = false, length = 254)
    private String email;

    /** A rua do endereço, obrigatória e de até 100 caracteres. */
    @Column(name = "rua", nullable = false, length = 100)
    private String rua;

    /** O número do endereço, em texto, obrigatório e de até 10 caracteres. */
    @Column(name = "numero", nullable = false, length = 10)
    private String numero;

    /** A cidade do endereço, obrigatória e de até 100 caracteres. */
    @Column(name = "cidade", nullable = false, length = 100)
    private String cidade;

    /** O país do endereço, obrigatório e de até 100 caracteres. */
    @Column(name = "pais", nullable = false, length = 100)
    private String pais;

    /** O CEP, só com os oito dígitos, obrigatório. */
    @Column(name = "cep", nullable = false, length = 8)
    private String cep;

    /**
     * A versão da linha, que o JPA incrementa a cada atualização: é o que recusa uma atualização
     * feita sobre uma leitura velha.
     */
    @Version
    @Column(name = "versao", nullable = false)
    private Long versao;

    /** O instante da criação, preenchido pela auditoria e nunca alterado. */
    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private Instant dataCriacao;

    /** O instante da última atualização, preenchido pela auditoria. */
    @LastModifiedDate
    @Column(name = "data_atualizacao", nullable = false)
    private Instant dataAtualizacao;

    /**
     * Cria a linha vazia: o mapeador a preenche antes de gravar, e o JPA a instancia ao ler.
     */
    public ConsumidorEntidade() {
    }

    /**
     * Devolve a chave da linha.
     *
     * @return a chave gerada pelo banco, ou nulo antes da primeira gravação
     */
    public Long getId() {
        return id;
    }

    /**
     * Devolve a versão da linha.
     *
     * @return a versão atual, que o JPA incrementa a cada atualização
     */
    public Long getVersao() {
        return versao;
    }
}
