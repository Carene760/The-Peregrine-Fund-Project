# Security remediation runbook: leaked secrets + git history purge

Written as part of the overnight security/reliability pass. **Nothing in
the "purge" section has been executed.** This is a plan for the repo owner
to review and run manually.

## 1. Secrets confirmed present in tracked files / git history

Verified with `git grep`/`git log`/`git show` against tracked files and
full history (`git rev-list --all`), not assumed. Re-run these commands
yourself before acting - the repo changes over time.

| Secret | Where | Status |
|---|---|---|
| AES encryption key `0123456789abcdef` | `application/app/src/main/assets/config.properties` (currently tracked, every commit before this pass), and a stray committed build artifact `Site/app/build/intermediates/assets/debug/mergeDebugAssets/config.properties` (+ a `.jar` copy alongside it) | **Leaked in history.** Rotated to a new value in this pass's HEAD commit for both client and server config, but the old value remains readable in every prior commit and in the stray build-artifact copies until purged. |
| Gateway auth password `39M6xJPK` | Was tracked in `Site/src/main/resources/application.properties` at commit `4b17187` ("Suppression des dossiers compilés et sécurisation des clés API"), removed from tracking at commit `c84aeed` ("Nettoyage"). Same value is still the live value in the current gitignored `application.properties` and in the app's `config.properties` is not stored (server-only credential). | **Leaked in history**, still in active use. Must be rotated at the source (change the value the app/gateway is configured to send) - not just removed from a future commit. |
| SendGrid API key | Searched every tracked file and full history for `SG.` prefixed strings and `sendgrid.api.key=` assignments: the only occurrences found are the placeholder `api_key_here` (commit `4b17187`) and the current gitignored `application.properties` (never committed). **No real SendGrid key found in git history in this audit.** | Not confirmed leaked via git, but the live key in the gitignored file has been sitting in a widely-shared dev file for a long time - treat as should-rotate-anyway (see section 2). |
| DB password | Historical tracked `application.properties` (commit `4b17187`) had `spring.datasource.password=postgres` (the Postgres default, low sensitivity) and `documentation/*/EXECUTION_v1.md` show example values (`VotreMotDePasse`, `postgres`) that read as placeholders/docs, not real production credentials. | Low risk / likely not a real leak, but confirm with whoever owns the actual database instance. |
| Fixed gateway phone number `+261382318042` | `application/app/src/main/assets/config.properties`, tracked | Not a "secret" in the credential sense, but it identifies real hardware/a real SIM - worth being aware it's public in git history. |
| ngrok tunnel URLs (`server.url`, `server.backup.url`, old fallback in `SyncService.java`) | Tracked `config.properties` and (until this pass) hardcoded in `SyncService.java` | Ephemeral (ngrok free tier URLs rotate/expire) but still exposes infra details; the hardcoded fallback in `SyncService.java` was removed in this pass (see commit "P0-3 & P1-7/8"). |

**Bottom line: the encryption key and the gateway password are the two
concretely confirmed leaked secrets that are still live/reused. Both need
real rotation (see section 2), not just a new value in the latest commit.**

## 2. Rotating the value in the current file is NOT enough

This pass rotated `encryption.secret-key` / `secret.key` (AES-256, both
client and server, matching values so the wire format still works) as part
of the AES/GCM migration commit. **This is necessary but not sufficient**:
anyone who cloned this repo before that commit still has the old key in
their local history, and the old key remains recoverable from any mirror,
fork, or backup of this repository regardless of what today's HEAD
contains. A secret is compromised the moment it is pushed to a shared
remote, permanently, until the *value itself* is retired everywhere it's
used - rewriting history only prevents *new* clones from seeing it (see
"limitations" in section 4).

Action items, in priority order:
1. **Encryption key**: already rotated in this pass (client+server, this
   commit). Confirm both sides use the new value after pulling this
   branch; anyone with an old build of the Android app will fail to
   decrypt/encrypt against a server running the new key until they update.
2. **Gateway auth password** (`gateway.auth.username` /
   `gateway.auth.password`, currently `sms` / `39M6xJPK`): rotate this to a
   new random value in the gitignored `application.properties`, AND
   reconfigure the SMSSync gateway device/app with the new value - the old
   value stays valid (and compromised) until the *gateway itself* stops
   accepting it.
3. **SendGrid API key**: even though not confirmed leaked via git, rotate
   it from the SendGrid dashboard (Settings > API Keys > regenerate) as a
   precaution given how long it has lived in a shared dev file, and update
   the gitignored `application.properties` with the new value.
4. **Database password**: rotate at the Postgres instance itself (`ALTER
   USER ... PASSWORD ...` or your hosting provider's credential rotation
   UI), then update the gitignored `application.properties`.
5. **`app.api.key` / `app.remember-me.secret`**: newly introduced in this
   pass with dev-generated placeholder values (see commit "P1-6"). Treat
   these the same as any other secret before a real deployment - regenerate
   via a proper secrets manager, do not reuse the values committed for
   local dev testing.

## 3. Recommended: use a secrets manager going forward

