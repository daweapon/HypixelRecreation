package dev.daweapon.type.theend.dragon;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public final class DragonSidebar {
    private DragonSidebar() {
    }

    public static String hpLine(double health) {
        return "<f>Dragon HP: <a>" + StringUtility.commaify(Math.ceil(health)) + " <c>♥";
    }

    public static String damageLine(double damage) {
        return "<f>Your Damage: <c>" + String.format(Locale.US, "%,.1f", damage);
    }

    public static @Nullable List<String> lines(SkyBlockPlayer player) {
        EnderDragonMob dragon = EnderDragonFight.dragon();
        if (dragon == null || dragon.isRemoved() || dragon.isDead() || dragon.getInstance() != player.getInstance()) return null;
        return List.of(hpLine(dragon.getHealth()), damageLine(dragon.damageDealtBy(player.getUuid())));
    }
}
