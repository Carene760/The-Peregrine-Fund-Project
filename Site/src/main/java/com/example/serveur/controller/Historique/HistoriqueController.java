package com.example.serveur.controller.Historique;

import com.example.serveur.model.*;
import com.example.serveur.repository.InterventionRepository;
import com.example.serveur.repository.EvenementRepository;
import com.example.serveur.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;

@Controller
public class HistoriqueController {

    private final MessageService messageService;
    private final AlerteService alerteService;
    private final HistoriqueMessageStatusService historiqueMessageStatusService;
    private final SiteService siteService;
    private final HistoriqueMessageEditService historiqueMessageEditService;
    private final MessageExportService messageExportService;
    private final MessageImportService messageImportService;
    private final InterventionRepository interventionRepository;
    private final EvenementRepository evenementRepository;

    @Autowired
    public HistoriqueController(MessageService messageService,
                                AlerteService alerteService,
                                HistoriqueMessageStatusService historiqueMessageStatusService,
                                SiteService siteService,
                                HistoriqueMessageEditService historiqueMessageEditService,
                                MessageExportService messageExportService,
                                MessageImportService messageImportService,
                                InterventionRepository interventionRepository,
                                EvenementRepository evenementRepository) {
        this.messageService = messageService;
        this.alerteService = alerteService;
        this.historiqueMessageStatusService = historiqueMessageStatusService;
        this.siteService = siteService;
        this.historiqueMessageEditService = historiqueMessageEditService;
        this.messageExportService = messageExportService;
        this.messageImportService = messageImportService;
        this.interventionRepository = interventionRepository;
        this.evenementRepository = evenementRepository;
    }

    @GetMapping("/history")
    public String showHistory(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");

        // Récupération des données
        List<Message> messages = messageService.findAll();
        List<Alerte> alertes = alerteService.findAll();
        List<HistoriqueMessageStatus> status = historiqueMessageStatusService.findAll();
        List<Site> sites = siteService.findAll();
        List<Intervention> interventions = interventionRepository.findAll();
        List<Evenement> evenements = evenementRepository.findAll();

        // Associer chaque message avec ses alertes
        Map<Integer, List<Alerte>> alerteMessage = new HashMap<>();
        for (Alerte a : alertes) {
            alerteMessage.computeIfAbsent(a.getMessage().getIdMessage(), k -> new ArrayList<>()).add(a);
        }

        // Associer chaque message avec ses historiques de statut
        Map<Integer, List<HistoriqueMessageStatus>> statusMessage = new HashMap<>();
        for (HistoriqueMessageStatus s : status) {
            statusMessage.computeIfAbsent(s.getMessage().getIdMessage(), k -> new ArrayList<>()).add(s);
        }

        // Extraire uniquement le dernier statut par message
        Map<Integer, HistoriqueMessageStatus> dernierStatusMessage = new HashMap<>();
        for (Map.Entry<Integer, List<HistoriqueMessageStatus>> entry : statusMessage.entrySet()) {
            int messageId = entry.getKey();
            List<HistoriqueMessageStatus> historiques = entry.getValue();

            HistoriqueMessageStatus dernier = historiques.stream()
                    .max(Comparator.comparing(HistoriqueMessageStatus::getDateChangement))
                    .orElse(null);

            if (dernier != null) {
                dernierStatusMessage.put(messageId, dernier);
            }
        }

        // Passer les données au modèle
        model.addAttribute("user", currentUser);
        model.addAttribute("messages", messages);
        model.addAttribute("alerteMessage", alerteMessage);
        model.addAttribute("statusMessage", dernierStatusMessage);
        model.addAttribute("sites", sites);
        model.addAttribute("interventions", interventions);
        model.addAttribute("evenements", evenements);
        model.addAttribute("directions", new String[]{"N", "NE", "E", "SE", "S", "SO", "O", "NO"});

        return "historique";
    }

    @PostMapping("/history/messages/{id}")
    @ResponseBody
    public Map<String, Object> updateMessage(@PathVariable("id") int messageId,
                                             @RequestBody Map<String, Object> payload,
                                             HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        Map<String, Object> response = new HashMap<>();
        
        if (!historiqueMessageEditService.isAdmin(currentUser)) {
            response.put("success", false);
            response.put("message", "Action reservee a l administrateur");
            return response;
        }

        boolean updated = historiqueMessageEditService.updateMessageAsAdmin(currentUser, messageId, payload);
        response.put("success", updated);
        response.put("message", updated ? "Message modifie" : "Message introuvable");
        return response;
    }

