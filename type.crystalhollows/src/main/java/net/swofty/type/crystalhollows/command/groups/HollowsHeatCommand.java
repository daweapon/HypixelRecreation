package net.swofty.type.crystalhollows.command.groups;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.crystalhollows.HeatService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Sets your Heat",
        labels = "heat",
        usage = "/hollows heat <0-100>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsHeatCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentNumber<Double> amount = ArgumentType.Double("amount").between(0.0, HeatService.DEATH_HEAT);
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            HeatService.set(player, context.get(amount));
            player.sendMessage("<a>Heat set to <e>{}<a>. It cools on its own outside hot zones.", context.get(amount));
        }, amount);
    }
}
