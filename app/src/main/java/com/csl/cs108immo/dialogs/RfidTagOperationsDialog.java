package com.csl.cs108immo.dialogs;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.csl.cslibrary4a.RfidReaderChipData;

public class RfidTagOperationsDialog extends DialogFragment {
    private String tagEpc;
    private TextView textViewEpcValue;
    private EditText editTextNewEpc;
    private EditText editTextAccessPassword;
    private Button buttonReadEpc, buttonReadTid, buttonReadUser, buttonWriteEpc;
    private Button buttonHiddenAccess; // Button caché pour AccessTask
    
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private AccessTask1 accessTask;
    private boolean isOperationInProgress = false;
    
    // Runnable pour vérifier le résultat
    private Runnable resultCheckRunnable;
    private int checkCount = 0;
    private static final int MAX_CHECK_COUNT = 50; // 50 * 100ms = 5 secondes max
    
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
        buttonHiddenAccess = view.findViewById(R.id.buttonHiddenAccess);
        Button buttonClose = view.findViewById(R.id.buttonClose);
        
        textViewEpcValue.setText("EPC: " + tagEpc);
        editTextAccessPassword.setText("00000000");
        
        buttonClose.setOnClickListener(v -> dismiss());
    }
    
    private void setupListeners() {
        buttonReadEpc.setOnClickListener(v -> {
            if (!isOperationInProgress) {
                performReadOperation(1, 2, 6, "EPC");
            }
        });
        
        buttonReadTid.setOnClickListener(v -> {
            if (!isOperationInProgress) {
                performReadOperation(2, 0, 2, "TID");
            }
        });
        
        buttonReadUser.setOnClickListener(v -> {
            if (!isOperationInProgress) {
                performReadOperation(3, 0, 2, "User Memory");
            }
        });
        
        buttonWriteEpc.setOnClickListener(v -> {
            if (isOperationInProgress) {
                Toast.makeText(requireContext(), "Operation in progress...", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String newEpc = editTextNewEpc.getText().toString().trim();
            if (newEpc.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter new EPC", Toast.LENGTH_SHORT).show();
                return;
            }
            performWriteEpc(newEpc);
        });
    }
    
    private void performReadOperation(int bank, int offset, int size, String bankName) {
        if (MainActivity.csLibrary4A.isBleConnected() == false) {
            Toast.makeText(requireContext(), R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        
        isOperationInProgress = true;
        disableButtons(true);
        
        String password = editTextAccessPassword.getText().toString();
        if (password.isEmpty()) {
            password = "00000000";
        }
        
        // Réinitialiser le compteur
        checkCount = 0;
        
        // Mettre à jour le texte du button caché
        buttonHiddenAccess.setText("READ");
        buttonHiddenAccess.setEnabled(true);
        
        try {
            accessTask = new AccessTask1(
                buttonHiddenAccess,
                false,
                bank, offset, size, size,
                "", tagEpc, 0, 32,
                password, 300,
                RfidReaderChipData.HostCommands.CMD_18K6CREAD,
                null
            );
            
            accessTask.execute();
            
            // Démarrer la vérification du résultat
            startResultCheck(bankName, true);
        } catch (Exception e) {
            isOperationInProgress = false;
            disableButtons(false);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void performWriteEpc(String newEpc) {
        if (MainActivity.csLibrary4A.isBleConnected() == false) {
            Toast.makeText(requireContext(), R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        
        isOperationInProgress = true;
        disableButtons(true);
        
        String password = editTextAccessPassword.getText().toString();
        if (password.isEmpty()) {
            password = "00000000";
        }
        
        // Calculer la taille en mots (1 mot = 4 caractères hex)
        int sizeInWords = (newEpc.length() + 3) / 4;
        if (sizeInWords == 0) sizeInWords = 1;
        
        // Réinitialiser le compteur
        checkCount = 0;
        
        // Mettre à jour le texte du button caché
        buttonHiddenAccess.setText("WRITE");
        buttonHiddenAccess.setEnabled(true);
        
        try {
            accessTask = new AccessTask1(
                buttonHiddenAccess,
                false,
                1, 2, sizeInWords, sizeInWords,
                newEpc, tagEpc, 0, 32,
                password, 300,
                RfidReaderChipData.HostCommands.CMD_18K6CWRITE,
                null
            );
            
            accessTask.execute();
            
            // Démarrer la vérification du résultat
            startResultCheck("EPC", false);
        } catch (Exception e) {
            isOperationInProgress = false;
            disableButtons(false);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void startResultCheck(String target, boolean isRead) {
        // Annuler tout Runnable précédent
        if (resultCheckRunnable != null) {
            mHandler.removeCallbacks(resultCheckRunnable);
        }
        
        resultCheckRunnable = new Runnable() {
            @Override
            public void run() {
                checkCount++;
                
                boolean isComplete = false;
                String result = null;
                
                if (accessTask != null) {
                    // Vérifier si la tâche est terminée
                    AsyncTask.Status status = accessTask.getStatus();
                    
                    if (status == AsyncTask.Status.FINISHED) {
                        // Vérifier si le résultat est prêt
                        if (accessTask.isResultReady()) {
                            result = accessTask.getResult();
                            isComplete = true;
                        }
                    }
                }
                
                if (isComplete && result != null && !result.isEmpty()) {
                    // Opération réussie
                    isOperationInProgress = false;
                    disableButtons(false);
                    
                    if (isRead) {
                        showResult("Read " + target + ": " + result);
                    } else {
                        showResult("Write successful");
                        tagEpc = editTextNewEpc.getText().toString().trim();
                        textViewEpcValue.setText("EPC: " + tagEpc);
                    }
                } else if (checkCount >= MAX_CHECK_COUNT) {
                    // Timeout
                    isOperationInProgress = false;
                    disableButtons(false);
                    showResult("Operation timeout");
                    
                    if (accessTask != null) {
                        accessTask.cancel(true);
                    }
                } else {
                    // Continuer à vérifier
                    mHandler.postDelayed(this, 100);
                }
            }
        };
        
        mHandler.postDelayed(resultCheckRunnable, 100);
    }
    
    private void disableButtons(boolean disable) {
        buttonReadEpc.setEnabled(!disable);
        buttonReadTid.setEnabled(!disable);
        buttonReadUser.setEnabled(!disable);
        buttonWriteEpc.setEnabled(!disable);
        editTextNewEpc.setEnabled(!disable);
        editTextAccessPassword.setEnabled(!disable);
    }
    
    private void showResult(String message) {
        if (isAdded() && getActivity() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onDestroy() {
        if (resultCheckRunnable != null) {
            mHandler.removeCallbacks(resultCheckRunnable);
        }
        if (accessTask != null) {
            accessTask.cancel(true);
        }
        super.onDestroy();
    }
}