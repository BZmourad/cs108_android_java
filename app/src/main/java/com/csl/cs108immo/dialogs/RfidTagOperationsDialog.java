package com.csl.cs108immo.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.csl.cs108immo.AccessTask1;
import com.csl.cs108immo.MainActivity;
import com.csl.cs108immo.R;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReaderChipData;

public class RfidTagOperationsDialog extends DialogFragment {
    private String tagEpc;
    private TextView textViewEpcValue;
    private EditText editTextNewEpc;
    private EditText editTextAccessPassword;
    private Button buttonReadEpc, buttonReadTid, buttonReadUser, buttonWriteEpc;
    
    private Handler mHandler = new Handler();
    private AccessTask1 accessTask;
    private boolean operationRead = false;
    private String accessResult = "";
    
    public static RfidTagOperationsDialog newInstance(String epc) {
        RfidTagOperationsDialog dialog = new RfidTagOperationsDialog();
        Bundle args = new Bundle();
        args.putString("tag_epc", epc);
        dialog.setArguments(args);
        return dialog;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tagEpc = getArguments().getString("tag_epc", "");
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rfid_tag_operations, null);
        dialog.setContentView(view);
        
        initViews(view);
        setupListeners();
        
        return dialog;
    }
    
    private void initViews(View view) {
        textViewEpcValue = view.findViewById(R.id.textViewEpcValue);
        editTextNewEpc = view.findViewById(R.id.editTextNewEpc);
        editTextAccessPassword = view.findViewById(R.id.editTextAccessPassword);
        buttonReadEpc = view.findViewById(R.id.buttonReadEpc);
        buttonReadTid = view.findViewById(R.id.buttonReadTid);
        buttonReadUser = view.findViewById(R.id.buttonReadUser);
        buttonWriteEpc = view.findViewById(R.id.buttonWriteEpc);
        Button buttonClose = view.findViewById(R.id.buttonClose);
        
        textViewEpcValue.setText("EPC: " + tagEpc);
        editTextAccessPassword.setText("00000000");
        
        buttonClose.setOnClickListener(v -> dismiss());
    }
    
    private void setupListeners() {
        buttonReadEpc.setOnClickListener(v -> {
            operationRead = true;
            performReadOperation(1); // Bank 1 = EPC
        });
        
        buttonReadTid.setOnClickListener(v -> {
            operationRead = true;
            performReadOperation(2); // Bank 2 = TID
        });
        
        buttonReadUser.setOnClickListener(v -> {
            operationRead = true;
            performReadOperation(3); // Bank 3 = User
        });
        
        buttonWriteEpc.setOnClickListener(v -> {
            String newEpc = editTextNewEpc.getText().toString().trim();
            if (newEpc.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter new EPC", Toast.LENGTH_SHORT).show();
                return;
            }
            operationRead = false;
            performWriteEpc(newEpc);
        });
    }
    
    private void performReadOperation(int bank) {
        if (MainActivity.csLibrary4A.isBleConnected() == false) {
            Toast.makeText(requireContext(), R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        
        String password = editTextAccessPassword.getText().toString();
        int accSize = 2; // Default read size in words
        int accOffset = 0;
        
        if (bank == 1) {
            accOffset = 2; // Skip PC bits for EPC
            accSize = 6; // Read up to 96 bits of EPC
        }
        
        accessTask = new AccessTask1(
            null, false,
            bank, accOffset, accSize, accSize,
            "", tagEpc, 0, 32,
            password, 300,
            RfidReaderChipData.HostCommands.CMD_18K6CREAD,
            null
        );
        
        accessTask.execute();
        
        // Wait for result
        mHandler.postDelayed(() -> {
            if (accessTask != null && accessTask.isResultReady()) {
                accessResult = accessTask.getResult();
                if (accessResult != null) {
                    showResult("Read Result: " + accessResult);
                } else {
                    showResult("Read failed");
                }
            }
        }, 1000);
    }
    
    private void performWriteEpc(String newEpc) {
        if (MainActivity.csLibrary4A.isBleConnected() == false) {
            Toast.makeText(requireContext(), R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        
        String password = editTextAccessPassword.getText().toString();
        
        // Write EPC at bank 1, offset 2 (after PC)
        accessTask = new AccessTask1(
            null, false,
            1, 2, newEpc.length() / 4, newEpc.length() / 4,
            newEpc, tagEpc, 0, 32,
            password, 300,
            RfidReaderChipData.HostCommands.CMD_18K6CWRITE,
            null
        );
        
        accessTask.execute();
        
        mHandler.postDelayed(() -> {
            if (accessTask != null && accessTask.isResultReady()) {
                accessResult = accessTask.getResult();
                if (accessResult != null) {
                    showResult("Write successful");
                    tagEpc = newEpc;
                    textViewEpcValue.setText("EPC: " + tagEpc);
                } else {
                    showResult("Write failed");
                }
            }
        }, 1000);
    }
    
    private void showResult(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onDestroy() {
        if (accessTask != null) {
            accessTask.cancel(true);
        }
        super.onDestroy();
    }
}