For anything beyond local dev, stop storing real secrets in
`application.properties` (even gitignored, it's easy to accidentally commit
later) or in `config.properties` (tracked, so it's *always* wrong for real
secrets). Options: your hosting platform's env var store (Railway/Render
both have one, see `DEPLOYMENT.md`), or a dedicated manager (Doppler, 1Password
Secrets Automation, AWS/GCP Secret Manager, etc.) injecting env vars at
deploy time. Spring Boot already supports this with zero code changes
(relaxed env-var binding, e.g. `ENCRYPTION_SECRET_KEY`); the Android app
would need `config.properties` replaced by a build-time-injected value
(e.g. via `BuildConfig` fields populated from CI secrets) to fully close
this gap on the client side - not done in this pass, flagged here as a
follow-up.

## 4. Git history purge (NOT EXECUTED - review and run manually)

Purpose: remove the old AES key, old gateway password, and the stray
`Site/app/build/**` committed build-artifact tree (which also contains a
copy of the leaked key, plus is pure repo-hygiene noise) from every commit
in history, not just future ones.

**Preferred tool: `git filter-repo`** (actively maintained; the older
`git filter-branch` is officially deprecated by Git and BFG Repo-Cleaner is
also a reasonable alternative but filter-repo is generally recommended
today).

### 4.1 Prerequisites

```bash
# Install (one of):
pip install git-filter-repo
# or: brew install git-filter-repo
# or download the single-file script from https://github.com/newren/git-filter-repo

# ALWAYS work on a fresh throwaway clone, never your only working copy:
git clone --no-local /path/to/The-Peregrine-Fund-Project The-Peregrine-Fund-Project-purge
cd The-Peregrine-Fund-Project-purge
```

### 4.2 Remove the stray committed build-artifact tree

```bash
# Deletes Site/app/build/** and application's own build/** (if tracked)
# from every commit in history, not just HEAD.
git filter-repo --path Site/app/build --invert-paths
git filter-repo --path application/.gradle --invert-paths
git filter-repo --path application/build --invert-paths
git filter-repo --path Site/build --invert-paths
git filter-repo --path Site/target --invert-paths
```

Run `git filter-repo --analyze` first (writes a report under
`.git/filter-repo/analysis/`) to double check the exact paths present in
history before deciding what to strip - the list above is based on what
this audit found tracked at HEAD, but earlier history may contain
additional stray build paths that were later moved/renamed.

### 4.3 Scrub the leaked secret values themselves

Even after removing whole files, the *values* could still appear elsewhere
(e.g. copy-pasted into a commit message, a log file, a doc). Use
`--replace-text` to blank out the specific known-leaked strings wherever
they appear in any blob:

```bash
cat > /tmp/secrets-to-purge.txt <<'EOF'
0123456789abcdef==>***REMOVED-AES-KEY***
39M6xJPK==>***REMOVED-GATEWAY-PASSWORD***
EOF

git filter-repo --replace-text /tmp/secrets-to-purge.txt
```

### 4.4 Verify before pushing anywhere

```bash
# Confirm the strings are gone from every commit:
git log --all -p | grep -F '0123456789abcdef'   # expect no output
git log --all -p | grep -F '39M6xJPK'            # expect no output
git log --all --oneline -- Site/app/build        # expect no output

# Sanity-check the repo still builds from a fresh checkout of the purged history.
```

### 4.5 Rewrite is destructive - coordinate before pushing

`git filter-repo` rewrites every commit hash after the earliest touched
commit. This means:
- **This requires a force-push** (`git push --force-with-lease` at minimum,
  ideally `--force` after explicit coordination) to every remote branch -
  **not done by this pass**, and not something to run without the repo
  owner's explicit go-ahead given how disruptive it is.
- **Every other contributor with a local clone must be told before you
  push**, and must re-clone (or carefully rebase their own unpushed work
  onto the new history) afterward - their existing local branches will
  diverge irreconcilably from the rewritten remote history otherwise.
- **Any open pull requests will likely need to be recreated** against the
  new history.
- GitHub also caches old commits reachable via forks/PRs/releases
  indefinitely in some cases - a full purge may require contacting GitHub
  support to expire cached views of the old objects
  (https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository).

### 4.6 Limitations - purge is not the same as "never leaked"

Rewriting history stops *new* clones/forks from seeing the old secret
values. It does **not** un-expose a value that was already leaked -
anyone who already cloned/forked/mirrored the repo before the purge keeps
the old history with the old secrets in it indefinitely. This is why
section 2 (rotate at the source) is the actually load-bearing fix; the
purge is hygiene on top of that, not a substitute for it.

## 5. Summary checklist for the repo owner

- [ ] Rotate the gateway auth password at the SMSSync gateway device itself
      (not just in `application.properties`)
- [ ] Rotate the SendGrid API key from the SendGrid dashboard
- [ ] Rotate the Postgres password at the database instance
- [ ] Confirm the new AES key (already rotated in this pass) is deployed to
      both the server and every Android app install before relying on it
- [ ] Decide whether/when to run the `git filter-repo` purge above (review
      section 4 fully first, coordinate with any other contributor, back
      up the repo before running)
- [ ] Move secret storage to environment variables / a secrets manager
      going forward instead of properties files (see `DEPLOYMENT.md`)
