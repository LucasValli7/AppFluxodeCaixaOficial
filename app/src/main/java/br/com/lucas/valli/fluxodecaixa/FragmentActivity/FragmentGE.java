package br.com.lucas.valli.fluxodecaixa.FragmentActivity;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.annotation.ElementType;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import br.com.lucas.valli.fluxodecaixa.Adapter.AdapterDadosSaidaE;
import br.com.lucas.valli.fluxodecaixa.Model.DadosSaidaE;
import br.com.lucas.valli.fluxodecaixa.NovaSaida;
import br.com.lucas.valli.fluxodecaixa.R;
import br.com.lucas.valli.fluxodecaixa.TelaPrincipalEntradas;
import br.com.lucas.valli.fluxodecaixa.TelaPrincipalSaidas;
import dalvik.bytecode.OpcodeInfo;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class FragmentGE extends Fragment {


    private List<DadosSaidaE> dadosSaidaEListE;
    private FirebaseFirestore db;
    private String usuarioID;
    private Date x = new Date();
    private String dia = new SimpleDateFormat("dd", new Locale("pt", "BR")).format(x);
    private String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
    private String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);
    View v;
    private RecyclerView recyclerViewList;

    private TextView ValorTotalSaidas, ValorTotalSaidasEPD;
    private Button btnAddSaida;

    private ProgressBar progressBar;

    Locale ptbr = new Locale("pt", "BR");
    private Double vazio = Double.parseDouble("0.00");


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.ge_fragment, container, false);
        recyclerViewList = (RecyclerView) v.findViewById(R.id.ListaTipoSaidaE);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        checkConnection();
        return v;
    }

    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            Log.d("NETCONEX", "SEM INTERNET");
           progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            RecuperarDadosSaidasE();
            RecuperarTotalSaidaE();
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
    public void RecuperarDadosSaidasE(){


        dadosSaidaEListE = new ArrayList<>();

        recyclerViewList = (RecyclerView) v.findViewById(R.id.ListaTipoSaidaE);

        AdapterDadosSaidaE adapterDadosSaidaE = new AdapterDadosSaidaE(getContext(),dadosSaidaEListE);
        recyclerViewList.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewList.setAdapter(adapterDadosSaidaE);

        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                .collection("nova saida").document("categoria").collection("Gastos Essenciais")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete(@org.checkerframework.checker.nullness.qual.NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for(QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                                DadosSaidaE dadosSaidaE = queryDocumentSnapshot.toObject(DadosSaidaE.class);
                                dadosSaidaEListE.add(dadosSaidaE);
                                adapterDadosSaidaE.notifyDataSetChanged();

                                // efeito Swipe
                                ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                        return false;
                                    }

                                    @Override
                                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                        final int position = viewHolder.getLayoutPosition();

                                        DadosSaidaE item = dadosSaidaEListE.get(position);
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Aviso Importante");
                                        builder.setMessage("Após a confirmação de exclusão, não será possível recuperar os dados apagados");
                                        builder.setCancelable(false);
                                        builder.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                progressBar.setVisibility(View.VISIBLE);

                                                DocumentReference documentReferenceAnual = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("saidas").collection("TotalSaidaAnual")
                                                        .document("Total");
                                                DocumentReference documentReferenceV = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                        .collection("nova saida").document("categoria").collection("Gastos Essenciais")
                                                        .document(item.getId());
                                                DocumentReference documentReferenceTotalDiario = db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalSaidasDiario")
                                                        .document(dia);

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
                                                                                                //document reference total Essenciais
                                                                                                DocumentReference documentReferenceE = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasE")
                                                                                                        .document("Total");

                                                                                                DocumentReference documentReferenceV = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                                        .collection("nova saida").document("categoria").collection("Gastos Essenciais")
                                                                                                        .document(item.getId());


                                                                                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskGeral) {
                                                                                                        documentReferenceE.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> taskE) {
                                                                                                                documentReferenceV.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskV) {
                                                                                                                        DocumentSnapshot documentSnapshot = taskGeral.getResult();
                                                                                                                        DocumentSnapshot documentSnapshotE = taskE.getResult();
                                                                                                                        DocumentSnapshot documentSnapshotV = taskV.getResult();
                                                                                                                        if (documentSnapshotV.exists()){
                                                                                                                            Double totalSaida = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaSaida"));
                                                                                                                            Double totalSaida2 = Double.parseDouble(documentSnapshotE.getString("ResultadoDaSomaSaidaE"));
                                                                                                                            Double totalSaida3 = Double.parseDouble(documentSnapshotV.getString("ValorDeSaidaDouble"));
                                                                                                                            Double op = totalSaida - totalSaida3;
                                                                                                                            Double op2 = totalSaida2 - totalSaida3;
                                                                                                                            if (op < 0){
                                                                                                                                op = 0.0;
                                                                                                                            } else if (op2 < 0) {
                                                                                                                                op2 = 0.0;
                                                                                                                            }
                                                                                                                            String cv = String.valueOf(op);
                                                                                                                            String cv2 = String.valueOf(op2);


                                                                                                                            db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de Saidas")
                                                                                                                                    .document("Total").update("ResultadoDaSomaSaida",cv);

                                                                                                                            db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasE")
                                                                                                                                    .document("Total").update("ResultadoDaSomaSaidaE",cv2);

                                                                                                                            db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                                                                                                                                    .collection("nova saida").document("categoria").collection("Gastos Essenciais")
                                                                                                                                    .document(item.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                        @Override
                                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                            try {
                                                                                                                                                Toast.makeText(getContext(), "item excluido com sucesso", Toast.LENGTH_LONG).show();
                                                                                                                                                progressBar.setVisibility(View.GONE);
                                                                                                                                                dadosSaidaEListE.remove(position);
                                                                                                                                                adapterDadosSaidaE.notifyDataSetChanged();
                                                                                                                                                RecuperarTotalSaidaE();
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
                                                adapterDadosSaidaE.notifyItemChanged(viewHolder.getAdapterPosition());
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

                                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                                    }

                                };
                                new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerViewList);
                            }

                        }
                    }
                });

    }
    public void RecuperarTotalSaidaE(){
        ValorTotalSaidas = (TextView) v.findViewById(R.id.ValorTotalSaidas);
        ValorTotalSaidasEPD = (TextView) v.findViewById(R.id.ValorTotalSaidasEPD);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //document reference total Essenciais
        DocumentReference documentReferenceE = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasE")
                .document("Total");


        //document reference total Essenciais
        documentReferenceE.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskE) {

                DocumentSnapshot documentSnapshotE = taskE.getResult();


                if (!documentSnapshotE.exists()) {
                    String vazioFormatado = NumberFormat.getCurrencyInstance(ptbr).format(vazio);
                    ValorTotalSaidasEPD.setText(vazioFormatado);
                    Log.d("TOTALRRCUPERAR", vazioFormatado);


                }else {
                    Double totalSaidaE = Double.parseDouble(documentSnapshotE.getString("ResultadoDaSomaSaidaE"));
                    String totalSaidaFormatadoE = NumberFormat.getCurrencyInstance(ptbr).format(totalSaidaE);
                    ValorTotalSaidasEPD.setText(totalSaidaFormatadoE);
                    Log.d("TOTALRRCUPERARE", totalSaidaFormatadoE);

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
                    Log.d("TOTALRRCUPERAR", totalSaidaFormatado);

                }



            }
        });


    }


}
