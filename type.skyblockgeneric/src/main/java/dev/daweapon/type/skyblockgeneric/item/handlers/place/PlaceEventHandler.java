package dev.daweapon.type.skyblockgeneric.item.handlers.place;

import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@FunctionalInterface
public interface PlaceEventHandler {
    void onPlace(PlayerBlockPlaceEvent event, SkyBlockPlayer player, SkyBlockItem item);
}