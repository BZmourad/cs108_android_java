package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.apps.SharedPrefManager;
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
import java.util.List;
import java.util.Locale;
import com.csl.cs108ademoapp.app.db.DatabaseHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.ContentValues;

public class AddImmoFragment extends Fragment {
    private EditText editTextCodeBarre, editTextNumSerie, editTextNote;
    private TextView textViewTag;
    private Spinner spinnerDescription, spinnerEtat, spinnerEmplacement;
    private Button buttonSave;
    private ProgressBar progressBar;
    private String tag;
    private String codeInv, codeMission, equipe;
    private String token;
    private String baseUrl;
    private boolean isOnline;
    
    private List<String> descriptionList = new ArrayList<>();
    private List<String> etatList = new ArrayList<>();
    private List<String> etatCodeList = new ArrayList<>();
    private List<String> emplacementList = new ArrayList<>();
    private List<String> emplacementCodeList = new ArrayList<>();

    public static AddImmoFragment newInstance(String tag, String codeInv, String codeMission, String equipe) {
        AddImmoFragment fragment = new AddImmoFragment();
        Bundle args = new Bundle();
        args.putString("tag", tag);
        args.putString("codeInv", codeInv);
        args.putString("codeMission", codeMission);
        args.putString("equipe", equipe);
        fragment.setArguments(args);
        return fragment;
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
        MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Initialisé avec tag=" + tag + ", mode=" + (isOnline ? "online" : "offline"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_immo, container, false);
        
        textViewTag = view.findViewById(R.id.textViewTag);
        editTextCodeBarre = view.findViewById(R.id.editTextCodeBarre);
        editTextNumSerie = view.findViewById(R.id.editTextNumSerie);
        editTextNote = view.findViewById(R.id.editTextNote);
        spinnerDescription = view.findViewById(R.id.spinnerDescription);
        spinnerEtat = view.findViewById(R.id.spinnerEtat);
        spinnerEmplacement = view.findViewById(R.id.spinnerEmplacement);
        buttonSave = view.findViewById(R.id.buttonSave);
        progressBar = view.findViewById(R.id.progressBar);
        
        // Initialiser avec la valeur du tag
        textViewTag.setText(tag);
        
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImmo();
            }
        });
        
        // Charger les données des spinners
        showProgress(true);
        loadAllData();
        
        return view;
    }
    
    private void loadAllData() {
        if (isOnline) {
            loadDescriptionsOnline();
            loadEtatsOnline();
            loadEmplacementsOnline();
        } else {
            loadDescriptionsOffline();
            loadEtatsOffline();
            loadEmplacementsOffline();
        }
    }
    
    // ==================== OFFLINE METHODS ====================
    
    private void loadDescriptionsOffline() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                    
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                                android.R.layout.simple_spinner_item, descriptionList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerDescription.setAdapter(adapter);
                            checkAllDataLoaded();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Erreur chargement descriptions offline=" + e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Erreur chargement descriptions", Toast.LENGTH_SHORT).show();
                            checkAllDataLoaded();
                        }
                    });
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                    db.close();
                }
            }
        }).start();
    }
    
    private void loadEtatsOffline() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseHelper dbHelper = new DatabaseHelper(getContext());
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                String query = "SELECT * FROM SouSEtat ORDER BY LibelleEtat";
                Cursor cursor = null;
                try {
                    cursor = db.rawQuery(query, null);
                    etatList.clear();
                    etatCodeList.clear();
                    while (cursor.moveToNext()) {
                        // Assuming table has columns: code, LibelleEtat
                        String code = cursor.getString(cursor.getColumnIndexOrThrow("LibelleEtat"));
                        String libelle = cursor.getString(cursor.getColumnIndexOrThrow("DescriptionEtat"));
                        etatList.add(libelle);
                        etatCodeList.add(code);
                    }
                    
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                                android.R.layout.simple_spinner_item, etatList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerEtat.setAdapter(adapter);
                            
                            // Sélectionner "Bien" par défaut si disponible
                            for (int i = 0; i < etatList.size(); i++) {
                                if ("Bien".equals(etatList.get(i))) {
                                    spinnerEtat.setSelection(i);
                                    break;
                                }
                            }
                            checkAllDataLoaded();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Erreur chargement états offline=" + e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Erreur chargement états", Toast.LENGTH_SHORT).show();
                            checkAllDataLoaded();
                        }
                    });
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                    db.close();
                }
            }
        }).start();
    }
    
    private void loadEmplacementsOffline() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseHelper dbHelper = new DatabaseHelper(getContext());
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                String query = "SELECT DISTINCT Inv_EquipeInventaire.CodeEmplacement AS CodeEmplacement, " +
                               "ArbreEmp.path AS Libelle " +
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
                    
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                                android.R.layout.simple_spinner_item, emplacementList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerEmplacement.setAdapter(adapter);
                            checkAllDataLoaded();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Erreur chargement emplacements offline=" + e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Erreur chargement emplacements", Toast.LENGTH_SHORT).show();
                            checkAllDataLoaded();
                        }
                    });
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                    db.close();
                }
            }
        }).start();
    }
    
    private void saveImmoOffline() {
        String codeImmoValue = tag;
        
        if (codeImmoValue.isEmpty()) {
            Toast.makeText(getContext(), "Code Immo non disponible", Toast.LENGTH_SHORT).show();
            showProgress(false);
            return;
        }
        
        if (spinnerDescription.getSelectedItem() == null || spinnerDescription.getSelectedItemPosition() < 0) {
            Toast.makeText(getContext(), "Veuillez sélectionner une description", Toast.LENGTH_SHORT).show();
            showProgress(false);
            return;
        }
        
        if (spinnerEtat.getSelectedItem() == null || spinnerEtat.getSelectedItemPosition() < 0) {
            Toast.makeText(getContext(), "Veuillez sélectionner un état", Toast.LENGTH_SHORT).show();
            showProgress(false);
            return;
        }
        
        if (spinnerEmplacement.getSelectedItem() == null || spinnerEmplacement.getSelectedItemPosition() < 0) {
            Toast.makeText(getContext(), "Veuillez sélectionner un emplacement", Toast.LENGTH_SHORT).show();
            showProgress(false);
            return;
        }
        
        final String codeImmo = codeImmoValue;
        final String description = spinnerDescription.getSelectedItem().toString();
        final String codeBarre = editTextCodeBarre.getText().toString().trim();
        final String numSerie = editTextNumSerie.getText().toString().trim();
        final String note = editTextNote.getText().toString().trim();
        final String codeEtat = etatCodeList.get(spinnerEtat.getSelectedItemPosition());
        final String codeEmp = emplacementCodeList.get(spinnerEmplacement.getSelectedItemPosition());
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseHelper dbHelper = new DatabaseHelper(getContext());
                SQLiteDatabase db = null;
                try {
                    db = dbHelper.getWritableDatabase();
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String dateheurenow = sdf.format(new Date());
                    String userCode = SharedPrefManager.getInstance(getContext()).getUserCode();
                    
                    ContentValues values = new ContentValues();
                    values.put("CodeInventaire", codeInv);
                    values.put("CodeImmo", tag);
                    values.put("Descp_immo", description);
                    values.put("Observation", note);
                    values.put("codeBarre_Corr", codeBarre);
                    values.put("NumSerie_Corr", numSerie);
                    values.put("codeEtat_Corr", codeEtat);
                    values.put("codeEmpInv", codeEmp);
                    values.put("codemission", codeMission);
                    values.put("codeEquipe", equipe);
                    values.put("EtatCorrection", 3);
                    values.put("issync", 0);
                    values.put("nbr_Crt", 1);
                    values.put("nbr_Crt_valid", 1);
                    values.put("nbr_Valid", 0);
                    values.put("DateLecture", dateheurenow);
                    values.put("UserLecture", userCode);
                    
                    MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Tentative insertion avec ContentValues");
                    
                    long result = db.insertOrThrow(DatabaseHelper.TABLE_DET_INV_IMMO, null, values);
                    
                    MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Insertion réussie, result=" + result);
                    
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            Toast.makeText(getContext(), "Immobilisation enregistrée en mode offline", Toast.LENGTH_LONG).show();
                            MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Insertion offline réussie");
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    });
                } catch (android.database.sqlite.SQLiteException e) {
                    e.printStackTrace();
                    String errorMsg = "SQLiteException [" + e.getMessage() + "]: " + e.getMessage();
                    MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: " + errorMsg);
                    final String finalError = errorMsg;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            Toast.makeText(getContext(), "Erreur SQL: " + finalError, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Exception=" + e.getMessage());
                    final String errorMsg = e.getMessage() != null ? e.getMessage() : "Erreur inconnue";
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            Toast.makeText(getContext(), "Erreur: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    if (db != null) {
                        db.close();
                    }
                }
            }
        }).start();
    }
    
    // ==================== ONLINE METHODS (renamed from original) ====================
    
    private void loadDescriptionsOnline() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                        
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                                    android.R.layout.simple_spinner_item, descriptionList);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerDescription.setAdapter(adapter);
                                
                                checkAllDataLoaded();
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Erreur chargement descriptions", Toast.LENGTH_SHORT).show();
                                checkAllDataLoaded();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Erreur chargement descriptions=" + e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Erreur chargement descriptions", Toast.LENGTH_SHORT).show();
                            checkAllDataLoaded();
                        }
                    });
                }
            }
        }).start();
    }
    
    private void loadEtatsOnline() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                        
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                                    android.R.layout.simple_spinner_item, etatList);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerEtat.setAdapter(adapter);
                                
                                // Sélectionner "Bien" par défaut si disponible
                                for (int i = 0; i < etatList.size(); i++) {
                                    if ("Bien".equals(etatList.get(i))) {
                                        spinnerEtat.setSelection(i);
                                        break;
                                    }
                                }
                                checkAllDataLoaded();
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Erreur chargement états", Toast.LENGTH_SHORT).show();
                                checkAllDataLoaded();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Erreur chargement états=" + e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Erreur chargement états", Toast.LENGTH_SHORT).show();
                            checkAllDataLoaded();
                        }
                    });
                }
            }
        }).start();
    }
    
    private void loadEmplacementsOnline() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                            emplacementList.add(obj.getString("sPath"));
                            emplacementCodeList.add(obj.getString("sCode"));
                        }
                        
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                                    android.R.layout.simple_spinner_item, emplacementList);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerEmplacement.setAdapter(adapter);
                                
                                checkAllDataLoaded();
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Erreur chargement emplacements", Toast.LENGTH_SHORT).show();
                                checkAllDataLoaded();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Erreur chargement emplacements=" + e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Erreur chargement emplacements", Toast.LENGTH_SHORT).show();
                            checkAllDataLoaded();
                        }
                    });
                }
            }
        }).start();
    }
    
    private void saveImmoOnline() {
        // Vérifier les champs obligatoires
        String codeImmoValue = tag; // Utiliser le tag comme code_immo
        
        if (codeImmoValue.isEmpty()) {
            Toast.makeText(getContext(), "Code Immo non disponible", Toast.LENGTH_SHORT).show();
            showProgress(false);
            return;
        }
        
        if (spinnerDescription.getSelectedItem() == null || spinnerDescription.getSelectedItemPosition() < 0) {
            Toast.makeText(getContext(), "Veuillez sélectionner une description", Toast.LENGTH_SHORT).show();
            showProgress(false);
            return;
        }
        
        if (spinnerEtat.getSelectedItem() == null || spinnerEtat.getSelectedItemPosition() < 0) {
            Toast.makeText(getContext(), "Veuillez sélectionner un état", Toast.LENGTH_SHORT).show();
            showProgress(false);
            return;
        }
        
        if (spinnerEmplacement.getSelectedItem() == null || spinnerEmplacement.getSelectedItemPosition() < 0) {
            Toast.makeText(getContext(), "Veuillez sélectionner un emplacement", Toast.LENGTH_SHORT).show();
            showProgress(false);
            return;
        }
        
        final String username = SharedPrefManager.getInstance(getContext()).getUsername();
        final String codeImmo = codeImmoValue;
        final String description = spinnerDescription.getSelectedItem().toString();
        final String codeBarre = editTextCodeBarre.getText().toString().trim();
        final String numSerie = editTextNumSerie.getText().toString().trim();
        final String note = editTextNote.getText().toString().trim();
        final String codeEtat = etatCodeList.get(spinnerEtat.getSelectedItemPosition());
        final String codeEmp = emplacementCodeList.get(spinnerEmplacement.getSelectedItemPosition());
        
        MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Sauvegarde immo - code_immo=" + codeImmo + " (tag)");
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(baseUrl + "api-update-immo-mobile.awp?log=" + token + "&inv=" + codeInv);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("user", username);
                    jsonParam.put("code_immo", codeImmo);
                    jsonParam.put("code_art", "");
                    jsonParam.put("desc", description);
                    jsonParam.put("note", note);
                    jsonParam.put("code_barre", codeBarre);
                    jsonParam.put("num_serie", numSerie);
                    jsonParam.put("code_etat", codeEtat);
                    jsonParam.put("codeEmpInv", codeEmp);
                    jsonParam.put("codemission", codeMission);
                    jsonParam.put("codeEquipe", equipe);
                    
                    MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Envoi données: " + jsonParam.toString());
                    
                    OutputStream os = connection.getOutputStream();
                    os.write(jsonParam.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();
                    
                    int responseCode = connection.getResponseCode();
                    MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Response code=" + responseCode);
                    
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
                    
                    MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Réponse=" + response.toString());
                    
                    JSONObject result = new JSONObject(response.toString());
                    if ("ok".equals(result.optString("sRes"))) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Immobilisation ajoutée avec succès", Toast.LENGTH_LONG).show();
                                MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Sauvegarde réussie");
                                showProgress(false);
                                getActivity().getSupportFragmentManager().popBackStack();
                            }
                        });
                    } else {
                        final String errorMsg = result.optString("sRes", "Erreur inconnue");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                Toast.makeText(getContext(), "Erreur: " + errorMsg, Toast.LENGTH_LONG).show();
                                MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Erreur réponse=" + errorMsg);
                            }
                        });
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Exception=" + e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            Toast.makeText(getContext(), "Erreur de connexion: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
    
    private int loadedCount = 0;
    private void checkAllDataLoaded() {
        loadedCount++;
        if (loadedCount >= 3) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgress(false);
                }
            });
        }
    }
    
    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            buttonSave.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonSave.setEnabled(true);
        }
    }
    
    private void saveImmo() {
        showProgress(true);
        buttonSave.setEnabled(false);
        
        if (isOnline) {
            saveImmoOnline();
        } else {
            saveImmoOffline();
        }
    }
}