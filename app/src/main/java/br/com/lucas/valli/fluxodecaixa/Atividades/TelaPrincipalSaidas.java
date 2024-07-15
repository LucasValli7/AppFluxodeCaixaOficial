package br.com.lucas.valli.fluxodecaixa.Atividades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.lucas.valli.fluxodecaixa.Adapter.ViewPagerAdapter;
import br.com.lucas.valli.fluxodecaixa.FragmentActivity.FragmentDP;
import br.com.lucas.valli.fluxodecaixa.FragmentActivity.FragmentGE;
import br.com.lucas.valli.fluxodecaixa.FragmentActivity.FragmentPD;
import br.com.lucas.valli.fluxodecaixa.R;
import br.com.lucas.valli.fluxodecaixa.databinding.ActivityTelaPrincipalSaidasBinding;

public class TelaPrincipalSaidas extends AppCompatActivity {
    private ActivityTelaPrincipalSaidasBinding binding;
    private String usuarioID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Date x = new Date();
    private String mes = new SimpleDateFormat("MMMM", new Locale("pt", "BR")).format(x);
    private String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onStart() {
        super.onStart();
        AtivarPagerView();
        checkConnection();

    }
    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            Log.d("NETCONEX", "SEM INTERNET");
            Toast.makeText(TelaPrincipalSaidas.this, "Verifique sua conexão com a Internet", Toast.LENGTH_SHORT).show();
            return false;
        }else {

        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
            Log.d("NETCONEX", "WIFI");
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
            Log.d("NETCONEX", "DADOS");
        }
        return networkInfo.isConnected();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityTelaPrincipalSaidasBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        Toolbar toolbar = findViewById(R.id.tolbar);
        setSupportActionBar(toolbar);

        binding.AddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelaPrincipalSaidas.this, NovaSaida.class);
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
    public void AtivarPagerView(){
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.ViewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        viewPagerAdapter.AddFragment(new FragmentGE(),"Gastos essenciais");
        viewPagerAdapter.AddFragment(new FragmentPD(),"Pagamento de dívidas");
        viewPagerAdapter.AddFragment(new FragmentDP(),"desejos pessoais");


        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);


    }



}