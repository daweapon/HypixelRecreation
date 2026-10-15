package dev.daweapon.type.generic.command.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.Player;
import dev.daweapon.commons.ServerType;
import dev.daweapon.proxyapi.ProxyPlayer;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.WipeSnapshotService;
import dev.daweapon.type.generic.data.domain.PlayerDataService;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import org.tinylog.Logger;

import java.util.UUID;

@CommandParameters(description = "Restores a player's profile if they were wiped within the last hour",
        usage = "/restore <player>",
        permission = Rank.STAFF,
        labels = "restore",
        allowsConsole = false)
// Staff command to restore a player's profile from a wipe snapshot, if wiped within the last hour.
public class RestoreCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString nameArgument = ArgumentType.String("player");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            String name = context.get(nameArgument);
            UUID targetUuid = HypixelDataHandler.getPotentialUUIDFromName(name);
            if (targetUuid == null) {
                sender.sendMessage("<c>Could not find a player by that name.");
                return;
            }

            HypixelPlayer player = (HypixelPlayer) sender;
            Thread.startVirtualThread(() -> restore(player, targetUuid, name));
        }, nameArgument);
    }

    private void restore(HypixelPlayer sender, UUID targetUuid, String name) {
        WipeSnapshotService.RestoreResult result = WipeSnapshotService.restore(targetUuid);

        switch (result) {
            case SUCCESS -> {
                sender.sendMessage("<a>Restored <e>{}<a>'s profile.", name);
                notifyTarget(targetUuid);
            }
            case EXPIRED -> sender.sendMessage(
                    "<c>{} was wiped more than an hour ago and can no longer be restored.", name);
            case NOT_FOUND -> sender.sendMessage("<c>No recoverable wipe found for {}.", name);
        }
    }

    // A still-connected session holds the pre-restore (wiped) data in memory and will happily autosave it
    // right back over the restore. A local player is fixed in place with a fresh reload (same technique
    // WipeCommand uses); a remote player is bounced off their current server instead, since there's no
    // cross-server equivalent of an in-process reload available.
    private void notifyTarget(UUID targetUuid) {
        Player localPlayer = findLocalPlayer(targetUuid);
        if (localPlayer != null) {
            reloadLocal((HypixelPlayer) localPlayer, targetUuid);
            return;
        }

        ProxyPlayer target = new ProxyPlayer(targetUuid);
        target.isOnline().thenAccept(online -> {
            if (!online) return;
            target.sendMessage("<a>Your profile has been restored. Reconnecting you now.");
            target.transferWithoutDataTo(ServerType.PROTOTYPE_LOBBY);
        });
    }

    private void reloadLocal(HypixelPlayer target, UUID targetUuid) {
        ServerType type = HypixelConst.getTypeLoader().getType();
        try {
            PlayerDataService.discardAll(type, targetUuid);
            PlayerDataService.loadAll(type, targetUuid);
            PlayerDataService.attachAll(type, target);
            PlayerDataService.applyAll(type, target);
            target.sendMessage("<a>Your profile has been restored.");
        } catch (Exception e) {
            Logger.error(e, "Failed to reload restored user {}", targetUuid);
            ScheduleUtility.nextTick(() -> {
                if (target.isOnline()) target.kick("<a>Your profile has been restored. Please reconnect.");
            });
        }
    }

    private Player findLocalPlayer(UUID uuid) {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (player.getUuid().equals(uuid)) return player;
        }
        return null;
    }
}
