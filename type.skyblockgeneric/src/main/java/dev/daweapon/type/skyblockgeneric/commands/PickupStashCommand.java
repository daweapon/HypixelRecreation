package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentWord;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointStash;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

@CommandParameters(labels = "pickupstash",
        description = "Pickup items from your stash",
        usage = "/pickupstash [item|material]",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class PickupStashCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord typeArg = new ArgumentWord("type").from("item", "material");

        // No args - pick up from both stashes
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            pickupFromItemStash(player);
            pickupFromMaterialStash(player);
        });

        // With type argument
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String type = context.get(typeArg);

            if (type.equalsIgnoreCase("material")) {
                pickupFromMaterialStash(player);
            } else {
                pickupFromItemStash(player);
            }
        }, typeArg);
    }

    private void pickupFromItemStash(SkyBlockPlayer player) {
        DatapointStash.PlayerStash stash = player.getStash();

        if (stash.getItemStashCount() == 0) {
            player.sendMessage("<c>Your item stash is already empty!");
            return;
        }

        int pickedUp = 0;
        while (player.hasEmptySlots(1) && stash.getItemStashCount() > 0) {
            SkyBlockItem removed = stash.removeFromItemStash(0);
            if (removed != null) {
                player.addAndUpdateItem(removed);
                player.sendMessage("<a>From stash: <f>{}", removed.getDisplayName());
                pickedUp++;
            }
        }

        if (pickedUp == 0) {
            player.sendMessage("<c>Couldn't unstash your item stash! Your inventory is full!");
        } else if (stash.getItemStashCount() == 0) {
            player.sendMessage("<a>You picked up all items from your item stash!");
        } else {
            player.sendMessage("<e>You still have <c>{} <e>items in there!", stash.getItemStashCount());
        }
    }

    private void pickupFromMaterialStash(SkyBlockPlayer player) {
        DatapointStash.PlayerStash stash = player.getStash();

        if (stash.getMaterialStashCount() == 0) {
            player.sendMessage("<c>Your material stash is already empty!");
            return;
        }

        int pickedUp = 0;
        List<ItemType> types = new ArrayList<>(stash.getMaterialStash().keySet());

        for (ItemType type : types) {
            int amount = stash.getMaterialStash().getOrDefault(type, 0);
            int maxStackSize = type.material.maxStackSize();

            while (amount > 0 && player.hasEmptySlots(1)) {
                int toPickup = Math.min(amount, maxStackSize);
                int removed = stash.removeFromMaterialStash(type, toPickup);
                if (removed > 0) {
                    SkyBlockItem newItem = new SkyBlockItem(type);
                    newItem.setAmount(removed);
                    player.addAndUpdateItem(newItem);
                    player.sendMessage("<a>From stash: <f>{} <7>x{}", type.getDisplayName(), removed);
                    pickedUp += removed;
                    amount -= removed;
                } else {
                    break;
                }
            }

            if (!player.hasEmptySlots(1)) break;
        }

        if (pickedUp == 0) {
            player.sendMessage("<c>Couldn't unstash your material stash! Your inventory is full!");
        } else if (stash.getMaterialStashCount() == 0) {
            player.sendMessage("<a>You picked up all items from your material stash!");
        } else {
            player.sendMessage("<e>You still have <c>{} <e>materials totalling <c>{} <e>types of materials in there!",
                    stash.getMaterialStashCount(), stash.getMaterialTypeCount());
        }
    }
}
