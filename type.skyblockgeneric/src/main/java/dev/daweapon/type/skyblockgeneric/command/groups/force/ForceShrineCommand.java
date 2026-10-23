package dev.daweapon.type.skyblockgeneric.command.groups.force;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.technoblade.AncientPigEffect;
import dev.daweapon.type.skyblockgeneric.technoblade.TechnobladeShrine;

import java.util.Arrays;

@CommandParameters(labels = "shrine", description = "Controls the Blood God Shrine: relics, Ancient Pig effects and reset",
        usage = "/force shrine <status|reset|relic <1-7|all>|effect <effect>>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceShrineCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord actionArgument = ArgumentType.Word("action").from("status", "reset", "relic", "effect");
        ArgumentWord valueArgument = ArgumentType.Word("value");
        valueArgument.setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("all"));
            for (int relic = 1; relic <= TechnobladeShrine.relicCount(); relic++) suggestion.addEntry(new SuggestionEntry(String.valueOf(relic)));
            Arrays.stream(AncientPigEffect.values()).forEach(effect -> suggestion.addEntry(new SuggestionEntry(effect.name().toLowerCase())));
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            switch (context.get(actionArgument)) {
                case "status" -> sender.sendMessage("<7>Shrine: <f>" + TechnobladeShrine.status());
                case "reset" -> {
                    TechnobladeShrine.reset();
                    sender.sendMessage("<a>Reset the Blood God Shrine: relics, effect and timers cleared.");
                }
                default -> sender.sendMessage("<c>Usage: /force shrine status, reset, relic 1-7 or all, effect NAME");
            }
        }, actionArgument);
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            String value = context.get(valueArgument);
            switch (context.get(actionArgument)) {
                case "relic" -> relic(sender, value);
                case "effect" -> effect(sender, value);
                default -> sender.sendMessage("<c>Usage: /force shrine status, reset, relic 1-7 or all, effect NAME");
            }
        }, actionArgument, valueArgument);
    }

    private static void relic(net.minestom.server.command.CommandSender sender, String value) {
        int count = TechnobladeShrine.relicCount();
        if (value.equalsIgnoreCase("all")) {
            for (int index = 0; index < count; index++) TechnobladeShrine.forceRelic(index, "Staff");
            sender.sendMessage("<a>Placed all " + count + " Shiny Relics on the shrine.");
            return;
        }
        int number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            sender.sendMessage("<c>Relic must be 1-" + count + " or all.");
            return;
        }
        if (!TechnobladeShrine.forceRelic(number - 1, "Staff")) {
            sender.sendMessage("<c>Relic must be 1-" + count + " (and Redis must be available).");
            return;
        }
        sender.sendMessage("<a>Placed Shiny Relic " + number + " on the shrine.");
    }

    private static void effect(net.minestom.server.command.CommandSender sender, String value) {
        AncientPigEffect effect = AncientPigEffect.parse(value);
        if (effect == null) {
            sender.sendMessage("<c>Unknown effect. Use tab completion.");
            return;
        }
        TechnobladeShrine.forceEffect(effect);
        sender.sendMessage("<a>Started the <6>" + effect.displayName() + " <a>Ancient Pig effect.");
    }
}
