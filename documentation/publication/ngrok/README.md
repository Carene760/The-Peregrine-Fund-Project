# Publication du site avec ngrok

Date de creation: 2026-04-09
Public cible: developpeurs

## 1) Presentation rapide

ngrok permet d exposer temporairement un service local (ex: Spring Boot sur `localhost:8080`) sur une URL publique HTTPS.

Cas d usage dans ce projet:

- publier rapidement le serveur pour tests externes,
- connecter des clients mobiles hors reseau local,
- faire des demonstrations sans deployer une infra complete.

## 2) Installation de ngrok

### Option A - Installation Windows (recommandee)

1. Creer un compte ngrok.
2. Telecharger l agent sur le site officiel.
3. Decompresser l executable.
4. Ajouter ngrok au PATH (optionnel mais recommande).

### Option B - Via package manager

Selon votre environnement, ngrok peut aussi etre installe via un gestionnaire de paquets.

## 3) Configuration initiale

Apres creation du compte, recuperer votre authtoken puis executer:

```powershell
ngrok config add-authtoken <VOTRE_AUTHTOKEN>
```

Verifier la configuration:

```powershell
ngrok config check
```

## 4) Execution pour ce projet

### Etape 1 - Lancer le serveur local

Dans le module serveur:

```powershell
cd Site
mvn spring-boot:run
```

Le serveur tourne generalement sur le port `8080`.

### Etape 2 - Ouvrir un tunnel ngrok

Dans un autre terminal:

```powershell
ngrok http 8080
```

ngrok affiche une URL publique de type:

```text
https://xxxx-xx-xx-xx-xx.ngrok-free.app
```

### Etape 3 - Mettre a jour les clients

Utiliser l URL publique ngrok dans les configurations clientes qui appellent votre API (ex application Android, webhook, outils tiers).

## 5) Exemple d integration projet

### Cote Android (assets config)

Dans le fichier de configuration mobile, remplacer temporairement l URL serveur:

```properties
server.url=https://<votre-sous-domaine-ngrok>
server.backup.url=https://<votre-sous-domaine-ngrok>
```

### Cote serveur

Si le serveur doit appeler un service mobile expose, adapter aussi les URLs cibles selon le tunnel actif.

## 6) Limites de ngrok (plan gratuit)

Attention: les limites peuvent evoluer selon la politique ngrok. Verifier toujours la page Pricing officielle.

Points courants du plan gratuit observes:

- credits d usage limites,
- nombre limite d endpoints en ligne,
- quota de transfert de donnees,
- quota de requetes HTTP,
- page interstitielle sur endpoints HTTP/S,
- domaine de developpement assigne (pas forcement domaine personnalise).

Impact pratique:

- convient bien pour dev, test, demo,
- moins adapte pour production stable continue.

## 7) Conseils d usage

1. Ne pas utiliser ngrok gratuit comme hebergement production principal.
2. Garder un fichier de config `dev` et un autre `prod` pour eviter les erreurs d URL.
3. Documenter l URL ngrok active dans l equipe lors de chaque session de test.
4. Proteger les endpoints exposes (auth, whitelists, validation forte).
5. Surveiller les logs serveur quand le tunnel est public.

## 8) Investir pour publication

Si vous publiez regulierement ou pour des utilisateurs reels:

- envisager un plan payant ngrok (moins de frictions, plus de controle),
- ou migrer vers une infra deploiement stable (cloud/vps/reverse proxy) avec domaine fixe.

Recommandation projet:

- ngrok gratuit pour developpement et validation rapide,
- solution payante ou infra dediee pour pre-production/production.

## 9) Liens officiels utiles

- Documentation: https://ngrok.com/docs
- Download: https://ngrok.com/download
- Pricing: https://ngrok.com/pricing

Derniere mise a jour: 2026-04-09
