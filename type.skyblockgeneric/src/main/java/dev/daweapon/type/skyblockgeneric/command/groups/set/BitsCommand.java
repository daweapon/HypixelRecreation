package dev.daweapon.type.skyblockgeneric.command.groups.set;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.entity.Player;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Sets your bits balance",
    labels = "bits",
        usage = "/set bits <amount> [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class BitsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentNumber<Integer> bitsArgument = ArgumentType.Integer("amount").min(0);
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            setBits((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, context.get(bitsArgument));
        }, bitsArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }
            setBits((SkyBlockPlayer) sender, (SkyBlockPlayer) target, context.get(bitsArgument));
        }, bitsArgument, targetArgument);
    }

    private void setBits(SkyBlockPlayer sender, SkyBlockPlayer target, int amount) {
        target.getSkyblockDataHandler().get(dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BITS, DatapointInteger.class).setValue(amount);

        if (target == sender) {
            sender.sendMessage("<a>Successfully set bits to to <e>{}<a>.", amount);
        } else {
            target.sendMessage("<a>Your bits were set to <e>{}<a>.", amount);
            sender.sendMessage("<a>Set <e>{}<a>'s bits to <e>{}<a>.", target.getUsername(), amount);
        }
    }
}
