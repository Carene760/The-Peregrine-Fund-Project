package com.example.theperegrinefund;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.example.theperegrinefund.security.ConfigLoader;
import com.example.theperegrinefund.security.CredentialUtil;
import com.example.theperegrinefund.security.CryptoUtils;

public class LoginActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_SEND_SMS = 1;
    private static final int PERMISSION_REQUEST_RECEIVE_SMS = 2;

    private EditText editName, editPassword;
    private Button btnSignIn;
    private TextView textViewSms;
    private SmsSender smsSender;
    private String lastChiffre;
    private boolean waitingForResponse = false;
   

    private BroadcastReceiver smsReceiver; // pour écouter les réponses

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editName = findViewById(R.id.edit_name);
        editPassword = findViewById(R.id.edit_password);
        btnSignIn = findViewById(R.id.btn_sign_in);
        textViewSms = findViewById(R.id.text_view_sms);

        smsSender = new SmsSender(this);

        // Vérifie la permission RECEIVE_SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS}, PERMISSION_REQUEST_RECEIVE_SMS);
        }

        // Bouton connexion
        btnSignIn.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            try {
                String cle = ConfigLoader.getSecretKey(this);
                String combined = CredentialUtil.combineNamePassword(name, password);

                CryptoUtils crypto = new CryptoUtils(combined);
                lastChiffre = crypto.chiffrer(cle);

                waitingForResponse = true;
                textViewSms.setText("En attente de réponse…");

                if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(LoginActivity.this,
                            new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
                } else {
                    sendMessage();
                }

            } catch (Exception e) {
                Toast.makeText(this, "Erreur chiffrement: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Préparer le receiver pour capter les SMS
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("SMS_RECU_APP".equals(intent.getAction())) {
                    String corps = intent.getStringExtra("message");
                    if (waitingForResponse && corps != null) {
                        waitingForResponse = false;

                        if (corps.contains("ID")) {

                            Toast.makeText(LoginActivity.this, "Authentification réussie!", Toast.LENGTH_SHORT).show();
                            int userId = extraireIdUser(corps);
                            AppData.setCurrentUserId(userId);

                             Intent intentDashboard = new Intent(LoginActivity.this, DashboardActivity.class);
                                startActivity(intentDashboard);

                                // (Optionnel) : fermer LoginActivity pour que l'utilisateur ne revienne pas dessus
                                finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Échec de l'authentification", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        };
    }
    private int extraireIdUser(String sms) {
        if (sms == null) return -1;

        Pattern pattern = Pattern.compile("ID:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(sms);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1)); // retourne l'ID en int
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return -1; // rien trouvé ou erreur
    }

    private void sendMessage() {
        if (lastChiffre == null || lastChiffre.isEmpty()) {
            Toast.makeText(this, "Aucun message chiffré à envoyer", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            smsSender.sendUser(lastChiffre);
            Toast.makeText(this, "Message envoyé, en attente de réponse…", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur envoi SMS : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(smsReceiver, new IntentFilter("SMS_RECU_APP"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(smsReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendMessage();
            } else {
                Toast.makeText(this, "Permission SMS refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
