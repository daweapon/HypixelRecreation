package net.swofty.type.game.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.protocol.objects.orchestrator.GameHeartbeatProtocol;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class InstanceHeartbeat {
    private static final int START_DELAY_SECONDS = 5;

    private InstanceHeartbeat() {}

    public record Identity(Supplier<UUID> uuid, Supplier<String> shortName, IntSupplier maxPlayers, IntSupplier onlinePlayers) {}

    public static GameHeartbeatProtocol.HeartbeatMessage message(InstanceHost host, Identity identity) {
        return new GameHeartbeatProtocol.HeartbeatMessage(
                identity.uuid().get(),
                identity.shortName().get(),
                host.type(),
                identity.maxPlayers().getAsInt(),
                identity.onlinePlayers().getAsInt(),
                host.instances(),
                List.of(),
                host.remainingSlots());
    }

    public static void start(InstanceHost host, Identity identity, Consumer<GameHeartbeatProtocol.HeartbeatMessage> sender) {
        MinecraftServer.getSchedulerManager()
                .buildTask(() -> sender.accept(message(host, identity)))
                .delay(TaskSchedule.seconds(START_DELAY_SECONDS))
                .repeat(TaskSchedule.seconds(1))
                .schedule();
    }
}
