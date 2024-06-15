package br.com.lucas.valli.fluxodecaixa;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import br.com.lucas.valli.fluxodecaixa.databinding.ActivityFormCadastroBinding;

public class Form_Cadastro extends AppCompatActivity {
    private ActivityFormCadastroBinding binding;
    private String usuarioID;
    private Uri mSelecionarUri;


    @Override
    protected void onStart() {
        super.onStart();
        checkConnection();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFormCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SelecionarFoto();

        binding.editCadastroNome.addTextChangedListener(cadastroTextWatcher);
        binding.editCadastroEmail.addTextChangedListener(cadastroTextWatcher);
        binding.editCadastroSenha.addTextChangedListener(cadastroTextWatcher);


        binding.btnSelecionarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelecionarFoto();
            }
        });

        binding.btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CadastrarUsuario(v);
            }
        });

    }
    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            Log.d("NETCONEX", "SEM INTERNET");
            Toast.makeText(Form_Cadastro.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
            Log.d("NETCONEX", "WIFI");
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
            Log.d("NETCONEX", "DADOS");
        }
        return networkInfo.isConnected();

    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent dados = result.getData();
                        mSelecionarUri = dados.getData();

                        try {
                            binding.CircleImageView.setImageURI(mSelecionarUri);


                        }catch (Exception exception){
                            exception.printStackTrace();


                        }
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        finish();
                        Toast.makeText(Form_Cadastro.this, "Por favor, escolha sua foto de perfil", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    public void SelecionarFoto(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activityResultLauncher.launch(intent);
    }

    TextWatcher cadastroTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String nome = binding.editCadastroNome.getText().toString();
            String email = binding.editCadastroEmail.getText().toString();
            String senha = binding.editCadastroSenha.getText().toString();

            if (!nome.isEmpty() && !email.isEmpty() && !senha.isEmpty()){

                binding.btnCadastrar.setEnabled(true);
                binding.btnCadastrar.setBackgroundDrawable(getDrawable(R.drawable.button));
            } else{
                binding.btnCadastrar.setEnabled(false);
                binding.btnCadastrar.setBackgroundDrawable(getDrawable(R.drawable.button_desativado));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    public void CadastrarUsuario(View view){

        String email = binding.editCadastroEmail.getText().toString();
        String senha = binding.editCadastroSenha.getText().toString();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    SalvarDadosUsuario();
                    SalvarFotoUsuario();
                    Snackbar snackbar = Snackbar.make(view,"Cadastro realizado com SUCESSO!",Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {finish();}
                            });
                    snackbar.show();
                }else {
                    String erro;

                    try {
                        throw task.getException();

                    } catch (FirebaseAuthWeakPasswordException e) {
                        erro = "Coloque uma senha com no mínimo 6 caractéres!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        erro = "E-mail inválido.";
                    }catch (FirebaseAuthUserCollisionException e){
                        erro = "Esta conta já está cadastrada.";
                    }catch (FirebaseNetworkException e){
                        erro = "Sem conexão com a Internet";
                    } catch (Exception e) {
                        erro = "Erro ao cadastrar usuário";
                    }
                    binding.txtMsgErro.setText(erro);
                }

            }
        });
}

    public void SalvarFotoUsuario(){

        String nomeArquivo = UUID.randomUUID().toString();


        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("/imagens/"+ nomeArquivo);
        storageReference.putFile(mSelecionarUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                String foto = uri.toString();
                                String nome = binding.editCadastroNome.getText().toString();
                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                                Map<String,Object> usuarios = new HashMap<>();
                                usuarios.put("foto",foto);
                                usuarios.put("nome",nome);




                                usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DocumentReference documentReference = db.collection(usuarioID).document("usuario");
                                documentReference.set(usuarios).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {



                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });



    }
    public void SalvarDadosUsuario(){

        String nome = binding.editCadastroNome.getText().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String,Object> usuarios = new HashMap<>();
        usuarios.put("nome",nome);


        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference documentReference = db.collection(usuarioID).document("usuario");
        documentReference.set(usuarios).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


}