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

public class AddImmoFragment extends Fragment {
    private EditText editTextCodeImmo, editTextCodeBarre, editTextNumSerie, editTextNote;
    private Spinner spinnerDescription, spinnerEtat, spinnerEmplacement;
    private Button buttonSave;
    private String tag;
    private String codeInv, codeMission, equipe;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_immo, container, false);
        
        editTextCodeImmo = view.findViewById(R.id.editTextCodeImmo);
        editTextCodeBarre = view.findViewById(R.id.editTextCodeBarre);
        editTextNumSerie = view.findViewById(R.id.editTextNumSerie);
        editTextNote = view.findViewById(R.id.editTextNote);
        spinnerDescription = view.findViewById(R.id.spinnerDescription);
        spinnerEtat = view.findViewById(R.id.spinnerEtat);
        spinnerEmplacement = view.findViewById(R.id.spinnerEmplacement);
        buttonSave = view.findViewById(R.id.buttonSave);
        
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImmo();
            }
        });
        
        // Charger les données des spinners
        loadSpinnerData();
        
        return view;
    }
    
    private void loadSpinnerData() {
        // Implémentez le chargement des listes via les API
        // GET pour les descriptions, états et emplacements
    }
    
    private void saveImmo() {
        // Implémentez l'appel API pour sauvegarder
        // GET: baseUrl + "api-update-immo-mobile.awp?log="+token+"&inv="+codeInv
        String username = SharedPrefManager.getInstance(getContext()).getUsername();
        String token = SharedPrefManager.getInstance(getContext()).getToken();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String baseUrl = SharedPrefManager.getInstance(getContext()).getServerUrl();
                    URL url = new URL(baseUrl + "api-update-immo-mobile.awp?log=" + token + "&inv=" + codeInv);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type", "application/octet-stream");
                    connection.setDoOutput(true);
                    
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("user", username);
                    jsonParam.put("code_immo", editTextCodeImmo.getText().toString());
                    jsonParam.put("code_art", "");
                    jsonParam.put("desc", spinnerDescription.getSelectedItem().toString());
                    jsonParam.put("note", editTextNote.getText().toString());
                    jsonParam.put("code_barre", editTextCodeBarre.getText().toString());
                    jsonParam.put("num_serie", editTextNumSerie.getText().toString());
                    jsonParam.put("Code_RFID", tag);
                    jsonParam.put("code_etat", spinnerEtat.getSelectedItem().toString());
                    jsonParam.put("codeEmpInv", spinnerEmplacement.getSelectedItem().toString());
                    jsonParam.put("codemission", codeMission);
                    jsonParam.put("codeEquipe", equipe);
                    
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
                                Toast.makeText(getContext(), "Immobilisation ajoutée avec succès", Toast.LENGTH_SHORT).show();
                                // Rediriger vers EditImmoFragment
                                EditImmoFragment editFragment = EditImmoFragment.newInstance(tag, codeInv, codeMission, equipe);
                                getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.content_frame, editFragment)
                                    .commit();
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