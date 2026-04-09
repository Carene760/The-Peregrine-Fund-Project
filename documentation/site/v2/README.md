# Module Site - Version 2

Version: 2.0
Statut: en cours d integration

## Contenu de v2

- `EXECUTION_v2.md`: execution, configuration v2, webhook auto, erreurs frequentes.
- `ARCHITECTURE_v2.md`: architecture cible v2 avec contrats de message explicites.
- `FONCTIONNALITE_v2.md`: regles metier v2 (type explicite, parser cle=valeur, date_envoi).
- `CHANGELOG.md`: ecarts entre v1 et v2.

## Modifications principales par rapport a v1

1. Interpretation messages simplifiee:
   - abandon de la detection par nombre de separateurs,
   - introduction du format cle=valeur avec `type` explicite.

2. Evolution du modele message:
   - ajout de `date_envoi` dans la table `message`.

3. Integration gateway amelioree:
   - creation/suppression webhook integree dans le systeme (plus dependance manuelle Postman uniquement).

4. Configuration centralisee:
   - nouvelles cles webhook/gateway dans `application.properties`.

## References code v2

- `Site/src/main/java/com/example/serveur/service/SmsProcessingService.java`
- `Site/src/main/java/com/example/serveur/service/AlerteService.java`
- `Site/src/main/java/com/example/serveur/controller/ServeurController.java`
- `Site/src/main/java/com/example/serveur/model/Message.java`
- `Site/src/main/resources/application.properties`

Derniere mise a jour: 2026-04-10
