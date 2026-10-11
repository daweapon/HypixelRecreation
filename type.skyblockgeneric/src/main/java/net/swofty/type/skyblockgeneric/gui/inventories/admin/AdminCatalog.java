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
            Map.entry("debug", new Details(Category.DEVELOPMENT, "/debug <configids|damageindicator|lag|mining|recipes|tasks|headrotation|mutex|particles|anvilgui|music|formatname|localedata|npcpathfinding|explosion|signgui|component> ...", "Diagnostics and test tools. Each topic keeps the arguments of the command it replaced.")),
            Map.entry("force", new Details(Category.WORLD, "/force <drop|diana|darkauction|eruption> ...", "Forces the next drop of an item, the next Diana mob, a Dark Auction or a Blazing Volcano eruption.")),
            Map.entry("hand", new Details(Category.ITEMS, "/hand <enchant|reforge|recomb|mithrilinfuse|nbt|setnbt|tracked> ...", "Edits or inspects the item in your hand.")),
            Map.entry("max", new Details(Category.ITEMS, "/max <coa|profile [player]>", "Fills a Crown of Avarice to its Coins consumed cap, or fully maxes a profile like THE MAX STICK.")),
            Map.entry("mission", new Details(Category.DEVELOPMENT, "/mission <info|data|start|clear|clearset> ...", "Shows, starts, clears and finishes missions.")),
            Map.entry("reset", new Details(Category.ITEMS, "/reset <chickenrace|stocks> [player]", "Resets the Chicken Race or your shop stocks.")),
            Map.entry("set", new Details(Category.ITEMS, "/set <coins|bits|xp|essence|reputation|petlevel|petrarity|bestiary|collection|stat> ...", "Sets a profile value. Each topic keeps the arguments of the command it replaced.")),
            Map.entry("spawn", new Details(Category.WORLD, "/spawn <mob> [amount]", "Spawns any mob, sea creature or Mythological creature at your feet.")),
            Map.entry("tp", new Details(Category.WORLD, "/tp <player> | <x> <y> <z> | npc <npc>", "Teleports you to a player, to coordinates or to an NPC.")),
            Map.entry("unlock", new Details(Category.ITEMS, "/unlock <warps|bestiary> [player]", "Unlocks Fast Travel warps or the Bestiary.")),
            Map.entry("world", new Details(Category.WORLD, "/world <structure|tree|regrow|hologram|crystal|deleteorb|minionspeed|dragon|velocity> ...", "World, structure, hologram, crystal and minion tools.")),
            Map.entry("adminbypass", new Details(Category.WORLD, "/adminbypass <event>|auction [uuid]|bin_grace [uuid]|auctionhouse|bazaar|bank|etable|skipforgetime [slot]", "Skips a SkyBlock calendar or Auction House timer, or opens a Cookie-gated menu without a Cookie Buff.")),
            Map.entry("adminme", new Details(Category.DEVELOPMENT, "/adminme", "Literally just gives me admin. Developer allowlist only, despite DEFAULT annotation. Resolves allowed UUIDs and grants STAFF; not a general player feature.")),
            Map.entry("ahview", new Details(Category.DEVELOPMENT, "/ahview <uuid>", "Views an auction.")),
            Map.entry("allowaccessforapiauthentication", new Details(Category.DEVELOPMENT, "/allowaccessforapiauthentication <code>", "Allows access for API authentication for the given code.")),
            Map.entry("centauri", new Details(Category.DEVELOPMENT, "/centauri", "Opens the Centauri menu.")),
            Map.entry("itemlist", new Details(Category.DEVELOPMENT, "/itemlist [lookup]", "Open the E menu. Opens the item browser; optional search.")),
            Map.entry("sc", new Details(Category.DEVELOPMENT, "/sc <message>", "Sends a message to staff chat.")),
            Map.entry("throwexception", new Details(Category.DEVELOPMENT, "/throwexception", "Throws an exception for testing purposes.")),
            Map.entry("addtostash", new Details(Category.ITEMS, "/addtostash <item> [amount] [player]", "Force add an item to your stash.")),
            Map.entry("clear", new Details(Category.ITEMS, "/clear", "Rid your inventory of useless items.")),
            Map.entry("item", new Details(Category.ITEMS, "/item <item> [amount] [player]", "Gives an item to the player. Also accepts /item <item> <player> with the default amount. Target is an online player selector.")),
            Map.entry("admincoopkick", new Details(Category.MODERATION, "/admincoopkick <player>", "Force-removes a player from their co-op, bypassing the member vote.")),
            Map.entry("ban", new Details(Category.MODERATION, "/ban <player> [duration] <reason>", "Ban a player from the server.")),
            Map.entry("mute", new Details(Category.MODERATION, "/mute <player> [duration] <reason>", "Mute a player from the server.")),
            Map.entry("rank", new Details(Category.MODERATION, "/rank <player> <rank>", "Rank command.")),
            Map.entry("restore", new Details(Category.MODERATION, "/restore <player>", "Restores a player's profile if they were wiped within the last hour. Restores a recent wipe snapshot (one-hour window); see handler for online/offline handling.")),
            Map.entry("unban", new Details(Category.MODERATION, "/unban <player>", "Unban a player from the server.")),
            Map.entry("unmute", new Details(Category.MODERATION, "/unmute <player>", "Unmute a player on the server.")),
            Map.entry("wipe", new Details(Category.MODERATION, "/wipe [player]", "Wipes a player's profile data. No target wipes yourself. Captures a snapshot and wipes account/profile ownership data; rank and identity fields are preserved. Target selector resolves locally.")),
            Map.entry("build", new Details(Category.WORLD, "/build", "Toggles whether or not you are in build mode.")),
            Map.entry("gamemode", new Details(Category.WORLD, "/gamemode <gamemode>", "Sets a players gamemode.")),
            Map.entry("regions", new Details(Category.WORLD, "/regions <add|remove|wand> ...", "Handles regions across the server. See region syntax below.")),
            Map.entry("sendto", new Details(Category.WORLD, "/sendto <server_type>", "Transfers a player to another server.")),
            Map.entry("speed", new Details(Category.WORLD, "/speed <speed> [FLY|WALK]", "Sets a players speed. Speed range 0\u201310; defaults to FLY.")),
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
