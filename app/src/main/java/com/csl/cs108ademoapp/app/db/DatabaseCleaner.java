package com.csl.cs108ademoapp.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DatabaseCleaner {
    
    public interface CleanDatabaseListener {
        void onProgressUpdate(int progress);
        void onComplete();
    }
    
    public static void clearAllData(Context context, CleanDatabaseListener listener) {
        new ClearDatabaseTask(context, listener).execute();
    }
    
    private static class ClearDatabaseTask extends AsyncTask<Void, Integer, Boolean> {
        private Context context;
        private DatabaseHelper dbHelper;
        private CleanDatabaseListener listener;
        
        public ClearDatabaseTask(Context context, CleanDatabaseListener listener) {
            this.context = context;
            this.dbHelper = new DatabaseHelper(context);
            this.listener = listener;
        }
        
        @Override
        protected void onPreExecute() {
            if (listener != null) {
                listener.onProgressUpdate(0);
            }
        }
        
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                
                // Table list in specific order to avoid foreign key constraints
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
                
                int totalTables = tables.length;
                int currentTable = 0;
                
                // Disable foreign key constraints temporarily
                db.execSQL("PRAGMA foreign_keys=OFF;");
                
                for (String table : tables) {
                    db.delete(table, null, null);
                    currentTable++;
                    int progress = (currentTable * 100) / totalTables;
                    publishProgress(progress);
                }
                
                // Re-enable foreign key constraints
                db.execSQL("PRAGMA foreign_keys=ON;");
                
                db.close();
                return true;
                
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        
        @Override
        protected void onProgressUpdate(Integer... values) {
            if (listener != null) {
                listener.onProgressUpdate(values[0]);
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            if (listener != null) {
                listener.onComplete();
            }
            if (success) {
                Toast.makeText(context, "Suppression effectuée avec succès", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Erreur lors de la suppression", Toast.LENGTH_LONG).show();
            }
        }
    }
}