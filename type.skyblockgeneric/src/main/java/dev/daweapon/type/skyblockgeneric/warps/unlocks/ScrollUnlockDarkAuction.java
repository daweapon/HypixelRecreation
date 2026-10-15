package dev.daweapon.type.skyblockgeneric.warps.unlocks;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.warps.ScrollUnlockReason;

public class ScrollUnlockDarkAuction extends ScrollUnlockReason {
    public ScrollUnlockDarkAuction() {}

    @Override
    public Text getTitleReason() {
        return Text.of("<c>Scroll bought in Dark Auction!");
    }

    @Override
    public Text getSubReason() {
        return Text.of("<7>Buy this scroll in a Dark Auction event.");
    }
}
