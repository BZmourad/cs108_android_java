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
import java.util.ArrayList;
import java.util.List;

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
        token = SharedPrefManager.getInstance(getContext()).getToken();
        baseUrl = SharedPrefManager.getInstance(getContext()).getServerUrl();
        MainActivity.csLibrary4A.appendToLogView("AddImmoFragment: Initialisé avec tag=" + tag);
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
        loadDescriptions();
        loadEtats();
        loadEmplacements();
    }
    
    private void loadDescriptions() {
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
    
    private void loadEtats() {
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
    
    private void loadEmplacements() {
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
                            emplacementList.add(obj.getString("sNom"));
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
        // Vérifier les champs obligatoires
        String codeImmoValue = tag; // Utiliser le tag comme code_immo
        
        if (codeImmoValue.isEmpty()) {
            Toast.makeText(getContext(), "Code Immo non disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (spinnerDescription.getSelectedItem() == null || spinnerDescription.getSelectedItemPosition() < 0) {
            Toast.makeText(getContext(), "Veuillez sélectionner une description", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (spinnerEtat.getSelectedItem() == null || spinnerEtat.getSelectedItemPosition() < 0) {
            Toast.makeText(getContext(), "Veuillez sélectionner un état", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (spinnerEmplacement.getSelectedItem() == null || spinnerEmplacement.getSelectedItemPosition() < 0) {
            Toast.makeText(getContext(), "Veuillez sélectionner un emplacement", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showProgress(true);
        buttonSave.setEnabled(false);
        
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
                    jsonParam.put("code_immo", codeImmo);  // Utiliser le tag comme code_immo
                    jsonParam.put("code_art", "");
                    jsonParam.put("desc", description);
                    jsonParam.put("note", note);
                    jsonParam.put("code_barre", codeBarre);
                    jsonParam.put("num_serie", numSerie);
                    jsonParam.put("code_etat", codeEtat);
                    jsonParam.put("codeEmpInv", codeEmp);
                    jsonParam.put("codemission", codeMission);
                    jsonParam.put("codeEquipe", equipe);
                    // Note: Code_RFID n'est plus envoyé
                    
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
                                // Retour au fragment précédent
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
}