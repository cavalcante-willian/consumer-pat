package br.com.alelo.consumer.consumerpat.seguranca;

import br.com.alelo.consumer.consumerpat.medicao.CartaoDeDisputa;
import br.com.alelo.consumer.consumerpat.medicao.OperacoesDoAlvo;
import br.com.alelo.consumer.consumerpat.medicao.RespostaHttp;

/**
 * O que a bateria sabe pedir a um sistema, sem saber como ele o chama.
 *
 * <p>
 * É o que permite fazer as mesmas verificações contra o legado e contra o novo: cada sistema traduz
 * cada tentativa para as suas rotas e os seus corpos. O dinheiro, que os dois têm, vem das
 * {@link OperacoesDoAlvo} do comparativo de desempenho, e o resto é daqui.
 */
public interface SondaDeSeguranca {

    /**
     * O nome do sistema, para o relatório.
     *
     * @return {@code legado} ou {@code novo}
     */
    String nome();

    /**
     * As operações de dinheiro: preparar um cartão com saldo, comprar nele e ler o saldo.
     *
     * @return as operações do sistema
     */
    OperacoesDoAlvo operacoes();

    /**
     * Tenta mudar o saldo de um cartão pela rota que atualiza o cadastro.
     *
     * @param cartao um cartão preparado
     * @param saldo  o saldo que o atacante quer impor
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp atualizarMandandoSaldo(CartaoDeDisputa cartao, String saldo) throws Exception;

    /**
     * Tenta comprar com um {@code GET}, que não devia mudar estado.
     *
     * @param cartao um cartão preparado
     * @param valor  o valor da compra
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp comprarPorGet(CartaoDeDisputa cartao, String valor) throws Exception;

    /**
     * Cadastra um consumidor novo, de documento único, com o nome dado.
     *
     * @param nome o nome, que pode ser hostil ou enorme
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp cadastrarComNome(String nome) throws Exception;

    /**
     * Cadastra com um campo que o sistema não conhece.
     *
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp cadastrarComCampoDesconhecido() throws Exception;

    /**
     * Cadastra com um JSON truncado.
     *
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp cadastrarComJsonMalformado() throws Exception;

    /**
     * Compra mandando um número de cartão que não é número.
     *
     * @param numero o número do cartão, que pode ser hostil
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp comprarComNumeroDeCartao(String numero) throws Exception;

    /**
     * Lista os consumidores com uma ordenação pedida pelo cliente.
     *
     * @param ordenacao o campo de ordenação, que pode ser hostil
     * @param direcao   a direção, que pode ser hostil
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp listar(String ordenacao, String direcao) throws Exception;

    /**
     * Credita um cartão que não existe.
     *
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp creditarCartaoInexistente() throws Exception;

    /**
     * Cadastra um consumidor de documento e cartão conhecidos e lê o que o sistema mostra dele.
     *
     * @return o texto lido e o que foi cadastrado
     * @throws Exception se a chamada falha
     */
    ExposicaoDeDados exporDadosDeConsumidor() throws Exception;

    /**
     * Uma requisição válida e barata, para olhar os cabeçalhos e medir a taxa.
     *
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp requisicaoValida() throws Exception;

    /**
     * Um {@code TRACE} numa rota da aplicação.
     *
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp trace() throws Exception;

    /**
     * Um pré-voo de CORS vindo de uma origem que o sistema não conhece.
     *
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp preVooDeOrigemEstranha() throws Exception;

    /**
     * Abre o console do H2.
     *
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp consoleDoH2() throws Exception;

    /**
     * Abre a página do Swagger.
     *
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp swagger() throws Exception;

    /**
     * Pede a especificação OpenAPI.
     *
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp especificacao() throws Exception;
}
