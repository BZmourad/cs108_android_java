-- Création de la table ArbreEmp
CREATE TABLE "ArbreEmp"("code" NVARCHAR(50)  NOT NULL  UNIQUE,"Libelle" NVARCHAR(50),"Idparent" NVARCHAR(20),"path" NVARCHAR(200),"n1" VARCHAR(50),"n2" VARCHAR(50),"n3" VARCHAR(50),"n4" VARCHAR(50),"n5" VARCHAR(50),"n6" VARCHAR(50),"n7" VARCHAR(50),"n8" VARCHAR(50),"n9" VARCHAR(50),"n10" VARCHAR(50) );
CREATE INDEX "WDIDX_ArbreEmp_path" ON "ArbreEmp" ("path");

-- Création de la table ArbreFamile
CREATE TABLE "ArbreFamile"("code" VARCHAR(50)  NOT NULL,"Libelle" VARCHAR(50),"Idparent" VARCHAR(20) );
CREATE INDEX "WDIDX_ArbreFamile_code" ON "ArbreFamile" ("code");

-- Création de la table Attributs
CREATE TABLE "Attributs"("Description" VARCHAR(50),"obligatoire" BIT,"type" VARCHAR(50),"valeurDéfaut" VARCHAR(50),"libCode" VARCHAR(50),"isAffiche" BIT,"isInconnu" BIT );
CREATE INDEX "WDIDX_Attributs_libCode" ON "Attributs" ("libCode");

-- Création de la table Attributs_attbset
CREATE TABLE "Attributs_attbset"("codeAttribus" VARCHAR(50),"codeAttribusSet" VARCHAR(50) );
CREATE INDEX "WDIDX_Attributs_attbset_codeAttribus" ON "Attributs_attbset" ("codeAttribus");
CREATE INDEX "WDIDX_Attributs_attbset_codeAttribusSet" ON "Attributs_attbset" ("codeAttribusSet");
CREATE INDEX "WDIDX_Attributs_attbset_codeAttribuscodeAttribusSet" ON "Attributs_attbset" ("codeAttribus","codeAttribusSet");

-- Création de la table congfSysteme
CREATE TABLE "congfSysteme"("isAttset" BIT,"isCreImmo" BIT,"isInvPartl" BIT,"empldefaut" NVARCHAR(50),"isUpdateValide" BIT,"isVerfEtat" BIT,"isVerfFmlle" BIT,"isVerfFrnsr" BIT,"isVerfEmplye" BIT,"isVrfEmp" BIT,"GenrJurdque" BIT,"GenrUtlstr" BIT,"GenrEmply" BIT,"n10" VARCHAR(50),"GenrFornsr" BIT,"nbr_Niveau_empl" VARCHAR(4),"affiche_Niveau" BIT,"n1" VARCHAR(50),"n2" VARCHAR(50),"n3" VARCHAR(50),"n4" VARCHAR(50),"n5" VARCHAR(50),"n6" VARCHAR(50),"n7" VARCHAR(50),"n8" VARCHAR(50),"n9" VARCHAR(50) );

