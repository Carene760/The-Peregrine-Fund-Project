package com.example.theperegrinefund;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.theperegrinefund.security.ConfigLoader;
import com.example.theperegrinefund.security.CryptoUtils;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    String format = bundle.getString("format");
                    SmsMessage sms;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                    } else {
                        sms = SmsMessage.createFromPdu((byte[]) pdu);
                    }
                    String sender = sms.getDisplayOriginatingAddress();
                    String body = sms.getMessageBody();

                    try {
                        String cle = ConfigLoader.getSecretKey(context);
                        CryptoUtils crypto = new CryptoUtils("");
                        String dechiffre = crypto.dechiffrer(cle, body);

                        Log.d("SmsReceiver", "SMS reçu de " + sender + " : " + dechiffre);

                        // Envoie du SMS déchiffré vers LoginActivity
                        Intent i = new Intent("SMS_RECU_APP");
                        i.setPackage(context.getPackageName());
                        i.putExtra("message", dechiffre);
                        i.putExtra("sender", sender);
                        context.sendBroadcast(i);

                    } catch (Exception e) {
                        Log.e("SmsReceiver", "Erreur de déchiffrement: " + e.getMessage());
                    }
                }
            }
        }
    }
}
