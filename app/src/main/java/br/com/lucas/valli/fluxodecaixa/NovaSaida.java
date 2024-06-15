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
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.tasks.OnSuccessListener;
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
import br.com.lucas.valli.fluxodecaixa.databinding.ActivityNovaSaidaBinding;

public class NovaSaida extends AppCompatActivity {
    private ActivityNovaSaidaBinding binding;
    private String usuarioID;
    private Locale ptbr = new Locale("pt", "BR");
    private Date x = new Date();
    private String mes2 = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
    private String ano2 = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);
    private String dia2 = new SimpleDateFormat("dd", new Locale("pt", "BR")).format(x);
    private String dataFormat = dia2 +"/" + mes2 +"/"+ ano2;
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> adapterItem;
    private InterstitialAd mInterstitialAd;


    @Override
    protected void onStart() {
        super.onStart();
        PassarDataAutomatica();
        BtnSalvar();
        OuvinteRadioGroup();
        Initialize();

    }
    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            Log.d("NETCONEX", "SEM INTERNET");
            binding.progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(NovaSaida.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            String novaSaida = binding.editNovaSaida.getText().toString();
            String novoValor = binding.editNovoValor.getText().toString();
            String categoria = binding.autoCompleteTextCategoria.getText().toString();
            String formPagamento = binding.autoCompleteTextForm.getText().toString();
            if (novaSaida.isEmpty() || novoValor.isEmpty() || categoria.isEmpty() || formPagamento.isEmpty()){
                Snackbar snackbar = Snackbar.make(getWindow().getDecorView(),"Preencha todos os campos",Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                snackbar.show();
            } else if (binding.checksSim.isChecked() || binding.checksNao.isChecked()) {
                Toast.makeText(NovaSaida.this, "Salvo com Sucesso", Toast.LENGTH_SHORT).show();
                EnviarDadosListaSaidaBD();
                EnviarTotalDiario();
                EnviarTotalMensal();
                EnviarTotalAnual();
                Condicao();
                ShowIntesticial();
                binding.floatingActionButton.setEnabled(false);
                binding.progressBar.setVisibility(View.GONE);

                if (binding.checksSim.isChecked()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NovaSaida.this);
                    builder.setTitle("Deseja adicionar um lembrete de pagamento?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Lógica para quando o radioButton1 é selecionado
                            String titulo = binding.editNovaSaida.getText().toString();
                            String valor = binding.editNovoValor.getText().toString();
                            scheduleNotification("Pagar " + titulo + " no valor de " + "R$ " + valor);
                        }
                    }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(NovaSaida.this, TelaPrincipal.class);
                            finish();
                        }
                    }).show();
                }else {
                    finish();
                }

        } else {

                Snackbar snackbar = Snackbar.make(getWindow().getDecorView(),"Preencha o campo Contas a Receber",Snackbar.LENGTH_INDEFINITE)
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
        binding = ActivityNovaSaidaBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        FormaPagamento();
        Categoria();

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
            mInterstitialAd.show(NovaSaida.this);
        } else {
            Log.d("ADSTESTE", "The interstitial ad wasn't ready yet.");
        }
    }
    public void FormaPagamento(){

        String [] formaPagamento = {"Boleto", "Depósito", "Transferência", "Pix", "Dinheiro", "Cartão de Crédito","Nenhuma"};

        autoCompleteTextView = findViewById(R.id.auto_complete_text_form);
        adapterItem = new ArrayAdapter<String>(this,R.layout.nova_saida_item, formaPagamento);
        autoCompleteTextView.setAdapter(adapterItem);


        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String Item = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(NovaSaida.this, Item+ " selecionado", Toast.LENGTH_SHORT).show();

            }
        });

    }
    public void Categoria(){
        String [] categoria = {"Gastos Essenciais", "Pagamento de Dívidas", "Desejos Pessoais"};

        autoCompleteTextView = findViewById(R.id.auto_complete_text_categoria);
        adapterItem = new ArrayAdapter<String>(this,R.layout.nova_saida_item, categoria);
        autoCompleteTextView.setAdapter(adapterItem);

        autoCompleteTextView.setInputType(InputType.TYPE_NULL);
        autoCompleteTextView.setFocusable(false);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String Item = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(NovaSaida.this, Item+ " selecionado", Toast.LENGTH_SHORT).show();

            }
        });



    }
    public void EnviarTotalDiario(){
        String mes = binding.addData2.getText().toString();
        String ano = binding.addData3.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReferenceTotalDiario = db.collection(usuarioID).document(ano).collection(mes2).document("ResumoDiario").collection("TotalSaidasDiario")
                .document(dia2);

        documentReferenceTotalDiario.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskDiario) {
                DocumentSnapshot documentSnapshotTotalDiario = taskDiario.getResult();
                String str = formatPriceSave(binding.editNovoValor.getText().toString());
                Double ValorCv = Double.parseDouble(str);

                if (documentSnapshotTotalDiario.contains("ResultadoDaSomaSaidaDiario")){
                    Double ValorDiario = Double.parseDouble(documentSnapshotTotalDiario.getString("ResultadoDaSomaSaidaDiario"));
                    Double SomaSaida = ValorDiario + ValorCv;
                    String SomaSaidaCv = String.valueOf(SomaSaida);

                    Map<String, Object> valorTotalDiairo = new HashMap<>();
                    valorTotalDiairo.put("ResultadoDaSomaSaidaDiario", SomaSaidaCv);

                    db.collection(usuarioID).document(ano).collection(mes2).document("ResumoDiario").collection("TotalSaidasDiario")
                            .document(dia2).set(valorTotalDiairo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                }
                            });

                }else {
                    String ValorStringCv = String.valueOf(ValorCv);
                    Map<String, Object> valorTotalDiairo = new HashMap<>();
                    valorTotalDiairo.put("ResultadoDaSomaSaidaDiario", ValorStringCv);

                    db.collection(usuarioID).document(ano).collection(mes2).document("ResumoDiario").collection("TotalSaidasDiario")
                            .document(dia2).set(valorTotalDiairo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                }
                            });

                }
            }
        });
    }
    public void EnviarTotalMensal(){
        String mes = binding.addData2.getText().toString();
        String ano = binding.addData3.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReferenceMensal = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de Saidas")
                .document("Total");

        documentReferenceMensal.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskGeral) {
                DocumentSnapshot documentSnapshotMensal = taskGeral.getResult();
                String str = formatPriceSave(binding.editNovoValor.getText().toString());
                Double ValorCv = Double.parseDouble(str);

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
    }
    public void EnviarTotalAnual(){
        String mes = binding.addData2.getText().toString();
        String ano = binding.addData3.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //document reference ResumoAnual
        DocumentReference documentReferenceAnual = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("saidas").collection("TotalSaidaAnual")
                .document("Total");

        documentReferenceAnual.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskAnual) {
                DocumentSnapshot documentSnapshotAnual = taskAnual.getResult();
                String str = formatPriceSave(binding.editNovoValor.getText().toString());
                Double ValorCv = Double.parseDouble(str);

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
    }
    public void EnviarTotalGastosE(){
        String mes = binding.addData2.getText().toString();
        String ano = binding.addData3.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //document reference total Essenciais
        DocumentReference documentReferenceE = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasE")
                .document("Total");

        documentReferenceE.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskE) {
                DocumentSnapshot documentSnapshotE = taskE.getResult();
                String str = formatPriceSave(binding.editNovoValor.getText().toString());
                Double valorCV = Double.parseDouble(str);

                if (documentSnapshotE.contains("ResultadoDaSomaSaidaE")){
                    Double ValorDiario = Double.parseDouble(documentSnapshotE.getString("ResultadoDaSomaSaidaE"));
                    Double SomaSaida = ValorDiario + valorCV;
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
                    String ValorCvString = String.valueOf(valorCV);
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
    }
    public void EnviarTotalGastosP(){
        String mes = binding.addData2.getText().toString();
        String ano = binding.addData3.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //document reference total Pagamento de Dívidas
        DocumentReference documentReferenceP = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasP")
                .document("Total");

        documentReferenceP.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskP) {
                DocumentSnapshot documentSnapshotP = taskP.getResult();
                String str = formatPriceSave(binding.editNovoValor.getText().toString());
                Double valorCV = Double.parseDouble(str);

                if (documentSnapshotP.contains("ResultadoDaSomaSaidaP")){
                    Double ValorP= Double.parseDouble(documentSnapshotP.getString("ResultadoDaSomaSaidaP"));
                    Double SomaSaida = ValorP + valorCV;
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
                    String ValorCvString = String.valueOf(valorCV);
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
    }
    public void EnviarTotalGastosD(){
        String mes = binding.addData2.getText().toString();
        String ano = binding.addData3.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //document reference total Desejos Pessoais
        DocumentReference documentReferenceD = db.collection(usuarioID).document(ano).collection(mes).document("saidas").collection("Total de SaidasD")
                .document("Total");

        documentReferenceD.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskD) {
                DocumentSnapshot documentSnapshotD = taskD.getResult();
                String str = formatPriceSave(binding.editNovoValor.getText().toString());
                Double valorCV = Double.parseDouble(str);

                if (documentSnapshotD.contains("ResultadoDaSomaSaidaD")){
                    Double ValorD= Double.parseDouble(documentSnapshotD.getString("ResultadoDaSomaSaidaD"));
                    Double SomaSaida = ValorD + valorCV;
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
                    String ValorCvString = String.valueOf(valorCV);
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
    public void EnviarTotalContasPagar(){
        String mes = binding.addData2.getText().toString();
        String ano = binding.addData3.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //document reference total Contas À pagar
        DocumentReference documentReferenceC = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                .collection("Total Contas A Pagar").document("Total");

        documentReferenceC.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskC) {
                DocumentSnapshot documentSnapshotC = taskC.getResult();
                String str = formatPriceSave(binding.editNovoValor.getText().toString());
                Double valorCV = Double.parseDouble(str);

                if (documentSnapshotC.contains("ResultadoDaSomaSaidaC")){
                    Double ValorC = Double.parseDouble(documentSnapshotC.getString("ResultadoDaSomaSaidaC"));
                    Double SomaSaida = ValorC + valorCV;
                    String SomaSaidaCv = String.valueOf(SomaSaida);

                    // HasMap total D
                    Map<String, Object> valorTotalC = new HashMap<>();
                    valorTotalC.put("ResultadoDaSomaSaidaC", SomaSaidaCv);

                    db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                            .collection("Total Contas A Pagar").document("Total").set(valorTotalC).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                    valorTotalC.put("ResultadoDaSomaSaidaC", ValorCvString);

                    db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                            .collection("Total Contas A Pagar").document("Total").set(valorTotalC).addOnCompleteListener(new OnCompleteListener<Void>() {
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
    public void Condicao(){

        String epd = binding.autoCompleteTextCategoria.getText().toString();
        if (binding.checksSim.isChecked()) {
            EnviarTotalContasPagar();
        }

        if (epd.equals("Gastos Essenciais")){
            EnviarTotalGastosE();
        } else if (epd.equals("Pagamento de Dívidas")) {
            EnviarTotalGastosP();
        } else if (epd.equals("Desejos Pessoais")) {
            EnviarTotalGastosD();
        }

    }
    public void EnviarDadosListaSaidaBD(){
        String dia = binding.addData.getText().toString();
        String mes = binding.addData2.getText().toString();
        String ano = binding.addData3.getText().toString();


        String DadosSaida = binding.editNovaSaida.getText().toString();

        String str = formatPriceSave(binding.editNovoValor.getText().toString());
        Double ValorSaida = Double.parseDouble(str);
        String ValorSaidaDouble = String.valueOf(ValorSaida); // valor convertido para string para recuperar em fragmento
        String ValorSaidaConvertido = NumberFormat.getCurrencyInstance(ptbr).format(ValorSaida);
        String id = UUID.randomUUID().toString();

        String dataSaida = dataFormat;
        String formaPagament = binding.autoCompleteTextForm.getText().toString();
        String formaPagamento = "Saída realizada por " + formaPagament;
        String categoria = binding.autoCompleteTextCategoria.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> saidas = new HashMap<>();
        saidas.put("TipoDeSaida", DadosSaida);
        saidas.put("ValorDeSaidaDouble", ValorSaidaDouble);
        saidas.put("ValorDeSaida", ValorSaidaConvertido);
        saidas.put("dataDeSaida", dataSaida);
        saidas.put("id", id);

        saidas.put("formPagamento", formaPagamento);
        saidas.put("categoria", categoria);
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReference = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                .collection("nova saida").document("categoria").collection(categoria).document(id);

        DocumentReference documentReferenceContasApagar = db.collection(usuarioID).document(ano).collection(mes).document("saidas")
                .collection("ContasApagar").document(id);


        documentReference.set(saidas).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        if (binding.checksSim.isChecked()){
            documentReferenceContasApagar.set(saidas).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        Date x = new Date();
        String dia = new SimpleDateFormat("dd", new Locale("pt", "BR")).format(x);
        String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
        String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);
        binding.addData.setText(dia + "/");
        binding.addData2.setText(mes+ "/");
        binding.addData3.setText(ano);
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

                        TimePickerDialog timePickerDialog = new TimePickerDialog(NovaSaida.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        // Configurar o horário e a data para a notificação
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.set(chosenYear, chosenMonth, chosenDay, hourOfDay, minute, 0);

                                        // Criar uma intenção para o BroadcastReceiver
                                        Intent intent = new Intent(getApplicationContext(), AlarmPagar.class);
                                        intent.putExtra("notification_text", text); // Passar o corpo da notificação como um extra
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
                                            Toast.makeText(NovaSaida.this, "Notificação agendada com sucesso", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(NovaSaida.this, "Falha ao agendar a notificação", Toast.LENGTH_SHORT).show();
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