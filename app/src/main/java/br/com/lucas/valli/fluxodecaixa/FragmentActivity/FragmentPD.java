package br.com.lucas.valli.fluxodecaixa.FragmentActivity;

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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.lucas.valli.fluxodecaixa.Adapter.AdapterDadosSaidaP;
import br.com.lucas.valli.fluxodecaixa.Model.DadosSaidaP;
import br.com.lucas.valli.fluxodecaixa.NovaSaida;
import br.com.lucas.valli.fluxodecaixa.R;
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
    private Button btnAddSaida;
    private ProgressBar progressBar;

    private Locale ptbr = new Locale("pt", "BR");
    private Double vazio = Double.parseDouble("0.00");

    @Override
    public void onStart() {
        super.onStart();

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.pd_fragment, container, false);
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
