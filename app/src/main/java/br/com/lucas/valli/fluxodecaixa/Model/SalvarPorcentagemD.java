package br.com.lucas.valli.fluxodecaixa.Model;

import android.content.Context;
import android.content.SharedPreferences;

public class SalvarPorcentagemD {

    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private final String PORCENTAGEM_D = "arquivo.porcentagemD";

    private final String CHAVE_D = "DESEJOS_PESSOAIS";


    public SalvarPorcentagemD(Context c) {
        this.context = c;
        preferences = context.getSharedPreferences(PORCENTAGEM_D, 0);

        editor = preferences.edit();
    }

    public void salvarPorcentagemD(String salvarAnotação){
        editor.putString(CHAVE_D, salvarAnotação);
        editor.commit();
    }

    public String recuperarPorcentagemD(){
        return preferences.getString(CHAVE_D, "");

    }
}
