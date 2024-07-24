package br.com.lucas.valli.fluxodecaixa.FragmentActivity;

import static br.com.lucas.valli.fluxodecaixa.Classes.ConversorDeMoeda.formatPriceSave;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import br.com.lucas.valli.fluxodecaixa.Adapter.AdapterDadosSaidaP;
import br.com.lucas.valli.fluxodecaixa.Classes.ConversorDeMoeda;
import br.com.lucas.valli.fluxodecaixa.Model.DadosSaidaP;
import br.com.lucas.valli.fluxodecaixa.R;
import br.com.lucas.valli.fluxodecaixa.RecyclerItemClickListener.RecyclerItemClickListener;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class FragmentPD extends Fragment {

    private List<DadosSaidaP> dadosSaidaEListP;
    private FirebaseFirestore db;
    private String usuarioID;
    private Date x = new Date();
    private String dia = new SimpleDateFormat("dd", new Locale("pt", "BR")).format(x);
    private String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
    private String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);
    View v;
    private RecyclerView recyclerViewList;
    private TextView ValorTotalSaidas;
    private TextView ValorTotalSaidasEPD;
    private ProgressBar progressBar;
    private Locale ptbr = new Locale("pt", "BR");
    private Double vazio = Double.parseDouble("0.00");
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onStart() {
        super.onStart();

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.pd_fragment, container, false);
        recyclerViewList = (RecyclerView) v.findViewById(R.id.ListaTipoSaidaD);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        checkConnection();
        recyclerViewList.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerViewList,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        DadosSaidaP item = dadosSaidaEListP.get(position);
                        progressBar.setVisibility(View.VISIBLE);

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
                        DocumentReference documentReferenceDados = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                .collection("nova saida").document("categoria").collection("Pagamento de Dívidas")
                                .document(item.getId());

                        documentReferenceDados.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.contains("TipoDeSaida") && documentSnapshot.contains("ValorDeSaida")
                                        && documentSnapshot.contains("dataDeSaida")) {

                                    String valorSaida = documentSnapshot.getString("ValorDeSaida");
                                    String TipoDeSaida = documentSnapshot.getString("TipoDeSaida");
                                    String formaPagamento = documentSnapshot.getString("formPagamento");


                                    txt_tipo.setText(TipoDeSaida);
                                    txt_valor.setText(valorSaida);
                                    txt_formPagamento.setText(formaPagamento);

                                }
                            }
                        });

                        // Cria e mostra o AlertDialog
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                        builder.setView(dialogView)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                                        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                                        if (networkInfo == null){
                                            Log.d("NETCONEX", "SEM INTERNET");
                                            progressBar.setVisibility(View.VISIBLE);
                                            Toast.makeText(getContext(), "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();

                                        }else {
                                            progressBar.setVisibility(View.GONE);

                                            DocumentReference documentReferenceTotalDiario = db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalSaidasDiario")
                                                    .document(dia);

                                            DocumentReference documentReferenceTotalMensal = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de Saidas")
                                                    .document("Total");

                                            DocumentReference documentReferenceTotalAnual = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("saidas").collection("TotalSaidaAnual")
                                                    .document("Total");

                                            DocumentReference documentReferenceP = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasP")
                                                    .document("Total");

                                            DocumentReference documentReferenceResumo = db.collection(usuarioID).document("resumoCaixa").collection("ResumoDeCaixa").document("saidas").collection("total")
                                                    .document("ResumoTotal");

                                            documentReferenceResumo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    documentReferenceP.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> taskP) {
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
                                                                                            DocumentSnapshot documentSnapshotTotalP = taskP.getResult();
                                                                                            if (documentSnapshot.contains("TipoDeSaida") && documentSnapshot.contains("ValorDeSaida")
                                                                                                    && documentSnapshot.contains("dataDeSaida")) {

                                                                                                String dataSaida= documentSnapshot.getString("dataDeSaida");
                                                                                                String valorSaidaDouble = documentSnapshot.getString("ValorDeSaidaDouble");
                                                                                                String TipoDeSaida = documentSnapshot.getString("TipoDeSaida");
                                                                                                String formaPagamento = documentSnapshot.getString("formPagamento");


                                                                                                //texto campos dialogoCustom
                                                                                                String txt_tipoString =String.valueOf(txt_tipo.getText());
                                                                                                String txt_valorString =String.valueOf(txt_valor.getText());
                                                                                                String txt_formPagamentoString =String.valueOf(txt_formPagamento.getText());

                                                                                                //entradas convertidas para Double
                                                                                                String str = formatPriceSave(txt_valorString);
                                                                                                Double valorDoubleEditText = Double.parseDouble(str);
                                                                                                Double valorDoubleDb = Double.parseDouble(valorSaidaDouble);

                                                                                                //mesmo valor double para String
                                                                                                String txt_valor = String.valueOf(valorDoubleEditText);

                                                                                                //converter formato moeda
                                                                                                String ValorEntradaConvertido = NumberFormat.getCurrencyInstance(ptbr).format(valorDoubleEditText);

                                                                                                Log.d("PAGAMENTOOK", txt_tipoString + "\n" + valorDoubleDb + "\n" +txt_formPagamentoString);

                                                                                                if (!txt_tipoString.isEmpty() && !txt_valor.isEmpty()
                                                                                                        && !txt_formPagamentoString.isEmpty()) {

                                                                                                    if (!txt_tipoString.equals(TipoDeSaida)) {
                                                                                                        db.collection(usuarioID).document(ano).collection(mes)
                                                                                                                .document("entradas")
                                                                                                                .collection("nova entrada")
                                                                                                                .document(item.getId()).update("TipoDeEntrada", txt_tipoString);
                                                                                                    }

                                                                                                    if (!txt_valor.equals(valorSaidaDouble)) {

                                                                                                        if (valorDoubleEditText < valorDoubleDb) {
                                                                                                            Double op = valorDoubleDb - valorDoubleEditText;

                                                                                                            if (documentSnapshotTotalMensal.contains("ResultadoDaSomaSaida")) {
                                                                                                                Double valorTotal = Double.parseDouble(documentSnapshotTotalMensal.getString("ResultadoDaSomaSaida"));
                                                                                                                Double valorTotalP = Double.parseDouble(documentSnapshotTotalP.getString("ResultadoDaSomaSaidaP"));
                                                                                                                Double op2 = valorTotal - op;
                                                                                                                Double opP = valorTotalP - op;


                                                                                                                db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de Saidas")
                                                                                                                        .document("Total").update("ResultadoDaSomaSaida",
                                                                                                                                String.valueOf(op2));

                                                                                                                db.collection(usuarioID).document(ano).collection("ResumoAnual").document("saidas").collection("TotalSaidaAnual")
                                                                                                                        .document("Total").update("ResultadoTotalSaidaAnual",
                                                                                                                                String.valueOf(op2));

                                                                                                                documentReferenceResumo.update("ResultadoTotal",String.valueOf(op2));

                                                                                                                db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasP")
                                                                                                                        .document("Total").update("ResultadoDaSomaSaidaP",
                                                                                                                                String.valueOf(opP));

                                                                                                                db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                                                        .collection("nova saida").document("categoria").collection("Pagamento de Dívidas")
                                                                                                                        .document(item.getId()).update("ValorDeSaida", ValorEntradaConvertido, "ValorDeSaidaDouble", txt_valor);
                                                                                                            }

                                                                                                        } else if (valorDoubleEditText > valorDoubleDb) {
                                                                                                            Double op = valorDoubleEditText - valorDoubleDb;

                                                                                                            if (documentSnapshotTotalMensal.contains("ResultadoDaSomaSaida")) {
                                                                                                                Double valorTotal = Double.parseDouble(documentSnapshotTotalMensal.getString("ResultadoDaSomaSaida"));
                                                                                                                Double valorTotalP = Double.parseDouble(documentSnapshotTotalP.getString("ResultadoDaSomaSaidaP"));
                                                                                                                Double op2 = op + valorTotal;
                                                                                                                Double opP = op + valorTotalP;

                                                                                                                db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de Saidas")
                                                                                                                        .document("Total").update("ResultadoDaSomaSaida",
                                                                                                                                String.valueOf(op2));

                                                                                                                db.collection(usuarioID).document(ano).collection("ResumoAnual").document("saidas").collection("TotalSaidaAnual")
                                                                                                                        .document("Total").update("ResultadoTotalSaidaAnual",
                                                                                                                                String.valueOf(op2));

                                                                                                                documentReferenceResumo.update("ResultadoTotal",String.valueOf(op2));

                                                                                                                db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasP")
                                                                                                                        .document("Total").update("ResultadoDaSomaSaidaP",
                                                                                                                                String.valueOf(opP));

                                                                                                                db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                                                        .collection("nova saida").document("categoria").collection("Pagamento de Dívidas")
                                                                                                                        .document(item.getId()).update("ValorDeSaida", ValorEntradaConvertido, "ValorDeSaidaDouble", txt_valor);

                                                                                                            }



                                                                                                        }

                                                                                                    }

                                                                                                    if (!txt_formPagamentoString.equals(formaPagamento)) {
                                                                                                        db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                                                .collection("nova saida").document("categoria").collection("Pagamento de Dívidas")
                                                                                                                .document(item.getId()).update("formPagamento", txt_formPagamentoString);
                                                                                                    }

                                                                                                    String dataAtual = dia+"/"+mes+"/"+ano;
                                                                                                    // total diario
                                                                                                    if (dataSaida.equals(dataAtual)){
                                                                                                        if (!txt_valor.equals(valorSaidaDouble)) {

                                                                                                            if (valorDoubleEditText < valorDoubleDb) {
                                                                                                                Double op = valorDoubleDb - valorDoubleEditText;

                                                                                                                if (documentSnapshotTotalDiario.contains("ResultadoDaSomaSaidaDiario")) {
                                                                                                                    Double valorTotal = Double.parseDouble(documentSnapshotTotalDiario.getString("ResultadoDaSomaSaidaDiario"));
                                                                                                                    Double op2 = valorTotal - op;
                                                                                                                    db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalSaidasDiario")
                                                                                                                            .document(dia).update("ResultadoDaSomaSaidaDiario",
                                                                                                                                    String.valueOf(op2));


                                                                                                                }

                                                                                                            } else if (valorDoubleEditText > valorDoubleDb) {
                                                                                                                Double op = valorDoubleEditText - valorDoubleDb;

                                                                                                                if (documentSnapshotTotalDiario.contains("ResultadoDaSomaSaidaDiario")) {
                                                                                                                    Double valorTotal = Double.parseDouble(documentSnapshotTotalDiario.getString("ResultadoDaSomaSaidaDiario"));
                                                                                                                    Double op2 = op + valorTotal;

                                                                                                                    db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalSaidasDiario")
                                                                                                                            .document(dia).update("ResultadoDaSomaSaidaDiario",
                                                                                                                                    String.valueOf(op2));


                                                                                                                }



                                                                                                            }

                                                                                                        }
                                                                                                    }

                                                                                                    onStart();
                                                                                                }else {
                                                                                                    Toast.makeText(getContext(), "Preencha todos os campos", Toast.LENGTH_LONG).show();
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
                                            });

                                        }



                                    }
                                })
                                .setNegativeButton("Cancelar", null);
                        progressBar.setVisibility(View.GONE);

                        androidx.appcompat.app.AlertDialog dialog = builder.create();
                        dialog.show();




                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        return v;
    }
    private void refreshData() {
        new Handler().postDelayed(() -> {
            checkConnection();
            swipeRefreshLayout.setRefreshing(false);
        }, 1000); // Simula um delay de 1 segundos
    }
    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            Log.d("NETCONEX", "SEM INTERNET");
            progressBar.setVisibility(View.VISIBLE);
            return false;
        }else {
            RecuperarDadosSaidasP();
            RecuperarTotalSaidaP();
            RecuperarTotalSaidaMensal();
            progressBar.setVisibility(View.GONE);
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
            Log.d("NETCONEX", "WIFI");
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
            Log.d("NETCONEX", "DADOS");
        }
        return networkInfo.isConnected();

    }
    public void RecuperarDadosSaidasP(){
        dadosSaidaEListP = new ArrayList<>();

        recyclerViewList = (RecyclerView) v.findViewById(R.id.ListaTipoSaidaP);

        AdapterDadosSaidaP adapterDadosSaidaP = new AdapterDadosSaidaP(getContext(),dadosSaidaEListP);
        recyclerViewList.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewList.setAdapter(adapterDadosSaidaP);

        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                .collection("nova saida").document("categoria").collection("Pagamento de Dívidas")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@org.checkerframework.checker.nullness.qual.NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for(QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){

                                DadosSaidaP dadosSaidaP = queryDocumentSnapshot.toObject(DadosSaidaP.class);
                                dadosSaidaEListP.add(dadosSaidaP);
                                adapterDadosSaidaP.notifyDataSetChanged();


                                //efeito Swipe
                                ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                        return false;
                                    }

                                    @Override
                                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                        final int position = viewHolder.getLayoutPosition();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        DadosSaidaP item = dadosSaidaEListP.get(position);

                                        builder.setTitle("Aviso Importante");
                                        builder.setMessage("Após a confirmação de exclusão, não será possível recuperar os dados apagados");
                                        builder.setCancelable(false);
                                        builder.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                progressBar.setVisibility(View.VISIBLE);

                                                DocumentReference documentReferenceAnual = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("saidas").collection("TotalSaidaAnual")
                                                        .document("Total");
                                                DocumentReference documentReferenceV = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                        .collection("nova saida").document("categoria").collection("Pagamento de Dívidas")
                                                        .document(item.getId());
                                                DocumentReference documentReferenceTotalDiario = db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalSaidasDiario")
                                                        .document(dia);
                                                DocumentReference documentReferenceResumo = db.collection(usuarioID).document("resumoCaixa").collection("ResumoDeCaixa").document("saidas").collection("total")
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
                                                                        documentReferenceTotalDiario.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> taskTotalDiario) {
                                                                                DocumentSnapshot documentSnapshotSaidasTotalV = taskTotalV.getResult();
                                                                                DocumentSnapshot documentSnapshotTotalDiario= taskTotalDiario.getResult();
                                                                                DocumentSnapshot documentSnapshotTotalResumo= taskResumo.getResult();

                                                                                String dataSet = dia + "/" +mes+ "/" + ano;

                                                                                if (taskTotalAnual.isSuccessful()){
                                                                                    DocumentSnapshot documentSnapshotSaidasAnual = taskTotalAnual.getResult();
                                                                                    Double totalVDouble = Double.parseDouble(documentSnapshotSaidasTotalV.getString("ValorDeSaidaDouble"));

                                                                                    if (taskTotalDiario.isSuccessful()){
                                                                                        if (documentSnapshotSaidasTotalV.contains("dataDeSaida")){
                                                                                            String data = documentSnapshotSaidasTotalV.getString("dataDeSaida");
                                                                                            if (data.equals(dataSet)){
                                                                                                if (documentSnapshotTotalDiario.contains("ResultadoDaSomaSaidaDiario")){
                                                                                                    Double totalDiario = Double.parseDouble(documentSnapshotTotalDiario.getString("ResultadoDaSomaSaidaDiario"));
                                                                                                    Double op = totalDiario - totalVDouble;
                                                                                                    if (op < 0){
                                                                                                        op = 0.0;
                                                                                                    }
                                                                                                    String opCvDiario = String.valueOf(op);

                                                                                                    db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalSaidasDiario")
                                                                                                            .document(dia).update("ResultadoDaSomaSaidaDiario", opCvDiario);

                                                                                                }
                                                                                            }

                                                                                        }
                                                                                    }

                                                                                    if (documentSnapshotSaidasAnual.contains("ResultadoTotalSaidaAnual"));{
                                                                                        Double totalAnualDouble = Double.parseDouble(documentSnapshotSaidasAnual.getString("ResultadoTotalSaidaAnual"));
                                                                                        Double opDouble = totalAnualDouble - totalVDouble;
                                                                                        if (opDouble < 0){
                                                                                            opDouble = 0.0;
                                                                                        }
                                                                                        String cvString = String.valueOf(opDouble);


                                                                                        db.collection(usuarioID).document(ano).collection("ResumoAnual").document("saidas").collection("TotalSaidaAnual")
                                                                                                .document("Total").update("ResultadoTotalSaidaAnual",cvString).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void unused) {
                                                                                                        //document reference total Geral
                                                                                                        DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de Saidas")
                                                                                                                .document("Total");
                                                                                                        //document reference total Pagamento de Dívidas
                                                                                                        DocumentReference documentReferenceP = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasP")
                                                                                                                .document("Total");

                                                                                                        DocumentReference documentReferenceV = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                                                .collection("nova saida").document("categoria").collection("Pagamento de Dívidas")
                                                                                                                .document(item.getId());

                                                                                                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> taskGeral) {
                                                                                                                documentReferenceP.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskP) {
                                                                                                                        documentReferenceV.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> taskV) {
                                                                                                                                DocumentSnapshot documentSnapshot = taskGeral.getResult();
                                                                                                                                DocumentSnapshot documentSnapshotP= taskP.getResult();
                                                                                                                                DocumentSnapshot documentSnapshotV= taskV.getResult();

                                                                                                                                if (documentSnapshotV.exists()){
                                                                                                                                    Double totalSaida = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaSaida"));
                                                                                                                                    Double totalSaida2 = Double.parseDouble(documentSnapshotP.getString("ResultadoDaSomaSaidaP"));
                                                                                                                                    Double totalSaida3 = Double.parseDouble(documentSnapshotV.getString("ValorDeSaidaDouble"));
                                                                                                                                    Double soma = totalSaida - totalSaida3;
                                                                                                                                    Double soma2 = totalSaida2 - totalSaida3;
                                                                                                                                    if (soma < 0){
                                                                                                                                        soma = 0.0;
                                                                                                                                    } else if (soma2 < 0) {
                                                                                                                                        soma2 = 0.0;
                                                                                                                                    }
                                                                                                                                    String cv = String.valueOf(soma);
                                                                                                                                    String cv2 = String.valueOf(soma2);

                                                                                                                                    db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de Saidas")
                                                                                                                                            .document("Total").update("ResultadoDaSomaSaida",cv);
                                                                                                                                    db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasP")
                                                                                                                                            .document("Total").update("ResultadoDaSomaSaidaP",cv2);

                                                                                                                                    db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                                                                            .collection("nova saida").document("categoria").collection("Pagamento de Dívidas")
                                                                                                                                            .document(item.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                                @Override
                                                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                                    try {
                                                                                                                                                        Toast.makeText(getContext(), "item excluido com sucesso", Toast.LENGTH_LONG).show();
                                                                                                                                                        progressBar.setVisibility(View.GONE);
                                                                                                                                                        dadosSaidaEListP.remove(position);
                                                                                                                                                        adapterDadosSaidaP.notifyDataSetChanged();
                                                                                                                                                        RecuperarTotalSaidaP();
                                                                                                                                                        RecuperarTotalSaidaMensal();
                                                                                                                                                    }catch (IndexOutOfBoundsException e){
                                                                                                                                                        Log.i("ErroTry", "erro ao removerItem");
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
                                                adapterDadosSaidaP.notifyItemChanged(viewHolder.getAdapterPosition());
                                            }
                                        });
                                        builder.show();

                                    }

                                    public void onChildDraw (Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){

                                        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                                                .addSwipeLeftBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red))
                                                .addSwipeLeftActionIcon(R.drawable.ic_delete)
                                                .addSwipeLeftLabel("Excluir")
                                                .setSwipeLeftLabelColor(ContextCompat.getColor(getContext(), R.color.white))
                                                .create()
                                                .decorate();

                                        if (isCurrentlyActive) {
                                            swipeRefreshLayout.setEnabled(false);
                                        } else {
                                            swipeRefreshLayout.setEnabled(true);
                                        }

                                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                                    }

                                };
                                new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerViewList);
                            }
                        }
                    }
                });

    }
    public void RecuperarTotalSaidaP(){
        ValorTotalSaidas = (TextView) v.findViewById(R.id.ValorTotalSaidas);
        ValorTotalSaidasEPD = (TextView) v.findViewById(R.id.ValorTotalSaidasEPD);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //document reference total Essenciais
        DocumentReference documentReferenceP = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasP")
                .document("Total");


        //document reference total Essenciais
        documentReferenceP.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskP) {

                DocumentSnapshot documentSnapshotP = taskP.getResult();


                if (!documentSnapshotP.exists()) {
                    String vazioFormatado = NumberFormat.getCurrencyInstance(ptbr).format(vazio);
                    ValorTotalSaidasEPD.setText(vazioFormatado);

                }else {
                    Double totalSaidaP = Double.parseDouble(documentSnapshotP.getString("ResultadoDaSomaSaidaP"));
                    String totalSaidaFormatadoP = NumberFormat.getCurrencyInstance(ptbr).format(totalSaidaP);
                    ValorTotalSaidasEPD.setText(totalSaidaFormatadoP);
                }


            }
        });


    }
    public void RecuperarTotalSaidaMensal(){
        ValorTotalSaidas = (TextView) v.findViewById(R.id.ValorTotalSaidas);
        ValorTotalSaidasEPD = (TextView) v.findViewById(R.id.ValorTotalSaidasEPD);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //document reference total Mensal
        DocumentReference documentReferenceMensal = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de Saidas")
                .document("Total");

        //document reference total(Geral)
        documentReferenceMensal.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskMensal) {
                DocumentSnapshot documentSnapshotMensal = taskMensal.getResult();
                if (!documentSnapshotMensal.exists()) {
                    String vazioFormatado = NumberFormat.getCurrencyInstance(ptbr).format(vazio);
                    ValorTotalSaidas.setText(vazioFormatado);

                }else {
                    Double totalSaida = Double.parseDouble(documentSnapshotMensal.getString("ResultadoDaSomaSaida"));
                    String totalSaidaFormatado = NumberFormat.getCurrencyInstance(ptbr).format(totalSaida);
                    ValorTotalSaidas.setText(totalSaidaFormatado);

                }



            }
        });


    }
}
