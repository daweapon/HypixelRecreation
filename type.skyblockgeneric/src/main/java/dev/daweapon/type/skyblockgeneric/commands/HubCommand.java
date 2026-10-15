package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.mission.MissionSet;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "h hub",
        description = "Sends the player to their hub",
        usage = "/hub",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class HubCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = ((SkyBlockPlayer) sender);

            if (!MissionSet.GETTING_STARTED.hasCompleted(player)) {
                player.sendMessage("<c>You must complete your starting missions!");
                return;
            }

            if (HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_HUB) {
                player.teleport(new Pos(0.5, 77, -0.5, -180, 0));
                return;
            }

            player.sendTo(ServerType.SKYBLOCK_HUB);
        });
    }
}
