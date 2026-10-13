package net.swofty.type.crystalhollows.events;

import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.block.Block;
import net.swofty.type.crystalhollows.quest.CrystalHollowsProgress;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionKingsScentWater implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!CrystalHollowsProgress.hasKingsScent(player.getUuid())) return;
        if (player.getInstance() == null) return;
        Block block = player.getInstance().getBlock(event.getNewPosition());
        if (!block.compare(Block.WATER)) return;
        CrystalHollowsProgress.clearKingsScent(player.getUuid());
        player.sendMessage("<c>The water washed away King Yolkar's scent!");
    }
}
