package com.csl.cs108immo.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import com.csl.cs108immo.MainActivity;
import com.csl.cs108immo.R;
import com.csl.cs108immo.apps.SharedPrefManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.csl.cs108immo.app.db.DatabaseHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.ContentValues;

public class EditImmoDialog extends DialogFragment {
    
    private EditText editTextCodeBarre, editTextNumSerie, editTextRFID;
    private Spinner spinnerDescription, spinnerEtat, spinnerEmplacement;
    private Button buttonUpdate, buttonCancel;
    private ProgressBar progressBar;
    private String tag;
    private String codeInv, codeMission, equipe;
    private String token;
    private String baseUrl;
    private boolean isOnline;
    private VerificationCallback callback;
    
    private List<String> descriptionList = new ArrayList<>();
    private List<String> etatList = new ArrayList<>();
    private List<String> etatCodeList = new ArrayList<>();
    private List<String> emplacementList = new ArrayList<>();
    private List<String> emplacementCodeList = new ArrayList<>();
    
    private String currentDescription = "";
    private String currentEtatCode = "";
    private String currentEmplacementCode = "";
    private String currentCodeRFID = "";
    private String currentCodeBarre = "";
    private String currentNumSerie = "";
    private String currentNote = "";
    private String codeImmo = "";
    
    private int loadedCount = 0;
    private int totalLoadCount = 5; // 4 data loads + 1 attribut load
    
    // Map to store attribut configuration
    private Map<String, Boolean> attributAfficher = new HashMap<>();
    private Map<String, Boolean> attributSaisir = new HashMap<>();
    
    public interface VerificationCallback {
        void onComplete();
    }
    
