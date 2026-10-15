package dev.daweapon.type.skyblockgeneric.command.groups.debug;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import dev.daweapon.LinkedField;
import dev.daweapon.codec.Codecs;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.commons.skyblock.CoopLinks;
import dev.daweapon.type.skyblockgeneric.data.CoopSync;
import dev.daweapon.type.skyblockgeneric.data.DataMutexService;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBankData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@CommandParameters(labels = "mutex",
        description = "Test the data mutex service against your own co-op container",
        usage = "/debug mutex <read|counter|stress|reset> [count]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class TestMutexCommand extends HypixelCommand {
    private static final LinkedField<UUID, Integer> TEST_COUNTER =
            LinkedField.create("coop", "_mutex_test_counter", Codecs.INT, 0, CoopLinks.COOP);
    private static final int MAX_STRESS_OPERATIONS = 20;
    private static final AtomicBoolean TRACKED = new AtomicBoolean();

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString operation = ArgumentType.String("operation");
        ArgumentInteger count = ArgumentType.Integer("count");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            execute((SkyBlockPlayer) sender, context.get(operation), 10);
        }, operation);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            execute((SkyBlockPlayer) sender, context.get(operation), context.get(count));
        }, operation, count);
    }

    private void execute(SkyBlockPlayer player, String operation, int count) {
        if (TRACKED.compareAndSet(false, true)) CoopSync.track(TEST_COUNTER);
        UUID profileId = player.getSkyblockDataHandler().getCurrentProfileId();
        UUID coopId = DataMutexService.coopIdFor(profileId);

        player.sendMessage("<e>Profile: <f>{}", profileId);
        player.sendMessage("<e>Shared container: <f>{} <7>({})", coopId, player.isCoop() ? "co-op" : "solo");

        switch (operation.toLowerCase()) {
            case "read" -> readBankData(player, profileId);
            case "counter" -> incrementCounter(player, coopId);
            case "stress" -> stressCounter(player, coopId, Math.clamp(count, 1, MAX_STRESS_OPERATIONS));
            case "reset" -> resetCounter(player, coopId);
            default -> player.sendMessage("<c>Invalid operation! Use: read, counter, stress, reset");
        }
    }

    private void readBankData(SkyBlockPlayer player, UUID profileId) {
        DataMutexService.Outcome outcome = DataMutexService.withSynchronizedData(profileId,
                SkyBlockDataHandler.Data.BANK_DATA,
                (DatapointBankData.BankData bankData) -> {
                    player.sendMessage("<7>Balance: <6>{}", bankData.getAmount());
                    player.sendMessage("<7>Balance limit: <6>{}", bankData.getBalanceLimit());
                    player.sendMessage("<7>Transactions: <f>{}", bankData.getTransactions().size());
                    player.sendMessage("<7>Last claimed interest: <f>{}", bankData.getLastClaimedInterest());
                    return null;
                });

        if (outcome == DataMutexService.Outcome.UNCHANGED) {
            player.sendMessage("<a>Read completed under the distributed lock, nothing written.");
        } else {
            player.sendMessage("<c>Read failed: <f>{}", outcome);
        }
    }

    private void incrementCounter(SkyBlockPlayer player, UUID coopId) {
        DataMutexService.Outcome outcome = DataMutexService.withSynchronizedField(coopId, TEST_COUNTER,
                current -> (current == null ? 0 : current) + 1);

        if (outcome == DataMutexService.Outcome.APPLIED) {
            player.sendMessage("<a>Counter is now <6>{}", readCounter(coopId));
        } else {
            player.sendMessage("<c>Increment failed: <f>{}", outcome);
        }
    }

    private void stressCounter(SkyBlockPlayer player, UUID coopId, int operations) {
        int startingValue = readCounter(coopId);
        player.sendMessage("<e>Running <f>{} <e>concurrent increments from <6>{}<e>...", operations, startingValue);

        AtomicInteger applied = new AtomicInteger();
        AtomicInteger finished = new AtomicInteger();

        for (int i = 0; i < operations; i++) {
            Thread.startVirtualThread(() -> {
                DataMutexService.Outcome outcome = DataMutexService.withSynchronizedField(coopId, TEST_COUNTER,
                        current -> (current == null ? 0 : current) + 1);
                if (outcome == DataMutexService.Outcome.APPLIED) applied.incrementAndGet();

                if (finished.incrementAndGet() != operations) return;

                int finalValue = readCounter(coopId);
                player.sendMessage("<7>Applied: <f>{}<7>/<f>{}", applied.get(), operations);
                player.sendMessage("<7>Expected: <6>{} <7>Actual: <6>{}", startingValue + applied.get(), finalValue);
                if (finalValue == startingValue + applied.get()) {
                    player.sendMessage("<a>No lost updates.");
                } else {
                    player.sendMessage("<c>Lost updates detected!");
                }
            });
        }
    }

    private void resetCounter(SkyBlockPlayer player, UUID coopId) {
        DataMutexService.Outcome outcome = DataMutexService.withSynchronizedField(coopId, TEST_COUNTER, current -> 0);
        player.sendMessage("<e>Counter reset: <f>{}", outcome);
    }

    private int readCounter(UUID coopId) {
        Integer value = SharedData.profile().getDirect(coopId, TEST_COUNTER);
        return value == null ? 0 : value;
    }
}
