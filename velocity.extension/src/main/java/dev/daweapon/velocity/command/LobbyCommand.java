package dev.daweapon.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.daweapon.commons.text.Text;
import dev.daweapon.commons.LobbyDestination;
import dev.daweapon.commons.ServerType;
import dev.daweapon.velocity.gamemanager.GameManager;
import dev.daweapon.velocity.gamemanager.TransferHandler;

// "/lobby [game]" command that transfers the player to a default or named lobby destination.
public class LobbyCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player player)) {
            source.sendMessage(Text.of("<c>This command can only be used by players."));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length > 1) {
            player.sendMessage(Text.of("<c>Usage: /lobby [game]"));
            return;
        }

        ServerType currentType = player.getCurrentServer()
            .map(connection -> GameManager.getTypeFromRegisteredServer(connection.getServer()))
            .orElse(null);

        ServerType destination;
        if (args.length == 0) {
            destination = LobbyDestination.resolveDefaultDestination(currentType);
        } else {
            destination = LobbyDestination.resolveFromAlias(args[0]);
            if (destination == null) {
                player.sendMessage(Text.of("<c>Unknown lobby destination. Try /lobby bw, /lobby sw, /lobby bedwars, /lobby skywars."));
                return;
            }
        }

        new TransferHandler(player).transferTo(destination);
    }
}
