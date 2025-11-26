package com.csl.cs108ademoapp.database.models;

public class SousEtat extends Model {
    public Integer sousEtat;
    public String LibelleEtat;
    public String TypeEtat;
    public String DescriptionEtat;
    public String coler;

    public SousEtat() {}

    public SousEtat(Integer sousEtat, String libelleEtat) {
        this.sousEtat = sousEtat;
        this.LibelleEtat = libelleEtat;
    }
}