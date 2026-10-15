package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.VisitManager;

// Owner/coop-member moderation - kicks every current guest off the island at once.
@CommandParameters(labels = "sbkickall",
        description = "Kicks every guest off your island",
        usage = "/sbkickall",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class SbKickAllCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            if (VisitManager.isVisiting(player.getUuid())) {
                player.sendMessage("<c>You can't kick guests while visiting someone else's island!");
                return;
            }

            if (VisitManager.visitorIds(player.getSkyBlockIsland().getIslandID()).isEmpty()) {
                player.sendMessage("<c>Nobody is currently visiting your island!");
                return;
            }

            // kickAllGuests() blocks on scheduleNextTick(...).join() per guest - must not run on the tick thread.
            Thread.startVirtualThread(() -> {
                int kicked = VisitManager.kickAllGuests(player);
                player.sendMessage("<e>Kicked {} guest(s) off your island.", kicked);
            });
        });
    }
}
