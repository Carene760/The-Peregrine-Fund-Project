package com.example.serveur;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication  // Annotation qui configure automatiquement Spring Boot
@EnableScheduling
@EnableAsync
public class ServeurApplication {

    /**
     * RestTemplate partagé (utilisé notamment par SmsResponseService pour
     * relayer les réponses/notifications via la passerelle SMSSync,
     * gateway.internal-send-url).
     *
     * ROBUSTESSE: `new RestTemplate()` par défaut n'a AUCUN timeout de
     * connexion/lecture - si la passerelle SMS est injoignable (éteinte,
     * hors réseau, mauvaise IP), l'appel bloque le thread de la requête en
     * cours indéfiniment (observé lors d'un test de charge: /api/message
     * restait bloqué plusieurs dizaines de secondes sans réponse alors que
     * l'alerte était déjà enregistrée en base). Des timeouts courts bornent
     * l'échec au lieu de le laisser indéfini. On passe par
     * SimpleClientHttpRequestFactory (API stable depuis longtemps, en
     * millisecondes) plutôt que RestTemplateBuilder.connectTimeout(Duration)
     * pour éviter toute dépendance à une version précise de Spring Boot.
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }

    public static void main(String[] args) {
        SpringApplication.run(ServeurApplication.class, args);  // Démarre le serveur
    }
}
