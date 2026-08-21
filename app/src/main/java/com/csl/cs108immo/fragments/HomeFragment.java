package com.csl.cs108immo.fragments;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.LOCATION_SERVICE;
import static androidx.core.app.ServiceCompat.STOP_FOREGROUND_REMOVE;
import static androidx.core.app.ServiceCompat.stopForeground;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.csl.cs108immo.MainActivity.isHomeFragment;
import static com.csl.cs108immo.MainActivity.mContext;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.os.Handler;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.csl.cs108immo.CustomAlertDialog;
import com.csl.cs108immo.CustomPopupWindow;
import com.csl.cs108immo.CustomProgressDialog;
import com.csl.cs108immo.MainActivity;
import com.csl.cs108immo.MyForegroundService;
import com.csl.cs108immo.R;
import android.content.DialogInterface;
import android.widget.Toast;

import com.csl.cs108immo.apps.SharedPrefManager;
import com.csl.cs108immo.app.db.DatabaseCleaner;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.csl.cs108immo.app.sync.SynchronizationManager;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;
import com.csl.cs108immo.app.db.DatabaseHelper;

public class HomeFragment extends CommonFragment {
    final boolean DEBUG = false;
    protected Handler connectionHandler = new Handler();
    protected Runnable connectionChecker;
    protected static final int CHECK_INTERVAL = 1000;

