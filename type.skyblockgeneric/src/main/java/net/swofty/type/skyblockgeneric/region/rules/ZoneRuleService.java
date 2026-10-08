package net.swofty.type.skyblockgeneric.region.rules;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Applies every registered {@link ZoneRule}: pauses a player's buffs on the way in and hands them back unchanged on
 * the way out, and caps their Health while inside. The cap is read by the stat calculation, so it holds whatever
 * gear, pets or potions add.
 */
public final class ZoneRuleService {
    public static final String PAUSED = "<7>Your active effects and <c>God Potion <7>are paused here.";
    public static final String RESUMED = "<7>Your active effects and <c>God Potion <7>have resumed.";
    private static final int CHECK_TICKS = 5;

    private static final List<ZoneRule> RULES = new CopyOnWriteArrayList<>();
    private static final Map<UUID, PausedBuffs> PAUSED_BUFFS = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> HEALTH_CAPS = new ConcurrentHashMap<>();
    private static boolean started;

    private ZoneRuleService() {
    }

    public static synchronized void register(ZoneRule rule) {
        RULES.removeIf(existing -> existing.id().equals(rule.id()));
        RULES.add(rule);
        if (started) return;
        started = true;
        MinecraftServer.getSchedulerManager().buildTask(ZoneRuleService::tick).repeat(TaskSchedule.tick(CHECK_TICKS)).schedule();
        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, event -> {
            if (event.getPlayer() instanceof SkyBlockPlayer player) forget(player);
        });
    }

    public static boolean isPaused(SkyBlockPlayer player) {
        return PAUSED_BUFFS.containsKey(player.getUuid());
    }

    public static ItemStatistics capStatistics(SkyBlockPlayer player, ItemStatistics total) {
        Double cap = HEALTH_CAPS.get(player.getUuid());
        if (cap == null || total.getOverall(ItemStatistic.HEALTH) <= cap) return total;
        return total.withExactValue(ItemStatistic.HEALTH, cap);
    }

    public static double capMaxHealth(SkyBlockPlayer player, double maxHealth) {
        Double cap = HEALTH_CAPS.get(player.getUuid());
        return cap == null ? maxHealth : Math.min(cap, maxHealth);
    }

    public static void refresh(SkyBlockPlayer player) {
        boolean suspend = false;
        Double cap = null;
        for (ZoneRule rule : RULES) {
            if (!rule.applies().test(player)) continue;
            suspend |= rule.suspendsBuffs();
            if (rule.maxHealth() != null) cap = cap == null ? rule.maxHealth() : Math.min(cap, rule.maxHealth());
        }
        applyCap(player, cap);
        applySuspension(player, suspend);
    }

    private static void tick() {
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (player.getInstance() == null || player.getSkyblockDataHandler() == null) continue;
            refresh(player);
        }
    }

    private static void applyCap(SkyBlockPlayer player, Double cap) {
        UUID uuid = player.getUuid();
        if (cap == null) {
            HEALTH_CAPS.remove(uuid);
            return;
        }
        HEALTH_CAPS.put(uuid, cap);
        if (player.getHealth() > cap) player.setHealth(cap.floatValue());
    }

    private static void applySuspension(SkyBlockPlayer player, boolean suspend) {
        UUID uuid = player.getUuid();
        PausedBuffs paused = PAUSED_BUFFS.get(uuid);
        if (suspend && paused == null) {
            paused = new PausedBuffs(player);
            PAUSED_BUFFS.put(uuid, paused);
            if (paused.pause()) player.sendMessage(PAUSED);
        } else if (suspend) {
            paused.sweep();
        } else if (paused != null) {
            PAUSED_BUFFS.remove(uuid);
            if (paused.restore()) player.sendMessage(RESUMED);
        }
    }

    private static void forget(SkyBlockPlayer player) {
        HEALTH_CAPS.remove(player.getUuid());
        PausedBuffs paused = PAUSED_BUFFS.remove(player.getUuid());
        if (paused != null) paused.forget();
    }
}
