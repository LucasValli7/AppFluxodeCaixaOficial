package br.com.lucas.valli.fluxodecaixa.Atividades;

import static br.com.lucas.valli.fluxodecaixa.Classes.ConversorDeMoeda.formatPriceSave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import br.com.lucas.valli.fluxodecaixa.Adapter.ViewPagerAdapter;
import br.com.lucas.valli.fluxodecaixa.Classes.ConversorDeMoeda;
import br.com.lucas.valli.fluxodecaixa.FragmentActivity.FragmentContasAPagar;
import br.com.lucas.valli.fluxodecaixa.FragmentActivity.FragmentContasAReceber;
import br.com.lucas.valli.fluxodecaixa.FragmentActivity.FragmentResumeAnual;
import br.com.lucas.valli.fluxodecaixa.FragmentActivity.FragmentResumeDiario;
import br.com.lucas.valli.fluxodecaixa.FragmentActivity.FragmentResumeMensal;
import br.com.lucas.valli.fluxodecaixa.Model.DadosEntrada;
import br.com.lucas.valli.fluxodecaixa.R;
import br.com.lucas.valli.fluxodecaixa.RecyclerItemClickListener.RecyclerItemClickListener;
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

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
        binding.container2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Infla o layout personalizado
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_editar_resumo, null);
                EditText txt_valor = dialogView.findViewById(R.id.edit_valorEntrada);
                String valorAtual = binding.txtValorLiquido.getText().toString();
                txt_valor.setHint(valorAtual);
                // Adiciona o TextWatcher ao TextView
                if (txt_valor != null) {
                    txt_valor.addTextChangedListener(new ConversorDeMoeda(dialogView.findViewById(R.id.edit_valorEntrada)));
                } else {
                    Log.e("TAG", "TextView é nulo");
                }


                // Cria e mostra o AlertDialog
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TelaPrincipal.this);
                builder.setView(dialogView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                                if (networkInfo == null){
                                    Log.d("NETCONEX", "SEM INTERNET");
                                    binding.progressBar.setVisibility(View.VISIBLE);
                                    Toast.makeText(TelaPrincipal.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();

                                }else {

                                    DocumentReference documentReferenceResumoEntrada = db.collection(usuarioID).document("resumoCaixa").collection("ResumoDeCaixa").document("entradas").collection("total")
                                            .document("ResumoTotal");

                                    DocumentReference documentReferenceResumoSaida = db.collection(usuarioID).document("resumoCaixa").collection("ResumoDeCaixa").document("saidas").collection("total")
                                            .document("ResumoTotal");

                                    documentReferenceResumoEntrada.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> taskEntrada) {
                                            documentReferenceResumoSaida.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> taskSaida) {
                                                    DocumentSnapshot documentSnapshotEntrada = taskEntrada.getResult();
                                                    DocumentSnapshot documentSnapshotSaida = taskSaida.getResult();

                                                    String txt_valorString = String.valueOf(txt_valor.getText());
                                                    String str = formatPriceSave(txt_valorString);

                                                    String vazioString = String.valueOf(vazio);

                                                    if (documentSnapshotEntrada.contains("ResultadoTotal")) {
                                                        documentReferenceResumoEntrada.update("ResultadoTotal", str);
                                                        if (documentSnapshotSaida.contains("ResultadoTotal")) {
                                                            documentReferenceResumoSaida.update("ResultadoTotal", vazioString);
                                                        }
                                                    } else if (!documentSnapshotEntrada.contains("ResultadoTotal")) {
                                                        //document reference ResumoAnual
                                                        DocumentReference documentReference = db.collection(usuarioID).document("resumoCaixa").collection("ResumoDeCaixa").document("entradas").collection("total")
                                                                .document("ResumoTotal");

                                                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                DocumentSnapshot documentSnapshot= task.getResult();
                                                                Double ValorCv = Double.parseDouble(str);

                                                                if (documentSnapshot.contains("ResultadoTotal")){
                                                                    Double ValorDiario = Double.parseDouble(documentSnapshot.getString("ResultadoTotal"));
                                                                    Double SomaSaida = ValorDiario + ValorCv;
                                                                    String SomaSaidaCv = String.valueOf(SomaSaida);

                                                                    //HasMap total
                                                                    Map<String, Object> valorTotal = new HashMap<>();
                                                                    valorTotal.put("ResultadoTotal", SomaSaidaCv);

                                                                    documentReference.set(valorTotal);

                                                                }else {
                                                                    String ValorStringCv = String.valueOf(ValorCv);
                                                                    //HasMap total
                                                                    Map<String, Object> valorTotal = new HashMap<>();
                                                                    valorTotal.put("ResultadoTotal", ValorStringCv);

                                                                    documentReference.set(valorTotal);

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
                        })
                        .setNegativeButton("Cancelar", null);
                binding.progressBar.setVisibility(View.GONE);

                androidx.appcompat.app.AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    private void refreshData() {
        new Handler().postDelayed(() -> {
            checkConnection();
            binding.swipeRefreshLayout.setRefreshing(false);
        }, 1000); // Simula um delay de 1 segundos
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
        DocumentReference documentReferenceAnualSaidas = db.collection(usuarioID).document("resumoCaixa").collection("ResumoDeCaixa").document("saidas").collection("total")
                .document("ResumoTotal");

        //document reference total Entradas
        DocumentReference documentReferenceAnualEntradas = db.collection(usuarioID).document("resumoCaixa").collection("ResumoDeCaixa").document("entradas").collection("total")
                .document("ResumoTotal");

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
                                Double SomaSaida = Double.parseDouble(documentSnapshotAnualSaidas.getString("ResultadoTotal"));
                                Double op = vazio - SomaSaida;
                                binding.txtValorLiquido.setTextColor(getResources().getColor(R.color.red));
                                String valorConvertido = NumberFormat.getCurrencyInstance(ptBr).format(op);
                                binding.txtValorLiquido.setText(valorConvertido);

                            } else if (documentSnapshotEntradasAnual.exists() && !documentSnapshotAnualSaidas.exists()) {
                                Double SomaEntrada = Double.parseDouble(documentSnapshotEntradasAnual.getString("ResultadoTotal"));
                                String valorCv = NumberFormat.getCurrencyInstance(ptBr).format(SomaEntrada);
                                binding.txtValorLiquido.setText(valorCv);

                            } else if (documentSnapshotEntradasAnual.exists() && documentSnapshotAnualSaidas.exists()){
                                Double SomaEntrada = Double.parseDouble(documentSnapshotEntradasAnual.getString("ResultadoTotal"));
                                Double SomaSaida = Double.parseDouble(documentSnapshotAnualSaidas.getString("ResultadoTotal"));

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

}