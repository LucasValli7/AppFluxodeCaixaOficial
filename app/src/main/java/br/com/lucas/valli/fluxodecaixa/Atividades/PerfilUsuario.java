package br.com.lucas.valli.fluxodecaixa.Atividades;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import br.com.lucas.valli.fluxodecaixa.databinding.ActivityPerfilUsuarioBinding;

public class PerfilUsuario extends AppCompatActivity {

    private ActivityPerfilUsuarioBinding binding;
    private String usuarioID;
    private Uri mSelecionarUri;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onStart() {
        super.onStart();
        checkConnection();
    }
    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            Log.d("NETCONEX", "SEM INTERNET");
            Toast.makeText(PerfilUsuario.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.VISIBLE);
            return false;
        }else {
            RecuperarDadosUsuario();
            Buttons();
            binding.progressBar.setVisibility(View.GONE);
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
            Log.d("NETCONEX", "WIFI");
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
            Log.d("NETCONEX", "DADOS");
        }
        return networkInfo.isConnected();

    }
    public void Buttons(){
        binding.btnSelecionarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelecionarFoto();

            }
        });
        binding.editAtualizarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PerfilUsuario.this, EditarNome.class);
                startActivity(intent);
            }
        });
        binding.editAtualizarEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PerfilUsuario.this, EditarEmail.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPerfilUsuarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                            binding.circleUsuarioPerfil.setImageURI(mSelecionarUri);
                            AlterarFotoUsuario();
                        }catch (Exception exception){
                            exception.printStackTrace();

                        }
                    }
                }
            });


    public void AlterarFotoUsuario(){

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
                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                                Map<String,Object> usuarios = new HashMap<>();
                                usuarios.put("foto",foto);

                                usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                db.collection(usuarioID).document("usuario")
                                        .update("foto", foto).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        binding.progressBar.setVisibility(View.VISIBLE);

                                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                binding.progressBar.setVisibility(View.INVISIBLE);
                                                Toast.makeText(PerfilUsuario.this, "Foto de perfil alterada com sucesso", Toast.LENGTH_LONG).show();
                                                RecuperarDadosUsuario();
                                            }
                                        },1000);
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
    public void SelecionarFoto(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activityResultLauncher.launch(intent);
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
                    binding.editAtualizarNome.setText(documentSnapshot.getString("nome"));
                    binding.editAtualizarEmail.setText(email);
                    Glide.with(getApplicationContext()).load(documentSnapshot.getString("foto")).into(binding.circleUsuarioPerfil);

                }
            }
        });

    }
}