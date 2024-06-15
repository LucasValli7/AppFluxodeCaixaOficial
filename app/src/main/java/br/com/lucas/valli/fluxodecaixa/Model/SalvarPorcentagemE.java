package br.com.lucas.valli.fluxodecaixa.Model;

import android.content.Context;
import android.content.SharedPreferences;

public class SalvarPorcentagemE {

    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private final String PORCENTAGEM_E = "arquivo.porcentagemE";



    private final String CHAVE_E = "essenciais";


    public SalvarPorcentagemE(Context c) {
        this.context = c;
        preferences = context.getSharedPreferences(PORCENTAGEM_E, 0);

        editor = preferences.edit();
    }

    public void salvarPorcentagemE(String salvarAnotação){
        editor.putString(CHAVE_E, salvarAnotação);
        editor.commit();
    }

    public String recuperarPorcentagemE(){
        return preferences.getString(CHAVE_E, "");

    }

}
