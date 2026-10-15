package dev.daweapon.type.generic.command.commands;

import dev.daweapon.type.generic.command.CommandGroup;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.generic.command.groups.debug.ExplodeSimulationCommand;
import dev.daweapon.type.generic.command.groups.debug.FormatNameCommand;
import dev.daweapon.type.generic.command.groups.debug.LocaleDataCommand;
import dev.daweapon.type.generic.command.groups.debug.NPCPathfindingCommand;
import dev.daweapon.type.generic.command.groups.debug.SignGUICommand;
import dev.daweapon.type.generic.command.groups.debug.TranslatableComponentCommand;

import java.util.List;
import java.util.Map;

@CommandParameters(labels = "debug",
        description = "Diagnostics and test tools",
        usage = "/debug <formatname|localedata|npcpathfinding|explosion|signgui|component|configids|damageindicator|lag|mining|recipes|tasks|headrotation|mutex|particles|anvilgui|music> ...",
        permission = Rank.STAFF,
        allowsConsole = false)
public class DebugCommand extends CommandGroup {
    @Override
    protected List<HypixelCommand> subcommands() {
        return List.of(new FormatNameCommand(),
                new LocaleDataCommand(),
                new NPCPathfindingCommand(),
                new ExplodeSimulationCommand(),
                new SignGUICommand(),
                new TranslatableComponentCommand());
    }

    @Override
    protected Map<String, String> legacyLabels() {
        return Map.ofEntries(Map.entry("formatname", "debug formatname"),
                Map.entry("getnameformatted", "debug formatname"),
                Map.entry("localedata", "debug localedata"),
                Map.entry("npcpathfinding", "debug npcpathfinding"),
                Map.entry("explodesimulation", "debug explosion"),
                Map.entry("signgui", "debug signgui"),
                Map.entry("signgraphicaluserinterface", "debug signgui"),
                Map.entry("translatablecomponent", "debug component"));
    }
}
