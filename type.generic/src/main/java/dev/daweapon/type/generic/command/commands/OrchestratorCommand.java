package dev.daweapon.type.generic.command.commands;

import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.protocol.objects.orchestrator.OrchestratorStatusProtocol;
import dev.daweapon.proxyapi.ProxyService;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

import java.util.concurrent.TimeUnit;

@CommandParameters(labels = "orchestrator",
        description = "Shows server and instance capacity from the orchestrator",
        usage = "/orchestrator [status]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class OrchestratorCommand extends HypixelCommand {
    private static final ProxyService ORCHESTRATOR = new ProxyService(ServiceType.ORCHESTRATOR);

    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            Thread.startVirtualThread(() -> showStatus(player));
        });
        command.addSyntax((sender, _) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            Thread.startVirtualThread(() -> showStatus(player));
        }, net.minestom.server.command.builder.arguments.ArgumentType.Literal("status"));
    }

    private static void showStatus(HypixelPlayer player) {
        try {
            OrchestratorStatusProtocol.StatusResponse response = ORCHESTRATOR
                    .handleRequest(new OrchestratorStatusProtocol(), new OrchestratorStatusProtocol.StatusRequest())
                    .get(5, TimeUnit.SECONDS);
            player.sendMessage("<b>Orchestrator capacity");
            if (response.types().isEmpty()) {
                player.sendMessage("<7>No servers have reported in.");
                return;
            }
            for (OrchestratorStatusProtocol.TypeStatus status : response.types()) {
                player.sendMessage("<e>" + status.type().name() + "<7>: <f>" + status.servers() + " server(s), "
                        + status.instances() + " instance(s), " + status.freeInstanceSlots() + " free slot(s), "
                        + status.onlinePlayers() + "/" + status.maxPlayers() + " players, "
                        + status.queued() + " queued");
            }
        } catch (Exception e) {
            player.sendMessage("<c>The orchestrator did not answer.");
        }
    }
}
