package dev.daweapon.type.skyblockgeneric.accessory.enrichment;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import org.jetbrains.annotations.Nullable;

/** The eleven Accessory Enrichments and the stat each adds (wiki Enrichments). */
@Getter
public enum Enrichment {
    SPEED("SPEED_ENRICHMENT", ItemStatistic.SPEED, 1),
    INTELLIGENCE("INTELLIGENCE_ENRICHMENT", ItemStatistic.INTELLIGENCE, 2),
    CRITICAL_DAMAGE("CRITICAL_DAMAGE_ENRICHMENT", ItemStatistic.CRITICAL_DAMAGE, 1),
    CRITICAL_CHANCE("CRITICAL_CHANCE_ENRICHMENT", ItemStatistic.CRITICAL_CHANCE, 1),
    STRENGTH("STRENGTH_ENRICHMENT", ItemStatistic.STRENGTH, 1),
    DEFENSE("DEFENSE_ENRICHMENT", ItemStatistic.DEFENSE, 1),
    HEALTH("HEALTH_ENRICHMENT", ItemStatistic.HEALTH, 3),
    MAGIC_FIND("MAGIC_FIND_ENRICHMENT", ItemStatistic.MAGIC_FIND, 0.5),
    FEROCITY("FEROCITY_ENRICHMENT", ItemStatistic.FEROCITY, 0.3),
    SEA_CREATURE_CHANCE("SEA_CREATURE_CHANCE_ENRICHMENT", ItemStatistic.SEA_CREATURE_CHANCE, 0.3),
    ATTACK_SPEED("ATTACK_SPEED_ENRICHMENT", ItemStatistic.BONUS_ATTACK_SPEED, 0.5);

    private final String itemId;
    private final ItemStatistic statistic;
    private final double amount;

    Enrichment(String itemId, ItemStatistic statistic, double amount) {
        this.itemId = itemId;
        this.statistic = statistic;
        this.amount = amount;
    }

    public @Nullable ItemType item() {
        return ItemType.get(itemId);
    }

    public static @Nullable Enrichment byItem(@Nullable ItemType type) {
        if (type == null) return null;
        for (Enrichment enrichment : values()) {
            if (enrichment.itemId.equals(type.name())) return enrichment;
        }
        return null;
    }

    public static @Nullable Enrichment byName(@Nullable String name) {
        if (name == null || name.isEmpty()) return null;
        for (Enrichment enrichment : values()) {
            if (enrichment.name().equals(name)) return enrichment;
        }
        return null;
    }
}
