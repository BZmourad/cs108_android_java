package com.csl.cs108ademoapp.database.models;

public class ArbreEmp extends Model {
    public String code;
    public String Libelle;
    public String Idparent;
    public String path;
    public String n1;
    public String n2;
    public String n3;
    public String n4;
    public String n5;
    public String n6;
    public String n7;
    public String n8;
    public String n9;
    public String n10;

    public ArbreEmp() {}

    public ArbreEmp(String code, String libelle, String idparent, String path) {
        this.code = code;
        this.Libelle = libelle;
        this.Idparent = idparent;
        this.path = path;
    }
}