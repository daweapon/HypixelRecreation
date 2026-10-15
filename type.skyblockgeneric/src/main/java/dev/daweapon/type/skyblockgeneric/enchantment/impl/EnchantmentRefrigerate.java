package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Refrigerate ultimate enchantment: a share of the Mana spent on abilities becomes Defense for 10s,
// up to 150 Defense per enchanted armor piece.
public class EnchantmentRefrigerate implements Ench {
    private static final double[] PERCENT_OF_MANA = {3, 3.75, 4.5, 5.25, 6};
    private static final double MAX_DEFENSE_PER_PIECE = 150;
    private static final long DURATION_MS = 10_000;
    private static final String BUFF_NAME = "Refrigerate";
    private static final Map<UUID, Buff> ACTIVE = new ConcurrentHashMap<>();

    private record Buff(double defense, long expiresAt) {
    }

    @Override
    public String getDescription(int level) {
        return "Convert <a>" + StringUtility.decimalify(PERCENT_OF_MANA[level - 1], 2) + "% <7>of <b>✎ Mana <7>used as <a>❈ Defense"
                + " <7>for <a>10s<7>, up to <a>150 ❈ Defense <7>per armor piece.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(1, 50, 2, 100, 3, 150, 4, 200, 5, 250)));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.ARMOR);
    }

    @Override
    public boolean isUltimate() {
        return true;
    }

    public static double defenseGained(Map<Integer, Integer> piecesByLevel, double manaUsed) {
        double gained = 0;
        for (Map.Entry<Integer, Integer> entry : piecesByLevel.entrySet()) {
            double perPiece = manaUsed * PERCENT_OF_MANA[entry.getKey() - 1] / 100D;
            gained += Math.min(MAX_DEFENSE_PER_PIECE, perPiece) * entry.getValue();
        }
        return gained;
    }

    public static void onManaUsed(SkyBlockPlayer player, double manaUsed) {
        Map<Integer, Integer> pieces = player.getStatistics().getAllOfEnchants(EnchantmentType.REFRIGERATE, false);
        int pieceCount = pieces.values().stream().mapToInt(Integer::intValue).sum();
        if (pieceCount == 0 || manaUsed <= 0) return;

        long now = System.currentTimeMillis();
        Buff previous = ACTIVE.get(player.getUuid());
        double carried = previous != null && previous.expiresAt() > now ? previous.defense() : 0;
        double defense = Math.min(MAX_DEFENSE_PER_PIECE * pieceCount, carried + defenseGained(pieces, manaUsed));
        ACTIVE.put(player.getUuid(), new Buff(defense, now + DURATION_MS));

        player.getStatistics().removeTemporaryStatistics(statistic -> BUFF_NAME.equals(statistic.getDisplayName()));
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(ItemStatistics.builder().withBase(ItemStatistic.DEFENSE, defense).build())
                .withExpirationInMs(DURATION_MS)
                .withDisplayName(BUFF_NAME)
                .withDisplayColor("<a>")
                .build());
    }
}
