package dev.daweapon.type.skyblockgeneric.event.actions.player;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.gui.inventories.GUIAnvil;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Opens the Anvil GUI when a player interacts with an anvil block
public class ActionAnvilClick implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (!event.getBlock().name().equals("minecraft:anvil")) return;

        new GUIAnvil().open(player);
    }
}
