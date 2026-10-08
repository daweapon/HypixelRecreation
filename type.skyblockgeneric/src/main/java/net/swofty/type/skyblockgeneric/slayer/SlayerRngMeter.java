package net.swofty.type.skyblockgeneric.slayer;

import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * A slayer RNG Meter: Slayer XP from Tier III+ kills fills it, the selected drop becomes up to three
 * times as likely as the meter fills, and a full meter guarantees that drop on the next kill.
 */
public final class SlayerRngMeter {
    public static final SlayerTier MINIMUM_TIER = SlayerTier.III;

    private SlayerRngMeter() {
    }

    public static boolean unlocked(DatapointSlayer.SlayerProgress progress) {
        for (SlayerTier tier : SlayerTier.values()) {
            if (tier.number() >= MINIMUM_TIER.number() && progress.completions(tier) > 0) return true;
        }
        return false;
    }

    public static @Nullable SlayerDrop selected(SlayerDefinition definition, DatapointSlayer.SlayerProgress progress) {
        if (progress.getRngItem() == null) return null;
        return definition.drops().stream()
            .filter(drop -> drop.hasRngMeter() && drop.key().equals(progress.getRngItem()))
            .findFirst().orElse(null);
    }

    public static double weightMultiplier(DatapointSlayer.SlayerProgress progress, SlayerDrop drop) {
        if (!drop.hasRngMeter() || !drop.key().equals(progress.getRngItem())) return 1;
        return 1 + Math.min(2D * progress.getRngXp() / drop.rngMeterXp(), 2D);
    }

    public static boolean full(DatapointSlayer.SlayerProgress progress, SlayerDrop drop) {
        return drop.key().equals(progress.getRngItem()) && progress.getRngXp() >= drop.rngMeterXp();
    }

    public static void onObtained(DatapointSlayer.SlayerProgress progress, SlayerDrop drop) {
        if (!drop.key().equals(progress.getRngItem())) return;
        progress.setRngXp(progress.getRngXp() >= drop.rngMeterXp() ? progress.getRngXp() - drop.rngMeterXp() : 0);
    }

    public static void addXp(DatapointSlayer.SlayerProgress progress, SlayerTier tier, int slayerXp) {
        if (tier.number() < MINIMUM_TIER.number() || progress.getRngItem() == null) return;
        progress.setRngXp(progress.getRngXp() + slayerXp);
    }

    public static void fill(SlayerDefinition definition, DatapointSlayer.SlayerProgress progress) {
        SlayerDrop target = selected(definition, progress);
        if (target == null) {
            target = definition.drops().stream()
                .filter(drop -> drop.hasRngMeter() && drop.available())
                .max(Comparator.comparingLong(SlayerDrop::rngMeterXp))
                .orElse(null);
            if (target == null) return;
            progress.setRngItem(target.key());
            progress.setRngXp(0);
        }
        progress.setRngXp(Math.max(progress.getRngXp(), target.rngMeterXp()));
    }

    public static double percent(DatapointSlayer.SlayerProgress progress, SlayerDrop drop) {
        return Math.min(100D, progress.getRngXp() * 100D / drop.rngMeterXp());
    }

    public static void select(SkyBlockPlayer player, SlayerType type, @Nullable SlayerDrop drop) {
        DatapointSlayer datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SLAYER, DatapointSlayer.class);
        DatapointSlayer.SlayerData data = datapoint.getValue();
        DatapointSlayer.SlayerProgress progress = data.progress(type);
        String key = drop == null ? null : drop.key();
        if (key != null && key.equals(progress.getRngItem())) return;
        progress.setRngItem(key);
        progress.setRngXp(0);
        datapoint.setValue(data);
    }
}
