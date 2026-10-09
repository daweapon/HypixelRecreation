package net.swofty.type.skyblockgeneric.event.actions.item;

import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.item.ItemAnimation;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.ArrowComponent;
import net.swofty.type.skyblockgeneric.item.components.ItemRequirementsComponent;
import net.swofty.type.skyblockgeneric.item.handlers.bow.BowShot;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Fires shortbows instantly on right click and validates arrow availability for regular bows
public class ActionUseShortBow implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBeginItemUseEvent event) {
        if (!event.getAnimation().equals(ItemAnimation.BOW)) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        SkyBlockItem item = new SkyBlockItem(player.getItemInMainHand());

        if (BowShot.tryShortBow(player, item)) {
            event.setCancelled(true);
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
