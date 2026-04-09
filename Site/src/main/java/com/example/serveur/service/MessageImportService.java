package com.example.serveur.service;

import com.example.serveur.model.Evenement;
import com.example.serveur.model.Intervention;
import com.example.serveur.model.Message;
import com.example.serveur.model.Patrouilleurs;
import com.example.serveur.model.UserApp;
import com.example.serveur.repository.EvenementRepository;
import com.example.serveur.repository.InterventionRepository;
import com.example.serveur.repository.PatrouilleursRepository;
import com.example.serveur.repository.UserAppRepository;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MessageImportService {

    private static final String TSV_SEPARATOR = "\t";
    private static final List<String> REQUIRED_HEADERS = List.of(
            "date_commencement",
            "date_signalement",
            "site",
            "agent",
            "intervention",
            "direction"
    );
    private static final List<String> EXPECTED_HEADERS = List.of(
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
    );
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withResolverStyle(ResolverStyle.SMART),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").withResolverStyle(ResolverStyle.SMART)
    );

    private final MessageService messageService;
    private final InterventionRepository interventionRepository;
    private final EvenementRepository evenementRepository;
    private final PatrouilleursRepository patrouilleursRepository;
    private final UserAppRepository userAppRepository;

    public MessageImportService(MessageService messageService,
                                InterventionRepository interventionRepository,
                                EvenementRepository evenementRepository,
                                PatrouilleursRepository patrouilleursRepository,
                                UserAppRepository userAppRepository) {
        this.messageService = messageService;
        this.interventionRepository = interventionRepository;
        this.evenementRepository = evenementRepository;
        this.patrouilleursRepository = patrouilleursRepository;
        this.userAppRepository = userAppRepository;
    }

    public ImportResult parseAndValidateCsv(String csvContent) {
        List<List<String>> rows = new ArrayList<>();
        String normalized = csvContent.replace("\r\n", "\n").replace('\r', '\n');
        for (String line : normalized.split("\n", -1)) {
            rows.add(parseDelimitedLine(line, TSV_SEPARATOR.charAt(0)));
        }
        return parseRows(rows, "CSV");
    }

    public ImportResult parseAndValidateXlsx(byte[] xlsxContent) {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(xlsxContent))) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();
            List<List<String>> rows = new ArrayList<>();

            for (Row row : sheet) {
                List<String> values = new ArrayList<>();
                int lastCellNum = Math.max(row.getLastCellNum(), 0);
                for (int columnIndex = 0; columnIndex < lastCellNum; columnIndex++) {
                    values.add(dataFormatter.formatCellValue(row.getCell(columnIndex)));
                }
                rows.add(values);
            }

            return parseRows(rows, "XLSX");
        } catch (Exception e) {
            ImportResult result = new ImportResult();
            result.addError(new ValidationErrorMessage(1, "Fichier", "Impossible de lire le fichier XLSX", e.getMessage()));
            return result;
        }
    }

    public ImportResult parseAndValidateFile(String fileName, byte[] content) {
        String normalizedName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (normalizedName.endsWith(".xlsx") || normalizedName.endsWith(".xls")) {
            return parseAndValidateXlsx(content);
        }
        return parseAndValidateCsv(new String(content, StandardCharsets.UTF_8));
    }

    public ImportResult parseAndValidateCsvLines(List<List<String>> rows) {
        return parseRows(rows, "CSV");
    }

    private ImportResult parseRows(List<List<String>> rows, String sourceLabel) {
        ImportResult result = new ImportResult();
        if (rows.isEmpty()) {
            result.addError(new ValidationErrorMessage(1, "Fichier", "Le fichier est vide", null));
            return result;
        }

        Map<String, Integer> headerIndex = buildHeaderIndex(rows.get(0));
        validateHeaders(headerIndex, result);
        if (result.hasErrors()) {
            return result;
        }

        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            List<String> values = rows.get(rowIndex);
            if (values.stream().allMatch(value -> value == null || value.trim().isEmpty())) {
                continue;
            }

            MessageParseResult parseResult = parseRow(values, headerIndex, rowIndex + 1, sourceLabel);
            result.errors.addAll(parseResult.errors);
            if (parseResult.message != null) {
                result.validMessages.add(parseResult.message);
            }
        }

        return result;
    }

    private void validateHeaders(Map<String, Integer> headerIndex, ImportResult result) {
        for (String requiredHeader : REQUIRED_HEADERS) {
            if (!headerIndex.containsKey(requiredHeader)) {
                result.addError(new ValidationErrorMessage(1, requiredHeader, "Colonne obligatoire manquante", null));
            }
        }
    }

    private MessageParseResult parseRow(List<String> values, Map<String, Integer> headerIndex, int lineNumber, String sourceLabel) {
        MessageParseResult result = new MessageParseResult();
        Message message = new Message();
        List<ValidationErrorMessage> errors = new ArrayList<>();

        String dateCommencementText = getValue(values, headerIndex, "date_commencement");
        LocalDateTime dateCommencement = parseDateTime(dateCommencementText);
        if (dateCommencement == null) {
            errors.add(new ValidationErrorMessage(lineNumber, "date_commencement", "Format de date invalide", dateCommencementText));
        } else {
            message.setDateCommencement(dateCommencement);
        }

        String dateSignalementText = getValue(values, headerIndex, "date_signalement");
        LocalDateTime dateSignalement = parseDateTime(dateSignalementText);
        if (dateSignalement == null) {
            errors.add(new ValidationErrorMessage(lineNumber, "date_signalement", "Format de date invalide", dateSignalementText));
        } else {
            message.setDateSignalement(dateSignalement);
        }

        String direction = trimToNull(getValue(values, headerIndex, "direction"));
        if (direction == null) {
            errors.add(new ValidationErrorMessage(lineNumber, "direction", "Champ obligatoire manquant", null));
        } else {
            message.setDirection(direction);
        }

        String siteName = trimToNull(firstNonBlank(
                getValue(values, headerIndex, "site"),
                getValue(values, headerIndex, "site_nom"),
                getValue(values, headerIndex, "nom_site")
        ));
        String agentName = trimToNull(firstNonBlank(
                getValue(values, headerIndex, "agent"),
                getValue(values, headerIndex, "user_app_nom"),
                getValue(values, headerIndex, "patrouilleur")
        ));

        if (siteName == null) {
            errors.add(new ValidationErrorMessage(lineNumber, "site", "Champ obligatoire manquant", null));
        }
        if (agentName == null) {
            errors.add(new ValidationErrorMessage(lineNumber, "agent", "Champ obligatoire manquant", null));
        }

        if (siteName != null && agentName != null) {
            Optional<Patrouilleurs> patrouilleurOpt = patrouilleursRepository.findFirstByNomAndSiteNomIgnoreCase(agentName, siteName);
            if (patrouilleurOpt.isEmpty()) {
                errors.add(new ValidationErrorMessage(lineNumber, "agent", "Agent introuvable pour ce site", agentName + " / " + siteName));
            } else {
                Optional<UserApp> userAppOpt = userAppRepository.findFirstByPatrouilleur(patrouilleurOpt.get());
                if (userAppOpt.isEmpty()) {
                    errors.add(new ValidationErrorMessage(lineNumber, "agent", "Compte mobile introuvable pour cet agent", agentName));
                } else {
                    message.setUserApp(userAppOpt.get());
                }
            }
        }

        String interventionName = trimToNull(firstNonBlank(
                getValue(values, headerIndex, "intervention"),
                getValue(values, headerIndex, "intervention_nom")
        ));
        if (interventionName == null) {
            errors.add(new ValidationErrorMessage(lineNumber, "intervention", "Champ obligatoire manquant", null));
        } else {
            Intervention intervention = interventionRepository.findFirstByInterventionIgnoreCase(interventionName);
            if (intervention == null) {
                errors.add(new ValidationErrorMessage(lineNumber, "intervention", "Intervention introuvable", interventionName));
            } else {
                message.setIntervention(intervention);
            }
        }

        String evenementName = trimToNull(firstNonBlank(
                getValue(values, headerIndex, "evenement"),
                getValue(values, headerIndex, "evenement_nom")
        ));
        if (evenementName != null) {
            Evenement evenement = evenementRepository.findFirstByNomIgnoreCase(evenementName).orElse(null);
            if (evenement == null) {
                errors.add(new ValidationErrorMessage(lineNumber, "evenement", "Événement introuvable", evenementName));
            } else {
                message.setEvenement(evenement);
            }
        }

        message.setPointRepere(trimToNull(getValue(values, headerIndex, "point_repere")));
        message.setDescription(trimToNull(getValue(values, headerIndex, "description")));

        String surfaceText = trimToNull(firstNonBlank(
                getValue(values, headerIndex, "surface_m2"),
                getValue(values, headerIndex, "surface_approximative")
        ));
        if (surfaceText != null) {
            Double surface = parseDouble(surfaceText);
            if (surface == null) {
                errors.add(new ValidationErrorMessage(lineNumber, "surface_m2", "Valeur numérique invalide", surfaceText));
            } else {
                message.setSurfaceApproximative(surface);
            }
        }

        String renfortText = trimToNull(getValue(values, headerIndex, "renfort"));
        if (renfortText != null) {
            Boolean renfort = parseBoolean(renfortText);
            if (renfort == null) {
                errors.add(new ValidationErrorMessage(lineNumber, "renfort", "Valeur attendue: oui/non, true/false ou 1/0", renfortText));
            } else {
                message.setRenfort(renfort);
            }
        }

        String longitudeText = trimToNull(getValue(values, headerIndex, "longitude"));
        if (longitudeText != null) {
            Double longitude = parseDouble(longitudeText);
            if (longitude == null) {
                errors.add(new ValidationErrorMessage(lineNumber, "longitude", "Valeur numérique invalide", longitudeText));
            } else {
                message.setLongitude(longitude);
            }
        }

        String latitudeText = trimToNull(getValue(values, headerIndex, "latitude"));
        if (latitudeText != null) {
            Double latitude = parseDouble(latitudeText);
            if (latitude == null) {
                errors.add(new ValidationErrorMessage(lineNumber, "latitude", "Valeur numérique invalide", latitudeText));
            } else {
                message.setLatitude(latitude);
            }
        }

        if (!errors.isEmpty()) {
            result.errors.addAll(errors);
            return result;
        }

        result.message = message;
        return result;
    }

    private Map<String, Integer> buildHeaderIndex(List<String> headers) {
        Map<String, Integer> index = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String normalized = normalizeHeader(headers.get(i));
            if (!normalized.isEmpty()) {
                index.putIfAbsent(normalized, i);
            }
        }
        return index;
    }

    private String getValue(List<String> values, Map<String, Integer> headerIndex, String key) {
        Integer columnIndex = headerIndex.get(normalizeHeader(key));
        if (columnIndex == null || columnIndex < 0 || columnIndex >= values.size()) {
            return null;
        }
        return values.get(columnIndex);
    }

    private String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }
        String normalized = header.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replace('é', 'e').replace('è', 'e').replace('ê', 'e').replace('à', 'a');
        normalized = normalized.replace('ô', 'o').replace('ù', 'u').replace('î', 'i').replace('ï', 'i');
        normalized = normalized.replaceAll("[^a-z0-9_]+", "_");
        normalized = normalized.replaceAll("_+", "_");
        return normalized.replaceAll("^_|_$", "");
    }

    private List<String> parseDelimitedLine(String line, char separator) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);
            if (currentChar == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (currentChar == separator && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(currentChar);
            }
        }

        values.add(current.toString());
        return values;
    }

    private LocalDateTime parseDateTime(String value) {
        String text = trimToNull(value);
        if (text == null) {
            return null;
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDateTime.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // essayer le format suivant
            }
        }
        return null;
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean parseBoolean(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (Arrays.asList("true", "1", "oui", "yes", "y").contains(normalized)) {
            return true;
        }
        if (Arrays.asList("false", "0", "non", "no", "n").contains(normalized)) {
            return false;
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    public static class ImportResult {
        private final List<Message> validMessages = new ArrayList<>();
        private final List<ValidationErrorMessage> errors = new ArrayList<>();

        public List<Message> getValidMessages() {
            return validMessages;
        }

        public List<ValidationErrorMessage> getErrors() {
            return errors;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public int getValidCount() {
            return validMessages.size();
        }

        public int getErrorCount() {
            return errors.size();
        }

        public void addError(ValidationErrorMessage error) {
            errors.add(error);
        }
    }

    private static class MessageParseResult {
        private Message message;
        private List<ValidationErrorMessage> errors = new ArrayList<>();
    }

    public static class ValidationErrorMessage {
        private final int line;
        private final String column;
        private final String message;
        private final String value;

        public ValidationErrorMessage(int line, String column, String message, String value) {
            this.line = line;
            this.column = column;
            this.message = message;
            this.value = value;
        }

        public int getLine() {
            return line;
        }

        public String getColumn() {
            return column;
        }

        public String getMessage() {
            return message;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            if (value == null || value.isBlank()) {
                return "Ligne " + line + ", colonne " + column + " : " + message;
            }
            return "Ligne " + line + ", colonne " + column + " : " + message + " (valeur: " + value + ")";
        }
    }
}
