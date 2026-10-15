package dev.daweapon.type.skyblockgeneric.warps;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class ScrollWarp {
    private ScrollWarp() {}

    public static void travel(SkyBlockPlayer player, TravelScrollType scroll, ServerType destination) {
        if (HypixelConst.getTypeLoader().getType() == destination) {
            player.teleport(scroll.getLocation());
            player.sendMessage("<7>You have been warped to {}<7>!", scroll.getDisplayName());
            return;
        }

        player.asProxyPlayer().transferToWithIndication(destination, scroll.getLocation()).thenRun(() ->
                player.asProxyPlayer().sendMessage("<7>You have been warped to {}<7>!", scroll.getDisplayName()));
    }
}
