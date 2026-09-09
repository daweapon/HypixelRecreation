# HypixelRecreation SkyBlock Server — Session Handoff Notes

Context for picking up work on this project. Player: `daweapon`, staff/OP rank. Server: [Swofty's HypixelRecreation](https://github.com/Swofty-Developments/HypixelRecreation) (Minestom-based Hypixel SkyBlock clone), running on Windows WSL2/Ubuntu via Docker.

## Key locations

- `~/hypixel-dev` — **full development clone** (real git clone, has actual `.java` source). This is where all code edits happen.
- `~/.hypixel-skyblock/_repo` — the repo used by the **running Docker install**. This is a *sparse checkout* (only `DockerFiles/` and `configuration/` — no source code). Editing here doesn't touch Java source; it's only relevant for deployment.
- `~/.hypixel-skyblock/_repo/DockerFiles/jars/HypixelCore.jar` — if a jar exists here, Docker's build uses it *instead of* downloading the stock release jar from GitHub. This is how local fixes survive a fresh install/rebuild.
- `~/.hypixel-skyblock/_repo/configuration/` — the config baked into the Docker image at build time. Must be updated here (in addition to `~/hypixel-dev/configuration/`) for YAML changes to survive a full rebuild.
- `~/.hypixel-skyblock/configuration/` — a *different* directory, bind-mounted for world files / resource packs only (not item YAML).

## The core dev/deploy loop

1. Edit code in `~/hypixel-dev`.
2. `cd ~/hypixel-dev && ./gradlew :type.skyblockgeneric:compileJava` (fast sanity check on one module).
3. `./gradlew :loader:shadowJar` → produces `~/hypixel-dev/loader/build/libs/HypixelCore.jar` (the full fat jar, ~71MB).
4. Deploy to a **running container** for live testing:
   ```bash
   docker cp ~/hypixel-dev/loader/build/libs/HypixelCore.jar hypixelcore_skyblock_island:/app/HypixelCore.jar
   # if YAML config changed too:
   docker cp ~/hypixel-dev/configuration/skyblock/items/.../foo.yml hypixelcore_skyblock_island:/app/configuration/skyblock/items/.../foo.yml
   docker restart hypixelcore_skyblock_island
   ```
5. Once verified working, **persist** so it survives a full reinstall:
   ```bash
   cp ~/hypixel-dev/loader/build/libs/HypixelCore.jar ~/.hypixel-skyblock/_repo/DockerFiles/jars/HypixelCore.jar
   cp ~/hypixel-dev/configuration/skyblock/items/.../foo.yml ~/.hypixel-skyblock/_repo/configuration/skyblock/items/.../foo.yml
   ```

**Gotcha hit repeatedly tonight:** VS Code edits sometimes don't actually save to disk (paste can silently truncate, or Find & Replace results were shown but never `Ctrl+S`'d). Always verify with `cat`/`wc -l`/`tail` from the terminal after editing — don't trust what was pasted back into chat as proof the file is correct.

## Docker/infra quirks (all confirmed, all have known fixes)

- **Two orchestration paths conflict.** The install's TUI manager (`~/.hypixel-skyblock/.cache/skyblock-installer/...`) and manually running `docker compose` from `_repo` produce *differently-named containers* for the same servers (e.g. `hypixelcore_skyblock_hub` vs `hypixelcore_hub`). Running both creates duplicate/competing container sets. **Prefer the manager's own Start/Stop/Rebuild** over manual `docker compose` commands when possible.
- **"No X servers available" / missed pings.** Restarting any game server (especially island/hub, which are heavier — resource pack rebuild, etc.) can cause a brief CPU spike that makes `prototype_lobby` (or others) miss the proxy's ping deadline (~300ms) 5 times in a row, getting de-listed. Fix: `docker restart hypixelcore_prototype_lobby` (or whichever server) to force re-registration. This is not a crash — the container itself stays healthy the whole time.
- **World files & resource pack don't persist across container recreation** (`docker rm` + fresh `up`, not just `restart`). Must be re-copied into each game server + `hypixel_pack_server` after any full reset. World files live at `~/.hypixel-skyblock/configuration/world/`, real resource pack at `/tmp/real_pack/` on the host (hash `08b772607390bbdb4201c070db649e2f931d1113`).
- **Staff rank Redis fix** may be needed after a Redis container recreation — script at `/tmp/set_staff.lua`, must be re-copied into `hypixel_redis` each time. Must be run while the player is disconnected.

## Real bugs found & fixed tonight

