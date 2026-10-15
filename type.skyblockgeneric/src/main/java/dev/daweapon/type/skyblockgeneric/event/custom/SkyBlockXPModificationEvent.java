package dev.daweapon.type.skyblockgeneric.event.custom;

import lombok.Getter;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import dev.daweapon.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

@Getter
// Fired when a player's overall SkyBlock XP changes, carrying the cause and old/new totals
public class SkyBlockXPModificationEvent implements PlayerInstanceEvent {
    @Getter
    private SkyBlockPlayer player;
    /**
     * The cause that granted the XP, or null when the change came from per-source progress or
     * an admin XP adjustment rather than a single completed cause.
     */
    private @Nullable SkyBlockLevelCauseAbstr cause;
    private double oldXP;
    private double newXP;

    public SkyBlockXPModificationEvent(SkyBlockPlayer player, @Nullable SkyBlockLevelCauseAbstr cause, double oldXP, double newXP) {
        this.player = player;
        this.cause = cause;
        this.oldXP = oldXP;
        this.newXP = newXP;
    }
}
