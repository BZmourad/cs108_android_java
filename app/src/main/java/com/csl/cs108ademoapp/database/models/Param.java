package com.csl.cs108ademoapp.database.models;

public class Param extends Model {
    public String adress;
    public String mode;
    public String projet;

    public Param() {}

    public Param(String adress, String mode) {
        this.adress = adress;
        this.mode = mode;
    }
}