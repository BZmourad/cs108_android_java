package com.csl.cs108immo.apps;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.csl.cs108immo.R;

public class ConfigActivity extends AppCompatActivity {
    
    private EditText etServerUrl;
    private RadioGroup rgMode;
    private RadioButton rbOnline, rbOffline;
    private Button btnSave;
    private SharedPrefManager sharedPrefManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        
        // Initialize SharedPrefManager
        sharedPrefManager = SharedPrefManager.getInstance(this);
        
        // Initialize views
        etServerUrl = findViewById(R.id.etServerUrl);
        rgMode = findViewById(R.id.rgMode);
        rbOnline = findViewById(R.id.rbOnline);
        rbOffline = findViewById(R.id.rbOffline);
        btnSave = findViewById(R.id.btnSave);
        
        // Load existing configuration
        loadConfiguration();
        
        // Set click listener for save button
        btnSave.setOnClickListener(v -> saveConfiguration());
    }
    
    private void loadConfiguration() {
        // Load server URL
        String serverUrl = sharedPrefManager.getServerUrl();
        etServerUrl.setText(serverUrl);
        
        // Load mode
        String appMode = sharedPrefManager.getAppMode();
        if (appMode.equals("online")) {
            rbOnline.setChecked(true);
        } else {
            rbOffline.setChecked(true);
        }
    }
    
    private void saveConfiguration() {
        String serverUrl = etServerUrl.getText().toString().trim();
        
        // Validate server URL
        if (serverUrl.isEmpty()) {
            Toast.makeText(this, "Please enter server URL", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Add trailing slash if not present
        if (!serverUrl.endsWith("/")) {
            serverUrl += "/";
        }
        
        // Get selected mode
        String appMode;
        if (rbOnline.isChecked()) {
            appMode = "online";
        } else {
            appMode = "offline";
        }
        
        // Save configuration
        sharedPrefManager.saveServerConfig(serverUrl, appMode);
        
        Toast.makeText(this, "Configuration saved successfully", Toast.LENGTH_SHORT).show();
        finish(); // Close the configuration window
    }
}