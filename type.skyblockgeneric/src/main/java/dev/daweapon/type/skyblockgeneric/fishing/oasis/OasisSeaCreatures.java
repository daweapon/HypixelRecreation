package dev.daweapon.type.skyblockgeneric.fishing.oasis;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureBehaviour;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.minestom.server.entity.EntityType;

import java.util.Arrays;
import java.util.List;

/**
 * The two Sea Creatures fished up in the Oasis (wiki: Oasis_Sheep, Oasis_Rabbit). Stats, drops, XP and messages come
 * from those pages; the Oasis Rabbit's arrow attack and the animals' return-to-landing-point behaviour are not built.
 */
public final class OasisSeaCreatures {
    public static final String OASIS_SHEEP = "OASIS_SHEEP";
    public static final String OASIS_RABBIT = "OASIS_RABBIT";

    public static final List<SeaCreatureProfile> PROFILES = List.of(
            profile(OASIS_SHEEP, "Oasis Sheep", EntityType.SHEEP, "An Oasis Sheep appears from the water.",
                    Drop.always("MUTTON", 1), Drop.always("WHITE_WOOL", 1),
                    Drop.always("LILY_PAD", 1), Drop.chance("LILY_PAD", 1, 50),
                    Drop.chance("ENCHANTED_MUTTON", 15, 5), Drop.chance("ENCHANTED_COOKED_MUTTON", 1, 0.1),
                    Drop.oneIn("DYE_AQUAMARINE", 5_000_000)),
            profile(OASIS_RABBIT, "Oasis Rabbit", EntityType.RABBIT, "An Oasis Rabbit appears from the water.",
                    Drop.always("LILY_PAD", 1), Drop.chance("LILY_PAD", 1, 50),
                    Drop.always("RAW_RABBIT", 1), Drop.chance("RABBIT_FOOT", 1, 70), Drop.chance("RABBIT_HIDE", 1, 70),
                    Drop.chance("ENCHANTED_RAW_RABBIT", 15, 5), Drop.oneIn("DYE_AQUAMARINE", 5_000_000)));

    private OasisSeaCreatures() {
    }

    private static SeaCreatureProfile profile(String id, String name, EntityType entity, String catchMessage, Drop... drops) {
        return SeaCreatureProfile.builder(id)
                .displayName(name)
                .level(4)
                .entityType(entity)
                .health(6_000).damage(0).speed(90D)
                .behaviour(SeaCreatureBehaviour.passive(8))
                .mobTypes(MobType.AQUATIC, MobType.ANIMAL)
                .damageCooldownMs(350)
                .fishingXpReward(40).xpOrbs(5)
                .catchMessage(catchMessage)
                .lootTable(table(drops))
                .build();
    }

    private static SkyBlockLootTable table(Drop... drops) {
        return new SkyBlockLootTable() {
            @Override
            public List<LootRecord> getLootTable() {
                return Arrays.stream(drops)
                        .filter(drop -> drop.type() != null)
                        .map(drop -> new LootRecord(drop.type(), drop.amount(), drop.chancePercent()))
                        .toList();
            }

            @Override
            public CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }

    private record Drop(String item, int amount, double chancePercent) {
        static Drop always(String item, int amount) {
            return new Drop(item, amount, 100);
        }

        static Drop chance(String item, int amount, double chancePercent) {
            return new Drop(item, amount, chancePercent);
        }

        static Drop oneIn(String item, double odds) {
            return new Drop(item, 1, 100D / odds);
        }

        ItemType type() {
            return ItemType.get(item);
        }
    }
}
