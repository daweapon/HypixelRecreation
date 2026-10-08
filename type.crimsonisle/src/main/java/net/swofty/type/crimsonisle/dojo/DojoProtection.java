package net.swofty.type.crimsonisle.dojo;

import net.minestom.server.entity.Player;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Master Tao keeps empty-handed students in the Dojo from taking any damage. */
public final class DojoProtection {
    private static final String GRANTED = "<e>Master Tao sees you getting ready to do the Dojo and grants you damage protection.";
    private static final String ENTERED = "<e>As Master Tao's student, he protects you by preventing all damage in the Dojo.";
    private static final String LEFT = "<e>As you leave the Dojo, Master Tao's protection dissipates.";
    private static final String REMOVED = "<e>Master Tao sees you getting ready to leave the Dojo and removes your protection.";

    private static final Set<UUID> PROTECTED = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> INSIDE = ConcurrentHashMap.newKeySet();

    private DojoProtection() {
    }

    public static boolean isProtected(SkyBlockPlayer player) {
        return PROTECTED.contains(player.getUuid()) || DojoService.isInChallenge(player);
    }

    public static void update() {
        for (Player online : DojoArena.instance().getPlayers()) {
            if (online instanceof SkyBlockPlayer player) update(player);
        }
    }

    public static void forget(SkyBlockPlayer player) {
        PROTECTED.remove(player.getUuid());
        INSIDE.remove(player.getUuid());
    }

    private static void update(SkyBlockPlayer player) {
        if (DojoService.isInChallenge(player)) return;
        UUID uuid = player.getUuid();
        boolean inside = inDojo(player);
        boolean wasInside = INSIDE.contains(uuid);
        boolean empty = DojoRequirements.isEmptyHanded(player);

        if (!inside) {
            if (wasInside && PROTECTED.remove(uuid)) player.sendMessage(LEFT);
            INSIDE.remove(uuid);
            return;
        }
        INSIDE.add(uuid);
        boolean isProtected = PROTECTED.contains(uuid);
        if (empty && !isProtected) {
            PROTECTED.add(uuid);
            player.sendMessage(wasInside ? GRANTED : ENTERED);
        } else if (!empty && isProtected) {
            PROTECTED.remove(uuid);
            player.sendMessage(REMOVED);
        }
    }

    private static boolean inDojo(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        return region != null && region.getType() == RegionType.DOJO;
    }
}
