# Commands reference

Sorted reference for the current HypixelRecreation source tree. Updated 2026-09-08.

This catalog covers **154 annotated game-server command definitions**, **7 proxy commands**, and the optional Spark command. It describes the remake as implemented, not a promise of complete real-Hypixel parity. Alias research is in [command-alias-audit.md](command-alias-audit.md).

## Contents

- [Reading the reference](#reading-the-reference)
- [Player commands](#player-commands)
- [Staff commands](#staff-commands)
- [Proxy commands](#proxy-commands)
- [Subcommand reference](#subcommand-reference)
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
| `/attributesmenu` | `/attributes`, `/attributemenu`, `/am` | Opens your Attributes menu. | DEFAULT | No | `type.skyblockgeneric` |
| `/auctionhouse` | `/ah` | Opens the Auction House remotely. Requires an active Booster Cookie. | DEFAULT | No | `type.skyblockgeneric` |
| `/bait` | — | Opens the Bait Guide, the same menu Angler Angus shows after he gives you Fish Bait. | DEFAULT | No | `type.skyblockgeneric` |
| `/bank` | — | Open your Personal Bank. | DEFAULT | No | `type.skyblockgeneric` |
| `/bazzar` | `/bazaar`, `/bz` | Opens the Bazaar remotely. Requires an active Booster Cookie and SkyBlock Level 7. | DEFAULT | No | `type.skyblockgeneric` |
| `/calendar` | — | Opens the SkyBlock Calendar. | DEFAULT | No | `type.skyblockgeneric` |
| `/chocolatefactory` | `/cf`, `/factory` | Opens the Chocolate Factory menu. | DEFAULT | No | `type.skyblockgeneric` |
| `/clearstash [confirm]` | — | Clear your stash. Confirmation is required before deleting the stash. | DEFAULT | No | `type.skyblockgeneric` |
| `/etable` | `/et`, `/enchantingtable`, `/enchantmenttable`, `/enchantingtablemenu`, `/enchantmenttablemenu` | Opens an Enchantment Table remotely. Requires an active Booster Cookie. | DEFAULT | No | `type.skyblockgeneric` |
| `/help [page\|command]` | — | Alphabetical chat command list, eight per page, with clickable navigation. Click a command or look up its alias for usage and aliases. Filters by minimum rank and loaded game-server commands; includes proxy travel commands. | DEFAULT | No | `type.generic` |
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
| `/is` | `/island` | Sends the player to their island. | DEFAULT | No | `type.skyblockgeneric` |
| `/parkour` | — | Parkour related commands. | DEFAULT | No | `type.lobby` |
| `/sl <number>` | — | Switch to a numbered lobby.  Help uses /swaplobby, but it is not a registered label; use the spelling shown here. | DEFAULT | No | `type.lobby` |
| `/warp <warp>` | `/teleportregion` | Warps to an unlocked island or travel-scroll destination, using Hypixel's warp names and aliases. Islands: `home` (`island`), `hub` (`village`), `dungeon_hub` (`dungeons`, `dhub`, `dh`), `barn` (`farming`), `park` (`trees`, `foraging`), `gold`, `deep`, `dwarven` (`dwarves`, `mines`, `dmines`, `deeper`), `spider` (`spiders`), `end`, `nether` (`isle`, `crimson`, `ci`), `jerry` (`winter`, `workshop`), `bayou` (`backwater`) and `moonglade` (`galatea`, `marsh`). Scroll warps: Hub `museum`, `da`, `castle`, `crypts` (`crypt`), `wizard` (`wiz`, `tower`, `wizard_tower`), `elizabeth`, `stonks`, `taylor`; `desert`, `trapper` (`trap`); `howl` (`howling_cave`, `wolf_cave`), `jungle`; `forge`, `basecamp` (`base`, `camp`, `glacite`, `tunnel`, `tunnels`, `gt`); `nest` (`top`, `mound`), `arachne` (`sanctuary`); `drag` (`dragons`), `void` (`sepulture`); `kuudra` (`skull`), `wasteland`, `dragontail`, `scarleton`, `smold` (`smoldering`, `smoldering_tomb`); `murkwater` (`murk`, `loch`, `murkwater_loch`). The Park unlocks with its Foraging travel scroll and the Crimson Isle with its scroll plus Combat XXII; other islands unlock on the first visit, and each scroll warp needs its own scroll. `crystals`, `nucleus`, `garden`, `rift`, `torrhus`, `springs`, `safari` and `lotus` are listed but answer that the destination isn't in SkyBlock yet. | DEFAULT | No | `type.skyblockgeneric` |

## Staff commands

### Development and diagnostics

| Command / syntax | Aliases | Purpose and notes | Minimum rank | Console declared | Module |
| --- | --- | --- | --- | --- | --- |
| `/admin` | `/adminmenu` | Opens the staff Admin GUI; also available in the SkyBlock menu. Browse categories, search commands, enter arguments and review before running. | STAFF | No | `type.skyblockgeneric` |
| `/adminme` | `/forceadmin` | Literally just gives me admin. Developer allowlist only, despite DEFAULT annotation. Resolves allowed UUIDs and grants STAFF; not a general player feature. | DEFAULT | No | `type.generic` |
| `/ahview <uuid>` | `/auctionview` | Views an auction. | DEFAULT | No | `type.skyblockgeneric` |
| `/allowaccessforapiauthentication <code>` | `/verifyapi` | Allows access for API authentication for the given code. | STAFF | No | `type.skyblockgeneric` |
| `/anvilgui <text>` | `/anvilgraphicaluserinterface` | Opens a graphical user interface. | STAFF | No | `type.skyblockgeneric` |
| `/centauri` | — | Opens the Centauri menu. | STAFF | No | `type.skyblockgeneric` |
| `/clearmission` | — | Clears your missions.  Help uses /missionempty, but it is not a registered label; use the spelling shown here. | STAFF | No | `type.skyblockgeneric` |
| `/clearmissionset <set> [player]` | `/completemissionset` | Completes the mission set for yourself or an optional online player target. | STAFF | No | `type.skyblockgeneric` |
| `/displayconfigids` | — | Prints out the item IDs from the config into the console. | STAFF | No | `type.skyblockgeneric` |
| `/displaymissiondata` | — | Displays the mission data of a player. | STAFF | No | `type.skyblockgeneric` |
| `/dmgindicdisplaydebugcmd <dmg> <crit>` | — | Display damage indic. | STAFF | No | `type.skyblockgeneric` |
| `/dragon <spawn\|follow\|stop\|remove> [speed]` | — | Dragon management commands. The handler also registers a numeric speed argument. | STAFF | No | `type.generic` |
| `/editnbt <key> <value>` | — | Modifies the NBT of the item in your hand.  Help uses /changenbt, but it is not a registered label; use the spelling shown here. | STAFF | No | `type.skyblockgeneric` |
| `/explodesimulation <radius> [knockbackStrength]` | — | Simulates an explosion with falling blocks. | STAFF | No | `type.generic` |
| `/formatname <name>` | `/getnameformatted` | Gets the format of a players name from their username. | STAFF | No | `type.generic` |
| `/itemlist [lookup]` | `/e` | Open the E menu. Opens the item browser; optional search. | STAFF | No | `type.skyblockgeneric` |
| `/itemtrackedinformation` | — | Gets the information of a tracked item in the players hand. | STAFF | No | `type.skyblockgeneric` |
| `/lagprofile [seconds]` | — | Samples the server's busy threads for 1–30 seconds (default 5) and lists the SkyBlock code they spent the most time in. Run it while TPS is low to find what's causing lag. | STAFF | No | `type.skyblockgeneric` |
| `/localedata [update]` | — | Locale Data. Inspect locale data or execute the update branch. | STAFF | No | `type.generic` |
| `/mininginfo` | — | Debug command to display mining handler relationships.  Help uses /miningdebug, but it is not a registered label; use the spelling shown here. | STAFF | No | `type.skyblockgeneric` |
| `/mission` | `/missiondata` | Collects mission information. | STAFF | No | `type.skyblockgeneric` |
| `/nbt` | `/iteminfo` | Returns the players held item info. | STAFF | No | `type.skyblockgeneric` |
| `/npcpathfinding <npc> <x> <y> <z>` | — | NPC Pathfinding test. Position is a relative vector; NPC must exist. | STAFF | No | `type.generic` |
| `/playmusic <song>` | `/playsong` | Plays a song. | STAFF | No | `type.skyblockgeneric` |
| `/recipedebug` | — | Prints recipe debug. | STAFF | No | `type.skyblockgeneric` |
| `/resetmystocksnow` | — | Resets your shop stocks back to MAXIMUM.  Help uses /resetmystocks, but it is not a registered label; use the spelling shown here. | STAFF | No | `type.skyblockgeneric` |
| `/sc <message>` | `/staffchat` | Sends a message to staff chat. | STAFF | No | `type.generic` |
| `/signgui <text>` | `/signgraphicaluserinterface` | Opens a graphical user interface. | STAFF | No | `type.generic` |
| `/spawnhologram <text>` | `/spawnholo` | Rank command. | STAFF | No | `type.skyblockgeneric` |
| `/startmission <mission_id>` | — | Starts a mission for testing purposes. | STAFF | No | `type.skyblockgeneric` |
| `/taskcount` | — | Gets the number of tasks currently running on the server. | STAFF | No | `type.skyblockgeneric` |
| `/testheadrotation <x>` | `/testheadrot` | Tests the head rotation of ArmorStands. Requires a numeric argument omitted from help. | STAFF | No | `type.skyblockgeneric` |
| `/testpe <radius> <intensity> <duration> <wireframe>` | `/testparticleengine` | Literally just gives me admin. wireframe is a required boolean despite its omission from help. | STAFF | No | `type.skyblockgeneric` |
| `/throwexception` | — | Throws an exception for testing purposes. | STAFF | Yes | `type.generic` |
| `/translatablecomponent <string>` | — | Preview a TranslatableComponent. | STAFF | No | `type.generic` |

### Items and progression

| Command / syntax | Aliases | Purpose and notes | Minimum rank | Console declared | Module |
| --- | --- | --- | --- | --- | --- |
| `/addtostash <item> [amount] [player]` | — | Force add an item to your stash. | STAFF | No | `type.skyblockgeneric` |
| `/bits <amount> [player]` | `/bit` | Sets your bits balance. | STAFF | No | `type.skyblockgeneric` |
| `/clear` | `/cl` | Rid your inventory of useless items. | STAFF | No | `type.skyblockgeneric` |
| `/coins <amount> [player]` | — | Sets your purse balance. | STAFF | No | `type.skyblockgeneric` |
| `/enchant <enchantment_type> <level>` | `/ench` | Enchants the contents o the players hand. | STAFF | No | `type.skyblockgeneric` |
| `/factionquests <list\|reroll>; /factionquests complete <slot>` | `/fquests` | Lists today's Crimson Isle Faction Quests for your current faction, rolls a fresh Town Board for today (`reroll`), or marks slot 1–5 complete so it can be claimed at the Town Board without doing the task (`complete`). Needs a faction. | STAFF | No | `type.skyblockgeneric` |
| `/giveskyblockxp <cause> [player]` | `/awardskyblockxp` | Gives yourself skyblock xp. | STAFF | No | `type.skyblockgeneric` |
| `/item <item> [amount] [player]` | `/i`, `/sbitem` | Gives an item to the player. `<item>` is an item ID (case-insensitive, suggested as you type) or the item's display name written as one word, e.g. `KINDLEBANE_DAGGER` gives the Kindlebane Dagger (ID `BURSTFIRE_DAGGER`). Also accepts /item <item> <player> with the default amount. Target is an online player selector. | STAFF | No | `type.skyblockgeneric` |
| `/max coa` | — | Fills the Crown of Avarice in your hand (or on your head) to its 1,000,000,000 Coins consumed cap, granting the full Overindulgence bonus. | STAFF | No | `type.skyblockgeneric` |
| `/maxprofile [player]` | — | Fully maxes out a profile, identical to THE MAX STICK. Unlocks every Fast Travel warp, as `/unlockwarps` does. Maxes every Slayer (Revenant, Tarantula, Sven, Voidgloom, Inferno Demonlord, and the catalogued Riftstalker): max level and XP with every level reward, stat and slayer-gated recipe it brings, a kill of every boss tier (unlocking each tier, each boss and the RNG Meter), and a full RNG Meter on the selected drop, or on the rarest drop when none is selected. Also puts every pet in the Pets menu at its highest rarity and level (an owned copy is upgraded in place, so rerunning adds no duplicates), stores a non-soulbound copy of every donatable Museum item (Special cap bypassed; the copies can be retrieved and traded), and fills the Accessory Bag with the best accessory of every upgrade line plus every standalone accessory, recombobulated where allowed (Rift-only and Bingo-only accessories are skipped; rerunning adds nothing already placed), then sorts the whole bag highest rarity first. At Maxwell it learns every stone power, unlocks all four tuning templates, selects the power with the most total stats at the profile's Accessory Power, and puts every Tuning Point into Strength if none are assigned (existing tuning is kept unless it no longer fits). Each step runs on its own: if one fails, the rest still run, the error is logged, and the player sees a red "PARTLY MAXED!" message naming the failed steps instead of "MAXED!". | STAFF | No | `type.skyblockgeneric` |
| `/mithrilinfuse` | — | Mithril Infuses the minion in the players hand. | STAFF | No | `type.skyblockgeneric` |
| `/recomb` | — | Toggles the Recombobulated flag on the held item, ignoring the Recombobulator 3000's rules (pets are refused).  Help uses /recombobulate, but it is not a registered label; use the spelling shown here. | STAFF | No | `type.skyblockgeneric` |
| `/reforge` | `/reforgeitem` | Reforges the item in the players hand. | STAFF | No | `type.skyblockgeneric` |
| `/resetchickenrace [player]` | — | Resets the Chicken Race cluckpoints claimed on the profile and the talked-to-Frosty flag, so Gulliver's rewards can be raced for again. Items already won are kept. | STAFF | No | `type.skyblockgeneric` |
| `/setessence <type> <amount> [player]` | — | Sets the profile's balance of one essence type (`WITHER`, `UNDEAD`, `CRIMSON`, `SPIDER`, `DRAGON`, `GOLD`, `DIAMOND`, `ICE`, `FOREST`, `FOSSIL`, `SUN_GECKO`, `SAFARI`), capped at 2,000,000,000. | STAFF | No | `type.skyblockgeneric` |
| `/setpetlevel <level>` | `/updatepetlevel` | Sets the held pet's level (1–200, capped at that pet's own max level, 200 for the dragons) using its own rarity; with no pet in hand it levels your active pet. | STAFF | No | `type.skyblockgeneric` |
| `/setpetrarity <rarity>` | — | Sets the rarity of the pet in your hand; only rarities that pet exists at are accepted, and its XP is kept (the level is recalculated). | STAFF | No | `type.skyblockgeneric` |
| `/setreputation <faction> <amount> [player]` | `/setrep` | Sets the profile's Crimson Isle reputation with `MAGE` or `BARBARIAN` (floored at 0). Grants the SkyBlock XP of any reputation title reached for the first time. | STAFF | No | `type.skyblockgeneric` |
| `/setstat <statistic> <value> [player]; /setstat clear <statistic\|all> [player]; /setstat list [player]` | `/setstatistic` | Pins any statistic (Health, Strength, Speed, Magic Find, ...) to an exact final value from 0 to 99,999 for yourself or an online player, overriding gear, pets and buffs. Tab completes every statistic and shows its cap; capped statistics such as Speed still read as their cap. Overrides are saved to the profile and survive server switches until cleared. | STAFF | No | `type.skyblockgeneric` |
| `/skyblockxp <action> [amount] [player]` | `/sbxp` | Manages SkyBlock XP and SkyBlock Level. See the SkyBlock XP subcommand reference below. | STAFF | No | `type.skyblockgeneric` |
| `/unlockbestiary [player]` | — | Unlocks the Bestiary menu for yourself or an optional online player without needing to speak to Bramass Beastslayer. | STAFF | No | `type.skyblockgeneric` |
| `/updatebestiary <mob> <amount> [player]` | — | Updates the bestiary of a player.  Help uses /setbestiary, but it is not a registered label; use the spelling shown here. | STAFF | No | `type.skyblockgeneric` |
| `/updatecollection <item_type> <amount> [player]` | — | Updates the collection of a player.  Help uses /setcollection, but it is not a registered label; use the spelling shown here. | STAFF | No | `type.skyblockgeneric` |
| `/xp <level> [player]` | `/experience` | Sets your experience levels. | STAFF | No | `type.skyblockgeneric` |

### Moderation and accounts

| Command / syntax | Aliases | Purpose and notes | Minimum rank | Console declared | Module |
| --- | --- | --- | --- | --- | --- |
| `/admincoopkick <player>` | — | Force-removes a player from their co-op, bypassing the member vote. | STAFF | No | `type.skyblockgeneric` |
| `/ban <player> [duration] <reason>` | `/tempban`, `/banip`, `/tempbanip` | Ban a player from the server. | STAFF | No | `type.generic` |
| `/mute <player> [duration] <reason>` | `/tempmute` | Mute a player from the server. | STAFF | No | `type.generic` |
| `/rank <player> <rank>` | `/setrank` | Rank command. | STAFF | Yes | `type.generic` |
| `/restore <player>` | — | Restores a player's profile if they were wiped within the last hour. Restores a recent wipe snapshot (one-hour window); see handler for online/offline handling. | STAFF | No | `type.generic` |
| `/testmutex <read\|counter\|stress\|reset> [count]` | — | Test the data mutex service against your own co-op container. | STAFF | No | `type.skyblockgeneric` |
| `/unban <player>` | `/pardon`, `/unbanip`, `/pardonip` | Unban a player from the server. | STAFF | No | `type.generic` |
| `/unmute <player>` | — | Unmute a player on the server. | STAFF | No | `type.generic` |
| `/wipe [player]` | — | Wipes a player's profile data. No target wipes yourself. Captures a snapshot and wipes account/profile ownership data; rank and identity fields are preserved. Target selector resolves locally. | STAFF | No | `type.generic` |

### Travel, world, and events

| Command / syntax | Aliases | Purpose and notes | Minimum rank | Console declared | Module |
| --- | --- | --- | --- | --- | --- |
| `/addcrystal <itemType>` | `/spawncrystal` | Spawns in a crystal at the player's location. Item must provide the orb and skull components the handler uses. | STAFF | No | `type.skyblockgeneric` |
| `/adminbypass <event>`<br>`/adminbypass auction [uuid]`<br>`/adminbypass bin_grace [uuid]`<br>`/adminbypass <auctionhouse\|bazaar\|bank\|etable>`<br>`/adminbypass skiptospecialmayor`<br>`/adminbypass forcemayor <mayor>`<br>`/adminbypass resetyear` | `/jumptonextevent`, `/skiptoevent` | Skips the selected SkyBlock calendar timer, ends an active Auction House listing, removes a BIN listing's 20-second purchase grace period, opens a Cookie-gated menu without an active Booster Cookie, force-elects the next special mayor (Scorpius/Derpy/Jerry, in rotation) for testing without a real election, force-elects a chosen mayor (by ID, e.g. `DIANA`) with all of their perks for testing perks such as the Mythological Ritual, or resets this server's local SkyBlock calendar back to Year 1, Early Spring, Day 1. Omitting the UUID targets your newest owned auction; the Cookie-menu bypasses require an in-game player. Event IDs are suggested by the command. The calendar is per-server (not synced), so `resetyear` only affects the server it's run on. | STAFF | Yes | `type.skyblockgeneric` |
| `/build` | `/buildmode` | Toggles whether or not you are in build mode. | STAFF | No | `type.skyblockgeneric` |
| `/deleteorb` | — | Deletes orbs at the player's location.  Help uses /removeorb, but it is not a registered label; use the spelling shown here. | STAFF | No | `type.skyblockgeneric` |
| `/forcedarkauction` | — | Force starts a Dark Auction. | STAFF | No | `type.skyblockgeneric` |
| `/forcedianamob <creature>` | `/dianamob` | Makes your next Griffin Mob burrow spawn the chosen Mythological creature (e.g. `MINOS_INQUISITOR`, `KING_MINOS`) instead of a random roll. Applies once; if your Griffin's rarity can't normally dig that creature, its first variant is used. Burrows only appear during Diana's Mythological Ritual. | STAFF | No | `type.skyblockgeneric` |
| `/forcedrop <item> [player]` | — | Guarantees the item from the next chance-based drop that could give it to you (or the given online player), once: mob and slayer loot, sea creatures, fishing catches and trophy fish, Griffin burrow treasure, block-break rare drops, pet perk drops, Tree Gifts, attribute shards (`SHARD_<NAME>`), Jerry Boxes, Spooky candy and more. Takes any item ID, plus `chimera` for the Minos Inquisitor's Chimera I book. Forced drops are held in memory on the current server and clear on restart. | STAFF | No | `type.skyblockgeneric` |
| `/forceeruption [explosivity]` | — | Starts a Blazing Volcano eruption right now, with the given explosivity (`CHILL` … `CATACLYSMIC`) or a random one. The eruption lasts about 20 minutes and spews Sulphur Ore and Matchos in waves throughout. Only works on the Crimson Isle server. | STAFF | No | `type.skyblockgeneric` |
| `/gamemode <gamemode>` | `/gm` | Sets a players gamemode. | STAFF | No | `type.generic` |
| `/handleminionspeed <NORMAL\|MULTIPLY_2\|MULTIPLY_5\|MULTIPLY_10>` | — | Handle minion speed. Changes the server-side minion speed divisor. | STAFF | No | `type.skyblockgeneric` |
| `/regions <add\|remove\|wand> ...` | — | Handles regions across the server. See region syntax below. | STAFF | No | `type.skyblockgeneric` |
| `/regrowtrees <distance> <seed>` | — | Respawns trees in an area with a new seed.  Help uses /respawntreesinarea, but it is not a registered label; use the spelling shown here. | STAFF | No | `type.skyblockgeneric` |
| `/sendto <server_type>` | `/transfer` | Transfers a player to another server. | STAFF | No | `type.generic` |
| `/setvelocity <player> <x> <y> <z>` | — | Sets the velocity of a player. Help says /velocity, which is not a registered label. | STAFF | No | `type.generic` |
| `/spawncrimsonmob <mob>` | `/spawncm` | Spawns a Crimson Isle mob, miniboss or Magma Boss at your feet by its mob ID (e.g. `BLAZE`, `MATCHO`, `VANQUISHER`, `BLADESOUL`, `MAGMA_BOSS`), suggested by the command. Minibosses spawned this way are extra copies outside their respawn timers. Summons that need a parent (Summoned Skulls, Magma Glares, Blazing Souls) vanish on their own. | STAFF | No | `type.skyblockgeneric` |
| `/spawnseacreature <creature> [amount]` | `/spawnsc` | Spawns a sea creature at your feet as if you had fished it up: you are its angler, its catch message plays, and the Reindrake's server-wide announcement fires. Takes any of the game's sea creatures, water, Festival sharks, Jerry's Workshop and all 16 Crimson Isle lava creatures (e.g. `FROZEN_STEVE`, `REINDRAKE`, `GREAT_WHITE_SHARK`, `THUNDER`, `LORD_JAWBUS`, `PLHLEGBLAST`, `RAGNAROK`, `FIERY_SCUTTLER`), suggested with their names; case, hyphens and underscores are ignored (`lordjawbus` works). `amount` (1-10, default 1) spawns several at once; lava creatures still respect the 10-per-player cap. A Reindrake brings Gregory to Jerry Pond while it lives. | STAFF | No | `type.skyblockgeneric` |
| `/speed <speed> [FLY\|WALK]` | `/flyspeed` | Sets a players speed. Speed range 0–10; defaults to FLY. | STAFF | No | `type.generic` |
| `/structure <rotation>` | — | Places a test debug structure. Requires an integer rotation omitted from help. | STAFF | No | `type.skyblockgeneric` |
| `/tp <x> <y> <z>` | `/teleport` | teleports to a player. Coordinate syntax works in the registration. Player-target branch omits its argument from addSyntax; do not rely on /tp <player> until fixed. | STAFF | No | `type.generic` |
| `/tpnpc <npc>` | `/teleportnpc` | Teleports you to the specified NPC. | STAFF | No | `type.generic` |
| `/tree <type> [minHeight maxHeight] [minWidth maxWidth]` | — | Generates a tree at your location.  Help uses /generatetree, but it is not a registered label; use the spelling shown here. | STAFF | No | `type.skyblockgeneric` |
| `/unlockwarps [player]` | — | Unlocks every Fast Travel warp (every island and every travel-scroll destination, including ones added later) on your profile or an online player's current profile. `/maxprofile` does the same. Destinations that aren't in SkyBlock yet are unlocked too but still refuse the warp. | STAFF | No | `type.skyblockgeneric` |
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
| `/skyblockxp source <xpSource> <amount> [player]` | Sets one XP source's progress, clamped to that source's real Hypixel maximum. Rejected for sources backed by their own causes — use `/giveskyblockxp` for those. |

`/giveskyblockxp` remains the way to award a single named cause (a skill level, collection tier, museum reward and so on). `/skyblockxp` works in XP totals and whole sources.

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
| Set a target's purse as staff | `/coins 1000 PlayerName` |
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
| `/setup <subcommand>` | `/mapsetup`, `/autosetup` | Murder Mystery map configuration tool.  Help uses /mmsetup, but it is not a registered label; use the spelling shown here. | STAFF | No |

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
| `/items [lookup]` | `/e` | Browse every implemented Ravengard item. Opens the item browser; optional search. Help uses /itemlist, but it is not a registered label; use the spelling shown here. | STAFF | No |
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
| `/setswlevel <level>` | — | Set your SkyWars level for testing.  Help uses /setskywarlevel, but it is not a registered label; use the spelling shown here. | STAFF | No |
| `/swadmin <give\|set> <coins\|souls\|tokens> <amount>` | `/skywarsadmin` | Admin command for managing SkyWars currencies. | STAFF | No |

### Optional Spark profiler

`/spark [arguments...]` is registered when Spark is enabled by the loader. Its subcommands and permissions are delegated to the bundled Spark platform. Use its in-game help for the bundled version. It is not one of the 152 annotated commands.

## Known registration and help discrepancies

This documentation does not change the handlers. The table distinguishes source defects from supported syntax:

| Finding | Consequence |
| --- | --- |
| `/tp` player branch fails to supply `entityArgument` to `addSyntax` | Player-target teleport is not correctly registered; the coordinate form is documented instead. |
| Several help strings name labels that are not registered | Use the actual labels in the tables: examples include `/mininginfo`, `/editnbt`, `/clearmissionset`, `/clearmission`, `/deleteorb`, `/recomb`, `/setvelocity`, `/regrowtrees`, and `/sl`. |
| Help omits arguments for crystal, minion-speed, structure, particle, head-rotation, and NPC-option commands | The core tables include the arguments found in registration. |
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
