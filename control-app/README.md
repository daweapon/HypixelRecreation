# Hypixel Control

Native operations console for the HypixelRecreation stack. One Flutter codebase builds a native
Android APK (phone + Galaxy Tab) and a native Windows `.exe`. It is a client only — all logic
lives behind `service.api`.

Roadmap item **#33**. **Phases 1, 2 and 3 are built.**

- **Phase 1:** fleet dashboard, shared-dependency health, staggered lifecycle control, log
  tailing, the watchdog, the audit log, jar deploys and owner recovery.
- **Phase 2:** push alerts to a phone through ntfy, scheduled Mongo + Redis backups with
  restore, the version-skew view with reconcile, proxy-registration state, the config-drift
  detector and post-restart verification of self-deploys.
- **Phase 3:** exception triage across every container's logs, an optional TOTP second factor
  on OWNER actions, spark metrics and the remaining operations screens.

Each capability has its own section below. FCM push is the one deliberate omission; see
[Alerting](#alerting).

## Architecture

```
  Flutter app  ──HTTPS/HTTP──>  service.api (8080)  ──internal network──>  service_control (9090)
   device token                  session + roles                            Docker Engine API
   + access token                authenticated proxy                        /var/run/docker.sock
```

`service_control` is a separate container, not part of `service.api`, because it holds the
Docker socket. That socket is root-equivalent on the host, so it is deliberately kept out of
the process that also serves public CONSUMER API traffic. The agent publishes **no ports** — it
is reachable only from inside `hypixel_network`.

The agent has **no Redis or Mongo dependency**. Its job is to report on the stack when the
stack is broken, so it must start when Mongo and Redis are down. That is the exact outage it
exists to catch.

## Deploying the backend

1. Build the jars:
   ```bash
   cd ~/hypixel-dev
   ./gradlew :service.control:shadowJar :service.api:shadowJar
   ```
2. Copy them next to the other service jars so the image build picks them up:
   ```bash
   cp service.control/build/libs/ServiceControl.jar  <stack>/DockerFiles/jars/
   cp service.api/build/libs/ServiceAPI.jar          <stack>/DockerFiles/jars/
   ```
   On the Synology the stack root is `/volume1/docker/hypixel-skyblock`. Build on the Windows
   box — never let Gradle run on the NAS — then `scp` the jars across.
3. Set a shared secret in a `.env` file beside `docker-compose.yml`:
   ```
   CONTROL_TOKEN=<32+ random characters>
   ```
   Compose **fails loudly** if `CONTROL_TOKEN` is unset rather than starting with an empty one.
   The optional settings are listed in [Environment](#environment).
4. Rebuild and start:
   ```bash
   sudo docker compose build game_server_builder
   sudo docker compose up -d service_control service_api
   ```
5. Confirm the agent sees the socket:
   ```bash
   sudo docker exec service_control curl -s localhost:9090/control/ping
   # {"ok":true}
   ```

## Pairing a device

Enrolment reuses the existing in-game handshake rather than adding a second credential path:

1. Open the app, enter the server address (e.g. `http://192.168.100.10:8080`), tap
   **Request code**.
2. Run `/verifyapi <code>` in game. The command is already STAFF-gated.
3. Tap **Complete pairing**.

The **first device ever paired becomes OWNER**. Every later device starts **READ_ONLY** and must
be promoted by an owner — a device that can stop containers is never handed out by default.

| Role | Can do |
|---|---|
| `READ_ONLY` | View fleet, logs, alerts, backups, exception triage, audit log |
| `OPERATOR` | Everything above, plus start / stop / restart and **back up now** |
| `OWNER` | Everything above, plus deploy, restore, backup settings, test alerts, the second factor, and enrolling, promoting and revoking devices |

Tokens: the long-lived **device token** is held in platform secure storage and is only ever sent
to exchange for a 15-minute **access token**, which is what every control call actually carries.
Only the SHA-256 hash of a device token is stored server-side.

## Recovering owner access

Promotion normally requires an existing OWNER device. When none is usable — the device was
lost, or the app was uninstalled, which wipes secure storage — the pairing screen's
**Lost the owner device?** toggle claims OWNER directly.

Authority is the same STAFF-gated `/verifyapi` handshake, so no bearer token is needed: the
caller by definition has no working credential. The server decides:

| Situation | Result |
|---|---|
| Your own account already owns a live owner device | Allowed — you are replacing your own hardware |
| No owner device has checked in for 72 hours | Allowed — the owner set is presumed lost |
| No owner device has ever existed | Allowed |
| Somebody else's owner device is still active | **Refused**, naming who to ask for promotion |

Leave **Revoke my previous owner devices** ticked when the old device is genuinely gone; it
only ever revokes devices belonging to *your* account, never another operator's. Every attempt,
refusals included, is recorded in the audit log as `owner-recovery`.

The idle window is `CONTROL_OWNER_RECOVERY_IDLE_HOURS` in the stack `.env` (default 72).

## Building the app

Android (works from WSL/Linux):
```bash
flutter build apk --release
# build/app/outputs/flutter-apk/app-release.apk
```

Windows — **must be built on Windows**, with Visual Studio's C++ desktop workload installed.
A Windows `.exe` cannot be produced from WSL:
```powershell
flutter build windows --release
# build\windows\x64\runner\Release\
```

The build also needs symlink support, which means either Developer Mode enabled or an elevated
shell; without it Flutter stops at `Building with plugins requires symlink support`. Driving it
from WSL works, but `cmd.exe` cannot start in a `\\wsl.localhost\...` directory, so copy the
source onto `C:` first and build from there.

Ship the whole `Release\` directory, not just the `.exe` — it needs the DLLs and `data/`
alongside it.

Distribution is self-hosted: serve the APK off the NAS and sideload it. No app store, and
choosing Android over iOS avoids a $99/yr Apple Developer dependency.

## Design

Dark operations console, "Reactor HUD" treatment: ground `#06090D`, arc-reactor cyan `#49D4E8`,
amber `#E8A33D` warnings, `#FF6F5E` critical. Chakra Petch for labels over IBM Plex Mono for all
data. Chamfered panels, hairline borders, no rounded cards. Both fonts are bundled rather than
fetched at runtime, so the console renders correctly when the network is the thing that is broken.

The three shared dependencies are drawn as three arcs of **one reactor ring** with a single
integrity figure at its centre, so "lead with shared-dependency health" is literally the largest
object on screen rather than three tiles among many.

Status is always **colour plus a short text code** (`UP` / `WRN` / `LOOP` / `EXIT` / `DWN`) so it
never depends on colour alone.

**Motion means attention is needed.** A nominal stack is an almost-still screen; faults breathe.
Separately, a slow radar sweep on the outer ring says the view is live rather than a frozen
capture. Everything is behind a reduced-motion guard.

## Environment

All optional except `CONTROL_TOKEN`. They go in the stack `.env`; the reference
`docker-compose.yml` already passes them to `service_control`.

| Variable | Default | Effect |
|---|---|---|
| `CONTROL_TOKEN` | required | Shared secret between `service_api` and the agent |
| `ALERT_NTFY_URL` | unset | ntfy topic URL, e.g. `https://ntfy.sh/hypixel-ops-<random>` |
| `ALERT_NTFY_TOKEN` | unset | Bearer token, only for a protected topic |
| `ALERT_WEBHOOK_URL` | unset | Generic JSON webhook (Gotify, Discord bridge, ...) |
| `BACKUP_SCHEDULE` | `daily@04:00` | `off`, `daily@HH:MM` or `every <n>h` / `every <n>m` (minimum 15 min) |
| `BACKUP_RETAIN` | `14` | Newest complete backups kept |
| `BACKUP_TIMEZONE` | container zone (UTC) | Zone for `daily@` times, e.g. `America/Chicago` |
| `BACKUP_DIR` | `/app/backups` | Where backups are written inside the agent |
| `TRIAGE_PROJECT_PACKAGES` | `dev.daweapon.,net.swofty.` | Packages counted as "project" frames when grouping exceptions |
| `CONTROL_OWNER_RECOVERY_IDLE_HOURS` | `72` | See [Recovering owner access](#recovering-owner-access) |

Schedule and retention changed from the app are saved to `backups/control/settings.json` and
win over the env values from then on.

**The NAS runs its own generated compose plus `docker-compose.override.yml`**, so the repo
compose alone changes nothing there. Add these under `service_control` in the override:

```yaml
    environment:
      ALERT_NTFY_URL: "${ALERT_NTFY_URL:-}"
      ALERT_NTFY_TOKEN: "${ALERT_NTFY_TOKEN:-}"
      BACKUP_TIMEZONE: "America/Chicago"
    volumes:
      - ./backups:/app/backups
```

then `sudo docker compose up -d service_control`. Without the `./backups` mount the agent still
runs: the Backups screen says the directory is not mounted and backup-now is disabled.

## Alerting

The watchdog polls every 30s and alerts on **transitions**, requiring a condition to hold for 3
consecutive polls before firing — without that, every routine staggered restart would page you.
Recovery is announced too, with how long the condition lasted.

**Deliberate operations never page.** While a staggered start/stop/restart, a deploy or a
restore is running, nothing the watchdog sees counts towards the 3-poll confirmation; a fault
must persist for the full window *after* the operation ends. Recoveries still go out.

Transports, all at once:

- **log** - always on.
- **ntfy** (`ALERT_NTFY_URL`) - the recommended phone channel. Install the ntfy app, subscribe to
  a private, hard-to-guess topic, set the URL. Critical alerts are priority 5 (bypasses do-not-
  disturb on Android if you allow it), warnings 4, recoveries and tests 3, each tagged with an
  emoji and the alert kind (`dependency`, `loop`, `backup`).
- **webhook** (`ALERT_WEBHOOK_URL`) - plain JSON POST for anything else.

The **Alerts** screen shows the watchdog state, configured channels, active conditions and the
last 100 alerts, and an OWNER can **send a test alert** that reports what each transport
answered. Do this once after setting the URL; a channel nobody has seen deliver is not one to
trust. Failed scheduled backups alert too (`backup:scheduled`), with a recovery on the next
good one.

**FCM is not built**, deliberately: it needs a Firebase project and `google-services.json`. The
seam is `AlertTransport` - an `FcmAlertTransport` registered in
`AlertDispatcher.fromEnvironment()`, plus a service.api endpoint for the app to register its
device token and `firebase_messaging` in the app.

## Backups and restore

The agent backs up **Mongo and Redis** on a schedule (default daily 04:00, keep 14) and on
demand. It holds no database credentials by design, so both run *inside* the database
containers through Docker exec and are copied out through the Engine API:

- Mongo: `mongodump --archive --gzip` in `hypixel_mongo`, copied to `mongo.archive.gz`.
- Redis: `BGSAVE` in `hypixel_redis`, waits for `LASTSAVE` to move and the save to report ok,
  then copies the RDB to `redis.rdb`.

Each backup is a folder `backups/control/<id>/` with a `manifest.json` recording sizes and
sha256s. A backup where one database failed is kept as PARTIAL. Retention only ever touches the
agent's own folders: **hand-made dumps elsewhere in `backups/` are listed, never pruned and
never restored from.** Pre-restore snapshots rotate separately (newest 3), and a failed backup is
pruned only once a newer complete one exists.

**Back up now** needs OPERATOR. **Settings** and **restore** need OWNER (and the second factor
once enrolled). Restore is two-step on the server, not just in the UI: the first request returns
a preview of every step and a two-minute single-use token bound to that device, backup, parts and
mode; only the second request with that token runs it. Both steps are audited
(`restore-request`, `restore`).

| Mode | Mongo | Redis |
|---|---|---|
| **Scratch** (default) | Restored into `restore_<db>` databases beside the live ones | Verified with `redis-check-rdb`; live Redis untouched |
| **Live** | `mongorestore --drop` into the live databases | Redis stopped, data files replaced, started |

A live restore **always takes a fresh pre-restore backup first and aborts if it fails**, so a
live restore can itself be undone. The preview warns when game servers are running, since they
will write their in-memory state over restored data; stop them first unless that is intended.
With AOF on, Redis ignores `dump.rdb`, so a live Redis restore also writes a new AOF manifest
whose base file is the backup's RDB (with a sequence number no existing file uses).
`admin`, `config` and `local` are never restored.

To check a restore: restore in scratch mode, then compare a known document in `restore_Minestom`
with the live one (`mongosh` in `hypixel_mongo`). Drop the `restore_*` databases when done.

## Exception triage

The **Exceptions** screen scans the last 2000 log lines of every project container and groups
stack traces and ERROR lines by **signature**: exception class plus the first stack frame in the
project's own packages, without line numbers, so one bug thrown from every game server across
several builds is one row. ERROR lines with no trace group by message with numbers, UUIDs and
hex normalised. Each group shows its count, the containers it came from, first/last seen and a
sample trace. Boot-race noise is hidden by default, as in the log view. Read-only, any role;
results are cached for a minute.

## Second factor (optional TOTP)

Off until enrolled, and **nothing changes until then**. From the **Second factor** screen an
OWNER device enrols: the server returns a secret (as an `otpauth://` URI and as grouped text to
type into any authenticator app) and enforces nothing until one code from the app is confirmed.

Once enrolled, these need a current code (the app prompts for it):
deploy, restore, device role change and revoke, backup settings, reconcile, replacing or removing
the secret, and owner recovery. A missing or wrong code is refused (HTTP 403) and audited as
DENIED. The code travels in the `X-Control-TOTP` header; owner recovery, which has no bearer
token, also accepts a `totp` body field.

One secret covers the install, not each device. Codes are valid for their 30s step plus one
either side and are not single-use, so one code can cover two actions in quick succession; a
code is useless without the device's bearer token. The secret is stored in Mongo
`api-control-totp` (TOTP needs it in usable form). **Lost the authenticator:** delete the
`{_id: "install"}` document from that collection with a shell on the NAS.

## Release routine

1. Bump `version:` in `control-app/pubspec.yaml` (the app shows it in the corner).
2. Build the backend jars on the dev box, never on the NAS:
   `./gradlew :service.control:shadowJar :service.api:shadowJar`.
3. Deploy `ServiceAPI.jar` from the app (OWNER). Update `ServiceControl.jar` by hand - see
   [Updating or recovering the agent](#updating-or-recovering-the-agent) - since the agent
   cannot deploy itself. Apply any compose/override changes first.
4. Build the APK (WSL): `flutter build apk --release`.
5. Build the Windows app on the PC (Developer Mode on): `flutter build windows --release`, zip
   `Release\` as `hypixel-control-<ver>-windows.zip`.
6. Copy both to `control-deploy/` (`windows-pc/` for the zip), regenerate `SHA256SUMS`, and
   sideload the APK on each phone/tablet.

## Audit log

Every action taken through the app is recorded to an append-only Mongo collection
(`api-control-audit`): who, which device, what role, what action, which containers, the
outcome and when. **Denied attempts are recorded too** - a log of successes alone cannot answer
"who tried to stop the fleet and was refused", which is the question that matters once more
than one person holds a paired device. Device enrolments, role changes and revocations are
recorded as well.

Readable by any paired device, not owners only: the people who can press the buttons should be
able to see what has been pressed. View it from the receipt icon in the command bar, or
`GET /api/control/audit?limit=200`.

Honest limitation: this is append-only *by construction in the application* - nothing in the
service can update or delete an entry - but it is not cryptographically tamper-evident. Anyone
with direct Mongo access on the NAS can still edit the collection, and since Docker socket
access already implies host root, that is not a boundary this layer can enforce. It answers
"who did what" for accountability, not against an adversary who already owns the box.

## Deploying from the app

Deploys run without the operator holding any privilege on the NAS. You copy a built jar into
the **drop folder** over SMB (`\\thelibrary\home\Hypixel\control-deploy`), then press
**Deploy** in the app. The agent does the privileged part.

What a deploy does, in order:

1. **Hash and verify** the jar in the drop folder before touching anything.
2. **Persist** it to `DockerFiles/jars/` so a future image rebuild cannot silently revert the
   deploy - the stale-jar trap that has cost this project real work before.
3. **Push** it into each target container over the Engine API (the equivalent of `docker cp`),
   then stat it back to confirm it actually landed.
4. **Restart** the targets one at a time with the usual stagger, never in parallel.
5. **Confirm** each container came back up, reporting per-step rather than pass/fail overall.

Targets are derived from the jar name rather than chosen by whoever presses the button:
`HypixelCore.jar` goes to **every** game server (a partial rollout is the version-skew bug that
once looked like a wiped inventory), `ServiceFoo.jar` goes to its own `service_foo` container.
A jar whose targets cannot be resolved is refused rather than guessed at.

**`ServiceControl.jar` cannot be deployed from the app.** Applying it restarts the agent that is
running the deploy, and on 2026-10-05 that killed the agent mid-job, left the jar missing from
`DockerFiles/jars/`, and destroyed the `service_control` container - with no way back from the
app, since every container action goes through the agent. Both the API and the agent now refuse
it (HTTP 409 / 400, recorded as DENIED in the audit log). Update the agent by hand instead - see
[Updating or recovering the agent](#updating-or-recovering-the-agent).

**Deploying requires OWNER**, not OPERATOR. Restarting existing code and pushing arbitrary new
code are different grants and do not share a role. Both are recorded in the audit log, including
refused attempts.

Mounts involved (see `docker-compose.override.yml`): the drop folder is mounted **read-only** at
`/app/deploy-drop` (override with `DEPLOY_DROP_DIR` in `.env`), and `DockerFiles/jars` is mounted
read-write at `/app/jar-store`.

### Shipping a content update

A code change ships as a jar, so that half of the round trip stays inside the app. Only
`loader` produces a deployable artifact - it fat-jars every `type.*` module into
`HypixelCore.jar`, so build that whichever module you actually edited:

```bash
./gradlew :loader:shadowJar
```

Copy the built jar to `\\thelibrary\home\Hypixel\control-deploy` over SMB, press **Deploy**,
pick it. The drop folder is the operator's own home share, so writing to it needs no privilege
on the NAS.

**Configuration YAML is not a jar change.** The server reads `./configuration`, which
`Dockerfile.game_server` fills once at build time; the live host config is mounted at
`./configuration_files`. `ConfigurationOverlay` copies the mount over the baked copy at startup,
so write the YAML to `\\thelibrary\docker\hypixel-skyblock\configuration\` over SMB and
restart the game servers from the app.

`ServiceAPI.jar` restarts the container serving the request, which is worth expecting rather
than debugging:

- `ServiceAPI.jar` restarts the API the app is talking to, so the job completes but the app
  loses sight of it part-way and shows an error. Reopen the app; the state will be correct.

### What a deploy cannot do

Still needs a shell on the NAS, so keep a tested administrator account for them:

- Compose or `.env` changes, including adding a container or a new service
- Image rebuilds where a `Dockerfile` itself changed - deploys replace jars inside existing
  images, they do not rebuild
- Non-jar content other than `configuration/` YAML: world data, schematics and anything else
  not shipped inside a jar
- Updating `ServiceControl.jar` itself
- Any recovery while `service_api` or `service_control` is down, which is when the app is least
  able to help

### Updating or recovering the agent

`ServiceControl.jar` is bind-mounted from `DockerFiles/jars/ServiceControl.jar`. If that file is
missing, Docker creates a *directory* at the mount point and the agent fails to start even after
a correct recreate, so restore the file first.

1. Copy the new jar to `DockerFiles/jars/ServiceControl.jar` in the stack directory. The
   directory is root-owned, so this needs a shell (`sudo cp`) or a DSM Task Scheduler task run
   as root - the SMB account cannot write it.
2. Restart the agent: DSM -> Container Manager -> `service_control` -> Restart, or
   `sudo docker restart service_control`.
3. If the container no longer exists (DSM shows a row whose Start fails with
   `No such container`), recreate it from compose: DSM -> Container Manager -> Project ->
   `hypixel-skyblock` -> Action -> Build, or `sudo docker compose up -d service_control` in
   `/volume1/docker/hypixel-skyblock`. DSM Task Scheduler runs with a minimal `PATH`, so use
   `/usr/local/bin/docker` there.

Game servers, the proxy and `service_api` keep running throughout; only the app's fleet
actions are unavailable until the agent is back.

## Fleet integrity

The Integrity screen (`IntegrityScreen`) and the dashboard's row tags answer "is everything
running what it should, where it should":

| Check | What it compares | Action (role) |
|---|---|---|
| Version skew | sha256 of the jar inside each container (hashed in place) against `DockerFiles/jars` (`/app/jar-store`); without a stored copy, against the other containers running that jar | **Reconcile** pushes the stored jar into the skewed containers through the deploy pipeline (OWNER) |
| Proxy registration | the proxy's server list (asked over Redis by `service.api`) against running game servers, matched by registered hostname | **Re-register** restarts a DELISTED server, the only way it registers again (OPERATOR) |
| Config drift | each game server's `/app/configuration` (the pack server's `resourcepacks/` only) against the host `configuration/` | **Sync** copies the differing files in and restarts, pack server first (OWNER) |
| Resource pack | the hash each game server advertises against what `hypixel_pack_server` answers for; LIVE / DEAD / STALE | **Verify download** checks the served zip's SHA-1 |
| Memory budget | `mem_limit` of running containers plus a start's candidates against host RAM from Docker `/info`, less `CAPACITY_HOST_RESERVE_MB` (default 4096) | the start confirmation turns red when over budget |

DELISTED servers also raise a watchdog alert (`delisted:<container>`), with the usual
3-poll debounce and a recovery notice. Every finished deploy, reconcile, config sync and agent
self-restart is written to the audit log as `deploy-verify` with OK or FAILED, by whichever
`service.api` is running once it finishes - which is how a `ServiceAPI.jar` deploy, which restarts
the API, still gets a verdict. The agent going unreachable for 3 minutes is audited as
`control-agent-unreachable`.

Production needs, beyond the new jars:

- The **config-drift** check reads the host tree through a read-only mount. Add to
  `service_control`'s `volumes:` in `docker-compose.override.yml`, then recreate it:
  ```yaml
      - ./configuration:/app/host-configuration:ro
  ```
  Without it the screen says so and names this line; nothing else is affected.
- **Proxy registration** needs the rebuilt `SkyBlockProxy.jar` (it now answers server-list
  queries from services and reports each server's host). Until then every row reads UNKNOWN.
  The proxy jar is baked into its image, so this is a DSM rebuild, not an app deploy.
- Optional `.env`/environment: `CAPACITY_HOST_RESERVE_MB`, `CAPACITY_UNLIMITED_ESTIMATE_MB`
  (default 1024, for containers with no `mem_limit` and no `-Xmx`), `PACK_PUBLIC_URL`,
  `PACK_FALLBACK_URL`, `CONFIG_DRIFT_IGNORE` (default `config.yml,config.example.yml`).

## Known gaps

- **FCM push** is not implemented (needs a Firebase project); ntfy covers the need.
- **TPS-threshold alerting** (capability 14) needs the vendored `spark` metrics surfaced first.
- **Proxy-registration state** (capability 3) is not yet separated from container state — the
  dashboard currently shows whether the proxy *container* is up, not whether each game server is
  *registered* with it.
- **Backups cover Mongo and Redis only**, not world data, schematics or `configuration/`.
- A **live Mongo restore** replaces collections present in the backup; collections created since
  are left in place.
- Cross-server player and event admin, backups and spark metrics are not built.
- **Owner recovery is rate-limited only by the idle window**, not by attempt count; a STAFF
  account is trusted absolutely once no live owner remains.
