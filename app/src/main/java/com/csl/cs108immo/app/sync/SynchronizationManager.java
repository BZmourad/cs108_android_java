package com.csl.cs108immo.app.sync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.csl.cs108immo.app.db.DatabaseHelper;
import com.csl.cs108immo.app.db.DatabaseCleaner;
import com.csl.cs108immo.app.db.models.*;
import com.csl.cs108immo.apps.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SynchronizationManager {
    private static SynchronizationManager instance;
    private Context context;
    private DatabaseHelper dbHelper;
    private SyncListener listener;
    private String token;
    private String baseUrl;
    private boolean isCancelled = false;
    
    public interface SyncListener {
        void onProgressUpdate(int progress, String libelle);
        void onComplete(boolean success, String message);
    }
    
    private SynchronizationManager(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.token = SharedPrefManager.getInstance(context).getToken();
        this.baseUrl = SharedPrefManager.getInstance(context).getServerUrl();
    }
    
    public static synchronized SynchronizationManager getInstance(Context context) {
        if (instance == null) {
            instance = new SynchronizationManager(context);
        }
        return instance;
    }
    
    public void synchronize(SyncListener listener) {
        this.listener = listener;
        isCancelled = false;
        new SyncTask().execute();
    }
    
    private class SyncTask extends AsyncTask<Void, Integer, Boolean> {
        private String errorMessage = "";
        private int currentProgress = 0;
        private String currentLibelle = "";
        
        @Override
        protected void onPreExecute() {
            if (listener != null) {
                listener.onProgressUpdate(0, "Démarrage de la synchronisation...");
            }
        }
        
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Post historique
                currentLibelle = "Historique inv";
                publishProgress(currentProgress);
                if (!postHisto()) {
                    errorMessage = "Synchronisation échouée au niveau envoyer historique, veuillez réessayer";
                    return false;
                }
                currentProgress += 10;
                currentLibelle = "Historique inv terminé";
                publishProgress(currentProgress);
                
                // Post détails inv
                currentLibelle = "Détails inv";
                publishProgress(currentProgress);
                if (!postInvDet()) {
                    errorMessage = "Synchronisation échouée au niveau envoyer Détails inventaire, veuillez réessayer";
                    return false;
                }
                currentProgress += 10;
                currentLibelle = "Détails inv terminé";
                publishProgress(currentProgress);
                
                // Post immo valeurs
                currentLibelle = "Inv immo valeurs";
                publishProgress(currentProgress);
                if (!postInvImmoValeur()) {
                    errorMessage = "Synchronisation échouée au niveau envoyer valeurs attributs immo inv, veuillez réessayer";
                    return false;
                }
                currentProgress += 10;
                currentLibelle = "Inv immo valeurs terminé";
                publishProgress(currentProgress);
                
                // Suppression des données locales
                currentLibelle = "Suppression des données locales";
                publishProgress(currentProgress);
                if (!clearAllLocalData()) {
                    errorMessage = "Erreur lors de la suppression des données locales";
                    return false;
                }
                currentProgress += 10;
                currentLibelle = "Suppression terminée";
                publishProgress(currentProgress);
                
                // Récupération des données
                currentLibelle = "Equipes inv";
                publishProgress(currentProgress);
                if (!getAllEquip()) return false;
                currentProgress += 5;
                currentLibelle = "Equipes inv terminé";
                publishProgress(currentProgress);
                
                currentLibelle = "Membres équipes inv";
                publishProgress(currentProgress);
                if (!getAllEquipeMembre()) return false;
                currentProgress += 5;
                currentLibelle = "Membres équipes inv terminé";
                publishProgress(currentProgress);
                
                currentLibelle = "Inventaire";
                publishProgress(currentProgress);
                if (!getAllInv()) return false;
                currentProgress += 10;
                currentLibelle = "Inventaire terminé";
                publishProgress(currentProgress);
                
                currentLibelle = "Missions inv";
                publishProgress(currentProgress);
                if (!getAllMissionInv()) return false;
                currentProgress += 5;
                currentLibelle = "Missions inv terminé";
                publishProgress(currentProgress);
                
                currentLibelle = "Attribut inventaire";
                publishProgress(currentProgress);
                if (!getAllInvAttributeParam()) return false;
                currentProgress += 5;
                currentLibelle = "Attribut inventaire terminé";
                publishProgress(currentProgress);
                
                currentLibelle = "Emplacement inventaire";
                publishProgress(currentProgress);
                if (!getAllEquipEmpl()) return false;
                currentProgress += 5;
                currentLibelle = "Emplacement inventaire terminé";
                publishProgress(currentProgress);
                
                currentLibelle = "Etats";
                publishProgress(currentProgress);
                if (!getAllEtat()) return false;
                currentProgress += 5;
                currentLibelle = "Etats terminé";
                publishProgress(currentProgress);
                
                currentLibelle = "Employés";
                publishProgress(currentProgress);
                if (!getInvEmp()) return false;
                currentProgress += 5;
                currentLibelle = "Employés terminé";
                publishProgress(currentProgress);
                
                currentLibelle = "Liste attributs";
                publishProgress(currentProgress);
                if (!getInvAtt()) return false;
                currentProgress += 5;
                currentLibelle = "Liste attributs terminé";
                publishProgress(currentProgress);
                
                currentLibelle = "Liste valeurs attributs";
                publishProgress(currentProgress);
                if (!getAttValr()) return false;
                currentProgress += 5;
                currentLibelle = "Liste valeurs attributs terminé";
                publishProgress(currentProgress);
                
                currentLibelle = "Attributs d'attribut set";
                publishProgress(currentProgress);
                if (!getAttributeSet()) return false;
                currentProgress += 5;
                currentLibelle = "Attributs d'attribut set terminé";
                publishProgress(currentProgress);
                
                currentLibelle = "Utilisateurs";
                publishProgress(currentProgress);
                if (!getAllUser()) return false;
                currentProgress += 5;
                currentLibelle = "Utilisateurs terminé";
                publishProgress(currentProgress);
                
                currentLibelle = "Emplacements";
                publishProgress(currentProgress);
                if (!getAllEmplacements()) return false;
                currentProgress += 5;
                currentLibelle = "Emplacements terminé";
                publishProgress(currentProgress);
                
                currentLibelle = "Détails inv valeurs";
                publishProgress(currentProgress);
                if (!getDetInvValImmo()) return false;
                
                currentProgress = 100;
                currentLibelle = "Synchronisation terminée";
                publishProgress(currentProgress);
                return true;
                
            } catch (Exception e) {
                errorMessage = "Erreur: " + e.getMessage();
                Log.e("Sync", "Erreur", e);
                return false;
            }
        }
        
        @Override
        protected void onProgressUpdate(Integer... values) {
            if (listener != null && values.length > 0) {
                listener.onProgressUpdate(values[0], currentLibelle);
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            if (listener != null) {
                String message = success ? "Synchronisation effectuée avec succès" : errorMessage;
                listener.onComplete(success, message);
            }
        }
        

        private String getProgressMessage(int progressCode) {
            // Cette méthode sera appelée avec les libellés
            return "Synchronisation...";
        }
        

        private boolean clearAllLocalData() {
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String[] tables = {
                    DatabaseHelper.TABLE_ARBRE_EMP,
                    DatabaseHelper.TABLE_PROPRIETAIRE,
                    DatabaseHelper.TABLE_EMPLOYEE,
                    DatabaseHelper.TABLE_FOURNISSEUR,
                    DatabaseHelper.TABLE_SOUS_ETAT,
                    DatabaseHelper.TABLE_ATTRIBUTS,
                    DatabaseHelper.TABLE_VALEURS_ATTRIBUT,
                    DatabaseHelper.TABLE_ATTRIBUTS_ATTBSET,
                    DatabaseHelper.TABLE_UTILISATEUR,
                    DatabaseHelper.TABLE_IMMO,
                    DatabaseHelper.TABLE_INV_IMMO,
                    DatabaseHelper.TABLE_INV_ATTRIBUTINV_IMMO,
                    DatabaseHelper.TABLE_INV_EQUIPE,
                    DatabaseHelper.TABLE_INV_EMOMBREEQUIPE,
                    DatabaseHelper.TABLE_INV_MISSION,
                    DatabaseHelper.TABLE_DET_INV_IMMO,
                    DatabaseHelper.TABLE_HISTORIQUE_ECAR_INV,
                    DatabaseHelper.TABLE_IMMO_INV_IMAGE,
                    DatabaseHelper.TABLE_INV_IMMO_ATTRIBUT_VALR
                };
                
                db.execSQL("PRAGMA foreign_keys=OFF;");
                for (String table : tables) {
                    db.delete(table, null, null);
                }
                db.execSQL("PRAGMA foreign_keys=ON;");
                db.close();
                return true;
            } catch (Exception e) {
                Log.e("Sync", "Erreur suppression", e);
                return false;
            }
        }
        
        private String httpPost(String endpoint, JSONObject data) {
            HttpURLConnection connection = null;
            try {
                String urlString = baseUrl + endpoint + "?log=" + URLEncoder.encode(token, "UTF-8");
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(data.toString());
                out.flush();
                out.close();
                
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
                    return jsonResponse.optString("sRes", "");
                }
                return null;
            } catch (Exception e) {
                Log.e("Sync", "Erreur HTTP", e);
                return null;
            } finally {
                if (connection != null) connection.disconnect();
            }
        }
        
        private String httpGet(String endpoint) {
            HttpURLConnection connection = null;
            try {
                String urlString = baseUrl + endpoint + "?log=" + URLEncoder.encode(token, "UTF-8");
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                }
                return null;
            } catch (Exception e) {
                Log.e("Sync", "Erreur HTTP GET", e);
                return null;
            } finally {
                if (connection != null) connection.disconnect();
            }
        }
        
        // Méthodes GET simplifiées (à implémenter de manière similaire)
        private boolean getAllEquip() { return executeGetAndInsert("api-get-all-equipe.awp", "tab_equipe", DatabaseHelper.TABLE_INV_EQUIPE); }
        private boolean getAllEquipeMembre() { return executeGetAndInsert("api-get-all-equipe-membre.awp", "tab_equipe", DatabaseHelper.TABLE_INV_EMOMBREEQUIPE); }
        private boolean getAllInv() { return executeGetAndInsert("api-get-all-inv.awp", "tabInv", DatabaseHelper.TABLE_INV_IMMO); }
        private boolean getAllMissionInv() { return executeGetAndInsert("api-get-all-MissionInv.awp", "tab_mission", DatabaseHelper.TABLE_INV_MISSION); }
        private boolean getAllInvAttributeParam() { return executeGetAndInsert("api-get-inv-attribute-param.awp", "tab_inv_att", DatabaseHelper.TABLE_INV_ATTRIBUTINV_IMMO); }
        private boolean getAllEquipEmpl() { return executeGetAndInsert("api-get-all-equipe-emplacement.awp", "tab_equipe", DatabaseHelper.TABLE_INV_EQUIPE_INVENTAIRE); }
        private boolean getAllEtat() { return executeGetAndInsert("api-get-etat.awp", "tab_etat", DatabaseHelper.TABLE_SOUS_ETAT); }
        private boolean getInvEmp() { return executeGetAndInsert("api-get-empyes.awp", "tab_empyes", DatabaseHelper.TABLE_EMPLOYEE); }
        private boolean getInvAtt() { return executeGetAndInsert("api-get-invAtt.awp", "tab_att", DatabaseHelper.TABLE_ATTRIBUTS); }
        private boolean getAttValr() { return executeGetAndInsert("api-get-AttValeur.awp", "tab_attValr", DatabaseHelper.TABLE_VALEURS_ATTRIBUT); }
        private boolean getAttributeSet() { return executeGetAndInsert("api-get-AttSet.awp", "tab_att", DatabaseHelper.TABLE_ATTRIBUTS_ATTBSET); }
        private boolean getAllUser() { return executeGetAndInsert("api-get-all-user.awp", "tabUtlstr", DatabaseHelper.TABLE_UTILISATEUR); }
        private boolean getAllEmplacements() { return executeGetAndInsert("api-get-all-emplacement.awp", "tabRetoure", DatabaseHelper.TABLE_ARBRE_EMP); }
        
        private boolean getDetInvValImmo() {
            // Cette méthode nécessite une implémentation plus complexe car elle fait des appels paginés
            // Pour simplifier, on appelle une version simplifiée
            return executeGetAndInsert("api-get-inv-det.awp", "tab_det_inv", DatabaseHelper.TABLE_DET_INV_IMMO);
        }

        // Méthode utilitaire pour obtenir une valeur de colonne en toute sécurité
        private String getColumnString(Cursor cursor, String columnName) {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex >= 0) {
                return cursor.getString(columnIndex);
            }
            return "";
        }
        
        private int getColumnInt(Cursor cursor, String columnName) {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex >= 0) {
                return cursor.getInt(columnIndex);
            }
            return 0;
        }
        
        // Implémentation corrigée de postHisto()
        private boolean postHisto() {
            try {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.query(DatabaseHelper.TABLE_HISTORIQUE_ECAR_INV, null, null, null, null, null, null);
                
                if (cursor.getCount() == 0) {
                    cursor.close();
                    return true;
                }
                
                JSONObject data = new JSONObject();
                JSONArray items = new JSONArray();
                
                while (cursor.moveToNext()) {
                    JSONObject item = new JSONObject();
                    item.put("code_att", getColumnString(cursor, "code_att"));
                    item.put("code_att_inv", getColumnString(cursor, "code_att_inv"));
                    item.put("code_att_th", getColumnString(cursor, "code_att_th"));
                    item.put("Code_immo", getColumnString(cursor, "Code_immo"));
                    item.put("code_Inv", getColumnString(cursor, "code_Inv"));
                    item.put("dat", getColumnString(cursor, "DateValidation"));
                    item.put("user", getColumnString(cursor, "Uservalidation"));
                    items.put(item);
                }
                cursor.close();
                
                data.put("items", items);
                String response = httpPost("api-post-ecar-inv.awp", data);
                return "ok".equals(response);
            } catch (Exception e) {
                Log.e("Sync", "Erreur postHisto", e);
                return false;
            }
        }
        
        // Implémentation corrigée de postInvDet()
        private boolean postInvDet() {
            try {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.query(DatabaseHelper.TABLE_DET_INV_IMMO, 
                    null, null, null, null, null, null);
                
                if (cursor.getCount() == 0) {
                    cursor.close();
                    return true;
                }
                
                JSONObject data = new JSONObject();
                JSONArray items = new JSONArray();
                
                while (cursor.moveToNext()) {
                    JSONObject item = new JSONObject();
                    item.put("code_Article", getColumnString(cursor, "codeArticle"));
                    item.put("Descp_immo", getColumnString(cursor, "Descp_immo"));
                    item.put("codeImmo", getColumnString(cursor, "CodeImmo"));
                    item.put("codeInv", getColumnString(cursor, "CodeInventaire"));
                    item.put("nbr_Crt", getColumnInt(cursor, "nbr_Crt"));
                    item.put("nbr_Crt_val", getColumnInt(cursor, "nbr_Crt_valid"));
                    item.put("nbr_Val", getColumnInt(cursor, "nbr_Valid"));
                    item.put("codeEquipe", getColumnString(cursor, "codeEquipe"));
                    item.put("codemission", getColumnString(cursor, "codemission"));
                    item.put("codeEmpInv", getColumnString(cursor, "codeEmpInv"));
                    item.put("dat", getColumnString(cursor, "DateLecture"));
                    item.put("User", getColumnString(cursor, "UserLecture"));
                    item.put("etatCorr", getColumnInt(cursor, "EtatCorrection"));
                    item.put("note", getColumnString(cursor, "Observation"));
                    items.put(item);
                }
                cursor.close();
                
                data.put("items", items);
                String response = httpPost("api-post-inv.awp", data);
                return "ok".equals(response);
            } catch (Exception e) {
                Log.e("Sync", "Erreur postInvDet", e);
                return false;
            }
        }
        
        // Implémentation corrigée de postInvImmoValeur()
        private boolean postInvImmoValeur() {
            try {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.query(DatabaseHelper.TABLE_INV_IMMO_ATTRIBUT_VALR,
                    null, null, null, null, null, null);
                
                if (cursor.getCount() == 0) {
                    cursor.close();
                    return true;
                }
                
                JSONObject data = new JSONObject();
                JSONArray items = new JSONArray();
                
                while (cursor.moveToNext()) {
                    JSONObject item = new JSONObject();
                    item.put("codeAtt", getColumnString(cursor, "codeAttrib"));
                    item.put("codeImmo", getColumnString(cursor, "codeImmo"));
                    item.put("codeInv", getColumnString(cursor, "codeInv"));
                    item.put("valr", getColumnString(cursor, "valr"));
                    items.put(item);
                }
                cursor.close();
                
                data.put("items", items);
                String response = httpPost("api-post-invimmoValeur.awp", data);
                return "ok".equals(response);
            } catch (Exception e) {
                Log.e("Sync", "Erreur postInvImmoValeur", e);
                return false;
            }
        }
        
        // Méthode générique améliorée pour les requêtes GET
        private boolean executeGetAndInsert(String endpoint, String arrayKey, String tableName) {
            try {
                String response = httpGet(endpoint);
                if (response == null) return false;
                
                JSONObject json = new JSONObject(response);
                if (!"ok".equals(json.optString("sRes"))) return false;
                
                JSONArray items = json.optJSONArray(arrayKey);
                if (items == null || items.length() == 0) return true;
                
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                
                try {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        ContentValues values = new ContentValues();
                        
                        // Remplir les valeurs selon le nom de la table
                        switch (tableName) {
                            case DatabaseHelper.TABLE_INV_EQUIPE:
                                values.put("codeEquipe", item.optString("sCode"));
                                values.put("NoEquipe", item.optString("sNom"));
                                values.put("chefEquipe", item.optString("sChefEquipe"));
                                values.put("Codeinv", item.optString("sCodeInv"));
                                values.put("UserCreation", item.optString("sUser"));
                                values.put("DateCreation", item.optString("datecre"));
                                break;
                                
                            case DatabaseHelper.TABLE_INV_EMOMBREEQUIPE:
                                values.put("CodeUtil", item.optString("sUser"));
                                values.put("codeEquipe", item.optString("sCode"));
                                values.put("Codeinv", item.optString("sCodeInv"));
                                values.put("NOM", item.optString("sNom"));
                                break;
                                
                            case DatabaseHelper.TABLE_INV_IMMO:
                                values.put("CodeInventaire", item.optString("sCode"));
                                values.put("DateCreation", item.optString("datecre"));
                                values.put("EtatInventaire", item.optInt("sEtat", 0));
                                values.put("Description", item.optString("desc"));
                                values.put("verif_art_inv", item.optInt("verif_art", 0));
                                values.put("affich_art_inv", item.optInt("affich_art", 0));
                                values.put("multi_eq_mis", item.optInt("multi_eq_mis", 0));
                                break;
                                
                            case DatabaseHelper.TABLE_INV_MISSION:
                                values.put("codemission", item.optString("sCodeMis"));
                                values.put("Codeinv", item.optString("sCodeInv"));
                                values.put("Description", item.optString("sDesc"));
                                values.put("etatmission", item.optString("sEtat"));
                                values.put("DateCree", item.optString("sDateCre"));
                                values.put("UserCreation", item.optString("sUserCre"));
                                values.put("DateMaj", item.optString("sDateMaj"));
                                values.put("usermaj", item.optString("sUserMaj"));
                                break;
                                
                            case DatabaseHelper.TABLE_INV_ATTRIBUTINV_IMMO:
                                values.put("codeatribut", item.optString("sCode"));
                                values.put("Codeinv", item.optString("sCodeInv"));
                                values.put("name", item.optString("sNom"));
                                values.put("afficher", item.optInt("bAffiche", 0));
                                values.put("saisie", item.optInt("bSaisir", 0));
                                values.put("validation", item.optInt("bValidation", 0));
                                break;
                                
                            case DatabaseHelper.TABLE_INV_EQUIPE_INVENTAIRE:
                                values.put("CodeEmplacement", item.optString("sCodeEmp"));
                                values.put("codeEquipe", item.optString("sCode"));
                                values.put("codemission", item.optString("sCodeMis"));
                                values.put("Codeinv", item.optString("sCodeInv"));
                                break;
                                
                            case DatabaseHelper.TABLE_SOUS_ETAT:
                                values.put("coler", item.optString("sColor"));
                                values.put("sousEtat", item.optInt("nEtat", 0));
                                values.put("TypeEtat", item.optString("sType"));
                                values.put("DescriptionEtat", item.optString("sDesc"));
                                values.put("LibelleEtat", item.optString("sLib"));
                                break;
                                
                            case DatabaseHelper.TABLE_EMPLOYEE:
                                values.put("CodeEmployee", item.optString("sCode"));
                                values.put("CodeFunction", item.optString("sCodeFunction"));
                                values.put("CodeGroup", item.optString("sCodeGroup"));
                                values.put("DateCre", item.optString("sDateCre"));
                                values.put("DateMaj", item.optString("sDateMaj"));
                                values.put("EmailPerso", item.optString("sEmailPerso"));
                                values.put("EmailWork", item.optString("sEmailWork"));
                                values.put("NomPrenom", item.optString("sNomPrenom"));
                                values.put("NumPerso", item.optLong("sNumPerso", 0));
                                values.put("NumTravail", item.optLong("sNumTravail", 0));
                                values.put("Addresse", item.optString("sAdres"));
                                values.put("ETAT", item.optInt("sEtat", 0));
                                values.put("UserCreation", item.optString("sUser"));
                                break;
                                
                            case DatabaseHelper.TABLE_ATTRIBUTS:
                                values.put("libCode", item.optString("sLib"));
                                values.put("isAffiche", item.optInt("bIsAffiche", 0));
                                values.put("isInconnu", item.optInt("bIsInconnu", 0));
                                values.put("obligatoire", item.optInt("bIsOblig", 0));
                                values.put("valeurDéfaut", item.optString("sValr"));
                                values.put("type", item.optString("sType"));
                                break;
                                
                            case DatabaseHelper.TABLE_VALEURS_ATTRIBUT:
                                values.put("codeAttribut", item.optString("sCodeAtt"));
                                values.put("Valeur", item.optString("sValr"));
                                values.put("isdefault", item.optInt("bIsdefault", 0));
                                break;
                                
                            case DatabaseHelper.TABLE_ATTRIBUTS_ATTBSET:
                                values.put("codeAttribus", item.optString("sCodeAtt"));
                                values.put("codeAttribusSet", item.optString("sCode"));
                                break;
                                
                            case DatabaseHelper.TABLE_UTILISATEUR:
                                values.put("CodeUtil", item.optString("sCode"));
                                values.put("NomPrenomUtil", item.optString("sNom"));
                                values.put("MotPasse", item.optString("sPass"));
                                values.put("login", item.optString("sLogin"));
                                break;
                                
                            case DatabaseHelper.TABLE_ARBRE_EMP:
                                values.put("code", item.optString("sCode"));
                                values.put("Libelle", item.optString("sNom"));
                                values.put("Idparent", item.optString("sCodeParnt"));
                                values.put("path", item.optString("sPath"));
                                values.put("n1", item.optString("n1"));
                                values.put("n2", item.optString("n2"));
                                values.put("n3", item.optString("n3"));
                                values.put("n4", item.optString("n4"));
                                values.put("n5", item.optString("n5"));
                                values.put("n6", item.optString("n6"));
                                values.put("n7", item.optString("n7"));
                                values.put("n8", item.optString("n8"));
                                values.put("n9", item.optString("n9"));
                                break;
                                
                            case DatabaseHelper.TABLE_DET_INV_IMMO:
                                values.put("CodeInventaire", item.optString("sCodeInv"));
                                values.put("CodeImmo", item.optString("sCodeImmo"));
                                values.put("DateLecture", item.optString("dateLu"));
                                values.put("DateValidation", item.optString("dateVal"));
                                values.put("UserLecture", item.optString("userLu"));
                                values.put("Uservalidation", item.optString("userVal"));
                                values.put("codeArticle", item.optString("sCodeArticle"));
                                values.put("codeBarre_Corr", item.optString("codeBarr_inv"));
                                values.put("codeEmpCor", item.optString("codeEmpCor"));
                                values.put("codeEmpInv", item.optString("codeEmpInv"));
                                values.put("CodeEmpTh", item.optString("CodeEmpTh"));
                                values.put("codeEquipe", item.optString("sCodeEquipe"));
                                values.put("codemission", item.optString("sCodeMis"));
                                values.put("empl_Corr", item.optString("empl_inv"));
                                values.put("EtatCorrection", item.optInt("etatCorr", 0));
                                values.put("NumSerie_Corr", item.optString("nSerie_inv"));
                                values.put("RFIDCode_Corr", item.optString("rfid_inv"));
                                values.put("codeEtat_Corr", item.optString("statue_inv"));
                                values.put("nbr_Crt", item.optInt("nbr_Crt", 0));
                                values.put("nbr_Crt_valid", item.optInt("nbr_Crt_val", 0));
                                values.put("nbr_Valid", item.optInt("nbr_Val", 0));
                                values.put("Descp_immo", item.optString("sDesc"));
                                values.put("issync", item.optInt("bIssync", 0));
                                values.put("Observation", item.optString("note"));
                                break;
                        }
                        
                        // Insérer ou remplacer
                        long result = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                        if (result == -1) {
                            Log.e("Sync", "Erreur insertion dans " + tableName);
                        }
                    }
                    
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                    db.close();
                }
                
                return true;
            } catch (Exception e) {
                Log.e("Sync", "Erreur GET " + endpoint, e);
                return false;
            }
        }
    }
}