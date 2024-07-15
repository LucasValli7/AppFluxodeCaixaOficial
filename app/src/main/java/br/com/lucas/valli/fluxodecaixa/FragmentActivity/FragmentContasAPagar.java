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

import br.com.lucas.valli.fluxodecaixa.Atividades.ContasAPagar;
import br.com.lucas.valli.fluxodecaixa.R;

public class FragmentContasAPagar extends Fragment {

    private String usuarioID;
    private Locale ptBr = new Locale("pt", "BR");
    private Date x = new Date();
    private String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
    private String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);
    private Double vazio = Double.parseDouble("0.00");
    View v, Container;
    TextView valorTotalCp;

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
            TotalContasAPagar();
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
        v = inflater.inflate(R.layout.contas_a_pagar_fragment, container, false);
        return v;

    }
    public void RecuperarTextView(){
        valorTotalCp = v.findViewById(R.id.txt_valorCp);
        Container = v.findViewById(R.id.container5);
    }
    public void Buttons(){
        Container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ContasAPagar.class);
                startActivity(intent);
            }
        });
    }

    public void TotalContasAPagar(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                .collection("Total Contas A Pagar").document("Total");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (!documentSnapshot.exists()) {
                    String vazioFormatado = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                    valorTotalCp.setText(vazioFormatado);

                }else {
                    Double totalContasP = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaSaidaC"));
                    String totalContasPFormat = NumberFormat.getCurrencyInstance(ptBr).format(totalContasP);
                    valorTotalCp.setText(totalContasPFormat);

                }
            }
        });
    }

}
