# Commands reference

Sorted reference for the current HypixelRecreation source tree. Updated 2026-10-11.

This catalog covers **130 top-level game-server command definitions** (after merging 52 staff commands into subcommand groups, see Merged staff commands), **7 proxy commands**, and the optional Spark command. It describes the remake as implemented, not a promise of complete real-Hypixel parity. Alias research is in [command-alias-audit.md](command-alias-audit.md).

## Contents

- [Reading the reference](#reading-the-reference)
- [Player commands](#player-commands)
- [Staff commands](#staff-commands)
- [Proxy commands](#proxy-commands)
- [Subcommand reference](#subcommand-reference)
- [Merged staff commands](#merged-staff-commands)
- [Examples](#examples)
- [Other game modes](#other-game-modes)
- [Known registration and help discrepancies](#known-registration-and-help-discrepancies)
- [Admin GUI planning](#admin-gui-planning)

## Reading the reference

`<argument>` is required; `[argument]` is optional; `a|b` lists alternatives. The command column uses a readable registered label; every other registered label is in Aliases. Entries are alphabetized within each purpose group. Repeated labels in separate game-mode modules are separate definitions, not automatically conflicting commands.

**Availability:** `type.generic` commands are loaded across game servers; `type.skyblockgeneric` commands are loaded on SkyBlock servers; `type.lobby` commands are loaded by the shared lobby loader. Other modules require their corresponding server type. A command existing in source does not mean that mode is currently deployed. Proxy commands run at the Velocity layer.

**Permissions:** DEFAULT means the default rank passes the entry check; it does not override party roles, guild permissions, co-op membership, progression requirements, or other checks inside the handler/service. STAFF is the remake's staff rank. Console is the annotation's declared eligibility, not proof the entire handler is console-safe: some handlers cast senders to players. Prefer in-game staff execution unless console behavior has been verified for that command.

**Arguments:** Entity selectors used by staff commands resolve players on the current game server unless the notes say otherwise. Name-based commands can perform cross-server lookups. For enum IDs (items, skills, server types, etc.), use tab completion rather than assuming display names are valid IDs. Amount-setting commands such as `/coins`, `/bits`, and `/xp` set values; they are not automatically additive grants.

## Player commands

### Co-op and visiting

| Command / syntax | Aliases | Purpose and notes | Minimum rank | Console declared | Module |
| --- | --- | --- | --- | --- | --- |
| `/coop <player> [player ...]` | `/cooperative` | Primary coop command. Starts the co-op invitation flow; existing co-op and profile restrictions apply. | DEFAULT | No | `type.skyblockgeneric` |
| `/coopadd <player>` | — | Adds a player to your existing co-op. | DEFAULT | No | `type.skyblockgeneric` |
| `/coopbank; /coopbank <deposit\|withdraw> <amount>` | `/cbank` | Deposits or withdraws from your co-op's shared bank account. Shared co-op bank; membership, balance, and amount checks apply. | DEFAULT | No | `type.skyblockgeneric` |
| `/coopchat <message>` | `/cchat`, `/cc` | Sends a message to your co-op. | DEFAULT | No | `type.skyblockgeneric` |
| `/coopcheck` | `/cooperativecheck` | Checks your pending co-op invite. Opens the relevant outgoing or incoming invitation GUI. | DEFAULT | No | `type.skyblockgeneric` |
| `/coopclear` | — | Clears pending co-op invites. A member cancels the co-op's outgoing invites; an invited player clears their own incoming invite. | DEFAULT | No | `type.skyblockgeneric` |
| `/coopkick <player>` | — | Kicks a player from your co-op. | DEFAULT | No | `type.skyblockgeneric` |
| `/coopleave` | `/cooperativeleave` | Leaves the current coop. | DEFAULT | No | `type.skyblockgeneric` |
| `/coopstats` | — | Shows each co-op member's mining/farming contribution. | DEFAULT | No | `type.skyblockgeneric` |
| `/coopview <player>` | — | Shows the named player's current co-op members. | DEFAULT | No | `type.skyblockgeneric` |
| `/coopvote <accept\|deny>` | — | Votes on an active co-op add/kick vote. | DEFAULT | No | `type.skyblockgeneric` |
| `/ignore <add\|remove\|list> [player]` | — | Blocks a player from visiting your island. | DEFAULT | No | `type.skyblockgeneric` |
| `/islandprivacy <public\|friends\|private>` | — | Sets who can visit your island. | DEFAULT | No | `type.skyblockgeneric` |
| `/sbkick <player>` | — | Kicks a guest off your island. | DEFAULT | No | `type.skyblockgeneric` |
| `/sbkickall` | — | Kicks every guest off your island. | DEFAULT | No | `type.skyblockgeneric` |
| `/visit <player>` | — | Visits another player's SkyBlock island. | DEFAULT | No | `type.skyblockgeneric` |

### Menus and progression

| Command / syntax | Aliases | Purpose and notes | Minimum rank | Console declared | Module |
| --- | --- | --- | --- | --- | --- |
| `/achievements` | `/achievement` | Opens the achievements menu. On SkyBlock servers it opens the SkyBlock achievements page (also reachable from Your Equipment and Stats); in lobbies it opens the full achievements menu. The achievement-unlock chat message links here. | DEFAULT | No | `type.generic` |
| `/attributesmenu` | `/attributes`, `/attributemenu`, `/am` | Opens your Attributes menu. | DEFAULT | No | `type.skyblockgeneric` |
| `/auctionhouse` | `/ah` | Opens the Auction House remotely. Requires an active Booster Cookie. | DEFAULT | No | `type.skyblockgeneric` |
| `/bait` | — | Opens the Bait Guide, the same menu Angler Angus shows after he gives you Fish Bait. | DEFAULT | No | `type.skyblockgeneric` |
| `/bank` | — | Open your Personal Bank. | DEFAULT | No | `type.skyblockgeneric` |
| `/bazzar` | `/bazaar`, `/bz` | Opens the Bazaar remotely. Requires an active Booster Cookie and SkyBlock Level 7. | DEFAULT | No | `type.skyblockgeneric` |
| `/bestiary` | `/be` | Opens the Bestiary menu. Needs the Bestiary unlocked by speaking to Bramass Beastslayer (staff can use `/unlock bestiary`). | DEFAULT | No | `type.skyblockgeneric` |
| `/calendar` | — | Opens the SkyBlock Calendar. | DEFAULT | No | `type.skyblockgeneric` |
| `/chips` | `/gardenchips` | Opens the Garden Chips menu, where you level up the chips you have redeemed with Sowdust. | DEFAULT | No | `type.skyblockgeneric` |
| `/chocolatefactory` | `/cf`, `/factory` | Opens the Chocolate Factory menu. | DEFAULT | No | `type.skyblockgeneric` |
| `/clearstash [confirm]` | — | Clear your stash. Confirmation is required before deleting the stash. | DEFAULT | No | `type.skyblockgeneric` |
| `/etable` | `/et`, `/enchantingtable`, `/enchantmenttable`, `/enchantingtablemenu`, `/enchantmenttablemenu` | Opens an Enchantment Table remotely. Requires an active Booster Cookie. | DEFAULT | No | `type.skyblockgeneric` |
| `/farmingtoolkit` | — | Opens the Farming Toolkit, which stores one of each Specialized Farming Tool. Click a valid tool in your inventory to store it and click a stored tool to take it back. Requires consuming a Farming Toolkit item (SkyMart, 500 Copper) once. | DEFAULT | No | `type.skyblockgeneric` |
| `/help [page\|command]` | — | Alphabetical chat command list, eight per page, with clickable navigation. Click a command or look up its alias for usage and aliases. Filters by minimum rank and loaded game-server commands; includes proxy travel commands. | DEFAULT | No | `type.generic` |
| `/hotm` | `/heartofthemountain` | Opens the Heart of the Mountain: spend Tokens and Powder on mining perks, upgrade the Core of the Mountain, unlock and select pickaxe abilities and reset perks. Also reachable from the Mining skill menu. Perks that need Glacite Mineshafts, the Forge or Crystal Hollows can be bought but have no effect until that content exists. | DEFAULT | No | `type.skyblockgeneric` |
| `/huntingbox` | `/hb` | Opens your Hunting Box. | DEFAULT | No | `type.skyblockgeneric` |
| `/pets` | `/petsmenu`, `/petmenu`, `/viewpetsmenu` | Opens your Pets menu. | DEFAULT | No | `type.skyblockgeneric` |
| `/pickupstash [item\|material]` | — | Pickup items from your stash. | DEFAULT | No | `type.skyblockgeneric` |
| `/rankcolor` | `/rankcolour` | Change your MVP+ rank color. | DEFAULT | No | `type.generic` |
| `/replay <uuid> [hex]` | — | Watch a specific replay by ID. | DEFAULT | No | `type.generic` |
| `/replays` | — | View your game replays. | DEFAULT | No | `type.generic` |
| `/selectnpcoption <npc> <option>` | — | Selects an option from an NPC dialogue. Internal NPC dialogue entry point; both arguments are required. | DEFAULT | No | `type.skyblockgeneric` |
| `/sphinxanswer <sphinx> <choice>` | — | Answers a Sphinx's riddle during Diana's Mythological Ritual. Internal entry point behind the clickable answers; only the player who dug the Sphinx up can answer, once. | DEFAULT | No | `type.skyblockgeneric` |
| `/slayer start <type> <tier>; /slayer status; /slayer cancel` | — | Starts and manages Slayer quests. Starts a quest, displays status, or cancels it; progression and cost checks apply. | DEFAULT | No | `type.skyblockgeneric` |
| `/toggledropalert` | `/dropalert` | Toggles drop alert. | DEFAULT | No | `type.skyblockgeneric` |
| `/viewcollection <collection>` | `/vc` | Opens up a collections GUI. | DEFAULT | No | `type.skyblockgeneric` |
| `/viewrecipe <recipe>` | `/vr` | Opens up a recipe GUI. | DEFAULT | No | `type.skyblockgeneric` |
| `/viewskill <skill>` | `/vs` | Opens up a skills GUI. | DEFAULT | No | `type.skyblockgeneric` |
| `/viewstash [item\|material]` | — | View your stash. | DEFAULT | No | `type.skyblockgeneric` |
| `/wardrobe` | `/wd` | Open your Wardrobe. | DEFAULT | No | `type.skyblockgeneric` |
| `/wikithis` | `/wikihand`, `/wikiinhand` | Shows page link of the item held towards the official Hypixel Wiki. Emits wiki links for the held item. Official wiki URLs now redirect to the closure announcement; see alias audit. | DEFAULT | No | `type.skyblockgeneric` |

### Social and chat

| Command / syntax | Aliases | Purpose and notes | Minimum rank | Console declared | Module |
| --- | --- | --- | --- | --- | --- |
| `/chat <a\|all\|p\|party\|s\|staff\|sv\|staffview>` | `/chatmode`, `/ch` | Changes chat mode. Staff modes require STAFF. Guild, officer, and co-op persistent modes are not implemented here. /ch is an alias. | DEFAULT | No | `type.generic` |
| `/fl [page\|best]` | `/friendlist` | List your friends. Lists friends; best selects the best-friend view. | DEFAULT | No | `type.generic` |
| `/friend <subcommand>` | `/f` | Friend management commands. | DEFAULT | No | `type.generic` |
| `/gchat <message>` | `/gc` | Sends a message to guild chat. Forwards to /guild chat; requires guild membership. | DEFAULT | No | `type.generic` |
| `/gl` | — | Lists your guild members. Forwards to /guild list. | DEFAULT | No | `type.generic` |
| `/guild <subcommand>` | `/g` | Guild management commands. | DEFAULT | No | `type.generic` |
| `/msg <player> <message>` | `/message`, `/whisper`, `/w`, `/tell`, `/t`, `/m`, `/whipser` | Sends a message to another player. | DEFAULT | No | `type.generic` |
| `/ochat <message>` | `/oc` | Sends a message to guild chat. Forwards to /guild officerchat; guild rank permission required. | DEFAULT | No | `type.generic` |
| `/party <subcommand>` | `/p` | Party management commands. | DEFAULT | No | `type.generic` |
| `/pchat <message>` | `/pc` | Sends a message to party chat. Forwards to /party chat with existing membership and chat restrictions. | DEFAULT | No | `type.generic` |
| `/pl` | — | Lists your party members. Forwards to /party list. | DEFAULT | No | `type.generic` |
| `/trade <player>` | — | Sends a trade request to a player, or accepts theirs if they already sent you one; shift-right-clicking a player or clicking the Emerald in their profile does the same. Opens the trade menu for both. The player must be on your server and within 9 blocks; Ironman profiles can't trade; Soulbound items, Sacks and the SkyBlock Menu are refused, and Co-op Soulbound items only go to co-op members. Coins sent per day are capped at 50M (1B from SkyBlock Level 50, 10B from Level 100). Requests expire after 60 seconds. | DEFAULT | No | `type.skyblockgeneric` |

### Travel and lobbies

| Command / syntax | Aliases | Purpose and notes | Minimum rank | Console declared | Module |
| --- | --- | --- | --- | --- | --- |
| `/hub` | `/h` | Sends the player to their hub. | DEFAULT | No | `type.skyblockgeneric` |
| `/instancerequeue` | — | Inside a Kuudra run, queues your party for a new run of the same tier, as the clickable line at the end of a run does. Only the instance creator (the player who queued the run) can use it; anyone else gets "Only the instance creator can re-queue!". Works mid-run too: everyone leaves the old run for the new one. The usual queue checks apply (party leader, party of 1-4, every member meets the tier's requirements), and while Kuudra is staff-only only staff can queue. Only on Kuudra servers. | DEFAULT | No | `type.kuudra` |
| `/is` | `/island` | Sends the player to their island. | DEFAULT | No | `type.skyblockgeneric` |
| `/parkour` | — | Parkour related commands. | DEFAULT | No | `type.lobby` |
| `/plottp <plot>` | — | Teleports you to the centre of one of your unlocked Garden plots (`0` is the Barn). Only works while you are in your Garden; the pest spawn message links to it. | DEFAULT | No | `type.skyblockgeneric` |
| `/sl <number>` | — | Switch to a numbered lobby. | DEFAULT | No | `type.lobby` |
| `/warp <warp>` | `/teleportregion` | Warps to an unlocked island or travel-scroll destination, using Hypixel's warp names and aliases. Islands: `home` (`island`), `hub` (`village`), `dungeon_hub` (`dungeons`, `dhub`, `dh`), `farming` (the Farming Islands Barn), `garden` (`barn`), `park` (`trees`, `foraging`), `gold`, `deep`, `dwarven` (`dwarves`, `mines`, `dmines`, `deeper`), `spider` (`spiders`), `end`, `nether` (`isle`, `crimson`, `ci`), `jerry` (`winter`, `workshop`), `bayou` (`backwater`) and `moonglade` (`galatea`, `marsh`). Scroll warps: Hub `museum`, `da`, `castle`, `crypts` (`crypt`), `wizard` (`wiz`, `tower`, `wizard_tower`), `elizabeth`, `stonks`, `taylor`; `desert`, `trapper` (`trap`); `howl` (`howling_cave`, `wolf_cave`), `jungle`; `forge`, `basecamp` (`base`, `camp`, `glacite`, `tunnel`, `tunnels`, `gt`); `nest` (`top`, `mound`), `arachne` (`sanctuary`); `drag` (`dragons`), `void` (`sepulture`); `kuudra` (`skull`), `wasteland`, `dragontail`, `scarleton`, `smold` (`smoldering`, `smoldering_tomb`); `murkwater` (`murk`, `loch`, `murkwater_loch`). The Park unlocks with its Foraging travel scroll and the Crimson Isle with its scroll plus Combat XXII; the Garden unlocks once you have met Sam and opened it (`garden`, `barn`), other islands unlock on the first visit, and each scroll warp needs its own scroll. `crystals`, `nucleus`, `rift`, `torrhus`, `springs`, `safari` and `lotus` are listed but answer that the destination isn't in SkyBlock yet. | DEFAULT | No | `type.skyblockgeneric` |

## Staff commands

### Development and diagnostics

| Command / syntax | Aliases | Purpose and notes | Minimum rank | Console declared | Module |
| --- | --- | --- | --- | --- | --- |
| `/admin` | `/adminmenu` | Opens the staff Admin GUI; also available in the SkyBlock menu. Browse categories, search commands, enter arguments and review before running. | STAFF | No | `type.skyblockgeneric` |
| `/adminme` | `/forceadmin` | Literally just gives me admin. Developer allowlist only, despite DEFAULT annotation. Resolves allowed UUIDs and grants STAFF; not a general player feature. | DEFAULT | No | `type.generic` |
| `/ahview <uuid>` | `/auctionview` | Views an auction. | DEFAULT | No | `type.skyblockgeneric` |
| `/allowaccessforapiauthentication <code>` | `/verifyapi` | Allows access for API authentication for the given code. | STAFF | No | `type.skyblockgeneric` |
| `/centauri` | — | Opens the Centauri menu: Item List (opens the four item categories), Coin Generator and Toy Box. | STAFF | No | `type.skyblockgeneric` |
| `/debug <configids\|damageindicator\|lag\|mining\|recipes\|tasks\|headrotation\|mutex\|particles\|anvilgui\|music\|formatname\|localedata\|npcpathfinding\|explosion\|signgui\|component> ...` | `/formatname`, `/getnameformatted`, `/localedata`, `/npcpathfinding`, `/explodesimulation`, `/signgui`, `/signgraphicaluserinterface`, `/translatablecomponent`, `/displayconfigids`, `/dmgindicdisplaydebugcmd`, `/lagprofile`, `/mininginfo`, `/recipedebug`, `/taskcount`, `/testheadrotation`, `/testheadrot`, `/testmutex`, `/testpe`, `/testparticleengine`, `/anvilgui`, `/anvilgraphicaluserinterface`, `/playmusic`, `/playsong` | Diagnostics and test tools. Generic topics work on every server type; SkyBlock topics are added on SkyBlock servers. | STAFF | No | `type.generic` |
| `/itemlist [lookup]` | `/e` | With no argument, opens the Item List category menu (Regular Items, Pets, Talismans, Materials), the same menu as the Item List button in `/centauri`. With a lookup, searches every category at once and opens the browser on the matches. Raw vanilla Minecraft items are no longer listed; `/item` can still give any item. | STAFF | No | `type.skyblockgeneric` |
| `/mission [info]`<br>`/mission <data\|start\|clear\|clearset> ...` | `/missiondata`, `/displaymissiondata`, `/startmission`, `/clearmission`, `/clearmissionset`, `/completemissionset` | Shows, starts, clears and finishes missions. With no subcommand it runs `info`. | STAFF | No | `type.skyblockgeneric` |
| `/sc <message>` | `/staffchat` | Sends a message to staff chat. | STAFF | No | `type.generic` |
| `/throwexception` | — | Throws an exception for testing purposes. | STAFF | Yes | `type.generic` |

### Items and progression

| Command / syntax | Aliases | Purpose and notes | Minimum rank | Console declared | Module |
| --- | --- | --- | --- | --- | --- |
| `/addtostash <item> [amount] [player]` | — | Force add an item to your stash. | STAFF | No | `type.skyblockgeneric` |
| `/clear` | `/cl` | Rid your inventory of useless items. | STAFF | No | `type.skyblockgeneric` |
| `/factionquests <list\|reroll>; /factionquests complete <slot>` | `/fquests` | Lists today's Crimson Isle Faction Quests for your current faction, rolls a fresh Town Board for today (`reroll`), or marks slot 1–5 complete so it can be claimed at the Town Board without doing the task (`complete`). Needs a faction. | STAFF | No | `type.skyblockgeneric` |
| `/hand <enchant\|reforge\|recomb\|mithrilinfuse\|nbt\|setnbt\|tracked> ...` | `/enchant`, `/ench`, `/reforge`, `/reforgeitem`, `/recomb`, `/mithrilinfuse`, `/nbt`, `/iteminfo`, `/editnbt`, `/itemtrackedinformation` | Edits or inspects the item in your hand: enchant, reforge, recombobulate, Mithril Infuse, NBT and tracked-item information. | STAFF | No | `type.skyblockgeneric` |
| `/item <item> [amount] [player]` | `/i`, `/sbitem` | Gives an item to the player. `<item>` is an item ID (case-insensitive, suggested as you type) or the item's display name written as one word, e.g. `KINDLEBANE_DAGGER` gives the Kindlebane Dagger (ID `BURSTFIRE_DAGGER`). Also accepts /item <item> <player> with the default amount. Target is an online player selector. | STAFF | No | `type.skyblockgeneric` |
| `/kuudrarun <win\|lose\|start\|phase\|supplies\|build\|fuel\|stun\|lair\|eat\|spit> [phase]`, `/kuudrarun <eat\|spit> <player>`, `/kuudrarun <damage\|tokens\|wave> <amount>`, `/kuudrarun spawn <mob>` | — | Controls the Kuudra run you are standing in, for testing the fight. `start` begins the run at once, skipping the ready check where everyone talks to Elle (intro dialogue, bridge collapse, Kuudra rising, supply crates, tentacles, wave timer). `phase` moves to the phase that naturally follows (Supplies → Build → Fuel → Stun on Burning and up → DPS); `phase <supplies\|build\|fuel\|stun\|dps\|lair>` jumps to a phase the tier has. `supplies` drops off every supply and starts the Build, `build` finishes the Ballista, `fuel` loads every Fuel Cell and arms the Ballista. On Burning, Fiery and Infernal: `eat [player]` swallows you (or a party member) into Kuudra's stomach as the Consumption Zone would, `spit [player]` throws them back out of his mouth, and `stun` breaks one of the stomach's pods as if you had mined it, stunning Kuudra for his damage window; any of the three skips ahead to the Fuel phase first if the Ballista isn't built, and asks you to repeat it while Kuudra rises. On Infernal, `lair` takes Kuudra to 25% and starts the lair sequence (Elle's lines, the platform collapse, Kuudra diving into the lair); using it again plays the rest of the sequence at once. `damage <percent>` takes that share of Kuudra's health (reaching 0% plays his death and wins the run; on Infernal it stops at 25% and opens the lair), `tokens <amount>` gives every party member that many Kuudra Tokens, `wave <number>` spawns that wave's minions now (every fourth wave, 3, 7, 11 and so on, also lights four Energized Chunks), and `spawn <mob>` spawns one Kuudra minion by its `mobs.yml` id (e.g. `BLIGHT`, `DROPSHIP`, `MAGMA_BACTERIA`) where you stand. `win` ends a started run as a win: every party member in the Hollow gets the full `/kuudrasim` payout and the run's Free and Paid chests, which open straight away and stay at Vesuvius for 72 hours. `lose` ends the run as a defeat, started or not. Either way everyone returns to the Crimson Isle at the Forgotten Skull after 20 seconds. Only on Kuudra servers, and only inside a run. | STAFF | No | `type.kuudra` |
| `/kuudrasim <tier> [player]` | — | Simulates winning a Kuudra run, for yourself or an online player, so its rewards can be tested before the fight exists. `<tier>` is `BASIC`, `HOT`, `BURNING`, `FIERY`, `INFERNAL` or `1`–`5`. Records the clear (Kuudra boss collection, first-clear and collection SkyBlock XP, collection tier rewards), advances Kuudra Faction Quests, pays that tier's daily reputation once per UTC day, finishes the questline's "Defeat Kuudra" step, grants Kuudra Conundrum, rolls the Tentacle Dye, and rolls the run's Free and Paid chests. The chests open straight away and stay at Vesuvius for 72 hours; only one can be opened, and the Paid chest takes that tier's Kuudra Key. Works on every SkyBlock server. | STAFF | No | `type.skyblockgeneric` |
| `/max coa`<br>`/max profile [player]` | `/maxprofile` | Maxes out a held or worn Crown of Avarice (`coa`) or a whole profile (`profile`, identical to THE MAX STICK). | STAFF | No | `type.skyblockgeneric` |
| `/reset <chickenrace\|stocks> [player]` | `/resetchickenrace`, `/resetmystocksnow` | Resets the Chicken Race progress on a profile, or your shop stocks back to maximum. | STAFF | No | `type.skyblockgeneric` |
| `/set <coins\|bits\|xp\|essence\|reputation\|petlevel\|petrarity\|bestiary\|collection\|stat> ...` | `/coins`, `/bits`, `/bit`, `/xp`, `/experience`, `/setessence`, `/setreputation`, `/setrep`, `/setpetlevel`, `/updatepetlevel`, `/setpetrarity`, `/updatebestiary`, `/updatecollection`, `/setstat`, `/setstatistic` | Sets a profile value for yourself or an online player. One subcommand per value; each keeps the arguments of the command it replaced (see Merged staff commands). | STAFF | No | `type.skyblockgeneric` |
| `/skyblockxp <action> [amount] [player]` | `/sbxp` | Manages SkyBlock XP and SkyBlock Level. Also `cause <cause> [player]`, which awards a single named cause (old `/giveskyblockxp` and `/awardskyblockxp`). See the SkyBlock XP subcommand reference below. | STAFF | No | `type.skyblockgeneric` |
| `/unlock <warps\|bestiary> [player]` | `/unlockwarps`, `/unlockbestiary` | Unlocks every Fast Travel warp, or the Bestiary, for yourself or an online player. | STAFF | No | `type.skyblockgeneric` |
| `/visitor spawn <visitor>` | — | Summons any Garden visitor (even ones you are not eligible for yet) onto your Garden, with Tab completion on visitor ids and names. Garden server only; fails if the visitor is already present or the Garden is full. | STAFF | No | `type.skyblockgeneric` |

### Moderation and accounts

| Command / syntax | Aliases | Purpose and notes | Minimum rank | Console declared | Module |
| --- | --- | --- | --- | --- | --- |
| `/admincoopkick <player>` | — | Force-removes a player from their co-op, bypassing the member vote. | STAFF | No | `type.skyblockgeneric` |
| `/ban <player> [duration] <reason>` | `/tempban`, `/banip`, `/tempbanip` | Ban a player from the server. | STAFF | No | `type.generic` |
| `/mute <player> [duration] <reason>` | `/tempmute` | Mute a player from the server. | STAFF | No | `type.generic` |
| `/rank <player> <rank>` | `/setrank` | Rank command. | STAFF | Yes | `type.generic` |
| `/restore <player>` | — | Restores a player's profile if they were wiped within the last hour. Restores a recent wipe snapshot (one-hour window); see handler for online/offline handling. | STAFF | No | `type.generic` |
| `/unban <player>` | `/pardon`, `/unbanip`, `/pardonip` | Unban a player from the server. | STAFF | No | `type.generic` |
| `/unmute <player>` | — | Unmute a player on the server. | STAFF | No | `type.generic` |
| `/wipe [player]` | — | Wipes a player's profile data. No target wipes yourself. Captures a snapshot and wipes account/profile ownership data; rank and identity fields are preserved. Target selector resolves locally. | STAFF | No | `type.generic` |

### Travel, world, and events

| Command / syntax | Aliases | Purpose and notes | Minimum rank | Console declared | Module |
| --- | --- | --- | --- | --- | --- |
| `/adminbypass <event>`<br>`/adminbypass auction [uuid]`<br>`/adminbypass bin_grace [uuid]`<br>`/adminbypass <auctionhouse\|bazaar\|bank\|etable>`<br>`/adminbypass skiptospecialmayor`<br>`/adminbypass forcemayor <mayor>`<br>`/adminbypass resetyear`<br>`/adminbypass skipforgetime [slot]` | `/jumptonextevent`, `/skiptoevent` | Skips the selected SkyBlock calendar timer, ends an active Auction House listing, removes a BIN listing's 20-second purchase grace period, opens a Cookie-gated menu without an active Booster Cookie, force-elects the next special mayor (Scorpius/Derpy/Jerry, in rotation) for testing without a real election, force-elects a chosen mayor (by ID, e.g. `DIANA`) with all of their perks for testing perks such as the Mythological Ritual, resets this server's local SkyBlock calendar back to Year 1, Early Spring, Day 1, or finishes your active Forge processes (all of them, or only the given slot 1-7, ready to collect). Omitting the UUID targets your newest owned auction; the Cookie-menu bypasses require an in-game player. Event IDs are suggested by the command. The calendar is per-server (not synced), so `resetyear` only affects the server it's run on. | STAFF | Yes | `type.skyblockgeneric` |
| `/build` | `/buildmode` | Toggles whether or not you are in build mode. | STAFF | No | `type.skyblockgeneric` |
| `/force <drop\|diana\|darkauction\|eruption> ...` | `/forcedrop`, `/forcedianamob`, `/dianamob`, `/forcedarkauction`, `/forceeruption` | Forces a chance-based or timed event the next time it could happen: an item drop, the next Griffin burrow creature, a Dark Auction or a Blazing Volcano eruption. | STAFF | No | `type.skyblockgeneric` |
| `/gamemode <gamemode>` | `/gm` | Sets a players gamemode. | STAFF | No | `type.generic` |
| `/hollows <layout\|tp\|candidate\|anchor\|statue\|chest\|region\|spawn\|bounds\|validate\|debug\|heat\|spawnworm\|paste\|schematics> ...` | — | Staff tooling for capturing the Crystal Hollows world's coordinates in game and validating the configuration. See Merged staff commands. Only registered on the Crystal Hollows server. | STAFF | No | `type.crystalhollows` |
| `/regions <add\|remove\|wand> ...` | — | Handles regions across the server. See region syntax below. | STAFF | No | `type.skyblockgeneric` |
| `/sendto <server_type>` | `/transfer` | Transfers a player to another server. | STAFF | No | `type.generic` |
| `/spawn <mob> [amount]` | `/spawnseacreature`, `/spawnsc`, `/spawncrimsonmob`, `/spawncm` | Spawns any sea creature, registered SkyBlock mob, Mythological creature or Garden Pest (`PEST_<id>`, e.g. `PEST_FLY`, `PEST_FIELD_MOUSE`, `PEST_LUNAR_MOTH`) at your feet, 1-50 at a time. The id list comes from the game's registries, so new mobs are included automatically. See Spawning below. | STAFF | No | `type.skyblockgeneric` |
| `/speed <speed> [FLY\|WALK]` | `/flyspeed` | Sets a players speed. Speed range 0–10; defaults to FLY. | STAFF | No | `type.generic` |
| `/tp <player>`<br>`/tp <x> <y> <z>`<br>`/tp npc <npc>` | `/teleport`, `/tpnpc`, `/teleportnpc` | Teleports you to an online player, to coordinates or to an NPC. | STAFF | No | `type.generic` |
| `/world <structure\|tree\|regrow\|hologram\|crystal\|deleteorb\|minionspeed\|dragon\|velocity> ...` | `/dragon`, `/setvelocity`, `/structure`, `/tree`, `/regrowtrees`, `/spawnholo`, `/spawnhologram`, `/spawncrystal`, `/addcrystal`, `/deleteorb`, `/handleminionspeed` | World, structure, hologram, crystal, orb, minion-speed, dragon and velocity tools. Dragon and velocity are on every server type; the rest are added on SkyBlock servers. | STAFF | No | `type.generic` |
| `/worldtime (tick)` | `/skipworldtime` | Skips the time in the current instance. | STAFF | No | `type.generic` |

## Proxy commands

| Command | Aliases | Access | Purpose |
| --- | --- | --- | --- |
| `/find <username> [tp]` | — | STAFF, player only | Finds an online player's server; `tp` also transfers/teleports to their location. |
| `/limbo` | — | Player only | Enters Limbo, or displays information when already there. |
| `/lobby [game]` | — | Player only | Travels to the default or named lobby; destination aliases include `bw`, `bedwars`, `sw`, and `skywars`. `/l` is not registered here. |
| `/login <password>` | — | Unauthenticated player; authentication enabled | Logs into the local authentication system. |
| `/protocolversion` | `/protocol` | Player only | Displays the connected client's protocol version. |
| `/register <password> <password>` | — | Unauthenticated player; authentication enabled | Registers a local authentication password. |
| `/serverstatus` | `/status` | No explicit rank restriction; console accepted | Lists registered game servers and player counts. |

## Subcommand reference

### Party

Use `/party` or `/p`. `/party` alone displays help. The following forms are implemented; membership and role validation remains in the party service.

| Syntax | Behavior |
| --- | --- |
| `/party <player>` | Invites a player using the single-argument fallback. |
| `/party accept <player>` | Accepts that player's invitation. |
| `/party chat <message>` | Sends party chat; aliases `/pchat`, `/pc`. |
| `/party demote <player>` | Demotes a party member. |
| `/party disband` | Disbands the party. |
| `/party hijack <player>` | Staff override; see permission check in the handler. |
| `/party invite <player>` | Invites a player; `add` is equivalent. |
| `/party kick <player>` | Removes a member; `remove` is equivalent. |
| `/party kickoffline` | Removes offline members; `removeoffline` is equivalent. |
| `/party leave` | Leaves the party. |
| `/party list` | Lists party members and roles; alias `/pl`. |
| `/party movetoserver <server_uuid>` | Internal transfer entry point: parses a UUID and transfers the invoking player, not the entire party. |
| `/party mute` | Toggles restricted party chat. |
| `/party promote <player>` | Promotes a member. |
| `/party settings allinvite` | Toggles non-leader/moderator invitations. |
| `/party settings size <number>` | Sets the party's configured member limit. |
| `/party transfer <player>` | Transfers leadership. |
| `/party warp` | Warps party members to the leader. |

### Friends

Use `/friend` or `/f`; no arguments shows help. `/friend <player>` is the add-friend fallback.

| Syntax | Behavior |
| --- | --- |
| `/friend accept <player>` | Accepts a request. |
| `/friend add <player>` | Sends a request. |
| `/friend best <player>` | Toggles best-friend status. |
| `/friend deny <player>` | Rejects a request. |
| `/friend help` | Displays help. |
| `/friend list [page\|best]` | Lists friends; `/fl [page\|best]` and `/friendlist` are also registered. |
| `/friend nickname <player> <nickname>` | Sets a nickname. |
| `/friend notifications` | Toggles friend notifications. |
| `/friend remove <player>` | Removes a friend. |
| `/friend removeall` | Removes all friends. |
| `/friend requests [page]` | Lists pending requests. |
| `/friend toggle` | Toggles requests. |

### Guild

Use `/guild` or `/g`; no arguments displays help. These commands require a responding guild service. Guild rank permissions and ownership restrictions are enforced by the existing handlers/service; DEFAULT access to `/g` does not grant those permissions.

| Syntax | Behavior |
| --- | --- |
| `/guild accept <player>` | Accepts an invitation. |
| `/guild chat <message>` | Sends guild chat; aliases `/gchat`, `/gc`. |
| `/guild create <name>` | Creates a guild. The current dispatcher uses the first argument as the name. |
| `/guild demote <player>` | Demotes a member. |
| `/guild description <text>` | Updates the description. |
| `/guild disband` | Disbands the guild. |
| `/guild discord [value]` | Reads/updates the Discord value through its handler. |
| `/guild finder <value>` | Updates the finder setting. |
| `/guild help` | Displays help. |
| `/guild invite <player>` | Invites a player. |
| `/guild join <name>` | Requests/attempts to join a guild. |
| `/guild kick <player> [reason]` | Removes a member. |
| `/guild leave` | Leaves the guild. |
| `/guild list` | Shows guild information; `info` and `/gl` use that same path. |
| `/guild log` | Shows guild history; `history` is equivalent. |
| `/guild member <player>` | Shows a member. |
| `/guild menu` | Displays instructions to open the profile's Guild menu; does not directly open a GUI. |
| `/guild motd [list\|preview\|clear]` | Views or clears the MOTD. |
| `/guild motd add <text>` | Appends a line. |
| `/guild motd set <line> <text>` | Replaces a numbered line. |
| `/guild mute [player\|everyone] [duration]` | Mutes a member/all; default target everyone, default duration one hour. |
| `/guild mypermissions` | Lists your guild-rank permissions. |
| `/guild notifications` | Toggles guild notifications. |
| `/guild officerchat <message>` | Officer chat; `oc`, `/ochat`, and `/oc` forward to this behavior. |
| `/guild online` | Shows online members; `members` is equivalent. |
| `/guild onlinemode` | Toggles the corresponding setting. |
| `/guild openjoin` | Toggles open joining. |
| `/guild party` | Forms a party with guild members. |
| `/guild permissions [rank permission]` | No argument lists permissions; supplied rank/permission toggles that permission through the service. |
| `/guild promote <player>` | Promotes a member. |
| `/guild quest` | Displays a no-active-quest response; this is not a complete quest system. |
| `/guild rank <create\|delete> <name>` | Creates/deletes a custom rank. |
| `/guild rename <value>` | Renames the guild. |
| `/guild setrank <player> <rank>` | Assigns a rank. |
| `/guild settings <setting> <value>` | Updates a named setting. |
| `/guild slow` | Toggles slow mode. |
| `/guild tag <value>` | Sets the tag. |
| `/guild tagcolor <value>` | Sets the tag color. |
| `/guild toggle` | Toggles the corresponding guild setting. |
| `/guild top` | Shows the top-member view. |
| `/guild transfer <player>` | Transfers ownership. |
| `/guild unmute [player\|everyone]` | Unmutes a member/all. |

### Regions and world editing

- `/regions add <region_id> <region_type> <x1> <y1> <z1> <x2> <y2> <z2>`
- `/regions remove <region_id>`
- `/regions wand <region_id> <region_type>`

Position arguments support the registered relative-block-position syntax. Region types come from the `RegionType` enum.

### Co-op, visitation, and chat limits

Co-op commands are distinct top-level commands in the player tables, not `/coop <subcommand>`. `/coopadd` and `/coopkick` run vote/membership flows. `/admincoopkick` is the STAFF override. `/coopbank` uses the shared co-op balance. `/islandprivacy` accepts `public`, `friends`, or `private`.

`/chat` and `/ch` currently select ALL, PARTY, or STAFF, plus the staff-view toggle. `/gc`, `/oc`, and `/cc` send a single message through their respective existing systems; they do not add persistent guild/officer/co-op modes. `/reply` and `/r` are not implemented by this command inventory.

### Merged staff commands

Related staff commands are merged into one command with subcommands (tab-completed). Every old command name is still registered as a hidden alias that forwards to the new form with the same arguments, so existing habits, scripts and the Admin GUI keep working; hidden aliases do not appear in `/help` or the Admin GUI. Permissions are unchanged: every merged command is STAFF and not console-declared.

#### /set

Sets a profile value for yourself or an online player. One subcommand per value; each keeps the arguments of the command it replaced (see Merged staff commands).

| Syntax | Replaces | Behaviour |
| --- | --- | --- |
| `/set coins <amount> [player]` | `/coins` | Sets your purse balance. |
| `/set bits <amount> [player]` | `/bits`, `/bit` | Sets your bits balance. |
| `/set xp <level> [player]` | `/xp`, `/experience` | Sets your experience levels. |
| `/set essence <type> <amount> [player]` | `/setessence` | Sets the profile's balance of one essence type (`WITHER`, `UNDEAD`, `CRIMSON`, `SPIDER`, `DRAGON`, `GOLD`, `DIAMOND`, `ICE`, `FOREST`, `FOSSIL`, `SUN_GECKO`, `SAFARI`), capped at 2,000,000,000. |
| `/set reputation <faction> <amount> [player]` | `/setreputation`, `/setrep` | Sets the profile's Crimson Isle reputation with `MAGE` or `BARBARIAN` (floored at 0). Grants the SkyBlock XP of any reputation title reached for the first time. |
| `/set petlevel <level>` | `/setpetlevel`, `/updatepetlevel` | Sets the held pet's level (1–200, capped at that pet's own max level, 200 for the dragons) using its own rarity; with no pet in hand it levels your active pet. |
| `/set petrarity <rarity>` | `/setpetrarity` | Sets the rarity of the pet in your hand; only rarities that pet exists at are accepted, and its XP is kept (the level is recalculated). |
| `/set bestiary <mob> <amount> [player]` | `/updatebestiary` | Updates the bestiary of a player. |
| `/set collection <item_type> <amount> [player]` | `/updatecollection` | Updates the collection of a player. |
| `/set stat <statistic> <value> [player]; /set stat clear <statistic\|all> [player]; /set stat list [player]` | `/setstat`, `/setstatistic` | Pins any statistic (Health, Strength, Speed, Magic Find, ...) to an exact final value from 0 to 99,999 for yourself or an online player, overriding gear, pets and buffs. Tab completes every statistic and shows its cap; capped statistics such as Speed still read as their cap. Overrides are saved to the profile and survive server switches until cleared. |

#### /force

Forces a chance-based or timed event the next time it could happen: an item drop, the next Griffin burrow creature, a Dark Auction or a Blazing Volcano eruption.

| Syntax | Replaces | Behaviour |
| --- | --- | --- |
| `/force drop <item> [player]` | `/forcedrop` | Guarantees the item from the next chance-based drop that could give it to you (or the given online player), once: mob and slayer loot, sea creatures, fishing catches and trophy fish, Griffin burrow treasure, block-break rare drops, pet perk drops, Tree Gifts, attribute shards (`SHARD_<NAME>`), Jerry Boxes, Spooky candy and more. Takes any item ID, plus `chimera` for the Minos Inquisitor's Chimera I book. Forced drops are held in memory on the current server and clear on restart. |
| `/force diana <creature>` | `/forcedianamob`, `/dianamob` | Makes your next Griffin Mob burrow spawn the chosen Mythological creature (e.g. `MINOS_INQUISITOR`, `KING_MINOS`) instead of a random roll. Applies once; if your Griffin's rarity can't normally dig that creature, its first variant is used. Burrows only appear during Diana's Mythological Ritual. |
| `/force darkauction` | `/forcedarkauction` | Force starts a Dark Auction. |
| `/force eruption [explosivity]` | `/forceeruption` | Starts a Blazing Volcano eruption right now, with the given explosivity (`CHILL` … `CATACLYSMIC`) or a random one. The eruption lasts about 20 minutes and spews Sulphur Ore and Matchos in waves throughout. Only works on the Crimson Isle server. |

#### /unlock

Unlocks every Fast Travel warp, or the Bestiary, for yourself or an online player.

| Syntax | Replaces | Behaviour |
| --- | --- | --- |
| `/unlock warps [player]` | `/unlockwarps` | Unlocks every Fast Travel warp (every island and every travel-scroll destination, including ones added later) on your profile or an online player's current profile. `/maxprofile` does the same. Destinations that aren't in SkyBlock yet are unlocked too but still refuse the warp. |
| `/unlock bestiary [player]` | `/unlockbestiary` | Unlocks the Bestiary menu for yourself or an optional online player without needing to speak to Bramass Beastslayer. |

#### /reset

Resets the Chicken Race progress on a profile, or your shop stocks back to maximum.

| Syntax | Replaces | Behaviour |
| --- | --- | --- |
| `/reset chickenrace [player]` | `/resetchickenrace` | Resets the Chicken Race cluckpoints claimed on the profile and the talked-to-Frosty flag, so Gulliver's rewards can be raced for again. Items already won are kept. |
| `/reset stocks` | `/resetmystocksnow` | Resets your shop stocks back to MAXIMUM. |

#### /max

Maxes out a held or worn Crown of Avarice (`coa`) or a whole profile (`profile`, identical to THE MAX STICK).

| Syntax | Replaces | Behaviour |
| --- | --- | --- |
| `/max coa` | — | Fills the Crown of Avarice in your hand (or on your head) to its 1,000,000,000 Coins consumed cap, granting the full Overindulgence bonus. |
| `/max profile [player]` | `/maxprofile` | Fully maxes out a profile, identical to THE MAX STICK. Unlocks every Fast Travel warp, as `/unlockwarps` does. Maxes every Slayer (Revenant, Tarantula, Sven, Voidgloom, Inferno Demonlord, and the catalogued Riftstalker): max level and XP with every level reward, stat and slayer-gated recipe it brings, a kill of every boss tier (unlocking each tier, each boss and the RNG Meter), and a full RNG Meter on the selected drop, or on the rarest drop when none is selected. Also puts every pet in the Pets menu at its highest rarity and level (an owned copy is upgraded in place, so rerunning adds no duplicates), stores a non-soulbound copy of every donatable Museum item (Special cap bypassed; the copies can be retrieved and traded), and fills the Accessory Bag with the best accessory of every upgrade line plus every standalone accessory, recombobulated where allowed (Rift-only and Bingo-only accessories are skipped; rerunning adds nothing already placed), then sorts the whole bag highest rarity first. At Maxwell it learns every stone power, unlocks all four tuning templates, selects the power with the most total stats at the profile's Accessory Power, and puts every Tuning Point into Strength if none are assigned (existing tuning is kept unless it no longer fits). Records a clear of every Kuudra tier and enough Infernal clears to max the Kuudra boss collection (paying its tier rewards and SkyBlock XP once); Kuudra Keys stay unbought. Unlocks the Secret Railroad Pass, so every Ticket Master lets you ride to the Glacite Tunnels without forging one. Donates every fossil at the Fossil Research Center (and so unlocks every fossil pet Forge recipe) and maxes the Fossil Essence shop's available perks. Fills the Sack of Sacks with the largest sack of every sack family (Silver over Bronze for Trophy Fishing) and every unlocked quiver slot with a stack of Flint Arrows. Each step runs on its own: if one fails, the rest still run, the error is logged, and the player sees a red "PARTLY MAXED!" message naming the failed steps instead of "MAXED!". |

#### /mission

Shows, starts, clears and finishes missions. With no subcommand it runs `info`.

| Syntax | Replaces | Behaviour |
| --- | --- | --- |
| `/mission info` | `/missiondata` | Collects mission information. |
| `/mission data` | `/displaymissiondata` | Displays the mission data of a player. |
| `/mission start <mission_id>` | `/startmission` | Starts a mission for testing purposes. |
| `/mission clear` | `/clearmission` | Clears your missions. |
| `/mission clearset <set> [player]` | `/clearmissionset`, `/completemissionset` | Completes the mission set for yourself or an optional online player target. |

#### /hand

Edits or inspects the item in your hand: enchant, reforge, recombobulate, Mithril Infuse, NBT and tracked-item information.

| Syntax | Replaces | Behaviour |
| --- | --- | --- |
| `/hand enchant <enchantment_type> <level>` | `/enchant`, `/ench` | Enchants the contents o the players hand. |
| `/hand reforge` | `/reforge`, `/reforgeitem` | Reforges the item in the players hand. |
| `/hand recomb` | `/recomb` | Toggles the Recombobulated flag on the held item, ignoring the Recombobulator 3000's rules (pets are refused). |
| `/hand mithrilinfuse` | `/mithrilinfuse` | Mithril Infuses the minion in the players hand. |
| `/hand nbt` | `/nbt`, `/iteminfo` | Returns the players held item info. |
| `/hand setnbt <key> <value>` | `/editnbt` | Modifies the NBT of the item in your hand. |
| `/hand tracked` | `/itemtrackedinformation` | Gets the information of a tracked item in the players hand. |

#### /debug

Diagnostics and test tools. Generic topics work on every server type; SkyBlock topics are added on SkyBlock servers.

| Syntax | Replaces | Behaviour |
| --- | --- | --- |
| `/debug formatname <name>` | `/formatname`, `/getnameformatted` | Gets the format of a players name from their username. |
| `/debug localedata [update]` | `/localedata` | Locale Data. Inspect locale data or execute the update branch. |
| `/debug npcpathfinding <npc> <x> <y> <z>` | `/npcpathfinding` | NPC Pathfinding test. Position is a relative vector; NPC must exist. |
| `/debug explosion <radius> [knockbackStrength]` | `/explodesimulation` | Simulates an explosion with falling blocks. |
| `/debug signgui <text>` | `/signgui`, `/signgraphicaluserinterface` | Opens a graphical user interface. |
| `/debug component <string>` | `/translatablecomponent` | Preview a TranslatableComponent. |
| `/debug configids` | `/displayconfigids` | Prints out the item IDs from the config into the console. |
| `/debug damageindicator <dmg> <crit>` | `/dmgindicdisplaydebugcmd` | Display damage indic. |
| `/debug lag [seconds]` | `/lagprofile` | Samples the server's busy threads for 1–30 seconds (default 5) and lists the SkyBlock code they spent the most time in. Run it while TPS is low to find what's causing lag. |
| `/debug mining` | `/mininginfo` | Debug command to display mining handler relationships. |
| `/debug recipes` | `/recipedebug` | Prints recipe debug. |
| `/debug tasks` | `/taskcount` | Gets the number of tasks currently running on the server. |
| `/debug headrotation <x>` | `/testheadrotation`, `/testheadrot` | Tests the head rotation of ArmorStands. |
| `/debug mutex <read\|counter\|stress\|reset> [count]` | `/testmutex` | Test the data mutex service against your own co-op container. |
| `/debug particles <radius> <intensity> <duration> <wireframe>` | `/testpe`, `/testparticleengine` | Tests the particle engine. `wireframe` is a required boolean. |
| `/debug anvilgui <text>` | `/anvilgui`, `/anvilgraphicaluserinterface` | Opens a graphical user interface. |
| `/debug music <song>` | `/playmusic`, `/playsong` | Plays a song. |

#### /world

World, structure, hologram, crystal, orb, minion-speed, dragon and velocity tools. Dragon and velocity are on every server type; the rest are added on SkyBlock servers.

| Syntax | Replaces | Behaviour |
| --- | --- | --- |
| `/world dragon <spawn\|follow\|stop\|remove> [speed]` | `/dragon` | Dragon management commands. The handler also registers a numeric speed argument. |
| `/world velocity <player> <x> <y> <z>` | `/setvelocity` | Sets the velocity of a player. |
| `/world structure <rotation>` | `/structure` | Places a test debug structure. |
| `/world tree <type> [minHeight maxHeight] [minWidth maxWidth]` | `/tree` | Generates a tree at your location. |
| `/world regrow <distance> <seed>` | `/regrowtrees` | Respawns trees in an area with a new seed. |
| `/world hologram <text>` | `/spawnholo`, `/spawnhologram` | Spawns a hologram with that text at your position. |
| `/world crystal <itemType>` | `/spawncrystal`, `/addcrystal` | Spawns in a crystal at the player's location. Item must provide the orb and skull components the handler uses. |
| `/world deleteorb` | `/deleteorb` | Deletes orbs at the player's location. |
| `/world minionspeed <NORMAL\|MULTIPLY_2\|MULTIPLY_5\|MULTIPLY_10>` | `/handleminionspeed` | Handle minion speed. Changes the server-side minion speed divisor. |

#### /hollows

Crystal Hollows capture and validation tools, Crystal Hollows server only. Every write goes to the yml under `configuration/skyblock/crystal_hollows/` (or `Minestom.regions.yml`) and takes effect immediately; structure and chest writes also reload the layout in memory with the same seed. Offsets are measured from the candidate origin nearest to you.

| Syntax | Behaviour |
| --- | --- |
| `/hollows layout` | Prints the layout seed and every placed structure with its origin and anchor count. |
| `/hollows layout regenerate [seed]` | Re-rolls the layout with a random or given seed, then restores the previous schematic pastes and pastes every structure's schematic (and empty patches over unused sites) for the new layout. |
| `/hollows tp <structure>`<br>`/hollows tp <structure>/<anchor>` | Teleports to a placed structure's origin or one of its anchors. |
| `/hollows candidate add <structure>` | Records your position and yaw as a new candidate origin of the structure. |
| `/hollows candidate list <structure>` | Lists the structure's candidate origins. |
| `/hollows candidate clear <structure>` | Removes every candidate of the structure. |
| `/hollows anchor set <structure> <anchorName>` | Stores your position and facing as the anchor, as an offset from the nearest candidate so it follows randomization. Needs at least one candidate. |
| `/hollows statue set <crystal>` | Stores the block you are looking at (or standing in) as the Nucleus statue of `JADE`, `AMBER`, `AMETHYST`, `SAPPHIRE` or `TOPAZ`. |
| `/hollows chest set <id>` | Stores the chest block you are looking at as the anchor of the configured structure chest `<id>` (ids come from `chests.yml`). |
| `/hollows region set <regionId>` | Two-step capture: run at one corner, then at the opposite corner. Rewrites the x/z of the Crystal Hollows region (`crystal_hollows` or `crystal_hollows_<zone>`), keeping its height range, and creates it if missing. |
| `/hollows spawn set` | Sets the lobby spawn (`world.yml`) to your position and facing. |
| `/hollows bounds set` | Two-step capture of the world's x/z bounds (`world.yml`). |
| `/hollows paste <structure> [here]` | Pastes the structure's `schematic` (from `configuration/skyblock/crystal_hollows/schematics/`) at its placed origin, or with `here` at your block position. The origin is the schematic's minimum corner. |
| `/hollows schematics` | Lists the `.schem` files with their size and unknown-block count, which structures use each as schematic or empty patch, and referenced files that are missing. |
| `/hollows validate` | Also reports missing or unreadable schematic files, unknown blocks, size mismatches between an empty patch and its site, and sites without an empty patch. Lists every structure without candidates, every anchor referenced by NPCs, bosses or chests that is unset, missing statues, placeholder or undefined regions and an unset spawn. |
| `/hollows debug` | Toggles particle markers for yourself: flame on the placed origin, sparks on other candidates, green on anchors. |
| `/hollows heat <0-100>` | Sets your Heat. |
| `/hollows spawnworm [stoneworm\|scatha]` | Spawns a worm at your feet. |

### Spawning

`/spawn <mob> [amount]` spawns any mob at your feet: every sea creature (as if fished up, with the angler and catch message), every registered SkyBlock mob (Hub, Farming, Deep Caverns, Dwarven Mines, Crimson Isle, Kuudra-free minibosses and so on) every Mythological creature and every Garden Pest (`PEST_<id>`; only in your Garden, on the plot you stand on, counting toward the 8-Pest cap, and the Vacuum can kill and loot them). The mob list is built from the game's own registries, so new mobs appear automatically. `amount` is 1-50 (default 1); lava sea creatures still respect the 10-per-player cap. Old names `/spawnseacreature`, `/spawnsc`, `/spawncrimsonmob` and `/spawncm` forward to it.

### SkyBlock XP

`/skyblockxp` (alias `/sbxp`) is STAFF-only and not console-declared. Every form takes an optional trailing online-player selector; omit it to target yourself. One SkyBlock Level is 100 SkyBlock XP.

| Subcommand | Purpose and notes |
| --- | --- |
| `/skyblockxp give <amount> [player]` | Adds XP to the admin bonus offset. Earned causes and per-source progress are left untouched. |
| `/skyblockxp take <amount> [player]` | Subtracts from the bonus offset. Total XP floors at 0, so it can never go negative. |
| `/skyblockxp set <amount> [player]` | Moves total XP to an exact figure by adjusting the bonus offset only. |
| `/skyblockxp setlevel <level> [player]` | Sets total XP to that level's threshold. Valid range is 0-700. |
| `/skyblockxp prestige [player]` | Jumps to the next prefix tier, which real Hypixel changes every 40 levels. |
| `/skyblockxp max [player]` | Grants level 578.74, completes every source, and unlocks every prefix emblem. Same path `/maxprofile` and THE MAX STICK use. |
| `/skyblockxp reset [player]` | Clears all causes, source progress, and the bonus offset back to zero XP. |
| `/skyblockxp info [player]` | Prints level, total XP, earned-vs-bonus split, and progress to the next level. |
| `/skyblockxp cause <cause> [player]` | Awards one named cause (a skill level, collection tier, museum reward and so on). Same as `/giveskyblockxp`. |
| `/skyblockxp source <xpSource> <amount> [player]` | Sets one XP source's progress, clamped to that source's real Hypixel maximum. Rejected for sources backed by their own causes — use `/skyblockxp cause` for those. |

`/skyblockxp cause` (old name `/giveskyblockxp`) remains the way to award a single named cause (a skill level, collection tier, museum reward and so on). `/skyblockxp` works in XP totals and whole sources.

## Examples

Replace player names and enum IDs with your intended targets. These are syntax examples, not commands executed during documentation work.

| Purpose | Example |
| --- | --- |
| Invite a friend to a party | `/p invite FriendName` |
| Send party chat | `/pc Ready to go?` |
| List the party | `/pl` |
| Send guild chat | `/gc Hello guild` |
| Send a private message | `/w FriendName Hello` |
| Return to public chat | `/ch a` |
| Open wardrobe | `/wd` |
| Open attributes | `/am` |
| Visit an island | `/visit FriendName` |
| Vote on a co-op request | `/coopvote accept` |
| Deposit shared coins | `/coopbank deposit 1000` |
| Set a target's purse as staff | `/set coins 1000 PlayerName` |
| Grant a configured item as staff | `/i <ITEM_ID> 16 PlayerName` |
| Find a player across servers as staff | `/find PlayerName` |
| Teleport to coordinates as staff | `/tp 0 100 0` |
| Change flying speed as staff | `/speed 1 FLY` |

## Other game modes

These definitions are in the repository but are loaded only for the relevant game/module. Configurator branches and preconditions vary by game mode. Syntax here records the declared usage with corrected registered command spellings; this appendix is not a live deployment or functional certification of those modes.

### type.bedwarsconfigurator

| Command / syntax | Aliases | Purpose | Minimum rank | Console declared |
| --- | --- | --- | --- | --- |
| `/autosetup <subcommand>` | `/setup`, `/mapsetup` | Automatic BedWars map configuration tool. | STAFF | No |
| `/choosemap <map>` | `/choose`, `/selectmap`, `/select` | Choose a BedWars map to configure. | STAFF | No |

### type.bedwarsgame

| Command / syntax | Aliases | Purpose | Minimum rank | Console declared |
| --- | --- | --- | --- | --- |
| `/bwadmin <breakbed\|respawnbed\|endgame\|info> [team]` | — | Admin commands for BedWars game management. | STAFF | No |
| `/shout <message>` | — | Shouts a message to all players on the game. | DEFAULT | No |

### type.bedwarslobby

| Command / syntax | Aliases | Purpose | Minimum rank | Console declared |
| --- | --- | --- | --- | --- |
| `/rejoin` | — | Rejoin an active BedWars game. | DEFAULT | No |

### type.murdermysteryconfigurator

| Command / syntax | Aliases | Purpose | Minimum rank | Console declared |
| --- | --- | --- | --- | --- |
| `/choosemap <map>` | `/choose`, `/selectmap`, `/select` | Choose a Murder Mystery map to configure. | STAFF | No |
| `/setup <subcommand>` | `/mapsetup`, `/autosetup` | Murder Mystery map configuration tool. | STAFF | No |

### type.murdermysterygame

| Command / syntax | Aliases | Purpose | Minimum rank | Console declared |
| --- | --- | --- | --- | --- |
| `/forcestart [seconds]` | — | Starts a murder mystery game immediately. | STAFF | No |

### type.ravengardgeneric

| Command / syntax | Aliases | Purpose | Minimum rank | Console declared |
| --- | --- | --- | --- | --- |
| `/animreview [start <clip>\|stop\|goto <tick>\|markstart\|mark <name>\|marks\|unmark <name>]` | — | Review captured animations and mark their segments. | STAFF | No |
| `/class <knight\|warrior\|hunter\|assassin>` | — | Sets your Ravengard class. | DEFAULT | No |
| `/crowns <amount>` | — | Sets your crown balance. | STAFF | No |
| `/dungeon <join\|list\|generate [seed] [rooms]>` | — | Ravengard dungeon administration. | STAFF | No |
| `/hub` | `/h` | Sends the player to the Ravengard hub. | DEFAULT | No |
| `/items [lookup]` | `/e` | Browse every implemented Ravengard item. Opens the item browser; optional search. | STAFF | No |
| `/spawn <mob>` | — | Spawns a Ravengard mob at your position. | STAFF | No |
| `/spawnsatchel` | — | Drops a loot satchel at your position. | STAFF | No |
| `/switch <world>` | — | Switches between captured polar worlds. | DEFAULT | No |

### type.replayviewer

| Command / syntax | Aliases | Purpose | Minimum rank | Console declared |
| --- | --- | --- | --- | --- |
| `/goto <time\|tick>` | — | Seeks to a specific time in the replay. | DEFAULT | No |
| `/share` | — | Share your current replay position. | DEFAULT | No |
| `/viewer <play\|pause\|speed\|skip\|goto\|restart\|leave>` | — | Controls replay playback. | DEFAULT | No |

### type.skywarsconfigurator

| Command / syntax | Aliases | Purpose | Minimum rank | Console declared |
| --- | --- | --- | --- | --- |
| `/choosemap <map>` | `/choose`, `/selectmap`, `/select` | Choose a SkyWars map to configure. | STAFF | No |
| `/swconfig <subcommand>` | — | Configure SkyWars maps. | STAFF | No |

### type.skywarsgame

| Command / syntax | Aliases | Purpose | Minimum rank | Console declared |
| --- | --- | --- | --- | --- |
| `/forcestart [seconds]` | — | Force starts the skywars game. | STAFF | No |
| `/skipevent` | — | Skips to the next game event (refill, dragon spawn). | STAFF | No |

### type.skywarslobby

| Command / syntax | Aliases | Purpose | Minimum rank | Console declared |
| --- | --- | --- | --- | --- |
| `/setswlevel <level>` | — | Set your SkyWars level for testing. | STAFF | No |
| `/swadmin <give\|set> <coins\|souls\|tokens> <amount>` | `/skywarsadmin` | Admin command for managing SkyWars currencies. | STAFF | No |

### Optional Spark profiler

`/spark [arguments...]` is registered when Spark is enabled by the loader. Its subcommands and permissions are delegated to the bundled Spark platform. Use its in-game help for the bundled version. It is not one of the annotated commands.

## Known registration and help discrepancies

This documentation does not change the handlers. The table distinguishes source defects from supported syntax:

| Finding | Consequence |
| --- | --- |
| Several help strings name labels that are not registered | Use the actual labels in the tables: examples include `/sl`. The merged staff commands now carry their own usage strings. |
| Help omits the argument of the NPC-option command | The core tables include the arguments found in registration. |
| `/coopcheck` help says `/coop` | `/coopcheck` and `/cooperativecheck` are the registered labels for checking invitations. |
| `allowsConsole=true` can coexist with player casts | Treat Console declared as metadata, not a tested compatibility guarantee. |
| `/adminme` has DEFAULT entry permission | It has an additional hardcoded developer allowlist; it is grouped with diagnostics, not ordinary player commands. |
| `/guild menu` and `/guild quest` do not implement full GUI/quest behavior | Their actual informational behavior is documented above. |
| Commands can depend on Redis-backed services | A service-offline response is not proof the alias is unregistered. |

## Admin GUI planning

Use the staff groups as the initial GUI categories: **Moderation and accounts**, **Items and progression**, **Travel, world, and events**, and **Development and diagnostics**. Preserve the existing STAFF and service-level permission checks; keep developer allowlist actions distinct. Provide a target selector only where the command supports a target. Use the registered labels and syntax rather than copying stale annotation help. Wipe, restore, balance setters, and inventory-clearing actions need their behavior made clear in the GUI.

The Admin GUI is implemented through `/admin` (alias `/adminmenu`) and the staff-only SkyBlock menu button. It lists registered commands available on the current server, organized into the four groups above, with paging, search, argument entry and an execution review. Commands run as the invoking player through the normal dispatcher. `/adminbypass` is available in Travel, world, and events.

## Maintenance and verification

Inventory source: every `@CommandParameters` definition under `*/src/main`, Velocity's explicit registrations, and the optional Spark wrapper. Each annotated definition appears once in a main/appendix table; aliases stay on that row. The player/staff split is based on declared rank with the explicit developer-allowlist exception above. Sorting uses the command spelling shown in the table.

Validation for this document checks inventory coverage, registered command spellings, sorted groups. No gameplay command, build, or server restart is needed for this documentation-only change. Update this reference whenever command syntax, aliases, or permissions change.
