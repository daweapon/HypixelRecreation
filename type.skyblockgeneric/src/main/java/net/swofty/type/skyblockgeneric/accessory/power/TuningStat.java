package net.swofty.type.skyblockgeneric.accessory.power;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.statistics.ItemStatistic;

/** The eight stats Maxwell can tune, with Hypixel's value per Tuning Point (wiki Maxwell/UI). */
@Getter
public enum TuningStat {
    HEALTH(ItemStatistic.HEALTH, 5, Material.GOLDEN_APPLE),
    DEFENSE(ItemStatistic.DEFENSE, 1, Material.IRON_CHESTPLATE),
    SPEED(ItemStatistic.SPEED, 1.5, Material.SUGAR),
    STRENGTH(ItemStatistic.STRENGTH, 1, Material.BLAZE_POWDER),
    CRIT_DAMAGE(ItemStatistic.CRITICAL_DAMAGE, 1, Material.IRON_SWORD),
    CRIT_CHANCE(ItemStatistic.CRITICAL_CHANCE, 0.2, Material.FLINT),
    ATTACK_SPEED(ItemStatistic.BONUS_ATTACK_SPEED, 0.3, Material.GOLDEN_AXE),
    INTELLIGENCE(ItemStatistic.INTELLIGENCE, 2, Material.ENCHANTED_BOOK);

    private final ItemStatistic statistic;
    private final double perPoint;
    private final Material icon;

    TuningStat(ItemStatistic statistic, double perPoint, Material icon) {
        this.statistic = statistic;
        this.perPoint = perPoint;
        this.icon = icon;
    }
}
