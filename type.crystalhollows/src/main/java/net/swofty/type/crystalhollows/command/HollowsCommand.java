package net.swofty.type.crystalhollows.command;

import net.swofty.type.crystalhollows.command.groups.HollowsAnchorCommand;
import net.swofty.type.crystalhollows.command.groups.HollowsBoundsCommand;
import net.swofty.type.crystalhollows.command.groups.HollowsCandidateCommand;
import net.swofty.type.crystalhollows.command.groups.HollowsChestCommand;
import net.swofty.type.crystalhollows.command.groups.HollowsDebugCommand;
import net.swofty.type.crystalhollows.command.groups.HollowsHeatCommand;
import net.swofty.type.crystalhollows.command.groups.HollowsLayoutCommand;
import net.swofty.type.crystalhollows.command.groups.HollowsRegionCommand;
import net.swofty.type.crystalhollows.command.groups.HollowsSpawnCommand;
import net.swofty.type.crystalhollows.command.groups.HollowsSpawnWormCommand;
import net.swofty.type.crystalhollows.command.groups.HollowsStatueCommand;
import net.swofty.type.crystalhollows.command.groups.HollowsTpCommand;
import net.swofty.type.crystalhollows.command.groups.HollowsValidateCommand;
import net.swofty.type.generic.command.CommandGroup;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;

import java.util.List;
import java.util.Map;

@CommandParameters(labels = "hollows",
        description = "Staff tooling for capturing and validating the Crystal Hollows layout",
        usage = "/hollows <layout|tp|candidate|anchor|statue|chest|region|spawn|bounds|validate|debug|heat|spawnworm> ...",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsCommand extends CommandGroup {
    @Override
    protected List<HypixelCommand> subcommands() {
        return List.of(new HollowsLayoutCommand(),
                new HollowsTpCommand(),
                new HollowsCandidateCommand(),
                new HollowsAnchorCommand(),
                new HollowsStatueCommand(),
                new HollowsChestCommand(),
                new HollowsRegionCommand(),
                new HollowsSpawnCommand(),
                new HollowsBoundsCommand(),
                new HollowsValidateCommand(),
                new HollowsDebugCommand(),
                new HollowsHeatCommand(),
                new HollowsSpawnWormCommand());
    }

    @Override
    protected Map<String, String> legacyLabels() {
        return Map.of();
    }
}
