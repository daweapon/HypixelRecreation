package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.commons.skyblock.CoopLinks;
import dev.daweapon.commons.skyblock.SkyBlockPlayerProfiles;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.mongodb.ProfilesDatabase;
import dev.daweapon.type.generic.data.mongodb.UserDatabase;
import dev.daweapon.type.generic.data.datapoints.DatapointString;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.ProfileSwitcher;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointUUID;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.CoopVoteManager;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandParameters(labels = "coopkick",
        description = "Kicks a player from your co-op",
        usage = "/coopkick <player>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class CoopKickCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord targetArg = ArgumentType.Word("player");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String targetName = context.get(targetArg);

            CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());
            if (!CoopDatabase.isMember(coop, player.getUuid())) {
                player.sendMessage("<b>[Co-op] <c>You are not part of a co-op!");
                return;
            }

            UUID targetUuid;
            try {
                targetUuid = ProfilesDatabase.fetchUUID(targetName);
            } catch (Exception e) {
                player.sendMessage("<b>[Co-op] <c>That player could not be found!");
                return;
            }

            if (targetUuid.equals(player.getUuid())) {
                player.sendMessage("<b>[Co-op] <c>You can't kick yourself - disband isn't supported yet, ask staff for help.");
                return;
            }

            if (!coop.members().contains(targetUuid)) {
                player.sendMessage("<b>[Co-op] <c>That player is not a member of your co-op!");
                return;
            }

            UUID targetProfileId = findMemberProfile(coop, targetUuid);
            if (targetProfileId == null) {
                player.sendMessage("<b>[Co-op] <c>Could not locate that member's co-op profile!");
                return;
            }

            // Real Hypixel co-op has no owner - kicking a member is decided by a majority vote of
            // the other online members instead, with a 35-day-inactivity bypass for 2-person co-ops.
            String voteError = CoopVoteManager.startKickVote(player, coop, targetUuid, targetName,
                    () -> performKick(player, coop.coopUUID(), targetUuid, targetProfileId, targetName),
                    () -> player.sendMessage("<b>[Co-op] <c>The vote to kick {} did not pass.", targetName));

            if (voteError != null) {
                player.sendMessage("<b>[Co-op] <c>{}", voteError);
            }
        }, targetArg);
    }

    // Package-visible (and static) so AdminCoopKickCommand can reuse the same removal logic
    // while bypassing the vote/inactivity requirements CoopVoteManager enforces for players.
    static CompletableFuture<Boolean> performKick(SkyBlockPlayer kicker, UUID coopId, UUID targetUuid,
                                                  UUID targetProfileId, String targetName) {
        SkyBlockPlayer targetOnline = SkyBlockGenericLoader.getLoadedPlayers().stream()
                .filter(loaded -> loaded.getUuid().equals(targetUuid))
                .findFirst().orElse(null);

        // A kicked member's co-op profile is being deleted outright, so if they're currently
        // playing on it they must be moved off it first - deleting a profile document out
        // from under a live SkyBlockDataHandler is what caused the mission-tick crash earlier.
        if (targetOnline != null && targetProfileId.equals(targetOnline.getProfiles().getCurrentlySelected())) {
            UUID fallback = targetOnline.getProfiles().getProfiles().stream()
                    .filter(candidate -> !candidate.equals(targetProfileId))
                    .findFirst().orElse(null);
            if (fallback == null) {
                kicker.sendMessage("<b>[Co-op] <c>That member has no other profile to fall back to - they need to make one first.");
                return CompletableFuture.completedFuture(false);
            }
            return ProfileSwitcher.switchTo(targetOnline, fallback).thenApply(result -> {
                if (result != ProfileSwitcher.Result.SWITCHED && result != ProfileSwitcher.Result.TRANSFERRED) return false;
                finishKick(kicker, coopId, targetUuid, targetProfileId, targetName);
                return true;
            });
        }

        finishKick(kicker, coopId, targetUuid, targetProfileId, targetName);
        return CompletableFuture.completedFuture(true);
    }

    static UUID createFallbackProfile(UUID targetUuid, UUID removedProfileId) {
        SkyBlockPlayer targetOnline = SkyBlockGenericLoader.getLoadedPlayers().stream()
                .filter(loaded -> loaded.getUuid().equals(targetUuid))
                .findFirst().orElse(null);
        SkyBlockPlayerProfiles profiles = targetOnline == null
                ? new UserDatabase(targetUuid).getProfiles()
                : targetOnline.getProfiles();

        UUID existingFallback = profiles.getProfiles().stream()
                .filter(profileId -> !profileId.equals(removedProfileId))
                .findFirst()
                .orElse(null);
        if (existingFallback != null) return existingFallback;

        UUID fallbackProfileId = UUID.randomUUID();
        SkyBlockDataHandler handler = SkyBlockDataHandler.initUserWithDefaultData(targetUuid, fallbackProfileId);
        handler.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class)
                .setValue(SkyBlockPlayerProfiles.getRandomName());
        handler.get(SkyBlockDataHandler.Data.ISLAND_UUID, DatapointUUID.class)
                .setValue(fallbackProfileId);
        new ProfilesDatabase(fallbackProfileId.toString()).saveDocument(handler.toProfileDocument());

        profiles.addProfile(fallbackProfileId);
        new UserDatabase(targetUuid).saveProfiles(profiles);
        return fallbackProfileId;
    }

    static void finishKick(SkyBlockPlayer kicker, UUID coopId, UUID targetUuid, UUID targetProfileId, String targetName) {
        CoopDatabase.update(coopId, latest -> {
            latest.members().remove(targetUuid);
            latest.removeInvite(targetUuid);
            latest.memberProfiles().remove(targetProfileId);
        });

        SharedData.profile().unlink(targetProfileId, CoopLinks.COOP);
        ProfilesDatabase.deleteDocument(targetProfileId.toString());

        SkyBlockPlayerProfiles targetProfiles = new UserDatabase(targetUuid).getProfiles();
        targetProfiles.removeProfile(targetProfileId);
        new UserDatabase(targetUuid).saveProfiles(targetProfiles);

        kicker.sendMessage("<b>[Co-op] <e>You kicked {} from your co-op!", targetName);

        SkyBlockPlayer targetOnline = SkyBlockGenericLoader.getLoadedPlayers().stream()
                .filter(loaded -> loaded.getUuid().equals(targetUuid))
                .findFirst().orElse(null);
        if (targetOnline != null) {
            targetOnline.sendMessage("<b>[Co-op] <c>You were kicked from the co-op by {}!", kicker.getUsername());
        }
    }

    static UUID findMemberProfile(CoopDatabase.Coop coop, UUID targetUuid) {
        for (UUID profileId : coop.memberProfiles()) {
            ProfilesDatabase database = new ProfilesDatabase(profileId.toString());
            if (!database.exists()) continue;
            Document doc = database.getDocument();
            String owner = doc.getString("_owner");
            if (targetUuid.toString().equals(owner)) return profileId;
        }
        return null;
    }
}
