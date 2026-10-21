package dev.daweapon.type.skyblockgeneric.fishing.galatea;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureBehaviour;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.minestom.server.entity.EntityType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The seven Sea Creatures fished up on Moonglade Marsh (wiki: Sea Creatures/List/Moonglade Marsh and each creature's page).
 * Stats, drops, XP and messages come from those pages; entity models, speeds and AI are estimates.
 */
public final class GalateaSeaCreatures {
    public static final String BOGGED = "BOGGED";
    public static final String WETWING = "WETWING";
    public static final String TADGANG = "TADGANG";
    public static final String ENT = "ENT";
    public static final String THE_LOCH_EMPEROR = "THE_LOCH_EMPEROR";
    public static final String NESSIE = "NESSIE";
    public static final String STRIDERSURFER = "STRIDERSURFER";

    public static final List<SeaCreatureProfile> PROFILES = List.of(
            profile(BOGGED, "Bogged", 25, EntityType.BOGGED, 3_000, 100, 187, 28,
                    "You've hooked a Bogged!",
                    SeaCreatureBehaviour.aggressive(1.5, 20, 16), List.of(MobType.AQUATIC, MobType.SKELETAL),
                    Drop.always("SEA_LUMIES", 1), Drop.chance("SEA_LUMIES", 1, 50),
                    Drop.always("MANGROVE_LOG", 8), Drop.chance("MANGROVE_LOG", 8, 50),
                    Drop.oneIn("DYE_AQUAMARINE", 5_000_000)),
            profile(WETWING, "Wetwing", 46, EntityType.CHICKEN, 8_000, 300, 460, 55,
                    "Look! A Wetwing emerges!",
                    SeaCreatureBehaviour.aggressive(1.6, 20, 16),
                    List.of(MobType.AQUATIC, MobType.UNDEAD, MobType.ANIMAL),
                    Drop.always("SEA_LUMIES", 2), Drop.chance("SEA_LUMIES", 1, 50), Drop.chance("SEA_LUMIES", 1, 50),
                    Drop.always("MANGROVE_LOG", 16), Drop.chance("MANGROVE_LOG", 8, 50), Drop.chance("MANGROVE_LOG", 8, 50),
                    Drop.chance("ENCHANTED_MANGROVE_LOG", 10), Drop.chance("WET_WATER", 2.5),
                    Drop.oneIn("DYE_AQUAMARINE", 5_000_000)),
            profile(TADGANG, "Tadgang", 29, EntityType.FROG, 5_000, 100, 362, 37,
                    "A gang of Liltads!",
                    SeaCreatureBehaviour.aggressive(1.4, 20, 16), List.of(MobType.AQUATIC, MobType.ANIMAL),
                    Drop.always("SEA_LUMIES", 4, 8), Drop.always("MANGROVE_LOG", 32, 64),
                    Drop.chance("ENCHANTED_MANGROVE_LOG", 10), Drop.chance("ENCHANTED_SEA_LUMIES", 2.5),
                    Drop.chance("GILL_MEMBRANE", 20), Drop.oneIn("DYE_AQUAMARINE", 2_500_000)),
            profile(ENT, "Ent", 60, EntityType.IRON_GOLEM, 25_000, 275, 900, 90,
                    "You've hooked an Ent, as ancient as the forest itself.",
                    SeaCreatureBehaviour.aggressive(1.2, 25, 18), List.of(MobType.WOODLAND, MobType.AQUATIC),
                    Drop.always("SEA_LUMIES", 8), Drop.chance("SEA_LUMIES", 8, 50),
                    Drop.always("ENCHANTED_MANGROVE_LOG", 1), Drop.chance("ENCHANTED_MANGROVE_LOG", 2, 50),
                    Drop.chance("ENCHANTED_SEA_LUMIES", 5), Drop.chance("FORAGING_EXP_BOOST", 1),
                    Drop.chance("MANGCORE", 0.5), Drop.oneIn("DYE_AQUAMARINE", 2_500_000)),
            profile(THE_LOCH_EMPEROR, "The Loch Emperor", 101, EntityType.WITHER_SKELETON, 800_000, 100, 2_020, 252,
                    "The Loch Emperor arises from the depths.",
                    SeaCreatureBehaviour.aggressive(1.8, 30, 22),
                    List.of(MobType.AQUATIC, MobType.SKELETAL, MobType.ELUSIVE),
                    Drop.always("SEA_LUMIES", 32, 64), Drop.always("ENCHANTED_MANGROVE_LOG", 8, 16),
                    Drop.chance("EMPERORS_SKULL", 20), Drop.chance("ENCHANTED_SEA_LUMIES", 20),
                    Drop.chance("MANGCORE", 1), Drop.oneIn("DYE_AQUAMARINE", 50_000)),
            profile(NESSIE, "Nessie", 292, EntityType.ELDER_GUARDIAN, 2_000_000, 2_000, 7_300, 1_463,
                    "You've caused a disturbance in the loch. Could it be... Nessie?",
                    SeaCreatureBehaviour.aggressive(1.6, 40, 28),
                    List.of(MobType.AQUATIC, MobType.ELUSIVE, MobType.ANIMAL),
                    Drop.always("ENCHANTED_SEA_LUMIES", 2, 4), Drop.always("ENCHANTED_MANGROVE_LOG", 16, 32),
                    Drop.chance("MANGCORE", 10), Drop.chance("MOUND_OF_SEAGRASS", 2),
                    Drop.chance("BOBBIN_SCRIPTURES", 1), Drop.chance("VIBRANT_CORAL", 0.5),
                    Drop.oneIn("DYE_AQUAMARINE", 50_000)),
            profile(STRIDERSURFER, "Stridersurfer", 62, EntityType.STRIDER, 20_000, 400, 775, 81,
                    "You caught a Stridersurfer.",
                    SeaCreatureBehaviour.aggressive(1.4, 20, 18),
                    List.of(MobType.MAGMATIC, MobType.UNDEAD, MobType.WOODLAND),
                    Drop.always("GILL_MEMBRANE", 1), Drop.chance("GILL_MEMBRANE", 1, 40),
                    Drop.chance("STURDY_BONE", 40), Drop.oneIn("DYE_CARMINE", 2_500_000)));

    private GalateaSeaCreatures() {
    }

    private static SeaCreatureProfile profile(String id, String name, int level, EntityType entity, double health,
                                              double damage, long fishingXp, int xpOrbs, String catchMessage,
                                              SeaCreatureBehaviour behaviour, List<MobType> types, Drop... drops) {
        return SeaCreatureProfile.builder(id)
                .displayName(name)
                .level(level)
                .entityType(entity)
                .health(health).damage(damage).speed(90D)
                .behaviour(behaviour)
                .mobTypes(types.toArray(MobType[]::new))
                .damageCooldownMs(350)
                .fishingXpReward(fishingXp).xpOrbs(xpOrbs)
                .catchMessage(catchMessage)
                .lootTable(table(drops))
                .build();
    }

    private static SkyBlockLootTable table(Drop... drops) {
        return new SkyBlockLootTable() {
            @Override
            public List<LootRecord> getLootTable() {
                return java.util.Arrays.stream(drops)
                        .filter(drop -> drop.type() != null)
                        .map(drop -> new LootRecord(drop.type(), drop.rollAmount(), drop.chancePercent()))
                        .toList();
            }

            @Override
            public CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }

    private record Drop(String item, int min, int max, double chancePercent) {
        static Drop always(String item, int min, int max) {
            return new Drop(item, min, max, 100);
        }

        static Drop always(String item, int amount) {
            return always(item, amount, amount);
        }

        static Drop chance(String item, double chancePercent) {
            return chance(item, 1, chancePercent);
        }

        static Drop chance(String item, int amount, double chancePercent) {
            return new Drop(item, amount, amount, chancePercent);
        }

        static Drop oneIn(String item, double odds) {
            return chance(item, 100D / odds);
        }

        ItemType type() {
            return ItemType.get(item);
        }

        int rollAmount() {
            return min == max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
        }
    }
}
