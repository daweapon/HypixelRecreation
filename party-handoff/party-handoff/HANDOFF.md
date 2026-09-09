# Handoff: Intermittent silent failure of `/p <player>` (party invite)

## Standing rules the user has set for this project — follow these on every task

The user has explicitly established two workflow rules that apply to **all** future work on this
project, not just the party bug. Whoever picks this up (Codex or otherwise) should follow both:

1. **Before starting work on any roadmap/checklist item, mark it "In Progress" first.** This
   project is tracked in a Google Sheet ("HypixelSkyblock_NextFeatures_Checklist (my copy)",
   file ID `11n4a2VfNAKf4SYMc9xEo-EvJzuOsWGzfWguNS-0PiY0`, tab `Roadmap`). Set the item's Status
   column to "In Progress" the moment work begins (verify the write actually landed — see the
   API gotcha below), do the work, then set it to "Done" with full notes only once
   deployed/verified. Don't wait until the end to update status.
2. **Before starting any section of work, cross-reference the specific game mechanic against
   real Hypixel SkyBlock first** (wiki, forums, live API — whatever's authoritative), *before*
   writing any code. This is not just for "suspicious-looking" features — it applies up front to
   every section, to avoid building something that doesn't match real Hypixel and then having to
   rework it. This rule was created directly because of a prior mistake: a fictional
   `OWNER`/`TRUSTED`/`MEMBER` coop-role hierarchy was built and deployed before checking that
   real Hypixel SkyBlock coops have no hierarchy at all (majority vote, all members equal) —
   that whole system had to be ripped out and rebuilt. The hardcoded `MAX_PARTY_SIZE = 5` (real
   Hypixel has no fixed cap) was a second instance of the same mistake, made before this rule
   existed.

**Google Sheet write-API gotcha** (so the "mark In Progress" step actually works): the Apps
Script bridge expects JSON fields `rowNumber` (int) and `updates` (object) — NOT `row`/`values`.
Sending the wrong field names causes the API to return `{"success":true}` while writing nothing
(a `for...in` over `undefined` silently no-ops). Also, `rowNumber` = the displayed item `#` in
the sheet **+ 1** (row 1 is a header row) — off-by-one here clobbers the row above the intended
target. Always verify a write actually landed (e.g. via Drive file `modifiedTime` before/after)
rather than trusting a `{"success":true}` response at face value.

## Project context
"HypixelRecreation" — a Minestom-based SkyBlock clone. Source lives at `~/hypixel-dev` on the
original machine; deployed via Docker Compose at `~/.hypixel-skyblock/`. Multiple Docker
containers: 3 game servers (`hypixelcore_skyblock_island`, `hypixelcore_prototype_lobby`,
`hypixelcore_skyblock_hub`), a `service_party` microservice container, a `hypixel_redis`
container, and a Velocity proxy. Services and game servers communicate over Redis pub/sub
request/response ("Redis RPC").

## The bug, as observed
`/p <player>` (party invite) intermittently produces **zero output** — no success message, no
error message, nothing in chat. It is not consistently broken: it has worked at least once after
a deploy ("We did it" — full trace confirmed end-to-end), then failed again after a *later*
deploy of the *same* code with no further changes ("doesnt work" / "nope"). Two full rounds of
tracing were added:

1. First round (now removed): verbose step-by-step logs inside `PartyManager.invitePlayer`
   (`resolved targetUUID=`, `targetOnline=`, `about to send`, `send() returned`, etc.). This
   **did** show the full pipeline completing once, and the user confirmed the invite worked.
2. Second round (partially still in the code, see below): after a subsequent failure with no
   exception logged, tracing was added at every hop: client command handler
   (`PartyCommand`), `PartyManager.invitePlayer` (wrapped in try/catch/rethrow logging
   `Throwable`), `PartyActionEndpoint.handle()` (service-side entry log right after pulling the
   action off the request), `PartyCache.handleInvite()` (entry + around the broadcast call),
   `PartyBroadcastHandler.handle()` and `.renderInvited()` (client-side receipt). On the most
   recent deploy of this second round, the user said "nope" but **had not yet retried after this
   exact deploy** when the session was interrupted — so it's unconfirmed whether trace round 2
   ever actually captured a failing attempt.

Also fixed and confirmed-working along the way (real bugs, but NOT proven to be the cause of the
invite silence):
- **`pendingCommands` stuck-forever guard**: `PartyCommand.pendingCommands` was a plain
  non-thread-safe `ArrayList<UUID>` added/removed manually at scattered return points. Any
  uncaught exception before the `remove()` call would permanently block all future `/party`
  commands for that player until a JVM restart. Fixed with `ConcurrentHashMap.newKeySet()` +
  `try/finally`. This is correct and necessary but the "no exception logged anywhere" failure
  mode proves it isn't the culprit for every instance.
- **`\<player>` markup escaping bug** in i18n properties files, and a **4-location config drift**
  gotcha (`hypixel-dev/configuration` vs `_repo/configuration` vs the host bind-mount vs
  `/app/configuration/` *inside* the container, which is only populated from
  `/app/configuration_files` at image-build time, not by a plain `docker restart`). Confirmed
  fixed via screenshot. Unrelated to the silent-invite bug but consumed significant debugging
  time before being ruled out — worth knowing about if new symptoms look like "my config change
  isn't taking effect."

## STRONG NEW LEAD FOUND WHILE PREPARING THIS HANDOFF (not yet tested against the live bug)

While assembling files for this handoff, I found what is very likely the actual root cause, and
it fits every observed symptom (intermittent, survives identical redeploys, zero exceptions
anywhere in the pipeline, previously "worked once" then stopped).

### The mechanism

`PartyManager.send(PartyAction action)` calls:
```java
partyService.handleRequest(new SendPartyActionProtocol.Request(action));
```
This is the **single-argument** overload of `ProxyService.handleRequest(T request)`, which calls
`RedisClient.requestService(ServiceType, T message)`:
```java
public static <T, R> CompletableFuture<R> requestService(ServiceType service, T message) {
    RedisProtocol<T, R> protocol = protocolFor(message);   // <-- resolves protocol BY MESSAGE CLASS
    return requestService(service, protocol, message);
}

public static <T, R> RedisProtocol<T, R> protocolFor(T request) {
    RedisProtocol<?, ?> protocol = protocolsByRequest.get(request.getClass().getSimpleName());
    ...
}
```
`protocolsByRequest` is a single shared `Map<String, RedisProtocol<?, ?>>` keyed by the **simple
class name** of the request record — NOT the fully-qualified name, NOT the owning protocol class.

`SendPartyActionProtocol` defines its request type as a nested record literally named `Request`:
```java
public class SendPartyActionProtocol extends RedisProtocol<
        SendPartyActionProtocol.Request, SendPartyActionProtocol.Response> {
    public record Request(PartyAction action) {}
    public record Response(boolean success, @Nullable String error) {}
}
```
`request.getClass().getSimpleName()` for this object is just `"Request"`.

**At least five other, completely unrelated protocols do the exact same thing** — name their
nested request record `Request` — and are all registered into the *same* map at startup:

| Protocol | File |
|---|---|
| `SendPartyActionProtocol.Request` | `commons/.../protocol/objects/party/SendPartyActionProtocol.java` |
| `PartyBroadcastPushProtocol.Request` | `commons/.../protocol/objects/party/PartyBroadcastPushProtocol.java` |
| `BazaarTransactionPushProtocol.Request` | `commons/.../protocol/objects/bazaar/BazaarTransactionPushProtocol.java` |
| `GuildEventPushProtocol.Request` | `commons/.../protocol/objects/guild/GuildEventPushProtocol.java` |
| `FriendEventPushProtocol.Request` | `commons/.../protocol/objects/friend/FriendEventPushProtocol.java` |
| `SendMessagePushProtocol.Request` | `commons/.../protocol/objects/messaging/SendMessagePushProtocol.java` |

(There may be more — this list was found with a quick grep, not exhaustive. Search for
`record Request(` across `commons/src/main/java/net/swofty/commons/protocol/objects/` to get the
full set.)

At startup, `Hypixel.java` does:
```java
List<RedisProtocol> protocols = SkyBlockGenericLoader.loopThroughPackage(
        "net.swofty.commons.protocol.objects", RedisProtocol.class)
        .filter(obj -> !obj.getClass().getPackageName().startsWith("net.swofty.commons.protocol.objects.proxy"))
        .toList();
protocols.forEach(RedisClient::registerResponseProtocol);
```
`registerResponseProtocol` does `protocolsByRequest.put(requestTypeName(protocol), protocol)` —
**last write wins**. Whichever of the six-plus "Request"-named protocols is registered *last*
during this loop is the one that stays in the map under key `"Request"` — silently clobbering
all the others. Every subsequent call to `protocolFor()` with *any* of these six request types
resolves to that single winning protocol, regardless of which one was actually intended.

### Why it's intermittent across restarts with identical code

`loopThroughPackage` is:
```java
public static <T> Stream<T> loopThroughPackage(String packageName, Class<T> clazz) {
    Reflections reflections = new Reflections(packageName);
    Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(clazz);
    return subTypes.stream()...
}
```
(`type.generic/src/main/java/net/swofty/type/generic/HypixelGenericLoader.java`, also duplicated
in a few other loader classes.) `Reflections.getSubTypesOf` returns a `HashSet<Class<?>>`. `Class`
does not override `hashCode()`, so it falls back to `Object`'s identity hash code — which is
derived from the object's memory address / an internal JVM counter, and **is not guaranteed
stable across JVM runs**. That means the iteration order of this `Set`, and therefore the
registration order into `protocolsByRequest`, can differ **every time the JVM restarts**, even
with byte-for-byte identical code and the same jar. This is consistent with: works after one
restart, silently fails after the next restart of the exact same deploy, with no code change in
between.

### Why there's no exception anywhere

If `protocolFor(message)` resolves to, say, `GuildEventPushProtocol` instead of
`SendPartyActionProtocol`:
- The channel used becomes `RedisChannels.protocol(protocol)` = `"GuildEventPushProtocol"`
  (channel name is the *protocol* class's simple name, via `RedisProtocol.channel()` →
  `getClass().getSimpleName()` — this part is NOT the buggy lookup, it's downstream of it).
- The request is published on that channel, serialized with `GuildEventPushProtocol`'s
  `JacksonSerializer` (Jackson serializes the actual runtime object's fields regardless of the
  declared generic type, so this likely "succeeds" as far as producing *some* JSON, just JSON
  shaped like a `SendPartyActionProtocol.Request`, i.e. `{"action": {...}}`, not like a real
  `GuildEventPushProtocol.Request`).
- **Nothing is subscribed to that channel expecting a party invite.** `PartyActionEndpoint` (the
  only thing that would ever call `PartyCache.handleInvite`) is registered against
  `SendPartyActionProtocol`'s own channel (`"SendPartyActionProtocol"`), not
  `"GuildEventPushProtocol"`. The message is published into Redis and never picked up by anything
  relevant. No exception is thrown on the client (the publish itself succeeds fine — it's just
  going to the wrong channel), and no exception is thrown on any server (nothing is listening, so
  nothing even attempts to deserialize/handle it wrong). This exactly matches the "nope" report:
  comprehensive tracing at `PartyActionEndpoint.handle()`, `PartyCache.handleInvite()`, etc. would
  never fire, because the request never physically reaches the party service's Redis subscriber
  at all.

### This is NOT confirmed yet — it's a strong, well-fitting hypothesis

I did not get to test this against the live failure before the session was interrupted. It fits
every symptom precisely, but should be verified, e.g. by:
1. Adding a one-time startup log in `RedisClient.registerResponseProtocol` printing
   `requestTypeName(protocol) -> protocol.getClass().getSimpleName()` for every registration, on
   both a working and a failing boot, and diffing which protocol ends up owning the `"Request"`
   key each time.
2. Or, more directly: just fix it (see below) and see if the bug disappears permanently across
   many restarts — much cheaper than instrumenting further.

### Recommended fix

In `commons/src/main/java/net/swofty/commons/redis/RedisClient.java`:
- Change `protocolsByRequest` to be keyed by the request class's **fully-qualified name**
  (`request.getClass().getName()`, not `getSimpleName()`), and change `requestTypeName(...)` to
  return the type argument's fully-qualified name (`((Class<?>) firstTypeArg).getName()`) to
  match. Fully-qualified names are unique per nested class (e.g.
  `net.swofty.commons.protocol.objects.party.SendPartyActionProtocol$Request` vs
  `net.swofty.commons.protocol.objects.guild.GuildEventPushProtocol$Request`), so this fully
  eliminates the collision class of bug, not just for party.
- This is a one-line-ish change in two places but touches a shared, load-bearing class — grep for
  all callers of `protocolFor(...)` and `registerResponseProtocol(...)` before touching it to make
  sure nothing else depends on the simple-name behavior (nothing should, but verify).
- Optionally also fix `loopThroughPackage`'s use of a `HashSet` if determinism of registration
  order matters elsewhere (e.g. switch to a `LinkedHashSet` sorted by class name) — lower
  priority once the key-collision bug itself is fixed, since after the fix the registration order
  no longer matters (there's no more collision to be order-dependent about).
- As a mitigation with an even smaller blast radius if you want to avoid touching the shared
  class today: change `PartyManager.send()` to use the **two-argument** `handleRequest(protocol,
  request)` overload with an explicit `new SendPartyActionProtocol()` instance, bypassing
  `protocolFor()`/the collision entirely for this one call site. This does not fix the underlying
  bug for other protocols sharing a `Request`/`Response` name, but would very likely make party
  invites reliable on its own. **Prefer the real fix in `RedisClient` if you have time** — the
  same collision class silently affects any other single-arg `handleRequest(T)` call site across
  the codebase using a colliding protocol (bazaar, guild events, friend events, generic
  messaging, and the party broadcast push itself).

## What's in this zip

```
src/
  commons/.../party/{FullParty,PartyAction,PartyBroadcast,PendingParty,Party}.java
  commons/.../protocol/objects/party/{SendPartyActionProtocol,PartyBroadcastPushProtocol}.java
  commons/.../protocol/objects/{GetPartyProtocol,IsPlayerInPartyProtocol}.java  (under protocol/objects/party/)
  commons/.../protocol/RedisProtocol.java
  commons/.../redis/{RedisClient,RedisMessageHandler,RedisMessageContext,RedisMessageBus,RedisChannels,RedisEndpoint}.java
  commons/.../protocol/objects/{bazaar/BazaarTransactionPushProtocol,guild/GuildEventPushProtocol,
                                  friend/FriendEventPushProtocol,messaging/SendMessagePushProtocol}.java
      (the other protocols found to collide on the "Request" simple name)
  service.party/.../{PartyService,PartyCache}.java
  service.party/.../endpoints/{PartyActionEndpoint,GetPartyEndpoint,IsPlayerInPartyEndpoint}.java
  type.generic/.../party/PartyManager.java
  type.generic/.../command/commands/PartyCommand.java
  type.generic/.../redis/service/PartyBroadcastHandler.java
  type.generic/.../data/HypixelDataHandler.java
  type.generic/.../HypixelGenericLoader.java   (contains loopThroughPackage / the HashSet ordering issue)
  loader/.../Hypixel.java                       (contains the startup protocol-registration loop)
  proxy.api/.../{ProxyService,ProxyPlayer}.java
  configuration/i18n/en_US/commands.properties  (party help text, already-fixed escaping bug for reference)
HANDOFF.md   (this file)
```

Note: two rounds of temporary `[PARTY-TRACE]` / `[PARTY-TRACE-SVC]` debug logging are still
present in `PartyManager.java`, `PartyCommand.java`, `PartyActionEndpoint.java`, `PartyCache.java`
(service side — not included in full here, see live file), and `PartyBroadcastHandler.java`. These
should be removed once the real fix above is confirmed to resolve the issue, but leaving them in
is harmless and may help verify the fix.

---

# Broader project context (merged from 3 prior handoff docs)

The user keeps a running series of handoff notes in their Windows Downloads folder:
`hypixel-skyblock-handoff.md` (original), `hypixel-skyblock-handoff-part2.md` (WSL/infra crisis
notes), `hypixelskyblockhandoff3.md` (latest cumulative — supersedes the original for
project-structure/bugs/features content, but does NOT include part2's WSL/infra section). All
three are folded in below so this single document is self-contained. This section is about the
**project as a whole**, not just the party-invite bug above — useful background if you end up
touching anything else, or if infra looks broken in a way that resembles a code bug.

## Key locations

- `~/hypixel-dev` — full development clone (real git clone, has actual `.java` source). All code
  edits happen here.
- `~/.hypixel-skyblock/_repo` — the repo used by the **running Docker install**. Sparse checkout
  (only `DockerFiles/` and `configuration/` — no source code). Editing here doesn't touch Java
  source; only relevant for deployment.
- `~/.hypixel-skyblock/_repo/DockerFiles/jars/HypixelCore.jar` — if a jar exists here, Docker's
  build uses it *instead of* downloading the stock release jar from GitHub. This is how local
  fixes survive a fresh install/rebuild.
- `~/.hypixel-skyblock/_repo/configuration/` — config baked into the Docker image at build time.
  Must be updated here (in addition to `~/hypixel-dev/configuration/`) for YAML/i18n changes to
  survive a full rebuild.
- `~/.hypixel-skyblock/configuration/` — a *different* directory, bind-mounted, live-updates
  running containers instantly for world files/resource packs (and other config, see the
  4-location gotcha below) — but does not survive a full rebuild by itself.

## The core dev/deploy loop

1. Edit code in `~/hypixel-dev`.
2. `cd ~/hypixel-dev && ./gradlew :type.skyblockgeneric:compileJava` (fast sanity check on one
   module; substitute the relevant module).
3. `./gradlew :loader:shadowJar` → produces `~/hypixel-dev/loader/build/libs/HypixelCore.jar`
   (~71MB fat jar covering all game-server types). Other modules (e.g. `:service.party`) have
   their own `shadowJar` task producing their own service jar.
4. Deploy to a running container for live testing:
   ```bash
   docker cp ~/hypixel-dev/loader/build/libs/HypixelCore.jar hypixelcore_skyblock_island:/app/HypixelCore.jar
   docker restart hypixelcore_skyblock_island
   ```
5. Once verified, persist so it survives a full reinstall:
   ```bash
   cp ~/hypixel-dev/loader/build/libs/HypixelCore.jar ~/.hypixel-skyblock/_repo/DockerFiles/jars/HypixelCore.jar
   ```

**Recurring gotcha:** VS Code edits sometimes don't actually save to disk (paste can silently
truncate, or Find & Replace results shown but never `Ctrl+S`'d). Always verify with
`cat`/`wc -l`/`tail` from the terminal after editing — don't trust what's shown in an editor pane
or pasted into chat as proof the file is correct.

## Docker/infra quirks (all confirmed, all have known fixes)

- **Two orchestration paths conflict.** The install's TUI manager and manually running
  `docker compose` from `_repo` produce *differently-named containers* for the same servers (e.g.
  `hypixelcore_skyblock_hub` vs `hypixelcore_hub`). Running both creates duplicate/competing
  container sets. Prefer the manager's own Start/Stop/Rebuild over manual `docker compose`
  commands when possible.
- **"No X servers available" / missed pings.** Restarting any game server (especially
  island/hub — resource pack rebuild, etc.) can cause a brief CPU spike that makes another server
  (e.g. `prototype_lobby`) miss the proxy's ping deadline (~300ms) 5 times in a row and get
  de-listed, even though it's healthy. Fix: `docker restart` the de-listed server to force
  re-registration. (See also memory `hypixel_proxy_ping_delist_gotcha.md` — mass-restarting
  `hypixelcore_*` containers can trigger this via missed Redis pings.)
- **World files & resource pack don't persist across container recreation** (`docker rm` + fresh
  `up`, not just `restart`). Must be re-copied into each game server + `hypixel_pack_server`
  after any full reset. World files at `~/.hypixel-skyblock/configuration/world/`; resource pack
  originally at `/tmp/real_pack/` on host.
- **Staff rank Redis fix** may be needed after a Redis container recreation — a Lua script,
  must be re-copied into `hypixel_redis` each time, run while the player is disconnected.
- **Four config-tree locations drift independently** (extends memory
  `hypixel_config_repo_drift_gotcha.md`): `configuration_files/` inside a container is the live
  bind-mount and updates instantly on host edit + restart, but `./configuration/` (no
  `configuration_files` prefix) inside the *same* container is a **separate copy baked in at
  Docker image build time** (`cp -a configuration_files/. ./configuration/` during
  `docker compose build`), and does NOT refresh from a host edit or a plain `docker restart`.
  `HypixelTranslator` and likely other config readers use `Path.of("./configuration/...")` —
  i.e. the *stale* baked copy. A `docker cp` straight into `./configuration/...` inside the
  running container + restart fixes it immediately but is undone by any future image rebuild
  unless the host-side file is also fixed (which durably survives a rebuild once
  `game_server_prepared` is rebuilt).

## Real bugs found & fixed (cumulative)

1. **Severe TPS drop (~0.5 TPS on Island) — fixed, PR #908 open.** `SkillLoader.getRewards()`
   rebuilt its entire reward list from scratch every call, called every tick per player via the
   tablist XP calculation chain. Fixed with a `cachedRewards` field (the underlying config never
   changes after YAML load). 0.5 → 18 TPS.
2. **Sirius NPC (Dark Auction) not spawning, and the Dark Auction round-start NPE** — both
   listed as unfixed in the original handoff, later confirmed **fully fixed** per user report in
   part2, but exact fix details weren't captured in these notes — check recent git history in
   `~/hypixel-dev` for the actual diff if this needs revisiting.
3. **Trophy fish items show default Steve head** — diagnosed as unfinished (no YAML entries,
   likely meant to be `PLAYER_HEAD` custom-skin items like real Hypixel), not fixed.
4. **Disconnect-cleanup race condition** (2026-09-07, not yet fixed): `ActionPlayerClearSkyBlockCache.run()`
   calls `player.getPetData()` → ... → `PlayerDataService.get()` after the player's SkyBlock data
   was already evicted from cache, throwing `RuntimeException: User <uuid> does not exist!`
   wrapped in an `InvocationTargetException`. Observed once on a real disconnect, not a storm —
   looks like an ordering bug between this cleanup step and whatever evicts SkyBlock data on
   quit. Not root-caused.
5. **Items given via `/i <item> [amount] [player]` showed no texture for the recipient** —
   RESOLVED, turned out to be the recipient's resource pack failing to download (tunnel issue),
   not a code bug.
6. **Bazaar transaction/display bugs — all fixed and deployed (2026-09-07):**
   - Buy-order refunds silently failed: `BazaarTransactionHandler` had no case for
     `BuyOrderRefundTransaction` in its dispatch switch or `parseTransaction()`, so refunds
     logged `Unknown transaction type` and players never got coins back. Fixed by wiring in the
     already-correct `BazaarAwarder.processRefundTransaction`/`processRefund` logic.
   - Same gap one layer up in `BazaarPropagator.handleServerResponses` — an undeliverable
     refund (player offline) never fell back to `PendingTransactionsDatabase`, losing it
     forever instead of paying out on next login. Fixed to mirror the existing expired-order
     fallback path.
   - Bazaar items showed raw translation keys (e.g. `item.minecraft.wheat`) instead of names
     for vanilla materials with no configured display name, because
     `ItemLore.getConfiguredOrMaterialName()` used `Component.translatable(...)`, which only
     resolves client-side. Fixed to build a literal name from the material via
     `StringUtility.toNormalCase(material.name())`.
   - `GUIBazaarItemSet.getFromSkyBlockItem()` only special-cased `SkullHeadComponent` for
     tile icons — any item using a plain material + `ItemModelComponent` silently showed the
     bare vanilla material. Fixed both the normal and error-fallback paths to apply
     `DataComponents.ITEM_MODEL` when present. **Still open as of end of last session:** after
     this fix + redeploy, Garden Resources icons confirmed correct, but Combat/Woods & Fishes
     icons were still reported wrong by the user — root cause not found (fix verified present
     in the deployed jar via `javap`, resource pack assets structurally identical to the
     working example, pack confirmed downloaded by the client). **Do not assume this is fixed —
     re-verify visually with a fresh screenshot before considering it closed.**
   - `GUIBazaarItemSet`'s `SLOTS` map hard-codes layouts for 1–10 items with no pagination —
     larger real Hypixel categories (16-item groups) were split into arbitrary "II" sub-sets as
     a workaround. **Will not scale to the deferred Oddities category** (180 pet shards, 300+
     enchanted book variants) — needs real pagination before that category is attempted. A
     pre-existing, unrelated `Oddities → Attribute Shards` tile hits this exact same NPE today,
     confirmed live in hub logs, not yet fixed.

## Features built from scratch (previously flagged `NOT_FINISHED_YET`)

Both follow the same template — good reference for implementing more `NOT_FINISHED_YET` items:

- **Personal Deletor (4000–7000):** auto-deletes junk items from inventory.
  `type.skyblockgeneric/.../deletor/PersonalDeletorService.java` (1s scheduled tick, scans 36
  slots, checks toggle + material match, deletes) + `.../gui/inventories/deletor/GUIPersonalDeletor.java`
  (settings menu). New `DatapointToggles.Toggles.ToggleType` entries for 8 junk materials,
  default `true`. Tier (1–4) = max junk stacks cleared per second.
- **Personal Compactor (4000–7000):** auto-compacts 9 raw materials → 1 block form (iron, gold,
  diamond, emerald, coal, redstone, lapis), matching vanilla 3×3 ratios. Same architecture,
  `.../compactor/` files, GUI + toggle pattern reused directly from Personal Deletor.

## Key architecture patterns (useful for future feature work)

- **Scheduled per-player logic:**
  `MinecraftServer.getSchedulerManager().submitTask(() -> { ...; return TaskSchedule.seconds(N); }, ExecutionType.TICK_START)`,
  iterating `SkyBlockGenericLoader.getLoadedPlayers()`. Modeled on `HuntrapService`.
- **Right-click → GUI:** register a handler in `InteractableRegistry`, set `id: INTERACTABLE` +
  `handler_id: <your_id>` on the item in YAML. Modeled on `SKYBLOCK_MENU_INTERACT`.
- **Toggle persistence:** `DatapointToggles.Toggles.ToggleType` is a plain enum with a default
  bool — cheap to extend for any fixed-set boolean player setting. Access via
  `player.getToggles().get(...)/.set(...)/.inverse(...)`.
- **Tiered accessories:** register in `TieredTalismansRegistry` (check first, may already be
  done) so `player.getAccessoryBag().getUniqueAccessories()` correctly collapses multiple tiers
  to the highest one held.
- **Vanilla materials vs custom ItemTypes:** plain vanilla items/blocks often have **no** custom
  `ItemType` entry (`vanillaItems.yml` only lists vanilla materials with special SkyBlock
  behavior). For raw-material logic, match on Minestom's own `Material` enum directly.
- **Color/MiniMessage gotcha:** `Text.of(markup, args)` (used by `ItemStacks.item(...)`) parses
  color tags only in the *static* markup string, at compile time, before argument substitution.
  A color tag embedded inside a substituted `{}` argument (e.g. `"<a>ENABLED"`) renders as
  literal text, not a color. Fix: use two full static templates (each with embedded color tags)
  chosen via a ternary, substituting only plain uncolored values.

## Wheat/tree/farming region fixes (2026-09-07, all done)

Multiple real bugs found while wiring up crop/log respawning ("crystals"):
- `ActionPlayerDamageBlock` treated every vanilla `CANCELLED_DIGGING`/`FINISHED_DIGGING` resend
  as a fresh cancel, wiping break progress just before completion — fixed by ignoring
  same-position resends while a `BreakingTask` is already in flight.
- `BreakingTask.run()` fired `PlayerBlockBreakEvent` with `resultBlock = block.block()` instead
  of `Block.AIR` — harmless normally (players cancel and remove manually) but for a
  `bypassBuild` player, Minestom put the same block right back with no drop.
- Individual logs now respawn independently 10s after chopping
  (`ForagingTreeManager.scheduleSingleLogRegeneration`), not just on full-tree regrowth.
- `MathUtility.getNearbyBlocks()` compared blocks with `==` instead of by type key — real
  farmland almost always has non-default state (moisture ≠ 0), so it never matched, meaning
  wheat crystals never grew anything, anywhere. Fixed to compare `.key()`.
- `MineWheatConfiguration` revived harvested wheat to `Block.AIR` instead of `Block.WHEAT` — a
  straight bug. `WheatAndFlowersConfiguration` had a milder version (revived to age-0 wheat,
  which then never grew further since there's no natural crop-growth ticking). Both now revive
  directly to mature (`age=7`) wheat.
- The `FARM` region's bounds were smaller than the actual field, miscategorizing its edges as
  `VILLAGE`. Expanded to cover the whole field with margin.
- `FOREST` (a large buffer region wrapping the smaller `FORAGING_CAMP`) also granted the
  log-mining handler, making wood choppable outside the actual camp. Removed from `FOREST`.

## Bazaar item-catalog parity project (2026-09-07)

Cross-referenced this server's bazaar against real Hypixel's live bazaar (2,197 products via
`api.hypixel.net/v2/skyblock/bazaar`), adding whatever's missing but relevant to zones/content
that exist in this clone. Full findings and raw per-category item lists are in
**`bazaar-parity-tracking.md`** (repo root, not included in this handoff bundle) — read that
first before continuing, it has the raw data so it doesn't need re-fetching from the API again.

- **Done:** Farming, Mining, Combat, and Woods & Fishes (previously an empty stub) now match
  real Hypixel's catalog/category structure. ~95 new placeholder `ItemType`s added across
  several YAML files, each rendering bold-red "ITEM NOT ADDED YET JUST FOR BAZAAR PLACEHOLDER"
  lore via a new `BazaarPlaceholderComponent` (YAML id `BAZAAR_PLACEHOLDER`).
- **Not started:** Oddities (~800 real items — pet shards, enchanted books per level, minion
  upgrades, essences). Deliberately deferred — organized by game *system* rather than zone, so
  needs its own scoping conversation about which systems are actually implemented first, plus
  the pagination fix mentioned above.
- **Data-quality lesson:** the community wiki's item list isn't fully reliable — some
  wiki-listed items turned out not to be real tradable bazaar products when checked against the
  live API (exclude these); conversely some "missing" items already existed under a different
  real ID via non-obvious naming (gemstones use `_GEM` not `_GEMSTONE`, dragon fragments drop
  the word "DRAGON", "Drill Motor" → `DRILL_ENGINE`, etc.). Always cross-check both directions.
- Icon fixes: cross-referenced new placeholders against the local resource pack
  (`configuration/resourcepacks/skyblockpack`) and wired `ITEM_MODEL` for ~89 of ~95; 6 items
  genuinely have no matching asset and stay on vanilla fallback.
- **New, unstarted request:** consolidate the 12 separate Gemstone tiles (Jade/Amber/Topaz/etc.,
  5 items each) into one "Gemstones" subsection under Mining, mirroring real Hypixel's own
  drill-down — blocked on the same 10-slot pagination limit above.

## Planned tasks (ranked easiest → hardest, as of 2026-09-07 — check current status before assuming still pending)

1. Write a documentation file cataloging all current/updated commands (pure consolidation, no
   new logic) — meant to double as the structural reference for task 5.
2. ~~Wire up crop/log respawning~~ **DONE** (see farming fixes above).
3. ~~Fix the bazaar~~ **IN PROGRESS** — transaction/display bugs done; catalog-parity pass
   mostly done, one icon bug still open (Combat/Woods & Fishes, see above).
4. **Audit and fix regular player commands/systems to match real Hypixel** — Party, Guild, the
   coop system, island visitation, and other standard player-facing commands. **This is the task
   whose Party sub-thread produced the invite bug documented at the top of this file** — coop
   system and the Party permission/size-cap audit were already completed this session (see the
   top section); Guild and island-visitation auditing have not been started yet.
5. **Build a full staff-only Admin GUI** in the SkyBlock menu, mirroring admin commands,
   organized by categories similar to task 1's command catalog. Largest task — should come last,
   after 1 and ideally 4 settle the canonical command/ability set.

## Stub NPCs/actions still needing real implementations

A large number of NPCs/GUI buttons across many `type.*` modules previously called
`HypixelPlayer.notImplemented()` (stock "hasn't been implemented yet, open a PR" message,
i18n key `general.not_implemented`). That stub call was removed everywhere (so players stop
seeing a placeholder message) but **none of the underlying features were actually built** — each
of these is still a real feature waiting to be implemented:

- **type.hub:** NPCs Amelia, AnglerAngus, Anita, Christopher, Erihann, Fann, Guy, Jacob,
  Jacobus, LotusAtoll, Lucius, MaddoxTheSlayer, Malik, Marco, Mort, Ophelia, Ozanne, Scoop,
  Taylor, Udium, Wizard; villagers ApprenticeVillager, PlumberJoeVillager, SmithmongerVillager;
  plus 5 unimplemented interactable branches in `TypeHubLoader.java` (~lines 119-131, may have
  shifted).
- **type.galatea:** NPCs Alan, Albert, Amaury, Auryon, BankerBonsai, Boris, Brian, Dax, Jaeger,
  Kiara, Kysha, Loras, Nemo, Nina, Oden, Rosemary, SalvageOldAttributes, Sawyer, Swoop, Teddy.
- **type.thefarmingislands:** NPCs Beth, FarmerJon, FriendlyHiker, HungryHiker, HunterAva, Jake,
  Mason, Moby, Shepherd, Talbot, Tammy, Tony, TreasureHunter, TrevorTheTrapper; villager
  WindmillOperator.
- **type.deepcaverns:** LapisMiner, RedstoneMiner, Rhys.
- **type.dwarvenmines:** Bubu, Bulvar, LiftOperator, Puzzler.
- **type.bedwarslobby:** NPCs DoormanDave, HypixelStore; GUI `GUILeaderboardSettings`,
  `cosmetics/GUIBedWarsMenuShop` (2 shop entries).
- **type.mainlobby:** NPCs AchievementGuide, HypixelStore, LobbyExplorerOliver;
  `item/impl/Collectibles.java` (a collectible-click action).
- **type.lobby:** `gui/GUIGameMenu.java` (a game-select entry), `gui/GUITheDeliveryMan.java`
  (9 separate click branches — whole NPC/GUI feature is a stub), `item/impl/Collectibles.java`.
- **type.replayviewer:** `view/GUIReplayViewer.java` (submit button), `view/GUIViewPlayer.java`
  (report button).
- **type.murdermysterylobby:** NPC Infection.
- **type.skyblockgeneric:** `item/handlers/ability/AbilityRegistry.java` (an unimplemented item
  ability).
- **type.generic:** `gui/impl/collectibles/CollectibleSelectionView.java` (a collectible
  selection action).
- **type.jerrysworkshop:** `TypeJerrysWorkshopLoader.java`: 5 `GlassDisplay` props near
  (13-16, 77, 91-101) with no click behavior.
- **type.thepark:** `TrialOfFire.java` (a stubbed step/branch in the minigame).

## Election system

Confirmed there's a `/skiptoevent`-style staff command that force-triggers it. The normal
(non-staff) trigger path has never been fully traced.

## Public server exposure (unfinished)

Router port forwarding for 25565 + 7270 configured; Windows Firewall inbound rules added for
both. Resource pack's `server-url` in `config.yml` still needs updating from `127.0.0.1`/
`http://127.0.0.1:7270` to a real public IP or DDNS hostname before external players can
actually connect/download the pack. Not verified end-to-end from outside the network as of the
last session that touched it.

## WSL/Windows infra crises (part2 — resolved, but good to recognize if they recur)

These were **not** code bugs; they cost significant debugging time before being correctly
diagnosed as environment/infra issues. If something looks like a mysterious, uniform problem
across many unrelated containers/services simultaneously, suspect infra before code.

1. **WSL DNS breaks repeatedly.** Symptom: `curl`/any network call fails with "Could not resolve
   host," eventually hanging Claude Code entirely (it needs network access). Root cause: WSL
   auto-regenerates `/etc/resolv.conf` on its own restart, pointing to a WSL-internal nameserver
   that intermittently doesn't resolve — happened 3+ times in under 2 hours whenever WSL
   restarted for any reason. Permanent fix:
   - `/etc/wsl.conf`: add `[network]` section with `generateResolvConf = false`.
   - Rewrite `/etc/resolv.conf` with `nameserver 8.8.8.8` / `8.8.4.4`.
   - `sudo chattr +i /etc/resolv.conf` (make immutable so nothing silently overwrites it again).
   - Requires a full `wsl --shutdown` from **Windows PowerShell** (not from inside WSL) for the
     `wsl.conf` change to take effect. Confirmed to survive a full WSL restart and later a full
     Windows restart. If DNS breaks again despite this, check `lsattr /etc/resolv.conf` first —
     if the immutable flag is missing, something explicitly removed it, which is a new,
     different problem worth investigating rather than just reapplying the fix.
2. **Mongo/Redis/proxy don't reliably auto-restart after a WSL shutdown.** Symptom: after a
   `wsl --shutdown` + restart cycle, most containers auto-restored, but `hypixel_mongo`,
   `hypixel_redis`, and `hypixel_proxy` specifically stayed `Exited (255)`. Consequence: every
   game server, unable to reach Mongo/Redis, got stuck in a hard connection-retry loop, driving
   CPU to 170-228% per container across 6+ servers simultaneously — enough to overload WSL and
   cause VS Code Remote / Claude Code to disconnect/hang, looking like a resource problem when it
   wasn't. Fix: `docker start hypixel_mongo hypixel_redis`, wait ~5s, then
   `docker start hypixel_proxy`. CPU dropped from 170-228% back to 3-9% within seconds. **Lesson:**
   if CPU is uniformly insane across many different server types right after a WSL/Docker
   restart, check `docker ps -a | grep -E "mongo|redis|proxy"` for anything `Exited` before
   assuming a new code-level performance bug — a uniform spike across every server type points
   to a shared dependency being down, not a bug in any one server's code.
3. **WSL itself became unstable/crashed** (`Catastrophic failure — Error code:
   Wsl/Service/E_UNEXPECTED`) after several `wsl --shutdown` cycles in one session. Ruled out:
   disk space, `wsl --status` (responded normally), `.wslconfig` syntax (valid). Fix: a full
   **Windows restart**, not another WSL shutdown — clears Hyper-V/WSL VM/virtual networking state
   all at once. Lesson: after repeated shutdown/restart cycles in one session put WSL in a bad
   state, go straight to a full Windows restart rather than more `wsl --shutdown` attempts.
4. **WSL2 memory limit increased** from default ~7.75GB to 12GB (machine has 16GB total) via
   `.wslconfig`'s `[wsl2]` section (`memory=12GB`) — needed given the full Docker stack (proxy +
   13 services + up to 8 game world servers) plus Gradle builds plus VS Code Server plus Claude
   Code all sharing one allocation. **Caution:** Notepad edits to `.wslconfig` failed to save
   correctly twice in a row in one session (once landing empty, once reverting to a stale
   `memory=8GB`) — always verify with `Get-Content "$env:USERPROFILE\.wslconfig"` from
   PowerShell before restarting WSL, don't just trust Notepad's save succeeded. Requires
   `wsl --shutdown` to take effect (stops the whole Docker stack; restart via the manager after).

## Deploy mechanics (for whoever picks this up)

- Source at `~/hypixel-dev`, deployed containers at `~/.hypixel-skyblock/`.
- Party changes require rebuilding **both** `:loader:shadowJar` (bundled into all 3 game server
  containers as `HypixelCore.jar`) **and** `:service.party:shadowJar` (as `ServiceParty.jar` for
  the `service_party` container).
- `service_party` needs a **double restart** after deploying a new jar for Redis pub/sub
  subscriptions to settle correctly (an existing known pattern, not new).
- Config/i18n changes need to be copied into **four** locations to take full, durable effect —
  see the full write-up in memory file `hypixel_config_repo_drift_gotcha.md` (extended
  2026-09-07 section) if config-looking symptoms show up again; not directly relevant to this bug
  but easy to confuse with it.

## Continuation update (2026-09-08 to 2026-09-09)

### Party fix is confirmed and deployed

The suspected Redis protocol collision at the top of this handoff was the real cause. Claude
changed `commons/.../redis/RedisClient.java` so `protocolFor()` and
`registerResponseProtocol()` use each request class's fully-qualified name rather than its simple
name. That prevents the nested `Request` and `Response` records from unrelated protocols from
overwriting each other at JVM startup. The temporary `[PARTY-TRACE]` logs were removed, both
`ServiceParty.jar` and `HypixelCore.jar` were rebuilt with `--no-build-cache`, validated with
`jar tf`, deployed, and the party service was double-restarted. `/p <player>` was then confirmed
reliable.

### Work completed after the party task

- Built a sorted [COMMANDS.md](../../../../hypixel-dev/COMMANDS.md) command reference and made it
  a standing rule in `AGENTS.md`: update it whenever a command is added or changed.
- Added paginated chat `/help` and marked it Done in the Roadmap.
- Replaced non-void surrounding island terrain with void-world island generation. Profile creation
  was fixed and user-tested.
- Completed a broad Auction House audit and repair pass: listing creation, browser/menu loading,
  categories, custom menu textures, navigation arrows, profile ownership, bidding across accounts,
  BIN grace behavior, and browser visibility. The final user decision is that auctions in the
  BIN grace period remain visible in the browser.
- Built the staff Admin GUI and the timer-oriented `/adminbypass` command. `/adminbypass auction
  [uuid]` finishes an active auction and `/adminbypass bin_grace [uuid]` clears the BIN grace
  period. With no UUID, it uses the executing player's newest active auction. The old
  `/skiptoevent` behavior is now represented by `/adminbypass <event>` and the legacy aliases
  remain supported.
- Added the staff command aliases `/completemissionset` and `/unlockbeastiary` for execution on
  another player where applicable.
- Item display cleanup removed the literal `minecraft:` prefix from vanilla item names.

### New remote commands (implemented, tested, and deployed)

Two remote-economy commands were added in `type.skyblockgeneric` and documented in `COMMANDS.md`:

| Command | Aliases | Access rules |
| --- | --- | --- |
| `/auctionhouse` | `/ah` | Active Booster Cookie required. |
| `/bazzar` | `/bazaar`, `/bz` | Active Booster Cookie and SkyBlock Level 7 required. |

The user explicitly requested the misspelled `/bazzar`; it is retained as the primary registered
label, while official `/bazaar` and `/bz` work too. The official Hypixel SkyBlock Wiki was used
for parity: it documents Cookie-gated remote `/ah` and `/bazaar`, and the Bazaar's level-7 gate.
`./gradlew :type.skyblockgeneric:test --no-build-cache` passed. `:loader:shadowJar
--no-build-cache` was validated successfully; the jar contains both command classes.

### GitHub private backup

A private GitHub repository now exists at:

`https://github.com/daweapon/HypixelRecreation-Private`

It has description: "Minecraft 26.2 recreation of Hypixel, including SkyBlock and its supporting
microservices." The entire then-current working snapshot (1,814 changed/untracked files) was
credential-scanned, committed as `4597dd046 chore: create private project snapshot`, and pushed
to `main`. The local branch `fix/skill-rewards-caching` tracks `private/main`; original `origin`
and `fork` remotes were preserved. Windows GitHub CLI is installed at
`C:\Program Files\GitHub CLI\gh.exe`; it is not automatically on the WSL PATH. Use it through
`powershell.exe` or add the appropriate Windows directory to PATH if desired.

### Current Docker/WSL startup diagnosis and repair

The normal startup script is now `~/.hypixel-skyblock/start-servers.sh`. It starts databases,
proxy, services, then game servers with an 8-second stagger; use this instead of manually
starting all JVM containers, as simultaneous starts cause severe `vmmem` CPU spikes.

On 2026-09-09, the stack entered restart loops despite Redis and MongoDB appearing "Up". New
containers logged `UnknownHostException: redis` and `UnknownHostException: mongodb`. Diagnosis:
Docker's `hypixel-skyblock_hypixel_network` had stale endpoint/DNS registrations for the old
Redis and Mongo containers. `docker network inspect` did not list them even though each
container's own inspect data claimed it was attached. A normal disconnect/reconnect did not fix
the stale daemon endpoint. The working repair was:

```bash
cd ~/.hypixel-skyblock
docker compose up -d --force-recreate redis mongodb proxy
```

Their named data volumes remained intact. Verify DNS from a service before restarting the game
servers:

```bash
docker exec service_api sh -lc 'getent hosts redis; getent hosts mongodb'
```

This should return Docker-network addresses. Once it did, the lobby, hub, island, and optional
worlds started successfully; the two remaining optional worlds (`spiders_den` and `the_end`)
needed one final staggered `docker restart` each. Avoid a wholesale WSL memory clear while the
stack is running: it stops the server processes and can reintroduce the Docker networking issue.
