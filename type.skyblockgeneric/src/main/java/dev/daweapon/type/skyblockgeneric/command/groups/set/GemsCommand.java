package dev.daweapon.type.skyblockgeneric.command.groups.set;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.entity.Player;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Sets your Gems balance",
    labels = "gems",
        usage = "/set gems <amount> [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class GemsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentNumber<Integer> gemsArgument = ArgumentType.Integer("amount").min(0);
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            setGems((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, context.get(gemsArgument));
        }, gemsArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }
            setGems((SkyBlockPlayer) sender, (SkyBlockPlayer) target, context.get(gemsArgument));
        }, gemsArgument, targetArgument);
    }

    private void setGems(SkyBlockPlayer sender, SkyBlockPlayer target, int amount) {
        target.setGems(amount);

        if (target == sender) {
            sender.sendMessage("<a>Successfully set Gems to to <e>{}<a>.", amount);
        } else {
            target.sendMessage("<a>Your Gems were set to <e>{}<a>.", amount);
            sender.sendMessage("<a>Set <e>{}<a>'s Gems to <e>{}<a>.", target.getUsername(), amount);
        }
    }
}
