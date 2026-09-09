package net.swofty.type.skyblockgeneric.gui.inventories.admin;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.Material;
import net.swofty.type.generic.command.help.HelpCatalog;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import java.util.*;

public final class AdminCatalog {
    public enum Category {
        MODERATION("Moderation and accounts", Material.IRON_BARS),
        ITEMS("Items and progression", Material.DIAMOND_SWORD),
        WORLD("Travel, world, and events", Material.COMPASS),
        DEVELOPMENT("Development and diagnostics", Material.COMMAND_BLOCK);
        public final String title;
        public final Material icon;
        Category(String title, Material icon) { this.title=title; this.icon=icon; }
    }
    public record Details(Category category, String usage, String description) {}
    public record Action(HelpCatalog.Entry command, Details details) {}
    private static final Map<String,Details> DETAILS=Map.ofEntries(
            Map.entry("adminbypass", new Details(Category.WORLD, "/adminbypass <event>|auction [uuid]|bin_grace [uuid]", "Skips a SkyBlock calendar or Auction House timer; omitted UUID targets your newest auction.")),
            Map.entry("adminme", new Details(Category.DEVELOPMENT, "/adminme", "Literally just gives me admin. Developer allowlist only, despite DEFAULT annotation. Resolves allowed UUIDs and grants STAFF; not a general player feature.")),
            Map.entry("ahview", new Details(Category.DEVELOPMENT, "/ahview <uuid>", "Views an auction.")),
            Map.entry("allowaccessforapiauthentication", new Details(Category.DEVELOPMENT, "/allowaccessforapiauthentication <code>", "Allows access for API authentication for the given code.")),
            Map.entry("anvilgui", new Details(Category.DEVELOPMENT, "/anvilgui <text>", "Opens a graphical user interface.")),
            Map.entry("centauri", new Details(Category.DEVELOPMENT, "/centauri", "Opens the Centauri menu.")),
            Map.entry("clearmission", new Details(Category.DEVELOPMENT, "/clearmission", "Clears your missions.  Help uses /missionempty, but it is not a registered label; use the spelling shown here.")),
            Map.entry("clearmissionset", new Details(Category.DEVELOPMENT, "/completemissionset <set> [player]", "Completes a mission set for yourself or an online player.")),
            Map.entry("displayconfigids", new Details(Category.DEVELOPMENT, "/displayconfigids", "Prints out the item IDs from the config into the console.")),
            Map.entry("displaymissiondata", new Details(Category.DEVELOPMENT, "/displaymissiondata", "Displays the mission data of a player.")),
            Map.entry("dmgindicdisplaydebugcmd", new Details(Category.DEVELOPMENT, "/dmgindicdisplaydebugcmd <dmg> <crit>", "Display damage indic.")),
            Map.entry("dragon", new Details(Category.DEVELOPMENT, "/dragon <spawn|follow|stop|remove> [speed]", "Dragon management commands. The handler also registers a numeric speed argument.")),
            Map.entry("editnbt", new Details(Category.DEVELOPMENT, "/editnbt <key> <value>", "Modifies the NBT of the item in your hand.  Help uses /changenbt, but it is not a registered label; use the spelling shown here.")),
            Map.entry("explodesimulation", new Details(Category.DEVELOPMENT, "/explodesimulation <radius> [knockbackStrength]", "Simulates an explosion with falling blocks.")),
            Map.entry("formatname", new Details(Category.DEVELOPMENT, "/formatname <name>", "Gets the format of a players name from their username.")),
            Map.entry("itemlist", new Details(Category.DEVELOPMENT, "/itemlist [lookup]", "Open the E menu. Opens the item browser; optional search.")),
            Map.entry("itemtrackedinformation", new Details(Category.DEVELOPMENT, "/itemtrackedinformation", "Gets the information of a tracked item in the players hand.")),
            Map.entry("localedata", new Details(Category.DEVELOPMENT, "/localedata [update]", "Locale Data. Inspect locale data or execute the update branch.")),
            Map.entry("mininginfo", new Details(Category.DEVELOPMENT, "/mininginfo", "Debug command to display mining handler relationships.  Help uses /miningdebug, but it is not a registered label; use the spelling shown here.")),
            Map.entry("mission", new Details(Category.DEVELOPMENT, "/mission", "Collects mission information.")),
            Map.entry("nbt", new Details(Category.DEVELOPMENT, "/nbt", "Returns the players held item info.")),
            Map.entry("npcpathfinding", new Details(Category.DEVELOPMENT, "/npcpathfinding <npc> <x> <y> <z>", "NPC Pathfinding test. Position is a relative vector; NPC must exist.")),
            Map.entry("playmusic", new Details(Category.DEVELOPMENT, "/playmusic <song>", "Plays a song.")),
            Map.entry("recipedebug", new Details(Category.DEVELOPMENT, "/recipedebug", "Prints recipe debug.")),
            Map.entry("resetmystocksnow", new Details(Category.DEVELOPMENT, "/resetmystocksnow", "Resets your shop stocks back to MAXIMUM.  Help uses /resetmystocks, but it is not a registered label; use the spelling shown here.")),
            Map.entry("sc", new Details(Category.DEVELOPMENT, "/sc <message>", "Sends a message to staff chat.")),
            Map.entry("signgui", new Details(Category.DEVELOPMENT, "/signgui <text>", "Opens a graphical user interface.")),
            Map.entry("spawnhologram", new Details(Category.DEVELOPMENT, "/spawnhologram <text>", "Rank command.")),
            Map.entry("startmission", new Details(Category.DEVELOPMENT, "/startmission <mission_id>", "Starts a mission for testing purposes.")),
            Map.entry("taskcount", new Details(Category.DEVELOPMENT, "/taskcount", "Gets the number of tasks currently running on the server.")),
            Map.entry("testheadrotation", new Details(Category.DEVELOPMENT, "/testheadrotation <x>", "Tests the head rotation of ArmorStands. Requires a numeric argument omitted from help.")),
            Map.entry("testpe", new Details(Category.DEVELOPMENT, "/testpe <radius> <intensity> <duration> <wireframe>", "Literally just gives me admin. wireframe is a required boolean despite its omission from help.")),
            Map.entry("throwexception", new Details(Category.DEVELOPMENT, "/throwexception", "Throws an exception for testing purposes.")),
            Map.entry("translatablecomponent", new Details(Category.DEVELOPMENT, "/translatablecomponent <string>", "Preview a TranslatableComponent.")),
            Map.entry("addtostash", new Details(Category.ITEMS, "/addtostash <item> [amount] [player]", "Force add an item to your stash.")),
            Map.entry("bits", new Details(Category.ITEMS, "/bits <amount> [player]", "Sets your bits balance.")),
            Map.entry("clear", new Details(Category.ITEMS, "/clear", "Rid your inventory of useless items.")),
            Map.entry("coins", new Details(Category.ITEMS, "/coins <amount> [player]", "Sets your purse balance.")),
            Map.entry("enchant", new Details(Category.ITEMS, "/enchant <enchantment_type> <level>", "Enchants the contents o the players hand.")),
            Map.entry("giveskyblockxp", new Details(Category.ITEMS, "/giveskyblockxp <cause> [player]", "Gives yourself skyblock xp.")),
            Map.entry("item", new Details(Category.ITEMS, "/item <item> [amount] [player]", "Gives an item to the player. Also accepts /item <item> <player> with the default amount. Target is an online player selector.")),
            Map.entry("maxprofile", new Details(Category.ITEMS, "/maxprofile [player]", "Fully maxes out a profile, identical to THE MAX STICK.")),
            Map.entry("mithrilinfuse", new Details(Category.ITEMS, "/mithrilinfuse", "Mithril Infuses the minion in the players hand.")),
            Map.entry("recomb", new Details(Category.ITEMS, "/recomb", "Recombobulates the item in the players hand.  Help uses /recombobulate, but it is not a registered label; use the spelling shown here.")),
            Map.entry("reforge", new Details(Category.ITEMS, "/reforge", "Reforges the item in the players hand.")),
            Map.entry("setpetlevel", new Details(Category.ITEMS, "/setpetlevel <level>", "Sets the level of the pet.")),
            Map.entry("setpetrarity", new Details(Category.ITEMS, "/setpetrarity <rarity>", "Sets the rarity of the pet in your hand.")),
            Map.entry("unlockbestiary", new Details(Category.ITEMS, "/unlockbestiary [player]", "Unlocks the Bestiary menu for yourself or an online player without needing to speak to Bramass Beastslayer.")),
            Map.entry("updatebestiary", new Details(Category.ITEMS, "/updatebestiary <mob> <amount> [player]", "Updates the bestiary of a player.  Help uses /setbestiary, but it is not a registered label; use the spelling shown here.")),
            Map.entry("updatecollection", new Details(Category.ITEMS, "/updatecollection <item_type> <amount> [player]", "Updates the collection of a player.  Help uses /setcollection, but it is not a registered label; use the spelling shown here.")),
            Map.entry("xp", new Details(Category.ITEMS, "/xp <level> [player]", "Sets your experience levels.")),
            Map.entry("admincoopkick", new Details(Category.MODERATION, "/admincoopkick <player>", "Force-removes a player from their co-op, bypassing the member vote.")),
            Map.entry("ban", new Details(Category.MODERATION, "/ban <player> [duration] <reason>", "Ban a player from the server.")),
            Map.entry("mute", new Details(Category.MODERATION, "/mute <player> [duration] <reason>", "Mute a player from the server.")),
            Map.entry("rank", new Details(Category.MODERATION, "/rank <player> <rank>", "Rank command.")),
            Map.entry("restore", new Details(Category.MODERATION, "/restore <player>", "Restores a player's profile if they were wiped within the last hour. Restores a recent wipe snapshot (one-hour window); see handler for online/offline handling.")),
            Map.entry("testmutex", new Details(Category.MODERATION, "/testmutex <read|counter|stress|reset> [count]", "Test the data mutex service against your own co-op container.")),
            Map.entry("unban", new Details(Category.MODERATION, "/unban <player>", "Unban a player from the server.")),
            Map.entry("unmute", new Details(Category.MODERATION, "/unmute <player>", "Unmute a player on the server.")),
            Map.entry("wipe", new Details(Category.MODERATION, "/wipe [player]", "Wipes a player's profile data. No target wipes yourself. Captures a snapshot and wipes account/profile ownership data; rank and identity fields are preserved. Target selector resolves locally.")),
            Map.entry("addcrystal", new Details(Category.WORLD, "/addcrystal <itemType>", "Spawns in a crystal at the player's location. Item must provide the orb and skull components the handler uses.")),
            Map.entry("build", new Details(Category.WORLD, "/build", "Toggles whether or not you are in build mode.")),
            Map.entry("deleteorb", new Details(Category.WORLD, "/deleteorb", "Deletes orbs at the player's location.  Help uses /removeorb, but it is not a registered label; use the spelling shown here.")),
            Map.entry("forcedarkauction", new Details(Category.WORLD, "/forcedarkauction", "Force starts a Dark Auction.")),
            Map.entry("gamemode", new Details(Category.WORLD, "/gamemode <gamemode>", "Sets a players gamemode.")),
            Map.entry("handleminionspeed", new Details(Category.WORLD, "/handleminionspeed <NORMAL|MULTIPLY_2|MULTIPLY_5|MULTIPLY_10>", "Handle minion speed. Changes the server-side minion speed divisor.")),
            Map.entry("regions", new Details(Category.WORLD, "/regions <add|remove|wand> ...", "Handles regions across the server. See region syntax below.")),
            Map.entry("regrowtrees", new Details(Category.WORLD, "/regrowtrees <distance> <seed>", "Respawns trees in an area with a new seed.  Help uses /respawntreesinarea, but it is not a registered label; use the spelling shown here.")),
            Map.entry("sendto", new Details(Category.WORLD, "/sendto <server_type>", "Transfers a player to another server.")),
            Map.entry("setvelocity", new Details(Category.WORLD, "/setvelocity <player> <x> <y> <z>", "Sets the velocity of a player. Help says /velocity, which is not a registered label.")),
            Map.entry("speed", new Details(Category.WORLD, "/speed <speed> [FLY|WALK]", "Sets a players speed. Speed range 0\u201310; defaults to FLY.")),
            Map.entry("structure", new Details(Category.WORLD, "/structure <rotation>", "Places a test debug structure. Requires an integer rotation omitted from help.")),
            Map.entry("tp", new Details(Category.WORLD, "/tp <x> <y> <z>", "teleports to a player. Coordinate syntax works in the registration. Player-target branch omits its argument from addSyntax; do not rely on /tp <player> until fixed.")),
            Map.entry("tpnpc", new Details(Category.WORLD, "/tpnpc <npc>", "Teleports you to the specified NPC.")),
            Map.entry("tree", new Details(Category.WORLD, "/tree <type> [minHeight maxHeight] [minWidth maxWidth]", "Generates a tree at your location.  Help uses /generatetree, but it is not a registered label; use the spelling shown here.")),
            Map.entry("worldtime", new Details(Category.WORLD, "/worldtime (tick)", "Skips the time in the current instance.")));
    private AdminCatalog() {}
    public static List<Action> actions(HypixelPlayer player) {
        return actions(MinecraftServer.getCommandManager().getCommands(),player.getRank());
    }
    static List<Action> actions(Collection<net.minestom.server.command.builder.Command> commands,Rank rank) {
        if (!rank.isStaff()) return List.of();
        var registered=new HashSet<String>();
        commands.forEach(command -> { registered.add(command.getName()); if(command.getAliases()!=null) registered.addAll(Arrays.asList(command.getAliases())); });
        return HelpCatalog.entries(commands,rank,false).stream()
                .filter(entry -> registered.contains(entry.name()) && !entry.name().equals("admin"))
                .filter(entry -> entry.permission().isEqualOrHigherThan(Rank.STAFF) || entry.labels().contains("forceadmin"))
                .map(entry -> new Action(entry,entry.labels().stream().map(DETAILS::get).filter(Objects::nonNull).findFirst().orElse(new Details(Category.DEVELOPMENT,entry.usage(),entry.description()))))
                .sorted(Comparator.comparing(action -> action.command().name())).toList();
    }
    public static String commandLine(String name, String arguments) {
        if (!name.matches("[a-zA-Z0-9_-]+")) throw new IllegalArgumentException("Invalid command");
        String args=arguments==null ? "" : arguments.trim();
        if(args.length()>500 || args.chars().anyMatch(c -> Character.isISOControl(c))) throw new IllegalArgumentException("Use one line, up to 500 characters");
        return name+(args.isEmpty() ? "" : " "+args);
    }
}
