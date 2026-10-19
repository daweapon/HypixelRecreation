# Bazaar Parity Tracking

Working doc for cross-referencing this server's Bazaar (`type.skyblockgeneric/.../bazaar/BazaarCategories.java`) against the real Hypixel SkyBlock Bazaar (2,197 tradable products as of 2026-09-07, pulled live from `https://api.hypixel.net/v2/skyblock/bazaar`). Scope, per user decision (2026-09-07): **only add items relevant to zones/content that already exist in this clone.** Everything else is recorded here, grouped by real-game sub-category, so a future pass can pick it up mechanically once that zone/system gets built.

Real bazaar category/sub-group data below is transcribed from `https://hypixelskyblock.minecraft.wiki/w/Bazaar/Item_List` (raw wikitext, fetched 2026-09-07) — re-fetch if this file is more than a few months stale, prices/catalog do change.

Zones/mechanics that exist in this clone (from `loader/build.gradle.kts` module deps): `island` (private island), `hub`, `thefarmingislands`, `spidersden`, `theend`, `crimsonisle`, `goldmine`, `deepcaverns`, `dwarvenmines`, `thepark`, `galatea`, `backwaterbayou`, `jerrysworkshop`, `dungeonhub`. Notably **absent**: Rift, Glacite Tunnels/Mineshaft (post-Dwarven update), Crystal Hollows as its own zone (may be folded into `deepcaverns` — needs checking when tackling Mining), and any zone-agnostic system whose implementation status needs checking case-by-case (pets, minions, enchanting — these matter for Oddities).

## Status

