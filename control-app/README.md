# Hypixel Control

Native operations console for the HypixelRecreation stack. One Flutter codebase builds a native
Android APK (phone + Galaxy Tab) and a native Windows `.exe`. It is a client only — all logic
lives behind `service.api`.

Roadmap item **#33**. This is **Phase 1**: fleet dashboard, shared-dependency health, staggered
lifecycle control, log tailing, and push alerting.

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
   # optional: delivers alerts to a phone today, before FCM is wired up
   ALERT_WEBHOOK_URL=https://ntfy.sh/your-private-topic
   ```
   Compose **fails loudly** if `CONTROL_TOKEN` is unset rather than starting with an empty one.
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
| `READ_ONLY` | View fleet, logs, alerts |
| `OPERATOR` | Everything above, plus start / stop / restart |
| `OWNER` | Everything above, plus enrol, promote and revoke devices |

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

## Alerting

The watchdog polls every 30s and alerts on **transitions**, requiring a condition to hold for 3
consecutive polls before firing — without that, every routine staggered restart would page you.
Recovery is announced too.

Alerts currently go to the log plus an optional webhook (`ALERT_WEBHOOK_URL`). FCM is **not**
wired up yet, and deliberately so: an FCM device token can only come from an installed app, so
the webhook is what delivers to a phone today. Point it at an ntfy/Gotify topic.

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

`ServiceControl.jar` is bind-mounted, so replacing the stored file *is* the deploy and no push
is needed; the agent handles that distinction itself.

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

**Configuration YAML is not a jar change, and the app cannot ship it.** The server reads
`./configuration`, which `Dockerfile.game_server` fills once at build time by copying
`configuration_files` into it. The compose bind mount covers `configuration_files`, which
nothing reads back, and the entrypoint only ever rewrites `config.yml`. A restart therefore
keeps serving the copy baked into the image. Changing anything under `configuration/` needs a
shell on the NAS - see below.

Two deploys restart the container serving the request, which is worth expecting rather than
debugging:

- `ServiceAPI.jar` restarts the API the app is talking to, so the job completes but the app
  loses sight of it part-way and shows an error. Reopen the app; the state will be correct.
- `ServiceControl.jar` restarts the agent running the job, so it is effectively fire-and-forget
  and the post-restart verification never runs.

### What a deploy cannot do

Still needs a shell on the NAS, so keep a tested administrator account for them:

- Compose or `.env` changes, including adding a container or a new service
- Image rebuilds where a `Dockerfile` itself changed - deploys replace jars inside existing
  images, they do not rebuild
- Non-jar content: `configuration/` YAML, world data, schematics and anything else not
  shipped inside a jar
- Any recovery while `service_api` or `service_control` is down, which is when the app is least
  able to help

## Known gaps

- **FCM push** is not implemented; the webhook transport covers the need for now.
- **TPS-threshold alerting** (capability 14) needs the vendored `spark` metrics surfaced first.
- **Proxy-registration state** (capability 3) is not yet separated from container state — the
  dashboard currently shows whether the proxy *container* is up, not whether each game server is
  *registered* with it.
- Phases 2 and 3 (jar deploy with verification, config-drift detector, cross-server player and
  event admin, backups, spark metrics) are untouched.
- **Owner recovery is rate-limited only by the idle window**, not by attempt count; a STAFF
  account is trusted absolutely once no live owner remains.
