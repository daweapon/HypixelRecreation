package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.swofty.commons.data.SwoftyData;
import net.swofty.proxyapi.ProxyPlayer;
import net.swofty.proxyapi.ProxyPlayerSet;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.mongodb.ProfilesDatabase;
import net.swofty.type.generic.data.mongodb.UserDatabase;
import net.swofty.type.generic.friend.FriendManager;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.data.ProfileSwitcher;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.island.IslandHostLookup;
import net.swofty.type.skyblockgeneric.user.island.IslandPrivacy;
import net.swofty.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.swofty.type.skyblockgeneric.user.island.VisitManager;
import org.bson.Document;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

// Real-Hypixel /visit - joins another online player's SkyBlock island as a guest, subject to their
// privacy setting and guest limit, without touching the visitor's own profile/island state.
@CommandParameters(labels = "visit",
        description = "Visits another player's SkyBlock island",
        usage = "/visit <player>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class VisitCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord targetArg = ArgumentType.Word("player");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String targetName = context.get(targetArg);

            if (targetName.equalsIgnoreCase(player.getUsername())) {
                player.sendMessage("<c>You can't visit your own island - use /is to go home!");
                return;
            }

            UUID targetUuid;
            try {
                targetUuid = ProfilesDatabase.fetchUUID(targetName);
            } catch (Exception e) {
                player.sendMessage("<c>That player could not be found!");
                return;
            }

            visitAsync(player, targetUuid, targetName);
        }, targetArg);
    }

    // Command syntax callbacks run directly on the tick thread. VisitManager.moveTo() blocks on
    // scheduleNextTick(...).join() - calling that from the tick thread deadlocks forever, since
    // "next tick" can't happen until the current tick (this very call) finishes. Dispatching onto a
    // virtual thread first is the same fix ProfileSwitcher.switchTo() uses for the identical pattern.
    public static void visitAsync(SkyBlockPlayer player, UUID targetUuid, String targetName) {
        Thread.startVirtualThread(() -> visit(player, targetUuid, targetName));
    }

    // Shared by the /visit command and the profile GUI's "Visit Island" button (which already has
    // a live viewedPlayer, so it skips the online/name-lookup step and calls straight in here).
    // Must always be entered via visitAsync() / an existing virtual thread - see the note above.
    public static void visit(SkyBlockPlayer player, UUID targetUuid, String targetName) {
        ProxyPlayerSet chosenPlayers = new ProxyPlayerSet(List.of(targetUuid));
        if (chosenPlayers.getPlayers().stream().anyMatch(Objects::isNull)) {
            player.sendMessage("<c>That player is not online!");
            return;
        }
        ProxyPlayer targetProxy = chosenPlayers.asProxyPlayers().getFirst();
        if (!targetProxy.isOnline().join()) {
            player.sendMessage("<c>That player is not online!");
            return;
        }

        UUID targetProfileId = new UserDatabase(targetUuid).getProfiles().getCurrentlySelected();
        if (targetProfileId == null) {
            player.sendMessage("<c>That player doesn't have a SkyBlock profile yet!");
            return;
        }

        UUID targetIslandId = ProfileSwitcher.islandOf(targetProfileId);
        if (targetIslandId.equals(player.getSkyBlockIsland().getIslandID())) {
            player.sendMessage("<c>You already share this island!");
            return;
        }

        Document targetProfileDoc = ProfilesDatabase.fetchDocument(targetProfileId.toString());
        if (isIgnored(targetProfileDoc, player.getUuid())) {
            player.sendMessage("<c>You can't visit {}'s island!", targetName);
            return;
        }

        IslandPrivacy privacy = IslandPrivacy.fromStored(
                targetProfileDoc == null ? null : targetProfileDoc.getString(SkyBlockDataHandler.Data.ISLAND_PRIVACY.getKey()));

        if (privacy == IslandPrivacy.PRIVATE) {
            player.sendMessage("<c>{}'s island is set to private!", targetName);
            return;
        }
        if (privacy == IslandPrivacy.FRIENDS_ONLY && !FriendManager.areFriends(player, targetUuid)) {
            player.sendMessage("<c>You must be friends with {} to visit their island!", targetName);
            return;
        }

        Rank targetRank = rankOf(targetUuid);
        int guestLimit = targetRank.maxIslandGuests();
        if (VisitManager.visitorCount(targetIslandId) >= guestLimit) {
            player.sendMessage("<c>{}'s island is full! (Max {} guests)", targetName, guestLimit);
            return;
        }

        UUID host = resolveHost(targetIslandId);
        if (host == null) {
            player.sendMessage("<c>{}'s island isn't currently active!", targetName);
            return;
        }

        VisitManager.queuePendingVisit(player.getUuid(), targetIslandId, targetProfileId, targetUuid);
        player.sendMessage("<e>Visiting {}'s island...", targetName);

        if (host.equals(HypixelConst.getServerUUID())) {
            VisitManager.VisitSession session = VisitManager.consumePendingVisit(player.getUuid());
            if (session == null) return;
            SkyBlockIsland island = SkyBlockIsland.getOrCreate(session.islandId(), session.hostProfileId());
            VisitManager.moveTo(player, island.getSharedInstance().join());
            VisitManager.markVisiting(player.getUuid(), session);
        } else {
            player.asProxyPlayer().transferToWithIndication(host).join();
        }
    }

    // Same-server-preferring host lookup: an island already loaded on this JVM is used directly
    // instead of round-tripping a Redis query for something we can already answer locally.
    private static UUID resolveHost(UUID islandId) {
        if (SkyBlockIsland.hasIsland(islandId)) return HypixelConst.getServerUUID();
        return IslandHostLookup.findRemoteHost(islandId);
    }

    // IGNORED_VISITORS serializes as a plain comma-joined string (DatapointStringList's Serializer,
    // unlike Jackson-backed datapoints, doesn't wrap it in JSON quotes).
    private static boolean isIgnored(Document targetProfileDoc, UUID visitorUuid) {
        if (targetProfileDoc == null) return false;
        String raw = targetProfileDoc.getString(SkyBlockDataHandler.Data.IGNORED_VISITORS.getKey());
        if (raw == null || raw.isEmpty()) return false;
        return List.of(raw.split(",")).contains(visitorUuid.toString());
    }

    private static Rank rankOf(UUID playerUuid) {
        String raw = SwoftyData.account().get(playerUuid, HypixelDataHandler.Data.RANK.accountField());
        if (raw == null) return Rank.DEFAULT;
        try {
            return Rank.valueOf(raw.replace("\"", "").trim());
        } catch (IllegalArgumentException exception) {
            return Rank.DEFAULT;
        }
    }
}
