package com.csl.cs108immo.db.models;

public class ArbreEmp {
    private int id;
    private String code;
    private String libelle;
    private String idparent;
    private String path;
    private String n1;
    private String n2;
    private String n3;
    private String n4;
    private String n5;
    private String n6;
    private String n7;
    private String n8;
    private String n9;
    private String n10;

    // Constructeurs
    public ArbreEmp() {}

    public ArbreEmp(String code, String libelle, String idparent, String path) {
        this.code = code;
        this.libelle = libelle;
        this.idparent = idparent;
        this.path = path;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getIdparent() { return idparent; }
    public void setIdparent(String idparent) { this.idparent = idparent; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getN1() { return n1; }
    public void setN1(String n1) { this.n1 = n1; }

    public String getN2() { return n2; }
    public void setN2(String n2) { this.n2 = n2; }

    public String getN3() { return n3; }
    public void setN3(String n3) { this.n3 = n3; }

    public String getN4() { return n4; }
    public void setN4(String n4) { this.n4 = n4; }

    public String getN5() { return n5; }
    public void setN5(String n5) { this.n5 = n5; }

    public String getN6() { return n6; }
    public void setN6(String n6) { this.n6 = n6; }

    public String getN7() { return n7; }
    public void setN7(String n7) { this.n7 = n7; }

    public String getN8() { return n8; }
    public void setN8(String n8) { this.n8 = n8; }

    public String getN9() { return n9; }
    public void setN9(String n9) { this.n9 = n9; }

    public String getN10() { return n10; }
    public void setN10(String n10) { this.n10 = n10; }
}
