package br.com.lucas.valli.fluxodecaixa.Atividades;

import static br.com.lucas.valli.fluxodecaixa.Classes.ConversorDeMoeda.formatPriceSave;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import br.com.lucas.valli.fluxodecaixa.Adapter.AdapterContasAreceber;
import br.com.lucas.valli.fluxodecaixa.Classes.AlarmReceber;
import br.com.lucas.valli.fluxodecaixa.Classes.ConversorDeMoeda;
import br.com.lucas.valli.fluxodecaixa.Model.ContasAreceber;
import br.com.lucas.valli.fluxodecaixa.R;
import br.com.lucas.valli.fluxodecaixa.RecyclerItemClickListener.RecyclerItemClickListener;
import br.com.lucas.valli.fluxodecaixa.databinding.ActivityContasAreceberBinding;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ContasAReceber extends AppCompatActivity {
    private ActivityContasAreceberBinding binding;
    private AdapterContasAreceber adapterContasAreceber;
    private List<ContasAreceber> contasAreceber;
    private FirebaseFirestore db;
    private String usuarioID;

    private Date x = new Date();
    private String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
    private String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);

    private Locale ptBr = new Locale("pt", "BR");
    private Double vazio = Double.parseDouble("0.00");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityContasAreceberBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        binding.listaContasAReceber.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(),
                binding.listaContasAReceber, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
            }
            @Override
            public void onLongItemClick(View view, int position) {
                ContasAreceber item = contasAreceber.get(position);

                binding.progressBar.setVisibility(View.VISIBLE);

                // Infla o layout personalizado
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_editar_r, null);
                EditText txt_tipo = dialogView.findViewById(R.id.edit_tipoEntrada);
                EditText txt_valor = dialogView.findViewById(R.id.edit_valorEntrada);
                EditText txt_formPagamento = dialogView.findViewById(R.id.form_pagament);
                ImageView img_relogio = dialogView.findViewById(R.id.img_relogio);

                DocumentReference documentReferenceAlarm = db.collection(usuarioID).document("ContasAreceber").collection("dados")
                        .document(item.getId());

                documentReferenceAlarm.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.contains("TipoDeEntrada") && documentSnapshot.contains("ValorDeEntrada")
                                && documentSnapshot.contains("dataDeEntrada")){

                            String idMovimentacao = documentSnapshot.getString("idMovimentacao");
                            String dataVencimento = documentSnapshot.getString("dataVencimento");
                            int requestCode = Integer.parseInt(idMovimentacao); // O mesmo requestCode usado ao configurar o alarme
                            Intent intent = new Intent(getApplicationContext(), AlarmReceber.class);
                            intent.putExtra("notification_text_r", "Seu texto de notificação aqui"); // Deve corresponder ao Intent usado originalmente

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

                // Adiciona o TextWatcher ao TextView
                if (txt_valor != null) {
                    txt_valor.addTextChangedListener(new ConversorDeMoeda(dialogView.findViewById(R.id.edit_valorEntrada)));
                } else {
                    Log.e("TAG", "TextView é nulo");
                }

                DocumentReference documentReference = db.collection(usuarioID).document("ContasAreceber").collection("dados")
                        .document(item.getId());

                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ContasAReceber.this);
                builder.setView(dialogView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                                if (networkInfo == null){
                                    Log.d("NETCONEX", "SEM INTERNET");
                                    binding.progressBar.setVisibility(View.VISIBLE);
                                    Toast.makeText(ContasAReceber.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();

                                }else {
                                    binding.progressBar.setVisibility(View.GONE);

                                   DocumentReference documentReferenceTotal = db.collection(usuarioID).document("ContasAreceber")
                                           .collection("TotalContasAReceber").document("Total");

                                   documentReferenceTotal.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                       @Override
                                       public void onComplete(@NonNull Task<DocumentSnapshot> taskTotal) {
                                           documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                               @Override
                                               public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                   DocumentSnapshot documentSnapshot = task.getResult();
                                                   DocumentSnapshot documentSnapshotTotal = taskTotal.getResult();
                                                   if (documentSnapshot.contains("TipoDeEntrada") && documentSnapshot.contains("ValorDeEntrada")
                                                           && documentSnapshot.contains("dataDeEntrada")) {

                                                       String valorEntrada = documentSnapshot.getString("ValorDeEntrada");
                                                       String dataEntrada = documentSnapshot.getString("dataDeEntrada");
                                                       String valorEntradaDouble = documentSnapshot.getString("ValorDeEntradaDouble");
                                                       String TipoDeEntrada = documentSnapshot.getString("TipoDeEntrada");
                                                       String formaPagamento = documentSnapshot.getString("formPagamento");
                                                       String idMovimentacao = documentSnapshot.getString("idMovimentacao");

                                                       //texto campos dialogoCustom
                                                       String txt_tipoString =String.valueOf(txt_tipo.getText());
                                                       String txt_valorString =String.valueOf(txt_valor.getText());
                                                       String txt_formPagamentoString =String.valueOf(txt_formPagamento.getText());

                                                       //entradas convertidas para Double
                                                       String str = formatPriceSave(txt_valorString);
                                                       Double valorDoubleEditText = Double.parseDouble(str);
                                                       Double valorDoubleDb = Double.parseDouble(valorEntradaDouble);

                                                       //mesmo valor double para String
                                                       String txt_valor = String.valueOf(valorDoubleEditText);

                                                       //converter formato moeda
                                                       String ValorEntradaConvertido = NumberFormat.getCurrencyInstance(ptBr).format(valorDoubleEditText);

                                                       Log.d("PAGAMENTOOK", txt_tipoString + "\n" + valorDoubleDb + "\n" +txt_formPagamentoString);

                                                       if (!txt_tipoString.isEmpty() && !txt_valor.isEmpty()
                                                               && !txt_formPagamentoString.isEmpty()) {

                                                           if (!txt_tipoString.equals(TipoDeEntrada)) {
                                                               db.collection(usuarioID).document("ContasAreceber").collection("dados")
                                                                       .document(item.getId()).update("TipoDeEntrada", txt_tipoString);
                                                           }

                                                           if (!txt_valor.equals(valorEntradaDouble)) {

                                                               if (valorDoubleEditText < valorDoubleDb) {
                                                                   Double op = valorDoubleDb - valorDoubleEditText;

                                                                   if (documentSnapshotTotal.contains("ResultadoDaSomaEntradaC")) {
                                                                       Double valorTotal = Double.parseDouble(documentSnapshotTotal.getString("ResultadoDaSomaEntradaC"));
                                                                       Double op2 = valorTotal - op;
                                                                       db.collection(usuarioID).document("ContasAreceber")
                                                                               .collection("TotalContasAReceber").document("Total").update("ResultadoDaSomaEntradaC",
                                                                                       String.valueOf(op2));

                                                                       db.collection(usuarioID).document("ContasAreceber").collection("dados")
                                                                               .document(item.getId()).update("ValorDeEntrada", ValorEntradaConvertido, "ValorDeEntradaDouble", txt_valor);
                                                                   }

                                                               } else if (valorDoubleEditText > valorDoubleDb) {
                                                                   Double op = valorDoubleEditText - valorDoubleDb;

                                                                   if (documentSnapshotTotal.contains("ResultadoDaSomaEntradaC")) {
                                                                       Double valorTotal = Double.parseDouble(documentSnapshotTotal.getString("ResultadoDaSomaEntradaC"));
                                                                       Double op2 = op + valorTotal;

                                                                       db.collection(usuarioID).document("ContasAreceber")
                                                                               .collection("TotalContasAReceber").document("Total").update("ResultadoDaSomaEntradaC",
                                                                                       String.valueOf(op2));

                                                                       db.collection(usuarioID).document("ContasAreceber").collection("dados")
                                                                               .document(item.getId()).update("ValorDeEntrada", ValorEntradaConvertido, "ValorDeEntradaDouble", txt_valor);

                                                                   }
                                                               }

                                                           }


                                                           if (!txt_formPagamentoString.equals(formaPagamento)) {
                                                               db.collection(usuarioID).document("ContasAreceber").collection("dados")
                                                                       .document(item.getId()).update("formPagamento", txt_formPagamentoString);
                                                           }

                                                           onStart();
                                                       }else {
                                                           Toast.makeText(ContasAReceber.this, "Preencha todos os campos", Toast.LENGTH_LONG).show();
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
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContasAReceber.this, NovasContasAreceber.class);
                startActivity(intent);
            }
        });
        binding.tolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    public void cancelarAlarm(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(ContasAReceber.this);
                builder.setTitle("Atenção");
                builder.setMessage("deseja desativar o lembrete?");
                builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        binding.progressBar.setVisibility(View.VISIBLE);

                        ContasAreceber item = contasAreceber.get(position);

                        DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("ContasAreceber")
                                .document(item.getId());

                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.contains("TipoDeEntrada") && documentSnapshot.contains("ValorDeEntrada")
                                        && documentSnapshot.contains("dataDeEntrada")){

                                    String idMovimentacao = documentSnapshot.getString("idMovimentacao");
                                    int requestCode = Integer.parseInt(idMovimentacao); // O mesmo requestCode usado ao configurar o alarme
                                    Intent intent = new Intent(getApplicationContext(), AlarmReceber.class);
                                    intent.putExtra("notification_text_r", "Seu texto de notificação aqui"); // Deve corresponder ao Intent usado originalmente

                                    int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        flags |= PendingIntent.FLAG_IMMUTABLE;
                                    }

                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, flags);

                                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                    if (alarmManager != null) {
                                        alarmManager.cancel(pendingIntent);
                                        pendingIntent.cancel();
                                        Toast.makeText(ContasAReceber.this, "Alarme desativado com sucesso", Toast.LENGTH_SHORT).show();
                                        binding.progressBar.setVisibility(View.GONE);

                                    } else {
                                        Toast.makeText(ContasAReceber.this, "Falha ao desativar o alarme", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(ContasAReceber.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            RecuperarDadosContasAReceber();
            TotalContasAReceber();
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
    public void TotalContasAReceber(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReference = db.collection(usuarioID).document("ContasAreceber")
                .collection("TotalContasAReceber").document("Total");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (!documentSnapshot.exists()) {
                    String vazioFormatado = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                    binding.ValorTotal.setText(vazioFormatado);
                }else {
                    Double totalContasP = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaEntradaC"));
                    String totalContasPFormat = NumberFormat.getCurrencyInstance(ptBr).format(totalContasP);
                    binding.ValorTotal.setText(totalContasPFormat);
                }
            }
        });
    }
    public void RecuperarDadosContasAReceber(){

        Date x = new Date();
        String dia = new SimpleDateFormat("dd", new Locale("pt", "BR")).format(x);
        String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
        String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);

        contasAreceber = new ArrayList<>();
        adapterContasAreceber = new AdapterContasAreceber(getApplicationContext(), contasAreceber);
        binding.listaContasAReceber.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.listaContasAReceber.setHasFixedSize(true);
        binding.listaContasAReceber.setAdapter(adapterContasAreceber);


        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        db.collection(usuarioID).document("ContasAreceber").collection("dados")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                ContasAreceber contasAReceber = queryDocumentSnapshot.toObject(ContasAreceber.class);
                                contasAreceber.add(contasAReceber);
                                adapterContasAreceber.notifyDataSetChanged();

                                // efeito Swipe
                                ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                        return false;
                                    }

                                    @Override
                                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                        final int position = viewHolder.getLayoutPosition();

                                        if (direction == ItemTouchHelper.LEFT){
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ContasAReceber.this);
                                            builder.setTitle("Atenção");
                                            builder.setMessage("deseja excluir esse item?");
                                            builder.setCancelable(false);
                                            builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    binding.progressBar.setVisibility(View.VISIBLE);

                                                    ContasAreceber item = contasAreceber.get(position);
                                                    DocumentReference documentReferenceCTotal = db.collection(usuarioID).document("ContasAreceber")
                                                            .collection("TotalContasAReceber").document("Total");

                                                    DocumentReference documentReferenceC = db.collection(usuarioID).document("ContasAreceber").collection("dados")
                                                            .document(item.getId());

                                                    documentReferenceCTotal.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onEvent(@Nullable DocumentSnapshot documentSnapshotCTotal, @Nullable FirebaseFirestoreException error) {
                                                            documentReferenceC.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onEvent(@Nullable DocumentSnapshot documentSnapshotC, @Nullable FirebaseFirestoreException error) {
                                                                    if (documentSnapshotC.exists()){
                                                                        Double totalContasAReceber = Double.parseDouble(documentSnapshotCTotal.getString("ResultadoDaSomaEntradaC"));
                                                                        Double valorDoubleC = Double.parseDouble(documentSnapshotC.getString("ValorDeEntradaDouble"));
                                                                        Double operacao = totalContasAReceber - valorDoubleC;

                                                                        if (operacao < 0){
                                                                            operacao = 0.0;
                                                                        }

                                                                        String cv = String.valueOf(operacao);
                                                                        Log.d("DADOSRECEBER", cv);

                                                                        db.collection(usuarioID).document("ContasAreceber")
                                                                                .collection("TotalContasAReceber").document("Total").update("ResultadoDaSomaEntradaC", cv);

                                                                        db.collection(usuarioID).document("ContasAreceber").collection("dados")
                                                                                .document(item.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            Toast.makeText(getApplicationContext(), "item excluido com sucesso", Toast.LENGTH_LONG).show();
                                                                                            binding.progressBar.setVisibility(View.GONE);
                                                                                            contasAreceber.remove(viewHolder.getAdapterPosition());
                                                                                            adapterContasAreceber.notifyDataSetChanged();
                                                                                            onStart();

                                                                                        }else {

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
                                                    adapterContasAreceber.notifyItemChanged(viewHolder.getAdapterPosition());
                                                }
                                            });
                                            builder.show();
                                        } else if (direction == ItemTouchHelper.RIGHT) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ContasAReceber.this);
                                            builder.setTitle("Atenção");
                                            builder.setMessage("deseja confirmar recebimento?");
                                            builder.setCancelable(false);
                                            builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    binding.progressBar.setVisibility(View.VISIBLE);

                                                    ContasAreceber item = contasAreceber.get(position);

                                                    DocumentReference documentReference = db.collection(usuarioID).document("ContasAreceber").collection("dados")
                                                            .document(item.getId());

                                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            DocumentSnapshot documentSnapshot = task.getResult();
                                                            if (documentSnapshot.contains("TipoDeEntrada") && documentSnapshot.contains("ValorDeEntrada")
                                                                    && documentSnapshot.contains("dataDeEntrada")){

                                                                String tipoEntrada = documentSnapshot.getString("TipoDeEntrada");
                                                                String valorEntrada = documentSnapshot.getString("ValorDeEntrada");
                                                                String valorEntradaDouble = documentSnapshot.getString("ValorDeEntradaDouble");
                                                                String dataEntrada = documentSnapshot.getString("dataDeEntrada");
                                                                String formaPagamento = documentSnapshot.getString("formPagamento");
                                                                Double ValorCv = Double.parseDouble(valorEntradaDouble);
                                                                Log.d("PAGAMENTOOK", tipoEntrada +"\n" + valorEntrada + "\n" +valorEntradaDouble +
                                                                        "\n" + dataEntrada);

                                                                String id = UUID.randomUUID().toString();
                                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                                Map<String, Object> contasRecebidas = new HashMap<>();
                                                                contasRecebidas.put("dataDeEntrada", dataEntrada);
                                                                contasRecebidas.put("ValorDeEntradaDouble", valorEntradaDouble);
                                                                contasRecebidas.put("ValorDeEntrada", valorEntrada);
                                                                contasRecebidas.put("formPagamento", formaPagamento);
                                                                contasRecebidas.put("TipoDeEntrada", tipoEntrada);
                                                                contasRecebidas.put("id", id);
                                                                usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                                                DocumentReference documentReferenceContasAreceber = db.collection(usuarioID).document(ano).collection(mes)
                                                                        .document("entradas")
                                                                        .collection("nova entrada").document(id);


                                                                documentReferenceContasAreceber.set(contasRecebidas).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {

                                                                    }
                                                                });

                                                                //document reference ResumoDiaria
                                                                DocumentReference documentReferenceDiario = db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalEntradaDiario")
                                                                        .document(dia);
                                                                documentReferenceDiario.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskDiario) {
                                                                        DocumentSnapshot documentSnapshotDiario = taskDiario.getResult();


                                                                        if (documentSnapshotDiario.contains("ResultadoDaSomaEntradaDiario")){
                                                                            Double ValorDiario = Double.parseDouble(documentSnapshotDiario.getString("ResultadoDaSomaEntradaDiario"));

                                                                            Double SomaEntrada = ValorDiario + ValorCv;
                                                                            String SomaEntradaCv = String.valueOf(SomaEntrada);

                                                                            Map<String, Object> valorTotalDiairo = new HashMap<>();
                                                                            valorTotalDiairo.put("ResultadoDaSomaEntradaDiario", SomaEntradaCv);

                                                                            db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalEntradaDiario")
                                                                                    .document(dia).set(valorTotalDiairo).addOnCompleteListener(new OnCompleteListener<Void>() {
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

                                                                            Map<String, Object> valorTotalDiairo = new HashMap<>();
                                                                            valorTotalDiairo.put("ResultadoDaSomaEntradaDiario", ValorCvString);

                                                                            db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalEntradaDiario")
                                                                                    .document(dia).set(valorTotalDiairo).addOnCompleteListener(new OnCompleteListener<Void>() {
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

                                                                //document reference ResumoMensal
                                                                DocumentReference documentReferenceMensal = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                                                                        .document("Total");

                                                                documentReferenceMensal.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        DocumentSnapshot documentSnapshot = task.getResult();


                                                                        if (documentSnapshot.contains("ResultadoDaSomaEntrada")){

                                                                            Double ValorMensal = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaEntrada"));

                                                                            Double SomaEntrada = ValorMensal + ValorCv;
                                                                            String SomaEntradaCv = String.valueOf(SomaEntrada);

                                                                            Map<String, Object> valorTotalMensal = new HashMap<>();
                                                                            valorTotalMensal.put("ResultadoDaSomaEntrada", SomaEntradaCv);

                                                                            db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                                                                                    .document("Total").set(valorTotalMensal).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                        }
                                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {

                                                                                        }
                                                                                    });

                                                                        } else  {
                                                                            String ValorCvString = String.valueOf(ValorCv);

                                                                            Map<String, Object> valorTotal = new HashMap<>();
                                                                            valorTotal.put("ResultadoDaSomaEntrada", ValorCvString);

                                                                            db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                                                                                    .document("Total").set(valorTotal).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                        }
                                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {

                                                                                        }
                                                                                    });
                                                                        }

                                                                        DocumentReference documentReferenceCTotal = db.collection(usuarioID).document("ContasAreceber")
                                                                                .collection("TotalContasAReceber").document("Total");

                                                                        DocumentReference documentReferenceC = db.collection(usuarioID).document("ContasAreceber").collection("dados")
                                                                                .document(item.getId());

                                                                        documentReferenceCTotal.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshotCTotal, @Nullable FirebaseFirestoreException error) {
                                                                                documentReferenceC.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                                    @Override
                                                                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshotC, @Nullable FirebaseFirestoreException error) {
                                                                                        if (documentSnapshotC.exists()){
                                                                                            Double totalContasAReceber = Double.parseDouble(documentSnapshotCTotal.getString("ResultadoDaSomaEntradaC"));
                                                                                            Double valorDoubleC = Double.parseDouble(documentSnapshotC.getString("ValorDeEntradaDouble"));
                                                                                            Double operacao = totalContasAReceber - valorDoubleC;

                                                                                            if (operacao < 0){
                                                                                                operacao = 0.0;
                                                                                            }

                                                                                            String cv = String.valueOf(operacao);
                                                                                            Log.d("DADOSRECEBER", cv);

                                                                                            db.collection(usuarioID).document("ContasAreceber")
                                                                                                    .collection("TotalContasAReceber").document("Total").update("ResultadoDaSomaEntradaC", cv);

                                                                                            db.collection(usuarioID).document("ContasAreceber").collection("dados")
                                                                                                    .document(item.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()){
                                                                                                                Toast.makeText(getApplicationContext(), "Pagamento confirmado!", Toast.LENGTH_LONG).show();
                                                                                                                binding.progressBar.setVisibility(View.GONE);
                                                                                                                contasAreceber.remove(viewHolder.getAdapterPosition());
                                                                                                                adapterContasAreceber.notifyDataSetChanged();
                                                                                                                onStart();

                                                                                                            }else {

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

                                                                //document reference ResumoAnual
                                                                DocumentReference documentReferenceEntradasAnual = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("entradas").collection("TotalEntradaAnual")
                                                                        .document("Total");

                                                                documentReferenceEntradasAnual.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskAnual) {
                                                                        DocumentSnapshot documentSnapshotEntradasAnual = taskAnual.getResult();

                                                                        if (documentSnapshotEntradasAnual.contains("ResultadoTotalEntradaAnual")){
                                                                            Double ValorAnual = Double.parseDouble(documentSnapshotEntradasAnual.getString("ResultadoTotalEntradaAnual"));
                                                                            Double somaEntrada = ValorAnual + ValorCv;

                                                                            String SomaEntradaCv = String.valueOf(somaEntrada);

                                                                            Map<String, Object> ValorTotalAnual = new HashMap<>();
                                                                            ValorTotalAnual.put("ResultadoTotalEntradaAnual", SomaEntradaCv);

                                                                            db.collection(usuarioID).document(ano).collection("ResumoAnual").document("entradas").collection("TotalEntradaAnual")
                                                                                    .document("Total").set(ValorTotalAnual).addOnCompleteListener(new OnCompleteListener<Void>() {
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

                                                                            Map<String, Object> ValorTotalAnual = new HashMap<>();
                                                                            ValorTotalAnual.put("ResultadoTotalEntradaAnual", ValorCvString);

                                                                            db.collection(usuarioID).document(ano).collection("ResumoAnual").document("entradas").collection("TotalEntradaAnual")
                                                                                    .document("Total").set(ValorTotalAnual).addOnCompleteListener(new OnCompleteListener<Void>() {
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

                                                                //document reference Resumo
                                                                DocumentReference documentReferenceResumo = db.collection(usuarioID).document("resumoCaixa").collection("ResumoDeCaixa").document("entradas").collection("total")
                                                                        .document("ResumoTotal");

                                                                documentReferenceResumo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskResumo) {
                                                                        DocumentSnapshot documentSnapshotEntradasResumo = taskResumo.getResult();
                                                                        if (documentSnapshotEntradasResumo.contains("ResultadoTotal")){
                                                                            Double ValorDiario = Double.parseDouble(documentSnapshotEntradasResumo.getString("ResultadoTotal"));
                                                                            Log.i("RESUMO", String.valueOf(ValorDiario));
                                                                            Double SomaSaida = ValorDiario + ValorCv;
                                                                            Log.i("RESUMO", String.valueOf(SomaSaida));
                                                                            String SomaSaidaCv = String.valueOf(SomaSaida);

                                                                            //HasMap total
                                                                            Map<String, Object> valorTotal = new HashMap<>();
                                                                            valorTotal.put("ResultadoTotal", SomaSaidaCv);

                                                                            documentReferenceResumo.set(valorTotal);

                                                                        }else if(!documentSnapshotEntradasResumo.contains("ResultadoTotal")) {
                                                                            String ValorStringCv = String.valueOf(ValorCv);
                                                                            //HasMap total
                                                                            Map<String, Object> valorTotal = new HashMap<>();
                                                                            valorTotal.put("ResultadoTotal", ValorStringCv);

                                                                            documentReferenceResumo.set(valorTotal);
                                                                        }
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
                                                    adapterContasAreceber.notifyItemChanged(viewHolder.getAdapterPosition());
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
                                                    .addSwipeRightLabel("Recebido")
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
                                new ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.listaContasAReceber);

                            }
                        }
                    }
                });
    }
    private void showCustomDialog() {
        // Infla o layout personalizado
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_custom_cr, null);

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
                        DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("entradas")
                                .collection("Total Contas A Receber").document("Total");
                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (!documentSnapshot.exists()) {
                                    String vazioFormatado = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                                    binding.ValorTotal.setText(vazioFormatado);
                                    Log.d("TOTALENTRADAS", "SEM DADOS");
                                }else {
                                    Double totalContasR = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaEntradaC"));
                                    String totalContasRFormat = NumberFormat.getCurrencyInstance(ptBr).format(totalContasR);
                                    binding.ValorTotal.setText(totalContasRFormat);
                                    Log.d("TOTALENTRADAS", "COM DADOS");

                                }
                            }
                        });



                        contasAreceber = new ArrayList<>();
                        adapterContasAreceber = new AdapterContasAreceber(getApplicationContext(), contasAreceber);
                        binding.listaContasAReceber.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        binding.listaContasAReceber.setHasFixedSize(true);
                        binding.listaContasAReceber.setAdapter(adapterContasAreceber);


                        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        db = FirebaseFirestore.getInstance();
                        db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("ContasAreceber")
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                ContasAreceber contasAReceber = queryDocumentSnapshot.toObject(ContasAreceber.class);
                                                contasAreceber.add(contasAReceber);
                                                adapterContasAreceber.notifyDataSetChanged();

                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                                DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("entradas")
                                                        .collection("Total Contas A Receber").document("Total");
                                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        DocumentSnapshot documentSnapshot = task.getResult();
                                                        if (!documentSnapshot.exists()) {
                                                            String vazioFormatado = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                                                            binding.ValorTotal.setText(vazioFormatado);
                                                        }else {
                                                            Double totalContasR = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaEntradaC"));
                                                            String totalContasRFormat = NumberFormat.getCurrencyInstance(ptBr).format(totalContasR);
                                                            binding.ValorTotal.setText(totalContasRFormat);
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

                                                        if (direction == ItemTouchHelper.LEFT){
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(ContasAReceber.this);
                                                            builder.setTitle("Atenção");
                                                            builder.setMessage("deseja excluir esse item?");
                                                            builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    binding.progressBar.setVisibility(View.VISIBLE);

                                                                    ContasAreceber item = contasAreceber.get(position);
                                                                    DocumentReference documentReferenceCTotal = db.collection(usuarioID).document(ano).collection(mes).document("entradas")
                                                                            .collection("Total Contas A Receber").document("Total");

                                                                    DocumentReference documentReferenceC = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("ContasAreceber")
                                                                            .document(item.getId());

                                                                    documentReferenceCTotal.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onEvent(@Nullable DocumentSnapshot documentSnapshotCTotal, @Nullable FirebaseFirestoreException error) {
                                                                            documentReferenceC.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                                @Override
                                                                                public void onEvent(@Nullable DocumentSnapshot documentSnapshotC, @Nullable FirebaseFirestoreException error) {
                                                                                    if (documentSnapshotC.exists()){
                                                                                        Double totalContasAReceber = Double.parseDouble(documentSnapshotCTotal.getString("ResultadoDaSomaEntradaC"));
                                                                                        Double valorDoubleC = Double.parseDouble(documentSnapshotC.getString("ValorDeEntradaDouble"));
                                                                                        Double operacao = totalContasAReceber - valorDoubleC;

                                                                                        if (operacao < 0){
                                                                                            operacao = 0.0;
                                                                                        }

                                                                                        String cv = String.valueOf(operacao);
                                                                                        Log.d("DADOSRECEBER", cv);

                                                                                        db.collection(usuarioID).document(ano).collection(mes).document("entradas")
                                                                                                .collection("Total Contas A Receber").document("Total").update("ResultadoDaSomaEntradaC", cv);

                                                                                        db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("ContasAreceber")
                                                                                                .document(item.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()){
                                                                                                            Toast.makeText(getApplicationContext(), "item excluido com sucesso", Toast.LENGTH_LONG).show();
                                                                                                            binding.progressBar.setVisibility(View.GONE);
                                                                                                            contasAreceber.remove(viewHolder.getAdapterPosition());
                                                                                                            adapterContasAreceber.notifyDataSetChanged();
                                                                                                            onStart();

                                                                                                        }else {

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
                                                                    adapterContasAreceber.notifyItemChanged(viewHolder.getAdapterPosition());
                                                                }
                                                            });
                                                            builder.show();
                                                        } else if (direction == ItemTouchHelper.RIGHT) {
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(ContasAReceber.this);
                                                            builder.setTitle("Atenção");
                                                            builder.setMessage("deseja confirmar recebimento?");
                                                            builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    binding.progressBar.setVisibility(View.VISIBLE);

                                                                    ContasAreceber item = contasAreceber.get(position);

                                                                    DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("ContasAreceber")
                                                                            .document(item.getId());

                                                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            DocumentSnapshot documentSnapshot = task.getResult();
                                                                            if (documentSnapshot.contains("TipoDeEntrada") && documentSnapshot.contains("ValorDeEntrada")
                                                                                    && documentSnapshot.contains("dataDeEntrada")){

                                                                                String valorEntrada = documentSnapshot.getString("ValorDeEntrada");
                                                                                String dataEntrada = documentSnapshot.getString("dataDeEntrada");
                                                                                String valorEntradaDouble = documentSnapshot.getString("ValorDeEntradaDouble");
                                                                                String DadosEntrada = documentSnapshot.getString("TipoDeEntrada");
                                                                                String formaPagamento = documentSnapshot.getString("formPagamento");
                                                                                String idMovimentacao = documentSnapshot.getString("idMovimentacao");
                                                                                String id = UUID.randomUUID().toString();



                                                                                Log.d("PAGAMENTOOK", DadosEntrada +"\n" + valorEntrada + "\n" + dataEntrada + "\n" + valorEntradaDouble);

                                                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                                                Map<String, Object> entradas = new HashMap<>();
                                                                                entradas.put("ValorDeEntrada", valorEntrada);
                                                                                entradas.put("dataDeEntrada", dataEntrada);
                                                                                entradas.put("ValorDeEntradaDouble", valorEntradaDouble);
                                                                                entradas.put("TipoDeEntrada", DadosEntrada);
                                                                                entradas.put("formPagamento", formaPagamento);
                                                                                entradas.put("idMovimentacao", idMovimentacao);
                                                                                entradas.put("id", id);
                                                                                usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();


                                                                                DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes)
                                                                                        .document("entradas")
                                                                                        .collection("nova entrada").document(id);


                                                                                documentReference.set(entradas).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {

                                                                                    }
                                                                                });

                                                                                //document reference total
                                                                                DocumentReference documentReferenceEntradas = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                                                                                        .document("Total");

                                                                                documentReferenceEntradas.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                        DocumentSnapshot documentSnapshot = task.getResult();


                                                                                        if (documentSnapshot.contains("ResultadoDaSomaEntradas")) {
                                                                                            Double ValorC = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaEntradas"));
                                                                                            Double valorCVentrada = Double.parseDouble(valorEntradaDouble);
                                                                                            Double SomaEntrada = ValorC + valorCVentrada;
                                                                                            String SomaEntradaCv = String.valueOf(SomaEntrada);


                                                                                            Map<String, Object> valorTotalMensal = new HashMap<>();
                                                                                            valorTotalMensal.put("ResultadoDaSomaEntrada", SomaEntradaCv);
                                                                                            db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
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
                                                                                                String ValorCvString = (valorEntradaDouble);

                                                                                                Map<String, Object> valorTotal = new HashMap<>();
                                                                                                valorTotal.put("ResultadoDaSomaEntrada", ValorCvString);

                                                                                                db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                                                                                                        .document("Total").set(valorTotal).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                                        }
                                                                    });

                                                                }
                                                            });
                                                            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.cancel();
                                                                    adapterContasAreceber.notifyItemChanged(viewHolder.getAdapterPosition());
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
                                                                    .addSwipeRightLabel("Recebido")
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
                                                new ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.listaContasAReceber);
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
    public void cancelarAlarme() {
        int requestCode = 12345; // O mesmo requestCode usado ao configurar o alarme
        Intent intent = new Intent(getApplicationContext(), AlarmReceber.class);
        intent.putExtra("notification_text_r", "Seu texto de notificação aqui"); // Deve corresponder ao Intent usado originalmente

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, flags);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(this, "Alarme desativado com sucesso", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Falha ao desativar o alarme", Toast.LENGTH_SHORT).show();
        }
    }


}