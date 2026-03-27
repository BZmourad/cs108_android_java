package com.csl.cs108ademoapp.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import android.content.ContentValues;
import android.database.Cursor;
import com.csl.cs108ademoapp.app.db.models.Utilisateur;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "invImmoDb.db";
    private static final int DATABASE_VERSION = 2;

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
    public static final String TABLE_INV_ATTRIBUTINV_IMMO = "Inv_attributinvImmo";
    public static final String TABLE_INV_EQUIPE = "Inv_equipe";
    public static final String TABLE_INV_EQUIPE_INVENTAIRE = "Inv_EquipeInventaire";
    public static final String TABLE_INV_IMMO = "inv_immo";
    public static final String TABLE_INV_MISSION = "inv_mission";
    public static final String TABLE_INV_EMOMBREEQUIPE = "Inv_eMombreequipe";
    public static final String TABLE_INV_IMMO_ATTRIBUT_VALR = "InvImmoAttributValr";
    public static final String TABLE_PARAM = "param";
    public static final String TABLE_PROPRIETAIRE = "Proprietaire";
    public static final String TABLE_SOUS_ETAT = "SouSEtat";
    public static final String TABLE_UTILISATEUR = "Utilisateur";
    public static final String TABLE_VALEURS_ATTRIBUT = "valeursAttribut";

    // Create table statements
    private static final String CREATE_TABLE_ARBRE_EMP =
            "CREATE TABLE " + TABLE_ARBRE_EMP + " (" +
                    "code NVARCHAR(50) NOT NULL UNIQUE, " +
                    "Libelle NVARCHAR(50), " +
                    "Idparent NVARCHAR(20), " +
                    "path NVARCHAR(200), " +
                    "n1 VARCHAR(50), " +
                    "n2 VARCHAR(50), " +
                    "n3 VARCHAR(50), " +
                    "n4 VARCHAR(50), " +
                    "n5 VARCHAR(50), " +
                    "n6 VARCHAR(50), " +
                    "n7 VARCHAR(50), " +
                    "n8 VARCHAR(50), " +
                    "n9 VARCHAR(50), " +
                    "n10 VARCHAR(50))";

    private static final String CREATE_TABLE_ARBRE_FAMILE =
            "CREATE TABLE " + TABLE_ARBRE_FAMILE + " (" +
                    "code VARCHAR(50) NOT NULL, " +
                    "Libelle VARCHAR(50), " +
                    "Idparent VARCHAR(20))";

    private static final String CREATE_TABLE_ATTRIBUTS =
            "CREATE TABLE " + TABLE_ATTRIBUTS + " (" +
                    "Description VARCHAR(50), " +
                    "obligatoire TINYINT DEFAULT 0, " +
                    "type VARCHAR(50), " +
                    "valeurDéfaut VARCHAR(50), " +
                    "libCode VARCHAR(50), " +
                    "isAffiche TINYINT DEFAULT 0, " +
                    "isInconnu TINYINT DEFAULT 0)";

    private static final String CREATE_TABLE_ATTRIBUTS_ATTBSET =
            "CREATE TABLE " + TABLE_ATTRIBUTS_ATTBSET + " (" +
                    "codeAttribus VARCHAR(50), " +
                    "codeAttribusSet VARCHAR(50))";

    private static final String CREATE_TABLE_CONGF_SYSTEME =
            "CREATE TABLE " + TABLE_CONGF_SYSTEME + " (" +
                    "isAttset TINYINT DEFAULT 0, " +
                    "isCreImmo TINYINT DEFAULT 0, " +
                    "isInvPartl TINYINT DEFAULT 0, " +
                    "empldefaut NVARCHAR(50), " +
                    "isUpdateValide TINYINT DEFAULT 0, " +
                    "isVerfEtat TINYINT DEFAULT 0, " +
                    "isVerfFmlle TINYINT DEFAULT 0, " +
                    "isVerfFrnsr TINYINT DEFAULT 0, " +
                    "isVerfEmplye TINYINT DEFAULT 0, " +
                    "isVrfEmp TINYINT DEFAULT 0, " +
                    "GenrJurdque TINYINT DEFAULT 0, " +
                    "GenrUtlstr TINYINT DEFAULT 0, " +
                    "GenrEmply TINYINT DEFAULT 0, " +
                    "n10 VARCHAR(50), " +
                    "GenrFornsr TINYINT DEFAULT 0, " +
                    "nbr_Niveau_empl VARCHAR(4), " +
                    "affiche_Niveau TINYINT DEFAULT 0, " +
                    "n1 VARCHAR(50), " +
                    "n2 VARCHAR(50), " +
                    "n3 VARCHAR(50), " +
                    "n4 VARCHAR(50), " +
                    "n5 VARCHAR(50), " +
                    "n6 VARCHAR(50), " +
                    "n7 VARCHAR(50), " +
                    "n8 VARCHAR(50), " +
                    "n9 VARCHAR(50))";

    private static final String CREATE_TABLE_DET_INV_IMMO =
            "CREATE TABLE " + TABLE_DET_INV_IMMO + " (" +
                    "CodeInventaire NVARCHAR(50), " +
                    "CodeImmo NVARCHAR(50), " +
                    "CodeEmpTh NVARCHAR(50), " +
                    "DateLecture TIMESTAMP, " +
                    "UserLecture NVARCHAR(20), " +
                    "Observation NVARCHAR(120), " +
                    "DateValidation TIMESTAMP, " +
                    "Uservalidation NVARCHAR(20), " +
                    "EtatCorrection TINYINT DEFAULT 0, " +
                    "codeEmpInv NVARCHAR(50), " +
                    "codeEmpCor NVARCHAR(50), " +
                    "codemission NVARCHAR(50), " +
                    "codeEquipe NVARCHAR(50), " +
                    "codeArticle NVARCHAR(50), " +
                    "codeEtat_Corr NVARCHAR(50), " +
                    "empl_Corr NVARCHAR(50), " +
                    "codeBarre_Corr NVARCHAR(50), " +
                    "NumSerie_Corr NVARCHAR(50), " +
                    "nbr_Valid INTEGER DEFAULT 0, " +
                    "nbr_Crt INTEGER DEFAULT 0, " +
                    "nbr_Crt_valid INTEGER DEFAULT 0, " +
                    "Descp_immo NVARCHAR(120), " +
                    "issync INTEGER DEFAULT 0, " +
                    "RFIDCode_Corr VARCHAR(50))";

    private static final String CREATE_TABLE_EMPLOYEE =
            "CREATE TABLE " + TABLE_EMPLOYEE + " (" +
                    "CodeFunction VARCHAR(50), " +
                    "CodeGroup VARCHAR(50), " +
                    "CodeEmployee VARCHAR(50) UNIQUE, " +
                    "DateCre TIMESTAMP, " +
                    "NomPrenom VARCHAR(50), " +
                    "EmailWork VARCHAR(50), " +
                    "NumPerso NUMERIC(20,0), " +
                    "NumTravail NUMERIC(20,0), " +
                    "EmailPerso VARCHAR(50), " +
                    "Addresse VARCHAR(150), " +
                    "Notes LONGTEXT, " +
                    "ETAT TINYINT DEFAULT 1, " +
                    "UserCreation VARCHAR(20), " +
                    "UseMaj VARCHAR(20), " +
                    "DateMaj TIMESTAMP)";

    private static final String CREATE_TABLE_FOURNISSEUR =
            "CREATE TABLE " + TABLE_FOURNISSEUR + " (" +
                    "IDFournisseur VARCHAR(50) UNIQUE DEFAULT '0', " +
                    "CompteCollectif VARCHAR(15), " +
                    "CompteCharge VARCHAR(15), " +
                    "NumTVA VARCHAR(50), " +
                    "Reference VARCHAR(50), " +
                    "Notes LONGTEXT, " +
                    "Etat SMALLINT DEFAULT 0, " +
                    "DateHeureCreation DATE, " +
                    "AuteurCreation VARCHAR(50), " +
                    "DateHeureModification DATE, " +
                    "AuteurModification VARCHAR(50), " +
                    "Vide LONGTEXT, " +
                    "Lettrage VARCHAR(10), " +
                    "Code VARCHAR(50), " +
                    "Adresse_email VARCHAR(260), " +
                    "Code_Postal VARCHAR(5), " +
                    "Fax VARCHAR(13) DEFAULT '0', " +
                    "Numero_Fournisseur NUMERIC(10,0) DEFAULT 0, " +
                    "Pays VARCHAR(64), " +
                    "ADR1 VARCHAR(20), " +
                    "ADR2 VARCHAR(20), " +
                    "CP VARCHAR(5), " +
                    "VILLE VARCHAR(40), " +
                    "Conttact VARCHAR(120))";

    private static final String CREATE_TABLE_HISTORIQUE_ECAR_INV =
            "CREATE TABLE " + TABLE_HISTORIQUE_ECAR_INV + " (" +
                    "Code_immo NVARCHAR(50) DEFAULT '0', " +
                    "code_Inv NVARCHAR(50), " +
                    "DateValidation TIMESTAMP DEFAULT '0000-01-01 00:00:00.000', " +
                    "Uservalidation NVARCHAR(20), " +
                    "code_att_inv NVARCHAR(50), " +
                    "code_att_th NVARCHAR(50), " +
                    "typeVal NVARCHAR(50), " +
                    "code_att NVARCHAR(50))";

    private static final String CREATE_TABLE_IMMO =
            "CREATE TABLE " + TABLE_IMMO + " (" +
                    "Descp_immo NVARCHAR(120), " +
                    "code_etat NVARCHAR(50) DEFAULT '1', " +
                    "fournsr NVARCHAR(40), " +
                    "Code_immo NVARCHAR(50) NOT NULL, " +
                    "Employee NVARCHAR(50), " +
                    "type_etat NVARCHAR(50), " +
                    "Num_serie NVARCHAR(50), " +
                    "entite_juridique NVARCHAR(50), " +
                    "prix_acquis INTEGER DEFAULT 0, " +
                    "emplacement NVARCHAR(50), " +
                    "date_desactivation TIMESTAMP DEFAULT '0000-01-01 00:00:00.000', " +
                    "date_aquisition TIMESTAMP DEFAULT '0000-01-01 00:00:00.000', " +
                    "prix_cession INTEGER DEFAULT 0, " +
                    "val_cession INTEGER DEFAULT 0, " +
                    "dat_service TIMESTAMP DEFAULT '0000-01-01 00:00:00.000', " +
                    "Chek INTEGER DEFAULT 0, " +
                    "code_barre NVARCHAR(50), " +
                    "code_famille NVARCHAR(50), " +
                    "attribut_set NVARCHAR(50), " +
                    "code_article NVARCHAR(50), " +
                    "motif NVARCHAR(50), " +
                    "user NVARCHAR(50), " +
                    "date_cre TIMESTAMP DEFAULT '0000-01-01 00:00:00.000', " +
                    "id_immo BIGINT PRIMARY KEY, " +
                    "attValr VARCHAR(1) DEFAULT '0', " +
                    "issync INTEGER DEFAULT 0, " +
                    "Code_RFID VARCHAR(50))";

    private static final String CREATE_TABLE_IMMO_INV_IMAGE =
            "CREATE TABLE " + TABLE_IMMO_INV_IMAGE + " (" +
                    "CodeImmo VARCHAR(50), " +
                    "CodeDoc VARCHAR(50), " +
                    "url VARCHAR(200), " +
                    "img LONGBLOB, " +
                    "parDefault TINYINT DEFAULT 0, " +
                    "codeInv VARCHAR(50), " +
                    "isSny INTEGER DEFAULT 0)";

    private static final String CREATE_TABLE_INV_ATTRIBUTINV_IMMO =
            "CREATE TABLE " + TABLE_INV_ATTRIBUTINV_IMMO + " (" +
                    "name NVARCHAR(128), " +
                    "codeatribut VARCHAR(50), " +
                    "saisie TINYINT DEFAULT 0, " +
                    "Codeinv VARCHAR(50), " +
                    "afficher TINYINT DEFAULT 0, " +
                    "validation TINYINT DEFAULT 0)";

    private static final String CREATE_TABLE_INV_EQUIPE =
            "CREATE TABLE " + TABLE_INV_EQUIPE + " (" +
                    "codeEquipe VARCHAR(50) UNIQUE, " +
                    "NoEquipe VARCHAR(50), " +
                    "chefEquipe VARCHAR(50), " +
                    "Codeinv VARCHAR(50), " +
                    "UserCreation VARCHAR(20), " +
                    "DateCreation TIMESTAMP)";

    private static final String CREATE_TABLE_INV_EQUIPE_INVENTAIRE =
            "CREATE TABLE " + TABLE_INV_EQUIPE_INVENTAIRE + " (" +
                    "CodeEmplacement VARCHAR(50), " +
                    "codeEquipe VARCHAR(50), " +
                    "codemission VARCHAR(50), " +
                    "Codeinv VARCHAR(50))";

    private static final String CREATE_TABLE_INV_IMMO =
            "CREATE TABLE " + TABLE_INV_IMMO + " (" +
                    "CodeInventaire VARCHAR(50) NOT NULL UNIQUE DEFAULT '0', " +
                    "DateCreation TIMESTAMP DEFAULT '0000-01-01 00:00:00.000', " +
                    "EtatInventaire INTEGER DEFAULT 0, " +
                    "DateCloture TIMESTAMP, " +
                    "UserCloture VARCHAR(20), " +
                    "UserCreation VARCHAR(20), " +
                    "Description VARCHAR(60), " +
                    "verif_art_inv TINYINT DEFAULT 0, " +
                    "affich_art_inv TINYINT DEFAULT 0, " +
                    "nbrAtt INTEGER DEFAULT 0, " +
                    "multi_eq_mis TINYINT DEFAULT 0)";

    private static final String CREATE_TABLE_INV_MISSION =
            "CREATE TABLE " + TABLE_INV_MISSION + " (" +
                    "codemission VARCHAR(50), " +
                    "etatmission VARCHAR(50), " +
                    "DateCree TIMESTAMP, " +
                    "UserCreation VARCHAR(20), " +
                    "DateMaj TIMESTAMP, " +
                    "usermaj VARCHAR(20), " +
                    "Description VARCHAR(60), " +
                    "Codeinv VARCHAR(50))";

    private static final String CREATE_TABLE_INV_EMOMBREEQUIPE =
            "CREATE TABLE " + TABLE_INV_EMOMBREEQUIPE + " (" +
                    "CodeUtil VARCHAR(20), " +
                    "codeEquipe VARCHAR(50), " +
                    "Codeinv VARCHAR(50), " +
                    "NOM VARCHAR(40))";

    private static final String CREATE_TABLE_INV_IMMO_ATTRIBUT_VALR =
            "CREATE TABLE " + TABLE_INV_IMMO_ATTRIBUT_VALR + " (" +
                    "codeInv VARCHAR(50), " +
                    "codeImmo VARCHAR(50), " +
                    "codeAttrib VARCHAR(50), " +
                    "valr VARCHAR(50), " +
                    "issync TINYINT DEFAULT 0)";

    private static final String CREATE_TABLE_PARAM =
            "CREATE TABLE " + TABLE_PARAM + " (" +
                    "adress VARCHAR(50) UNIQUE DEFAULT '0', " +
                    "mode VARCHAR(10) DEFAULT '0', " +
                    "projet VARCHAR(50))";

    private static final String CREATE_TABLE_PROPRIETAIRE =
            "CREATE TABLE " + TABLE_PROPRIETAIRE + " (" +
                    "CodeProprietaire VARCHAR(50) UNIQUE, " +
                    "Libelle VARCHAR(50), " +
                    "Addresse VARCHAR(150))";

    private static final String CREATE_TABLE_SOUS_ETAT =
            "CREATE TABLE " + TABLE_SOUS_ETAT + " (" +
                    "sousEtat TINYINT NOT NULL DEFAULT 0, " +
                    "LibelleEtat VARCHAR(50), " +
                    "TypeEtat VARCHAR(50), " +
                    "DescriptionEtat VARCHAR(200), " +
                    "coler VARCHAR(50) DEFAULT '0')";

    private static final String CREATE_TABLE_UTILISATEUR =
            "CREATE TABLE " + TABLE_UTILISATEUR + " (" +
                    "CodeUtil VARCHAR(20) UNIQUE, " +
                    "NomPrenomUtil VARCHAR(50), " +
                    "MotPasse VARCHAR(50), " +
                    "login VARCHAR(50))";

    private static final String CREATE_TABLE_VALEURS_ATTRIBUT =
            "CREATE TABLE " + TABLE_VALEURS_ATTRIBUT + " (" +
                    "codeAttribut VARCHAR(50), " +
                    "Valeur VARCHAR(50), " +
                    "isdefault TINYINT DEFAULT 0)";

    // Index creation statements
    private static final String CREATE_INDEX_ARBRE_EMP_PATH =
            "CREATE INDEX WDIDX_ArbreEmp_path ON " + TABLE_ARBRE_EMP + " (path)";

    private static final String CREATE_INDEX_ARBRE_FAMILE_CODE =
            "CREATE INDEX WDIDX_ArbreFamile_code ON " + TABLE_ARBRE_FAMILE + " (code)";

    private static final String CREATE_INDEX_ATTRIBUTS_LIBCODE =
            "CREATE INDEX WDIDX_Attributs_libCode ON " + TABLE_ATTRIBUTS + " (libCode)";

    private static final String CREATE_INDEX_ATTRIBUTS_ATTBSET_CODEATTRIBUS =
            "CREATE INDEX WDIDX_Attributs_attbset_codeAttribus ON " + TABLE_ATTRIBUTS_ATTBSET + " (codeAttribus)";

    private static final String CREATE_INDEX_ATTRIBUTS_ATTBSET_CODEATTRIBUSSET =
            "CREATE INDEX WDIDX_Attributs_attbset_codeAttribusSet ON " + TABLE_ATTRIBUTS_ATTBSET + " (codeAttribusSet)";

    private static final String CREATE_INDEX_ATTRIBUTS_ATTBSET_COMBINED =
            "CREATE INDEX WDIDX_Attributs_attbset_codeAttribuscodeAttribusSet ON " + TABLE_ATTRIBUTS_ATTBSET + " (codeAttribus,codeAttribusSet)";

    private static final String CREATE_INDEX_DET_INV_IMMO_CODEINVENTAIRE =
            "CREATE INDEX WDIDX_Det_inv_immo_CodeInventaire ON " + TABLE_DET_INV_IMMO + " (CodeInventaire)";

    private static final String CREATE_INDEX_DET_INV_IMMO_CODEIMMO =
            "CREATE INDEX WDIDX_Det_inv_immo_CodeImmo ON " + TABLE_DET_INV_IMMO + " (CodeImmo)";

    private static final String CREATE_INDEX_DET_INV_IMMO_CODEEMPTH =
            "CREATE INDEX WDIDX_Det_inv_immo_CodeEmpTh ON " + TABLE_DET_INV_IMMO + " (CodeEmpTh)";

    private static final String CREATE_INDEX_DET_INV_IMMO_CODEMISSION =
            "CREATE INDEX WDIDX_Det_inv_immo_codemission ON " + TABLE_DET_INV_IMMO + " (codemission)";

    private static final String CREATE_INDEX_DET_INV_IMMO_CODEEQUIPE =
            "CREATE INDEX WDIDX_Det_inv_immo_codeEquipe ON " + TABLE_DET_INV_IMMO + " (codeEquipe)";

    private static final String CREATE_INDEX_DET_INV_IMMO_CODEARTICLE =
            "CREATE INDEX WDIDX_Det_inv_immo_codeArticle ON " + TABLE_DET_INV_IMMO + " (codeArticle)";

    private static final String CREATE_INDEX_DET_INV_IMMO_RFIDCODE_CORR =
            "CREATE INDEX WDIDX_Det_inv_immo_RFIDCode_Corr ON " + TABLE_DET_INV_IMMO + " (RFIDCode_Corr)";

    private static final String CREATE_UNIQUE_INDEX_DET_INV_IMMO =
            "CREATE UNIQUE INDEX WDIDX_Det_inv_immo_CodeInventaireCodeImmo ON " + TABLE_DET_INV_IMMO + " (CodeInventaire,CodeImmo)";

    private static final String CREATE_INDEX_HISTORIQUE_ECAR_INV_CODE_IMMO =
            "CREATE INDEX WDIDX_historique_ecar_inv_Code_immo ON " + TABLE_HISTORIQUE_ECAR_INV + " (Code_immo)";

    private static final String CREATE_INDEX_HISTORIQUE_ECAR_INV_CODE_INV =
            "CREATE INDEX WDIDX_historique_ecar_inv_code_Inv ON " + TABLE_HISTORIQUE_ECAR_INV + " (code_Inv)";

    private static final String CREATE_INDEX_HISTORIQUE_ECAR_INV_CODE_ATT =
            "CREATE INDEX WDIDX_historique_ecar_inv_code_att ON " + TABLE_HISTORIQUE_ECAR_INV + " (code_att)";

    private static final String CREATE_INDEX_HISTORIQUE_ECAR_INV_COMBINED =
            "CREATE INDEX WDIDX_historique_ecar_inv_code_InvCode_immoCode_att ON " + TABLE_HISTORIQUE_ECAR_INV + " (code_Inv,Code_immo,code_att)";

    private static final String CREATE_INDEX_IMMO_DESCP_IMMO =
            "CREATE INDEX WDIDX_Immo_Descp_immo ON " + TABLE_IMMO + " (Descp_immo)";

    private static final String CREATE_INDEX_IMMO_CODE_ETAT =
            "CREATE INDEX WDIDX_Immo_code_etat ON " + TABLE_IMMO + " (code_etat)";

    private static final String CREATE_INDEX_IMMO_FOURNSR =
            "CREATE INDEX WDIDX_Immo_fournsr ON " + TABLE_IMMO + " (fournsr)";

    private static final String CREATE_INDEX_IMMO_CODE_IMMO =
            "CREATE INDEX WDIDX_Immo_Code_immo ON " + TABLE_IMMO + " (Code_immo)";

    private static final String CREATE_INDEX_IMMO_EMPLOYEE =
            "CREATE INDEX WDIDX_Immo_Employee ON " + TABLE_IMMO + " (Employee)";

    private static final String CREATE_INDEX_IMMO_NUM_SERIE =
            "CREATE INDEX WDIDX_Immo_Num_serie ON " + TABLE_IMMO + " (Num_serie)";

    private static final String CREATE_INDEX_IMMO_EMPLACEMENT =
            "CREATE INDEX WDIDX_Immo_emplacement ON " + TABLE_IMMO + " (emplacement)";

    private static final String CREATE_INDEX_IMMO_CODE_BARRE =
            "CREATE INDEX WDIDX_Immo_code_barre ON " + TABLE_IMMO + " (code_barre)";

    private static final String CREATE_INDEX_IMMO_CODE_FAMILLE =
            "CREATE INDEX WDIDX_Immo_code_famille ON " + TABLE_IMMO + " (code_famille)";

    private static final String CREATE_INDEX_IMMO_ATTRIBUT_SET =
            "CREATE INDEX WDIDX_Immo_attribut_set ON " + TABLE_IMMO + " (attribut_set)";

    private static final String CREATE_INDEX_IMMO_CODE_RFID =
            "CREATE INDEX WDIDX_Immo_Code_RFID ON " + TABLE_IMMO + " (Code_RFID)";

    private static final String CREATE_INDEX_IMMO_INV_IMAGE_CODEIMMO =
            "CREATE INDEX WDIDX_immo_inv_image_CodeImmo ON " + TABLE_IMMO_INV_IMAGE + " (CodeImmo)";

    private static final String CREATE_INDEX_IMMO_INV_IMAGE_CODEDOC =
            "CREATE INDEX WDIDX_immo_inv_image_CodeDoc ON " + TABLE_IMMO_INV_IMAGE + " (CodeDoc)";

    private static final String CREATE_INDEX_IMMO_INV_IMAGE_CODEINV =
            "CREATE INDEX WDIDX_immo_inv_image_codeInv ON " + TABLE_IMMO_INV_IMAGE + " (codeInv)";

    private static final String CREATE_INDEX_IMMO_INV_IMAGE_COMBINED1 =
            "CREATE INDEX WDIDX_immo_inv_image_CodeImmoCodeDoc ON " + TABLE_IMMO_INV_IMAGE + " (CodeImmo,CodeDoc)";

    private static final String CREATE_INDEX_IMMO_INV_IMAGE_COMBINED2 =
            "CREATE INDEX WDIDX_immo_inv_image_codeInvCodeImmoCodeDoc ON " + TABLE_IMMO_INV_IMAGE + " (codeInv,CodeImmo,CodeDoc)";

    private static final String CREATE_INDEX_IMMO_INV_IMAGE_COMBINED3 =
            "CREATE INDEX WDIDX_immo_inv_image_codeInvCodeImmo ON " + TABLE_IMMO_INV_IMAGE + " (codeInv,CodeImmo)";

    private static final String CREATE_INDEX_INV_ATTRIBUTINV_IMMO_CODEATRIBUT =
            "CREATE INDEX WDIDX_Inv_attributinvImmo_codeatribut ON " + TABLE_INV_ATTRIBUTINV_IMMO + " (codeatribut)";

    private static final String CREATE_INDEX_INV_ATTRIBUTINV_IMMO_CODEINV =
            "CREATE INDEX WDIDX_Inv_attributinvImmo_Codeinv ON " + TABLE_INV_ATTRIBUTINV_IMMO + " (Codeinv)";

    private static final String CREATE_INDEX_INV_ATTRIBUTINV_IMMO_COMBINED1 =
            "CREATE INDEX WDIDX_Inv_attributinvImmo_Codeinvname ON " + TABLE_INV_ATTRIBUTINV_IMMO + " (Codeinv,name)";

    private static final String CREATE_INDEX_INV_ATTRIBUTINV_IMMO_COMBINED2 =
            "CREATE INDEX WDIDX_Inv_attributinvImmo_Codeinvcodeatribut ON " + TABLE_INV_ATTRIBUTINV_IMMO + " (Codeinv,codeatribut)";

    private static final String CREATE_INDEX_INV_EQUIPE_NOEQUIPE =
            "CREATE INDEX WDIDX_Inv_equipe_NoEquipe ON " + TABLE_INV_EQUIPE + " (NoEquipe)";

    private static final String CREATE_INDEX_INV_EQUIPE_CHEFEQUIPE =
            "CREATE INDEX WDIDX_Inv_equipe_chefEquipe ON " + TABLE_INV_EQUIPE + " (chefEquipe)";

    private static final String CREATE_INDEX_INV_EQUIPE_CODEINV =
            "CREATE INDEX WDIDX_Inv_equipe_Codeinv ON " + TABLE_INV_EQUIPE + " (Codeinv)";

    private static final String CREATE_INDEX_INV_EQUIPE_COMBINED =
            "CREATE INDEX WDIDX_Inv_equipe_CodeinvcodeEquipe ON " + TABLE_INV_EQUIPE + " (Codeinv,codeEquipe)";

    private static final String CREATE_INDEX_INV_EQUIPE_INVENTAIRE_CODEEMPLACEMENT =
            "CREATE INDEX WDIDX_Inv_EquipeEmpl_CodeEmplacement ON " + TABLE_INV_EQUIPE_INVENTAIRE + " (CodeEmplacement)";

    private static final String CREATE_INDEX_INV_EQUIPE_INVENTAIRE_CODEEQUIPE =
            "CREATE INDEX WDIDX_Inv_EquipeEmpl_codeEquipe ON " + TABLE_INV_EQUIPE_INVENTAIRE + " (codeEquipe)";

    private static final String CREATE_INDEX_INV_EQUIPE_INVENTAIRE_CODEMISSION =
            "CREATE INDEX WDIDX_Inv_EquipeEmpl_codemission ON " + TABLE_INV_EQUIPE_INVENTAIRE + " (codemission)";

    private static final String CREATE_INDEX_INV_EQUIPE_INVENTAIRE_CODEINV =
            "CREATE INDEX WDIDX_Inv_EquipeEmpl_Codeinv ON " + TABLE_INV_EQUIPE_INVENTAIRE + " (Codeinv)";

    private static final String CREATE_UNIQUE_INDEX_INV_EQUIPE_INVENTAIRE =
            "CREATE UNIQUE INDEX WDIDX_Inv_EquipeEmpl_CodeinvcodemissioncodeEquipeCodeEmplacement ON " + TABLE_INV_EQUIPE_INVENTAIRE + " (Codeinv,codemission,codeEquipe,CodeEmplacement)";

    private static final String CREATE_INDEX_INV_EQUIPE_INVENTAIRE_COMBINED1 =
            "CREATE INDEX WDIDX_Inv_EquipeEmpl_CodeinvcodeEquipecodemission ON " + TABLE_INV_EQUIPE_INVENTAIRE + " (Codeinv,codeEquipe,codemission)";

    private static final String CREATE_INDEX_INV_EQUIPE_INVENTAIRE_COMBINED2 =
            "CREATE INDEX WDIDX_Inv_EquipeEmpl_CodeinvCodeEmplacement ON " + TABLE_INV_EQUIPE_INVENTAIRE + " (Codeinv,CodeEmplacement)";

    private static final String CREATE_INDEX_INV_IMMO_DATECREATION =
            "CREATE INDEX WDIDX_inv_immo_DateCreation ON " + TABLE_INV_IMMO + " (DateCreation)";

    private static final String CREATE_INDEX_INV_IMMO_ETATINVENTAIRE =
            "CREATE INDEX WDIDX_inv_immo_EtatInventaire ON " + TABLE_INV_IMMO + " (EtatInventaire)";

    private static final String CREATE_INDEX_INV_IMMO_DATECLOTURE =
            "CREATE INDEX WDIDX_inv_immo_DateCloture ON " + TABLE_INV_IMMO + " (DateCloture)";

    private static final String CREATE_INDEX_INV_MISSION_CODEMISSION =
            "CREATE INDEX WDIDX_inv_mission_codemission ON " + TABLE_INV_MISSION + " (codemission)";

    private static final String CREATE_INDEX_INV_MISSION_CODEINV =
            "CREATE INDEX WDIDX_inv_mission_Codeinv ON " + TABLE_INV_MISSION + " (Codeinv)";

    private static final String CREATE_INDEX_INV_MISSION_COMBINED =
            "CREATE INDEX WDIDX_inv_mission_Codeinvcodemission ON " + TABLE_INV_MISSION + " (Codeinv,codemission)";

    private static final String CREATE_INDEX_INV_EMOMBREEQUIPE_CODEUTIL =
            "CREATE INDEX WDIDX_Inv_Mombreequipe_CodeUtil ON " + TABLE_INV_EMOMBREEQUIPE + " (CodeUtil)";

    private static final String CREATE_INDEX_INV_EMOMBREEQUIPE_CODEEQUIPE =
            "CREATE INDEX WDIDX_Inv_Mombreequipe_codeEquipe ON " + TABLE_INV_EMOMBREEQUIPE + " (codeEquipe)";

    private static final String CREATE_INDEX_INV_EMOMBREEQUIPE_CODEINV =
            "CREATE INDEX WDIDX_Inv_Mombreequipe_Codeinv ON " + TABLE_INV_EMOMBREEQUIPE + " (Codeinv)";

    private static final String CREATE_INDEX_INV_EMOMBREEQUIPE_COMBINED =
            "CREATE INDEX WDIDX_Inv_Mombreequipe_CodeinvcodeEquipeCodeUtil ON " + TABLE_INV_EMOMBREEQUIPE + " (Codeinv,codeEquipe,CodeUtil)";

    private static final String CREATE_INDEX_INV_IMMO_ATTRIBUT_VALR_CODEINV =
            "CREATE INDEX WDIDX_InvImmoAttributValr_codeInv ON " + TABLE_INV_IMMO_ATTRIBUT_VALR + " (codeInv)";

    private static final String CREATE_INDEX_INV_IMMO_ATTRIBUT_VALR_CODEIMMO =
            "CREATE INDEX WDIDX_InvImmoAttributValr_codeImmo ON " + TABLE_INV_IMMO_ATTRIBUT_VALR + " (codeImmo)";

    private static final String CREATE_INDEX_INV_IMMO_ATTRIBUT_VALR_CODEATTRIB =
            "CREATE INDEX WDIDX_InvImmoAttributValr_codeAttrib ON " + TABLE_INV_IMMO_ATTRIBUT_VALR + " (codeAttrib)";

    private static final String CREATE_INDEX_INV_IMMO_ATTRIBUT_VALR_COMBINED1 =
            "CREATE INDEX WDIDX_InvImmoAttributValr_codeInvcodeImmo ON " + TABLE_INV_IMMO_ATTRIBUT_VALR + " (codeInv,codeImmo)";

    private static final String CREATE_INDEX_INV_IMMO_ATTRIBUT_VALR_COMBINED2 =
            "CREATE INDEX WDIDX_InvImmoAttributValr_codeInvcodeImmocodeAttrib ON " + TABLE_INV_IMMO_ATTRIBUT_VALR + " (codeInv,codeImmo,codeAttrib)";

    private static final String CREATE_INDEX_SOUS_ETAT_SOUSETAT =
            "CREATE INDEX WDIDX_SouSEtat_sousEtat ON " + TABLE_SOUS_ETAT + " (sousEtat)";

    private static final String CREATE_INDEX_SOUS_ETAT_LIBELLEETAT =
            "CREATE INDEX WDIDX_SouSEtat_LibelleEtat ON " + TABLE_SOUS_ETAT + " (LibelleEtat)";

    private static final String CREATE_INDEX_UTILISATEUR_LOGIN =
            "CREATE INDEX WDIDX_Utilisateur_login ON " + TABLE_UTILISATEUR + " (login)";

    private static final String CREATE_INDEX_VALEURS_ATTRIBUT_CODEATTRIBUT =
            "CREATE INDEX WDIDX_valeursAttribut_codeAttribut ON " + TABLE_VALEURS_ATTRIBUT + " (codeAttribut)";

    private static final String CREATE_INDEX_VALEURS_ATTRIBUT_VALEUR =
            "CREATE INDEX WDIDX_valeursAttribut_Valeur ON " + TABLE_VALEURS_ATTRIBUT + " (Valeur)";

    private static final String CREATE_INDEX_VALEURS_ATTRIBUT_COMBINED =
            "CREATE INDEX WDIDX_valeursAttribut_codeAttributValeur ON " + TABLE_VALEURS_ATTRIBUT + " (codeAttribut,Valeur)";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(CREATE_TABLE_ARBRE_EMP);
        db.execSQL(CREATE_TABLE_ARBRE_FAMILE);
        db.execSQL(CREATE_TABLE_ATTRIBUTS);
        db.execSQL(CREATE_TABLE_ATTRIBUTS_ATTBSET);
        db.execSQL(CREATE_TABLE_CONGF_SYSTEME);
        db.execSQL(CREATE_TABLE_DET_INV_IMMO);
        db.execSQL(CREATE_TABLE_EMPLOYEE);
        db.execSQL(CREATE_TABLE_FOURNISSEUR);
        db.execSQL(CREATE_TABLE_HISTORIQUE_ECAR_INV);
        db.execSQL(CREATE_TABLE_IMMO);
        db.execSQL(CREATE_TABLE_IMMO_INV_IMAGE);
        db.execSQL(CREATE_TABLE_INV_ATTRIBUTINV_IMMO);
        db.execSQL(CREATE_TABLE_INV_EQUIPE);
        db.execSQL(CREATE_TABLE_INV_EQUIPE_INVENTAIRE);
        db.execSQL(CREATE_TABLE_INV_IMMO);
        db.execSQL(CREATE_TABLE_INV_MISSION);
        db.execSQL(CREATE_TABLE_INV_EMOMBREEQUIPE);
        db.execSQL(CREATE_TABLE_INV_IMMO_ATTRIBUT_VALR);
        db.execSQL(CREATE_TABLE_PARAM);
        db.execSQL(CREATE_TABLE_PROPRIETAIRE);
        db.execSQL(CREATE_TABLE_SOUS_ETAT);
        db.execSQL(CREATE_TABLE_UTILISATEUR);
        db.execSQL(CREATE_TABLE_VALEURS_ATTRIBUT);

        // Create indexes
        db.execSQL(CREATE_INDEX_ARBRE_EMP_PATH);
        db.execSQL(CREATE_INDEX_ARBRE_FAMILE_CODE);
        db.execSQL(CREATE_INDEX_ATTRIBUTS_LIBCODE);
        db.execSQL(CREATE_INDEX_ATTRIBUTS_ATTBSET_CODEATTRIBUS);
        db.execSQL(CREATE_INDEX_ATTRIBUTS_ATTBSET_CODEATTRIBUSSET);
        db.execSQL(CREATE_INDEX_ATTRIBUTS_ATTBSET_COMBINED);
        db.execSQL(CREATE_INDEX_DET_INV_IMMO_CODEINVENTAIRE);
        db.execSQL(CREATE_INDEX_DET_INV_IMMO_CODEIMMO);
        db.execSQL(CREATE_INDEX_DET_INV_IMMO_CODEEMPTH);
        db.execSQL(CREATE_INDEX_DET_INV_IMMO_CODEMISSION);
        db.execSQL(CREATE_INDEX_DET_INV_IMMO_CODEEQUIPE);
        db.execSQL(CREATE_INDEX_DET_INV_IMMO_CODEARTICLE);
        db.execSQL(CREATE_INDEX_DET_INV_IMMO_RFIDCODE_CORR);
        db.execSQL(CREATE_UNIQUE_INDEX_DET_INV_IMMO);
        db.execSQL(CREATE_INDEX_HISTORIQUE_ECAR_INV_CODE_IMMO);
        db.execSQL(CREATE_INDEX_HISTORIQUE_ECAR_INV_CODE_INV);
        db.execSQL(CREATE_INDEX_HISTORIQUE_ECAR_INV_CODE_ATT);
        db.execSQL(CREATE_INDEX_HISTORIQUE_ECAR_INV_COMBINED);
        db.execSQL(CREATE_INDEX_IMMO_DESCP_IMMO);
        db.execSQL(CREATE_INDEX_IMMO_CODE_ETAT);
        db.execSQL(CREATE_INDEX_IMMO_FOURNSR);
        db.execSQL(CREATE_INDEX_IMMO_CODE_IMMO);
        db.execSQL(CREATE_INDEX_IMMO_EMPLOYEE);
        db.execSQL(CREATE_INDEX_IMMO_NUM_SERIE);
        db.execSQL(CREATE_INDEX_IMMO_EMPLACEMENT);
        db.execSQL(CREATE_INDEX_IMMO_CODE_BARRE);
        db.execSQL(CREATE_INDEX_IMMO_CODE_FAMILLE);
        db.execSQL(CREATE_INDEX_IMMO_ATTRIBUT_SET);
        db.execSQL(CREATE_INDEX_IMMO_CODE_RFID);
        db.execSQL(CREATE_INDEX_IMMO_INV_IMAGE_CODEIMMO);
        db.execSQL(CREATE_INDEX_IMMO_INV_IMAGE_CODEDOC);
        db.execSQL(CREATE_INDEX_IMMO_INV_IMAGE_CODEINV);
        db.execSQL(CREATE_INDEX_IMMO_INV_IMAGE_COMBINED1);
        db.execSQL(CREATE_INDEX_IMMO_INV_IMAGE_COMBINED2);
        db.execSQL(CREATE_INDEX_IMMO_INV_IMAGE_COMBINED3);
        db.execSQL(CREATE_INDEX_INV_ATTRIBUTINV_IMMO_CODEATRIBUT);
        db.execSQL(CREATE_INDEX_INV_ATTRIBUTINV_IMMO_CODEINV);
        db.execSQL(CREATE_INDEX_INV_ATTRIBUTINV_IMMO_COMBINED1);
        db.execSQL(CREATE_INDEX_INV_ATTRIBUTINV_IMMO_COMBINED2);
        db.execSQL(CREATE_INDEX_INV_EQUIPE_NOEQUIPE);
        db.execSQL(CREATE_INDEX_INV_EQUIPE_CHEFEQUIPE);
        db.execSQL(CREATE_INDEX_INV_EQUIPE_CODEINV);
        db.execSQL(CREATE_INDEX_INV_EQUIPE_COMBINED);
        db.execSQL(CREATE_INDEX_INV_EQUIPE_INVENTAIRE_CODEEMPLACEMENT);
        db.execSQL(CREATE_INDEX_INV_EQUIPE_INVENTAIRE_CODEEQUIPE);
        db.execSQL(CREATE_INDEX_INV_EQUIPE_INVENTAIRE_CODEMISSION);
        db.execSQL(CREATE_INDEX_INV_EQUIPE_INVENTAIRE_CODEINV);
        db.execSQL(CREATE_UNIQUE_INDEX_INV_EQUIPE_INVENTAIRE);
        db.execSQL(CREATE_INDEX_INV_EQUIPE_INVENTAIRE_COMBINED1);
        db.execSQL(CREATE_INDEX_INV_EQUIPE_INVENTAIRE_COMBINED2);
        db.execSQL(CREATE_INDEX_INV_IMMO_DATECREATION);
        db.execSQL(CREATE_INDEX_INV_IMMO_ETATINVENTAIRE);
        db.execSQL(CREATE_INDEX_INV_IMMO_DATECLOTURE);
        db.execSQL(CREATE_INDEX_INV_MISSION_CODEMISSION);
        db.execSQL(CREATE_INDEX_INV_MISSION_CODEINV);
        db.execSQL(CREATE_INDEX_INV_MISSION_COMBINED);
        db.execSQL(CREATE_INDEX_INV_EMOMBREEQUIPE_CODEUTIL);
        db.execSQL(CREATE_INDEX_INV_EMOMBREEQUIPE_CODEEQUIPE);
        db.execSQL(CREATE_INDEX_INV_EMOMBREEQUIPE_CODEINV);
        db.execSQL(CREATE_INDEX_INV_EMOMBREEQUIPE_COMBINED);
        db.execSQL(CREATE_INDEX_INV_IMMO_ATTRIBUT_VALR_CODEINV);
        db.execSQL(CREATE_INDEX_INV_IMMO_ATTRIBUT_VALR_CODEIMMO);
        db.execSQL(CREATE_INDEX_INV_IMMO_ATTRIBUT_VALR_CODEATTRIB);
        db.execSQL(CREATE_INDEX_INV_IMMO_ATTRIBUT_VALR_COMBINED1);
        db.execSQL(CREATE_INDEX_INV_IMMO_ATTRIBUT_VALR_COMBINED2);
        db.execSQL(CREATE_INDEX_SOUS_ETAT_SOUSETAT);
        db.execSQL(CREATE_INDEX_SOUS_ETAT_LIBELLEETAT);
        db.execSQL(CREATE_INDEX_UTILISATEUR_LOGIN);
        db.execSQL(CREATE_INDEX_VALEURS_ATTRIBUT_CODEATTRIBUT);
        db.execSQL(CREATE_INDEX_VALEURS_ATTRIBUT_VALEUR);
        db.execSQL(CREATE_INDEX_VALEURS_ATTRIBUT_COMBINED);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop tables if they exist and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VALEURS_ATTRIBUT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UTILISATEUR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SOUS_ETAT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROPRIETAIRE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARAM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV_IMMO_ATTRIBUT_VALR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV_EMOMBREEQUIPE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV_MISSION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV_IMMO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV_EQUIPE_INVENTAIRE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV_EQUIPE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV_ATTRIBUTINV_IMMO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMMO_INV_IMAGE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMMO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORIQUE_ECAR_INV);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOURNISSEUR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DET_INV_IMMO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONGF_SYSTEME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTRIBUTS_ATTBSET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTRIBUTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARBRE_FAMILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARBRE_EMP);

        onCreate(db);
    }
    
    public boolean addUser(Utilisateur utilisateur) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put("CodeUtil", utilisateur.CodeUtil);
        values.put("NomPrenomUtil", utilisateur.NomPrenomUtil);
        values.put("MotPasse", utilisateur.MotPasse);
        values.put("login", utilisateur.login);
        
        long result = db.insert(TABLE_UTILISATEUR, null, values);
        db.close();
        
        return result != -1;
    }

    public boolean checkUser(String login, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"CodeUtil"};
        String selection = "login = ? AND MotPasse = ?";
        String[] selectionArgs = {login, password};
        
        Cursor cursor = db.query(TABLE_UTILISATEUR, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        
        return count > 0;
    }

    public Utilisateur getUserDetails(String login) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"CodeUtil", "NomPrenomUtil", "MotPasse", "login"};
        String selection = "login = ?";
        String[] selectionArgs = {login};
        
        Cursor cursor = db.query(TABLE_UTILISATEUR, columns, selection, selectionArgs, null, null, null);
        
        Utilisateur utilisateur = null;
        if (cursor.moveToFirst()) {
            utilisateur = new Utilisateur();
            utilisateur.CodeUtil = cursor.getString(0);
            utilisateur.NomPrenomUtil = cursor.getString(1);
            utilisateur.MotPasse = cursor.getString(2);
            utilisateur.login = cursor.getString(3);
        }
        cursor.close();
        db.close();
        
        return utilisateur;
    }

}