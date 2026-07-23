package com.example.serveur.service;

import org.springframework.stereotype.Service;
import com.example.serveur.model.Message;
import com.example.serveur.repository.UserRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Gmail exige que l'adresse "From" corresponde au compte authentifié
    // (spring.mail.username) - pas de "from" arbitraire comme avec une
    // vraie API transactionnelle (SendGrid, Brevo...).
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void envoyerAlerte(String emailDestinataire, String zoneAlerte, Message message) {
        String objet = String.format("%s Alerte incendie - Niveau %s", picto(zoneAlerte), libelleZone(zoneAlerte));
        String corps = construireCorpsAlerte(zoneAlerte, message);
        envoyerAvecSendGrid(emailDestinataire, objet, corps);
    }

    private String picto(String zoneAlerte) {
        if (zoneAlerte == null) return "⚪";
        switch (zoneAlerte.toLowerCase()) {
            case "vert": return "🟢";
            case "jaune": return "🟡";
            case "orange": return "🟠";
            case "rouge": return "🔴";
            default: return "⚪";
        }
    }

    private String libelleZone(String zoneAlerte) {
        if (zoneAlerte == null) return "Inconnu";
        switch (zoneAlerte.toLowerCase()) {
            case "vert": return "Vert - Situation normale";
            case "jaune": return "Jaune - Risque faible, restez attentif";
            case "orange": return "Orange - Risque élevé, préparez-vous à agir";
            case "rouge": return "Rouge - Danger critique, action immédiate requise";
            default: return "Inconnu";
        }
    }

    /**
     * Corps de l'email d'alerte: contrairement à l'ancien texte générique
     * par zone (qui ne disait rien de l'incendie lui-même), reprend les
     * informations réelles du message signalé pour que le destinataire
     * sache tout de suite où, quoi, et depuis quand - sans avoir à se
     * connecter au tableau de bord pour le découvrir.
     */
    private String construireCorpsAlerte(String zoneAlerte, Message message) {
        if (message == null) {
            return String.format("%s Alerte de niveau %s signalée. Consultez le tableau de bord pour le détail.",
                    picto(zoneAlerte), libelleZone(zoneAlerte));
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format("""
            %s ALERTE INCENDIE - Niveau %s

            Signalé le : %s
            Type d'intervention : %s
            Renfort demandé : %s
            Point de repère : %s
            Direction : %s
            Surface estimée : %s
            Localisation GPS : %s, %s

            Description :
            %s

            Merci de vous coordonner avec l'équipe de terrain pour la suite des opérations.
            """,
            picto(zoneAlerte),
            libelleZone(zoneAlerte),
            message.getDateSignalement() != null ? message.getDateSignalement().format(formatter) : "Non renseigné",
            message.getIntervention() != null ? message.getIntervention().getIntervention() : "Non renseigné",
            Boolean.TRUE.equals(message.getRenfort()) ? "Oui" : "Non",
            message.getPointRepere() != null && !message.getPointRepere().isBlank() ? message.getPointRepere() : "Non renseigné",
            message.getDirection() != null && !message.getDirection().isBlank() ? message.getDirection() : "Non renseignée",
            message.getSurfaceApproximative() != null ? message.getSurfaceApproximative() + " m²" : "Non renseignée",
            message.getLatitude() != null ? message.getLatitude() : "0",
            message.getLongitude() != null ? message.getLongitude() : "0",
            message.getDescription() != null && !message.getDescription().isBlank() ? message.getDescription() : "Aucune description fournie"
        );
    }

    private void envoyerAvecSendGrid(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromEmail);
            mail.setTo(toEmail);
            mail.setSubject(subject);
            mail.setText(content);
            mailSender.send(mail);
            System.out.println("✅ Email envoyé avec succès à: " + toEmail);
        } catch (Exception ex) {
            System.err.println("❌ Erreur d'envoi email à " + toEmail + ": " + ex.getMessage());
            throw new RuntimeException("Erreur d'envoi d'email", ex);
        }
    }

    /**
     * @Async: appelée depuis AlerteService.processAlerte() juste après
     * l'enregistrement en base de l'alerte - la notification email est une
     * fonctionnalité annexe et ne doit jamais retarder/bloquer la réponse
     * HTTP renvoyée au ranger. Avant ce correctif, un appel SendGrid lent ou
     * bloqué (ex: pas d'accès internet sortant sur le terrain) faisait
     * indéfiniment attendre toute la requête d'envoi d'alerte, alors que
     * l'alerte elle-même était déjà enregistrée en base (voir test de charge:
     * requêtes /api/message restées bloquées ~15s+ sans réponse).
     */
    @Async
    public void envoyerAlertesPourZone(String zoneAlerte, Message message) {
        List<String> emails = userRepository.findEmailsByZoneAlerte(zoneAlerte);

        if (emails == null || emails.isEmpty()) {
            System.out.println("⚠️ Aucun utilisateur trouvé pour la zone : " + zoneAlerte);
            return;
        }

        System.out.println("📧 Envoi d'alertes pour la zone " + zoneAlerte + " à " + emails.size() + " utilisateurs");

        for (String email : emails) {
            try {
                envoyerAlerte(email, zoneAlerte, message);
                // Petite pause pour éviter le rate limiting
                Thread.sleep(100);
            } catch (Exception e) {
                System.err.println("❌ Impossible d'envoyer l'alerte à " + email + " : " + e.getMessage());
            }
        }
    }

}