package net.swofty.type.skyblockgeneric.warps;

import net.swofty.commons.ServerType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
