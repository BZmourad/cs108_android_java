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

public class EditImmoFragment extends Fragment {
    private EditText editTextCodeBarre, editTextNumSerie, editTextNote;
    private TextView textViewTag;
    private Spinner spinnerEtat, spinnerEmplacement;
    private Button buttonUpdate;
    private ProgressBar progressBar;
    private String tag;
    private String codeInv, codeMission, equipe;
    private String codeImmo;
    private String token;
    private String baseUrl;
    private boolean isOnline;
    
    private List<String> etatList = new ArrayList<>();
    private List<String> etatCodeList = new ArrayList<>();
    private List<String> emplacementList = new ArrayList<>();
    private List<String> emplacementCodeList = new ArrayList<>();
    
    private String currentEtatCode = "";
    private String currentEmplacementCode = "";
    private String currentCodeBarre = "";
    private String currentNumSerie = "";
    private String currentNote = "";

    public static EditImmoFragment newInstance(String tag, String codeInv, String codeMission, String equipe) {
        EditImmoFragment fragment = new EditImmoFragment();
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
        MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Initialisé avec tag=" + tag + ", mode=" + (isOnline ? "online" : "offline"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_immo, container, false);
        
        textViewTag = view.findViewById(R.id.textViewTag);
        editTextCodeBarre = view.findViewById(R.id.editTextCodeBarre);
        editTextNumSerie = view.findViewById(R.id.editTextNumSerie);
        editTextNote = view.findViewById(R.id.editTextNote);
        spinnerEtat = view.findViewById(R.id.spinnerEtat);
        spinnerEmplacement = view.findViewById(R.id.spinnerEmplacement);
        buttonUpdate = view.findViewById(R.id.buttonUpdate);
        progressBar = view.findViewById(R.id.progressBar);
        
        textViewTag.setText(tag);
        
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateImmo();
            }
        });
        
        showProgress(true);
        loadAllData();
        
        return view;
    }
    
    private void loadAllData() {
        if (isOnline) {
            loadImmoDetailsOnline();
            loadEtatsOnline();
            loadEmplacementsOnline();
        } else {
            loadImmoDetailsOffline();
            loadEtatsOffline();
            loadEmplacementsOffline();
        }
    }
    
    // ==================== OFFLINE METHODS ====================
    
    private void loadImmoDetailsOffline() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseHelper dbHelper = new DatabaseHelper(getContext());
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                // Requête corrigée - retiré ancienCode_Corr qui n'existe pas
                String query = "SELECT " +
                    "CodeInventaire, " +
                    "CodeImmo, " +
                    "Descp_immo, " +
                    "codeArticle, " +
                    "issync, " +
                    "EtatCorrection, " +
                    "empl_Corr, " +
                    "codeEtat_Corr, " +
                    "codeBarre_Corr, " +
                    "codeEmpInv, " +
                    "NumSerie_Corr, " +
                    "Observation " +
                    "FROM Det_inv_immo " +
                    "WHERE CodeInventaire = ? " +
                    "AND (CodeImmo = ? " +
                    "OR NumSerie_Corr = ? " +
                    "OR codeBarre_Corr = ?)";
                    
                String[] args = {codeInv, tag, tag, tag};
                Cursor cursor = null;
                try {
                    cursor = db.rawQuery(query, args);
                    
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment (offline): Nombre de résultats=" + cursor.getCount());
                    
                    if (cursor.moveToFirst()) {
                        codeImmo = cursor.getString(cursor.getColumnIndexOrThrow("CodeImmo"));
                        currentCodeBarre = cursor.getString(cursor.getColumnIndexOrThrow("codeBarre_Corr"));
                        currentNumSerie = cursor.getString(cursor.getColumnIndexOrThrow("NumSerie_Corr"));
                        currentEtatCode = cursor.getString(cursor.getColumnIndexOrThrow("codeEtat_Corr"));
                        currentEmplacementCode = cursor.getString(cursor.getColumnIndexOrThrow("codeEmpInv"));
                        currentNote = cursor.getString(cursor.getColumnIndexOrThrow("Observation"));
                        
                        MainActivity.csLibrary4A.appendToLogView("EditImmoFragment (offline): Détails chargés - code_immo=" + codeImmo);
                        
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editTextCodeBarre.setText(currentCodeBarre != null ? currentCodeBarre : "");
                                editTextNumSerie.setText(currentNumSerie != null ? currentNumSerie : "");
                                editTextNote.setText(currentNote != null ? currentNote : "");
                                checkAllDataLoaded();
                            }
                        });
                    } else {
                        MainActivity.csLibrary4A.appendToLogView("EditImmoFragment (offline): Aucun résultat trouvé pour tag=" + tag);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Immobilisation non trouvée en mode offline", Toast.LENGTH_SHORT).show();
                                getActivity().getSupportFragmentManager().popBackStack();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Erreur chargement détails offline=" + e.getMessage());
                    final String errorMsg = e.getMessage();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Erreur de chargement: " + errorMsg, Toast.LENGTH_LONG).show();
                            getActivity().getSupportFragmentManager().popBackStack();
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
                // Requête corrigée - utiliser les bonnes colonnes
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
                    
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment (offline): Etats chargés - count=" + etatList.size());
                    
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                                android.R.layout.simple_spinner_item, etatList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerEtat.setAdapter(adapter);
                            
                            // Sélectionner l'état actuel
                            for (int i = 0; i < etatCodeList.size(); i++) {
                                if (etatCodeList.get(i).equals(currentEtatCode)) {
                                    spinnerEtat.setSelection(i);
                                    break;
                                }
                            }
                            checkAllDataLoaded();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Erreur chargement états offline=" + e.getMessage());
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
                // Utiliser Inv_EquipeInventaire au lieu de Inv_EquipeInventaire
                
                String query = "SELECT DISTINCT " +
                    "Inv_EquipeInventaire.CodeEmplacement AS CodeEmplacement, " +
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
                    
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment (offline): Emplacements chargés - count=" + emplacementList.size());
                    
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                                android.R.layout.simple_spinner_item, emplacementList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerEmplacement.setAdapter(adapter);
                            
                            // Sélectionner l'emplacement actuel
                            for (int i = 0; i < emplacementCodeList.size(); i++) {
                                if (emplacementCodeList.get(i).equals(currentEmplacementCode)) {
                                    spinnerEmplacement.setSelection(i);
                                    break;
                                }
                            }
                            checkAllDataLoaded();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Erreur chargement emplacements offline=" + e.getMessage());
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
    
    private void updateImmoOffline() {
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
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                    
                    // Construire la requête UPDATE
                    ContentValues values = new ContentValues();
                    values.put("DateLecture", dateheurenow);
                    values.put("UserLecture", userCode);
                    values.put("codeEmpInv", codeEmp);
                    values.put("codemission", codeMission);
                    values.put("codeEquipe", equipe);
                    values.put("codeBarre_Corr", codeBarre);
                    values.put("NumSerie_Corr", numSerie);
                    values.put("Observation", note);
                    values.put("issync", 2);
                    values.put("codeEtat_Corr", codeEtat);
                    
                    // Logique métier pour EtatCorrection
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
                    
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Mise à jour offline - rowsAffected=" + rowsAffected);
                    
                    final int finalRowsAffected = rowsAffected;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            if (finalRowsAffected > 0) {
                                Toast.makeText(getContext(), "Immobilisation modifiée en mode offline", Toast.LENGTH_LONG).show();
                                MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Mise à jour offline réussie");
                                getActivity().getSupportFragmentManager().popBackStack();
                            } else {
                                Toast.makeText(getContext(), "Erreur lors de la modification offline", Toast.LENGTH_LONG).show();
                                MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Erreur mise à jour offline - rows=" + finalRowsAffected);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Exception offline=" + e.getMessage());
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
    
    // ==================== ONLINE METHODS ====================
    
    private void loadImmoDetailsOnline() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                        final String codeBarre = tabSrch.optString("codeBarre_inv", "");
                        final String numSerie = tabSrch.optString("num_serie_inv", "");
                        currentEtatCode = tabSrch.optString("codeEtat_Corr", "");
                        currentEmplacementCode = tabSrch.optString("codeEmpInv", "");
                        currentNote = tabSrch.optString("Observation", "");
                        
                        MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Détails chargés - code_immo=" + codeImmo);
                        
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editTextCodeBarre.setText(codeBarre);
                                editTextNumSerie.setText(numSerie);
                                editTextNote.setText(currentNote);
                                checkAllDataLoaded();
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Impossible de charger les détails", Toast.LENGTH_SHORT).show();
                                getActivity().getSupportFragmentManager().popBackStack();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Erreur chargement détails=" + e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Erreur de chargement", Toast.LENGTH_SHORT).show();
                            getActivity().getSupportFragmentManager().popBackStack();
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
                                
                                for (int i = 0; i < etatCodeList.size(); i++) {
                                    if (etatCodeList.get(i).equals(currentEtatCode)) {
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
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Erreur chargement états=" + e.getMessage());
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
                                
                                for (int i = 0; i < emplacementCodeList.size(); i++) {
                                    if (emplacementCodeList.get(i).equals(currentEmplacementCode)) {
                                        spinnerEmplacement.setSelection(i);
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
                                Toast.makeText(getContext(), "Erreur chargement emplacements", Toast.LENGTH_SHORT).show();
                                checkAllDataLoaded();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Erreur chargement emplacements=" + e.getMessage());
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
    
    private void updateImmoOnline() {
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
        final String codeBarre = editTextCodeBarre.getText().toString().trim();
        final String numSerie = editTextNumSerie.getText().toString().trim();
        final String note = editTextNote.getText().toString().trim();
        final String codeEtat = etatCodeList.get(spinnerEtat.getSelectedItemPosition());
        final String codeEmp = emplacementCodeList.get(spinnerEmplacement.getSelectedItemPosition());
        
        MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Mise à jour immo - code_immo=" + codeImmo);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                    jsonParam.put("note", note);
                    jsonParam.put("code_barre", codeBarre);
                    jsonParam.put("num_serie", numSerie);
                    jsonParam.put("code_etat", codeEtat);
                    jsonParam.put("code_emp", codeEmp);
                    jsonParam.put("mission", codeMission);
                    jsonParam.put("equipe", equipe);
                    
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Envoi données: " + jsonParam.toString());
                    
                    OutputStream os = connection.getOutputStream();
                    os.write(jsonParam.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();
                    
                    int responseCode = connection.getResponseCode();
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Response code=" + responseCode);
                    
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
                    
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Réponse=" + response.toString());
                    
                    JSONObject result = new JSONObject(response.toString());
                    if ("ok".equals(result.optString("sRes"))) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Immobilisation modifiée avec succès", Toast.LENGTH_LONG).show();
                                MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Mise à jour réussie");
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
                                MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Erreur réponse=" + errorMsg);
                            }
                        });
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.csLibrary4A.appendToLogView("EditImmoFragment: Exception=" + e.getMessage());
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
            buttonUpdate.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonUpdate.setEnabled(true);
        }
    }
    
    private void updateImmo() {
        showProgress(true);
        buttonUpdate.setEnabled(false);
        
        if (isOnline) {
            updateImmoOnline();
        } else {
            updateImmoOffline();
        }
    }
}