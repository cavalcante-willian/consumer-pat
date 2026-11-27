package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia;

import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao.CartaoEntidade;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidorEntidade;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fixa o que as duas entidades públicas mostram a quem está fora do pacote delas.
 *
 * <p>
 * Os acessores que o Lombok gera são de pacote, e o que outro pacote usa de fato é escrito à mão.
 * Sem este teste, bastaria trocar uma anotação para abrir de volta todos os campos da linha, e o
 * Javadoc dos acessores gerados, que o compilador cobra de todo método público, voltaria a faltar.
 */
class AcessoDasEntidadesTeste {

    private static List<String> publicosDe(Class<?> classe) {
        return Arrays.stream(classe.getDeclaredMethods())
                .filter(metodo -> Modifier.isPublic(metodo.getModifiers())).map(Method::getName)
                .toList();
    }

    @Test
    void deve_mostrar_so_o_id_e_a_versao_quando_listar_os_metodos_publicos_do_consumidor() {
        // Agir
        var publicos = publicosDe(ConsumidorEntidade.class);

        // Verificar
        assertThat(publicos).containsExactlyInAnyOrder("getId", "getVersao");
    }

    @Test
    void deve_nao_mostrar_metodo_nenhum_quando_listar_os_metodos_publicos_do_cartao() {
        // Agir
        var publicos = publicosDe(CartaoEntidade.class);

        // Verificar
        assertThat(publicos).isEmpty();
    }

    @Test
    void deve_ter_o_construtor_protegido_quando_consultar_o_do_cartao() throws Exception {
        // Agir
        var construtor = CartaoEntidade.class.getDeclaredConstructor();

        // Verificar: o JPA o aceita, e só o pacote do cartão cria a linha
        assertThat(Modifier.isProtected(construtor.getModifiers())).isTrue();
    }

    @Test
    void deve_ter_o_construtor_publico_quando_consultar_o_do_consumidor() throws Exception {
        // Agir
        var construtor = ConsumidorEntidade.class.getDeclaredConstructor();

        // Verificar: o pacote de cadastro cria a linha do consumidor
        assertThat(Modifier.isPublic(construtor.getModifiers())).isTrue();
    }
}
