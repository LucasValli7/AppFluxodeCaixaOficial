package br.com.lucas.valli.fluxodecaixa.Atividades;

import static br.com.lucas.valli.fluxodecaixa.Classes.ConversorDeMoeda.formatPriceSave;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Random;
import java.util.UUID;

import br.com.lucas.valli.fluxodecaixa.Classes.AlarmReceber;
import br.com.lucas.valli.fluxodecaixa.Classes.ConversorDeMoeda;
import br.com.lucas.valli.fluxodecaixa.R;
import br.com.lucas.valli.fluxodecaixa.databinding.ActivityNovasContasReceberBinding;

public class NovasContasAreceber extends AppCompatActivity {
    private ActivityNovasContasReceberBinding binding;
    private String usuarioID;
    private Locale ptbr = new Locale("pt", "BR");
    private Double vazio = Double.parseDouble("0.00");
    private Date x = new Date();
    private String dia = new SimpleDateFormat("dd", new Locale("pt", "BR")).format(x);
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItem;
    private InterstitialAd mInterstitialAd;

    protected void onStart() {
        super.onStart();
        PassarDataAutomatica();
        BtnSalvar();
        Initialize();
        OuvinteRadioGroup();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNovasContasReceberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FormaPagamento();
        gerarIdMovimentacao();

        binding.date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCalendar();
            }
        });
        binding.horaContasApagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirRelogio();
            }
        });
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
    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            Log.d("NETCONEX", "SEM INTERNET");
            Toast.makeText(NovasContasAreceber.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();
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

            }else if (binding.checksSim.isChecked() || binding.checksNao.isChecked()){
                Toast.makeText(NovasContasAreceber.this,"Salvo com Sucesso",Toast.LENGTH_SHORT).show();
                EnviarDadosListaEntradaBD();
                EnviarTotalContasReceber();
                ShowIntesticial();
                binding.floatingActionButton.setEnabled(false);
                binding.progressBar.setVisibility(View.GONE);

                if (binding.checksSim.isChecked()) {
                    String titulo = binding.editNovaEntrada.getText().toString();
                    String valor = binding.editNovoValor.getText().toString();
                    AgendarNotificacao("Receber \" " + titulo + "\" no valor de " + "R$ " + valor);
                }
                finish();
            }else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
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

        InterstitialAd.load(this, String.valueOf("ca-app-pub-7099783455876849/3315422269"), adRequest,
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
            mInterstitialAd.show(NovasContasAreceber.this);
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
                Toast.makeText(NovasContasAreceber.this, Item+ " selecionado", Toast.LENGTH_SHORT).show();



            }
        });

    }
    public void EnviarTotalContasReceber(){
        String mes = binding.addData2.getText().toString();
        String ano = binding.addData3.getText().toString();


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //document reference total Contas À receber
        DocumentReference documentReferenceC = db.collection(usuarioID).document("ContasAreceber")
                .collection("TotalContasAReceber").document("Total");

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

                    db.collection(usuarioID).document("ContasAreceber")
                            .collection("TotalContasAReceber").document("Total").set(valorTotalC).addOnCompleteListener(new OnCompleteListener<Void>() {
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

                    db.collection(usuarioID).document("ContasAreceber")
                            .collection("TotalContasAReceber").document("Total").set(valorTotalC).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        String idMovimentacao = binding.idMotimentacao.getText().toString();
        String DadosEntrada = binding.editNovaEntrada.getText().toString();
        String dataVencimento = binding.dataContasApagar.getText().toString();

        String str = formatPriceSave(binding.editNovoValor.getText().toString());
        Double ValorEntrada = Double.parseDouble(str);
        String ValorEntradaConvertido = NumberFormat.getCurrencyInstance(ptbr).format(ValorEntrada);
        String ValorEntradaDouble = String.valueOf(ValorEntrada);
        String dataEntrada = dia + mes + ano;
        String formaPagament = binding.autoCompleteTextForm.getText().toString();
        String id = UUID.randomUUID().toString();


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> contasAreceber = new HashMap<>();
        contasAreceber.put("dataDeEntrada", dataEntrada);
        contasAreceber.put("ValorDeEntradaDouble", ValorEntradaDouble);
        contasAreceber.put("ValorDeEntrada", ValorEntradaConvertido);
        contasAreceber.put("TipoDeEntrada", DadosEntrada);
        contasAreceber.put("id", id);
        contasAreceber.put("dataVencimento", dataVencimento);
        contasAreceber.put("formPagamento", formaPagament);
        contasAreceber.put("idMovimentacao", idMovimentacao);

        DocumentReference documentReferenceContasAreceber = db.collection(usuarioID).document("ContasAreceber").collection("dados").document(id);


            documentReferenceContasAreceber.set(contasAreceber).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    public void gerarIdMovimentacao(){
        Random random = new Random();
        int min = 100000000;
        int max = 999999999;
        int randomAleatorio = random.nextInt((max - min +1 )) +min;

        String idAleatorioString = String.valueOf(randomAleatorio);
        binding.idMotimentacao.setText(idAleatorioString);
    }
    private void AgendarNotificacao(String text) {

        String date = binding.dataContasApagar.getText().toString();
        String time = binding.horaContasApagar.getText().toString();

// Parse da string para um objeto Calendar
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Formato de data inválido", Toast.LENGTH_SHORT).show();
            return;
        }


        // Parse da string de hora para definir a hora e o minuto
        String[] timeParts = time.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

// Configurar o alarme para disparar no horário especificado
        Intent intent = new Intent(getApplicationContext(), AlarmReceber.class);
        intent.putExtra("notification_text_r", text); // Passar o corpo da notificação como um extra


        int requestCode = Integer.parseInt(binding.idMotimentacao.getText().toString()); // ID único para o PendingIntent
        // int uniqueId = (int) System.currentTimeMillis();

        int flags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, flags);

// Configura o alarme para disparar no horário escolhido
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(NovasContasAreceber.this, "Notificação agendada com sucesso", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(NovasContasAreceber.this, "Falha ao agendar a notificação", Toast.LENGTH_SHORT).show();

        }

    }
    public void abrirCalendar() {
        Calendar currentTime = Calendar.getInstance();
        int year = currentTime.get(Calendar.YEAR);
        int month = currentTime.get(Calendar.MONTH);
        int dayOfMonth = currentTime.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Adicionando 1 ao monthOfYear pois os meses em Calendar começam em 0 (Janeiro é 0)
                        monthOfYear += 1;
                        String formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear, year);
                        binding.dataContasApagar.setText(formattedDate);
                    }
                }, year, month, dayOfMonth);

        // Exibindo a data atual no formato correto no TextView
        month += 1; // Adicionando 1 ao mês atual
        String formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month, year);
        binding.dataContasApagar.setText(formattedDate);

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
    public void abrirRelogio(){

        // Obtém a hora atual
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Abre o TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    // Define a hora selecionada no TextView
                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    binding.horaContasApagar.setText(time);
                }, hour, minute, true);

        timePickerDialog.show();

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
    public void OuvinteRadioGroup(){
        binding.radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId é o ID do RadioButton selecionado no RadioGroup
                switch (checkedId) {
                    case R.id.checksSim:
                        binding.date.setVisibility(View.VISIBLE);
                        break;
                    case R.id.checksNao:
                        binding.date.setVisibility(View.GONE);
                        break;
                    // Adicione casos para outros RadioButtons, se necessário
                }
            }
        });
    }
}