package net.swofty.type.skyblockgeneric.mythological;

import lombok.Getter;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Every Mythological creature that can be dug out of a Griffin Burrow, with the SkyBlock Wiki's
 * spawn weight, Bestiary entry and per-Griffin-rarity level, health, damage, coins, Combat XP,
 * experience orbs and drop table.
 */
@Getter
public enum MythologicalCreature {
    MINOS_HUNTER("Minos Hunter", EntityType.ZOMBIE, Material.ZOMBIE_SPAWN_EGG, Rarity.COMMON, 1000, 100, 1,
            List.of(MobType.MYTHOLOGICAL, MobType.HUMANOID),
            new BestiaryProfile("The hunter becomes the hunted.", 5, 20),
            List.of(
                    tier(Rarity.COMMON, "MINOS_HUNTER_15", 20, 4_000, 40, 6, 30, 22,
                            drop(ItemType.ANCIENT_CLAW, 1, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 1, 50),
                            drop(ItemType.HILT_OF_REVELATIONS, 1, 0.25),
                            dye(1_000_000)),
                    tier(Rarity.UNCOMMON, "MINOS_HUNTER_25", 43, 15_000, 125, 14, 64, 47,
                            drop(ItemType.ANCIENT_CLAW, 2, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 2, 50),
                            drop(ItemType.HILT_OF_REVELATIONS, 1, 0.5),
                            dye(1_000_000)),
                    tier(Rarity.RARE, "MINOS_HUNTER_60", 92, 100_000, 500, 30, 138, 101,
                            drop(ItemType.ANCIENT_CLAW, 3, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 3, 50),
                            drop(ItemType.HILT_OF_REVELATIONS, 1, 0.75),
                            dye(1_000_000)),
                    tier(Rarity.EPIC, "MINOS_HUNTER_80", 137, 350_000, 1_250, 45, 205, 150,
                            drop(ItemType.ANCIENT_CLAW, 4, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 4, 50),
                            drop(ItemType.HILT_OF_REVELATIONS, 1, 1),
                            dye(1_000_000)),
                    tier(Rarity.LEGENDARY, "MINOS_HUNTER_125", 184, 1_000_000, 2_750, 61, 276, 202,
                            drop(ItemType.ANCIENT_CLAW, 6, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 6, 50),
                            drop(ItemType.HILT_OF_REVELATIONS, 1, 1.5),
                            dye(1_000_000)),
                    tier(Rarity.MYTHIC, "MINOS_HUNTER_200", 211, 1_750_000, 4_000, 70, 316, 232,
                            drop(ItemType.ANCIENT_CLAW, 8, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 8, 50),
                            drop(ItemType.HILT_OF_REVELATIONS, 1, 2),
                            dye(1_000_000)))),
    SIAMESE_LYNXES("Siamese Lynxes", EntityType.CAT, Material.CAT_SPAWN_EGG, Rarity.COMMON, 1000, 110, 2,
            List.of(MobType.MYTHOLOGICAL, MobType.ANIMAL),
            new BestiaryProfile("Not separated at birth.", 5, 20),
            List.of(
                    tier(Rarity.COMMON, "SIAMESE_LYNX_15", 15, 2_500, 30, 5, 22, 16,
                            drop(ItemType.ANCIENT_CLAW, 1, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 1, 50),
                            drop(ItemType.CROCHET_TIGER_PLUSHIE, 1, 0.05),
                            dye(1_000_000)),
                    tier(Rarity.UNCOMMON, "SIAMESE_LYNX_25", 36, 12_500, 75, 12, 54, 39,
                            drop(ItemType.ANCIENT_CLAW, 2, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 2, 50),
                            drop(ItemType.CROCHET_TIGER_PLUSHIE, 1, 0.1),
                            dye(1_000_000)),
                    tier(Rarity.RARE, "SIAMESE_LYNX_55", 80, 75_000, 350, 26, 120, 88,
                            drop(ItemType.ANCIENT_CLAW, 3, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 3, 50),
                            drop(ItemType.CROCHET_TIGER_PLUSHIE, 1, 0.15),
                            dye(1_000_000)),
                    tier(Rarity.EPIC, "SIAMESE_LYNX_85", 124, 250_000, 1_000, 41, 186, 136,
                            drop(ItemType.ANCIENT_CLAW, 4, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 4, 50),
                            drop(ItemType.CROCHET_TIGER_PLUSHIE, 1, 0.2),
                            dye(1_000_000)),
                    tier(Rarity.LEGENDARY, "SIAMESE_LYNX_155", 171, 750_000, 2_250, 57, 256, 188,
                            drop(ItemType.ANCIENT_CLAW, 6, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 6, 50),
                            drop(ItemType.CROCHET_TIGER_PLUSHIE, 1, 0.3),
                            dye(1_000_000)),
                    tier(Rarity.MYTHIC, "SIAMESE_LYNX_200", 193, 1_250_000, 3_000, 64, 289, 212,
                            drop(ItemType.ANCIENT_CLAW, 8, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 8, 50),
                            drop(ItemType.CROCHET_TIGER_PLUSHIE, 1, 0.4),
                            dye(1_000_000)))),
    STRANDED_NYMPH("Stranded Nymph", EntityType.DROWNED, Material.DROWNED_SPAWN_EGG, Rarity.UNCOMMON, 800, 80, 1,
            List.of(MobType.MYTHOLOGICAL, MobType.AQUATIC, MobType.HUMANOID),
            new BestiaryProfile("Just add water!", 5, 20),
            List.of(
                    tier(Rarity.UNCOMMON, "STRANDED_NYMPH_40", 44, 20_000, 100, 14, 66, 48,
                            drop(ItemType.ANCIENT_CLAW, 4, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 4, 50),
                            drop(ItemType.WASHED_UP_SOUVENIR, 1, 0.2),
                            dye(1_000_000)),
                    tier(Rarity.RARE, "STRANDED_NYMPH_70", 83, 125_000, 250, 27, 124, 91,
                            drop(ItemType.ANCIENT_CLAW, 5, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 5, 50),
                            drop(ItemType.WASHED_UP_SOUVENIR, 1, 0.25),
                            dye(1_000_000)),
                    tier(Rarity.EPIC, "STRANDED_NYMPH_95", 128, 400_000, 750, 42, 192, 140,
                            drop(ItemType.ANCIENT_CLAW, 6, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 6, 50),
                            drop(ItemType.WASHED_UP_SOUVENIR, 1, 0.3),
                            dye(1_000_000)),
                    tier(Rarity.LEGENDARY, "STRANDED_NYMPH_150", 178, 1_250_000, 1_750, 59, 267, 195,
                            drop(ItemType.ANCIENT_CLAW, 8, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 8, 50),
                            drop(ItemType.WASHED_UP_SOUVENIR, 1, 0.4),
                            dye(1_000_000)),
                    tier(Rarity.MYTHIC, "STRANDED_NYMPH_250", 208, 2_500_000, 2_500, 69, 312, 228,
                            drop(ItemType.ANCIENT_CLAW, 10, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 10, 50),
                            drop(ItemType.WASHED_UP_SOUVENIR, 1, 0.5),
                            dye(1_000_000)))),
    CRETAN_BULL("Cretan Bull", EntityType.COW, Material.COW_SPAWN_EGG, Rarity.UNCOMMON, 800, 150, 1,
            List.of(MobType.MYTHOLOGICAL, MobType.ANIMAL),
            new BestiaryProfile("A very angry fella.", 5, 20),
            List.of(
                    tier(Rarity.UNCOMMON, "CRETAN_BULL_40", 47, 25_000, 100, 15, 70, 51,
                            drop(ItemType.ANCIENT_CLAW, 4, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 4, 50),
                            drop(ItemType.CRETAN_URN, 1, 0.2),
                            dye(1_000_000)),
                    tier(Rarity.RARE, "CRETAN_BULL_70", 95, 150_000, 400, 31, 142, 104,
                            drop(ItemType.ANCIENT_CLAW, 5, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 5, 50),
                            drop(ItemType.CRETAN_URN, 1, 0.25),
                            dye(1_000_000)),
                    tier(Rarity.EPIC, "CRETAN_BULL_95", 135, 500_000, 800, 45, 202, 148,
                            drop(ItemType.ANCIENT_CLAW, 6, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 6, 50),
                            drop(ItemType.CRETAN_URN, 1, 0.3),
                            dye(1_000_000)),
                    tier(Rarity.LEGENDARY, "CRETAN_BULL_150", 186, 1_500_000, 2_000, 62, 279, 204,
                            drop(ItemType.ANCIENT_CLAW, 8, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 8, 50),
                            drop(ItemType.CRETAN_URN, 1, 0.4),
                            dye(1_000_000)),
                    tier(Rarity.MYTHIC, "CRETAN_BULL_250", 216, 3_000_000, 2_750, 72, 324, 237,
                            drop(ItemType.ANCIENT_CLAW, 10, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 10, 50),
                            drop(ItemType.CRETAN_URN, 1, 0.5),
                            dye(1_000_000)))),
    HARPY("Harpy", EntityType.SKELETON, Material.SKELETON_SPAWN_EGG, Rarity.RARE, 600, 110, 1,
            List.of(MobType.MYTHOLOGICAL, MobType.HUMANOID, MobType.ANIMAL),
            new BestiaryProfile("The winged terror.", 6, 20),
            List.of(
                    tier(Rarity.RARE, "HARPY_85", 113, 200_000, 750, 37, 169, 124,
                            drop(ItemType.ANCIENT_CLAW, 7, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 7, 50),
                            drop(ItemType.ANTIQUE_REMEDIES, 1, 0.35),
                            dye(500_000)),
                    tier(Rarity.EPIC, "HARPY_110", 160, 750_000, 1_500, 53, 240, 176,
                            drop(ItemType.ANCIENT_CLAW, 8, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 8, 50),
                            drop(ItemType.ANTIQUE_REMEDIES, 1, 0.4),
                            dye(500_000)),
                    tier(Rarity.LEGENDARY, "HARPY_175", 213, 2_000_000, 3_750, 71, 319, 234,
                            drop(ItemType.ANCIENT_CLAW, 10, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 10, 50),
                            drop(ItemType.ANTIQUE_REMEDIES, 1, 0.5),
                            dye(500_000)),
                    tier(Rarity.MYTHIC, "HARPY_325", 251, 3_750_000, 6_500, 83, 376, 276,
                            drop(ItemType.ANCIENT_CLAW, 12, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 12, 50),
                            drop(ItemType.ANTIQUE_REMEDIES, 1, 0.6),
                            dye(500_000)))),
    GAIA_CONSTRUCT("Gaia Construct", EntityType.IRON_GOLEM, Material.IRON_GOLEM_SPAWN_EGG, Rarity.RARE, 600, 80, 1,
            List.of(MobType.MYTHOLOGICAL, MobType.CONSTRUCT),
            new BestiaryProfile("A construct truly connected to the Earth.", 6, 20),
            List.of(
                    tier(Rarity.RARE, "GAIA_CONSTRUCT_85", 106, 175_000, 600, 35, 159, 116,
                            drop(ItemType.ANCIENT_CLAW, 7, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 7, 50),
                            drop(ItemType.DWARF_TURTLE_SHELMET, 1, 0.35),
                            dye(500_000)),
                    tier(Rarity.EPIC, "GAIA_CONSTRUCT_140", 164, 650_000, 2_000, 54, 246, 180,
                            drop(ItemType.ANCIENT_CLAW, 8, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 8, 50),
                            drop(ItemType.DWARF_TURTLE_SHELMET, 1, 0.4),
                            dye(500_000)),
                    tier(Rarity.LEGENDARY, "GAIA_CONSTRUCT_260", 203, 1_500_000, 3_500, 67, 304, 223,
                            drop(ItemType.ANCIENT_CLAW, 10, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 10, 50),
                            drop(ItemType.DWARF_TURTLE_SHELMET, 1, 0.5),
                            dye(500_000)),
                    tier(Rarity.MYTHIC, "GAIA_CONSTRUCT_325", 240, 3_500_000, 5_000, 80, 360, 264,
                            drop(ItemType.ANCIENT_CLAW, 12, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 12, 50),
                            drop(ItemType.DWARF_TURTLE_SHELMET, 1, 0.6),
                            dye(500_000)))),
    MINOTAUR("Minotaur", EntityType.VINDICATOR, Material.VINDICATOR_SPAWN_EGG, Rarity.EPIC, 400, 110, 1,
            List.of(MobType.MYTHOLOGICAL, MobType.HUMANOID, MobType.ANIMAL),
            new BestiaryProfile("Buys his axes in bulk.", 7, 20),
            List.of(
                    tier(Rarity.EPIC, "MINOTAUR_45", 141, 1_500_000, 350, 47, 211, 155,
                            drop(ItemType.ANCIENT_CLAW, 10, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 10, 50),
                            drop(ItemType.DAEDALUS_STICK, 1, 0.04),
                            dye(250_000)),
                    tier(Rarity.LEGENDARY, "MINOTAUR_120", 195, 8_000_000, 500, 65, 292, 214,
                            drop(ItemType.ANCIENT_CLAW, 12, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 12, 50),
                            drop(ItemType.DAEDALUS_STICK, 1, 0.06),
                            dye(250_000)),
                    tier(Rarity.MYTHIC, "MINOTAUR_210", 236, 15_500_000, 1_000, 78, 354, 259,
                            drop(ItemType.ANCIENT_CLAW, 14, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 14, 50),
                            drop(ItemType.DAEDALUS_STICK, 1, 0.08),
                            dye(250_000)))),
    MINOS_CHAMPION("Minos Champion", EntityType.WITHER_SKELETON, Material.WITHER_SKELETON_SPAWN_EGG, Rarity.EPIC, 400, 110, 1,
            List.of(MobType.MYTHOLOGICAL, MobType.HUMANOID),
            new BestiaryProfile("The champion of King Minos.", 7, 20),
            List.of(
                    tier(Rarity.EPIC, "MINOS_CHAMPION_175", 175, 2_000_000, 1_000, 58, 262, 192,
                            drop(ItemType.ANCIENT_CLAW, 10, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 10, 50),
                            drop(ItemType.MINOS_RELIC, 1, 0.02),
                            dye(250_000)),
                    tier(Rarity.LEGENDARY, "MINOS_CHAMPION_310", 248, 12_500_000, 1_750, 82, 372, 272,
                            drop(ItemType.ANCIENT_CLAW, 12, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 12, 50),
                            drop(ItemType.MINOS_RELIC, 1, 0.03),
                            dye(250_000)),
                    tier(Rarity.MYTHIC, "MINOS_CHAMPION_550", 285, 25_000_000, 2_500, 95, 427, 313,
                            drop(ItemType.ANCIENT_CLAW, 14, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 14, 50),
                            drop(ItemType.MINOS_RELIC, 1, 0.04),
                            dye(250_000)))),
    SPHINX("Sphinx", EntityType.HUSK, Material.HUSK_SPAWN_EGG, Rarity.LEGENDARY, 75, 100, 1,
            List.of(MobType.MYTHOLOGICAL, MobType.HUMANOID, MobType.ELUSIVE),
            new BestiaryProfile("A Cquizical Qreature.", 7, 20),
            List.of(
                    tier(Rarity.LEGENDARY, "SPHINX_750", 315, 40_000_000, 3_500, 105, 472, 346,
                            drop(ItemType.ANCIENT_CLAW, 16, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 16, 50),
                            drop(ItemType.ENCHANTED_ANCIENT_CLAW, 1, 50),
                            drop(ItemType.BRAIN_FOOD, 1, 0.25),
                            dye(50_000)),
                    tier(Rarity.MYTHIC, "SPHINX_1250", 349, 65_000_000, 5_000, 116, 523, 383,
                            drop(ItemType.ANCIENT_CLAW, 32, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 32, 50),
                            drop(ItemType.ENCHANTED_ANCIENT_CLAW, 2, 50),
                            drop(ItemType.BRAIN_FOOD, 1, 0.5),
                            dye(50_000)))),
    MINOS_INQUISITOR("Minos Inquisitor", EntityType.EVOKER, Material.EVOKER_SPAWN_EGG, Rarity.LEGENDARY, 75, 110, 1,
            List.of(MobType.MYTHOLOGICAL, MobType.HUMANOID, MobType.ELUSIVE),
            new BestiaryProfile("The ultimate guardian of Minos' Literature.", 7, 20),
            List.of(
                    tier(Rarity.LEGENDARY, "MINOS_INQUISITOR_750", 264, 50_000_000, 700, 88, 396, 290,
                            drop(ItemType.ANCIENT_CLAW, 16, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 16, 50),
                            drop(ItemType.ENCHANTED_ANCIENT_CLAW, 1, 50),
                            unavailable("Chimera I", 1, 1),
                            dye(50_000)),
                    tier(Rarity.MYTHIC, "MINOS_INQUISITOR_1250", 290, 80_000_000, 900, 96, 435, 319,
                            drop(ItemType.ANCIENT_CLAW, 32, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 32, 50),
                            drop(ItemType.ENCHANTED_ANCIENT_CLAW, 2, 50),
                            unavailable("Chimera I", 1, 1.25),
                            dye(50_000)))),
    MANTICORE("Manticore", EntityType.RAVAGER, Material.RAVAGER_SPAWN_EGG, Rarity.MYTHIC, 15, 160, 1,
            List.of(MobType.MYTHOLOGICAL, MobType.ANIMAL, MobType.ELUSIVE),
            new BestiaryProfile("Boasts an insatiable hunger.", 8, 15),
            List.of(
                    tier(Rarity.MYTHIC, "MANTICORE_1750", 377, 125_000_000, 5_000, 125, 565, 414,
                            drop(ItemType.ANCIENT_CLAW, 48, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 48, 50),
                            drop(ItemType.ENCHANTED_ANCIENT_CLAW, 4, 50),
                            drop(ItemType.FATEFUL_STINGER, 1, 0.5),
                            drop(ItemType.MANTI_CORE, 1, 0.2),
                            dye(10_000)))),
    KING_MINOS("King Minos", EntityType.ILLUSIONER, Material.GOLDEN_HELMET, Rarity.MYTHIC, 15, 100, 1,
            List.of(MobType.MYTHOLOGICAL, MobType.HUMANOID, MobType.ELUSIVE),
            new BestiaryProfile("Rules with an iron fist and a fluffy sheep.", 8, 15),
            List.of(
                    tier(Rarity.MYTHIC, "KING_MINOS_1750", 338, 100_000_000, 2_500, 112, 507, 371,
                            drop(ItemType.ANCIENT_CLAW, 48, 100),
                            drop(ItemType.ENCHANTED_GOLD_INGOT, 48, 50),
                            drop(ItemType.ENCHANTED_ANCIENT_CLAW, 4, 50),
                            drop(ItemType.CROWN_OF_GREED, 1, 2),
                            drop(ItemType.SHIMMERING_WOOL, 1, 0.2),
                            dye(10_000))));

