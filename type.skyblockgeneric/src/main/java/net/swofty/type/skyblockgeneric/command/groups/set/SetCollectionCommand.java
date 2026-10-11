package net.swofty.type.skyblockgeneric.command.groups.set;

import net.swofty.type.skyblockgeneric.commands.ArgumentItemType;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(labels = "collection",
        description = "Updates the collection of a player",
        usage = "/set collection <item_type> <amount> [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SetCollectionCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentItemType itemType = new ArgumentItemType("item_type");
        ArgumentInteger amountArgument = new ArgumentInteger("amount");
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            setCollection((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, context.get(itemType), context.get(amountArgument));
        }, itemType, amountArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }
            setCollection((SkyBlockPlayer) sender, (SkyBlockPlayer) target, context.get(itemType), context.get(amountArgument));
        }, itemType, amountArgument, targetArgument);
    }

    private void setCollection(SkyBlockPlayer sender, SkyBlockPlayer target, ItemType type, int amount) {
        target.getCollection().set(type, amount);
        Text category = CollectionCategories.getCategory(type).getName();

        if (target == sender) {
            sender.sendMessage("<a>Updated your <e>{} <a>category for the <c>{} <a>collection to <e>{}<a>.",
                    category, type.getDisplayName(), amount);
        } else {
            target.sendMessage("<a>Your <e>{} <a>category for the <c>{} <a>collection was set to <e>{}<a>.",
                    category, type.getDisplayName(), amount);
            sender.sendMessage("<a>Updated <e>{}<a>'s <e>{} <a>category for the <c>{} <a>collection to <e>{}<a>.",
                    target.getUsername(), category, type.getDisplayName(), amount);
        }
    }
}
