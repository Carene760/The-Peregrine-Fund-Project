# SMSSync Master - Documentation d integration

Ce dossier documente l utilisation de l application gateway **SMSSync Master** dans le systeme The Peregrine Fund.

## Sources officielles

- Documentation developpeur: http://smssync.ushahidi.com/developers/
- Telechargement: http://smssync.ushahidi.com/download/
- APK release v3.1.1: https://github.com/ushahidi/SMSSync/releases/tag/v3.1.1
- Captures ecran: http://smssync.ushahidi.com/screenshots/

## Contenu de ce dossier

- `README.md`: vue d ensemble
- `CONFIGURATION_ET_INTEGRATION.md`: configuration SMSSync + integration avec le serveur Spring
- `LIMITE&CONSEIL.md`: limites, obsolescence, alternatives, et impact de migration

## Swagger UI

A ce jour, **aucun Swagger UI officiel public n est documente pour SMSSync**.

- SMSSync fournit une documentation HTTP/JSON (webhook et tasks) sur la page developers.
- L integration se fait via endpoints et payloads documentes, pas via une interface Swagger standard.

## Points d architecture

- Android gateway (SMSSync) recoit/envoie SMS via SIM.
- Le serveur Site recoit les messages via HTTP webhook.
- Le serveur peut demander a SMSSync d envoyer des SMS de reponse (task/send ou endpoint interne selon mode utilise).

## Rappel important

SMSSync est un projet ancien (documentation historique last update 2015, derniere release 2017).
Consulter `LIMITE&CONSEIL.md` avant un deploiement production.

Derniere mise a jour: 2026-04-09