    public record BestiaryProfile(String description, int bracket, int maxTier) {
    }

    private final String displayName;
    private final EntityType entityType;
    private final Material icon;
    private final Rarity minimumGriffin;
    private final int weight;
    private final double speed;
    private final int groupSize;
    private final List<MobType> mobTypes;
    private final BestiaryProfile bestiary;
    private final Map<Rarity, MythologicalVariant> variants;

    MythologicalCreature(String displayName, EntityType entityType, Material icon, Rarity minimumGriffin, int weight,
                         double speed, int groupSize, List<MobType> mobTypes, BestiaryProfile bestiary,
                         List<MythologicalVariant> variants) {
        this.displayName = displayName;
        this.entityType = entityType;
        this.icon = icon;
        this.minimumGriffin = minimumGriffin;
        this.weight = weight;
        this.speed = speed;
        this.groupSize = groupSize;
        this.mobTypes = mobTypes;
        this.bestiary = bestiary;
        EnumMap<Rarity, MythologicalVariant> byRarity = new EnumMap<>(Rarity.class);
        variants.forEach(variant -> byRarity.put(variant.griffinRarity(), variant));
        this.variants = Collections.unmodifiableMap(byRarity);
    }

    public Optional<MythologicalVariant> variant(Rarity griffinRarity) {
        return Optional.ofNullable(variants.get(griffinRarity));
    }

