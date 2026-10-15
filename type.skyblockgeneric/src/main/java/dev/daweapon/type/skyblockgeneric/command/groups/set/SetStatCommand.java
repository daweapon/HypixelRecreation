package dev.daweapon.type.skyblockgeneric.command.groups.set;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.entity.Player;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.StatOverrides;

import java.util.Locale;
import java.util.Map;

@CommandParameters(labels = "stat",
        description = "Pins any of a player's statistics to an exact value",
        usage = "/set stat <statistic> <value> [player] | /set stat clear <statistic|all> [player] | /set stat list [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SetStatCommand extends HypixelCommand {
    private static final String ALL = "all";

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord statArgument = ArgumentType.Word("statistic");
        statArgument.setSuggestionCallback((_, _, suggestion) -> {
            for (ItemStatistic stat : ItemStatistic.values()) {
                suggest(suggestion, stat.name(), "<7>{}{}", stat.getDisplayName(),
                        stat.getCap() == null ? "" : " <8>(capped at " + StringUtility.commaify(stat.getCap()) + ")");
            }
        });
        ArgumentNumber<Double> valueArgument = ArgumentType.Double("value").between(0D, StatOverrides.MAX_VALUE);
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        ArgumentWord clearLiteral = ArgumentType.Word("clear").from("clear");
        ArgumentWord clearArgument = ArgumentType.Word("statistic");
        clearArgument.setSuggestionCallback((_, _, suggestion) -> {
            suggest(suggestion, ALL, "<7>Every overridden statistic");
            for (ItemStatistic stat : ItemStatistic.values()) {
                suggest(suggestion, stat.name(), "<7>{}", stat.getDisplayName());
            }
        });
        ArgumentWord listLiteral = ArgumentType.Word("list").from("list");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            set((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, context.get(statArgument), context.get(valueArgument));
        }, statArgument, valueArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer target = resolve((SkyBlockPlayer) sender, context.get(targetArgument).findFirstPlayer(sender));
            if (target == null) return;
            set((SkyBlockPlayer) sender, target, context.get(statArgument), context.get(valueArgument));
        }, statArgument, valueArgument, targetArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            clear((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, context.get(clearArgument));
        }, clearLiteral, clearArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer target = resolve((SkyBlockPlayer) sender, context.get(targetArgument).findFirstPlayer(sender));
            if (target == null) return;
            clear((SkyBlockPlayer) sender, target, context.get(clearArgument));
        }, clearLiteral, clearArgument, targetArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            list((SkyBlockPlayer) sender, (SkyBlockPlayer) sender);
        }, listLiteral);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer target = resolve((SkyBlockPlayer) sender, context.get(targetArgument).findFirstPlayer(sender));
            if (target == null) return;
            list((SkyBlockPlayer) sender, target);
        }, listLiteral, targetArgument);
    }

    private static SkyBlockPlayer resolve(SkyBlockPlayer sender, Player found) {
        if (found == null) {
            sender.sendMessage("<c>Could not find that player.");
            return null;
        }
        return (SkyBlockPlayer) found;
    }

    private static ItemStatistic parse(SkyBlockPlayer sender, String name) {
        try {
            return ItemStatistic.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            sender.sendMessage("<c>Unknown statistic '" + name + "'. Press Tab to see them all.");
            return null;
        }
    }

    private static void set(SkyBlockPlayer sender, SkyBlockPlayer target, String name, double value) {
        ItemStatistic stat = parse(sender, name);
        if (stat == null) return;
        StatOverrides.set(target, stat, value);
        sender.sendMessage("<a>Set " + target.getUsername() + "<a>'s <e>" + stat.getDisplayName()
                + " <a>to <e>" + StringUtility.commaify(value) + "<a>.");
        if (stat.getCap() != null && value > stat.getCap()) {
            sender.sendMessage("<7>" + stat.getDisplayName() + " is capped at " + StringUtility.commaify(stat.getCap())
                    + " in game, so it will read as that.");
        }
        if (target != sender) {
            target.sendMessage("<e>Your " + stat.getDisplayName() + " was set to " + StringUtility.commaify(value) + " by staff.");
        }
    }

    private static void clear(SkyBlockPlayer sender, SkyBlockPlayer target, String name) {
        if (name.equalsIgnoreCase(ALL)) {
            int cleared = StatOverrides.clearAll(target);
            sender.sendMessage("<a>Cleared " + cleared + " statistic override" + (cleared == 1 ? "" : "s")
                    + " for " + target.getUsername() + "<a>.");
            return;
        }
        ItemStatistic stat = parse(sender, name);
        if (stat == null) return;
        if (StatOverrides.clear(target, stat)) {
            sender.sendMessage("<a>" + target.getUsername() + "<a>'s <e>" + stat.getDisplayName()
                    + " <a>is back to its normal value.");
        } else {
            sender.sendMessage("<c>" + target.getUsername() + " has no override on " + stat.getDisplayName() + ".");
        }
    }

    private static void list(SkyBlockPlayer sender, SkyBlockPlayer target) {
        Map<ItemStatistic, Double> overrides = StatOverrides.of(target);
        if (overrides.isEmpty()) {
            sender.sendMessage("<7>" + target.getUsername() + " has no statistic overrides.");
            return;
        }
        sender.sendMessage("<a>Statistic overrides for " + target.getUsername() + "<a>:");
        overrides.forEach((stat, value) ->
                sender.sendMessage("<8>- <e>" + stat.getDisplayName() + "<7>: <f>" + StringUtility.commaify(value)));
    }
}
