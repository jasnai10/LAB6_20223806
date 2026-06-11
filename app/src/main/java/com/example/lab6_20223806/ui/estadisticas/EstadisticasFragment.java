package com.example.lab6_20223806.ui.estadisticas;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lab6_20223806.databinding.FragmentEstadisticasBinding;
import com.example.lab6_20223806.model.EstadoPronostico;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class EstadisticasFragment extends Fragment {

    private FragmentEstadisticasBinding binding;
    private FirebaseFirestore db;
    private String uid;
    private ListenerRegistration listenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEstadisticasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        configurarGrafico();
    }

    @Override
    public void onStart() {
        super.onStart();
        escucharEstadisticas();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    private void configurarGrafico() {
        binding.pieChart.getDescription().setEnabled(false);
        // Pie sólido, sin agujero central
        binding.pieChart.setDrawHoleEnabled(false);
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.setDrawEntryLabels(true);
        binding.pieChart.setEntryLabelColor(Color.WHITE);
        binding.pieChart.setEntryLabelTextSize(13f);
        binding.pieChart.setExtraOffsets(8f, 8f, 8f, 8f);

        // Leyenda debajo del gráfico
        Legend legend = binding.pieChart.getLegend();
        legend.setEnabled(true);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(13f);
        legend.setTextColor(Color.DKGRAY);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    private void escucharEstadisticas() {
        listenerRegistration = db.collection("pronosticos")
                .whereEqualTo("userId", uid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null || binding == null) return;

                    int total = snapshots.size();
                    int acertados = 0, fallados = 0, pendientes = 0;

                    for (var doc : snapshots.getDocuments()) {
                        String estado = doc.getString("estado");
                        if (EstadoPronostico.ACERTADO.equals(estado)) acertados++;
                        else if (EstadoPronostico.FALLADO.equals(estado)) fallados++;
                        else pendientes++;
                    }

                    actualizarUI(total, acertados, fallados, pendientes);
                });
    }

    private void actualizarUI(int total, int acertados, int fallados, int pendientes) {
        binding.tvTotal.setText(String.valueOf(total));
        binding.tvAcertados.setText(String.valueOf(acertados));
        binding.tvFallados.setText(String.valueOf(fallados));
        binding.tvPendientes.setText(String.valueOf(pendientes));

        // Construir entradas y colores en paralelo para garantizar correspondencia
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (acertados > 0) {
            entries.add(new PieEntry(acertados, "Acertados"));
            colors.add(Color.parseColor("#4CAF50"));
        }
        if (fallados > 0) {
            entries.add(new PieEntry(fallados, "Fallados"));
            colors.add(Color.parseColor("#F44336"));
        }
        if (pendientes > 0) {
            entries.add(new PieEntry(pendientes, "Pendientes"));
            colors.add(Color.parseColor("#FFC107"));
        }

        if (entries.isEmpty()) {
            binding.pieChart.clear();
            binding.pieChart.setNoDataText("Sin pronósticos aún");
            binding.pieChart.setNoDataTextColor(Color.GRAY);
            binding.pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(6f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(13f);
        dataSet.setValueLinePart1Length(0.4f);
        dataSet.setValueLinePart2Length(0.4f);
        // Mostrar porcentaje con símbolo %, e.g. "66.7%"
        dataSet.setValueFormatter(new PercentFormatter(binding.pieChart));

        PieData pieData = new PieData(dataSet);
        binding.pieChart.setData(pieData);
        binding.pieChart.animateY(600);
        binding.pieChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
