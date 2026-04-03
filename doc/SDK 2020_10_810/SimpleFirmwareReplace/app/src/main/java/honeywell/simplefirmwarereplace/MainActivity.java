package honeywell.simplefirmwarereplace;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.provider.OpenableColumns;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.Manifest;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;

import honeywell.hedc.Constants;
import honeywell.hedc.Progress;
import honeywell.hedc.EngineCommunication;
import honeywell.hedc.HsmCompatActivity;

public class MainActivity extends HsmCompatActivity {
    private static final String TAG = "honeywell"; // Debugging purposes
    final Context context = this;
    private static final int PERMISSION_REQUEST_CODE_EXTERNAL_STORAGE_READ = 1;
    private SharedPreferences m_prefs= null;

    protected boolean m_LastConnectionStatus = false;
    protected TextView m_LogWindow = null;
    private TextView m_FileDisplayName = null;
    private Uri m_uri = null;
    private ProgressBar m_ProgressStatus = null;
    private boolean m_DownloadIsActive = false;
    public Button m_BrowseButton = null;
    public Button m_StartFlashButton = null;
    protected long m_startTime=0;

    enum HandlerCommands {
        UNKNOWN,
        SET_URI,
    }

    enum State {
        UNKNOWN,
        Downloading,
        Stopping,
        Error,
        Stopped,
    }

    private State m_DownloadState = State.Stopped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        m_FileDisplayName = findViewById(R.id.filePath);
        m_ProgressStatus = findViewById(R.id.progressBar);
        m_BrowseButton = findViewById(R.id.browseButton);
        m_StartFlashButton = findViewById(R.id.updateButton);

        m_LogWindow = findViewById(R.id.textBarcodeContents);
        m_LogWindow.setMovementMethod(new ScrollingMovementMethod());
	    m_prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onStart ()
    {
        super.onStart ();

        final android.content.Intent intent = getIntent ();
        if (intent != null)
        {
            final Uri uri = intent.getData();
            if (uri != null)
            {
                StoreUri(m_uri);
            }
        }
        m_engine.setFirmwareReplace(true);  // this ensures we keep the engine in simple mode
    }

    protected void FixFocus() {
        m_StartFlashButton.requestFocus();        // avoid getting the online keyboard
    }

    protected void EnableUI(boolean enable) {
        FreezeOrientation(!enable);    // rotation would disconnect
        m_StartFlashButton.setEnabled(enable);
        m_BrowseButton.setEnabled(enable);
        if(enable) {
            m_ProgressStatus.setVisibility(View.INVISIBLE);
        } else {
            m_ProgressStatus.setVisibility(View.VISIBLE);
        }
    }

    public void onClickBrowse(View view) {
        RequestExternalStoragePermission();
        ShowFileSelector();
        // Will callback to setFilePath
    }

    public void onClickUpdateFirmware(View view) {
        RequestExternalStoragePermission();
        if(m_engine.isConnected())
            SetFlashInProgress(true);
        else
            append(getString(R.string.not_connected));
    }

