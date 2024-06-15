package br.com.lucas.valli.fluxodecaixa;

import static br.com.lucas.valli.fluxodecaixa.Model.ConversorDeMoeda.formatPriceSave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.TimePicker;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import br.com.lucas.valli.fluxodecaixa.Model.ConversorDeMoeda;
import br.com.lucas.valli.fluxodecaixa.databinding.ActivityNovaEntradaBinding;
public class NovaEntrada extends AppCompatActivity {
    private ActivityNovaEntradaBinding binding;
    private String usuarioID;
    private Locale ptbr = new Locale("pt", "BR");
    private Double vazio = Double.parseDouble("0.00");
    private Date x = new Date();
    private String dia = new SimpleDateFormat("dd", new Locale("pt", "BR")).format(x);
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItem;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onStart() {
        super.onStart();
        PassarDataAutomatica();
        BtnSalvar();
        OuvinteRadioGroup();



    }
    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            Log.d("NETCONEX", "SEM INTERNET");
            Toast.makeText(NovaEntrada.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.VISIBLE);
            return false;
        }else {
            String novaEntrada = binding.editNovaEntrada.getText().toString();
            String novoValor = binding.editNovoValor.getText().toString();
            String formPagamento = binding.autoCompleteTextForm.getText().toString();
            if (novaEntrada.isEmpty() || novoValor.isEmpty() || formPagamento.isEmpty()) {
                Snackbar snackbar = Snackbar.make(getWindow().getDecorView(), "Preencha todos os campos", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                snackbar.show();

            } else if (binding.checksSim.isChecked() || binding.checksNao.isChecked()) {
                Toast.makeText(NovaEntrada.this,"Salvo com Sucesso",Toast.LENGTH_SHORT).show();
                EnviarDadosListaEntradaBD();
                EnviarTotalDiario();
                EnviarTotalMensal();
                EnviarTotalAnual();
                ShowIntesticial();
                binding.floatingActionButton.setEnabled(false);
                binding.progressBar.setVisibility(View.GONE);

                if (binding.checksSim.isChecked()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NovaEntrada.this);
                    builder.setTitle("Deseja adicionar um lembrete de pagamento?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Lógica para quando o radioButton1 é selecionado
                            String titulo = binding.editNovaEntrada.getText().toString();
                            String valor = binding.editNovoValor.getText().toString();
                            scheduleNotification("Receber " + titulo + " no valor de " + "R$ " + valor);
                        }
                    }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(NovaEntrada.this, TelaPrincipal.class);
                            finish();
                        }
                    }).show();
                }else {
                    finish();
                }


            }else {

                Snackbar snackbar = Snackbar.make(getWindow().getDecorView(), "Preencha o campo Contas a Pagar", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                snackbar.show();


            }
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
            Log.d("NETCONEX", "WIFI");
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
            Log.d("NETCONEX", "DADOS");
        }
        return networkInfo.isConnected();

    }
    public void BtnSalvar(){
        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnection();

            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityNovaEntradaBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        FormaPagamento();

        binding.editNovoValor.addTextChangedListener(new ConversorDeMoeda(binding.editNovoValor));
        binding.tolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.li.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackbar = Snackbar.make(v,"Não é permitido adicionar movimentação com datas retroativas",Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                snackbar.show();
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

        InterstitialAd.load(this, String.valueOf(R.string.admob_id_teste), adRequest,
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
            mInterstitialAd.show(NovaEntrada.this);
        } else {
            Log.d("ADSTESTE", "The interstitial ad wasn't ready yet.");
        }
    }
    public void FormaPagamento(){
        String [] formaPagamento = {"Transferência", "Boleto", "Pix", "Dinheiro", "Depósito","Nenhuma"};

        autoCompleteTextView = findViewById(R.id.auto_complete_text_form);
        adapterItem = new ArrayAdapter<String>(this,R.layout.nova_entrada_item, formaPagamento);
        autoCompleteTextView.setAdapter(adapterItem);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String Item = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(NovaEntrada.this, Item+ " selecionado", Toast.LENGTH_SHORT).show();



            }
        });

    }
    public void EnviarTotalDiario(){
        String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
        String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReferenceDiario = db.collection(usuarioID).document(ano).collection(mes).document("ResumoDiario").collection("TotalEntradaDiario")
                .document(dia);

        documentReferenceDiario.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskDiario) {
                DocumentSnapshot documentSnapshotDiario = taskDiario.getResult();
                String str = formatPriceSave(binding.editNovoValor.getText().toString());
                Double valorCV = Double.parseDouble(str);

                if (documentSnapshotDiario.contains("ResultadoDaSomaEntradaDiario")){
                    Double ValorDiario = Double.parseDouble(documentSnapshotDiario.getString("ResultadoDaSomaEntradaDiario"));

                    Double SomaEntrada = ValorDiario + valorCV;
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
                    String ValorCvString = String.valueOf(valorCV);

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
    }
    public void EnviarTotalMensal(){

        String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
        String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("entradas").collection("Total de Entradas")
                .document("Total");

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                String str = formatPriceSave(binding.editNovoValor.getText().toString());
                Double valorCV = Double.parseDouble(str);

                if (documentSnapshot.exists()){

                    Double ValorMensal = Double.parseDouble(documentSnapshot.getString("ResultadoDaSomaEntrada"));

                    Double SomaEntrada = ValorMensal + valorCV;
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
                    String ValorCvString = String.valueOf(valorCV);

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
    public void EnviarTotalAnual(){
        String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
        String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReferenceEntradasAnual = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("entradas").collection("TotalEntradaAnual")
                .document("Total");

        documentReferenceEntradasAnual.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskAnual) {
                DocumentSnapshot documentSnapshotEntradasAnual = taskAnual.getResult();
                String str = formatPriceSave(binding.editNovoValor.getText().toString());
                Double valorCV = Double.parseDouble(str);

                if (documentSnapshotEntradasAnual.contains("ResultadoTotalEntradaAnual")){
                    Double ValorAnual = Double.parseDouble(documentSnapshotEntradasAnual.getString("ResultadoTotalEntradaAnual"));
                    Double somaEntrada = ValorAnual + valorCV;

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
                    String ValorCvString = String.valueOf(valorCV);

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
    }
    public void EnviarTotalContasReceber(){
        String mes = binding.addData2.getText().toString();
        String ano = binding.addData3.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //document reference total Contas À receber
        DocumentReference documentReferenceC = db.collection(usuarioID).document(ano).collection(mes).document("entradas")
                .collection("Total Contas A Receber").document("Total");

        documentReferenceC.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskC) {
                DocumentSnapshot documentSnapshotC = taskC.getResult();
                String str = formatPriceSave(binding.editNovoValor.getText().toString());
                Double valorCV = Double.parseDouble(str);

                if (documentSnapshotC.contains("ResultadoDaSomaEntradaC")){
                    Double ValorC = Double.parseDouble(documentSnapshotC.getString("ResultadoDaSomaEntradaC"));
                    Double SomaEntrada = ValorC + valorCV;
                    String SomaEntradaCv = String.valueOf(SomaEntrada);

                    // HasMap total D
                    Map<String, Object> valorTotalC = new HashMap<>();
                    valorTotalC.put("ResultadoDaSomaEntradaC", SomaEntradaCv);

                    db.collection(usuarioID).document(ano).collection(mes).document("entradas")
                            .collection("Total Contas A Receber").document("Total").set(valorTotalC).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                }else {
                    String ValorCvString = String.valueOf(valorCV);
                    // HasMap total C
                    Map<String, Object> valorTotalC = new HashMap<>();
                    valorTotalC.put("ResultadoDaSomaEntradaC", ValorCvString);

                    db.collection(usuarioID).document(ano).collection(mes).document("entradas")
                            .collection("Total Contas A Receber").document("Total").set(valorTotalC).addOnCompleteListener(new OnCompleteListener<Void>() {
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
    public void EnviarDadosListaEntradaBD(){
        String dia = binding.addData.getText().toString();
        String mes = binding.addData2.getText().toString();
        String ano = binding.addData3.getText().toString();

        String DadosEntrada = binding.editNovaEntrada.getText().toString();

        String str = formatPriceSave(binding.editNovoValor.getText().toString());
        Double ValorEntrada = Double.parseDouble(str);
        String ValorEntradaConvertido = NumberFormat.getCurrencyInstance(ptbr).format(ValorEntrada);
        String ValorEntradaDouble = String.valueOf(ValorEntrada);
        String dataEntrada = dia + mes + ano;
        String formaPagament = binding.autoCompleteTextForm.getText().toString();
        String formaPagamento = "Entrada realizada por " + formaPagament;
        String id = UUID.randomUUID().toString();


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> entradas = new HashMap<>();
        entradas.put("dataDeEntrada", dataEntrada);
        entradas.put("ValorDeEntradaDouble", ValorEntradaDouble);
        entradas.put("ValorDeEntrada", ValorEntradaConvertido);
        entradas.put("TipoDeEntrada", DadosEntrada);
        entradas.put("id", id);
        entradas.put("formPagamento", formaPagamento);
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes)
                .document("entradas")
                .collection("nova entrada").document(id);

        DocumentReference documentReferenceContasAreceber = db.collection(usuarioID).document(ano).collection(mes).document("entradas")
                .collection("ContasAreceber").document(id);

        documentReference.set(entradas).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        if (binding.checksSim.isChecked()){
            EnviarTotalContasReceber();

            documentReferenceContasAreceber.set(entradas).addOnCompleteListener(new OnCompleteListener<Void>() {
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
    public void PassarDataAutomatica(){
        String dia = new SimpleDateFormat("dd", new Locale("pt", "BR")).format(x);
        String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
        String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);

        binding.addData.setText(dia + "/");
        binding.addData2.setText(mes+ "/");
        binding.addData3.setText(ano);
        binding.dataContasApagar.setText(dia + "/" + mes +"/" + ano);

        Calendar calendar = Calendar.getInstance();
        String hora = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String minuto = String.valueOf(calendar.get(Calendar.MINUTE));
        String segundo = String.valueOf(calendar.get(Calendar.SECOND));
        binding.horaContasApagar.setText(hora +":" + minuto);
    }
    private void scheduleNotification(String text) {
        Calendar currentTime = Calendar.getInstance();
        int year = currentTime.get(Calendar.YEAR);
        int month = currentTime.get(Calendar.MONTH);
        int dayOfMonth = currentTime.get(Calendar.DAY_OF_MONTH);
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        final int chosenYear = year;
                        final int chosenMonth = monthOfYear;
                        final int chosenDay = dayOfMonth;

                        TimePickerDialog timePickerDialog = new TimePickerDialog(NovaEntrada.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        // Configurar o horário e a data para a notificação
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.set(chosenYear, chosenMonth, chosenDay, hourOfDay, minute, 0);

                                        // Criar uma intenção para o BroadcastReceiver
                                        Intent intent = new Intent(getApplicationContext(), AlarmReceber.class);
                                        intent.putExtra("notification_text_r", text); // Passar o corpo da notificação como um extra
                                        int uniqueId = (int) System.currentTimeMillis(); // ID único para o PendingIntent


                                        int flags;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
                                        } else {
                                            flags = PendingIntent.FLAG_UPDATE_CURRENT;
                                        }

                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), uniqueId, intent, flags);

                                        // Configura o alarme para disparar no horário escolhido
                                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                        if (alarmManager != null) {
                                            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                                            Toast.makeText(NovaEntrada.this, "Notificação agendada com sucesso", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(NovaEntrada.this, "Falha ao agendar a notificação", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }
                                }, hour, minute, true);

                        timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                finish();
                            }
                        });



                        // Mostrar o TimePickerDialog após selecionar a data
                        timePickerDialog.show();
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });

        // Mostrar o DatePickerDialog
        datePickerDialog.show();
    }
    public void OuvinteRadioGroup(){
        binding.radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId é o ID do RadioButton selecionado no RadioGroup
                switch (checkedId) {
                    case R.id.checksSim:

                        break;
                    case R.id.checksNao:
                        // Lógica para quando o radioButton2 é selecionado

                        break;
                    // Adicione casos para outros RadioButtons, se necessário
                }
            }
        });
    }




}



