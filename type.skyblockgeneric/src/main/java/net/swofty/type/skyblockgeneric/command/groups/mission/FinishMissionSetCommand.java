package net.swofty.type.skyblockgeneric.command.groups.mission;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.mission.MissionSet;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.util.Arrays;

@CommandParameters(labels = "clearset",
        description = "Clears the mission set given",
        usage = "/mission clearset <set> [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class FinishMissionSetCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<MissionSet> set = ArgumentType.Enum("set", MissionSet.class);
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            completeMissionSet((SkyBlockPlayer) sender, context.get(set), (SkyBlockPlayer) sender);
        }, set);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (!(target instanceof SkyBlockPlayer skyBlockTarget)) {
                sender.sendMessage(Text.of("<c>Could not find that player."));
                return;
            }

            completeMissionSet((SkyBlockPlayer) sender, context.get(set), skyBlockTarget);
        }, set, targetArgument);
    }

    private void completeMissionSet(SkyBlockPlayer sender, MissionSet missionSet, SkyBlockPlayer target) {
        Arrays.stream(missionSet.getMissions()).forEach(mission -> {
            try {
                target.getMissionData().endMission(mission);
            } catch (Exception ignored) {}
        });

        if (target == sender) {
            sender.sendMessage(Text.of("<a>Successfully finished mission set <e>{}", missionSet.name()));
        } else {
            sender.sendMessage(Text.of("<a>Successfully finished mission set <e>{}<a> for <e>{}<a>.",
                    missionSet.name(), target.getUsername()));
        }
    }
}
