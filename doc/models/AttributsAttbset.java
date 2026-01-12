package com.csl.cs108ademoapp.database.models;

public class AttributsAttbset extends Model {
    public String codeAttribus;
    public String codeAttribusSet;

    public AttributsAttbset() {}

    public AttributsAttbset(String codeAttribus, String codeAttribusSet) {
        this.codeAttribus = codeAttribus;
        this.codeAttribusSet = codeAttribusSet;
    }
}