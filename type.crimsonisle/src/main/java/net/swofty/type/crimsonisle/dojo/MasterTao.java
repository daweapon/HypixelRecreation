package net.swofty.type.crimsonisle.dojo;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** Master Tao's chat voice, usable from the challenge flow where no NPC instance is at hand. */
public final class MasterTao {
    public static final String NAME = "Master Tao";

    private MasterTao() {
    }

    public static void say(SkyBlockPlayer player, String line) {
        player.sendMessage("<e>[NPC] " + NAME + "<f>: " + line);
        player.playSound(Sound.sound(Key.key("entity.villager.celebrate"), Sound.Source.NEUTRAL, 1f, 1f), Sound.Emitter.self());
    }
}
