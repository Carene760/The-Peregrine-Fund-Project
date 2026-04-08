package com.example.serveur.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class SmsLoggingService {
    
    private final String logFile;

    public SmsLoggingService(@Value("${sms.logfile.path}") String logFile) {
        this.logFile = logFile;
    }

    public void logSms(String from, String message, String timestamp) {
        String logLine = String.format("[%s] %s: %s\n",
                timestamp != null ? timestamp : LocalDateTime.now(),
                from,
                message);

        try (FileWriter fw = new FileWriter(logFile, true)) {
            fw.write(logLine);
        } catch (IOException e) {
            System.err.println("❌ Erreur écriture log: " + e.getMessage());
        }
    }
}