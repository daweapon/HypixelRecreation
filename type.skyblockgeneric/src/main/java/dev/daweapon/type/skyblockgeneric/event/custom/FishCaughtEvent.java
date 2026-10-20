package dev.daweapon.type.skyblockgeneric.event.custom;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import lombok.Getter;
import net.minestom.server.event.trait.PlayerInstanceEvent;

@Getter
public class FishCaughtEvent implements PlayerInstanceEvent {
    public enum Kind { ITEM, TREASURE, SEA_CREATURE, TROPHY_FISH, ESSENCE, QUEST }

    private final SkyBlockPlayer player;
    private final Kind kind;
    private final String id;
    private final String quality;

    public FishCaughtEvent(SkyBlockPlayer player, Kind kind, String id, String quality) {
        this.player = player;
        this.kind = kind;
        this.id = id;
        this.quality = quality;
    }
}
