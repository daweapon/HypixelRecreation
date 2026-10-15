package dev.daweapon.type.skywarsgame.luckyblock.items;

import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;

public interface LuckyBlockConsumable extends LuckyBlockItem {

    void onConsume(SkywarsPlayer player);

    default boolean shouldRemoveOnConsume() {
        return true;
    }

    default int getCooldownTicks() {
        return 0;
    }

    @Override
    default boolean onUse(SkywarsPlayer holder) {
        onConsume(holder);
        return true;
    }

    @Override
    default boolean hasUseEffect() {
        return true;
    }
}
