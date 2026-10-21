package dev.daweapon.type.skyblockgeneric.hunting.creature.trigger;

import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpawner;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

final class ScheduledSpawns {
    private static final Map<String, Long> NEXT_TIMED = new HashMap<>();
    private static int lastHour = -1;

    private ScheduledSpawns() {
    }

    static void start() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            try {
                tick();
            } catch (RuntimeException exception) {
                org.tinylog.Logger.error(exception, "Creature scheduled spawn pass failed");
            }
            return TaskSchedule.seconds(2);
        });
    }

    private static void tick() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null || SkyBlockGenericLoader.getLoadedPlayers().isEmpty()) return;
        timed(instance);
        hourly(instance);
    }

    private static void timed(Instance instance) {
        long now = System.currentTimeMillis();
        for (CreatureSpec spec : TriggerSupport.specs("TIMED")) {
            long due = NEXT_TIMED.computeIfAbsent(spec.id(), id -> now + delay(spec));
            if (now < due || !TriggerSupport.hasRoom(spec)) continue;
            Located located = randomLocation(spec, instance);
            if (located == null || !TriggerSupport.playerNear(instance, located.position())) continue;
            HuntingCreatureMob mob = TriggerSupport.spawn(spec, instance, located.position());
            NEXT_TIMED.put(spec.id(), now + delay(spec));
            if (mob == null) continue;
            var message = TriggerSupport.message(spec, "spawn", "{location}", located.name());
            if (message != null) SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> player.sendMessage(message));
            scheduleFlyAway(spec, mob);
        }
    }

    private static void scheduleFlyAway(CreatureSpec spec, HuntingCreatureMob mob) {
        int minutes = intDetail(spec, "despawnMinutes", 0);
        if (minutes <= 0) return;
        ScheduleUtility.delay(() -> {
            if (mob.isDead() || mob.isRemoved() || mob.isCaptured()) return;
            mob.remove();
            var message = TriggerSupport.message(spec, "flew_away");
            if (message != null) SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> player.sendMessage(message));
        }, minutes * 60 * 20);
    }

    private static void hourly(Instance instance) {
        int hour = SkyBlockCalendar.getHour();
        boolean changed = TriggerMath.hourChanged(lastHour, hour);
        lastHour = hour;
        if (!changed) return;
        for (CreatureSpec spec : TriggerSupport.specs("SKYBLOCK_HOUR")) {
            if (!TriggerSupport.hasRoom(spec) || !TriggerSupport.roll(null, spec, spec.spawn().trigger().chance())) continue;
            Pos position = freePoint(spec, instance);
            if (position == null) {
                Located located = randomLocation(spec, instance);
                position = located == null ? null : located.position();
            }
            if (position != null && TriggerSupport.playerNear(instance, position)) TriggerSupport.spawn(spec, instance, position);
        }
    }

    private static @Nullable Pos freePoint(CreatureSpec spec, Instance instance) {
        List<Pos> free = spec.spawn().points().stream()
                .filter(point -> instance.isChunkLoaded(point.blockX() >> 4, point.blockZ() >> 4))
                .filter(point -> CreatureSpawner.alive(spec.id()).stream().noneMatch(mob -> mob.getPosition().distance(point) < 3))
                .toList();
        return free.isEmpty() ? null : free.get(ThreadLocalRandom.current().nextInt(free.size()));
    }

    private static @Nullable Located randomLocation(CreatureSpec spec, Instance instance) {
        List<SkyBlockRegion> regions = SkyBlockRegion.getRegions().stream()
                .filter(region -> spec.spawn().regions().contains(region.getType())).toList();
        if (regions.isEmpty()) return null;
        SkyBlockRegion region = regions.get(ThreadLocalRandom.current().nextInt(regions.size()));
        Pos position = region.getRandomPositionForEntity(instance);
        if (position == null) return null;
        return new Located(spec.medium() == CreatureSpec.Medium.AIR ? position.add(0.5, 3, 0.5) : position.add(0.5, 0, 0.5), region.getType().getName());
    }

    private static long delay(CreatureSpec spec) {
        return TriggerMath.delayMillis(intDetail(spec, "minMinutes", 10), intDetail(spec, "maxMinutes", 10), ThreadLocalRandom.current());
    }

    private static int intDetail(CreatureSpec spec, String key, int fallback) {
        return spec.spawn().trigger().details().get(key) instanceof Number number ? number.intValue() : fallback;
    }

    private record Located(Pos position, String name) {
    }
}
