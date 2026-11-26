package br.com.alelo.consumer.consumerpat.seguranca;

/**
 * Cadastrar mandando um campo que o sistema não conhece: o contrato é estrito, e o excesso é
 * recusado com {@code 400}, e não gravado nem ignorado em silêncio.
 *
 * <p>
 * Aceitar campo desconhecido é a porta do mass assignment: o dia em que o objeto ganha um campo
 * sensível, o cliente já sabe mandá-lo.
 */
public final class VerificacaoDeCampoDesconhecido implements Verificacao {

    @Override
    public String chave() {
        return "campo-desconhecido";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var resposta = sonda.cadastrarComCampoDesconhecido();
        return new Achado(chave(), "cadastro com um campo que o sistema não conhece",
                resposta.status() == 400, "status " + resposta.status() + ", e o esperado é 400");
    }
}
