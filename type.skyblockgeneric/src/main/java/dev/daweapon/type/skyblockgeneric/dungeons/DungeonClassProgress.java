package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassXp;
import dev.daweapon.type.generic.data.datapoints.DatapointBoolean;
import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.generic.data.datapoints.DatapointString;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Per-profile dungeon classes: the selected class, each class's XP and level, and Mort's Auto Ready Up toggle. {@link #completeRun} is the hook the
 * Catacombs server calls for every member of a won run. A run pays the floor's base Catacombs XP as class XP, without
 * the Catacombs daily bonus; the wiki has no formula for it, so this is an estimate.
 */
public final class DungeonClassProgress {
    public static final DungeonClassType DEFAULT_CLASS = DungeonClassType.HEALER;

    private DungeonClassProgress() {
    }

    public static DungeonClassType selected(SkyBlockPlayer player) {
        return parse(selectedPoint(player).getValue());
    }

    public static DungeonClassType parse(String stored) {
        if (stored == null || stored.isBlank()) return DEFAULT_CLASS;
        return Arrays.stream(DungeonClassType.values()).filter(type -> type.name().equals(stored)).findFirst().orElse(DEFAULT_CLASS);
    }

    public static void select(SkyBlockPlayer player, DungeonClassType type) {
        selectedPoint(player).setValue(type.name());
    }

    public static long xp(SkyBlockPlayer player, DungeonClassType type) {
        Long stored = xpPoint(player, type).getValue();
        return stored == null ? 0 : stored;
    }

    public static int level(SkyBlockPlayer player, DungeonClassType type) {
        return DungeonClassXp.level(xp(player, type));
    }

    public static double classAverage(SkyBlockPlayer player) {
        return Arrays.stream(DungeonClassType.values()).mapToInt(type -> level(player, type)).average().orElse(0);
    }

    /** Pays each class its share of the run's experience; the run's score screen shows the amounts. */
    public static Map<DungeonClassType, Long> completeRun(SkyBlockPlayer player, DungeonClassType played,
                                                          Collection<DungeonClassType> teammates, CatacombsFloor floor) {
        Map<DungeonClassType, Long> earned = DungeonClassXp.earned(played, teammates, CatacombsRunXp.base(floor));
        for (DungeonClassType type : DungeonClassType.values()) {
            Long amount = earned.get(type);
            if (amount != null) addXp(player, type, amount);
        }
        return earned;
    }

    public static String runXpLine(DungeonClassType type, long amount, boolean teamBonus) {
        return "<3>+<b>" + String.format("%,d", amount) + " " + type.displayName() + " Experience"
                + (teamBonus ? " <8>(Team Bonus)" : "");
    }

    public static void addXp(SkyBlockPlayer player, DungeonClassType type, long amount) {
        if (amount <= 0) return;
        long before = xp(player, type);
        long after = DungeonClassXp.capped(before + amount);
        xpPoint(player, type).setValue(after);
        int from = DungeonClassXp.level(before);
        int to = DungeonClassXp.level(after);
        if (to <= from) return;
        levelUpLines(type, from, to).forEach(player::sendMessage);
        player.getSkyBlockExperience().addSourceXP(SkyBlockXPSource.CLASS_LEVEL_UP, DungeonClassXp.skyBlockXp(from, to));
    }

    public static List<String> levelUpLines(DungeonClassType type, int from, int to) {
        String bar = "<9><m>-----------------------------";
        List<String> lines = new ArrayList<>();
        lines.add(bar);
        lines.add("<3><l>  CLASS LEVEL UP <a>" + type.displayName() + " <8>" + from + "➜<3>" + to);
        lines.add("");
        lines.add("  <a><l>REWARDS");
        lines.add("    <8>+<b>" + DungeonClassXp.skyBlockXp(from, to) + " SkyBlock XP");
        lines.add(bar);
        return lines;
    }

    public static void maxOut(SkyBlockPlayer player) {
        for (DungeonClassType type : DungeonClassType.values()) {
            int from = level(player, type);
            if (xp(player, type) < DungeonClassXp.maxXp()) xpPoint(player, type).setValue(DungeonClassXp.maxXp());
            player.getSkyBlockExperience().addSourceXP(SkyBlockXPSource.CLASS_LEVEL_UP,
                    DungeonClassXp.skyBlockXp(from, DungeonClassXp.MAXIMUM_LEVEL));
        }
    }

    /** Mort's Ready Up toggle: ready up on arriving in a run without opening his menu. */
    public static boolean autoReady(SkyBlockPlayer player) {
        return Boolean.TRUE.equals(autoReadyPoint(player).getValue());
    }

    public static void setAutoReady(SkyBlockPlayer player, boolean enabled) {
        autoReadyPoint(player).setValue(enabled);
    }

    private static DatapointBoolean autoReadyPoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.DUNGEON_AUTO_READY, DatapointBoolean.class);
    }

    private static DatapointString selectedPoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.DUNGEON_CLASS, DatapointString.class);
    }

    private static DatapointLong xpPoint(SkyBlockPlayer player, DungeonClassType type) {
        SkyBlockDataHandler.Data data = switch (type) {
            case HEALER -> SkyBlockDataHandler.Data.HEALER_CLASS_XP;
            case MAGE -> SkyBlockDataHandler.Data.MAGE_CLASS_XP;
            case BERSERK -> SkyBlockDataHandler.Data.BERSERK_CLASS_XP;
            case ARCHER -> SkyBlockDataHandler.Data.ARCHER_CLASS_XP;
            case TANK -> SkyBlockDataHandler.Data.TANK_CLASS_XP;
        };
        return player.getSkyblockDataHandler().get(data, DatapointLong.class);
    }
}
