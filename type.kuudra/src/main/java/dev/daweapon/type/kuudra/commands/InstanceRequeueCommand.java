package dev.daweapon.type.kuudra.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.kuudra.run.KuudraRequeue;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "instancerequeue",
        description = "Re-queues your party for the same Kuudra tier",
        usage = "/instancerequeue",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class InstanceRequeueCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, _) -> {
            if (!permissionCheck(sender)) return;
            KuudraRequeue.requeue((SkyBlockPlayer) sender);
        });
    }
}
