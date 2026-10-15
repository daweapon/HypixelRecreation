package dev.daweapon.type.skyblockgeneric.fishing;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;

/**
 * Lets a server claim a reel-in before it becomes a catch, such as Kuudra's supply crates. A hook that returns
 * true consumed the reel.
 */
public final class FishingReelHooks {
    private static final List<BiPredicate<SkyBlockPlayer, Pos>> HOOKS = new CopyOnWriteArrayList<>();

    private FishingReelHooks() {
    }

    public static void register(BiPredicate<SkyBlockPlayer, Pos> hook) {
        HOOKS.add(hook);
    }

    public static boolean consume(SkyBlockPlayer player, Pos bobber) {
        for (BiPredicate<SkyBlockPlayer, Pos> hook : HOOKS) {
            if (hook.test(player, bobber)) return true;
        }
        return false;
    }
}
