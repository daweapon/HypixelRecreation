package dev.daweapon.type.skyblockgeneric.greatspook.mob;

import net.minestom.server.entity.EntityType;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.greatspook.PrimalFearRules;

import java.util.List;

public final class PrimalProfiles {
    public static final int LEVEL = 29;
    public static final String DARK_CANDY = "DARK_CANDY";
    public static final String GREEN_CANDY = "GREEN_CANDY";
    public static final String PURPLE_CANDY = "PURPLE_CANDY";
    public static final double PURPLE_CHANCE_PERCENT = 35;
    private static final double SPEED = 55;

    private PrimalProfiles() {
    }

    public static List<MobDrop> drops() {
        return List.of(
                MobDrop.always(DARK_CANDY, 1).noMagicFind(),
                MobDrop.always(GREEN_CANDY, 1).noMagicFind(),
                MobDrop.chance(GREEN_CANDY, 1, 50).noMagicFind(),
                MobDrop.chance(PURPLE_CANDY, 1, PURPLE_CHANCE_PERCENT).noMagicFind());
    }

    public static CrimsonMobProfile of(String id, String displayName) {
        return CrimsonMobProfile.builder(id, displayName)
                .level(LEVEL)
                .entity(EntityType.ZOMBIE)
                .stats(PrimalFearRules.HEALTH, 1)
                .speed(SPEED)
                .rewards(43, 9, 31)
                .types(MobType.SPOOKY, MobType.SHIELDED)
                .drops(drops().toArray(MobDrop[]::new))
                .neverCorrupted()
                .build();
    }
}
