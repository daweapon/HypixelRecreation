package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandGroup;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.command.groups.max.MaxCoaCommand;
import net.swofty.type.skyblockgeneric.command.groups.max.MaxCrystalsCommand;
import net.swofty.type.skyblockgeneric.command.groups.max.MaxProfileCommand;

import java.util.List;
import java.util.Map;

@CommandParameters(labels = "max",
        description = "Maxes out a Crown of Avarice, gives every Nucleus crystal or maxes a whole profile",
        usage = "/max <coa|crystals|profile> ...",
        permission = Rank.STAFF,
        allowsConsole = false)
public class MaxCommand extends CommandGroup {
    @Override
    protected List<HypixelCommand> subcommands() {
        return List.of(new MaxCoaCommand(),
                new MaxCrystalsCommand(),
                new MaxProfileCommand());
    }

    @Override
    protected Map<String, String> legacyLabels() {
        return Map.ofEntries(Map.entry("maxprofile", "max profile"));
    }
}
