package dev.daweapon.type.skyblockgeneric.fishing.hotspot;

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
 * The water Hotspot pool's own Sea Creatures (wiki: Fishing_Hotspots and each creature's page); the pool's Inkling is
 * this server's Night Squid. Stats, Fishing XP, messages and drops are the wiki's; the vanilla entities are stand-ins.
 * Frog Man's bow, the Manta Ray's belly-flop and the Wiki Tiki's totems, fire ring and magic resistance are not built.
 */
public final class HotspotSeaCreatures {
    public static final SeaCreatureProfile FROG_MAN = SeaCreatureProfile.builder("FROG_MAN")
            .displayName("Frog Man").level(24).entityType(EntityType.FROG)
            .health(2_500).damage(100).speed(100)
            .behaviour(SeaCreatureBehaviour.aggressive(1.8, 20, 16))
            .mobTypes(MobType.AQUATIC, MobType.ANIMAL, MobType.HUMANOID)
            .fishingXpReward(180).xpOrbs(26)
            .catchMessage("Is it a frog? Is it a man? Well, yes, sorta, IT'S FROG MAN!!!!!!")
            .lootTable(table(always("HALF_EATEN_MUSHROOM", 1), always("CLAY_BALL", 1, 2),
                    oneIn("DYE_AQUAMARINE", 5_000_000)))
            .build();

    public static final SeaCreatureProfile SNAPPING_TURTLE = SeaCreatureProfile.builder("SNAPPING_TURTLE")
            .displayName("Snapping Turtle").level(60).entityType(EntityType.TURTLE)
            .health(35_000).damage(200).speed(70)
            .behaviour(SeaCreatureBehaviour.aggressive(1.4, 25, 16))
            .mobTypes(MobType.AQUATIC, MobType.ANIMAL)
            .fishingXpReward(750).xpOrbs(78)
            .catchMessage("A Snapping Turtle is coming your way, and it's ANGRY!")
            .lootTable(table(always("CLAY_BALL", 8, 16), chance("ENCHANTED_CLAY_BALL", 1, 2),
                    chance("BROKEN_RADAR", 1, 1), chance("EDIBLE_SEAWEED", 1, 0.1),
                    chance("PET_ITEM_FISHING_SKILL_BOOST_RARE", 1, 0.05), oneIn("DYE_AQUAMARINE", 2_500_000)))
            .build();

    public static final SeaCreatureProfile MANTA_RAY = SeaCreatureProfile.builder("MANTA_RAY")
            .displayName("Manta Ray").level(124).entityType(EntityType.GLOW_SQUID)
            .health(500_000).damage(500).speed(90)
            .behaviour(SeaCreatureBehaviour.aggressive(1.6, 20, 20))
            .mobTypes(MobType.AQUATIC, MobType.ANIMAL)
            .fishingXpReward(1_860).xpOrbs(186)
            .catchMessage("A majestic creature rises from the water. It's a Manta Ray.")
            .lootTable(table(always("CLAY_BALL", 16, 32), chance("ENCHANTED_CLAY_BALL", 1, 10),
                    chance("ENCHANTED_CLAY_BLOCK", 1, 0.1), chance("PET_ITEM_FISHING_SKILL_BOOST_EPIC", 1, 0.05),
                    oneIn("DYE_AQUAMARINE", 2_500_000)))
            .build();

    public static final SeaCreatureProfile BLUE_RINGED_OCTOPUS = SeaCreatureProfile.builder("BLUE_RINGED_OCTOPUS")
            .displayName("Blue Ringed Octopus").level(237).entityType(EntityType.SQUID)
            .health(4_000_000).damage(4_000).speed(90)
            .behaviour(SeaCreatureBehaviour.aggressive(1.8, 20, 20))
            .mobTypes(MobType.AQUATIC, MobType.ANIMAL, MobType.ELUSIVE)
            .fishingXpReward(4_740).xpOrbs(594)
            .catchMessage("A garish set of tentacles arise. It's a Blue Ringed Octopus!")
            .lootTable(table(always("ENCHANTED_CLAY_BALL", 2, 4), always("BLUE_RING", 1),
                    chance("ENCHANTED_CLAY_BLOCK", 1, 0.5), chance("OCTOPUS_TENDRIL", 1, 0.1),
                    oneIn("DYE_AQUAMARINE", 50_000)))
            .build();

    public static final SeaCreatureProfile WIKI_TIKI = SeaCreatureProfile.builder("WIKI_TIKI")
            .displayName("Wiki Tiki").level(592).entityType(EntityType.IRON_GOLEM)
            .health(75_000_000).damage(12_500).speed(80)
            .behaviour(SeaCreatureBehaviour.aggressive(1.4, 25, 24))
            .mobTypes(MobType.AQUATIC, MobType.CONSTRUCT, MobType.ELUSIVE)
            .fishingXpReward(14_800).xpOrbs(2_964)
            .catchMessage("<c><b>The water bubbles and froths. A massive form emerges- you have disturbed the Wiki Tiki! You shall pay the price.")
            .lootTable(table(always("ENCHANTED_JUNGLE_LOG", 8, 16), always("ENCHANTED_CLAY_BALL", 8, 16),
                    chance("ENCHANTED_CLAY_BLOCK", 1, 1), chance("BOBBIN_SCRIPTURES", 1, 1),
                    chance("TROUBLED_BUBBLE", 1, 0.2), chance("TIKI_MASK", 1, 0.1),
                    oneIn("DYE_AQUAMARINE", 50_000)))
            .build();

    public static final List<SeaCreatureProfile> PROFILES = List.of(
            FROG_MAN, SNAPPING_TURTLE, MANTA_RAY, BLUE_RINGED_OCTOPUS, WIKI_TIKI);

    private HotspotSeaCreatures() {
    }
}
