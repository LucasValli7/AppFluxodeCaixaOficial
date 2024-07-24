package br.com.lucas.valli.fluxodecaixa.Atividades;

import static br.com.lucas.valli.fluxodecaixa.Classes.ConversorDeMoeda.formatPriceSave;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
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
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import br.com.lucas.valli.fluxodecaixa.Adapter.AdapterContasApagar;
import br.com.lucas.valli.fluxodecaixa.Classes.AlarmPagar;
import br.com.lucas.valli.fluxodecaixa.Classes.AlarmReceber;
import br.com.lucas.valli.fluxodecaixa.Classes.ConversorDeMoeda;
import br.com.lucas.valli.fluxodecaixa.Model.ContasApagar;
import br.com.lucas.valli.fluxodecaixa.Model.ContasAreceber;
import br.com.lucas.valli.fluxodecaixa.R;
import br.com.lucas.valli.fluxodecaixa.RecyclerItemClickListener.RecyclerItemClickListener;
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
    Locale ptbr = new Locale("pt", "BR");
    private Double vazio = Double.parseDouble("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityContasApagarBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContasAPagar.this, NovasContasApagar.class);
                startActivity(intent);
            }
        });
        binding.listaContasAPagar.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(),
                binding.listaContasAPagar, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onLongItemClick(View view, int position) {



                ContasApagar item = contasApagar.get(position);
                binding.progressBar.setVisibility(View.VISIBLE);

                // Infla o layout personalizado
                LayoutInflater inflater = getLayoutInflater();
               View dialogView = inflater.inflate(R.layout.dialog_editar_p, null);
                EditText txt_tipo = dialogView.findViewById(R.id.edit_tipoSaida);
                EditText txt_valor = dialogView.findViewById(R.id.edit_valorSaida);
                EditText txt_formPagamento = dialogView.findViewById(R.id.form_pagament);
                ImageView img_relogio = dialogView.findViewById(R.id.img_relogio);

                DocumentReference documentReferenceAlarm = db.collection(usuarioID).document("ContasApagar").collection("dados")
                        .document(item.getId());

                documentReferenceAlarm.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.contains("TipoDeSaida") && documentSnapshot.contains("ValorDeSaida")
                                && documentSnapshot.contains("dataDeSaida")){

                            String idMovimentacao = documentSnapshot.getString("idMovimentacao");
                            String dataVencimento = documentSnapshot.getString("dataVencimento");
                            int requestCode = Integer.parseInt(idMovimentacao); // O mesmo requestCode usado ao configurar o alarme
                            Intent intent = new Intent(getApplicationContext(), AlarmPagar.class);
                            intent.putExtra("notification_text", "Seu texto de notificação aqui"); // Deve corresponder ao Intent usado originalmente

                            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                flags |= PendingIntent.FLAG_IMMUTABLE;
                            }

                            // Formato da data
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            

                            try {
                                // Converter string para Date
                                Date dataAlvo = sdf.parse(dataVencimento);

                                // Data atual
                                Date dataAtual = new Date();
                                dataAtual = sdf.parse(sdf.format(dataAtual));

                                // Verificar se a data alvo já passou
                                if (dataAlvo.before(dataAtual)) {
                                    System.out.println("A data já passou.");
                                    img_relogio.setImageResource(R.drawable.ic_alarm_disabled);
                                    
                                } else if (dataAlvo.equals(dataAtual)) {
                                    System.out.println("A data é hoje.");
                                    img_relogio.setImageResource(R.drawable.ic_alarm_activated);
                                } else{
                                    System.out.println("A data ainda não passou.");
                                    img_relogio.setImageResource(R.drawable.ic_alarm_activated);
                                }
                            } catch (ParseException e) {
                                System.out.println("Formato de data inválido.");
                            }





                        }
                    }
                });
                

                Spinner txt_categoria = dialogView.findViewById(R.id.spinner_categoria);

                ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(ContasAPagar.this,
                        R.array.category_array, android.R.layout.simple_spinner_item);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                txt_categoria.setAdapter(categoryAdapter);

                DocumentReference documentReference = db.collection(usuarioID).document("ContasApagar").collection("dados")
                        .document(item.getId());



                // Adiciona o TextWatcher ao TextView
                if (txt_valor != null) {
                    txt_valor.addTextChangedListener(new ConversorDeMoeda(dialogView.findViewById(R.id.edit_valorSaida)));
                } else {
                    Log.e("TAG", "TextView é nulo");
                }

                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();

                        if (documentSnapshot.contains("TipoDeSaida") && documentSnapshot.contains("ValorDeSaida")
                                && documentSnapshot.contains("dataDeSaida")) {

                            String valorSaida = documentSnapshot.getString("ValorDeSaida");
                            String dataSaida = documentSnapshot.getString("dataDeSaida");
                            String valorSaidaDouble = documentSnapshot.getString("ValorDeSaidaDouble");
                            String TipoDeSaida = documentSnapshot.getString("TipoDeSaida");
                            String formaPagamento = documentSnapshot.getString("formPagamento");
                            String idMovimentacao = documentSnapshot.getString("idMovimentacao");
                            String categoria = documentSnapshot.getString("categoria");

                            txt_tipo.setText(TipoDeSaida);
                            txt_valor.setText(valorSaida);
                            txt_formPagamento.setText(formaPagamento);

                            if (categoria.equals("Gastos Essenciais")){
                                txt_categoria.setSelection(0);
                            } else if (categoria.equals("Pagamento de Dívidas")) {
                                txt_categoria.setSelection(1);
                            }else if (categoria.equals("Desejos Pessoais")) {
                                txt_categoria.setSelection(2);
                            }


                        }
                    }
                });


                // Cria e mostra o AlertDialog
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ContasAPagar.this);
                builder.setView(dialogView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                                if (networkInfo == null){
                                    Log.d("NETCONEX", "SEM INTERNET");
                                    binding.progressBar.setVisibility(View.VISIBLE);
                                    Toast.makeText(ContasAPagar.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();

                                }else {



                                    DocumentReference documentReferenceTotal = db.collection(usuarioID).document("ContasApagar")
                                            .collection("TotalContasAPagar").document("Total");

                                    documentReferenceTotal.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> taskTotal) {
                                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    DocumentSnapshot documentSnapshot = task.getResult();
                                                    DocumentSnapshot documentSnapshotTotal = taskTotal.getResult();
                                                    if (documentSnapshot.contains("TipoDeSaida") && documentSnapshot.contains("ValorDeSaida")
                                                            && documentSnapshot.contains("dataDeSaida")) {

                                                        String valorSaida = documentSnapshot.getString("ValorDeSaida");
                                                        String dataSaida = documentSnapshot.getString("dataDeSaida");
                                                        String valorSaidaDouble = documentSnapshot.getString("ValorDeSaidaDouble");
                                                        String TipoDeSaida = documentSnapshot.getString("TipoDeSaida");
                                                        String formaPagamento = documentSnapshot.getString("formPagamento");
                                                        String idMovimentacao = documentSnapshot.getString("idMovimentacao");
                                                        String categoria = documentSnapshot.getString("categoria");

                                                        //texto campos dialogoCustom
                                                        String txt_tipoString =String.valueOf(txt_tipo.getText());
                                                        String txt_valorString =String.valueOf(txt_valor.getText());
                                                        String txt_formPagamentoString =String.valueOf(txt_formPagamento.getText());


                                                        //entradas convertidas para Double
                                                        String str = formatPriceSave(txt_valorString);
                                                        Double valorDoubleEditText = Double.parseDouble(str);
                                                        Double valorDoubleDb = Double.parseDouble(valorSaidaDouble);
                                                        String txt_category = txt_categoria.getSelectedItem().toString();

                                                        //mesmo valor double para String
                                                        String txt_valor = String.valueOf(valorDoubleEditText);

                                                        //converter formato moeda
                                                        String ValorSaidaConvertido = NumberFormat.getCurrencyInstance(ptbr).format(valorDoubleEditText);

                                                        Log.d("PAGAMENTOOK", txt_tipoString + "\n" + valorDoubleDb + "\n" +txt_formPagamentoString);

                                                        if (!txt_tipoString.isEmpty() && !txt_valor.isEmpty()
                                                                && !txt_formPagamentoString.isEmpty() && !txt_category.isEmpty()) {


                                                            if (!txt_tipoString.equals(TipoDeSaida)) {
                                                                db.collection(usuarioID).document("ContasApagar").collection("dados")
                                                                        .document(item.getId()).update("TipoDeSaida", txt_tipoString);
                                                            }

                                                            if (!txt_valor.equals(valorSaidaDouble)) {

                                                                if (valorDoubleEditText < valorDoubleDb) {
                                                                    Double op = valorDoubleDb - valorDoubleEditText;

                                                                    if (documentSnapshotTotal.contains("ResultadoDaSomaSaidaC")) {
                                                                        Double valorTotal = Double.parseDouble(documentSnapshotTotal.getString("ResultadoDaSomaSaidaC"));
                                                                        Double op2 = valorTotal - op;
                                                                        db.collection(usuarioID).document("ContasApagar")
                                                                                .collection("TotalContasAPagar").document("Total").update("ResultadoDaSomaSaidaC",
                                                                                        String.valueOf(op2));

                                                                        db.collection(usuarioID).document("ContasApagar").collection("dados")
                                                                                .document(item.getId()).update("ValorDeSaida", ValorSaidaConvertido, "ValorDeSaidaDouble", txt_valor);
                                                                    }

                                                                } else if (valorDoubleEditText > valorDoubleDb) {
                                                                    Double op = valorDoubleEditText - valorDoubleDb;

                                                                    if (documentSnapshotTotal.contains("ResultadoDaSomaSaidaC")) {
                                                                        Double valorTotal = Double.parseDouble(documentSnapshotTotal.getString("ResultadoDaSomaSaidaC"));
                                                                        Double op2 = op + valorTotal;

                                                                        db.collection(usuarioID).document("ContasApagar")
                                                                                .collection("TotalContasAPagar").document("Total").update("ResultadoDaSomaSaidaC",
                                                                                        String.valueOf(op2));

                                                                        db.collection(usuarioID).document("ContasApagar").collection("dados")
                                                                                .document(item.getId()).update("ValorDeSaida", ValorSaidaConvertido, "ValorDeSaidaDouble", txt_valor);

                                                                    }
                                                                }

                                                            }


                                                            if (!txt_formPagamentoString.equals(formaPagamento)) {
                                                                db.collection(usuarioID).document("ContasApagar").collection("dados")
                                                                        .document(item.getId()).update("formPagamento", txt_formPagamentoString);
                                                            }


                                                            if (!txt_category.equals(categoria)) {
                                                                db.collection(usuarioID).document("ContasApagar").collection("dados")
                                                                        .document(item.getId()).update("categoria", txt_category);
                                                            }

                                                            onStart();
                                                        }else {
                                                            Toast.makeText(ContasAPagar.this, "Preencha todos os campos", Toast.LENGTH_LONG).show();
                                                        }

                                                    }
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

        DocumentReference documentReference = db.collection(usuarioID).document("ContasApagar")
                .collection("TotalContasAPagar").document("Total");
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
        db.collection(usuarioID).document("ContasApagar").collection("dados")
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
                                        builder.setCancelable(false);
                                        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                binding.progressBar.setVisibility(View.VISIBLE);

                                                ContasApagar item = contasApagar.get(position);
                                                DocumentReference documentReferenceCTotal = db.collection(usuarioID).document("ContasApagar")
                                                        .collection("TotalContasAPagar").document("Total");

                                                DocumentReference documentReferenceC = db.collection(usuarioID).document("ContasApagar").collection("dados")
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

                                                                    db.collection(usuarioID).document("ContasApagar")
                                                                            .collection("TotalContasAPagar").document("Total").update("ResultadoDaSomaSaidaC", cv);

                                                                    db.collection(usuarioID).document("ContasApagar").collection("dados")
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
                                        builder.setCancelable(false);
                                        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                binding.progressBar.setVisibility(View.VISIBLE);

                                                ContasApagar item = contasApagar.get(position);

                                                DocumentReference documentReference = db.collection(usuarioID).document("ContasApagar").collection("dados")
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
                                                            String formaPagamento = documentSnapshot.getString("formPagamento");
                                                            String categoria = documentSnapshot.getString("categoria");
                                                            Double ValorCv = Double.parseDouble(valorSaidaDouble);
                                                            Log.d("PAGAMENTOOK", tipoSaida +"\n" + valorSaida + "\n" +valorSaidaDouble +
                                                                    "\n" + dataSaida +"\n"+formaPagamento+"\n"+categoria);

                                                            String id = UUID.randomUUID().toString();
                                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                            Map<String, Object> contasApagar = new HashMap<>();
                                                            contasApagar.put("TipoDeSaida", tipoSaida);
                                                            contasApagar.put("ValorDeSaidaDouble", valorSaidaDouble);
                                                            contasApagar.put("ValorDeSaida", valorSaida);
                                                            contasApagar.put("dataDeSaida", dataSaida);
                                                            contasApagar.put("id", id);
                                                            contasApagar.put("formPagamento", formaPagamento);
                                                            contasApagar.put("categoria", categoria);
                                                            usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();


                                                            DocumentReference documentReferenceContasApagar = db.collection(usuarioID).document(ano).collection(mes)
                                                                    .document("saidas")
                                                                    .collection("nova saida").document("categoria").collection(categoria).document(id);

                                                            documentReferenceContasApagar.set(contasApagar).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {

                                                                }
                                                            });

                                                            //document reference ResumoDiario
                                                            DocumentReference documentReferenceTotalDiario = db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalSaidasDiario")
                                                                    .document(dia);
                                                            documentReferenceTotalDiario.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> taskDiario) {
                                                                    DocumentSnapshot documentSnapshotTotalDiario = taskDiario.getResult();

                                                                    if (documentSnapshotTotalDiario.contains("ResultadoDaSomaSaidaDiario")){
                                                                        Double ValorDiario = Double.parseDouble(documentSnapshotTotalDiario.getString("ResultadoDaSomaSaidaDiario"));
                                                                        Double SomaSaida = ValorDiario + ValorCv;
                                                                        String SomaSaidaCv = String.valueOf(SomaSaida);

                                                                        Map<String, Object> valorTotalDiairo = new HashMap<>();
                                                                        valorTotalDiairo.put("ResultadoDaSomaSaidaDiario", SomaSaidaCv);

                                                                        db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalSaidasDiario")
                                                                                .document(dia).set(valorTotalDiairo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {

                                                                                    }
                                                                                });

                                                                    }else {
                                                                        String ValorStringCv = String.valueOf(ValorCv);
                                                                        Map<String, Object> valorTotalDiairo = new HashMap<>();
                                                                        valorTotalDiairo.put("ResultadoDaSomaSaidaDiario", ValorStringCv);

                                                                        db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalSaidasDiario")
                                                                                .document(dia).set(valorTotalDiairo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {

                                                                                    }
                                                                                });

                                                                    }
                                                                }
                                                            });

                                                            //document reference ResumoMensal
                                                            DocumentReference documentReferenceMensal = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de Saidas")
                                                                    .document("Total");

                                                            documentReferenceMensal.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> taskGeral) {
                                                                    DocumentSnapshot documentSnapshotMensal = taskGeral.getResult();

                                                                    if (documentSnapshotMensal.contains("ResultadoDaSomaSaida")){
                                                                        Double ValorDiario = Double.parseDouble(documentSnapshotMensal.getString("ResultadoDaSomaSaida"));
                                                                        Double SomaSaida = ValorDiario + ValorCv;
                                                                        String SomaSaidaCv = String.valueOf(SomaSaida);

                                                                        //HasMap total
                                                                        Map<String, Object> valorTotalMensal = new HashMap<>();
                                                                        valorTotalMensal.put("ResultadoDaSomaSaida", SomaSaidaCv);

                                                                        // recuperar total(geral)
                                                                        db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de Saidas")
                                                                                .document("Total").set(valorTotalMensal).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {


                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {

                                                                                    }
                                                                                });
                                                                    }else {
                                                                        String ValorStringCv = String.valueOf(ValorCv);
                                                                        //HasMap total
                                                                        Map<String, Object> valorTotalMensal = new HashMap<>();
                                                                        valorTotalMensal.put("ResultadoDaSomaSaida", ValorStringCv);

                                                                        // recuperar total(geral)
                                                                        db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de Saidas")
                                                                                .document("Total").set(valorTotalMensal).addOnCompleteListener(new OnCompleteListener<Void>() {
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

                                                            //document reference ResumoAnual
                                                            DocumentReference documentReferenceAnual = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("saidas").collection("TotalSaidaAnual")
                                                                    .document("Total");

                                                            documentReferenceAnual.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> taskAnual) {
                                                                    DocumentSnapshot documentSnapshotAnual = taskAnual.getResult();

                                                                    if (documentSnapshotAnual.contains("ResultadoTotalSaidaAnual")){
                                                                        Double ValorDiario = Double.parseDouble(documentSnapshotAnual.getString("ResultadoTotalSaidaAnual"));
                                                                        Double SomaSaida = ValorDiario + ValorCv;
                                                                        String SomaSaidaCv = String.valueOf(SomaSaida);

                                                                        //HasMap total
                                                                        Map<String, Object> valorTotalAnual = new HashMap<>();
                                                                        valorTotalAnual.put("ResultadoTotalSaidaAnual", SomaSaidaCv);

                                                                        db.collection(usuarioID).document(ano).collection("ResumoAnual").document("saidas").collection("TotalSaidaAnual")
                                                                                .document("Total").set(valorTotalAnual).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {

                                                                                    }
                                                                                });

                                                                    }else {
                                                                        String ValorStringCv = String.valueOf(ValorCv);
                                                                        //HasMap total
                                                                        Map<String, Object> valorTotalAnual = new HashMap<>();
                                                                        valorTotalAnual.put("ResultadoTotalSaidaAnual", ValorStringCv);

                                                                        db.collection(usuarioID).document(ano).collection("ResumoAnual").document("saidas").collection("TotalSaidaAnual")
                                                                                .document("Total").set(valorTotalAnual).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {

                                                                                    }
                                                                                });

                                                                    }
                                                                }
                                                            });

                                                            //document reference Resumo
                                                            DocumentReference documentReferenceResumo = db.collection(usuarioID).document("resumoCaixa").collection("ResumoDeCaixa").document("saidas").collection("total")
                                                                    .document("ResumoTotal");

                                                            documentReferenceResumo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> taskResumo) {
                                                                    DocumentSnapshot documentSnapshotEntradasResumo = taskResumo.getResult();
                                                                    if (documentSnapshotEntradasResumo.contains("ResultadoTotal")){
                                                                        Double ValorDiario = Double.parseDouble(documentSnapshotEntradasResumo.getString("ResultadoTotal"));
                                                                        Log.i("RESUMO", String.valueOf(ValorDiario));
                                                                        Double SomaSaida = ValorDiario + ValorCv;
                                                                        String SomaSaidaCv = String.valueOf(SomaSaida);
                                                                        Log.i("RESUMO", String.valueOf(SomaSaida));

                                                                        //HasMap total
                                                                        Map<String, Object> valorTotal = new HashMap<>();
                                                                        valorTotal.put("ResultadoTotal", SomaSaidaCv);

                                                                        documentReferenceResumo.set(valorTotal);

                                                                    }else {
                                                                        String ValorStringCv = String.valueOf(ValorCv);
                                                                        //HasMap total
                                                                        Map<String, Object> valorTotal = new HashMap<>();
                                                                        valorTotal.put("ResultadoTotal", ValorStringCv);

                                                                        documentReferenceResumo.set(valorTotal);
                                                                    }
                                                                }
                                                            });




                                                            if (categoria.equals("Gastos Essenciais")){
                                                                //document reference total Essenciais
                                                                DocumentReference documentReferenceE = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasE")
                                                                        .document("Total");

                                                                documentReferenceE.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskE) {
                                                                        DocumentSnapshot documentSnapshotE = taskE.getResult();

                                                                        if (documentSnapshotE.contains("ResultadoDaSomaSaidaE")){
                                                                            Double ValorDiario = Double.parseDouble(documentSnapshotE.getString("ResultadoDaSomaSaidaE"));
                                                                            Double SomaSaida = ValorDiario + ValorCv;
                                                                            String SomaSaidaCv = String.valueOf(SomaSaida);

                                                                            // HasMap total E
                                                                            Map<String, Object> valorTotalE = new HashMap<>();
                                                                            valorTotalE.put("ResultadoDaSomaSaidaE", SomaSaidaCv);

                                                                            db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasE")
                                                                                    .document("Total").set(valorTotalE).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                                            // HasMap total E
                                                                            Map<String, Object> valorTotalE = new HashMap<>();
                                                                            valorTotalE.put("ResultadoDaSomaSaidaE", ValorCvString);

                                                                            db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasE")
                                                                                    .document("Total").set(valorTotalE).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                            } else if (categoria.equals("Pagamento de Dívidas")) {
                                                                //document reference total Pagamento de Dívidas
                                                                DocumentReference documentReferenceP = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasP")
                                                                        .document("Total");

                                                                documentReferenceP.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskP) {
                                                                        DocumentSnapshot documentSnapshotP = taskP.getResult();

                                                                        if (documentSnapshotP.contains("ResultadoDaSomaSaidaP")){
                                                                            Double ValorP= Double.parseDouble(documentSnapshotP.getString("ResultadoDaSomaSaidaP"));
                                                                            Double SomaSaida = ValorP + ValorCv;
                                                                            String SomaSaidaCv = String.valueOf(SomaSaida);

                                                                            // HasMap total P
                                                                            Map<String, Object> valorTotalP = new HashMap<>();
                                                                            valorTotalP.put("ResultadoDaSomaSaidaP", SomaSaidaCv);

                                                                            db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasP")
                                                                                    .document("Total").set(valorTotalP).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                                            // HasMap total E
                                                                            Map<String, Object> valorTotalP = new HashMap<>();
                                                                            valorTotalP.put("ResultadoDaSomaSaidaP", ValorCvString);

                                                                            db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasP")
                                                                                    .document("Total").set(valorTotalP).addOnCompleteListener(new OnCompleteListener<Void>() {
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

                                                            } else if (categoria.equals("Desejos Pessoais")) {
                                                                //document reference total Desejos Pessoais
                                                                DocumentReference documentReferenceD = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasD")
                                                                        .document("Total");

                                                                documentReferenceD.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskD) {
                                                                        DocumentSnapshot documentSnapshotD = taskD.getResult();

                                                                        if (documentSnapshotD.contains("ResultadoDaSomaSaidaD")){
                                                                            Double ValorD= Double.parseDouble(documentSnapshotD.getString("ResultadoDaSomaSaidaD"));
                                                                            Double SomaSaida = ValorD + ValorCv;
                                                                            String SomaSaidaCv = String.valueOf(SomaSaida);

                                                                            // HasMap total D
                                                                            Map<String, Object> valorTotalD = new HashMap<>();
                                                                            valorTotalD.put("ResultadoDaSomaSaidaD", SomaSaidaCv);

                                                                            db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasD")
                                                                                    .document("Total").set(valorTotalD).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                                            // HasMap total D
                                                                            Map<String, Object> valorTotalD = new HashMap<>();
                                                                            valorTotalD.put("ResultadoDaSomaSaidaD", ValorCvString);

                                                                            db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasD")
                                                                                    .document("Total").set(valorTotalD).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                            }

                                                            DocumentReference documentReferenceCTotal = db.collection(usuarioID).document("ContasApagar")
                                                                    .collection("TotalContasAPagar").document("Total");

                                                            DocumentReference documentReferenceC = db.collection(usuarioID).document("ContasApagar").collection("dados")
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

                                                                                db.collection(usuarioID).document("ContasApagar")
                                                                                        .collection("TotalContasAPagar").document("Total").update("ResultadoDaSomaSaidaC", cv);

                                                                                db.collection(usuarioID).document("ContasApagar").collection("dados")
                                                                                        .document(item.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    Toast.makeText(getApplicationContext(), "Pagamento confirmado!", Toast.LENGTH_LONG).show();
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
                                                        Log.d("Swipe", "dX: " + dX);

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
                                                        Log.d("Swipe", "dX: " + dX);

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
    public void cancelarAlarme(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ContasAPagar.this);
                builder.setTitle("Atenção");
                builder.setMessage("deseja desativar o lembrete?");
                builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        Date x = new Date();
                        String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
                        String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);
                        ContasApagar item = contasApagar.get(position);

                        DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("ContasApagar")
                                .document(item.getId());

                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.contains("TipoDeSaida") && documentSnapshot.contains("ValorDeSaida")
                                        && documentSnapshot.contains("dataDeSaida")){

                                    String idMovimentacao = documentSnapshot.getString("idMovimentacao");
                                    int requestCode = Integer.parseInt(idMovimentacao); // O mesmo requestCode usado ao configurar o alarme
                                    Intent intent = new Intent(getApplicationContext(), AlarmPagar.class);
                                    intent.putExtra("notification_text", "Seu texto de notificação aqui"); // Deve corresponder ao Intent usado originalmente

                                    int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        flags |= PendingIntent.FLAG_IMMUTABLE;
                                    }

                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, flags);

                                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                    if (alarmManager != null) {
                                        alarmManager.cancel(pendingIntent);
                                        Toast.makeText(ContasAPagar.this, "Alarme desativado com sucesso", Toast.LENGTH_SHORT).show();
                                        binding.progressBar.setVisibility(View.GONE);

                                    } else {
                                        Toast.makeText(ContasAPagar.this, "Falha ao desativar o alarme", Toast.LENGTH_SHORT).show();
                                    }






                                }
                            }
                        });

                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });
                builder.show();
    }

}