package br.com.alelo.consumer.consumerpat.seguranca;

import br.com.alelo.consumer.consumerpat.medicao.ClienteHttp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Roda a bateria contra o legado que ainda está nesta árvore, num servidor real em porta aleatória,
 * para a sonda do legado não apodrecer entre uma execução do comparativo e outra.
 *
 * <p>
 * Aqui o legado não é cobrado: o teste só prova que a bateria roda inteira contra ele e que os
 * defeitos conhecidos aparecem como exposição. Sai junto com o legado, no item que o remove. O que
 * fica depois dele é a {@link SondaDoLegado}, que fala por HTTP com o original extraído do
 * histórico.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SegurancaDoLegadoDaArvoreTeste {

    @LocalServerPort
    private int porta;

    @Test
    void deve_mostrar_os_defeitos_conhecidos_como_exposicao_quando_rodar_a_bateria()
            throws Exception {
        // Agir
        var sonda = new SondaDoLegado(new ClienteHttp("http://localhost:" + porta));
        var achados = BateriaDeSeguranca.executar(sonda);

        // Verificar: o legado devolve um achado por verificação, e os defeitos dele aparecem
        assertThat(achados).hasSameSizeAs(BateriaDeSeguranca.verificacoes());
        assertThat(achados).filteredOn(achado -> !achado.protegido()).extracting(Achado::chave)
                .contains("valor-negativo", "saldo-por-atualizacao", "estado-alterado-por-get",
                        "cabecalhos-de-seguranca", "dado-sensivel-na-resposta",
                        "erro-de-cliente-nao-vira-500");
    }
}
