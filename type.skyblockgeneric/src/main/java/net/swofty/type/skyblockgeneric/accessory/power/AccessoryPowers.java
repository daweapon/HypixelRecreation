package net.swofty.type.skyblockgeneric.accessory.power;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryPowers;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryPowers.PowerData;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

/**
 * Powers and Stats Tuning at Maxwell: which powers a profile can use, the selected power's stats at
 * the current Accessory Power, and the stats from assigned Tuning Points.
 */
public final class AccessoryPowers {
    public static final int SKYBLOCK_XP_PER_POWER = 15;
    public static final int TEMPLATE_SLOTS = 4;
    public static final int TEMPLATE_BITS_COST = 10_000;

    private AccessoryPowers() {
    }

    public static DatapointAccessoryPowers datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ACCESSORY_POWERS, DatapointAccessoryPowers.class);
    }

    public static PowerData data(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    public static void save(SkyBlockPlayer player, PowerData data) {
        datapoint(player).setValue(data);
        player.getStatistics().updateAccessoryStatistics();
    }

    public static int combatLevel(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.COMBAT);
    }

    public static boolean isUnlocked(PowerData data, AccessoryPowerType power) {
        return !power.isStonePower() || data.getLearned().contains(power);
    }

    public static boolean meetsRequirement(AccessoryPowerType power, int combatLevel) {
        return combatLevel >= power.getCombatLevel();
    }

    public static boolean canSelect(PowerData data, AccessoryPowerType power, int combatLevel) {
        return isUnlocked(data, power) && meetsRequirement(power, combatLevel);
    }

    public static void select(PowerData data, @Nullable AccessoryPowerType power) {
        data.setSelected(power);
        data.setPowerChosen(true);
    }

    /** Learns a stone power; false when it was already known or isn't a stone power. */
    public static boolean learn(PowerData data, AccessoryPowerType power) {
        return power.isStonePower() && data.getLearned().add(power);
    }

    public static void awardLearnXP(SkyBlockPlayer player, int powersLearned) {
        player.getSkyBlockExperience().addSourceXP(SkyBlockXPSource.UNLOCKING_POWERS, powersLearned * SKYBLOCK_XP_PER_POWER);
    }

    public static ItemStatistics statistics(PowerData data, int accessoryPower) {
        return ItemStatistics.add(powerStatistics(data, accessoryPower), tuningStatistics(data, accessoryPower));
    }

    public static ItemStatistics powerStatistics(PowerData data, int accessoryPower) {
        AccessoryPowerType selected = data.getSelected();
        return selected == null ? ItemStatistics.empty() : PowerScaling.statistics(selected, accessoryPower);
    }

    public static ItemStatistics tuningStatistics(PowerData data, int accessoryPower) {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        effectiveTuning(data, accessoryPower).forEach((stat, points) -> {
            if (points > 0) builder.withBase(stat.getStatistic(), points * stat.getPerPoint());
        });
        return builder.build();
    }

    /** Assigned points that fit the current budget, filled in Maxwell's stat order if Accessory Power has dropped. */
    public static Map<TuningStat, Integer> effectiveTuning(PowerData data, int accessoryPower) {
        int budget = PowerScaling.tuningPoints(accessoryPower);
        Map<TuningStat, Integer> effective = new EnumMap<>(TuningStat.class);
        for (TuningStat stat : TuningStat.values()) {
            int points = Math.min(data.pointsIn(stat), budget);
            if (points <= 0) continue;
            effective.put(stat, points);
            budget -= points;
        }
        return effective;
    }

    public static int unassignedPoints(PowerData data, int accessoryPower) {
        return Math.max(0, PowerScaling.tuningPoints(accessoryPower) - data.assignedPoints());
    }

    /** Adds up to {@code amount} points, limited by the unassigned budget; returns how many were added. */
    public static int addPoints(PowerData data, TuningStat stat, int amount, int accessoryPower) {
        int added = Math.min(amount, unassignedPoints(data, accessoryPower));
        if (added <= 0) return 0;
        data.getTuning().merge(stat, added, Integer::sum);
        return added;
    }

    public static int removePoints(PowerData data, TuningStat stat, int amount) {
        int removed = Math.min(amount, data.pointsIn(stat));
        if (removed <= 0) return 0;
        int left = data.pointsIn(stat) - removed;
        if (left == 0) data.getTuning().remove(stat);
        else data.getTuning().put(stat, left);
        return removed;
    }

    public static void saveTemplate(PowerData data, int slot) {
        data.getTemplates().put(slot, new EnumMap<>(data.getTuning()));
    }

    public static boolean loadTemplate(PowerData data, int slot) {
        Map<TuningStat, Integer> template = data.getTemplates().get(slot);
        if (template == null) return false;
        data.getTuning().clear();
        data.getTuning().putAll(template);
        return true;
    }

    public static boolean isTemplateUnlocked(PowerData data, int slot) {
        return slot < data.getTemplatesUnlocked();
    }

    /** The selectable power with the most total stats at this Accessory Power. */
    public static @Nullable AccessoryPowerType best(PowerData data, int accessoryPower, int combatLevel) {
        return AccessoryPowerType.menuOrder().stream()
                .filter(power -> canSelect(data, power, combatLevel))
                .max(Comparator.comparingDouble(power -> PowerScaling.totalStats(power, accessoryPower)))
                .orElse(null);
    }
}
