package com.example.theperegrinefund;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import android.graphics.Color;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.Collections;
import android.database.ContentObserver;
import android.os.Handler;
import android.net.Uri;
import android.database.Cursor;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import com.example.theperegrinefund.security.ConfigLoader;
import com.example.theperegrinefund.dao.MessageDao;
import com.example.theperegrinefund.Message;
import java.util.List;
import android.content.Intent;

public class StatActivity extends AppCompatActivity {

    private ImageView backIcon;
    private MapView map;
    private PieChart pieChart;
    private BarChart barChart;
    private String FIXED_NUMBER;  
    private String SECRET_KEY;
    private String dernierSms = "";
    private static final int PERMISSION_REQUEST_READ_SMS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);

        try {
            FIXED_NUMBER = ConfigLoader.getFixedNumber(this);
            SECRET_KEY = ConfigLoader.getSecretKey(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Vérifier la permission READ_SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS}, PERMISSION_REQUEST_READ_SMS);
        } else {
            initialiserSmsObserver();
        }
        // Toast.makeText(StatActivity.this, dernierSms, Toast.LENGTH_LONG).show();

        // loadDataFromString("-18.8792/47.5079~30/35/20/15~site1?10/site2?37/site3?15/site4?5");
        loadDataFromString(dernierSms);

        // Bouton "Back"
        backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ImageView logoutIcon = findViewById(R.id.logout_icon);
        logoutIcon.setOnClickListener(v -> logout());
    }

     private void logout() {
        Intent intent = new Intent(StatActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    } 
    private void initialiserSmsObserver() {
        // Enregistrer un ContentObserver pour surveiller la boîte de réception SMS
        getContentResolver().registerContentObserver(
                Uri.parse("content://sms/"),
                true,
                new SmsObserver(new Handler())
        );

        // Récupérer le dernier SMS existant au démarrage
        recupererDernierSms();
        // Toast.makeText(StatActivity.this, dernierSms, Toast.LENGTH_LONG).show();

        // Charger les données de test (à adapter selon ton besoin)
        // loadDataFromString("-18.8792/47.5079~30/35/20/15~site1?10/site2?37/site3?15/site4?5");
    }

    // ContentObserver qui détecte les changements dans la boîte SMS
    private class SmsObserver extends ContentObserver {
        public SmsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // Mise à jour automatique du dernier SMS
            recupererDernierSms();
        }
    }

    // Méthode pour récupérer le dernier SMS du numéro ciblé
    private void recupererDernierSms() {
    Uri inboxUri = Uri.parse("content://sms/inbox");
    String selection = "address = ?";
    String[] selectionArgs = { FIXED_NUMBER };
    String tri = "date DESC";

    Cursor cursor = getContentResolver().query(inboxUri, null, selection, selectionArgs, tri);

    if (cursor != null) {
        while (cursor.moveToNext()) {
            String corpss = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            // String corps = "";

            // Message message = new Message(null, null, null, false, null, 0.0, null, null, null, 0.0, 0.0, 0);
            // try {
            //     corps = message.dechiffrer(SECRET_KEY, corpss);
            // } catch (Exception e) {
            //     e.printStackTrace(); // ou Log.e("SmsActivity", "Erreur de déchiffrement", e);
            //     return; // ou gérer le cas d'erreur (par ex. ignorer le SMS corrompu)
            // }
            String corps = "";

            Message message = new Message(null, null, 0, false, null, 0.0, null, null, null, 0.0, 0.0, 0);
            try {
                corps = message.dechiffrer(SECRET_KEY, corpss);
                // tu continues ton traitement ici
            } catch (Exception e) {
                e.printStackTrace(); // ou Log.e("SmsActivity", "Erreur de déchiffrement", e);
                return; // ou gérer le cas d'erreur (par ex. ignorer le SMS corrompu)
            }


            // Vérifie si le message correspond à "string~string~string"
            if (corps.matches("^.+~.+~.+$")) {
                dernierSms = corps;
                Log.d("SmsActivity", "Dernier SMS valide de " + FIXED_NUMBER + ": " + dernierSms);
                break; // on s'arrête dès qu'on trouve le plus récent qui correspond
            }
        }
        cursor.close();
    }
}


    // Getter pour récupérer le dernier SMS depuis d’autres classes ou fragments
    public String getDernierSms() {
        return dernierSms;
    }

    // Résultat de la demande de permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_READ_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée
                initialiserSmsObserver();
            } else {
                // Permission refusée
                Toast.makeText(this, "Permission READ_SMS refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void loadDataFromString(String dataString) {
        try {
            // Découpage en 3 parties
            String[] parts = dataString.split("~");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Le format du string est invalide");
            }

            String coordPart = parts[0];      // "-18.8792/47.5079"
            String piePart = parts[1];        // "30/35/20/15"
            String barPart = parts[2];        // "site1?10/site2?37/site3?15/site4?5"

            // 1️⃣ Carte : centre de la map
            loadOSMDroid(coordPart);

            // 2️⃣ PieChart
            ArrayList<PieEntry> pieEntries = new ArrayList<>();
            loadPieChart(piePart, pieEntries); // pieEntries = ArrayList<PieEntry>

            // 3️⃣ BarChart
            ArrayList<String> barSites = new ArrayList<>();
            ArrayList<BarEntry> barEntries = new ArrayList<>();
            loadBarChart(barPart, barSites, barEntries); // barSites = ArrayList<String>, barEntries = ArrayList<BarEntry>

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadPieChart(String dataString, ArrayList<PieEntry> entries)
    {
        pieChart = findViewById(R.id.pieChart);
        entries.clear();

        String[] labels = {"Verte", "Jaune", "Orange", "Rouge"};

        try {
            String[] parts = dataString.split("/"); // découpe "40/25/20/15"
            for (int i = 0; i < parts.length && i < labels.length; i++) {
                float value = Float.parseFloat(parts[i]);
                entries.add(new PieEntry(value, labels[i]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.GREEN,
                Color.YELLOW,
                Color.rgb(255,165,0),
                Color.RED
        });

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);

        // --- Style du PieChart ---
        pieChart.setData(data);
        pieChart.setUsePercentValues(true); // valeurs en %
        pieChart.setDrawHoleEnabled(true);  // active le donut
        pieChart.setHoleRadius(40f);        // taille du trou (40% du rayon)
        pieChart.setTransparentCircleRadius(45f);

        pieChart.getDescription().setEnabled(false); // enlever la description
        pieChart.setEntryLabelColor(Color.BLACK);

        // --- Légende à droite ---
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);

        // Animation
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
    public void loadBarChart(String dataString, ArrayList<String> sites, ArrayList<BarEntry> entries2)
    {
        barChart = findViewById(R.id.barChart);
        sites.clear();
        entries2.clear();

        try {
            String[] items = dataString.split("/"); // découpe chaque "site?valeur"
            for (int i = 0; i < items.length; i++) {
                String[] parts = items[i].split("\\?"); // découpe site et valeur
                if (parts.length == 2) {
                    String site = parts[0];
                    float value = Float.parseFloat(parts[1]);

                    sites.add(site);
                    entries2.add(new BarEntry(i, value)); // i = position sur l'axe X
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Créer plusieurs BarDataSet pour que chaque site ait sa couleur et son label
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();

        for (int i = 0; i < sites.size(); i++) {
            BarDataSet set = new BarDataSet(
                Collections.singletonList(new BarEntry(i, entries2.get(i).getY())),
                sites.get(i) // ici le label de la légende = nom du site
            );
            set.setColor(ColorTemplate.MATERIAL_COLORS[i % ColorTemplate.MATERIAL_COLORS.length]);
            set.setValueTextColor(Color.BLACK);
            set.setValueTextSize(12f);
            dataSets.add(set);
        }

        BarData barData = new BarData(dataSets);
        barData.setBarWidth(0.5f);
        barChart.setData(barData);

        // Axe X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(sites));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);

        // Axe Y
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);

        // Légende verticale à gauche
        Legend legend2 = barChart.getLegend();
        legend2.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend2.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend2.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend2.setDrawInside(false);
        legend2.setTextSize(12f);

        // Animation et rafraîchissement
        barChart.animateY(1500);
        barChart.invalidate();
    }

   public void loadOSMDroid(String coord) {
        // Configuration OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Initialiser la MapView
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // Contrôleur de la carte
        IMapController mapController = map.getController();
        mapController.setZoom(10.0);

        // Centrer la carte sur la coordonnée passée en paramètre
        try {
            String[] parts = coord.split("/");
            if (parts.length == 2) {
                double latitude = Double.parseDouble(parts[0]);
                double longitude = Double.parseDouble(parts[1]);
                GeoPoint point = new GeoPoint(latitude, longitude);
                mapController.setCenter(point);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Récupération des messages depuis SQLite
        MessageDao dbHelper = new MessageDao(this); // Si Fragment : getContext()
        List<Message> messages = dbHelper.getAllMessages(); // Utilisez getAllMessages() au lieu de getAllMessagesArrayList()

// Ajouter des marqueurs sur la carte
for (Message m : messages) {
    // Vérifier que les coordonnées sont valides avant d'ajouter le marqueur
    if (m.getLatitude() != 0.0 && m.getLongitude() != 0.0) {
        org.osmdroid.views.overlay.Marker marker = new org.osmdroid.views.overlay.Marker(map);
        GeoPoint point = new GeoPoint(m.getLatitude(), m.getLongitude());
        marker.setPosition(point);
        marker.setTitle(m.getDescription());
        marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                        org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);
        
        // Log pour déboguer
        Log.d("MapDebug", "Marqueur ajouté: " + m.getDescription() + 
              " à " + m.getLatitude() + ", " + m.getLongitude());
    } else {
        Log.d("MapDebug", "Coordonnées invalides pour: " + m.getDescription());
    }
}

// Rafraîchir la carte après avoir ajouté tous les marqueurs
map.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume(); // Requis par OSMDroid
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();  // Requis par OSMDroid
    }
}


