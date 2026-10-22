package dev.daweapon.type.skyblockgeneric.fishing.resolver;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.fishing.FishingContext;
import dev.daweapon.type.skyblockgeneric.fishing.catches.CatchPayload;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeId;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;

import java.util.Optional;
import java.util.random.RandomGenerator;

public final class TombFishing {
    public static final String TABLE_ID = "MOONGLADE_TOMB_FLOODWAY";
    public static final double TREASURE_CHANCE_BONUS = 10;
    public static final double FLEXBONE_BASE_PERCENT = 25;
    public static final String FLEXIBLE_FISHER = "U47";

    private TombFishing() {
    }

    public static double treasureChanceBonus(String tableId) {
        return TABLE_ID.equals(tableId) ? TREASURE_CHANCE_BONUS : 0;
    }

    public static double flexbonePercent(double flexibleFisher) {
        return FLEXBONE_BASE_PERCENT + flexibleFisher;
    }

    public static int flexboneAmount(String quality, RandomGenerator random) {
        return switch (quality) {
            case "GREAT" -> random.nextInt(2, 5);
            case "OUTSTANDING" -> 8;
            default -> 1;
        };
    }

    static Optional<CatchPayload> flexbone(FishingContext context, String tableId, String quality, double xp) {
        if (!TABLE_ID.equals(tableId)) return Optional.empty();
        double fisher = AttributeEffectService.value(context.player().getHuntingData(), AttributeId.parse(FLEXIBLE_FISHER));
        if (!LootRoll.roll(context.player().getUuid(), ItemType.FLEXBONE, flexbonePercent(fisher))) return Optional.empty();
        int amount = flexboneAmount(quality, java.util.concurrent.ThreadLocalRandom.current());
        return Optional.of(new CatchPayload.Treasure(ItemType.FLEXBONE.name(), amount, xp, quality));
    }
}
