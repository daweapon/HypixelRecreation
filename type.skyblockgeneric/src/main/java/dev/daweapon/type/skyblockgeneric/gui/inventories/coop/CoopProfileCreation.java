package dev.daweapon.type.skyblockgeneric.gui.inventories.coop;

import dev.daweapon.commons.data.SharedData;
import dev.daweapon.commons.skyblock.SkyBlockPlayerProfiles;
import dev.daweapon.type.generic.data.datapoints.DatapointBoolean;
import dev.daweapon.type.generic.data.datapoints.DatapointString;
import dev.daweapon.type.generic.data.mongodb.ProfilesDatabase;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.commons.skyblock.CoopLinks;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointUUID;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

// Creates a new co-op member's SkyBlock profile, cloning shared island data from an existing member
final class CoopProfileCreation {

    private CoopProfileCreation() {}

    static UUID create(SkyBlockPlayer player, CoopDatabase.Coop coop) {
        UUID profileId = UUID.randomUUID();
        SharedData.profile().link(profileId, CoopLinks.COOP, coop.coopUUID());

        SkyBlockDataHandler handler = SkyBlockDataHandler.initUserWithDefaultData(player.getUuid(), profileId);
        SkyBlockDataHandler source = coop.memberProfiles().isEmpty()
                ? null
                : sourceHandler(coop.memberProfiles().getFirst());

        if (source == null) {
            handler.get(SkyBlockDataHandler.Data.ISLAND_UUID, DatapointUUID.class).setValue(UUID.randomUUID());
            handler.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class)
                    .setValue(SkyBlockPlayerProfiles.getRandomName());
        } else {
            handler.get(SkyBlockDataHandler.Data.ISLAND_UUID, DatapointUUID.class)
                    .setValue(source.get(SkyBlockDataHandler.Data.ISLAND_UUID, DatapointUUID.class).getValue());
            handler.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class)
                    .setValue(source.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class).getValue());
            handler.get(SkyBlockDataHandler.Data.PROFILE_MODE, DatapointString.class)
                    .setValue(source.get(SkyBlockDataHandler.Data.PROFILE_MODE, DatapointString.class).getValue());
        }

        handler.get(SkyBlockDataHandler.Data.IS_COOP, DatapointBoolean.class).setValue(true);

        new ProfilesDatabase(profileId.toString()).saveDocument(handler.toProfileDocument());
        return profileId;
    }

    private static SkyBlockDataHandler sourceHandler(UUID memberProfile) {
        ProfilesDatabase database = new ProfilesDatabase(memberProfile.toString());
        if (database.exists()) return SkyBlockDataHandler.createFromProfileOnly(database.getDocument());

        SkyBlockPlayer owner = SkyBlockGenericLoader.getPlayerFromProfileUUID(memberProfile);
        return owner == null ? null : SkyBlockDataHandler.getUser(owner.getUuid());
    }
}
