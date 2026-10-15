package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import dev.daweapon.proxyapi.ProxyPlayer;
import dev.daweapon.proxyapi.ProxyPlayerSet;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.mongodb.ProfilesDatabase;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.CoopVoteManager;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

// Adds a player to an already-formed co-op - the real-Hypixel counterpart to /coop, which only
// covers the initial invite batch. Without this, once your co-op's other members have left there
// is no way to invite anyone new, since /coop refuses whenever you're already an active member.
@CommandParameters(labels = "coopadd",
        description = "Adds a player to your existing co-op",
        usage = "/coopadd <player>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class CoopAddCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord targetArg = ArgumentType.Word("player");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String targetName = context.get(targetArg);

            CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());
            if (!CoopDatabase.isMember(coop, player.getUuid())) {
                player.sendMessage("<b>[Co-op] <c>You don't have a co-op yet - use /coop <player> to start one!");
                return;
            }

            if (targetName.equalsIgnoreCase(player.getUsername())) {
                player.sendMessage("<b>[Co-op] <c>You can't invite yourself to your own co-op!");
                return;
            }

            UUID targetUuid;
            try {
                targetUuid = ProfilesDatabase.fetchUUID(targetName);
            } catch (Exception e) {
                player.sendMessage("<b>[Co-op] <c>That player could not be found!");
                return;
            }

            ProxyPlayerSet chosenPlayers = new ProxyPlayerSet(List.of(targetUuid));
            if (chosenPlayers.getPlayers().stream().anyMatch(Objects::isNull)) {
                player.sendMessage("<b>[Co-op] <c>That player is not online!");
                return;
            }
            ProxyPlayer invitedPlayer = chosenPlayers.asProxyPlayers().getFirst();
            if (!invitedPlayer.isOnline().join()) {
                player.sendMessage("<b>[Co-op] <c>That player is not online!");
                return;
            }

            if (coop.members().contains(targetUuid) || coop.memberInvites().contains(targetUuid)) {
                player.sendMessage("<b>[Co-op] <c>That player is already a member or already invited!");
                return;
            }

            if (CoopDatabase.getFromMember(targetUuid) != null) {
                player.sendMessage("<b>[Co-op] <c>That player already has a co-op or an invite pending!");
                return;
            }

            int totalSlots = coop.members().size() + coop.memberInvites().size();
            if (totalSlots >= 5) {
                player.sendMessage("<b>[Co-op] <c>Your co-op is full! (Max 5 players)");
                return;
            }

            // Real Hypixel co-op has no owner - since an August 2023 update, existing online members
            // must vote to approve a new addition before the invite is even sent to the target.
            String voteError = CoopVoteManager.startAddVote(player, coop, targetUuid, targetName,
                    () -> {
                        CoopDatabase.update(coop.coopUUID(), latest -> latest.addInvite(targetUuid));

                        player.sendMessage("<b>[Co-op] <e>You invited {} to your SkyBlock co-op!", targetName);

                        invitedPlayer.sendMessage("<b>----------------------------------------");
                        invitedPlayer.sendMessage("{} <e>invited you to a SkyBlock co-op!", player.getFullDisplayName());
                        invitedPlayer.sendMessage("<hover:'<e>Click here to view the invite'><click:run:'/coopcheck'>" +
                                "<6>Click here <e>to view!</click></hover>");
                        invitedPlayer.sendMessage("<b>----------------------------------------");
                    },
                    () -> player.sendMessage("<b>[Co-op] <c>The vote to add {} did not pass.", targetName));

            if (voteError != null) {
                player.sendMessage("<b>[Co-op] <c>{}", voteError);
            }
        }, targetArg);
    }
}
