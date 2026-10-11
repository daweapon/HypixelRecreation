package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.MinecraftServer;
import net.swofty.type.generic.command.CommandGroup;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.command.groups.mission.ClearMissionCommand;
import net.swofty.type.skyblockgeneric.command.groups.mission.FinishMissionSetCommand;
import net.swofty.type.skyblockgeneric.command.groups.mission.MissionInfoCommand;
import net.swofty.type.skyblockgeneric.command.groups.mission.PrintMissionDataCommand;
import net.swofty.type.skyblockgeneric.command.groups.mission.StartMission;

import java.util.List;
import java.util.Map;

@CommandParameters(labels = "mission",
        description = "Shows, starts, clears and finishes missions",
        usage = "/mission <info|data|start|clear|clearset> ...",
        permission = Rank.STAFF,
        allowsConsole = false)
public class MissionCommand extends CommandGroup {
    @Override
    protected List<HypixelCommand> subcommands() {
        return List.of(new MissionInfoCommand(),
                new PrintMissionDataCommand(),
                new StartMission(),
                new ClearMissionCommand(),
                new FinishMissionSetCommand());
    }

    @Override
    protected Map<String, String> legacyLabels() {
        return Map.ofEntries(Map.entry("missiondata", "mission info"),
                Map.entry("displaymissiondata", "mission data"),
                Map.entry("startmission", "mission start"),
                Map.entry("clearmission", "mission clear"),
                Map.entry("clearmissionset", "mission clearset"),
                Map.entry("completemissionset", "mission clearset"));
    }

    @Override
    public void registerUsage(MinestomCommand command) {
        super.registerUsage(command);
        command.setDefaultExecutor((sender, _) -> MinecraftServer.getCommandManager().execute(sender, "mission info"));
    }
}
