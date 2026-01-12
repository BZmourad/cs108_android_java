package com.csl.cs108ademoapp.apps;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.csl.cs108ademoapp.R;
import android.widget.TextView;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class LayoutBarActivity extends AppCompatActivity {
    protected BottomAppBar bottomAppBar;
    protected Toolbar toolbar;
    protected ImageView btnLogout, ledInternet, ledServer, ledRfid;
    protected ProgressDialog progressDialog;
    protected Handler connectionHandler = new Handler();
    protected Runnable connectionChecker;
    protected static final String SERVER_IP = "192.168.100.50";
    protected static final int SERVER_PORT = 80; // Changez selon votre configuration
    protected static final int CHECK_INTERVAL = 5000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bottomAppBar = findViewById(R.id.bottom_app_bar);
        btnLogout = findViewById(R.id.btn_logout);
        ledInternet = findViewById(R.id.led_internet);
        ledServer = findViewById(R.id.led_server);
        ledRfid = findViewById(R.id.led_rfid);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnLogout.setOnClickListener(v -> {
            SharedPrefManager.getInstance(this).logout();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });

        updateConnectionStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate both menus
        getMenuInflater().inflate(R.menu.menu_home, menu);
        getMenuInflater().inflate(R.menu.menu_connection, menu);
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
        // Vérifier la connexion Internet
        boolean isOnline = SharedPrefManager.getInstance(this).isOnlineMode();
        runOnUiThread(() -> {
            ledInternet.setImageResource(isOnline ? R.drawable.led_green : R.drawable.led_red);
        });

        // Vérifier la connexion au serveur dans un thread séparé
        new Thread(() -> {
            boolean isServerReachable = isServerOnline();
            runOnUiThread(() -> {
                ledServer.setImageResource(isServerReachable ? R.drawable.led_green : R.drawable.led_red);
            });
        }).start();

        boolean isRfidConnected = isRfidConnect();
        runOnUiThread(() -> {
            ledRfid.setImageResource(isRfidConnected ? R.drawable.led_green : R.drawable.led_red);
        });
    }

    public boolean isServerOnline() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), 2000);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isRfidConnect() {
        return false;
    }
}