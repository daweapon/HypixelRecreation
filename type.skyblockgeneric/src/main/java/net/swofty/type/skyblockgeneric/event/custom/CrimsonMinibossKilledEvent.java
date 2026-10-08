package net.swofty.type.skyblockgeneric.event.custom;

import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.swofty.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMiniboss;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Fired once for every player credited with a Crimson Isle miniboss or Magma Boss kill, which is everyone who
 * dealt at least 250,000 damage to it (loot sharing).
 */
public record CrimsonMinibossKilledEvent(SkyBlockPlayer player, CrimsonMiniboss miniboss) implements PlayerInstanceEvent {
    @Override
    public @NotNull SkyBlockPlayer getPlayer() {
        return player;
    }
}
