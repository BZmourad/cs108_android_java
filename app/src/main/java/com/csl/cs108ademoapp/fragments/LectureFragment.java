package com.csl.cs108ademoapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.apps.SharedPrefManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class LectureFragment extends CommonFragment {
    boolean is_online = false;
    public Context ctx;

    private TextView textViewCurrentForm, textViewTotalForms;
    private TextInputEditText editTextBarcode, editTextRfid, editTextSerial;
    private AutoCompleteTextView autoCompleteEtat, autoCompleteEmployee;
    private AppCompatImageButton buttonPrev, buttonNext;
    private Button buttonFinish;

    private int currentForm = 1;
    private int totalForms = 5; // Changé de final à variable
    private List<FormData> formDataList;
    private List<Boolean> formValideList; // Liste pour suivre les formulaires validés

    private String codeInv;
    private String codeMission;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        View view = inflater.inflate(R.layout.fragment_lecture, container, false);

        ctx = getActivity();
        // Récupérer les paramètres
        Bundle args = getArguments();
        if (args != null) {
            codeInv = args.getString("codeInv", "");
            codeMission = args.getString("codeMission", "");
        }
        initializeViews(view);
        setupFormData();
        setupDropdowns();
        updateFormDisplay();

        return view;
    }

    private void initializeViews(View view) {
        textViewCurrentForm = view.findViewById(R.id.textViewCurrentForm);
        textViewTotalForms = view.findViewById(R.id.textViewTotalForms);

        editTextBarcode = view.findViewById(R.id.editTextBarcode);
        editTextRfid = view.findViewById(R.id.editTextRfid);
        editTextSerial = view.findViewById(R.id.editTextSerial);
        autoCompleteEtat = view.findViewById(R.id.autoCompleteEtat);
        autoCompleteEmployee = view.findViewById(R.id.autoCompleteEmployee);

        buttonPrev = view.findViewById(R.id.buttonPrev);
        buttonNext = view.findViewById(R.id.buttonNext);
        buttonFinish = view.findViewById(R.id.buttonFinish);

        // Afficher le nombre total de formulaires initial
        textViewTotalForms.setText(String.valueOf(totalForms));

        // Setup listeners
        buttonPrev.setOnClickListener(v -> navigateToPreviousForm());
        buttonNext.setOnClickListener(v -> navigateToNextForm());
        buttonFinish.setOnClickListener(v -> validateCurrentForm());
    }

    private void setupFormData() {
        formDataList = new ArrayList<>();
        formValideList = new ArrayList<>();

        for (int i = 0; i < totalForms; i++) {
            formDataList.add(new FormData());
            formValideList.add(false); // Tous les formulaires sont non validés au début
        }
    }

    private void setupDropdowns() {
        // Données d'exemple pour les dropdowns
        String[] etatOptions = {"Bon état", "Usé", "Défectueux", "En réparation", "Hors service"};
        String[] employeeOptions = {"Jean Dupont", "Marie Martin", "Pierre Durand", "Sophie Lambert", "Michel Bernard"};

        ArrayAdapter<String> etatAdapter = new ArrayAdapter<>(ctx,
                android.R.layout.simple_dropdown_item_1line, etatOptions);
        ArrayAdapter<String> employeeAdapter = new ArrayAdapter<>(ctx,
                android.R.layout.simple_dropdown_item_1line, employeeOptions);

        autoCompleteEtat.setAdapter(etatAdapter);
        autoCompleteEmployee.setAdapter(employeeAdapter);
    }

    private void updateFormDisplay() {
        // Mettre à jour le numéro du formulaire courant
        textViewCurrentForm.setText(String.valueOf(currentForm));
        textViewTotalForms.setText(String.valueOf(totalForms));

        // Charger les données du formulaire courant seulement s'il n'est pas encore validé
        if (!formValideList.get(currentForm - 1)) {
            FormData currentData = formDataList.get(currentForm - 1);
            editTextBarcode.setText(currentData.barcode);
            editTextRfid.setText(currentData.rfid);
            editTextSerial.setText(currentData.serial);
            autoCompleteEtat.setText(currentData.etat, false);
            autoCompleteEmployee.setText(currentData.employee, false);

            // Activer les champs si le formulaire n'est pas validé
            setFormEnabled(true);
        } else {
            // Désactiver les champs si le formulaire est déjà validé
            setFormEnabled(false);

            // Afficher les données en lecture seule
            FormData currentData = formDataList.get(currentForm - 1);
            editTextBarcode.setText(currentData.barcode);
            editTextRfid.setText(currentData.rfid);
            editTextSerial.setText(currentData.serial);
            autoCompleteEtat.setText(currentData.etat, false);
            autoCompleteEmployee.setText(currentData.employee, false);
        }

        // Navigation circulaire - toujours activer les deux boutons
        buttonPrev.setEnabled(true);
        buttonNext.setEnabled(true);

        // Masquer le bouton Valider si le formulaire est déjà validé
        buttonFinish.setVisibility(formValideList.get(currentForm - 1) ? View.GONE : View.VISIBLE);
    }

    private void setFormEnabled(boolean enabled) {
        editTextBarcode.setEnabled(enabled);
        editTextRfid.setEnabled(enabled);
        editTextSerial.setEnabled(enabled);
        autoCompleteEtat.setEnabled(enabled);
        autoCompleteEmployee.setEnabled(enabled);
    }

    private void saveCurrentFormData() {
        // Sauvegarder seulement si le formulaire n'est pas encore validé
        if (!formValideList.get(currentForm - 1)) {
            FormData currentData = formDataList.get(currentForm - 1);
            currentData.barcode = editTextBarcode.getText().toString().trim();
            currentData.rfid = editTextRfid.getText().toString().trim();
            currentData.serial = editTextSerial.getText().toString().trim();
            currentData.etat = autoCompleteEtat.getText().toString().trim();
            currentData.employee = autoCompleteEmployee.getText().toString().trim();
        }
    }

    private void navigateToPreviousForm() {
        saveCurrentFormData();

        // Trouver le formulaire précédent non validé
        int previousForm = currentForm;
        do {
            if (previousForm == 1) {
                previousForm = totalForms;
            } else {
                previousForm--;
            }

            // Si on revient au formulaire de départ, sortir de la boucle
            if (previousForm == currentForm) {
                break;
            }
        } while (formValideList.get(previousForm - 1));

        currentForm = previousForm;
        updateFormDisplay();
    }

    private void navigateToNextForm() {
        saveCurrentFormData();

        // Trouver le formulaire suivant non validé
        int nextForm = currentForm;
        do {
            if (nextForm == totalForms) {
                nextForm = 1;
            } else {
                nextForm++;
            }

            // Si on revient au formulaire de départ, sortir de la boucle
            if (nextForm == currentForm) {
                break;
            }
        } while (formValideList.get(nextForm - 1));

        currentForm = nextForm;
        updateFormDisplay();
    }

    private boolean validateFormData() {
        String barcode = editTextBarcode.getText().toString().trim();
        String rfid = editTextRfid.getText().toString().trim();
        String serial = editTextSerial.getText().toString().trim();
        String etat = autoCompleteEtat.getText().toString().trim();
        String employee = autoCompleteEmployee.getText().toString().trim();

        if (barcode.isEmpty() && rfid.isEmpty() && serial.isEmpty()) {
            Toast.makeText(ctx, "Veuillez remplir au moins un champ (Code à barre, RFID ou Série)",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etat.isEmpty()) {
            Toast.makeText(ctx, "Veuillez sélectionner un état", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (employee.isEmpty()) {
            Toast.makeText(ctx, "Veuillez sélectionner un employé", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void validateCurrentForm() {
        if (validateFormData()) {
            saveCurrentFormData();

            // Marquer le formulaire comme validé
            formValideList.set(currentForm - 1, true);

            // Désactiver les champs
            setFormEnabled(false);

            // Traiter les données du formulaire
            FormData data = formDataList.get(currentForm - 1);
            if (is_online) {
                sendDataToAPI(data, currentForm);
            } else {
                saveDataToDatabase(data, currentForm);
            }

            // Réduire le nombre total de formulaires
            totalForms--;

            Toast.makeText(ctx, "Formulaire " + currentForm + " validé! Formulaires restants: " + totalForms,
                    Toast.LENGTH_SHORT).show();

            // Si tous les formulaires sont validés
            if (totalForms == 0) {
                finishAllForms();
                return;
            }

            // Naviguer vers le formulaire suivant non validé
            navigateToNextNonValidatedForm();
        }
    }

    private void navigateToNextNonValidatedForm() {
        // Trouver le prochain formulaire non validé
        int nextForm = currentForm;
        boolean found = false;

        for (int i = 1; i <= formValideList.size(); i++) {
            int testForm = (currentForm + i - 1) % formValideList.size() + 1;
            if (!formValideList.get(testForm - 1)) {
                nextForm = testForm;
                found = true;
                break;
            }
        }

        if (found) {
            currentForm = nextForm;
            updateFormDisplay();
        }
    }

    private void finishAllForms() {
        Toast.makeText(ctx, "Tous les formulaires ont été validés avec succès!",
                Toast.LENGTH_SHORT).show();

        // Retourner au fragment précédent ou effectuer une autre action
        getActivity().onBackPressed();
    }

    private void sendDataToAPI(FormData data, int formNumber) {
        // Implémenter l'envoi des données à l'API
        Toast.makeText(ctx, "Envoi formulaire " + formNumber + " à l'API - Inventaire: " + codeInv,
                Toast.LENGTH_SHORT).show();
    }

    private void saveDataToDatabase(FormData data, int formNumber) {
        // Implémenter la sauvegarde en base de données locale
        Toast.makeText(ctx, "Sauvegarde formulaire " + formNumber + " en base locale - Mission: " + codeMission,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(codeInv+" - "+codeMission);

        if (SharedPrefManager.getInstance(ctx).isOnlineMode()) {
            is_online = true;
        } else {
            is_online = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public LectureFragment() {
        super("LectureFragment");
    }

    // Classe pour stocker les données d'un formulaire
    private static class FormData {
        String barcode = "";
        String rfid = "";
        String serial = "";
        String etat = "";
        String employee = "";

        void reset() {
            barcode = "";
            rfid = "";
            serial = "";
            etat = "";
            employee = "";
        }
    }
}