- **FARMING — DONE (2026-09-07).** All real Farming items now exist as ItemTypes and are wired into `BazaarCategories.FARMING`. New placeholder items (marked `BAZAAR_PLACEHOLDER` component, bold red "ITEM NOT ADDED YET JUST FOR BAZAAR PLACEHOLDER" lore): `CROPIE`, `SQUASH`, `FERMENTO`, `CONDENSED_FERMENTO`, `WRIGGLING_LARVA`, `FINE_FLOUR`, `ENCHANTED_CARROT_ON_A_STICK`, `ENCHANTED_COOKED_RABBIT`. Split into "Garden" / "Garden Resources" (12-item real group > 10-slot cap).
- **MINING — DONE (2026-09-07).** All real Mining items wired, including all 60 gemstones (already existed under `{TIER}_{GEM}_GEM` naming, just needed wiring — no new items). New placeholder items (in `configuration/skyblock/items/mining/vanilla.yml` and `dwarven.yml`): `REFINED_DIAMOND`, `DRILL_ENGINE`, `BEJEWELED_HANDLE`, `MITHRIL_PLATE`, `GOLDEN_PLATE`, `PRECURSOR_APPARATUS`, `JUNGLE_HEART`, `YOGGIE`, `REFINED_UMBER`, `UMBER_PLATE`, `UMBER_KEY`, `REFINED_TUNGSTEN`, `TUNGSTEN_PLATE`, `TUNGSTEN_KEY`, `GLACITE_AMALGAMATION`, `PERFECT_PLATE`, `SKELETON_KEY`, `SUSPICIOUS_SCRAP`. Confirmed Glacite Tunnels content (Umber/Tungsten/Glacite) IS already present in this clone under `dwarvenmines`, contrary to this doc's earlier assumption — corrected. Excluded "Fuel Canister" (listed on the wiki but not a real bazaar product per the live API). "Crystal Hollows"/"Glacite Tunnels" real groups (16 items each) split in two.
- **COMBAT — DONE (2026-09-07).** 57 new placeholder items in `configuration/skyblock/items/combat/bazaar_placeholders.yml`. Several wiki display names map to different real IDs than a literal guess would produce — notably dragon fragments drop the word "DRAGON" entirely (`PROTECTOR_FRAGMENT` not `PROTECTOR_DRAGON_FRAGMENT`), "Drill Motor" → `DRILL_ENGINE`, "Trap"/"Decoy" → `DUNGEON_TRAP`/`DUNGEON_DECOY`, current "Amalgamated Crimsonite" → `AMALGAMATED_CRIMSONITE_NEW` (an old, price-0 `AMALGAMATED_CRIMSONITE` also exists — unused, legacy). Excluded "Arachne's Calling", "Jolly Pink Rock", and the entire "Catacombs - Special Fragments" subgroup (Jolly Pink Rock, L.A.S.R.'s Eye, Bigfoot's Lasso, Diamante's Handle) — none of these are real tradable bazaar products per the live API despite being wiki-listed, likely non-bazaar boss-drop crafting fragments.
- **WOODS & FISHES — DONE (2026-09-07).** Was a fully empty stub category — now has all real sub-groups wired. 30 new placeholder items in `configuration/skyblock/items/miscellaneous/fishing_bazaar_placeholders.yml`. Notable corrections from wiki names: "Hunk of Ice"/"Hunk of Blue Ice" → `ICE_HUNK`/`BLUE_ICE_HUNK`, "Thunder Fragment" → `THUNDER_SHARDS`. **Excluded "Fish - Raw Cod" entirely** (Raw Cod, Enchanted Raw Cod, Enchanted Cooked Cod) — confirmed not real bazaar-tradable products under any ID variant in the live API, despite being wiki-listed as their own subcategory.
- **ODDITIES — not started, and structurally different.** Real Oddities is dominated by cross-cutting game *systems* (every pet's Shard, every enchantment at every level as a separate tradable product, minion upgrades, essences, event currencies) rather than zone drops. Relevance there should be judged by whether the underlying system (pets, minions, enchanting, a specific event) is implemented, not by zone. This category alone is ~800+ real products (180+ pet shards, 300+ enchanted-book level-variants) — needs its own scoping conversation before starting, not just a mechanical add-what's-missing pass.

## Video pass (2026-10-12)

The user recorded the real Bazaar (2026-10-12 00-45-43.mkv). The full transcription is `.claude/items-work/bazaar_video_catalogue.md` and the change list is `.claude/items-work/bz_video_report.md`. Summary of what the video changed:

- Every category now has exactly the tiles of the video, in the video's slot order (24-slot grid, explicit `.at()` positions where a tile we do not have leaves a gap). Mining uses light blue panes.
- Real menu frame: left tab column, panes only on the outer ring, button row (Search, Sell Inventory Now, Sell Sacks Now, Close, Manage Orders, Bazaar History, Bazaar Settings), black-pane group menus with `Sell Inventory Now`, `Go Back`, `Close`, `Manage Orders` and the view-mode button, titles `<parent> ➜ <group>` with `(n/N) ` when paged.
- Group tooltips list every product with a rarity-coloured arrow; one-product tiles show Buy price and Sell price; product tiles show the `<Rarity> commodity` line.
- Garden is a nested group (Rare Crops, Sprayonator, Other, Garden Chips, Mutations); Minion Upgrades and Pet Upgrades are nested groups of the video's 27 and 29 entries; Inferno Minion moved from Slayers to Oddities; Lotus Atoll is its own Woods & Fishes tile.
- Removed from the Bazaar because the video's categories do not list them: Griffin Feather, Daedalus Stick, Ancient Claw, Enchanted Ancient Claw, Mythos Fragment (the old Combat "Mythological" tile), Signal Enhancer, Rabbit, Stretching Sticks, Coalroot, Jacob's Participation Medal, Tool Exp Capsule, Walnut, Miria's Coupon, Filled Rosewater Flask, Brine Tonic, Blizzard in a Bottle Mixin, Sunflower Butter and Iridium.
- Still missing (not buildable as plain items): the Enchantments tile (113 normal and 26 ultimate enchantment products) and the Essence tile (11 essence products). They need per-level enchanted book products and essence-to-currency delivery in `BazaarAwarder`.

## Data quality note

The live bazaar API (2,197 products) was treated as ground truth throughout. Several times the wiki's `Bazaar/Item_List` page listed an item under a category that turned out not to be a real tradable bazaar product at all when checked against the live API (e.g. Raw Cod, Arachne's Calling, Jolly Pink Rock, the whole "Catacombs - Special Fragments" group) — always cross-check a wiki-only name against `bazaar.json`/`items.json` before creating a placeholder for it. Conversely, several "MISSING" hits from naive name→ID guessing turned out to already exist under a different real ID (gemstones use `_GEM` not `_GEMSTONE`; dragon fragments drop the word "DRAGON"; several wiki display names differ from their internal product ID) — always grep for near-matches before creating a new item.

## Known infrastructure limit

`GUIBazaarItemSet`'s `SLOTS` map (`type.skyblockgeneric/.../gui/inventories/bazaar/GUIBazaarItemSet.java`) hard-codes layouts for item-set sizes 1–10 with no pagination; a set of 11+ throws NPE (`SLOTS.get(size)` returns null). Farming's Garden group was split into two sets to work around this. Mining's Gemstone (12 types × 5 tiers = 60), Dwarven Mines/Crystal Hollows groups, and basically all of Oddities will hit this constantly. **Before starting Mining, consider adding real pagination to this GUI instead of keep splitting into arbitrarily-named sub-sets** — splitting doesn't scale past a handful of categories.

## Real Bazaar item lists (for reference — Farming omitted, already done)

### MINING

- Cobblestone: Cobblestone, Enchanted Cobblestone
- Coal: Coal, Enchanted Coal, Enchanted Charcoal, Enchanted Coal Block
- Iron: Iron Ingot, Enchanted Iron Ingot, Enchanted Iron Block
- Gold: Gold Ingot, Enchanted Gold Ingot, Enchanted Gold Block
- Diamond: Diamond, Enchanted Diamond, Enchanted Diamond Block, Refined Diamond
- Lapis: Lapis Lazuli, Enchanted Lapis Lazuli, Enchanted Lapis Lazuli Block
- Emerald: Emerald, Enchanted Emerald, Enchanted Emerald Block
- Redstone Dust: Redstone Dust, Enchanted Redstone Dust, Enchanted Redstone Block
- Obsidian: Obsidian, Enchanted Obsidian
- End Stone: End Stone, Enchanted End Stone — **zone: The End (exists — `theend`)**
- Flint & Gravel: Flint, Enchanted Flint, Gravel
- Sand: Sand, Enchanted Sand
- Ice: Ice, Packed Ice, Enchanted Ice, Enchanted Packed Ice, Glacial Fragment
- Snow: Snowball, Snow Block, Enchanted Snow Block
- Hard Stone: Hard Stone, Enchanted Hard Stone, Concentrated Stone
- Gemstones (12 types × 5 tiers = 60 items: Jade/Amber/Topaz/Sapphire/Amethyst/Jasper/Ruby/Opal/Onyx/Aquamarine/Citrine/Peridot, each Rough/Flawed/Fine/Flawless/Perfect) — zone: Crystal Hollows, need to confirm if this exists under `deepcaverns` or is missing entirely
- Goblin Eggs: Goblin Egg, Red/Blue/Yellow/Green Goblin Egg
- Dwarven Mines: Mithril, Enchanted Mithril, Refined Mithril, Starfall, Titanium, Enchanted Titanium, Refined Titanium, Treasurite, Sorrow — **zone exists (`dwarvenmines`)**, most already wired per current `BazaarCategories.MINING`
- Forge: Plasma, Glacite Jewel, Fuel Canister, Bejeweled Handle, Gemstone Mixture, Mithril Plate, Drill Motor, Golden Plate — zone: Dwarven Mines Forge, check if implemented
- Crystal Hollows: Precursor Apparatus, Electron Transmitter, FTX 3070, Robotron Reflector, Superlite Motor, Control Switch, Synthetic Heart, Jungle Key, Jungle Heart, Divan Fragment, Corleonite, Sludge Juice, Yoggie, Ascension Rope, Wishing Compass, Bob-omb
- Crimson Isle — Glowstone/Netherrack/Nether Quartz/Red Sand/Sulphur/Mycelium: **zone exists (`crimsonisle`)**, all already wired per current `BazaarCategories.MINING`
- Glacite Tunnels: Umber, Tungsten, Glacite (+ refined/plate/key variants), Perfect Plate, Skeleton Key, Suspicious Scrap — **zone likely does NOT exist in this clone (post-Dwarven-update content) — defer entire group**

### COMBAT

- Rotten Flesh, Bone, Arachnids (String/Spider Eye/etc. — zone: Spider's Den, exists), Gunpowder, Ender Pearl, Ghast Tear, Slimes, Magma Cream, Blaze Rod — all general/zone-agnostic or Spider's Den, likely mostly already covered, needs a pass like Farming got
- The End: Summoning Eye, Crystal Fragment, Dragon Fragments (8 types), Draconic Sacrifice (Ritual Residue, Mite Gel) — **zone exists (`theend`)**
- Crimson Isle — Mob Drops / Mini Bosses / Special — **zone exists (`crimsonisle`)**
- Slayers (Revenant/Tarantula/Sven/Voidgloom/Inferno Demonlord materials) — need to check if the Slayer system itself is implemented before adding
- Catacombs (Special Fragments/Mob Drops/Secret Drops/Misc.) — **zone exists (`dungeonhub`)**, check Catacombs implementation status

### WOODS & FISHES

- Wood: Oak/Spruce/Birch/Dark Oak/Acacia/Jungle (general, Hub/Park foraging — exists), Fig/Mangrove — **zone: Galatea (exists)**
- Fish: Raw Cod/Salmon/Tropical Fish/Pufferfish — general fishing, likely relevant everywhere fishing exists
- Flowers, Prismarine, Clay Ball, Sponge, Lily Pad, Ink Sac — general/ocean fishing byproducts
- Baits (20 types) — general fishing system item, check if bait/fishing system implemented
- Mob Drops, Magmafish, Lava Fishing, Water Orbs, Chum Fishing — mostly Crimson Isle lava fishing (**zone exists**) or general
- Backwater Bayou — **zone exists (`backwaterbayou`)**
- Moonglade Marsh — **zone: Galatea (exists)**

### ODDITIES

See full raw list already captured in this session's transcript / re-fetch `https://hypixelskyblock.minecraft.wiki/w/Bazaar/Item_List?action=raw` (raw wikitext, the rendered page truncates before Oddities). Rough shape: Booster Cookie; Modifiers (Item Upgrades, Wither Scrolls, Master Stars, Book Modifiers, Foraging Boosters); Enchantments (Normal + Ultimate, each level a separate product — ~300+ items); EXP Bottles; Shards (one per pet, ~180 items); Reforge Stones; Power Stones; Pet Upgrades (candies, cores); Inferno Minion items; Minion Upgrades (storage, compactors, fuels); Essence (9 types); Events (Spooky/Winter/Minerals/Jacob's/Fishing Festival/Jerry Boxes/Diaz/Hoppity's/Year of the Seal); Consumables; Cross-Dimensional; Upgrade Fragments.

**Do not start Oddities mechanically** — first confirm with the user which underlying systems (pets, minions, enchanting, specific seasonal events) are actually implemented in this clone, since that's the real relevance filter here, not zones.
