package net.swofty.type.generic.command.commands;

import net.swofty.type.generic.command.CommandGroup;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.command.groups.world.DragonCommand;
import net.swofty.type.generic.command.groups.world.SetVelocityCommand;

import java.util.List;
import java.util.Map;

@CommandParameters(labels = "world",
        description = "World, structure, hologram, crystal and minion tools",
        usage = "/world <dragon|velocity|structure|tree|regrow|hologram|crystal|deleteorb|minionspeed> ...",
        permission = Rank.STAFF,
        allowsConsole = false)
public class WorldCommand extends CommandGroup {
    @Override
    protected List<HypixelCommand> subcommands() {
        return List.of(new DragonCommand(),
                new SetVelocityCommand());
    }

    @Override
    protected Map<String, String> legacyLabels() {
        return Map.ofEntries(Map.entry("dragon", "world dragon"),
                Map.entry("setvelocity", "world velocity"));
    }
}
