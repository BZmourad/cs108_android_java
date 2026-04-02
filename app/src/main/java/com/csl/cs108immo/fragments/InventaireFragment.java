package com.csl.cs108immo.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.csl.cs108immo.R;
import com.csl.cs108immo.apps.SharedPrefManager;
import com.csl.cs108immo.app.db.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.appcompat.app.AlertDialog;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Filter;
import android.widget.Filterable;
import java.util.Locale;

public class InventaireFragment extends CommonFragment {
    boolean is_online = false;
    public Context ctx;
    private ListView listViewInventaire;
    private InventaireAdapter inventaireAdapter;
    private List<InventaireItem> inventaireList;
    private ExecutorService executorService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        View view = inflater.inflate(R.layout.fragment_inventaire, container, false);

        ctx = getActivity();
        listViewInventaire = view.findViewById(R.id.listViewInventaire);
        inventaireList = new ArrayList<>();
        inventaireAdapter = new InventaireAdapter(getActivity(), inventaireList);
        listViewInventaire.setAdapter(inventaireAdapter);

        executorService = Executors.newSingleThreadExecutor();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Inventaire");

        if (SharedPrefManager.getInstance(ctx).isOnlineMode()) {
            is_online = true;
            loadDataFromAPI();
        } else {
            is_online = false;
            loadDataFromDatabase();
        }
    }

    private void loadDataFromAPI() {
        executorService.execute(() -> {
            try {
                String token = SharedPrefManager.getInstance(ctx).getToken();
                String baseUrl = SharedPrefManager.getInstance(getContext()).getServerUrl();
                String apiUrl = baseUrl + "api-menu-mission.awp?log=" + token;

                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    if (jsonResponse.getString("sRes").equals("ok")) {
                        JSONArray tabMiss = jsonResponse.getJSONArray("tab_miss");
                        List<InventaireItem> newItems = new ArrayList<>();

                        for (int i = 0; i < tabMiss.length(); i++) {
                            JSONObject mission = tabMiss.getJSONObject(i);
                            String codeInv = mission.getString("codeinv");
                            String codeMission = mission.getString("codemission");
                            String description = mission.getString("Description");
                            String equipe = mission.getString("equipe");

                            newItems.add(new InventaireItem(codeInv, codeMission, equipe, description));
                        }

                        getActivity().runOnUiThread(() -> {
                            inventaireList.clear();
                            inventaireList.addAll(newItems);
                            inventaireAdapter.notifyDataSetChanged();
                        });
                    }
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(ctx, "Erreur API: " + responseCode, Toast.LENGTH_SHORT).show());
                }

                connection.disconnect();

            } catch (IOException | JSONException e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(ctx, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadDataFromDatabase() {
        executorService.execute(() -> {
            try {
                DatabaseHelper dbHelper = new DatabaseHelper(ctx);
                String userCode = SharedPrefManager.getInstance(ctx).getUserCode();

                // Exécuter la requête SQL
                String query = "SELECT DISTINCT " +
                        "inv_mission.codemission AS codemission, " +
                        "inv_mission.Codeinv AS codeinv, " +
                        "inv_mission.Description AS Description, " +
                        "Inv_eMombreequipe.codeEquipe AS equipe " +
                        "FROM inv_mission, Inv_eMombreequipe " +
                        "WHERE inv_mission.Codeinv = Inv_eMombreequipe.Codeinv " +
                        "AND inv_mission.etatmission = 'en cours' " +
                        "AND Inv_eMombreequipe.CodeUtil = ?";

                List<InventaireItem> newItems = new ArrayList<>();

                // Note: Vous devrez implémenter cette méthode dans DatabaseHelper
                // ou utiliser rawQuery directement
                android.database.sqlite.SQLiteDatabase db = dbHelper.getReadableDatabase();
                android.database.Cursor cursor = db.rawQuery(query, new String[]{userCode});

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String codeInv = cursor.getString(cursor.getColumnIndexOrThrow("codeinv"));
                        String codeMission = cursor.getString(cursor.getColumnIndexOrThrow("codemission"));
                        String description = cursor.getString(cursor.getColumnIndexOrThrow("Description"));
                        String equipe = cursor.getString(cursor.getColumnIndexOrThrow("equipe"));

                        newItems.add(new InventaireItem(codeInv, codeMission, equipe, description));
                    } while (cursor.moveToNext());

                    cursor.close();
                }

                db.close();

                getActivity().runOnUiThread(() -> {
                    inventaireList.clear();
                    inventaireList.addAll(newItems);
                    inventaireAdapter.notifyDataSetChanged();

                    if (newItems.isEmpty()) {
                        Toast.makeText(ctx, "Aucun inventaire trouvé", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(ctx, "Erreur base de données: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    public InventaireFragment() {
        super("InventaireFragment");
    }

    // Classe pour représenter un item d'inventaire
    public static class InventaireItem {
        private String codeInv;
        private String codeMission;
        private String equipe;
        private String description;

        public InventaireItem(String codeInv, String codeMission, String equipe, String description) {
            this.codeInv = codeInv;
            this.codeMission = codeMission;
            this.equipe = equipe;
            this.description = description;
        }

        // Getters
        public String getCodeInv() { return codeInv; }
        public String getCodeMission() { return codeMission; }
        public String getEquipe() { return equipe; }
        public String getDescription() { return description; }
    }

    // Adapter pour la liste
    private class InventaireAdapter extends BaseAdapter {
        private Context context;
        private List<InventaireItem> items;

        public InventaireAdapter(Context context, List<InventaireItem> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_inventaire, parent, false);
                holder = new ViewHolder();
                holder.textViewCodeInv = convertView.findViewById(R.id.textViewCodeInv);
                holder.textViewCodeMission = convertView.findViewById(R.id.textViewCodeMission);
                holder.textViewEquipe = convertView.findViewById(R.id.textViewEquipe);
                holder.textViewDescription = convertView.findViewById(R.id.textViewDescription);
                holder.buttonLancer = convertView.findViewById(R.id.buttonLancer);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final InventaireItem item = items.get(position);

            holder.textViewCodeInv.setText("Code Inv: " + item.getCodeInv());
            holder.textViewCodeMission.setText("Code Mission: " + item.getCodeMission());
            holder.textViewEquipe.setText("Equipe: " + item.getEquipe());
            holder.textViewDescription.setText("Description: " + item.getDescription());

            holder.buttonLancer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openInventoryFragment(item.getCodeInv(), item.getCodeMission(), item.getEquipe());
                }
            });

            return convertView;
        }

        class ViewHolder {
            TextView textViewCodeInv;
            TextView textViewCodeMission;
            TextView textViewEquipe;
            TextView textViewDescription;
            Button buttonLancer;
        }

        // Remplacer la méthode openInventoryFragment dans InventaireAdapter par :

        private void openInventoryFragment(String codeInv, String codeMission, String equipe) {
            // Créer et afficher le popup de sélection d'emplacement
            showEmplacementPopup(codeInv, codeMission, equipe);
        }

        private void showEmplacementPopup(String codeInv, String codeMission, String equipe) {
            // Créer un dialog personnalisé
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_emplacement_selection, null);
            builder.setView(dialogView);
            builder.setTitle("Sélectionner un emplacement");
            
            ListView listViewEmplacements = dialogView.findViewById(R.id.listViewEmplacements);
            ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
            TextView textViewEmpty = dialogView.findViewById(R.id.textViewEmpty);
            EditText editTextSearch = dialogView.findViewById(R.id.editTextSearch);
            
            AlertDialog dialog = builder.create();
            dialog.show();
            
            // Ajuster la taille du dialog
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );
                dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
            
            // Charger les emplacements
            loadEmplacementsForPopup(context, codeInv, codeMission, equipe, 
                listViewEmplacements, progressBar, textViewEmpty, editTextSearch, dialog, 
                codeInv, codeMission, equipe);
        }

        private void loadEmplacementsForPopup(Context context, String codeInv, String codeMission, 
                                    String equipe, ListView listView, ProgressBar progressBar,
                                    TextView textViewEmpty, EditText editTextSearch, AlertDialog dialog,
                                    String finalCodeInv, String finalCodeMission, String finalEquipe) {
    
            boolean isOnline = SharedPrefManager.getInstance(context).isOnlineMode();
            
            if (isOnline) {
                loadEmplacementsOnlineForPopup(context, codeInv, codeMission, equipe, 
                    listView, progressBar, textViewEmpty, editTextSearch, dialog, 
                    finalCodeInv, finalCodeMission, finalEquipe);
            } else {
                loadEmplacementsOfflineForPopup(context, codeInv, codeMission, equipe, 
                    listView, progressBar, textViewEmpty, editTextSearch, dialog, 
                    finalCodeInv, finalCodeMission, finalEquipe);
            }
        }

        private void loadEmplacementsOnlineForPopup(Context context, String codeInv, String codeMission, 
                                                    String equipe, ListView listView, ProgressBar progressBar,
                                                    TextView textViewEmpty, EditText editTextSearch, AlertDialog dialog,
                                                    String finalCodeInv, String finalCodeMission, String finalEquipe) {
            
            new Thread(() -> {
                try {
                    String token = SharedPrefManager.getInstance(context).getToken();
                    String baseUrl = SharedPrefManager.getInstance(context).getServerUrl();
                    String apiUrl = baseUrl + "api-lecture-empl.awp?log=" + token + 
                        "&inv=" + codeInv + "&mis=" + codeMission + "&eqp=" + equipe;
                    
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    
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
                    
                    String responseStr = response.toString();
                    
                    List<EmplacementItem> emplacements = new ArrayList<>();
                    
                    try {
                        JSONObject result = new JSONObject(responseStr);
                        String sRes = result.getString("sRes");
                        
                        if ("ok".equals(sRes)) {
                            if (result.has("tab_empl")) {
                                JSONArray tabEmpl = result.getJSONArray("tab_empl");
                                
                                for (int i = 0; i < tabEmpl.length(); i++) {
                                    JSONObject obj = tabEmpl.getJSONObject(i);
                                    String code = obj.getString("sCode");
                                    String libelle = obj.getString("sPath");
                                    emplacements.add(new EmplacementItem(code, libelle));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    
                    final List<EmplacementItem> finalEmplacements = emplacements;
                    ((AppCompatActivity) getActivity()).runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        
                        if (finalEmplacements.isEmpty()) {
                            textViewEmpty.setVisibility(View.VISIBLE);
                            textViewEmpty.setText("Aucun emplacement trouvé");
                            editTextSearch.setVisibility(View.GONE);
                        } else {
                            textViewEmpty.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            editTextSearch.setVisibility(View.VISIBLE);
                            
                            EmplacementAdapter adapter = new EmplacementAdapter(context, finalEmplacements);
                            listView.setAdapter(adapter);
                            
                            // Ajouter le TextWatcher pour le filtre
                            editTextSearch.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                                
                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    adapter.getFilter().filter(s);
                                }
                                
                                @Override
                                public void afterTextChanged(Editable s) {}
                            });
                            
                            listView.setOnItemClickListener((parent, view, position, id) -> {
                                EmplacementItem selected = (EmplacementItem) adapter.getItem(position);
                                dialog.dismiss();
                                navigateToInventoryFragment(finalCodeInv, finalCodeMission, finalEquipe, selected.getCode());
                            });
                        }
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    ((AppCompatActivity) getActivity()).runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        textViewEmpty.setVisibility(View.VISIBLE);
                        textViewEmpty.setText("Erreur: " + e.getMessage());
                        editTextSearch.setVisibility(View.GONE);
                        Toast.makeText(context, "Erreur chargement emplacements: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        }

        private void loadEmplacementsOfflineForPopup(Context context, String codeInv, String codeMission, 
                                            String equipe, ListView listView, ProgressBar progressBar,
                                            TextView textViewEmpty, EditText editTextSearch, AlertDialog dialog,
                                            String finalCodeInv, String finalCodeMission, String finalEquipe) {
    
            new Thread(() -> {
                List<EmplacementItem> emplacements = new ArrayList<>();
                
                try {
                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    
                    String query = "SELECT DISTINCT " +
                        "Inv_EquipeInventaire.CodeEmplacement AS CodeEmplacement, " +
                        "ArbreEmp.code AS Libelle " +
                        "FROM Inv_EquipeInventaire, ArbreEmp " +
                        "WHERE Inv_EquipeInventaire.CodeEmplacement = ArbreEmp.code " +
                        "AND ArbreEmp.Idparent <> '#' " +
                        "AND Inv_EquipeInventaire.Codeinv = ? " +
                        "AND Inv_EquipeInventaire.codemission = ? " +
                        "AND Inv_EquipeInventaire.codeEquipe = ?";
                    
                    String[] args = {codeInv, codeMission, equipe};
                    
                    Cursor cursor = db.rawQuery(query, args);
                    
                    while (cursor.moveToNext()) {
                        String code = cursor.getString(0);
                        String libelle = cursor.getString(1);
                        emplacements.add(new EmplacementItem(code, libelle));
                    }
                    cursor.close();
                    db.close();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                final List<EmplacementItem> finalEmplacements = emplacements;
                
                ((AppCompatActivity) getActivity()).runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (finalEmplacements.isEmpty()) {
                        textViewEmpty.setVisibility(View.VISIBLE);
                        textViewEmpty.setText("Aucun emplacement trouvé dans la base");
                        editTextSearch.setVisibility(View.GONE);
                    } else {
                        textViewEmpty.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        editTextSearch.setVisibility(View.VISIBLE);
                        
                        EmplacementAdapter adapter = new EmplacementAdapter(context, finalEmplacements);
                        listView.setAdapter(adapter);
                        
                        // Ajouter le TextWatcher pour le filtre
                        editTextSearch.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                            
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                adapter.getFilter().filter(s);
                            }
                            
                            @Override
                            public void afterTextChanged(Editable s) {}
                        });
                        
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            EmplacementItem selected = (EmplacementItem) adapter.getItem(position);
                            dialog.dismiss();
                            navigateToInventoryFragment(finalCodeInv, finalCodeMission, finalEquipe, selected.getCode());
                        });
                    }
                });
            }).start();
        }

        private void navigateToInventoryFragment(String codeInv, String codeMission, String equipe, String codeEmplacement) {
            InventoryFragment inventoryFragment = new InventoryFragment();
            Bundle args = new Bundle();
            args.putString("codeInv", codeInv);
            args.putString("codeMission", codeMission);
            args.putString("equipe", equipe);
            args.putString("codeEmplacement", codeEmplacement);
            inventoryFragment.setArguments(args);
            
            getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, inventoryFragment)
                .addToBackStack(null)
                .commit();
        }

        // Classe pour les items d'emplacement
        public class EmplacementItem {
            private String code;
            private String libelle;
            
            public EmplacementItem(String code, String libelle) {
                this.code = code;
                this.libelle = libelle;
            }
            
            public String getCode() { return code; }
            public String getLibelle() { return libelle; }
        }

        // Adapter pour la liste des emplacements (avec filtre)
        private class EmplacementAdapter extends BaseAdapter implements Filterable {
            private Context context;
            private List<EmplacementItem> items;
            private List<EmplacementItem> filteredItems;
            private EmplacementFilter emplacementFilter;
            
            public EmplacementAdapter(Context context, List<EmplacementItem> items) {
                this.context = context;
                this.items = items;
                this.filteredItems = new ArrayList<>(items);
            }
            
            @Override
            public int getCount() { 
                return filteredItems != null ? filteredItems.size() : 0; 
            }
            
            @Override
            public Object getItem(int position) { 
                return filteredItems != null ? filteredItems.get(position) : null; 
            }
            
            @Override
            public long getItemId(int position) { 
                return position; 
            }
            
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(context)
                        .inflate(android.R.layout.simple_list_item_2, parent, false);
                }
                
                EmplacementItem item = filteredItems.get(position);
                TextView text1 = convertView.findViewById(android.R.id.text1);
                TextView text2 = convertView.findViewById(android.R.id.text2);
                
                text1.setText(item.getLibelle());
                text2.setText("Code: " + item.getCode());
                text2.setTextSize(12);
                
                return convertView;
            }
            
            @Override
            public Filter getFilter() {
                if (emplacementFilter == null) {
                    emplacementFilter = new EmplacementFilter();
                }
                return emplacementFilter;
            }
            
            private class EmplacementFilter extends Filter {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<EmplacementItem> filteredList = new ArrayList<>();
                    
                    if (constraint == null || constraint.length() == 0) {
                        // Pas de filtre, retourner toute la liste
                        filteredList.addAll(items);
                    } else {
                        String filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim();
                        for (EmplacementItem item : items) {
                            // Rechercher dans le libellé et le code
                            if (item.getLibelle().toLowerCase(Locale.getDefault()).contains(filterPattern) ||
                                item.getCode().toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                                filteredList.add(item);
                            }
                        }
                    }
                    
                    results.values = filteredList;
                    results.count = filteredList.size();
                    return results;
                }
                
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredItems = (List<EmplacementItem>) results.values;
                    notifyDataSetChanged();
                }
            }
            
            public void updateData(List<EmplacementItem> newItems) {
                this.items = newItems;
                this.filteredItems = new ArrayList<>(newItems);
                notifyDataSetChanged();
            }
        }
    }
}