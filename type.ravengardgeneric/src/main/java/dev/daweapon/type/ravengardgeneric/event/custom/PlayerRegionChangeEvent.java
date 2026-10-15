package dev.daweapon.type.ravengardgeneric.event.custom;

import lombok.Getter;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import dev.daweapon.type.ravengardgeneric.region.RavengardRegionType;
import dev.daweapon.type.ravengardgeneric.user.RavengardPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerRegionChangeEvent implements PlayerInstanceEvent {
    private final RavengardPlayer player;
    @Getter
    private final RavengardRegionType from;
    @Getter
    private final RavengardRegionType to;

    public PlayerRegionChangeEvent(RavengardPlayer player, RavengardRegionType from, RavengardRegionType to) {
        this.player = player;
        this.from = from;
        this.to = to;
    }

    @Override
    public @NotNull RavengardPlayer getPlayer() {
        return player;
    }
}