-- Création de la table Det_inv_immo
CREATE TABLE "Det_inv_immo"("CodeInventaire" NVARCHAR(50),"CodeImmo" NVARCHAR(50),"CodeEmpTh" NVARCHAR(50),"DateLecture" TIMESTAMP,"UserLecture" NVARCHAR(20),"Observation" NVARCHAR(120),"DateValidation" TIMESTAMP,"Uservalidation" NVARCHAR(20),"EtatCorrection" SMALLINT,"codeEmpInv" NVARCHAR(50),"codeEmpCor" NVARCHAR(50),"codemission" NVARCHAR(50),"codeEquipe" NVARCHAR(50),"codeArticle" NVARCHAR(50),"codeEtat_Corr" NVARCHAR(50),"empl_Corr" NVARCHAR(50),"codeBarre_Corr" NVARCHAR(50),"NumSerie_Corr" NVARCHAR(50),"nbr_Valid" INTEGER,"nbr_Crt" INTEGER,"nbr_Crt_valid" INTEGER,"Descp_immo" NVARCHAR(120),"issync" INTEGER,"RFIDCode_Corr" VARCHAR(50) );
CREATE INDEX "WDIDX_Det_inv_immo_CodeInventaire" ON "Det_inv_immo" ("CodeInventaire");
CREATE INDEX "WDIDX_Det_inv_immo_CodeImmo" ON "Det_inv_immo" ("CodeImmo");
CREATE INDEX "WDIDX_Det_inv_immo_CodeEmpTh" ON "Det_inv_immo" ("CodeEmpTh");
CREATE INDEX "WDIDX_Det_inv_immo_codemission" ON "Det_inv_immo" ("codemission");
CREATE INDEX "WDIDX_Det_inv_immo_codeEquipe" ON "Det_inv_immo" ("codeEquipe");
CREATE INDEX "WDIDX_Det_inv_immo_codeArticle" ON "Det_inv_immo" ("codeArticle");
CREATE INDEX "WDIDX_Det_inv_immo_RFIDCode_Corr" ON "Det_inv_immo" ("RFIDCode_Corr");
CREATE INDEX "WDIDX_Det_inv_immo_CodeInventaireCodeImmo" ON "Det_inv_immo" ("CodeInventaire","CodeImmo");

-- Création de la table Employee
CREATE TABLE "Employee"("CodeFunction" VARCHAR(50),"CodeGroup" VARCHAR(50),"CodeEmployee" VARCHAR(50)  UNIQUE,"DateCre" TIMESTAMP,"NomPrenom" VARCHAR(50),"EmailWork" VARCHAR(50),"NumPerso" NUMERIC(20,0),"NumTravail" NUMERIC(20,0),"EmailPerso" VARCHAR(50),"Addresse" VARCHAR(150),"Notes" LONGVARCHAR,"ETAT" SMALLINT,"UserCreation" VARCHAR(20),"UseMaj" VARCHAR(20),"DateMaj" TIMESTAMP );

-- Création de la table FOURNISSEUR
CREATE TABLE "dbo"."FOURNISSEUR"("IDFournisseur" VARCHAR(50)  UNIQUE,"CompteCollectif" VARCHAR(15),"CompteCharge" VARCHAR(15),"NumTVA" VARCHAR(50),"Reference" VARCHAR(50),"Notes" LONGVARCHAR,"Etat" SMALLINT,"DateHeureCreation" DATE,"AuteurCreation" VARCHAR(50),"DateHeureModification" DATE,"AuteurModification" VARCHAR(50),"Vide" LONGVARCHAR,"Lettrage" VARCHAR(10),"Code" VARCHAR(50),"Adresse_email" VARCHAR(260),"Code_Postal" VARCHAR(5),"Fax" VARCHAR(13),"Numero_Fournisseur" NUMERIC(10,0),"Pays" VARCHAR(64),"ADR1" VARCHAR(20),"ADR2" VARCHAR(20),"CP" VARCHAR(5),"VILLE" VARCHAR(40),"Conttact" VARCHAR(120) );

-- Création de la table historique_ecar_inv
CREATE TABLE "historique_ecar_inv"("Code_immo" NVARCHAR(50),"code_Inv" NVARCHAR(50),"DateValidation" TIMESTAMP,"Uservalidation" NVARCHAR(20),"code_att_inv" NVARCHAR(50),"code_att_th" NVARCHAR(50),"typeVal" NVARCHAR(50),"code_att" NVARCHAR(50) );
CREATE INDEX "WDIDX_historique_ecar_inv_Code_immo" ON "historique_ecar_inv" ("Code_immo");
CREATE INDEX "WDIDX_historique_ecar_inv_code_Inv" ON "historique_ecar_inv" ("code_Inv");
CREATE INDEX "WDIDX_historique_ecar_inv_code_att" ON "historique_ecar_inv" ("code_att");
CREATE INDEX "WDIDX_historique_ecar_inv_code_InvCode_immoCode_att" ON "historique_ecar_inv" ("code_Inv","Code_immo","code_att");

