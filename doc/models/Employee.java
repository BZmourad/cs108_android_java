package com.csl.cs108ademoapp.database.models;

import java.util.Date;

public class Employee extends Model {
    public String CodeFunction;
    public String CodeGroup;
    public String CodeEmployee;
    public Date DateCre;
    public String NomPrenom;
    public String EmailWork;
    public Double NumPerso;
    public Double NumTravail;
    public String EmailPerso;
    public String Addresse;
    public String Notes;
    public Integer ETAT;
    public String UserCreation;
    public String UseMaj;
    public Date DateMaj;

    public Employee() {}

    public Employee(String codeEmployee, String nomPrenom) {
        this.CodeEmployee = codeEmployee;
        this.NomPrenom = nomPrenom;
    }
}