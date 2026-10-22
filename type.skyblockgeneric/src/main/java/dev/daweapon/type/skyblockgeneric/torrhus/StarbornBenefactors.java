package dev.daweapon.type.skyblockgeneric.torrhus;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public final class StarbornBenefactors {
    public enum Result { DONATED, NOT_ACTIVATED, CANNOT_AFFORD }

    private static final long COUNT_CACHE_MILLIS = 30_000;
    private static final Map<StarbornTemple, long[]> CACHE = new ConcurrentHashMap<>();

    private StarbornBenefactors() {
    }

    public static boolean activated(SkyBlockPlayer player, StarbornTemple temple) {
        return player.getHuntingData().counter(temple.activeKey()) > 0;
    }

    public static boolean benefactor(SkyBlockPlayer player, StarbornTemple temple) {
        return StarbornTemple.benefactorActive(player.getHuntingData().counter(temple.benefactorKey()), System.currentTimeMillis());
    }

    public static long millisLeft(SkyBlockPlayer player, StarbornTemple temple) {
        return Math.max(0, player.getHuntingData().counter(temple.benefactorKey()) - System.currentTimeMillis());
    }

    public static boolean onGalatea() {
        ServerType type = HypixelConst.getTypeLoader() == null ? null : HypixelConst.getTypeLoader().getType();
        return type == ServerType.SKYBLOCK_GALATEA || type == ServerType.SKYBLOCK_TORRHUS_CANYON;
    }

    public static long count(StarbornTemple temple) {
        long now = System.currentTimeMillis();
        long[] cached = CACHE.get(temple);
        if (cached != null && now - cached[1] < COUNT_CACHE_MILLIS) return cached[0];
        long value = cached == null ? 0 : cached[0];
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            value = jedis.zcount(temple.redisKey(), now, Double.POSITIVE_INFINITY);
        } catch (RuntimeException ignored) {
        }
        CACHE.put(temple, new long[]{value, now});
        return value;
    }

    public static double will(SkyBlockPlayer player, StarbornTemple temple) {
        if (!activated(player, temple)) return 0;
        double value = temple.will(count(temple));
        return benefactor(player, temple) ? value * StarbornTemple.BENEFACTOR_WILL_MULTIPLIER : value;
    }

    public static double willHunterFortune(SkyBlockPlayer player) {
        return onGalatea() ? will(player, StarbornTemple.DESERT) : 0;
    }

    public static double speedBonus(SkyBlockPlayer player) {
        return benefactor(player, StarbornTemple.DESERT) ? StarbornTemple.SPEED_BONUS : 0;
    }

    public static Result donate(SkyBlockPlayer player, StarbornTemple temple, int days, boolean withCoins) {
        if (!activated(player, temple)) return Result.NOT_ACTIVATED;
        if (withCoins) {
            if (player.getCoins() < StarbornTemple.coinCost(days)) return Result.CANNOT_AFFORD;
            player.removeCoins(StarbornTemple.coinCost(days));
        } else {
            if (player.getAmountInInventory(temple.donationItem()) < StarbornTemple.itemCost(days)) return Result.CANNOT_AFFORD;
            player.removeItemFromPlayer(temple.donationItem(), StarbornTemple.itemCost(days));
        }
        long expiry = StarbornTemple.extend(System.currentTimeMillis(), player.getHuntingData().counter(temple.benefactorKey()), days);
        player.getHuntingData().setCounter(temple.benefactorKey(), expiry);
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.zadd(temple.redisKey(), expiry, player.getUuid().toString());
            jedis.zremrangeByScore(temple.redisKey(), 0, System.currentTimeMillis());
        } catch (RuntimeException ignored) {
        }
        CACHE.remove(temple);
        player.sendMessage("<9><l>BENEFACTOR<f>: <e>You donated to the <a>{} Temple <e>and will receive its bonus for <d>+{}d<e>!",
                temple.displayName(), days);
        ChapterTriggers.fire(player, "starborn.benefactor");
        return Result.DONATED;
    }
}
