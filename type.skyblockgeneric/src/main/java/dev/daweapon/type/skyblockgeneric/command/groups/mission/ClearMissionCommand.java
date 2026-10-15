package dev.daweapon.type.skyblockgeneric.command.groups.mission;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointMissionData;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.missions.MissionBreakLog;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(description = "Clears your missions",
        usage = "/mission clear",
        permission = Rank.STAFF,
        labels = "clear",
        allowsConsole = false)
public class ClearMissionCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            sender.sendMessage("<a>Your missions have been cleared.");
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.MISSION_DATA, DatapointMissionData.class)
                    .setValue(new MissionData());
            player.getMissionData().startMission(MissionBreakLog.class);
        });
    }
}