-- Création de la table Immo
CREATE TABLE "Immo"("Descp_immo" NVARCHAR(120),"code_etat" NVARCHAR(50),"fournsr" NVARCHAR(40),"Code_immo" NVARCHAR(50)  NOT NULL,"Employee" NVARCHAR(50),"type_etat" NVARCHAR(50),"Num_serie" NVARCHAR(50),"entite_juridique" NVARCHAR(50),"prix_acquis" INTEGER,"emplacement" NVARCHAR(50),"date_desactivation" TIMESTAMP,"date_aquisition" TIMESTAMP,"prix_cession" INTEGER,"val_cession" INTEGER,"dat_service" TIMESTAMP,"Chek" INTEGER,"code_barre" NVARCHAR(50),"code_famille" NVARCHAR(50),"attribut_set" NVARCHAR(50),"code_article" NVARCHAR(50),"motif" NVARCHAR(50),"user" NVARCHAR(50),"date_cre" TIMESTAMP,"id_immo" NUMERIC(19,0)  PRIMARY KEY,"attValr" VARCHAR(1),"issync" INTEGER,"Code_RFID" VARCHAR(50) );
CREATE INDEX "WDIDX_Immo_Descp_immo" ON "Immo" ("Descp_immo");
CREATE INDEX "WDIDX_Immo_code_etat" ON "Immo" ("code_etat");
CREATE INDEX "WDIDX_Immo_fournsr" ON "Immo" ("fournsr");
CREATE INDEX "WDIDX_Immo_Code_immo" ON "Immo" ("Code_immo");
CREATE INDEX "WDIDX_Immo_Employee" ON "Immo" ("Employee");
CREATE INDEX "WDIDX_Immo_Num_serie" ON "Immo" ("Num_serie");
CREATE INDEX "WDIDX_Immo_emplacement" ON "Immo" ("emplacement");
CREATE INDEX "WDIDX_Immo_code_barre" ON "Immo" ("code_barre");
CREATE INDEX "WDIDX_Immo_code_famille" ON "Immo" ("code_famille");
CREATE INDEX "WDIDX_Immo_attribut_set" ON "Immo" ("attribut_set");
CREATE INDEX "WDIDX_Immo_Code_RFID" ON "Immo" ("Code_RFID");

-- Création de la table immo_inv_image
CREATE TABLE "immo_inv_image"("CodeImmo" VARCHAR(50),"CodeDoc" VARCHAR(50),"url" VARCHAR(200),"img" LONGVARBINARY,"parDefault" BIT,"codeInv" VARCHAR(50),"isSny" INTEGER );
CREATE INDEX "WDIDX_immo_inv_image_CodeImmo" ON "immo_inv_image" ("CodeImmo");
CREATE INDEX "WDIDX_immo_inv_image_CodeDoc" ON "immo_inv_image" ("CodeDoc");
CREATE INDEX "WDIDX_immo_inv_image_codeInv" ON "immo_inv_image" ("codeInv");
CREATE INDEX "WDIDX_immo_inv_image_CodeImmoCodeDoc" ON "immo_inv_image" ("CodeImmo","CodeDoc");
CREATE INDEX "WDIDX_immo_inv_image_codeInvCodeImmoCodeDoc" ON "immo_inv_image" ("codeInv","CodeImmo","CodeDoc");
CREATE INDEX "WDIDX_immo_inv_image_codeInvCodeImmo" ON "immo_inv_image" ("codeInv","CodeImmo");

-- Création de la table Inv_attributinvImmo
CREATE TABLE "Inv_attributinvImmo"("name" NVARCHAR(128),"codeatribut" VARCHAR(50),"saisie" BIT,"Codeinv" VARCHAR(50),"afficher" BIT,"validation" BIT );
CREATE INDEX "WDIDX_Inv_attributinvImmo_codeatribut" ON "Inv_attributinvImmo" ("codeatribut");
CREATE INDEX "WDIDX_Inv_attributinvImmo_Codeinv" ON "Inv_attributinvImmo" ("Codeinv");
CREATE INDEX "WDIDX_Inv_attributinvImmo_Codeinvname" ON "Inv_attributinvImmo" ("Codeinv","name");
CREATE INDEX "WDIDX_Inv_attributinvImmo_Codeinvcodeatribut" ON "Inv_attributinvImmo" ("Codeinv","codeatribut");

