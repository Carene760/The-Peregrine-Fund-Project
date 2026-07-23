# Deployment guide (Site backend)

Concise instructions for deploying the Spring Boot backend (`Site/`) to a
free/hobby-tier host with a managed Postgres database, and wiring up the
GitHub Actions secrets the prepared workflows need. Nothing in this
document has been executed - it is a plan for the user to follow and adapt.

This project has **not** been deployed anywhere by this pass. No accounts
were created, no services were provisioned, no money was spent.

## 1. Choose a managed Postgres

Either works for a hobby/free tier:

- **Neon** (https://neon.tech) - serverless Postgres, generous free tier,
  branching support useful for a staging DB copy.
- **Supabase** (https://supabase.com) - Postgres + extras, free tier.

Steps (either provider):
1. Create an account, create a new Postgres project/database.
2. Copy the connection string it gives you (host, port, database name,
   username, password - usually as a single `postgres://...` URL).
3. Run the schema against it once: `bdd/structure.sql` (or
   `bdd/structure_lisible.sql`) creates the tables; the app also has
   `spring.jpa.hibernate.ddl-auto=update` which will create missing
   tables/columns on startup, but starting from the real schema file is
   safer and faster.
4. Note down: host, port, database name, username, password.

## 2. Choose a host for the Spring Boot app

Either works for a hobby/free tier:

- **Railway** (https://railway.app) - simplest: point it at the `Site/`
  subdirectory (root directory setting) or build the `Site/Dockerfile`
  directly; it auto-detects the exposed port from `server.port`/`$PORT`.
- **Render** (https://render.com) - "Web Service" from a Dockerfile, same
  idea; free tier services sleep after inactivity (cold starts).

Steps (either provider):
1. Create an account, connect this GitHub repo.
2. Point the service at `Site/Dockerfile` as the build method (both
   Railway and Render support "Dockerfile" as a build strategy - set the
   Docker build context to the `Site/` directory).
3. Configure environment variables (see section 3) instead of committing an
   `application.properties` - Spring Boot reads any property as an
   uppercase-with-underscores env var automatically (relaxed binding), e.g.
   `spring.datasource.password` -> `SPRING_DATASOURCE_PASSWORD`.
4. Set the exposed port to `8080` (or read `$PORT` if the platform injects
   one - Railway/Render both set `PORT`; you can add
   `server.port=${PORT:8080}` to application.properties if you want the app
   to honour whichever port the platform assigns rather than hardcoding
   8080).
5. Deploy. Watch the logs for `Started ServeurApplication` and for the
   `PasswordMigrationRunner` / `AllowedNumbersService` startup lines
   confirming DB connectivity.

## 3. Required environment variables

Copy every key from `Site/src/main/resources/application.properties.example`
and set a real value for each as an environment variable (uppercase, dots
and dashes become underscores) in Railway/Render's dashboard. At minimum:

| Property | Env var | Notes |
|---|---|---|
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | from step 1 |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | from step 1 |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` | from step 1, treat as a secret |
| `sendgrid.api.key` | `SENDGRID_API_KEY` | rotate first, see SECURITY_REMEDIATION.md |
| `sendgrid.from.email` | `SENDGRID_FROM_EMAIL` | |
| `app.api.key` | `APP_API_KEY` | generate with `openssl rand -base64 32`; must match the Android app's `config.properties` `api.key` |
| `app.remember-me.secret` | `APP_REMEMBER_ME_SECRET` | generate with `openssl rand -base64 32` |
| `gateway.auth.username` / `gateway.auth.password` | `GATEWAY_AUTH_USERNAME` / `GATEWAY_AUTH_PASSWORD` | shared with the SMS gateway device |
| `gateway.base-url` / `gateway.webhook.callback-url` | `GATEWAY_BASE_URL` / `GATEWAY_WEBHOOK_CALLBACK_URL` | must be reachable from the gateway phone and point back at the publicly deployed server, respectively |
| `encryption.secret-key` | `ENCRYPTION_SECRET_KEY` | generate a fresh 32-char value; must match the Android app's `config.properties` `secret.key` exactly |

Do not reuse the example/dev values checked in anywhere in this repo for a
real deployment - they are dev-only placeholders (or, for the AES key and
gateway credentials in `application/app/src/main/assets/config.properties`,
already leaked via git history - see SECURITY_REMEDIATION.md).

## 4. Point the Android app at the deployed server

Update `application/app/src/main/assets/config.properties`:
- `server.url` / `server.backup.url` -> the public HTTPS URL of the
  deployed backend.
- `api.key` -> must match `app.api.key` set in step 3.
- `secret.key` -> must match `encryption.secret-key` set in step 3.

## 5. GitHub Actions secrets

For `.github/workflows/release-apk.yml` to produce a signed, installable
release APK, add these repository secrets (Settings > Secrets and
variables > Actions > New repository secret):

- `ANDROID_KEYSTORE_BASE64` - `base64 -w0 your-release.keystore`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

See the comments at the top of `release-apk.yml` for how to generate a
keystore and the (not-yet-wired) `signingConfig` block that still needs
adding to `application/app/build.gradle` to actually consume these secrets.

`.github/workflows/ci.yml` needs no secrets - it only builds and runs unit
tests for both `Site` (Maven) and `application` (Gradle).

## 6. Known gaps to resolve before a real deployment

- **Release APK build currently fails** on unrelated, pre-existing
  corrupted PNG resources (`app/src/main/res/drawable/logo_raptor.png` and
  `central_decoration.png` fail AAPT compilation) - confirmed unrelated to
  this security/reliability pass (last touched in an older commit). Fix
  these image files before `release-apk.yml` can produce a build.
- **CSRF protection is still disabled** for the admin web UI (see
  `SecurityConfig` comments and SECURITY_REMEDIATION.md) - do not expose
  the admin UI to the public internet without addressing this first, or at
  minimum put it behind a VPN/IP allowlist at the hosting platform level.
- **No TLS termination configured** in the Dockerfile/app itself - rely on
  the hosting platform's HTTPS termination (Railway/Render both provide
  this automatically on their default domains).
