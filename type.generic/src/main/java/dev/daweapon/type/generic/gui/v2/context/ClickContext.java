package dev.daweapon.type.generic.gui.v2.context;

import net.minestom.server.inventory.click.Click;
import dev.daweapon.type.generic.user.HypixelPlayer;

public record ClickContext<S>(
        int slot,
        Click click,
        HypixelPlayer player,
        S state
) {
}

