package dev.daweapon.type.skyblockgeneric.command.groups.force;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.miningevent.MiningEventScheduler;
import dev.daweapon.type.skyblockgeneric.miningevent.MiningEventType;
import dev.daweapon.type.skyblockgeneric.miningevent.RandomMiningEvents;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

@CommandParameters(labels = "event", description = "Starts a Mining Event right now, spawns a random occurrence, or ends the running event",
        usage = "/force event <2x_powder|better_together|mithril_gourmand|goblin_raid|raffle|fallen_star|powder_ghast|golden_goblin|end>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceEventCommand extends HypixelCommand {
    private static List<String> names() {
        List<String> names = new ArrayList<>();
        for (MiningEventType type : MiningEventType.values()) names.add(type.name().toLowerCase().replace("double_powder", "2x_powder"));
        names.addAll(List.of("fallen_star", "powder_ghast", "golden_goblin", "end"));
        return names;
    }

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord eventArgument = ArgumentType.Word("kind");
        eventArgument.setSuggestionCallback((sender, context, suggestion) -> names().forEach(name -> suggestion.addEntry(new SuggestionEntry(name))));

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            run((SkyBlockPlayer) sender, context.get(eventArgument).toLowerCase().replace("2x_powder", "double_powder"));
        }, eventArgument);
    }

    private void run(SkyBlockPlayer player, String name) {
        if (MiningEventScheduler.zone() == null) {
            player.sendMessage("<c>Mining Events only run on the Dwarven Mines and Crystal Hollows servers.");
            return;
        }
        boolean done = switch (name) {
            case "end" -> {
                MiningEventScheduler.stopCurrent();
                RandomMiningEvents.endFallenStar();
                yield true;
            }
            case "fallen_star" -> RandomMiningEvents.spawnFallenStar();
            case "powder_ghast" -> RandomMiningEvents.spawnPowderGhast(player.getPosition());
            case "golden_goblin" -> RandomMiningEvents.spawnGoldenGoblin(player);
            default -> {
                MiningEventType type = null;
                for (MiningEventType candidate : MiningEventType.values()) {
                    if (candidate.name().toLowerCase().equals(name)) type = candidate;
                }
                yield type != null && MiningEventScheduler.force(type, true);
            }
        };
        player.sendMessage(done ? "<a>Done." : "<c>That event can't run here, or there was nothing to do.");
    }
}
