package net.swofty.type.skyblockgeneric.command.groups.unlock;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.warps.TravelScrollIslands;

@CommandParameters(labels = "warps",
        description = "Unlocks every Fast Travel warp on a profile, as /maxprofile does",
        usage = "/unlock warps [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class UnlockWarpsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            unlock((SkyBlockPlayer) sender, (SkyBlockPlayer) sender);
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }

            unlock((SkyBlockPlayer) sender, (SkyBlockPlayer) target);
        }, targetArgument);
    }

    private static void unlock(SkyBlockPlayer sender, SkyBlockPlayer target) {
        TravelScrollIslands.unlockAll(target);
        target.sendMessage("<a>Every Fast Travel warp is now unlocked on this profile.");
        if (target != sender) {
            sender.sendMessage("<a>Unlocked every Fast Travel warp for <e>{}<a>.", target.getUsername());
        }
    }
}
