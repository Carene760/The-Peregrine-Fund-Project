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

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.theperegrinefund.security.ConfigLoader;
import com.example.theperegrinefund.SmsSender;
import com.example.theperegrinefund.ServerSender;
import com.example.theperegrinefund.ApiService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
    
    private ServerSender serverSender;
    private String SERVER_URL;
    private String SECRET_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        try {
            SERVER_URL = ConfigLoader.getServerUrl(this);
            SECRET_KEY = ConfigLoader.getSecretKey(this);
            
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

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        LinearLayout mainContent = findViewById(R.id.main_content);

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mainContent.setTranslationX(slideOffset * drawerView.getWidth());
            }
        });

        menuIcon = findViewById(R.id.menu_icon);
        historyRecyclerView = findViewById(R.id.history_recycler_view);

        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.history_drawer))) {
                drawerLayout.closeDrawer(findViewById(R.id.history_drawer));
            } else {
                drawerLayout.openDrawer(findViewById(R.id.history_drawer));
            }
        });

        newIcon = findViewById(R.id.new_icon);
        infoIcon = findViewById(R.id.info_icon);
        ImageView logoutIcon = findViewById(R.id.logout_icon);
        logoutIcon.setOnClickListener(v -> logout());
        
        MessageDao messageDao = new MessageDao(this);
        SyncService syncService = new SyncService(this);
        AppData appData = new AppData();
        int userId = appData.getCurrentUserId();
         FIXED_USER_ID = userId;
       // FIXED_USER_ID = 1;
        syncService.downloadStatus(new SyncService.StatusCallback() {
            @Override
            public void onComplete(List<StatusMessage> statusMessages) {
                syncService.downloadIntervention(new SyncService.InterventionCallback() {
                    @Override
                    public void onComplete(List<Intervention> interventions) {
                        syncService.downloadMessages(FIXED_USER_ID, new SyncService.MessageCallback() {
                            @Override
                            public void onComplete(List<Message> messages) {
                                syncService.downloadHistorique(FIXED_USER_ID, new SyncService.HistoriqueCallback() {
                                    @Override
                                    public void onComplete(List<HistoriqueMessageStatus> historiques) {
                                        runOnUiThread(() -> {
                                          //  Toast.makeText(DashboardActivity.this, "Synchronisation terminée", Toast.LENGTH_SHORT).show();
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

        newIcon.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, BaseActivity.class);
            startActivity(intent);
        });

        infoIcon.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, StatActivity.class)));

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

        // Toast.makeText(this, "Nombre de messages en local: " + messages.size(), Toast.LENGTH_SHORT).show();

        for (Message msg : messages) {
            historyItems.add(new HistoryItemD(
                    msg.getDescription() + " (" + msg.getDateCommencement() + ")",
                    false,
                    msg.getIdMessage()
            ));
        }

        historyAdapter.notifyDataSetChanged();
    }

    private void showMessageDetails(int messageId) {
        MessageDao messageDao = new MessageDao(this);
        HistoriqueMessageStatusDao historiqueDao = new HistoriqueMessageStatusDao(this);
        Message message = messageDao.getMessageById(messageId);

        if (message != null) {
          //  Toast.makeText(this, "Message trouvé avec l'ID: " + messageId, Toast.LENGTH_SHORT).show();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());

            TextView tvDateTime = findViewById(R.id.tv_datetime);
            TextView tvSignalement = findViewById(R.id.tv_signalement);
            TextView tvSurface = findViewById(R.id.tv_surface);
            TextView tvDescription = findViewById(R.id.tv_description);
            TextView tvHistoryContent = findViewById(R.id.tv_history_content);
            Button btnEnCours = findViewById(R.id.btn_en_cours);
            Button btnMaitrise = findViewById(R.id.btn_maitrise);
            TextView tvStatusMessage = findViewById(R.id.tv_status_message);

            if (message.getDateCommencement() != null) {
                 tvDateTime.setText(message.getDateCommencement().format(formatter));
            } else {
                tvDateTime.setText("Date inconnue");
            }
            
            if (message.getDateSignalement() != null) {
                tvSignalement.setText(message.getDateSignalement().format(formatter));
            } else {
                tvSignalement.setText("Date inconnue");
            }
            
            tvSurface.setText(message.getSurfaceApproximative() + " ha");
            tvDescription.setText(message.getDescription());

            int lastStatusId = historiqueDao.getLastStatusForMessage(messageId);

            btnEnCours.setVisibility(View.GONE);
            btnMaitrise.setVisibility(View.GONE);
            tvStatusMessage.setVisibility(View.GONE);

            if (lastStatusId == 1) {
                btnEnCours.setVisibility(View.VISIBLE);
                btnMaitrise.setVisibility(View.VISIBLE);
            } else if (lastStatusId == 2) {
                btnMaitrise.setVisibility(View.VISIBLE);
            } else if (lastStatusId == 3) {
                tvStatusMessage.setVisibility(View.VISIBLE);
            }

            btnEnCours.setOnClickListener(v -> updateStatus(messageId, "En Cours", btnEnCours, btnMaitrise));
            btnMaitrise.setOnClickListener(v -> updateStatus(messageId, "Maitrise", btnEnCours, btnMaitrise));

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

    private void updateStatus(int messageId, String newStatus, Button btnEnCours, Button btnMaitrise) {
        btnEnCours.setVisibility(View.GONE);
        btnMaitrise.setVisibility(View.GONE);
        SmsSender smsSender = new SmsSender(this);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateString = LocalDateTime.now().format(formatter);

        HistoriqueMessageStatus historique = new HistoriqueMessageStatus(
            currentDateString,
            getStatusIdFromName(newStatus),
            messageId
        );
        
       try {
            smsSender.sendHistory(historique);

            runOnUiThread(() -> {
                showMessageDetails(messageId);
                Toast.makeText(this, "Historique envoyé par SMS", Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            Toast.makeText(this, "Erreur envoi SMS : " + e.getMessage(), Toast.LENGTH_LONG).show();

            // Réafficher les boutons en cas d’échec
            btnEnCours.setVisibility(View.VISIBLE);
            btnMaitrise.setVisibility(View.VISIBLE);
        }

      //  showMessageDetails(messageId);
    }

    private int getStatusIdFromName(String statusName) {
        switch (statusName) {
            case "En Cours":
                return 2;
            case "Maitrise":
                return 3;
            default:
                return -1;
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