    @Override
    public void onAttach(Context context) {
        Log.i("Hello", "HomeFragment.onAttach");
        super.onAttach(context);
    }
    @Override
    public void onStart() {
        Log.i("Hello", "HomeFragment.onStart");
        isHomeFragment = true;
        MainActivity.csLibrary4A.appendToLog("isHomeFragment1 = " + isHomeFragment);
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateButtonStatesBasedOnMode();
        startConnectionMonitoring1();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopConnectionMonitoring1();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        if (getActivity().getPackageName().contains("com.csl.cs710ademoapp")) return inflater.inflate(R.layout.home_layout710, container, false);
        else return inflater.inflate(R.layout.dashboard, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (true && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            //actionBar.setIcon(android.R.drawable.ic_menu_save);
            if (getActivity().getPackageName().contains("cs710ademoapp")) actionBar.setTitle(R.string.title_activity_home_cs710);
            else actionBar.setTitle(R.string.title_activity_home_cs108);
        }
        if (true) {
            String strForegroundReader = MainActivity.csLibrary4A.getForegroundReader();
            MainActivity.csLibrary4A.appendToLog("strForegroundReader = " + strForegroundReader + ", getForegroundServiceEnable = " + MainActivity.csLibrary4A.getForegroundServiceEnable());
            if (!getActivity().getPackageName().contains("com.csl.cs710awedgeapp") && strForegroundReader != null && strForegroundReader.length() != 0) {
                MainActivity.csLibrary4A.appendToLog("strForegroundReader = " + strForegroundReader + ", getForegroundServiceEnable = " + MainActivity.csLibrary4A.getForegroundServiceEnable());
                LinearLayout linearLayout1, linearLayout2, linearLayout3, linearLayout4, linearLayout5;
                linearLayout1 = (LinearLayout) getActivity().findViewById(R.id.mainRow1);
                linearLayout2 = (LinearLayout) getActivity().findViewById(R.id.mainRow2);
                linearLayout3 = (LinearLayout) getActivity().findViewById(R.id.mainRow3);
                linearLayout4 = (LinearLayout) getActivity().findViewById(R.id.mainRow4);
                linearLayout5 = (LinearLayout) getActivity().findViewById(R.id.mainRow5);
                if (getActivity().getPackageName().contains("com.csl.cs710ademoapp")) {
                    FrameLayout frameLayout = (FrameLayout) getActivity().findViewById(R.id.mainButton1);
                    frameLayout.setVisibility(View.INVISIBLE);
                } else {
                    Button button = (Button) getActivity().findViewById(R.id.mainButton1);
                    button.setVisibility(View.INVISIBLE);
                }
                linearLayout1.setVisibility(View.INVISIBLE);
                linearLayout2.setVisibility(View.GONE);
                linearLayout3.setVisibility(View.VISIBLE);
                linearLayout4.setVisibility(View.INVISIBLE);
                //linearLayout5.setVisibility(View.INVISIBLE);

            }
        }
        MainActivity.tagType = null; MainActivity.mDid = null;
        if (true || MainActivity.sharedObjects.versionWarningShown == false)
            mHandler.post(runnableConfiguring);
        mHandler.postDelayed(runnableStartService, 1000);
        updateButtonStatesBasedOnMode();
    }

    private void updateButtonStatesBasedOnMode() {
        LinearLayout mainRow2 = getActivity().findViewById(R.id.mainRow2);
        if (isOnline) {
            mainRow2.setVisibility(View.VISIBLE);
        } else {
            mainRow2.setVisibility(View.GONE)
        }
        /*
        if (mainRow2 != null) {
            boolean isOnline = SharedPrefManager.getInstance(getActivity()).isOnlineMode();
            
            FrameLayout syncLayout = (FrameLayout) mainRow2.getChildAt(0);
            FrameLayout clearLayout = (FrameLayout) mainRow2.getChildAt(1);
            FrameLayout testLayout = (FrameLayout) mainRow2.getChildAt(2);

            if (isOnline) {
                syncLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startSynchronization();
                    }
                });
                
                clearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showClearDatabaseConfirmation();
                    }
                });

                testLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDatabaseStats();
                    }
                });
                
                syncLayout.setAlpha(1.0f);
                clearLayout.setAlpha(1.0f);
                testLayout.setAlpha(1.0f);
                syncLayout.setClickable(true);
                clearLayout.setClickable(true);
                testLayout.setClickable(true);
            } else {
                syncLayout.setOnClickListener(null);
                clearLayout.setOnClickListener(null);
                testLayout.setOnClickListener(null);
                syncLayout.setAlpha(0.5f);
                clearLayout.setAlpha(0.5f);
                testLayout.setAlpha(0.5f);
                syncLayout.setClickable(false);
                clearLayout.setClickable(false);
                testLayout.setClickable(false);
            }
        }*/
    }

    private void showDatabaseStats() {
        // Créer un AlertDialog avec une vue personnalisée
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Statistiques de la base de données");
        
        // Créer un LinearLayout pour contenir la liste
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        
        // Ajouter un ScrollView pour permettre le défilement
        android.widget.ScrollView scrollView = new android.widget.ScrollView(getActivity());
        scrollView.addView(layout);
        
        // Récupérer les statistiques
        List<DatabaseStats.TableInfo> tableStats = getDatabaseStats();
        
        // Ajouter les informations à l'affichage
        for (DatabaseStats.TableInfo tableInfo : tableStats) {
            TextView textView = new TextView(getActivity());
            String displayText;
            if (tableInfo.hasError) {
                displayText = tableInfo.number + ". " + tableInfo.tableName + " : ERREUR";
                textView.setTextColor(0xFFFF0000); // Rouge pour les erreurs
            } else {
                displayText = tableInfo.number + ". " + tableInfo.tableName + " : " + tableInfo.count + " enregistrement(s)";
                textView.setTextColor(0xFF000000); // Noir pour les données normales
            }
            textView.setText(displayText);
            textView.setTextSize(12);
            textView.setPadding(10, 6, 10, 6);
            textView.setTypeface(null, android.graphics.Typeface.NORMAL);
            layout.addView(textView);
        }
        
        // Ajouter une ligne de séparation
        View separator = new View(getActivity());
        separator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        separator.setBackgroundColor(0xFFDDDDDD);
        layout.addView(separator);
        
        // Calculer le total des enregistrements (sans compter les erreurs)
        int totalRecords = 0;
        for (DatabaseStats.TableInfo tableInfo : tableStats) {
            if (!tableInfo.hasError) {
                totalRecords += tableInfo.count;
            }
        }
        
        // Ajouter le total
        TextView totalView = new TextView(getActivity());
        totalView.setText("TOTAL : " + totalRecords + " enregistrement(s)");
        totalView.setTextSize(14);
        totalView.setPadding(10, 15, 10, 8);
        totalView.setTextColor(0xFF000000);
        totalView.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(totalView);
        
        builder.setView(scrollView);
        builder.setPositiveButton("Fermer", null);
        builder.show();
    }

    // Classe interne pour les statistiques
    private static class DatabaseStats {
        static class TableInfo {
            int number;
            String tableName;
            int count;
            boolean hasError;
            
            TableInfo(int num, String name, int cnt, boolean error) {
                number = num;
                tableName = name;
                count = cnt;
                hasError = error;
            }
        }
    }
    
    // Méthode pour obtenir les statistiques de la base avec tous les tables
    private List<DatabaseStats.TableInfo> getDatabaseStats() {
        List<DatabaseStats.TableInfo> stats = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        SQLiteDatabase db = null;
        
        try {
            db = dbHelper.getReadableDatabase();
            
            // Liste de toutes les tables avec leurs numéros et noms
            TableInfoEntry[] tables = {
                new TableInfoEntry(1, "ArbreEmp", DatabaseHelper.TABLE_ARBRE_EMP),
                new TableInfoEntry(2, "ArbreFamile", DatabaseHelper.TABLE_ARBRE_FAMILE),
                new TableInfoEntry(3, "Attributs", DatabaseHelper.TABLE_ATTRIBUTS),
                new TableInfoEntry(4, "Attributs_attbset", DatabaseHelper.TABLE_ATTRIBUTS_ATTBSET),
                new TableInfoEntry(5, "congfSysteme", DatabaseHelper.TABLE_CONGF_SYSTEME),
                new TableInfoEntry(6, "Det_inv_immo", DatabaseHelper.TABLE_DET_INV_IMMO),
                new TableInfoEntry(7, "Employee", DatabaseHelper.TABLE_EMPLOYEE),
                new TableInfoEntry(8, "FOURNISSEUR", DatabaseHelper.TABLE_FOURNISSEUR),
                new TableInfoEntry(9, "historique_ecar_inv", DatabaseHelper.TABLE_HISTORIQUE_ECAR_INV),
                new TableInfoEntry(10, "Immo", DatabaseHelper.TABLE_IMMO),
                new TableInfoEntry(11, "immo_inv_image", DatabaseHelper.TABLE_IMMO_INV_IMAGE),
                new TableInfoEntry(12, "Inv_attributinvImmo", DatabaseHelper.TABLE_INV_ATTRIBUTINV_IMMO),
                new TableInfoEntry(13, "Inv_equipe", DatabaseHelper.TABLE_INV_EQUIPE),
                new TableInfoEntry(14, "Inv_EquipeInventaire", DatabaseHelper.TABLE_INV_EQUIPE_INVENTAIRE),
                new TableInfoEntry(15, "inv_immo", DatabaseHelper.TABLE_INV_IMMO),
                new TableInfoEntry(16, "inv_mission", DatabaseHelper.TABLE_INV_MISSION),
                new TableInfoEntry(17, "Inv_eMombreequipe", DatabaseHelper.TABLE_INV_EMOMBREEQUIPE),
                new TableInfoEntry(18, "InvImmoAttributValr", DatabaseHelper.TABLE_INV_IMMO_ATTRIBUT_VALR),
                new TableInfoEntry(19, "param", DatabaseHelper.TABLE_PARAM),
                new TableInfoEntry(20, "Proprietaire", DatabaseHelper.TABLE_PROPRIETAIRE),
                new TableInfoEntry(21, "SouSEtat", DatabaseHelper.TABLE_SOUS_ETAT),
                new TableInfoEntry(22, "Utilisateur", DatabaseHelper.TABLE_UTILISATEUR),
                new TableInfoEntry(23, "valeursAttribut", DatabaseHelper.TABLE_VALEURS_ATTRIBUT)
            };
            
            for (TableInfoEntry tableEntry : tables) {
                try {
                    Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableEntry.tableConstant, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int count = cursor.getInt(0);
                        stats.add(new DatabaseStats.TableInfo(
                            tableEntry.number,
                            tableEntry.tableName,
                            count,
                            false
                        ));
                        cursor.close();
                    } else {
                        // Table existe mais cursor est null ou vide
                        stats.add(new DatabaseStats.TableInfo(
                            tableEntry.number,
                            tableEntry.tableName,
                            0,
                            true
                        ));
                        if (cursor != null) cursor.close();
                    }
                } catch (Exception e) {
                    Log.e("HomeFragment", "Erreur pour la table " + tableEntry.tableName, e);
                    stats.add(new DatabaseStats.TableInfo(
                        tableEntry.number,
                        tableEntry.tableName,
                        0,
                        true
                    ));
                }
            }
            
        } catch (Exception e) {
            Log.e("HomeFragment", "Erreur d'accès à la base de données", e);
            // En cas d'erreur générale, ajouter toutes les tables avec erreur
            TableInfoEntry[] tables = {
                new TableInfoEntry(1, "ArbreEmp", DatabaseHelper.TABLE_ARBRE_EMP),
                new TableInfoEntry(2, "ArbreFamile", DatabaseHelper.TABLE_ARBRE_FAMILE),
                new TableInfoEntry(3, "Attributs", DatabaseHelper.TABLE_ATTRIBUTS),
                new TableInfoEntry(4, "Attributs_attbset", DatabaseHelper.TABLE_ATTRIBUTS_ATTBSET),
                new TableInfoEntry(5, "congfSysteme", DatabaseHelper.TABLE_CONGF_SYSTEME),
                new TableInfoEntry(6, "Det_inv_immo", DatabaseHelper.TABLE_DET_INV_IMMO),
                new TableInfoEntry(7, "Employee", DatabaseHelper.TABLE_EMPLOYEE),
                new TableInfoEntry(8, "FOURNISSEUR", DatabaseHelper.TABLE_FOURNISSEUR),
                new TableInfoEntry(9, "historique_ecar_inv", DatabaseHelper.TABLE_HISTORIQUE_ECAR_INV),
                new TableInfoEntry(10, "Immo", DatabaseHelper.TABLE_IMMO),
                new TableInfoEntry(11, "immo_inv_image", DatabaseHelper.TABLE_IMMO_INV_IMAGE),
                new TableInfoEntry(12, "Inv_attributinvImmo", DatabaseHelper.TABLE_INV_ATTRIBUTINV_IMMO),
                new TableInfoEntry(13, "Inv_equipe", DatabaseHelper.TABLE_INV_EQUIPE),
                new TableInfoEntry(14, "Inv_EquipeInventaire", DatabaseHelper.TABLE_INV_EQUIPE_INVENTAIRE),
                new TableInfoEntry(15, "inv_immo", DatabaseHelper.TABLE_INV_IMMO),
                new TableInfoEntry(16, "inv_mission", DatabaseHelper.TABLE_INV_MISSION),
                new TableInfoEntry(17, "Inv_eMombreequipe", DatabaseHelper.TABLE_INV_EMOMBREEQUIPE),
                new TableInfoEntry(18, "InvImmoAttributValr", DatabaseHelper.TABLE_INV_IMMO_ATTRIBUT_VALR),
                new TableInfoEntry(19, "param", DatabaseHelper.TABLE_PARAM),
                new TableInfoEntry(20, "Proprietaire", DatabaseHelper.TABLE_PROPRIETAIRE),
                new TableInfoEntry(21, "SouSEtat", DatabaseHelper.TABLE_SOUS_ETAT),
                new TableInfoEntry(22, "Utilisateur", DatabaseHelper.TABLE_UTILISATEUR),
                new TableInfoEntry(23, "valeursAttribut", DatabaseHelper.TABLE_VALEURS_ATTRIBUT)
            };
            for (TableInfoEntry tableEntry : tables) {
                stats.add(new DatabaseStats.TableInfo(
                    tableEntry.number,
                    tableEntry.tableName,
                    0,
                    true
                ));
            }
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return stats;
    }

    // Classe utilitaire pour stocker les informations des tables
    private static class TableInfoEntry {
        int number;
        String tableName;
        String tableConstant;
        
        TableInfoEntry(int num, String name, String constant) {
            number = num;
            tableName = name;
            tableConstant = constant;
        }
    }

    // Méthode utilitaire pour obtenir un nom d'affichage plus lisible
    private String getDisplayTableName(String tableName) {
        switch (tableName) {
            case DatabaseHelper.TABLE_ARBRE_EMP:
                return "Emplacements";
            case DatabaseHelper.TABLE_PROPRIETAIRE:
                return "Propriétaires";
            case DatabaseHelper.TABLE_EMPLOYEE:
                return "Employés";
            case DatabaseHelper.TABLE_FOURNISSEUR:
                return "Fournisseurs";
            case DatabaseHelper.TABLE_SOUS_ETAT:
                return "États";
            case DatabaseHelper.TABLE_ATTRIBUTS:
                return "Attributs";
            case DatabaseHelper.TABLE_VALEURS_ATTRIBUT:
                return "Valeurs attributs";
            case DatabaseHelper.TABLE_ATTRIBUTS_ATTBSET:
                return "Attributs set";
            case DatabaseHelper.TABLE_UTILISATEUR:
                return "Utilisateurs";
            case DatabaseHelper.TABLE_IMMO:
                return "Immobilisations";
            case DatabaseHelper.TABLE_INV_IMMO:
                return "Inventaires";
            case DatabaseHelper.TABLE_INV_ATTRIBUTINV_IMMO:
                return "Attributs inventaire";
            case DatabaseHelper.TABLE_INV_EQUIPE:
                return "Équipes";
            case DatabaseHelper.TABLE_INV_EMOMBREEQUIPE:
                return "Membres équipes";
            case DatabaseHelper.TABLE_INV_MISSION:
                return "Missions";
            case DatabaseHelper.TABLE_DET_INV_IMMO:
                return "Détails inventaire";
            case DatabaseHelper.TABLE_HISTORIQUE_ECAR_INV:
                return "Historique écarts";
            case DatabaseHelper.TABLE_IMMO_INV_IMAGE:
                return "Images";
            case DatabaseHelper.TABLE_INV_IMMO_ATTRIBUT_VALR:
                return "Valeurs attributs inventaire";
            default:
                return tableName;
        }
    }

    private void startSynchronization() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Synchronisation");
        progressDialog.setMessage("Synchronisation en cours...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        SynchronizationManager.getInstance(getActivity()).synchronize(new SynchronizationManager.SyncListener() {
            @Override
            public void onProgressUpdate(int progress, String libelle) {
                progressDialog.setProgress(progress);
                progressDialog.setMessage(libelle);
            }
            
            @Override
            public void onComplete(boolean success, String message) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showClearDatabaseConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(getActivity())
            .setTitle("Confirmation")
            .setMessage("Voulez-vous vraiment supprimer la base?")
            .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startDatabaseCleanup();
                }
            })
            .setNegativeButton("Non", null)
            .show();
    }

    private void startDatabaseCleanup() {
        // Trouver la ProgressBar dans le layout
        // Vous devrez ajouter une ProgressBar dans home_layout_new.xml
        // Pour l'instant, utilisons une ProgressDialog
        
        final android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(getActivity());
        progressDialog.setTitle("Suppression en cours");
        progressDialog.setMessage("Suppression des données...");
        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        DatabaseCleaner.clearAllData(getActivity(), new DatabaseCleaner.CleanDatabaseListener() {
            @Override
            public void onProgressUpdate(int progress) {
                progressDialog.setProgress(progress);
                progressDialog.setMessage("Suppression des données... " + progress + "%");
            }
            
            @Override
            public void onComplete() {
                progressDialog.dismiss();
                // Optional: refresh UI if needed
            }
        });
    }

    @Override
    public void onStop() {
        stopProgressDialog();
        mHandler.removeCallbacks(runnableConfiguring);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        isHomeFragment = false;
        //MainActivity.csLibrary4A.appendToLog("isHomeFragment1 = " + isHomeFragment);
        super.onDestroyView();
    }

    public HomeFragment() {
        super("HomeFragment");
    }

    CustomProgressDialog progressDialog;
    void stopProgressDialog() {
        if (progressDialog != null) { if (progressDialog.isShowing()) progressDialog.dismiss(); }
    }
    Runnable runnableConfiguring = new Runnable() {
        boolean DEBUG = false;
        @Override
        public void run() {
            if (true) MainActivity.csLibrary4A.appendToLog("runnableConfiguring(): mrfidToWriteSize = " + MainActivity.csLibrary4A.mrfidToWriteSize());
            boolean progressShown = false;
            if (progressDialog != null) { if (progressDialog.isShowing()) progressShown = true; }
            if (MainActivity.csLibrary4A.isBleConnected() == false || MainActivity.csLibrary4A.isRfidFailure()) {
                if (progressShown) {
                    stopProgressDialog();
                    /*String stringPopup = "Connection failed, please rescan.";
                    CustomPopupWindow customPopupWindow = new CustomPopupWindow((Context) getActivity());
                    customPopupWindow.popupStart(stringPopup, false); */
                }
            } else if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0) {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("mrfidToWriteSize = " + MainActivity.csLibrary4A.mrfidToWriteSize());
                mHandler.postDelayed(runnableConfiguring, 250);
                if (progressShown == false) {
                    progressDialog = new CustomProgressDialog(getActivity(), "Initializing reader. Please wait.");
                    progressDialog.show();
                }
            } else {
                stopProgressDialog();
                if (MainActivity.sharedObjects.versionWarningShown == false) {
                    String stringPopup = MainActivity.csLibrary4A.checkVersion();
                    if (false && stringPopup != null && stringPopup.length() != 0) {
                        stringPopup = "Firmware too old\nPlease upgrade firmware to at least:" + stringPopup;
                        CustomPopupWindow customPopupWindow = new CustomPopupWindow((Context)getActivity());
                        customPopupWindow.popupStart(stringPopup, false);
                    }
                    MainActivity.sharedObjects.versionWarningShown = true;
                }
            }
            MainActivity.csLibrary4A.setPwrManagementMode(true);
        }
    };

    Runnable runnableStartService = new Runnable() {
        boolean DEBUG = false;

        @Override
        public void run() {
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE) = " + ActivityCompat.checkSelfPermission(mContext, WRITE_EXTERNAL_STORAGE));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE)  = " + ActivityCompat.checkSelfPermission(mContext, READ_EXTERNAL_STORAGE));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mContext.checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    MainActivity.csLibrary4A.appendToLog("runnableStartService: requestPermissions WRITE_EXTERNAL_STORAGE"); //
                    requestPermissions(new String[] { WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE }, 1);
                    //Toast.makeText(mContext, com.csl.cslibrary4a.R.string.toast_permission_not_granted, Toast.LENGTH_SHORT).show();
                } else MainActivity.csLibrary4A.appendToLog("runnableStartService: WRITE_EXTERNAL_STORAGE is permitted"); ///
            } else MainActivity.csLibrary4A.appendToLog("runnableStartService: no need to handle WRITE_EXTERNAL_STORAGE");

            LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            MainActivity.csLibrary4A.appendToLog("runnableStartService: locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) = " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) = " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) = " + ActivityCompat.checkSelfPermission(mContext, ACCESS_FINE_LOCATION));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION)  = " + ActivityCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if ((ActivityCompat.checkSelfPermission(mContext, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    CustomAlertDialog appdialog = new CustomAlertDialog();
                    appdialog.Confirm(getActivity(), "Use your location",
                            "This app collects location data in the background.  In terms of the features using this location data in the background, this App collects location data when it is reading RFID tag in all inventory pages.  The purpose of this is to correlate the RFID tag with the actual GNSS(GPS) location of the tag.  In other words, this is to track the physical location of the logistics item tagged with the RFID tag.",
                            "Non merci", "Turn on",
                            new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.csLibrary4A.appendToLog("runnableStartService: allow permission in ACCESS_FINE_LOCATION handler");
                                    MainActivity.csLibrary4A.appendToLog("runnableStartService: requestPermissions ACCESS_FINE_LOCATION");
                                    requestPermissions(new String[] { ACCESS_FINE_LOCATION }, 123); //ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
                                    //if (false) Toast.makeText(mContext, com.csl.cslibrary4a.R.string.toast_permission_not_granted, Toast.LENGTH_SHORT).show();
                                    /*{
                                        LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
                                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == false) {
                                            MainActivity.csLibrary4A.appendToLog("popupAlert: StreamOut: start activity ACTION_LOCATION_SOURCE_SETTINGS");
                                            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            mContext.startActivity(intent1);
                                        }
                                    }*/
                                    //bleEnableRequestShown0 = true; mHandler.postDelayed(mRquestAllowRunnable, 60000);
                                    //bAlerting = false;
                                }
                            },
                            new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.csLibrary4A.appendToLog("runnableStartService: reject permission in ACCESS_FINE_LOCATION handler");
                                    //bAlerting = false;
                                    //bleEnableRequestShown0 = true; mHandler.postDelayed(mRquestAllowRunnable, 60000);
                                }
                            });
                    MainActivity.csLibrary4A.appendToLog("runnableStartService: started ACCESS_FINE_LOCATION handler");
                } else MainActivity.csLibrary4A.appendToLog("runnableStartService: handled ACCESS_FINE_LOCATION");
            } else MainActivity.csLibrary4A.appendToLog("runnableStartService: no need to handle ACCESS_FINE_LOCATION");

            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, BLUETOOTH_CONNECT) = " + ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_CONNECT));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, BLUETOOTH_SCAN)  = " + ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_SCAN));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    MainActivity.csLibrary4A.appendToLog("runnableStartService: requestPermissions BLUETOOTH_SCAN and BLUETOOTH_CONNECT");
                    requestPermissions(new String[] { BLUETOOTH_SCAN, BLUETOOTH_CONNECT }, 123);
                } else MainActivity.csLibrary4A.appendToLog("runnableStartService: handled BLUETOOTH_CONNECT and BLUETOOTH_SCAN");
            } else MainActivity.csLibrary4A.appendToLog("runnableStartService: no need to handle BLUETOOTH_SCAN and BLUETOOTH_CONNECT");

            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, POST_NOTIFICATIONS) = " + ActivityCompat.checkSelfPermission(mContext, POST_NOTIFICATIONS));
            if (NotificationManagerCompat.from(getActivity()).areNotificationsEnabled()) MainActivity.csLibrary4A.appendToLog("Notification is enabled");
            else MainActivity.csLibrary4A.appendToLog("Notification is disabled");
            if (!MainActivity.foregroundServiceEnable) { }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(mContext, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    MainActivity.csLibrary4A.appendToLog("runnableStartService: requestPermissions POST_NOTIFICATIONS");
                    requestPermissions(new String[] { POST_NOTIFICATIONS }, 10); //POST_NOTIFICATIONS, FOREGROUND_SERVICE_LOCATION
                } else {
                    MainActivity.csLibrary4A.appendToLog("runnableStartService: handled POST_NOTIFICATIONS");
                    startService();
                }
            } else {
                MainActivity.csLibrary4A.appendToLog("runnableStartService: no need to handle POST_NOTIFICATIONS");
                startService();
            }
        }
    };
    Intent serviceIntent;
    Service serviceStarted;
    public void startService() {
        ActivityManager activityManager = (ActivityManager) getSystemService(getContext(), ActivityManager.class);
        for (ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            MainActivity.csLibrary4A.appendToLog("MyForegroundService.class.getName = " + MyForegroundService.class.getName() + ", service.service.getClassName = " + service.service.getClassName());
            if (MyForegroundService.class.getName().equals(service.service.getClassName())) {
                return;
            }
        }
        serviceIntent = new Intent(getActivity(), MyForegroundService.class);
        Log.i("Hello", "getActivity is " + (getActivity() == null ? "NULL" : "Valid"));
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        Log.i("Hello", "getContext is " + (getContext() == null ? "NULL" : "Valid"));
        ContextCompat.startForegroundService(getContext(), serviceIntent);
    }
    public void stopService1() {
        //Intent serviceIntent = new Intent(getActivity(), CustomForegroundService.class);
        stopForeground(serviceStarted, STOP_FOREGROUND_REMOVE);
    }

    void popupAlert() {

    }

    public void startConnectionMonitoring1() {
        connectionChecker = new Runnable() {
            @Override
            public void run() {
                updateConnectionStatus1();
                connectionHandler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        connectionHandler.post(connectionChecker);
    }

    public void stopConnectionMonitoring1() {
        connectionHandler.removeCallbacks(connectionChecker);
    }

    public void updateConnectionStatus1() {

        boolean isRfidConnected = MainActivity.csLibrary4A.isBleConnected();
        getActivity().runOnUiThread(() -> {
            if(isRfidConnected){
                CardView cardReaderConnected = (CardView) getActivity().findViewById(R.id.cardReaderConnected);
                cardReaderConnected.setVisibility(View.VISIBLE);
                TextView cardReaderConnectedPower = (TextView) getActivity().findViewById(R.id.cardReaderConnectedPower);
                TextView cardReaderConnectedBattery = (TextView) getActivity().findViewById(R.id.cardReaderConnectedBattery);
                ProgressBar batteryProgress = (ProgressBar) getActivity().findViewById(R.id.batteryProgress);
                cardReaderConnectedPower.setText(String.valueOf(MainActivity.csLibrary4A.getPwrlevel()));
                cardReaderConnectedBattery.setText(MainActivity.csLibrary4A.getBatteryDisplay2());
                batteryProgress.setProgress(Integer.parseInt(MainActivity.csLibrary4A.getBatteryDisplay2().replace("%", "")));
                CardView cardReaderDisconnected = (CardView) getActivity().findViewById(R.id.cardReaderDisconnected);
                cardReaderDisconnected.setVisibility(View.GONE);
            } else {
                CardView cardReaderConnected = (CardView) getActivity().findViewById(R.id.cardReaderConnected);
                cardReaderConnected.setVisibility(View.GONE);
                CardView cardReaderDisconnected = (CardView) getActivity().findViewById(R.id.cardReaderDisconnected);
                cardReaderDisconnected.setVisibility(View.VISIBLE);
            }
        });
    }

}
