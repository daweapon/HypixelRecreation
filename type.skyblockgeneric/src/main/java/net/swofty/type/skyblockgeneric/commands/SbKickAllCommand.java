package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.island.VisitManager;

import java.util.List;
import java.util.UUID;

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

            UUID islandId = player.getSkyBlockIsland().getIslandID();
            List<UUID> visitorIds = List.copyOf(VisitManager.visitorIds(islandId));
            if (visitorIds.isEmpty()) {
                player.sendMessage("<c>Nobody is currently visiting your island!");
                return;
            }

            int kicked = 0;
            for (UUID visitorId : visitorIds) {
                SkyBlockPlayer target = SkyBlockGenericLoader.getLoadedPlayers().stream()
                        .filter(loaded -> loaded.getUuid().equals(visitorId))
                        .findFirst().orElse(null);
                if (target == null) continue;

                // sendHome() blocks on scheduleNextTick(...).join() - must not run on the tick thread.
                Thread.startVirtualThread(() -> VisitManager.sendHome(target));
                target.sendMessage("<c>You were kicked off {}'s island.", player.getUsername());
                kicked++;
            }

            player.sendMessage("<e>Kicked {} guest(s) off your island.", kicked);
        });
    }
}
