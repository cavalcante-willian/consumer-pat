package br.com.alelo.consumer.consumerpat.seguranca;

/**
 * Um pré-voo de CORS vindo de uma origem desconhecida não é autorizado.
 *
 * <p>
 * Sem cliente de navegador de origem conhecida, o estado desejado é o fechado: nenhum
 * {@code Access-Control-Allow-Origin} sai, e nenhuma página de terceiros lê a API com as
 * credenciais do navegador de quem a visita.
 */
public final class VerificacaoDeCors implements Verificacao {

    @Override
    public String chave() {
        return "cors-fechado";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var resposta = sonda.preVooDeOrigemEstranha();
        var permitida = resposta.cabecalho("Access-Control-Allow-Origin");
        return new Achado(chave(), "pré-voo de CORS de uma origem desconhecida",
                permitida.isEmpty(), "status " + resposta.status() + ", origem permitida: "
                        + permitida.orElse("nenhuma"));
    }
}
