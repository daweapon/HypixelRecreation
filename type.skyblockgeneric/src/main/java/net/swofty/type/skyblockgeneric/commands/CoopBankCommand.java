package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.datapoints.DatapointDouble;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Real-Hypixel co-op bank account: a coin pool shared by every member of the island's co-op, on top
// of each member's own personal balance. Any member can deposit or withdraw.
@CommandParameters(labels = "coopbank cbank",
        description = "Deposits or withdraws from your co-op's shared bank account",
        usage = "/coopbank [deposit|withdraw] [amount]",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class CoopBankCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord actionArg = ArgumentType.Word("action");
        ArgumentWord amountArg = ArgumentType.Word("amount");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            viewBalance((SkyBlockPlayer) sender);
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String action = context.get(actionArg);
            String amountText = context.get(amountArg);

            long amount;
            try {
                amount = net.swofty.type.skyblockgeneric.utility.CoinAmountParser.parseLong(amountText);
            } catch (NumberFormatException e) {
                player.sendMessage("<c>Usage: {}", "/coopbank <deposit|withdraw> <amount>");
                return;
            }

            if (amount <= 0) {
                player.sendMessage("<c>You must {} a positive amount!", action.equalsIgnoreCase("withdraw") ? "withdraw" : "deposit");
                return;
            }

            switch (action.toLowerCase()) {
                case "deposit" -> deposit(player, amount);
                case "withdraw" -> withdraw(player, amount);
                default -> player.sendMessage("<c>Usage: {}", "/coopbank <deposit|withdraw> <amount>");
            }
        }, actionArg, amountArg);
    }

    private void viewBalance(SkyBlockPlayer player) {
        CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());
        if (!CoopDatabase.isMember(coop, player.getUuid())) {
            player.sendMessage("<c>You are not in a co-op!");
            return;
        }
        player.sendMessage("<b>[Co-op Bank] <e>Balance: <6>{:,} coins", coop.bankBalance());
    }

    private void deposit(SkyBlockPlayer player, long amount) {
        CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());
        if (!CoopDatabase.isMember(coop, player.getUuid())) {
            player.sendMessage("<c>You are not in a co-op!");
            return;
        }

        DatapointDouble coins = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class);
        if (coins.getValue() < amount) {
            player.sendMessage("<c>You don't have that many coins!");
            return;
        }

        coins.setValue(coins.getValue() - amount);
        CoopDatabase.Coop updated = CoopDatabase.adjustBankBalance(coop.coopUUID(), amount);
        player.sendMessage("<b>[Co-op Bank] <e>Deposited <6>{:,} coins<e>. Balance: <6>{:,}", amount, updated.bankBalance());
    }

    private void withdraw(SkyBlockPlayer player, long amount) {
        CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());
        if (!CoopDatabase.isMember(coop, player.getUuid())) {
            player.sendMessage("<c>You are not in a co-op!");
            return;
        }

        // Checked-and-decremented atomically under the roster lock, so two concurrent withdrawals
        // can never together push the balance negative.
        CoopDatabase.Coop updated = CoopDatabase.tryWithdraw(coop.coopUUID(), amount);
        if (updated == null) {
            player.sendMessage("<c>The co-op bank only has {:,} coins!", coop.bankBalance());
            return;
        }

        DatapointDouble coins = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class);
        coins.setValue(coins.getValue() + amount);
        player.sendMessage("<b>[Co-op Bank] <e>Withdrew <6>{:,} coins<e>. Balance: <6>{:,}", amount, updated.bankBalance());
    }
}
