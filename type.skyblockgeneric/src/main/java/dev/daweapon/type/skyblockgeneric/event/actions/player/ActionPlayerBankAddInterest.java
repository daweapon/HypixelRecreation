package dev.daweapon.type.skyblockgeneric.event.actions.player;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.bank.BankInterestCalculator;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.data.DataMutexService;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBankData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Applies overdue bank interest to a player (and co-op members) when they rejoin
public class ActionPlayerBankAddInterest implements HypixelEventClass {


    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerSpawnEvent event) {
        if (event.isFirstSpawn()) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        Thread.startVirtualThread(() -> applyInterest(player));
    }

    private void applyInterest(SkyBlockPlayer player) {
        SkyBlockDataHandler handler = SkyBlockDataHandler.getUser(player);
        if (handler == null || !player.isOnline()) return;

        if (!player.isCoop()) {
            applySolo(player, handler);
            return;
        }

        double[] awarded = new double[1];
        DataMutexService.Outcome outcome = DataMutexService.withSynchronizedData(
                handler.getCurrentProfileId(),
                SkyBlockDataHandler.Data.BANK_DATA,
                (DatapointBankData.BankData latestBankData) -> {
                    Double given = applyInterestIfDue(latestBankData);
                    if (given == null) return null;

                    awarded[0] = given;
                    return latestBankData;
                });

        if (outcome == DataMutexService.Outcome.APPLIED) announce(player, awarded[0]);
    }

    private void applySolo(SkyBlockPlayer player, SkyBlockDataHandler handler) {
        DatapointBankData datapoint = handler.get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class);

        Double awarded;
        synchronized (datapoint) {
            DatapointBankData.BankData bankData = datapoint.getValue();

            awarded = applyInterestIfDue(bankData);
            if (awarded == null) return;

            datapoint.setValue(bankData);
        }

        announce(player, awarded);
    }

    private Double applyInterestIfDue(DatapointBankData.BankData bankData) {
        long difference = SkyBlockCalendar.getDifferenceInHours(bankData.getLastClaimedInterest());
        if (difference < SkyBlockCalendar.INTEREST_INTERVAL) return null;

        int times = (int) Math.min(difference / SkyBlockCalendar.INTEREST_INTERVAL, 2);
        double balance = bankData.getAmount();
        double totalToGive = 0;
        for (int i = 0; i < times; i++) {
            double interest = BankInterestCalculator.calculate(balance, bankData.getAccountTier(), bankData.getMuseumMilestone());
            double awarded = Math.clamp(interest, 0, bankData.getBalanceLimit() - balance);
            balance += awarded;
            totalToGive += awarded;
        }

        bankData.setLastClaimedInterest(SkyBlockCalendar.getElapsed());
        bankData.setLastInterest(totalToGive);
        bankData.setAmount(balance);

        if (totalToGive > 0) {
            bankData.addTransaction(new DatapointBankData.Transaction(
                    System.currentTimeMillis(),
                    totalToGive,
                    "<c>Bank Interest"
            ));
        }

        return totalToGive;
    }

    private void announce(SkyBlockPlayer player, double totalToGive) {
        if (totalToGive == 0) return;
        if (!player.isOnline()) return;

        player.sendMessage("<b>------------------------------------------------");
        player.sendMessage("<a>You have just received <6>{:,} coins<a> as bank interest!", totalToGive);
        player.sendMessage("<b>------------------------------------------------");
    }
}