    public void onMenuAbort() {
        if (m_DownloadState != State.Stopped) {
            // user pressed stop, better ask to be sure
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle(getString(R.string.AreYouSure));
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), (DialogInterface.OnClickListener) null);
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "Aborted by User");
                    m_DownloadState = State.Stopping;
                    m_engine.AbortFirmwareReplace();
                }
            });
            alertDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.about:
                String Versions = String.format(getString(R.string.appversion),
                        getString(R.string.svn_rev), m_engine.getVersionString());
                m_engine.ShowAlert(getString(R.string.versions), Versions);
                return true;
            case R.id.clearscreen:
                ClearScreen();
                return true;
            case R.id.abort:
                onMenuAbort();
                return true;
            case R.id.action_USBDevices:
                DisplayUsbDevicesList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Called to update the generic UI
    @Override
    public void OnConnectionStateEvent(EngineCommunication.ConnectionState state) {
        Log.d(TAG, "OnConnectionStateEvent");
        String ConnectString="";
        boolean connected = m_engine.isConnected();
        if(connected) {
            //ConnectString = (m_engine.GetUsbPid()!=0) ? "Connected PID=%04X\n" : "Connected\n";
            //append(String.format(ConnectString, m_engine.GetUsbPid()));
            //ShowDeviceInfo();
            if (!m_LastConnectionStatus) { // only show first time after connection
                InitImager();
            }
        } else {
            //ConnectString = (m_engine.GetUsbPid()!=0) ? "Disconnected PID=%04X\n" : "Disconnected\n";
            //append(String.format(ConnectString, m_engine.GetUsbPid()));
        }
        m_LastConnectionStatus = connected;
    }

    protected void InitImager() {
        // Here you can add special commands if you need such.
    }


    public void ReadIni() {
        String sName = m_prefs.getString("pref_FirmwareFile", "/storage/emulated/0/Pictures/BI000927AAC.moc");
        m_FileDisplayName.setText(sName);
        String sUri = m_prefs.getString("pref_FirmwareUri", "");
        m_uri = Uri.parse(sUri);
    }

    public void WriteIni() {
        // not maintained by preference screen
        SharedPreferences.Editor editor = m_prefs.edit();
        editor.putString("pref_FirmwareFile", m_FileDisplayName.getText().toString());
        editor.putString("pref_FirmwareUri", m_uri.toString());
        editor.commit();
    }

    // A request code's purpose is to match the result of a "startActivityForResult" with
    // the type of the original request.  Choose any value.
    private static final int READ_REQUEST_CODE = 2549;

    /**
     * Fires an intent to spin up the "file picker" UI and select a file.
     */
    protected void ShowFileSelector() {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);  //ACTION_OPEN_DOCUMENT and ACTION_OPEN_DOCUMENT_TREE

        // Filter to only show results that can be "opened", such as a file (as opposed to a list
        // of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers, it would be
        // "*/*".
        intent.setType("*/*");  // TODO: limit to *.moc and *.bin

        try {
            this.startActivityForResult(
                    Intent.createChooser(intent, getString(honeywell.hedc.R.string.SelectFW)), READ_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, getString(honeywell.hedc.R.string.NoFileManger), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.i(TAG, "Received an \"Activity Result\"");
        super.onActivityResult(requestCode, resultCode, resultData);
        // BEGIN_INCLUDE (parse_open_document_response)
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                if (uri != null) {
                    setUri(uri);
                }
            }
            // END_INCLUDE (parse_open_document_response)
        }
        ClearScreen();
    }

    // Called from another thread, so we need to run in a handler
    protected void setUri(Uri uri) {
        Message msg = Message.obtain(handler, HandlerCommands.SET_URI.ordinal(), uri);
        handler.sendMessage(msg);
    }

    // https://developer.android.com/training/secure-file-sharing/retrieve-info#java
    protected String GetFileName(Uri uri) {
        Cursor returnCursor =
                getContentResolver().query(uri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         * move to the first row in the Cursor, get the data,
         * and display it.
         */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        return returnCursor.getString(nameIndex);
    }

    protected void StoreUri(Uri uri) {
        try {
            if (m_engine.IsValidFirmwareFile(context, uri)) {
                m_uri = uri;
                m_FileDisplayName.setText(GetFileName(uri));
            } else {
                m_uri = null;
                m_FileDisplayName.setText("");
                append(getString(R.string.ERROR_NO_FW_FILE) + "\n");
                Toast.makeText(this, getString(honeywell.hedc.R.string.NotValidFW), Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(honeywell.hedc.R.string.ERROR_READ_FAULT), Toast.LENGTH_LONG).show();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(honeywell.hedc.R.string.ERROR_INVALID_PARAMETER), Toast.LENGTH_LONG).show();
        }
    }

    protected String getExt(String filePath){
        int strLength = filePath.lastIndexOf(".");
        if(strLength > 0)
            return filePath.substring(strLength + 1).toLowerCase();
        return "";
    }

    public Handler handler = new Handler() {
        public void handleMessage (Message msg){
            //Log.d(TAG, "handler");
            HandlerCommands cmd = HandlerCommands.values()[msg.what];
            switch (cmd) {
                case SET_URI:
                    StoreUri((Uri)msg.obj);
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        ReadIni();
        FixFocus();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        WriteIni();
        super.onPause();
    }

    protected void append(String str) {
        if(m_LogWindow!=null) {
            m_LogWindow.append(str);
        }
    }

    protected void setText(String str) {
        if(m_LogWindow!=null) {
            m_LogWindow.setText(str);
        }
    }

    protected void ClearScreen() {
        setText("");
    }

    protected void SetFlashInProgress(boolean running)
    {
        m_ProgressStatus.setProgress(0);
        m_ProgressStatus.setSecondaryProgress(0);
        if (m_DownloadIsActive != running) {
            m_DownloadIsActive = running;
            if (running) {
                Log.d(TAG, "Start FW replace");
                EnableUI(false);
                append(getString(R.string.CurrentFW));
                append(m_engine.GetFirmwareVersion() + "\n");
                m_startTime = System.nanoTime();

                m_DownloadState = State.Downloading;
                try {
                    m_engine.ReplaceFirmwareUri(context, m_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                    append(e.toString() + "\n");
                    m_DownloadState = State.Error;
                    SetFlashInProgress(false);  // call ourselfs to cleanup
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    append(getString(R.string.ERROR_INVALID_PARAMETER) + "\n");
                    m_DownloadState = State.Error;
                    SetFlashInProgress(false);  // call ourselfs to cleanup
                }
            } else {
                float difference = (System.nanoTime() - m_startTime)/(1000*1000*1000);
                String format = getString(R.string.done);
                if(m_DownloadState == State.Error)
                    format = getString(R.string.aborted);
                String time = String.format(format, difference);
                append(time);
                m_DownloadState = State.Stopped;
                EnableUI(true);
                Log.d(TAG, "End FW replace");
            }
        }
    }

    @Override
    public void OnProgress(int wparam, int lparam) {
        boolean Running=true;
        String string ="";
        int percent = lparam;

        switch (wparam) {
            case Progress.PR_Downloading:
            case Progress.PR_Flashing:
                // This is sent while download and flash
                m_ProgressStatus.setProgress(percent);
                break;
            case Progress.PR_Percent:
                // This is sent after an aborted download
                m_ProgressStatus.setSecondaryProgress(percent);
                break;

            case Progress.PR_DownloadStarted:
                string = getString(R.string.PR_DownloadStarted);
                break;
            case Progress.PR_FlashFinished:
                if (lparam != Constants.ERROR_SUCCESS) {
                    string = GetErrorText(lparam) +"\n";
                } else {
                    string = getString(R.string.PR_FlashFinishedSuccess);
                    string += m_engine.GetFirmwareVersion() +"\n";
                }
                Running=false;
                break;
            case Progress.PR_NothingToDo:
                string = getString(R.string.PR_NothingToDo);
                break;
            case Progress.PR_DownloadFinished:
                if (lparam != Constants.ERROR_SUCCESS) {
                    string = GetErrorText(lparam) +"\n";
                    Running=false;
                } else {
                    string = getString(R.string.PR_DownloadFinishedSuccess);
                }
                break;
            case Progress.PR_FlashStillRunning:
                string = getString(R.string.PR_FlashStillRunning);
                break;
            case Progress.PR_Aborted:
                // called right after the thread sees the abort.
                // You could implement some immeditate reaction here
                string = getString(R.string.PR_Aborted);
                break;
            case Progress.PR_ConnectionStatus:
                if (lparam==0) {
                    string = getString(R.string.PR_ConnectionStatusBad);
                    Running=false;
                } else {
                    string = getString(R.string.PR_ConnectionStatus);
                }
                break;
            case Progress.PR_Validating:
                string = getString(R.string.PR_Validating);
                break;
        }
        append(string);
        if(!Running) {
            SetFlashInProgress(Running);    // must be last, so texts are in correct order
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

    // Called to update the Barcode view
    @Override
    public void OnBarcodeData(String str) {
        append(str);
        append("\n");
    }

    // Called to update the Barcode view
    @Override
    public void OnBarcodeDataRaw(final byte[] data) {
        append(MakeHumanReadable(data));
        append("\n");
    }

    public void onClickExit(View view) {
        Log. d(TAG, "onClickExit");
        m_engine.Disconnect();
        finish();
    }

    protected void ShowDeviceInfo() {
        append("Product:  " + m_engine.GetInterfaceName() + m_engine.GetModelName() + "\n");
        append("S/N:  " + m_engine.GetSerialNumber() + "\n");
        append("Firmware:  " + m_engine.GetFirmwareVersion() + "\n");
        append("Revision:  " + m_engine.GetFirmwareRevision() + "\n");
        append("\n");
    }

    private void RequestExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE_EXTERNAL_STORAGE_READ);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_EXTERNAL_STORAGE_READ:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(TAG, "EXTERNAL_STORAGE_READ permission denied");
                }
                break;
        }
    }

    public void FreezeOrientation(boolean status) {
        if (status) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }
    }
}
