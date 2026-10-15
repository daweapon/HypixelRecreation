package dev.daweapon.type.jerrysworkshop.defense;

import lombok.Getter;
import net.minestom.server.coordinate.Pos;

/**
 * One of the five gift piles the Magma Cubes march on. A pile lost during a wave pays nothing for
 * that wave. The wiki gives neither coordinates nor health for the piles; they stand where the
 * island's five Jerrys do, and their health is an estimate.
 */
@Getter
public enum GiftPile {
    A('Ⓐ', new Pos(-33.5, 76, 77.5), 3, 0, 0),
    B('Ⓑ', new Pos(6.5, 76, 72.5), 3, 0, 0),
    C('Ⓒ', new Pos(-14.5, 76, 49.5), 1, 2, 0),
    D('Ⓓ', new Pos(18.5, 76, 31.5), 0, 2, 1),
    E('Ⓔ', new Pos(-11.5, 76, 13.5), 0, 2, 1);

    public static final int MAX_HEALTH = 500;

    private final char symbol;
    private final Pos position;
    private final int whiteGifts;
    private final int greenGifts;
    private final int redGifts;
    private int health = MAX_HEALTH;
    private boolean targeted;

    GiftPile(char symbol, Pos position, int whiteGifts, int greenGifts, int redGifts) {
        this.symbol = symbol;
        this.position = position;
        this.whiteGifts = whiteGifts;
        this.greenGifts = greenGifts;
        this.redGifts = redGifts;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public void damage(int amount) {
        health = Math.max(0, health - amount);
    }

    public void setTargeted(boolean targeted) {
        this.targeted = targeted;
    }

    public static void restoreAll() {
        for (GiftPile pile : values()) {
            pile.health = MAX_HEALTH;
            pile.targeted = false;
        }
    }

    public String status() {
        if (isDestroyed()) return "<c>Ⓧ";
        if (targeted) return "<e>" + symbol;
        if (health < MAX_HEALTH) return "<f>" + symbol;
        return "<a>" + symbol;
    }
}
