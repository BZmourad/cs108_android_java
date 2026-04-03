package com.csl.cs108immo.fragments;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.csl.cs108immo.InventoryBarcodeTask;
import com.csl.cs108immo.MainActivity;
import com.csl.cs108immo.R;
import com.csl.cs108immo.SaveList2ExternalTask;
import com.csl.cs108immo.adapters.ReaderListAdapter;
import com.csl.cslibrary4a.NotificationConnector;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cs108immo.fragments.SettingFragment;
import java.util.Collections;
import com.csl.cs108immo.apps.SharedPrefManager;
import android.content.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import org.json.JSONObject;
import android.widget.ImageView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import com.csl.cs108immo.SettingTask;
import com.csl.cs108immo.app.db.DatabaseHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import com.csl.cs108immo.dialogs.AddImmoDialog;
import com.csl.cs108immo.dialogs.EditImmoDialog;
import android.app.ProgressDialog;
import androidx.fragment.app.FragmentManager;

public class InventoryBarcodeFragment extends CommonFragment {
    private ListView barcodeListView;
    private Button settingBarcodeButton;
    private TextView barcodeEmptyView;
    private TextView barcodeRunTime, barcodeVoltageLevel;
    private TextView barcodeYieldView, barcodeTotal;
    private Button button;
    private Button verifyBarcodeButton;
    private String codeInv;
    private String codeMission;
    private String equipe;
    private String token;
    public Context ctx;
    private String codeEmplacement;
    private ProgressDialog verificationProgressDialog;
    private boolean isVerifying = false;
    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";

    MediaPlayer playerO, playerN;

    private ReaderListAdapter readerListAdapter;

    InventoryBarcodeTask inventoryBarcodeTask;

