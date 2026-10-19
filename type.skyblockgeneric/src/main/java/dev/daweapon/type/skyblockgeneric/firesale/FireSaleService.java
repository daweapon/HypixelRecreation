package dev.daweapon.type.skyblockgeneric.firesale;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.RuneComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import redis.clients.jedis.Jedis;

import java.util.Optional;

public final class FireSaleService {
    public static final long DEFAULT_DURATION_MINUTES = 24 * 60;
    public static final int RUNE_LEVEL = 3;
    private static final long REFRESH_MILLIS = 2_000;
    private static final String ACTIVE_KEY = "hsb:firesale:active";
    private static final String SOLD_KEY = "hsb:firesale:sold";
    private static final String LAST_KEY = "hsb:firesale:last";
    private static final long ANNOUNCE_WINDOW_MILLIS = 60_000;

    private static volatile ActiveFireSale cached;
    private static String announcedKey = "";
    private static boolean announcedEnd = true;

    private FireSaleService() {
    }

    public static void initialize() {
        Thread.startVirtualThread(() -> {
            while (true) {
                try {
                    Thread.sleep(REFRESH_MILLIS);
                } catch (InterruptedException interrupted) {
                    return;
                }
                refreshAndAnnounce();
            }
        });
    }

    public static Optional<ActiveFireSale> latest() {
        return Optional.ofNullable(cached);
    }

    public static Optional<ActiveFireSale> active() {
        return latest().filter(sale -> !sale.ended(System.currentTimeMillis()));
    }

    public static @Nullable String lastSaleId() {
        if (SharedData.jedisPool() == null) return null;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return jedis.get(LAST_KEY);
        }
    }

    public static boolean start(FireSale sale, long durationMinutes) {
        if (SharedData.jedisPool() == null) return false;
        long now = System.currentTimeMillis();
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.set(SOLD_KEY, "0");
            jedis.set(ACTIVE_KEY, sale.id() + "|" + now + "|" + (now + durationMinutes * 60_000));
            jedis.set(LAST_KEY, sale.id());
        }
        cached = readActive();
        return true;
    }

    public static boolean end() {
        if (SharedData.jedisPool() == null) return false;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.del(ACTIVE_KEY);
        }
        cached = readActive();
        return true;
    }

    public static String purchase(SkyBlockPlayer player, ActiveFireSale active, String itemId) {
        FireSale sale = active.sale();
        if (active.ended(System.currentTimeMillis())) return "<c>This Fire Sale has ended!";
        if (!sale.items().contains(itemId)) return "<c>That item is not part of this Fire Sale!";
        String unavailable = sale.unavailableReason(itemId);
        if (unavailable != null) return "<c>" + unavailable;
        if (player.getGems() < sale.gems()) return "<c>You cannot afford this!";
        if (!reserve(sale)) {
            cached = readActive();
            return "<c>This Fire Sale is sold out!";
        }
        player.removeGems(sale.gems());
        player.addAndUpdateItem(buildItem(itemId));
        cached = readActive();
        return "<a>You purchased <6>" + sale.name() + " <a>for <a>" + StringUtility.commaify(sale.gems()) + " Gems<a>!";
    }

    public static SkyBlockItem buildItem(String itemId) {
        SkyBlockItem item = new SkyBlockItem(ItemType.get(itemId));
        if (item.hasComponent(RuneComponent.class)) item.getAttributeHandler().setRuneLevel(RUNE_LEVEL);
        return item;
    }

    private static boolean reserve(FireSale sale) {
        if (SharedData.jedisPool() == null) return false;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            long sold = jedis.incr(SOLD_KEY);
            if (!sale.unlimited() && sold > sale.stock()) {
                jedis.decr(SOLD_KEY);
                return false;
            }
            return true;
        }
    }

    private static @Nullable ActiveFireSale readActive() {
        if (SharedData.jedisPool() == null) return null;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            String raw = jedis.get(ACTIVE_KEY);
            if (raw == null) return null;
            String[] parts = raw.split("\\|");
            FireSale sale = FireSaleCatalogue.get(parts[0]);
            if (sale == null) return null;
            String sold = jedis.get(SOLD_KEY);
            return new ActiveFireSale(sale, Long.parseLong(parts[2]), sold == null ? 0 : Long.parseLong(sold));
        } catch (Exception exception) {
            Logger.error(exception, "Failed to read the active Fire Sale");
            return null;
        }
    }

    private static long startedAt() {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            String raw = jedis.get(ACTIVE_KEY);
            return raw == null ? 0 : Long.parseLong(raw.split("\\|")[1]);
        }
    }

    private static void refreshAndAnnounce() {
        try {
            if (SharedData.jedisPool() == null) return;
            cached = readActive();
            Optional<ActiveFireSale> current = active();
            if (current.isPresent()) {
                ActiveFireSale sale = current.get();
                String key = sale.sale().id() + sale.endsAt();
                if (key.equals(announcedKey)) return;
                announcedKey = key;
                announcedEnd = false;
                if (System.currentTimeMillis() - startedAt() > ANNOUNCE_WINDOW_MILLIS) return;
                broadcast("<6><b>FIRE SALE <r><e>A new Fire Sale started: <6>" + sale.sale().name() + "<e>! Visit <a>Elizabeth <e>in the Community Center.");
            } else if (!announcedEnd) {
                announcedEnd = true;
                broadcast("<6><b>FIRE SALE <r><c>The Fire Sale has ended.");
            }
        } catch (Exception exception) {
            Logger.error(exception, "Fire Sale announcement failed");
        }
    }

    private static void broadcast(String message) {
        MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
                .filter(SkyBlockPlayer.class::isInstance)
                .forEach(player -> ((SkyBlockPlayer) player).sendMessage(message));
    }
}
