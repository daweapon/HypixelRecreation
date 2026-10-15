package dev.daweapon.type.skyblockgeneric.block.impl;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import dev.daweapon.type.skyblockgeneric.block.SkyBlockBlock;

// Callback contract for blocks that react to being right-clicked by a player.
public interface BlockInteractable {
    void onInteract(PlayerBlockInteractEvent event, SkyBlockBlock block);
}
