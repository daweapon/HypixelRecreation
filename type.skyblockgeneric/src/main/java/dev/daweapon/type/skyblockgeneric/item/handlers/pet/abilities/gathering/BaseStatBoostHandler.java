package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering;

import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * Raises the pet's own base stats by a percentage while a condition holds.
 */
public final class BaseStatBoostHandler implements PetAbilityHandler {
    private final Predicate<SkyBlockPlayer> active;
    private final ToDoubleFunction<PetAbilityContext> percent;

    public BaseStatBoostHandler(Predicate<SkyBlockPlayer> active, ToDoubleFunction<PetAbilityContext> percent) {
        this.active = active;
        this.percent = percent;
    }

    public static BaseStatBoostHandler byValue(Predicate<SkyBlockPlayer> active) {
        return new BaseStatBoostHandler(active, context -> context.value(0));
    }

    public static BaseStatBoostHandler doubling(Predicate<SkyBlockPlayer> active) {
        return new BaseStatBoostHandler(active, _ -> 100);
    }

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        SkyBlockPlayer player = context.player();
        if (player == null || !active.test(player)) return ItemStatistics.empty();
        ItemStatistics base = context.pet().getComponent(PetComponent.class).statisticsAt(context.rarity(), context.level());
        return scaled(base, percent.applyAsDouble(context) / 100D);
    }

    public static ItemStatistics scaled(ItemStatistics statistics, double factor) {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        statistics.getOverall().forEach((statistic, value) -> {
            if (value != 0) builder.withBase(statistic, value * factor);
        });
        return builder.build();
    }
}
