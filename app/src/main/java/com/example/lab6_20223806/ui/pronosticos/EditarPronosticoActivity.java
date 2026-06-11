package com.example.lab6_20223806.ui.pronosticos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20223806.R;
import com.example.lab6_20223806.databinding.ActivityEditarPronosticoBinding;
import com.example.lab6_20223806.model.EstadoPronostico;
import com.example.lab6_20223806.model.Pronostico;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditarPronosticoActivity extends AppCompatActivity {

    private ActivityEditarPronosticoBinding binding;
    private FirebaseFirestore db;
    private String pronosticoId;
    private Date fechaSeleccionada;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditarPronosticoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.editar_pronostico));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        pronosticoId = getIntent().getStringExtra("pronosticoId");

        configurarDropdowns();
        configurarFechaPicker();
        cargarPronostico();
        binding.btnGuardar.setOnClickListener(v -> guardar());
    }

    private void configurarDropdowns() {
        String[] selecciones = getResources().getStringArray(R.array.selecciones_mundial);
        ArrayAdapter<String> adapterSel = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, selecciones);
        binding.actvSeleccionA.setAdapter(adapterSel);
        binding.actvSeleccionB.setAdapter(adapterSel);

        String[] estados = getResources().getStringArray(R.array.estados_pronostico);
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, estados);
        binding.actvEstado.setAdapter(adapterEstado);
    }

    private void configurarFechaPicker() {
        binding.etFecha.setOnClickListener(v -> mostrarDatePicker());
        binding.tilFecha.setEndIconOnClickListener(v -> mostrarDatePicker());
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (fechaSeleccionada != null) cal.setTime(fechaSeleccionada);
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            fechaSeleccionada = cal.getTime();
            binding.etFecha.setText(sdf.format(fechaSeleccionada));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void cargarPronostico() {
        db.collection("pronosticos").document(pronosticoId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { finish(); return; }
                    Pronostico p = doc.toObject(Pronostico.class);
                    if (p == null) { finish(); return; }

                    binding.actvSeleccionA.setText(p.getSeleccionA(), false);
                    binding.actvSeleccionB.setText(p.getSeleccionB(), false);
                    if (p.getFechaPartido() != null) {
                        fechaSeleccionada = p.getFechaPartido();
                        binding.etFecha.setText(sdf.format(fechaSeleccionada));
                    }
                    binding.etGolesA.setText(String.valueOf(p.getGolesA()));
                    binding.etGolesB.setText(String.valueOf(p.getGolesB()));
                    binding.actvEstado.setText(EstadoPronostico.displayName(p.getEstado()), false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void guardar() {
        String selA = binding.actvSeleccionA.getText().toString().trim();
        String selB = binding.actvSeleccionB.getText().toString().trim();
        String golesAStr = binding.etGolesA.getText().toString().trim();
        String golesBStr = binding.etGolesB.getText().toString().trim();
        String estadoDisplay = binding.actvEstado.getText().toString().trim();

        if (selA.isEmpty()) { binding.tilSeleccionA.setError("Selecciona una selección"); return; }
        if (selB.isEmpty()) { binding.tilSeleccionB.setError("Selecciona una selección"); return; }
        if (selA.equalsIgnoreCase(selB)) {
            binding.tilSeleccionB.setError("Las selecciones no pueden ser iguales");
            return;
        }
        if (fechaSeleccionada == null) { binding.tilFecha.setError("Selecciona la fecha"); return; }
        if (golesAStr.isEmpty()) { binding.tilGolesA.setError("Ingresa los goles"); return; }
        if (golesBStr.isEmpty()) { binding.tilGolesB.setError("Ingresa los goles"); return; }

        binding.tilSeleccionA.setError(null);
        binding.tilSeleccionB.setError(null);
        binding.tilFecha.setError(null);
        binding.tilGolesA.setError(null);
        binding.tilGolesB.setError(null);

        long golesA, golesB;
        try {
            golesA = Long.parseLong(golesAStr);
            golesB = Long.parseLong(golesBStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Goles inválidos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir display name a constante
        String estadoFinal;
        switch (estadoDisplay) {
            case "Acertado": estadoFinal = EstadoPronostico.ACERTADO; break;
            case "Fallado":  estadoFinal = EstadoPronostico.FALLADO; break;
            default:         estadoFinal = EstadoPronostico.PENDIENTE; break;
        }

        DocumentReference ref = db.collection("pronosticos").document(pronosticoId);

        // Verificar que aún es PENDIENTE antes de actualizar
        ref.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) { finish(); return; }
            String estadoActual = doc.getString("estado");
            if (!EstadoPronostico.PENDIENTE.equals(estadoActual)) {
                Toast.makeText(this, "Este pronóstico ya fue cerrado", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("seleccionA", selA);
            data.put("seleccionB", selB);
            data.put("fechaPartido", fechaSeleccionada);
            data.put("golesA", golesA);
            data.put("golesB", golesB);
            data.put("estado", estadoFinal);

            ref.update(data)
                    .addOnSuccessListener(unused -> {
                        Snackbar.make(binding.getRoot(), "Pronóstico actualizado correctamente", Snackbar.LENGTH_SHORT).show();
                        binding.getRoot().postDelayed(this::finish, 1500);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
