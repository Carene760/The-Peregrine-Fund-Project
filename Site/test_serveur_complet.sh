#!/bin/bash

# =============================================================================
# CONFIGURATION
# =============================================================================
PORT=8080
BASE_URL="http://localhost:$PORT/api"

# Numéros de test (remplacez par ceux de votre base de données)
AUTHORIZED_PHONE_1="+261349322431"  # Responsable Tsimembo
AUTHORIZED_PHONE_2="+261383817421"  # Agent Mandrozo
AUTHORIZED_PHONE_3="+261382305086"  # Agent Bemanevika
UNAUTHORIZED_PHONE="+33123456789"   # Numéro non autorisé

# Fichiers de log
LOG_FILE="test_results_$(date +%Y%m%d_%H%M%S).log"
DEBUG_FILE="debug_messages.log"

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# =============================================================================
# FONCTIONS UTILITAIRES
# =============================================================================

log() {
    echo -e "$1" | tee -a "$LOG_FILE"
}

debug() {
    echo -e "$1" >> "$DEBUG_FILE"
}

# Fonction pour tester un endpoint
test_endpoint() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local expected_http_code="$5"
    
    log "${BLUE}🧪 Testing $name...${NC}"
    log "URL: $url"
    
    if [ -n "$data" ]; then
        log "Data: $data"
    fi
    
    local response
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\nHTTP_CODE:%{http_code}" "$url")
    else
        response=$(curl -s -X "$method" -H "Content-Type: application/json" -d "$data" -w "\nHTTP_CODE:%{http_code}" "$url")
    fi
    
    # Extraire le code HTTP et le corps de la réponse
    local http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d':' -f2)
    local body=$(echo "$response" | sed '/HTTP_CODE:/d')
    
    log "Response HTTP Code: $http_code"
    log "Response Body: $body"
    
    if [ "$http_code" = "$expected_http_code" ]; then
        log "${GREEN}✅ SUCCESS: $name returned HTTP $http_code${NC}\n"
        return 0
    else
        log "${RED}❌ FAILURE: $name expected HTTP $expected_http_code but got $http_code${NC}\n"
        return 1
    fi
}

# Fonction pour attendre que le serveur soit prêt
wait_for_server() {
    log "${YELLOW}⏳ Attente du démarrage du serveur...${NC}"
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "http://localhost:$PORT/actuator/health" > /dev/null; then
            log "${GREEN}✅ Serveur démarré!${NC}"
            return 0
        fi
        log "Tentative $attempt/$max_attempts..."
        sleep 2
        ((attempt++))
    done
    
    log "${RED}❌ Le serveur n'a pas démarré dans le temps imparti${NC}"
    return 1
}

# =============================================================================
# DÉMARRAGE DU SERVEUR (OPTIONNEL)
# =============================================================================

start_server() {
    log "${YELLOW}🚀 Démarrage du serveur Spring Boot...${NC}"
    
    # Vérifier si Maven est installé
    if ! command -v mvn &> /dev/null; then
        log "${RED}❌ Maven n'est pas installé${NC}"
        exit 1
    fi
    
    # Nettoyer et compiler
    mvn clean compile >> "$DEBUG_FILE" 2>&1
    
    # Démarrer en arrière-plan
    mvn spring-boot:run >> "$DEBUG_FILE" 2>&1 &
    SERVER_PID=$!
    
    # Attendre que le serveur démarre
    if wait_for_server; then
        log "${GREEN}✅ Serveur démarré avec PID: $SERVER_PID${NC}"
    else
        log "${RED}❌ Échec du démarrage du serveur${NC}"
        stop_server
        exit 1
    fi
}

stop_server() {
    if [ ! -z "$SERVER_PID" ]; then
        log "${YELLOW}🛑 Arrêt du serveur (PID: $SERVER_PID)...${NC}"
        kill $SERVER_PID 2>> "$DEBUG_FILE"
        sleep 3
    fi
}

# =============================================================================
# TESTS DES FONCTIONNALITÉS
# =============================================================================

