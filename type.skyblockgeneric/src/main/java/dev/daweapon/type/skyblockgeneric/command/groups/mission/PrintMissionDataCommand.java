package dev.daweapon.type.skyblockgeneric.command.groups.mission;
import dev.daweapon.commons.text.Text;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "data",
        description = "Displays the mission data of a player",
        usage = "/mission data",
        permission = Rank.STAFF,
        allowsConsole = false)
public class PrintMissionDataCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            MissionData data = player.getMissionData();

            player.sendMessage("<a>Mission Data:");
            player.sendMessage("<7>Active Missions:");
            data.getActiveMissions().forEach((mission) -> {
                player.sendMessage("<e>{}<7>:", mission.toString());
                mission.getCustomData().forEach((key, value) -> {
                    player.sendMessage("<7>{}<e>: {}", key, value);
                });
            });

            player.sendMessage("<7>Completed Missions:");
            data.getCompletedMissions().forEach((mission) -> {
                player.sendMessage("<e>{}", mission.toString());
                mission.getCustomData().forEach((key, value) -> {
                    player.sendMessage("<7>{}<e>: {}", key, value);
                });
            });
        });
    }
}
