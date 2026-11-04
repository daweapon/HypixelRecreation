package dev.daweapon.type.skyblockgeneric.fishing.bayou;

import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureBehaviour;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;
import net.minestom.server.entity.EntityType;

import java.util.List;

import static dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureDrop.always;
import static dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureDrop.chance;
import static dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureDrop.oneIn;
import static dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureDrop.table;

/**
 * The six Backwater Bayou Sea Creatures (wiki: Sea_Creatures/List, each creature's page). Stats, Fishing XP, catch
 * messages and drops are the wiki's; the vanilla entities are stand-ins because the wiki never names them. The
 * Alligator's throw and death roll, the Titanoboa's segments and the Bayou Sludge's Sludgelings are not built.
 */
public final class BayouSeaCreatures {
    public static final SeaCreatureProfile TRASH_GOBBLER = SeaCreatureProfile.builder("TRASH_GOBBLER")
            .displayName("Trash Gobbler").level(17).entityType(EntityType.SILVERFISH)
            .health(2_000).damage(50).speed(90)
            .behaviour(SeaCreatureBehaviour.aggressive(1.8, 20, 16))
            .mobTypes(MobType.AQUATIC, MobType.ARTHROPOD)
            .fishingXpReward(127).xpOrbs(19)
            .catchMessage("The Trash Gobbler is hungry for you!")
            .lootTable(table(always("LILY_PAD", 1, 2), chance("CAN_OF_WORMS", 1, 5),
                    oneIn("DYE_AQUAMARINE", 5_000_000)))
            .build();

    public static final SeaCreatureProfile DUMPSTER_DIVER = SeaCreatureProfile.builder("DUMPSTER_DIVER")
            .displayName("Dumpster Diver").level(21).entityType(EntityType.ZOMBIE)
            .health(2_500).damage(75).speed(90)
            .behaviour(SeaCreatureBehaviour.aggressive(1.6, 20, 16))
            .mobTypes(MobType.AQUATIC, MobType.HUMANOID)
            .fishingXpReward(210).xpOrbs(26)
            .catchMessage("A Dumpster Diver has emerged from the swamp!")
            .lootTable(table(always("LILY_PAD", 2, 4), chance("BRONZE_BOWL", 1, 2),
                    chance("ENCHANTED_LILY_PAD", 1, 1), chance("OVERFLOWING_TRASH_CAN", 1, 0.5),
                    oneIn("DYE_AQUAMARINE", 5_000_000)))
            .build();

    public static final SeaCreatureProfile BANSHEE = SeaCreatureProfile.builder("BANSHEE")
            .displayName("Banshee").level(56).entityType(EntityType.DROWNED)
            .health(17_500).damage(300).speed(95)
            .behaviour(SeaCreatureBehaviour.aggressive(1.8, 20, 18))
            .mobTypes(MobType.AQUATIC, MobType.HUMANOID)
            .fishingXpReward(700).xpOrbs(73)
            .catchMessage("The desolate wail of a Banshee breaks the silence.")
            .lootTable(table(always("LILY_PAD", 8, 16), chance("TORN_CLOTH", 1, 3),
                    chance("ENCHANTED_LILY_PAD", 1, 2), chance("CALCIFIED_HEART", 1, 0.1),
                    oneIn("DYE_AQUAMARINE", 2_500_000)))
            .build();

    public static final SeaCreatureProfile BAYOU_SLUDGE = SeaCreatureProfile.builder("BAYOU_SLUDGE")
            .displayName("Bayou Sludge").level(64).entityType(EntityType.SLIME)
            .health(20_000).damage(450).speed(80)
            .behaviour(SeaCreatureBehaviour.aggressive(1.6, 25, 16))
            .mobTypes(MobType.AQUATIC, MobType.CUBIC)
            .fishingXpReward(960).xpOrbs(96)
            .catchMessage("A swampy mass of slime emerges, the Bayou Sludge!")
            .lootTable(table(always("LILY_PAD", 16, 32), always("SLIME_BALL", 16, 32),
                    chance("ENCHANTED_LILY_PAD", 1, 4), chance("ENCHANTED_SLIME_BALL", 1, 4),
                    chance("POISON_SAMPLE", 1, 0.5), chance("ENCHANTED_SLIME_BLOCK", 1, 0.05),
                    oneIn("DYE_AQUAMARINE", 2_500_000)))
            .build();

    public static final SeaCreatureProfile ALLIGATOR = SeaCreatureProfile.builder("ALLIGATOR")
            .displayName("Alligator").level(132).entityType(EntityType.RAVAGER)
            .health(600_000).damage(600).speed(100)
            .behaviour(SeaCreatureBehaviour.aggressive(1.8, 20, 20))
            .mobTypes(MobType.AQUATIC, MobType.ANIMAL, MobType.ELUSIVE)
            .fishingXpReward(2_640).xpOrbs(332)
            .catchMessage("A long snout breaks the surface of the water. It's an Alligator!")
            .lootTable(table(always("ENCHANTED_LILY_PAD", 2, 4), always("ALLIGATOR_SKIN", 1),
                    chance("CONDENSED_LILY_PAD", 1, 0.5), oneIn("DYE_AQUAMARINE", 50_000)))
            .build();

    public static final SeaCreatureProfile TITANOBOA = SeaCreatureProfile.builder("TITANOBOA")
            .displayName("Titanoboa").level(517).entityType(EntityType.SILVERFISH)
            .health(45_000_000).damage(6_500).speed(100)
            .behaviour(SeaCreatureBehaviour.aggressive(2.0, 20, 24))
            .mobTypes(MobType.AQUATIC, MobType.ANIMAL, MobType.ELUSIVE)
            .fishingXpReward(12_925).xpOrbs(2_588)
            .catchMessage("<c><b>A massive Titanoboa surfaces. Its body stretches as far as the eye can see.")
            .lootTable(table(always("ENCHANTED_LILY_PAD", 2, 4), chance("CONDENSED_LILY_PAD", 1, 1),
                    chance("BOBBIN_SCRIPTURES", 1, 1), chance("TITANOBOA_SHED", 1, 0.2),
                    chance("SNAKE_EYES", 1, 0.11), oneIn("DYE_AQUAMARINE", 50_000)))
            .build();

    public static final List<SeaCreatureProfile> PROFILES = List.of(
            TRASH_GOBBLER, DUMPSTER_DIVER, BANSHEE, BAYOU_SLUDGE, ALLIGATOR, TITANOBOA);

    private BayouSeaCreatures() {
    }
}
