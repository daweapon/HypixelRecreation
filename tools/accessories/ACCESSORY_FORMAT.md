# Accessory YAML format (AF framework)

Written by worker AF for the Accessories update (Roadmap #36). Every accessory is **one item YAML
block with an `ACCESSORY` component** (`TALISMAN` is an alias). Most accessories need no Java: stats
go in `default_statistics`, and conditional or combat effects go in a typed `effects:` list.

SBG = `type.skyblockgeneric/src/main/java/dev/daweapon/type/skyblockgeneric`.

| What | Where |
|---|---|
| Parser | `SBG/accessory/effect/AccessoryComponentParser.java` |
| Component | `SBG/item/components/AccessoryComponent.java` |
| Families (upgrade lines, "only one counts" groups) | `configuration/skyblock/accessories/families.yml`, loaded by `SBG/accessory/AccessoryFamilies.java` |
| The one "owns accessory" rule | `SBG/accessory/AccessoryOwnership.java` |
| Accessory Power | `SBG/accessory/AccessoryPower.java` |
| Stats and typed effects | `SBG/accessory/AccessoryStats.java`, damage hooks in `SBG/item/events/AccessoryDamage*Event.java` |
| Java-backed effect ids | `SBG/accessory/AccessoryHandlers.java` |
| Guard test | `type.skyblockgeneric/src/test/java/.../accessory/AccessoryFrameworkTest.java` |
| Draft generator | `tools/accessories/accessory_yaml.py` |
| Family generator | `tools/accessories/accessory_families.py` |

---

## 1. Quick start for a content worker

1. List what's missing, then draft blocks from the research data:
   ```
   python3 -I tools/accessories/accessory_yaml.py "$PWD" --missing
   python3 -I tools/accessories/accessory_yaml.py "$PWD" SPEED_RELIC "Pulse Ring"
   ```
   It prints complete item blocks: name, rarity, stats, flags, the skull texture, a shaped recipe
   (only when every ingredient already exists), soulbound, and the effect text as one `INACTIVE`
   effect. Warnings on stderr say which recipe ingredients or stats are missing and where the
   accessory comes from (shop, drop, Dark Auction...), so you can wire its source.
2. Paste the blocks into a YAML file under `configuration/skyblock/items/accessories/` with a
   top-level `items:` list. Use a new file per line or theme; don't pile everything into
   `accessories.yml`.
3. Turn the `INACTIVE` effect into typed effects (section 4) where the system exists. Leave it
   `INACTIVE` when it needs something unbuilt (Dungeons, the Rift, Garden pests...): its lore then
   ends with a grey "Not active yet", which is the operator's rule.
4. Regenerate `ItemType`: `./gradlew :commons:generateItemTypes` (never hand-edit it).
5. Run the guard tests with `--rerun` (config-only changes don't rerun tests otherwise):
   ```
   ./gradlew :type.skyblockgeneric:test --tests dev.daweapon.type.skyblockgeneric.accessory.AccessoryFrameworkTest --tests dev.daweapon.type.skyblockgeneric.item.EveryItemParsesTest --rerun -Dorg.gradle.jvmargs=-Xmx1536m --no-daemon -q
   ```
6. Nothing else. The family file already lists every Hypixel upgrade line (by the ids this server
   uses), so a new member joins its line automatically. The bag, stats, Accessory Power, SkyBlock
   XP, Maxwell and `/maxprofile` all read the YAML.

**A component that fails to parse does not fail `EveryItemParsesTest`.** The item still loads
without its `ACCESSORY` component. `AccessoryFrameworkTest` catches that
("ACCESSORY block failed to parse"), and the server log names the field, for example
`ACCESSORY[SKELETON_TALISMAN].effects[0].mob_types[0]: unknown value SKELETON`.

---

## 2. Complete worked example

These are the real Blaze Talisman (`items/crimson/accessories.yml`) and Mine Affinity Talisman
(`items/accessories/accessories.yml`), annotated. Between them they show every common field.

```yaml
items:
  - id: BLAZE_TALISMAN                 # ItemType name; use Hypixel's id
    material: player_head
    name: "Blaze Talisman"
    rarity: RARE                       # base rarity; recombobulation adds one tier on top
    default_statistics:                # flat stats, applied once while the accessory counts
      true_defense: 3
    lore:                              # optional plain lore lines, shown before effect text
      - "§7Reduces the damage taken from §4♨"
      - "§4Infernal §7mobs by §a5%§7."
    components:
      - id: SKULL_HEAD
        texture: 9de3a407d2be387b45bebdd52d3d4d75a6a1b479e2b8c03267d021d4af31e072
      - id: TALISMAN                   # same as ACCESSORY
        effects:
          - type: DAMAGE_REDUCTION     # typed effect: works with no Java
            percent: 5
            mob_types: [INFERNAL]
      - id: DEFAULT_CRAFTABLE
        recipes:
          - type: SHAPED
            recipe-type: COMBAT
            pattern:
              - "AAA"
              - "ABA"
              - "AAA"
            ingredients:
              A:
                type: MILLENIA_OLD_BLAZE_ASHES
                amount: 1
              B:
                type: ENCHANTED_BLAZE_ROD
                amount: 8
            result:
              type: BLAZE_TALISMAN
              amount: 1

  - id: MINE_AFFINITY_TALISMAN
    material: PLAYER_HEAD
    name: "Mine Affinity Talisman"
    rarity: COMMON
    components:
      - id: SKULL_HEAD
        texture: d9563fdc4acab6db324b21bc43b238fe465e530a6327e7eef11d2d0c4ea
      - id: TALISMAN
        effects:
          - type: STAT                 # conditional stat
            statistics: { speed: 25 }
            servers: [SKYBLOCK_GOLD_MINE, SKYBLOCK_DEEP_CAVERNS, SKYBLOCK_DWARVEN_MINES]
            text:
              - "Grants <f>+25 Speed <7>while on Mining"
              - "Islands."
```

What it produces:
- The tooltip shows the stats, then `Accessory Power: +8` for the Blaze Talisman (Rare), its lore,
  any effect text, and the `RARE ACCESSORY` footer.
- In the bag or inventory the Blaze Talisman gives +3 True Defense and takes 5% off damage from
  Infernal mobs. In the bag it also adds 8 Accessory Power (12 once recombobulated).
- The Mine Affinity Talisman gives +25 Speed only on the three mining servers.

---

## 3. `ACCESSORY` component fields

| Field | Default | Meaning |
|---|---|---|
| `recombobulate` | `true` | `false` for items Hypixel won't recombobulate (Voter's Badges, Rift Prism, Runebook, Pandora's Box, Safety Badge, Book of Progression, Beastslayer's Codex, Celestial Starstone). |
| `dungeon` | `false` | Dungeon accessory: footer reads `DUNGEON ACCESSORY` and its Accessory Power doubles inside Dungeons (`DungeonAccessoryHook`, off until Dungeons exist). |
| `only_in` | `ANYWHERE` | `RIFT` or `BINGO`. Such accessories give no Accessory Power, apply no effects outside their realm (neither exists yet), and `/maxprofile` skips them. |
| `power` | `NORMAL` | `DOUBLE` (Hegemony Artifact) or `ABIPHONE_CONTACTS` (Abicases: +1 per 2 contacts on the Abiphone in the inventory). |
| `dynamic_max_rarity` | none | For accessories whose rarity changes with progress (Pandora's Box, Book of Progression, Runebook, Codex, Pulse Ring). The item's stored rarity is used instead of the YAML one, and `/maxprofile` sets it to this. |
| `effects` | none | Section 4. |

Other per-item fields stay where they always were: `rarity`, `default_statistics`, `lore`,
`SKULL_HEAD`, `DEFAULT_CRAFTABLE`, `DEFAULT_SOULBOUND`, `SELLABLE`, `NOT_FINISHED_YET`.

**Stats:** `default_statistics` keys are `ItemStatistic` names in lower case (`critical_damage`,
`health_regeneration`, `bonus_attack_speed`, `trophy_fish_chance`, `sea_creature_chance`, crop
fortunes such as `melon_fortune`). They apply once per family, never by holding the item. A stat
that only applies somewhere goes in a `STAT` effect instead.

**Rarity and recombobulation:** the YAML `rarity` is the base. A Recombobulator 3000 (anvil) raises
the rarity used for the tooltip, Accessory Power and SkyBlock XP by one tier. Accessory base stats
don't change with rarity (that's Hypixel's rule since Powers replaced accessory reforges); more
Accessory Power means stronger Powers.

---

## 4. Effects

Every effect has a `type` and may have `text:` lines (shown in the tooltip, grey by default, markup
allowed; remember `<b>` is aqua and bold is `<l>`).

| `type` | Fields | Behaviour |
|---|---|---|
| `STAT` | `statistics`, conditions | Adds the stats while every condition holds. |
| `DAMAGE_REDUCTION` | `percent`, `mob_types` (empty = every mob), conditions | Multiplies damage the player takes from a matching mob by `1 - percent/100`. Several reductions stack multiplicatively. |
| `DAMAGE_BONUS` | `percent`, `mob_types`, conditions | Adds `percent` to the damage the player deals to a matching mob. Bonuses add together. |
| `HANDLER` | `handler` | The behaviour lives in Java and asks `AccessoryOwnership.owns(player, type)`. The id must be in `AccessoryHandlers` (the parser rejects unknown ids). |
| `TEXT` | `text` (required) | Tooltip only, for a fact that needs no code (e.g. "Only ONE case will have an effect"). |
| `INACTIVE` | `text`, optional `reason` | Tooltip text followed by a grey "Not active yet". Use it for anything that needs an unbuilt system. |

**Conditions** (any combination; all must hold):
- `servers: [SKYBLOCK_HUB, SKYBLOCK_CRIMSON_ISLE, ...]`: `ServerType` names.
- `regions: [VILLAGE, ...]`: `RegionType` names. Note that Crimson Isle has no region boxes yet, so
  use `servers:` there.
- `time: DAY` or `NIGHT` (SkyBlock time, day is 6:00 to 20:00).

`mob_types` are `MobType` names: `AIRBORNE ANIMAL AQUATIC ARCANE ARTHROPOD CONSTRUCT CUBIC ELUSIVE
ENDER FROZEN GLACIAL HUMANOID INFERNAL MAGMATIC MYTHOLOGICAL PEST SHIELDED SKELETAL SPOOKY
SUBTERRANEAN TIMID UNDEAD WITHER WOODLAND`.

### Writing a new HANDLER

1. Add `Map.entry("MY_ID", "path/to/TheClass")` to `AccessoryHandlers`.
2. In your code, check ownership with `AccessoryOwnership.owns(player, ItemType.X)` or loop over
   `AccessoryOwnership.activeItems(player)`. **Never** scan the bag or inventory yourself: the
   ownership rule already handles the bag plus inventory, duplicates and families.
3. Any chance roll goes through `LootRoll`.
4. Put `- type: HANDLER` / `handler: MY_ID` on each accessory it covers.

---

## 5. Families

`configuration/skyblock/accessories/families.yml` holds 95 families generated from Hypixel data:
91 upgrade lines (`UPGRADE_LINE`, members lowest to highest) and 4 "only one counts" groups
(`EXCLUSIVE_GROUP`: Hatcessories, Abicases, Dante, Beastslayer's Codex) with a `pick` for
`/maxprofile`.

- Within a family only one member counts for stats, effects and Accessory Power: the highest tier
  of an upgrade line, or the member worth the most Accessory Power in a group. Duplicates of the
  same item never stack. An accessory in no family stands alone.
- Members are written with **the ids this server uses**. Thirteen older repo ids differ from
  Hypixel's (Scarf's line, the Abicases, Mine/Village Affinity); `ALIASES` in
  `accessory_families.py` maps them. Add a new accessory under its Hypixel id and it joins its
  line with no edit.
- A line Hypixel adds later: append a family block at the end of the file by hand. An item can be
  in only one family (the loader refuses duplicates).

---

## 6. Rules the framework applies (don't re-implement them)

- **Where accessories work:** in the Accessory Bag or the player's inventory. Only the counted ones
  that sit in the bag add Accessory Power.
- **Accessory Power:** rarity value (Common 3, Uncommon 5, Rare 8, Epic 12, Legendary 16, Mythic
  22, Divine 28, Special 3, Very Special 5) of the effective rarity, then `power:` and `dungeon:`
  rules, plus 11 once a Rift Prism has been imbued at Erihann.
- **SkyBlock XP:** the Accessory Bag task pays +1 XP per current Accessory Power.
- **Bag size:** 9 base, +6 per Redstone collection tier II to IX, +2 per Jacobus purchase (up to
  99), +1 per Accessory Size attribute level (up to 10), and the unbuilt Community Center (12) and
  Redstone Miner (4) sources: 281 at most.
- **`/maxprofile`:** gives each family's `pick` (or top member) and every standalone accessory,
  sets dynamic accessories to `dynamic_max_rarity`, recombobulates where allowed, and skips
  `only_in` accessories. New accessories are covered automatically.
