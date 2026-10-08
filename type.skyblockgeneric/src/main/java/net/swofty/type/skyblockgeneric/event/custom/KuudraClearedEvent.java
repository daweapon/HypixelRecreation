package net.swofty.type.skyblockgeneric.event.custom;

import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;

/** Fired through {@code HypixelEventHandler.callCustomEvent} each time a player wins a Kuudra run. */
public record KuudraClearedEvent(SkyBlockPlayer player, KuudraTier tier) implements PlayerInstanceEvent {
    @Override
    public @NotNull SkyBlockPlayer getPlayer() {
        return player;
    }
}
