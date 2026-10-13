package net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.minestom.server.entity.EntityType;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

/**
 * Canonical Hypixel sea creature profiles. Each constant is the entire
 * declarative spec for one creature: stats, AI flavour, fishing rewards.
 * Adding a new creature is one builder chain, not a new class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SeaCreatureProfiles {

    /* ---------- water ---------- */

    public static final SeaCreatureProfile SQUID = SeaCreatureProfile.builder("SQUID")
            .displayName("Squid")
            .level(1)
            .entityType(EntityType.SQUID)
            .health(145D).damage(5D).speed(30D)
            .behaviour(SeaCreatureBehaviour.passive(8))
            .mobTypes(MobType.AQUATIC)
            .fishingXpReward(20).xpOrbs(4)
            .build();

    public static final SeaCreatureProfile SEA_WALKER = SeaCreatureProfile.builder("SEA_WALKER")
            .displayName("Sea Walker")
            .level(4)
            .entityType(EntityType.ZOMBIE)
            .health(240D).damage(25D).speed(70D)
            .behaviour(SeaCreatureBehaviour.aggressive(1.6, 20, 16))
            .mobTypes(MobType.AQUATIC, MobType.UNDEAD)
            .damageCooldownMs(400)
            .fishingXpReward(40).xpOrbs(6)
            .build();

    public static final SeaCreatureProfile NIGHT_SQUID = SeaCreatureProfile.builder("NIGHT_SQUID")
            .displayName("Night Squid")
            .level(6)
            .entityType(EntityType.GLOW_SQUID)
            .health(400D).damage(15D).speed(30D)
            .behaviour(SeaCreatureBehaviour.passive(6))
            .mobTypes(MobType.AQUATIC)
            .fishingXpReward(80).xpOrbs(8)
            .build();

    public static final SeaCreatureProfile SEA_GUARDIAN = SeaCreatureProfile.builder("SEA_GUARDIAN")
            .displayName("Sea Guardian")
            .level(8)
            .entityType(EntityType.GUARDIAN)
            .health(600D).damage(40D).speed(60D)
            .behaviour(SeaCreatureBehaviour.aggressive(2.0, 25, 18))
            .mobTypes(MobType.AQUATIC)
            .damageCooldownMs(400)
            .fishingXpReward(120).xpOrbs(10)
            .build();

    public static final SeaCreatureProfile SEA_WITCH = SeaCreatureProfile.builder("SEA_WITCH")
            .displayName("Sea Witch")
            .level(10)
            .entityType(EntityType.WITCH)
            .health(800D).damage(60D).speed(60D)
            .behaviour(SeaCreatureBehaviour.aggressive(1.5, 30, 16))
            .mobTypes(MobType.AQUATIC, MobType.ARCANE)
            .damageCooldownMs(450)
            .fishingXpReward(150).xpOrbs(12)
            .lootTable(drops(
                    new SkyBlockLootTable.LootRecord(ItemType.FAIRY_HELMET, 1, 1.25),
                    new SkyBlockLootTable.LootRecord(ItemType.FAIRY_CHESTPLATE, 1, 1.25),
                    new SkyBlockLootTable.LootRecord(ItemType.FAIRY_LEGGINGS, 1, 1.25),
                    new SkyBlockLootTable.LootRecord(ItemType.FAIRY_BOOTS, 1, 1.25)))
            .build();

    public static final SeaCreatureProfile SEA_ARCHER = SeaCreatureProfile.builder("SEA_ARCHER")
            .displayName("Sea Archer")
            .level(15)
            .entityType(EntityType.SKELETON)
            .health(1000D).damage(80D).speed(75D)
            .behaviour(SeaCreatureBehaviour.aggressive(1.7, 25, 20))
            .mobTypes(MobType.AQUATIC, MobType.SKELETAL)
            .damageCooldownMs(400)
            .fishingXpReward(200).xpOrbs(14)
            .build();

    public static final SeaCreatureProfile MONSTER_OF_THE_DEEP = SeaCreatureProfile.builder("MONSTER_OF_THE_DEEP")
            .displayName("Monster of the Deep")
            .level(20)
            .entityType(EntityType.DROWNED)
            .health(1500D).damage(120D).speed(80D)
            .behaviour(SeaCreatureBehaviour.aggressive(2.0, 22, 20))
            .mobTypes(MobType.AQUATIC, MobType.UNDEAD)
            .damageCooldownMs(400)
            .fishingXpReward(300).xpOrbs(16)
            .build();

    public static final SeaCreatureProfile CATFISH = SeaCreatureProfile.builder("CATFISH")
            .displayName("Catfish")
            .level(24)
            .entityType(EntityType.OCELOT)
            .health(2000D).damage(150D).speed(90D)
            .behaviour(SeaCreatureBehaviour.aggressive(2.2, 18, 18))
            .mobTypes(MobType.AQUATIC, MobType.ANIMAL)
            .damageCooldownMs(350)
            .fishingXpReward(400).xpOrbs(18)
            .build();

    public static final SeaCreatureProfile CARROT_KING = SeaCreatureProfile.builder("CARROT_KING")
            .displayName("Carrot King")
            .level(26)
            .entityType(EntityType.RABBIT)
            .health(2500D).damage(170D).speed(95D)
            .behaviour(SeaCreatureBehaviour.aggressive(2.0, 25, 16))
            .mobTypes(MobType.AQUATIC, MobType.ANIMAL)
            .damageCooldownMs(400)
            .fishingXpReward(500).xpOrbs(20)
            .lootTable(drops(new SkyBlockLootTable.LootRecord(ItemType.PET_ITEM_LUCKY_CLOVER_DROP, 1, 0.66)))
            .build();

    public static final SeaCreatureProfile SEA_LEECH = SeaCreatureProfile.builder("SEA_LEECH")
            .displayName("Sea Leech")
            .level(30)
            .entityType(EntityType.SILVERFISH)
            .health(3000D).damage(200D).speed(110D)
            .behaviour(SeaCreatureBehaviour.aggressive(2.4, 18, 14))
            .mobTypes(MobType.AQUATIC, MobType.ARTHROPOD)
            .damageCooldownMs(350)
            .fishingXpReward(650).xpOrbs(22)
            .build();

    public static final SeaCreatureProfile GUARDIAN_DEFENDER = SeaCreatureProfile.builder("GUARDIAN_DEFENDER")
            .displayName("Guardian Defender")
            .level(35)
            .entityType(EntityType.GUARDIAN)
            .health(5000D).damage(250D).speed(70D)
            .behaviour(SeaCreatureBehaviour.aggressive(1.8, 30, 20))
            .mobTypes(MobType.AQUATIC, MobType.SHIELDED)
            .damageCooldownMs(450)
            .fishingXpReward(900).xpOrbs(26)
            .build();

    public static final SeaCreatureProfile WATER_HYDRA = SeaCreatureProfile.builder("WATER_HYDRA")
            .displayName("Water Hydra")
            .level(60)
            .entityType(EntityType.WITHER)
            .health(50000D).damage(800D).speed(80D)
            .behaviour(SeaCreatureBehaviour.aggressive(2.5, 20, 28))
            .mobTypes(MobType.AQUATIC, MobType.ELUSIVE)
            .damageCooldownMs(300)
            .fishingXpReward(7500).xpOrbs(60)
            .lootTable(drops(new SkyBlockLootTable.LootRecord(ItemType.FISH_AFFINITY_TALISMAN, 1, 30),
                    new SkyBlockLootTable.LootRecord(ItemType.WATER_HYDRA_HEAD, 1, 14)))
            .build();

    public static final SeaCreatureProfile THE_SEA_EMPEROR = SeaCreatureProfile.builder("THE_SEA_EMPEROR")
            .displayName("The Sea Emperor")
            .level(80)
            .entityType(EntityType.ELDER_GUARDIAN)
            .health(150000D).damage(1500D).speed(60D)
            .behaviour(SeaCreatureBehaviour.aggressive(2.0, 30, 32))
            .mobTypes(MobType.AQUATIC, MobType.MYTHOLOGICAL, MobType.ELUSIVE)
            .damageCooldownMs(400)
            .fishingXpReward(20000).xpOrbs(120)
            .build();

    /* ---------- lava (Crystal Hollows; Crimson Isle creatures are LavaSeaCreature) ---------- */

    public static final SeaCreatureProfile LAVA_BLAZE = SeaCreatureProfile.builder("LAVA_BLAZE")
            .displayName("Lava Blaze")
            .level(123)
            .entityType(EntityType.BLAZE)
            .health(400_000D).damage(600D).speed(90D)
            .behaviour(SeaCreatureBehaviour.aggressive(2.5, 20, 20))
            .mobTypes(MobType.MAGMATIC)
            .damageCooldownMs(350)
            .fishingXpReward(500).xpOrbs(185)
            .lootTable(drops(
                    new SkyBlockLootTable.LootRecord(ItemType.ROUGH_TOPAZ_GEM, 28, 100),
                    new SkyBlockLootTable.LootRecord(ItemType.BLAZE_ROD, 5, 100),
                    new SkyBlockLootTable.LootRecord(ItemType.BLAZEN_SPHERE, 1, 1),
                    new SkyBlockLootTable.LootRecord(ItemType.ETERNAL_FLAME_RING, 1, 0.5),
                    new SkyBlockLootTable.LootRecord(ItemType.MAGMA_CORE, 1, 0.5),
                    new SkyBlockLootTable.LootRecord(ItemType.DYE_CARMINE, 1, 0.00004)))
            .build();

    public static final SeaCreatureProfile LAVA_PIGMAN = SeaCreatureProfile.builder("LAVA_PIGMAN")
            .displayName("Lava Pigman")
            .level(129)
            .entityType(EntityType.ZOMBIFIED_PIGLIN)
            .health(450_000D).damage(700D).speed(90D)
            .behaviour(SeaCreatureBehaviour.aggressive(2.0, 20, 20))
            .mobTypes(MobType.MAGMATIC)
            .damageCooldownMs(350)
            .fishingXpReward(500).xpOrbs(194)
            .lootTable(drops(
                    new SkyBlockLootTable.LootRecord(ItemType.ROUGH_TOPAZ_GEM, 28, 100),
                    new SkyBlockLootTable.LootRecord(ItemType.ETERNAL_FLAME_RING, 1, 0.5),
                    new SkyBlockLootTable.LootRecord(ItemType.MAGMA_CORE, 1, 0.5),
                    new SkyBlockLootTable.LootRecord(ItemType.DYE_CARMINE, 1, 0.00004)))
            .build();

    public static final SeaCreatureProfile FLAMING_WORM = SeaCreatureProfile.builder("FLAMING_WORM")
            .displayName("Flaming Worm")
            .level(92)
            .entityType(EntityType.MAGMA_CUBE)
            .health(100_000D).damage(500D).speed(80D)
            .behaviour(SeaCreatureBehaviour.aggressive(2.0, 20, 20))
            .mobTypes(MobType.MAGMATIC)
            .damageCooldownMs(350)
            .fishingXpReward(1150).xpOrbs(119)
            .lootTable(drops(
                    new SkyBlockLootTable.LootRecord(ItemType.ROUGH_SAPPHIRE_GEM, 28, 100),
                    new SkyBlockLootTable.LootRecord(ItemType.WORM_MEMBRANE, 1, 25),
                    new SkyBlockLootTable.LootRecord(ItemType.ETERNAL_FLAME_RING, 1, 0.5),
                    new SkyBlockLootTable.LootRecord(ItemType.DYE_CARMINE, 1, 0.00004)))
            .build();

    public static final SeaCreatureProfile WATER_WORM = SeaCreatureProfile.builder("WATER_WORM")
            .displayName("Water Worm")
            .level(71)
            .entityType(EntityType.SILVERFISH)
            .health(50_000D).damage(300D).speed(80D)
            .behaviour(SeaCreatureBehaviour.aggressive(2.0, 20, 20))
            .mobTypes(MobType.AQUATIC)
            .damageCooldownMs(350)
            .fishingXpReward(887).xpOrbs(93)
            .lootTable(drops(
                    new SkyBlockLootTable.LootRecord(ItemType.HARD_STONE, 10, 100),
                    new SkyBlockLootTable.LootRecord(ItemType.ROUGH_AMBER_GEM, 13, 100),
                    new SkyBlockLootTable.LootRecord(ItemType.WORM_MEMBRANE, 1, 15),
                    new SkyBlockLootTable.LootRecord(ItemType.DYE_AQUAMARINE, 1, 0.00004)))
            .build();

    public static final SeaCreatureProfile POISONED_WATER_WORM = SeaCreatureProfile.builder("POISONED_WATER_WORM")
            .displayName("Poisoned Water Worm")
            .level(76)
            .entityType(EntityType.SILVERFISH)
            .health(75_000D).damage(400D).speed(80D)
            .behaviour(SeaCreatureBehaviour.aggressive(2.0, 20, 20))
            .mobTypes(MobType.AQUATIC)
            .damageCooldownMs(350)
            .fishingXpReward(1000).xpOrbs(100)
            .lootTable(drops(
                    new SkyBlockLootTable.LootRecord(ItemType.HARD_STONE, 10, 100),
                    new SkyBlockLootTable.LootRecord(ItemType.ROUGH_AMBER_GEM, 13, 100),
                    new SkyBlockLootTable.LootRecord(ItemType.WORM_MEMBRANE, 1, 15),
                    new SkyBlockLootTable.LootRecord(ItemType.DYE_AQUAMARINE, 1, 0.00004)))
            .build();

    public static final SeaCreatureProfile ABYSSAL_MINER = SeaCreatureProfile.builder("ABYSSAL_MINER")
            .displayName("Abyssal Miner")
            .level(182)
            .entityType(EntityType.ZOMBIE)
            .health(2_000_000D).damage(1_300D).speed(90D)
            .behaviour(SeaCreatureBehaviour.aggressive(1.6, 20, 20))
            .mobTypes(MobType.AQUATIC, MobType.HUMANOID)
            .damageCooldownMs(350)
            .fishingXpReward(3640).xpOrbs(457)
            .catchMessage("An Abyssal Miner breaks out of the water!")
            .lootTable(drops(
                    new SkyBlockLootTable.LootRecord(ItemType.ROUGH_AMETHYST_GEM, 20, 100),
                    new SkyBlockLootTable.LootRecord(ItemType.ROUGH_JADE_GEM, 20, 100),
                    new SkyBlockLootTable.LootRecord(ItemType.ROUGH_SAPPHIRE_GEM, 20, 100),
                    new SkyBlockLootTable.LootRecord(ItemType.QUARTZ, 10, 100),
                    new SkyBlockLootTable.LootRecord(ItemType.FLAWED_AMETHYST_GEM, 1, 20),
                    new SkyBlockLootTable.LootRecord(ItemType.FLAWED_JADE_GEM, 1, 20),
                    new SkyBlockLootTable.LootRecord(ItemType.FLAWED_SAPPHIRE_GEM, 1, 20),
                    new SkyBlockLootTable.LootRecord(ItemType.ENCHANTED_BOOK, 1, 2),
                    new SkyBlockLootTable.LootRecord(ItemType.DYE_AQUAMARINE, 1, 0.002)))
            .droppedBook(EnchantmentType.PISCARY, 6)
            .build();

    public static final List<SeaCreatureProfile> CANONICAL = List.of(
            // Water
            SQUID, SEA_WALKER, NIGHT_SQUID, SEA_GUARDIAN,
            SEA_WITCH, SEA_ARCHER, MONSTER_OF_THE_DEEP, CATFISH, CARROT_KING,
            SEA_LEECH, GUARDIAN_DEFENDER, WATER_HYDRA, THE_SEA_EMPEROR,
            WATER_WORM, POISONED_WATER_WORM, ABYSSAL_MINER,
            // Lava
            LAVA_BLAZE, LAVA_PIGMAN, FLAMING_WORM
    );

    private static SkyBlockLootTable drops(SkyBlockLootTable.LootRecord... records) {
        return new SkyBlockLootTable() {
            @Override
            public List<LootRecord> getLootTable() {
                return List.of(records);
            }

            @Override
            public CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }
}
