package dev.daweapon.type.crystalhollows.command;

import dev.daweapon.type.crystalhollows.command.groups.HollowsAnchorCommand;
import dev.daweapon.type.crystalhollows.command.groups.HollowsBoundsCommand;
import dev.daweapon.type.crystalhollows.command.groups.HollowsCandidateCommand;
import dev.daweapon.type.crystalhollows.command.groups.HollowsChestCommand;
import dev.daweapon.type.crystalhollows.command.groups.HollowsDebugCommand;
import dev.daweapon.type.crystalhollows.command.groups.HollowsHeatCommand;
import dev.daweapon.type.crystalhollows.command.groups.HollowsLayoutCommand;
import dev.daweapon.type.crystalhollows.command.groups.HollowsPasteCommand;
import dev.daweapon.type.crystalhollows.command.groups.HollowsRegionCommand;
import dev.daweapon.type.crystalhollows.command.groups.HollowsSchematicsCommand;
import dev.daweapon.type.crystalhollows.command.groups.HollowsSpawnCommand;
import dev.daweapon.type.crystalhollows.command.groups.HollowsSpawnWormCommand;
import dev.daweapon.type.crystalhollows.command.groups.HollowsStatueCommand;
import dev.daweapon.type.crystalhollows.command.groups.HollowsTpCommand;
import dev.daweapon.type.crystalhollows.command.groups.HollowsValidateCommand;
import dev.daweapon.type.generic.command.CommandGroup;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;

import java.util.List;
import java.util.Map;

@CommandParameters(labels = "hollows",
        description = "Staff tooling for capturing and validating the Crystal Hollows layout",
        usage = "/hollows <layout|tp|candidate|anchor|statue|chest|region|spawn|bounds|validate|debug|heat|spawnworm|paste|schematics> ...",
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
                new HollowsSpawnWormCommand(),
                new HollowsPasteCommand(),
                new HollowsSchematicsCommand());
    }

    @Override
    protected Map<String, String> legacyLabels() {
        return Map.of();
    }
}
