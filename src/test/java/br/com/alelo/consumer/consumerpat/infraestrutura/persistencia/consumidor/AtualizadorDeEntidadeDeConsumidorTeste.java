package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.DadosDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Contato;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Telefone;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AtualizadorDeEntidadeDeConsumidorTeste {

    @Test
    void deve_copiar_todos_os_dados_cadastrais_quando_atualizar_a_linha() {
        // Preparar
        var dados = FabricaDeConsumidores.dados();

        // Agir
        var linha = AtualizadorDeEntidadeDeConsumidor.atualizar(new ConsumidorEntidade(), dados);

        // Verificar
        assertThat(linha.getNome()).isEqualTo("Maria Souza");
        assertThat(linha.getDocumento()).isEqualTo(FabricaDeConsumidores.DOCUMENTO_PADRAO);
        assertThat(linha.getDataNascimento()).isEqualTo(dados.dataNascimento());
        assertThat(linha.getCelular()).isEqualTo("11999998888");
        assertThat(linha.getEmail()).isEqualTo("maria@exemplo.com.br");
        assertThat(linha.getRua()).isEqualTo("Rua das Flores");
        assertThat(linha.getNumero()).isEqualTo("12-B");
        assertThat(linha.getCidade()).isEqualTo("São Paulo");
        assertThat(linha.getPais()).isEqualTo("Brasil");
        assertThat(linha.getCep()).isEqualTo("01001000");
    }

    @Test
    void deve_gravar_nulo_nos_telefones_quando_eles_sao_ausentes() {
        // Agir
        var linha = AtualizadorDeEntidadeDeConsumidor.atualizar(new ConsumidorEntidade(),
                FabricaDeConsumidores.dados());

        // Verificar
        assertThat(linha.getTelefoneResidencial()).isNull();
        assertThat(linha.getTelefone()).isNull();
    }

    @Test
    void deve_gravar_os_telefones_quando_eles_sao_informados() {
        // Preparar
        var base = FabricaDeConsumidores.dados();
        var contato = new Contato(base.contato().celular(), Optional.of(new Telefone("1133334444")),
                Optional.of(new Telefone("1144445555")), base.contato().email());
        var dados = new DadosDoConsumidor(base.nome(), base.documento(), base.dataNascimento(),
                contato, base.endereco());

        // Agir
        var linha = AtualizadorDeEntidadeDeConsumidor.atualizar(new ConsumidorEntidade(), dados);

        // Verificar
        assertThat(linha.getTelefoneResidencial()).isEqualTo("1133334444");
        assertThat(linha.getTelefone()).isEqualTo("1144445555");
    }

    @Test
    void deve_limpar_um_telefone_que_deixou_de_existir_quando_atualizar_uma_linha_existente() {
        // Preparar
        var linha = new ConsumidorEntidade();
        linha.setTelefone("1144445555");

        // Agir
        AtualizadorDeEntidadeDeConsumidor.atualizar(linha, FabricaDeConsumidores.dados());

        // Verificar
        assertThat(linha.getTelefone()).isNull();
    }

    @Test
    void deve_manter_o_id_a_versao_e_a_auditoria_quando_atualizar_uma_linha_existente() {
        // Preparar
        var linha = new ConsumidorEntidade();
        linha.setId(5L);
        linha.setVersao(3L);

        // Agir
        AtualizadorDeEntidadeDeConsumidor.atualizar(linha, FabricaDeConsumidores.dados());

        // Verificar
        assertThat(linha.getId()).isEqualTo(5L);
        assertThat(linha.getVersao()).isEqualTo(3L);
        assertThat(linha.getDataCriacao()).isNull();
    }

    @Test
    void deve_devolver_a_mesma_linha_quando_atualizar() {
        // Preparar
        var linha = new ConsumidorEntidade();

        // Agir
        var devolvida = AtualizadorDeEntidadeDeConsumidor.atualizar(linha,
                FabricaDeConsumidores.dados());

        // Verificar
        assertThat(devolvida).isSameAs(linha);
    }
}
