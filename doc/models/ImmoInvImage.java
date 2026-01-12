package com.csl.cs108ademoapp.database.models;

public class ImmoInvImage extends Model {
    public String CodeImmo;
    public String CodeDoc;
    public String url;
    public byte[] img;
    public Boolean parDefault;
    public String codeInv;
    public Integer isSny;

    public ImmoInvImage() {}

    public ImmoInvImage(String codeImmo, String codeDoc, String codeInv) {
        this.CodeImmo = codeImmo;
        this.CodeDoc = codeDoc;
        this.codeInv = codeInv;
    }
}