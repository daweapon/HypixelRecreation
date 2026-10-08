package net.swofty.type.skyblockgeneric.warps.unlocks;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.warps.ScrollUnlockReason;

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
