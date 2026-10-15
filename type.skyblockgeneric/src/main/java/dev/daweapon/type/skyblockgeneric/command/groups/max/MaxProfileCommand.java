package dev.daweapon.type.skyblockgeneric.command.groups.max;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.staff.MaxStickService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "profile",
        description = "Fully maxes out a profile, identical to THE MAX STICK",
        usage = "/max profile [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class MaxProfileCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            MaxStickService.maxProfile((SkyBlockPlayer) sender);
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }

            SkyBlockPlayer skyBlockTarget = (SkyBlockPlayer) target;
            MaxStickService.maxProfile(skyBlockTarget);

            if (target != sender) {
                ((SkyBlockPlayer) sender).sendMessage("<a>Fully maxed out <e>{}<a>'s profile.", skyBlockTarget.getUsername());
            }
        }, targetArgument);
    }
}
