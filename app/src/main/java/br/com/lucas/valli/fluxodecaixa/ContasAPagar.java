package br.com.lucas.valli.fluxodecaixa;

import static br.com.lucas.valli.fluxodecaixa.Model.ConversorDeMoeda.formatPriceSave;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import br.com.lucas.valli.fluxodecaixa.Adapter.AdapterContasApagar;
import br.com.lucas.valli.fluxodecaixa.Adapter.AdapterHistoricoEntrada;
import br.com.lucas.valli.fluxodecaixa.Adapter.AdapterHistoricoSaida;
import br.com.lucas.valli.fluxodecaixa.Model.ContasApagar;
import br.com.lucas.valli.fluxodecaixa.Model.HistoricoEntrada;
import br.com.lucas.valli.fluxodecaixa.Model.HistoricoSaida;
import br.com.lucas.valli.fluxodecaixa.databinding.ActivityContasApagarBinding;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class ContasAPagar extends AppCompatActivity {
    private ActivityContasApagarBinding binding;
    private AdapterContasApagar adapterContasApagar;
    private List<ContasApagar> contasApagar;
    private FirebaseFirestore db;
    private String usuarioID;
    private final Date x = new Date();
    private final String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
    private final String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);
    private final String dia = new SimpleDateFormat("dd", new Locale("pt", "BR")).format(x);
    private String testeData = dia + mes + ano;
    Locale ptbr = new Locale("pt", "BR");
    private Double vazio = Double.parseDouble("0.00");
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItem;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityContasApagarBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        binding.tolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }
    public void BtnPesquisar(){
        binding.btnPesquisa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog();
            }
        });
    }

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
            binding.progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(ContasAPagar.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            RecuperarDadosContasAPagarI();
            RecuperarTotalContasAPagarI();
            BtnPesquisar();
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
    public void RecuperarTotalContasAPagarI(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                .collection("Total Contas A Pagar").document("Total");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (!documentSnapshot.exists()) {
                    String vazioFormatado = NumberFormat.getCurrencyInstance(ptbr).format(vazio);
                    binding.ValorTotal.setText(vazioFormatado);


                }else {
                    Double totalContasP = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaSaidaC"));
                    String totalContasPFormat = NumberFormat.getCurrencyInstance(ptbr).format(totalContasP);
                    binding.ValorTotal.setText(totalContasPFormat);

                }
            }
        });
    }
    public void RecuperarDadosContasAPagarI(){


        contasApagar = new ArrayList<>();
        adapterContasApagar = new AdapterContasApagar(getApplicationContext(), contasApagar);
        binding.listaContasAPagar.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.listaContasAPagar.setHasFixedSize(true);
        binding.listaContasAPagar.setAdapter(adapterContasApagar);


        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("ContasApagar")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                ContasApagar contasAPagar = queryDocumentSnapshot.toObject(ContasApagar.class);
                                contasApagar.add(contasAPagar);
                                adapterContasApagar.notifyDataSetChanged();

                            }

                            // efeito Swipe
                            ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                                @Override
                                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                    return false;
                                }

                                @Override
                                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                    final int position = viewHolder.getLayoutPosition();
                                    
                                    if (direction == ItemTouchHelper.LEFT) {

                                        AlertDialog.Builder builder = new AlertDialog.Builder(ContasAPagar.this);
                                        builder.setTitle("Atenção");
                                        builder.setMessage("deseja excluir esse item?");
                                        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                binding.progressBar.setVisibility(View.VISIBLE);

                                                ContasApagar item = contasApagar.get(position);
                                                DocumentReference documentReferenceCTotal = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                        .collection("Total Contas A Pagar").document("Total");

                                                DocumentReference documentReferenceC = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("ContasApagar")
                                                        .document(item.getId());

                                                documentReferenceCTotal.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshotCTotal, @Nullable FirebaseFirestoreException error) {
                                                        documentReferenceC.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshotC, @Nullable FirebaseFirestoreException error) {
                                                                if (documentSnapshotC.exists()) {
                                                                    Double totalContasAPagar = Double.parseDouble(documentSnapshotCTotal.getString("ResultadoDaSomaSaidaC"));
                                                                    Double valorDoubleC = Double.parseDouble(documentSnapshotC.getString("ValorDeSaidaDouble"));
                                                                    Double operacao = totalContasAPagar - valorDoubleC;

                                                                    if (operacao < 0){
                                                                        operacao = 0.0;
                                                                    }

                                                                    String cv = String.valueOf(operacao);

                                                                    db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                            .collection("Total Contas A Pagar").document("Total").update("ResultadoDaSomaSaidaC", cv);

                                                                    db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("ContasApagar")
                                                                            .document(item.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Toast.makeText(getApplicationContext(), "item excluido com sucesso", Toast.LENGTH_LONG).show();
                                                                                        binding.progressBar.setVisibility(View.GONE);
                                                                                        contasApagar.remove(viewHolder.getAdapterPosition());
                                                                                        adapterContasApagar.notifyDataSetChanged();
                                                                                        onStart();

                                                                                    } else {

                                                                                    }
                                                                                }
                                                                            });
                                                                }
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
                                                adapterContasApagar.notifyItemChanged(viewHolder.getAdapterPosition());
                                            }
                                        });
                                        builder.show();
                                    } else if (direction == ItemTouchHelper.RIGHT) {

                                        AlertDialog.Builder builder = new AlertDialog.Builder(ContasAPagar.this);
                                        builder.setTitle("Atenção");
                                        builder.setMessage("deseja confirmar pagamento?");
                                        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                binding.progressBar.setVisibility(View.VISIBLE);

                                                ContasApagar item = contasApagar.get(position);

                                                DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("ContasApagar")
                                                        .document(item.getId());

                                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        DocumentSnapshot documentSnapshot = task.getResult();
                                                        if (documentSnapshot.contains("TipoDeSaida") && documentSnapshot.contains("ValorDeSaida")
                                                                && documentSnapshot.contains("dataDeSaida")){


                                                            String tipoSaida = documentSnapshot.getString("TipoDeSaida");
                                                            String valorSaida = documentSnapshot.getString("ValorDeSaida");
                                                            String valorSaidaDouble = documentSnapshot.getString("ValorDeSaidaDouble");
                                                            String dataSaida = documentSnapshot.getString("dataDeSaida");
                                                            Double ValorCv = Double.parseDouble(valorSaidaDouble);
                                                            Log.d("PAGAMENTOOK", tipoSaida +"\n" + valorSaida + "\n" + dataSaida + "\n" + valorSaidaDouble);

                                                            String id = UUID.randomUUID().toString();
                                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                            Map<String, Object> contasPagas = new HashMap<>();
                                                            contasPagas.put("dataDeSaida", dataSaida);
                                                            contasPagas.put("ValorDeSaidaDouble", valorSaidaDouble);
                                                            contasPagas.put("ValorDeSaida", valorSaida);
                                                            contasPagas.put("TipoDeSaida", tipoSaida);
                                                            contasPagas.put("id", id);
                                                            usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();


                                                            DocumentReference documentReferenceContasApagar = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                    .collection("ContasPagas").document(id);



                                                            documentReferenceContasApagar.set(contasPagas).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {

                                                                }
                                                            });

                                                            //document reference total Contas Pagas
                                                            DocumentReference documentReferenceContasPagas = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                    .collection("Total Contas Pagas").document("Total");

                                                            documentReferenceContasPagas.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> taskC) {
                                                                    DocumentSnapshot documentSnapshotC = taskC.getResult();


                                                                    if (documentSnapshotC.contains("ResultadoDaSomaContasPagas")){
                                                                        Double ValorC = Double.parseDouble(documentSnapshotC.getString("ResultadoDaSomaContasPagas"));

                                                                        Double SomaSaida = ValorC + ValorCv;
                                                                        String SomaSaidaCv = String.valueOf(SomaSaida);

                                                                        // HasMap total D
                                                                        Map<String, Object> valorTotalC = new HashMap<>();
                                                                        valorTotalC.put("ResultadoDaSomaContasPagas", SomaSaidaCv);

                                                                        db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                .collection("Total Contas Pagas").document("Total").set(valorTotalC).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {

                                                                                    }
                                                                                });
                                                                    }else {
                                                                        String ValorCvString = String.valueOf(ValorCv);
                                                                        // HasMap total C
                                                                        Map<String, Object> valorTotalC = new HashMap<>();
                                                                        valorTotalC.put("ResultadoDaSomaContasPagas", ValorCvString);

                                                                        db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                .collection("Total Contas Pagas").document("Total").set(valorTotalC).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {

                                                                                    }
                                                                                });
                                                                    }

                                                                }
                                                            });

                                                            DocumentReference documentReferenceCTotal = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                    .collection("Total Contas A Pagar").document("Total");

                                                            DocumentReference documentReferenceC = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("ContasApagar")
                                                                    .document(item.getId());

                                                            documentReferenceCTotal.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onEvent(@Nullable DocumentSnapshot documentSnapshotCTotal, @Nullable FirebaseFirestoreException error) {
                                                                    documentReferenceC.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onEvent(@Nullable DocumentSnapshot documentSnapshotC, @Nullable FirebaseFirestoreException error) {
                                                                            if (documentSnapshotC.exists()) {
                                                                                Double totalContasAPagar = Double.parseDouble(documentSnapshotCTotal.getString("ResultadoDaSomaSaidaC"));
                                                                                Double valorDoubleC = Double.parseDouble(documentSnapshotC.getString("ValorDeSaidaDouble"));
                                                                                Double operacao = totalContasAPagar - valorDoubleC;
                                                                                String cv = String.valueOf(operacao);

                                                                                db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                        .collection("Total Contas A Pagar").document("Total").update("ResultadoDaSomaSaidaC", cv);

                                                                                db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("ContasApagar")
                                                                                        .document(item.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    Toast.makeText(getApplicationContext(), "item excluido com sucesso", Toast.LENGTH_LONG).show();
                                                                                                    binding.progressBar.setVisibility(View.GONE);
                                                                                                    contasApagar.remove(viewHolder.getAdapterPosition());
                                                                                                    adapterContasApagar.notifyDataSetChanged();
                                                                                                    onStart();

                                                                                                } else {

                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    }
                                                });

                                            }
                                        });
                                        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                                adapterContasApagar.notifyItemChanged(viewHolder.getAdapterPosition());
                                            }
                                        });
                                        builder.show();




                                    }
                                }

                                @Override
                                public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                                    RecyclerViewSwipeDecorator.Builder decorator = new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                                    if (dX > 0) {
                                        // Swipe para a direita
                                        decorator.addSwipeRightBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green))
                                                .addSwipeRightActionIcon(R.drawable.ic_pagamento_realizado)
                                                .addSwipeRightLabel("Pago")
                                                .setSwipeRightLabelColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                    } else {
                                        // Swipe para a esquerda
                                        decorator.addSwipeLeftBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red))
                                                .addSwipeLeftActionIcon(R.drawable.ic_delete)
                                                .addSwipeLeftLabel("Excluir")
                                                .setSwipeLeftLabelColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                    }

                                    decorator.create().decorate();
                                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                                }
                            };
                            new ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.listaContasAPagar);
                        }
                    }
                });
    }
    private void showCustomDialog() {
        // Infla o layout personalizado
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_custom_cp, null);

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

        // Cria e mostra o AlertDialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Filtro de pesquisa")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ano = yearSpinner.getSelectedItem().toString();
                        String mes = monthSpinner.getSelectedItem().toString();

                        //recuperar total
                        DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                .collection("Total Contas A Pagar").document("Total");
                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (!documentSnapshot.exists()) {
                                    String vazioFormatado = NumberFormat.getCurrencyInstance(ptbr).format(vazio);
                                    binding.ValorTotal.setText(vazioFormatado);
                                    Log.d("TOTALSAIDAS", "SEM DADOS");


                                }else {
                                    Double totalContasP = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaSaidaC"));
                                    String totalContasPFormat = NumberFormat.getCurrencyInstance(ptbr).format(totalContasP);
                                    binding.ValorTotal.setText(totalContasPFormat);
                                    Log.d("TOTALSAIDAS", "COM DADOS");

                                }
                            }
                        });

                        contasApagar = new ArrayList<>();
                        adapterContasApagar = new AdapterContasApagar(getApplicationContext(), contasApagar);
                        binding.listaContasAPagar.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        binding.listaContasAPagar.setHasFixedSize(true);
                        binding.listaContasAPagar.setAdapter(adapterContasApagar);


                        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        db = FirebaseFirestore.getInstance();
                        db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("ContasApagar")
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                ContasApagar contasAPagar = queryDocumentSnapshot.toObject(ContasApagar.class);
                                                contasApagar.add(contasAPagar);
                                                adapterContasApagar.notifyDataSetChanged();

                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                                DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                        .collection("Total Contas A Pagar").document("Total");
                                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        DocumentSnapshot documentSnapshot = task.getResult();
                                                        if (!documentSnapshot.exists()) {
                                                            String vazioFormatado = NumberFormat.getCurrencyInstance(ptbr).format(vazio);
                                                            binding.ValorTotal.setText(vazioFormatado);

                                                        }else {
                                                            Double totalContasP = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaSaidaC"));
                                                            String totalContasPFormat = NumberFormat.getCurrencyInstance(ptbr).format(totalContasP);
                                                            binding.ValorTotal.setText(totalContasPFormat);

                                                        }
                                                    }
                                                });


                                                // efeito Swipe
                                                ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                                                    @Override
                                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                                        return false;
                                                    }

                                                    @Override
                                                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                                        final int position = viewHolder.getLayoutPosition();

                                                        if (direction == ItemTouchHelper.LEFT) {

                                                            AlertDialog.Builder builder = new AlertDialog.Builder(ContasAPagar.this);
                                                            builder.setTitle("Atenção");
                                                            builder.setMessage("deseja excluir esse item?");
                                                            builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    binding.progressBar.setVisibility(View.VISIBLE);

                                                                    ContasApagar item = contasApagar.get(position);
                                                                    DocumentReference documentReferenceCTotal = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                            .collection("Total Contas A Pagar").document("Total");

                                                                    DocumentReference documentReferenceC = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("ContasApagar")
                                                                            .document(item.getId());

                                                                    documentReferenceCTotal.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onEvent(@Nullable DocumentSnapshot documentSnapshotCTotal, @Nullable FirebaseFirestoreException error) {
                                                                            documentReferenceC.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                                @Override
                                                                                public void onEvent(@Nullable DocumentSnapshot documentSnapshotC, @Nullable FirebaseFirestoreException error) {
                                                                                    if (documentSnapshotC.exists()) {
                                                                                        Double totalContasAPagar = Double.parseDouble(documentSnapshotCTotal.getString("ResultadoDaSomaSaidaC"));
                                                                                        Double valorDoubleC = Double.parseDouble(documentSnapshotC.getString("ValorDeSaidaDouble"));
                                                                                        Double operacao = totalContasAPagar - valorDoubleC;

                                                                                        if (operacao < 0){
                                                                                            operacao = 0.0;
                                                                                        }

                                                                                        String cv = String.valueOf(operacao);

                                                                                        db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                                .collection("Total Contas A Pagar").document("Total").update("ResultadoDaSomaSaidaC", cv);

                                                                                        db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("ContasApagar")
                                                                                                .document(item.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()) {
                                                                                                            Toast.makeText(getApplicationContext(), "item excluido com sucesso", Toast.LENGTH_LONG).show();
                                                                                                            binding.progressBar.setVisibility(View.GONE);
                                                                                                            contasApagar.remove(viewHolder.getAdapterPosition());
                                                                                                            adapterContasApagar.notifyDataSetChanged();
                                                                                                            onStart();

                                                                                                        } else {

                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
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
                                                                    adapterContasApagar.notifyItemChanged(viewHolder.getAdapterPosition());
                                                                }
                                                            });
                                                            builder.show();
                                                        } else if (direction == ItemTouchHelper.RIGHT) {

                                                            AlertDialog.Builder builder = new AlertDialog.Builder(ContasAPagar.this);
                                                            builder.setTitle("Atenção");
                                                            builder.setMessage("deseja confirmar pagamento?");
                                                            builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    binding.progressBar.setVisibility(View.VISIBLE);

                                                                    ContasApagar item = contasApagar.get(position);

                                                                    DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("ContasApagar")
                                                                            .document(item.getId());

                                                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            DocumentSnapshot documentSnapshot = task.getResult();
                                                                            if (documentSnapshot.contains("TipoDeSaida") && documentSnapshot.contains("ValorDeSaida")
                                                                                    && documentSnapshot.contains("dataDeSaida")){


                                                                                String tipoSaida = documentSnapshot.getString("TipoDeSaida");
                                                                                String valorSaida = documentSnapshot.getString("ValorDeSaida");
                                                                                String valorSaidaDouble = documentSnapshot.getString("ValorDeSaidaDouble");
                                                                                String dataSaida = documentSnapshot.getString("dataDeSaida");
                                                                                Double ValorCv = Double.parseDouble(valorSaidaDouble);
                                                                                Log.d("PAGAMENTOOK", tipoSaida +"\n" + valorSaida + "\n" + dataSaida + "\n" + valorSaidaDouble);

                                                                                String id = UUID.randomUUID().toString();
                                                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                                                Map<String, Object> contasPagas = new HashMap<>();
                                                                                contasPagas.put("dataDeSaida", dataSaida);
                                                                                contasPagas.put("ValorDeSaidaDouble", valorSaidaDouble);
                                                                                contasPagas.put("ValorDeSaida", valorSaida);
                                                                                contasPagas.put("TipoDeSaida", tipoSaida);
                                                                                contasPagas.put("id", id);
                                                                                usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();


                                                                                DocumentReference documentReferenceContasApagar = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                        .collection("ContasPagas").document(id);



                                                                                documentReferenceContasApagar.set(contasPagas).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {

                                                                                    }
                                                                                });

                                                                                //document reference total Contas Pagas
                                                                                DocumentReference documentReferenceContasPagas = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                        .collection("Total Contas Pagas").document("Total");

                                                                                documentReferenceContasPagas.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskC) {
                                                                                        DocumentSnapshot documentSnapshotC = taskC.getResult();


                                                                                        if (documentSnapshotC.contains("ResultadoDaSomaContasPagas")){
                                                                                            Double ValorC = Double.parseDouble(documentSnapshotC.getString("ResultadoDaSomaContasPagas"));

                                                                                            Double SomaSaida = ValorC + ValorCv;
                                                                                            String SomaSaidaCv = String.valueOf(SomaSaida);

                                                                                            // HasMap total D
                                                                                            Map<String, Object> valorTotalC = new HashMap<>();
                                                                                            valorTotalC.put("ResultadoDaSomaContasPagas", SomaSaidaCv);

                                                                                            db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                                    .collection("Total Contas Pagas").document("Total").set(valorTotalC).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                                        }
                                                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                                                        @Override
                                                                                                        public void onFailure(@NonNull Exception e) {

                                                                                                        }
                                                                                                    });
                                                                                        }else {
                                                                                            String ValorCvString = String.valueOf(ValorCv);
                                                                                            // HasMap total C
                                                                                            Map<String, Object> valorTotalC = new HashMap<>();
                                                                                            valorTotalC.put("ResultadoDaSomaContasPagas", ValorCvString);

                                                                                            db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                                    .collection("Total Contas Pagas").document("Total").set(valorTotalC).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                                        }
                                                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                                                        @Override
                                                                                                        public void onFailure(@NonNull Exception e) {

                                                                                                        }
                                                                                                    });
                                                                                        }

                                                                                    }
                                                                                });

                                                                                DocumentReference documentReferenceCTotal = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                        .collection("Total Contas A Pagar").document("Total");

                                                                                DocumentReference documentReferenceC = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("ContasApagar")
                                                                                        .document(item.getId());

                                                                                documentReferenceCTotal.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                                    @Override
                                                                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshotCTotal, @Nullable FirebaseFirestoreException error) {
                                                                                        documentReferenceC.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                                            @Override
                                                                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshotC, @Nullable FirebaseFirestoreException error) {
                                                                                                if (documentSnapshotC.exists()) {
                                                                                                    Double totalContasAPagar = Double.parseDouble(documentSnapshotCTotal.getString("ResultadoDaSomaSaidaC"));
                                                                                                    Double valorDoubleC = Double.parseDouble(documentSnapshotC.getString("ValorDeSaidaDouble"));
                                                                                                    Double operacao = totalContasAPagar - valorDoubleC;
                                                                                                    String cv = String.valueOf(operacao);

                                                                                                    db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                                            .collection("Total Contas A Pagar").document("Total").update("ResultadoDaSomaSaidaC", cv);

                                                                                                    db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("ContasApagar")
                                                                                                            .document(item.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                    if (task.isSuccessful()) {
                                                                                                                        Toast.makeText(getApplicationContext(), "item excluido com sucesso", Toast.LENGTH_LONG).show();
                                                                                                                        binding.progressBar.setVisibility(View.GONE);
                                                                                                                        contasApagar.remove(viewHolder.getAdapterPosition());
                                                                                                                        adapterContasApagar.notifyDataSetChanged();
                                                                                                                        onStart();

                                                                                                                    } else {

                                                                                                                    }
                                                                                                                }
                                                                                                            });
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });

                                                                }
                                                            });
                                                            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.cancel();
                                                                    adapterContasApagar.notifyItemChanged(viewHolder.getAdapterPosition());
                                                                }
                                                            });
                                                            builder.show();




                                                        }
                                                    }

                                                    @Override
                                                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                                                        RecyclerViewSwipeDecorator.Builder decorator = new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                                                        if (dX > 0) {
                                                            // Swipe para a direita
                                                            decorator.addSwipeRightBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green))
                                                                    .addSwipeRightActionIcon(R.drawable.ic_pagamento_realizado)
                                                                    .addSwipeRightLabel("Pago")
                                                                    .setSwipeRightLabelColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                                        } else {
                                                            // Swipe para a esquerda
                                                            decorator.addSwipeLeftBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red))
                                                                    .addSwipeLeftActionIcon(R.drawable.ic_delete)
                                                                    .addSwipeLeftLabel("Excluir")
                                                                    .setSwipeLeftLabelColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                                        }

                                                        decorator.create().decorate();
                                                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                                                    }
                                                };
                                                new ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.listaContasAPagar);
                                            }
                                        }
                                    }
                                });

                    }
                })
                .setNegativeButton("Cancelar", null);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

}