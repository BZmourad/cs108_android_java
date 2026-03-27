package com.csl.cs108ademoapp.apps;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.csl.cs108ademoapp.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class LayoutBarActivity extends AppCompatActivity {
    protected BottomAppBar bottomAppBar;
    protected Toolbar toolbar;
    protected ImageView btnLogout, ledInternet, ledServer, ledRfid;
    protected TextView tvUserName; // Add this for username display
    protected ProgressDialog progressDialog;
    protected Handler connectionHandler = new Handler();
    protected Runnable connectionChecker;
    protected static final int CHECK_INTERVAL = 5000;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure to set content view
        bottomAppBar = findViewById(R.id.bottom_app_bar);
        btnLogout = findViewById(R.id.btn_logout);
        ledInternet = findViewById(R.id.led_internet);
        ledServer = findViewById(R.id.led_server);
        ledRfid = findViewById(R.id.led_rfid);
        
        // Find the username TextView in the layout
        tvUserName = findViewById(R.id.tv_user_name);
        
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Setup the bottom app bar with username
        setupBottomAppBar();
        
        btnLogout.setOnClickListener(v -> {
            SharedPrefManager.getInstance(this).logout();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
        
        updateConnectionStatus();
    }
    
    private void setupBottomAppBar() {
        // Add username next to logout button
        /*if (tvUserName != null) {
            String userName = SharedPrefManager.getInstance(this).getUserName();
            if (userName != null && !userName.isEmpty()) {
                tvUserName.setText(userName);
                tvUserName.setVisibility(View.VISIBLE);
            } else {
                tvUserName.setVisibility(View.GONE);
            }
        }*/
        String userName = SharedPrefManager.getInstance(this).getUsername();
        tvUserName.setText(userName);
        tvUserName.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate both menus
        //getMenuInflater().inflate(R.menu.menu_home, menu);
        //getMenuInflater().inflate(R.menu.menu_connection, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startConnectionMonitoring();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopConnectionMonitoring();
    }
    
    public void showProgressDialog(Context context, String message) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = new ProgressDialog(context, R.style.ProgressDialogTheme);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setContentView(R.layout.custom_progress_dialog);
        TextView tvMessage = progressDialog.findViewById(R.id.tvProgressMessage);
        if (tvMessage != null) {
            tvMessage.setText(message);
        }
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void showLoading(String message) {
        showProgressDialog(this, message);
    }

    public void hideLoading() {
        hideProgressDialog();
    }

    public void startConnectionMonitoring() {
        connectionChecker = new Runnable() {
            @Override
            public void run() {
                updateConnectionStatus();
                connectionHandler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        connectionHandler.post(connectionChecker);
    }

    public void stopConnectionMonitoring() {
        connectionHandler.removeCallbacks(connectionChecker);
    }

    public void updateConnectionStatus() {
        if (ledInternet == null || ledServer == null || ledRfid == null) {
            return;
        }
        
        // Check Internet connection
        boolean isOnline = SharedPrefManager.getInstance(this).isOnlineMode();
        runOnUiThread(() -> {
            ledInternet.setImageResource(isOnline ? R.drawable.led_green : R.drawable.led_red);
        });

        // Check server connection in separate thread
        new Thread(() -> {
            boolean isServerReachable = isServerOnline();
            runOnUiThread(() -> {
                ledServer.setImageResource(isServerReachable ? R.drawable.led_green : R.drawable.led_red);
                // Disable server LED if offline or server not reachable
                if (!isOnline || !isServerReachable) {
                    ledServer.setImageResource(R.drawable.led_red);
                }
            });
        }).start();

        boolean isRfidConnected = isRfidConnect();
        runOnUiThread(() -> {
            ledRfid.setImageResource(isRfidConnected ? R.drawable.led_green : R.drawable.led_red);
        });
    }

    public boolean isServerOnline() {
        String serverUrl = SharedPrefManager.getInstance(this).getServerUrl();
        HttpURLConnection connection = null;
        
        try {
            URL url = new URL(serverUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000); // 3 seconds timeout
            connection.setReadTimeout(3000);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 400);
        } catch (IOException e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isRfidConnect() {
        // Implement your RFID connection check logic here
        return false;
    }
}