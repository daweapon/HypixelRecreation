package dev.daweapon.type.skyblockgeneric.item.handlers.bow;

import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@FunctionalInterface
public interface BowHandler {
    void onShoot(SkyBlockPlayer player, SkyBlockItem item, double power);
}
