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
- **Two separate config trees exist inside every game/service container, and only one is live.** `configuration_files/` is the live bind-mount of `~/.hypixel-skyblock/configuration/` (or `_repo/configuration/`) — editing it on the host updates it instantly inside the container. But `./configuration/` (no `configuration_files` prefix) is a **separate copy baked in at Docker image build time** (`Dockerfile.game_server` step 7 does `cp -a configuration_files/. ./configuration/` during `docker compose build`) and does NOT get refreshed by editing the host files or even by a plain `docker restart`. `HypixelTranslator` (and likely other config readers — check for `Path.of("./configuration/...")` patterns) reads from this **baked, stale** copy, not the live mount. Confirmed hands-on: editing `configuration/i18n/en_US/general.properties` on the host had zero effect on running servers until the corrected file was `docker cp`'d directly into `./configuration/i18n/...` **inside each container** and the container restarted (translations load once at JVM startup). For a durable fix (survives a future full rebuild), the host-side edit alone is enough *only if* you also rebuild the `game_server_prepared` image (`docker compose build game_server_builder`) before recreating containers — a plain `docker cp` fix into a live container is a temporary patch that a future image rebuild will silently undo back to whatever's on disk at build time (which will be correct, since the host file is fixed — just don't assume `docker restart` alone ever picks up a `./configuration`-path change).

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
- GUI + toggle pattern reused directly from Personal Deletor.
- **Color gotcha found and fixed:** `Text.of(markup, args)` (used by `ItemStacks.item(...)`) parses color tags **only in the static markup string**, before argument substitution. A color tag embedded *inside* a substituted argument (e.g. `String status = "<a>ENABLED"` passed into a `{}` placeholder) renders as literal text, not a color. Fix: choose between fully separate static templates (each with embedded color tags) via a ternary, substituting only plain uncolored values into `{}`.

## Key architecture patterns discovered (useful for future feature work)

- **Scheduled per-player logic:** `MinecraftServer.getSchedulerManager().submitTask(() -> { ...; return TaskSchedule.seconds(N); }, ExecutionType.TICK_START)`, iterating `SkyBlockGenericLoader.getLoadedPlayers()`. Modeled on `HuntrapService`.
- **Right-click → GUI:** register a handler in `InteractableRegistry` (static block or an `initialize()` call), then set `id: INTERACTABLE` + `handler_id: <your_id>` on the item in YAML. Modeled on the existing `SKYBLOCK_MENU_INTERACT` handler.
- **Toggle persistence:** `DatapointToggles.Toggles.ToggleType` is a plain enum with a default bool — cheap to extend for any fixed-set boolean player setting, reusing all existing serialization/persistence. Access via `player.getToggles().get(...)/.set(...)/.inverse(...)` (defined on `HypixelPlayer`, inherited by `SkyBlockPlayer`).
- **Tiered accessories:** register in `TieredTalismansRegistry` (may already be done — check first) so `player.getAccessoryBag().getUniqueAccessories()` correctly collapses multiple tiers to just the highest one held.
- **Vanilla materials vs custom ItemTypes:** plain vanilla items/blocks (cobblestone, iron ingot, etc.) often have **no** custom `ItemType` entry at all — `vanillaItems.yml` only lists vanilla materials with special SkyBlock behavior. For raw-material logic, match on Minestom's own `Material` enum directly, not `ItemType`.

## Other loose ends from tonight

- **Public server access:** router port forwarding for 25565 + 7270 configured; Windows Firewall inbound rules for both ports added. Resource pack `server-url` in `config.yml` still needs updating from `http://127.0.0.1:7270` to the real public IP/DDNS hostname before external players can actually download the pack. Not yet verified end-to-end from outside the network.
- **Election system:** confirmed there's a `/skiptoevent`-style staff command that can force-trigger it (found by the user, not by us digging through code) — never fully traced the normal (non-staff) trigger path.
- Several dozen other items across the codebase are still flagged `NOT_FINISHED_YET` (grep for it under `configuration/skyblock/items/`) — good source of well-scoped next features.

## Bazaar work (2026-09-07)

### Transaction/display bugs — all fixed and deployed

- **Buy-order refunds silently failed.** `BazaarTransactionHandler` (`type.skyblockgeneric/.../redis/service/BazaarTransactionHandler.java`) never had a case for `BuyOrderRefundTransaction` in either its dispatch `switch` or `parseTransaction()`, so every refund (completed/expired/cancelled buy orders) logged `Unknown transaction type: BuyOrderRefundTransaction` and the player never got their coins back. `BazaarAwarder` already had the correct handling logic (`processRefundTransaction`/`processRefund`) — it just wasn't wired up. Added the missing switch cases + a `handleRefundTransaction` method mirroring the existing expired-order handler.
- **Same bug, one layer up.** `BazaarPropagator` (`service.bazaar/.../BazaarPropagator.java`) had the identical gap in `handleServerResponses` — refunds that couldn't be delivered live (player offline) never fell back to `PendingTransactionsDatabase`, so an offline player's refund was lost forever instead of waiting for next login. Fixed with a `handleRefundTransactionResponses` method mirroring the expired-order path.
- **Bazaar items showed raw translation keys instead of names** (e.g. `item.minecraft.wheat` instead of "Wheat") for any vanilla-material commodity with no configured display name. Root cause: `ItemLore.getConfiguredOrMaterialName()` (`type.skyblockgeneric/.../item/ItemLore.java`) fell back to `Component.translatable(material)`, which only resolves client-side — anywhere the server flattens it to plain text (bazaar GUI, `SkyBlockItem.getDisplayName()` used in chat messages) printed the raw key. Fixed to build a literal name from the material (`StringUtility.toNormalCase(material.name())`) instead.

### Bazaar item-catalog parity project

User asked to cross-reference this server's bazaar against the real Hypixel bazaar (2,197 live products, pulled from `api.hypixel.net/v2/skyblock/bazaar`) and add whatever's missing but relevant to zones/content that exist in this clone, deferring the rest. Full findings and the real per-category item lists are in **`bazaar-parity-tracking.md`** (repo root) — read that first before continuing this work, it has the raw data so it doesn't need re-fetching.

**Done:** Farming, Mining, Combat, and Woods & Fishes (previously an empty stub category) all now match real Hypixel's bazaar catalog and category structure. ~95 new placeholder `ItemType`s created across `configuration/skyblock/items/farming/farming.yml`, `mining/vanilla.yml`, `mining/dwarven.yml`, and two new files (`combat/bazaar_placeholders.yml`, `miscellaneous/fishing_bazaar_placeholders.yml`). Each placeholder has a new `BazaarPlaceholderComponent` (`type.skyblockgeneric/.../item/components/BazaarPlaceholderComponent.java`, YAML id `BAZAAR_PLACEHOLDER`) that renders bold red "ITEM NOT ADDED YET JUST FOR BAZAAR PLACEHOLDER" lore (wired into `ItemLore.fillBazaarPlaceholder()` and a new `LoreText.Sections.BAZAAR_PLACEHOLDER` footer slot).

**Not started:** Oddities (~800 real items — pet shards, enchanted books per level, minion upgrades, essences). Deliberately deferred: it's organized by game *system* (pets/minions/enchanting/events), not by zone, so needs its own scoping conversation about which systems are actually implemented before any items get added — a mechanical add-what's-missing pass doesn't work for it the way it did for the other four categories.

**Data-quality lessons** (also in the tracking doc): the community wiki's `Bazaar/Item_List` page isn't fully reliable — several wiki-listed items (Raw Cod, Arachne's Calling, Jolly Pink Rock, the whole "Catacombs - Special Fragments" group) turned out not to be real tradable bazaar products when checked against the live API, so were excluded. Conversely several "missing" items from naive name→ID guessing already existed under a different real ID (gemstones use `_GEM` not `_GEMSTONE` suffix; dragon fragments drop the word "DRAGON" from their ID; "Drill Motor" → `DRILL_ENGINE`, etc.) — always cross-check both ways before creating a new item.

**Known infra limit hit repeatedly:** `GUIBazaarItemSet`'s `SLOTS` map hard-codes layouts for 1–10 items with no pagination; anything larger throws `NullPointerException`. Real per-category groups over 10 items (Mining's Crystal Hollows/Glacite Tunnels at 16 each, Combat's Crimson Isle Mob Drops at 16) were split into arbitrarily-named "II" sub-sets to work around this rather than fixing the underlying limitation. **This will not scale to Oddities** (180 pet shards, 300+ enchanted book variants) — add real pagination to this GUI before starting that category. Note: the pre-existing `Oddities → Attribute Shards` tile (uses `AttributeBazaarItems.values()`, untouched this session) throws this exact same NPE today — confirmed live in hub logs when a player opened it, not yet fixed.

**Item icons — mostly fixed, one bug still open as of end of session.** The ~95 new placeholders were originally just plain vanilla-material icons (paper, wheat, etc.). Cross-referenced every one against this project's own local resource pack (`configuration/resourcepacks/skyblockpack`, already present on disk — not fetched from anywhere new) and found real models/textures already existing for ~89 of them; wired `ITEM_MODEL` component references for those (6 items — `Enchanted Carrot on a Stick`, `Enchanted Cooked Rabbit`, `Empty Chum Bucket`, `Full Chum Bucket`, `Titanoboa Shed` — genuinely have no matching asset in the local pack and were left on vanilla fallback; `Nether Star` doesn't need one, it's a real vanilla item). While doing this, found and fixed a **real, pre-existing bug**: `GUIBazaarItemSet.getFromSkyBlockItem()` (the method that builds each bazaar listing tile's icon) only ever special-cased `SkullHeadComponent` — any item using a plain material + `ItemModelComponent` (i.e. most non-head custom items) had its `ITEM_MODEL` silently dropped, showing the bare vanilla material instead. Fixed both the normal path and the error-fallback path in that file to apply `DataComponents.ITEM_MODEL` when present.

**RESOLVED (2026-09-07).** After the `GUIBazaarItemSet` fix, Combat/Woods & Fishes were *still* showing wrong icons — turned out the reported-broken screenshot was actually a completely different screen than the one already fixed. `Bazaar → Combat` at the category level is `GUIBazaar.java` (one tile per item-set, e.g. "Bone", "Crimson Isle - Mob Drops" — icon built from `BazaarItemSet.displayMaterial`), not `GUIBazaarItemSet.java` (the individual-items-within-a-set screen, e.g. "Garden Resources", which *was* already fixed and rendering correctly). `GUIBazaar.getItem()` had the exact same bug, independently — built each category tile straight from `cs.itemSet().displayMaterial.material`, ignoring any `ITEM_MODEL`. Fixed the same way. Confirmed working end-to-end by the user after this second fix. **Lesson: when two screens look similar in a screenshot, verify which actual GUI class is in play before assuming a fix in one covers the other — this cost a full extra debugging round.**

**New, unimplemented request:** user wants the 12 separate Gemstone tiles (Jade/Amber/Topaz/etc., 5 items each) consolidated into a single "Gemstones" subsection under Mining. Since all 60 gemstones can't fit in one `BazaarItemSet` (10-slot cap, see above), this needs either the pagination fix mentioned above, or a nested sub-category screen (Mining → "Gemstones" tile → 12 gem-type tiles → 5-tier list each, mirroring real Hypixel's own drill-down for this exact case). Not started.

