package net.swofty.type.generic.command.help;

import net.minestom.server.command.builder.Command;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;

import java.util.*;

public final class HelpCatalog {
    public static final int PAGE_SIZE = 8;
    private static final Set<String> INTERNAL_COMMANDS = Set.of("forceadmin", "selectnpcoption", "sphinxanswer");
    private static final Map<String, String> USAGE_OVERRIDES = Map.ofEntries(
            Map.entry("tp", "/tp <x> <y> <z>"),
            Map.entry("speed", "/speed <speed> [FLY|WALK]"),
            Map.entry("npcpathfinding", "/npcpathfinding <npc> <x> <y> <z>"),
            Map.entry("cooperative", "/coop <player> [player ...]"),
            Map.entry("cooperativecheck", "/coopcheck"),
            Map.entry("clearstash", "/clearstash [confirm]"),
            Map.entry("testpe", "/testpe <radius> <intensity> <duration> <wireframe>"),
            Map.entry("testheadrot", "/testheadrotation <x>"),
            Map.entry("structure", "/structure <rotation>"),
            Map.entry("itemlist", "/itemlist [lookup]"),
            Map.entry("handleminionspeed", "/handleminionspeed <NORMAL|MULTIPLY_2|MULTIPLY_5|MULTIPLY_10>"),
            Map.entry("spawncrystal", "/addcrystal <itemType>"),
            Map.entry("regions", "/regions <add|remove|wand> ..."),
            Map.entry("selectnpcoption", "/selectnpcoption <npc> <option>"),
            Map.entry("localedata", "/localedata [update]"),
            Map.entry("fl", "/fl [page|best]"),
            Map.entry("chatmode", "/chat <a|all|p|party|s|staff|sv|staffview>"),
            Map.entry("slayer", "/slayer start <type> <tier> | /slayer status | /slayer cancel")
    );

    private HelpCatalog() {}

    public static List<Entry> entries(Collection<Command> commands, Rank rank, boolean authenticationEnabled) {
        Map<String, Entry> entries = new TreeMap<>();
        for (Command command : commands) {
            if (!(command instanceof HypixelCommand.MinestomCommand hypixelCommand)) continue;
            var parameters = hypixelCommand.getParameters();
            if (!rank.isEqualOrHigherThan(parameters.permission())) continue;
            if (!rank.isStaff() && INTERNAL_COMMANDS.contains(command.getName())) continue;
            List<String> labels = new ArrayList<>();
            labels.add(command.getName());
            if (command.getAliases() != null) labels.addAll(Arrays.asList(command.getAliases()));
            String usage = USAGE_OVERRIDES.getOrDefault(command.getName(), parameters.usage()).trim();
            String preferred = usage.startsWith("/") ? usage.substring(1).split("[\\s;]", 2)[0] : command.getName();
            if (!labels.contains(preferred)) {
                preferred = command.getName();
                usage = usage.replaceFirst("^/\\S+", "/" + preferred);
            }
            if (usage.isBlank()) usage = "/" + preferred;
            entries.put(preferred, new Entry(preferred, List.copyOf(labels), usage,
                    parameters.description().isBlank() ? "No description available." : parameters.description(),
                    parameters.permission()));
        }
        add(entries, "lobby", List.of("lobby"), "/lobby [game]", "Travel to a lobby.", Rank.DEFAULT);
        add(entries, "limbo", List.of("limbo"), "/limbo", "Travel to Limbo.", Rank.DEFAULT);
        add(entries, "protocolversion", List.of("protocolversion", "protocol"), "/protocolversion", "Show your client protocol version.", Rank.DEFAULT);
        add(entries, "serverstatus", List.of("serverstatus", "status"), "/serverstatus", "List registered game servers.", Rank.DEFAULT);
        if (rank.isStaff()) add(entries, "find", List.of("find"), "/find <player> [tp]", "Find a player across servers; optionally teleport.", Rank.STAFF);
        if (authenticationEnabled) {
            add(entries, "login", List.of("login"), "/login <password>", "Log in while unauthenticated.", Rank.DEFAULT);
            add(entries, "register", List.of("register"), "/register <password> <password>", "Register while unauthenticated.", Rank.DEFAULT);
        }
        return List.copyOf(entries.values());
    }

    private static void add(Map<String, Entry> entries, String name, List<String> labels, String usage, String description, Rank rank) {
        entries.putIfAbsent(name, new Entry(name, labels, usage, description, rank));
    }

    public static Optional<Entry> find(List<Entry> entries, String input) {
        String label = input.replaceFirst("^/", "").toLowerCase(Locale.ROOT);
        return entries.stream().filter(entry -> entry.labels().contains(label)).findFirst();
    }

    public static int pageCount(List<Entry> entries) {
        return Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    public static List<Entry> page(List<Entry> entries, int page) {
        if (page < 1 || page > pageCount(entries)) throw new IllegalArgumentException("Invalid help page");
        int start = (page - 1) * PAGE_SIZE;
        return entries.subList(start, Math.min(start + PAGE_SIZE, entries.size()));
    }

    public record Entry(String name, List<String> labels, String usage, String description, Rank permission) {}
}
