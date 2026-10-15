package dev.daweapon.type.skyblockgeneric.warps.unlocks;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.warps.ScrollUnlockReason;

public class ScrollUnlockPurchase extends ScrollUnlockReason {
    private final String npc;

    public ScrollUnlockPurchase(String npc) {
        this.npc = npc;
    }

    @Override
    public Text getTitleReason() {
        return Text.of("<c>Scroll bought from NPC!");
    }

    @Override
    public Text getSubReason() {
        return Text.of("<7>Purchase the scroll from the {}.", StringUtility.toNormalCase(npc));
    }
}
