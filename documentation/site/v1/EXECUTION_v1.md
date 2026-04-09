# Guide d execution du module Site - Version 1

Date de creation: 2026-04-09
Version: 1.0
Statut: production/dev

---

## Table des matieres

1. Prerequis
2. Fichiers obligatoires
3. Configuration par environnement
4. Procedure d execution par etapes
5. Verification post-demarrage
6. Maintenance legere et erreurs frequentes
7. Checklist de validation

---

## 1) Prerequis

### Obligatoire

- Java 17+ (recommande avec Spring Boot 3.3.x)
- Maven 3.9+
- PostgreSQL 16+
- Base `tpf` disponible
- Scripts SQL executes:
  - `bdd/structure_lisible.sql`
  - `bdd/data_lisible.sql` (optionnel selon contexte)

### Verification rapide

```powershell
java -version
mvn -v
psql --version
```

---

## 2) Fichiers obligatoires

Pour que l application fonctionne correctement, verifier la presence de:

- `Site/pom.xml`
- `Site/src/main/resources/application.properties`
- `Site/src/main/java/com/example/serveur/ServeurApplication.java`
- `Site/src/main/java/com/example/serveur/config/SecurityConfig.java`
- `Site/src/main/java/com/example/serveur/controller/ServeurController.java`
- `Site/src/main/resources/templates/*`
- `Site/src/main/resources/static/*`

---

## 3) Configuration par environnement

> Important: ne pas committer de secrets reels. Utiliser des valeurs de test en local.

### Variables critiques (application.properties)

| Categorie   | Cle                               | Role                 | Obligatoire        |
| ----------- | --------------------------------- | -------------------- | ------------------ |
| Serveur     | `server.port`                   | Port HTTP            | Oui                |
| Serveur     | `server.address`                | Bind reseau          | Oui                |
| Database    | `spring.datasource.url`         | Connexion PostgreSQL | Oui                |
| Database    | `spring.datasource.username`    | User BDD             | Oui                |
| Database    | `spring.datasource.password`    | Password BDD         | Oui                |
| JPA         | `spring.jpa.hibernate.ddl-auto` | Strategie schema     | Oui                |
| Message     | `message.separator`             | Parsing SMS/Internet | Oui                |
| Retour      | `info.separator`                | Format info retour   | Oui                |
| SMS Gateway | `gateway.auth.username`         | Auth interne gateway | Oui                |
| SMS Gateway | `gateway.auth.password`         | Auth interne gateway | Oui                |
| SMS Gateway | `gateway.internal-send-url`     | Endpoint sortie SMS  | Oui                |
| Chiffrement | `encryption.secret-key`         | Cle AES              | Oui                |
| Mail        | `sendgrid.api.key`              | API SendGrid         | Oui si email actif |
| Mail        | `sendgrid.from.email`           | Expediteur email     | Oui si email actif |
| Logging     | `sms.logfile.path`              | Fichier logs SMS     | Oui                |

### Exemple DEV

```properties
server.port=8080
server.address=0.0.0.0

spring.datasource.url=jdbc:postgresql://localhost:5432/tpf
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

message.separator=/
info.separator=~

encryption.secret-key=0123456789abcdef
```

### Recommandation TEST

- `spring.jpa.show-sql=false`
- jeux de donnees controles
- endpoint externes simules (SMS/SendGrid)

### Recommandation PROD

- `spring.jpa.show-sql=false`
- mots de passe hors fichier, injectes via variables d environnement
- logs niveaux `INFO/WARN/ERROR`
- acces reseau filtre sur endpoints sensibles

---

## 4) Procedure d execution par etapes

### Phase 1 - Preparation BDD

```powershell
psql -U postgres -d tpf -f bdd/structure_lisible.sql
psql -U postgres -d tpf -f bdd/data_lisible.sql
```

### Phase 2 - Compilation

```powershell
cd Site
mvn clean compile
```

### Phase 3 - Tests

```powershell
mvn test
```

### Phase 4 - Lancement

```powershell
mvn spring-boot:run
```

### Phase 5 - Packaging (optionnel)

```powershell
mvn clean package
java -jar target/serveur-1.0-SNAPSHOT.jar
```

---

## 5) Verification post-demarrage

1. Verification application

```powershell
curl http://localhost:8080/
```

2. Verification endpoints API critiques

```powershell
curl http://localhost:8080/api/test-chiffrement?message=test
curl http://localhost:8080/sync/status
curl http://localhost:8080/sync/interventions
```

3. Verification templates

- page login chargee
- pages historique/statistique accessibles selon routes

4. Verification logs

- absence d erreur de connexion BDD
- absence d erreur d initialisation SendGrid
- creation/maj du fichier `sms.logfile.path`

---

## 6) Maintenance legere et erreurs frequentes

### Erreur 1 - Connexion BDD refusee

Symptome:

- `org.postgresql.util.PSQLException`

Checks:

- service PostgreSQL actif
- URL/user/password corrects
- base `tpf` existante

Correction:

```powershell
psql -U postgres -c "\l"
```

### Erreur 2 - Port deja utilise

Symptome:

- echec bind sur `8080`

Correction:

- changer `server.port`
- ou liberer le port

### Erreur 3 - Parsing message invalide

Symptome:

- format alerte refuse

Cause probable:

- mauvais `message.separator`
- message avec nombre de segments inattendu

Correction:

- verifier `message.separator=/`
- verifier format metier de l alerte dans `FONCTIONNALITE_v1.md`

### Erreur 4 - Envoi SMS retour en echec

Symptome:

- reponse SMS non envoyee

Checks:

- `gateway.internal-send-url`
- authentification gateway
- connectivite reseau

### Erreur 5 - Envoi email en echec

Symptome:

- exceptions SendGrid

Checks:

- `sendgrid.api.key`
- `sendgrid.from.email`
- acces internet sortant

### Erreur 6 - Numero non autorise

Symptome:

- message ignore

Cause:

- numero absent de la liste chargee depuis patrouilleurs

Action:

- verifier donnees `patrouilleur`
- attendre le refresh periodique du service

---

## 7) Checklist de validation

- [ ] Base `tpf` prete et joignable
- [ ] `application.properties` coherent avec l environnement
- [ ] `mvn clean compile` ok
- [ ] `mvn test` ok
- [ ] application demarree sans stacktrace bloquante
- [ ] endpoints critiques repondent
- [ ] logs SMS et logs applicatifs generes

Derniere mise a jour: 2026-04-09
