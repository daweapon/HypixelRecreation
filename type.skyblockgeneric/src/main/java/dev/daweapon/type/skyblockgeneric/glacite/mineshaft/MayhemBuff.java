package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

public enum MayhemBuff {
    SCRAP_CHANCE("<a>+5% <9>Suspicious Scrap <a>chance", null, 0),
    MINING_FORTUNE("<a>+100 <6>⸕ Mining Fortune", ItemStatistic.MINING_FORTUNE, 100),
    MINING_SPEED("<a>+200 <6>⸕ Mining Speed", ItemStatistic.MINING_SPEED, 200),
    COLD_RESISTANCE("<a>+10 <b>❄ Cold Resistance", ItemStatistic.COLD_RESISTANCE, 10),
    ABILITY_COOLDOWN("<a>-25% <6>Pickaxe Ability <a>cooldown", null, 0);

    public static final double SCRAP_BONUS_PERCENT = 5;
    public static final double COOLDOWN_MULTIPLIER = 0.75;

    private final String description;
    private final @Nullable ItemStatistic statistic;
    private final double amount;

    MayhemBuff(String description, @Nullable ItemStatistic statistic, double amount) {
        this.description = description;
        this.statistic = statistic;
        this.amount = amount;
    }

    public String description() {
        return description;
    }

    public @Nullable ItemStatistic statistic() {
        return statistic;
    }

    public double amount() {
        return amount;
    }

    public static MayhemBuff roll(@Nullable UUID player, RandomGenerator random) {
        return LootRoll.pick(player, List.of(values()), _ -> null, _ -> 1.0, random);
    }
}