-- Création de la table Inv_equipe
CREATE TABLE "Inv_equipe"("codeEquipe" VARCHAR(50)  UNIQUE,"NoEquipe" VARCHAR(50),"chefEquipe" VARCHAR(50),"Codeinv" VARCHAR(50),"UserCreation" VARCHAR(20),"DateCreation" TIMESTAMP );
CREATE INDEX "WDIDX_Inv_equipe_NoEquipe" ON "Inv_equipe" ("NoEquipe");
CREATE INDEX "WDIDX_Inv_equipe_chefEquipe" ON "Inv_equipe" ("chefEquipe");
CREATE INDEX "WDIDX_Inv_equipe_Codeinv" ON "Inv_equipe" ("Codeinv");
CREATE INDEX "WDIDX_Inv_equipe_CodeinvcodeEquipe" ON "Inv_equipe" ("Codeinv","codeEquipe");

-- Création de la table Inv_EquipeEmpl
CREATE TABLE "Inv_EquipeInventaire"("CodeEmplacement" VARCHAR(50),"codeEquipe" VARCHAR(50),"codemission" VARCHAR(50),"Codeinv" VARCHAR(50) );
CREATE INDEX "WDIDX_Inv_EquipeEmpl_CodeEmplacement" ON "Inv_EquipeInventaire" ("CodeEmplacement");
CREATE INDEX "WDIDX_Inv_EquipeEmpl_codeEquipe" ON "Inv_EquipeInventaire" ("codeEquipe");
CREATE INDEX "WDIDX_Inv_EquipeEmpl_codemission" ON "Inv_EquipeInventaire" ("codemission");
CREATE INDEX "WDIDX_Inv_EquipeEmpl_Codeinv" ON "Inv_EquipeInventaire" ("Codeinv");
CREATE INDEX "WDIDX_Inv_EquipeEmpl_CodeinvcodemissioncodeEquipeCodeEmplacement" ON "Inv_EquipeInventaire" ("Codeinv","codemission","codeEquipe","CodeEmplacement");
CREATE INDEX "WDIDX_Inv_EquipeEmpl_CodeinvcodeEquipecodemission" ON "Inv_EquipeInventaire" ("Codeinv","codeEquipe","codemission");
CREATE INDEX "WDIDX_Inv_EquipeEmpl_CodeinvCodeEmplacement" ON "Inv_EquipeInventaire" ("Codeinv","CodeEmplacement");

-- Création de la table inv_immo
CREATE TABLE "inv_immo"("CodeInventaire" VARCHAR(50)  NOT NULL  UNIQUE,"DateCreation" TIMESTAMP,"EtatInventaire" INTEGER,"DateCloture" TIMESTAMP,"UserCloture" VARCHAR(20),"UserCreation" VARCHAR(20),"Description" VARCHAR(60),"verif_art_inv" BIT,"affich_art_inv" BIT,"nbrAtt" INTEGER,"multi_eq_mis" BIT );
CREATE INDEX "WDIDX_inv_immo_DateCreation" ON "inv_immo" ("DateCreation");
CREATE INDEX "WDIDX_inv_immo_EtatInventaire" ON "inv_immo" ("EtatInventaire");
CREATE INDEX "WDIDX_inv_immo_DateCloture" ON "inv_immo" ("DateCloture");

-- Création de la table inv_mission
CREATE TABLE "inv_mission"("codemission" VARCHAR(50),"etatmission" VARCHAR(50),"DateCree" TIMESTAMP,"UserCreation" VARCHAR(20),"DateMaj" TIMESTAMP,"usermaj" VARCHAR(20),"Description" VARCHAR(60),"Codeinv" VARCHAR(50) );
CREATE INDEX "WDIDX_inv_mission_codemission" ON "inv_mission" ("codemission");
CREATE INDEX "WDIDX_inv_mission_Codeinv" ON "inv_mission" ("Codeinv");
CREATE INDEX "WDIDX_inv_mission_Codeinvcodemission" ON "inv_mission" ("Codeinv","codemission");

