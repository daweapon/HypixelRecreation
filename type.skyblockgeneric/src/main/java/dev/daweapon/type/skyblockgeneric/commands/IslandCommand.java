package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.VisitManager;

@CommandParameters(labels = "is island",
        description = "Sends the player to their island",
        usage = "/is",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class IslandCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = ((SkyBlockPlayer) sender);

            // While visiting, /is means "take me home" rather than the usual (load-balanced) trip
            // to a SKYBLOCK_ISLAND server - player.getSkyBlockIsland() was never touched by the
            // visit, so a plain transfer/move back to it is all that's needed. Dispatched onto a
            // virtual thread since command syntax callbacks run on the tick thread, and
            // VisitManager.sendHome() blocks on scheduleNextTick(...).join() - see VisitCommand.visitAsync().
            if (VisitManager.isVisiting(player.getUuid())) {
                Thread.startVirtualThread(() -> VisitManager.sendHome(player));
                return;
            }

            player.sendTo(ServerType.SKYBLOCK_ISLAND);
        });
    }
}
