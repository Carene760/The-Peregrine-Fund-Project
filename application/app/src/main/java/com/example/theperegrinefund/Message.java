package com.example.theperegrinefund;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Locale;   
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.example.theperegrinefund.security.CryptoUtils;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class Message {

    private int idMessage;
    private String phoneNumber;
    private LocalDateTime dateCommencement;
    private LocalDateTime dateSignalement;
    private LocalDateTime dateEnvoi;
    private String pointRepere;
    private double surfaceApproximative;
    private String description;
    private String direction;
    private boolean renfort;
    private double longitude;
    private double latitude;
    private int idIntervention;

    // Champ utilisé côté mobile pour la synchro
    private int idUserApp;
    private Integer idEvenement;
    private Evenement evenement;

   public Message( LocalDateTime dateCommencement, LocalDateTime dateSignalement,int idIntervention,boolean renfort,String direction,
                double surfaceApproximative,String pointRepere,String description, String phoneNumber,
                 double longitude, double latitude, int idUserApp) {
    
        this.phoneNumber = phoneNumber;
        this.dateCommencement = dateCommencement;
        this.dateSignalement = dateSignalement;
        this.pointRepere = pointRepere;
        this.surfaceApproximative = surfaceApproximative;
        this.description = description;
        this.direction = direction;
        this.renfort = renfort;
        this.longitude = longitude;
        this.latitude = latitude;
        this.idIntervention = idIntervention;
        this.idUserApp = idUserApp;
    }
    public Message() {
        // constructeur vide nécessaire pour MessageDao
    }


    // --- Getters et setters ---
    public int getIdMessage() { return idMessage; }
    public void setIdMessage(int idMessage) { this.idMessage = idMessage; }

    public LocalDateTime getDateCommencement() { return dateCommencement; }
    public void setDateCommencement(LocalDateTime dateCommencement) { this.dateCommencement = dateCommencement; }

    public LocalDateTime getDateSignalement() { return dateSignalement; }
    public void setDateSignalement(LocalDateTime dateSignalement) { this.dateSignalement = dateSignalement; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public String getPointRepere() { return pointRepere; }
    public void setPointRepere(String pointRepere) { this.pointRepere = pointRepere; }

    public double getSurfaceApproximative() { return surfaceApproximative; }
    public void setSurfaceApproximative(double surfaceApproximative) { this.surfaceApproximative = surfaceApproximative; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public boolean isRenfort() { return renfort; }
    public void setRenfort(boolean renfort) { this.renfort = renfort; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public boolean isValid() {
        return phoneNumber != null && !phoneNumber.isEmpty();
    }

    public int getIdIntervention() { return idIntervention; }
    public void setIdIntervention(int idIntervention) { this.idIntervention = idIntervention; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public int getIdUserApp() { return idUserApp; }
    public void setIdUserApp(int idUserApp) { this.idUserApp = idUserApp; }

    public Integer getIdEvenement() { return idEvenement; }
    public void setIdEvenement(Integer idEvenement) { this.idEvenement = idEvenement; }

    public Evenement getEvenement() { return evenement; }
    public void setEvenement(Evenement evenement) { this.evenement = evenement; }

    // Délègue à CryptoUtils (AES/GCM/NoPadding) plutôt que de dupliquer la
    // logique de chiffrement ici. Cette classe utilisait auparavant sa
    // propre copie de Cipher.getInstance("AES") (ECB, non sécurisé) - voir
    // CryptoUtils pour le détail du correctif et le format de fil (IV
    // préfixé au ciphertext).
    public String chiffrer(String cleSecrete, String mess) throws Exception {
        return CryptoUtils.encrypt(cleSecrete, mess);
    }

    public String dechiffrer(String cleSecrete, String texteChiffre) throws Exception {
        return CryptoUtils.decrypt(cleSecrete, texteChiffre);
    }
   public int save(Context context) {
    MyDatabaseHelper dbHelper = new MyDatabaseHelper(context);
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    ContentValues values = new ContentValues();

    // Formatter pour LocalDateTime
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    // Conversion des LocalDateTime en String
    if (dateCommencement != null) {
        values.put(MyDatabaseHelper.COLUMN_DATE_COMMENCEMENT, dateCommencement.format(formatter));
    }
    if (dateSignalement != null) {
        values.put(MyDatabaseHelper.COLUMN_DATE_SIGNAL, dateSignalement.format(formatter));
    }
    if (dateEnvoi == null) {
        dateEnvoi = LocalDateTime.now();
    }
    values.put(MyDatabaseHelper.COLUMN_DATE_ENVOI, dateEnvoi.format(formatter));

    values.put(MyDatabaseHelper.COLUMN_PHONE_NUMBER, phoneNumber);
    values.put(MyDatabaseHelper.COLUMN_POINT_REPERE, pointRepere);
    values.put(MyDatabaseHelper.COLUMN_SURFACE, surfaceApproximative);
    values.put(MyDatabaseHelper.COLUMN_DESCRIPTION, description);
    values.put(MyDatabaseHelper.COLUMN_DIRECTION, direction);

    // Boolean en INTEGER
    values.put(MyDatabaseHelper.COLUMN_RENFORT, renfort ? 1 : 0);

    values.put(MyDatabaseHelper.COLUMN_LONGITUDE, longitude);
    values.put(MyDatabaseHelper.COLUMN_LATITUDE, latitude);

    values.put(MyDatabaseHelper.COLUMN_INTERVENTION_FK, idIntervention);
    values.put(MyDatabaseHelper.COLUMN_USER_FK, idUserApp);
    values.put(MyDatabaseHelper.COLUMN_EVENEMENT_FK, idEvenement);

    // Insert et récupération de l'ID inséré
    int newId = (int) db.insert(MyDatabaseHelper.TABLE_MESSAGE, null, values);

    db.close();
    return newId; // retourne l'ID inséré ou -1 en cas d'erreur
}

  
}


    