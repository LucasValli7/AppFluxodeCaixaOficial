package br.com.lucas.valli.fluxodecaixa.Atividades;

import static br.com.lucas.valli.fluxodecaixa.Classes.ConversorDeMoeda.formatPriceSave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.lucas.valli.fluxodecaixa.Adapter.AdapterDadosEntrada;
import br.com.lucas.valli.fluxodecaixa.Classes.ConversorDeMoeda;
import br.com.lucas.valli.fluxodecaixa.Model.DadosEntrada;
import br.com.lucas.valli.fluxodecaixa.R;
import br.com.lucas.valli.fluxodecaixa.RecyclerItemClickListener.RecyclerItemClickListener;
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

        binding.ListaTipoEntrada.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), binding.ListaTipoEntrada,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                         DadosEntrada item = dadosEntradas.get(position);
                        binding.progressBar.setVisibility(View.VISIBLE);

                        // Infla o layout personalizado
                        LayoutInflater inflater = getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.dialog_editar_movimentacao, null);
                        EditText txt_tipo = dialogView.findViewById(R.id.edit_tipoEntrada);
                        EditText txt_valor = dialogView.findViewById(R.id.edit_valorEntrada);
                        EditText txt_formPagamento = dialogView.findViewById(R.id.form_pagament);

                        // Adiciona o TextWatcher ao TextView
                        if (txt_valor != null) {
                            txt_valor.addTextChangedListener(new ConversorDeMoeda(dialogView.findViewById(R.id.edit_valorEntrada)));
                        } else {
                            Log.e("TAG", "TextView é nulo");
                        }
                        DocumentReference documentReferenceDados = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("nova entrada")
                                .document(item.getId());

                        documentReferenceDados.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.contains("TipoDeEntrada") && documentSnapshot.contains("ValorDeEntrada")
                                        && documentSnapshot.contains("dataDeEntrada")) {

                                    String valorEntrada = documentSnapshot.getString("ValorDeEntrada");
                                    String dataEntrada = documentSnapshot.getString("dataDeEntrada");
                                    String valorEntradaDouble = documentSnapshot.getString("ValorDeEntradaDouble");
                                    String TipoDeEntrada = documentSnapshot.getString("TipoDeEntrada");
                                    String formaPagamento = documentSnapshot.getString("formPagamento");
                                    String idMovimentacao = documentSnapshot.getString("idMovimentacao");


                                    txt_tipo.setText(TipoDeEntrada);
                                    txt_valor.setText(valorEntrada);
                                    txt_formPagamento.setText(formaPagamento);

                                }
                            }
                        });

                        // Cria e mostra o AlertDialog
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TelaPrincipalEntradas.this);
                        builder.setView(dialogView)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                                        if (networkInfo == null){
                                            Log.d("NETCONEX", "SEM INTERNET");
                                            binding.progressBar.setVisibility(View.VISIBLE);
                                            Toast.makeText(TelaPrincipalEntradas.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();

                                        }else {
                                            binding.progressBar.setVisibility(View.GONE);

                                            DocumentReference documentReferenceTotalDiario = db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalEntradaDiario")
                                                    .document(dia);

                                            DocumentReference documentReferenceTotalMensal = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                                                    .document("Total");

                                            DocumentReference documentReferenceTotalAnual = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("entradas").collection("TotalEntradaAnual")
                                                    .document("Total");
                                            DocumentReference documentReferenceResumo = db.collection(usuarioID).document("resumoCaixa").collection("ResumoDeCaixa").document("entradas").collection("total")
                                                    .document("ResumoTotal");

                                            documentReferenceResumo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> taskResumo) {
                                                    documentReferenceTotalAnual.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> taskAnual) {
                                                            documentReferenceTotalDiario.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> taskDiario) {
                                                                    documentReferenceTotalMensal.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> taskMensal) {
                                                                            documentReferenceDados.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<DocumentSnapshot> taskDados) {
                                                                                    DocumentSnapshot documentSnapshot = taskDados.getResult();
                                                                                    DocumentSnapshot documentSnapshotTotalMensal = taskMensal.getResult();
                                                                                    DocumentSnapshot documentSnapshotTotalDiario = taskDiario.getResult();
                                                                                    DocumentSnapshot documentSnapshotTotalAual = taskAnual.getResult();
                                                                                    DocumentSnapshot documentSnapshotTotalResumo = taskResumo.getResult();
                                                                                    if (documentSnapshot.contains("TipoDeEntrada") && documentSnapshot.contains("ValorDeEntrada")
                                                                                            && documentSnapshot.contains("dataDeEntrada")) {

                                                                                        String dataEntrada = documentSnapshot.getString("dataDeEntrada");
                                                                                        String valorEntradaDouble = documentSnapshot.getString("ValorDeEntradaDouble");
                                                                                        String TipoDeEntrada = documentSnapshot.getString("TipoDeEntrada");
                                                                                        String formaPagamento = documentSnapshot.getString("formPagamento");


                                                                                        //texto campos dialogoCustom
                                                                                        String txt_tipoString =String.valueOf(txt_tipo.getText());
                                                                                        String txt_valorString = String.valueOf(txt_valor.getText());
                                                                                        String txt_formPagamentoString =String.valueOf(txt_formPagamento.getText());

                                                                                        //entradas convertidas para Double
                                                                                        String str = formatPriceSave(txt_valorString);
                                                                                        Double valorDoubleEditText = Double.parseDouble(str);
                                                                                        Double valorDoubleDb = Double.parseDouble(valorEntradaDouble);

                                                                                        //mesmo valor double para String
                                                                                        String txt_valor = String.valueOf(valorDoubleEditText);

                                                                                        //converter formato moeda
                                                                                        String ValorEntradaConvertido = NumberFormat.getCurrencyInstance(ptbr).format(valorDoubleEditText);

                                                                                        Log.d("PAGAMENTOOK", txt_tipoString + "\n" + valorDoubleDb + "\n" +txt_formPagamentoString);

                                                                                        if (!txt_tipoString.isEmpty() && !txt_valor.isEmpty()
                                                                                                && !txt_formPagamentoString.isEmpty()) {

                                                                                            if (!txt_tipoString.equals(TipoDeEntrada)) {
                                                                                                db.collection(usuarioID).document(ano).collection(mes)
                                                                                                        .document("entradas")
                                                                                                        .collection("nova entrada")
                                                                                                        .document(item.getId()).update("TipoDeEntrada", txt_tipoString);
                                                                                            }

                                                                                            if (!txt_valor.equals(valorEntradaDouble)) {

                                                                                                if (valorDoubleEditText < valorDoubleDb) {
                                                                                                    Double op = valorDoubleDb - valorDoubleEditText;

                                                                                                    if (documentSnapshotTotalMensal.contains("ResultadoDaSomaEntrada")) {
                                                                                                        Double valorTotal = Double.parseDouble(documentSnapshotTotalMensal.getString("ResultadoDaSomaEntrada"));
                                                                                                        Double op2 = valorTotal - op;
                                                                                                        db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                                                                                                                .document("Total").update("ResultadoDaSomaEntrada",
                                                                                                                        String.valueOf(op2));

                                                                                                        db.collection(usuarioID).document(ano).collection("ResumoAnual").document("entradas").collection("TotalEntradaAnual")
                                                                                                                .document("Total").update("ResultadoTotalEntradaAnual",
                                                                                                                        String.valueOf(op2));

                                                                                                        documentReferenceResumo.update("ResultadoTotal", String.valueOf(op2));

                                                                                                        db.collection(usuarioID).document(ano).collection(mes)
                                                                                                                .document("entradas")
                                                                                                                .collection("nova entrada")
                                                                                                                .document(item.getId()).update("ValorDeEntrada", ValorEntradaConvertido, "ValorDeEntradaDouble", txt_valor);
                                                                                                    }

                                                                                                } else if (valorDoubleEditText > valorDoubleDb) {
                                                                                                    Double op = valorDoubleEditText - valorDoubleDb;

                                                                                                    if (documentSnapshotTotalMensal.contains("ResultadoDaSomaEntrada")) {
                                                                                                        Double valorTotal = Double.parseDouble(documentSnapshotTotalMensal.getString("ResultadoDaSomaEntrada"));
                                                                                                        Double op2 = op + valorTotal;

                                                                                                        db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                                                                                                                .document("Total").update("ResultadoDaSomaEntrada",
                                                                                                                        String.valueOf(op2));

                                                                                                        db.collection(usuarioID).document(ano).collection("ResumoAnual").document("entradas").collection("TotalEntradaAnual")
                                                                                                                .document("Total").update("ResultadoTotalEntradaAnual",
                                                                                                                        String.valueOf(op2));

                                                                                                        documentReferenceResumo.update("ResultadoTotal", String.valueOf(op2));

                                                                                                        db.collection(usuarioID).document(ano).collection(mes)
                                                                                                                .document("entradas")
                                                                                                                .collection("nova entrada")
                                                                                                                .document(item.getId()).update("ValorDeEntrada", ValorEntradaConvertido, "ValorDeEntradaDouble", txt_valor);

                                                                                                    }



                                                                                                }

                                                                                            }

                                                                                            if (!txt_formPagamentoString.equals(formaPagamento)) {
                                                                                                db.collection(usuarioID).document(ano).collection(mes)
                                                                                                        .document("entradas")
                                                                                                        .collection("nova entrada")
                                                                                                        .document(item.getId()).update("formPagamento", txt_formPagamentoString);
                                                                                            }

                                                                                            String dataAtual = dia+"/"+mes+"/"+ano;
                                                                                            // total diario
                                                                                            if (dataEntrada.equals(dataAtual)){
                                                                                                if (!txt_valor.equals(valorEntradaDouble)) {

                                                                                                    if (valorDoubleEditText < valorDoubleDb) {
                                                                                                        Double op = valorDoubleDb - valorDoubleEditText;

                                                                                                        if (documentSnapshotTotalDiario.contains("ResultadoDaSomaEntradaDiario")) {
                                                                                                            Double valorTotal = Double.parseDouble(documentSnapshotTotalDiario.getString("ResultadoDaSomaEntradaDiario"));
                                                                                                            Double op2 = valorTotal - op;
                                                                                                            db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalEntradaDiario")
                                                                                                                    .document(dia).update("ResultadoDaSomaEntradaDiario",
                                                                                                                            String.valueOf(op2));


                                                                                                        }

                                                                                                    } else if (valorDoubleEditText > valorDoubleDb) {
                                                                                                        Double op = valorDoubleEditText - valorDoubleDb;

                                                                                                        if (documentSnapshotTotalDiario.contains("ResultadoDaSomaEntradaDiario")) {
                                                                                                            Double valorTotal = Double.parseDouble(documentSnapshotTotalDiario.getString("ResultadoDaSomaEntradaDiario"));
                                                                                                            Double op2 = op + valorTotal;

                                                                                                            db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalEntradaDiario")
                                                                                                                    .document(dia).update("ResultadoDaSomaEntradaDiario",
                                                                                                                            String.valueOf(op2));


                                                                                                        }



                                                                                                    }

                                                                                                }
                                                                                            }

                                                                                            onStart();
                                                                                        }else {
                                                                                            Toast.makeText(TelaPrincipalEntradas.this, "Preencha todos os campos", Toast.LENGTH_LONG).show();
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
                                                }
                                            });

                                        }



                                    }
                                })
                                .setNegativeButton("Cancelar", null);
                        binding.progressBar.setVisibility(View.GONE);

                        androidx.appcompat.app.AlertDialog dialog = builder.create();
                        dialog.show();




                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }));
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
       binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
    }

    private void refreshData() {
        new Handler().postDelayed(() -> {
            checkConnection();
            binding.swipeRefreshLayout.setRefreshing(false);
        }, 1000); // Simula um delay de 1 segundos
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

                        DocumentReference documentReferenceResumo = db.collection(usuarioID).document("resumoCaixa").collection("ResumoDeCaixa").document("entradas").collection("total")
                                .document("ResumoTotal");

                        documentReferenceResumo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> taskResumo) {
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
                                                        DocumentSnapshot documentSnapshotTotalResumo= taskResumo.getResult();

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

                                                            if (documentSnapshotTotalResumo.contains("ResultadoTotal")){
                                                                Double totalResumoDouble = Double.parseDouble(documentSnapshotTotalResumo.getString("ResultadoTotal"));
                                                                Double opDouble = totalResumoDouble - totalVDouble;
                                                                String cvString = String.valueOf(opDouble);
                                                                documentReferenceResumo.update("ResultadoTotal",cvString);
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

                if (isCurrentlyActive) {
                    binding.swipeRefreshLayout.setEnabled(false);
                } else {
                    binding.swipeRefreshLayout.setEnabled(true);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.ListaTipoEntrada);
    }

}

