package dev.daweapon.type.skyblockgeneric.garden.greenhouse;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class GreenhouseCreatures {
    private static final Map<String, GreenhouseMobs.Summoned> LIVE = new HashMap<>();

    private GreenhouseCreatures() {
    }

    private static String key(Instance instance, int plot, int anchor, String tag) {
        return System.identityHashCode(instance) + ":" + plot + ":" + anchor + ":" + tag;
    }

    public static synchronized boolean alive(Instance instance, int plot, int anchor, String tag) {
        GreenhouseMobs.Summoned mob = LIVE.get(key(instance, plot, anchor, tag));
        return mob != null && !mob.isRemoved() && !mob.isDead();
    }

    public static synchronized void spawn(Instance instance, int plot, int anchor, String tag, GreenhouseMobs.Summoned mob, Pos at, Consumer<SkyBlockPlayer> onSlain) {
        mob.spawnAt(instance, at, onSlain);
        LIVE.put(key(instance, plot, anchor, tag), mob);
    }

    public static synchronized void dismiss(Instance instance, int plot, int anchor, String tag) {
        GreenhouseMobs.Summoned mob = LIVE.remove(key(instance, plot, anchor, tag));
        if (mob != null && !mob.isRemoved()) mob.remove();
    }
}
