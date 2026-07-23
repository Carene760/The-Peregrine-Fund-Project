package com.example.theperegrinefund;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.LinearLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.DatePicker;
import android.widget.TimePicker;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.util.Log;
import com.example.theperegrinefund.AppData;

import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Date;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
// Android de base
import android.location.Location;

// Google Play Services Location
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.Collections;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.location.LocationManager;
import java.util.concurrent.atomic.AtomicBoolean;
import android.net.Uri;
import android.database.Cursor;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.theperegrinefund.security.ConfigLoader;
import com.example.theperegrinefund.HistoryItemD;
import com.example.theperegrinefund.dao.MessageDao;
import com.example.theperegrinefund.Message;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private List<HistoryItemD> historyItems;
    private ImageView menuIcon;
    private ImageView newIcon;
    private ImageView infoIcon;
    private LinearLayout mainContent;
    private ViewPager2 viewPager;
    private CardPagerAdapter pagerAdapter;
    private View buttonPage1;
    private View buttonPage2;
    private  int FIXED_USER_ID ;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private SmsSender smsSender;
    private ServerSender serverSender;
    private String FIXED_NUMBER;
    private String SECRET_KEY;
    private String SERVER_URL;
    private FusedLocationProviderClient fusedLocationClient;
    private int user;
    private String dernierSms = "";
    private static final int PERMISSION_REQUEST_READ_SMS = 100;

    private Button btnSend;
    private String btnSendDefaultText;
    private boolean isSendingMessage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        try {
            FIXED_NUMBER = ConfigLoader.getFixedNumber(this);
            SERVER_URL = ConfigLoader.getServerUrl(this);
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

        // CONF RELIE AU SERVEUR
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(SERVER_URL) // Remplace par l’IP locale de ton PC
            .client(com.example.theperegrinefund.network.NetworkClientProvider.get(this))
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        ApiService apiService = retrofit.create(ApiService.class);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnSend = findViewById(R.id.btn_send_mess);
        btnSendDefaultText = btnSend.getText().toString();
        smsSender = new SmsSender(this);
        serverSender = new ServerSender(apiService, smsSender, this);

        int userId = AppData.getCurrentUserId(this);
        FIXED_USER_ID = userId;

        // File d'attente de renvoi (WorkManager): relance périodiquement (et dès
        // le retour du réseau) les messages restés PENDING dans l'outbox local.
        com.example.theperegrinefund.work.RetryWorkScheduler.schedulePeriodic(this);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSendingMessage) {
                    Log.d(TAG, "Clic ignoré: un envoi est déjà en cours.");
                    Toast.makeText(BaseActivity.this, "Envoi déjà en cours…", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ContextCompat.checkSelfPermission(BaseActivity.this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BaseActivity.this,
                            new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);
                } else {
                    sendMessage();
                }
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        mainContent = findViewById(R.id.main_content);
        menuIcon = findViewById(R.id.menu_icon);
        newIcon = findViewById(R.id.new_icon);
        infoIcon = findViewById(R.id.info_icon);
        ImageView logoutIcon = findViewById(R.id.logout_icon);
        logoutIcon.setOnClickListener(v -> logout());
        buttonPage1 = findViewById(R.id.button_page1);
        buttonPage2 = findViewById(R.id.button_page2);

        // Drawer
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mainContent.setTranslationX(slideOffset * drawerView.getWidth());
            }
        });

        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.history_drawer))) {
                drawerLayout.closeDrawer(findViewById(R.id.history_drawer));
            } else {
                drawerLayout.openDrawer(findViewById(R.id.history_drawer));
            }
        });

        newIcon.setOnClickListener(v -> startActivity(new Intent(BaseActivity.this, BaseActivity.class)));
        infoIcon.setOnClickListener(v -> startActivity(new Intent(BaseActivity.this, StatActivity.class)));

        // ViewPager2
        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new CardPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Initialisation des boutons de page
        updatePageButtons(0);

        // Listener pour changer les boutons selon la page
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updatePageButtons(position);
            }
        });

        // Clic sur les boutons
        buttonPage1.setOnClickListener(v -> viewPager.setCurrentItem(0));
        buttonPage2.setOnClickListener(v -> viewPager.setCurrentItem(1));

        historyRecyclerView = findViewById(R.id.history_recycler_view);
        historyItems = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyItems, this::onHistoryItemClick);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(historyAdapter);
        loadSampleData();
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
        // Toast.makeText(BaseActivity.this,"QQQQQQQ" + dernierSms, Toast.LENGTH_LONG).show();

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
            // if (corps.matches("^.+~.+~.+$")) {
            if (corps.matches("^.+!.+:.+$")) {
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

    /**
     * Retour à l'écran d'accueil après un envoi réussi (HTTP ou SMS direct).
     * DashboardActivity resynchronise automatiquement dans son onResume(),
     * donc pas besoin de déclencher la synchro explicitement ici.
     */
    private void returnToHomeAfterSend() {
        finish();
    }

    /** Active/désactive le bouton d'envoi et bascule son libellé pendant le traitement. */
    private void setSendButtonBusy(boolean busy) {
        isSendingMessage = busy;
        if (btnSend != null) {
            btnSend.setEnabled(!busy);
            btnSend.setText(busy ? "Envoi en cours…" : btnSendDefaultText);
        }
    }

    private void sendMessage() {
    CardPagerAdapter adapter = (CardPagerAdapter) viewPager.getAdapter();
    if (adapter == null) {
        return;
    }
    setSendButtonBusy(true);
    Log.d(TAG, "Envoi du message: démarrage (localisation en cours d'acquisition).");
    {
        Fragment1 f1 = adapter.getFragment1();
        Fragment2 f2 = adapter.getFragment2();

        final Message message = new Message(
            f1.getDateCommencement(),
            f2.getDateSignalement(),
                f1.getIntervention().getIdIntervention(),
                f1.isRenfort(),
                f1.getDirection(),
                f1.getSurface(),
                f2.getPointRepere(),
                f2.getDescription(),
                FIXED_NUMBER,
                0, 0, FIXED_USER_ID
        );
        message.setDateEnvoi(LocalDateTime.now());

        // Obtenir la localisation puis envoyer le message.
        // Priorité HTTP direct si internet est disponible ; repli automatique
        // sur le SMS gateway sinon (voir ServerSender.dispatch()).
        getCurrentLocationAndSend(message, new Runnable() {
            @Override
            public void run() {
                if (serverSender != null) {
                    Log.d(TAG, "Envoi du message: localisation acquise, envoi vers le serveur/SMS.");
                    serverSender.send(message, f1.getStatus(), new ServerSender.SendCallback() {
                        @Override
                        public void onSent() {
                            Log.d(TAG, "Envoi du message: terminé avec succès.");
                            runOnUiThread(() -> {
                                setSendButtonBusy(false);
                                returnToHomeAfterSend();
                            });
                        }

                        @Override
                        public void onQueuedForRetry() {
                            Log.w(TAG, "Envoi du message: échec, mis en file d'attente pour renvoi automatique.");
                            runOnUiThread(() -> setSendButtonBusy(false));
                        }
                    });
                } else {
                    // Config serveur indisponible au démarrage: on retombe directement sur le SMS.
                    try {
                        smsSender.send(message, f1.getStatus());
                        Log.d(TAG, "Envoi du message: terminé (SMS direct, pas de config serveur).");
                        Toast.makeText(BaseActivity.this, " Message envoyé par SMS.", Toast.LENGTH_SHORT).show();
                        returnToHomeAfterSend();
                    } catch (Exception ex) {
                        Log.e(TAG, "Envoi du message: échec de l'envoi SMS direct.", ex);
                        Toast.makeText(BaseActivity.this, "Erreur SMS : " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    } finally {
                        setSendButtonBusy(false);
                    }
                }
            }
        });
    }
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


    private void getCurrentLocationAndSend(final Message message, final Runnable onLocationReady) {
    // Vérifier les permissions
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        // onLocationReady ne sera jamais appelé pour cette tentative (l'utilisateur
        // doit d'abord répondre à la demande de permission puis retaper "Envoyer") -
        // sans ça le bouton resterait bloqué en "Envoi en cours…" indéfiniment.
        Log.w(TAG, "Envoi du message: permission de localisation manquante, envoi annulé.");
        setSendButtonBusy(false);
        return;
    }

    // Si la localisation est désactivée au niveau système (GPS ET réseau
    // coupés), fusedLocationClient.requestLocationUpdates() n'appellera
    // JAMAIS onLocationResult - l'ancien mécanisme de "timeout" ci-dessous
    // ne comptait les tentatives que DANS ce callback, donc ne se déclenchait
    // jamais dans ce cas: l'envoi restait bloqué indéfiniment (observé:
    // 5+ minutes d'attente, aucune requête n'atteignant jamais le serveur).
    // On vérifie donc l'état des services de localisation en amont pour
    // sauter l'attente immédiatement si elle ne peut de toute façon aboutir.
    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    boolean locationServiceEnabled = locationManager != null
            && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    if (!locationServiceEnabled) {
        Log.w(TAG, "Envoi du message: services de localisation désactivés, envoi sans coordonnées GPS.");
        Toast.makeText(this, "Localisation désactivée : message envoyé sans position GPS.", Toast.LENGTH_LONG).show();
        if (onLocationReady != null) {
            onLocationReady.run();
        }
        return;
    }

    // Créer la requête GPS
    LocationRequest locationRequest = LocationRequest.create();
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // GPS pur
    locationRequest.setInterval(1000); // Mise à jour toutes les secondes
    locationRequest.setFastestInterval(1000);

    // Garantit que onLocationReady n'est appelé qu'une seule fois, que ce
    // soit via un fix GPS obtenu, via l'ancien compteur de tentatives, ou
    // via le timeout matériel ci-dessous (celui-ci est le vrai filet de
    // sécurité: il se déclenche même si onLocationResult n'est JAMAIS appelé).
    final AtomicBoolean alreadyProceeded = new AtomicBoolean(false);
    final Handler timeoutHandler = new Handler(Looper.getMainLooper());

    // Déclarer le callback
    final LocationCallback locationCallback = new LocationCallback() {
        private int attempts = 0;
        private final int MAX_ATTEMPTS = 20; // Timeout après 20 updates (~20 secondes)

        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) return;

            Location location = locationResult.getLastLocation();
            attempts++;

            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                if (lat != 0.0 && lon != 0.0) {
                    // Position GPS valide
                    message.setLatitude(lat);
                    message.setLongitude(lon);
                    fusedLocationClient.removeLocationUpdates(this);
                    timeoutHandler.removeCallbacksAndMessages(null);
                    if (onLocationReady != null && alreadyProceeded.compareAndSet(false, true)) {
                        onLocationReady.run();
                    }
                    return;
                }
            }

            // Timeout "logique" si on dépasse le nombre max d'essais (ne se
            // déclenche que si onLocationResult est bien appelé au moins
            // MAX_ATTEMPTS fois - voir le timeout matériel ci-dessous pour
            // le cas où il n'est jamais appelé du tout).
            if (attempts >= MAX_ATTEMPTS) {
                fusedLocationClient.removeLocationUpdates(this);
                timeoutHandler.removeCallbacksAndMessages(null);
                if (onLocationReady != null && alreadyProceeded.compareAndSet(false, true)) {
                    onLocationReady.run(); // On envoie le message même si GPS pas fixé
                }
            }
        }
    };

    // Timeout matériel (15s), indépendant du callback: filet de sécurité
    // si onLocationResult n'est jamais appelé (ex: fix GPS impossible en
    // intérieur, provider qui ne répond pas malgré isProviderEnabled=true).
    timeoutHandler.postDelayed(() -> {
        if (alreadyProceeded.compareAndSet(false, true)) {
            Log.w(TAG, "Envoi du message: timeout localisation (15s), envoi sans position GPS.");
            fusedLocationClient.removeLocationUpdates(locationCallback);
            if (onLocationReady != null) {
                onLocationReady.run();
            }
        }
    }, 15000);

    // Demander les mises à jour GPS
    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
}



    @Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                       @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        if (requestCode == PERMISSION_REQUEST_READ_SMS) {
            // Permission SMS accordée
            initialiserSmsObserver();
        } else if (requestCode == PERMISSION_REQUEST_CODE) {
            // Permission pour envoyer SMS accordée
            sendMessage();
        }
    } else {
        Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
    }
}


    private void updatePageButtons(int page) {
        buttonPage1.setBackgroundResource(page == 0 ? R.drawable.circle_active : R.drawable.circle_inactive);
        buttonPage2.setBackgroundResource(page == 1 ? R.drawable.circle_active : R.drawable.circle_inactive);
    }

    private void loadSampleData() {
        historyItems.clear();

        MessageDao messageDao = new MessageDao(this);
        List<Message> messages = messageDao.getAllMessages(AppData.getCurrentUserId(this));

        for (Message msg : messages) {
            historyItems.add(new HistoryItemD(
                    msg.getDescription() + " (" + msg.getDateCommencement() + ")",
                    false,
                    msg.getIdMessage() // Ajoutez le troisième paramètre
            ));
        }

        historyAdapter.notifyDataSetChanged();
    }
    
    private void onHistoryItemClick(HistoryItemD item, int position) {
        for (HistoryItemD historyItem : historyItems) {
            historyItem.setSelected(false);
        }
        item.setSelected(true);
        historyAdapter.notifyDataSetChanged();
         // Récupérer l'id du message si besoin
    int messageId = item.getMessageId();

    // Créer l'intent pour aller vers le Dashboard
    Intent intent = new Intent(this, DashboardActivity.class);
    // Tu peux passer des données si nécessaire
    intent.putExtra("MESSAGE_ID", messageId);
    startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_refresh) {
            refreshData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshData() {
        loadSampleData();
    }

    private void logout() {
        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(findViewById(R.id.history_drawer))) {
            drawerLayout.closeDrawer(findViewById(R.id.history_drawer));
        } else {
            super.onBackPressed();
        }
    }
}
