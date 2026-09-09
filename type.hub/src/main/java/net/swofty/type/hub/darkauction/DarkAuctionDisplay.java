package net.swofty.type.hub.darkauction;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.hub.gui.GUIDarkAuction;
import net.swofty.type.skyblockgeneric.darkauction.DarkAuctionHandler;
import net.swofty.type.skyblockgeneric.entity.GlassDisplay;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Manages the glass display for the Dark Auction that shows the current item being bid on.
 */
public class DarkAuctionDisplay {
    // GlassDisplay centers its item/hologram by adding +0.5 to this position's X/Z,
    // so this is offset -0.5/-0.5 from the measured in-game midpoint of (86, 54, 158).
    private static final Pos DISPLAY_POSITION = new Pos(85.5, 54, 157.5);

    private final Instance instance;
    private GlassDisplay currentDisplay;

    public DarkAuctionDisplay(Instance instance) {
        this.instance = instance;
    }

    /**
     * Updates the display to show the current auction item.
     * Removes the old display and spawns a new one with the current item.
     */
    public void update() {
        remove();

        DarkAuctionHandler.DarkAuctionLocalState state = DarkAuctionHandler.getLocalState();
        if (state == null) {
            return;
        }

        String itemTypeName = state.getCurrentItemType();
        if (itemTypeName == null) {
            return;
        }

        SkyBlockItem item;
        try {
            ItemType itemType = ItemType.valueOf(itemTypeName);
            item = new SkyBlockItem(itemType);
        } catch (Exception e) {
            return;
        }

        Text[] extraLines = {
                Text.of("<f>Starting Bid: <6>{:,} Coins", state.getCurrentBid()),
                Text.of("<e>Click to view!")
        };

        currentDisplay = GlassDisplay.create(item, instance, DISPLAY_POSITION, extraLines, (player, event) -> {
            if (!(player instanceof SkyBlockPlayer skyBlockPlayer)) {
                return;
            }

            if (!DarkAuctionHandler.isPlayerInAuction(skyBlockPlayer.getUuid())) {
                skyBlockPlayer.sendMessage("<c>You must join the Dark Auction to bid!");
                return;
            }

            new GUIDarkAuction().open(skyBlockPlayer);
        });
    }

    /**
     * Removes the current display from the world.
     */
    public void remove() {
        if (currentDisplay != null) {
            currentDisplay.remove();
            currentDisplay = null;
        }
    }
}
