package net.swofty.type.skyblockgeneric.event.actions.item;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.deployable.DeployableInteractions;
import net.swofty.type.skyblockgeneric.deployable.Deployables;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionDeployableUse implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onUse(PlayerUseItemEvent event) {
        use(event.getPlayer());
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onBlockUse(PlayerBlockInteractEvent event) {
        use(event.getPlayer());
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onLeave(PlayerDisconnectEvent event) {
        Deployables.removeOwned(event.getPlayer().getUuid());
    }

    private static void use(net.minestom.server.entity.Player player) {
        SkyBlockItem item = new SkyBlockItem(player.getItemInMainHand());
        if (item.isAir() || !DeployableInteractions.isDeployable(item.getAttributeHandler().getTypeAsString())) return;
        DeployableInteractions.use((SkyBlockPlayer) player, item);
    }
}
