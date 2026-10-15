package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** The blast heard by everyone nearby when an ability detonates. */
public final class ExplosionSound {
    private ExplosionSound() {
    }

    public static void play(SkyBlockPlayer player, Point at) {
        if (player.getInstance() == null) return;
        player.getInstance().playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.PLAYER, 2f, 0.9f),
                at.x(), at.y(), at.z());
        player.getInstance().playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.PLAYER, 1.2f, 0.6f),
                at.x(), at.y(), at.z());
    }
}
