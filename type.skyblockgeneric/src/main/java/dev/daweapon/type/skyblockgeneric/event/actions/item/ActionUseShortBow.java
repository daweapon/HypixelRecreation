package dev.daweapon.type.skyblockgeneric.event.actions.item;

import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.item.ItemAnimation;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.ArrowComponent;
import dev.daweapon.type.skyblockgeneric.item.components.ItemRequirementsComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.bow.BowShot;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Fires shortbows instantly on right click and validates arrow availability for regular bows
public class ActionUseShortBow implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBeginItemUseEvent event) {
        if (!event.getAnimation().equals(ItemAnimation.BOW)) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        SkyBlockItem item = new SkyBlockItem(player.getItemInMainHand());

        if (BowShot.tryShortBow(player, item)) {
            event.setCancelled(true);
            BowShot.stopDrawing(player);
            return;
        }
        if (item.hasComponent(ItemRequirementsComponent.class)
                && !item.getComponent(ItemRequirementsComponent.class).ensureCanUse(player)) {
            event.setCancelled(true);
            return;
        }
        if (player.getAllOfComponentInInventory(ArrowComponent.class).isEmpty()) event.setCancelled(true);
    }
}
