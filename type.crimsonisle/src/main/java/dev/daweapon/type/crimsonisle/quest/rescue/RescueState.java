package dev.daweapon.type.crimsonisle.quest.rescue;

import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestTier;
import dev.daweapon.type.skyblockgeneric.crimson.quest.RescueStage;

/** A cached snapshot of a player's unfinished Rescue Mission, so NPC visibility checks stay cheap. */
public record RescueState(RescueSite site, FactionQuestTier tier, int stage) {
    public boolean infiltrating(RescueSite at) {
        return site == at && stage == RescueStage.INFILTRATING;
    }

    public boolean recruited(RescueSite at) {
        return site == at && stage >= RescueStage.RECRUITED;
    }
}
