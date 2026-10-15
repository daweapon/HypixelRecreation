package dev.daweapon.type.skyblockgeneric.event.actions.item;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.deployable.DeployableInteractions;
import dev.daweapon.type.skyblockgeneric.deployable.Deployables;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
