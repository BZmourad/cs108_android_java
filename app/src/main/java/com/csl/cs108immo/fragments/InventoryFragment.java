package com.csl.cs108immo.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.csl.cs108immo.MainActivity;
import com.csl.cs108immo.R;
import com.csl.cslibrary4a.AdapterTab;
import com.google.android.material.tabs.TabLayout;
import com.csl.cs108immo.apps.SharedPrefManager;
import android.content.Context;

public class InventoryFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager viewPager;
    AdapterTab adapter;
    private Button verifyRfidButton;

    private String[] tabs = {"RFID", "Barcode"};
    private String codeInv;
    private String codeMission;
    private String equipe;
    private String token;
    public Context ctx;
    private String codeEmplacement;
    private TextView textViewEmplacement;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        savedInstanceState = null;
        super.onCreateView(inflater, container, savedInstanceState, true);
        ctx = getActivity();
        token = SharedPrefManager.getInstance(ctx).getToken();
        if (getArguments() != null) {
            codeInv = getArguments().getString("codeInv");
            codeMission = getArguments().getString("codeMission");
            equipe = getArguments().getString("equipe");
            codeEmplacement = getArguments().getString("codeEmplacement");
        }
        return inflater.inflate(R.layout.custom_tabbed_layout, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MainActivity.csLibrary4A.appendToLog("InventoryFragment: onOptionsItemSelected with viewPager as " + viewPager.getCurrentItem());
        LectureFragment fragment0 = (LectureFragment) adapter.getItem(0);
        InventoryBarcodeFragment fragment1 = (InventoryBarcodeFragment) adapter.getItem(1);
        switch (viewPager.getCurrentItem()) {
            case 0:
                return fragment0.onOptionsItemSelected(item);
            case 1:
                return fragment1.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(R.string.title_activity_inventory);

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.OperationsTabLayout);

        adapter = new AdapterTab(getActivity().getSupportFragmentManager(), tabs.length);
        
        // Pass the parameters to both fragments
        adapter.setFragment(0, LectureFragment.newInstance(false, null, null, codeInv, codeMission, equipe, codeEmplacement));
        adapter.setFragment(1, InventoryBarcodeFragment.newInstance(codeInv, codeMission, equipe, codeEmplacement));

        viewPager = (ViewPager) getActivity().findViewById(R.id.OperationsPager);
        Log.i("Hello", "InventoryFragment.onActivity");
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        for (String tab_name : tabs) {
            tabLayout.addTab(tabLayout.newTab().setText(tab_name));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.headerLayout).setVisibility(View.VISIBLE);

        // Initialiser et mettre à jour l'affichage de l'emplacement
        textViewEmplacement = view.findViewById(R.id.textViewEmplacement);
        if (textViewEmplacement != null) {
            String emplacementText = "Emplacement: ";
            if (codeEmplacement != null && !codeEmplacement.isEmpty()) {
                emplacementText += codeEmplacement;
            } else {
                emplacementText += "Non spécifié";
            }
            textViewEmplacement.setText(emplacementText);

            // Optionnel: Changer la couleur selon online/offline
            if (SharedPrefManager.getInstance(ctx).isOnlineMode()) {
                textViewEmplacement.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
            } else {
                textViewEmplacement.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
            }
        }
    }

    @Override
    public void onPause() {
        adapter.fragment0.onPause();
        adapter.fragment1.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        adapter.fragment0.onStop();
        adapter.fragment1.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        adapter.fragment0.onDestroyView();
        adapter.fragment1.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        adapter.fragment0.onDestroy();
        adapter.fragment1.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        adapter.fragment0.onDetach();
        adapter.fragment1.onDetach();
        super.onDetach();
    }

    public InventoryFragment() {
        super("InventoryFragment");
    }
}
