package com.example.lab6_20223806.ui.pronosticos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20223806.R;
import com.example.lab6_20223806.databinding.ActivityRegistrarPronosticoBinding;
import com.example.lab6_20223806.model.EstadoPronostico;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegistrarPronosticoActivity extends AppCompatActivity {

    private ActivityRegistrarPronosticoBinding binding;
    private FirebaseFirestore db;
    private Date fechaSeleccionada;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrarPronosticoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.registrar_pronostico));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();

        configurarDropdowns();
        configurarFechaPicker();
        binding.btnRegistrar.setOnClickListener(v -> registrar());
    }

    private void configurarDropdowns() {
        String[] selecciones = getResources().getStringArray(R.array.selecciones_mundial);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, selecciones);
        binding.actvSeleccionA.setAdapter(adapter);
        binding.actvSeleccionB.setAdapter(adapter);
    }

    private void configurarFechaPicker() {
        binding.etFecha.setOnClickListener(v -> mostrarDatePicker());
        binding.tilFecha.setEndIconOnClickListener(v -> mostrarDatePicker());
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            fechaSeleccionada = cal.getTime();
            binding.etFecha.setText(sdf.format(fechaSeleccionada));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void registrar() {
        String selA = binding.actvSeleccionA.getText().toString().trim();
        String selB = binding.actvSeleccionB.getText().toString().trim();
        String golesAStr = binding.etGolesA.getText().toString().trim();
        String golesBStr = binding.etGolesB.getText().toString().trim();

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

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("userId", uid);
        data.put("seleccionA", selA);
        data.put("seleccionB", selB);
        data.put("fechaPartido", fechaSeleccionada);
        data.put("golesA", golesA);
        data.put("golesB", golesB);
        data.put("estado", EstadoPronostico.PENDIENTE);
        data.put("fechaCreacion", FieldValue.serverTimestamp());

        db.collection("pronosticos").add(data)
                .addOnSuccessListener(ref -> {
                    Snackbar.make(binding.getRoot(), "Pronóstico registrado correctamente", Snackbar.LENGTH_SHORT).show();
                    binding.getRoot().postDelayed(this::finish, 1500);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