    void clearTagsList() {
        barcodeYieldView.setText(""); barcodeTotal.setText("");
        MainActivity.sharedObjects.barsList.clear();
        readerListAdapter.notifyDataSetChanged();
    }
    void sortTagsList() {
        Collections.sort(MainActivity.sharedObjects.barsList);
        readerListAdapter.notifyDataSetChanged();
    }
    void saveTagsList() {
        SaveList2ExternalTask saveExternalTask = new SaveList2ExternalTask(MainActivity.sharedObjects.barsList);
        saveExternalTask.execute();
    }
    void shareTagsList() {
        SaveList2ExternalTask saveExternalTask = new SaveList2ExternalTask(MainActivity.sharedObjects.barsList);
        String stringOutput = saveExternalTask.createStrEpcList();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, stringOutput);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Sharing to"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_inventory_barcode, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuAction_clear) {
            clearTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_sortRssi) {
            //sortTagsListByRssi();
            return true;
        } else if (item.getItemId() == R.id.menuAction_sort) {
            sortTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_save) {
            saveTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_share) {
            shareTagsList();
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ctx = getActivity();
        token = SharedPrefManager.getInstance(ctx).getToken();
        if (getArguments() != null) {
            codeInv = getArguments().getString("codeInv");
            codeMission = getArguments().getString("codeMission");
            equipe = getArguments().getString("equipe");
            codeEmplacement = getArguments().getString("codeEmplacement");
        }
        barcodeListView = (ListView) getActivity().findViewById(R.id.inventoryBarcodeList);
        barcodeEmptyView = (TextView) getActivity().findViewById(R.id.inventoryBarcodeEmpty);
        barcodeListView.setEmptyView(barcodeEmptyView);
        readerListAdapter = new ReaderListAdapter(getActivity(), R.layout.readers_list_item, MainActivity.sharedObjects.barsList, true, false);
        barcodeListView.setAdapter(readerListAdapter);
        barcodeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        barcodeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ReaderDevice readerDevice = readerListAdapter.getItem(position);
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("Position  = " + position);
                if (readerDevice.getSelected()) {
                    readerDevice.setSelected(false);
                } else {
                    readerDevice.setSelected(true);
                }
                MainActivity.sharedObjects.barsList.set(position, readerDevice);
                if (readerDevice.getSelected()) MainActivity.tagSelected = readerDevice;
                else MainActivity.tagSelected = null;
                for (int i = 0; i < MainActivity.sharedObjects.barsList.size(); i++) {
                    if (i != position) {
                        ReaderDevice readerDevice1 = MainActivity.sharedObjects.barsList.get(i);
                        if (readerDevice1.getSelected()) {
                            readerDevice1.setSelected(false);
                            MainActivity.sharedObjects.barsList.set(i, readerDevice1);
                        }
                    }
                }
                readerListAdapter.notifyDataSetChanged();
            }
        });

        // NOUVEAU: Gestion de l'ajout manuel de code-barres
        final EditText manualBarcodeInput = (EditText) getActivity().findViewById(R.id.manualBarcodeInput);
        Button addBarcodeButton = (Button) getActivity().findViewById(R.id.addBarcodeButton);

        addBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String barcode = manualBarcodeInput.getText().toString().trim();
                
                if (barcode.isEmpty()) {
                    Toast.makeText(getActivity(), "Veuillez entrer un code-barres", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Vérifier les doublons
                for (ReaderDevice device : MainActivity.sharedObjects.barsList) {
                    if (device.getAddress() != null && device.getAddress().equals(barcode)) {
                        Toast.makeText(getActivity(), "Code-barres déjà existant", Toast.LENGTH_SHORT).show();
                        manualBarcodeInput.setText("");
                        return;
                    }
                }
                
                // Créer et ajouter le nouveau device
                ReaderDevice newDevice = new ReaderDevice(barcode, barcode, false, "", 0, 0.0);
                MainActivity.sharedObjects.barsList.add(newDevice);
                readerListAdapter.notifyDataSetChanged();
                
                // Mettre à jour l'affichage
                if (barcodeTotal != null) {
                    barcodeTotal.setText(String.valueOf(MainActivity.sharedObjects.barsList.size()));
                }
                
                manualBarcodeInput.setText("");
                Toast.makeText(getActivity(), "✓ " + barcode + " ajouté", Toast.LENGTH_SHORT).show();
                barcodeListView.smoothScrollToPosition(MainActivity.sharedObjects.barsList.size() - 1);
            }
        });

        barcodeRunTime = (TextView) getActivity().findViewById(R.id.inventoryBarcodeRunTime);
        barcodeVoltageLevel = (TextView) getActivity().findViewById(R.id.inventoryBarcodeVoltageLevel);

        barcodeYieldView = (TextView) getActivity().findViewById(R.id.inventoryBarcodeYield);
        button = (Button) getActivity().findViewById(R.id.inventoryBarcodeButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopHandler(false);
            }
        });

        barcodeTotal = (TextView) getActivity().findViewById(R.id.inventoryBarcodeTotal);

        Button clearListBarcodeButton = (Button) getActivity().findViewById(R.id.clearListBarcodeButton);
        clearListBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearTagsList();
                Toast.makeText(getActivity(), "Liste vidée", Toast.LENGTH_SHORT).show();
            }
        });

        verifyBarcodeButton = (Button) getActivity().findViewById(R.id.verifyBarcodeButton);
        verifyBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SharedPrefManager.getInstance(ctx).isOnlineMode()) {
                    verifyTagsList();
                } else {
                    verifyTagsListOffline();
                }
            }
        });

        settingBarcodeButton = getActivity().findViewById(R.id.settingBarcodeButton);
        if (settingBarcodeButton != null) {
            settingBarcodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    SettingFragment settingFragment = new SettingFragment();
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content_frame, settingFragment, TAG_CONTENT_FRAGMENT)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        MainActivity.csLibrary4A.getBarcodePreSuffix();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (true) MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment().onResume(): userVisibleHint = " + userVisibleHint);
        if (userVisibleHint) {
            MainActivity.csLibrary4A.setAutoBarStartSTop(true); setNotificationListener();
        }
    }

    @Override
    public void onPause() {
        MainActivity.csLibrary4A.setNotificationListener(null);
        if (inventoryBarcodeTask != null) {
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment().onDestory(): VALID inventoryBarcodeTask");
            inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.DESTORY;
        }
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment().onDestory(): onDestory()");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        MainActivity.csLibrary4A.setAutoBarStartSTop(false); MainActivity.csLibrary4A.setNotificationListener(null);
        super.onDestroy();
    }

    public static InventoryBarcodeFragment newInstance(String codeInv, String codeMission, String equipe, String codeEmplacement) {
        InventoryBarcodeFragment myFragment = new InventoryBarcodeFragment();
        Bundle args = new Bundle();
        args.putString("codeInv", codeInv);
        args.putString("codeMission", codeMission);
        args.putString("equipe", equipe);
        args.putString("codeEmplacement", codeEmplacement);
        myFragment.setArguments(args);
        return myFragment;
    }

    boolean userVisibleHint = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment is now VISIBLE");
            userVisibleHint = true;
            MainActivity.csLibrary4A.setAutoBarStartSTop(true); setNotificationListener();
        } else {
            MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment is now INVISIBLE");
            userVisibleHint = false;
            MainActivity.csLibrary4A.setAutoBarStartSTop(false); MainActivity.csLibrary4A.setNotificationListener(null);
        }
    }

    public InventoryBarcodeFragment() {
        super("InventoryBarcodeFragment");
    }

    void setNotificationListener() {
        MainActivity.csLibrary4A.setNotificationListener(new NotificationConnector.NotificationListener() {
            @Override
            public void onChange() {
                startStopHandler(true);
            }
        });
    }

    void startStopHandler(boolean buttonTrigger) {
        if (buttonTrigger) MainActivity.csLibrary4A.appendToLog("BARTRIGGER: getTriggerButtonStatus = " + MainActivity.csLibrary4A.getTriggerButtonStatus());
        if (MainActivity.sharedObjects.runningInventoryRfidTask) {
            Toast.makeText(MainActivity.mContext, "Running RFID inventory", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean started = false;
        if (inventoryBarcodeTask != null) if (inventoryBarcodeTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        if (buttonTrigger && ((started && MainActivity.csLibrary4A.getTriggerButtonStatus()) || (started == false && MainActivity.csLibrary4A.getTriggerButtonStatus() == false))) {
            MainActivity.csLibrary4A.appendToLog("BARTRIGGER: trigger ignore");
            return;
        }
        if (started == false) {
            if (MainActivity.csLibrary4A.isBleConnected() == false) {
                Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                return;
            }
            if (MainActivity.csLibrary4A.isBarcodeFailure()) {
                Toast.makeText(MainActivity.mContext, "Barcode is disabled", Toast.LENGTH_SHORT).show();
                return;
            }
            MainActivity.csLibrary4A.appendToLog("BARTRIGGER: Start Barcode inventory");
            started = true;
            inventoryBarcodeTask = new InventoryBarcodeTask(MainActivity.sharedObjects.barsList, readerListAdapter, null, barcodeRunTime, barcodeVoltageLevel, barcodeYieldView, button, null, barcodeTotal, false);
            inventoryBarcodeTask.execute();
        } else {
            MainActivity.csLibrary4A.appendToLog("BARTRIGGER: Stop Barcode inventory");
            if (buttonTrigger) inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.BUTTON_RELEASE;
            else    inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.STOP;
        }
    }

    // Helper method to get the ListView
    private ListView getListView() {
        return barcodeListView;
    }

    private void updateVerificationStatusOffline(int position, String tag, int status, String foundInLocation, String expectedLocation) {
        View listItemView = getListView().getChildAt(position - getListView().getFirstVisiblePosition());

        if (listItemView != null) {
            ImageView verifyStatus = listItemView.findViewById(R.id.verify_status);
            ImageButton buttonEditDetail = listItemView.findViewById(R.id.buttonEditDetail);
            ImageButton buttonAddDetail = listItemView.findViewById(R.id.buttonAddDetail);



            switch (status) {
                case 0: // NOT FOUND
                    verifyStatus.setImageResource(R.drawable.ic_not_found);
                    verifyStatus.setVisibility(View.VISIBLE);
                    buttonAddDetail.setVisibility(View.VISIBLE);
                    buttonEditDetail.setVisibility(View.GONE);


                    buttonAddDetail.setOnClickListener(v -> {
                        AddImmoFragment addFragment = AddImmoFragment.newInstance(tag, codeInv, codeMission, equipe);
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_frame, addFragment)
                                .addToBackStack("add_immo")
                                .commit();
                    });
                    break;

                case 1: // FOUND IN CORRECT LOCATION
                    verifyStatus.setImageResource(R.drawable.ic_verified);
                    verifyStatus.setVisibility(View.VISIBLE);
                    buttonEditDetail.setVisibility(View.VISIBLE);
                    buttonAddDetail.setVisibility(View.GONE);


                    buttonEditDetail.setOnClickListener(v -> {
                        EditImmoFragment editFragment = EditImmoFragment.newInstance(tag, codeInv, codeMission, equipe);
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_frame, editFragment)
                                .addToBackStack("edit_immo")
                                .commit();
                    });
                    break;

                case 2: // FOUND IN WRONG LOCATION
                    verifyStatus.setImageResource(R.drawable.ic_warning);
                    verifyStatus.setVisibility(View.VISIBLE);
                    buttonEditDetail.setVisibility(View.VISIBLE);
                    buttonAddDetail.setVisibility(View.GONE);


                    buttonEditDetail.setOnClickListener(v -> {
                        EditImmoFragment editFragment = EditImmoFragment.newInstance(tag, codeInv, codeMission, equipe);
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_frame, editFragment)
                                .addToBackStack("edit_immo")
                                .commit();
                    });
                    break;
            }

            readerListAdapter.notifyDataSetChanged();
        }
    }

    /*************************** */
    private void verifyTagsList() {
        if (isVerifying) {
            Toast.makeText(getActivity(), "Vérification en cours...", Toast.LENGTH_SHORT).show();
            return;
        }

        isVerifying = true;

        // Afficher le loader
        verificationProgressDialog = new ProgressDialog(getActivity());
        verificationProgressDialog.setMessage("Vérification des immobilisations en cours...");
        verificationProgressDialog.setCancelable(false);
        verificationProgressDialog.show();

        final List<ReaderDevice> tagsToVerify = new ArrayList<>();
        for (int i = 0; i < readerListAdapter.getCount(); i++) {
            ReaderDevice device = readerListAdapter.getItem(i);
            if (device != null) {
                tagsToVerify.add(device);
            }
        }

        verifyTagsListAsync(tagsToVerify, 0);
    }

    private void verifyTagsListAsync(final List<ReaderDevice> tags, final int index) {
        if (index >= tags.size()) {
            // Vérification terminée
            if (verificationProgressDialog != null && verificationProgressDialog.isShowing()) {
                verificationProgressDialog.dismiss();
            }
            isVerifying = false;
            Toast.makeText(getActivity(), "Vérification terminée", Toast.LENGTH_SHORT).show();
            return;
        }

        final ReaderDevice device = tags.get(index);
        final String tag = device.getAddress();

        verificationProgressDialog.setMessage("Vérification " + (index + 1) + "/" + tags.size() + " : " + tag);

        new VerifyTagTask(index, tag, new VerifyTagCallback() {
            @Override
            public void onComplete(int position, int status, String foundInLocation, String expectedLocation) {
                updateVerificationStatus(position, tag, status, foundInLocation, expectedLocation);
                // Passer au suivant
                verifyTagsListAsync(tags, index + 1);
            }
        }).execute();
    }

    private void verifyTagsListOffline() {
        if (isVerifying) {
            Toast.makeText(getActivity(), "Vérification en cours...", Toast.LENGTH_SHORT).show();
            return;
        }

        isVerifying = true;

        // Afficher le loader
        verificationProgressDialog = new ProgressDialog(getActivity());
        verificationProgressDialog.setMessage("Vérification des immobilisations en cours (offline)...");
        verificationProgressDialog.setCancelable(false);
        verificationProgressDialog.show();

        final List<ReaderDevice> tagsToVerify = new ArrayList<>();
        for (int i = 0; i < readerListAdapter.getCount(); i++) {
            ReaderDevice device = readerListAdapter.getItem(i);
            if (device != null) {
                tagsToVerify.add(device);
            }
        }

        verifyTagsListOfflineAsync(tagsToVerify, 0);
    }

    private void verifyTagsListOfflineAsync(final List<ReaderDevice> tags, final int index) {
        if (index >= tags.size()) {
            if (verificationProgressDialog != null && verificationProgressDialog.isShowing()) {
                verificationProgressDialog.dismiss();
            }
            isVerifying = false;
            Toast.makeText(getActivity(), "Vérification terminée", Toast.LENGTH_SHORT).show();
            return;
        }

        final ReaderDevice device = tags.get(index);
        final String tag = device.getAddress();

        verificationProgressDialog.setMessage("Vérification " + (index + 1) + "/" + tags.size() + " : " + tag);

        DatabaseHelper dbHelper = new DatabaseHelper(ctx);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT Det_inv_immo.CodeImmo, Det_inv_immo.codeEmpInv, Inv_EquipeInventaire.CodeEmplacement " +
                "FROM Det_inv_immo " +
                "LEFT JOIN Inv_EquipeInventaire ON Det_inv_immo.CodeInventaire = Inv_EquipeInventaire.Codeinv " +
                "AND Det_inv_immo.codeMission = Inv_EquipeInventaire.codemission " +
                "WHERE Det_inv_immo.CodeInventaire = ? " +
                "AND (Det_inv_immo.CodeImmo = ? " +
                "OR Det_inv_immo.NumSerie_Corr = ? " +
                "OR Det_inv_immo.codeBarre_Corr = ?) " +
                "LIMIT 1";
        String[] args = {codeInv, tag, tag, tag};
        Cursor cursor = null;
        String foundInLocation = null;
        String expectedLocation = null;
        boolean found = false;

        try {
            cursor = db.rawQuery(query, args);
            if (cursor != null && cursor.moveToFirst()) {
                found = true;
                foundInLocation = cursor.getString(cursor.getColumnIndexOrThrow("codeEmpInv"));
                expectedLocation = cursor.getString(cursor.getColumnIndexOrThrow("CodeEmplacement"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        int status;
        if (!found) {
            status = 0;
        } else if (foundInLocation != null && foundInLocation.equals(codeEmplacement)) {
            status = 1;
        } else {
            status = 2;
        }

        updateVerificationStatus(index, tag, status, foundInLocation, expectedLocation);

        // Passer au suivant
        verifyTagsListOfflineAsync(tags, index + 1);
    }

    // Interface pour le callback de vérification
    private interface VerifyTagCallback {
        void onComplete(int position, int status, String foundInLocation, String expectedLocation);
    }

    // Modifiez la classe VerifyTagTask
    private class VerifyTagTask extends AsyncTask<Void, Void, JSONObject> {
        private int position;
        private String tag;
        private VerifyTagCallback callback;

        public VerifyTagTask(int position, String tag, VerifyTagCallback callback) {
            this.position = position;
            this.tag = tag;
            this.callback = callback;
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            try {
                String baseUrl = SharedPrefManager.getInstance(getContext()).getServerUrl();
                String urlString = baseUrl + "api-inv-search-cod.awp?log=" + token + "&inv=" + codeInv + "&code=" + tag;
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return new JSONObject(response.toString());
                } else {
                    JSONObject errorObj = new JSONObject();
                    errorObj.put("sRes", "error");
                    return errorObj;
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    JSONObject errorObj = new JSONObject();
                    errorObj.put("sRes", "error");
                    return errorObj;
                } catch (Exception ex) {
                    return null;
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (getActivity() != null && result != null) {
                try {
                    String sRes = result.getString("sRes");
                    String foundInLocation = null;
                    String expectedLocation = null;
                    boolean found = false;

                    if ("ok".equals(sRes) && result.has("tab_srch")) {
                        JSONObject tabSrch = result.getJSONObject("tab_srch");
                        String codeImmo = tabSrch.optString("CodeImmo", "");
                        String codeEmpInv = tabSrch.optString("codeEmpInv", "");
                        if (!"".equals(codeImmo)) {
                            found = true;
                            foundInLocation = codeEmpInv;
                            expectedLocation = codeEmplacement;
                        }
                    }

                    int status;
                    if (!found) {
                        status = 0;
                    } else if (foundInLocation != null && foundInLocation.equals(codeEmplacement)) {
                        status = 1;
                    } else {
                        status = 2;
                    }

                    if (callback != null) {
                        callback.onComplete(position, status, foundInLocation, expectedLocation);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onComplete(position, 0, null, null);
                    }
                }
            }
        }
    }

    // Modifiez updateVerificationStatus
    private void updateVerificationStatus(int position, String tag, int status, String foundInLocation, String expectedLocation) {
        View listItemView = getListView().getChildAt(position - getListView().getFirstVisiblePosition());
        if (listItemView != null) {
            ImageView verifyStatus = listItemView.findViewById(R.id.verify_status);
            ImageButton buttonEditDetail = listItemView.findViewById(R.id.buttonEditDetail);
            ImageButton buttonAddDetail = listItemView.findViewById(R.id.buttonAddDetail);

            switch (status) {
                case 0: // NOT FOUND
                    verifyStatus.setImageResource(R.drawable.ic_not_found);
                    verifyStatus.setVisibility(View.VISIBLE);
                    buttonAddDetail.setVisibility(View.VISIBLE);
                    buttonEditDetail.setVisibility(View.GONE);
                    buttonAddDetail.setOnClickListener(v -> {
                        AddImmoDialog dialog = AddImmoDialog.newInstance(tag, tag,codeInv, codeMission, equipe, codeEmplacement,
                                () -> {
                                    // Re-exécuter la vérification après fermeture
                                    if (isVerifying) {
                                        // Attendre la fin de la vérification en cours
                                        new android.os.Handler().postDelayed(() -> {
                                            if (!isVerifying) {
                                                if (SharedPrefManager.getInstance(ctx).isOnlineMode()) {
                                                    verifyTagsList();
                                                } else {
                                                    verifyTagsListOffline();
                                                }
                                            }
                                        }, 500);
                                    } else {
                                        if (SharedPrefManager.getInstance(ctx).isOnlineMode()) {
                                            verifyTagsList();
                                        } else {
                                            verifyTagsListOffline();
                                        }
                                    }
                                });
                        dialog.show(getChildFragmentManager(), "AddImmoDialog");
                    });
                    break;
                case 1: // FOUND IN CORRECT LOCATION
                    verifyStatus.setImageResource(R.drawable.ic_verified);
                    verifyStatus.setVisibility(View.VISIBLE);
                    buttonEditDetail.setVisibility(View.VISIBLE);
                    buttonAddDetail.setVisibility(View.GONE);
                    buttonEditDetail.setOnClickListener(v -> {
                        EditImmoDialog dialog = EditImmoDialog.newInstance(tag, codeInv, codeMission, equipe,
                                () -> {
                                    // Re-exécuter la vérification après fermeture
                                    if (isVerifying) {
                                        new android.os.Handler().postDelayed(() -> {
                                            if (!isVerifying) {
                                                if (SharedPrefManager.getInstance(ctx).isOnlineMode()) {
                                                    verifyTagsList();
                                                } else {
                                                    verifyTagsListOffline();
                                                }
                                            }
                                        }, 500);
                                    } else {
                                        if (SharedPrefManager.getInstance(ctx).isOnlineMode()) {
                                            verifyTagsList();
                                        } else {
                                            verifyTagsListOffline();
                                        }
                                    }
                                });
                        dialog.show(getChildFragmentManager(), "EditImmoDialog");
                    });
                    break;
                case 2: // FOUND IN WRONG LOCATION
                    verifyStatus.setImageResource(R.drawable.ic_warning);
                    verifyStatus.setVisibility(View.VISIBLE);
                    buttonEditDetail.setVisibility(View.VISIBLE);
                    buttonAddDetail.setVisibility(View.GONE);
                    buttonEditDetail.setOnClickListener(v -> {
                        EditImmoDialog dialog = EditImmoDialog.newInstance(tag, codeInv, codeMission, equipe,
                                () -> {
                                    // Re-exécuter la vérification après fermeture
                                    if (isVerifying) {
                                        new android.os.Handler().postDelayed(() -> {
                                            if (!isVerifying) {
                                                if (SharedPrefManager.getInstance(ctx).isOnlineMode()) {
                                                    verifyTagsList();
                                                } else {
                                                    verifyTagsListOffline();
                                                }
                                            }
                                        }, 500);
                                    } else {
                                        if (SharedPrefManager.getInstance(ctx).isOnlineMode()) {
                                            verifyTagsList();
                                        } else {
                                            verifyTagsListOffline();
                                        }
                                    }
                                });
                        dialog.show(getChildFragmentManager(), "EditImmoDialog");
                    });
                    break;
            }
            readerListAdapter.notifyDataSetChanged();
        }
    }

    // Ajoutez cette méthode pour annuler la vérification si nécessaire
    @Override
    public void onDestroyView() {
        if (verificationProgressDialog != null && verificationProgressDialog.isShowing()) {
            verificationProgressDialog.dismiss();
        }
        isVerifying = false;
        super.onDestroyView();
    }
}
