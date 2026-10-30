package dev.daweapon.type.skyblockgeneric.command.groups.force;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.glacite.mineshaft.MineshaftPity;
import dev.daweapon.type.skyblockgeneric.glacite.mineshaft.MineshaftPortal;
import dev.daweapon.type.skyblockgeneric.glacite.mineshaft.MineshaftService;
import dev.daweapon.type.skyblockgeneric.glacite.mineshaft.ShaftLayout;
import dev.daweapon.type.skyblockgeneric.glacite.mineshaft.ShaftType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "mineshaft", description = "Spawns a mineshaft portal, enters a shaft or edits the mineshaft pity",
        usage = "/force mineshaft <status|portal [layout]|enter [layout]|pity <0-2000>>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceMineshaftCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord actionArgument = ArgumentType.Word("action").from("status", "portal", "enter", "pity");
        ArgumentWord valueArgument = ArgumentType.Word("value");
        valueArgument.setSuggestionCallback((sender, context, suggestion) -> {
            for (ShaftType type : ShaftType.values()) {
                for (ShaftLayout layout : type.layouts()) suggestion.addEntry(new SuggestionEntry(layout.code()));
            }
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            run(sender, context.get(actionArgument), null);
        }, actionArgument);
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            run(sender, context.get(actionArgument), context.get(valueArgument));
        }, actionArgument, valueArgument);
    }

    private static void run(CommandSender sender, String action, String value) {
        SkyBlockPlayer player = (SkyBlockPlayer) sender;
        switch (action) {
            case "status" -> player.sendMessage("<7>Mineshaft pity: <b>{}<7>/{} <8>(chance bonus +{}%, {} active shafts)",
                    MineshaftPity.displayed(MineshaftService.pityCounter(player)), MineshaftPity.START,
                    MineshaftService.chanceBonusPercent(player), MineshaftService.activeShafts());
            case "pity" -> pity(player, value);
            case "portal" -> {
                ShaftLayout layout = layout(player, value);
                if (layout != null) MineshaftPortal.spawn(player, layout);
            }
            case "enter" -> {
                ShaftLayout layout = layout(player, value);
                if (layout != null) MineshaftService.enter(player, layout);
            }
            default -> player.sendMessage("<c>Usage: /force mineshaft status, portal [layout], enter [layout] or pity 0-2000");
        }
    }

    private static ShaftLayout layout(SkyBlockPlayer player, String value) {
        if (value == null) return MineshaftService.rollLayout(player, null);
        ShaftLayout layout = ShaftType.parseLayout(value);
        if (layout == null) player.sendMessage("<c>Unknown layout. Use tab completion, e.g. RUBY_2.");
        return layout;
    }

    private static void pity(SkyBlockPlayer player, String value) {
        try {
            MineshaftService.setDisplayedPity(player, Integer.parseInt(value));
            player.sendMessage("<a>Mineshaft pity set to <b>{}<a>.", MineshaftPity.displayed(MineshaftService.pityCounter(player)));
        } catch (NumberFormatException | NullPointerException exception) {
            player.sendMessage("<c>Pity must be a number from 0 to {}.", MineshaftPity.START);
        }
    }
}
