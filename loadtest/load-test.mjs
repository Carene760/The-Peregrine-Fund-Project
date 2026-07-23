#!/usr/bin/env node
// Test de charge simple pour le backend Spring Boot (Site/), sans dependance
// externe (utilise fetch + crypto natifs de Node >=18). Simule N "rangers"
// envoyant des alertes (et/ou se connectant) en parallele pendant une duree
// donnee, et rapporte throughput / latence / taux d'erreur.
//
// Usage:
//   node loadtest/load-test.mjs --url http://localhost:8080 --vus 20 --duration 30 --scenario alerte
//
// Scenarios:
//   alerte  (defaut) - POST /api/message avec un message ALERTE chiffre (V2)
//   login             - POST /api/message avec un message LOGIN chiffre (V2)
//                        exercice le hachage bcrypt cote serveur meme si le
//                        mot de passe est faux (le login est classifie et le
//                        hash est bien compare - c'est le chemin CPU le plus
//                        couteux du serveur)
//   mixed             - melange alerte/login/sync en proportions realistes
//                        (rangers qui envoient + dashboard qui synchronise)
//
// Ce script chiffre lui-meme les payloads en AES-256-GCM, au meme format
// que Site/.../util/EncryptionUtil.java (IV 12 octets aleatoire + tag GCM
// 128 bits, le tout prefixe par l'IV puis encode en Base64) - donc PAS
// besoin de l'app Android pour generer du trafic realiste.

import { readFileSync } from 'node:fs';
import { createCipheriv, randomFillSync } from 'node:crypto';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

// ---------------------------------------------------------------------------
// Arguments CLI
// ---------------------------------------------------------------------------
function parseArgs(argv) {
    const args = { url: 'http://localhost:8080', vus: 10, duration: 30, scenario: 'alerte', timeoutMs: 10000 };
    for (let i = 0; i < argv.length; i++) {
        const a = argv[i];
        if (a === '--url') args.url = argv[++i];
        else if (a === '--vus') args.vus = parseInt(argv[++i], 10);
        else if (a === '--duration') args.duration = parseInt(argv[++i], 10);
        else if (a === '--scenario') args.scenario = argv[++i];
        else if (a === '--timeout') args.timeoutMs = parseInt(argv[++i], 10);
        else if (a === '--help' || a === '-h') { printHelp(); process.exit(0); }
    }
    return args;
}

function printHelp() {
    console.log(`Usage: node load-test.mjs [--url http://localhost:8080] [--vus 10] [--duration 30] [--scenario alerte|login|mixed] [--timeout 10000]`);
}

// ---------------------------------------------------------------------------
// Config serveur (cle de chiffrement + cle API) lues depuis application.properties
// pour ne jamais avoir a dupliquer/perimer un secret dans ce script.
// ---------------------------------------------------------------------------
function readProperty(propsText, key) {
    const match = propsText.match(new RegExp(`^${key}\\s*=\\s*(.+)$`, 'm'));
    if (!match) throw new Error(`Propriete manquante dans application.properties: ${key}`);
    return match[1].trim();
}

const propsPath = path.join(__dirname, '..', 'Site', 'src', 'main', 'resources', 'application.properties');
const propsText = readFileSync(propsPath, 'utf8');
const SECRET_KEY = readProperty(propsText, 'encryption.secret-key');
const API_KEY = readProperty(propsText, 'app.api.key');

if (Buffer.byteLength(SECRET_KEY, 'utf8') !== 32) {
    console.warn(`⚠️  encryption.secret-key fait ${Buffer.byteLength(SECRET_KEY, 'utf8')} octets (attendu 32 pour AES-256). Verifiez application.properties.`);
}

// ---------------------------------------------------------------------------
// Chiffrement AES-256-GCM, identique a EncryptionUtil.chiffrer() cote serveur:
// IV aleatoire 12 octets, tag 128 bits, sortie = base64(iv || ciphertext || tag)
// ---------------------------------------------------------------------------
function encrypt(plaintext) {
    const iv = randomFillSync(Buffer.alloc(12));
    const key = Buffer.from(SECRET_KEY, 'utf8');
    const cipher = createCipheriv('aes-256-gcm', key, iv);
    const ciphertext = Buffer.concat([cipher.update(plaintext, 'utf8'), cipher.final()]);
    const tag = cipher.getAuthTag();
    return Buffer.concat([iv, ciphertext, tag]).toString('base64');
}

