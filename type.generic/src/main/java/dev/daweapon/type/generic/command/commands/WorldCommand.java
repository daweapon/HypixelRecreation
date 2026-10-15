package dev.daweapon.type.generic.command.commands;

import dev.daweapon.type.generic.command.CommandGroup;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.generic.command.groups.world.DragonCommand;
import dev.daweapon.type.generic.command.groups.world.SetVelocityCommand;

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
