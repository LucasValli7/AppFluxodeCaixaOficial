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

import br.com.lucas.valli.fluxodecaixa.ContasAPagar;
import br.com.lucas.valli.fluxodecaixa.Model.ContasApagar;
import br.com.lucas.valli.fluxodecaixa.Model.ContasAreceber;
import br.com.lucas.valli.fluxodecaixa.R;

public class AdapterContasAreceber extends RecyclerView.Adapter<AdapterContasAreceber.DadosViewHolder> {

    private Context context;
    private List<ContasAreceber> contasAreceberList;

    private FirebaseFirestore db;
    private String usuarioID;
    private Date x = new Date();
    private String mes = new SimpleDateFormat("MMMM", new Locale("pt", "BR")).format(x);
    private String ano = new SimpleDateFormat("yyyy", new Locale("pt", "BR")).format(x);
    private ContasAPagar contasAPagar;


    public AdapterContasAreceber(Context context, List<ContasAreceber> contasAreceberList) {
        this.context = context;
        this.contasAreceberList = contasAreceberList;
    }


    @NonNull
    @Override
    public DadosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View ItemLista;
        LayoutInflater LayoutInflater = android.view.LayoutInflater.from(context);
        ItemLista = LayoutInflater.inflate(R.layout.dados_item_contas_a_receber, parent,false);
        return new DadosViewHolder(ItemLista);

    }

    @Override
    public void onBindViewHolder(@NonNull DadosViewHolder holder, int position) {
        holder.dataItemSaida.setText(contasAreceberList.get(position).getDataDeEntrada());
        holder.tipoDeSaida.setText(contasAreceberList.get(position).getTipoDeEntrada());
        holder.valorDeSaida.setText(contasAreceberList.get(position).getValorDeEntrada());
        holder.dataVencimento.setText(contasAreceberList.get(position).getDataVencimento());

    }

    @Override
    public int getItemCount() {
        return contasAreceberList.size();
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