    public static EditImmoDialog newInstance(String tag, String codeInv, String codeMission, String equipe, VerificationCallback callback) {
        EditImmoDialog dialog = new EditImmoDialog();
        Bundle args = new Bundle();
        args.putString("tag", tag);
        args.putString("codeInv", codeInv);
        args.putString("codeMission", codeMission);
        args.putString("equipe", equipe);
        dialog.setArguments(args);
        dialog.callback = callback;
        return dialog;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tag = getArguments().getString("tag");
            codeInv = getArguments().getString("codeInv");
            codeMission = getArguments().getString("codeMission");
            equipe = getArguments().getString("equipe");
        }
        isOnline = SharedPrefManager.getInstance(getContext()).isOnlineMode();
        token = SharedPrefManager.getInstance(getContext()).getToken();
        baseUrl = SharedPrefManager.getInstance(getContext()).getServerUrl();
        MainActivity.csLibrary4A.appendToLogView("EditImmoDialog: Initialisé avec tag=" + tag);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_immo, null);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(v -> dismiss());
        
        editTextCodeBarre = view.findViewById(R.id.editTextCodeBarre);
        editTextRFID = view.findViewById(R.id.editTextRFID);
        editTextNumSerie = view.findViewById(R.id.editTextNumSerie);
        spinnerDescription = view.findViewById(R.id.spinnerDescription);
        spinnerEtat = view.findViewById(R.id.spinnerEtat);
        spinnerEmplacement = view.findViewById(R.id.spinnerEmplacement);
        buttonUpdate = view.findViewById(R.id.buttonUpdate);
        progressBar = view.findViewById(R.id.progressBar);
                
        buttonUpdate.setOnClickListener(v -> updateImmo());
        
        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        
        showProgress(true);
        loadAllData();
        
        return dialog;
    }
    
    private void loadAllData() {
        loadedCount = 0;
        if (isOnline) {
            loadAttributsOnline();
            loadImmoDetailsOnline();
            loadDescriptionsOnline();
            loadEtatsOnline();
            loadEmplacementsOnline();
        } else {
            loadAttributsOffline();
            loadImmoDetailsOffline();
            loadDescriptionsOffline();
            loadEtatsOffline();
            loadEmplacementsOffline();
        }
    }
    
    private void loadAttributsOnline() {
        new Thread(() -> {
            try {
                URL url = new URL(baseUrl + "api-Inv-attributinvImmo.awp?log=" + token + "&inv=" + codeInv);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONObject result = new JSONObject(response.toString());
                if ("ok".equals(result.getString("sRes"))) {
                    JSONArray tabAttribut = result.getJSONArray("tab_attribut");
                    attributAfficher.clear();
                    attributSaisir.clear();
                    for (int i = 0; i < tabAttribut.length(); i++) {
                        JSONObject obj = tabAttribut.getJSONObject(i);
                        String codeAttribut = obj.getString("codeatribut");
                        boolean afficher = obj.getBoolean("afficher");
                        boolean saisir = obj.getBoolean("saisir");
                        attributAfficher.put(codeAttribut, afficher);
                        attributSaisir.put(codeAttribut, saisir);
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> applyAttributRules());
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> checkAllDataLoaded());
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        checkAllDataLoaded();
                        Toast.makeText(getContext(), "Erreur chargement attributs", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }
    
    private void loadAttributsOffline() {
        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String query = "SELECT codeatribut, saisie as saisir, afficher FROM Inv_attributinvImmo WHERE Codeinv = ?";
            String[] args = {codeInv};
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(query, args);
                attributAfficher.clear();
                attributSaisir.clear();
                while (cursor.moveToNext()) {
                    String codeAttribut = cursor.getString(0);
                    boolean saisir = cursor.getInt(1) == 1;
                    boolean afficher = cursor.getInt(2) == 1;
                    attributAfficher.put(codeAttribut, afficher);
                    attributSaisir.put(codeAttribut, saisir);
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> applyAttributRules());
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> checkAllDataLoaded());
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> checkAllDataLoaded());
                }
            } finally {
                if (cursor != null) cursor.close();
                db.close();
            }
        }).start();
    }
    
    private void applyAttributRules() {
        // Apply rules for ancien_code (editTextRFID)
        applyFieldRules("ancien_code", editTextRFID);
        
        // Apply rules for code_barre (editTextCodeBarre)
        applyFieldRules("code_barre", editTextCodeBarre);
        
        // Apply rules for num_serie (editTextNumSerie)
        applyFieldRules("num_serie", editTextNumSerie);
        
        // Apply rules for code_etat (spinnerEtat)
        applySpinnerRules("code_etat", spinnerEtat);
        
        // Apply rules for emplment (spinnerEmplacement)
        applySpinnerRules("emplment", spinnerEmplacement);
    }
    
    private void applyFieldRules(String attributCode, EditText editText) {
        Boolean afficher = attributAfficher.get(attributCode);
        Boolean saisir = attributSaisir.get(attributCode);
        
        if (afficher != null) {
            if (!afficher) {
                // Hide the EditText and its label
                editText.setVisibility(View.GONE);
                // Find and hide the corresponding label
                View parent = (View) editText.getParent();
                if (parent instanceof android.view.ViewGroup) {
                    android.view.ViewGroup group = (android.view.ViewGroup) parent;
                    for (int i = 0; i < group.getChildCount(); i++) {
                        View child = group.getChildAt(i);
                        if (child instanceof TextView && child != editText) {
                            TextView label = (TextView) child;
                            // Check if this label is just before our EditText
                            int editTextIndex = group.indexOfChild(editText);
                            int labelIndex = group.indexOfChild(label);
                            if (labelIndex == editTextIndex - 1) {
                                label.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            } else {
                editText.setVisibility(View.VISIBLE);
                // Show the label
                View parent = (View) editText.getParent();
                if (parent instanceof android.view.ViewGroup) {
                    android.view.ViewGroup group = (android.view.ViewGroup) parent;
                    for (int i = 0; i < group.getChildCount(); i++) {
                        View child = group.getChildAt(i);
                        if (child instanceof TextView && child != editText) {
                            TextView label = (TextView) child;
                            int editTextIndex = group.indexOfChild(editText);
                            int labelIndex = group.indexOfChild(label);
                            if (labelIndex == editTextIndex - 1) {
                                label.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        }
        
        if (saisir != null) {
            editText.setEnabled(saisir);
            if (!saisir) {
                editText.setAlpha(0.5f); // Visual indication of disabled state
            } else {
                editText.setAlpha(1.0f);
            }
        }
    }
    
    private void applySpinnerRules(String attributCode, Spinner spinner) {
        Boolean afficher = attributAfficher.get(attributCode);
        Boolean saisir = attributSaisir.get(attributCode);
        
        if (afficher != null) {
            if (!afficher) {
                spinner.setVisibility(View.GONE);
                // Hide the corresponding label
                View parent = (View) spinner.getParent();
                if (parent instanceof android.view.ViewGroup) {
                    android.view.ViewGroup group = (android.view.ViewGroup) parent;
                    for (int i = 0; i < group.getChildCount(); i++) {
                        View child = group.getChildAt(i);
                        if (child instanceof TextView && child != spinner) {
                            TextView label = (TextView) child;
                            int spinnerIndex = group.indexOfChild(spinner);
                            int labelIndex = group.indexOfChild(label);
                            if (labelIndex == spinnerIndex - 1) {
                                label.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            } else {
                spinner.setVisibility(View.VISIBLE);
                View parent = (View) spinner.getParent();
                if (parent instanceof android.view.ViewGroup) {
                    android.view.ViewGroup group = (android.view.ViewGroup) parent;
                    for (int i = 0; i < group.getChildCount(); i++) {
                        View child = group.getChildAt(i);
                        if (child instanceof TextView && child != spinner) {
                            TextView label = (TextView) child;
                            int spinnerIndex = group.indexOfChild(spinner);
                            int labelIndex = group.indexOfChild(label);
                            if (labelIndex == spinnerIndex - 1) {
                                label.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        }
        
        if (saisir != null) {
            spinner.setEnabled(saisir);
            if (!saisir) {
                spinner.setAlpha(0.5f);
            } else {
                spinner.setAlpha(1.0f);
            }
        }
    }
    
    private void loadImmoDetailsOffline() {
        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String query = "SELECT CodeImmo, Descp_immo, codeBarre_Corr, NumSerie_Corr, " +
                           "codeEtat_Corr, codeEmpInv, Observation, ancienCode_Corr " +
                           "FROM Det_inv_immo " +
                           "WHERE CodeInventaire = ? " +
                           "AND (CodeImmo = ? OR NumSerie_Corr = ? OR codeBarre_Corr = ?)";
            String[] args = {codeInv, tag, tag, tag};
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(query, args);
                if (cursor.moveToFirst()) {
                    codeImmo = cursor.getString(0);
                    currentDescription = cursor.getString(1);
                    currentCodeRFID = cursor.getString(7);
                    currentCodeBarre = cursor.getString(2);
                    currentNumSerie = cursor.getString(3);
                    currentEtatCode = cursor.getString(4);
                    currentEmplacementCode = cursor.getString(5);
                    currentNote = cursor.getString(6);
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            editTextCodeBarre.setText(currentCodeBarre != null ? currentCodeBarre : "");
                            editTextRFID.setText(currentCodeRFID != null ? currentCodeRFID : "");
                            editTextNumSerie.setText(currentNumSerie != null ? currentNumSerie : "");
                            checkAllDataLoaded();
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Immobilisation non trouvée", Toast.LENGTH_SHORT).show();
                            dismiss();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
                db.close();
            }
        }).start();
    }
    
    private void loadImmoDetailsOnline() {
        new Thread(() -> {
            try {
                URL url = new URL(baseUrl + "api-inv-search-cod.awp?log=" + token + "&inv=" + codeInv + "&code=" + tag);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONObject result = new JSONObject(response.toString());
                if ("ok".equals(result.getString("sRes"))) {
                    JSONObject tabSrch = result.getJSONObject("tab_srch");
                    codeImmo = tabSrch.getString("CodeImmo");
                    currentDescription = tabSrch.optString("Descp_immo", "");
                    currentCodeRFID = tabSrch.optString("ancien_code_inv", "");
                    currentCodeBarre = tabSrch.optString("codeBarre_inv", "");
                    currentNumSerie = tabSrch.optString("num_serie_inv", "");
                    currentEtatCode = tabSrch.optString("codeEtat_Corr", "");
                    currentEmplacementCode = tabSrch.optString("codeEmpInv", "");
                    currentNote = tabSrch.optString("Observation", "");
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            editTextCodeBarre.setText(currentCodeBarre);
                            editTextRFID.setText(currentCodeRFID);
                            editTextNumSerie.setText(currentNumSerie);
                            checkAllDataLoaded();
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Impossible de charger les détails", Toast.LENGTH_SHORT).show();
                            dismiss();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Erreur de chargement", Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
                }
            }
        }).start();
    }
    
    private void loadDescriptionsOffline() {
        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String query = "SELECT DISTINCT Descp_immo FROM Det_inv_immo WHERE CodeInventaire = ?";
            String[] args = {codeInv};
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(query, args);
                descriptionList.clear();
                while (cursor.moveToNext()) {
                    String desc = cursor.getString(0);
                    if (desc != null && !desc.isEmpty()) {
                        descriptionList.add(desc);
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_item, descriptionList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerDescription.setAdapter(adapter);
                        // Sélectionner la description actuelle
                        for (int i = 0; i < descriptionList.size(); i++) {
                            if (descriptionList.get(i).equals(currentDescription)) {
                                spinnerDescription.setSelection(i);
                                break;
                            }
                        }
                        checkAllDataLoaded();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> checkAllDataLoaded());
                }
            } finally {
                if (cursor != null) cursor.close();
                db.close();
            }
        }).start();
    }
    
    private void loadDescriptionsOnline() {
        new Thread(() -> {
            try {
                URL url = new URL(baseUrl + "api-listDescp.awp?log=" + token + "&inv=" + codeInv + "&rech=");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONObject result = new JSONObject(response.toString());
                if ("ok".equals(result.getString("sRes"))) {
                    JSONArray tabSrch = result.getJSONArray("tab_srch");
                    descriptionList.clear();
                    for (int i = 0; i < tabSrch.length(); i++) {
                        JSONObject obj = tabSrch.getJSONObject(i);
                        descriptionList.add(obj.getString("lib"));
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                android.R.layout.simple_spinner_item, descriptionList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerDescription.setAdapter(adapter);
                            for (int i = 0; i < descriptionList.size(); i++) {
                                if (descriptionList.get(i).equals(currentDescription)) {
                                    spinnerDescription.setSelection(i);
                                    break;
                                }
                            }
                            checkAllDataLoaded();
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> checkAllDataLoaded());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> checkAllDataLoaded());
                }
            }
        }).start();
    }
    
    private void loadEtatsOffline() {
        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String query = "SELECT LibelleEtat, DescriptionEtat FROM SouSEtat ORDER BY LibelleEtat";
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(query, null);
                etatList.clear();
                etatCodeList.clear();
                while (cursor.moveToNext()) {
                    String code = cursor.getString(0);
                    String libelle = cursor.getString(1);
                    etatList.add(libelle);
                    etatCodeList.add(code);
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_item, etatList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerEtat.setAdapter(adapter);
                        for (int i = 0; i < etatCodeList.size(); i++) {
                            if (etatCodeList.get(i).equals(currentEtatCode)) {
                                spinnerEtat.setSelection(i);
                                break;
                            }
                        }
                        checkAllDataLoaded();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> checkAllDataLoaded());
                }
            } finally {
                if (cursor != null) cursor.close();
                db.close();
            }
        }).start();
    }
    
    private void loadEtatsOnline() {
        new Thread(() -> {
            try {
                URL url = new URL(baseUrl + "api-Inv-attributinvImmo.awp?log=" + token + "&inv=" + codeInv);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONObject result = new JSONObject(response.toString());
                if ("ok".equals(result.getString("sRes"))) {
                    JSONArray tabEtat = result.getJSONArray("tab_etat");
                    etatList.clear();
                    etatCodeList.clear();
                    for (int i = 0; i < tabEtat.length(); i++) {
                        JSONObject obj = tabEtat.getJSONObject(i);
                        etatList.add(obj.getString("nom"));
                        etatCodeList.add(obj.getString("code"));
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                android.R.layout.simple_spinner_item, etatList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerEtat.setAdapter(adapter);
                            for (int i = 0; i < etatCodeList.size(); i++) {
                                if (etatCodeList.get(i).equals(currentEtatCode)) {
                                    spinnerEtat.setSelection(i);
                                    break;
                                }
                            }
                            checkAllDataLoaded();
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> checkAllDataLoaded());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> checkAllDataLoaded());
                }
            }
        }).start();
    }
    
    private void loadEmplacementsOffline() {
        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String query = "SELECT DISTINCT Inv_EquipeInventaire.CodeEmplacement AS CodeEmplacement, " +
                           "ArbreEmp.code AS Libelle " +
                           "FROM Inv_EquipeInventaire, ArbreEmp " +
                           "WHERE Inv_EquipeInventaire.CodeEmplacement = ArbreEmp.code " +
                           "AND ArbreEmp.Idparent <> '#' " +
                           "AND Inv_EquipeInventaire.Codeinv = ? " +
                           "AND Inv_EquipeInventaire.codemission = ? " +
                           "AND Inv_EquipeInventaire.codeEquipe = ?";
            String[] args = {codeInv, codeMission, equipe};
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(query, args);
                emplacementList.clear();
                emplacementCodeList.clear();
                while (cursor.moveToNext()) {
                    String code = cursor.getString(0);
                    String libelle = cursor.getString(1);
                    emplacementList.add(libelle);
                    emplacementCodeList.add(code);
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_item, emplacementList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerEmplacement.setAdapter(adapter);
                        for (int i = 0; i < emplacementCodeList.size(); i++) {
                            if (emplacementCodeList.get(i).equals(currentEmplacementCode)) {
                                spinnerEmplacement.setSelection(i);
                                break;
                            }
                        }
                        checkAllDataLoaded();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> checkAllDataLoaded());
                }
            } finally {
                if (cursor != null) cursor.close();
                db.close();
            }
        }).start();
    }
    
    private void loadEmplacementsOnline() {
        new Thread(() -> {
            try {
                URL url = new URL(baseUrl + "api-lecture-empl.awp?log=" + token + "&inv=" + codeInv +
                    "&mis=" + codeMission + "&eqp=" + equipe);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONObject result = new JSONObject(response.toString());
                if ("ok".equals(result.getString("sRes"))) {
                    JSONArray tabEmpl = result.getJSONArray("tab_empl");
                    emplacementList.clear();
                    emplacementCodeList.clear();
                    for (int i = 0; i < tabEmpl.length(); i++) {
                        JSONObject obj = tabEmpl.getJSONObject(i);
                        emplacementList.add(obj.getString("sCode"));
                        emplacementCodeList.add(obj.getString("sCode"));
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                android.R.layout.simple_spinner_item, emplacementList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerEmplacement.setAdapter(adapter);
                            for (int i = 0; i < emplacementCodeList.size(); i++) {
                                if (emplacementCodeList.get(i).equals(currentEmplacementCode)) {
                                    spinnerEmplacement.setSelection(i);
                                    break;
                                }
                            }
                            checkAllDataLoaded();
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> checkAllDataLoaded());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> checkAllDataLoaded());
                }
            }
        }).start();
    }
    
    private void checkAllDataLoaded() {
        loadedCount++;
        if (loadedCount >= totalLoadCount && getActivity() != null) {
            getActivity().runOnUiThread(() -> showProgress(false));
        }
    }
    
    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            buttonUpdate.setEnabled(false);
            buttonCancel.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonUpdate.setEnabled(true);
            buttonCancel.setEnabled(true);
        }
    }
    
    private void updateImmo() {
        if (spinnerDescription.getSelectedItem() == null || spinnerDescription.getSelectedItemPosition() < 0) {
            Toast.makeText(getContext(), "Veuillez sélectionner une description", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerEtat.getVisibility() == View.VISIBLE && 
            (spinnerEtat.getSelectedItem() == null || spinnerEtat.getSelectedItemPosition() < 0)) {
            Toast.makeText(getContext(), "Veuillez sélectionner un état", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerEmplacement.getVisibility() == View.VISIBLE && 
            (spinnerEmplacement.getSelectedItem() == null || spinnerEmplacement.getSelectedItemPosition() < 0)) {
            Toast.makeText(getContext(), "Veuillez sélectionner un emplacement", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showProgress(true);
        buttonUpdate.setEnabled(false);
        
        if (isOnline) {
            updateImmoOnline();
        } else {
            updateImmoOffline();
        }
    }
    
    private void updateImmoOffline() {
        final String description = spinnerDescription.getSelectedItem().toString();
        final String codeRFID = editTextRFID.getText().toString().trim();
        final String codeBarre = editTextCodeBarre.getText().toString().trim();
        final String numSerie = editTextNumSerie.getText().toString().trim();
        final String codeEtat = spinnerEtat.getVisibility() == View.VISIBLE ? 
            etatCodeList.get(spinnerEtat.getSelectedItemPosition()) : currentEtatCode;
        final String codeEmp = spinnerEmplacement.getVisibility() == View.VISIBLE ? 
            emplacementCodeList.get(spinnerEmplacement.getSelectedItemPosition()) : currentEmplacementCode;
        
        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String dateheurenow = sdf.format(new Date());
                String userCode = SharedPrefManager.getInstance(getContext()).getUserCode();
                
                // Récupérer l'emplacement théorique actuel
                String selectQuery = "SELECT codeEmpInv FROM Det_inv_immo WHERE CodeInventaire = ? AND CodeImmo = ?";
                String[] selectArgs = {codeInv, codeImmo};
                Cursor cursor = null;
                String currentEmp = null;
                try {
                    cursor = db.rawQuery(selectQuery, selectArgs);
                    if (cursor.moveToFirst()) {
                        currentEmp = cursor.getString(0);
                    }
                } finally {
                    if (cursor != null) cursor.close();
                }
                
                ContentValues values = new ContentValues();
                values.put("Descp_immo", description);
                values.put("DateLecture", dateheurenow);
                values.put("UserLecture", userCode);
                values.put("codeEmpInv", codeEmp);
                values.put("codemission", codeMission);
                values.put("codeEquipe", equipe);
                values.put("codeBarre_Corr", codeBarre);
                values.put("ancienCode_Corr", codeRFID);
                values.put("NumSerie_Corr", numSerie);
                values.put("issync", 2);
                values.put("codeEtat_Corr", codeEtat);
                
                if (currentEmp != null && !currentEmp.equals(codeEmp)) {
                    values.put("nbr_Crt", 1);
                    values.put("nbr_Crt_valid", 1);
                    values.put("EtatCorrection", 4);
                } else {
                    values.put("nbr_Crt", 0);
                    values.put("nbr_Crt_valid", 0);
                    if (currentEmp != null && currentEmp.equals(codeEmp)) {
                        values.put("EtatCorrection", 1);
                        values.put("DateValidation", dateheurenow);
                        values.put("Uservalidation", userCode);
                    } else {
                        values.put("EtatCorrection", 2);
                    }
                }
                
                String whereClause = "CodeInventaire = ? AND CodeImmo = ?";
                String[] whereArgs = {codeInv, codeImmo};
                int rowsAffected = db.update(DatabaseHelper.TABLE_DET_INV_IMMO, values, whereClause, whereArgs);
                
                if (getActivity() != null) {
                    final int finalRowsAffected = rowsAffected;
                    getActivity().runOnUiThread(() -> {
                        if (finalRowsAffected > 0) {
                            Toast.makeText(getContext(), "Immobilisation modifiée en mode offline", Toast.LENGTH_LONG).show();
                            dismiss();
                            if (callback != null) callback.onComplete();
                        } else {
                            showProgress(false);
                            Toast.makeText(getContext(), "Erreur lors de la modification", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(getContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            } finally {
                if (db != null) db.close();
            }
        }).start();
    }
    
    private void updateImmoOnline() {
        final String username = SharedPrefManager.getInstance(getContext()).getUsername();
        final String description = spinnerDescription.getSelectedItem().toString();
        final String codeRFID = editTextRFID.getText().toString().trim();
        final String codeBarre = editTextCodeBarre.getText().toString().trim();
        final String numSerie = editTextNumSerie.getText().toString().trim();
        final String codeEtat = spinnerEtat.getVisibility() == View.VISIBLE ? 
            etatCodeList.get(spinnerEtat.getSelectedItemPosition()) : currentEtatCode;
        final String codeEmp = spinnerEmplacement.getVisibility() == View.VISIBLE ? 
            emplacementCodeList.get(spinnerEmplacement.getSelectedItemPosition()) : currentEmplacementCode;
        
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(baseUrl + "api-update-det-inv.awp?log=" + token + "&inv=" + codeInv + "&immo=" + codeImmo);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("user", username);
                jsonParam.put("desc", description);
                jsonParam.put("code_barre", codeBarre);
                jsonParam.put("ancien_code", codeRFID);
                jsonParam.put("num_serie", numSerie);
                jsonParam.put("code_etat", codeEtat);
                jsonParam.put("code_emp", codeEmp);
                jsonParam.put("mission", codeMission);
                jsonParam.put("equipe", equipe);
                
                OutputStream os = connection.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.flush();
                os.close();
                
                int responseCode = connection.getResponseCode();
                BufferedReader reader;
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                JSONObject result = new JSONObject(response.toString());
                if ("ok".equals(result.optString("sRes"))) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Immobilisation modifiée avec succès", Toast.LENGTH_LONG).show();
                            dismiss();
                            if (callback != null) callback.onComplete();
                        });
                    }
                } else {
                    final String errorMsg = result.optString("sRes", "Erreur inconnue");
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showProgress(false);
                            Toast.makeText(getContext(), "Erreur: " + errorMsg, Toast.LENGTH_LONG).show();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(getContext(), "Erreur de connexion: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }
}