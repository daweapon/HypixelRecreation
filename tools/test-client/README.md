# SkyBlock test client

A Mineflayer bot that joins the local stack as a staff test player, so scenarios can be scripted from the shell.
Offline accounts only; no real credentials are used or stored.

## Run it

```bash
/home/bradl/.hypixel-skyblock/start-servers.sh           # local stack
tools/test-client/scripts/offline-proxy.sh on            # once per proxy container: offline accounts + ViaVersion
tools/test-client/scripts/grant-staff.sh ClaudeBot       # once per test user, after its first join
cd tools/test-client && npm install && ./run.js all      # or: ./run.js 02-skyblock-menu --json
```

Exit code is 0 only when every scenario passes. Plain output goes to the terminal and the full JSON to
`last-result.json`; `--json` prints the JSON instead. `./run.js --list` shows the scenarios.

Ad-hoc commands: `SB_SKYBLOCK=1 SB_COMMANDS='["/set coins 5","/menu"]' ./run.js adhoc --json`.

Redeploy-and-retest loop (local stack only, never the NAS): `scripts/redeploy-local.sh` stops the stack, builds
`:loader:shadowJar`, installs it as the local `HypixelCore.jar`, syncs tracked `configuration/` files, rebuilds the image
and restarts everything in test mode. Then rerun the scenarios.

## How it connects

- Proxy: Velocity, modern forwarding. `online-mode` is true, so `offline-proxy.sh on` flips it to false in the
  proxy container (restore with `off`). It also turns on `integrations.via-version`.
- Backend: Minestom 26.2 (protocol 776). Mineflayer 4.39 only knows 26.1, so the bot joins as 26.1 and the proxy's
  ViaVersion translates. Joining as 26.1 directly is refused by the backend ("please use 26.2").
- Proxy restarts drop the game servers' registration: restart them after any proxy restart, or flip the proxy first.
- Resource pack: answered by the bot (accepted, downloaded, loaded) without downloading it. `--pack decline` tests the kick.
  Mineflayer's own reply is malformed (UUID object), so the harness answers itself.
- Rank: STAFF is written into `hsb:acct:players:<offline uuid>` in local Redis while the bot is offline.

## Writing scenarios

`scenarios/<name>.js` exports `{ run(t, bot) }`. `t.step(description, fn)` records pass/fail, `t.assert*` fail a step.
Useful `bot` calls: `command`, `waitChat`, `chatSince`, `sidebar`, `tabList`, `bossBars`, `entities`, `findNpc`,
`walkNear` (teleports), `interact`, `attack`, `useHotbarItem`, `waitWindow`, `window`, `findSlot`, `click(slot,{button,mode})`,
`detectInertMenu`, `joinSkyblock`, `snapshot`. The raw mineflayer bot is `bot.bot`.

## What it cannot test

- Anything visual: resource-pack models, textures, GUI tile colours, particles, held-item rendering, hologram layout.
- Audio beyond sound-event names.
- Real physics-based walking, parkour feel and lag; movement is mostly `/tp` plus look/interact.
- Production behaviour: the local jar and configuration, ViaVersion on the proxy (production has it off), offline UUIDs.
- Real-account flows: Microsoft auth, skins, the Cloudflare/NAS pack download path, off-LAN pack kicks.
