package com.csl.cs108ademoapp.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.csl.cs108ademoapp.database.DatabaseHelper;
import com.csl.cs108ademoapp.database.models.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SynchronizationController {
    private static final String TAG = "SynchronizationController";
    private static final String BASE_URL = "http://192.168.100.50/GESTION_INV_CNAM_WEB/FR/";
    
    private Context context;
    private DatabaseHelper dbHelper;
    private String gsToken;
    
    public SynchronizationController(Context context, String gsToken) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.gsToken = gsToken;
    }
    
    // Exceptions personnalisées
    public class SynchronizationException extends Exception {
        public SynchronizationException(String message) {
            super(message);
        }
        
        public SynchronizationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public class NetworkException extends SynchronizationException {
        public NetworkException(String message) {
            super(message);
        }
        
        public NetworkException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public class DataParseException extends SynchronizationException {
        public DataParseException(String message) {
            super(message);
        }
        
        public DataParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    // Méthode utilitaire pour les appels API
    private JSONObject executeApiCall(String apiUrl) throws SynchronizationException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        
        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new NetworkException("HTTP error code: " + responseCode);
            }
            
            InputStream inputStream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            return new JSONObject(response.toString());
            
        } catch (IOException e) {
            throw new NetworkException("Network error during API call", e);
        } catch (JSONException e) {
            throw new DataParseException("Error parsing JSON response", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing reader", e);
                }
            }
        }
    }
    
    // Méthode utilitaire pour convertir les dates
    private Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return format.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + dateString, e);
            return null;
        }
    }
    
    // 1. Vider les tables
    public void clearTables() throws SynchronizationException {
        try {
            String[] tables = {
                "ArbreEmp", "Proprietaire", "Employee", "FOURNISSEUR", "SouSEtat", 
                "Attributs", "valeursAttribut", "Attributs_attbset", "Utilisateur", 
                "Immo", "inv_immo", "Inv_attributinvImmo", "Inv_equipe", 
                "Inv_EquipeInventaire", "inv_mission", "Inv_eMombreequipe", 
                "historique_ecar_inv", "Det_inv_immo", "immo_inv_image", "InvImmoAttributValr"
            };
            
            for (String table : tables) {
                dbHelper.delete(table, null, null);
            }
            Log.i(TAG, "All tables cleared successfully");
            
        } catch (Exception e) {
            throw new SynchronizationException("Error clearing tables", e);
        }
    }
    
    // 2. Synchroniser les équipes
    public boolean get_all_equip() throws SynchronizationException {
        try {
            String apiUrl = BASE_URL + "api-get-all-equipe.awp?log=" + gsToken;
            JSONObject response = executeApiCall(apiUrl);
            
            if ("ok".equals(response.optString("sRes"))) {
                JSONArray tabEquipe = response.optJSONArray("tab_equipe");
                if (tabEquipe != null) {
                    for (int j = 0; j < tabEquipe.length(); j++) {
                        JSONObject equipe = tabEquipe.getJSONObject(j);
                        String sCode = equipe.optString("sCode");
                        
                        // Vérifier l'existence
                        Cursor cursor = dbHelper.query(DatabaseHelper.TABLE_INV_EQUIPE, 
                                new String[]{"codeEquipe"}, 
                                "codeEquipe = ?", 
                                new String[]{sCode}, 
                                null);
                        
                        boolean exists = cursor.getCount() > 0;
                        cursor.close();
                        
                        if (!exists) {
                            ContentValues values = new ContentValues();
                            values.put("codeEquipe", sCode);
                            values.put("DateCreation", parseDate(equipe.optString("datecre")) != null ? 
                                    parseDate(equipe.optString("datecre")).getTime() : null);
                            values.put("chefEquipe", equipe.optString("sChefEquipe"));
                            values.put("Codeinv", equipe.optString("sCodeInv"));
                            values.put("UserCreation", equipe.optString("sUser"));
                            values.put("NoEquipe", equipe.optString("sNom"));
                            
                            dbHelper.insert(DatabaseHelper.TABLE_INV_EQUIPE, values);
                        }
                    }
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            throw new SynchronizationException("Error synchronizing equipment", e);
        }
    }
    
    // 3. Synchroniser les membres d'équipe
    public boolean get_all_equipe_membre() throws SynchronizationException {
        try {
            String apiUrl = BASE_URL + "api-get-all-equipe-membre.awp?log=" + gsToken;
            JSONObject response = executeApiCall(apiUrl);
            
            if ("ok".equals(response.optString("sRes"))) {
                JSONArray tabEquipe = response.optJSONArray("tab_equipe");
                if (tabEquipe != null) {
                    for (int j = 0; j < tabEquipe.length(); j++) {
                        JSONObject membre = tabEquipe.getJSONObject(j);
                        String sCodeInv = membre.optString("sCodeInv");
                        String sCode = membre.optString("sCode");
                        String sUser = membre.optString("sUser");
                        
                        // Vérifier l'existence
                        Cursor cursor = dbHelper.query(DatabaseHelper.TABLE_INV_MOMBRE_EQUIPE, 
                                new String[]{"CodeUtil"}, 
                                "Codeinv = ? AND codeEquipe = ? AND CodeUtil = ?", 
                                new String[]{sCodeInv, sCode, sUser}, 
                                null);
                        
                        boolean exists = cursor.getCount() > 0;
                        cursor.close();
                        
                        if (!exists) {
                            ContentValues values = new ContentValues();
                            values.put("codeEquipe", sCode);
                            values.put("Codeinv", sCodeInv);
                            values.put("CodeUtil", sUser);
                            values.put("NOM", membre.optString("sNom"));
                            
                            dbHelper.insert(DatabaseHelper.TABLE_INV_MOMBRE_EQUIPE, values);
                        }
                    }
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            throw new SynchronizationException("Error synchronizing team members", e);
        }
    }
    
    // 4. Synchroniser les inventaires
    public boolean get_all_inv() throws SynchronizationException {
        try {
            String apiUrl = BASE_URL + "api-get-all-inv.awp?log=" + gsToken;
            JSONObject response = executeApiCall(apiUrl);
            
            if ("ok".equals(response.optString("sRes"))) {
                JSONArray tabInv = response.optJSONArray("tabInv");
                if (tabInv != null) {
                    for (int j = 0; j < tabInv.length(); j++) {
                        JSONObject inv = tabInv.getJSONObject(j);
                        String sCode = inv.optString("sCode");
                        
                        // Vérifier l'existence
                        Cursor cursor = dbHelper.query(DatabaseHelper.TABLE_INV_IMMO, 
                                new String[]{"CodeInventaire"}, 
                                "CodeInventaire = ?", 
                                new String[]{sCode}, 
                                null);
                        
                        boolean exists = cursor.getCount() > 0;
                        cursor.close();
                        
                        if (!exists) {
                            ContentValues values = new ContentValues();
                            values.put("CodeInventaire", sCode);
                            values.put("DateCreation", parseDate(inv.optString("datecre")) != null ? 
                                    parseDate(inv.optString("datecre")).getTime() : null);
                            values.put("EtatInventaire", inv.optInt("sEtat"));
                            values.put("Description", inv.optString("desc"));
                            values.put("verif_art_inv", inv.optBoolean("verif_art") ? 1 : 0);
                            values.put("affich_art_inv", inv.optBoolean("affich_art") ? 1 : 0);
                            values.put("multi_eq_mis", inv.optBoolean("multi_eq_mis") ? 1 : 0);
                            
                            dbHelper.insert(DatabaseHelper.TABLE_INV_IMMO, values);
                        }
                    }
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            throw new SynchronizationException("Error synchronizing inventories", e);
        }
    }
    
    // 5. Synchroniser les missions d'inventaire
    public boolean get_all_MissionInv() throws SynchronizationException {
        try {
            String apiUrl = BASE_URL + "api-get-all-MissionInv.awp?log=" + gsToken;
            JSONObject response = executeApiCall(apiUrl);
            
            if ("ok".equals(response.optString("sRes"))) {
                JSONArray tabMission = response.optJSONArray("tab_mission");
                if (tabMission != null) {
                    for (int j = 0; j < tabMission.length(); j++) {
                        JSONObject mission = tabMission.getJSONObject(j);
                        String sCodeInv = mission.optString("sCodeInv");
                        String sCodeMis = mission.optString("sCodeMis");
                        
                        // Vérifier l'existence
                        Cursor cursor = dbHelper.query(DatabaseHelper.TABLE_INV_MISSION, 
                                new String[]{"codemission"}, 
                                "Codeinv = ? AND codemission = ?", 
                                new String[]{sCodeInv, sCodeMis}, 
                                null);
                        
                        boolean exists = cursor.getCount() > 0;
                        cursor.close();
                        
                        if (!exists) {
                            ContentValues values = new ContentValues();
                            values.put("Codeinv", sCodeInv);
                            values.put("codemission", sCodeMis);
                            values.put("Description", mission.optString("sDesc"));
                            values.put("etatmission", mission.optString("sEtat"));
                            values.put("DateCree", parseDate(mission.optString("sDateCre")) != null ? 
                                    parseDate(mission.optString("sDateCre")).getTime() : null);
                            values.put("UserCreation", mission.optString("sUserCre"));
                            values.put("DateMaj", parseDate(mission.optString("sDateMaj")) != null ? 
                                    parseDate(mission.optString("sDateMaj")).getTime() : null);
                            values.put("usermaj", mission.optString("sUserMaj"));
                            
                            dbHelper.insert(DatabaseHelper.TABLE_INV_MISSION, values);
                        }
                    }
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            throw new SynchronizationException("Error synchronizing inventory missions", e);
        }
    }
    
    // 6. Synchroniser les paramètres d'attributs d'inventaire
    public boolean get_all_inv_attribute_param() throws SynchronizationException {
        try {
            String apiUrl = BASE_URL + "api-get-inv-attribute-param.awp?log=" + gsToken;
            JSONObject response = executeApiCall(apiUrl);
            
            if ("ok".equals(response.optString("sRes"))) {
                JSONArray tabInvAtt = response.optJSONArray("tab_inv_att");
                if (tabInvAtt != null) {
                    for (int j = 0; j < tabInvAtt.length(); j++) {
                        JSONObject att = tabInvAtt.getJSONObject(j);
                        String sCodeInv = att.optString("sCodeInv");
                        String sCode = att.optString("sCode");
                        
                        // Vérifier l'existence
                        Cursor cursor = dbHelper.query(DatabaseHelper.TABLE_INV_ATTRIBUT_INV_IMMO, 
                                new String[]{"codeatribut"}, 
                                "Codeinv = ? AND codeatribut = ?", 
                                new String[]{sCodeInv, sCode}, 
                                null);
                        
                        boolean exists = cursor.getCount() > 0;
                        cursor.close();
                        
                        if (!exists) {
                            ContentValues values = new ContentValues();
                            values.put("codeatribut", sCode);
                            values.put("Codeinv", sCodeInv);
                            values.put("name", att.optString("sNom"));
                            values.put("afficher", att.optBoolean("bAffiche") ? 1 : 0);
                            values.put("saisie", att.optBoolean("bSaisir") ? 1 : 0);
                            values.put("validation", att.optBoolean("bValidation") ? 1 : 0);
                            
                            dbHelper.insert(DatabaseHelper.TABLE_INV_ATTRIBUT_INV_IMMO, values);
                        }
                    }
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            throw new SynchronizationException("Error synchronizing inventory attribute parameters", e);
        }
    }
    
    // 7. Synchroniser les équipements d'emplacement
    public boolean get_all_equip_empl() throws SynchronizationException {
        try {
            String apiUrl = BASE_URL + "api-get-all-equipe-emplacement.awp?log=" + gsToken;
            JSONObject response = executeApiCall(apiUrl);
            
            if ("ok".equals(response.optString("sRes"))) {
                JSONArray tabEquipe = response.optJSONArray("tab_equipe");
                if (tabEquipe != null) {
                    for (int j = 0; j < tabEquipe.length(); j++) {
                        JSONObject equipe = tabEquipe.getJSONObject(j);
                        String sCodeInv = equipe.optString("sCodeInv");
                        String sCodeMis = equipe.optString("sCodeMis");
                        String sCode = equipe.optString("sCode");
                        String sCodeEmp = equipe.optString("sCodeEmp");
                        
                        // Vérifier l'existence
                        Cursor cursor = dbHelper.query(DatabaseHelper.TABLE_INV_EQUIPE_INVENTAIRE, 
                                new String[]{"CodeEmplacement"}, 
                                "Codeinv = ? AND codemission = ? AND codeEquipe = ? AND CodeEmplacement = ?", 
                                new String[]{sCodeInv, sCodeMis, sCode, sCodeEmp}, 
                                null);
                        
                        boolean exists = cursor.getCount() > 0;
                        cursor.close();
                        
                        if (!exists) {
                            ContentValues values = new ContentValues();
                            values.put("codeEquipe", sCode);
                            values.put("Codeinv", sCodeInv);
                            values.put("CodeEmplacement", sCodeEmp);
                            values.put("codemission", sCodeMis);
                            
                            dbHelper.insert(DatabaseHelper.TABLE_INV_EQUIPE_INVENTAIRE, values);
                        }
                    }
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            throw new SynchronizationException("Error synchronizing equipment locations", e);
        }
    }
    
    // 8. Synchroniser les états
    public boolean get_all_etat() throws SynchronizationException {
        try {
            String apiUrl = BASE_URL + "api-get-etat.awp?log=" + gsToken;
            JSONObject response = executeApiCall(apiUrl);
            
            if ("ok".equals(response.optString("sRes"))) {
                JSONArray tabEtat = response.optJSONArray("tab_etat");
                if (tabEtat != null) {
                    for (int j = 0; j < tabEtat.length(); j++) {
                        JSONObject etat = tabEtat.getJSONObject(j);
                        String sLib = etat.optString("sLib");
                        
                        // Vérifier l'existence
                        Cursor cursor = dbHelper.query(DatabaseHelper.TABLE_SOUS_ETAT, 
                                new String[]{"LibelleEtat"}, 
                                "LibelleEtat = ?", 
                                new String[]{sLib}, 
                                null);
                        
                        boolean exists = cursor.getCount() > 0;
                        cursor.close();
                        
                        if (!exists) {
                            ContentValues values = new ContentValues();
                            values.put("coler", etat.optString("sColor"));
                            values.put("sousEtat", etat.optInt("nEtat"));
                            values.put("TypeEtat", etat.optString("sType"));
                            values.put("DescriptionEtat", etat.optString("sDesc"));
                            values.put("LibelleEtat", sLib);
                            
                            dbHelper.insert(DatabaseHelper.TABLE_SOUS_ETAT, values);
                        }
                    }
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            throw new SynchronizationException("Error synchronizing states", e);
        }
    }
    
    // 9. Synchroniser les employés
    public boolean get_inv_emp() throws SynchronizationException {
        try {
            String apiUrl = BASE_URL + "api-get-empyes.awp?log=" + gsToken;
            JSONObject response = executeApiCall(apiUrl);
            
            if ("ok".equals(response.optString("sRes"))) {
                JSONArray tabEmpyes = response.optJSONArray("tab_empyes");
                if (tabEmpyes != null) {
                    for (int j = 0; j < tabEmpyes.length(); j++) {
                        JSONObject emp = tabEmpyes.getJSONObject(j);
                        String sCode = emp.optString("sCode");
                        
                        // Vérifier l'existence
                        Cursor cursor = dbHelper.query(DatabaseHelper.TABLE_EMPLOYEE, 
                                new String[]{"CodeEmployee"}, 
                                "CodeEmployee = ?", 
                                new String[]{sCode}, 
                                null);
                        
                        boolean exists = cursor.getCount() > 0;
                        cursor.close();
                        
                        if (!exists) {
                            ContentValues values = new ContentValues();
                            values.put("CodeEmployee", sCode);
                            values.put("CodeFunction", emp.optString("sCodeFunction"));
                            values.put("CodeGroup", emp.optString("sCodeGroup"));
                            
                            Date dateCre = parseDate(emp.optString("sDateCre"));
                            values.put("DateCre", dateCre != null ? dateCre.getTime() : null);
                            
                            Date dateMaj = parseDate(emp.optString("sDateMaj"));
                            values.put("DateMaj", dateMaj != null ? dateMaj.getTime() : null);
                            
                            values.put("EmailPerso", emp.optString("sEmailPerso"));
                            values.put("EmailWork", emp.optString("sEmailWork"));
                            values.put("NomPrenom", emp.optString("sNomPrenom"));
                            values.put("NumPerso", emp.optDouble("sNumPerso"));
                            values.put("NumTravail", emp.optDouble("sNumTravail"));
                            values.put("Addresse", emp.optString("sAdres"));
                            values.put("ETAT", emp.optInt("sEtat"));
                            values.put("UserCreation", emp.optString("sUser"));
                            
                            dbHelper.insert(DatabaseHelper.TABLE_EMPLOYEE, values);
                        }
                    }
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            throw new SynchronizationException("Error synchronizing employees", e);
        }
    }
    
    // 10. Synchroniser les attributs
    public boolean get_invAtt() throws SynchronizationException {
        try {
            String apiUrl = BASE_URL + "api-get-empyes.awp?log=" + gsToken;
            JSONObject response = executeApiCall(apiUrl);
            
            if ("ok".equals(response.optString("sRes"))) {
                JSONArray tabAtt = response.optJSONArray("tab_att");
                if (tabAtt != null) {
                    for (int j = 0; j < tabAtt.length(); j++) {
                        JSONObject att = tabAtt.getJSONObject(j);
                        String sLib = att.optString("sLib");
                        
                        // Vérifier l'existence
                        Cursor cursor = dbHelper.query(DatabaseHelper.TABLE_ATTRIBUTS, 
                                new String[]{"libCode"}, 
                                "libCode = ?", 
                                new String[]{sLib}, 
                                null);
                        
                        boolean exists = cursor.getCount() > 0;
                        cursor.close();
                        
                        if (!exists) {
                            ContentValues values = new ContentValues();
                            values.put("libCode", sLib);
                            values.put("isAffiche", att.optBoolean("bIsAffiche") ? 1 : 0);
                            values.put("isInconnu", att.optBoolean("bIsInconnu") ? 1 : 0);
                            values.put("obligatoire", att.optBoolean("bIsOblig") ? 1 : 0);
                            values.put("valeurDefaut", att.optString("sValr"));
                            values.put("type", att.optString("sType"));
                            
                            dbHelper.insert(DatabaseHelper.TABLE_ATTRIBUTS, values);
                        }
                    }
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            throw new SynchronizationException("Error synchronizing attributes", e);
        }
    }
    
    // 11. Synchroniser les valeurs d'attributs
    public boolean get_AttValr() throws SynchronizationException {
        try {
            String apiUrl = BASE_URL + "api-get-AttValeur.awp?log=" + gsToken;
            JSONObject response = executeApiCall(apiUrl);
            
            if ("ok".equals(response.optString("sRes"))) {
                JSONArray tabAttValr = response.optJSONArray("tab_attValr");
                if (tabAttValr != null) {
                    for (int j = 0; j < tabAttValr.length(); j++) {
                        JSONObject attValr = tabAttValr.getJSONObject(j);
                        
                        ContentValues values = new ContentValues();
                        values.put("codeAttribut", attValr.optString("sCodeAtt"));
                        values.put("Valeur", attValr.optString("sValr"));
                        values.put("isdefault", attValr.optBoolean("bIsdefault") ? 1 : 0);
                        
                        dbHelper.insert(DatabaseHelper.TABLE_VALEURS_ATTRIBUT, values);
                    }
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            throw new SynchronizationException("Error synchronizing attribute values", e);
        }
    }
    
    // 12. Synchroniser les ensembles d'attributs
    public boolean get_attribute_set() throws SynchronizationException {
        try {
            String apiUrl = BASE_URL + "api-get-AttSet.awp?log=" + gsToken;
            JSONObject response = executeApiCall(apiUrl);
            
            if ("ok".equals(response.optString("sRes"))) {
                JSONArray tabAtt = response.optJSONArray("tab_equipe");
                if (tabAtt != null) {
                    for (int j = 0; j < tabAtt.length(); j++) {
                        JSONObject att = tabAtt.getJSONObject(j);
                        String sCodeAtt = att.optString("sCodeAtt");
                        String sCode = att.optString("sCode");
                        
                        // Vérifier l'existence
                        Cursor cursor = dbHelper.query(DatabaseHelper.TABLE_ATTRIBUTS_ATTBSET, 
                                new String[]{"codeAttribus"}, 
                                "codeAttribus = ? AND codeAttribusSet = ?", 
                                new String[]{sCodeAtt, sCode}, 
                                null);
                        
                        boolean exists = cursor.getCount() > 0;
                        cursor.close();
                        
                        if (!exists) {
                            ContentValues values = new ContentValues();
                            values.put("codeAttribus", sCodeAtt);
                            values.put("codeAttribusSet", sCode);
                            
                            dbHelper.insert(DatabaseHelper.TABLE_ATTRIBUTS_ATTBSET, values);
                        }
                    }
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            throw new SynchronizationException("Error synchronizing attribute sets", e);
        }
    }
    
    // 13. Synchroniser les utilisateurs et la configuration
    public boolean get_all_user() throws SynchronizationException {
        try {
            String apiUrl = BASE_URL + "api-get-all-user.awp?log=" + gsToken;
            JSONObject response = executeApiCall(apiUrl);
            
            if ("ok".equals(response.optString("sRes"))) {
                // Synchroniser les utilisateurs
                JSONArray tabUtlstr = response.optJSONArray("tabUtlstr");
                if (tabUtlstr != null) {
                    for (int j = 0; j < tabUtlstr.length(); j++) {
                        JSONObject user = tabUtlstr.getJSONObject(j);
                        String sLogin = user.optString("sLogin");
                        
                        // Vérifier l'existence
                        Cursor cursor = dbHelper.query(DatabaseHelper.TABLE_UTILISATEUR, 
                                new String[]{"login"}, 
                                "login = ?", 
                                new String[]{sLogin}, 
                                null);
                        
                        boolean exists = cursor.getCount() > 0;
                        cursor.close();
                        
                        if (!exists) {
                            ContentValues values = new ContentValues();
                            values.put("CodeUtil", user.optString("sCode"));
                            values.put("NomPrenomUtil", user.optString("sNom"));
                            values.put("MotPasse", user.optString("sPass"));
                            values.put("login", sLogin);
                            
                            dbHelper.insert(DatabaseHelper.TABLE_UTILISATEUR, values);
                        }
                    }
                }
                
                // Synchroniser la configuration
                JSONArray tabConfg = response.optJSONArray("tabConfg");
                if (tabConfg != null && tabConfg.length() > 0) {
                    JSONObject config = tabConfg.getJSONObject(0);
                    
                    // Vérifier si la table congfSysteme est vide
                    Cursor cursor = dbHelper.query(DatabaseHelper.TABLE_CONGF_SYSTEME, 
                            new String[]{"empldefaut"}, null, null, null);
                    
                    boolean isEmpty = cursor.getCount() == 0;
                    cursor.close();
                    
                    ContentValues values = new ContentValues();
                    values.put("affiche_Niveau", config.optBoolean("affiche_Niveau") ? 1 : 0);
                    values.put("nbr_Niveau_empl", config.optString("nbr_Niveau_empl"));
                    values.put("empldefaut", config.optString("empldefaut"));
                    values.put("isAttset", config.optBoolean("isAttset") ? 1 : 0);
                    values.put("n1", config.optString("n1"));
                    values.put("n2", config.optString("n2"));
                    values.put("n3", config.optString("n3"));
                    values.put("n4", config.optString("n4"));
                    values.put("n5", config.optString("n5"));
                    values.put("n6", config.optString("n6"));
                    values.put("n7", config.optString("n7"));
                    values.put("n8", config.optString("n8"));
                    values.put("n9", config.optString("n9"));
                    
                    if (isEmpty) {
                        dbHelper.insert(DatabaseHelper.TABLE_CONGF_SYSTEME, values);
                    } else {
                        dbHelper.update(DatabaseHelper.TABLE_CONGF_SYSTEME, values, null, null);
                    }
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            throw new SynchronizationException("Error synchronizing users and configuration", e);
        }
    }
    
    // 14. Synchroniser les emplacements
    public boolean get_all_emplacements() throws SynchronizationException {
        try {
            String apiUrl = BASE_URL + "api-get-all-emplacement.awp?log=" + gsToken;
            JSONObject response = executeApiCall(apiUrl);
            
            if ("ok".equals(response.optString("sRes"))) {
                JSONArray tabRetoure = response.optJSONArray("tabRetoure");
                if (tabRetoure != null) {
                    for (int j = 0; j < tabRetoure.length(); j++) {
                        JSONObject empl = tabRetoure.getJSONObject(j);
                        String sCode = empl.optString("sCode");
                        
                        // Vérifier l'existence
                        Cursor cursor = dbHelper.query(DatabaseHelper.TABLE_ARBRE_EMP, 
                                new String[]{"code"}, 
                                "code = ?", 
                                new String[]{sCode}, 
                                null);
                        
                        boolean exists = cursor.getCount() > 0;
                        cursor.close();
                        
                        if (!exists) {
                            ContentValues values = new ContentValues();
                            values.put("code", sCode);
                            values.put("Libelle", empl.optString("sNom"));
                            values.put("Idparent", empl.optString("sCodeParnt"));
                            values.put("path", empl.optString("sPath"));
                            values.put("n1", empl.optString("n1"));
                            values.put("n2", empl.optString("n2"));
                            values.put("n3", empl.optString("n3"));
                            values.put("n4", empl.optString("n4"));
                            values.put("n5", empl.optString("n5"));
                            values.put("n6", empl.optString("n6"));
                            values.put("n7", empl.optString("n7"));
                            values.put("n8", empl.optString("n8"));
                            values.put("n9", empl.optString("n9"));
                            
                            dbHelper.insert(DatabaseHelper.TABLE_ARBRE_EMP, values);
                        }
                    }
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            throw new SynchronizationException("Error synchronizing locations", e);
        }
    }
    
    // Méthode pour effectuer une synchronisation complète
    public boolean performFullSynchronization() {
        try {
            Log.i(TAG, "Starting full synchronization...");
            
            // Vider les tables
            clearTables();
            
            // Exécuter toutes les synchronisations dans l'ordre
            boolean success = true;
            
            String[] syncMethods = {
                "Equipments", "Team Members", "Inventories", "Missions", 
                "Attribute Parameters", "Equipment Locations", "States", 
                "Employees", "Attributes", "Attribute Values", 
                "Attribute Sets", "Users and Configuration", "Locations"
            };
            
            boolean[] results = {
                get_all_equip(),
                get_all_equipe_membre(),
                get_all_inv(),
                get_all_MissionInv(),
                get_all_inv_attribute_param(),
                get_all_equip_empl(),
                get_all_etat(),
                get_inv_emp(),
                get_invAtt(),
                get_AttValr(),
                get_attribute_set(),
                get_all_user(),
                get_all_emplacements()
            };
            
            for (int i = 0; i < results.length; i++) {
                Log.i(TAG, syncMethods[i] + " synchronization: " + (results[i] ? "SUCCESS" : "FAILED"));
                if (!results[i]) {
                    success = false;
                }
            }
            
            Log.i(TAG, "Full synchronization completed: " + (success ? "SUCCESS" : "PARTIAL FAILURE"));
            return success;
            
        } catch (SynchronizationException e) {
            Log.e(TAG, "Synchronization error: " + e.getMessage(), e);
            return false;
        }
    }
    
    // Méthode pour fermer la connexion à la base de données
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}