package com.csl.cs108immo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "cs108_database.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table names
    public static final String TABLE_ARBRE_EMP = "ArbreEmp";
    public static final String TABLE_ARBRE_FAMILE = "ArbreFamile";
    public static final String TABLE_ATTRIBUTS = "Attributs";
    public static final String TABLE_ATTRIBUTS_ATTBSET = "Attributs_attbset";
    public static final String TABLE_CONGF_SYSTEME = "congfSysteme";
    public static final String TABLE_DET_INV_IMMO = "Det_inv_immo";
    public static final String TABLE_EMPLOYEE = "Employee";
    public static final String TABLE_FOURNISSEUR = "FOURNISSEUR";
    public static final String TABLE_HISTORIQUE_ECAR_INV = "historique_ecar_inv";
    public static final String TABLE_IMMO = "Immo";
    public static final String TABLE_IMMO_INV_IMAGE = "immo_inv_image";
    public static final String TABLE_INV_ATTRIBUT_INV_IMMO = "Inv_attributinvImmo";
    public static final String TABLE_INV_EQUIPE = "Inv_equipe";
    public static final String TABLE_INV_EQUIPE_INVENTAIRE = "Inv_EquipeInventaire";
    public static final String TABLE_INV_IMMO = "inv_immo";
    public static final String TABLE_INV_MISSION = "inv_mission";
    public static final String TABLE_INV_MOMBRE_EQUIPE = "Inv_eMombreequipe";
    public static final String TABLE_INV_IMMO_ATTRIBUT_VALR = "InvImmoAttributValr";
    public static final String TABLE_PARAM = "param";
    public static final String TABLE_PROPRIETAIRE = "Proprietaire";
    public static final String TABLE_SOUS_ETAT = "SouSEtat";
    public static final String TABLE_UTILISATEUR = "Utilisateur";
    public static final String TABLE_VALEURS_ATTRIBUT = "valeursAttribut";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Création de toutes les tables
        createTableArbreEmp(db);
        createTableArbreFamile(db);
        createTableAttributs(db);
        createTableAttributsAttbset(db);
        createTableCongfSysteme(db);
        createTableDetInvImmo(db);
        createTableEmployee(db);
        createTableFournisseur(db);
        createTableHistoriqueEcarInv(db);
        createTableImmo(db);
        createTableImmoInvImage(db);
        createTableInvAttributInvImmo(db);
        createTableInvEquipe(db);
        createTableInvEquipeInventaire(db);
        createTableInvImmo(db);
        createTableInvMission(db);
        createTableInvMombreequipe(db);
        createTableInvImmoAttributValr(db);
        createTableParam(db);
        createTableProprietaire(db);
        createTableSousEtat(db);
        createTableUtilisateur(db);
        createTableValeursAttribut(db);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Supprimer toutes les tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARBRE_EMP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARBRE_FAMILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTRIBUTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTRIBUTS_ATTBSET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONGF_SYSTEME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DET_INV_IMMO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOURNISSEUR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORIQUE_ECAR_INV);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMMO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMMO_INV_IMAGE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV_ATTRIBUT_INV_IMMO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV_EQUIPE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV_EQUIPE_INVENTAIRE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV_IMMO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV_MISSION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV_MOMBRE_EQUIPE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV_IMMO_ATTRIBUT_VALR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARAM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROPRIETAIRE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SOUS_ETAT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UTILISATEUR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VALEURS_ATTRIBUT);
        
        // Recréer les tables
        onCreate(db);
    }
    
    // Méthodes de création des tables
    private void createTableArbreEmp(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ARBRE_EMP + "(" +
                "code NVARCHAR(50) NOT NULL UNIQUE," +
                "Libelle NVARCHAR(50)," +
                "Idparent NVARCHAR(20)," +
                "path NVARCHAR(200)," +
                "n1 VARCHAR(50)," +
                "n2 VARCHAR(50)," +
                "n3 VARCHAR(50)," +
                "n4 VARCHAR(50)," +
                "n5 VARCHAR(50)," +
                "n6 VARCHAR(50)," +
                "n7 VARCHAR(50)," +
                "n8 VARCHAR(50)," +
                "n9 VARCHAR(50)," +
                "n10 VARCHAR(50)" +
                ")";
        db.execSQL(createTable);
        
        // Création des index
        db.execSQL("CREATE INDEX WDIDX_ArbreEmp_path ON " + TABLE_ARBRE_EMP + " (path)");
    }
    
    private void createTableArbreFamile(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ARBRE_FAMILE + "(" +
                "code VARCHAR(50) NOT NULL," +
                "Libelle VARCHAR(50)," +
                "Idparent VARCHAR(20)" +
                ")";
        db.execSQL(createTable);
        
        db.execSQL("CREATE INDEX WDIDX_ArbreFamile_code ON " + TABLE_ARBRE_FAMILE + " (code)");
    }
    
    private void createTableAttributs(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ATTRIBUTS + "(" +
                "Description VARCHAR(50)," +
                "obligatoire BIT," +
                "type VARCHAR(50)," +
                "valeurDefaut VARCHAR(50)," +
                "libCode VARCHAR(50)," +
                "isAffiche BIT," +
                "isInconnu BIT" +
                ")";
        db.execSQL(createTable);
        
        db.execSQL("CREATE INDEX WDIDX_Attributs_libCode ON " + TABLE_ATTRIBUTS + " (libCode)");
    }
    
    private void createTableAttributsAttbset(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ATTRIBUTS_ATTBSET + "(" +
                "codeAttribus VARCHAR(50)," +
                "codeAttribusSet VARCHAR(50)" +
                ")";
        db.execSQL(createTable);
        
        db.execSQL("CREATE INDEX WDIDX_Attributs_attbset_codeAttribus ON " + TABLE_ATTRIBUTS_ATTBSET + " (codeAttribus)");
        db.execSQL("CREATE INDEX WDIDX_Attributs_attbset_codeAttribusSet ON " + TABLE_ATTRIBUTS_ATTBSET + " (codeAttribusSet)");
        db.execSQL("CREATE INDEX WDIDX_Attributs_attbset_codeAttribuscodeAttribusSet ON " + TABLE_ATTRIBUTS_ATTBSET + " (codeAttribus, codeAttribusSet)");
    }
    
    private void createTableCongfSysteme(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_CONGF_SYSTEME + "(" +
                "isAttset BIT," +
                "isCreImmo BIT," +
                "isInvPartl BIT," +
                "empldefaut NVARCHAR(50)," +
                "isUpdateValide BIT," +
                "isVerfEtat BIT," +
                "isVerfFmlle BIT," +
                "isVerfFrnsr BIT," +
                "isVerfEmplye BIT," +
                "isVrfEmp BIT," +
                "GenrJurdque BIT," +
                "GenrUtlstr BIT," +
                "GenrEmply BIT," +
                "n10 VARCHAR(50)," +
                "GenrFornsr BIT," +
                "nbr_Niveau_empl VARCHAR(4)," +
                "affiche_Niveau BIT," +
                "n1 VARCHAR(50)," +
                "n2 VARCHAR(50)," +
                "n3 VARCHAR(50)," +
                "n4 VARCHAR(50)," +
                "n5 VARCHAR(50)," +
                "n6 VARCHAR(50)," +
                "n7 VARCHAR(50)," +
                "n8 VARCHAR(50)," +
                "n9 VARCHAR(50)" +
                ")";
        db.execSQL(createTable);
    }
    
    private void createTableDetInvImmo(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_DET_INV_IMMO + "(" +
                "CodeInventaire NVARCHAR(50)," +
                "CodeImmo NVARCHAR(50)," +
                "CodeEmpTh NVARCHAR(50)," +
                "DateLecture TIMESTAMP," +
                "UserLecture NVARCHAR(20)," +
                "Observation NVARCHAR(120)," +
                "DateValidation TIMESTAMP," +
                "Uservalidation NVARCHAR(20)," +
                "EtatCorrection SMALLINT," +
                "codeEmpInv NVARCHAR(50)," +
                "codeEmpCor NVARCHAR(50)," +
                "codemission NVARCHAR(50)," +
                "codeEquipe NVARCHAR(50)," +
                "codeArticle NVARCHAR(50)," +
                "codeEtat_Corr NVARCHAR(50)," +
                "empl_Corr NVARCHAR(50)," +
                "codeBarre_Corr NVARCHAR(50)," +
                "NumSerie_Corr NVARCHAR(50)," +
                "nbr_Valid INTEGER," +
                "nbr_Crt INTEGER," +
                "nbr_Crt_valid INTEGER," +
                "Descp_immo NVARCHAR(120)," +
                "issync INTEGER," +
                "RFIDCode_Corr VARCHAR(50)" +
                ")";
        db.execSQL(createTable);
        
        // Création des index
        db.execSQL("CREATE INDEX WDIDX_Det_inv_immo_CodeInventaire ON " + TABLE_DET_INV_IMMO + " (CodeInventaire)");
        db.execSQL("CREATE INDEX WDIDX_Det_inv_immo_CodeImmo ON " + TABLE_DET_INV_IMMO + " (CodeImmo)");
        db.execSQL("CREATE INDEX WDIDX_Det_inv_immo_CodeEmpTh ON " + TABLE_DET_INV_IMMO + " (CodeEmpTh)");
        db.execSQL("CREATE INDEX WDIDX_Det_inv_immo_codemission ON " + TABLE_DET_INV_IMMO + " (codemission)");
        db.execSQL("CREATE INDEX WDIDX_Det_inv_immo_codeEquipe ON " + TABLE_DET_INV_IMMO + " (codeEquipe)");
        db.execSQL("CREATE INDEX WDIDX_Det_inv_immo_codeArticle ON " + TABLE_DET_INV_IMMO + " (codeArticle)");
        db.execSQL("CREATE INDEX WDIDX_Det_inv_immo_RFIDCode_Corr ON " + TABLE_DET_INV_IMMO + " (RFIDCode_Corr)");
        db.execSQL("CREATE INDEX WDIDX_Det_inv_immo_CodeInventaireCodeImmo ON " + TABLE_DET_INV_IMMO + " (CodeInventaire, CodeImmo)");
    }
    
    private void createTableEmployee(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_EMPLOYEE + "(" +
                "CodeFunction VARCHAR(50)," +
                "CodeGroup VARCHAR(50)," +
                "CodeEmployee VARCHAR(50) UNIQUE," +
                "DateCre TIMESTAMP," +
                "NomPrenom VARCHAR(50)," +
                "EmailWork VARCHAR(50)," +
                "NumPerso NUMERIC(20,0)," +
                "NumTravail NUMERIC(20,0)," +
                "EmailPerso VARCHAR(50)," +
                "Addresse VARCHAR(150)," +
                "Notes LONGVARCHAR," +
                "ETAT SMALLINT," +
                "UserCreation VARCHAR(20)," +
                "UseMaj VARCHAR(20)," +
                "DateMaj TIMESTAMP" +
                ")";
        db.execSQL(createTable);
    }
    
    private void createTableFournisseur(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_FOURNISSEUR + "(" +
                "IDFournisseur VARCHAR(50) UNIQUE," +
                "CompteCollectif VARCHAR(15)," +
                "CompteCharge VARCHAR(15)," +
                "NumTVA VARCHAR(50)," +
                "Reference VARCHAR(50)," +
                "Notes LONGVARCHAR," +
                "Etat SMALLINT," +
                "DateHeureCreation DATE," +
                "AuteurCreation VARCHAR(50)," +
                "DateHeureModification DATE," +
                "AuteurModification VARCHAR(50)," +
                "Vide LONGVARCHAR," +
                "Lettrage VARCHAR(10)," +
                "Code VARCHAR(50)," +
                "Adresse_email VARCHAR(260)," +
                "Code_Postal VARCHAR(5)," +
                "Fax VARCHAR(13)," +
                "Numero_Fournisseur NUMERIC(10,0)," +
                "Pays VARCHAR(64)," +
                "ADR1 VARCHAR(20)," +
                "ADR2 VARCHAR(20)," +
                "CP VARCHAR(5)," +
                "VILLE VARCHAR(40)," +
                "Conttact VARCHAR(120)" +
                ")";
        db.execSQL(createTable);
    }
    
    private void createTableHistoriqueEcarInv(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_HISTORIQUE_ECAR_INV + "(" +
                "Code_immo NVARCHAR(50)," +
                "code_Inv NVARCHAR(50)," +
                "DateValidation TIMESTAMP," +
                "Uservalidation NVARCHAR(20)," +
                "code_att_inv NVARCHAR(50)," +
                "code_att_th NVARCHAR(50)," +
                "typeVal NVARCHAR(50)," +
                "code_att NVARCHAR(50)" +
                ")";
        db.execSQL(createTable);
        
        // Création des index
        db.execSQL("CREATE INDEX WDIDX_historique_ecar_inv_Code_immo ON " + TABLE_HISTORIQUE_ECAR_INV + " (Code_immo)");
        db.execSQL("CREATE INDEX WDIDX_historique_ecar_inv_code_Inv ON " + TABLE_HISTORIQUE_ECAR_INV + " (code_Inv)");
        db.execSQL("CREATE INDEX WDIDX_historique_ecar_inv_code_att ON " + TABLE_HISTORIQUE_ECAR_INV + " (code_att)");
        db.execSQL("CREATE INDEX WDIDX_historique_ecar_inv_code_InvCode_immoCode_att ON " + TABLE_HISTORIQUE_ECAR_INV + " (code_Inv, Code_immo, code_att)");
    }
    
    private void createTableImmo(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_IMMO + "(" +
                "Descp_immo NVARCHAR(120)," +
                "code_etat NVARCHAR(50)," +
                "fournsr NVARCHAR(40)," +
                "Code_immo NVARCHAR(50) NOT NULL," +
                "Employee NVARCHAR(50)," +
                "type_etat NVARCHAR(50)," +
                "Num_serie NVARCHAR(50)," +
                "entite_juridique NVARCHAR(50)," +
                "prix_acquis INTEGER," +
                "emplacement NVARCHAR(50)," +
                "date_desactivation TIMESTAMP," +
                "date_aquisition TIMESTAMP," +
                "prix_cession INTEGER," +
                "val_cession INTEGER," +
                "dat_service TIMESTAMP," +
                "Chek INTEGER," +
                "code_barre NVARCHAR(50)," +
                "code_famille NVARCHAR(50)," +
                "attribut_set NVARCHAR(50)," +
                "code_article NVARCHAR(50)," +
                "motif NVARCHAR(50)," +
                "user NVARCHAR(50)," +
                "date_cre TIMESTAMP," +
                "id_immo NUMERIC(19,0) PRIMARY KEY," +
                "attValr VARCHAR(1)," +
                "issync INTEGER," +
                "Code_RFID VARCHAR(50)" +
                ")";
        db.execSQL(createTable);
        
        // Création des index
        db.execSQL("CREATE INDEX WDIDX_Immo_Descp_immo ON " + TABLE_IMMO + " (Descp_immo)");
        db.execSQL("CREATE INDEX WDIDX_Immo_code_etat ON " + TABLE_IMMO + " (code_etat)");
        db.execSQL("CREATE INDEX WDIDX_Immo_fournsr ON " + TABLE_IMMO + " (fournsr)");
        db.execSQL("CREATE INDEX WDIDX_Immo_Code_immo ON " + TABLE_IMMO + " (Code_immo)");
        db.execSQL("CREATE INDEX WDIDX_Immo_Employee ON " + TABLE_IMMO + " (Employee)");
        db.execSQL("CREATE INDEX WDIDX_Immo_Num_serie ON " + TABLE_IMMO + " (Num_serie)");
        db.execSQL("CREATE INDEX WDIDX_Immo_emplacement ON " + TABLE_IMMO + " (emplacement)");
        db.execSQL("CREATE INDEX WDIDX_Immo_code_barre ON " + TABLE_IMMO + " (code_barre)");
        db.execSQL("CREATE INDEX WDIDX_Immo_code_famille ON " + TABLE_IMMO + " (code_famille)");
        db.execSQL("CREATE INDEX WDIDX_Immo_attribut_set ON " + TABLE_IMMO + " (attribut_set)");
        db.execSQL("CREATE INDEX WDIDX_Immo_Code_RFID ON " + TABLE_IMMO + " (Code_RFID)");
    }
    
    private void createTableImmoInvImage(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_IMMO_INV_IMAGE + "(" +
                "CodeImmo VARCHAR(50)," +
                "CodeDoc VARCHAR(50)," +
                "url VARCHAR(200)," +
                "img LONGVARBINARY," +
                "parDefault BIT," +
                "codeInv VARCHAR(50)," +
                "isSny INTEGER" +
                ")";
        db.execSQL(createTable);
        
        // Création des index
        db.execSQL("CREATE INDEX WDIDX_immo_inv_image_CodeImmo ON " + TABLE_IMMO_INV_IMAGE + " (CodeImmo)");
        db.execSQL("CREATE INDEX WDIDX_immo_inv_image_CodeDoc ON " + TABLE_IMMO_INV_IMAGE + " (CodeDoc)");
        db.execSQL("CREATE INDEX WDIDX_immo_inv_image_codeInv ON " + TABLE_IMMO_INV_IMAGE + " (codeInv)");
        db.execSQL("CREATE INDEX WDIDX_immo_inv_image_CodeImmoCodeDoc ON " + TABLE_IMMO_INV_IMAGE + " (CodeImmo, CodeDoc)");
        db.execSQL("CREATE INDEX WDIDX_immo_inv_image_codeInvCodeImmoCodeDoc ON " + TABLE_IMMO_INV_IMAGE + " (codeInv, CodeImmo, CodeDoc)");
        db.execSQL("CREATE INDEX WDIDX_immo_inv_image_codeInvCodeImmo ON " + TABLE_IMMO_INV_IMAGE + " (codeInv, CodeImmo)");
    }
    
    private void createTableInvAttributInvImmo(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_INV_ATTRIBUT_INV_IMMO + "(" +
                "name NVARCHAR(128)," +
                "codeatribut VARCHAR(50)," +
                "saisie BIT," +
                "Codeinv VARCHAR(50)," +
                "afficher BIT," +
                "validation BIT" +
                ")";
        db.execSQL(createTable);
        
        // Création des index
        db.execSQL("CREATE INDEX WDIDX_Inv_attributinvImmo_codeatribut ON " + TABLE_INV_ATTRIBUT_INV_IMMO + " (codeatribut)");
        db.execSQL("CREATE INDEX WDIDX_Inv_attributinvImmo_Codeinv ON " + TABLE_INV_ATTRIBUT_INV_IMMO + " (Codeinv)");
        db.execSQL("CREATE INDEX WDIDX_Inv_attributinvImmo_Codeinvname ON " + TABLE_INV_ATTRIBUT_INV_IMMO + " (Codeinv, name)");
        db.execSQL("CREATE INDEX WDIDX_Inv_attributinvImmo_Codeinvcodeatribut ON " + TABLE_INV_ATTRIBUT_INV_IMMO + " (Codeinv, codeatribut)");
    }
    
    private void createTableInvEquipe(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_INV_EQUIPE + "(" +
                "codeEquipe VARCHAR(50) UNIQUE," +
                "NoEquipe VARCHAR(50)," +
                "chefEquipe VARCHAR(50)," +
                "Codeinv VARCHAR(50)," +
                "UserCreation VARCHAR(20)," +
                "DateCreation TIMESTAMP" +
                ")";
        db.execSQL(createTable);
        
        // Création des index
        db.execSQL("CREATE INDEX WDIDX_Inv_equipe_NoEquipe ON " + TABLE_INV_EQUIPE + " (NoEquipe)");
        db.execSQL("CREATE INDEX WDIDX_Inv_equipe_chefEquipe ON " + TABLE_INV_EQUIPE + " (chefEquipe)");
        db.execSQL("CREATE INDEX WDIDX_Inv_equipe_Codeinv ON " + TABLE_INV_EQUIPE + " (Codeinv)");
        db.execSQL("CREATE INDEX WDIDX_Inv_equipe_CodeinvcodeEquipe ON " + TABLE_INV_EQUIPE + " (Codeinv, codeEquipe)");
    }
    
    private void createTableInvEquipeInventaire(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_INV_EQUIPE_INVENTAIRE + "(" +
                "CodeEmplacement VARCHAR(50)," +
                "codeEquipe VARCHAR(50)," +
                "codemission VARCHAR(50)," +
                "Codeinv VARCHAR(50)" +
                ")";
        db.execSQL(createTable);
        
        // Création des index
        db.execSQL("CREATE INDEX WDIDX_Inv_EquipeEmpl_CodeEmplacement ON " + TABLE_INV_EQUIPE_INVENTAIRE + " (CodeEmplacement)");
        db.execSQL("CREATE INDEX WDIDX_Inv_EquipeEmpl_codeEquipe ON " + TABLE_INV_EQUIPE_INVENTAIRE + " (codeEquipe)");
        db.execSQL("CREATE INDEX WDIDX_Inv_EquipeEmpl_codemission ON " + TABLE_INV_EQUIPE_INVENTAIRE + " (codemission)");
        db.execSQL("CREATE INDEX WDIDX_Inv_EquipeEmpl_Codeinv ON " + TABLE_INV_EQUIPE_INVENTAIRE + " (Codeinv)");
        db.execSQL("CREATE INDEX WDIDX_Inv_EquipeEmpl_CodeinvcodemissioncodeEquipeCodeEmplacement ON " + TABLE_INV_EQUIPE_INVENTAIRE + " (Codeinv, codemission, codeEquipe, CodeEmplacement)");
        db.execSQL("CREATE INDEX WDIDX_Inv_EquipeEmpl_CodeinvcodeEquipecodemission ON " + TABLE_INV_EQUIPE_INVENTAIRE + " (Codeinv, codeEquipe, codemission)");
        db.execSQL("CREATE INDEX WDIDX_Inv_EquipeEmpl_CodeinvCodeEmplacement ON " + TABLE_INV_EQUIPE_INVENTAIRE + " (Codeinv, CodeEmplacement)");
    }
    
    private void createTableInvImmo(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_INV_IMMO + "(" +
                "CodeInventaire VARCHAR(50) NOT NULL UNIQUE," +
                "DateCreation TIMESTAMP," +
                "EtatInventaire INTEGER," +
                "DateCloture TIMESTAMP," +
                "UserCloture VARCHAR(20)," +
                "UserCreation VARCHAR(20)," +
                "Description VARCHAR(60)," +
                "verif_art_inv BIT," +
                "affich_art_inv BIT," +
                "nbrAtt INTEGER," +
                "multi_eq_mis BIT" +
                ")";
        db.execSQL(createTable);
        
        // Création des index
        db.execSQL("CREATE INDEX WDIDX_inv_immo_DateCreation ON " + TABLE_INV_IMMO + " (DateCreation)");
        db.execSQL("CREATE INDEX WDIDX_inv_immo_EtatInventaire ON " + TABLE_INV_IMMO + " (EtatInventaire)");
        db.execSQL("CREATE INDEX WDIDX_inv_immo_DateCloture ON " + TABLE_INV_IMMO + " (DateCloture)");
    }
    
    private void createTableInvMission(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_INV_MISSION + "(" +
                "codemission VARCHAR(50)," +
                "etatmission VARCHAR(50)," +
                "DateCree TIMESTAMP," +
                "UserCreation VARCHAR(20)," +
                "DateMaj TIMESTAMP," +
                "usermaj VARCHAR(20)," +
                "Description VARCHAR(60)," +
                "Codeinv VARCHAR(50)" +
                ")";
        db.execSQL(createTable);
        
        // Création des index
        db.execSQL("CREATE INDEX WDIDX_inv_mission_codemission ON " + TABLE_INV_MISSION + " (codemission)");
        db.execSQL("CREATE INDEX WDIDX_inv_mission_Codeinv ON " + TABLE_INV_MISSION + " (Codeinv)");
        db.execSQL("CREATE INDEX WDIDX_inv_mission_Codeinvcodemission ON " + TABLE_INV_MISSION + " (Codeinv, codemission)");
    }
    
    private void createTableInvMombreequipe(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_INV_MOMBRE_EQUIPE + "(" +
                "CodeUtil VARCHAR(20)," +
                "codeEquipe VARCHAR(50)," +
                "Codeinv VARCHAR(50)," +
                "NOM VARCHAR(40)" +
                ")";
        db.execSQL(createTable);
        
        // Création des index
        db.execSQL("CREATE INDEX WDIDX_Inv_Mombreequipe_CodeUtil ON " + TABLE_INV_MOMBRE_EQUIPE + " (CodeUtil)");
        db.execSQL("CREATE INDEX WDIDX_Inv_Mombreequipe_codeEquipe ON " + TABLE_INV_MOMBRE_EQUIPE + " (codeEquipe)");
        db.execSQL("CREATE INDEX WDIDX_Inv_Mombreequipe_Codeinv ON " + TABLE_INV_MOMBRE_EQUIPE + " (Codeinv)");
        db.execSQL("CREATE INDEX WDIDX_Inv_Mombreequipe_CodeinvcodeEquipeCodeUtil ON " + TABLE_INV_MOMBRE_EQUIPE + " (Codeinv, codeEquipe, CodeUtil)");
    }
    
    private void createTableInvImmoAttributValr(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_INV_IMMO_ATTRIBUT_VALR + "(" +
                "codeInv VARCHAR(50)," +
                "codeImmo VARCHAR(50)," +
                "codeAttrib VARCHAR(50)," +
                "valr VARCHAR(50)," +
                "issync SMALLINT" +
                ")";
        db.execSQL(createTable);
        
        // Création des index
        db.execSQL("CREATE INDEX WDIDX_InvImmoAttributValr_codeInv ON " + TABLE_INV_IMMO_ATTRIBUT_VALR + " (codeInv)");
        db.execSQL("CREATE INDEX WDIDX_InvImmoAttributValr_codeImmo ON " + TABLE_INV_IMMO_ATTRIBUT_VALR + " (codeImmo)");
        db.execSQL("CREATE INDEX WDIDX_InvImmoAttributValr_codeAttrib ON " + TABLE_INV_IMMO_ATTRIBUT_VALR + " (codeAttrib)");
        db.execSQL("CREATE INDEX WDIDX_InvImmoAttributValr_codeInvcodeImmo ON " + TABLE_INV_IMMO_ATTRIBUT_VALR + " (codeInv, codeImmo)");
        db.execSQL("CREATE INDEX WDIDX_InvImmoAttributValr_codeInvcodeImmocodeAttrib ON " + TABLE_INV_IMMO_ATTRIBUT_VALR + " (codeInv, codeImmo, codeAttrib)");
    }
    
    private void createTableParam(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_PARAM + "(" +
                "adress VARCHAR(50) UNIQUE," +
                "mode VARCHAR(10)," +
                "projet VARCHAR(50)" +
                ")";
        db.execSQL(createTable);
    }
    
    private void createTableProprietaire(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_PROPRIETAIRE + "(" +
                "CodeProprietaire VARCHAR(50) UNIQUE," +
                "Libelle VARCHAR(50)," +
                "Addresse VARCHAR(150)" +
                ")";
        db.execSQL(createTable);
    }
    
    private void createTableSousEtat(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_SOUS_ETAT + "(" +
                "sousEtat SMALLINT NOT NULL," +
                "LibelleEtat VARCHAR(50)," +
                "TypeEtat VARCHAR(50)," +
                "DescriptionEtat VARCHAR(200)," +
                "coler VARCHAR(50)" +
                ")";
        db.execSQL(createTable);
        
        // Création des index
        db.execSQL("CREATE INDEX WDIDX_SouSEtat_sousEtat ON " + TABLE_SOUS_ETAT + " (sousEtat)");
        db.execSQL("CREATE INDEX WDIDX_SouSEtat_LibelleEtat ON " + TABLE_SOUS_ETAT + " (LibelleEtat)");
    }
    
    private void createTableUtilisateur(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_UTILISATEUR + "(" +
                "CodeUtil VARCHAR(20) UNIQUE," +
                "NomPrenomUtil VARCHAR(50)," +
                "MotPasse VARCHAR(50)," +
                "login VARCHAR(50)" +
                ")";
        db.execSQL(createTable);
        
        db.execSQL("CREATE INDEX WDIDX_Utilisateur_login ON " + TABLE_UTILISATEUR + " (login)");
    }
    
    private void createTableValeursAttribut(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_VALEURS_ATTRIBUT + "(" +
                "codeAttribut VARCHAR(50)," +
                "Valeur VARCHAR(50)," +
                "isdefault BIT" +
                ")";
        db.execSQL(createTable);
        
        // Création des index
        db.execSQL("CREATE INDEX WDIDX_valeursAttribut_codeAttribut ON " + TABLE_VALEURS_ATTRIBUT + " (codeAttribut)");
        db.execSQL("CREATE INDEX WDIDX_valeursAttribut_Valeur ON " + TABLE_VALEURS_ATTRIBUT + " (Valeur)");
        db.execSQL("CREATE INDEX WDIDX_valeursAttribut_codeAttributValeur ON " + TABLE_VALEURS_ATTRIBUT + " (codeAttribut, Valeur)");
    }
    
    // Méthodes utilitaires pour les opérations CRUD basiques
    public long insert(String tableName, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(tableName, null, values);
    }
    
    public Cursor query(String tableName, String[] columns, String selection, 
                       String[] selectionArgs, String orderBy) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(tableName, columns, selection, selectionArgs, null, null, orderBy);
    }
    
    public int update(String tableName, ContentValues values, String selection, 
                     String[] selectionArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(tableName, values, selection, selectionArgs);
    }
    
    public int delete(String tableName, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(tableName, selection, selectionArgs);
    }
    
    // Méthode pour exécuter des requêtes SQL personnalisées
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(sql, selectionArgs);
    }
    
    // Méthode pour exécuter des commandes SQL (CREATE, DROP, etc.)
    public void execSQL(String sql) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(sql);
    }
}