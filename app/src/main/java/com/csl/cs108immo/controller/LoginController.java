package com.csl.cs108immo.controller;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.csl.cs108immo.MainActivity;
import com.csl.cs108immo.app.db.DatabaseHelper;
import com.csl.cs108immo.app.db.models.Utilisateur;
import com.csl.cs108immo.apps.SharedPrefManager;

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

public class LoginController {
    private static final String TAG = "LoginController";
    
    private Context context;
    private DatabaseHelper dbHelper;
    private SharedPrefManager sharedPrefManager;
    private LoginCallback callback;

    public interface LoginCallback {
        void onLoginSuccess();
        void onLoginError(String message);
        void onLoadingStarted();
        void onLoadingFinished();
    }

    public LoginController(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.sharedPrefManager = SharedPrefManager.getInstance(context);
    }

    public void setCallback(LoginCallback callback) {
        this.callback = callback;
    }

    public void login(String username, String password) {
        // Vérification des champs vides
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            if (callback != null) {
                callback.onLoginError("Please enter username and password");
            }
            return;
        }

        username = username.trim();
        password = password.trim();
        
        final boolean isOnline = sharedPrefManager.getAppMode().equals("online");

        if (callback != null) {
            callback.onLoadingStarted();
        }

        if (isOnline) {
            performOnlineLogin(username, password);
        } else {
            performOfflineLogin(username, password);
        }
    }

    private void performOfflineLogin(String username, String password) {
        try {
            // Mode hors ligne - vérification dans la base de données locale
            if (dbHelper.checkUser(username, password)) {
                Utilisateur user = dbHelper.getUserDetails(username);
                sharedPrefManager.userLogin(
                        username,
                        false,
                        null,
                        user != null ? user.CodeUtil : "",
                        user != null ? user.NomPrenomUtil : ""
                );
                
                if (callback != null) {
                    callback.onLoadingFinished();
                    callback.onLoginSuccess();
                }
            } else {
                if (callback != null) {
                    callback.onLoadingFinished();
                    callback.onLoginError("Invalid credentials or user not found");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Offline login error: " + e.getMessage());
            if (callback != null) {
                callback.onLoadingFinished();
                callback.onLoginError("Error during offline login: " + e.getMessage());
            }
        }
    }

    private void performOnlineLogin(String username, String password) {
        new OnlineLoginTask(username, password).execute();
    }

    private class OnlineLoginTask extends AsyncTask<Void, Void, LoginResult> {
        private String username;
        private String password;

        public OnlineLoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected LoginResult doInBackground(Void... params) {
            LoginResult result = new LoginResult();
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                // Récupérer l'URL du serveur depuis la configuration
                String baseUrl = sharedPrefManager.getServerUrl();
                // Construire l'URL complète
                String urlString = baseUrl + "api-verif-login.awp?log=" +
                        username + "&motPass=" + password;
                
                Log.d(TAG, "Attempting to call URL: " + urlString);

                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                Log.d(TAG, "Connecting to server...");

                result.responseCode = connection.getResponseCode();
                Log.d(TAG, "Response code: " + result.responseCode);

                if (result.responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    result.response = stringBuilder.toString();
                    Log.d(TAG, "Response: " + result.response);
                } else {
                    InputStream errorStream = connection.getErrorStream();
                    if (errorStream != null) {
                        reader = new BufferedReader(new InputStreamReader(errorStream));
                        StringBuilder errorBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            errorBuilder.append(line);
                        }
                        result.errorMessage = errorBuilder.toString();
                        Log.e(TAG, "Error response: " + result.errorMessage);
                    }
                }
            } catch (MalformedURLException e) {
                result.exception = e;
                Log.e(TAG, "Malformed URL: " + e.getMessage());
            } catch (SocketTimeoutException e) {
                result.exception = e;
                result.errorMessage = "Connection timeout";
                Log.e(TAG, "Connection timeout: " + e.getMessage());
            } catch (IOException e) {
                result.exception = e;
                result.errorMessage = "Network error";
                Log.e(TAG, "IO Exception: " + e.getMessage());
            } catch (Exception e) {
                result.exception = e;
                result.errorMessage = "Unexpected error";
                Log.e(TAG, "Unexpected error: " + e.getMessage());
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error closing connection: " + e.getMessage());
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(LoginResult result) {
            if (callback != null) {
                callback.onLoadingFinished();
            }
            
            // Gestion des erreurs
            if (result.exception != null) {
                String errorDetail;
                if (result.exception instanceof SocketTimeoutException) {
                    errorDetail = "Connection timeout. Please check your network and try again.";
                } else if (result.exception instanceof IOException) {
                    errorDetail = "Network error. Please check your internet connection.";
                } else {
                    errorDetail = "Error: " + result.errorMessage;
                }
                if (callback != null) {
                    callback.onLoginError(errorDetail);
                }
                Log.e(TAG, "Error details", result.exception);
                return;
            }
            
            // Vérification du code HTTP
            if (result.responseCode != HttpURLConnection.HTTP_OK) {
                String httpError = "Server returned HTTP code: " + result.responseCode;
                if (result.errorMessage != null && !result.errorMessage.isEmpty()) {
                    httpError += "\n" + result.errorMessage;
                }
                if (callback != null) {
                    callback.onLoginError(httpError);
                }
                return;
            }
            
            // Vérification de la réponse vide
            if (result.response == null || result.response.isEmpty()) {
                if (callback != null) {
                    callback.onLoginError("Empty response from server");
                }
                return;
            }
            
            try {
                Log.d(TAG, "Parsing response: " + result.response);
                JSONObject jsonObject = new JSONObject(result.response);
                
                if (!jsonObject.has("sRes")) {
                    if (callback != null) {
                        callback.onLoginError("Invalid server response format");
                    }
                    return;
                }
                
                String token = jsonObject.getString("sRes");
                Log.d(TAG, "Received token: " + token);

                if (token.equals("error")) {
                    if (callback != null) {
                        callback.onLoginError("Invalid credentials");
                    }
                } else {
                    // Login réussi
                    processSuccessfulLogin(jsonObject, token, username, password);
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON parsing error: " + e.getMessage());
                Log.e(TAG, "Response that failed parsing: " + result.response);
                if (callback != null) {
                    callback.onLoginError("Error parsing server response");
                }
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error: " + e.getMessage());
                if (callback != null) {
                    callback.onLoginError("Unexpected error: " + e.getMessage());
                }
            }
        }
    }

    private void processSuccessfulLogin(JSONObject jsonObject, String token, 
                                       String username, String password) {
        try {
            // Essayer de récupérer les détails de l'utilisateur depuis la base de données
            Utilisateur user = null;
            try {
                user = dbHelper.getUserDetails(username);
            } catch (Exception e) {
                Log.e(TAG, "Error getting user details: " + e.getMessage());
            }
            
            // Si l'utilisateur n'existe pas dans la base, le créer depuis la réponse API
            if (user == null) {
                user = new Utilisateur();
                user.login = username;
                user.MotPasse = password;
                
                // Essayer d'obtenir des informations supplémentaires depuis la réponse API
                if (jsonObject.has("CodeUtil")) {
                    user.CodeUtil = jsonObject.getString("CodeUtil");
                }
                if (jsonObject.has("NomPrenomUtil")) {
                    user.NomPrenomUtil = jsonObject.getString("NomPrenomUtil");
                }
                
                // Ajouter l'utilisateur à la base de données
                try {
                    dbHelper.addUser(user);
                    Log.d(TAG, "User added to database successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Error adding user: " + e.getMessage());
                }
                
                // Récupérer à nouveau l'utilisateur pour avoir toutes les données
                user = dbHelper.getUserDetails(username);
            }
            
            // Sauvegarder la session utilisateur
            sharedPrefManager.userLogin(
                    username,
                    true,
                    token,
                    user != null ? user.CodeUtil : "",
                    user != null ? user.NomPrenomUtil : ""
            );
            
            Log.d(TAG, "Login successful");
            
            if (callback != null) {
                callback.onLoginSuccess();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error during login process: " + e.getMessage());
            if (callback != null) {
                callback.onLoginError("Error saving user data: " + e.getMessage());
            }
        }
    }

    // Classe interne pour stocker le résultat du login
    private static class LoginResult {
        String response = "";
        Exception exception = null;
        int responseCode = -1;
        String errorMessage = "";
    }
}