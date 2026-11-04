package dev.daweapon.type.skyblockgeneric.rabbits;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public final class Chocobits {
    public static final int TOTAL = 33_333;
    public static final int UNLOCK_FACTORY_LEVEL = 6;
    public static final long EXTENSION_COINS = 1_000_000L;
    public static final long SKYBLOCK_YEAR_SECONDS = 124L * 3600L;
    public static final int RELEASE_YEARS_PAID = 2;
    private static final String OWNERS_KEY = "hsb:chocobit:owners";
    private static final int CLAIM_ATTEMPTS = 64;

    private Chocobits() {
    }

    public static boolean isUnlocked(ChocolateFactoryData data) {
        return data.getFactoryLevel() >= UNLOCK_FACTORY_LEVEL;
    }

    public static String label(int number) {
        return "Chocobit #" + number;
    }

    public static long releasePayout(ChocolateFactoryData data) {
        return (long) (data.getBaseChocolatePerSecond() * ChocolateFactoryData.CHOCOBIT_MULTIPLIER
                * SKYBLOCK_YEAR_SECONDS * RELEASE_YEARS_PAID);
    }

    public static void claim(SkyBlockPlayer player, Consumer<@Nullable Integer> onClaimed) {
        UUID profile = player.getProfiles().getCurrentlySelected();
        Thread.startVirtualThread(() -> {
            Integer number = reserve(profile);
            MinecraftServer.getSchedulerManager().scheduleNextTick(() -> onClaimed.accept(number));
        });
    }

    public static void award(SkyBlockPlayer player) {
        claim(player, number -> {
            if (!player.isOnline()) {
                if (number != null) release(player.getProfiles().getCurrentlySelected(), List.of(number));
                return;
            }
            if (number == null) {
                long[] gained = {0};
                ChocolateFactoryHelper.mutateData(player, data -> {
                    gained[0] = releasePayout(data) / RELEASE_YEARS_PAID;
                    data.addChocolate(gained[0]);
                });
                player.sendMessage("<d><l>HOPPITY'S HUNT</l> <7>Every Chocobit already has an owner! <6>+{} Chocolate",
                        ChocolateFactoryHelper.formatChocolate(gained[0]));
                return;
            }
            int year = SkyBlockCalendar.getYear();
            ChocolateFactoryHelper.updateProduction(player);
            ChocolateFactoryHelper.mutateData(player, data -> data.getChocobits().put(number, year));
            player.sendMessage("<d><l>NEW CHOCOBIT! <c>" + label(number) + " <7>moved into your <c>Chocobit Tower<7>! <6>+0.2x Chocolate <7>per second");
            player.sendMessage("<7>Pay <6>1M Coins <7>in the <c>Chocobit Tower <7>before the SkyBlock year ends to keep it!");
        });
    }

    public static boolean extend(SkyBlockPlayer player, int number) {
        ChocolateFactoryData current = ChocolateFactoryHelper.getData(player);
        Integer paidThrough = current.getChocobits().get(number);
        if (paidThrough == null || paidThrough > SkyBlockCalendar.getYear()) return false;
        if (player.getCoins() < EXTENSION_COINS) return false;
        player.removeCoins(EXTENSION_COINS);
        ChocolateFactoryHelper.mutateData(player, data -> data.getChocobits().computeIfPresent(number, (key, year) -> year + 1));
        return true;
    }

    public static int extendAll(SkyBlockPlayer player) {
        int extended = 0;
        for (int number : List.copyOf(ChocolateFactoryHelper.getData(player).getChocobits().keySet())) {
            if (extend(player, number)) extended++;
        }
        return extended;
    }

    public static void expire(SkyBlockPlayer player) {
        int year = SkyBlockCalendar.getYear();
        ChocolateFactoryData current = ChocolateFactoryHelper.getData(player);
        if (current.getChocobits().values().stream().noneMatch(paid -> paid < year)) return;
        List<Integer> lost = new ArrayList<>();
        long[] payout = {0};
        ChocolateFactoryHelper.mutateData(player, data -> {
            long each = releasePayout(data);
            for (Map.Entry<Integer, Integer> entry : List.copyOf(data.getChocobits().entrySet())) {
                if (entry.getValue() >= year) continue;
                lost.add(entry.getKey());
                data.getChocobits().remove(entry.getKey());
                payout[0] += each;
            }
            data.addChocolate(payout[0]);
        });
        if (lost.isEmpty()) return;
        for (int number : lost) {
            player.sendMessage("<c><l>HOPPITY'S HUNT:</l> <7>You lost <c>" + label(number) + "<7>!");
            player.sendMessage("<7>They are now <a><l>FREE</l><7>! Until someone else finds them...");
        }
        player.sendMessage("<7>As thanks, your Chocobits left you <6>{} Chocolate<7>.",
                ChocolateFactoryHelper.formatChocolate(payout[0]));
        release(player.getProfiles().getCurrentlySelected(), lost);
    }

    public static void release(UUID profile, List<Integer> numbers) {
        List<Integer> copy = List.copyOf(numbers);
        Thread.startVirtualThread(() -> {
            if (SharedData.jedisPool() == null) return;
            try (Jedis jedis = SharedData.jedisPool().getResource()) {
                for (int number : copy) {
                    String field = String.valueOf(number);
                    if (profile.toString().equals(jedis.hget(OWNERS_KEY, field))) jedis.hdel(OWNERS_KEY, field);
                }
            } catch (Exception exception) {
                Logger.error(exception, "Failed to release Chocobits {}", copy);
            }
        });
    }

    public static long ownedGlobally() {
        if (SharedData.jedisPool() == null) return 0;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return jedis.hlen(OWNERS_KEY);
        } catch (Exception exception) {
            return 0;
        }
    }

    public static String formatCoins(long coins) {
        return StringUtility.commaify(coins);
    }

    private static @Nullable Integer reserve(UUID profile) {
        if (SharedData.jedisPool() == null) return null;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int attempt = 0; attempt < CLAIM_ATTEMPTS; attempt++) {
                int number = random.nextInt(1, TOTAL + 1);
                if (jedis.hsetnx(OWNERS_KEY, String.valueOf(number), profile.toString()) == 1L) return number;
            }
            if (jedis.hlen(OWNERS_KEY) >= TOTAL) return null;
            for (int number = 1; number <= TOTAL; number++) {
                if (jedis.hsetnx(OWNERS_KEY, String.valueOf(number), profile.toString()) == 1L) return number;
            }
        } catch (Exception exception) {
            Logger.error(exception, "Failed to reserve a Chocobit");
        }
        return null;
    }
}
