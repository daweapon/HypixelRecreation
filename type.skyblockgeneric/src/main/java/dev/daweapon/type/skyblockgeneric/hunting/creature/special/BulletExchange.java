package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

final class BulletExchange {
    private final int needed;
    private int returns;

    BulletExchange(int needed) {
        this.needed = needed;
    }

    boolean returned() {
        returns++;
        return returns >= needed;
    }

    int returns() {
        return returns;
    }
}
