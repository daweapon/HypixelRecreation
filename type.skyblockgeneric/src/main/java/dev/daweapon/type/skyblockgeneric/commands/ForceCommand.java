package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandGroup;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.command.groups.force.ForceDarkAuctionCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.force.ForceDianaMobCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.force.ForceDropCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.force.ForceEruptionCommand;

import java.util.List;
import java.util.Map;

@CommandParameters(labels = "force",
        description = "Forces a drop, the next Diana mob, a Dark Auction or a Volcano eruption",
        usage = "/force <drop|diana|darkauction|eruption> ...",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceCommand extends CommandGroup {
    @Override
    protected List<HypixelCommand> subcommands() {
        return List.of(new ForceDropCommand(),
                new ForceDianaMobCommand(),
                new ForceDarkAuctionCommand(),
                new ForceEruptionCommand());
    }

    @Override
    protected Map<String, String> legacyLabels() {
        return Map.ofEntries(Map.entry("forcedrop", "force drop"),
                Map.entry("forcedianamob", "force diana"),
                Map.entry("dianamob", "force diana"),
                Map.entry("forcedarkauction", "force darkauction"),
                Map.entry("forceeruption", "force eruption"));
    }
}
