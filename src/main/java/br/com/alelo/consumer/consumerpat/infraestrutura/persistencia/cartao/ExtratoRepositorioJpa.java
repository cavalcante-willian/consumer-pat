package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * O acesso do Spring Data aos extratos. É pacote-privado: o que sai do pacote é a porta, que só
 * registra.
 */
interface ExtratoRepositorioJpa extends JpaRepository<ExtratoEntidade, Long> {
}
