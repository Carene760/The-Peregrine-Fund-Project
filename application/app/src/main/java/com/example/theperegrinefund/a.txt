package com.example.theperegrinefund;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.theperegrinefund.dao.MessageDao;
import com.example.theperegrinefund.Message;
import android.util.Log;
import com.example.theperegrinefund.service.SyncService;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Locale;
import com.example.theperegrinefund.HistoryAdapter;
import com.example.theperegrinefund.Intervention;
import com.example.theperegrinefund.dao.InterventionDao;
import com.example.theperegrinefund.StatusMessage;
import com.example.theperegrinefund.dao.StatusMessageDao;
import com.example.theperegrinefund.HistoriqueMessageStatus;
import com.example.theperegrinefund.dao.HistoriqueMessageStatusDao;
import com.example.theperegrinefund.AppData;
import com.example.theperegrinefund.StatActivity;
import com.example.theperegrinefund.HistoryItemD;

// Add these imports
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.theperegrinefund.security.ConfigLoader;
import com.example.theperegrinefund.SmsSender;
import com.example.theperegrinefund.ServerSender;
import com.example.theperegrinefund.ApiService;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private List<HistoryItemD> historyItems;
    private ImageView menuIcon;
    private ImageView newIcon;
    private ImageView infoIcon;
    private int FIXED_USER_ID;
    
    // Add ServerSender instance
    private ServerSender serverSender;
    private String SERVER_URL;
    private String SECRET_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize server configuration
        try {
            SERVER_URL = ConfigLoader.getServerUrl(this);
            SECRET_KEY = ConfigLoader.getSecretKey(this);
            
            // Initialize Retrofit and ServerSender
            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
            
            ApiService apiService = retrofit.create(ApiService.class);
            SmsSender smsSender = new SmsSender(this);
            serverSender = new ServerSender(apiService, smsSender, this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur de configuration serveur", Toast.LENGTH_SHORT).show();
        }

        // Initialisation du Drawer et du contenu principal
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        LinearLayout mainContent = findViewById(R.id.main_content); // LinearLayout contenant tout le contenu principal

        // Décalage du contenu principal quand le drawer s'ouvre
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mainContent.setTranslationX(slideOffset * drawerView.getWidth());
            }
        });

        // Initialisation des autres vues (menuIcon, RecyclerView, etc.)
        menuIcon = findViewById(R.id.menu_icon);
        historyRecyclerView = findViewById(R.id.history_recycler_view);

        // Clic sur le bouton menu
        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.history_drawer))) {
                drawerLayout.closeDrawer(findViewById(R.id.history_drawer));
            } else {
                drawerLayout.openDrawer(findViewById(R.id.history_drawer));
            }
        });

        // Initialisation des autres vues (newIcon, RecyclerView, etc.)
        newIcon = findViewById(R.id.new_icon);
        infoIcon = findViewById(R.id.info_icon);
        // Après l'initialisation des autres icônes
        ImageView logoutIcon = findViewById(R.id.logout_icon);
        logoutIcon.setOnClickListener(v -> logout());
        // Synchronisation des données au démarrage
        MessageDao messageDao = new MessageDao(this);
        SyncService syncService = new SyncService(this);
        AppData appData = new AppData();
        int userId = appData.getCurrentUserId();
       // FIXED_USER_ID = userId; // Utilisez l'ID utilisateur actuel
        FIXED_USER_ID = 1; // ID utilisateur fixe pour les tests
        // Synchronisez d'abord les statuts
        syncService.downloadStatus(new SyncService.StatusCallback() {
            @Override
            public void onComplete(List<StatusMessage> statusMessages) {
                Log.d("SYNC", "Statuts synchronisés : " + statusMessages.size());
                
                // Ensuite, synchronisez les interventions
                syncService.downloadIntervention(new SyncService.InterventionCallback() {
                    @Override
                    public void onComplete(List<Intervention> interventions) {
                        Log.d("SYNC", "Interventions synchronisées : " + interventions.size());
                        
                        // Puis synchronisez les messages
                        syncService.downloadMessages(FIXED_USER_ID, new SyncService.MessageCallback() {
                            @Override
                            public void onComplete(List<Message> messages) {
                                Log.d("SYNC", "Messages synchronisés : " + messages.size());
                                
                                // Enfin, synchronisez l'historique
                                syncService.downloadHistorique(FIXED_USER_ID, new SyncService.HistoriqueCallback() {
                                    @Override
                                    public void onComplete(List<HistoriqueMessageStatus> historiques) {
                                        Log.d("SYNC", "Historique synchronisé : " + historiques.size());
                                        runOnUiThread(() -> {
                                            Toast.makeText(DashboardActivity.this, "Synchronisation terminée", Toast.LENGTH_SHORT).show();
                                            loadSampleData();
                                        });
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e("SYNC", "Erreur de synchronisation historique", e);
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("SYNC", "Erreur de synchronisation messages", e);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("SYNC", "Erreur de synchronisation interventions", e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("SYNC", "Erreur de synchronisation statuts", e);
            }
        });

        // Clic sur le bouton new
        newIcon.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, BaseActivity.class);
            startActivity(intent);
        });

        infoIcon.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, StatActivity.class)));

        // Reste du code : RecyclerView, chargement des données, etc.
        historyItems = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyItems, this::onHistoryItemClick);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(historyAdapter);
        loadSampleData();
    }

    private void loadSampleData() {
        historyItems.clear();

        MessageDao messageDao = new MessageDao(this);
        List<Message> messages = messageDao.getAllMessages();

        for (Message msg : messages) {
            historyItems.add(new HistoryItemD(
                    msg.getDescription() + " (" + msg.getDateCommencement() + ")",
                    false,
                    msg.getIdMessage() // Ajoutez le troisième paramètre
            ));
        }

        historyAdapter.notifyDataSetChanged();
    }

    private void showMessageDetails(int messageId) {
    MessageDao messageDao = new MessageDao(this);
    HistoriqueMessageStatusDao historiqueDao = new HistoriqueMessageStatusDao(this);
    Message message = messageDao.getMessageById(messageId);

    if (message != null) {
        // Formatteur de date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        
        // Trouvez les vues dans votre CardView
        TextView tvDateTime = findViewById(R.id.tv_datetime);
        TextView tvSignalement = findViewById(R.id.tv_signalement);
        TextView tvSurface = findViewById(R.id.tv_surface);
        TextView tvDescription = findViewById(R.id.tv_description);
        TextView tvHistoryContent = findViewById(R.id.tv_history_content);
        Button btnEnCours = findViewById(R.id.btn_en_cours);
        Button btnMaitrise = findViewById(R.id.btn_maitrise);
        TextView tvStatusMessage = findViewById(R.id.tv_status_message); // Ajout de la référence

        // Mettez à jour les vues avec les données du message (convertir les dates en String)
        if (message.getDateCommencement() != null) {
            tvDateTime.setText(dateFormat.format(message.getDateCommencement()));
        } else {
            tvDateTime.setText("Date inconnue");
        }
        
        if (message.getDateSignalement() != null) {
            tvSignalement.setText(dateFormat.format(message.getDateSignalement()));
        } else {
            tvSignalement.setText("Date inconnue");
        }
        
        tvSurface.setText(message.getSurfaceApproximative() + " ha");
        tvDescription.setText(message.getDescription());

        // 🔹 Vérifier le dernier statut
        int lastStatusId = historiqueDao.getLastStatusForMessage(messageId);

        // Cacher tous les éléments par défaut
        btnEnCours.setVisibility(View.GONE);
        btnMaitrise.setVisibility(View.GONE);
        tvStatusMessage.setVisibility(View.GONE);

        // Gérer l'affichage selon le statut
        if (lastStatusId == 1) {
            // Statut "Nouveau" - Afficher les deux boutons
            btnEnCours.setVisibility(View.VISIBLE);
            btnMaitrise.setVisibility(View.VISIBLE);
        } else if (lastStatusId == 2) {
            // Statut "En Cours" - Afficher seulement le bouton "Maitrise"
            btnMaitrise.setVisibility(View.VISIBLE);
        } else if (lastStatusId == 3) {
            // Statut "Maitrise" - Afficher le message
            tvStatusMessage.setVisibility(View.VISIBLE);
        }

        // Set up button click listeners for status updates
        btnEnCours.setOnClickListener(v -> updateStatus(messageId, "En Cours"));
        btnMaitrise.setOnClickListener(v -> updateStatus(messageId, "Maitrise"));

        // Récupérer l'historique complet des statuts
        List<HistoriqueMessageStatus> historiqueList = historiqueDao.getStatusHistoryForMessage(messageId);
        StringBuilder historyBuilder = new StringBuilder();

        for (HistoriqueMessageStatus hist : historiqueList) {
            String statusName = getStatusNameFromId(hist.getIdStatusMessage());
            historyBuilder.append(hist.getDateChangement())
                        .append(" - ")
                        .append(statusName)
                        .append("\n");
        }

        tvHistoryContent.setText(historyBuilder.toString());
    }
}


    // Méthode pour mettre à jour le statut
    private void updateStatus(int messageId, String newStatus) {
        // Create a new HistoriqueMessageStatus with String date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateString = dateFormat.format(new Date());

        HistoriqueMessageStatus historique = new HistoriqueMessageStatus(
            currentDateString,  // dateChangement as String
            getStatusIdFromName(newStatus),  // idStatusMessage (you need to convert status name to ID)
            messageId           // idMessage
        );
        
        // Send the status update using ServerSender
        if (serverSender != null) {
            serverSender.sendHistory(historique);
        } else {
            Toast.makeText(this, "Erreur: Serveur non initialisé", Toast.LENGTH_SHORT).show();
        }
        
        // Rafraîchir l'affichage
        showMessageDetails(messageId);
    }
private int getStatusIdFromName(String statusName) {
    // Implement this method to get the status ID from the status name
    // This might involve querying your database or using a predefined mapping
    switch (statusName) {
        case "En Cours":
            return 2; // Replace with actual ID
        case "Maitrise":
            return 3; // Replace with actual ID
        // Add more cases as needed
        default:
            return -1; // Unknown status
    }
}
     public String getStatusNameFromId(int statusId) {
        String statusName = "Inconnu";
        StatusMessageDao statusDao = new StatusMessageDao(this);
        statusName = statusDao.getStatusNameId(statusId);
        return statusName;
    }
    private void onHistoryItemClick(HistoryItemD item, int position) {
        for (HistoryItemD historyItem : historyItems) {
            historyItem.setSelected(false);
        }
        item.setSelected(true);
        historyAdapter.notifyDataSetChanged();
        
        // Affichez les détails du message
        showMessageDetails(item.getMessageId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (toggle != null && toggle.onOptionsItemSelected(item)) {
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
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(findViewById(R.id.history_drawer))) {
            drawerLayout.closeDrawer(findViewById(R.id.history_drawer));
        } else {
            super.onBackPressed();
        }
    }
}