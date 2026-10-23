package dev.daweapon.type.skyblockgeneric.elections.perks;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor.Perk;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TaxPerks {
    public static final double NPC_SHOP_MARKUP = 0.10;
    public static final long CHAT_FEE = 5;
    public static final long CHAT_FEE_INTERVAL_MILLIS = 60_000;
    public static final long ISLAND_TOLL = 2;

    private static final Map<UUID, Long> lastChatFee = new ConcurrentHashMap<>();

    private TaxPerks() {}

    public static double auctionAndBazaarMultiplier() {
        return multiplier(ElectionPerks.current());
    }

    static double multiplier(ActivePerks perks) {
        if (perks.isActive(Perk.ANARCHY)) return 0D;
        return perks.factor(Perk.QUAD_TAXES) * perks.factor(Perk.ECONOMY_RESTORATION);
    }

    public static double npcShopMultiplier() {
        return ElectionPerks.isActive(Perk.ECONOMY_RESTORATION) ? 1D + NPC_SHOP_MARKUP : 1D;
    }

    public static long chatFee() {
        return ElectionPerks.isActive(Perk.ECONOMY_RESTORATION) ? CHAT_FEE : 0;
    }

    public static long islandToll() {
        return ElectionPerks.isActive(Perk.ECONOMY_RESTORATION) ? ISLAND_TOLL : 0;
    }

    public static boolean chargeChatFee(SkyBlockPlayer player) {
        long fee = chatFee();
        if (fee <= 0) return true;
        long now = System.currentTimeMillis();
        Long last = lastChatFee.get(player.getUuid());
        if (last != null && now - last < CHAT_FEE_INTERVAL_MILLIS) return true;
        if (player.getCoins() < fee) {
            player.sendMessage(Text.of("<c>Under Dante's rule chatting costs <6>{} coins <c>a minute, and you can't afford it!", fee));
            return false;
        }
        player.removeCoins(fee);
        lastChatFee.put(player.getUuid(), now);
        player.sendMessage(Text.of("<6>-{} Coins <7>(Dante's chat fee)", fee));
        return true;
    }

    public static boolean chargeIslandToll(SkyBlockPlayer player) {
        long toll = islandToll();
        if (toll <= 0) return true;
        if (player.getCoins() < toll) {
            player.sendMessage(Text.of("<c>Under Dante's rule travelling between islands costs <6>{} coins<c>, and you can't afford it!", toll));
            return false;
        }
        player.removeCoins(toll);
        player.sendMessage(Text.of("<6>-{} Coins <7>(Dante's island toll)", toll));
        return true;
    }
}
