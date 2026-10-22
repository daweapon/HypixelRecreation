package dev.daweapon.type.skyblockgeneric.beacon;

import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.Potion;
import net.minestom.server.timer.TaskSchedule;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BeaconEffectTicker {
    private static final int PERIOD_TICKS = 20;
    private static final int EFFECT_TICKS = 100;
    private static final Map<UUID, Instance> RUNNING = new ConcurrentHashMap<>();

    private BeaconEffectTicker() {
    }

    public static void ensureRunning(SkyBlockIsland island) {
        Instance instance = island.getIslandInstance();
        if (instance == null || RUNNING.get(island.getIslandID()) == instance) return;
        RUNNING.put(island.getIslandID(), instance);
        instance.scheduler().submitTask(() -> {
            if (island.getIslandInstance() != instance) {
                RUNNING.remove(island.getIslandID(), instance);
                return TaskSchedule.stop();
            }
            apply(island, instance);
            return TaskSchedule.tick(PERIOD_TICKS);
        });
    }

    private static void apply(SkyBlockIsland island, Instance instance) {
        BeaconData data = BeaconService.get(island);
        if (data == null) return;
        for (BeaconEffect effect : data.activeEffects(System.currentTimeMillis())) {
            if (effect.potion() == null) continue;
            for (Player player : instance.getPlayers()) {
                player.addEffect(new Potion(effect.potion(), (byte) effect.amplifier(), EFFECT_TICKS));
            }
        }
    }
}
