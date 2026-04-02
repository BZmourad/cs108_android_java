package com.csl.cs108immo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.csl.cs108immo.db.models.User;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserDatabase.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_CODE = "code";
    private static final String COLUMN_NAME = "name";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_CODE + " TEXT,"
            + COLUMN_USERNAME + " TEXT,"
            + COLUMN_PASSWORD + " TEXT,"
            + COLUMN_NAME + " TEXT"
            + ")";

    private static final String CREATE_TABLE_ArbreEmp = "CREATE TABLE ArbreEmp ("+
            "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "code NVARCHAR(50)  NOT NULL  UNIQUE ,"+
            "Libelle NVARCHAR(50) ,"+
            "Idparent NVARCHAR(20) ,"+
            "path NVARCHAR(200) ,"+
            "n1 VARCHAR(50) ,"+
            "n2 VARCHAR(50) ,"+
            "n3 VARCHAR(50) ,"+
            "n4 VARCHAR(50) ,"+
            "n5 VARCHAR(50) ,"+
            "n6 VARCHAR(50) ,"+
            "n7 VARCHAR(50) ,"+
            "n8 VARCHAR(50) ,"+
            "n9 VARCHAR(50) ,"+
            "n10 VARCHAR(50) )";

    private static final String CREATE_TABLE_inv_immo = "CREATE TABLE inv_immo ("+
            "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "CodeInventaire VARCHAR(50)  NOT NULL  UNIQUE ,"+
            "DateCreation TIMESTAMP ,"+
            "EtatInventaire INTEGER ,"+
            "DateCloture TIMESTAMP ,"+
            "UserCloture VARCHAR(20) ,"+
            "UserCreation VARCHAR(20) ,"+
            "Description VARCHAR(60) ,"+
            "verif_art_inv BIT ,"+
            "affich_art_inv BIT ,"+
            "nbrAtt INTEGER ,"+
            "multi_eq_mis BIT )";

    private static final String CREATE_TABLE_inv_mission = "CREATE TABLE inv_mission ("+
            "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "codemission VARCHAR(50) ,"+
            "etatmission VARCHAR(50) ,"+
            "DateCree TIMESTAMP ,"+
            "UserCreation VARCHAR(20) ,"+
            "DateMaj TIMESTAMP ,"+
            "usermaj VARCHAR(20) ,"+
            "Description VARCHAR(60) ,"+
            "Codeinv VARCHAR(50) )";

    private static final String CREATE_TABLE_Inv_eMombreequipe = "CREATE TABLE Inv_eMombreequipe ("+
            "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "CodeUtil VARCHAR(20) ,"+
            "codeEquipe VARCHAR(50) ,"+
            "Codeinv VARCHAR(50) ,"+
            "NOM VARCHAR(40) )";

    private static final String CREATE_TABLE_param = "CREATE TABLE param ("+
            "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "adress VARCHAR(50)  UNIQUE ,"+
            "mode VARCHAR(10) ,"+
            "projet VARCHAR(50) )";

    private static final String CREATE_TABLE_Utilisateur = "CREATE TABLE Utilisateur ("+
            "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "CodeUtil VARCHAR(20)  UNIQUE ,"+
            "NomPrenomUtil VARCHAR(50) ,"+
            "MotPasse VARCHAR(50) ,"+
            "login VARCHAR(50) )";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ArbreEmp);
        db.execSQL(CREATE_TABLE_inv_immo);
        db.execSQL(CREATE_TABLE_inv_mission);
        db.execSQL(CREATE_TABLE_Inv_eMombreequipe);
        db.execSQL(CREATE_TABLE_param);
        db.execSQL(CREATE_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public void addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ?" + " AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count > 0;
    }

    // Add method to get user details
    public User getUserDetails(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_CODE, COLUMN_NAME, COLUMN_USERNAME, COLUMN_PASSWORD},
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setCode(cursor.getString(0));
            user.setName(cursor.getString(1));
            user.setUsername(cursor.getString(2));
            user.setPassword(cursor.getString(3));
        }
        cursor.close();
        db.close();
        return user;
    }

    // Add method to clear and repopulate users table
    public void syncUsers(JSONArray usersArray) throws JSONException {
        SQLiteDatabase db = this.getWritableDatabase();

        // Clear existing users
        db.delete(TABLE_USERS, null, null);

        // Add all users from the array
        for (int i = 0; i < usersArray.length(); i++) {
            JSONObject user = usersArray.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(COLUMN_CODE, user.getString("sCode"));
            values.put(COLUMN_USERNAME, user.getString("sLogin"));
            values.put(COLUMN_PASSWORD, user.getString("sPass"));
            values.put(COLUMN_NAME, user.getString("sNom"));
            db.insert(TABLE_USERS, null, values);
        }

        db.close();
    }
}