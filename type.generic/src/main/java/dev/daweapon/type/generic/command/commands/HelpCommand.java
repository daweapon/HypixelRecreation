package dev.daweapon.type.generic.command.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.command.help.HelpCatalog;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

import java.util.List;

@CommandParameters(labels = "help",
        description = "Browse available commands and their usage",
        usage = "/help [page|command]",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class HelpCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> showPage((HypixelPlayer) sender, entries((HypixelPlayer) sender), 1));
        var topic = ArgumentType.Word("page_or_command");
        topic.setSuggestionCallback((sender, context, suggestion) -> {
            if (!(sender instanceof HypixelPlayer player)) return;
            var entries = entries(player);
            for (int page = 1; page <= HelpCatalog.pageCount(entries); page++) {
                suggestion.addEntry(new SuggestionEntry(Integer.toString(page)));
            }
            entries.stream().flatMap(entry -> entry.labels().stream()).distinct().sorted()
                    .forEach(label -> suggestion.addEntry(new SuggestionEntry(label)));
        });
        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            var entries = entries(player);
            String input = context.get(topic);
            if (input.matches("[+-]?\\d+")) {
                try {
                    showPage(player, entries, Integer.parseInt(input));
                } catch (NumberFormatException exception) {
                    invalidPage(player, entries);
                }
            } else {
                HelpCatalog.find(entries, input).ifPresentOrElse(entry -> showDetails(player, entry),
                        () -> player.sendMessage(Component.text("No help available for that command. Use /help to browse.", NamedTextColor.RED)));
            }
        }, topic);
    }

    private static List<HelpCatalog.Entry> entries(HypixelPlayer player) {
        return HelpCatalog.entries(MinecraftServer.getCommandManager().getCommands(), player.getRank(),
                ConfigProvider.settings().isRequireAuth());
    }

    private static void showPage(HypixelPlayer player, List<HelpCatalog.Entry> entries, int page) {
        int total = HelpCatalog.pageCount(entries);
        if (page < 1 || page > total) {
            invalidPage(player, entries);
            return;
        }
        player.sendMessage(Component.text("──── Commands • Page " + page + "/" + total + " ────", NamedTextColor.GOLD));
        for (var entry : HelpCatalog.page(entries, page)) {
            player.sendMessage(Component.text("/" + entry.name(), NamedTextColor.GREEN)
                    .append(Component.text(" — " + entry.description(), NamedTextColor.GRAY))
                    .clickEvent(ClickEvent.runCommand("/help " + entry.name()))
                    .hoverEvent(HoverEvent.showText(Component.text("Click for usage and aliases", NamedTextColor.YELLOW))));
        }
        Component navigation = Component.empty();
        if (page > 1) navigation = navigation.append(pageLink("« Previous", page - 1));
        navigation = navigation.append(Component.text("  " + page + "/" + total + "  ", NamedTextColor.GRAY));
        if (page < total) navigation = navigation.append(pageLink("Next »", page + 1));
        player.sendMessage(navigation);
        player.sendMessage(Component.text("/help <command> for details. Some commands have additional requirements.", NamedTextColor.GRAY));
    }

    private static Component pageLink(String label, int page) {
        return Component.text(label, NamedTextColor.YELLOW).clickEvent(ClickEvent.runCommand("/help " + page));
    }

    private static void invalidPage(HypixelPlayer player, List<HelpCatalog.Entry> entries) {
        player.sendMessage(Component.text("Choose a help page from 1 to " + HelpCatalog.pageCount(entries) + ".", NamedTextColor.RED));
    }

    private static void showDetails(HypixelPlayer player, HelpCatalog.Entry entry) {
        player.sendMessage(Component.text("──── /" + entry.name() + " ────", NamedTextColor.GOLD));
        player.sendMessage(Component.text(entry.description(), NamedTextColor.GRAY));
        player.sendMessage(Component.text("Usage: " + entry.usage(), NamedTextColor.GREEN)
                .clickEvent(ClickEvent.suggestCommand("/" + entry.name() + " "))
                .hoverEvent(HoverEvent.showText(Component.text("Click to prepare this command", NamedTextColor.YELLOW))));
        var aliases = entry.labels().stream().filter(label -> !label.equals(entry.name())).map(label -> "/" + label).toList();
        if (!aliases.isEmpty()) player.sendMessage(Component.text("Aliases: " + String.join(", ", aliases), NamedTextColor.GRAY));
        player.sendMessage(Component.text("Minimum rank: " + entry.permission().name() + ". Other command requirements still apply.", NamedTextColor.GRAY));
        player.sendMessage(Component.text("« Back to commands", NamedTextColor.YELLOW).clickEvent(ClickEvent.runCommand("/help")));
    }
}
