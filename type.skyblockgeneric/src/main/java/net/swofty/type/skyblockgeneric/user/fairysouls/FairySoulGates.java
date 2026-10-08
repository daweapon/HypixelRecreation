package net.swofty.type.skyblockgeneric.user.fairysouls;

import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/** Fairy souls that need something before they can be collected, such as Bruuh's toll in Dragontail. */
public final class FairySoulGates {
    private static final Map<Integer, Gate> GATES = new ConcurrentHashMap<>();

    private FairySoulGates() {
    }

    public interface Denial {
        void deny(SkyBlockPlayer player);
    }

    private record Gate(Predicate<SkyBlockPlayer> allowed, Denial denial) {
    }

    public static void register(int soulId, Predicate<SkyBlockPlayer> allowed, Denial denial) {
        GATES.put(soulId, new Gate(allowed, denial));
    }

    public static boolean mayCollect(SkyBlockPlayer player, FairySoul soul) {
        Gate gate = GATES.get(soul.getId());
        if (gate == null || gate.allowed().test(player)) return true;
        gate.denial().deny(player);
        return false;
    }
}
