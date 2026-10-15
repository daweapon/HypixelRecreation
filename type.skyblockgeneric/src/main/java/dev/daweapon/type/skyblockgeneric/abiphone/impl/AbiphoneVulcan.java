package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonContact;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

// Vulcan hands over his contact after the player's first Infernal Kuudra (added March 2026).
public class AbiphoneVulcan extends CrimsonContact {
    private static final String[] GREETING = {"Nice to see you're still alive and kicking.",
            "Hm, my <3>Abiphone Contact<f>?", "I suppose I could do that."};

    public AbiphoneVulcan() {
        super("vulcan", "<c>Vulcan", "Sells <d>Crimson Essence <7>perks at the Forgotten Skull.",
                "e7f9b8b809985ba98dd6124fe385f6a4466aa6e18249a1fe637cb65c40dfc797");
    }

    private static boolean worthy(SkyBlockPlayer player) {
        return KuudraProgress.hasCleared(player, KuudraTier.INFERNAL);
    }

    @Override
    protected @Nullable String[] refusal(SkyBlockPlayer player) {
        if (worthy(player)) return null;
        return new String[]{GREETING[0], GREETING[1], GREETING[2],
                "Come and see me after completing the <c><l>INFERNAL <f>tier of <c>Kuudra <f>for the first time, then we can talk."};
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (!worthy(player)) {
            super.onAdd(player, slot);
            return;
        }
        if (CrimsonScene.isPlaying(player)) return;
        CrimsonScene.create().phone().say(speaker(), GREETING[0], GREETING[1], GREETING[2],
                "Looks like you've completed the <c><l>INFERNAL <f>tier of <c>Kuudra<f>, so I'll consider you worthy.",
                "Here's my contact.").play(player).thenRun(() -> super.onAdd(player, slot));
    }

    @Override
    protected void call(SkyBlockPlayer player) {
        reply(player, "Got some <d>Crimson Essence <f>to spend? Come see me at the <c>Forgotten Skull<f>.");
    }
}