    @GetMapping("/history/export")
    public ResponseEntity<byte[]> exportMessages(@RequestParam(value = "format", defaultValue = "csv") String format,
                                                 @RequestParam(value = "year", required = false) String year,
                                                 @RequestParam(value = "month", required = false) String month,
                                                 @RequestParam(value = "alerte", required = false) String alerte,
                                                 @RequestParam(value = "site", required = false) String site,
                                                 @RequestParam(value = "status", required = false) String status) {
        List<Message> messages = filterMessagesForExport(year, month, alerte, site, status);
        byte[] payload = messageExportService.exportMessages(messages, format);

        String normalizedFormat = format == null ? "csv" : format.trim().toLowerCase();
        String extension = switch (normalizedFormat) {
            case "xlsx" -> "xlsx";
            case "pdf" -> "pdf";
            default -> "csv";
        };

        String filename = "historique_filtre_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "." + extension;
        String contentType = switch (extension) {
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "pdf" -> MediaType.APPLICATION_PDF_VALUE;
            default -> "text/csv; charset=UTF-8";
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(payload);
    }

    @PostMapping("/history/import")
    @ResponseBody
    public Map<String, Object> importMessages(@RequestParam("file") MultipartFile file,
                                              HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        Map<String, Object> response = new HashMap<>();

        // Vérifier que c'est un admin
        if (!historiqueMessageEditService.isAdmin(currentUser)) {
            response.put("success", false);
            response.put("message", "Action reservee a l administrateur");
            return response;
        }

        try {
            String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
            if (!originalFilename.endsWith(".csv") && !originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls")) {
                response.put("success", false);
                response.put("message", "Seuls les fichiers CSV ou XLSX sont acceptes");
                return response;
            }

            MessageImportService.ImportResult result = messageImportService.parseAndValidateFile(file.getOriginalFilename(), file.getBytes());

            if (result.hasErrors()) {
                response.put("success", false);
                response.put("message", "Le fichier contient des erreurs");
                response.put("validCount", result.getValidCount());
                response.put("errorCount", result.getErrorCount());
                
                List<String> errorMessages = new ArrayList<>();
                for (MessageImportService.ValidationErrorMessage err : result.getErrors()) {
                    errorMessages.add(err.toString());
                }
                response.put("errors", errorMessages);
                response.put("separator", "tabulation (\t)");
                response.put("expectedColumns", List.of(
                        "date_commencement",
                        "date_signalement",
                        "site",
                        "agent",
                        "intervention",
                        "evenement",
                        "point_repere",
                        "surface_m2",
                        "description",
                        "direction",
                        "renfort",
                        "longitude",
                        "latitude"
                ));
                return response;
            }

            // Sauvegarder les messages valides
            int importedCount = 0;
            for (Message msg : result.getValidMessages()) {
                try {
                    messageService.saveMessage(msg);
                    importedCount++;
                } catch (Exception e) {
                    // Continuer même si un message échoue
                }
            }

            response.put("success", true);
            response.put("message", importedCount + " message(s) importé(s) avec succès");
            response.put("importedCount", importedCount);
            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de l'import: " + e.getMessage());
            return response;
        }
    }

    private List<Message> filterMessagesForExport(String year,
                                                  String month,
                                                  String alerte,
                                                  String site,
                                                  String status) {
        List<Message> messages = messageService.findAll();
        List<Alerte> alertes = alerteService.findAll();
        List<HistoriqueMessageStatus> historiques = historiqueMessageStatusService.findAll();

        Map<Integer, List<Alerte>> alerteMessage = new HashMap<>();
        for (Alerte a : alertes) {
            alerteMessage.computeIfAbsent(a.getMessage().getIdMessage(), key -> new ArrayList<>()).add(a);
        }

        Map<Integer, List<HistoriqueMessageStatus>> statusMessage = new HashMap<>();
        for (HistoriqueMessageStatus s : historiques) {
            statusMessage.computeIfAbsent(s.getMessage().getIdMessage(), key -> new ArrayList<>()).add(s);
        }

        Map<Integer, HistoriqueMessageStatus> dernierStatusMessage = new HashMap<>();
        for (Map.Entry<Integer, List<HistoriqueMessageStatus>> entry : statusMessage.entrySet()) {
            HistoriqueMessageStatus dernier = entry.getValue().stream()
                    .max(Comparator.comparing(HistoriqueMessageStatus::getDateChangement))
                    .orElse(null);
            if (dernier != null) {
                dernierStatusMessage.put(entry.getKey(), dernier);
            }
        }

        List<Message> filteredMessages = new ArrayList<>();
        for (Message message : messages) {
            if (matchesExportFilters(message, year, month, alerte, site, status, alerteMessage, dernierStatusMessage)) {
                filteredMessages.add(message);
            }
        }
        return filteredMessages;
    }

    private boolean matchesExportFilters(Message message,
                                         String year,
                                         String month,
                                         String alerte,
                                         String site,
                                         String status,
                                         Map<Integer, List<Alerte>> alerteMessage,
                                         Map<Integer, HistoriqueMessageStatus> dernierStatusMessage) {
        String messageYear = message.getDateCommencement() != null ? message.getDateCommencement().format(DateTimeFormatter.ofPattern("yyyy")) : "";
        String messageMonth = message.getDateCommencement() != null ? message.getDateCommencement().format(DateTimeFormatter.ofPattern("MM")) : "";
        String messageAlerte = "";
        List<Alerte> messageAlertes = alerteMessage.get(message.getIdMessage());
        if (messageAlertes != null && !messageAlertes.isEmpty() && messageAlertes.get(0).getTypeAlerte() != null) {
            messageAlerte = messageAlertes.get(0).getTypeAlerte().getZone();
        }
        String messageSite = message.getUserApp() != null && message.getUserApp().getPatrouilleur() != null && message.getUserApp().getPatrouilleur().getSite() != null
                ? message.getUserApp().getPatrouilleur().getSite().getNom()
                : "";
        String messageStatus = dernierStatusMessage.get(message.getIdMessage()) != null && dernierStatusMessage.get(message.getIdMessage()).getStatus() != null
                ? dernierStatusMessage.get(message.getIdMessage()).getStatus().getStatus()
                : "";

        if (year != null && !year.isBlank() && !messageYear.equals(year)) {
            return false;
        }
        if (month != null && !month.isBlank() && !messageMonth.equals(month)) {
            return false;
        }
        if (alerte != null && !alerte.isBlank() && !messageAlerte.equalsIgnoreCase(alerte)) {
            return false;
        }
        if (site != null && !site.isBlank() && !messageSite.equalsIgnoreCase(site)) {
            return false;
        }
        if (status != null && !status.isBlank() && !messageStatus.equalsIgnoreCase(status)) {
            return false;
        }
        return true;
    }

}

