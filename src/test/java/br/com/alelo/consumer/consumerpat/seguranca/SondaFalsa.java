package br.com.alelo.consumer.consumerpat.seguranca;

import br.com.alelo.consumer.consumerpat.medicao.CartaoDeDisputa;
import br.com.alelo.consumer.consumerpat.medicao.OperacoesDoAlvo;
import br.com.alelo.consumer.consumerpat.medicao.RespostaHttp;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;

/**
 * Um sistema de mentira para provar as verificações sem subir nada.
 *
 * <p>
 * Cada resposta é um campo que o teste escolhe, e o que as verificações mandaram fica guardado para
 * o teste conferir. O padrão é um sistema que se defende: recusa com {@code 400}, não ecoa, fecha
 * as portas. Cada teste estraga uma coisa e vê a verificação reparar.
 */
final class SondaFalsa implements SondaDeSeguranca {

    final OperacoesDeSaldoFalsas operacoes;

    boolean aceitaSaldoNaAtualizacao;

    boolean compraPorGetMudaOSaldo;

    RespostaHttp recusa = resposta(400, "{\"detail\":\"entrada inválida\"}");

    RespostaHttp naoEncontrado = resposta(404, "{\"detail\":\"não encontrado\"}");

    RespostaHttp valida = resposta(200, "{}",
            Map.of("x-content-type-options", "nosniff", "x-frame-options", "DENY",
                    "content-security-policy", "default-src 'none'", "referrer-policy",
                    "no-referrer", "cache-control", "no-store"));

    RespostaHttp trace = resposta(405, "");

    RespostaHttp preVoo = resposta(403, "Invalid CORS request");

    RespostaHttp console = resposta(404, "");

    Optional<RespostaHttp> consoleDeFora = Optional.of(resposta(200,
            "<html>Sorry, remote connections ('webAllowOthers') are disabled on this "
                    + "server.</html>"));

    RespostaHttp swagger = resposta(404, "");

    RespostaHttp especificacao = resposta(404, "");

    ExposicaoDeDados exposicao = new ExposicaoDeDados("{\"documento\":\"****4705\"}", "39053344705",
            "4111111111111111");

    IntFunction<RespostaHttp> aCadaRequisicao = n -> valida;

    final List<String> nomesRecebidos = new ArrayList<>();

    final List<String> numerosRecebidos = new ArrayList<>();

    final List<String> ordenacoesRecebidas = new ArrayList<>();

    private int requisicoes;

    SondaFalsa(boolean sistemaSeDefende) {
        this.operacoes = new OperacoesDeSaldoFalsas(sistemaSeDefende);
    }

    static RespostaHttp resposta(int status, String corpo) {
        return resposta(status, corpo, Map.of());
    }

    static RespostaHttp resposta(int status, String corpo, Map<String, String> cabecalhos) {
        return new RespostaHttp(status, corpo.getBytes(StandardCharsets.UTF_8), Optional.empty(),
                cabecalhos);
    }

    @Override
    public String nome() {
        return "falsa";
    }

    @Override
    public OperacoesDoAlvo operacoes() {
        return operacoes;
    }

    @Override
    public RespostaHttp atualizarMandandoSaldo(CartaoDeDisputa cartao, String saldo) {
        operacoes.definirSaldo(Map.of(true, new BigDecimal(saldo), false, operacoes.saldo())
                .get(aceitaSaldoNaAtualizacao));
        return Map.of(true, resposta(200, "{}"), false, recusa).get(aceitaSaldoNaAtualizacao);
    }

    @Override
    public RespostaHttp comprarPorGet(CartaoDeDisputa cartao, String valor) {
        operacoes.definirSaldo(Map.of(true, operacoes.saldo().subtract(new BigDecimal(valor)),
                false, operacoes.saldo()).get(compraPorGetMudaOSaldo));
        return Map.of(true, resposta(200, ""), false, resposta(405, ""))
                .get(compraPorGetMudaOSaldo);
    }

    @Override
    public RespostaHttp cadastrarComNome(String nome) {
        nomesRecebidos.add(nome);
        return recusa;
    }

    @Override
    public RespostaHttp cadastrarComCampoDesconhecido() {
        return recusa;
    }

    @Override
    public RespostaHttp cadastrarComJsonMalformado() {
        return recusa;
    }

    @Override
    public RespostaHttp comprarComNumeroDeCartao(String numero) {
        numerosRecebidos.add(numero);
        return recusa;
    }

    @Override
    public RespostaHttp listar(String ordenacao, String direcao) {
        ordenacoesRecebidas.add(ordenacao);
        return Map.of(true, resposta(200, "{}"), false, recusa)
                .get("nome".equals(ordenacao) && "asc".equals(direcao));
    }

    @Override
    public RespostaHttp creditarCartaoInexistente() {
        return naoEncontrado;
    }

    @Override
    public ExposicaoDeDados exporDadosDeConsumidor() {
        return exposicao;
    }

    @Override
    public RespostaHttp requisicaoValida() {
        requisicoes++;
        return aCadaRequisicao.apply(requisicoes);
    }

    @Override
    public RespostaHttp trace() {
        return trace;
    }

    @Override
    public RespostaHttp preVooDeOrigemEstranha() {
        return preVoo;
    }

    @Override
    public RespostaHttp consoleDoH2() {
        return console;
    }

    @Override
    public Optional<RespostaHttp> consoleDoH2DeFora() {
        return consoleDeFora;
    }

    @Override
    public RespostaHttp swagger() {
        return swagger;
    }

    @Override
    public RespostaHttp especificacao() {
        return especificacao;
    }
}
