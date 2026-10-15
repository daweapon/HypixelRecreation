package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.set.impl.ArmorSet;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class ProtectorDragonSet implements ArmorSet {
    private static final ThreadLocal<Boolean> RESOLVING = ThreadLocal.withInitial(() -> false);

    public static double missingHealthPercent(double health, double maxHealth) {
        if (maxHealth <= 0) return 0;
        return Math.max(0, Math.min(100, (1 - health / maxHealth) * 100));
    }

    public static double bonusDefense(double pieceDefense, double missingPercent) {
        return pieceDefense * missingPercent / 100;
    }

    @Override
    public String getName() {
        return "Protective Blood";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "<7>Increases the defense of each",
                "<7>armor piece by <a>+1% <a>❈ Defense <7>for",
                "<7>each missing percent of HP."));
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player) {
        if (RESOLVING.get()) return ItemStatistics.empty();
        RESOLVING.set(true);
        try {
            double missing = missingHealthPercent(player.getHealth(), player.getMaxHealth());
            if (missing <= 0) return ItemStatistics.empty();
            double defense = 0;
            for (SkyBlockItem piece : player.getArmor()) {
                if (piece == null || piece.isAir()) continue;
                defense += piece.getAttributeHandler().getStatistics().getOverall(ItemStatistic.DEFENSE);
            }
            return ItemStatistics.builder().withBase(ItemStatistic.DEFENSE, bonusDefense(defense, missing)).build();
        } finally {
            RESOLVING.set(false);
        }
    }
}
