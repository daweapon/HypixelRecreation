package dev.daweapon.type.skyblockgeneric.event.actions.player;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.entity.mob.island.dummy.TrainingDummies;
import dev.daweapon.type.skyblockgeneric.entity.mob.island.dummy.TrainingDummyMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.island.dummy.TrainingDummyView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;

// Places Training Dummies from the item and opens a placed dummy's settings
public class ActionTrainingDummy implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void place(PlayerBlockInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        if (held.getAttributeHandler().getPotentialType() != ItemType.TRAINING_DUMMY) return;

        event.setBlockingItemUse(true);
        SkyBlockIsland island = SkyBlockIsland.forInstance(event.getInstance());
        if (!HypixelConst.isIslandServer() || island == null || island != player.getSkyBlockIsland()) {
            player.sendMessage("<c>You can only place Training Dummies on your own island!");
            return;
        }
        var direction = event.getBlockFace().toDirection();
        Pos position = new Pos(event.getBlockPosition().blockX() + direction.normalX() + 0.5,
                event.getBlockPosition().blockY() + direction.normalY(),
                event.getBlockPosition().blockZ() + direction.normalZ() + 0.5,
                player.getPosition().yaw() + 180f, 0);
        if (TrainingDummies.place(player, island, position)) player.takeItem(ItemType.TRAINING_DUMMY, 1);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void interact(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN || !(event.getTarget() instanceof TrainingDummyMob dummy)) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        SkyBlockIsland island = SkyBlockIsland.forInstance(player.getInstance());
        if (island == null || island != player.getSkyBlockIsland()) return;
        player.openView(new TrainingDummyView(island, dummy));
    }
}
