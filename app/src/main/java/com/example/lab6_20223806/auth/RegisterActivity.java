package com.example.lab6_20223806.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20223806.MainActivity;
import com.example.lab6_20223806.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnRegistrarse.setOnClickListener(v -> registrar());
        binding.tvIniciarSesion.setOnClickListener(v -> finish());
    }

    private void registrar() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirm = binding.etConfirmPassword.getText().toString().trim();

        if (email.isEmpty()) {
            binding.tilEmail.setError("Ingresa tu correo");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Correo inválido");
            return;
        }
        if (password.length() < 6) {
            binding.tilPassword.setError("Mínimo 6 caracteres");
            return;
        }
        if (!password.equals(confirm)) {
            binding.tilConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "createUserWithEmail:failure", e);
                    Toast.makeText(this, "Error al registrarse: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
