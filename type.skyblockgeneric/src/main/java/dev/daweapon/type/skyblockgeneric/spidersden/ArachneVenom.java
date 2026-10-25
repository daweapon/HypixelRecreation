package dev.daweapon.type.skyblockgeneric.spidersden;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Arachne's Venom Shot and the Keeper's poison. Venom drops a player to about 1% of their maximum health every
 * few seconds and, like on Hypixel, outlasts death. A Cow Hat stops both (wiki: Arachne, Arachne's Keeper).
 */
public final class ArachneVenom {
    static final String COW_HAT = "COW_HEAD";
    static final long VENOM_MILLIS = 12_000;
    static final long POISON_MILLIS = 6_000;
    static final int VENOM_PERIOD_SECONDS = 3;
    static final double VENOM_HEALTH_FRACTION = 0.01;
    static final double POISON_HEALTH_FRACTION = 0.02;

    private static final Map<UUID, Long> VENOM = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> POISON = new ConcurrentHashMap<>();
    private static boolean running;
    private static int seconds;

    private ArachneVenom() {
    }

    public static synchronized void start() {
        if (running) return;
        running = true;
        MinecraftServer.getSchedulerManager().buildTask(ArachneVenom::tick).repeat(TaskSchedule.seconds(1)).schedule();
    }

    public static boolean wearsCowHat(SkyBlockPlayer player) {
        ItemStack helmet = player.getHelmet();
        return !helmet.isAir() && SkyBlockItem.isSkyBlockItem(helmet)
                && COW_HAT.equals(new SkyBlockItem(helmet).getAttributeHandler().getTypeAsString());
    }

    public static boolean infect(SkyBlockPlayer player) {
        if (wearsCowHat(player)) return false;
        VENOM.put(player.getUuid(), System.currentTimeMillis() + VENOM_MILLIS);
        return true;
    }

    public static boolean poison(SkyBlockPlayer player) {
        if (wearsCowHat(player)) return false;
        POISON.put(player.getUuid(), System.currentTimeMillis() + POISON_MILLIS);
        player.addEffect(new Potion(PotionEffect.POISON, 0, (int) (POISON_MILLIS / 50)));
        return true;
    }

    public static boolean isVenomed(UUID player) {
        return VENOM.containsKey(player);
    }

    public static void clear(UUID player) {
        VENOM.remove(player);
        POISON.remove(player);
    }

    private static void tick() {
        seconds++;
        long now = System.currentTimeMillis();
        if (seconds % VENOM_PERIOD_SECONDS == 0) {
            VENOM.entrySet().removeIf(entry -> !drain(entry.getKey(), entry.getValue(), now, VENOM_HEALTH_FRACTION, true));
        }
        POISON.entrySet().removeIf(entry -> !drain(entry.getKey(), entry.getValue(), now, POISON_HEALTH_FRACTION, false));
    }

    private static boolean drain(UUID uuid, long until, long now, double fraction, boolean setTo) {
        if (now >= until) return false;
        Player online = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
        if (!(online instanceof SkyBlockPlayer player) || player.isDead()) return online != null;
        if (wearsCowHat(player)) return false;
        float floor = (float) Math.max(1, player.getMaxHealth() * VENOM_HEALTH_FRACTION);
        float target = setTo ? floor : (float) Math.max(floor, player.getHealth() - player.getMaxHealth() * fraction);
        if (player.getHealth() > target) player.setHealth(target);
        return true;
    }
}
