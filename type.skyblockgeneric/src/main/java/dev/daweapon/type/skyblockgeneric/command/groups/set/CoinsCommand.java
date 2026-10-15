package dev.daweapon.type.skyblockgeneric.command.groups.set;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.entity.Player;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.datapoints.DatapointDouble;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "coins", description = "Sets your purse balance",
        usage = "/set coins <amount> [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class CoinsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentNumber<Double> doubleArgument = ArgumentType.Double("amount").min(0D);
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            setCoins((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, context.get(doubleArgument));
        }, doubleArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }
            setCoins((SkyBlockPlayer) sender, (SkyBlockPlayer) target, context.get(doubleArgument));
        }, doubleArgument, targetArgument);
    }

    private void setCoins(SkyBlockPlayer sender, SkyBlockPlayer target, double amount) {
        target.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class).setValue(amount);

        if (target == sender) {
            sender.sendMessage("<a>Successfully set coins to to <e>{:,}<a>.", amount);
        } else {
            target.sendMessage("<a>Your coins were set to <e>{:,}<a>.", amount);
            sender.sendMessage("<a>Set <e>{}<a>'s coins to <e>{:,}<a>.", target.getUsername(), amount);
        }
    }
}
