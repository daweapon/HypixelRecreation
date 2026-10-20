package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.type.skyblockgeneric.event.custom.FishCaughtEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class StarlynScoring {
    private final StarlynConfig.Points points;

    public StarlynScoring(StarlynConfig.Points points) {
        this.points = points;
    }

    public static StarlynScoring of(StarlynSister sister) {
        return new StarlynScoring(StarlynConfig.get(sister).points());
    }

    public int treeGift() {
        return points.treeGift();
    }

    public int logsPerHundred(StarlynTree tree) {
        return points.logsPerHundred().getOrDefault(tree, 0);
    }

    public int hunting(String shardName, String rarityName) {
        if (!points.huntingByRarity().isEmpty()) return points.huntingByRarity().getOrDefault(rarityName, 0);
        return points.huntingByShard().getOrDefault(shardName.toLowerCase(Locale.ROOT), points.huntingDefault());
    }

    public int fishing(FishCaughtEvent.Kind kind, @Nullable String quality) {
        int total = points.catchPoints();
        if (kind == FishCaughtEvent.Kind.SEA_CREATURE) total += points.seaCreature();
        if (kind == FishCaughtEvent.Kind.TREASURE) total += treasure(quality);
        return total;
    }

    private int treasure(@Nullable String quality) {
        if (points.treasure() > 0) return points.treasure();
        return points.treasureByQuality().getOrDefault(quality == null ? "GOOD" : quality.toUpperCase(Locale.ROOT), 0);
    }

    public int combat() {
        return points.combat();
    }

    public int gathering(StarlynResource resource) {
        return points.gathering().getOrDefault(resource, 0);
    }
}
