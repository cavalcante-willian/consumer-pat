package br.com.alelo.consumer.consumerpat.api.comum.documentacao;

import io.swagger.v3.oas.models.tags.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagsDaApiTeste {

    @Test
    void deve_listar_um_grupo_por_recurso_na_ordem_da_documentacao_quando_listar() {
        // Agir
        var tags = TagsDaApi.lista();

        // Verificar
        assertThat(tags).extracting(Tag::getName).containsExactly(TagsDaApi.CONSUMIDORES,
                TagsDaApi.CARTOES, TagsDaApi.SAUDE);
    }

    @Test
    void deve_descrever_cada_grupo_quando_listar() {
        // Agir
        var tags = TagsDaApi.lista();

        // Verificar
        assertThat(tags).extracting(Tag::getDescription).doesNotContainNull()
                .allSatisfy(descricao -> assertThat(descricao).isNotBlank());
    }
}
