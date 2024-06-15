package br.com.lucas.valli.fluxodecaixa.Model;

public class DadosSaidaD {

    private String TipoDeSaida;
    private String ValorDeSaida;
    private String dataDeSaida;
    private String formPagamento;

    public String getFormPagamento() {
        return formPagamento;
    }

    public void setFormPagamento(String formPagamento) {
        this.formPagamento = formPagamento;
    }

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataDeSaida() {
        return dataDeSaida;
    }

    public void setDataDeSaida(String dataDeSaida) {
        this.dataDeSaida = dataDeSaida;
    }

    public String getTipoDeSaida() {
        return TipoDeSaida;
    }

    public void setTipoDeSaida(String tipoDeSaida) {
        TipoDeSaida = tipoDeSaida;
    }

    public String getValorDeSaida() {
        return ValorDeSaida;
    }

    public void setValorDeSaida(String valorDeSaida) {
        ValorDeSaida = valorDeSaida;
    }
}
