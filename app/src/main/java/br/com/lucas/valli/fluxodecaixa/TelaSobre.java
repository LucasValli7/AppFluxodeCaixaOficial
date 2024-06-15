package br.com.lucas.valli.fluxodecaixa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import br.com.lucas.valli.fluxodecaixa.databinding.ActivityTelaSobreBinding;

public class TelaSobre extends AppCompatActivity {

    private ActivityTelaSobreBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityTelaSobreBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        binding.tolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.btnAtualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayStore();
            }
        });
    }

    private void openPlayStore() {
        // Substitua "seu_pacote" pelo pacote do seu aplicativo na Play Store
        String appPackageName = "br.com.lucas.valli.fluxodecaixa";
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public void DadosAtualizacaoApp(){

        DocumentReference documentReference = db.collection("ArquivosDoApp").document("AppVersion");

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> TaskAppVersion) {

                if (TaskAppVersion.isSuccessful()){
                    DocumentSnapshot AppVersion = TaskAppVersion.getResult();

                    if (AppVersion.exists() && AppVersion.exists()){
                        binding.btnAtualizar.setVisibility(View.VISIBLE);
                        binding.txtDonwloadDisponivel.setVisibility(View.VISIBLE);

                        String link = AppVersion.getString("Link");
                        String versao = AppVersion.getString("Versao");

                        binding.txtDonwloadDisponivel.setText("Versão "+versao+" disponível para download " + "\n\n" + "BETA" );

                    }else {
                        Log.i("TESTEFIREBASE", "NÃO HÁ DADOS");
                    }
                }

            }
        });
    }
}