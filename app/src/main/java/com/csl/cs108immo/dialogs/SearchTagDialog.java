package com.csl.cs108immo.dialogs;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.csl.cs108immo.CustomMediaPlayer;
import com.csl.cs108immo.InventoryRfidTask;
import com.csl.cs108immo.MainActivity;
import com.csl.cs108immo.R;
import com.csl.cslibrary4a.RfidReaderChipData;
import com.csl.cs108immo.fragments.LectureFragment;

public class SearchTagDialog extends DialogFragment {
    
    private static final String ARG_TAG_ID = "tag_id";
    
    private String tagId;
    private ProgressBar geigerProgress;
    private CheckBox checkBoxGeigerTone;
    private SeekBar seekGeiger;
    private Spinner memoryBankSpinner;
    private EditText editTextRWSelectOffset;
    private EditText editTextGeigerAntennaPower;
    private TextView geigerThresholdView;
    private TextView geigerTagRssiView;
    private TextView geigerTagGotView;
    private TextView geigerRunTime;
    private TextView geigerVoltageLevelView;
    private TextView rfidYieldView;
    private TextView rfidRateView;
    private Button button;
    private Button closeButton;  // Déclarer le bouton Fermer
    
    private boolean isSearching = false;  // État de la recherche
    private int thresholdValue = 0;
    private InventoryRfidTask geigerSearchTask;
    private CustomMediaPlayer playerN;
    private final Handler mHandler = new Handler();
    
    private double alertRssi;
    private boolean alerting = false;
    private long alertRssiUpdateTime;
    private double dBuV_dBm_constant;
    private final int labelMin = -90;
    private final int labelMax = -10;
    
    public static SearchTagDialog newInstance(String tagId) {
        SearchTagDialog dialog = new SearchTagDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TAG_ID, tagId);
        dialog.setArguments(args);
        return dialog;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // Empêcher la fermeture en cliquant à l'extérieur
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
        
        if (getArguments() != null) {
            tagId = getArguments().getString(ARG_TAG_ID);
        }
        
        try {
            dBuV_dBm_constant = MainActivity.csLibrary4A.dBuV_dBm_constant;
            playerN = MainActivity.sharedObjects.playerL;
        } catch (Exception e) {
            // Handle initialization error
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_search_tag, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupListeners();
        
        // Empêcher la fermeture avec le bouton back quand la recherche est en cours
        getDialog().setCancelable(false);
    }
    
