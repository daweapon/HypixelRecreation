package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class VacuumGate {
    private static final long HINT_COOLDOWN_MILLIS = 2500;
    private static final Map<UUID, Long> HINTED = new ConcurrentHashMap<>();

    private VacuumGate() {
    }

    static boolean blocks(SkyBlockPlayer player) {
        if (HeldItems.holdsVacuum(player)) return false;
        long now = System.currentTimeMillis();
        Long last = HINTED.get(player.getUuid());
        if (last == null || now - last >= HINT_COOLDOWN_MILLIS) {
            HINTED.put(player.getUuid(), now);
            player.sendMessage("<c>This pest can only be damaged with a Vacuum!");
        }
        return true;
    }
}
