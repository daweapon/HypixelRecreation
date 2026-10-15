package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import net.minestom.server.entity.EntityType;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import static net.swofty.type.skyblockgeneric.crimson.mob.MobDrop.always;
import static net.swofty.type.skyblockgeneric.crimson.mob.MobDrop.chance;

/**
 * Crystal Hollows bosses, worms and their summons (wiki mob pages, October 2026). Worm and Bal health is the
 * number of hits it takes. Bal's hit count, every speed and the Bestiary heads (borrowed from related items)
 * are estimates; the wiki publishes none of them.
 */
public final class CrystalHollowsBossProfiles {
    public static final String CORLEONE_ID = "BOSS_CORLEONE";
    public static final String BAL_ID = "BAL";
    public static final String STONEWORM_ID = "STONEWORM";
    public static final String SCATHA_ID = "SCATHA";

    private static final String CORLEONITE_HEAD = "df52e888b00dd5f3b82d6f9b89375c9e74bbba41d465860b0c09872873cb0f51";
    private static final String BAL_HEAD = "c469ba2047122e0a2de3c7437ad3dd5d31f1ac2d27abde9f8841e1d92a8c5b75";
    private static final String WORM_HEAD = "df03ad96092f3f789902436709cdf69de6b727c121b3c2daef9ffa1ccaed186c";

    private CrystalHollowsBossProfiles() {
    }

    public static final CrimsonMobProfile CORLEONE = CrimsonMobProfile.builder(CORLEONE_ID, "Boss Corleone")
            .level(195).entity(EntityType.MANNEQUIN).stats(1_000_000, 4_000).rewards(292, 65, 214).speed(130)
            .types(MobType.HUMANOID).bestiary(7, 15, CORLEONITE_HEAD).neverCorrupted()
            .drops(always("ROUGH_RUBY_GEM", 15), chance("ROUGH_RUBY_GEM", 12, 75),
                    always("ROUGH_JADE_GEM", 18), chance("ROUGH_JADE_GEM", 14, 75), chance("ROUGH_JADE_GEM", 9, 60),
                    chance("CORLEONITE", 25))
            .build();

    public static final CrimsonMobProfile SMOG = CrimsonMobProfile.builder("SMOG", "Smog")
            .level(150).entity(EntityType.WITHER).stats(20_000, 1_500).rewards(0, 0, 0).speed(0)
            .types(MobType.WITHER, MobType.CONSTRUCT).neverCorrupted()
            .build();

    public static final CrimsonMobProfile BAL = CrimsonMobProfile.builder(BAL_ID, "Bal")
            .level(263).entity(EntityType.MAGMA_CUBE).stats(200, 100).rewards(1_972, 657, 789).speed(0)
            .types(MobType.CUBIC, MobType.INFERNAL, MobType.SHIELDED).bestiary(6, 15, BAL_HEAD).neverCorrupted()
            .drops(chance("FLAWED_RUBY_GEM", 5), chance("FLAWED_TOPAZ_GEM", 5), chance("YOGGIE", 4.5))
            .build();

    public static final CrimsonMobProfile BALD_BLAZE = CrimsonMobProfile.builder("BALD_BLAZE", "Bald Blaze")
            .level(16).entity(EntityType.BLAZE).stats(600, 150).rewards(24, 5, 17).speed(120)
            .types(MobType.INFERNAL).neverCorrupted()
            .drops(always("BLAZE_ROD", 2))
            .build();

    public static final CrimsonMobProfile FIRE_BAT = CrimsonMobProfile.builder("FIRE_BAT", "Fire Bat")
            .level(1).entity(EntityType.BAT).stats(12, 50).rewards(5, 1, 1).speed(120)
            .types(MobType.AIRBORNE, MobType.INFERNAL).neverCorrupted()
            .drops(always("GUNPOWDER", 1))
            .build();

    public static final CrimsonMobProfile STONEWORM = CrimsonMobProfile.builder(STONEWORM_ID, "Stoneworm")
            .level(119).entity(EntityType.ARMOR_STAND).stats(5, 400).rewards(178, 39, 130).speed(150)
            .types(MobType.SUBTERRANEAN, MobType.SHIELDED).bestiary(6, 15, WORM_HEAD).neverCorrupted()
            .drops(always("WORM_MEMBRANE", 1), always("ROUGH_TOPAZ_GEM", 1), chance("ROUGH_AMETHYST_GEM", 40),
                    chance("ROUGH_JADE_GEM", 20), chance("ROUGH_AMBER_GEM", 10), chance("ROUGH_SAPPHIRE_GEM", 10))
            .build();

    public static final CrimsonMobProfile SCATHA = CrimsonMobProfile.builder(SCATHA_ID, "Scatha")
            .level(181).entity(EntityType.ARMOR_STAND).stats(10, 2_500).rewards(271, 60, 199).speed(170)
            .types(MobType.SUBTERRANEAN, MobType.SHIELDED).bestiary(6, 15, WORM_HEAD).neverCorrupted()
            .drops(always("WORM_MEMBRANE", 4), always("FINE_TOPAZ_GEM", 1), chance("FINE_AMETHYST_GEM", 40),
                    chance("FINE_JADE_GEM", 20), chance("FINE_AMBER_GEM", 10), chance("FINE_SAPPHIRE_GEM", 10),
                    chance("DWARVEN_OS_BLOCK_BRAN", 10))
            .build();
}
