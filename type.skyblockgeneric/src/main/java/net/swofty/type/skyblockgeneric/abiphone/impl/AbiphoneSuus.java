package net.swofty.type.skyblockgeneric.abiphone.impl;

import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonContact;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

// Suus: calling her names a different Burning Desert mob that ate Turd's toy this time.
public class AbiphoneSuus extends CrimsonContact {
    public AbiphoneSuus() {
        super("suus", "<d>Suus", "Turd's mother in <c>Dragontail<7>.",
                "4afabbae24c1945188f72f8592ae1a35038698e6784558559e2c05f012b33599");
    }

    @Override
    protected String[] refusal(SkyBlockPlayer player) {
        if (CrimsonSideQuests.is(player, CrimsonSideQuests.SUUS_EVER)) return null;
        return new String[]{"Don't talk to strangers, little Turd."};
    }

    @Override
    protected void call(SkyBlockPlayer player) {
        if (!CrimsonReputationService.isMember(player, CrimsonFaction.BARBARIAN)) {
            noAnswer(player);
            return;
        }
        if (!CrimsonSideQuests.is(player, CrimsonSideQuests.SUUS_TOY)) {
            CrimsonSideQuests.set(player, CrimsonSideQuests.SUUS_MOB,
                    ThreadLocalRandom.current().nextInt(CrimsonSideQuests.SUUS_MOBS.size()));
        }
        reply(player, "The monsters ate his toy again! I don't know what to do!",
                "This time it was a <c>" + CrimsonSideQuests.suusMobName(player) + "<f>!",
                "Can you come back here and help?");
    }
}
