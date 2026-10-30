package dev.daweapon.type.skyblockgeneric.healing;

import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VitalityRegen {
    public static final double GROWTH_AMOUNT = 5;
    public static final long GROWTH_COOLDOWN_MS = 5_000;
    public static final long DRAIN_COOLDOWN_MS = 1_000;

    private static final List<String> GROWTH_PIECES = List.of("GROWTH_BOOTS", "GROWTH_LEGGINGS", "GROWTH_CHESTPLATE", "GROWTH_HELMET");
    private static final Map<UUID, Long> GROWTH_LAST = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> DRAIN_LAST = new ConcurrentHashMap<>();

    private VitalityRegen() {
    }

    public static double drainAmount(int level) {
        return Math.max(0, level) * 0.5;
    }

    public static boolean offCooldown(Map<UUID, Long> last, UUID id, long cooldownMs, long now) {
        Long previous = last.get(id);
        return previous == null || now - previous >= cooldownMs;
    }

    public static float add(float current, double amount, float max) {
        return (float) Math.max(0, Math.min(max, current + amount));
    }

    public static void grant(SkyBlockPlayer player, double amount) {
        if (amount <= 0) return;
        player.setVitality(add(player.getVitality(), amount, player.getMaxVitality()));
    }

    public static void onDrainHit(SkyBlockPlayer player, int level) {
        long now = System.currentTimeMillis();
        if (!offCooldown(DRAIN_LAST, player.getUuid(), DRAIN_COOLDOWN_MS, now)) return;
        DRAIN_LAST.put(player.getUuid(), now);
        grant(player, drainAmount(level));
    }

    public static void onMobKilled(SkyBlockPlayer player) {
        if (!wearsGrowthSet(player)) return;
        long now = System.currentTimeMillis();
        if (!offCooldown(GROWTH_LAST, player.getUuid(), GROWTH_COOLDOWN_MS, now)) return;
        GROWTH_LAST.put(player.getUuid(), now);
        grant(player, GROWTH_AMOUNT);
    }

    public static boolean wearsGrowthSet(SkyBlockPlayer player) {
        return GROWTH_PIECES.equals(List.of(idOf(player.getBoots()), idOf(player.getLeggings()),
                idOf(player.getChestplate()), idOf(player.getHelmet())));
    }

    private static String idOf(ItemStack stack) {
        SkyBlockItem item = new SkyBlockItem(stack);
        return item.isAir() ? "" : item.getAttributeHandler().getTypeAsString();
    }
}
