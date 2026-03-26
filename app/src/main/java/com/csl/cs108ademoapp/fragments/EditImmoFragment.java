package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.apps.SharedPrefManager;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditImmoFragment extends Fragment {
    private EditText editTextCodeBarre, editTextNumSerie, editTextNote;
    private Spinner spinnerEtat, spinnerEmplacement;
    private Button buttonUpdate;
    private String tag;
    private String codeInv, codeMission, equipe;
    private String codeImmo;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_immo, container, false);
        
        editTextCodeBarre = view.findViewById(R.id.editTextCodeBarre);
        editTextNumSerie = view.findViewById(R.id.editTextNumSerie);
        editTextNote = view.findViewById(R.id.editTextNote);
        spinnerEtat = view.findViewById(R.id.spinnerEtat);
        spinnerEmplacement = view.findViewById(R.id.spinnerEmplacement);
        buttonUpdate = view.findViewById(R.id.buttonUpdate);
        
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateImmo();
            }
        });
        
        // Charger les données existantes
        loadImmoDetails();
        // Charger les données des spinners
        loadSpinnerData();
        
        return view;
    }
    
    private void loadImmoDetails() {
        // Implémentez l'appel API pour récupérer les détails
        // GET: baseUrl + "api-inv-search-cod.awp?log="+token+"&inv="+codeInv+"&code="+tag
        String token = SharedPrefManager.getInstance(getContext()).getToken();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String baseUrl = SharedPrefManager.getInstance(getContext()).getServerUrl();
                    URL url = new URL(baseUrl + "api-inv-search-cod.awp?log=" + token + "&inv=" + codeInv + "&code=" + tag);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    
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
                        
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editTextCodeBarre.setText(tabSrch.optString("codeBarre_inv", ""));
                                editTextNumSerie.setText(tabSrch.optString("num_serie_inv", ""));
                                editTextNote.setText("");
                                // Sélectionner les valeurs dans les spinners
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    private void loadSpinnerData() {
        // Implémentez le chargement des listes via les API
        // GET pour les états et emplacements
    }
    
    private void updateImmo() {
        String username = SharedPrefManager.getInstance(getContext()).getUsername();
        String token = SharedPrefManager.getInstance(getContext()).getToken();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String baseUrl = SharedPrefManager.getInstance(getContext()).getServerUrl();
                    URL url = new URL(baseUrl + "api-update-det-inv.awp?log=" + token + "&inv=" + codeInv + "&immo=" + codeImmo);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type", "application/octet-stream");
                    connection.setDoOutput(true);
                    
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("user", username);
                    jsonParam.put("note", editTextNote.getText().toString());
                    jsonParam.put("code_barre", editTextCodeBarre.getText().toString());
                    jsonParam.put("num_serie", editTextNumSerie.getText().toString());
                    jsonParam.put("Code_RFID", tag);
                    jsonParam.put("code_etat", spinnerEtat.getSelectedItem().toString());
                    jsonParam.put("code_emp", spinnerEmplacement.getSelectedItem().toString());
                    jsonParam.put("mission", codeMission);
                    jsonParam.put("equipe", equipe);
                    
                    OutputStream os = connection.getOutputStream();
                    os.write(jsonParam.toString().getBytes());
                    os.flush();
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONObject result = new JSONObject(response.toString());
                    if ("ok".equals(result.getString("sRes"))) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Immobilisation modifiée avec succès", Toast.LENGTH_SHORT).show();
                                getActivity().getSupportFragmentManager().popBackStack();
                            }
                        });
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}