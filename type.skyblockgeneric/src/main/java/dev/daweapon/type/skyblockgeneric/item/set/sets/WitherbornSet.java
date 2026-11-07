package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.dungeons.DungeonGhosts;
import dev.daweapon.type.skyblockgeneric.item.armor.WitherArmor;
import dev.daweapon.type.skyblockgeneric.item.armor.WitherbornCycle;
import dev.daweapon.type.skyblockgeneric.item.armor.WitherbornMinion;
import dev.daweapon.type.skyblockgeneric.item.armor.WornArmor;
import dev.daweapon.type.skyblockgeneric.item.set.impl.SetRepeatable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Witherborn, the full set bonus of every Wither Armor set: a wither minion every 30 seconds, at most one out. */
public class WitherbornSet implements SetRepeatable {
    private static final int PERIOD_TICKS = 10;
    private static final Map<UUID, WitherbornCycle> CYCLES = new ConcurrentHashMap<>();
    private static final Map<UUID, WitherbornMinion> MINIONS = new ConcurrentHashMap<>();

    @Override
    public Task getTask(Scheduler scheduler) {
        return scheduler.buildTask(this::tick).repeat(TaskSchedule.tick(PERIOD_TICKS)).schedule();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        Set<UUID> online = new HashSet<>();
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            online.add(player.getUuid());
            try {
                tickPlayer(player, now);
            } catch (RuntimeException ignored) {
            }
        }
        for (UUID gone : Set.copyOf(MINIONS.keySet())) {
            if (!online.contains(gone)) dismiss(gone);
        }
        CYCLES.keySet().retainAll(online);
    }

    private void tickPlayer(SkyBlockPlayer player, long now) {
        WitherbornMinion minion = MINIONS.get(player.getUuid());
        if (minion != null && (minion.isRemoved() || minion.exploded())) {
            MINIONS.remove(player.getUuid());
            minion = null;
        }
        boolean active = player.getInstance() != null && !player.isDead() && !DungeonGhosts.isGhost(player)
                && WitherArmor.witherborn(WornArmor.cachedIds(player));
        if (!active) {
            dismiss(player.getUuid());
            WitherbornCycle cycle = CYCLES.get(player.getUuid());
            if (cycle != null) cycle.reset();
            return;
        }
        WitherbornCycle cycle = CYCLES.computeIfAbsent(player.getUuid(), _ -> new WitherbornCycle());
        if (!cycle.tick(now, minion == null ? 0 : 1)) return;
        WitherbornMinion spawned = new WitherbornMinion(player);
        spawned.setInstance(player.getInstance(), player.getPosition().add(0, 2, 0));
        MINIONS.put(player.getUuid(), spawned);
    }

    private static void dismiss(UUID owner) {
        WitherbornMinion minion = MINIONS.remove(owner);
        if (minion != null && !minion.isRemoved()) minion.remove();
    }
}
