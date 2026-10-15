package dev.daweapon.type.skywarsgame.events;

import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.luckyblock.items.LuckyBlockItemHandler;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class ActionGameCustomItems implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerUseItemOnBlockEvent event) {
        TypeSkywarsGameLoader.getItemHandler().onItemUseOnBlock(event);

        if (event.getPlayer() instanceof SkywarsPlayer player) {
            LuckyBlockItemHandler.processItemUse(player, event.getItemStack());
        }
    }

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerUseItemEvent event) {
        TypeSkywarsGameLoader.getItemHandler().onItemUse(event);

        if (event.getPlayer() instanceof SkywarsPlayer player) {
            LuckyBlockItemHandler.processItemUse(player, event.getItemStack());
        }
    }

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockPlaceEvent event) {
        TypeSkywarsGameLoader.getItemHandler().onBlockPlace(event);
    }
}
