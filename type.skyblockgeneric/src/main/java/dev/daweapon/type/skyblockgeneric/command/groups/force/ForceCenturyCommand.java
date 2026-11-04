package dev.daweapon.type.skyblockgeneric.command.groups.force;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.century.raffle.CenturyEvent;
import dev.daweapon.type.skyblockgeneric.century.raffle.RaffleDraws;
import dev.daweapon.type.skyblockgeneric.century.raffle.RaffleType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;

import java.util.concurrent.TimeUnit;

@CommandParameters(labels = "century", description = "Starts, ends or resets the Raffle of the Century, or draws a raffle early",
        usage = "/force century <start [hours]|end|clear|draw <speed|daily|bigone>>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceCenturyCommand extends HypixelCommand {
    private static final int DEFAULT_HOURS = 124;

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord action = ArgumentType.Word("action").from("start", "end", "clear", "draw");
        var hours = ArgumentType.Integer("hours").min(1);
        ArgumentWord raffle = ArgumentType.Word("raffle").from("speed", "daily", "bigone");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            run((SkyBlockPlayer) sender, context.get(action), DEFAULT_HOURS, null);
        }, action);
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            run((SkyBlockPlayer) sender, context.get(action), context.get(hours), null);
        }, action, hours);
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            run((SkyBlockPlayer) sender, context.get(action), DEFAULT_HOURS, context.get(raffle));
        }, action, raffle);
    }

    private void run(SkyBlockPlayer player, String action, int hours, String raffle) {
        switch (action) {
            case "start" -> player.sendMessage(CenturyEvent.forceStart(TimeUnit.HOURS.toMillis(hours))
                    ? "<a>Started the Raffle of the Century for <e>" + hours + " <a>hours on every server."
                    : "<c>Redis is unavailable.");
            case "end" -> player.sendMessage(CenturyEvent.forceEnd()
                    ? "<a>Ended the Raffle of the Century. Pending raffles are drawn within a minute."
                    : "<c>The Raffle of the Century isn't running.");
            case "clear" -> player.sendMessage(CenturyEvent.clearOverride()
                    ? "<a>Cleared the override. The raffle now follows the SkyBlock calendar (every 100th year)."
                    : "<c>Redis is unavailable.");
            case "draw" -> {
                RaffleType type = switch (raffle == null ? "speed" : raffle) {
                    case "daily" -> RaffleType.DAILY;
                    case "bigone" -> RaffleType.BIG_ONE;
                    default -> RaffleType.SPEED;
                };
                player.sendMessage(RaffleDraws.forceDraw(type)
                        ? "<a>Drawing the current <e>" + type.displayName() + " <a>now."
                        : "<c>The Raffle of the Century isn't running.");
            }
            default -> player.sendMessage("<c>Usage: /force century <start [hours]|end|clear|draw <speed|daily|bigone>>");
        }
    }
}
