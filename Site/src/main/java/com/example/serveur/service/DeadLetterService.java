package com.example.serveur.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Records messages that failed to parse/process (malformed alerts, status
 * updates, etc.) so they can be triaged later instead of being silently
 * dropped with just a "Format invalide" response sent back to the sender.
 *
 * Deliberately simple: appends structured lines to a dedicated log file
 * (same pattern already used by SmsLoggingService), not a new DB table -
 * enough to triage without adding schema/migration overhead for what is, by
 * definition, abnormal/unexpected input.
 */
@Service
public class DeadLetterService {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterService.class);

    private final String logFile;

    public DeadLetterService(@Value("${deadletter.logfile.path}") String logFile) {
        this.logFile = logFile;
    }

    public void record(String source, String phoneNumber, String reason, String rawContent) {
        String logLine = String.format("[%s] source=%s phone=%s reason=%s raw=%s%n",
                LocalDateTime.now(), source, phoneNumber, reason, rawContent);

        log.warn("Dead-letter: source={} phone={} reason={}", source, phoneNumber, reason);

        try (FileWriter fw = new FileWriter(logFile, true)) {
            fw.write(logLine);
        } catch (IOException e) {
            log.error("Impossible d'écrire dans le fichier dead-letter ({}): {}", logFile, e.getMessage());
        }
    }
}
