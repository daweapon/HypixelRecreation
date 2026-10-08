package net.swofty.type.skyblockgeneric.event.custom;

import lombok.Getter;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@Getter
// Fired when a player reels in a Sea Creature, once per creature (twice on a Double Hook).
public class SeaCreatureCaughtEvent implements PlayerInstanceEvent {
    private final SkyBlockPlayer player;
    private final String seaCreatureId;

    public SeaCreatureCaughtEvent(SkyBlockPlayer player, String seaCreatureId) {
        this.player = player;
        this.seaCreatureId = seaCreatureId;
    }
}
