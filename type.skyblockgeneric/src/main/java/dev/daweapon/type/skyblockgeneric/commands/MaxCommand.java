package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandGroup;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.command.groups.max.MaxCoaCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.max.MaxCrystalsCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.max.MaxProfileCommand;

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
