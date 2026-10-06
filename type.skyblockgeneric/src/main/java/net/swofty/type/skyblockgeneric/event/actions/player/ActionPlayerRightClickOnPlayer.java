package net.swofty.type.skyblockgeneric.event.actions.player;


import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.gifts.Gift;
import net.swofty.type.skyblockgeneric.gui.inventories.GUIViewPlayerProfile;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Opens a player's profile GUI when another player right-clicks them, or hands them a gift if one
// is held - the giver and the receiver both get what is inside.
public class ActionPlayerRightClickOnPlayer implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerEntityInteractEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (!(event.getTarget() instanceof SkyBlockPlayer target)) return;

        Gift gift = Gift.of(new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType());
        if (gift != null && event.getHand() == PlayerHand.MAIN) {
            player.setItemInMainHand(player.getItemInMainHand().withAmount(player.getItemInMainHand().amount() - 1));
            gift.give(player, target);
            return;
        }

        new GUIViewPlayerProfile(target).open(player);
    }
}
