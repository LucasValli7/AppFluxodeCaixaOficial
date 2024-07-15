package br.com.lucas.valli.fluxodecaixa.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.lucas.valli.fluxodecaixa.R;
import br.com.lucas.valli.fluxodecaixa.Atividades.TelaPrincipalEntradas;
import br.com.lucas.valli.fluxodecaixa.Atividades.TelaPrincipalSaidas;

public class FragmentResumeAnual extends Fragment {


    private String usuarioID;
    View v;
    private Date x = new Date();
    private String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
    private String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);
    Locale ptBr = new Locale("pt", "BR");
    private Double vazio = Double.parseDouble("0.00");
    View container1, container2;
    private TextView txtValorSaidas, txtValorEntradas, txtValorFimDoMes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.anual_resume_fragment, container, false);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        RecuperarTextView();
        checkConnection();
        Buttons();
    }
    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            Log.d("NETCONEX", "SEM INTERNET");
            return false;
        }else {
            RecuperarTotalSaidasResumo();
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
            Log.d("NETCONEX", "WIFI");
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
            Log.d("NETCONEX", "DADOS");
        }
        return networkInfo.isConnected();

    }

    public void RecuperarTextView(){
        txtValorSaidas =  v.findViewById(R.id.txt_valorSaidas);
        txtValorEntradas =  v.findViewById(R.id.txt_valorEntradas);
        txtValorFimDoMes =  v.findViewById(R.id.txt_ValorFimDoMes);
        container1 = v.findViewById(R.id.container1);
        container2 = v.findViewById(R.id.container2);
    }
    public void Buttons(){
        container1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), TelaPrincipalEntradas.class);
                startActivity(intent);
            }
        });
        container2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), TelaPrincipalSaidas.class);
                startActivity(intent);
            }
        });
    }

    public void RecuperarTotalSaidasResumo(){


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //document reference total Geral
        DocumentReference documentReferenceAnualSaidas = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("saidas").collection("TotalSaidaAnual")
                .document("Total");

        //document reference total Entradas
        DocumentReference documentReferenceAnualEntradas = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("entradas").collection("TotalEntradaAnual")
                .document("Total");

        documentReferenceAnualSaidas.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskSaidas) {
                documentReferenceAnualEntradas.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> taskEntradas) {
                        if (taskEntradas.isSuccessful() && taskSaidas.isSuccessful()){
                        DocumentSnapshot documentSnapshotAnualSaidas = taskSaidas.getResult();
                        DocumentSnapshot documentSnapshotEntradasAnual= taskEntradas.getResult();
                        if (!documentSnapshotAnualSaidas.exists()) {
                            String vazioFormatado = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                            txtValorSaidas.setText(vazioFormatado);
                            Log.i("TESTEDEBUG", "documento anual saida não existe");

                        }  else {
                            Double totalSaida = Double.parseDouble(documentSnapshotAnualSaidas.getString("ResultadoTotalSaidaAnual"));
                            String totalSaidaFormatado = NumberFormat.getCurrencyInstance(ptBr).format(totalSaida);
                            txtValorSaidas.setText(totalSaidaFormatado);

                            Log.i("TESTEDEBUG", "documento anual saida existe");

                        }


                        if (!documentSnapshotEntradasAnual.exists()) {
                            String valorConvertido = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                            txtValorEntradas.setText(valorConvertido);
                            Log.i("TESTEDEBUG", "documento anual entrada não existe");

                        }else {

                            Double SomaEntrada = Double.parseDouble(documentSnapshotEntradasAnual.getString("ResultadoTotalEntradaAnual"));
                            String valorConvertido = NumberFormat.getCurrencyInstance(ptBr).format(SomaEntrada);
                            txtValorEntradas.setText(valorConvertido);
                            Log.i("TESTEDEBUG", "documento anual entrada existe");
                        }


                        // se existir, permita que recupere dados do BD
                        if (!documentSnapshotEntradasAnual.exists() && !documentSnapshotAnualSaidas.exists()) {
                            String valorConvertido = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                            txtValorFimDoMes.setText(valorConvertido);

                        } else if (!documentSnapshotEntradasAnual.exists() && documentSnapshotAnualSaidas.exists()) {
                            Double SomaSaida = Double.parseDouble(documentSnapshotAnualSaidas.getString("ResultadoTotalSaidaAnual"));
                            Double op = vazio - SomaSaida;
                            String valorConvertido = NumberFormat.getCurrencyInstance(ptBr).format(op);
                            txtValorFimDoMes.setText(valorConvertido);

                        } else if (documentSnapshotEntradasAnual.exists() && !documentSnapshotAnualSaidas.exists()) {
                            Double SomaEntrada = Double.parseDouble(documentSnapshotEntradasAnual.getString("ResultadoTotalEntradaAnual"));
                            String valorCv = NumberFormat.getCurrencyInstance(ptBr).format(SomaEntrada);
                            txtValorFimDoMes.setText(valorCv);

                        } else if (documentSnapshotEntradasAnual.exists() && documentSnapshotAnualSaidas.exists()){
                            Double SomaEntrada = Double.parseDouble(documentSnapshotEntradasAnual.getString("ResultadoTotalEntradaAnual"));
                            Double SomaSaida = Double.parseDouble(documentSnapshotAnualSaidas.getString("ResultadoTotalSaidaAnual"));

                            // operação com os totais
                            if (SomaEntrada < SomaSaida){
                                Double subtracao = SomaEntrada - SomaSaida;
                                String SomaConvertida = NumberFormat.getCurrencyInstance(ptBr).format(subtracao);
                                txtValorFimDoMes.setText(String.valueOf(SomaConvertida));
                            }else {
                                Double subtracao = SomaEntrada - SomaSaida;
                                String SomaConvertida = NumberFormat.getCurrencyInstance(ptBr).format(subtracao);
                                txtValorFimDoMes.setText(String.valueOf(SomaConvertida));

                            }


                        }
                    } }
                });
            }
        });

    }

}
