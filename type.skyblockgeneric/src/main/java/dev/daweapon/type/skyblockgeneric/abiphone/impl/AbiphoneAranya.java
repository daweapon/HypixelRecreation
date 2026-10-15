package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonContact;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.crimson.town.CrimsonTownProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

// Aranya, the spider in The Wasteland, shares her contact once the player has brought her a meal.
public class AbiphoneAranya extends CrimsonContact {
    public AbiphoneAranya() {
        super("aranya", "<c>Aranya", "A hungry spider living in a den in <c>The Wasteland<7>.",
                "cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1");
    }

    @Override
    protected @Nullable String[] refusal(SkyBlockPlayer player) {
        if (CrimsonSideQuests.is(player, CrimsonTownProgress.ARANYA_FED)) return null;
        return new String[]{"It has to be a human, and they cannot be wearing any armor. That's all I'm asking!"};
    }

    @Override
    protected void call(SkyBlockPlayer player) {
        if (CrimsonSideQuests.isReady(player, CrimsonTownProgress.ARANYA_LAST_MEAL, CrimsonTownProgress.ARANYA_COOLDOWN)) {
            int pick = ThreadLocalRandom.current().nextInt(3);
            switch (pick) {
                case 0 -> reply(player, "I am famished! Bring me a <c>meal <f>and I will reward you!");
                case 1 -> reply(player, "Bring me a <c>human meal<f>. No armor! Thank you!");
                default -> reply(player, "Hey, just the one I needed to hear from!", "I'm hungry again and I need a <c>meal<f>.",
                        "Could you come help me out at my Den? And bring a friend. <a>;)");
            }
            return;
        }
        reply(player, "I'm full!");
    }
}