## Planned tasks (2026-09-07), ranked easiest → hardest

1. **Write a new commands file listing all current/updated commands.** Pure documentation/consolidation of what already exists after tonight's changes (target-player additions, `/wipe`, `/restore`, etc.) — no new game logic, just cataloging. Meant to double as the structural reference for task 5 below (admin GUI categories should mirror this file's organization).

2. ~~Wire up crop/log respawning ("crystals") in the farming and foraging lobby areas~~ **DONE (2026-09-07).** Turned out to be several real bugs, not just placement/config work:
   - **Logs never fully broke.** Two stacked bugs in the custom digging pipeline: (a) `ActionPlayerDamageBlock` treated every vanilla `CANCELLED_DIGGING`/`FINISHED_DIGGING` resend (sent whenever the client's own break-time prediction elapses before the server finishes) as a fresh cancel, wiping progress just before completion — fixed by ignoring same-position resends while a `BreakingTask` is already in flight. (b) `BreakingTask.run()` fired Minestom's native `PlayerBlockBreakEvent` with `resultBlock = block.block()` (the original block) instead of `Block.AIR` — harmless for normal players (who cancel the event and remove the block manually) but for any `bypassBuild` player, Minestom applied `resultBlock` directly, silently putting the same block right back with no drop.
   - **New feature added:** individual logs now respawn independently 10s after being chopped (`ForagingTreeManager.scheduleSingleLogRegeneration`), not just on full-tree regrowth.
   - **Wheat crystals never grew anything, anywhere.** `MathUtility.getNearbyBlocks()` (used by `ServerCrystalImpl` to find valid ground on spawn) compared blocks with `==` instead of by type key — real farmland almost always has non-default state (moisture ≠ 0), so it never `==`-matched the default-state constant. 100% miss rate for every crystal ever placed. Fixed to compare `.key()`.
   - **Harvested wheat never regrew.** `MineWheatConfiguration` (the `FARM` region's mining handler) revived harvested wheat to `Block.AIR` instead of `Block.WHEAT` — a straight-up bug, not a tuning issue. `WheatAndFlowersConfiguration` (`VILLAGE`'s handler) had the same class of bug in a milder form: revived to default-state (age 0) `Block.WHEAT`, which then sat there forever since this server has no natural crop-growth ticking. Both now revive directly to mature (`age=7`) wheat.
   - **`FARM` region was smaller than the actual field.** Bounds only covered z: -74 to -153, x: 28 to 107, but the real field (per crystal placements) spans z: -71 to -183, x: 24 to 77 — edges fell outside `FARM` and were miscategorized as `VILLAGE`. Expanded bounds (z: -65 to -190, x: 20 to 107) to cover the whole field with margin; confirmed no regression against smaller overlapping regions (coal mine, mining district, fishing outpost all stay smaller-volume and still win in their own areas).
   - **Wood was mineable outside the actual Foraging Camp.** `FOREST` (a large ~138×66 buffer region wrapping the much smaller `FORAGING_CAMP`) also granted `MineLogsConfiguration`, so any oak log/wood/leaves in that whole buffer zone was choppable, not just inside the camp. Removed the mining handler from `FOREST` — only `FORAGING_CAMP` allows chopping now.

3. ~~Fix the bazaar.~~ **IN PROGRESS (2026-09-07).** Split into two threads: (a) real transaction/display bugs — all fixed and deployed; (b) a full item-catalog parity pass against real Hypixel's bazaar — mostly done, one active bug being chased. Details below in "Bazaar work (2026-09-07)".

4. **Audit and fix regular player commands/systems to match real Hypixel** — Party, Guild, the coop system, island visitation, and any other standard player-facing commands. Broad scope: requires researching actual Hypixel behavior per system as a reference, then comparing and fixing this codebase's `service.party` (11 files), `service.guild` (13 files), coop system (13 files across the coop-related classes), and island-visiting flow. Harder than task 2 because it's a multi-system, research-heavy audit rather than a single bounded feature, but it's fixing/aligning existing systems rather than building new UI from scratch.

5. **Build a full staff-only Admin GUI in the SkyBlock menu**, giving in-GUI access to the same abilities as the admin commands, organized into categories "similar to how the commands file was written" (task 1 is the intended structural reference). This is the largest task by far — a wide new UI surface area needing permission-gating, category navigation, and a GUI equivalent for a large number of existing admin abilities (likely many sub-menus). Should be tackled last, after tasks 1 and (ideally) 4 settle what the canonical command/ability set even looks like.

## Bugs found (2026-09-07 session) — not yet fixed

- **Disconnect-cleanup race condition throws on player quit.** When a player disconnects, `ActionPlayerClearSkyBlockCache.run()` (in `type.skyblockgeneric/.../event/actions/player/data/ActionPlayerClearSkyBlockCache.java`) calls `player.getPetData()` → `SkyBlockPlayer.getSkyblockDataHandler()` → `SkyBlockDataHandler.getUser()` → `PlayerDataService.get()`, but the player's SkyBlock data had already been discarded from `PlayerDataService`'s cache by the time this cleanup step ran, throwing `RuntimeException: User <uuid> does not exist!` (wrapped in an `InvocationTargetException` from the event dispatch). Observed once in `hypixelcore_skyblock_island` logs on a real player disconnect (`PlayerDisconnectEvent`), not a repeating storm — looks like a genuine ordering bug between this cleanup action and whatever discards the SkyBlock data domain on quit, not something specific to that one disconnect. Not yet root-caused or fixed.

- ~~Items given to another player via `/i <item> [amount] [player]` show up with no texture (missing/placeholder model) for the recipient.~~ **RESOLVED (2026-09-07)** — confirmed by re-test after the resource-pack tunnel fix (see the playit.gg/Cloudflare tunnel work above): this was purely a symptom of the affected player's resource pack failing to download, not a bug in `/item` or the `SkyBlockItem`/`PlayerItemUpdater` pipeline. No code change was needed.

## Stub NPCs/actions to implement (dialogue removed 2026-09-07)

All of these called the shared `HypixelPlayer.notImplemented()` helper (`type.generic/.../user/HypixelPlayer.java`), which sends the stock "This feature hasn't been implemented yet. Open a Pull Request HERE" message (i18n key `general.not_implemented`). That call was removed from every site below (NPCs/villagers now just do nothing on click; GUI buttons/props now no-op) so players stop seeing the placeholder message — none of the underlying features were actually implemented, this only silenced the stub message. Each of these is a real feature still waiting to be built:

**type.hub** — NPCs: Amelia, AnglerAngus, Anita, Christopher, Erihann, Fann, Guy, Jacob, Jacobus, LotusAtoll, Lucius, MaddoxTheSlayer, Malik, Marco, Mort, Ophelia, Ozanne, Scoop, Taylor, Udium, Wizard; villagers: ApprenticeVillager, PlumberJoeVillager, SmithmongerVillager; plus 5 unimplemented interactable branches in `TypeHubLoader.java` (lines ~119-131 as of this session — check current line numbers, may have shifted).

**type.galatea** — NPCs: Alan, Albert, Amaury, Auryon, BankerBonsai, Boris, Brian, Dax, Jaeger, Kiara, Kysha, Loras, Nemo, Nina, Oden, Rosemary, SalvageOldAttributes, Sawyer, Swoop, Teddy.

**type.thefarmingislands** — NPCs: Beth, FarmerJon, FriendlyHiker, HungryHiker, HunterAva, Jake, Mason, Moby, Shepherd, Talbot, Tammy, Tony, TreasureHunter, TrevorTheTrapper; villager: WindmillOperator.

**type.deepcaverns** — NPCs: LapisMiner, RedstoneMiner, Rhys.
**type.dwarvenmines** — NPCs: Bubu, Bulvar, LiftOperator, Puzzler.
**type.bedwarslobby** — NPCs: DoormanDave, HypixelStore; GUI: `GUILeaderboardSettings` (a settings toggle), `cosmetics/GUIBedWarsMenuShop` (2 shop entries).
**type.mainlobby** — NPCs: AchievementGuide, HypixelStore, LobbyExplorerOliver; `item/impl/Collectibles.java` (a collectible-click action).
**type.lobby** — `gui/GUIGameMenu.java` (a game-select entry), `gui/GUITheDeliveryMan.java` (9 separate click branches — this whole NPC/GUI feature is a stub), `item/impl/Collectibles.java` (a collectible-click action).
**type.replayviewer** — `view/GUIReplayViewer.java` (a "submit" button), `view/GUIViewPlayer.java` (a "report" button).
**type.murdermysterylobby** — NPC: Infection.
**type.skyblockgeneric** — `item/handlers/ability/AbilityRegistry.java` (an unimplemented item ability).
**type.generic** — `gui/impl/collectibles/CollectibleSelectionView.java` (a collectible selection action).
**type.jerrysworkshop** — `TypeJerrysWorkshopLoader.java`: 5 `GlassDisplay` props at fixed positions near (13-16, 77, 91-101) with no real click behavior yet.
**type.thepark** — `TrialOfFire.java` (a stubbed step/branch in the Trial of Fire minigame).
