package br.com.lucas.valli.fluxodecaixa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import br.com.lucas.valli.fluxodecaixa.databinding.ActivityEditarEmailBinding;
import br.com.lucas.valli.fluxodecaixa.databinding.ActivityEditarNomeBinding;

public class EditarEmail extends AppCompatActivity {
    private ActivityEditarEmailBinding binding;
    private String usuarioID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onStart() {
        super.onStart();
        RecuperarDadosUsuario();
        BtnSalvar();
    }
    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            Log.d("NETCONEX", "SEM INTERNET");
            Toast.makeText(EditarEmail.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            UpdatEmail();
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
            Log.d("NETCONEX", "WIFI");
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
            Log.d("NETCONEX", "DADOS");
        }
        return networkInfo.isConnected();

    }
    public void BtnSalvar(){
        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityEditarEmailBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        binding.tolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    public void UpdatEmail(){

        String emailAtual = binding.editEmailAtual.getText().toString();
        String emailNovo = binding.editNovoEmail.getText().toString();
        String senhaAtual = binding.editSenha.getText().toString();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        if (email.equals(emailNovo) || !emailAtual.equals(email)){
            Toast.makeText(EditarEmail.this, "E-mail ou senha são inválidos", Toast.LENGTH_LONG).show();
        }else {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            // Get auth credentials from the user for re-authentication

            AuthCredential credential = EmailAuthProvider
                    .getCredential(email, senhaAtual); // Current Login Credentials \\
            // Prompt the user to re-provide their sign-in credentials
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //Now change your email address \\
                                //----------------Code for Changing Email Address----------\\
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                user.updateEmail(emailNovo)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(EditarEmail.this, "E-mail alterado com com sucesso", Toast.LENGTH_LONG).show();
                                                    RecuperarDadosUsuario();
                                                    finish();

                                                }else {
                                                    Log.i("TESTEUPDATE", " USUARIO NAO AUTTENTIFICADO");
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(EditarEmail.this, "E-mail ou senha são inválidos", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
        }
    }
    public void RecuperarDadosUsuario(){
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();


        DocumentReference documentReference = db.collection(usuarioID).document("usuario");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null){
                    binding.editEmailAtual.setText(email);


                }
            }
        });

    }
}