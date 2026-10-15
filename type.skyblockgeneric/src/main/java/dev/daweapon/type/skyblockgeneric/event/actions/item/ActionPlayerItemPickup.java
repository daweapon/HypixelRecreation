package dev.daweapon.type.skyblockgeneric.event.actions.item;

import net.minestom.server.event.player.PlayerMoveEvent;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.entity.drop.ItemPickup;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.entity.DroppedItemEntityImpl;
import dev.daweapon.type.skyblockgeneric.gifts.SnowCannon;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Picks up nearby dropped items for a player, routing them into sacks or inventory
public class ActionPlayerItemPickup implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        DroppedItemEntityImpl.getDroppedItems().computeIfPresent(player, (unused, list) -> {
            list.forEach(item -> {
                if (item.isRemoved() || !item.isPickable()) return;
                if (!ItemPickup.isWithinRange(player, item)) return;

                SkyBlockItem dropped = item.getItem();
                int amount = dropped.getAmount();

                ItemPickup.sendCollectPacket(player, item, amount);
                ItemPickup.playPickupSound(player);

                ItemType type = dropped.getAttributeHandler().getPotentialType();
                if (type == ItemType.SNOWBALL) {
                    amount = SnowCannon.absorbSnowballs(player, amount);
                    if (amount == 0) {
                        item.remove();
                        return;
                    }
                    dropped.setAmount(amount);
                }
                if (player.canInsertItemIntoSacks(type, amount)) {
                    player.getSackItems().increase(type, amount);
                } else {
                    player.addAndUpdateItem(dropped);
                }
                item.remove();
            });
            return list;
        });
    }
}
