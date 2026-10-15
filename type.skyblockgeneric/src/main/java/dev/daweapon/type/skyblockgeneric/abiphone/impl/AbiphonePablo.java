package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonContact;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;

// Pablo the painter asks for a flower every two days (+25 Mage Reputation).
public class AbiphonePablo extends CrimsonContact {
    public static final Duration COOLDOWN = Duration.ofDays(2);

    public AbiphonePablo() {
        super("pablo", "<5>Pablo", "A modest painter in <5>Scarleton<7>.",
                "81a704158fda337dcecf5ec19d6723d17ccae3a310667924f37dd96202edbd0a");
    }

    @Override
    protected String[] refusal(SkyBlockPlayer player) {
        if (CrimsonSideQuests.is(player, CrimsonSideQuests.PABLO_EVER)) return null;
        return new String[]{"I need quiet, please."};
    }

    @Override
    protected void call(SkyBlockPlayer player) {
        if (CrimsonReputationService.isMember(player, CrimsonFaction.BARBARIAN)) {
            noAnswer(player);
            return;
        }
        if (!CrimsonSideQuests.isReady(player, CrimsonSideQuests.PABLO_LAST, COOLDOWN)) {
            reply(player, "I don't need any flowers at the moment!");
            return;
        }
        String flower = CrimsonSideQuests.pabloFlower(player).getDisplayName();
        reply(player, "Hello, <player>!",
                "Are you available? I desperately need a " + flower + " today.",
                "Please meet me at my house if you have one. I would greatly appreciate it!");
    }
}
