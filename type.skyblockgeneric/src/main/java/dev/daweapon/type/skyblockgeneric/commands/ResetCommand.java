package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandGroup;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.command.groups.reset.ResetChickenRaceCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.reset.ResetMyStocksCommand;

import java.util.List;
import java.util.Map;

@CommandParameters(labels = "reset",
        description = "Resets the Chicken Race or your shop stocks",
        usage = "/reset <chickenrace|stocks> ...",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ResetCommand extends CommandGroup {
    @Override
    protected List<HypixelCommand> subcommands() {
        return List.of(new ResetChickenRaceCommand(),
                new ResetMyStocksCommand());
    }

    @Override
    protected Map<String, String> legacyLabels() {
        return Map.ofEntries(Map.entry("resetchickenrace", "reset chickenrace"),
                Map.entry("resetmystocksnow", "reset stocks"));
    }
}
