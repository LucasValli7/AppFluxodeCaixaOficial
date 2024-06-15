package br.com.lucas.valli.fluxodecaixa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.com.lucas.valli.fluxodecaixa.databinding.ActivityFormLoginBinding;

public class Form_Login extends AppCompatActivity {

    private ActivityFormLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityFormLoginBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        binding.txtCriarNovaConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Form_Login.this, Form_Cadastro.class);
                startActivity(intent);
            }
        });
        binding.btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginEmail = binding.editEmail.getText().toString();
                String loginSenha = binding.editSenha.getText().toString();

                if (loginEmail.isEmpty() || loginSenha.isEmpty()){
                    binding.txtMsgErro.setText("Preencha todos os campos!");
                }else {
                    binding.txtMsgErro.setText("");
                    AutenticarUsuario();
                }

            }
        });
        binding.txtEsqueceuSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = binding.editEmail.getText().toString();
                if (email.isEmpty()){
                    Snackbar snackbar = Snackbar.make(v, "Preencha o campo e-mail", Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                    snackbar.show();
                }else {
                    ResetSenha();
                    AlertDialog.Builder builder = new AlertDialog.Builder(Form_Login.this);
                    builder.setTitle("Atenção! ");
                    builder.setMessage("Enviamos um e-mail para " + email + "\n\n"+ "Dentro de instantes, acesse o seu e-mail e clique no link para redefinir sua senha.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {



                        }
                    }); builder.show();
                }
            }
        });

    }

    public void ResetSenha(){
        String email = binding.editEmail.getText().toString();
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioAtual != null){
            IniciarTelaPrincipal();
        }
    }

    public void AutenticarUsuario(){
        String loginEmail = binding.editEmail.getText().toString();
        String loginSenha = binding.editSenha.getText().toString();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(loginEmail, loginSenha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    binding.progressBar.setVisibility(View.VISIBLE);

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            IniciarTelaPrincipal();
                        }
                    },1000);
                }else{
                    String erro;

                    try {
                       throw task.getException();

                    }catch (Exception e){
                        erro ="Erro ao logar usuário";
                    }
                    binding.txtMsgErro.setText(erro);
                }
            }
        });

    }

    public void IniciarTelaPrincipal(){

        Intent intent = new Intent(Form_Login.this, TelaPrincipal.class);
        startActivity(intent);
        finish();
    }

}