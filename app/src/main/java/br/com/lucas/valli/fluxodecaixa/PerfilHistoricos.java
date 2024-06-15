package br.com.lucas.valli.fluxodecaixa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.lucas.valli.fluxodecaixa.Adapter.AdapterHistoricoEntrada;
import br.com.lucas.valli.fluxodecaixa.Adapter.AdapterHistoricoSaida;
import br.com.lucas.valli.fluxodecaixa.Adapter.ViewPagerAdapter;
import br.com.lucas.valli.fluxodecaixa.Model.HistoricoEntrada;
import br.com.lucas.valli.fluxodecaixa.Model.HistoricoSaida;
import br.com.lucas.valli.fluxodecaixa.databinding.ActivityPerfilHistoricosBinding;

public class PerfilHistoricos extends AppCompatActivity {
    private ActivityPerfilHistoricosBinding binding;
    private Double vazio = Double.parseDouble("0.00");

    private String usuarioID;
    private AdapterHistoricoSaida adapterHistoricoSaida;
    private List<HistoricoSaida> historicoSaidaList;
    private Date x = new Date();
    private String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
    private String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);
    String dia = new SimpleDateFormat("dd", new Locale("pt", "BR")).format(x);
    private AdapterHistoricoEntrada adapterHistoricoEntrada;
    private List<HistoricoEntrada> historicoEntradaList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Locale ptBr = new Locale("pt", "BR");
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onStart() {
        super.onStart();
        checkConnection();
        Initialize();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPerfilHistoricosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.tolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.btnLimpar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialogDelet();
            }
        });
        binding.btnPesquisa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog();
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PerfilHistoricos.this, TelaPrincipal.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            Log.d("NETCONEX", "SEM INTERNET");
            Toast.makeText(PerfilHistoricos.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            RecuperarDadosIniciar();
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
            Log.d("NETCONEX", "WIFI");
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
            Log.d("NETCONEX", "DADOS");
        }
        return networkInfo.isConnected();

    }

    private void showCustomDialogDelet(){
        // Infla o layout personalizado
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_custom_delet, null);

        // Configura o Spinner de anos
        Spinner yearSpinner = dialogView.findViewById(R.id.year_spinner);
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(this,
                R.array.years_array, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        // Configura o Spinner de meses
        Spinner monthSpinner = dialogView.findViewById(R.id.month_spinner);
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        // Configura o Spinner de tipo
        Spinner typeSpinner = dialogView.findViewById(R.id.type_spinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.type_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);


        // Cria e mostra o AlertDialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Excluir histórico")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                        if (networkInfo == null){
                            Log.d("NETCONEX", "SEM INTERNET");
                            Toast.makeText(PerfilHistoricos.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();

                        }else {
                            String ano = yearSpinner.getSelectedItem().toString();
                            String mes = monthSpinner.getSelectedItem().toString();
                            String tipo = typeSpinner.getSelectedItem().toString();


                            if (tipo.equals("Saída")){


                                binding.progressBar.setVisibility(View.VISIBLE);
                                usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                db = FirebaseFirestore.getInstance();

                                DocumentReference documentReference= db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de Saidas")
                                        .document("Total");
                                DocumentReference documentReferenceE= db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasE")
                                        .document("Total");
                                DocumentReference documentReferenceP= db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasP")
                                        .document("Total");
                                DocumentReference documentReferenceD= db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasD")
                                        .document("Total");
                                DocumentReference documentReferenceAnualSaidas = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("saidas").collection("TotalSaidaAnual")
                                        .document("Total");


                                db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("nova saida").document("categoria").collection("Desejos Pessoais")
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> taskD) {
                                                db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("nova saida").document("categoria").collection("Gastos Essenciais")
                                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> taskE) {
                                                                db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("nova saida").document("categoria").collection("Pagamento de Dívidas")
                                                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<QuerySnapshot> taskP) {

                                                                                if (taskE.isSuccessful() || taskD.isSuccessful() || taskP.isSuccessful()){

                                                                                    binding.progressBar.setVisibility(View.GONE);
                                                                                    ShowIntesticial();
                                                                                    Toast.makeText(PerfilHistoricos.this, "Saídas deletadas com sucesso", Toast.LENGTH_SHORT).show();

                                                                                    db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalSaidasDiario")
                                                                                            .document(dia).delete();

                                                                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                            DocumentSnapshot documentSnapshot = task.getResult();
                                                                                            if (documentSnapshot.exists()){
                                                                                                Double totalMensal = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaSaida"));
                                                                                                documentReferenceAnualSaidas.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                        DocumentSnapshot documentSnapshotAnual = task.getResult();
                                                                                                        Double totalAnual = Double.parseDouble(documentSnapshotAnual.getString("ResultadoTotalSaidaAnual"));
                                                                                                        Double op = totalAnual - totalMensal;
                                                                                                        String opCv = String.valueOf(op);
                                                                                                        db.collection(usuarioID).document(ano).collection("ResumoAnual").document("saidas").collection("TotalSaidaAnual")
                                                                                                                .document("Total").update("ResultadoTotalSaidaAnual", opCv).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        if (task.isSuccessful()){
                                                                                                                            documentReference.delete();
                                                                                                                            onStart();

                                                                                                                        }
                                                                                                                    }
                                                                                                                });
                                                                                                    }
                                                                                                });


                                                                                            }
                                                                                        }
                                                                                    });

                                                                                }

                                                                                if (taskE.isSuccessful()) {
                                                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : taskE.getResult()) {
                                                                                        queryDocumentSnapshot.getReference().delete();
                                                                                    }

                                                                                    documentReferenceE.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                            documentReferenceE.delete();
                                                                                        }
                                                                                    });
                                                                                }

                                                                                if (taskD.isSuccessful()) {
                                                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : taskD.getResult()) {
                                                                                        queryDocumentSnapshot.getReference().delete();
                                                                                    }

                                                                                    documentReferenceD.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                            documentReferenceD.delete();
                                                                                        }
                                                                                    });
                                                                                }

                                                                                if (taskP.isSuccessful()) {
                                                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : taskP.getResult()) {
                                                                                        queryDocumentSnapshot.getReference().delete();

                                                                                        documentReferenceP.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                documentReferenceP.delete();
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
                            else if (tipo.equals("Entrada")) {

                                binding.progressBar.setVisibility(View.VISIBLE);
                                usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                db = FirebaseFirestore.getInstance();

                                DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                                        .document("Total");

                                DocumentReference documentReferenceEntradasAnual = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("entradas").collection("TotalEntradaAnual")
                                        .document("Total");


                                db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("nova entrada")
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                        queryDocumentSnapshot.getReference().delete();
                                                    }
                                                    binding.progressBar.setVisibility(View.GONE);
                                                    ShowIntesticial();
                                                    Toast.makeText(PerfilHistoricos.this, "Entradas deletadas com sucesso", Toast.LENGTH_SHORT).show();

                                                    db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalEntradaDiario")
                                                            .document(dia).delete();

                                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            DocumentSnapshot documentSnapshot = task.getResult();
                                                            if (documentSnapshot.exists()){
                                                                Double totalMensal = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaEntrada"));
                                                                documentReferenceEntradasAnual.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        DocumentSnapshot documentSnapshotAnual = task.getResult();
                                                                        Double totalAnual = Double.parseDouble(documentSnapshotAnual.getString("ResultadoTotalEntradaAnual"));
                                                                        Double op = totalAnual - totalMensal;
                                                                        String opCv = String.valueOf(op);
                                                                        db.collection(usuarioID).document(ano).collection("ResumoAnual").document("entradas").collection("TotalEntradaAnual")
                                                                                .document("Total").update("ResultadoTotalEntradaAnual", opCv).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            documentReference.delete();
                                                                                            onStart();
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }


                                            }
                                        });

                            }
                        }



                    }
                })
                .setNegativeButton("Cancel", null);
        binding.progressBar.setVisibility(View.GONE);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void RecuperarDadosIniciar(){
        binding.progressBar.setVisibility(View.VISIBLE);

        historicoEntradaList = new ArrayList<>();
        adapterHistoricoEntrada = new AdapterHistoricoEntrada(getApplicationContext(), historicoEntradaList);
        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.RecyclerView.setAdapter(adapterHistoricoEntrada);

        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("nova entrada")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                HistoricoEntrada historicoPerfil = queryDocumentSnapshot.toObject(HistoricoEntrada.class);
                                historicoEntradaList.add(historicoPerfil);
                                adapterHistoricoEntrada.notifyDataSetChanged();

                            }
                            binding.progressBar.setVisibility(View.GONE);
                            ShowIntesticial();
                            DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                                    .document("Total");
                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if (!documentSnapshot.exists()) {
                                        String vazioFormatado = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                                        binding.txtTotal.setText("Total de entradas mensal: ");
                                        binding.ValorTotal.setText(vazioFormatado);
                                        Toast.makeText(PerfilHistoricos.this, "Não há entradas disponíveis para a data atual", Toast.LENGTH_SHORT).show();

                                    }else {
                                        Double totalMovimentacaoEntrada = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaEntrada"));
                                        String totalMovimentacaoEntradaFormat = NumberFormat.getCurrencyInstance(ptBr).format(totalMovimentacaoEntrada);
                                        binding.txtTotal.setText("Total de entradas mensal: ");
                                        binding.ValorTotal.setText(totalMovimentacaoEntradaFormat);


                                    }
                                }
                            });
                        }else {

                        }
                    }
                });
    }
    private void showCustomDialog() {
        // Infla o layout personalizado
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);

        // Configura o Spinner de anos
        Spinner yearSpinner = dialogView.findViewById(R.id.year_spinner);
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(this,
                R.array.years_array, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        // Configura o Spinner de meses
        Spinner monthSpinner = dialogView.findViewById(R.id.month_spinner);
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        // Configura o Spinner de tipo
        Spinner typeSpinner = dialogView.findViewById(R.id.type_spinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.type_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        // Configura o Spinner de tipo
        Spinner categorySpinner = dialogView.findViewById(R.id.category_spinner);
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Configura o RadioGroup de opções
        TextView txtTitulo = dialogView.findViewById(R.id.titulo_category);


        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                if (selectedType.equals("Saída")) {
                    txtTitulo.setVisibility(View.VISIBLE);
                    categorySpinner.setVisibility(View.VISIBLE);
                } else {
                    txtTitulo.setVisibility(View.GONE);
                    categorySpinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Não faz nada
            }
        });


        // Cria e mostra o AlertDialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Filtro de pesquisa")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                        if (networkInfo == null){
                            Log.d("NETCONEX", "SEM INTERNET");
                            Toast.makeText(PerfilHistoricos.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();

                        }else {
                            String ano = yearSpinner.getSelectedItem().toString();
                            String mes = monthSpinner.getSelectedItem().toString();
                            String tipo = typeSpinner.getSelectedItem().toString();


                            if (tipo.equals("Saída")){
                                String categoria = categorySpinner.getSelectedItem().toString();

                                binding.progressBar.setVisibility(View.VISIBLE);

                                historicoSaidaList = new ArrayList<>();
                                adapterHistoricoSaida = new AdapterHistoricoSaida(getApplicationContext(), historicoSaidaList);
                                binding.RecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                binding.RecyclerView.setAdapter(adapterHistoricoSaida);

                                usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                db = FirebaseFirestore.getInstance();
                                db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                        .collection("nova saida").document("categoria").collection(categoria)
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                        HistoricoSaida historicoPerfil = queryDocumentSnapshot.toObject(HistoricoSaida.class);
                                                        historicoSaidaList.add(historicoPerfil);
                                                        adapterHistoricoSaida.notifyDataSetChanged();

                                                    }

                                                    binding.progressBar.setVisibility(View.GONE);
                                                    ShowIntesticial();
                                                    DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de Saidas")
                                                            .document("Total");
                                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            DocumentSnapshot documentSnapshot = task.getResult();
                                                            if (!documentSnapshot.exists()) {
                                                                String vazioFormatado = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                                                                binding.txtTotal.setText("Total de saídas mensal: ");
                                                                binding.ValorTotal.setText(vazioFormatado);
                                                                Toast.makeText(PerfilHistoricos.this, "Não há saídas disponíveis para a data escolhida", Toast.LENGTH_SHORT).show();

                                                            }else {
                                                                Double totalMovimentacaoSaida = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaSaida"));
                                                                String totalMovimentacaoSaidaFormat = NumberFormat.getCurrencyInstance(ptBr).format(totalMovimentacaoSaida);
                                                                binding.txtTotal.setText("Total de saídas mensal: ");
                                                                binding.ValorTotal.setText(totalMovimentacaoSaidaFormat);


                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                            } else if (tipo.equals("Entrada")) {

                                binding.progressBar.setVisibility(View.VISIBLE);

                                historicoEntradaList = new ArrayList<>();
                                adapterHistoricoEntrada = new AdapterHistoricoEntrada(getApplicationContext(), historicoEntradaList);
                                binding.RecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                binding.RecyclerView.setAdapter(adapterHistoricoEntrada);

                                usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                db = FirebaseFirestore.getInstance();
                                db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("nova entrada")
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                        HistoricoEntrada historicoPerfil = queryDocumentSnapshot.toObject(HistoricoEntrada.class);
                                                        historicoEntradaList.add(historicoPerfil);
                                                        adapterHistoricoEntrada.notifyDataSetChanged();

                                                    }
                                                    binding.progressBar.setVisibility(View.GONE);
                                                    ShowIntesticial();
                                                    DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                                                            .document("Total");
                                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            DocumentSnapshot documentSnapshot = task.getResult();
                                                            if (!documentSnapshot.exists()) {
                                                                String vazioFormatado = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                                                                binding.txtTotal.setText("Total de entradas mensal: ");
                                                                binding.ValorTotal.setText(vazioFormatado);
                                                                Toast.makeText(PerfilHistoricos.this, "Não há entradas disponíveis para a data escolhida", Toast.LENGTH_SHORT).show();

                                                            }else {
                                                                Double totalMovimentacaoEntrada = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaEntrada"));
                                                                String totalMovimentacaoEntradaFormat = NumberFormat.getCurrencyInstance(ptBr).format(totalMovimentacaoEntrada);
                                                                binding.txtTotal.setText("Total de entradas mensal: ");
                                                                binding.ValorTotal.setText(totalMovimentacaoEntradaFormat);


                                                            }
                                                        }
                                                    });
                                                }else {

                                                }
                                            }
                                        });

                            }
                        }





                    }
                })
                .setNegativeButton("Cancel", null);
        binding.progressBar.setVisibility(View.GONE);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
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

        InterstitialAd.load(this,String.valueOf(R.string.admob_id_teste), adRequest,
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
            mInterstitialAd.show(PerfilHistoricos.this);
        } else {
            Log.d("ADSTESTE", "The interstitial ad wasn't ready yet.");
        }
    }

}
