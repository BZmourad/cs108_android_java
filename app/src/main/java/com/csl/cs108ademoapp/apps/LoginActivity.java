package com.csl.cs108ademoapp.apps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import com.csl.cs108ademoapp.db.models.User;
import com.csl.cs108ademoapp.apps.SharedPrefManager;
import com.csl.cs108ademoapp.db.DatabaseHelper;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.MainActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Switch switchMode;
    private Button btnLogin;
    private DatabaseHelper dbHelper;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Check if user is already logged in
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        switchMode = findViewById(R.id.switchMode);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        final boolean isOnline = switchMode.isChecked();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("Authenticating...");
        if (isOnline) {
            // Online mode - call API
            new LoginTask().execute(username, password);
        } else {
            // Offline mode - check local database
            if (dbHelper.checkUser(username, password)) {
                // Save user session
                User user = dbHelper.getUserDetails(username);
                SharedPrefManager.getInstance(LoginActivity.this).userLogin(
                        etUsername.getText().toString().trim(),
                        false,
                        null,
                        user != null ? user.getCode() : "",
                        user != null ? user.getName() : ""
                );
                hideLoading();
                // Proceed to dashboard
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                hideLoading();
                Toast.makeText(this, "Invalid credentials or user not found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        private Exception exception;
        private int responseCode = -1;
        private String errorMessage = "";

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            String response = "";
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                // Log the URL being called
                String urlString = "http://192.168.1.50/GESTION_INV_CNAM_WEB/FR/api-verif-login.awp?log=" +
                        username + "&motPass=" + password;
                Log.d("LoginTask", "Attempting to call URL: " + urlString);

                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000); // 15 seconds timeout
                connection.setReadTimeout(15000);

                // Log connection attempt
                Log.d("LoginTask", "Connecting to server...");

                responseCode = connection.getResponseCode();
                Log.d("LoginTask", "Response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    response = stringBuilder.toString();
                    Log.d("LoginTask", "Response: " + response);
                } else {
                    // Try to read error stream if available
                    InputStream errorStream = connection.getErrorStream();
                    if (errorStream != null) {
                        reader = new BufferedReader(new InputStreamReader(errorStream));
                        StringBuilder errorBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            errorBuilder.append(line);
                        }
                        errorMessage = errorBuilder.toString();
                        Log.e("LoginTask", "Error response: " + errorMessage);
                    }
                }
            } catch (MalformedURLException e) {
                exception = e;
                Log.e("LoginTask", "Malformed URL: " + e.getMessage());
            } catch (SocketTimeoutException e) {
                exception = e;
                errorMessage = "Connection timeout";
                Log.e("LoginTask", "Connection timeout: " + e.getMessage());
            } catch (IOException e) {
                exception = e;
                errorMessage = "Network error";
                Log.e("LoginTask", "IO Exception: " + e.getMessage());
            } catch (Exception e) {
                exception = e;
                errorMessage = "Unexpected error";
                Log.e("LoginTask", "Unexpected error: " + e.getMessage());
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                } catch (IOException e) {
                    Log.e("LoginTask", "Error closing connection: " + e.getMessage());
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            hideLoading();
            if (exception != null) {
                // Handle connection errors
                String errorDetail;
                if (exception instanceof SocketTimeoutException) {
                    errorDetail = "Connection timeout. Please check your network and try again.";
                } else if (exception instanceof IOException) {
                    errorDetail = "Network error. Please check your internet connection.";
                } else {
                    errorDetail = "Error: " + errorMessage;
                }

                Toast.makeText(LoginActivity.this, errorDetail, Toast.LENGTH_LONG).show();
                Log.e("LoginTask", "Error details", exception);
                return;
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                // Handle HTTP error codes
                String httpError = "Server returned HTTP code: " + responseCode;
                if (!errorMessage.isEmpty()) {
                    httpError += "\n" + errorMessage;
                }
                Toast.makeText(LoginActivity.this, httpError, Toast.LENGTH_LONG).show();
                return;
            }

            if (result == null || result.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Empty response from server", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Log.d("LoginTask", "Parsing response: " + result);
                JSONObject jsonObject = new JSONObject(result);

                if (!jsonObject.has("sRes")) {
                    Toast.makeText(LoginActivity.this, "Invalid server response format", Toast.LENGTH_SHORT).show();
                    return;
                }

                String token = jsonObject.getString("sRes");
                Log.d("LoginTask", "Received token: " + token);

                if (token.equals("error")) {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                } else {
                    // Login successful
                    String username = etUsername.getText().toString().trim();

                    DatabaseHelper dbHelper = new DatabaseHelper(LoginActivity.this);
                    User user = dbHelper.getUserDetails(etUsername.getText().toString().trim());

                    SharedPrefManager.getInstance(LoginActivity.this).userLogin(
                            etUsername.getText().toString().trim(),
                            true,
                            token,
                            user != null ? user.getCode() : "",
                            user != null ? user.getName() : ""
                    );

                    // Add user to local database (optional)
                    dbHelper.addUser(
                            username,
                            etPassword.getText().toString().trim()
                    );

                    // Proceed to dashboard
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            } catch (JSONException e) {
                Log.e("LoginTask", "JSON parsing error: " + e.getMessage());
                Log.e("LoginTask", "Response that failed parsing: " + result);
                Toast.makeText(LoginActivity.this, "Error parsing server response", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            hideLoading();
            Toast.makeText(LoginActivity.this, "Login task cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(String message) {
        /*if (progressDialog != null && progressDialog.isShowing()) {
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
        }*/
    }

    private void hideLoading() {
        /*if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }*/
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoading(); // Prevent memory leaks
    }
}
