package br.com.lucas.valli.fluxodecaixa.Model;

public class HistoricoEntrada {

    private String TipoDeEntrada;
    private String ValorDeEntrada;
    private String dataDeEntrada;
    private String formPagamento;

    public String getFormPagamento() {
        return formPagamento;
    }

    public void setFormPagamento(String formPagamento) {
        this.formPagamento = formPagamento;
    }

    public String getTipoDeEntrada() {
        return TipoDeEntrada;
    }

    public void setTipoDeEntrada(String tipoDeEntrada) {
        TipoDeEntrada = tipoDeEntrada;
    }

    public String getValorDeEntrada() {
        return ValorDeEntrada;
    }

    public void setValorDeEntrada(String valorDeEntrada) {
        ValorDeEntrada = valorDeEntrada;
    }

    public String getDataDeEntrada() {
        return dataDeEntrada;
    }

    public void setDataDeEntrada(String dataDeEntrada) {
        this.dataDeEntrada = dataDeEntrada;
    }
}




