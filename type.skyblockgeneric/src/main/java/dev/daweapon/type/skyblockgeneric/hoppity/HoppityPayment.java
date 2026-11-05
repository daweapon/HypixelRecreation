package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.bank.CoinPayment;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class HoppityPayment {
    private HoppityPayment() {
    }

    public static boolean canAfford(SkyBlockPlayer player, long price) {
        return CoinPayment.canAfford(player, price);
    }

    public static boolean pay(SkyBlockPlayer player, long price) {
        return CoinPayment.pay(player, price, "Hoppity");
    }

    public static double bankBalance(SkyBlockPlayer player) {
        return CoinPayment.bankBalance(player);
    }
}
