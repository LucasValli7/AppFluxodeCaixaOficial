package br.com.lucas.valli.fluxodecaixa.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.lucas.valli.fluxodecaixa.Model.DadosSaidaD;
import br.com.lucas.valli.fluxodecaixa.R;

public class AdapterDadosSaidaD extends RecyclerView.Adapter<AdapterDadosSaidaD.DadosViewHolder> {

    private List<DadosSaidaD> dadosSaidaListD;
    private Context context;



    public AdapterDadosSaidaD(Context context, List<DadosSaidaD> dadosSaidaListD){
        this.context = context;
        this.dadosSaidaListD = dadosSaidaListD;

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
        holder.TipoDeSaida.setText(dadosSaidaListD.get(position).getTipoDeSaida());
        holder.ValorDeSaida.setText(dadosSaidaListD.get(position).getValorDeSaida());
        holder.dataDeSaida.setText(dadosSaidaListD.get(position).getDataDeSaida());
    }

    @Override
    public int getItemCount() {
        return dadosSaidaListD.size();
    }

    public class DadosViewHolder extends RecyclerView.ViewHolder{

        private TextView TipoDeSaida, ValorDeSaida,dataDeSaida, formPagamento;

        public DadosViewHolder(@NonNull View itemView) {
            super(itemView);
            TipoDeSaida = itemView.findViewById(R.id.txt_valorTipoSaida);
            ValorDeSaida = itemView.findViewById(R.id.txt_valorSaida);
            dataDeSaida = itemView.findViewById(R.id.item_valorDataSaida);


        }
    }
}
