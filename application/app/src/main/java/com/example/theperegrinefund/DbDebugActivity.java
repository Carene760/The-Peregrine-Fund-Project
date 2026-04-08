package com.example.theperegrinefund;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DbDebugActivity extends AppCompatActivity {

    private TextView dbTextView;
    private MyDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_debug);

        dbTextView = findViewById(R.id.dbTextView);
        dbHelper = new MyDatabaseHelper(this);

        afficherTablesDepuisDB();
    }

    private void afficherTablesDepuisDB() {
        StringBuilder sb = new StringBuilder();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        sb.append("=== Table MESSAGE ===\n");
        Cursor cursorMessage = db.rawQuery("SELECT * FROM Message", null);
        if (cursorMessage.moveToFirst()) {
            do {
                int idMessage = cursorMessage.getInt(cursorMessage.getColumnIndexOrThrow("Id_Message"));
                String dateComm = cursorMessage.getString(cursorMessage.getColumnIndexOrThrow("Date_Commencement"));
                String dateSignal = cursorMessage.getString(cursorMessage.getColumnIndexOrThrow("Date_signalement"));
                String pointRepere = cursorMessage.getString(cursorMessage.getColumnIndexOrThrow("PointRepere"));
                double surface = cursorMessage.getDouble(cursorMessage.getColumnIndexOrThrow("Surface_Approximative"));
                String description = cursorMessage.getString(cursorMessage.getColumnIndexOrThrow("Description"));
                String direction = cursorMessage.getString(cursorMessage.getColumnIndexOrThrow("Direction"));
                int renfort = cursorMessage.getInt(cursorMessage.getColumnIndexOrThrow("renfort")); // 0 ou 1
                double longitude = cursorMessage.getDouble(cursorMessage.getColumnIndexOrThrow("longitude"));
                double latitude = cursorMessage.getDouble(cursorMessage.getColumnIndexOrThrow("latitude"));
                int idIntervention = cursorMessage.getInt(cursorMessage.getColumnIndexOrThrow("Id_Intervention"));
                int idUserApp = cursorMessage.getInt(cursorMessage.getColumnIndexOrThrow("IdUserApp"));

                sb.append(idMessage).append(" | ")
                .append(dateComm).append(" | ")
                .append(dateSignal).append(" | ")
                .append(pointRepere).append(" | ")
                .append(surface).append(" | ")
                .append(description).append(" | ")
                .append(direction).append(" | ")
                .append(renfort == 1 ? "Oui" : "Non").append(" | ")
                .append(longitude).append(" | ")
                .append(latitude).append(" | ")
                .append("Intervention ").append(idIntervention).append(" | ")
                .append("UserApp ").append(idUserApp)
                .append("\n");

            } while (cursorMessage.moveToNext());
        }
        cursorMessage.close();

        // --- Table HISTORIQUE MESSAGE STATUS ---
        sb.append("\n=== Table HISTORIQUE MESSAGE STATUS ===\n");
        Cursor cursorHist = db.rawQuery("SELECT * FROM historique_message_status", null);
        if (cursorHist.moveToFirst()) {
            do {
                int idHist = cursorHist.getInt(cursorHist.getColumnIndexOrThrow("Id_historique"));
                String dateChangement = cursorHist.getString(cursorHist.getColumnIndexOrThrow("date_changement"));
                int idStatusMessage = cursorHist.getInt(cursorHist.getColumnIndexOrThrow("Id_status_message"));
                int idMessage = cursorHist.getInt(cursorHist.getColumnIndexOrThrow("Id_Message"));

                sb.append(idHist).append(" | ")
                .append(dateChangement).append(" | ")
                .append("Status ").append(idStatusMessage).append(" | ")
                .append("Message ").append(idMessage)
                .append("\n");

            } while (cursorHist.moveToNext());
        }
        cursorHist.close();

        // --- Table STATUS_MESSAGE ---
        sb.append("=== Table STATUS_MESSAGE ===\n");
        Cursor cursorStatus = db.rawQuery("SELECT * FROM status_message", null);
        if (cursorStatus.moveToFirst()) {
            do {
                int idStatus = cursorStatus.getInt(cursorStatus.getColumnIndexOrThrow("Id_status_message"));
                String status = cursorStatus.getString(cursorStatus.getColumnIndexOrThrow("status"));
                sb.append(idStatus).append(" | ").append(status).append("\n");
            } while (cursorStatus.moveToNext());
        }
        cursorStatus.close();

        // --- Table INTERVENTION ---
        sb.append("\n=== Table INTERVENTION ===\n");
        Cursor cursorIntervention = db.rawQuery("SELECT * FROM Intervention", null);
        if (cursorIntervention.moveToFirst()) {
            do {
                int idInterv = cursorIntervention.getInt(cursorIntervention.getColumnIndexOrThrow("Id_Intervention"));
                String intervention = cursorIntervention.getString(cursorIntervention.getColumnIndexOrThrow("intervention"));
                sb.append(idInterv).append(" | ").append(intervention).append("\n");
            } while (cursorIntervention.moveToNext());
        }
        cursorIntervention.close();

        dbTextView.setText(sb.toString());
        db.close();
    }

}
