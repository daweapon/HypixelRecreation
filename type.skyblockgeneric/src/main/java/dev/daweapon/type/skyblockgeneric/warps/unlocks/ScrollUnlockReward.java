package dev.daweapon.type.skyblockgeneric.warps.unlocks;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.warps.ScrollUnlockReason;

public class ScrollUnlockReward extends ScrollUnlockReason {
    private final String source;

    public ScrollUnlockReward(String source) {
        this.source = source;
    }

    @Override
    public Text getTitleReason() {
        return Text.of("<c>Scroll given as a reward!");
    }

    @Override
    public Text getSubReason() {
        return Text.of("<7>{}", source);
    }
}