### 1. Severe TPS drop (~0.5 TPS on Island) — FIXED, PR open
- **Root cause:** `SkillLoader.getRewards()` (in `type.skyblockgeneric/.../skill/SkillLoader.java`) rebuilt its entire reward list from scratch on every call, with no caching. It's called every game tick, for every player, by the tablist module's XP calculation (`AccountInformationModule` → `PlayerStatistics.allStatistics` → `DatapointSkyBlockExperience.getTotalXP` → `SkillCategory.getReward` → `getRewards()`).
- **Diagnosis method:** `jstack` thread dump on the running container's JVM showed one thread burning ~70% of a core continuously in this exact call chain.
- **Fix:** added a `cachedRewards` field, computed once and reused (`config.rewards` never changes after initial YAML load).
- **Result:** 0.5 TPS → 18 TPS.
- **PR:** [#908](https://github.com/Swofty-Developments/HypixelRecreation/pull/908) — open, awaiting maintainer review.

### 2. Sirius NPC (Dark Auction) doesn't spawn — diagnosed, NOT fixed
- `NPCSirius.java` (in `type.hub/.../npcs/`) is a fully-written class with correct logic (position, dialogue, click handling all implemented).
- Confirmed `getNPCs()` in `TypeHubLoader` scans the whole `net.swofty.type.hub.npcs` package via reflection, and `NPCSirius` sits directly in that package — so structurally it *should* auto-register the same way the working `NPCClerkSeraphine` does.
- Never found the actual reason she doesn't appear — ruled out: registration (reflection scan should catch her), the round-start NPE (separate bug, in `DarkAuctionHandler.handleRoundStart`, also unfixed — missing null check on `localState.getCurrentRound()` at line ~178 despite an earlier check on the same field a few lines up).
- **This is the natural next thing to dig into if picking this project back up.**

### 3. Trophy fish items show default Steve head — diagnosed as likely unfinished, not fixed
- Not in the codebase's config-driven item system at all (no matching YAML entries). Likely intended as `PLAYER_HEAD`-based items with custom skin textures (the mechanism real Hypixel uses to make small "fish-shaped" head items), but that wiring appears incomplete in this project.

## Features built from scratch tonight (previously flagged `NOT_FINISHED_YET`)

Both follow the same architecture, established this session — good template for implementing more `NOT_FINISHED_YET` items:

### Personal Deletor (4000–7000)
- Auto-deletes junk items from inventory. Files:
  - `type.skyblockgeneric/.../deletor/PersonalDeletorService.java` — scheduled 1s tick, scans 36 inventory slots, checks toggle + material match, deletes.
  - `type.skyblockgeneric/.../gui/inventories/deletor/GUIPersonalDeletor.java` — settings menu, one togglable slot per junk material.
  - New entries in `DatapointToggles.Toggles.ToggleType` (in `type.generic`): `PERSONAL_DELETOR_COBBLESTONE/DIRT/NETHERRACK/ROTTEN_FLESH/GRAVEL/SAND/STRING/BONE`, all default `true`.
  - YAML: `NOT_FINISHED_YET` replaced with `INTERACTABLE` + `handler_id: PERSONAL_DELETOR_INTERACT` on all 4 tiers.
- Tier scaling: tier (1–4, from `PERSONAL_DELETOR_4000`→`7000`) = max junk stacks cleared per second.

### Personal Compactor (4000–7000)
- Auto-compacts 9 raw materials → 1 block form (iron/gold/diamond/emerald/coal/redstone/lapis), matching vanilla 3×3 ratios. Same architecture as above, files under `.../compactor/`.
- Uses `player.getInventory().addItemStack(...)` to give the compacted block (verified as the real method other code in this project uses for this).

## Key architecture patterns discovered (useful for future feature work)

- **Scheduled per-player logic:** `MinecraftServer.getSchedulerManager().submitTask(() -> { ...; return TaskSchedule.seconds(N); }, ExecutionType.TICK_START)`, iterating `SkyBlockGenericLoader.getLoadedPlayers()`. Modeled on `HuntrapService`.
- **Right-click → GUI:** register a handler in `InteractableRegistry` (static block or an `initialize()` call), then set `id: INTERACTABLE` + `handler_id: <your_id>` on the item in YAML. Modeled on the existing `SKYBLOCK_MENU_INTERACT` handler.
- **Toggle persistence:** `DatapointToggles.Toggles.ToggleType` is a plain enum with a default bool — cheap to extend for any fixed-set boolean player setting, reusing all existing serialization/persistence. Access via `player.getToggles().get(...)/.set(...)/.inverse(...)` (defined on `HypixelPlayer`, inherited by `SkyBlockPlayer`).
- **Tiered accessories:** register in `TieredTalismansRegistry` (may already be done — check first) so `player.getAccessoryBag().getUniqueAccessories()` correctly collapses multiple tiers to just the highest one held.
- **Vanilla materials vs custom ItemTypes:** plain vanilla items/blocks (cobblestone, iron ingot, etc.) often have **no** custom `ItemType` entry at all — `vanillaItems.yml` only lists vanilla materials with special SkyBlock behavior. For raw-material logic, match on Minestom's own `Material` enum directly, not `ItemType`.
- **Color/MiniMessage gotcha:** `Text.of(markup, args)` (used by `ItemStacks.item(...)`) parses color tags **only in the static markup string**, at compile time, before argument substitution. A color tag embedded *inside* a substituted argument (e.g. `String status = "<a>ENABLED"` passed into a `{}` placeholder) will render as **literal text**, not a color. Fix: choose between fully-separate static templates (each with its own embedded color tags) via a ternary, and only substitute plain uncolored values into `{}`.

## Other loose ends from tonight

- **Public server access:** router port forwarding for 25565 + 7270 configured; Windows Firewall inbound rules for both ports added. Resource pack `server-url` in `config.yml` still needs updating from `http://127.0.0.1:7270` to the real public IP/DDNS hostname before external players can actually download the pack. Not yet verified end-to-end from outside the network.
- **Election system:** confirmed there's a `/skiptoevent`-style staff command that can force-trigger it (found by the user, not by us digging through code) — never fully traced the normal (non-staff) trigger path.
- Several dozen other items across the codebase are still flagged `NOT_FINISHED_YET` (grep for it under `configuration/skyblock/items/`) — good source of well-scoped next features.
