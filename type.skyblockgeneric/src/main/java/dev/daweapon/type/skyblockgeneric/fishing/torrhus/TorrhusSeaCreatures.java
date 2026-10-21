package dev.daweapon.type.skyblockgeneric.fishing.torrhus;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureBehaviour;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.minestom.server.entity.EntityType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The six Sea Creatures fished up in Torrhus Canyon (wiki: Sea Creatures/List/Torrhus Canyon and each creature's page).
 * Stats, drops, XP and messages come from those pages; entity models, speeds and AI are estimates.
 */
public final class TorrhusSeaCreatures {
    public static final String HAGGARD = "HAGGARD";
    public static final String BRINELING = "BRINELING";
    public static final String SPRAWL = "SPRAWL";
    public static final String TORRID = "TORRID";
    public static final String SILKBREEZE = "SILKBREEZE";
    public static final String GIANT_ISOPOD = "GIANT_ISOPOD";

    public static final List<SeaCreatureProfile> PROFILES = List.of(
            profile(HAGGARD, "Haggard", 44, EntityType.ZOMBIE, 10_000, 200, 330, 48,
                    "A Haggard stumbles to the shore, ready for a fight!",
                    SeaCreatureBehaviour.aggressive(1.6, 20, 16), List.of(MobType.AQUATIC, MobType.SKELETAL),
                    Drop.always("BONE", 2, 4), Drop.always("HELIX_LOG", 8, 16), Drop.always("RUBY_VEILSHROOM", 1, 2),
                    Drop.chance("ENCHANTED_BONE", 2), Drop.chance("ENCHANTED_HELIX_LOG", 5),
                    Drop.chance("ENCHANTED_RUBY_VEILSHROOM", 1), Drop.oneIn("DYE_AQUAMARINE", 5_000_000)),
            profile(BRINELING, "Brineling", 58, EntityType.AXOLOTL, 25_000, 250, 580, 70,
                    "A Brineling interrupts you with a stream of bubbles!",
                    SeaCreatureBehaviour.aggressive(1.3, 30, 14), List.of(MobType.AQUATIC, MobType.ANIMAL),
                    Drop.always("HELIX_LOG", 16, 32), Drop.always("RUBY_VEILSHROOM", 2, 4),
                    Drop.chance("ENCHANTED_HELIX_LOG", 10), Drop.chance("ENCHANTED_RUBY_VEILSHROOM", 2.5),
                    Drop.chance("SEA_BRINE", 0.5), Drop.oneIn("DYE_AQUAMARINE", 5_000_000)),
            profile(SPRAWL, "Sprawl", 71, EntityType.VEX, 50_000, 300, 887, 93,
                    "A Sprawl emerges from the blue, and it's looking for you!",
                    SeaCreatureBehaviour.aggressive(1.5, 20, 18), List.of(MobType.AQUATIC, MobType.AIRBORNE),
                    Drop.always("HELIX_LOG", 32, 64), Drop.always("RUBY_VEILSHROOM", 8, 16),
                    Drop.chance("ENCHANTED_HELIX_LOG", 25), Drop.chance("ENCHANTED_RUBY_VEILSHROOM", 5),
                    Drop.oneIn("DYE_AQUAMARINE", 2_500_000)),
            profile(TORRID, "Torrid", 85, EntityType.VEX, 100_000, 350, 1_275, 128,
                    "The laughter of a Torrid echoes through the air.",
                    SeaCreatureBehaviour.aggressive(1.6, 20, 20), List.of(MobType.AQUATIC, MobType.AIRBORNE),
                    Drop.always("ENCHANTED_HELIX_LOG", 1), Drop.always("RUBY_VEILSHROOM", 32),
                    Drop.chance("ENCHANTED_RUBY_VEILSHROOM", 20), Drop.chance("HELIXIS", 0.2),
                    Drop.chance("VEILSHROOM_BUNCH", 0.2), Drop.chance("WATER_HYACINTH", 1),
                    Drop.oneIn("DYE_AQUAMARINE", 2_500_000)),
            profile(SILKBREEZE, "Silkbreeze", 105, EntityType.BREEZE, 250_000, 400, 2_100, 263,
                    "Something zips through the air - it's a Silkbreeze!",
                    SeaCreatureBehaviour.aggressive(2.0, 20, 22),
                    List.of(MobType.AQUATIC, MobType.AIRBORNE, MobType.ELUSIVE),
                    Drop.always("ENCHANTED_HELIX_LOG", 4, 8), Drop.always("ENCHANTED_RUBY_VEILSHROOM", 1),
                    Drop.chance("SUBLIME_SILK", 20), Drop.chance("VEILSHROOM_BUNCH", 1), Drop.chance("HELIXIS", 1),
                    Drop.chance("DISTANT_ECHO", 1), Drop.oneIn("DYE_AQUAMARINE", 50_000)),
            profile(GIANT_ISOPOD, "Giant Isopod", 186, EntityType.SILVERFISH, 500_000, 500, 4_650, 934,
                    "A Giant Isopod was dredged up from the depths!",
                    SeaCreatureBehaviour.aggressive(1.5, 20, 24),
                    List.of(MobType.AQUATIC, MobType.ARTHROPOD, MobType.ELUSIVE),
                    Drop.always("ENCHANTED_HELIX_LOG", 16, 32), Drop.always("ENCHANTED_RUBY_VEILSHROOM", 8),
                    Drop.chance("VEILSHROOM_BUNCH", 5), Drop.chance("HELIXIS", 5), Drop.chance("ISOPOD_HUSK", 2),
                    Drop.chance("REINFORCED_NETTING", 1), Drop.oneIn("DYE_AQUAMARINE", 50_000)));

    public static final net.minestom.server.coordinate.Pos ISOPOD_SPAWN = new net.minestom.server.coordinate.Pos(-643.5, 157, 184.5);

    private TorrhusSeaCreatures() {
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
            return new Drop(item, 1, 1, chancePercent);
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
