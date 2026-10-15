package dev.daweapon.type.skyblockgeneric.command.groups.mission;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "info",
        description = "Collects mission information",
        usage = "/mission info",
        permission = Rank.STAFF,
        allowsConsole = false)
public class MissionInfoCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            MissionData data = ((SkyBlockPlayer) sender).getMissionData();

            sender.sendMessage(Text.of("Active Missions: {}", data.getActiveMissions().size()));
            sender.sendMessage(Text.of("Completed Missions: {}", data.getCompletedMissions().size()));

            data.getCompletedMissions().forEach(mission -> {
                sender.sendMessage(Text.of("Completed Mission: {}", mission.getMissionID()));
            });
            data.getActiveMissions().forEach(mission -> {
                sender.sendMessage(Text.of("Active Mission: {}", mission.getMissionID()));
            });
        });
    }
}