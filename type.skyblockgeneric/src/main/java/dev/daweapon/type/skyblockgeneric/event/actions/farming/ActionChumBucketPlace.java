package dev.daweapon.type.skyblockgeneric.event.actions.farming;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.farming.chum.ChumBuckets;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;

// Places an Empty Chum Bucket on the clicked block
public class ActionChumBucketPlace implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType() != ItemType.EMPTY_CHUM_BUCKET) return;
        event.setBlockingItemUse(true);
        var direction = event.getBlockFace().toDirection();
        Pos position = new Pos(event.getBlockPosition().blockX() + direction.normalX() + 0.5,
                event.getBlockPosition().blockY() + direction.normalY(),
                event.getBlockPosition().blockZ() + direction.normalZ() + 0.5);
        ChumBuckets.place(player, event.getInstance(), position);
    }
}
