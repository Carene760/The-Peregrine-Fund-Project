package com.example.serveur.service;

import org.springframework.stereotype.Service;
import com.example.serveur.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

// Import SendGrid
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.io.IOException;

@Service
public class EmailService {

    private final SendGrid sendGrid;
    
    @Value("${sendgrid.from.email}")
    private String fromEmail;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    public EmailService(SendGrid sendGrid) {
        this.sendGrid = sendGrid;
    }

    public void envoyerAlerte(String emailDestinataire, String zoneAlerte) {
        String objet;
        String corps;

        switch (zoneAlerte.toLowerCase()) {
            case "vert":
                objet = "Alerte Catastrophe - Zone Verte";
                corps = "Situation normale. Aucun danger détecté.";
                break;
            case "jaune":
                objet = "Alerte Catastrophe - Zone Jaune";
                corps = "Risque faible. Restez attentif aux consignes.";
                break;
            case "orange":
                objet = "Alerte Catastrophe - Zone Orange";
                corps = "Risque élevé. Préparez-vous à agir en cas de besoin.";
                break;
            case "rouge":
                objet = "Alerte Catastrophe - Zone Rouge";
                corps = "Danger critique ! Prenez immédiatement toutes les mesures de sécurité.";
                break;
            default:
                objet = "Alerte Catastrophe - Zone inconnue";
                corps = "La zone d'alerte fournie n'est pas valide (utiliser : vert, jaune, orange, rouge).";
        }

        // Utilisation de SendGrid au lieu de JavaMailSender
        envoyerAvecSendGrid(emailDestinataire, objet, corps);
    }

    private void envoyerAvecSendGrid(String toEmail, String subject, String content) {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content emailContent = new Content("text/plain", content);
        Mail mail = new Mail(from, subject, to, emailContent);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            // Log du résultat
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ Email envoyé avec succès à: " + toEmail);
            } else {
                System.err.println("❌ Erreur SendGrid (" + response.getStatusCode() + "): " + response.getBody());
            }

        } catch (IOException ex) {
            System.err.println("❌ Erreur d'envoi SendGrid à " + toEmail + ": " + ex.getMessage());
            throw new RuntimeException("Erreur d'envoi d'email", ex);
        }
    }

    public void envoyerAlertesPourZone(String zoneAlerte) {
        List<String> emails = userRepository.findEmailsByZoneAlerte(zoneAlerte);
        
        if (emails == null || emails.isEmpty()) {
            System.out.println("⚠️ Aucun utilisateur trouvé pour la zone : " + zoneAlerte);
            return;
        }

        System.out.println("📧 Envoi d'alertes pour la zone " + zoneAlerte + " à " + emails.size() + " utilisateurs");

        for (String email : emails) {
            try {
                envoyerAlerte(email, zoneAlerte);
                // Petite pause pour éviter le rate limiting
                Thread.sleep(100);
            } catch (Exception e) {
                System.err.println("❌ Impossible d'envoyer l'alerte à " + email + " : " + e.getMessage());
            }
        }
    }

    // Méthode optionnelle pour envoyer des emails HTML
    public void envoyerAlerteHtml(String emailDestinataire, String zoneAlerte) {
        String objet;
        String htmlCorps;

        switch (zoneAlerte.toLowerCase()) {
            case "vert":
                objet = "Alerte Catastrophe - Zone Verte";
                htmlCorps = createHtmlTemplate("Zone Verte", "Situation normale. Aucun danger détecté.", "green");
                break;
            case "jaune":
                objet = "Alerte Catastrophe - Zone Jaune";
                htmlCorps = createHtmlTemplate("Zone Jaune", "Risque faible. Restez attentif aux consignes.", "yellow");
                break;
            case "orange":
                objet = "Alerte Catastrophe - Zone Orange";
                htmlCorps = createHtmlTemplate("Zone Orange", "Risque élevé. Préparez-vous à agir en cas de besoin.", "orange");
                break;
            case "rouge":
                objet = "Alerte Catastrophe - Zone Rouge";
                htmlCorps = createHtmlTemplate("Zone Rouge", "Danger critique ! Prenez immédiatement toutes les mesures de sécurité.", "red");
                break;
            default:
                objet = "Alerte Catastrophe - Zone inconnue";
                htmlCorps = createHtmlTemplate("Zone Inconnue", "La zone d'alerte fournie n'est pas valide.", "gray");
        }

        envoyerHtmlAvecSendGrid(emailDestinataire, objet, htmlCorps);
    }

    private void envoyerHtmlAvecSendGrid(String toEmail, String subject, String htmlContent) {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ Email HTML envoyé avec succès à: " + toEmail);
            } else {
                System.err.println("❌ Erreur SendGrid HTML (" + response.getStatusCode() + "): " + response.getBody());
            }

        } catch (IOException ex) {
            System.err.println("❌ Erreur d'envoi HTML SendGrid à " + toEmail + ": " + ex.getMessage());
        }
    }

    private String createHtmlTemplate(String titre, String message, String couleur) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 20px;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .header {
                        background-color: %s;
                        color: white;
                        padding: 20px;
                        text-align: center;
                    }
                    .content {
                        padding: 30px;
                        line-height: 1.6;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        font-size: 12px;
                        color: #666;
                    }
                    .urgence {
                        background-color: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px;
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        <p>%s</p>
                        <div class="urgence">
                            <strong>Information importante:</strong><br>
                            Cette alerte a été générée automatiquement par le système de surveillance.
                        </div>
                    </div>
                    <div class="footer">
                        © 2024 Système d'Alerte Catastrophe. Tous droits réservés.
                    </div>
                </div>
            </body>
            </html>
            """, getColorCode(couleur), titre, message);
    }

    private String getColorCode(String couleur) {
        switch (couleur.toLowerCase()) {
            case "green": return "#28a745";
            case "yellow": return "#ffc107";
            case "orange": return "#fd7e14";
            case "red": return "#dc3545";
            default: return "#6c757d";
        }
    }
}