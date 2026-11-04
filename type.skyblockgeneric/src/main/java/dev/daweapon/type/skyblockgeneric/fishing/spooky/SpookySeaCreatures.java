package dev.daweapon.type.skyblockgeneric.fishing.spooky;

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
 * The six Spooky Festival Sea Creatures (wiki: each creature's page). Stats, Fishing XP, messages and drops are the
 * wiki's; the vanilla entities are stand-ins. The Scarecrow's wheat, the Nightmare's toss, the Werewolf's damage
 * ramp, the Phantom Fisher's rod and the Grim Reaper's scythe projectiles are not built.
 */
public final class SpookySeaCreatures {
    public static final SeaCreatureProfile JUMPIN_JACK = SeaCreatureProfile.builder("JUMPIN_JACK")
            .displayName("Jumpin' Jack").level(28).entityType(EntityType.SNOW_GOLEM)
            .health(3_500).damage(125).speed(100)
            .behaviour(SeaCreatureBehaviour.aggressive(1.8, 20, 16))
            .mobTypes(MobType.AQUATIC, MobType.SPOOKY)
            .fishingXpReward(210).xpOrbs(31)
            .catchMessage("Watch out! It's Jumpin' Jack.")
            .lootTable(table(always("PUMPKIN", 2, 4), always("GREEN_CANDY", 1, 2),
                    chance("PURPLE_CANDY", 1, 10), chance("ENCHANTED_PUMPKIN", 1, 1),
                    oneIn("DYE_MIDNIGHT", 1_000_000), oneIn("DYE_AQUAMARINE", 5_000_000)))
            .build();

    public static final SeaCreatureProfile SCARECROW = SeaCreatureProfile.builder("SCARECROW")
            .displayName("Scarecrow").level(32).entityType(EntityType.ZOMBIE)
            .health(4_500).damage(150).speed(90)
            .behaviour(SeaCreatureBehaviour.aggressive(1.6, 20, 16))
            .mobTypes(MobType.AQUATIC, MobType.SPOOKY)
            .fishingXpReward(320).xpOrbs(39)
            .catchMessage("Phew! It's only a Scarecrow.")
            .lootTable(table(always("GREEN_CANDY", 2, 4), always("PUMPKIN", 4, 8),
                    chance("PURPLE_CANDY", 1, 25), chance("ENCHANTED_PUMPKIN", 1, 2.5),
                    oneIn("DYE_MIDNIGHT", 1_000_000), oneIn("DYE_AQUAMARINE", 5_000_000)))
            .build();

    public static final SeaCreatureProfile NIGHTMARE = SeaCreatureProfile.builder("NIGHTMARE")
            .displayName("Nightmare").level(77).entityType(EntityType.SKELETON_HORSE)
            .health(35_000).damage(600).speed(110)
            .behaviour(SeaCreatureBehaviour.aggressive(1.8, 20, 18))
            .mobTypes(MobType.AQUATIC, MobType.SPOOKY, MobType.UNDEAD)
            .fishingXpReward(962).xpOrbs(100)
            .catchMessage("You hear trotting from beneath the waves, you caught a Nightmare.")
            .lootTable(table(always("GREEN_CANDY", 4, 8), always("PUMPKIN", 8, 16),
                    chance("PURPLE_CANDY", 1, 50), chance("ENCHANTED_PUMPKIN", 1, 5),
                    chance("LUCKY_HOOF", 1, 0.5),
                    oneIn("DYE_MIDNIGHT", 1_000_000), oneIn("DYE_AQUAMARINE", 2_500_000)))
            .build();

    public static final SeaCreatureProfile WEREWOLF = SeaCreatureProfile.builder("WEREWOLF")
            .displayName("Werewolf").level(69).entityType(EntityType.WOLF)
            .health(50_000).damage(250).speed(120)
            .behaviour(SeaCreatureBehaviour.aggressive(2.0, 20, 20))
            .mobTypes(MobType.AQUATIC, MobType.SPOOKY, MobType.ANIMAL)
            .fishingXpReward(1_035).xpOrbs(103)
            .catchMessage("It must be a full moon, a Werewolf appears.")
            .lootTable(table(always("PUMPKIN", 16, 32), always("PURPLE_CANDY", 1, 2),
                    always("WEREWOLF_SKIN", 1), chance("ENCHANTED_PUMPKIN", 1, 15),
                    chance("DEEP_SEA_ORB", 1, 0.1),
                    oneIn("DYE_MIDNIGHT", 1_000_000), oneIn("DYE_AQUAMARINE", 2_500_000)))
            .build();

    public static final SeaCreatureProfile PHANTOM_FISHER = SeaCreatureProfile.builder("PHANTOM_FISHER")
            .displayName("Phantom Fisher").level(157).entityType(EntityType.STRAY)
            .health(1_000_000).damage(1_000).speed(100)
            .behaviour(SeaCreatureBehaviour.aggressive(1.8, 20, 20))
            .mobTypes(MobType.AQUATIC, MobType.SPOOKY, MobType.ELUSIVE)
            .fishingXpReward(3_140).xpOrbs(393)
            .catchMessage("The spirit of a long lost Phantom Fisher has come to haunt you.")
            .lootTable(table(always("PURPLE_CANDY", 2, 4), always("ENCHANTED_PUMPKIN", 1, 2),
                    chance("PHANTOM_HOOK", 1, 1), chance("DEEP_SEA_ORB", 1, 1),
                    oneIn("DYE_MIDNIGHT", 50_000), oneIn("DYE_AQUAMARINE", 50_000)))
            .build();

    public static final SeaCreatureProfile GRIM_REAPER = SeaCreatureProfile.builder("GRIM_REAPER")
            .displayName("Grim Reaper").level(312).entityType(EntityType.WITHER_SKELETON)
            .health(3_000_000).damage(2_100).speed(100)
            .behaviour(SeaCreatureBehaviour.aggressive(1.8, 20, 22))
            .mobTypes(MobType.AQUATIC, MobType.SPOOKY, MobType.ELUSIVE)
            .fishingXpReward(7_800).xpOrbs(1_564)
            .catchMessage("This can't be! The manifestation of death himself!")
            .lootTable(table(always("ENCHANTED_PUMPKIN", 4, 8), always("PURPLE_CANDY", 8, 16),
                    always("SOUL_FRAGMENT", 1), chance("PET_ITEM_VAMPIRE_FANG", 1, 10),
                    chance("POLISHED_PUMPKIN", 1, 2), chance("BOBBIN_SCRIPTURES", 1, 1),
                    chance("SHREDDED_LINE", 1, 1), chance("DEEP_SEA_ORB", 1, 1),
                    oneIn("DYE_MIDNIGHT", 50_000), oneIn("DYE_AQUAMARINE", 50_000)))
            .build();

    public static final List<SeaCreatureProfile> PROFILES = List.of(
            JUMPIN_JACK, SCARECROW, NIGHTMARE, WEREWOLF, PHANTOM_FISHER, GRIM_REAPER);

    private SpookySeaCreatures() {
    }
}
