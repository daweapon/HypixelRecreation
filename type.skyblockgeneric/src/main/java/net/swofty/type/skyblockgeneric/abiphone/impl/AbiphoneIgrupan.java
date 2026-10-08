package net.swofty.type.skyblockgeneric.abiphone.impl;

import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.faction.ReputationTier;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonContact;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.crimson.questline.MageStep;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Igrupan, once his chickens are home, calls about Chickzilla when the player is Honored with the Mages.
public class AbiphoneIgrupan extends CrimsonContact {
    public AbiphoneIgrupan() {
        super("igrupan", "<5>Igrupan", "Breeds chickens in <5>Scarleton<7>.",
                "5d0c25fd9d32e1204a1d32e5d95a2f1cfa09525a8f98e1deef5b2a3c77f98a8c");
    }

    public static boolean chickensReturned(SkyBlockPlayer player) {
        return CrimsonQuestlines.mage(player).ordinal() > MageStep.HELP_IGRUPAN.ordinal();
    }

    public static boolean canSpawnChickzilla(SkyBlockPlayer player) {
        return chickensReturned(player) && !CrimsonSideQuests.is(player, CrimsonSideQuests.CHICKZILLA_DONE)
                && CrimsonReputationService.isMember(player, CrimsonFaction.MAGE)
                && CrimsonReputationService.tier(player, CrimsonFaction.MAGE).isAtLeast(ReputationTier.HONORED);
    }

    @Override
    protected String[] refusal(SkyBlockPlayer player) {
        if (chickensReturned(player)) return null;
        return new String[]{"I'm busy working on my coop."};
    }

    @Override
    protected void call(SkyBlockPlayer player) {
        if (canSpawnChickzilla(player)) {
            reply(player, "HELP!!!!!", "MY CHICKENS... THEY MUTATED!", "COME TO THE CHICKEN COOP QUICKLY!!!!!");
            return;
        }
        reply(player, "I'll be more careful from now on!");
    }
}
