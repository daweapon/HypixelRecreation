package dev.daweapon.type.skyblockgeneric.dante;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class EnergyBall {
    public static final float LOOK_UP_PITCH = -60;
    public static final double DAMAGE_PER_MANA = 1;

    private EnergyBall() {
    }

    public static boolean ready(SkyBlockPlayer player) {
        return player.getPosition().pitch() <= LOOK_UP_PITCH && player.getMana() >= player.getMaxMana();
    }

    public static void collect(MobDante dante, List<SkyBlockPlayer> fighters) {
        for (SkyBlockPlayer player : fighters) {
            if (!ready(player)) continue;
            float mana = player.getMaxMana();
            player.setMana(0);
            player.sendMessage("<b><l>ENERGY BALL: <r><7>You are sending <b>" + StringUtility.commaify((int) mana)
                    + " mana <7>to <d>Technoblade<7>!");
            dante.rayOfHope(player, (float) (mana * DAMAGE_PER_MANA * ResistanceArmorSet.damageBonus(player)));
        }
    }
}
