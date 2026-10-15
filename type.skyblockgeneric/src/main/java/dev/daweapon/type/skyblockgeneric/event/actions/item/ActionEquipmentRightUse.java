package dev.daweapon.type.skyblockgeneric.event.actions.item;

import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.equipment.EquipmentSlots;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionEquipmentRightUse implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerUseItemEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        ItemStack held = player.getItemInMainHand();
        EquipmentSlots slot = EquipmentSlots.forItem(held);
        if (slot == null || !slot.isEmpty(player)) return;

        event.setCancelled(true);
        slot.equip(player, held);
        player.setItemInMainHand(ItemStack.AIR);
    }
}
