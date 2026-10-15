package dev.daweapon.type.skyblockgeneric.warps.unlocks;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.warps.ScrollUnlockReason;

public class ScrollUnlockFound extends ScrollUnlockReason {
    private final String island;

    public ScrollUnlockFound(String island) {
        this.island = island;
    }

    @Override
    public Text getTitleReason() {
        return Text.of("<c>Scroll found on {}!", island);
    }

    @Override
    public Text getSubReason() {
        return Text.of("<7>This scroll is found somewhere on the {}!", island);
    }
}