// ---------------------------------------------------------------------------
// Jeu de "rangers" reels (userapp <-> patrouilleur <-> telephone autorise)
// recupere depuis la base tpf locale - a adapter si les donnees changent.
// ---------------------------------------------------------------------------
const RANGERS = [
    { idUserApp: 12, login: 'bakoly', phone: '+261341638587' },
    { idUserApp: 13, login: 'bakomalala', phone: '+261383817421' },
    { idUserApp: 14, login: 'njara', phone: '+261382035996' },
    { idUserApp: 15, login: 'sedera', phone: '+261382318042' },
    { idUserApp: 16, login: 'tsiresy', phone: '+261346367580' },
    { idUserApp: 17, login: 'myrah', phone: '+261384984929' },
    { idUserApp: 18, login: 'mahaliana', phone: '+261381457929' },
];

const INTERVENTIONS = [1, 2, 3]; // Possible / Partielle / Impossible
const STATUSES = [1, 2, 3]; // Debut de feu / En cours / Maitrise
const DESCRIPTIONS = [
    'Depart de feu pres du sentier nord',
    'Fumee visible en lisiere de foret',
    'Feu de brousse en expansion',
    'Foyer secondaire signale par un riverain',
    'Feu maitrise, surveillance en cours',
];
const POINTS_REPERE = ['Pres du pont', 'A cote du village', 'Colline est', 'Bord de riviere', 'Piste principale'];
const DIRECTIONS = ['Nord', 'Sud', 'Est', 'Ouest'];

function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }
function randCoord(base, spread) { return (base + (Math.random() - 0.5) * spread).toFixed(6); }

function isoNow(offsetSeconds = 0) {
    return new Date(Date.now() + offsetSeconds * 1000).toISOString().slice(0, 19);
}

function buildAlertePlaintext() {
    const ranger = pick(RANGERS);
    const fields = {
        dateSignalement: isoNow(0),
        dateCommencement: isoNow(-60),
        idIntervention: pick(INTERVENTIONS),
        renfort: Math.random() < 0.3 ? 'true' : 'false',
        direction: pick(DIRECTIONS),
        surfaceApproximative: (Math.random() * 5000).toFixed(1),
        pointRepere: encodeURIComponent(pick(POINTS_REPERE)),
        description: encodeURIComponent(pick(DESCRIPTIONS) + ' [loadtest ' + Math.random().toString(36).slice(2, 8) + ']'),
        idUserApp: ranger.idUserApp,
        longitude: randCoord(47.0, 2.0),
        latitude: randCoord(-18.9, 2.0),
        idStatus: pick(STATUSES),
    };
    const plaintext = Object.entries(fields).map(([k, v]) => `${k}=${v}`).join('/');
    return { plaintext, phone: ranger.phone };
}

function buildLoginPlaintext() {
    const ranger = pick(RANGERS);
    // Mot de passe volontairement faux: on veut mesurer le cout CPU du
    // pipeline (dechiffrement + classification + bcrypt.matches), pas
    // reussir une vraie connexion.
    const plaintext = `login=${ranger.login}/password=loadtest-${Math.random().toString(36).slice(2, 8)}`;
    return { plaintext, phone: ranger.phone };
}

// ---------------------------------------------------------------------------
// Execution HTTP
// ---------------------------------------------------------------------------
async function sendOne(baseUrl, timeoutMs, scenario) {
    const { plaintext, phone } = scenario === 'login' ? buildLoginPlaintext() : buildAlertePlaintext();
    const message = encrypt(plaintext);

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);
    const startedAt = Date.now();
    try {
        const res = await fetch(`${baseUrl}/api/message`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'X-API-Key': API_KEY },
            body: JSON.stringify({ phoneNumber: phone, message }),
            signal: controller.signal,
        });
        const latencyMs = Date.now() - startedAt;
        const bodyText = await res.text();
        return { ok: res.ok, status: res.status, latencyMs, bodySample: bodyText.slice(0, 200) };
    } catch (err) {
        const latencyMs = Date.now() - startedAt;
        return { ok: false, status: 0, latencyMs, error: err.name === 'AbortError' ? 'timeout' : String(err.message || err) };
    } finally {
        clearTimeout(timer);
    }
}

