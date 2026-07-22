package com.example.theperegrinefund;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    /**
     * Endpoint générique côté serveur (ServeurController#handleAnyMessage,
     * mappé sur /api/message) qui accepte {phoneNumber, message} et route le
     * contenu chiffré vers le même pipeline de traitement que les SMS
     * (login / alerte / mise à jour de statut).
     *
     * NOTE: la version précédente pointait vers "/api/send", qui n'existe
     * pas côté serveur (bug corrigé - tout envoi direct HTTP échouait donc
     * systématiquement en 404 et retombait toujours sur le SMS).
     *
     * Le corps de la réponse est lu en brut (ResponseBody) plutôt que
     * désérialisé directement, pour permettre à l'appelant de vérifier que
     * la réponse est bien du JSON provenant du serveur avant de considérer
     * l'envoi comme réussi (protection contre les pages HTML intermédiaires
     * d'ngrok/proxy renvoyées avec un code 200).
     */
    @POST("/api/message")
    Call<ResponseBody> sendEncryptedMessage(@Body DirectMessageRequest request);

    /** Corps JSON attendu par /api/message côté serveur. */
    class DirectMessageRequest {
        private final String phoneNumber;
        private final String message;

        public DirectMessageRequest(String phoneNumber, String message) {
            this.phoneNumber = phoneNumber;
            this.message = message;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getMessage() {
            return message;
        }
    }
}
