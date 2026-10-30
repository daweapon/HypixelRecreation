package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointFrozenCorpses;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

public final class CorpseRngMeter {
    public static final String DYE_KEY = "frostbitten_dye";
    private static final double MAX_BOOST = 2;

    private CorpseRngMeter() {
    }

    public record Boost(@Nullable String selectedKey, long stored, long required) {
        public static final Boost NONE = new Boost(null, 0, 1);

        public boolean applies(@Nullable String meterKey) {
            return selectedKey != null && selectedKey.equals(meterKey);
        }

        public boolean guaranteed() {
            return selectedKey != null && stored >= required;
        }

        public double multiplier() {
            return selectedKey == null ? 1 : CorpseRngMeter.multiplier(stored, required);
        }

        public double weight(CorpseLootTable.Entry entry) {
            return applies(entry.meterKey()) ? entry.weight() * multiplier() : entry.weight();
        }
    }

    public static double multiplier(long stored, long required) {
        return 1 + Math.min(MAX_BOOST * stored / required, MAX_BOOST);
    }

    public static Boost boost(SkyBlockPlayer player) {
        DatapointFrozenCorpses.FrozenCorpseData data = CorpseProgress.data(player);
        CorpseLootTable.MeterDrop drop = CorpseLootTable.get().meterDrop(data.getRngSelected());
        return drop == null ? Boost.NONE : new Boost(drop.id(), data.getRngXp(), drop.requiredXp());
    }

    public static @Nullable CorpseLootTable.MeterDrop selected(SkyBlockPlayer player) {
        return CorpseLootTable.get().meterDrop(CorpseProgress.data(player).getRngSelected());
    }

    public static long stored(SkyBlockPlayer player) {
        return CorpseProgress.data(player).getRngXp();
    }

    public static double percent(long stored, long required) {
        return Math.min(100D, stored * 100D / required);
    }

    public static void select(SkyBlockPlayer player, @Nullable CorpseLootTable.MeterDrop drop) {
        DatapointFrozenCorpses.FrozenCorpseData data = CorpseProgress.data(player);
        String id = drop == null ? null : drop.id();
        if (java.util.Objects.equals(id, data.getRngSelected())) return;
        data.setRngSelected(id);
        data.setRngXp(0);
        CorpseProgress.save(player, data);
    }

    public static void addXp(SkyBlockPlayer player, long amount) {
        if (amount <= 0) return;
        DatapointFrozenCorpses.FrozenCorpseData data = CorpseProgress.data(player);
        data.setRngXp(data.getRngXp() + amount);
        CorpseProgress.save(player, data);
    }

    public static void onObtained(SkyBlockPlayer player, @Nullable String meterKey) {
        DatapointFrozenCorpses.FrozenCorpseData data = CorpseProgress.data(player);
        CorpseLootTable.MeterDrop drop = CorpseLootTable.get().meterDrop(data.getRngSelected());
        if (drop == null || !drop.id().equals(meterKey)) return;
        data.setRngXp(data.getRngXp() >= drop.requiredXp() ? data.getRngXp() - drop.requiredXp() : 0);
        CorpseProgress.save(player, data);
    }

    public static void fill(SkyBlockPlayer player) {
        CorpseLootTable.MeterDrop drop = selected(player);
        if (drop == null) return;
        DatapointFrozenCorpses.FrozenCorpseData data = CorpseProgress.data(player);
        data.setRngXp(Math.max(data.getRngXp(), drop.requiredXp()));
        CorpseProgress.save(player, data);
    }

    public static void reset(SkyBlockPlayer player) {
        DatapointFrozenCorpses.FrozenCorpseData data = CorpseProgress.data(player);
        data.setRngXp(0);
        CorpseProgress.save(player, data);
    }
}