async function syncPing(baseUrl, timeoutMs) {
    const ranger = pick(RANGERS);
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);
    const startedAt = Date.now();
    try {
        const res = await fetch(`${baseUrl}/sync/status`, {
            headers: { 'X-API-Key': API_KEY },
            signal: controller.signal,
        });
        const latencyMs = Date.now() - startedAt;
        await res.text();
        return { ok: res.ok, status: res.status, latencyMs };
    } catch (err) {
        const latencyMs = Date.now() - startedAt;
        return { ok: false, status: 0, latencyMs, error: err.name === 'AbortError' ? 'timeout' : String(err.message || err) };
    } finally {
        clearTimeout(timer);
    }
}

async function worker(id, baseUrl, timeoutMs, scenario, deadline, results) {
    while (Date.now() < deadline) {
        let result;
        if (scenario === 'mixed') {
            const roll = Math.random();
            if (roll < 0.55) result = await sendOne(baseUrl, timeoutMs, 'alerte');
            else if (roll < 0.75) result = await sendOne(baseUrl, timeoutMs, 'login');
            else result = { ...(await syncPing(baseUrl, timeoutMs)), scenarioTag: 'sync' };
        } else {
            result = await sendOne(baseUrl, timeoutMs, scenario);
        }
        results.push(result);
    }
}

function percentile(sortedArr, p) {
    if (sortedArr.length === 0) return 0;
    const idx = Math.min(sortedArr.length - 1, Math.ceil((p / 100) * sortedArr.length) - 1);
    return sortedArr[Math.max(0, idx)];
}

function summarize(results, elapsedSec) {
    const latencies = results.map(r => r.latencyMs).sort((a, b) => a - b);
    const ok = results.filter(r => r.ok);
    const failed = results.filter(r => !r.ok);
    const errorCounts = {};
    for (const r of failed) {
        const key = r.error ? r.error : `HTTP ${r.status}`;
        errorCounts[key] = (errorCounts[key] || 0) + 1;
    }

    console.log('\n========== RESULTATS ==========');
    console.log(`Duree              : ${elapsedSec.toFixed(1)}s`);
    console.log(`Requetes totales   : ${results.length}`);
    console.log(`Succes             : ${ok.length} (${((ok.length / results.length) * 100 || 0).toFixed(1)}%)`);
    console.log(`Echecs             : ${failed.length} (${((failed.length / results.length) * 100 || 0).toFixed(1)}%)`);
    console.log(`Debit              : ${(results.length / elapsedSec).toFixed(2)} req/s`);
    console.log('--- Latence (ms) ---');
    console.log(`min / avg / max    : ${latencies[0] || 0} / ${(latencies.reduce((a, b) => a + b, 0) / (latencies.length || 1)).toFixed(0)} / ${latencies[latencies.length - 1] || 0}`);
    console.log(`p50 / p90 / p95 / p99 : ${percentile(latencies, 50)} / ${percentile(latencies, 90)} / ${percentile(latencies, 95)} / ${percentile(latencies, 99)}`);
    if (Object.keys(errorCounts).length > 0) {
        console.log('--- Erreurs (par type) ---');
        for (const [key, count] of Object.entries(errorCounts)) {
            console.log(`  ${key}: ${count}`);
        }
        const sample = failed.find(r => r.bodySample);
        if (sample) console.log(`  Exemple de reponse: ${sample.bodySample}`);
    }
    console.log('================================\n');
}

async function main() {
    const args = parseArgs(process.argv.slice(2));
    console.log(`Cible: ${args.url} | VUs: ${args.vus} | Duree: ${args.duration}s | Scenario: ${args.scenario}`);
    console.log(`(Rappel: cible en local recommandee pour ce test - pas de ngrok, cf. discussion precedente.)\n`);

    const results = [];
    const startedAt = Date.now();
    const deadline = startedAt + args.duration * 1000;

    const workers = [];
    for (let i = 0; i < args.vus; i++) {
        workers.push(worker(i, args.url, args.timeoutMs, args.scenario, deadline, results));
    }
    await Promise.all(workers);

    const elapsedSec = (Date.now() - startedAt) / 1000;
    summarize(results, elapsedSec);
}

main().catch(err => {
    console.error('Erreur fatale du test de charge:', err);
    process.exit(1);
});
