package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonContact;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Sirih the writer, who keeps forgetting their Sulphur and turning back into a pig.
public class AbiphoneSirih extends CrimsonContact {
    public AbiphoneSirih() {
        super("sirih", "<d>Sirih", "A writer living as a recluse in <c>Dragontail<7>.",
                "804b2253c24d814bef3199f0a8611eeab54f7ce2ebf71a4bbe0028eb1a7bb08f");
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (CrimsonSideQuests.isSirihPig(player)) {
            player.sendMessage("<7>[<b>✆<7>] Oink.");
            return;
        }
        super.onAdd(player, slot);
    }

    @Override
    protected String[] refusal(SkyBlockPlayer player) {
        return null;
    }

    @Override
    protected void call(SkyBlockPlayer player) {
        if (CrimsonSideQuests.isSirihPig(player)) {
            reply(player, "Oink?");
            return;
        }
        replyRandom(player, "I'm pretty busy right now, sorry.",
                "Is this urgent? I'm working on my new novel at the moment.");
    }
}
