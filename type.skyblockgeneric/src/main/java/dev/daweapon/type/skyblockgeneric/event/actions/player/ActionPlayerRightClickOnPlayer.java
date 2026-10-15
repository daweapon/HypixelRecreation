package dev.daweapon.type.skyblockgeneric.event.actions.player;


import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.gifts.Gift;
import dev.daweapon.type.skyblockgeneric.gifts.PartyGift;
import dev.daweapon.type.skyblockgeneric.gifts.PlacedGift;
import dev.daweapon.type.skyblockgeneric.gui.inventories.GUIViewPlayerProfile;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.trade.TradeManager;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Opens a player's profile GUI when another player right-clicks them, sends a trade request when
// they shift-right-click, or places a gift for them to open if one is held.
public class ActionPlayerRightClickOnPlayer implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerEntityInteractEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (event.getHand() != PlayerHand.MAIN) return;

        ItemType held = new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType();
        Gift gift = Gift.of(held);
        boolean partyGift = PartyGift.isPartyGift(held);
        if (!(event.getTarget() instanceof SkyBlockPlayer target)) {
            if ((gift != null || partyGift) && event.getTarget() instanceof SkyBlockMob) {
                player.sendMessage("<c>Click a player to gift them! This isn't a player!");
            }
            return;
        }

        if ((gift != null || partyGift) && target.isIronman() && !player.isIronman()) {
            player.sendMessage(Text.of("{} <c>is on an Ironman profile and can only receive gifts from other Ironman players!",
                    target.getColouredName()));
            return;
        }

        if (partyGift) {
            player.setItemInMainHand(player.getItemInMainHand().withAmount(player.getItemInMainHand().amount() - 1));
            PartyGift.give(player, target);
            return;
        }

        if (gift != null) {
            player.setItemInMainHand(player.getItemInMainHand().withAmount(player.getItemInMainHand().amount() - 1));
            PlacedGift.place(gift, player, target);
            return;
        }

        if (player.isSneaking()) {
            TradeManager.request(player, target);
            return;
        }

        new GUIViewPlayerProfile(target).open(player);
    }
}
