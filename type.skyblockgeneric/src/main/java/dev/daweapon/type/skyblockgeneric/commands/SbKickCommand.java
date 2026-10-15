package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.VisitManager;

// Owner/coop-member moderation - kicks one current guest off the island back to their own.
@CommandParameters(labels = "sbkick",
        description = "Kicks a guest off your island",
        usage = "/sbkick <player>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class SbKickCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord targetArg = ArgumentType.Word("player");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String targetName = context.get(targetArg);

            if (VisitManager.isVisiting(player.getUuid())) {
                player.sendMessage("<c>You can't kick guests while visiting someone else's island!");
                return;
            }

            SkyBlockPlayer target = SkyBlockGenericLoader.getLoadedPlayers().stream()
                    .filter(loaded -> loaded.getUsername().equalsIgnoreCase(targetName))
                    .findFirst().orElse(null);

            VisitManager.VisitSession session = target == null ? null : VisitManager.activeVisit(target.getUuid());
            if (session == null || !session.islandId().equals(player.getSkyBlockIsland().getIslandID())) {
                player.sendMessage("<c>{} isn't visiting your island!", targetName);
                return;
            }

            SkyBlockPlayer finalTarget = target;
            // sendHome() blocks on scheduleNextTick(...).join() - must not run on the tick thread.
            Thread.startVirtualThread(() -> VisitManager.sendHome(finalTarget));
            player.sendMessage("<e>Kicked {} off your island.", targetName);
            target.sendMessage("<c>You were kicked off {}'s island.", player.getUsername());
        }, targetArg);
    }
}
