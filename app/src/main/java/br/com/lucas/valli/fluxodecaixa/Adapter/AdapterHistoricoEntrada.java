package br.com.lucas.valli.fluxodecaixa.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.lucas.valli.fluxodecaixa.Model.HistoricoEntrada;
import br.com.lucas.valli.fluxodecaixa.R;


public class AdapterHistoricoEntrada extends RecyclerView.Adapter<AdapterHistoricoEntrada.HistoricoViewHolder> {

    private Context context;
    private List<HistoricoEntrada> historicoEntradaList;



    public AdapterHistoricoEntrada(Context context, List<HistoricoEntrada> historicoEntradaList) {
        this.context = context;
        this.historicoEntradaList = historicoEntradaList;
    }

    @NonNull
    @Override
    public HistoricoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View HistoricoList;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        HistoricoList = layoutInflater.inflate(R.layout.item_historico_entrada,parent,false);
        return new HistoricoViewHolder(HistoricoList);
    }

    @Override
    public int getItemCount() {
        return historicoEntradaList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterHistoricoEntrada.HistoricoViewHolder holder, int position) {

        holder.ValorTipoEntradaItem.setText(historicoEntradaList.get(position).getTipoDeEntrada());
        holder.ValorEntradaItem.setText(historicoEntradaList.get(position).getValorDeEntrada());
        holder.DataItemList.setText(historicoEntradaList.get(position).getDataDeEntrada());


    }
    public class HistoricoViewHolder extends RecyclerView.ViewHolder{
        private TextView DataItemList, ValorTipoEntradaItem, ValorEntradaItem, formPagamento;


        public HistoricoViewHolder(@NonNull View itemView) {
            super(itemView);
            DataItemList = itemView.findViewById(R.id.item_valorDataEntrada);
            ValorTipoEntradaItem = itemView.findViewById(R.id.txt_valorTipoEntrada);
            ValorEntradaItem = itemView.findViewById(R.id.txt_valorEntrada);


        }
    }
}
