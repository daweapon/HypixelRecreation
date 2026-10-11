package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandGroup;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.command.groups.reset.ResetChickenRaceCommand;
import net.swofty.type.skyblockgeneric.command.groups.reset.ResetMyStocksCommand;

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
