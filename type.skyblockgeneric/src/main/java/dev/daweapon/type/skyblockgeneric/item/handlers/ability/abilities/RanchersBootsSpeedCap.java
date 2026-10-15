package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.generic.gui.HypixelSignGUI;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.region.rules.TempleLockdown;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RanchersBootsSpeedCap {
    public static final int MIN_CAP = 20;
    public static final int MAX_CAP = 1000;
    private static final long PROMPT_COOLDOWN_MS = 1000;
    private static final Map<UUID, Long> LAST_PROMPT = new ConcurrentHashMap<>();

    private RanchersBootsSpeedCap() {
    }

    public static boolean isWearing(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null || player.getBoots().isAir()) return false;
        return new SkyBlockItem(player.getBoots()).getAttributeHandler().getPotentialType() == ItemType.RANCHERS_BOOTS;
    }

    public static double cap(SkyBlockPlayer player) {
        if (TempleLockdown.isLocked(player) || !isWearing(player)) return Double.MAX_VALUE;
        int chosen = stored(player);
        return chosen <= 0 ? Double.MAX_VALUE : chosen;
    }

    public static void prompt(SkyBlockPlayer player) {
        if (TempleLockdown.isLocked(player)) {
            player.sendMessage("<c>The Jungle Temple's magic suppresses your Rancher's Boots!");
            return;
        }
        long now = System.currentTimeMillis();
        Long last = LAST_PROMPT.get(player.getUuid());
        if (last != null && now - last < PROMPT_COOLDOWN_MS) return;
        LAST_PROMPT.put(player.getUuid(), now);

        new HypixelSignGUI(player).open(new String[]{"Enter your max", "Speed (" + MIN_CAP + "-" + MAX_CAP + ")"})
                .thenAccept(input -> MinecraftServer.getSchedulerManager().scheduleNextTick(() -> apply(player, input)));
    }

    private static void apply(SkyBlockPlayer player, String input) {
        if (!player.isOnline() || input == null || input.isBlank()) return;
        if (TempleLockdown.isLocked(player) || !isWearing(player)) return;
        int value;
        try {
            value = Integer.parseInt(input.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            player.sendMessage("<c>That isn't a valid Speed!");
            return;
        }
        int clamped = Math.max(MIN_CAP, Math.min(MAX_CAP, value));
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.RANCHERS_SPEED_CAP, DatapointInteger.class).setValue(clamped);
        player.sendMessage("<a>Your maximum Speed is now <f>" + clamped + "<a>!");
    }

    private static int stored(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.RANCHERS_SPEED_CAP, DatapointInteger.class).getValue();
    }
}
