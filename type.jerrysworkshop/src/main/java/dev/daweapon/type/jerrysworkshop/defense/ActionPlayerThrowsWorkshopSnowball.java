package dev.daweapon.type.jerrysworkshop.defense;

import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.gifts.SnowballProjectile;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Throws a held Snowball during an active "Defend Jerry's Workshop" wave - the only way the Magma
// Cubes take damage. Frosty's cannons shoot through their own item handler.
public class ActionPlayerThrowsWorkshopSnowball implements HypixelEventClass {
    private static final double THROW_SPEED = 1.4;

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerUseItemEvent event) {
        if (JerryWorkshopDefenseManager.getPhase() != JerryWorkshopDefenseManager.Phase.WAVE_ACTIVE) return;
        if (event.getItemStack().material() != Material.SNOWBALL) return;

        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        event.setCancelled(true);
        SnowballProjectile.throwAlongLook(player, THROW_SPEED);
        int heldSlot = player.getHeldSlot();
        ItemStack currentItem = player.getInventory().getItemStack(heldSlot);
        player.getInventory().setItemStack(heldSlot,
                currentItem.amount() > 1 ? currentItem.withAmount(currentItem.amount() - 1) : ItemStack.AIR);
    }
}
