package dev.daweapon.type.skyblockgeneric.user;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointRank;
import dev.daweapon.type.generic.data.datapoints.DatapointString;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockEmblems;

import java.util.UUID;

class PlayerDisplayNames {
    private final SkyBlockPlayer player;

    PlayerDisplayNames(SkyBlockPlayer player) {
        this.player = player;
    }

    Text getFullDisplayName() {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
        SkyBlockEmblems.SkyBlockEmblem emblem = experience.getEmblem();
        return getFullDisplayName(emblem, experience.getLevel().getColor());
    }

    Text getFullDisplayName(String levelColor) {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
        return getFullDisplayName(experience.getEmblem(), levelColor);
    }

    Text getFullDisplayName(SkyBlockEmblems.SkyBlockEmblem displayEmblem) {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
        return getFullDisplayName(displayEmblem, experience.getLevel().getColor());
    }

    Text getFullDisplayName(SkyBlockEmblems.SkyBlockEmblem displayEmblem, String levelColor) {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();

        Text prefix = Text.of("<8>[" + levelColor + "{}<8>] ", experience.getLevel());
        if (displayEmblem != null) {
            prefix = prefix.append("{} ", displayEmblem.emblem());
        }
        return prefix.append("{}", player.getRank().displayName(player.getUsername()));
    }

    static Text getDisplayName(UUID uuid) {
        if (SkyBlockGenericLoader.getLoadedPlayers().stream().anyMatch(player -> player.getUuid().equals(uuid))) {
            return SkyBlockGenericLoader.getLoadedPlayers().stream().filter(player -> player.getUuid().equals(uuid)).findFirst().get().getFullDisplayName();
        } else {
            // Fallback for offline name display: use Hypixel account data (rank + ign)
            HypixelDataHandler account = HypixelDataHandler.getOfOfflinePlayer(uuid);
            return Text.of("{}", account.get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue()
                    .displayName(account.get(HypixelDataHandler.Data.IGN, DatapointString.class).getValue()));
        }
    }

    static String getRawName(UUID uuid) {
        if (SkyBlockGenericLoader.getLoadedPlayers().stream().anyMatch(player -> player.getUuid().equals(uuid))) {
            return SkyBlockGenericLoader.getLoadedPlayers().stream().filter(player -> player.getUuid().equals(uuid)).findFirst().get().getUsername();
        } else {
            HypixelDataHandler account = HypixelDataHandler.getOfOfflinePlayer(uuid);
            return account.get(HypixelDataHandler.Data.IGN, DatapointString.class).getValue();
        }
    }
}
