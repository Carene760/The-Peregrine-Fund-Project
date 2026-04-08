#!/bin/bash

# URL de l'API
API_URL="http://localhost:8080/api/message"

echo "🚀 Tests Complets du Système d'Alerte"
echo "======================================"

# Fonction pour envoyer une alerte et afficher la réponse
test_alerte() {
    local scenario=$1
    local message=$2
    local description=$3
    
    echo ""
    echo "🔵 Scenario: $scenario"
    echo "📝 Description: $description"
    echo "📨 Message: $message"
    
    response=$(curl -s -X POST "$API_URL" \
      -H "Content-Type: application/json" \
      -d '{
        "event": "sms:received",
        "payload": {
          "phoneNumber": "+261384984929",
          "message": "'"$message"'",
          "receivedAt": "2025-09-05T10:05:00Z"
        }
      }')
    
    echo "📤 Réponse du serveur:"
    echo "$response"
    echo "----------------------------------------"
}

test_alerte "2" "dJ8fjzhFbYqj4M4sawPKKA=="

# # Test 1: Alerte Vert (Début de feu + Intervention possible)
# test_alerte "1" "2025-09-05 10:00:00/2025-09-05 10:05:00/1/false/Sud/50.0/PointA/Petit feu/4/-18.879180/47.107890/1" "Vert - Début de feu avec intervention possible"

# # Test 1: Niveau VERT → Email Administrateur seulement
# test_scenario "VERT" "3" "1" "false" "Feu maîtrisé → Email Administrateur"

# # Test 2: Alerte Jaune (Début de feu + Intervention partielle)
# test_alerte "2" "2025-09-05 10:00:00/2025-09-05 10:05:00/2/false/Sud/150.5/PointB/Feu moyen/6/-18.880000/47.508000/1" "Jaune - Début de feu avec intervention partielle"

# # Test 3: Alerte Orange (Feu en cours + Intervention partielle + Renfort)
# test_alerte "3" "2025-09-05 10:00:00/2025-09-05 10:05:00/2/true/Sud/300.0/PointC/Feu important/5/-18.881000/47.509000/2" "Orange - Feu en cours avec renfort"

# Test 4: Alerte Rouge (Feu en cours + Intervention impossible)
# test_alerte "4" "2025-09-05 10:00:00/2025-09-05 10:05:00/3/false/Sud/500.0/PointD/Feu critique/4/-18.882000/47.510000/2" "Rouge - Feu en cours avec intervention impossible"

# # Test 5: Feu maîtrisé (Vert)
# test_alerte "5" "2025-09-05 10:00:00/2025-09-05 10:05:00/1/false/Sud/0.0/PointE/Feu éteint/2/-18.883000/47.511000/1" "Vert - Feu maîtrisé"

# # Test 6: Format invalide (doit retourner une erreur)      
# test_alerte "6" "2025-09-05 10:00:00/2025-09-05 10:05:00/2/false/Sud" "Format invalide - Doit retourner une erreur"

# Test 7: Numéro non autorisé (doit être ignoré)
# echo ""
# echo "🔵 Scenario: 7 - Numéro non autorisé"
# response=$(curl -s -X POST "$API_URL" \
#   -H "Content-Type: application/json" \
#   -d '{
#     "event": "sms:received",
#     "payload": {
#       "phoneNumber": "+261340000000",
#       "message": "2025-09-05 10:00:00/2025-09-05 10:05:00/2/false/Sud/150.5/PointA/Feu/2/-18.879190/47.507890/1",
#       "receivedAt": "2025-09-05T10:05:00Z"
#     }
#   }')
# echo "📝 Description: Numéro non autorisé - Doit être ignoré silencieusement"
# echo "📤 Réponse du serveur:"
# echo "$response"
# echo "----------------------------------------"

echo ""
echo "🎯 Résumé des Tests:"
echo "===================="
echo "1️⃣  Vert: Début de feu + Intervention possible"
echo "2️⃣  Jaune: Début de feu + Intervention partielle" 
echo "3️⃣  Orange: Feu en cours + Renfort"
echo "4️⃣  Rouge: Feu en cours + Intervention impossible"
echo "5️⃣  Vert: Feu maîtrisé"
echo "6️⃣  Erreur: Format invalide"
echo "7️⃣  Ignoré: Numéro non autorisé"
echo ""
echo "📊 Vérifiez les logs pour confirmer:"
echo "- Insertion dans les tables message et alerte"
echo "- Envoi des SMS de réponse"
echo "- Niveaux d'alerte corrects (Vert → Jaune → Orange → Rouge)"