    public List<MythologicalVariant> allVariants() {
        return List.copyOf(variants.values());
    }

    public boolean isElusive() {
        return minimumGriffin.isAtLeast(Rarity.LEGENDARY);
    }

    public double spawnWeight(double tracking) {
        return isElusive() ? weight * (1 + tracking / 100D) : weight;
    }

    public double rampagePercentPerSecond() {
        return switch (this) {
            case MINOS_INQUISITOR -> 6;
            case MINOS_CHAMPION -> 3.5;
            default -> 0;
        };
    }

    public static List<MythologicalCreature> diggableWith(Rarity griffinRarity) {
        return Arrays.stream(values())
                .filter(creature -> creature.variants.containsKey(griffinRarity))
                .toList();
    }

    public static MythologicalCreature roll(Rarity griffinRarity, double tracking, RandomGenerator random) {
        List<MythologicalCreature> pool = diggableWith(griffinRarity);
        double total = pool.stream().mapToDouble(creature -> creature.spawnWeight(tracking)).sum();
        double pick = random.nextDouble() * total;
        for (MythologicalCreature creature : pool) {
            pick -= creature.spawnWeight(tracking);
            if (pick < 0) return creature;
        }
        return pool.getLast();
    }

    private static MythologicalVariant tier(Rarity rarity, String mobId, int level, double health, double damage,
                                            int coins, int combatXp, int orbs, MythologicalDrop... drops) {
        return new MythologicalVariant(rarity, mobId, level, health, damage, coins, combatXp, orbs, List.of(drops));
    }

    private static MythologicalDrop drop(ItemType item, int amount, double chancePercent) {
        return new MythologicalDrop(item, item.getDisplayName(), amount, chancePercent);
    }

    private static MythologicalDrop dye(int oneIn) {
        return new MythologicalDrop(ItemType.DYE_MYTHOLOGICAL, "Mythological Dye", 1, 100D / oneIn);
    }

    private static MythologicalDrop unavailable(String name, int amount, double chancePercent) {
        return new MythologicalDrop(null, name, amount, chancePercent);
    }
}
