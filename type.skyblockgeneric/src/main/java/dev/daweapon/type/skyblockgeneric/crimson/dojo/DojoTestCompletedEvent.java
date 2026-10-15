package dev.daweapon.type.skyblockgeneric.crimson.dojo;

import net.minestom.server.event.trait.PlayerInstanceEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Fired through {@code HypixelEventHandler.callCustomEvent} whenever a Dojo test ends, including
 * fails and cancels (which score as they stood, usually an F). Listen on {@code EventNodes.CUSTOM}.
 */
public record DojoTestCompletedEvent(SkyBlockPlayer player, DojoTest test, int score, DojoGrade grade)
        implements PlayerInstanceEvent {

    @Override
    public @NotNull SkyBlockPlayer getPlayer() {
        return player;
    }
}
