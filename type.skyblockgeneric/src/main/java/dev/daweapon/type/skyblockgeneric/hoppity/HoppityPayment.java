package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.data.DataMutexService;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBankData;
import dev.daweapon.type.skyblockgeneric.bank.PersonalBankService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class HoppityPayment {
    private HoppityPayment() {
    }

    public static boolean canAfford(SkyBlockPlayer player, long price) {
        return player.getCoins() + bankBalance(player) >= price;
    }

    public static boolean pay(SkyBlockPlayer player, long price) {
        double fromPurse = Math.min(player.getCoins(), price);
        double fromBank = price - fromPurse;
        if (fromBank > bankBalance(player)) return false;
        if (fromBank > 0 && !withdrawFromBank(player, fromBank)) return false;
        if (fromPurse > 0) player.removeCoins(fromPurse);
        return true;
    }

    public static double bankBalance(SkyBlockPlayer player) {
        return PersonalBankService.data(player).getAmount();
    }

    private static boolean withdrawFromBank(SkyBlockPlayer player, double amount) {
        if (!player.isCoop()) {
            DatapointBankData datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class);
            DatapointBankData.BankData bank = datapoint.getValue();
            if (amount > bank.getAmount()) return false;
            bank.removeAmount(amount);
            bank.addTransaction(new DatapointBankData.Transaction(System.currentTimeMillis(), -amount, "Hoppity"));
            datapoint.setValue(bank);
            return true;
        }
        DataMutexService.Outcome outcome = DataMutexService.withSynchronizedData(
                player.getSkyblockDataHandler().getCurrentProfileId(),
                SkyBlockDataHandler.Data.BANK_DATA,
                (DatapointBankData.BankData latest) -> {
                    if (amount > latest.getAmount()) return null;
                    latest.removeAmount(amount);
                    latest.addTransaction(new DatapointBankData.Transaction(System.currentTimeMillis(), -amount, "Hoppity"));
                    return latest;
                });
        return outcome == DataMutexService.Outcome.APPLIED;
    }
}
