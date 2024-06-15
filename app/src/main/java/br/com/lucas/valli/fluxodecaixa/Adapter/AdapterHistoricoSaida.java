package br.com.lucas.valli.fluxodecaixa.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.lucas.valli.fluxodecaixa.Model.HistoricoSaida;
import br.com.lucas.valli.fluxodecaixa.R;

public class AdapterHistoricoSaida extends RecyclerView.Adapter<AdapterHistoricoSaida.HistoricoSaidasViewHolder> {

    private Context context;
    private List<HistoricoSaida> historicoSaidaList;

    public AdapterHistoricoSaida(Context context, List<HistoricoSaida> historicoSaidaList) {
        this.context = context;
        this.historicoSaidaList = historicoSaidaList;
    }

    @NonNull
    @Override
    public HistoricoSaidasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemListSaida;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        itemListSaida = layoutInflater.inflate(R.layout.item_historico_saida,parent,false);
        return new HistoricoSaidasViewHolder(itemListSaida);
    }

    @Override
    public int getItemCount() {
        return historicoSaidaList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull HistoricoSaidasViewHolder holder, int position) {
        holder.dataItemSaida.setText(historicoSaidaList.get(position).getDataDeSaida());
        holder.tipoDeSaida.setText(historicoSaidaList.get(position).getTipoDeSaida());
        holder.valorSaida.setText(historicoSaidaList.get(position).getValorDeSaida());



    }

    public class HistoricoSaidasViewHolder extends RecyclerView.ViewHolder{

        private TextView dataItemSaida, tipoDeSaida, valorSaida, formPagamento ;


        public HistoricoSaidasViewHolder(@NonNull View itemView) {
            super(itemView);

            dataItemSaida = itemView.findViewById(R.id.item_valorDataSaida);
            tipoDeSaida = itemView.findViewById(R.id.txt_valorTipoSaida);
            valorSaida = itemView.findViewById(R.id.txt_valorSaida);

        }
    }

}
