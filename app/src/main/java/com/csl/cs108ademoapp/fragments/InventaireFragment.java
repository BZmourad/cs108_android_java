package com.csl.cs108ademoapp.fragments;

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

import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.apps.SharedPrefManager;
import com.csl.cs108ademoapp.app.db.DatabaseHelper;

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
                    openLectureFragment(item.getCodeInv(), item.getCodeMission(), item.getEquipe());
                }
            });

            return convertView;
        }

        private void openLectureFragment(String codeInv, String codeMission, String equipe) {
            LectureFragment lectureFragment = new LectureFragment();
            // Passer les paramètres via Bundle
            Bundle args = new Bundle();
            args.putString("codeInv", codeInv);
            args.putString("codeMission", codeMission);
            args.putString("equipe", equipe);
            lectureFragment.setArguments(args);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, lectureFragment).addToBackStack(null).commit();
        }

        class ViewHolder {
            TextView textViewCodeInv;
            TextView textViewCodeMission;
            TextView textViewEquipe;
            TextView textViewDescription;
            Button buttonLancer;
        }
    }
}