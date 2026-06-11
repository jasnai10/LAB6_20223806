package com.example.lab6_20223806.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20223806.MainActivity;
import com.example.lab6_20223806.R;
import com.example.lab6_20223806.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Si ya hay sesión activa, ir directo a MainActivity
        if (mAuth.getCurrentUser() != null) {
            irAMain();
            return;
        }

        configurarGoogle();
        configurarListeners();
    }

    private void configurarGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void configurarListeners() {
        binding.btnLogin.setOnClickListener(v -> iniciarSesionEmail());
        binding.tvRegistrarse.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        binding.btnGoogle.setOnClickListener(v -> iniciarSesionGoogle());
        binding.btnGithub.setOnClickListener(v -> iniciarSesionGitHub());
    }

    private void iniciarSesionEmail() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            binding.tilEmail.setError("Ingresa tu correo");
            return;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError("Ingresa tu contraseña");
            return;
        }
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> irAMain())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "signInWithEmail:failure", e);
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                });
    }

    private void iniciarSesionGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> irAMain())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Google credential auth failed", e);
                    Toast.makeText(this, "Error al autenticar con Google", Toast.LENGTH_SHORT).show();
                });
    }

    private void iniciarSesionGitHub() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("github.com");
        provider.addCustomParameter("allow_signup", "true");

        mAuth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(authResult -> irAMain())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "GitHub sign in failed", e);
                    Toast.makeText(this, "Error al iniciar sesión con GitHub", Toast.LENGTH_SHORT).show();
                });
    }

    private void irAMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
