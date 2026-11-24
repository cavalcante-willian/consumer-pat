package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.DadosDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.contato.Telefone;

/**
 * Leva os dados cadastrais do domínio para a linha do consumidor.
 *
 * <p>
 * Serve à criação, com uma linha nova, e à atualização, com a que já existe. Só os dados mudam: o
 * id, a versão e a auditoria são do banco, e os cartões não passam por aqui. Um telefone ausente
 * vira nulo na coluna.
 */
final class AtualizadorDeEntidadeDeConsumidor {

    private AtualizadorDeEntidadeDeConsumidor() {
    }

    /**
     * Copia os dados para a linha.
     *
     * @param entidade a linha a preencher, nova ou existente
     * @param dados    os dados cadastrais
     * @return a mesma linha, preenchida
     */
    static ConsumidorEntidade atualizar(ConsumidorEntidade entidade, DadosDoConsumidor dados) {
        entidade.setNome(dados.nome());
        entidade.setDocumento(dados.documento().digitos());
        entidade.setDataNascimento(dados.dataNascimento());
        entidade.setCelular(dados.contato().celular().digitos());
        entidade.setTelefoneResidencial(
                dados.contato().telefoneResidencial().map(Telefone::digitos).orElse(null));
        entidade.setTelefone(dados.contato().telefone().map(Telefone::digitos).orElse(null));
        entidade.setEmail(dados.contato().email().endereco());
        entidade.setRua(dados.endereco().rua());
        entidade.setNumero(dados.endereco().numero());
        entidade.setCidade(dados.endereco().cidade());
        entidade.setPais(dados.endereco().pais());
        entidade.setCep(dados.endereco().cep().digitos());
        return entidade;
    }
}
