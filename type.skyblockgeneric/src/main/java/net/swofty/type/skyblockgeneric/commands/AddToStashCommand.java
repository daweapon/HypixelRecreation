package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointStash;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "addtostash",
        description = "Force add an item to your stash",
        usage = "/addtostash <item> [amount] [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class AddToStashCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentItemType itemArgument = new ArgumentItemType("item");
        ArgumentInteger amountArgument = ArgumentType.Integer("amount");
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            addItemToStash(player, player, context.get(itemArgument), 1);
        }, itemArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            addItemToStash(player, player, context.get(itemArgument), context.get(amountArgument));
        }, itemArgument, amountArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }
            addItemToStash((SkyBlockPlayer) sender, (SkyBlockPlayer) target, context.get(itemArgument), context.get(amountArgument));
        }, itemArgument, amountArgument, targetArgument);
    }

    private void addItemToStash(SkyBlockPlayer sender, SkyBlockPlayer target, ItemType itemType, int amount) {
        DatapointStash.PlayerStash stash = target.getStash();
        SkyBlockItem item = new SkyBlockItem(itemType);
        boolean toOther = target != sender;

        if (item.getMaterial().maxStackSize() > 1) {
            stash.addToMaterialStash(itemType, amount);
            if (toOther) {
                target.sendMessage("<a>You were given <e>{}x {} <a>in your material stash.", amount, itemType.getDisplayName());
                sender.sendMessage("<a>Added <e>{}x {} <a>to <e>{}<a>'s material stash.", amount, itemType.getDisplayName(), target.getUsername());
            } else {
                sender.sendMessage("<a>Added <e>{}x {} <a>to your material stash.", amount, itemType.getDisplayName());
            }
        } else {
            int added = 0;
            for (int i = 0; i < amount; i++) {
                if (stash.addToItemStash(new SkyBlockItem(itemType))) {
                    added++;
                } else {
                    break;
                }
            }
            if (added > 0) {
                if (toOther) {
                    target.sendMessage("<a>You were given <e>{}x {} <a>in your item stash.", added, itemType.getDisplayName());
                    sender.sendMessage("<a>Added <e>{}x {} <a>to <e>{}<a>'s item stash.", added, itemType.getDisplayName(), target.getUsername());
                } else {
                    sender.sendMessage("<a>Added <e>{}x {} <a>to your item stash.", added, itemType.getDisplayName());
                }
            }
            if (added < amount) {
                sender.sendMessage("<c>Couldn't add {} items - {}'s item stash is at limit (720).",
                        amount - added, toOther ? target.getUsername() : "your");
            }
        }
    }
}
