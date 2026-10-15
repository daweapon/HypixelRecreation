package dev.daweapon.type.skyblockgeneric.slayer;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.gui.inventories.slayer.GUISlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

public final class MaddoxBatphone {
    public static final int REQUIRED_WOLF_SLAYER_LEVEL = 3;
    private static final int CALL_LINES = 6;

    private MaddoxBatphone() {
    }

    public static void call(SkyBlockPlayer player) {
        SlayerDefinition wolf = SlayerRegistry.get(SlayerType.SVEN_PACKMASTER);
        int wolfLevel = wolf == null ? 0 : wolf.levelForXp(player.getSlayerData().progress(SlayerType.SVEN_PACKMASTER).getXp());
        if (wolfLevel < REQUIRED_WOLF_SLAYER_LEVEL) {
            player.sendMessage(Text.of("<4>☠ <c>Requires <5>Wolf Slayer {}<c>.", REQUIRED_WOLF_SLAYER_LEVEL));
            return;
        }

        int line = ThreadLocalRandom.current().nextInt(CALL_LINES) + 1;
        player.sendMessage(Text.of("<e>[NPC] <5>Maddox<f>: ")
            .append(Text.key("npcs_hub.maddox.batphone.call_" + line + ".1")));
        player.openView(new GUISlayer());
    }
}
