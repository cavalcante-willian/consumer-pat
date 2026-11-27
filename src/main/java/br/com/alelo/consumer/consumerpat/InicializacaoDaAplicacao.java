package br.com.alelo.consumer.consumerpat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do consumer-pat: liga o Spring Boot e mais nada.
 *
 * <p>
 * Não tem regra de negócio, e nenhum tipo de domínio, caso de uso ou validador nasce aqui. Os beans
 * são compostos nas classes de configuração de {@code api.configuracao}, e a varredura de
 * componentes parte do pacote desta classe, por isso ela fica na raiz.
 */
@SpringBootApplication
public class InicializacaoDaAplicacao {

    /**
     * Sobe a aplicação com os argumentos da linha de comando.
     *
     * @param args argumentos repassados ao Spring Boot, como {@code --spring.profiles.active=prod}
     */
    public static void main(String[] args) {
        SpringApplication.run(InicializacaoDaAplicacao.class, args);
    }
}
