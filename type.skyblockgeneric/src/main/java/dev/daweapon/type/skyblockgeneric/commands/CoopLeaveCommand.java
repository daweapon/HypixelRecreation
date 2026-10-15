package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.commons.data.SharedData;
import dev.daweapon.commons.skyblock.SkyBlockPlayerProfiles;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.mongodb.ProfilesDatabase;
import dev.daweapon.type.generic.data.mongodb.UserDatabase;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.commons.skyblock.CoopLinks;
import dev.daweapon.commons.skyblock.IslandStorage;
import dev.daweapon.type.skyblockgeneric.data.ProfileSwitcher;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;

import java.util.UUID;

@CommandParameters(labels = "cooperativeleave coopleave",
        description = "Leaves the current coop",
        usage = "/coopleave",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class CoopLeaveCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            if (!player.isCoop()) {
                player.sendMessage("<b>[Co-op] <c>You are not on a coop profile!");
                return;
            }

            CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());
            if (coop == null) {
                player.sendMessage("<b>[Co-op] <c>You are not part of a co-op!");
                return;
            }

            UUID coopId = coop.coopUUID();
            UUID playerId = player.getUuid();
            UUID profileId = player.getProfiles().getCurrentlySelected();
            UUID fallbackProfileId = player.getProfiles().getProfiles().stream()
                    .filter(candidate -> !candidate.equals(profileId))
                    .findFirst()
                    .orElse(null);

            if (fallbackProfileId == null) {
                player.sendMessage("<b>[Co-op] <c>You cannot leave your last profile!");
                player.sendMessage("<b>[Co-op] <e>Make another profile before deleting this one.");
                return;
            }

            ProfileSwitcher.switchTo(player, fallbackProfileId).thenAccept(result -> {
                if (result != ProfileSwitcher.Result.SWITCHED && result != ProfileSwitcher.Result.TRANSFERRED) return;
                leave(playerId, coopId, profileId);
            });
        });
    }

    private void leave(UUID playerId, UUID coopId, UUID profileId) {
        UUID islandId = ProfileSwitcher.islandOf(profileId);

        CoopDatabase.Coop remaining = CoopDatabase.update(coopId, latest -> {
            latest.members().remove(playerId);
            latest.removeInvite(playerId);
            latest.memberProfiles().remove(profileId);
        });

        SharedData.profile().unlink(profileId, CoopLinks.COOP);
        boolean disbanded = remaining == null || (remaining.members().isEmpty() && remaining.memberProfiles().isEmpty());
        if (disbanded) {
            SharedData.profile().deleteLink(CoopLinks.COOP, coopId);
        }

        ProfilesDatabase.deleteDocument(profileId.toString());

        // The last member leaving disbands the coop - its shared island would otherwise become
        // permanently unreferenced (no profile document left pointing at it). Same cleanup as
        // GUIProfileSelect's solo profile-delete path.
        if (disbanded) {
            SkyBlockIsland island = SkyBlockIsland.getIsland(islandId);
            if (island != null) {
                SkyBlockIsland.discard(islandId);
                island.runVacantCheck();
            }
            IslandStorage.delete(islandId);
        }

        SkyBlockPlayerProfiles profiles = new UserDatabase(playerId).getProfiles();
        profiles.removeProfile(profileId);
        new UserDatabase(playerId).saveProfiles(profiles);
    }
}
