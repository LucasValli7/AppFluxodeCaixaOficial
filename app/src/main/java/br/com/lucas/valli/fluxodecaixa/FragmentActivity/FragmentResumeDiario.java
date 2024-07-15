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

public class FragmentResumeDiario extends Fragment {


    private String usuarioID;
    View v;
    private Date x = new Date();

    private String dia = new SimpleDateFormat("dd", new Locale("pt", "BR")).format(x);
    private String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
    private String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);
    Locale ptBr = new Locale("pt", "BR");
    private Double vazio = Double.parseDouble("0.00");

    private TextView txtValorSaidas, txtValorEntradas, txtValorFimDoMes;
    View container1, container2;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.diario_resume_fragment, container, false);

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
    public void RecuperarTotalSaidasResumo(){


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //document reference total Geral
        DocumentReference documentReferenceSaidasDiario = db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalSaidasDiario")
                .document(dia);


        //document reference total Entradas
        DocumentReference documentReferenceEntradasDiario = db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalEntradaDiario")
                .document(dia);


        documentReferenceSaidasDiario.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskSaidas) {
                documentReferenceEntradasDiario.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> taskEntradas) {
                        if (taskEntradas.isSuccessful() && taskSaidas.isSuccessful()){
                        DocumentSnapshot documentSnapshotSaidaDiario = taskSaidas.getResult();
                        DocumentSnapshot documentSnapshotEntradaDiario= taskEntradas.getResult();


                        if (!documentSnapshotSaidaDiario.exists()) {
                            String vazioFormatado = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                            txtValorSaidas.setText(vazioFormatado);
                        }else {
                            Double totalSaida = Double.parseDouble(documentSnapshotSaidaDiario.getString("ResultadoDaSomaSaidaDiario"));
                            String totalSaidaFormatado = NumberFormat.getCurrencyInstance(ptBr).format(totalSaida);
                            txtValorSaidas.setText(totalSaidaFormatado);
                        }

                        if (!documentSnapshotEntradaDiario.exists()) {
                            String valorConvertido = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                            txtValorEntradas.setText(valorConvertido);

                        }else {
                            Double SomaEntrada = Double.parseDouble(documentSnapshotEntradaDiario.getString("ResultadoDaSomaEntradaDiario"));
                            String valorConvertido = NumberFormat.getCurrencyInstance(ptBr).format(SomaEntrada);
                            txtValorEntradas.setText(valorConvertido);
                        }




                        // se existir, permita que recupere dados do BD
                        if (!documentSnapshotEntradaDiario.exists() && !documentSnapshotSaidaDiario.exists()) {
                            String valorConvertido = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                            txtValorFimDoMes.setText(valorConvertido);

                        } else if (!documentSnapshotEntradaDiario.exists() && documentSnapshotSaidaDiario.exists()) {
                            Double SomaSaida = Double.parseDouble(documentSnapshotSaidaDiario.getString("ResultadoDaSomaSaidaDiario"));
                            Double op = vazio - SomaSaida;
                            String valorConvertido = NumberFormat.getCurrencyInstance(ptBr).format(op);
                            txtValorFimDoMes.setText(valorConvertido);


                        } else if (documentSnapshotEntradaDiario.exists() && !documentSnapshotSaidaDiario.exists()) {
                            Double SomaEntrada = Double.parseDouble(documentSnapshotEntradaDiario.getString("ResultadoDaSomaEntradaDiario"));
                            String valorConvertido = NumberFormat.getCurrencyInstance(ptBr).format(SomaEntrada);
                            txtValorFimDoMes.setText(valorConvertido);

                        } else if (documentSnapshotEntradaDiario.exists() && documentSnapshotSaidaDiario.exists()){
                            Double SomaEntrada = Double.parseDouble(documentSnapshotEntradaDiario.getString("ResultadoDaSomaEntradaDiario"));
                            Double SomaSaida = Double.parseDouble(documentSnapshotSaidaDiario.getString("ResultadoDaSomaSaidaDiario"));


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
                    }}
                });
            }
        });


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
}