-- Création de la table Inv_Mombreequipe
CREATE TABLE "Inv_eMombreequipe"("CodeUtil" VARCHAR(20),"codeEquipe" VARCHAR(50),"Codeinv" VARCHAR(50),"NOM" VARCHAR(40) );
CREATE INDEX "WDIDX_Inv_Mombreequipe_CodeUtil" ON "Inv_eMombreequipe" ("CodeUtil");
CREATE INDEX "WDIDX_Inv_Mombreequipe_codeEquipe" ON "Inv_eMombreequipe" ("codeEquipe");
CREATE INDEX "WDIDX_Inv_Mombreequipe_Codeinv" ON "Inv_eMombreequipe" ("Codeinv");
CREATE INDEX "WDIDX_Inv_Mombreequipe_CodeinvcodeEquipeCodeUtil" ON "Inv_eMombreequipe" ("Codeinv","codeEquipe","CodeUtil");

-- Création de la table InvImmoAttributValr
CREATE TABLE "InvImmoAttributValr"("codeInv" VARCHAR(50),"codeImmo" VARCHAR(50),"codeAttrib" VARCHAR(50),"valr" VARCHAR(50),"issync" SMALLINT );
CREATE INDEX "WDIDX_InvImmoAttributValr_codeInv" ON "InvImmoAttributValr" ("codeInv");
CREATE INDEX "WDIDX_InvImmoAttributValr_codeImmo" ON "InvImmoAttributValr" ("codeImmo");
CREATE INDEX "WDIDX_InvImmoAttributValr_codeAttrib" ON "InvImmoAttributValr" ("codeAttrib");
CREATE INDEX "WDIDX_InvImmoAttributValr_codeInvcodeImmo" ON "InvImmoAttributValr" ("codeInv","codeImmo");
CREATE INDEX "WDIDX_InvImmoAttributValr_codeInvcodeImmocodeAttrib" ON "InvImmoAttributValr" ("codeInv","codeImmo","codeAttrib");

-- Création de la table param
CREATE TABLE "param"("adress" VARCHAR(50)  UNIQUE,"mode" VARCHAR(10),"projet" VARCHAR(50) );

-- Création de la table Proprietaire
CREATE TABLE "Proprietaire"("CodeProprietaire" VARCHAR(50)  UNIQUE,"Libelle" VARCHAR(50),"Addresse" VARCHAR(150) );

-- Création de la table SouSEtat
CREATE TABLE "SouSEtat"("sousEtat" SMALLINT  NOT NULL,"LibelleEtat" VARCHAR(50),"TypeEtat" VARCHAR(50),"DescriptionEtat" VARCHAR(200),"coler" VARCHAR(50) );
CREATE INDEX "WDIDX_SouSEtat_sousEtat" ON "SouSEtat" ("sousEtat");
CREATE INDEX "WDIDX_SouSEtat_LibelleEtat" ON "SouSEtat" ("LibelleEtat");

-- Création de la table Utilisateur
CREATE TABLE "dbo"."Utilisateur"("CodeUtil" VARCHAR(20)  UNIQUE,"NomPrenomUtil" VARCHAR(50),"MotPasse" VARCHAR(50),"login" VARCHAR(50) );
CREATE INDEX "WDIDX_Utilisateur_login" ON "dbo"."Utilisateur" ("login");

-- Création de la table valeursAttribut
CREATE TABLE "valeursAttribut"("codeAttribut" VARCHAR(50),"Valeur" VARCHAR(50),"isdefault" BIT );
CREATE INDEX "WDIDX_valeursAttribut_codeAttribut" ON "valeursAttribut" ("codeAttribut");
CREATE INDEX "WDIDX_valeursAttribut_Valeur" ON "valeursAttribut" ("Valeur");
CREATE INDEX "WDIDX_valeursAttribut_codeAttributValeur" ON "valeursAttribut" ("codeAttribut","Valeur");
