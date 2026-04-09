package com.example.theperegrinefund;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.example.theperegrinefund.dao.EvenementDao;
import com.example.theperegrinefund.dao.InterventionDao;
import com.example.theperegrinefund.StatusMessage;
import com.example.theperegrinefund.dao.StatusMessageDao;
import com.example.theperegrinefund.Evenement;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private List<HistoryItemD> historyItems;
    private ImageView menuIcon;
    private ImageView newIcon;
    private ImageView filterIcon;
    private ImageView infoIcon;
    private Button syncTopButton;
    private int FIXED_USER_ID;
    private int selectedMessageId = -1;
    private String currentKeywordFilter = "";
    private LocalDate currentStartDateFilter = null;
    private String currentStatusFilter = "Tous";
    private List<Message> allMessages = new ArrayList<>();
    private int syncedStatusCount = 0;
    private int syncedEvenementCount = 0;
    private int syncedInterventionCount = 0;
    private int syncedMessageCount = 0;
    private int syncedHistoriqueCount = 0;
    private boolean isSyncInProgress = false;
    
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
        filterIcon = findViewById(R.id.filter_icon);
        infoIcon = findViewById(R.id.info_icon);
        ImageView logoutIcon = findViewById(R.id.logout_icon);
        logoutIcon.setOnClickListener(v -> logout());
        filterIcon.setOnClickListener(v -> showFilterDialog());
        
        AppData appData = new AppData();
        int userId = appData.getCurrentUserId();
        //  FIXED_USER_ID = userId;
        FIXED_USER_ID = 4;

        syncTopButton = findViewById(R.id.btn_sync_top);
        syncTopButton.setOnClickListener(v -> startSynchronization());

        startSynchronization();

        newIcon.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, BaseActivity.class);
            startActivity(intent);
        });

        infoIcon.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, StatActivity.class)));

        historyItems = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyItems, this::onHistoryItemClick);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(historyAdapter);
        loadMessagesAndApplyFilters();
    }

    private void startSynchronization() {
        if (isSyncInProgress) {
            Toast.makeText(this, "Synchronisation déjà en cours...", Toast.LENGTH_SHORT).show();
            return;
        }

        setSyncInProgress(true);
        SyncService syncService = new SyncService(this);
        syncService.downloadStatus(new SyncService.StatusCallback() {
            @Override
            public void onComplete(List<StatusMessage> statusMessages) {
                syncedStatusCount = statusMessages != null ? statusMessages.size() : 0;
                syncService.downloadEvenements(new SyncService.EvenementCallback() {
                    @Override
                    public void onComplete(List<Evenement> evenements) {
                        syncedEvenementCount = evenements != null ? evenements.size() : 0;
                        syncService.downloadIntervention(new SyncService.InterventionCallback() {
                            @Override
                            public void onComplete(List<Intervention> interventions) {
                                syncedInterventionCount = interventions != null ? interventions.size() : 0;
                                syncService.downloadMessages(FIXED_USER_ID, new SyncService.MessageCallback() {
                                    @Override
                                    public void onComplete(List<Message> messages) {
                                        syncedMessageCount = messages != null ? messages.size() : 0;
                                        syncService.downloadHistorique(FIXED_USER_ID, new SyncService.HistoriqueCallback() {
                                            @Override
                                            public void onComplete(List<HistoriqueMessageStatus> historiques) {
                                                syncedHistoriqueCount = historiques != null ? historiques.size() : 0;
                                                runOnUiThread(() -> {
                                                    setSyncInProgress(false);
                                                    Toast.makeText(DashboardActivity.this, "Synchronisation terminée", Toast.LENGTH_SHORT).show();
                                                    loadMessagesAndApplyFilters();
                                                });
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Log.e("SYNC", "Erreur de synchronisation historique", e);
                                                runOnUiThread(() -> {
                                                    setSyncInProgress(false);
                                                    Toast.makeText(DashboardActivity.this, "Synchronisation partielle (historique en erreur)", Toast.LENGTH_LONG).show();
                                                });
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e("SYNC", "Erreur de synchronisation messages", e);
                                        runOnUiThread(() -> {
                                            setSyncInProgress(false);
                                            Toast.makeText(DashboardActivity.this, "Synchronisation partielle (messages en erreur)", Toast.LENGTH_LONG).show();
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("SYNC", "Erreur de synchronisation interventions", e);
                                runOnUiThread(() -> {
                                    setSyncInProgress(false);
                                    Toast.makeText(DashboardActivity.this, "Synchronisation partielle (interventions en erreur)", Toast.LENGTH_LONG).show();
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("SYNC", "Erreur de synchronisation evenements", e);
                        runOnUiThread(() -> {
                            setSyncInProgress(false);
                            Toast.makeText(DashboardActivity.this, "Synchronisation partielle (evenements en erreur)", Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("SYNC", "Erreur de synchronisation statuts", e);
                runOnUiThread(() -> {
                    setSyncInProgress(false);
                    Toast.makeText(DashboardActivity.this, "Synchronisation échouée (statuts en erreur)", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setSyncInProgress(boolean inProgress) {
        isSyncInProgress = inProgress;

        if (syncTopButton == null) {
            return;
        }

        syncTopButton.setEnabled(!inProgress);
        syncTopButton.setAlpha(inProgress ? 0.6f : 1f);
        syncTopButton.setText(inProgress ? "Sync en cours..." : "Sync");
    }

    private void loadMessagesAndApplyFilters() {
        MessageDao messageDao = new MessageDao(this);
        allMessages = messageDao.getAllMessages();
        applyHistoryFilters();
    }

    private void loadSampleData() {
        loadMessagesAndApplyFilters();
    }

    private void applyHistoryFilters() {
        historyItems.clear();

        for (Message msg : allMessages) {
            if (matchesHistoryFilters(msg)) {
                historyItems.add(new HistoryItemD(
                        buildHistoryTitle(msg),
                        msg.getIdMessage() == selectedMessageId,
                        msg.getIdMessage()
                ));
            }
        }

        historyAdapter.notifyDataSetChanged();
    }

    private boolean matchesHistoryFilters(Message message) {
        if (message == null) {
            return false;
        }

        if (currentStartDateFilter != null) {
            if (message.getDateCommencement() == null || !currentStartDateFilter.equals(message.getDateCommencement().toLocalDate())) {
                return false;
            }
        }

        String keyword = currentKeywordFilter == null ? "" : currentKeywordFilter.trim().toLowerCase(Locale.getDefault());
        if (!keyword.isEmpty()) {
            String searchableText = safeLower(message.getDescription()) + " "
                    + safeLower(message.getPointRepere()) + " "
                    + safeLower(message.getDirection()) + " "
                    + safeLower(message.getPhoneNumber()) + " "
                    + safeLower(getEventSearchText(message));
            if (!searchableText.contains(keyword)) {
                return false;
            }
        }

        if (currentStatusFilter != null && !"Tous".equals(currentStatusFilter)) {
            HistoriqueMessageStatusDao historiqueDao = new HistoriqueMessageStatusDao(this);
            int lastStatusId = historiqueDao.getLastStatusForMessage(message.getIdMessage());
            if (lastStatusId != getStatusIdFromName(currentStatusFilter)) {
                return false;
            }
        }

        return true;
    }

    private String buildHistoryTitle(Message message) {
        if (message == null) {
            return "";
        }

        StringBuilder title = new StringBuilder();
        if (message.getDescription() != null && !message.getDescription().trim().isEmpty()) {
            title.append(message.getDescription().trim());
        } else {
            title.append("Message ").append(message.getIdMessage());
        }

        if (message.getDateCommencement() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());
            title.append(" (").append(message.getDateCommencement().format(formatter)).append(")");
        }

        return title.toString();
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.getDefault());
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filtrer l'historique");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(48, 32, 48, 0);

        EditText keywordInput = new EditText(this);
        keywordInput.setHint("Mot-clé");
        keywordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        keywordInput.setText(currentKeywordFilter);
        container.addView(keywordInput);

        EditText dateInput = new EditText(this);
        dateInput.setHint("Date de commencement");
        dateInput.setFocusable(false);
        dateInput.setClickable(true);
        dateInput.setInputType(InputType.TYPE_CLASS_DATETIME);
        if (currentStartDateFilter != null) {
            dateInput.setText(currentStartDateFilter.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())));
        }
        dateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (currentStartDateFilter != null) {
                calendar.set(currentStartDateFilter.getYear(), currentStartDateFilter.getMonthValue() - 1, currentStartDateFilter.getDayOfMonth());
            }

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        currentStartDateFilter = LocalDate.of(year, month + 1, dayOfMonth);
                        dateInput.setText(currentStartDateFilter.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });
        container.addView(dateInput);

        Spinner statusSpinner = new Spinner(this);
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Tous", "Debut de feu", "En Cours", "Maitrise"}
        );
        statusSpinner.setAdapter(statusAdapter);
        int selectedIndex = statusAdapter.getPosition(currentStatusFilter == null ? "Tous" : currentStatusFilter);
        statusSpinner.setSelection(Math.max(selectedIndex, 0));
        container.addView(statusSpinner);

        builder.setView(container);
        builder.setPositiveButton("Appliquer", (dialog, which) -> {
            currentKeywordFilter = keywordInput.getText() != null ? keywordInput.getText().toString() : "";
            currentStatusFilter = statusSpinner.getSelectedItem() != null ? statusSpinner.getSelectedItem().toString() : "Tous";
            applyHistoryFilters();
        });
        builder.setNeutralButton("Réinitialiser", (dialog, which) -> {
            currentKeywordFilter = "";
            currentStartDateFilter = null;
            currentStatusFilter = "Tous";
            selectedMessageId = -1;
            loadMessagesAndApplyFilters();
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void showMessageDetails(int messageId) {
        MessageDao messageDao = new MessageDao(this);
        HistoriqueMessageStatusDao historiqueDao = new HistoriqueMessageStatusDao(this);
        Message message = messageDao.getMessageById(messageId);

        if (message != null) {
             Toast.makeText(this, "Message trouvé avec l'ID: " + messageId, Toast.LENGTH_SHORT).show();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());

            TextView tvDateTime = findViewById(R.id.tv_datetime);
            TextView tvSignalement = findViewById(R.id.tv_signalement);
            TextView tvSurface = findViewById(R.id.tv_surface);
            TextView tvDescription = findViewById(R.id.tv_description);
            TextView tvEvenement = findViewById(R.id.tv_evenement);
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
            
            tvSurface.setText(message.getSurfaceApproximative() + " m2");
            tvDescription.setText(message.getDescription());
            tvEvenement.setText(formatEvenementForDisplay(message));

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

    private String formatEvenementForDisplay(Message message) {
        if (message == null) {
            return "Non lié";
        }

        if (message.getEvenement() != null && message.getEvenement().getNom() != null
                && !message.getEvenement().getNom().trim().isEmpty()) {
            return message.getEvenement().getNom().trim();
        }

        if (message.getIdEvenement() != null) {
            EvenementDao evenementDao = new EvenementDao(this);
            Evenement evenement = evenementDao.getEvenementById(message.getIdEvenement());
            if (evenement != null && evenement.getNom() != null && !evenement.getNom().trim().isEmpty()) {
                return evenement.getNom().trim();
            }
            return "ID " + message.getIdEvenement();
        }

        return "Non lié";
    }

    private String getEventSearchText(Message message) {
        if (message == null) {
            return "";
        }

        if (message.getEvenement() != null && message.getEvenement().getNom() != null) {
            return message.getEvenement().getNom();
        }

        if (message.getIdEvenement() != null) {
            EvenementDao evenementDao = new EvenementDao(this);
            Evenement evenement = evenementDao.getEvenementById(message.getIdEvenement());
            if (evenement != null) {
                StringBuilder builder = new StringBuilder();
                if (evenement.getNom() != null) {
                    builder.append(evenement.getNom()).append(' ');
                }
                if (evenement.getDescription() != null) {
                    builder.append(evenement.getDescription());
                }
                return builder.toString();
            }
        }

        return "";
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
            case "Debut de feu":
                return 1;
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
        selectedMessageId = item.getMessageId();
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
        loadMessagesAndApplyFilters();
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