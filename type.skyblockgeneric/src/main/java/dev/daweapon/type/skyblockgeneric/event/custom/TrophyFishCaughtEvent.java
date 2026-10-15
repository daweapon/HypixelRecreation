package dev.daweapon.type.skyblockgeneric.event.custom;

import lombok.Getter;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import dev.daweapon.type.skyblockgeneric.fishing.catches.TrophyTier;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@Getter
// Fired after a player catches a Trophy Fish (including the Golden Fish), once its count has been saved.
// fishId is the trophy definition id (e.g. "VOLCANIC_STONEFISH", "OBFUSCATED_1"); itemId the tiered item.
public class TrophyFishCaughtEvent implements PlayerInstanceEvent {
    private final SkyBlockPlayer player;
    private final String fishId;
    private final TrophyTier tier;
    private final String itemId;

    public TrophyFishCaughtEvent(SkyBlockPlayer player, String fishId, TrophyTier tier, String itemId) {
        this.player = player;
        this.fishId = fishId;
        this.tier = tier;
        this.itemId = itemId;
    }
}
