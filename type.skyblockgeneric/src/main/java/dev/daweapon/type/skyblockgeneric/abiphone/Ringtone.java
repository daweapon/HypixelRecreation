package dev.daweapon.type.skyblockgeneric.abiphone;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import dev.daweapon.type.generic.user.HypixelPlayer;

public enum Ringtone {
    CLASSIC("Classic", "block.note_block.bell"),
    MARIMBA("Marimba", "block.note_block.xylophone"),
    CHIME("Chime", "block.note_block.chime"),
    PLING("Pling", "block.note_block.pling"),
    BANJO("Banjo", "block.note_block.banjo"),
    FLUTE("Flute", "block.note_block.flute");

    private static final float[] PITCHES = {1.0f, 1.26f, 1.5f};

    public final String label;
    private final String sound;

    Ringtone(String label, String sound) {
        this.label = label;
        this.sound = sound;
    }

    public static Ringtone byName(String name) {
        for (Ringtone ringtone : values()) if (ringtone.name().equals(name)) return ringtone;
        return CLASSIC;
    }

    public void play(HypixelPlayer player, int ring) {
        player.playSound(Sound.sound(Key.key(sound), Sound.Source.MASTER, 1f, PITCHES[Math.floorMod(ring, PITCHES.length)]));
    }
}
