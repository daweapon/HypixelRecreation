package dev.daweapon.type.skyblockgeneric.user.island.portal;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.warps.ScrollWarp;
import dev.daweapon.type.skyblockgeneric.warps.TravelScrollIslands;
import dev.daweapon.type.skyblockgeneric.warps.TravelScrollType;
import org.jetbrains.annotations.Nullable;

public record PortalDestination(String itemId, String label, @Nullable TravelScrollIslands island, @Nullable TravelScrollType scroll) {
    public PortalDestination {
        if ((island == null) == (scroll == null)) {
            throw new IllegalArgumentException(itemId + " needs exactly one of island or scroll");
        }
    }

    public Text labelText() {
        return Text.of(label);
    }

    public TravelScrollIslands owningIsland() {
        return island != null ? island : TravelScrollIslands.getFromTravelScroll(scroll);
    }

    public boolean isAvailable() {
        TravelScrollIslands owner = owningIsland();
        return owner != null && owner.isAvailable();
    }

    public void travel(SkyBlockPlayer player) {
        TravelScrollIslands owner = owningIsland();
        if (owner == null || !owner.isAvailable()) {
            player.sendMessage(TravelScrollIslands.NOT_AVAILABLE_MESSAGE);
            return;
        }
        Text blocked = owner.entryBlockReason(player);
        if (blocked != null) {
            player.sendMessage(blocked);
            return;
        }
        if (scroll != null) {
            ScrollWarp.travel(player, scroll, owner.getServerType());
            return;
        }
        player.asProxyPlayer().transferToWithIndication(owner.getServerType()).thenRun(() ->
                player.asProxyPlayer().sendMessage("<7>You have been warped to {}<7>!", owner.getDescriptiveName()));
    }
}
