package dev.daweapon.type.skyblockgeneric.technoblade;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import net.minestom.server.entity.EntityType;

public final class ThunderProfiles {
    public static final CrimsonMobProfile CHARGED_CREEPER = CrimsonMobProfile.builder("THUNDER_CHARGED_CREEPER_50", "Charged Creeper")
            .level(50).entity(EntityType.CREEPER).stats(17_500, 300).rewards(10, 250, 10)
            .types(MobType.HUMANOID).drops(MobDrop.always("ENCHANTED_GUNPOWDER", 1)).neverCorrupted().build();

    public static final CrimsonMobProfile SKELETON_HORSE = CrimsonMobProfile.builder("THUNDER_SKELETON_HORSE_100", "Skeleton Horse")
            .level(100).entity(EntityType.SKELETON_HORSE).stats(35_000, 600).speed(150).rewards(100, 500, 20)
            .types(MobType.UNDEAD, MobType.SKELETAL)
            .drops(MobDrop.chance("ENCHANTED_BONE", 128, 50), MobDrop.chance("ENCHANTED_BONE", 64, 50))
            .neverCorrupted().build();

    private ThunderProfiles() {
    }
}