    private void setupViews(View view) {
        TextView textViewProgressLabelMin = view.findViewById(R.id.geigerProgressLabelMin);
        TextView textViewProgressLabelMid = view.findViewById(R.id.geigerProgressLabelMid);
        TextView textViewProgressLabelMax = view.findViewById(R.id.geigerProgressLabelMax);
        
        if (textViewProgressLabelMin != null) {
            textViewProgressLabelMin.setText(String.format("%.0f", MainActivity.csLibrary4A.getRssiDisplaySetting() != 0 ? labelMin : labelMin + dBuV_dBm_constant));
        }
        if (textViewProgressLabelMid != null) {
            textViewProgressLabelMid.setText(String.format("%.0f", MainActivity.csLibrary4A.getRssiDisplaySetting() != 0 ? labelMin + (labelMax - labelMin) / 2 : labelMin + (labelMax - labelMin) / 2 + dBuV_dBm_constant));
        }
        if (textViewProgressLabelMax != null) {
            textViewProgressLabelMax.setText(String.format("%.0f", MainActivity.csLibrary4A.getRssiDisplaySetting() != 0 ? labelMax : labelMax + dBuV_dBm_constant));
        }
        
        geigerProgress = view.findViewById(R.id.geigerProgress);
        checkBoxGeigerTone = view.findViewById(R.id.geigerToneCheck);
        seekGeiger = view.findViewById(R.id.geigerSeek);
        memoryBankSpinner = view.findViewById(R.id.selectMemoryBank);
        editTextRWSelectOffset = view.findViewById(R.id.selectMemoryOffset);
        editTextGeigerAntennaPower = view.findViewById(R.id.selectAntennaPower);
        geigerThresholdView = view.findViewById(R.id.geigerThreshold);
        geigerTagRssiView = view.findViewById(R.id.geigerTagRssi);
        geigerTagGotView = view.findViewById(R.id.geigerTagGot);
        geigerRunTime = view.findViewById(R.id.geigerRunTime);
        geigerVoltageLevelView = view.findViewById(R.id.geigerVoltageLevel);
        rfidYieldView = view.findViewById(R.id.geigerYield);
        rfidRateView = view.findViewById(R.id.geigerRate);
        button = view.findViewById(R.id.geigerStart);
        closeButton = view.findViewById(R.id.closeButton);  // Initialiser le bouton Fermer
        
        // Set tag ID
        EditText selectTagID = view.findViewById(R.id.selectTagID);
        if (selectTagID != null && tagId != null) {
            selectTagID.setText(tagId);
        }
        
        // Set antenna power
        if (editTextGeigerAntennaPower != null) {
            editTextGeigerAntennaPower.setText("300");
        }
        
        // Set up close button
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                if (!isSearching) {
                    dismiss();
                }
            });
        }
        
        // Setup memory bank spinner
        if (memoryBankSpinner != null) {
            ArrayAdapter<CharSequence> memoryBankAdapter = ArrayAdapter.createFromResource(
                    getActivity(), R.array.read_memoryBank_options, R.layout.custom_spinner_layout);
            memoryBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            memoryBankSpinner.setAdapter(memoryBankAdapter);
            memoryBankSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (editTextRWSelectOffset != null) {
                        switch (position) {
                            case 0: // EPC
                                editTextRWSelectOffset.setText("32");
                                break;
                            case 1: // TID
                            case 2: // USER
                                editTextRWSelectOffset.setText("0");
                                break;
                        }
                    }
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
        
        // Setup seek bar
        if (seekGeiger != null) {
            seekGeiger.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        thresholdValue = progress;
                        if (geigerThresholdView != null) {
                            geigerThresholdView.setText(String.format("%.2f", 
                                MainActivity.csLibrary4A.getRssiDisplaySetting() == 0 ? thresholdValue : thresholdValue - dBuV_dBm_constant));
                        }
                    }
                }
                
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
        
        // Setup RSSI text watcher
        if (geigerTagRssiView != null) {
            geigerTagRssiView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                
                @Override
                public void afterTextChanged(Editable s) {
                    if (alertRssiUpdateTime < 0 || geigerProgress == null) return;
                    try {
                        String text = s.toString();
                        if (text.isEmpty()) return;
                        double rssi = Double.parseDouble(text);
                        if (MainActivity.csLibrary4A.getRssiDisplaySetting() != 0) rssi += dBuV_dBm_constant;
                        double progressPos = geigerProgress.getMax() * (rssi - labelMin - dBuV_dBm_constant) / (labelMax - labelMin);
                        if (progressPos < 0) progressPos = 0;
                        if (progressPos > geigerProgress.getMax()) progressPos = geigerProgress.getMax();
                        geigerProgress.setProgress((int) progressPos);
                        alertRssiUpdateTime = System.currentTimeMillis();
                        alertRssi = rssi;
                        
                        if (rssi > thresholdValue && checkBoxGeigerTone != null && checkBoxGeigerTone.isChecked()) {
                            if (!alerting) {
                                alerting = true;
                                mHandler.removeCallbacks(mAlertRunnable);
                                mHandler.post(mAlertRunnable);
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            });
        }
    }
    
    private void setupListeners() {
        if (button != null) {
            button.setOnClickListener(v -> startStopHandler(false));
        }
    }
    
    /**
     * Met à jour l'état de l'UI selon que la recherche est en cours ou non
     */
    private void updateUIForSearchState(boolean searching) {
        isSearching = searching;
        
        // Mettre à jour le texte du bouton Start/Stop
        if (button != null) {
            button.setText(searching ? "Arrêter" : "Démarrer");
        }
        
        // Activer/désactiver le bouton Fermer
        if (closeButton != null) {
            closeButton.setEnabled(!searching);
            // Optionnel : changer l'apparence du bouton désactivé
            closeButton.setAlpha(searching ? 0.5f : 1.0f);
        }
        
        // Empêcher ou permettre la fermeture du dialog
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCancelable(!searching);
            dialog.setCanceledOnTouchOutside(!searching);
        }
        
        // Désactiver/activer les contrôles pendant la recherche
        View view = getView();
        if (view != null) {
            EditText selectTagID = view.findViewById(R.id.selectTagID);
            if (selectTagID != null) {
                selectTagID.setEnabled(!searching);
            }
            if (editTextGeigerAntennaPower != null) {
                editTextGeigerAntennaPower.setEnabled(!searching);
            }
            if (memoryBankSpinner != null) {
                memoryBankSpinner.setEnabled(!searching);
            }
            if (seekGeiger != null) {
                seekGeiger.setEnabled(!searching);
            }
            if (checkBoxGeigerTone != null) {
                checkBoxGeigerTone.setEnabled(!searching);
            }
        }
    }
    
    private void startStopHandler(boolean buttonTrigger) {
        boolean currentlyRunning = (geigerSearchTask != null && geigerSearchTask.getStatus() == AsyncTask.Status.RUNNING);
        
        if (buttonTrigger && 
                ((currentlyRunning && MainActivity.csLibrary4A.getTriggerButtonStatus())
                        || (!currentlyRunning && !MainActivity.csLibrary4A.getTriggerButtonStatus()))) {
            return;
        }
        
        if (!currentlyRunning) {
            if (!MainActivity.csLibrary4A.isBleConnected()) {
                Toast.makeText(getActivity(), R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                return;
            } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                Toast.makeText(getActivity(), "Rfid is disabled", Toast.LENGTH_SHORT).show();
                return;
            } else if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0) {
                Toast.makeText(getActivity(), R.string.toast_not_ready, Toast.LENGTH_SHORT).show();
                return;
            }
            startInventoryTask();
            alertRssiUpdateTime = 0;
            updateUIForSearchState(true);  // Désactiver le bouton Fermer
        } else {
            if (geigerSearchTask != null) {
                geigerSearchTask.taskCancelReason = buttonTrigger ? 
                        InventoryRfidTask.TaskCancelRReason.BUTTON_RELEASE : 
                        InventoryRfidTask.TaskCancelRReason.STOP;
            }
            alertRssiUpdateTime = -1;
            updateUIForSearchState(false);  // Réactiver le bouton Fermer
        }
    }
    
    private void startInventoryTask() {
        boolean invalidRequest = false;
        
        View view = getView();
        if (view == null) {
            updateUIForSearchState(false);
            return;
        }
        
        EditText selectTagID = view.findViewById(R.id.selectTagID);
        if (selectTagID == null) {
            updateUIForSearchState(false);
            return;
        }
        
        int memorybank = memoryBankSpinner != null ? memoryBankSpinner.getSelectedItemPosition() : 0;
        
        String powerStr = editTextGeigerAntennaPower != null ? editTextGeigerAntennaPower.getText().toString() : "300";
        int powerLevel = powerStr.isEmpty() ? 300 : Integer.parseInt(powerStr);
        
        if (powerLevel < 0 || powerLevel > 330) {
            invalidRequest = true;
        } else {
            String tagIdStr = selectTagID.getText().toString();
            if (!MainActivity.csLibrary4A.setSelectedTag(tagIdStr, memorybank + 1, powerLevel)) {
                invalidRequest = true;
            } else {
                MainActivity.csLibrary4A.startOperation(RfidReaderChipData.OperationTypes.TAG_SEARCHING);
            }
        }
        
        geigerSearchTask = new InventoryRfidTask(getContext(), -1, -1, 0, 0, 0, 0, 
                invalidRequest, true,
                null, null, geigerTagRssiView, null,
                geigerRunTime, geigerTagGotView, geigerVoltageLevelView, null, button, rfidRateView) {
            
            @Override
            protected void onCancelled() {
                super.onCancelled();
                // S'assurer que l'UI est mise à jour quand la tâche est annulée
                mHandler.post(() -> updateUIForSearchState(false));
            }
            
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                // S'assurer que l'UI est mise à jour quand la tâche se termine
                mHandler.post(() -> updateUIForSearchState(false));
            }
        };
        geigerSearchTask.execute();
    }
    
    private final Runnable mAlertRunnable = new Runnable() {
        @Override
        public void run() {
            if (playerN == null) return;
            
            boolean alerting1 = true;
            final int toneLength = 50;
            mHandler.removeCallbacks(mAlertRunnable);
            
            if (alertRssi < 20 || alertRssi < thresholdValue || 
                checkBoxGeigerTone == null || !checkBoxGeigerTone.isChecked() 
                    || alertRssiUpdateTime < 0 || System.currentTimeMillis() - alertRssiUpdateTime > 200) {
                alerting1 = false;
            }
            
            if (!alerting1) {
                playerN.pause();
                alerting = false;
            } else if (!playerN.isPlaying()) {
                mHandler.postDelayed(mAlertRunnable, toneLength);
                playerN.start();
            } else {
                int tonePause = 0;
                if (alertRssi >= 60) tonePause = toneLength;
                else if (alertRssi >= 50) tonePause = 250 - toneLength;
                else if (alertRssi >= 40) tonePause = 500 - toneLength;
                else if (alertRssi >= 30) tonePause = 1000 - toneLength;
                else if (alertRssi >= 20) tonePause = 2000 - toneLength;
                
                if (tonePause > 0) mHandler.postDelayed(mAlertRunnable, tonePause);
                if (tonePause <= 0 || alertRssi < 60) playerN.pause();
                alerting = tonePause > 0;
            }
        }
    };
    
    @Override
    public void onDestroyView() {
        // Arrêter la recherche si elle est en cours
        if (geigerSearchTask != null) {
            geigerSearchTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.DESTORY;
        }
        MainActivity.csLibrary4A.restoreAfterTagSelect();
        MainActivity.csLibrary4A.setVibrateOn(0);
        
        // Notify parent fragment to restore notification listener
        if (getParentFragment() instanceof LectureFragment) {
            ((LectureFragment) getParentFragment()).restoreNotificationListener();
        }
        
        super.onDestroyView();
    }
}