package com.csl.cs108immo.apps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.csl.cs108immo.MainActivity;
import com.csl.cs108immo.R;
import com.csl.cs108immo.controller.LoginController;

public class LoginActivity extends AppCompatActivity implements LoginController.LoginCallback {
    private EditText etUsername, etPassword;
    private Button btnLogin, btnConfig;
    private ProgressDialog progressDialog;
    private SharedPrefManager sharedPrefManager;
    private LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialiser les préférences partagées
        sharedPrefManager = SharedPrefManager.getInstance(this);
        
        // Initialiser le contrôleur de login
        loginController = new LoginController(this);
        loginController.setCallback(this);

        // Vérifier si l'utilisateur est déjà connecté
        if (sharedPrefManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialiser les vues
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnConfig = findViewById(R.id.btnConfig);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        btnConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent configIntent = new Intent(LoginActivity.this, ConfigActivity.class);
                startActivity(configIntent);
            }
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // Déléguer la logique de login au contrôleur
        loginController.login(username, password);
    }

    @Override
    public void onLoginSuccess() {
        // Rediriger vers MainActivity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onLoginError(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLoadingStarted() {
        showLoading("Authenticating...");
    }

    @Override
    public void onLoadingFinished() {
        hideLoading();
    }

    private void showLoading(String message) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = new ProgressDialog(this, R.style.ProgressDialogTheme);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setContentView(R.layout.custom_progress_dialog);
        TextView tvMessage = progressDialog.findViewById(R.id.tvProgressMessage);
        if (tvMessage != null) {
            tvMessage.setText(message);
        }
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoading(); // Prévenir les fuites de mémoire
    }
}