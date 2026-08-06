package com.csl.cs108immo.fragments;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.csl.cs108immo.CustomPopupWindow;
import com.csl.cs108immo.GenericTextWatcher;
import com.csl.cs108immo.InventoryRfidTask;
import com.csl.cs108immo.MainActivity;
import com.csl.cs108immo.R;
import com.csl.cs108immo.SaveList2ExternalTask;
import com.csl.cs108immo.adapters.ReaderListAdapter;
import com.csl.cslibrary4a.NotificationConnector;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReader;
import com.csl.cslibrary4a.RfidReaderChipData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import com.csl.cs108immo.apps.SharedPrefManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Date;
import java.io.OutputStream;
import android.content.ContentValues;

import org.json.JSONObject;
import android.widget.ImageView;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.csl.cs108immo.SettingTask;
import com.csl.cs108immo.app.db.DatabaseHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import com.csl.cs108immo.dialogs.AddImmoDialog;
import com.csl.cs108immo.dialogs.EditImmoDialog;
import android.app.ProgressDialog;
import androidx.fragment.app.FragmentManager;

import com.csl.cs108immo.dialogs.SearchTagDialog;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LectureFragment extends CommonFragment {
    final private boolean bAdd2End = false;
    private Button settingRfidButton;
    private boolean bMultiBank = false, bMultiBankInventory = false, bctesiusInventory = false;
    private RfidReader.TagType tagType;
    private String mDid = null;
    public Context ctx;
    int vibrateTimeBackup = 0;
    private CheckBox checkBoxDupElim, checkBoxFilterByTid, checkBoxFilterByEpc, checkBoxFilterByProtect;
    private EditText editTextProtectPassword;
    private Spinner spinnerBank1, spinnerBank2;
    private ListView rfidListView;
    private TextView rfidEmptyView;
    private TextView rfidRunTime, rfidVoltageLevel;
    private TextView rfidYieldView;
    private TextView rfidRateView;
    private Button button;
    private Button verifyRfidButton;
    private ReaderListAdapter readerListAdapter;
    private InventoryRfidTask inventoryRfidTask;
    private String codeInv;
    private String codeMission;
    private String equipe;
    private String token;
    private String codeEmplacement;
    private ProgressDialog verificationProgressDialog;
    private boolean isVerifying = false;
    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";
    private Spinner filterSpinner;
    private ArrayAdapter<String> filterAdapter;
    private ArrayList<ReaderDevice> originalTagsList = new ArrayList<>();
    private String currentFilter = "Tous";
    private SearchTagDialog currentSearchDialog;
    private String baseUrl2;

    void clearTagsList() {
        MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment: NOT onOptionsItemSelected: clearTagsList");
        MainActivity.csLibrary4A.appendToLog("runningInventoryRfidTask = " + MainActivity.sharedObjects.runningInventoryRfidTask + ", readerListAdapter" + (readerListAdapter != null ? " tagCount = " + String.valueOf(readerListAdapter.getCount()) : " = NULL"));
        if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
        rfidYieldView.setText("");
        rfidRateView.setText("");
        MainActivity.tagSelected = null;
        MainActivity.sharedObjects.tagsList.clear();
        MainActivity.sharedObjects.tagsIndexList.clear();
        readerListAdapter.notifyDataSetChanged();
        MainActivity.mLogView.setText("");
    }
    void sortTagsList() {
        if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
        Collections.sort(MainActivity.sharedObjects.tagsList);
        readerListAdapter.notifyDataSetChanged();
    }
    void sortTagsListByRssi() {
        if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
        Collections.sort(MainActivity.sharedObjects.tagsList, new Comparator<ReaderDevice>() {
            @Override
            public int compare(ReaderDevice deviceTag, ReaderDevice t1) {
                if (deviceTag.getRssi() == t1.getRssi()) return 0;
                else if (deviceTag.getRssi() < t1.getRssi()) return 1;
                else return -1;
            }
        });
        readerListAdapter.notifyDataSetChanged();
    }
    void saveTagsList() {
        if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
        SaveList2ExternalTask saveExternalTask = new SaveList2ExternalTask(MainActivity.sharedObjects.tagsList);
        saveExternalTask.execute();
    }
    void shareTagsList() {
        SaveList2ExternalTask saveExternalTask = new SaveList2ExternalTask(MainActivity.sharedObjects.tagsList);
        String stringOutput = saveExternalTask.createStrEpcList();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, stringOutput);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Sharing to"));
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, bMultiBankInventory | bctesiusInventory);
        return inflater.inflate(R.layout.fragment_inventory_rfid_multi, container, false);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment: onOptionsItemSelected");
        if (item.getItemId() == R.id.menuAction_clear) {
            clearTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_sortRssi) {
            sortTagsListByRssi();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getActivity();
        token = SharedPrefManager.getInstance(ctx).getToken();
        if (getArguments() != null) {
            bMultiBank = getArguments().getBoolean("bMultiBank");
            mDid = getArguments().getString("mDid");

            codeInv = getArguments().getString("codeInv");
            codeMission = getArguments().getString("codeMission");
            equipe = getArguments().getString("equipe");
            codeEmplacement = getArguments().getString("codeEmplacement");

            try {
                int iValue = getArguments().getInt("enumTagType");
                MainActivity.csLibrary4A.appendToLog("onCreate: enumTagType iValue = " + iValue);
                tagType = RfidReader.TagType.values()[iValue];
                MainActivity.csLibrary4A.appendToLog("onCreate: enumTagType = " + tagType.toString());
            } catch (Exception e) { }
            if (bMultiBank && mDid == null) {
                bMultiBankInventory = true;
            } else if (bMultiBank && mDid != null) {
                if (mDid.matches("E2003")
                        || mDid.matches("E200B0")
                        || mDid.matches("E203510")
                        || mDid.matches("E283A")) {
                    bctesiusInventory = true;
                }
            }
            codeInv = getArguments().getString("codeInv");
            codeMission = getArguments().getString("codeMission");
            equipe = getArguments().getString("equipe");
        }
    }
    private String getSavedPowerLevel() {
        return String.valueOf(MainActivity.csLibrary4A.getPwrlevel());
    }
    private void showPowerSettingsDialog() {
        String savedPowerLevel = getSavedPowerLevel();
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_power_settings, null);

        final EditText powerLevelEditText = dialogView.findViewById(R.id.powerLevelEditText);
        powerLevelEditText.setText(savedPowerLevel);
        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setTitle("Power Settings");

        builder.setPositiveButton("Validate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String powerLevelStr = powerLevelEditText.getText().toString();
                if (!powerLevelStr.isEmpty()) {
                    try {
                        long powerLevel = Long.parseLong(powerLevelStr);
                        if (powerLevel >= 0 && powerLevel <= 300) {
                            savePowerLevel(powerLevel);
                        } else {
                            Toast.makeText(getContext(), "Please enter a value between 0 and 300", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Please enter a power level", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void savePowerLevel(long powerLevel) {
        if (MainActivity.csLibrary4A.isBleConnected() == false) {
            Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
            return;
        } else if (MainActivity.csLibrary4A.isRfidFailure()) {
            Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
            return;
        } else {
            try {
                if (MainActivity.csLibrary4A.getPwrlevel() != powerLevel) {
                    if(MainActivity.csLibrary4A.setPowerLevel(powerLevel) == false){
                        boolean changedChannel = false;
                        SettingTask settingTask;
                        settingTask = new SettingTask(settingRfidButton, false, false);
                        settingTask.execute();
                        MainActivity.csLibrary4A.saveSetting2File();
                        //mHandler.post(updateRunnable);
                        Toast.makeText(MainActivity.mContext, R.string.toast_invalid_range, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Power level set to: " + powerLevel+" ("+getSavedPowerLevel()+")", Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (Exception ex) {
                Toast.makeText(MainActivity.mContext, R.string.toast_invalid_range, Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity.selectFor = -1;
        if (bMultiBankInventory | bctesiusInventory) {
            ActionBar actionBar;
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            //actionBar.setIcon(R.drawable.dl_inv);
            if (bMultiBankInventory) actionBar.setTitle(R.string.title_activity_inventoryRfidMulti);
            else if (bctesiusInventory) {
                if (mDid.matches("E2003")) actionBar.setTitle("Alien");
                else if (mDid.matches("E200B0")) actionBar.setTitle(R.string.title_activity_bap);
                else if (mDid.matches("E203510")) actionBar.setTitle(R.string.title_activity_ctesisu);
                else if (mDid.matches("E283A")) actionBar.setTitle("AS321x");
            }
        }
        if (bMultiBankInventory) {
            LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.inventoryMultibankSetting);
            linearLayout.setVisibility(View.VISIBLE);
            checkBoxDupElim = (CheckBox) getActivity().findViewById(R.id.accessInventoryDupElim);
            checkBoxDupElim.setVisibility(View.VISIBLE);
            checkBoxDupElim.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkBoxDupElim.isChecked()) readerListAdapter.setSelectDupElim(true);
                    else readerListAdapter.setSelectDupElim(false);
                }
            });
        }
        checkBoxFilterByTid = (CheckBox) getActivity().findViewById(R.id.accessInventoryFilterByTid);
        MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.onActivityCreated: mDid = " + mDid + ", MainActivity.mDid = " + MainActivity.mDid);
        if (mDid != null) {
            if (mDid.indexOf("E2827001") == 0) {
                checkBoxFilterByTid.setVisibility(View.VISIBLE);
                //checkBoxFilterByTid.setText("filter FM13DT160 only");
            }
        } else if (bMultiBankInventory == false) {
            checkBoxFilterByEpc = (CheckBox) getActivity().findViewById(R.id.accessInventoryFilterByEpc);
            //checkBoxFilterByEpc.setVisibility(View.VISIBLE);
        }
        checkBoxFilterByProtect = (CheckBox) getActivity().findViewById(R.id.accessInventoryFilterByProtect);
        editTextProtectPassword = (EditText) getActivity().findViewById(R.id.accessInventoryProtectPassword);
        editTextProtectPassword.addTextChangedListener(new GenericTextWatcher(editTextProtectPassword, 8));
        editTextProtectPassword.setText("00000000");
        ArrayAdapter<CharSequence> lockAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.memoryBank_options, R.layout.custom_spinner_layout);
        lockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBank1 = (Spinner) getActivity().findViewById(R.id.accessInventoryBank1);
        spinnerBank1.setAdapter(lockAdapter); spinnerBank1.setSelection(2);
        spinnerBank2 = (Spinner) getActivity().findViewById(R.id.accessInventoryBank2);
        spinnerBank2.setAdapter(lockAdapter); spinnerBank2.setSelection(3);
        rfidListView = (ListView) getActivity().findViewById(R.id.inventoryRfidList1);
        rfidEmptyView = (TextView) getActivity().findViewById(R.id.inventoryRfidEmpty1);
        rfidListView.setEmptyView(rfidEmptyView);
        boolean bSelect4detail = true;
        if (bMultiBankInventory) bSelect4detail = false;
        boolean needDupElim = true;
        boolean need4Extra1 = MainActivity.csLibrary4A.getPortNumber() > 1 ? true : false;
        boolean need4Extra2 = (mDid != null ? true : false);
        //readerListAdapter = new ReaderListAdapter(getActivity(), R.layout.readers_list_item_inventory_2, MainActivity.sharedObjects.tagsList, bSelect4detail, true, needDupElim, need4Extra1, need4Extra2);
        readerListAdapter = new ReaderListAdapter(getActivity(), R.layout.readers_list_item_inventory_2, MainActivity.sharedObjects.tagsList, bSelect4detail, true, needDupElim, need4Extra1, need4Extra2, tagId -> showSearchTagDialog(tagId));
        rfidListView.setAdapter(readerListAdapter);
        rfidListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        rfidListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ReaderDevice readerDevice = readerListAdapter.getItem(position);
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("Position  = " + position);
                if (readerDevice.getSelected()) {
                    readerDevice.setSelected(false);
                } else {
                    readerDevice.setSelected(true);
                    String strPopup = readerDevice.getUpcSerial();
                    MainActivity.csLibrary4A.appendToLog("strPopup = " + (strPopup == null ? "null" : strPopup));
                    if (strPopup != null && strPopup.trim().length() != 0) {
                        strPopup = MainActivity.csLibrary4A.getUpcSerialDetail(strPopup);
                        CustomPopupWindow epcSerialPopupWindow = new CustomPopupWindow(MainActivity.mContext);
                        epcSerialPopupWindow.popupStart(strPopup, false);
                    }
                }
                MainActivity.sharedObjects.tagsList.set(position, readerDevice);
                if (readerDevice.getSelected()) MainActivity.tagSelected = readerDevice;
                else MainActivity.tagSelected = null;
                for (int i = 0; i < MainActivity.sharedObjects.tagsList.size(); i++) {
                    if (i != position) {
                        ReaderDevice readerDevice1 = MainActivity.sharedObjects.tagsList.get(i);
                        if (readerDevice1.getSelected()) {
                            readerDevice1.setSelected(false);
                            MainActivity.sharedObjects.tagsList.set(i, readerDevice1);
                        }
                    }
                }
                readerListAdapter.notifyDataSetChanged();
            }
        });
        rfidRunTime = (TextView) getActivity().findViewById(R.id.inventoryRfidRunTime1);
        rfidVoltageLevel = (TextView) getActivity().findViewById(R.id.inventoryRfidVoltageLevel1);
        TextView rfidFilterOn = (TextView) getActivity().findViewById(R.id.inventoryRfidFilterOn1);
        if (mDid != null || (MainActivity.csLibrary4A.getSelectEnable() == false && MainActivity.csLibrary4A.getInvMatchEnable() == false && MainActivity.csLibrary4A.getRssiFilterEnable() == false))
            rfidFilterOn.setVisibility(View.GONE);
        rfidYieldView = (TextView) getActivity().findViewById(R.id.inventoryRfidYield1);
        rfidRateView = (TextView) getActivity().findViewById(R.id.inventoryRfidRate1);
        button = (Button) getActivity().findViewById(R.id.inventoryRfidButton1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopHandler(false);
            }
        });
        verifyRfidButton = (Button) getActivity().findViewById(R.id.verifyRfidButton);
        verifyRfidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SharedPrefManager.getInstance(ctx).isOnlineMode()) {
                    verifyTagsList();
                } else {
                    verifyTagsListOffline();
                }
            }
        });
        Button clearListButton = (Button) getActivity().findViewById(R.id.clearListButton);
        clearListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearTagsList();
                Toast.makeText(getActivity(), "Liste vidée", Toast.LENGTH_SHORT).show();
            }
        });
        vibrateTimeBackup = MainActivity.csLibrary4A.getVibrateTime();
        final Button buttonT1 = (Button) getActivity().findViewById(R.id.inventoryRfidButtonT1);
        buttonT1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = buttonT1.getText().toString().trim();
                if (buttonText.toUpperCase().matches("BUZ")) {
                    MainActivity.csLibrary4A.appendToLog("setVibrateOn G 1");
                    MainActivity.csLibrary4A.setVibrateTime(0); MainActivity.csLibrary4A.setVibrateOn(1);
                    buttonT1.setText("STOP");
                }
                else {
                    MainActivity.csLibrary4A.appendToLog("setVibrateOn H 0");
                    MainActivity.csLibrary4A.setVibrateOn(0);
                    buttonT1.setText("BUZ");
                }
            }
        });

        settingRfidButton = getActivity().findViewById(R.id.settingRfidButton);
        if (settingRfidButton != null) {
            settingRfidButton.setOnClickListener(new View.OnClickListener() {
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
        Button submitRfidButton = (Button) getActivity().findViewById(R.id.SubmitRfidButton);
        submitRfidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitCheckedTags();
            }
        });
        baseUrl2 = SharedPrefManager.getInstance(ctx).getServerUrl();
        setupFilterSpinner();
    }
    @Override
    public void onResume() {
        super.onResume();
        clearTagsList();
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("LectureFragment().onResume(): userVisibleHint = " + userVisibleHint);
        if (userVisibleHint) setNotificationListener();
    }
    @Override
    public void onPause() {
        MainActivity.csLibrary4A.setNotificationListener(null);
        super.onPause();
    }
    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(runnableCheckReady);
        MainActivity.csLibrary4A.setNotificationListener(null);
        if (inventoryRfidTask != null) {
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("LectureFragment().onDestory(): VALID inventoryRfidTask");
            inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.DESTORY;
            MainActivity.csLibrary4A.abortOperation(); //added in case inventoryRiidTask is removed
        }
        MainActivity.csLibrary4A.setSameCheck(true);
        MainActivity.csLibrary4A.setInvBrandId(false);
        resetSelectData();
        MainActivity.csLibrary4A.setVibrateTime(vibrateTimeBackup);
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("LectureFragment().onDestory(): onDestory()");
        super.onDestroy();
    }
    boolean userVisibleHint = true;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            userVisibleHint = true;
            MainActivity.csLibrary4A.appendToLog("LectureFragment is now VISIBLE");
            MainActivity.csLibrary4A.appendToLog("11 mDid = " + mDid + ", MainActivity.mDid = " + MainActivity.mDid);
            if (MainActivity.mDid != null && MainActivity.mDid.indexOf("E28011") == 0) {
                int iValue = Integer.valueOf(MainActivity.mDid.substring(6, 8), 16);
                iValue &= 0x0F;
                MainActivity.csLibrary4A.appendToLog(String.format("iValue = 0x%X", iValue));
                if (iValue > 0 && iValue < 6) {
                    checkBoxFilterByProtect.setVisibility(View.VISIBLE);
                    editTextProtectPassword.setVisibility(View.VISIBLE);
                } else {
                    checkBoxFilterByProtect.setVisibility(View.GONE); checkBoxFilterByProtect.setChecked(false);
                    editTextProtectPassword.setVisibility(View.GONE);
                }
            }
            MainActivity.csLibrary4A.appendToLog("setNotificationListener in multibank inventory");
            setNotificationListener();
        } else {
            userVisibleHint = false;
            MainActivity.csLibrary4A.appendToLog("LectureFragment is now INVISIBLE");
            MainActivity.csLibrary4A.appendToLog("setNotificationListener null in multibank inventory");
            MainActivity.csLibrary4A.setNotificationListener(null);
            if (inventoryRfidTask != null) {
                inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
            }
        }
    }
    public static LectureFragment newInstance(boolean bMultiBank, RfidReader.TagType tagType, String mDid, String codeInv, String codeMission, String equipe, String codeEmplacement) {
        LectureFragment myFragment = new LectureFragment();
        Bundle args = new Bundle();
        MainActivity.csLibrary4A.appendToLog("newInstance: enumTagType = " + (tagType == null ? "null" : tagType.toString()));
        if (tagType != null) args.putInt("enumTagType", tagType.ordinal());
        args.putBoolean("bMultiBank", bMultiBank);
        args.putString("mDid", mDid);
        
        // Add the new parameters
        args.putString("codeInv", codeInv);
        args.putString("codeMission", codeMission);
        args.putString("equipe", equipe);
        args.putString("codeEmplacement", codeEmplacement);
        
        myFragment.setArguments(args);
        return myFragment;
    }
    public LectureFragment() {
        super("LectureFragment");
    }
    void setNotificationListener() {
        // Don't set listener if search dialog is open
        if (currentSearchDialog != null && currentSearchDialog.isVisible()) {
            MainActivity.csLibrary4A.appendToLog("setNotificationListener blocked - search dialog is open");
            return;
        }
        
        MainActivity.csLibrary4A.appendToLog("setNotificationListener A in multibank inventory");
        MainActivity.csLibrary4A.setNotificationListener(new NotificationConnector.NotificationListener() {
            @Override
            public void onChange() {
                // Check again if dialog is open
                if (currentSearchDialog != null && currentSearchDialog.isVisible()) {
                    MainActivity.csLibrary4A.appendToLog("onChange blocked - search dialog is open");
                    return;
                }
                MainActivity.csLibrary4A.appendToLog("setNotificationListener TRIGGER key is pressed in multibank inventory.");
                startStopHandler(true);
            }
        });
    }
    boolean needResetData = false;
    void resetSelectData() {
        MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.resetSelectData: mDid = " + mDid + ", MainActivity.mDid = " + MainActivity.mDid);
        if (MainActivity.mDid != null && MainActivity.mDid.indexOf("E282405") == 0) { }
        else MainActivity.csLibrary4A.restoreAfterTagSelect();
        if (needResetData) {
            MainActivity.csLibrary4A.setTagRead(0);
            MainActivity.csLibrary4A.setAccessBank(1);
            MainActivity.csLibrary4A.setAccessOffset(0);
            MainActivity.csLibrary4A.setAccessCount(0);
            needResetData = false;
        }
        if (mDid != null && mDid.matches("E203510")) MainActivity.csLibrary4A.setSelectCriteriaDisable(1);
    }
    void startStopHandler(boolean buttonTrigger) {
        // Don't allow start/stop if search dialog is open
        if (currentSearchDialog != null && currentSearchDialog.isVisible()) {
            MainActivity.csLibrary4A.appendToLog("startStopHandler blocked - search dialog is open");
            return;
        }
        MainActivity.csLibrary4A.appendToLog("0 buttonTrigger is " + buttonTrigger);
        if (buttonTrigger) MainActivity.csLibrary4A.appendToLog("BARTRIGGER: getTriggerButtonStatus = " + MainActivity.csLibrary4A.getTriggerButtonStatus());
        if (MainActivity.sharedObjects.runningInventoryBarcodeTask) {
            Toast.makeText(MainActivity.mContext, "Running barcode inventory", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean started = false;
        if (inventoryRfidTask != null) if (inventoryRfidTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        if (buttonTrigger && ((started && MainActivity.csLibrary4A.getTriggerButtonStatus()) || (started == false && MainActivity.csLibrary4A.getTriggerButtonStatus() == false))) {
            MainActivity.csLibrary4A.appendToLog("BARTRIGGER: trigger ignore");
            return;
        }
        if (started == false) {
            if (MainActivity.csLibrary4A.isBleConnected() == false) {
                Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                return;
            } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                return;
            } else if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0) {
                Toast.makeText(MainActivity.mContext, R.string.toast_not_ready, Toast.LENGTH_SHORT).show();
                mHandler.post(runnableCheckReady);
                return;
            }
            if (bAdd2End) rfidListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            else rfidListView.setSelection(0);
            startInventoryTask();
        } else if (MainActivity.csLibrary4A.mrfidToWriteSize() == 0) {
            if (bAdd2End) rfidListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
            if (buttonTrigger) inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.BUTTON_RELEASE;
            else    inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
            MainActivity.csLibrary4A.appendToLogView("CANCELLING: StartStopHandler generates taskCancelReason = " + inventoryRfidTask.taskCancelReason.toString());
        } else MainActivity.csLibrary4A.appendToLog("BtData. Stop when still writing !!!");
    }
    void startInventoryTask() {
        MainActivity.csLibrary4A.appendToLog("startInventoryTask");
        //RfidReader.ExtraBankData extraBankData = new RfidReader.ExtraBankData();
        String mDid = this.mDid;
        MainActivity.csLibrary4A.appendToLog("Rin: mDid = " + (mDid == null ? "null" : mDid) + ", MainActivity.mDid = " + MainActivity.mDid + ", bMultiBankInventory = " + bMultiBankInventory);
        if (mDid != null && MainActivity.mDid != null) {
            if (MainActivity.mDid.indexOf("E280B12") != 0) mDid = MainActivity.mDid;
        }
        RfidReader.ExtraBankData extraBankData = new RfidReader.ExtraBankData();
        if (mDid != null) extraBankData.setExtraBankData(MainActivity.tagType, mDid);
        else if (bMultiBank) {
            CheckBox checkBox = (CheckBox) getActivity().findViewById(R.id.accessInventoryBankTitle1);
            int extra1Bank = 0, extra2Bank = 0;
            int extra1Count, extra2Count;
            int extra1Offset = 0, extra2Offset = 0;
            if (checkBox.isChecked()) {
                extra1Bank = spinnerBank1.getSelectedItemPosition();
                EditText editText = (EditText) getActivity().findViewById(R.id.accessInventoryOffset1);
                extra1Offset = Integer.valueOf(editText.getText().toString());
                editText = (EditText) getActivity().findViewById(R.id.accessInventoryLength1);
                extra1Count = Integer.valueOf(editText.getText().toString());
            } else extra1Count = 0;
            checkBox = (CheckBox) getActivity().findViewById(R.id.accessInventoryBankTitle2);
            if (checkBox.isChecked()) {
                extra2Bank = spinnerBank2.getSelectedItemPosition();
                EditText editText = (EditText) getActivity().findViewById(R.id.accessInventoryOffset2);
                extra2Offset = Integer.valueOf(editText.getText().toString());
                editText = (EditText) getActivity().findViewById(R.id.accessInventoryLength2);
                extra2Count = Integer.valueOf(editText.getText().toString());
            } else extra2Count = 0;
            extraBankData.setExtraBankData(extra1Bank, extra1Count, extra1Offset, extra2Bank, extra2Count, extra2Offset);
        }
        if (mDid != null) {
            MainActivity.csLibrary4A.appendToLog("mDid is valid as " + mDid);
            if (MainActivity.mDid != null && mDid.length() == 0) mDid = MainActivity.mDid;
            MainActivity.csLibrary4A.appendToLog("new mDid is " + mDid);
            MainActivity.csLibrary4A.appendToLog("mDid = " + mDid);
            if (mDid.matches("E203510")) {
                if (MainActivity.csLibrary4A.get98XX() == 2) MainActivity.csLibrary4A.setCurrentLinkProfile(1);
            } else if (mDid.matches("E283A")) {
                if (MainActivity.csLibrary4A.get98XX() == 2) MainActivity.csLibrary4A.setCurrentLinkProfile(9);
            } else if (mDid.matches("E282405")) {
                if (MainActivity.csLibrary4A.getRetryCount() < 2) MainActivity.csLibrary4A.setRetryCount(2);
            }
            boolean bNeedSelectedTagByTID = true;
            if (mDid.indexOf("E2806894") == 0) {
                Log.i(TAG, "HelloK: Find E2806894 with MainActivity.mDid = " + MainActivity.mDid + ", mDid = " + mDid);
                if (mDid.matches("E2806894A")) {
                    Log.i(TAG, "HelloK: Find E2806894A");
                    MainActivity.csLibrary4A.setInvBrandId(false);
                } else if (mDid.matches("E2806894B")) {
                    Log.i(TAG, "HelloK: Find E2806894B");
                    MainActivity.csLibrary4A.setInvBrandId(false);
                    if (true) bNeedSelectedTagByTID = false;
                } else if (mDid.matches("E2806894C") || mDid.matches("E2806894d")) {
                    Log.i(TAG, "HelloK: Find " + MainActivity.mDid);
                    MainActivity.csLibrary4A.setInvBrandId(true);
                    if (true) bNeedSelectedTagByTID = false;
                }
            }
            int iValue123 = MainActivity.csLibrary4A.setSelectData(MainActivity.tagType, mDid,
                    bNeedSelectedTagByTID && checkBoxFilterByTid.isChecked(),
                    (checkBoxFilterByProtect.isChecked() ? editTextProtectPassword.getText().toString(): null),
                    MainActivity.selectFor, MainActivity.selectHold);
            if (mDid.indexOf("E2806894") == 0) {
                mDid = "E2806894";
            }
            if (iValue123 > 0) MainActivity.selectFor = iValue123;
        }
        MainActivity.csLibrary4A.appendToLog("bSgtinOnly: bMultiBank is " + bMultiBank + ", checkBoxFilterByEpc is " + (checkBoxFilterByEpc == null ? "null" : checkBoxFilterByEpc.isChecked()));
        if (bMultiBank == false) {
            if (checkBoxFilterByEpc.isChecked()) {
                MainActivity.csLibrary4A.appendToLog("bSgtinOnly: clearTagList");
                clearTagsList();
            }
            MainActivity.csLibrary4A.restoreAfterTagSelect();
            inventoryRfidTask = new InventoryRfidTask(getContext(), -1, -1, 0, 0, 0, 0,
                    false, MainActivity.csLibrary4A.getInventoryBeep(),
                    MainActivity.sharedObjects.tagsList, readerListAdapter, null, null,
                    rfidRunTime, null, rfidVoltageLevel, rfidYieldView, button, rfidRateView);
            inventoryRfidTask.bSgtinOnly = checkBoxFilterByEpc.isChecked();
            MainActivity.csLibrary4A.appendToLog("Debug_Compact 1: InventoryRfidMultiFragment.startInventoryTask");
            MainActivity.csLibrary4A.startOperation(RfidReaderChipData.OperationTypes.TAG_INVENTORY_COMPACT);
        } else {
            boolean inventoryUcode8_bc = mDid != null && mDid.matches("E2806894") && MainActivity.mDid != null && (MainActivity.mDid.matches("E2806894B") || MainActivity.mDid.matches("E2806894C"));
            if ((extraBankData.extra1Bank != -1 && extraBankData.extra1Count != 0) || (extraBankData.extra2Bank != -1 && extraBankData.extra2Count != 0)) {
                extraBankData.adjustExtraBank1();
                MainActivity.csLibrary4A.appendToLog("HelloK: mDid = " + mDid + ", MainActivity.mDid = " + MainActivity.mDid + " with extra1Bank = " + extraBankData.extra1Bank + "," + extraBankData.extra1Offset + "," + extraBankData.extra1Count + ", extra2Bank = " + extraBankData.extra2Bank + "," + extraBankData.extra2Offset + "," + extraBankData.extra2Count);
                if (mDid != null) MainActivity.csLibrary4A.setResReadNoReply(mDid.matches("E281D"));
                if (inventoryUcode8_bc == false) {
                    MainActivity.csLibrary4A.appendToLog("BleStreamOut: Set Multibank");
                    MainActivity.csLibrary4A.setTagRead(extraBankData.extra2Count != 0 && extraBankData.extra2Count != 0 ? 2 : 1);
                    MainActivity.csLibrary4A.setAccessBank(extraBankData.extra1Bank, extraBankData.extra2Bank);
                    MainActivity.csLibrary4A.setAccessOffset(extraBankData.extra1Offset, extraBankData.extra2Offset);
                    MainActivity.csLibrary4A.setAccessCount(extraBankData.extra1Count, extraBankData.extra2Count);
                    needResetData = true;
                } else if (needResetData) {
                    MainActivity.csLibrary4A.setTagRead(0);
                    MainActivity.csLibrary4A.setAccessBank(1);
                    MainActivity.csLibrary4A.setAccessOffset(0);
                    MainActivity.csLibrary4A.setAccessCount(0);
                    needResetData = false;
                }
            } else resetSelectData();
            MainActivity.csLibrary4A.appendToLog("startInventoryTask: going to startOperation with extra1Bank = " + extraBankData.extra1Bank + "," + extraBankData.extra1Offset + "," + extraBankData.extra1Count + ", extra2Bank = " + extraBankData.extra2Bank + "," + extraBankData.extra2Offset + "," + extraBankData.extra2Count);
            inventoryRfidTask = new InventoryRfidTask(getContext(), extraBankData.extra1Bank, extraBankData.extra2Bank, extraBankData.extra1Count, extraBankData.extra2Count, extraBankData.extra1Offset, extraBankData.extra2Offset,
                    false, MainActivity.csLibrary4A.getInventoryBeep(),
                    MainActivity.sharedObjects.tagsList, readerListAdapter, null, mDid,
                    rfidRunTime, null, rfidVoltageLevel, rfidYieldView, button, rfidRateView);
            inventoryRfidTask.bProtectOnly = checkBoxFilterByProtect.isChecked();
            if (inventoryUcode8_bc) {
                MainActivity.csLibrary4A.appendToLog("Debug_Compact 2: InventoryRfidMultiFragment.startInventoryTask");
                MainActivity.csLibrary4A.startOperation(RfidReaderChipData.OperationTypes.TAG_INVENTORY_COMPACT);
            } else {
                MainActivity.csLibrary4A.appendToLog("Debug_Compact 3: InventoryRfidMultiFragment.startInventoryTask");
                MainActivity.csLibrary4A.startOperation(RfidReaderChipData.OperationTypes.TAG_INVENTORY);
            }
        }
        inventoryRfidTask.execute();
    }
    private final Runnable runnableCheckReady = new Runnable() {
        @Override
        public void run() {
            if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0) {
                button.setEnabled(false);
                button.setText("Please wait");
                MainActivity.csLibrary4A.setNotificationListener(null);
                mHandler.postDelayed(runnableCheckReady, 500);
            } else {
                button.setText("Démarrer");
                button.setEnabled(true);
                if (userVisibleHint) setNotificationListener();
            }
        }
    };

    // Helper method to get the ListView
    private ListView getListView() {
        return rfidListView;
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
            public void onComplete(int position, int status, String foundInLocation, String expectedLocation, int issync) {
                updateVerificationStatus(position, tag, status, foundInLocation, expectedLocation,issync);
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
        
        String query = "SELECT Det_inv_immo.issync, Det_inv_immo.CodeImmo, Det_inv_immo.codeEmpInv, Inv_EquipeInventaire.CodeEmplacement " +
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
        int issync = 0;
        boolean found = false;
        
        try {
            cursor = db.rawQuery(query, args);
            if (cursor != null && cursor.moveToFirst()) {
                found = true;
                foundInLocation = cursor.getString(cursor.getColumnIndexOrThrow("codeEmpInv"));
                expectedLocation = cursor.getString(cursor.getColumnIndexOrThrow("CodeEmplacement"));
                issync = cursor.getInt(cursor.getColumnIndexOrThrow("issync"));
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
        
        updateVerificationStatus(index, tag, status, foundInLocation, expectedLocation,issync);
        
        // Passer au suivant
        verifyTagsListOfflineAsync(tags, index + 1);
    }

    // Interface pour le callback de vérification
    private interface VerifyTagCallback {
        //void onComplete(int position, int status, String foundInLocation, String expectedLocation);

        void onComplete(int position, int status, String foundInLocation, String expectedLocation, int issync);
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
                    int issync = 0;

                    if ("ok".equals(sRes) && result.has("tab_srch")) {
                        JSONObject tabSrch = result.getJSONObject("tab_srch");
                        String codeImmo = tabSrch.optString("CodeImmo", "");
                        String codeEmpInv = tabSrch.optString("codeEmpInv", "");
                        issync = tabSrch.optInt("issync", 0);
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
                        callback.onComplete(position, status, foundInLocation, expectedLocation,issync);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onComplete(position, 0, null, null,0);
                    }
                }
            }
        }
    }

    // Modifiez updateVerificationStatus
    private void updateVerificationStatus(int position, String tag, int status, String foundInLocation, String expectedLocation, int isverified) {
        View listItemView = getListView().getChildAt(position - getListView().getFirstVisiblePosition());
        if (listItemView != null) {
            ImageView verifyStatus = listItemView.findViewById(R.id.verify_status);
            ImageView invStatus = listItemView.findViewById(R.id.inv_status);
            ImageButton buttonEditDetail = listItemView.findViewById(R.id.buttonEditDetail);
            ImageButton buttonAddDetail = listItemView.findViewById(R.id.buttonAddDetail);
            CheckBox reader_checkbox = listItemView.findViewById(R.id.reader_checkbox);
            if (isverified == 1){
                invStatus.setVisibility(View.VISIBLE);
            } else{
                invStatus.setVisibility(View.GONE);
            }
            switch (status) {
                case 0: // NOT FOUND
                    reader_checkbox.setChecked(false);
                    reader_checkbox.setEnabled(false);
                    verifyStatus.setImageResource(R.drawable.ic_not_found);
                    verifyStatus.setVisibility(View.VISIBLE);
                    buttonAddDetail.setVisibility(View.VISIBLE);
                    buttonEditDetail.setVisibility(View.GONE);
                    buttonAddDetail.setOnClickListener(v -> {
                        AddImmoDialog dialog = AddImmoDialog.newInstance(tag, "",codeInv, codeMission, equipe, codeEmplacement,
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
                    reader_checkbox.setChecked(true);
                    reader_checkbox.setEnabled(true);
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
                    reader_checkbox.setChecked(true);
                    reader_checkbox.setEnabled(true);
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

    private void setupFilterSpinner() {
        filterSpinner = getActivity().findViewById(R.id.filterSpinner);

        String[] filterOptions = {
                "Tous",
                "Non trouvé",
                "Trouvé dans la bonne endroit",
                "Trouvé avec fausse emplacement",
                "Non vérifié"
        };

        filterAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, filterOptions);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);

    }

    // Add this method to show search dialog
    public void showSearchTagDialog(String tagId) {
        try {
            // Stop any ongoing inventory before opening dialog
            if (MainActivity.sharedObjects.runningInventoryRfidTask) {
                if (inventoryRfidTask != null) {
                    inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
                }
                MainActivity.csLibrary4A.abortOperation();
            }
            
            // Disable notification listener while dialog is open
            MainActivity.csLibrary4A.setNotificationListener(null);
            
            // Create and show dialog
            SearchTagDialog dialog = SearchTagDialog.newInstance(tagId);
            dialog.show(getChildFragmentManager(), "SearchTagDialog");
            currentSearchDialog = dialog;
            
        } catch (Exception e) {
            MainActivity.csLibrary4A.appendToLog("Error showing search dialog: " + e.getMessage());
            Toast.makeText(getActivity(), "Error opening search dialog", Toast.LENGTH_SHORT).show();
        }
    }

    // Add method to restore notification listener
    public void restoreNotificationListener() {
        currentSearchDialog = null;
        if (userVisibleHint && getUserVisibleHint()) {
            setNotificationListener();
        }
    }

    private void submitCheckedTags() {
        List<ReaderDevice> checkedTags = new ArrayList<>();
        
        // Collect all checked tags
        for (int i = 0; i < readerListAdapter.getCount(); i++) {
            ReaderDevice device = readerListAdapter.getItem(i);
            if (device != null) {
                // Get the view for this position to check the checkbox state
                View listItemView = getListView().getChildAt(i - getListView().getFirstVisiblePosition());
                if (listItemView != null) {
                    CheckBox readerCheckbox = listItemView.findViewById(R.id.reader_checkbox);
                    if (readerCheckbox != null && readerCheckbox.isChecked() && readerCheckbox.isEnabled()) {
                        checkedTags.add(device);
                    }
                }
            }
        }
        
        if (checkedTags.isEmpty()) {
            Toast.makeText(getActivity(), "Aucun tag sélectionné", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress dialog
        ProgressDialog submitProgressDialog = new ProgressDialog(getActivity());
        submitProgressDialog.setMessage("Validation en cours...");
        submitProgressDialog.setCancelable(false);
        submitProgressDialog.show();
        
        // Process tags one by one
        submitCheckedTagsAsync(checkedTags, 0, submitProgressDialog);
    }

    private void submitCheckedTagsAsync(final List<ReaderDevice> tags, final int index, final ProgressDialog progressDialog) {
        if (index >= tags.size()) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Toast.makeText(getActivity(), "Validation terminée", Toast.LENGTH_SHORT).show();
            return;
        }
        
        final ReaderDevice device = tags.get(index);
        final String tag = device.getAddress();
        
        progressDialog.setMessage("Validation " + (index + 1) + "/" + tags.size() + " : " + tag);
        
        if (SharedPrefManager.getInstance(ctx).isOnlineMode()) {
            submitTagOnline(tag, new SubmitCallback() {
                @Override
                public void onComplete(boolean success, String message) {
                    if (success) {
                        // Update UI to show success
                        //updateTagSubmitStatus(index, tag, true);
                    }
                    // Continue with next tag
                    submitCheckedTagsAsync(tags, index + 1, progressDialog);
                }
            });
        } else {
            submitTagOffline(tag, new SubmitCallback() {
                @Override
                public void onComplete(boolean success, String message) {
                    if (success) {
                        // Update UI to show success
                        //updateTagSubmitStatus(index, tag, true);
                    }
                    // Continue with next tag
                    submitCheckedTagsAsync(tags, index + 1, progressDialog);
                }
            });
        }
    }

    private interface SubmitCallback {
        void onComplete(boolean success, String message);
    }

    private void submitTagOnline(String tag, SubmitCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                // First, get the immobilisation details
                String searchUrl = baseUrl2 + "api-inv-search-cod.awp?log=" + token + "&inv=" + codeInv + "&code=" + tag;
                URL url = new URL(searchUrl);
                connection = (HttpURLConnection) url.openConnection();
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
                connection.disconnect();
                
                JSONObject result = new JSONObject(response.toString());
                
                if ("ok".equals(result.getString("sRes")) && result.has("tab_srch")) {
                    JSONObject tabSrch = result.getJSONObject("tab_srch");
                    String codeImmo = tabSrch.getString("CodeImmo");
                    String currentDescription = tabSrch.optString("Descp_immo", "");
                    String currentCodeBarre = tabSrch.optString("codeBarre_inv", "");
                    String currentNumSerie = tabSrch.optString("num_serie_inv", "");
                    String currentEtatCode = tabSrch.optString("codeEtat_Corr", "");
                    String currentEmplacementCode = tabSrch.optString("codeEmpInv", "");
                    String currentNote = tabSrch.optString("Observation", "");
                    
                    // Update with current emplacement (validate)
                    String username = SharedPrefManager.getInstance(ctx).getUsername();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String dateheurenow = sdf.format(new Date());
                    
                    URL updateUrl = new URL(baseUrl2 + "api-update-det-inv.awp?log=" + token + "&inv=" + codeInv + "&immo=" + codeImmo);
                    connection = (HttpURLConnection) updateUrl.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("user", username);
                    jsonParam.put("desc", currentDescription);
                    jsonParam.put("note", currentNote);
                    jsonParam.put("code_barre", currentCodeBarre);
                    jsonParam.put("num_serie", currentNumSerie);
                    jsonParam.put("code_etat", currentEtatCode);
                    jsonParam.put("code_emp", currentEmplacementCode); // Keep same emplacement (validation)
                    jsonParam.put("mission", codeMission);
                    jsonParam.put("equipe", equipe);
                    
                    OutputStream os = connection.getOutputStream();
                    os.write(jsonParam.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();
                    
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader updateReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder updateResponse = new StringBuilder();
                        while ((line = updateReader.readLine()) != null) {
                            updateResponse.append(line);
                        }
                        updateReader.close();
                        
                        JSONObject updateResult = new JSONObject(updateResponse.toString());
                        if ("ok".equals(updateResult.optString("sRes"))) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> callback.onComplete(true, "OK"));
                            }
                            return;
                        }
                    }
                }
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> callback.onComplete(false, "Échec"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> callback.onComplete(false, e.getMessage()));
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void submitTagOffline(String tag, SubmitCallback callback) {
        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(ctx);
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                
                // Find the immobilisation
                String query = "SELECT CodeImmo, Descp_immo, codeBarre_Corr, NumSerie_Corr, " +
                            "codeEtat_Corr, Observation, codeEmpInv " +
                            "FROM Det_inv_immo " +
                            "WHERE CodeInventaire = ? " +
                            "AND (CodeImmo = ? OR NumSerie_Corr = ? OR codeBarre_Corr = ?)";
                String[] args = {codeInv, tag, tag, tag};
                Cursor cursor = null;
                
                try {
                    cursor = db.rawQuery(query, args);
                    if (cursor.moveToFirst()) {
                        String codeImmo = cursor.getString(0);
                        String currentDescription = cursor.getString(1);
                        String currentCodeBarre = cursor.getString(2);
                        String currentNumSerie = cursor.getString(3);
                        String currentEtatCode = cursor.getString(4);
                        String currentNote = cursor.getString(5);
                        String currentEmp = cursor.getString(6);
                        
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String dateheurenow = sdf.format(new Date());
                        String userCode = SharedPrefManager.getInstance(ctx).getUserCode();
                        
                        ContentValues values = new ContentValues();
                        values.put("DateValidation", dateheurenow);
                        values.put("Uservalidation", userCode);
                        values.put("EtatCorrection", 1); // Validated
                        values.put("issync", 2); // Mark as modified for sync
                        
                        String whereClause = "CodeInventaire = ? AND CodeImmo = ?";
                        String[] whereArgs = {codeInv, codeImmo};
                        
                        int rowsAffected = db.update(DatabaseHelper.TABLE_DET_INV_IMMO, values, whereClause, whereArgs);
                        
                        if (getActivity() != null) {
                            final boolean success = rowsAffected > 0;
                            getActivity().runOnUiThread(() -> callback.onComplete(success, success ? "OK" : "Échec"));
                        }
                        return;
                    }
                } finally {
                    if (cursor != null) cursor.close();
                }
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> callback.onComplete(false, "Non trouvé"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> callback.onComplete(false, e.getMessage()));
                }
            } finally {
                if (db != null) db.close();
            }
        }).start();
    }

    private void updateTagSubmitStatus(int position, String tag, boolean success) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                View listItemView = getListView().getChildAt(position - getListView().getFirstVisiblePosition());
                if (listItemView != null) {
                    ImageView verifyStatus = listItemView.findViewById(R.id.verify_status);
                    ImageView invStatus = listItemView.findViewById(R.id.inv_status);
                    CheckBox readerCheckbox = listItemView.findViewById(R.id.reader_checkbox);
                    
                    if (success) {
                        verifyStatus.setImageResource(R.drawable.ic_verified);
                        verifyStatus.setVisibility(View.VISIBLE);
                        invStatus.setVisibility(View.VISIBLE);
                        readerCheckbox.setChecked(true);
                        readerCheckbox.setEnabled(false); // Disable after validation
                    }
                    readerListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

}
