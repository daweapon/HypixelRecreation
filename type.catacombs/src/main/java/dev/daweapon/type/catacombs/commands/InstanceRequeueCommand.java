package dev.daweapon.type.catacombs.commands;

import dev.daweapon.type.catacombs.run.CatacombsRequeue;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "instancerequeue",
        description = "Re-queues your party for the same Catacombs floor",
        usage = "/instancerequeue",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class InstanceRequeueCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, _) -> {
            if (!permissionCheck(sender)) return;
            CatacombsRequeue.requeue((SkyBlockPlayer) sender);
        });
    }
}
