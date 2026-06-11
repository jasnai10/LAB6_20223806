package com.example.lab6_20223806.ui.pronosticos;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20223806.databinding.ItemPronosticoBinding;
import com.example.lab6_20223806.model.EstadoPronostico;
import com.example.lab6_20223806.model.Pronostico;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PronosticoAdapter extends RecyclerView.Adapter<PronosticoAdapter.ViewHolder> {

    public interface OnItemActionListener {
        void onEditar(Pronostico pronostico);
        void onEliminar(Pronostico pronostico);
    }

    private final List<Pronostico> items;
    private final OnItemActionListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public PronosticoAdapter(List<Pronostico> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPronosticoBinding b = ItemPronosticoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPronosticoBinding b;

        ViewHolder(ItemPronosticoBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Pronostico p) {
            b.tvSelecciones.setText(p.getSeleccionA() + " vs " + p.getSeleccionB());
            b.tvFecha.setText(p.getFechaPartido() != null ? sdf.format(p.getFechaPartido()) : "");
            b.tvResultado.setText(p.getGolesA() + " - " + p.getGolesB());

            String estado = p.getEstado() != null ? p.getEstado() : EstadoPronostico.PENDIENTE;
            b.tvEstado.setText(EstadoPronostico.displayName(estado));
            aplicarColorEstado(estado);

            boolean isPendiente = EstadoPronostico.PENDIENTE.equals(estado);
            b.layoutBotones.setVisibility(isPendiente ? View.VISIBLE : View.GONE);

            if (isPendiente) {
                b.btnEditar.setOnClickListener(v -> listener.onEditar(p));
                b.btnEliminar.setOnClickListener(v -> listener.onEliminar(p));
            }
        }

        private void aplicarColorEstado(String estado) {
            switch (estado) {
                case EstadoPronostico.ACERTADO:
                    b.tvEstado.getBackground().setTint(Color.parseColor("#4CAF50"));
                    b.tvEstado.setTextColor(Color.WHITE);
                    break;
                case EstadoPronostico.FALLADO:
                    b.tvEstado.getBackground().setTint(Color.parseColor("#F44336"));
                    b.tvEstado.setTextColor(Color.WHITE);
                    break;
                default:
                    b.tvEstado.getBackground().setTint(Color.parseColor("#FFC107"));
                    b.tvEstado.setTextColor(Color.BLACK);
                    break;
            }
        }
    }
}
