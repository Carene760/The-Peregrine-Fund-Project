package com.example.theperegrinefund;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MaBase.db";
    private static final int DATABASE_VERSION = 2;

    // ---------- TABLE USERS ----------
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "idUserApp";
    public static final String COLUMN_USER_NAME = "name";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_NAME + " TEXT NOT NULL);";

    public static final String TABLE_MESSAGE = "message";
    public static final String COLUMN_MESSAGE_ID = "id_message";
    public static final String COLUMN_DATE_COMMENCEMENT = "date_commencement";
    public static final String COLUMN_DATE_SIGNAL = "date_signalement";
    public static final String COLUMN_POINT_REPERE = "pointrepere";
    public static final String COLUMN_SURFACE = "surface_approximative";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DIRECTION = "direction";
    public static final String COLUMN_RENFORT = "renfort";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_INTERVENTION_FK = "id_intervention";
    public static final String COLUMN_USER_FK = "iduserapp";
    public static final String COLUMN_PHONE_NUMBER = "phoneNumber";



    private static final String CREATE_TABLE_MESSAGE =
            "CREATE TABLE " + TABLE_MESSAGE + " (" +
                    COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY , " +
                    COLUMN_DATE_COMMENCEMENT + " TEXT, " +
                    COLUMN_DATE_SIGNAL + " TEXT, " +
                    COLUMN_POINT_REPERE + " TEXT, " +
                    COLUMN_SURFACE + " REAL, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_DIRECTION + " TEXT, " +
                    COLUMN_RENFORT + " INTEGER, " + // SQLite n'a pas de type booléen, on utilise INTEGER (0=false, 1=true)
                    COLUMN_LONGITUDE + " REAL, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_INTERVENTION_FK + " INTEGER, " +
                    COLUMN_USER_FK + " INTEGER, " +
                    COLUMN_PHONE_NUMBER + " TEXT, " + 
                    "FOREIGN KEY(" + COLUMN_USER_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "));";
  

  // ---------- TABLE INTERVENTION ----------
public static final String TABLE_INTERVENTION = "intervention";
public static final String COLUMN_INTERVENTION_ID = "idIntervention";
public static final String COLUMN_INTERVENTION_NAME = "intervention";

private static final String CREATE_TABLE_INTERVENTION =
        "CREATE TABLE " + TABLE_INTERVENTION + " (" +
                COLUMN_INTERVENTION_ID + " INTEGER PRIMARY KEY, " + // Retirer AUTOINCREMENT
                COLUMN_INTERVENTION_NAME + " TEXT NOT NULL);";

  // ---------- TABLE STATUS MESSAGE ----------
public static final String TABLE_STATUS = "status_message";
public static final String COLUMN_STATUS_ID = "idStatusMessage";
public static final String COLUMN_STATUS_NAME = "status";

private static final String CREATE_TABLE_STATUS =
        "CREATE TABLE " + TABLE_STATUS + " (" +
                COLUMN_STATUS_ID + " INTEGER PRIMARY KEY, " + // Retirer AUTOINCREMENT
                COLUMN_STATUS_NAME + " TEXT NOT NULL UNIQUE);";
  // ---------- TABLE HISTORIQUE MESSAGE STATUS ----------
public static final String TABLE_HISTORIQUE = "historique_message_status";
public static final String COLUMN_HISTORIQUE_ID = "id_historique";
public static final String COLUMN_HISTORIQUE_DATE = "date_changement";
public static final String COLUMN_HISTORIQUE_STATUS_FK = "id_status_message";
public static final String COLUMN_HISTORIQUE_MESSAGE_FK = "id_message";

    private static final String CREATE_TABLE_HISTORIQUE =
            "CREATE TABLE " + TABLE_HISTORIQUE + " (" +
                    COLUMN_HISTORIQUE_ID + " INTEGER PRIMARY KEY , " +
                    COLUMN_HISTORIQUE_DATE + " TEXT, " +
                    COLUMN_HISTORIQUE_STATUS_FK + " INTEGER NOT NULL, " +
                    COLUMN_HISTORIQUE_MESSAGE_FK + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_HISTORIQUE_STATUS_FK + ") REFERENCES " + TABLE_STATUS + "(" + COLUMN_STATUS_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_HISTORIQUE_MESSAGE_FK + ") REFERENCES " + TABLE_MESSAGE + "(" + COLUMN_MESSAGE_ID + "));";

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Activer la gestion des clés étrangères
        db.execSQL("PRAGMA foreign_keys=ON;");
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_MESSAGE);
        db.execSQL(CREATE_TABLE_STATUS);
        db.execSQL(CREATE_TABLE_HISTORIQUE);
        db.execSQL(CREATE_TABLE_INTERVENTION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_INTERVENTION);
          db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORIQUE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUS);
        onCreate(db);
    }
    // Retourne l'id d'une intervention à partir de son nom
    public int getIdIntervention(String interventionName) {
        int id = -1; // valeur par défaut si non trouvé
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                "SELECT " + COLUMN_INTERVENTION_ID + " FROM " + TABLE_INTERVENTION + " WHERE " + COLUMN_INTERVENTION_NAME + " = ?",
                new String[]{interventionName}
            );
            if (cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INTERVENTION_ID));
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return id;
    }

    // Retourne l'id d'un status à partir du nom
    public int getIdStatusMessage(String status) {
        int id = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                "SELECT " + COLUMN_STATUS_ID + " FROM " + TABLE_STATUS + " WHERE " + COLUMN_STATUS_NAME + " = ?",
                new String[]{status}
            );
            if (cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATUS_ID));
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return id;
    }


}
