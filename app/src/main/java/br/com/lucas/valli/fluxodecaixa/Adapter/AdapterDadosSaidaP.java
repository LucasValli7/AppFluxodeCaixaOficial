package br.com.lucas.valli.fluxodecaixa.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.lucas.valli.fluxodecaixa.Model.DadosSaidaP;
import br.com.lucas.valli.fluxodecaixa.R;

public class AdapterDadosSaidaP extends RecyclerView.Adapter<AdapterDadosSaidaP.DadosViewHolder> {

    private List<DadosSaidaP> dadosSaidaListP;
    private Context context;


    public AdapterDadosSaidaP(Context context, List<DadosSaidaP> dadosSaidaListP){
        this.context = context;
        this.dadosSaidaListP = dadosSaidaListP;

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
        holder.TipoDeSaida.setText(dadosSaidaListP.get(position).getTipoDeSaida());
        holder.ValorDeSaida.setText(dadosSaidaListP.get(position).getValorDeSaida());
        holder.dataDeSaida.setText(dadosSaidaListP.get(position).getDataDeSaida());


    }

    @Override
    public int getItemCount() {
        return dadosSaidaListP.size();
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
