package br.com.lucas.valli.fluxodecaixa.Model;

import android.content.Context;
import android.content.SharedPreferences;

public class SalvarPorcentagemP {
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private final String PORCENTAGEM_P = "arquivo.porcentagemP";

    private final String CHAVE_P = "PAGAMENTO_DE_DIVIDAS";


    public SalvarPorcentagemP(Context c) {
        this.context = c;
        preferences = context.getSharedPreferences(PORCENTAGEM_P, 0);

        editor = preferences.edit();
    }

    public void salvarPorcentagemP(String salvarAnotação){
        editor.putString(CHAVE_P, salvarAnotação);
        editor.commit();
    }

    public String recuperarPorcentagemP(){
        return preferences.getString(CHAVE_P, "");

    }
}
