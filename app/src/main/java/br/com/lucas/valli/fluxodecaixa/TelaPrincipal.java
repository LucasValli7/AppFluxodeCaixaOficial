package br.com.lucas.valli.fluxodecaixa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import br.com.lucas.valli.fluxodecaixa.Adapter.ViewPagerAdapter;
import br.com.lucas.valli.fluxodecaixa.FragmentActivity.FragmentContasAPagar;
import br.com.lucas.valli.fluxodecaixa.FragmentActivity.FragmentContasAReceber;
import br.com.lucas.valli.fluxodecaixa.FragmentActivity.FragmentResumeAnual;
import br.com.lucas.valli.fluxodecaixa.FragmentActivity.FragmentResumeDiario;
import br.com.lucas.valli.fluxodecaixa.FragmentActivity.FragmentResumeMensal;
import br.com.lucas.valli.fluxodecaixa.databinding.ActivityTelaPrincipalBinding;

public class TelaPrincipal extends AppCompatActivity {

    private ActivityTelaPrincipalBinding binding;
    private String usuarioID;
    private Locale ptBr = new Locale("pt", "BR");
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Date x = new Date();
    private String mes = new SimpleDateFormat("MM", new Locale("pt", "BR")).format(x);
    private String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);

    private Double vazio = Double.parseDouble("0.00");
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");
    DrawerLayout drawerLayout;
    ImageView menu, perfil, nav_drawerFoto;
    LinearLayout linearHome, linearEntrada, linearSaida,
            linearContasApagar, linearContasAreceber, linearHorasExtras, linearListaDeCompras, linearHistorico, linearSobre, linearLogout;
    TextView meuPerfil, nomeUsuario;

    private ViewPager viewPagerResumo, viewPagerCp;
    private TabLayout tabLayoutResumo, tabLayoutCp;
    private ViewPagerAdapter viewPagerAdapter;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onStart() {
        super.onStart();
        checkConnection();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityTelaPrincipalBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        IniciarComponentes();
        ButtonsDrawerLayout();
        AtivarPagerViewResumo();
        AtivarPagerViewCp();
        TelaPrincipal.verificarEAbrirConfiguracoesDeNotificacoes(this);

    }



    public static void verificarEAbrirConfiguracoesDeNotificacoes(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        if (!notificationManager.areNotificationsEnabled()) {
            abrirConfiguracoesDeNotificacoes(context);
        }
    }

    private static void abrirConfiguracoesDeNotificacoes(Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("As notificações estão desativadas para este aplicativo. Ative as notificações nas configurações do dispositivo para receber atualizações importantes.")
                .setPositiveButton("Abrir Configurações", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                        context.startActivity(intent);

                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();



    }

    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            Log.d("NETCONEX", "SEM INTERNET");
            binding.progressBar.setVisibility(View.VISIBLE);
            return false;
        }else {
            /**/
            RecuperarDadosUsuario();
            RecuperarTotalSaidasResumo();
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
    public void RecuperarDadosUsuario(){
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();


        DocumentReference documentReference = db.collection(usuarioID).document("usuario");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null) {
                        nomeUsuario.setText(documentSnapshot.getString("nome"));
                        Glide.with(getApplicationContext()).load(documentSnapshot.getString("foto")).into(nav_drawerFoto);
                        Glide.with(getApplicationContext()).load(documentSnapshot.getString("foto")).into(perfil);

                    } else {
                        nav_drawerFoto.setImageResource(R.drawable.ic_perfil_icon);

                    }
                }else {

                }
            }
        });

    }
    public void RecuperarTotalSaidasResumo(){


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //document reference total Geral
        DocumentReference documentReferenceAnualSaidas = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("saidas").collection("TotalSaidaAnual")
                .document("Total");

        //document reference total Entradas
        DocumentReference documentReferenceAnualEntradas = db.collection(usuarioID).document(ano).collection("ResumoAnual").document("entradas").collection("TotalEntradaAnual")
                .document("Total");

        documentReferenceAnualSaidas.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskSaidas) {
                documentReferenceAnualEntradas.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> taskEntradas) {
                        if (taskEntradas.isSuccessful() && taskSaidas.isSuccessful()){
                            DocumentSnapshot documentSnapshotAnualSaidas = taskSaidas.getResult();
                            DocumentSnapshot documentSnapshotEntradasAnual= taskEntradas.getResult();

                            // se existir, permita que recupere dados do BD
                            if (!documentSnapshotEntradasAnual.exists() && !documentSnapshotAnualSaidas.exists()) {
                                String valorConvertido = NumberFormat.getCurrencyInstance(ptBr).format(vazio);
                                binding.txtValorLiquido.setText(valorConvertido);

                            } else if (!documentSnapshotEntradasAnual.exists() && documentSnapshotAnualSaidas.exists()) {
                                Double SomaSaida = Double.parseDouble(documentSnapshotAnualSaidas.getString("ResultadoTotalSaidaAnual"));
                                Double op = vazio - SomaSaida;
                                binding.txtValorLiquido.setTextColor(getResources().getColor(R.color.red));
                                String valorConvertido = NumberFormat.getCurrencyInstance(ptBr).format(op);
                                binding.txtValorLiquido.setText(valorConvertido);

                            } else if (documentSnapshotEntradasAnual.exists() && !documentSnapshotAnualSaidas.exists()) {
                                Double SomaEntrada = Double.parseDouble(documentSnapshotEntradasAnual.getString("ResultadoTotalEntradaAnual"));
                                String valorCv = NumberFormat.getCurrencyInstance(ptBr).format(SomaEntrada);
                                binding.txtValorLiquido.setText(valorCv);

                            } else if (documentSnapshotEntradasAnual.exists() && documentSnapshotAnualSaidas.exists()){
                                Double SomaEntrada = Double.parseDouble(documentSnapshotEntradasAnual.getString("ResultadoTotalEntradaAnual"));
                                Double SomaSaida = Double.parseDouble(documentSnapshotAnualSaidas.getString("ResultadoTotalSaidaAnual"));

                                // operação com os totais
                                if (SomaEntrada < SomaSaida){
                                    Double subtracao = SomaEntrada - SomaSaida;
                                    String SomaConvertida = NumberFormat.getCurrencyInstance(ptBr).format(subtracao);
                                    binding.txtValorLiquido.setText(String.valueOf(SomaConvertida));
                                    binding.txtValorLiquido.setTextColor(getResources().getColor(R.color.red));
                                }else {
                                    Double subtracao = SomaEntrada - SomaSaida;
                                    String SomaConvertida = NumberFormat.getCurrencyInstance(ptBr).format(subtracao);
                                    binding.txtValorLiquido.setText(String.valueOf(SomaConvertida));
                                    binding.txtValorLiquido.setTextColor(getResources().getColor(R.color.green));

                                }


                            }
                        } }
                });
            }
        });

    }
    public void AtivarPagerViewCp(){
        tabLayoutCp = findViewById(R.id.tabLayout2);
        viewPagerCp = findViewById(R.id.ViewPager2);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.AddFragment(new FragmentContasAPagar(), "Contas a Pagar");
        viewPagerAdapter.AddFragment(new FragmentContasAReceber(), "Contas a Receber");


        viewPagerCp.setAdapter(viewPagerAdapter);
        tabLayoutCp.setupWithViewPager(viewPagerCp);
        int paginaInicial = 0;
        viewPagerCp.setCurrentItem(paginaInicial);

    }
    public void AtivarPagerViewResumo(){
        tabLayoutResumo = findViewById(R.id.tabLayout);
        viewPagerResumo = findViewById(R.id.ViewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.AddFragment(new FragmentResumeDiario(), "Diário");
        viewPagerAdapter.AddFragment(new FragmentResumeMensal(), "Mensal");
        viewPagerAdapter.AddFragment(new FragmentResumeAnual(), "Anual");


        viewPagerResumo.setAdapter(viewPagerAdapter);
        tabLayoutResumo.setupWithViewPager(viewPagerResumo);
        int paginaInicial = 1;
        viewPagerResumo.setCurrentItem(paginaInicial);

    }
    public void IniciarComponentes (){
        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);
        perfil = findViewById(R.id.PerfilUsuario);
        nav_drawerFoto = findViewById(R.id.nav_drawerFoto);
        linearHome = findViewById(R.id.LinearHome);
        linearEntrada = findViewById(R.id.LinearEntrada);
        linearSaida = findViewById(R.id.LinearSaida);
        linearContasApagar = findViewById(R.id.LinearContasApagar);
        linearContasAreceber = findViewById(R.id.LinearContasAreceber);
        linearHorasExtras = findViewById(R.id.linearHorasExtras);
        linearListaDeCompras = findViewById(R.id.linear_ListaDeCompras);
        linearHistorico = findViewById(R.id.LinearHistorico);
        linearSobre = findViewById(R.id.LinearSobre);
        linearLogout = findViewById(R.id.LinearLogout);
        meuPerfil = findViewById(R.id.meuPerfil);
        nomeUsuario = findViewById(R.id.nomeUsuario);
    }
    public void ButtonsDrawerLayout() {
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer(drawerLayout);
            }
        });
        meuPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelaPrincipal.this, PerfilUsuario.class);
                startActivity(intent);
            }
        });
        linearHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer(drawerLayout);
            }
        });
        linearEntrada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TelaPrincipal.this, NovaEntrada.class);
                startActivity(intent);
            }
        });
        linearSaida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TelaPrincipal.this, NovaSaida.class);
                startActivity(intent);
            }
        });
        linearContasApagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelaPrincipal.this, ContasAPagar.class);
                startActivity(intent);
            }
        });
        linearContasAreceber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelaPrincipal.this, ContasAReceber.class);
                startActivity(intent);
            }
        });
        linearHorasExtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackbar = Snackbar.make(v, "Estará disponível na próxima atualização", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                snackbar.show();
            }
        });

        linearListaDeCompras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackbar = Snackbar.make(v, "Estará disponível na próxima atualização", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                snackbar.show();

            }
        });
        linearHistorico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelaPrincipal.this, PerfilHistoricos.class);
                startActivity(intent);
                finish();
            }
        });
        linearSobre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelaPrincipal.this, TelaSobre.class);
                startActivity(intent);

            }
        });
        linearLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();

            }
        });

        perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelaPrincipal.this, PerfilUsuario.class);
                startActivity(intent);
            }
        });

    }
    public static void openDrawer(DrawerLayout drawerLayout){
        drawerLayout.openDrawer(GravityCompat.START);
    }
    public static void closeDrawer(DrawerLayout drawerLayout){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
    public static void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer(binding.drawerLayout);
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

        InterstitialAd.load(this,String.valueOf("ca-app-pub-3940256099942544/1033173712"), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        ShowIntesticial();
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
            mInterstitialAd.show(TelaPrincipal.this);
        } else {
            Log.d("ADSTESTE", "The interstitial ad wasn't ready yet.");
        }
    }
}