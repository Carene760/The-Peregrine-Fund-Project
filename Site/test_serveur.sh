#!/bin/bash

# Configuration
PORT=8080
BASE_URL="http://localhost:$PORT/api"
TEST_PHONE="+261384984929"  # Remplacez par un numéro autorisé

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction pour logger les résultats
log() {
    echo -e "$1"
}

# Fonction pour tester un endpoint
test_endpoint() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local expected_http_code="$5"
    
    log "${BLUE}Testing $name...${NC}"
    log "URL: $url"
    
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
        log "${GREEN}✓ SUCCESS: $name returned HTTP $http_code${NC}\n"
        return 0
    else
        log "${RED}✗ FAILURE: $name expected HTTP $expected_http_code but got $http_code${NC}\n"
        return 1
    fi
}

# Fonction pour chiffrer un message (simplifié)
encrypt_message() {
    local message="$1"
    # Note: Cette fonction est une simulation simple
    # En production, utilisez votre EncryptionUtil Java
    echo "encrypted_${message}_$(date +%s)"
}

# ==================== TESTS ====================

log "${YELLOW}=== Démarrage des tests du ServeurController ===${NC}"

# 1. Test de chiffrement
log "${YELLOW}1. Test de chiffrement/déchiffrement${NC}"
test_endpoint "Test chiffrement" "GET" "$BASE_URL/test-chiffrement?message=HelloWorld" "" "200"

# 2. Test de login
log "${YELLOW}2. Test de login${NC}"
encrypted_login=$(encrypt_message "login/user/mdp")
login_data="{
  \"phoneNumber\": \"$TEST_PHONE\",
  \"encryptedMessage\": \"$encrypted_login\",
  \"sendSmsResponse\": false
}"
test_endpoint "Login" "POST" "$BASE_URL/login" "$login_data" "200"

# 3. Test d'alerte
log "${YELLOW}3. Test d'alerte${NC}"
encrypted_alert=$(encrypt_message "alerte/type/description/urgence")
alert_data="{
  \"phoneNumber\": \"$TEST_PHONE\",
  \"encryptedMessage\": \"$encrypted_alert\",
  \"sendSmsResponse\": false
}"
test_endpoint "Alerte" "POST" "$BASE_URL/alerte" "$alert_data" "200"

# 4. Test de mise à jour de statut
log "${YELLOW}4. Test mise à jour statut${NC}"
encrypted_status=$(encrypt_message "2023-09-09 10:00:00/12345/2")
status_data="{
  \"phoneNumber\": \"$TEST_PHONE\",
  \"encryptedMessage\": \"$encrypted_status\",
  \"sendSmsResponse\": false
}"
test_endpoint "Status Update" "POST" "$BASE_URL/update-status" "$status_data" "200"

# 5. Test webhook SMS (simulation)
log "${YELLOW}5. Test webhook SMS${NC}"
webhook_data='{
  "event": "sms:received",
  "payload": {
    "phoneNumber": "'$TEST_PHONE'",
    "message": "encrypted_sms_message",
    "receivedAt": "2023-09-09T10:00:00Z"
  }
}'
test_endpoint "Webhook SMS" "POST" "$BASE_URL/webhook" "$webhook_data" "200"

# 6. Test avec numéro non autorisé
log "${YELLOW}6. Test avec numéro non autorisé${NC}"
unauthorized_phone="+33123456789"
unauthorized_data="{
  \"phoneNumber\": \"$unauthorized_phone\",
  \"encryptedMessage\": \"test_message\",
  \"sendSmsResponse\": false
}"
test_endpoint "Login numéro non autorisé" "POST" "$BASE_URL/login" "$unauthorized_data" "200"

log "${YELLOW}=== Résumé des tests ===${NC}"
log "Les tests ont été exécutés. Vérifiez les réponses ci-dessus."
log ""
log "${YELLOW}Notes importantes:${NC}"
log "1. Remplacez $TEST_PHONE par un numéro présent dans votre allowedNumbersService"
log "2. Les messages 'encrypted_...' sont simulés - utilisez de vrais messages chiffrés"
log "3. Vérifiez que votre base de données est accessible"
log "4. Assurez-vous que le serveur est démarré sur le port $PORT"

# ==================== FONCTIONS UTILES ====================

# Fonction pour démarrer le serveur (optionnel)
start_server() {
    log "${YELLOW}Démarrage du serveur...${NC}"
    mvn clean spring-boot:run &
    SERVER_PID=$!
    sleep 10 # Attendre que le serveur démarre
}

# Fonction pour arrêter le serveur (optionnel)
stop_server() {
    if [ ! -z "$SERVER_PID" ]; then
        log "${YELLOW}Arrêt du serveur...${NC}"
        kill $SERVER_PID
    fi
}

# Exécution optionnelle du serveur
# start_server
# ... tests ...
# stop_server