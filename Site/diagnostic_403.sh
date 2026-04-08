#!/bin/bash

echo "=== DIAGNOSTIC SECURITY SPRING BOOT ==="
echo ""

# 1. Test CORS avec options préflight
echo "1. TEST CORS (OPTIONS)"
curl -X OPTIONS -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: content-type" \
  -v "http://localhost:8080/api/alerte" 2>&1 | grep -E "(HTTP/|< Access-Control)"

echo ""

# 2. Test avec différents en-têtes
echo "2. TEST AVEC EN-TÊTES"
curl -X POST "http://localhost:8080/api/alerte" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "Origin: http://localhost:3000" \
  -d '{"phoneNumber":"+261383817421","encryptedMessage":"test","sendSmsResponse":false}' \
  -v 2>&1 | grep -E "(HTTP/|<)"

echo ""

# 3. Vérifier les filtres actifs
echo "3. CONFIGURATION ACTUELLE SECURITY"
curl -s "http://localhost:8080/actuator/env" | grep -i security

echo ""
echo "=== FIN DU DIAGNOSTIC ==="