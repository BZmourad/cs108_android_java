package honeywell.hedc_demoapp2;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import honeywell.hedc.BuildConfig;
import honeywell.hedc.Constants;
import honeywell.hedc.EngineCommunication;
import honeywell.hedc.HsmCompatActivity;
import honeywell.hedc.Progress;
import timber.log.Timber;

public class MainActivity extends HsmCompatActivity
        implements AdapterView.OnItemSelectedListener,
        SettingsFragment.OnFragmentInteractionListener,
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TAG = "honeywell"; // Debugging purposes
    private static final String ACTION_USB_PERMISSION =
            "honeywell.hedc_demoapp2.USB_PERMISSION";
    final Context context = this;
    private static final int PERMISSION_REQUEST_CODE_EXTERNAL_STORAGE_READ = 1;
    protected boolean m_WaitingForImage = false;
    protected TextView m_Connect = null;
    protected ToggleButton m_ButtonConnect = null;
    protected Spinner m_Trigger = null;
    // protected Button   m_SnapShot                      = null;
    // protected Button   m_ExistingImage                 = null;
    //protected Button   m_ScanOn                        = null;
    // protected Button   m_ScanOff                       = null;
    protected Button m_BeepSuccess = null;
    protected Button m_BeepError = null;
    protected Button m_SendMenuCommand = null;
    protected TextView m_Cmd = null;
    private ProgressBar m_ProgressStatus = null;

    private SharedPreferences m_prefs = null;
    //private String m_trigger_mode[];
    private boolean m_UseHmodem = false;
    private int m_quality = 100;
    private boolean m_NonVolatile = false;
    private boolean m_AutoImage = false;
    private boolean m_WaitForTrigger = false;
    private boolean m_Illumination = true;
    private boolean m_Beep = false;
    private int m_Gain = 2;
    private int m_ImgFormat = Constants.HH_FORMAT_JPEG_GRAY;
    private int m_SubSample = 1;
    private boolean m_dualPane = false; // true does not work yet
    private UsbManager usbManager;

    // Fragments
    SettingsFragment m_Settings = null;
    TextFragment m_ScanWindow = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        m_Connect = findViewById(R.id.Connect);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        ///
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        ///
        //  m_SnapShot = findViewById(R.id.Snapshot);
        //   m_ExistingImage = findViewById(R.id.ExistingImage);
        // m_ScanOn= findViewById(R.id.ScanOn);
        // m_ScanOff= findViewById(R.id.ScanOff);
        //m_Trigger = findViewById(R.id.Triggermode);
        m_BeepSuccess = findViewById(R.id.BeepSuccess);
        m_BeepError = findViewById(R.id.BeepError);
        m_SendMenuCommand = findViewById(R.id.SendCmd);
        m_Cmd = findViewById(R.id.Cmd);
        m_ProgressStatus = findViewById(R.id.progressBar);
        DoneReceivingImage(Constants.ERROR_SUCCESS);

        m_prefs = PreferenceManager.getDefaultSharedPreferences(this);

        m_ButtonConnect = findViewById(R.id.ToggleConnect);
        m_ButtonConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //m_ButtonConnect.setEnabled(true);
                    m_engine.AllowConnect();
                } else {
                    //m_ButtonConnect.setEnabled(false);
                    m_engine.AllowDisconnect();
                }
            }
        });

        // m_trigger_mode = new String[3];
        // m_trigger_mode[0] = String.format(getString(R.string.manualtrigger));
        // m_trigger_mode[1] = String.format(getString(R.string.presentation));
        //m_trigger_mode[2] = String.format(getString(R.string.streamingpresentation));

        // ArrayAdapter<String> triggerAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_spinner_dropdown_item, m_trigger_mode);
        // m_Trigger.setAdapter(triggerAdapter);

        //m_Trigger.setOnItemSelectedListener(this);

        ShowScanWindow();
    }

    protected void EnableUI(boolean enable) {
        //m_SnapShot.setEnabled(enable);
        // m_ExistingImage.setEnabled(enable);
        // m_ScanOn.setEnabled(enable);
        //m_ScanOff.setEnabled(enable);
        //m_Trigger.setEnabled(enable);
        m_BeepSuccess.setEnabled(enable);
        m_BeepError.setEnabled(enable);
        m_SendMenuCommand.setEnabled(enable);
        m_Cmd.setEnabled(enable);
        m_ButtonConnect.setEnabled(enable);
        //if(enable)
        //  FixFocus();
    }

    /*protected void FixFocus() {
        m_ScanOff.requestFocus();        // avoid getting the online keyboard
    }*/

    @Override
    public void ReadIni() {
        m_NonVolatile = m_prefs.getBoolean("pref_UseNonVolatile", true);
        m_AutoImage = m_prefs.getBoolean("pref_AutoImage", false);
        m_WaitForTrigger = m_prefs.getBoolean("pref_WaitTrigger", false);
        m_Beep = m_prefs.getBoolean("pref_BeepSnap", false);
        m_Illumination = m_prefs.getBoolean("pref_UseIllum", false);

        m_quality = m_prefs.getInt("pref_jpgquality", 100);

        m_Gain = ParseInt(m_prefs, "pref_gain", 0);
        m_ImgFormat = ParseInt(m_prefs, "pref_imageformat", Constants.HH_FORMAT_JPEG_GRAY);
        m_SubSample = ParseInt(m_prefs, "pref_subsamples", 1);

        // not maintained by preference screen
        String cmd = m_prefs.getString("pref_cmd", "");
        m_Cmd.setText(cmd);
    }

    public void WriteIni() {
        // not maintained by preference screen
        SharedPreferences.Editor editor = m_prefs.edit();
        editor.putString("pref_cmd", m_Cmd.getText().toString());
        editor.commit();
    }

    protected int ParseInt(SharedPreferences prefs, String key, int defaultval) {
        String val = prefs.getString(key, Integer.toString(defaultval));
        return ParseInt(val, defaultval);
    }

    protected int ParseInt(String s, int defaultval) {
        int RetVal = defaultval;
        try {
            RetVal = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            RetVal = defaultval;
        }

        return RetVal;
    }

    @Override
    public void onResume() {
        super.onResume();
        ReadIni();
        //FixFocus();
    }

    @Override
    public void onPause() {
          Timber.tag(TAG).d( "MainActivity:onPause");
        WriteIni();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        int fragments = getSupportFragmentManager().getBackStackEntryCount();
        if (fragments == 1) {
            finish();
        } else if (fragments == 2) {
            EnableUI(true);
        }
        super.onBackPressed();
    }

    protected int TranslateTriggerModes(int mode) {
        int Pos = 0;
        switch (mode) {
            case Constants.TRGMOD_MANUAL:
                Pos = 0;
                break;
            case Constants.TRGMOD_PRESENT:
                Pos = 1;
                break;
            case Constants.TRGMOD_STREAM_PRESENT:
                Pos = 2;
                break;
        }
        return Pos;
    }

    public void addFragment(int id, Fragment fragment, boolean addToBackStack, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();


        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.replace(id, fragment, tag);
        ft.commit();
    }

    protected void ShowSettings() {
        EnableUI(false);
        // Instantiate the fragment.
        if (m_Settings == null) {
            m_Settings = SettingsFragment.newInstance();
        }

        // Add the fragment.
        if (m_dualPane) {
            addFragment(R.id.header_container, m_Settings, true, "Settings");
        } else {
            addFragment(R.id.fragment_container, m_Settings, true, "Settings");
        }
    }

    public void ShowScanWindow() {
        // Instantiate the fragment.
        if (m_ScanWindow == null) {
            m_ScanWindow = TextFragment.newInstance();
        }

        // Add the fragment.
        addFragment(R.id.fragment_container, m_ScanWindow, true, "Scan");
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, @NotNull Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        if (m_dualPane) {
            // Replace the existing Fragment with the new Fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_container, fragment)
                    .commit();
        } else {
            // Replace the existing Fragment with the new Fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
        return true;
    }

    public void ResetTriggerButton() {
        // noting to do here, can be used for toggle buttons.
    }

    // Called to update the generic UI
    @Override
    public void OnConnectionStateEvent(EngineCommunication.ConnectionState state) {
          Timber.tag(TAG).d( "OnConnectionStateEvent");
        String ConnectString = "";
        boolean connected = m_engine.isConnected();
        if (connected) {
            m_Connect.setTextColor(Color.BLUE);
            ConnectString = (m_engine.GetUsbPid() != 0) ? "Connected PID=%04X" : "Connected";
            DoneReceivingImage(Constants.ERROR_SUCCESS);
            setBarcodeText("");
            ShowDeviceInfo();
            int Pos = TranslateTriggerModes(m_engine.GetTriggerMode());
            // m_Trigger.setSelection(Pos);
            // Tell HEDC to redirect commands to non-volatile memory.
            if (m_NonVolatile) {
                final boolean bNonVolatile = true;
                m_engine.SetCmdsNonVolatile(bNonVolatile);
            }
        } else {
            ResetTriggerButton();
            m_Connect.setTextColor(Color.RED);
            ConnectString = (m_engine.GetUsbPid() != 0) ? "Disconnected PID=%04X" : "Disconnected";
            setBarcodeText("");
        }
        m_Connect.setText(String.format(ConnectString, m_engine.GetUsbPid()));
        if (m_ButtonConnect != null) {
            m_ButtonConnect.setChecked(connected);
        }
    }

    // Called to update the Barcode view
    @Override
    public void OnBarcodeData(String str) {
        appendBarcodeText(str);
        appendBarcodeText("\n");
        ResetTriggerButton();
        //if(m_AutoImage)
        //  GetExistingImage();
    }

    // Called to update the Barcode view
    @Override
    public void OnBarcodeDataRaw(final byte[] data) {
        if (!isWaitingForImage()) {
            appendBarcodeText(MakeHumanReadable(data));
            appendBarcodeText("\n");
        }
        ResetTriggerButton();
    }

    protected void WaitForImage() {
        m_WaitingForImage = true;
        m_ProgressStatus.setProgress(0);
        m_ProgressStatus.setVisibility(View.VISIBLE);
    }

    protected void DoneReceivingImage(int error) {
        m_WaitingForImage = false;
        m_ProgressStatus.setProgress(0);
        m_ProgressStatus.setVisibility(View.INVISIBLE);
        if (error != Constants.ERROR_SUCCESS) {
            appendBarcodeText("No Image\n");
        }
    }

    protected boolean isWaitingForImage() {
        return m_WaitingForImage;
    }

    // Called to update the picture view (if any)
    @Override
    public void OnImageData(final Bitmap bitmap) {
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float imagesize = m_engine.getLastImageSize();
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle(String.format("Image details %d x %d (transfer size: %.1f KB)", width, height, imagesize / 1024));
            alertDialog.getWindow().setLayout(width, height);
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Close", (DialogInterface.OnClickListener) null);
            ImageView view = new ImageView(context);
            view.setImageBitmap(bitmap);
            alertDialog.setView(view);
            alertDialog.show();
        }
    }

    public void onClickExit(View view) {
          Timber.tag(TAG).d( "onClickExit");
        m_engine.Disconnect();
        finish();
    }

    /*public void onClickScanOn(View view) {
        Log. d(TAG, "onClickScanOn");
        m_engine.StartReadingSession();
    }

    public void onClickScanOff(View view) {
        Log. d(TAG, "onClickScanOff");
        m_engine.StopReadingSession();
    }
*/
    protected String CreateImageSnapCmd() {
        String sCommand = String.format("IMGSNP%dB%dT%dL%dG", m_Beep ? 1 : 0,
                m_WaitForTrigger ? 1 : 0, m_Illumination ? 1 : 0, m_Gain);
        return sCommand;
    }

    protected String CreateImageShipCmd() {
        int protocol = 2;
        if (m_UseHmodem) {
            protocol = 4;
        }
        return String.format("IMGSHP%dP%dS%dF%dJ", protocol, m_SubSample, m_ImgFormat, m_quality);
    }

   /* protected void GetExistingImage() {
        WaitForImage();
        String sCommand = CreateImageShipCmd() + "!";
        appendBarcodeText(sCommand); // show it also on screen
        appendBarcodeText("\n");
        m_engine.SendMenuCommand(sCommand);
    } */

   /* public void onClickExistingImage(View view) {
          Timber.tag(TAG).d( "onClickExistingImage");
        GetExistingImage();
    }

    public void onClickGetNewImage(View view) {
        Log. d(TAG, "onClickGetNewImage");
        WaitForImage();
        String sCommand = CreateImageSnapCmd() + ";" + CreateImageShipCmd() + "!";
        appendBarcodeText(sCommand); // show it also on screen
        appendBarcodeText("\n");
        m_engine.SendMenuCommand(sCommand);
    } */


    public void onClickBeepSuccess(View view) {
          Timber.tag(TAG).d( "onClickBeepSuccess");
        m_engine.BeepSuccess();
    }

    public void onClickBeepError(View view) {
          Timber.tag(TAG).d( "onClickBeepError");
        m_engine.BeepError();
    }

    public void onClickSend(View view) {
          Timber.tag(TAG).d( "onClickSend");
        String Cmd = m_Cmd.getText().toString();
        ExecuteMenuCommandAndShowResult(Cmd);
    }

    public void onLongClickSend(View view) {
          Timber.tag(TAG).d( "onLongClickSend");
        String Cmd = m_Cmd.getText().toString();
        ExecuteMenuCommandAndShowResult(Cmd);
    }

    public void ExecuteMenuCommandAndShowResult(String Cmd) {
        if (Cmd == null || Cmd.length() < 1) {
            m_ScanWindow.append("Not valid command\n");
            return;
        }

        // Now execute command
        boolean success = m_engine.ExecuteMenuCommand(Cmd);
        String Result;
        if (success) {
            Result = "Success: ";
        } else {
            Result = "Failed: ";
        }
        m_ScanWindow.append(Result);
        m_ScanWindow.append(m_engine.GetRawCommandResponse());
        m_ScanWindow.append("\n");
    }

    protected void ShowDeviceInfo() {
        appendBarcodeText("Product:  " + m_engine.GetInterfaceName() + m_engine.GetModelName() + "\n");
        appendBarcodeText("S/N:  " + m_engine.GetSerialNumber() + "\n");
        appendBarcodeText("Firmware:  " + m_engine.GetFirmwareVersion() + "\n");
        appendBarcodeText("Revision:  " + m_engine.GetFirmwareRevision() + "\n");
        appendBarcodeText("\n");
    }

    protected void appendBarcodeText(String str) {
        if (m_ScanWindow != null) {
            m_ScanWindow.append(str);
        }
    }

    protected void setBarcodeText(final String str) {
        if (m_ScanWindow != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_ScanWindow.setText(str);
                }
            });
        }
    }

    protected void setBarcodeTextEnabled(boolean enable) {
        if (m_ScanWindow != null) {
            m_ScanWindow.setEnabled(enable);
        }
    }

    public void onClickClearScreen(View view) {
          Timber.tag(TAG).d( "onClickClearScreen");
        setBarcodeText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    protected boolean HandleToggleButton(MenuItem item) {
        boolean check = !item.isChecked();
        item.setChecked(check);
        return check;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.about:
                String Versions = String.format("AppVersion: %s%nHedcVersion: %s",
                        getString(R.string.svn_rev), m_engine.getVersionString());
                m_engine.ShowAlert("Versions", Versions);
                return true;
            case R.id.clearscreen:
                setBarcodeText("");
                return true;
            case R.id.preferences:
                ShowSettings();
                return true;
            case R.id.action_USBDevices:
                DisplayUsbDevicesList();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void DisplayUsbDevicesList() {
        String s = "";
        s = m_engine.listUsbDevices(false);
        if (s == "")
            s = getString(R.string.NoDevices);
        androidx.appcompat.app.AlertDialog.Builder AlertDlg = new androidx.appcompat.app.AlertDialog.Builder(context);
        AlertDlg.setPositiveButton(getString(R.string.ok), null);
        AlertDlg.setTitle(getString(R.string.HonDevices));
        AlertDlg.setMessage(s);
        AlertDlg.show();
    }

    @Override
    public void OnProgress(int wparam, int lparam) {
        if (isWaitingForImage()) {
            switch (wparam) {
                case Progress.PR_Percent:
                    m_ProgressStatus.setProgress(lparam);
                    break;
                case Progress.PR_ImageDone:
                    DoneReceivingImage(lparam);
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }

    final int TRG = 0x1000;

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {

        Message Msg = new Message();
        Msg.what = TRG;
        switch (position) {
            default:
            case 0:
                Msg.arg1 = Constants.TRGMOD_MANUAL;
                break;
            case 1:
                Msg.arg1 = Constants.TRGMOD_PRESENT;
                break;
            case 2:
                Msg.arg1 = Constants.TRGMOD_STREAM_PRESENT;
                break;
        }
        handler.sendMessage(Msg);
    }

    private void requestPermissionUsb(UsbDevice usbDevice) {
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(usbDevice, permissionIntent);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //   Obtain permission to communicate with a device
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                        }
                    } else {
                          Timber.tag(TAG).d( "permission denied for device " + device);
                    }
                }
            }
            //  Terminating communication with a device

        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TRG) {
                m_engine.SetTriggerMode(msg.arg1);
            }
        }
    };

}