run_tests() {
    log "${YELLOW}=================== DÉBUT DES TESTS ===================${NC}"
    
    # 1. Test de base - le serveur répond
    # log "${YELLOW}1. TEST DE CONNECTIVITÉ${NC}"
    # test_endpoint "Health Check" "GET" "http://localhost:$PORT/actuator/health" "" "200"
    
    # 2. Test de chiffrement
    # log "${YELLOW}2. TEST DE CHIFFREMENT${NC}"
    # test_endpoint "Test chiffrement" "GET" "$BASE_URL/test-chiffrement?message=HelloWorld" "" "200"
    
    # 3. Test de login avec numéro autorisé
    # log "${YELLOW}3. TEST DE LOGIN${NC}"
    # local login_data="{
    #   \"phoneNumber\": \"$AUTHORIZED_PHONE_3\",
    #   \"encryptedMessage\": \"jean_r/pass123\",
    #   \"sendSmsResponse\": false
    # }"
    # test_endpoint "Login autorisé" "POST" "$BASE_URL/login" "$login_data" "200"
    
    # 4. Test de login avec numéro non autorisé
    # local login_unauth_data="{
    #   \"phoneNumber\": \"$UNAUTHORIZED_PHONE\",
    #   \"encryptedMessage\": \"test/mdp\",
    #   \"sendSmsResponse\": false
    # }"
    # test_endpoint "Login non autorisé" "POST" "$BASE_URL/login" "$login_unauth_data" "200"
    
    # 5. Test d'alerte
    log "${YELLOW}4. TEST D'ALERTE${NC}"
    local alerte_data="{
      \"phoneNumber\": \"$AUTHORIZED_PHONE_2\",
      \"encryptedMessage\": \"dJ8fjzhFbYqj4M4sawPKKA==\",
      \"sendSmsResponse\": false
    }"
    test_endpoint "Envoi alerte" "POST" "$BASE_URL/webhook" "$alerte_data" "200"
    
    # 6. Test de mise à jour de statut
    # log "${YELLOW}5. TEST MISE À JOUR STATUT${NC}"
    # local status_data="{
    #   \"phoneNumber\": \"$AUTHORIZED_PHONE_3\",
    #   \"encryptedMessage\": \"2025-09-05 10:13:40/2/3\",
    #   \"sendSmsResponse\": false
    # }"
    # test_endpoint "Update status" "POST" "$BASE_URL/update-status" "$status_data" "200"
    
    # 7. Test webhook SMS (simulation)
    # log "${YELLOW}6. TEST WEBHOOK SMS${NC}"
    # local webhook_data='{
    #   "event": "sms:received",
    #   "payload": {
    #     "phoneNumber": "'$AUTHORIZED_PHONE_1'",
    #     "message": "jean_r/pass123",
    #     "receivedAt": "2023-09-09T10:00:00Z"
    #   }
    # }'
    # test_endpoint "Webhook SMS" "POST" "$BASE_URL/webhook" "$webhook_data" "200"
    
    log "${YELLOW}=================== FIN DES TESTS ===================${NC}"
}

# =============================================================================
# FONCTION PRINCIPALE
# =============================================================================

main() {
    log "${YELLOW}🧪 SCRIPT DE TEST COMPLET POUR SERVEUR SPRING BOOT${NC}"
    log "Date: $(date)"
    log "Fichier de log: $LOG_FILE"
    log "Fichier de debug: $DEBUG_FILE"
    log ""
    
    # Option: Démarrer le serveur automatiquement
    read -p "Voulez-vous démarrer le serveur automatiquement? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        start_server
        SERVER_STARTED_BY_SCRIPT=true
    else
        log "${YELLOW}ℹ️  Assurez-vous que le serveur est démarré sur le port $PORT${NC}"
    fi
    
    # Exécuter les tests
    run_tests
    
    # Arrêter le serveur si nous l'avons démarré
    if [ "$SERVER_STARTED_BY_SCRIPT" = true ]; then
        stop_server
    fi
    
    # Résumé
    log "${YELLOW}=================== RÉSUMÉ ===================${NC}"
    log "✅ Tests terminés. Consultez:"
    log "   - $LOG_FILE : Résultats détaillés"
    log "   - $DEBUG_FILE : Logs de debug complets"
    log ""
    log "${YELLOW}📋 PROCHAINES ÉTAPES:${NC}"
    log "1. Vérifiez les réponses dans le fichier de log"
    log "2. Consultez les logs de votre application Spring Boot"
    log "3. Ajustez les numéros de téléphone dans le script si nécessaire"
    log "4. Testez avec des messages réellement chiffrés"
}

# =============================================================================
# EXÉCUTION
# =============================================================================

# Nettoyer les anciens fichiers
> "$LOG_FILE"
> "$DEBUG_FILE"

# Trap pour garantir l'arrêt propre du serveur
trap stop_server EXIT INT TERM

# Lancer le script principal
main "$@"