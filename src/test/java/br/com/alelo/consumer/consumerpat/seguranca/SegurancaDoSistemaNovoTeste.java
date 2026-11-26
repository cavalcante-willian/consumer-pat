package br.com.alelo.consumer.consumerpat.seguranca;

import br.com.alelo.consumer.consumerpat.medicao.ClienteHttp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Roda a bateria de segurança inteira contra o sistema novo, num servidor real em porta aleatória,
 * e exige que ele se proteja em tudo, menos nos riscos que a padronização aceita.
 *
 * <p>
 * É o teste de regressão da bateria: o dia em que uma mudança deixar passar um valor negativo,
 * devolver uma pilha de chamadas ou concatenar uma ordenação numa consulta, ele falha, e a falha
 * traz a evidência observada.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SegurancaDoSistemaNovoTeste {

    /**
     * Os riscos aceitos do perfil padrão, que o {@code SEGURANCA.md} registra: a documentação fica
     * aberta porque o README do desafio a documenta, e nenhuma rota limita a taxa, porque isso
     * exige dependência nova ou um proxy à frente. O console do H2 não está aqui: de fora ele é
     * recusado, e só a própria máquina o abre, por desenho.
     */
    private static final Set<String> RISCOS_ACEITOS = Set.of("documentacao-publica",
            "limite-de-taxa");

    @LocalServerPort
    private int porta;

    @Test
    void deve_se_proteger_em_toda_verificacao_quando_o_risco_nao_e_aceito() throws Exception {
        // Agir
        var sonda = new SondaDoNovo(new ClienteHttp("http://localhost:" + porta));
        var achados = BateriaDeSeguranca.executar(sonda);

        // Verificar: a falha mostra o que foi observado. Uma verificação que o ambiente não deixou
        // fazer, como a do console numa máquina sem interface de rede, não é exposição, e não falha
        assertThat(achados).filteredOn(achado -> !RISCOS_ACEITOS.contains(achado.chave()))
                .filteredOn(achado -> achado.veredito() == Veredito.EXPOSTO)
                .extracting(achado -> achado.chave() + ": " + achado.evidencia()).isEmpty();
    }

    @Test
    void deve_fazer_todas_as_verificacoes_da_bateria_quando_executar() throws Exception {
        // Agir
        var sonda = new SondaDoNovo(new ClienteHttp("http://localhost:" + porta));
        var achados = BateriaDeSeguranca.executar(sonda);

        // Verificar: um achado por verificação, e os riscos aceitos existem de verdade
        assertThat(achados).hasSameSizeAs(BateriaDeSeguranca.verificacoes());
        assertThat(achados).extracting(Achado::chave).containsAll(RISCOS_ACEITOS);
    }
}
