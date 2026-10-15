package dev.daweapon.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** Krevius and Grelius, Queen Nyx's guards outside the Throne Room; they join the Forgotten Skull meeting too. */
public abstract class ThroneGuardNPC extends CrimsonNPC {
    private final Pos wasteland;

    protected ThroneGuardNPC(String name, String texture, Pos home, Pos wasteland) {
        super(name, texture, home);
        this.wasteland = wasteland;
    }

    @Override
    protected boolean moves() {
        return true;
    }

    @Override
    protected Pos position(SkyBlockPlayer player) {
        return ActiveLine.atForgottenSkull(player) ? wasteland : home();
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        ThroneRoomGate.talkToGuards(player);
    }
}
