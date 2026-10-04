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

## Known gaps

- **FCM push** is not implemented; the webhook transport covers the need for now.
- **TPS-threshold alerting** (capability 14) needs the vendored `spark` metrics surfaced first.
- **Proxy-registration state** (capability 3) is not yet separated from container state — the
  dashboard currently shows whether the proxy *container* is up, not whether each game server is
  *registered* with it.
- Phases 2 and 3 (jar deploy with verification, config-drift detector, cross-server player and
  event admin, backups, spark metrics) are untouched.
