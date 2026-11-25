package br.com.alelo.consumer.consumerpat.api.cartao.concorrencia;

import br.com.alelo.consumer.consumerpat.fabricas.DisputaSimultanea;
import br.com.alelo.consumer.consumerpat.fabricas.DocumentosDeTeste;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vinte cadastros do mesmo documento disputam o banco: a consulta que antecede a gravação tem
 * janela, e várias requisições passam por ela juntas. O que garante um só consumidor é a restrição
 * de unicidade do banco, e quem a dispara tem de receber o conflito, e não um erro de servidor.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ConcorrenciaDeCadastroTeste {

    private static final int CADASTROS = 20;

    @Autowired
    private MockMvc mvc;

    @Test
    void deve_cadastrar_um_so_quando_vinte_cadastros_simultaneos_do_mesmo_documento()
            throws Exception {
        // Preparar: cada cadastro leva um cartão de número próprio, para só o documento conflitar
        var api = new ChamadasDaApi(mvc);
        var documento = DocumentosDeTeste.proximo();
        var cadastros = IntStream.range(0, CADASTROS)
                .<Callable<Integer>>mapToObj(i -> () -> api.cadastrarComDocumento(documento))
                .toList();

        // Agir
        var status = DisputaSimultanea.disputar(cadastros);

        // Verificar: um consumidor nasce, e todos os outros recebem conflito, nunca erro de
        // servidor
        assertThat(status).containsOnly(201, 409);
        assertThat(Collections.frequency(status, 201)).isEqualTo(1);
        assertThat(Collections.frequency(status, 409)).isEqualTo(CADASTROS - 1);
    }
}
