package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.set.impl.ArmorSet;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class YoungDragonSet implements ArmorSet {
    public static final double BONUS_SPEED = 70;
    public static final double HEALTH_THRESHOLD = 0.5;
    private static final ThreadLocal<Boolean> RESOLVING = ThreadLocal.withInitial(() -> false);

    public static double speedBonus(double health, double maxHealth) {
        return maxHealth > 0 && health / maxHealth > HEALTH_THRESHOLD ? BONUS_SPEED : 0;
    }

    @Override
    public String getName() {
        return "Young Blood";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "<7>Gain <a>+70<7> Walk Speed while you are",
                "<7>above <a>50%<7> HP.",
                "<8>+100 Walk Speed Cap"));
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player) {
        if (RESOLVING.get()) return ItemStatistics.empty();
        RESOLVING.set(true);
        try {
            double bonus = speedBonus(player.getHealth(), player.getMaxHealth());
            return bonus == 0 ? ItemStatistics.empty() : ItemStatistics.builder().withBase(ItemStatistic.SPEED, bonus).build();
        } finally {
            RESOLVING.set(false);
        }
    }
}
