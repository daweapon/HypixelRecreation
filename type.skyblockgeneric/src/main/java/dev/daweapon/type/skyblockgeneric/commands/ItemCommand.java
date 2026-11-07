package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.item.ItemTypeResolver;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "i item sbitem",
        description = "Gives an item to the player",
        usage = "/item <item> [amount] [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ItemCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord itemArgument = ArgumentType.Word("item");
        itemArgument.setSuggestionCallback((_, context, suggestion) -> {
            String typed = context.getInput().substring(context.getInput().lastIndexOf(' ') + 1).toUpperCase();
            java.util.Set<String> offered = new java.util.HashSet<>();
            for (ItemType type : ItemType.values()) {
                String name = ItemTypeResolver.commandName(type);
                if (name.startsWith(typed) && offered.add(name)) suggestion.addEntry(new SuggestionEntry(name));
            }
        });
        ArgumentInteger amountArgument = ArgumentType.Integer("amount");
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            final ItemType itemTypeLinker = resolve(sender, context.get(itemArgument));
            if (itemTypeLinker == null) return;
            giveItem((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, itemTypeLinker, 1);
        }, itemArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            final ItemType itemTypeLinker = resolve(sender, context.get(itemArgument));
            if (itemTypeLinker == null) return;
            final int amount = context.get(amountArgument);
            giveItem((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, itemTypeLinker, amount);
        }, itemArgument, amountArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            final ItemType itemTypeLinker = resolve(sender, context.get(itemArgument));
            if (itemTypeLinker == null) return;
            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage(Text.of("<c>Could not find that player."));
                return;
            }
            giveItem((SkyBlockPlayer) sender, (SkyBlockPlayer) target, itemTypeLinker, 1);
        }, itemArgument, targetArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            final ItemType itemTypeLinker = resolve(sender, context.get(itemArgument));
            if (itemTypeLinker == null) return;
            final int amount = context.get(amountArgument);
            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage(Text.of("<c>Could not find that player."));
                return;
            }
            giveItem((SkyBlockPlayer) sender, (SkyBlockPlayer) target, itemTypeLinker, amount);
        }, itemArgument, amountArgument, targetArgument);
    }

    private static ItemType resolve(CommandSender sender, String input) {
        ItemType type = ItemTypeResolver.resolve(input);
        if (type == null) sender.sendMessage(Text.of("<c>No item matches <e>{}<c>.", input));
        return type;
    }

    private void giveItem(SkyBlockPlayer sender, SkyBlockPlayer target, ItemType itemTypeLinker, int amount) {
        SkyBlockItem item = new SkyBlockItem(itemTypeLinker);
        item.setAmount(amount);
        target.addAndUpdateItem(item);

        if (target == sender) {
            sender.sendMessage(amount == 1
                    ? Text.of("<a>Given you item <e>{}<a>.", ItemTypeResolver.commandName(itemTypeLinker))
                    : Text.of("<a>Given you item <e>{}<8> x{}<a>.", ItemTypeResolver.commandName(itemTypeLinker), amount));
        } else {
            target.sendMessage(amount == 1
                    ? Text.of("<a>You were given item <e>{}<a>.", ItemTypeResolver.commandName(itemTypeLinker))
                    : Text.of("<a>You were given item <e>{}<8> x{}<a>.", ItemTypeResolver.commandName(itemTypeLinker), amount));
            sender.sendMessage(amount == 1
                    ? Text.of("<a>Gave <e>{} <a>to <e>{}<a>.", ItemTypeResolver.commandName(itemTypeLinker), target.getUsername())
                    : Text.of("<a>Gave <e>{}<8> x{} <a>to <e>{}<a>.", ItemTypeResolver.commandName(itemTypeLinker), amount, target.getUsername()));
        }
    }
}
