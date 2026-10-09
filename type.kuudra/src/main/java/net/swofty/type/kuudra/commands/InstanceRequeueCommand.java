package net.swofty.type.kuudra.commands;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.kuudra.run.KuudraRequeue;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
