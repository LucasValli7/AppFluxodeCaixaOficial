package br.com.lucas.valli.fluxodecaixa.FragmentActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.lucas.valli.fluxodecaixa.Model.SalvarPorcentagemD;
import br.com.lucas.valli.fluxodecaixa.Model.SalvarPorcentagemE;
import br.com.lucas.valli.fluxodecaixa.Model.SalvarPorcentagemP;
import br.com.lucas.valli.fluxodecaixa.R;
import br.com.lucas.valli.fluxodecaixa.TelaPrincipal;

public class ControleGastoP extends Fragment {

    TextView txt_porcentagemPagamentoDeDividas, ValorDisponivelP, ValorGastoP, txtPorcentagemGastosP,txt_PagamentoDividas;
    ImageView img_edit;
    private SalvarPorcentagemP salvarPorcentagemP;
    private String usuarioID;
    private Locale ptBr = new Locale("pt", "BR");
    private Date x = new Date();
    private String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
    private String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Double vazio = Double.parseDouble("0.00");
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    View v;

    @Override
    public void onStart() {
        super.onStart();
        RecuperarTextView();
        Buttons();
        RecuperarPorcentagem();
        checkConnection();

    }
    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            Log.d("NETCONEX", "SEM INTERNET");
            return false;
        }else {
            ControleDeGastos();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.controle_gasto_p, container, false);
        return v;

    }

    public void RecuperarTextView(){
        txt_porcentagemPagamentoDeDividas = v.findViewById(R.id.txt_porcentagemPagamentoDeDividas);
        img_edit = v.findViewById(R.id.img_edit);
        ValorDisponivelP = v.findViewById(R.id.ValorDisponivelP);
        ValorGastoP = v.findViewById(R.id.ValorGastoP);
        txtPorcentagemGastosP = v.findViewById(R.id.txt_porcentagemGastosP);
        txt_PagamentoDividas = v.findViewById(R.id.txt_PagamentoDividas);
    }
    public void ControleDeGastos(){

        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference documentReferenceEntrada = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                .document("Total");
        documentReferenceEntrada.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshotEntrada = task.getResult();
                    if (documentSnapshotEntrada.exists()) {

                        Double SomaEntrada = Double.parseDouble(documentSnapshotEntrada.getString("ResultadoDaSomaEntrada"));
                        Double PorcentagemP = Double.parseDouble(txt_porcentagemPagamentoDeDividas.getText().toString());

                        if (PorcentagemP != 0) {

                            Double somaP = (SomaEntrada * PorcentagemP) / 100;
                            String valorConvertidoP = NumberFormat.getCurrencyInstance(ptBr).format(somaP);
                            ValorDisponivelP.setText(valorConvertidoP);

                        }
                    } else {
                        String valorConvertido = NumberFormat.getCurrencyInstance(ptBr).format(vazio);

                        ValorDisponivelP.setText(valorConvertido);

                    }
                }
            }
        });

    }
    public void Buttons(){
        String mes = new SimpleDateFormat("MMMM", new Locale("pt", "BR")).format(x);
        img_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Porcentagem Desejada");

                final EditText edittext_dialogo = new EditText(getContext());
                edittext_dialogo.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(edittext_dialogo);

                builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String dialogo = edittext_dialogo.getText().toString();
                        txt_porcentagemPagamentoDeDividas.setText(dialogo);


                        String porcentagemRecuperada = edittext_dialogo.getText().toString();
                        if (porcentagemRecuperada.equals("")){
                            Snackbar snackbar = Snackbar.make(v,"A porcentagem não é válida",Snackbar.LENGTH_INDEFINITE)
                                    .setAction("OK", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                        }
                                    });
                            snackbar.show();
                            txt_porcentagemPagamentoDeDividas.setText("30");
                        }else {
                            ControleDeGastos();

                            salvarPorcentagemP.salvarPorcentagemP(porcentagemRecuperada);
                            Toast.makeText(getContext(), "salvo com sucesso!", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
        txtPorcentagemGastosP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String porcentagem = txtPorcentagemGastosP.getText().toString();
                Toast.makeText(getContext(), "Você gastou " + porcentagem + "% " + " do seu Total de Entradas", Toast.LENGTH_SHORT ).show();
            }
        });

        txt_PagamentoDividas.setText(mes);

    }
    public void RecuperarPorcentagem(){
        salvarPorcentagemP = new SalvarPorcentagemP(getContext());
        String recuperarP = salvarPorcentagemP.recuperarPorcentagemP();


        if (!recuperarP.equals("")){
            txt_porcentagemPagamentoDeDividas.setText(recuperarP);


        }
    }
    public void RecuperarTotalSaidasResumo(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        //document reference total Pagamento de Dívidas
        DocumentReference documentReferenceP = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasP")
                .document("Total");

        //document reference total Entradas
        DocumentReference documentReferenceEntradas = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                .document("Total");

        //document reference total Pagamento de Dívidas
        documentReferenceP.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskP) {
                documentReferenceEntradas.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> taskEntradas) {
                        if (taskEntradas.isSuccessful() && taskP.isSuccessful()){
                        DocumentSnapshot documentSnapshotP = taskP.getResult();
                        DocumentSnapshot documentSnapshotEntradas= taskEntradas.getResult();

                        if (!documentSnapshotP.exists()) {
                            String vazioFormatado = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                            ValorGastoP.setText(vazioFormatado);

                        }else {
                            Double totalSaidaP = Double.parseDouble(documentSnapshotP.getString("ResultadoDaSomaSaidaP"));
                            String totalSaidaFormatadoP = NumberFormat.getCurrencyInstance(ptBr).format(totalSaidaP);
                            ValorGastoP.setText(totalSaidaFormatadoP);
                        }

                        if (documentSnapshotEntradas.exists()) {
                            double SomaEntrada = Double.parseDouble(documentSnapshotEntradas.getString("ResultadoDaSomaEntrada"));

                            double PorcentagemP = Double.parseDouble(txt_porcentagemPagamentoDeDividas.getText().toString());

                            if (PorcentagemP != 0){

                                Double somaP = (SomaEntrada * PorcentagemP) /100;
                                String valorConvertidoP = NumberFormat.getCurrencyInstance(ptBr).format(somaP);
                                ValorDisponivelP.setText(valorConvertidoP);


                                if (documentSnapshotP.exists()){
                                    // descobrir porcentagem partindo de outro valor P
                                    double totalSaidaP = Double.parseDouble(documentSnapshotP.getString("ResultadoDaSomaSaidaP"));
                                    Double operacaoP = totalSaidaP / SomaEntrada * 100;
                                    txtPorcentagemGastosP.setText(decimalFormat.format(operacaoP));
                                }

                            }

                        }
                    }}
                });
            }
        });


    }

}
