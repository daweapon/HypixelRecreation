package dev.daweapon.type.skyblockgeneric.item.tool;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Weather;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.event.actions.player.region.ActionRegionBlockBreak;
import dev.daweapon.type.skyblockgeneric.foraging.ForagingTreeManager;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class ToolInteractions {
    public static final Set<String> THROWING_AXES = Set.of("DECENT_AXE", "SERIOUSLY_DAMAGED_AXE", "JUNGLE_AXE", "TREECAPITATOR_AXE");
    public static final String SCULPTORS_AXE = "SCULPTORS_AXE";
    public static final String WEATHER_STICK = "WEATHER_STICK";
    public static final double THROWING_AXE_SWEEP_SCALE = 0.5;
    public static final int THROWING_AXE_RANGE = 20;
    public static final int THROWING_AXE_COOLDOWN_TICKS = 20;
    public static final int WEATHER_COOLDOWN_TICKS = 100;
    public static final int SCULPT_RANGE = 6;
    private static final long DUPLICATE_EVENT_WINDOW_MILLIS = 500;

    private ToolInteractions() {
    }

    public static void use(SkyBlockPlayer player, SkyBlockItem item, @Nullable Point clicked) {
        String id = item.getAttributeHandler().getTypeAsString();
        if (id == null) return;
        if (THROWING_AXES.contains(id)) throwAxe(player, item, clicked);
        else if (SCULPTORS_AXE.equals(id)) sculpt(player, clicked);
        else if (WEATHER_STICK.equals(id)) changeWeather(player, item);
    }

    public static boolean isInteractive(String id) {
        return id != null && (THROWING_AXES.contains(id) || SCULPTORS_AXE.equals(id) || WEATHER_STICK.equals(id));
    }

    public static @Nullable String wholeBarkKey(String logKey) {
        if (logKey.endsWith("_log")) return logKey.substring(0, logKey.length() - 4) + "_wood";
        if (logKey.endsWith("_stem")) return logKey.substring(0, logKey.length() - 5) + "_hyphae";
        return null;
    }

    public static Weather nextWeather(Weather current) {
        if (current.thunderLevel() > 0) return Weather.CLEAR;
        if (current.rainLevel() > 0) return Weather.THUNDER;
        return Weather.RAIN;
    }

    private static void throwAxe(SkyBlockPlayer player, SkyBlockItem item, @Nullable Point clicked) {
        if (HypixelConst.isIslandServer() || player.getInstance() == null) return;
        Point target = clicked != null ? clicked : player.getTargetBlockPosition(THROWING_AXE_RANGE);
        if (target == null) return;
        Block block = player.getInstance().getBlock(target);
        if (!ForagingTreeManager.isTreeLog(block)) return;
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(target);
        if (region == null) return;
        if (!player.getAbilityHandler().canUseAbility(item, THROWING_AXE_COOLDOWN_TICKS)) return;
        player.getAbilityHandler().startAbilityCooldown(item);
        ActionRegionBlockBreak.harvestTree(player, item, region, target, THROWING_AXE_SWEEP_SCALE);
    }

    private static void sculpt(SkyBlockPlayer player, @Nullable Point clicked) {
        if (!HypixelConst.isIslandServer() || player.getInstance() == null) return;
        Point target = clicked != null ? clicked : player.getTargetBlockPosition(SCULPT_RANGE);
        if (target == null) return;
        Instance instance = player.getInstance();
        Block block = instance.getBlock(target);
        String whole = wholeBarkKey(block.key().asString());
        if (whole == null) return;
        Block converted = Block.fromKey(whole);
        if (converted == null) return;
        instance.setBlock(target, converted);
    }

    private static void changeWeather(SkyBlockPlayer player, SkyBlockItem item) {
        if (!HypixelConst.isIslandServer() || player.getInstance() == null) {
            player.sendMessage("<c>You can only change the weather on your island!");
            return;
        }
        if (!player.getAbilityHandler().canUseAbility(item, WEATHER_COOLDOWN_TICKS)) {
            long remaining = player.getAbilityHandler().getRemainingCooldown(item, WEATHER_COOLDOWN_TICKS);
            if (remaining < WEATHER_COOLDOWN_TICKS * 50L - DUPLICATE_EVENT_WINDOW_MILLIS)
                player.sendMessage("<c>This ability is on cooldown for {}s.", Math.round(remaining / 1000f));
            return;
        }
        player.getAbilityHandler().startAbilityCooldown(item);
        player.getInstance().setWeather(nextWeather(player.getInstance().getWeather()));
    }
}
