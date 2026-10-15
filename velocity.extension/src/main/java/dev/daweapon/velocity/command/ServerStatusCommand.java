package dev.daweapon.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.daweapon.velocity.gamemanager.GameManager;
import dev.daweapon.velocity.gamemanager.GameManager.GameServer;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// "/serverstatus" command that lists all registered game servers grouped by type with basic status info.
public class ServerStatusCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        invocation.source().sendMessage(Text.of("***** Server Status *****"));

        Map<ServerType, ArrayList<GameServer>> serverMap = GameManager.getServers();
        if (serverMap.isEmpty()) {
            invocation.source().sendMessage(Text.of("No servers are currently registered."));
            return;
        }

        for (Map.Entry<ServerType, ArrayList<GameServer>> entry : serverMap.entrySet()) {
            ServerType type = entry.getKey();
            List<GameServer> gameServers = entry.getValue();

            invocation.source().sendMessage(Text.of("{}:", type.name()));

            if (gameServers.isEmpty()) {
                invocation.source().sendMessage(Text.of(" - No servers of this type are currently online."));
                continue;
            }

            for (GameServer server : gameServers) {
                invocation.source().sendMessage(Text.of(" - {} (ID: {}, Port: {}, PlayerCount: {})",
                        server.displayName(),
                        server.internalID(),
                        server.registeredServer().getServerInfo().getAddress().getPort(),
                        server.registeredServer().getPlayersConnected().size()
                ));
            }
        }
    }
}
