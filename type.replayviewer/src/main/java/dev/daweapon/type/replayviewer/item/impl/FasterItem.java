package dev.daweapon.type.replayviewer.item.impl;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.replayviewer.TypeReplayViewerLoader;
import dev.daweapon.type.replayviewer.item.ReplayItem;
import dev.daweapon.type.replayviewer.playback.ReplaySession;

public class FasterItem extends ReplayItem {

    public FasterItem() {
        super("faster");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.head("cf3821aab0a5abfe7f4937ac28ec8e31a3360cb515c11046ff750ae2a0a391af", "<a>Increase Speed").build();
    }


    @Override
    public void onItemInteract(PlayerInstanceEvent event) {
        ((CancellableEvent) event).setCancelled(true);
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
            ReplaySession::cycleSpeedUp,
            () -> player.sendMessage("<c>No active replay session.")
        );
    }

}
