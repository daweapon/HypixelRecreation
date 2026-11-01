package dev.daweapon.type.skyblockgeneric.gui.inventories.settings;

import dev.daweapon.type.generic.data.datapoints.DatapointToggles.Toggles.ToggleType;
import dev.daweapon.type.skyblockgeneric.tab.TabSettings;
import net.minestom.server.item.Material;

import java.util.List;

public enum SettingsCategory {
    PERSONAL("Settings - Personal", 10, Material.PLAYER_HEAD, "Personal",
            "General settings related to your\nexperience.", "Click to view!", List.of(
            SettingEntry.stored(11, Material.DIAMOND_SWORD, "Double Tap to Drop",
                    "Double tap the drop button to drop\ncertain items.", ToggleType.DOUBLE_TAP_TO_DROP),
            SettingEntry.stored(13, Material.PLAYER_HEAD, "Profile Viewer",
                    "View player profiles on right-click.", ToggleType.PROFILE_VIEWER),
            SettingEntry.stored(15, Material.GOLD_INGOT, "Player Trading",
                    "Receive trade requests from other\nplayers.", ToggleType.PLAYER_TRADING),
            SettingEntry.stored(29, Material.CHEST, "Inventory Full Notifications",
                    "Get a notification when your\ninventory is full.", ToggleType.INVENTORY_FULL_NOTIFICATIONS),
            SettingEntry.stored(31, Material.ARROW, "Pickup Arrows with Full Quiver",
                    "Pickup arrows when your quiver is\nfull.", ToggleType.PICKUP_ARROWS_FULL_QUIVER),
            SettingEntry.stored(33, Material.WHEAT, "Confirm breaking ungrown Mutations",
                    "Open a confirm menu when attempting\nto break mutation crops that are not\nfully grown yet.",
                    ToggleType.CONFIRM_BREAKING_UNGROWN_MUTATIONS))),
    COMMS("Settings - Comms", 12, Material.OAK_SIGN, "Comms",
            "Tweak notifications and invites you\nget from other players.", "Click for settings!", List.of(
            SettingEntry.stored(11, Material.OAK_SIGN, "Death Messages",
                    "Show death messages when other\nplayers die.", ToggleType.DEATH_MESSAGES),
            SettingEntry.stored(14, Material.EMERALD, "Guesting Invites",
                    "Receive guesting invitations from\nother players.", ToggleType.GUESTING_INVITES),
            SettingEntry.stored(16, Material.DIAMOND, "Co-op Invites",
                    "Receive co-op invitations from other\nplayers.", ToggleType.COOP_INVITES),
            SettingEntry.stored(28, Material.GOLDEN_HORSE_ARMOR, "Bid Notifications",
                    "Receive a message when someone\nbids on one of your auctions.", ToggleType.BID_NOTIFICATIONS),
            SettingEntry.stored(29, Material.GOLD_INGOT, "Outbid Notifications",
                    "Receive a message when you get\noutbid on an auction.", ToggleType.OUTBID_NOTIFICATIONS),
            SettingEntry.stored(30, Material.HOPPER, "Fill Notifications",
                    "Receive a message when one of your\norder gets filled in the Bazaar.", ToggleType.FILL_NOTIFICATIONS),
            SettingEntry.stored(32, Material.PAPER, "Guesting Notifications",
                    "Receive messages about players\nguesting on your island.", ToggleType.GUESTING_NOTIFICATIONS),
            SettingEntry.stored(34, Material.PAPER, "Co-op Travel Notifications",
                    "When playing co-op, receive\nnotifications on all your partners'\ntravels.", ToggleType.COOP_TRAVEL_NOTIFICATIONS))),
    USER_INTERFACE("Settings - User Interface", 14, Material.GLASS, "User Interface",
            "Tweak how information is displayed to\nyou.", "Click for settings!", List.of(
            new SettingEntry(11, Material.FILLED_MAP, "Player List Info", "View extra information in the player\nlist.",
                    player -> TabSettings.of(player).playerListInfo(),
                    (player, value) -> TabSettings.of(player).setPlayerListInfo(value)),
            SettingEntry.stored(13, Material.MAP, "Skill Numerals",
                    "Use Roman Numerals for skill levels.\nFor example I, II, IV...", ToggleType.SKILL_NUMERALS),
            SettingEntry.stored(15, Material.REDSTONE, "Dynamic Slayer Sidebar",
                    "View the required number of kills\nbased on recent mob kills in the\nsidebar during a slayer quest.",
                    ToggleType.DYNAMIC_SLAYER_SIDEBAR),
            SettingEntry.stored(29, Material.OAK_SIGN, "SkyBlock Levels in Chat",
                    "View other players' SkyBlock Level\nand their selected emblem in their\nchat messages.", ToggleType.SKYBLOCK_LEVELS_IN_CHAT),
            SettingEntry.stored(31, Material.LIGHT_WEIGHTED_PRESSURE_PLATE, "Zones in Action Bar",
                    "Whether zone names should be\ndisplayed in the Action Bar upon\nentering.", ToggleType.ZONES_IN_ACTION_BAR),
            SettingEntry.stored(33, Material.GOLDEN_PICKAXE, "Advanced Supercraft",
                    "Show extra information on\nSupercraft for veteran crafters.", ToggleType.ADVANCED_SUPERCRAFT))),
    FISHING("Settings - Fishing Settings", 16, Material.FISHING_ROD, "Fishing Settings",
            "Tweak settings related to fishing.", "Click for settings!", List.of(
            SettingEntry.stored(10, Material.PAPER, "Sea Creature Chat",
                    "View the message from catching Sea\nCreatures.", ToggleType.SEA_CREATURE_CHAT),
            SettingEntry.stored(12, Material.COD, "Fishing Status Holograms",
                    "When enabled, holograms will give a\nvisual aid when fishing.", ToggleType.FISHING_STATUS_HOLOGRAMS),
            SettingEntry.stored(14, Material.CLOCK, "Fishing Timer",
                    "When this and the \"Fishing Overlay\"\nis enabled, the overlay will count\ndown the time above the bobber until\na fish can be caught.", ToggleType.FISHING_TIMER),
            SettingEntry.stored(16, Material.NOTE_BLOCK, "Fishing Sound",
                    "When enabled, a distinct sound will\nplay when a fish reaches the bobber.", ToggleType.FISHING_SOUND))),
    SOUNDS("Settings - Sounds", 28, Material.JUKEBOX, "Sounds",
            "Edit details about music and sounds\nthat you hear.", "Click for settings!", List.of(
            SettingEntry.stored(11, Material.JUKEBOX, "Play Music",
                    "Play music when you enter zones or\nother circumstances.", ToggleType.PLAY_MUSIC),
            SettingEntry.stored(13, Material.MUSIC_DISC_CAT, "Ability Cooldown Sounds",
                    "Hear the sound effect when an\nAbility is on cooldown.", ToggleType.ABILITY_COOLDOWN_SOUNDS),
            SettingEntry.stored(15, Material.NOTE_BLOCK, "Rare Drop Sounds",
                    "Play a jingle when you receive a\nrare drop from a monster.", ToggleType.RARE_DROP_SOUNDS),
            SettingEntry.stored(30, Material.OAK_SIGN, "Menu Sounds",
                    "Play sound effects when opening\nmenus.", ToggleType.MENU_SOUNDS),
            SettingEntry.stored(32, Material.RED_DYE, "Ferocity Sounds",
                    "Hear the sound effect when Ferocity\nprocs.", ToggleType.FEROCITY_SOUNDS))),
    CHAT_FEEDBACK("Settings - Chat Feedback", 30, Material.BOOK, "Chat Settings",
            "Tweak feedback messages you get\nfrom certain items.", "Click to view!", List.of(
            SettingEntry.stored(10, Material.BEACON, "Ability Chat",
                    "View the message from ability damage\ndealt (certain items only).", ToggleType.ABILITY_CHAT),
            SettingEntry.stored(12, Material.BLAZE_ROD, "Ability Cooldown Chat",
                    "View the message from ability\ncooldowns.", ToggleType.ABILITY_COOLDOWN_CHAT),
            SettingEntry.stored(14, Material.RED_STAINED_GLASS, "Pristine Chat",
                    "View the Pristine message when mining.", ToggleType.PRISTINE_CHAT),
            SettingEntry.stored(16, Material.STONE, "Compact Chat",
                    "View the Compact message when mining.", ToggleType.COMPACT_CHAT),
            SettingEntry.stored(29, Material.EMERALD, "Auto-Recombobulator Chat",
                    "Receive chat messages when your\nAuto-recombobulator triggers.", ToggleType.AUTO_RECOMBOBULATOR_CHAT),
            SettingEntry.stored(31, Material.BARREL, "Sack Notifications",
                    "When enabled, items added to and\nremoved from your Sacks will be\ndisplayed in chat.", ToggleType.SACK_NOTIFICATIONS),
            SettingEntry.stored(33, Material.BLAZE_POWDER, "Fire Sale Notifications",
                    "When enabled, you will occasionally\nbe notified of active Fire Sales\noutside of the Hub.", ToggleType.FIRE_SALE_NOTIFICATIONS))),
    API("Settings - API", 34, Material.COMPARATOR, "API Settings",
            "Edit settings regarding third-party\naccess to your SkyBlock profile.\n\nThe Hypixel API lets apps and\nwebsites display your player info\nand games progress.\n\n<b>API settings apply to your current\n<b>profile only!", "Click for more settings!", List.of(
            SettingEntry.stored(10, Material.DIAMOND_SWORD, "API: <e>Skills",
                    "Allow other players to view your\nskills on third-party applications.", ToggleType.API_SKILLS),
            SettingEntry.stored(12, Material.PAINTING, "API: <e>Collections",
                    "Allow other players to view your\ncollections progress on third-party\napplications.", ToggleType.API_COLLECTIONS),
            SettingEntry.stored(14, Material.CHEST, "API: <e>Inventory",
                    "Allow other players to view your\ninventory, backpacks and ender\nchest on third-party applications.", ToggleType.API_INVENTORY),
            SettingEntry.stored(16, Material.ENDER_CHEST, "API: <e>Personal Bank Vault",
                    "Allow other players to view your\npersonal bank vault on third-party\napplications.", ToggleType.API_PERSONAL_BANK_VAULT),
            SettingEntry.stored(31, Material.GOLD_BLOCK, "API: <e>Museum",
                    "Allow other players to view your\nmuseum on third-party applications.", ToggleType.API_MUSEUM)));

