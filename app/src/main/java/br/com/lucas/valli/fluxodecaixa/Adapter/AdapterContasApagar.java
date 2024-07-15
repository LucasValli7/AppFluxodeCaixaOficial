package br.com.lucas.valli.fluxodecaixa.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.lucas.valli.fluxodecaixa.Atividades.ContasAPagar;
import br.com.lucas.valli.fluxodecaixa.Model.ContasApagar;
import br.com.lucas.valli.fluxodecaixa.R;

public class AdapterContasApagar extends RecyclerView.Adapter<AdapterContasApagar.DadosViewHolder> {

    private Context context;
    private List<ContasApagar> contasApagarList;

    private FirebaseFirestore db;
    private String usuarioID;
    private Date x = new Date();
    private String mes = new SimpleDateFormat("MMMM", new Locale("pt", "BR")).format(x);
    private String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);
    private ContasAPagar contasAPagar;


    public AdapterContasApagar(Context context, List<ContasApagar> contasApagarList) {
        this.context = context;
        this.contasApagarList = contasApagarList;
    }


    @NonNull
    @Override
    public DadosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View ItemLista;
        LayoutInflater LayoutInflater = android.view.LayoutInflater.from(context);
        ItemLista = LayoutInflater.inflate(R.layout.dados_item_contas_a_pagar, parent,false);
        return new DadosViewHolder(ItemLista);

    }

    @Override
    public void onBindViewHolder(@NonNull DadosViewHolder holder, int position) {
        holder.dataItemSaida.setText(contasApagarList.get(position).getDataDeSaida());
        holder.tipoDeSaida.setText(contasApagarList.get(position).getTipoDeSaida());
        holder.valorDeSaida.setText(contasApagarList.get(position).getValorDeSaida());
        holder.dataVencimento.setText(contasApagarList.get(position).getDataVencimento());

    }

    @Override
    public int getItemCount() {
        return contasApagarList.size();
    }



    public class DadosViewHolder extends RecyclerView.ViewHolder{

        private TextView dataItemSaida, tipoDeSaida, valorDeSaida, formPagamento, dataVencimento;



        public DadosViewHolder(@NonNull View itemView) {
            super(itemView);
            dataItemSaida = itemView.findViewById(R.id.item_valorDataSaida);
            tipoDeSaida = itemView.findViewById(R.id.txt_valorTipoSaida);
            valorDeSaida = itemView.findViewById(R.id.txt_valorSaida);
            dataVencimento = itemView.findViewById(R.id.txt_dataVencimento);


        }
    }
}
