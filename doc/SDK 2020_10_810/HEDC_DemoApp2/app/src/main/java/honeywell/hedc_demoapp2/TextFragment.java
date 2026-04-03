package honeywell.hedc_demoapp2;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import honeywell.hedc.BuildConfig;
import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TextFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TextFragment extends Fragment {
    private static final String TAG = "honeywell"; // Debugging purposes

    protected TextView m_TextWindow = null;
    protected CharSequence m_Save;

    public TextFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TextFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TextFragment newInstance() {
        TextFragment fragment = new TextFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text, container, false);

        m_TextWindow = view.findViewById(R.id.textBarcodeContents);
        m_TextWindow.setMovementMethod(new ScrollingMovementMethod());

        return view;
    }

    @Override
    public void onResume() {
          Timber.tag(TAG).d( "TextFragment.onResume");
        super.onResume();
        m_TextWindow.setText(m_Save);
    }

    @Override
    public void onPause() {
          Timber.tag(TAG).d( "TextFragment.onPause");
        m_Save = m_TextWindow.getText();
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setText(String s) {
        m_TextWindow.setText(s);
    }

    public void append(String s) {
        m_TextWindow.append(s);
    }

    public void setEnabled(boolean enable) {
        m_TextWindow.setEnabled(enable);
    }

}
