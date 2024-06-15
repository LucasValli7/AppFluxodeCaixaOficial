package br.com.lucas.valli.fluxodecaixa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.admin.v1.Index;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import br.com.lucas.valli.fluxodecaixa.Adapter.AdapterDadosEntrada;
import br.com.lucas.valli.fluxodecaixa.Model.DadosEntrada;
import br.com.lucas.valli.fluxodecaixa.Model.DadosSaidaE;
import br.com.lucas.valli.fluxodecaixa.databinding.ActivityTelaPrincipalEntradasBinding;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class TelaPrincipalEntradas extends AppCompatActivity {

    private ActivityTelaPrincipalEntradasBinding binding;
    private AdapterDadosEntrada adapterDados;
    private List<DadosEntrada> dadosEntradas;
    private FirebaseFirestore db;
    private String usuarioID;
    private Date x = new Date();
    private String dia = new SimpleDateFormat("dd", new Locale("pt", "BR")).format(x);
    private String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
    private String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);

    Locale ptbr = new Locale("pt", "BR");
    private Double vazio = Double.parseDouble("0.00");
    private InterstitialAd mInterstitialAd;


    @Override
    protected void onStart() {
        super.onStart();
        AbrirCalendario();
        checkConnection();

    }
    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            Log.d("NETCONEX", "SEM INTERNET");
            binding.progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(TelaPrincipalEntradas.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            RecuperarDadosListaEntradas();
            RecuperarTotalEntradas();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityTelaPrincipalEntradasBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());


        binding.tolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.AddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelaPrincipalEntradas.this, NovaEntrada.class);
                startActivity(intent);
            }
        });
    }
    public void Initialize(){
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        LoadInterticialAd();
    }
    public void LoadInterticialAd(){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-7099783455876849/3315422269", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d("ADSTESTE", "Ad was clicked.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d("ADSTESTE", "Ad dismissed fullscreen content.");
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e("ADSTESTE", "Ad failed to show fullscreen content.");
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d("ADSTESTE", "Ad recorded an impression.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d("ADSTESTE", "Ad showed fullscreen content.");
                            }
                        });
                        Log.i("ADSTESTE", "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d("ADSTESTE", loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });


    }
    public void ShowIntesticial(){
        if (mInterstitialAd != null) {
            mInterstitialAd.show(TelaPrincipalEntradas.this);
        } else {
            Log.d("ADSTESTE", "The interstitial ad wasn't ready yet.");
        }
    }
    public void AbrirCalendario() {
        Date x = new Date();
        String mes = new SimpleDateFormat("MMMM", new Locale("pt", "BR")).format(x);
        String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);

    }
    public void RecuperarDadosListaEntradas(){

        dadosEntradas = new ArrayList<>();
        adapterDados = new AdapterDadosEntrada(getApplicationContext(), dadosEntradas);
        binding.ListaTipoEntrada.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.ListaTipoEntrada.setHasFixedSize(true);
        binding.ListaTipoEntrada.setAdapter(adapterDados);

        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("nova entrada")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                DadosEntrada dadosEntrada = queryDocumentSnapshot.toObject(DadosEntrada.class);
                                dadosEntradas.add(dadosEntrada);
                                adapterDados.notifyDataSetChanged();
                                ItemToutchHelper();

                            }
                        }
                    }
                });
    }
    public void RecuperarTotalEntradas(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                .document("Total");

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();

                if (!documentSnapshot.exists()) {
                    String vazioFormatado = NumberFormat.getCurrencyInstance(ptbr).format(vazio);
                    binding.ValorTotalEntradas.setText(vazioFormatado);
                }else {
                    Double totalEntrada = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaEntrada"));
                    String totalEntradaFormat = NumberFormat.getCurrencyInstance(ptbr).format(totalEntrada);
                    binding.ValorTotalEntradas.setText(totalEntradaFormat);

                }
            }
        });
    }

    public void ItemToutchHelper(){
        // efeito Swipe
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getLayoutPosition();
                AlertDialog.Builder builder = new AlertDialog.Builder(TelaPrincipalEntradas.this);
                DadosEntrada item = dadosEntradas.get(position);

                builder.setTitle("Aviso Importante");
                builder.setMessage("Após a confirmação de exclusão, não será possível recuperar os dados apagados");
                builder.setCancelable(false);
                builder.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        binding.progressBar.setVisibility(View.VISIBLE);

                        DocumentReference documentReferenceAnual = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("entradas").collection("TotalEntradaAnual")
                                .document("Total");

                        DocumentReference documentReferenceV = db.collection(usuarioID).document(ano).collection(mes).document("entradas")
                                .collection("nova entrada").document(item.getId());

                        DocumentReference documentReferenceDiario = db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalEntradaDiario")
                                .document(dia);

                        documentReferenceAnual.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> taskTotalAnual) {
                                documentReferenceV.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskTotalV) {
                                        documentReferenceDiario.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> taskTotalDiario) {
                                                DocumentSnapshot documentSnapshotEntradaTotalV = taskTotalV.getResult();
                                                DocumentSnapshot documentSnapshotTotalDiario = taskTotalDiario.getResult();

                                                String dataSet = dia + "/" +mes+ "/" + ano;

                                                if (taskTotalAnual.isSuccessful()){
                                                    DocumentSnapshot documentSnapshotEntradaAnual = taskTotalAnual.getResult();
                                                    Double totalVDouble = Double.parseDouble(documentSnapshotEntradaTotalV.getString("ValorDeEntradaDouble"));

                                                    if (taskTotalDiario.isSuccessful()){
                                                        if (documentSnapshotEntradaTotalV.contains("dataDeEntrada")){
                                                            String data = documentSnapshotEntradaTotalV.getString("dataDeEntrada");
                                                            if (data.equals(dataSet)){
                                                                if (documentSnapshotTotalDiario.contains("ResultadoDaSomaEntradaDiario")){
                                                                    Double totalDiario = Double.parseDouble(documentSnapshotTotalDiario.getString("ResultadoDaSomaEntradaDiario"));
                                                                    Double op = totalDiario - totalVDouble;
                                                                    String opCvDiario = String.valueOf(op);

                                                                    db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalEntradaDiario")
                                                                            .document(dia).update("ResultadoDaSomaEntradaDiario", opCvDiario);

                                                                }
                                                            }

                                                        }
                                                    }

                                                    if (documentSnapshotEntradaAnual.contains("ResultadoTotalEntradaAnual"));{
                                                        Double totalAnualDouble = Double.parseDouble(documentSnapshotEntradaAnual.getString("ResultadoTotalEntradaAnual"));
                                                        Double opDouble = totalAnualDouble - totalVDouble;
                                                        String cvString = String.valueOf(opDouble);


                                                        db.collection(usuarioID).document(ano).collection("ResumoAnual").document("entradas").collection("TotalEntradaAnual")
                                                                .document("Total").update("ResultadoTotalEntradaAnual",cvString).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {

                                                                        //document reference total Geral
                                                                        DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                                                                                .document("Total");

                                                                        DocumentReference documentReferenceV = db.collection(usuarioID).document(ano).collection(mes).document("entradas")
                                                                                .collection("nova entrada").document(item.getId());

                                                                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                documentReferenceV.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskV) {
                                                                                        DocumentSnapshot documentSnapshot = task.getResult();
                                                                                        DocumentSnapshot documentSnapshotV = taskV.getResult();
                                                                                        if (documentSnapshot.exists()){
                                                                                            double totalEntrada = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaEntrada"));
                                                                                            double totalEntrada3 = Double.parseDouble(documentSnapshotV.getString("ValorDeEntradaDouble"));
                                                                                            Double op = totalEntrada - totalEntrada3;
                                                                                            if (op < 0){
                                                                                                op = 0.0;
                                                                                            }
                                                                                            String cv = String.valueOf(op);


                                                                                            db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                                                                                                    .document("Total").update("ResultadoDaSomaEntrada",cv);

                                                                                            db.collection(usuarioID).document(ano).collection(mes).document("entradas")
                                                                                                    .collection("nova entrada")
                                                                                                    .document(item.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            try {
                                                                                                                Toast.makeText(TelaPrincipalEntradas.this, "item excluido com sucesso", Toast.LENGTH_LONG).show();
                                                                                                                dadosEntradas.remove(position);
                                                                                                                adapterDados.notifyDataSetChanged();
                                                                                                                onStart();
                                                                                                                binding.progressBar.setVisibility(View.GONE);
                                                                                                            }catch (IndexOutOfBoundsException e){
                                                                                                                Log.i("ErroTry", "erro ao removerItem");
                                                                                                            }


                                                                                                        }
                                                                                                    });

                                                                                        }else {

                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                });



                                                    }
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });





                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        adapterDados.notifyItemChanged(viewHolder.getAdapterPosition());
                    }
                });
                builder.show();

            }

            public void onChildDraw (Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(TelaPrincipalEntradas.this, R.color.red))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete)
                        .addSwipeLeftLabel("Excluir")
                        .setSwipeLeftLabelColor(ContextCompat.getColor(TelaPrincipalEntradas.this, R.color.white))
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.ListaTipoEntrada);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemID = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}

