package br.com.lucas.valli.fluxodecaixa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
        DadosAtualizacaoApp();

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


    }


    public void DadosAtualizacaoApp() {
        DocumentReference documentReference = db.collection("ArquivosDoApp").document("AppVersion");

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> TaskAppVersion) {
                if (TaskAppVersion.isSuccessful()) {
                    DocumentSnapshot AppVersion = TaskAppVersion.getResult();

                    if (AppVersion != null && AppVersion.contains("Versao")) {
                        String appVersao = AppVersion.getString("Versao");
                        String versaoInstalada = getInstalledVersion();

                        if (novaVersaoDisponivel(versaoInstalada, appVersao)) {
                            binding.btnAtualizar.setVisibility(View.VISIBLE);
                            binding.txtDonwloadDisponivel.setVisibility(View.VISIBLE);

                            binding.txtDonwloadDisponivel.setText("Versão " + appVersao + " disponível para download ");
                            binding.btnAtualizar.setVisibility(View.VISIBLE);

                            binding.btnAtualizar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String appPackageName = "br.com.lucas.valli.fluxodecaixa";
                                    String playStoreUrl = "market://details?id=" + appPackageName;
                                    String fallbackUrl = "https://play.google.com/store/apps/details?id=" + appPackageName;

                                    try {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl));
                                        intent.setPackage("com.android.vending");
                                        startActivity(intent);
                                    } catch (android.content.ActivityNotFoundException e) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    } else {
                        Log.i("TESTEFIREBASE", "NÃO HÁ DADOS");
                    }
                }
            }
        });
    }

    private String getInstalledVersion() {
        String versaoInstalada = "";
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(getPackageName(), 0);
            versaoInstalada = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versaoInstalada;
    }

    private boolean novaVersaoDisponivel(String installedVersion, String latestVersion) {
        return !installedVersion.equals(latestVersion);
    }

}