package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandGroup;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.command.groups.force.ForceDarkAuctionCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.force.ForceDianaMobCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.force.ForceDropCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.force.ForceEruptionCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.force.ForceEventCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.force.ForceCallCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.force.ForceCenturyCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.force.ForceFireSaleCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.force.ForceMineshaftCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.force.ForceShrineCommand;

import java.util.List;
import java.util.Map;

@CommandParameters(labels = "force",
        description = "Forces a drop, the next Diana mob, a Dark Auction, a Volcano eruption, a Fire Sale, a Mining Event, the Blood God Shrine, a Glacite Mineshaft or the Raffle of the Century",
        usage = "/force <drop|call|diana|darkauction|eruption|firesale|event|shrine|mineshaft|century> ...",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceCommand extends CommandGroup {
    @Override
    protected List<HypixelCommand> subcommands() {
        return List.of(new ForceDropCommand(),
                new ForceCallCommand(),
                new ForceDianaMobCommand(),
                new ForceDarkAuctionCommand(),
                new ForceEruptionCommand(),
                new ForceFireSaleCommand(),
                new ForceEventCommand(),
                new ForceShrineCommand(),
                new ForceMineshaftCommand(),
                new ForceCenturyCommand());
    }

    @Override
    protected Map<String, String> legacyLabels() {
        return Map.ofEntries(Map.entry("forcedrop", "force drop"),
                Map.entry("forcedianamob", "force diana"),
                Map.entry("dianamob", "force diana"),
                Map.entry("forcedarkauction", "force darkauction"),
                Map.entry("forceeruption", "force eruption"),
                Map.entry("firesale", "force firesale"),
                Map.entry("forceevent", "force event"));
    }
}