    private final String title;
    private final int hubSlot;
    private final Material hubMaterial;
    private final String hubName;
    private final String blurb;
    private final String clickLine;
    private final List<SettingEntry> entries;

    SettingsCategory(String title, int hubSlot, Material hubMaterial, String hubName, String blurb, String clickLine,
                     List<SettingEntry> entries) {
        this.title = title;
        this.hubSlot = hubSlot;
        this.hubMaterial = hubMaterial;
        this.hubName = hubName;
        this.blurb = blurb;
        this.clickLine = clickLine;
        this.entries = entries;
    }

    public String title() {
        return title;
    }

    public int hubSlot() {
        return hubSlot;
    }

    public Material hubMaterial() {
        return hubMaterial;
    }

    public List<SettingEntry> entries() {
        return entries;
    }

    public String hubText() {
        StringBuilder text = new StringBuilder("<a>").append(hubName).append("\n");
        for (String line : blurb.split("\n", -1)) {
            text.append(line.startsWith("<") || line.isEmpty() ? line : "<7>" + line).append("\n");
        }
        if (this != API) {
            text.append("\n<7>Includes:\n");
            for (SettingEntry entry : entries) text.append("<a>✔ <7>").append(entry.plainName()).append("\n");
        }
        return text.append("\n<e>").append(clickLine).toString();
    }
}
