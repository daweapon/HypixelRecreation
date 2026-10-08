package net.swofty.type.skyblockgeneric.abiphone.impl;

import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonContact;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.crimson.questline.UniversalStep;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Elle hands over her contact during the faction questlines (wiki: Elle).
public class AbiphoneElle extends CrimsonContact {
    public AbiphoneElle() {
        super("elle", CrimsonScene.ELLE, "Your guide on the <c>Crimson Isle<7>.",
                "2333aa2414bcf1c291fddf6a9b0f805f996546ec4150ff3cef10bd529cc98261");
    }

    @Override
    protected String[] refusal(SkyBlockPlayer player) {
        if (CrimsonQuestlines.universal(player) == UniversalStep.COMPLETE) return null;
        return new String[]{"You don't need to follow me everywhere you know?"};
    }

    @Override
    protected void call(SkyBlockPlayer player) {
        replyRandom(player, "Thanks for sticking around, you looked real cool out there!",
                "I really have to do everything around here.");
    }
}
