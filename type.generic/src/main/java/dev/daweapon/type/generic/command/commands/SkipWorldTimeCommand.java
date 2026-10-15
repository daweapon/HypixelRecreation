package dev.daweapon.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;
import net.minestom.server.entity.Player;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(
    allowsConsole = false,
    description = "Skips the time in the current instance",
    permission = Rank.STAFF,
    labels = "worldtime skipworldtime",
    usage = "/worldtime (tick)"
)
// Staff command to set the world time of the sender's current instance.
public class SkipWorldTimeCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentLong timeArg = ArgumentType.Long("tick");
        command.addSyntax((sender, context) -> {
            long time = context.get(timeArg);
            if (time < 0) {
                sender.sendMessage("<c>Tick must be not negative.");
                return;
            }
            if (time > 24000) {
                sender.sendMessage("<c>Tick must be less than or equal to 24000.");
                return;
            }
            if (sender instanceof Player player) {
                player.getInstance().setTime(time);
                ((HypixelPlayer) player).sendMessage("<a>Set the world time to <e>{}<a> ticks.", time);
            }
        }, timeArg);
    }
}
