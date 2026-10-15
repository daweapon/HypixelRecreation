# Architecture and ownership

## Runtime shape

Players connect to a Velocity proxy (`velocity.extension`, built as `SkyBlockProxy.jar`). The proxy routes them to Minestom game servers. Every game server runs the same fat jar, `HypixelCore.jar`, built by `loader`. The `ServerType` it starts as decides which `type.*` module supplies its `HypixelTypeLoader`. Services (`service.*`) are separate JVMs that own shared state: auctions, bazaar, parties, friends, guilds, punishments, elections and so on. MongoDB stores persistent data and Redis carries messaging, locks and cached profile state.

```
player -> Velocity proxy -> HypixelCore game server (one per ServerType/area)
                   \             \
                    +--- Redis ---+--- service.* JVMs --- MongoDB
```

## Module map

| Group | Modules | Role |
|---|---|---|
| Shared | `commons` | Enums (`ServerType`, `ServiceType`), item and data models, Redis messaging (`commons.redis`), protocol objects (`commons.protocol`), config loading, `PackageScanner` |
| Vendored libraries | `lib.redisapi`, `lib.datahandler` | AtlasRedisAPI (MIT) and SharedDataHandler (Apache-2.0), compiled from source. See `NOTICE.md` |
| Game base | `type.generic`, `type.game`, `type.lobby` | `HypixelGenericLoader`, events, GUI v2, commands, player base class |
| SkyBlock base | `type.skyblockgeneric` | Items, skills, collections, mobs, pets, loot tables, SkyBlockPlayer and SkyBlockDataHandler |
| SkyBlock areas | `type.hub`, `type.island`, `type.thegarden`, `type.crimsonisle`, `type.crystalhollows`, `type.kuudra`, `type.dwarvenmines` and the other `type.*` area modules | One `Type*Loader` each: NPCs, regions, area events |
| Minigames | `type.bedwars*`, `type.skywars*`, `type.murdermystery*`, `type.ravengard*`, `type.replayviewer` | Games, their lobbies and configurators |
| Services | `service.generic` plus one `service.*` per domain | Redis-driven services; `service.generic` holds `ServiceInitializer` and the base `SkyBlockService` |
| Proxy | `velocity.extension`, `proxy.api` | Routing, heartbeat, server selection; `proxy.api` is the game-server side client |
| Entry point | `loader` | `Hypixel.main`, shades every `type.*` module into `HypixelCore.jar` |
| Tooling | `packer`, `anticheat`, `pvp`, `spark`, `dungeons`, `setup`, `control-app`, `clientmod`, `website`, `tools` | Resource pack server, anticheat (MIT), combat, web API, installer (Go), Flutter control app, Fabric client mod, docs |

## How a game server starts

1. `loader/Hypixel.main` picks the `ServerType` and builds the matching `HypixelTypeLoader`.
2. `HypixelGenericLoader` registers events, packets, commands and GUIs by scanning packages with `PackageScanner.instantiateSubtypes`.
3. SkyBlock types then run `SkyBlockGenericLoader`, which registers the SkyBlock registries (items, mobs, skills and so on).
4. The type loader supplies its own events, NPCs, regions and required services through `getTraditionalEvents`, `getNPCs`, `getRequiredServices` and similar methods.

Spawning a player into the main or empty instance is shared: type loaders wrap their scanned events with `SpawnOnJoin.intoMainInstance` or `SpawnOnJoin.intoEmptyInstance`. Do not copy a join handler into a new module.

## Messaging

`commons.redis.RedisMessageBus` and the `RedisEnvelope` protocol carry request and reply messages between game servers, services and the proxy. Handlers extend `RedisMessageHandler`. The contract tests live in `commons/src/test/.../redis`.

## Conventions that must hold

- Chance-based rewards go through `LootRoll`, never a bare random call.
- New commands go into `COMMANDS.md`. New currencies and progression also need Centauri Toy Box buttons and `/max profile` support. See `AGENTS.md`.
- Package scanning goes through `PackageScanner`; do not add another copy of the scanning loop.
- `commons/src/generated` is produced by `commons:generateItemTypes` and is not committed.
- Tests run in CI on every pull request (`./gradlew shadowJar test`).

## Ownership

One maintainer, `@daweapon`, owns the whole tree (`.github/CODEOWNERS`). The areas that need the most care are listed here so a reviewer knows where a change is risky:

| Area | Risk |
|---|---|
| `commons/protocol`, `commons/redis` | Wire format shared by every JVM; change both sides in one deploy |
| `type.skyblockgeneric` data handlers | Profile persistence; a bad change corrupts saves |
| `service.auctionhouse`, `service.bazaar` | Player economy |
| `loader`, `DockerFiles`, `setup`, `control-app` | Build and deploy path |
| `anticheat` | Separate MIT licence; keep its `LICENSE.md` |

## Third-party code and licences

The project is AGPL-3.0 and derives from Swofty's HypixelRecreation; upstream copyright and licence notices stay. Vendored libraries and their licences are in `NOTICE.md`.
