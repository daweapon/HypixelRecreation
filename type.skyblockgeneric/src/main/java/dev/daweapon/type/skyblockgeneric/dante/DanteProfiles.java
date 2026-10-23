package dev.daweapon.type.skyblockgeneric.dante;

import net.minestom.server.entity.EntityType;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;

public final class DanteProfiles {
    public static final CrimsonMobProfile GOON = CrimsonMobProfile.builder("DANTE_GOON_50", "Goon")
            .level(50).entity(EntityType.MANNEQUIN).stats(100_000, 1_000).speed(120)
            .types(MobType.HUMANOID).neverCorrupted().build();

    public static final CrimsonMobProfile SLIME_GOON = CrimsonMobProfile.builder("DANTE_SLIME_GOON_100", "Slime Goon")
            .level(100).entity(EntityType.SLIME).stats(10_000, 1_000).speed(0)
            .types(MobType.CUBIC).neverCorrupted().build();

    public static final CrimsonMobProfile DANTE = CrimsonMobProfile.builder("DANTE_BOSS", "Dante")
            .level(200).entity(EntityType.SLIME).stats(300_000, 800).speed(0)
            .types(MobType.CUBIC).neverCorrupted().build();

    private DanteProfiles() {
    }
}
