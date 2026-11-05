package dev.daweapon.type.skyblockgeneric.bank;

import dev.daweapon.type.skyblockgeneric.data.DataMutexService;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBankData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** Pays a price from the purse first and the bank for the rest, as Hypixel's purse-then-bank purchases do. */
public final class CoinPayment {
    private CoinPayment() {
    }

    public static boolean canAfford(SkyBlockPlayer player, long price) {
        return player.getCoins() + bankBalance(player) >= price;
    }

    public static boolean pay(SkyBlockPlayer player, long price, String transactionLabel) {
        if (price <= 0) return true;
        double fromPurse = Math.min(player.getCoins(), price);
        double fromBank = price - fromPurse;
        if (fromBank > bankBalance(player)) return false;
        if (fromBank > 0 && !withdrawFromBank(player, fromBank, transactionLabel)) return false;
        if (fromPurse > 0) player.removeCoins(fromPurse);
        return true;
    }

    public static double bankBalance(SkyBlockPlayer player) {
        return PersonalBankService.data(player).getAmount();
    }

    private static boolean withdrawFromBank(SkyBlockPlayer player, double amount, String label) {
        if (!player.isCoop()) {
            DatapointBankData datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class);
            DatapointBankData.BankData bank = datapoint.getValue();
            if (amount > bank.getAmount()) return false;
            bank.removeAmount(amount);
            bank.addTransaction(new DatapointBankData.Transaction(System.currentTimeMillis(), -amount, label));
            datapoint.setValue(bank);
            return true;
        }
        DataMutexService.Outcome outcome = DataMutexService.withSynchronizedData(
                player.getSkyblockDataHandler().getCurrentProfileId(),
                SkyBlockDataHandler.Data.BANK_DATA,
                (DatapointBankData.BankData latest) -> {
                    if (amount > latest.getAmount()) return null;
                    latest.removeAmount(amount);
                    latest.addTransaction(new DatapointBankData.Transaction(System.currentTimeMillis(), -amount, label));
                    return latest;
                });
        return outcome == DataMutexService.Outcome.APPLIED;
    }
}
