package br.com.lucas.valli.fluxodecaixa.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.lucas.valli.fluxodecaixa.Model.DadosSaidaE;
import br.com.lucas.valli.fluxodecaixa.FragmentActivity.FragmentGE;
import br.com.lucas.valli.fluxodecaixa.R;



public class AdapterDadosSaidaE extends RecyclerView.Adapter<AdapterDadosSaidaE.DadosViewHolder> {



    private List<DadosSaidaE> dadosSaidaEListE;
    private Context context;

    private FirebaseFirestore db;
    private String usuarioID;

    private Date x = new Date();
    private String mes = new SimpleDateFormat("MMMM", new Locale("pt", "BR")).format(x);
    private String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);
    private FragmentGE fragmentGE;



    public AdapterDadosSaidaE(Context context, List<DadosSaidaE> dadosSaidaEList){
        this.context = context;
        this.dadosSaidaEListE = dadosSaidaEList;


    }


    @NonNull
    @Override
    public DadosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        itemLista = layoutInflater.inflate(R.layout.dados_item_saida_epd,parent,false);
        return new DadosViewHolder(itemLista);
    }


    @Override
    public void onBindViewHolder(@NonNull DadosViewHolder holder, int position) {

        holder.TipoDeSaida.setText(dadosSaidaEListE.get(position).getTipoDeSaida());
        holder.ValorDeSaida.setText(dadosSaidaEListE.get(position).getValorDeSaida());
        holder.dataDeSaida.setText(dadosSaidaEListE.get(position).getDataDeSaida());


    }




    @Override
    public int getItemCount() {
        return dadosSaidaEListE.size();
    }

    public class DadosViewHolder extends RecyclerView.ViewHolder{

        private TextView TipoDeSaida, ValorDeSaida, dataDeSaida, formPagamento;

        public DadosViewHolder(@NonNull View itemView) {
            super(itemView);
            TipoDeSaida = itemView.findViewById(R.id.txt_valorTipoSaida);
            ValorDeSaida = itemView.findViewById(R.id.txt_valorSaida);
            dataDeSaida = itemView.findViewById(R.id.item_valorDataSaida);

        }
    }
}
