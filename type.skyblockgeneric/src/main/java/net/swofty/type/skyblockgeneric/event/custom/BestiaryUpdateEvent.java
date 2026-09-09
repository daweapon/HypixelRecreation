package net.swofty.type.skyblockgeneric.event.custom;

import lombok.Getter;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@Getter
// Fired when a player's bestiary kill count for a mob changes
public class BestiaryUpdateEvent implements PlayerInstanceEvent {
    private final SkyBlockPlayer player;
    private final BestiaryMob bestiaryMob;
    private final int oldTotalValue;
    private final int newTotalValue;

    public BestiaryUpdateEvent(SkyBlockPlayer player, BestiaryMob bestiaryMob, int oldTotalValue, int newTotalValue) {
        this.player = player;
        this.bestiaryMob = bestiaryMob;
        this.oldTotalValue = oldTotalValue;
        this.newTotalValue = newTotalValue;
    }
}
