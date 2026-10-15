package dev.daweapon.type.jerrysworkshop.defense;

import lombok.Getter;

/**
 * The Magma Cube mini-bosses that erupt from Mount Jerry, in spawn order, with the wiki's health.
 * Only snowballs hurt them. The wiki lists ten cubes over five waves without saying which wave
 * each belongs to, so they are taken two per wave in order.
 */
@Getter
public enum DefendCube {
    CUBIE("Cubie", 100, 1),
    MAGGIE("Maggie", 200, 2),
    CUBERT("Cubert", 300, 2),
    CUBE("Cübe", 400, 3),
    CUBETTE("Cubette", 500, 3),
    MAGMALENE("Magmalene", 600, 4),
    LUCKY_7("Lucky 7", 700, 4),
    EIGHT_BALL("8ball", 800, 5),
    MEGA_CUBE("Mega Cube", 900, 6),
    SUPER_CUBE("Super Cube", 900, 7);

    public static final int PER_WAVE = 2;

    private final String displayName;
    private final int health;
    private final int size;

    DefendCube(String displayName, int health, int size) {
        this.displayName = displayName;
        this.health = health;
        this.size = size;
    }

    public static DefendCube[] forWave(int wave) {
        DefendCube[] all = values();
        int start = Math.min((wave - 1) * PER_WAVE, all.length - PER_WAVE);
        return new DefendCube[]{all[start], all[start + 1]};
    }

    /** Pile damage per second; the wiki says only that it rises with each cube, so this is an estimate. */
    public int pileDamagePerSecond() {
        return 5 * (ordinal() + 1);
    }
}
