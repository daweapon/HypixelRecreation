package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "i item sbitem",
        description = "Gives an item to the player",
        usage = "/item <item> [amount] [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ItemCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<ItemType> itemArgument = ArgumentType.Enum("item", ItemType.class);
        ArgumentInteger amountArgument = ArgumentType.Integer("amount");
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            final ItemType itemTypeLinker = context.get(itemArgument);
            giveItem((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, itemTypeLinker, 1);
        }, itemArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            final ItemType itemTypeLinker = context.get(itemArgument);
            final int amount = context.get(amountArgument);
            giveItem((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, itemTypeLinker, amount);
        }, itemArgument, amountArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            final ItemType itemTypeLinker = context.get(itemArgument);
            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage(Text.of("<c>Could not find that player."));
                return;
            }
            giveItem((SkyBlockPlayer) sender, (SkyBlockPlayer) target, itemTypeLinker, 1);
        }, itemArgument, targetArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            final ItemType itemTypeLinker = context.get(itemArgument);
            final int amount = context.get(amountArgument);
            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage(Text.of("<c>Could not find that player."));
                return;
            }
            giveItem((SkyBlockPlayer) sender, (SkyBlockPlayer) target, itemTypeLinker, amount);
        }, itemArgument, amountArgument, targetArgument);
    }

    private void giveItem(SkyBlockPlayer sender, SkyBlockPlayer target, ItemType itemTypeLinker, int amount) {
        SkyBlockItem item = new SkyBlockItem(itemTypeLinker);
        item.setAmount(amount);
        target.addAndUpdateItem(item);

        if (target == sender) {
            sender.sendMessage(amount == 1
                    ? Text.of("<a>Given you item <e>{}<a>.", itemTypeLinker.name())
                    : Text.of("<a>Given you item <e>{}<8> x{}<a>.", itemTypeLinker.name(), amount));
        } else {
            target.sendMessage(amount == 1
                    ? Text.of("<a>You were given item <e>{}<a>.", itemTypeLinker.name())
                    : Text.of("<a>You were given item <e>{}<8> x{}<a>.", itemTypeLinker.name(), amount));
            sender.sendMessage(amount == 1
                    ? Text.of("<a>Gave <e>{} <a>to <e>{}<a>.", itemTypeLinker.name(), target.getUsername())
                    : Text.of("<a>Gave <e>{}<8> x{} <a>to <e>{}<a>.", itemTypeLinker.name(), amount, target.getUsername()));
        }
    }
}
