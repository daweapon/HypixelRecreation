package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.swofty.commons.ServerType;
import net.swofty.proxyapi.ProxyPlayer;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.PlayerWipeService;
import net.swofty.type.generic.data.WipeSnapshotService;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.utility.ScheduleUtility;
import org.tinylog.Logger;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@CommandParameters(description = "Wipes a player's profile data",
        usage = "/wipe [player]",
        permission = Rank.STAFF,
        labels = "wipe",
        allowsConsole = false)
// Staff command to wipe a player's profile data, snapshotting it first and transferring them off the server.
public class WipeCommand extends HypixelCommand {
    private static final ServerType DESTINATION = ServerType.PROTOTYPE_LOBBY;
    private static final long TRANSFER_TIMEOUT_SECONDS = 20;

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, _) -> {
            if (!permissionCheck(sender)) return;

            HypixelPlayer player = (HypixelPlayer) sender;
            beginWipe(player, player);
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }

            beginWipe((HypixelPlayer) sender, (HypixelPlayer) target);
        }, targetArgument);
    }

    private void beginWipe(HypixelPlayer sender, HypixelPlayer target) {
        UUID targetUuid = target.getUuid();
        boolean toOther = target != sender;

        if (!PlayerWipeService.begin(targetUuid)) {
            sender.sendMessage(toOther ? "<c>That player is already being wiped." : "<c>You are already being wiped.");
            return;
        }

        ServerType type = HypixelConst.getTypeLoader().getType();
        if (toOther) {
            target.sendMessage("<c><l>YOU HAVE BEEN WIPED! <c>Contact an admin if you think this was a mistake.");
            sender.sendMessage("<a>Wiping everything <e>{}<a> owns on every server.", target.getUsername());
        } else {
            sender.sendMessage("<c>Wiping everything you own on every server.");
        }
        Thread.startVirtualThread(() -> wipe(sender, target, targetUuid, type));
    }

    private void wipe(HypixelPlayer sender, HypixelPlayer target, UUID targetUuid, ServerType type) {
        try {
            WipeSnapshotService.capture(targetUuid, PlayerWipeService.profileIdsOf(targetUuid));
            PlayerDataService.discardAll(type, targetUuid);
            PlayerWipeService.Result result = PlayerWipeService.wipe(targetUuid);
            Logger.info("Wiped {} profile(s), {} island(s), {} leaderboard entr(ies) and {} tracked row(s) of {}",
                    result.profiles().size(), result.islands(), result.leaderboards(), result.ownedRows(), targetUuid);

            if (type == DESTINATION) {
                reload(sender, target, targetUuid, type);
                return;
            }

            new ProxyPlayer(targetUuid).transferWithoutDataTo(DESTINATION)
                    .orTimeout(TRANSFER_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .exceptionally(error -> {
                        Logger.error(error, "Failed to move wiped user {} to the prototype lobby", targetUuid);
                        kick(target);
                        return null;
                    });
        } catch (Exception e) {
            Logger.error(e, "Failed to wipe user {}", targetUuid);
            kick(target);
        } finally {
            PlayerWipeService.finish(targetUuid);
        }
    }

    private void reload(HypixelPlayer sender, HypixelPlayer target, UUID targetUuid, ServerType type) {
        try {
            PlayerDataService.loadAll(type, targetUuid);
            PlayerDataService.attachAll(type, target);
            PlayerDataService.applyAll(type, target);
            if (target == sender) {
                target.sendMessage("<a>You have been wiped.");
            } else {
                sender.sendMessage("<a>{} has been wiped.", target.getUsername());
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to reload wiped user {}", targetUuid);
            kick(target);
        }
    }

    private void kick(HypixelPlayer player) {
        ScheduleUtility.nextTick(() -> {
            if (player.isOnline()) player.kick("<c>You have been wiped");
        });
    }
}
